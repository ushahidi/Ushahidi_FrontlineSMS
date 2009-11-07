/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.ui;

import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import net.frontlinesms.AppProperties;
import net.frontlinesms.BuildProperties;
import net.frontlinesms.EmailSender;
import net.frontlinesms.EmailServerHandler;
import net.frontlinesms.ErrorUtils;
import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.csv.CsvUtils;
import net.frontlinesms.data.*;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.repository.*;
import net.frontlinesms.listener.EmailListener;
import net.frontlinesms.listener.UIListener;
import net.frontlinesms.plugins.PluginController;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.smsdevice.*;
import net.frontlinesms.smsdevice.internet.SmsInternetService;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.i18n.LanguageBundle;

import org.apache.log4j.Logger;

import org.springframework.transaction.annotation.Transactional;

import thinlet.FrameLauncher;
import thinlet.Thinlet;
import thinlet.ThinletText;

// FIXME should not be using static imports
import static net.frontlinesms.FrontlineSMSConstants.*;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.*;

/**
 * Class containing control methods for the Thinlet-driven GUI.
 * 
 * The public (void) methods in this class are called by reflection via the Thinlet class.
 * 
 * Employed within are a selection of different methods for essentially getting the same
 * thing done, e.g. caching of components at class level vs. searching every time they
 * are to be used.  This is because design methods have changed throughout the development
 * of this class.  Currently searching for components as and when they are needed is
 * favoured, and so this should be done where possible.
 * 
 * We're now in the process of separating this class into smaller classes which control separate,
 * modular parts of the UI, e.g. the {@link HomeTabController}.
 * 
 * @author Alex Anderson 
 * <li> alex(at)masabi(dot)com
 * @author Carlos Eduardo Genz
 * <li> kadu(at)masabi(dot)com
 */
@SuppressWarnings("serial")
public class UiGeneratorController extends FrontlineUI implements EmailListener, UIListener {

//> CONSTANTS
	/** Default height of the Thinlet frame launcher */
	public static final int DEFAULT_HEIGHT = 768;
	/** Default width of the Thinlet frame launcher */
	public static final int DEFAULT_WIDTH = 1024;
	/** Number of milliseconds in a day */
	private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

//> INSTANCE PROPERTIES
	/** Logging object */
	public Logger LOG = Utils.getLogger(UiGeneratorController.class);
	
	/** The INTERNAL NAME of the tab (a thinlet UI component) currently active */
	private String currentTab;
	
	/** Flag indicating whether this instance of UIGeneratorController is using the classic or advanced GUI */
	private final boolean classicMode;
	/** The manager of {@link SmsDevice}s */
	private final SmsDeviceManager phoneManager;
	/** Manager of {@link EmailAccount}s and {@link EmailSender}s */
	private final EmailServerHandler emailManager;
	/** Data Access Object for {@link Contact}s */
	private final ContactDao contactFactory;
	/** Data Access Object for {@link Group}s */
	private final GroupDao groupDao;
	/** Data Access Object for {@link Message}s */
	private final MessageDao messageFactory;
	/** Data Access Object for {@link Keyword}s */
	private final KeywordDao keywordFactory;
	/** Data Access Object for {@link KeywordAction}s */
	private final KeywordActionDao keywordActionFactory;
	/** Data Access Object for {@link SmsModemSettings}s */
	private final SmsModemSettingsDao phoneDetailsManager;
	/** Data Access Object for {@link EmailAccount}s */
	private final EmailAccountDao emailAccountFactory;
	/** Data Access Object for {@link Email}s */
	private final EmailDao emailFactory;
	
	/** Controller of the phones tab. */
	private final PhoneTabController phoneTabController;
	
	/** Fake group: The root group, of which all other top-level groups are children.  The name of this group specified in the constructor will not be used due to overridden {@link Group#getName()}. */
	private final Group rootGroup = new Group(null, "Root Group [i18n]") {
		@Override
		public Collection<Contact> getAllMembers() {
			return contactFactory.getAllContacts();
		}
		@Override
		/** This group can have no direct subgroups. */
		public Collection<Group> getDirectSubGroups() {
			return groupDao.getChildGroups(null);
		}
		@Override
		/** Provide an internationalised version of this group's name */
		public String getName() {
			return InternationalisationUtils.getI18NString(FrontlineSMSConstants.CONTACTS_ALL);
		}
	};
	/** Fake group: all contacts without a name set.  The name of this group specified in the constructor will not be used due to overridden {@link Group#getName()}. */
	private final Group unnamedContacts = new Group(null, "Unnamed [i18n]") {
		@Override
		public Collection<Contact> getAllMembers() {
			return contactFactory.getUnnamedContacts();
		}
		@Override
		/** This group can have no direct subgroups. */
		public Collection<Group> getDirectSubGroups() {
			return Collections.emptySet();
		}
		@Override
		/** Provide an internationalised version of this group's name */
		public String getName() {
			return InternationalisationUtils.getI18NString(FrontlineSMSConstants.CONTACTS_UNNAMED);
		}
	};
	/** Fake group: all contacts not a member of a group.  The name of this group specified in the constructor will not be used due to overridden {@link Group#getName()}. */
	private final Group ungroupedContacts = new Group(null, "Ungrouped [i18n]") {
		@Override
		public Collection<Contact> getAllMembers() {
			return contactFactory.getUngroupedContacts();
		}
		@Override
		/** This group can have no direct subgroups. */
		public Collection<Group> getDirectSubGroups() {
			return Collections.emptySet();
		}
		@Override
		/** Provide an internationalised version of this group's name */
		public String getName() {
			return InternationalisationUtils.getI18NString(FrontlineSMSConstants.CONTACTS_UNGROUPED);
		}
	};

	/** The number of people the current SMS will be sent to
	 * TODO this is a very strange variable to have.  This should be replaced with context-specific tracking of the number of messages to be sent. */
	public int numberToSend = 1;
	
	/** Start date of the message history, or <code>null</code> if none has been set. */
	private Long messageHistoryStart;
	/** End date of the message history, or <code>null</code> if none has been set. */
	private Long messageHistoryEnd;
	
	/**
	 * The UI Component COMPONENT_GROUP_LIST.  This is cached here to save searching
	 * for it later.  Whether this is a desirable feature is uncertain, and the
	 * opposite approach has been taken with COMPONENT_CONTACT_LIST - it must either
	 * be passed to a method, or searched for by the method.
	 */
	private final Object groupListComponent;
	private final Object messageListComponent;
	private final Object showSentMessagesComponent;
	private final Object showReceivedMessagesComponent;
	/** Thinlet UI Component: status bar at the bottom of the window */
	private final Object statusBarComponent;
	private final Object contactListComponent;
	private Object keywordListComponent;
	private Object emailListComponent;
	private final Object progressBarComponent;
	private final Object filterListComponent;

	/** Appears to be the in-focus item on the email tab. */
	private Object emailTabFocusOwner;
	
	/**
	 * Creates a new instance of the UI Controller.
	 * @param frontlineController
	 * @param detectPhones <code>true</code> if phone detection should be started automatically; <code>false</code> otherwise.
	 * @throws Throwable
	 */
	public UiGeneratorController(FrontlineSMS frontlineController, boolean detectPhones) throws Throwable {
		super();
		this.frontlineController = frontlineController;
		
		// Load the requested language file.
		AppProperties appProperties = AppProperties.getInstance();
		String currentLanguageFile = appProperties.getLanguageFilename();
		if (currentLanguageFile != null) {
			LanguageBundle languageBundle = InternationalisationUtils.getLanguageBundle(new File(InternationalisationUtils.getLanguageDirectoryPath() + currentLanguageFile));
			FrontlineUI.currentResourceBundle = languageBundle;
			setResourceBundle(languageBundle.getProperties(), languageBundle.isRightToLeft());
			Font requestedFont = languageBundle.getFont();
			if(requestedFont != null) {
				setFont(new Font(requestedFont.getName(), getFont().getStyle(), getFont().getSize()));
			}
			LOG.debug("Loaded language from file: " + ResourceUtils.getConfigDirectoryPath() + "languages/" + currentLanguageFile);
		}
		
		this.phoneManager = frontlineController.getSmsDeviceManager();
		this.contactFactory = frontlineController.getContactDao();
		this.groupDao = frontlineController.getGroupDao();
		this.messageFactory = frontlineController.getMessageDao();
		this.keywordFactory = frontlineController.getKeywordDao();
		this.keywordActionFactory = frontlineController.getKeywordActionDao();
		this.phoneDetailsManager = frontlineController.getSmsModemSettingsDao();
		this.emailAccountFactory = frontlineController.getEmailAccountFactory();
		this.emailFactory = frontlineController.getEmailDao();
		this.emailManager = frontlineController.getEmailServerManager();
		
		// Load the data mode from the ui.properties file
		UiProperties uiProperties = UiProperties.getInstance();
		this.classicMode = uiProperties.isViewModeClassic();
		LOG.debug("Classic Mode [" + this.classicMode + "]");
		LOG.debug("Detect Phones [" + detectPhones + "]");
		
		try {
			add(loadComponentFromFile(UI_FILE_HOME));
			
			// Find the languages submenu, and add all present language packs to it
			addLanguageMenu(find("menu_language"));
			
			setText(find(COMPONENT_TF_COST_PER_SMS), InternationalisationUtils.formatCurrency(this.getCostPerSms(), false));
			setText(find(COMPONENT_LB_COST_PER_SMS_PREFIX),
					InternationalisationUtils.isCurrencySymbolPrefix() 
							? InternationalisationUtils.getCurrencySymbol()
							: "");
			setText(find(COMPONENT_LB_COST_PER_SMS_SUFFIX),
					InternationalisationUtils.isCurrencySymbolSuffix() 
					? InternationalisationUtils.getCurrencySymbol()
					: "");
			
			Object tabbedPane = find(COMPONENT_TABBED_PANE);
			setBoolean(find("menu_tabs"), VISIBLE, !this.classicMode);
			this.phoneTabController = new PhoneTabController(this);
			if (this.classicMode) {
				add(tabbedPane, loadComponentFromFile(UI_FILE_CONTACT_MANAGER_TAB));
				add(tabbedPane, loadComponentFromFile(UI_FILE_SURVEY_MANAGER_TAB));
				add(tabbedPane, loadComponentFromFile(UI_FILE_SURVEY_ANALYST_TAB));
				addSendTab(tabbedPane);
				add(tabbedPane, loadComponentFromFile(UI_FILE_MESSAGE_TRACKER_TAB));
				add(tabbedPane, loadComponentFromFile(UI_FILE_RECEIVE_CONSOLE_TAB));
				add(tabbedPane, loadComponentFromFile(UI_FILE_REPLY_MANAGER_TAB));
				add(tabbedPane, phoneTabController.getTab());
				setText(find(COMPONENT_MENU_SWITCH_MODE), InternationalisationUtils.getI18NString(MENUITEM_SWITCH_TO_ADVANCED_VIEW));
				currentTab = TAB_GROUP_MANAGER;
			} else {
				if (uiProperties.isTabVisible("hometab")) {
					add(tabbedPane, new HomeTabController(this).getTab());
//					addHomeTab(tabbedPane, uiProperties);
					setSelected(find(COMPONENT_MI_HOME), true);
				}
				addContactsTab(tabbedPane);
				if (uiProperties.isTabVisible("keywordstab")) {
					addKeywordsTab(tabbedPane);
					setSelected(find(COMPONENT_MI_KEYWORD), true);
				}
				addMessagesTab(tabbedPane);
				if (uiProperties.isTabVisible("emailstab")) {
					addEmailsTab(tabbedPane);
					setSelected(find(COMPONENT_MI_EMAIL), true);
				}
				add(tabbedPane, phoneTabController.getTab());
				
				// Add plugins tabs
				for(PluginController controller : frontlineController.getPluginControllers()) {
					add(tabbedPane, controller.getTab(this));					
					//Initialize the data items for the plugin; load them from the DB
					controller.initializePluginData();
				}
				
				currentTab = TAB_HOME;
				setText(find(COMPONENT_MENU_SWITCH_MODE), InternationalisationUtils.getI18NString(MENUITEM_SWITCH_TO_CLASSIC_VIEW));
			}

			statusBarComponent = find(COMPONENT_STATUS_BAR);
			progressBarComponent = find(COMPONENT_PROGRESS_BAR);

			setStatus(InternationalisationUtils.getI18NString(MESSAGE_STARTING));
			groupListComponent = find(COMPONENT_CONTACT_MANAGER_GROUP_TREE);
			messageListComponent = find(COMPONENT_MESSAGE_LIST);
			contactListComponent = find(COMPONENT_CONTACT_MANAGER_CONTACT_LIST);
			filterListComponent = find(COMPONENT_FILTER_LIST);
			
			// Set the types for the message list columns...
			Object header = get(messageListComponent, ThinletText.HEADER);
			initMessageTableForSorting(header);
			
			// Set the types for the email list columns...
			header = get(emailListComponent, ThinletText.HEADER);
			initEmailTableForSorting(header);
			
			showReceivedMessagesComponent = find(COMPONENT_RECEIVED_MESSAGES_TOGGLE);
			showSentMessagesComponent = find(COMPONENT_SENT_MESSAGES_TOGGLE);
			
			if (this.classicMode) {
				deactivate(find(COMPONENT_SURVEY_DETAILS));
				deactivate(find(COMPONENT_REPLY_MANAGER_DETAILS));
				replyManager_enableButtons(false);
			} 
			
			// Try to add the emulator number to the contacts
			try {
				Contact testContact = new Contact(TEST_NUMBER_NAME, EMULATOR_MSISDN, "", "", "", true);
				contactFactory.saveContact(testContact);
			} catch(DuplicateKeyException ex) {
				LOG.debug("Contact already exists", ex);
			}
			
			if(classicMode) {
				classicMode_initListsForPaging();
				updateGroupList();
			} else {
				advancedMode_initListsForPaging();
			}
			// Initialise the phone manager, and start auto-detection of connected phones.
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_INITIALISING_PHONE_MANAGER));

			//Window size				
			Integer width = uiProperties.getWindowWidth();
			if(width == null) width = DEFAULT_WIDTH;
			
			Integer height = uiProperties.getWindowHeight();
			if(height == null) height = DEFAULT_HEIGHT;
			
			final String WINDOW_TITLE = "FrontlineSMS " + BuildProperties.getInstance().getVersion();
			frameLauncher = new FrameLauncher(WINDOW_TITLE, this, width, height, getIcon(Icon.FRONTLINE_ICON));
			if (uiProperties.isWindowStateMaximized()) {
				//Is maximised
				frameLauncher.setExtendedState(Frame.MAXIMIZED_BOTH);
			}
			
			frontlineController.setEmailListener(this);
			frontlineController.setUiListener(this);
			frontlineController.setSmsDeviceEventListener(this.phoneTabController);
			
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_PHONE_MANAGER_INITIALISED));
			
			if (detectPhones) {
				phoneTabController.phoneManager_detectModems();
			}
		} catch(Throwable t) {
			LOG.error("Problem starting User Interface module.", t);
			super.destroy();
			throw t;
		}
	}

	/**
	 * Adds the keywords tab.
	 * @param tabbedPane the pane to add the keywords into
	 */
	private void addKeywordsTab(Object tabbedPane) {
		int index = 2;
		if (find(TAB_HOME) == null) index--;
		add(tabbedPane, loadComponentFromFile(UI_FILE_KEYWORDS_TAB), index);
		keywordListComponent = find(COMPONENT_KEYWORD_LIST);
	}

	/**
	 * @param header
	 */
	private void initEmailTableForSorting(Object header) {
		for (Object o : getItems(header)) {
			String text = getString(o, Thinlet.TEXT);
			// Here, the FIELD property is set on each column of the message table.  These field objects are
			// then used for easy sorting of the message table.
			if (text != null) {
				if (text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_STATUS))) putProperty(o, PROPERTY_FIELD, Email.Field.STATUS);
				else if(text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_DATE))) putProperty(o, PROPERTY_FIELD, Email.Field.DATE);
				else if(text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_SENDER))) putProperty(o, PROPERTY_FIELD, Email.Field.FROM);
				else if(text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_RECIPIENT))) putProperty(o, PROPERTY_FIELD, Email.Field.TO);
				else if(text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_CONTENT))) putProperty(o, PROPERTY_FIELD, Email.Field.EMAIL_CONTENT);
				else if(text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_SUBJECT))) putProperty(o, PROPERTY_FIELD, Email.Field.SUBJECT);
			}
		}
	}

	private void initMessageTableForSorting(Object header) {
		for (Object o : getItems(header)) {
			String text = getString(o, Thinlet.TEXT);
			// Here, the FIELD property is set on each column of the message table.  These field objects are
			// then used for easy sorting of the message table.
			if(text != null) {
				if (text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_STATUS))) putProperty(o, PROPERTY_FIELD, Message.Field.STATUS);
				else if(text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_DATE))) putProperty(o, PROPERTY_FIELD, Message.Field.DATE);
				else if(text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_SENDER))) putProperty(o, PROPERTY_FIELD, Message.Field.SENDER_MSISDN);
				else if(text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_RECIPIENT))) putProperty(o, PROPERTY_FIELD, Message.Field.RECIPIENT_MSISDN);
				else if(text.equalsIgnoreCase(InternationalisationUtils.getI18NString(COMMON_MESSAGE))) putProperty(o, PROPERTY_FIELD, Message.Field.MESSAGE_CONTENT);
			}
		}
	}
	
	/**
	 * Initialises the Classic Mode UI list components to be ready for pagination.
	 */
	private final void classicMode_initListsForPaging() {
		// MESSAGE TRACKER LISTS \\
		Object pendingPanel = find("messageTracker_pendingMessagePanel");
		addPaginationToTable(pendingPanel, "messageTracker_refreshPendingMessageList", true);
		
		Object failedPanel = find("messageTracker_failedMessagePanel");
		addPaginationToTable(failedPanel, "messageTracker_refreshFailedMessageList", true);
		
		Object contactsListPanel = find("groupManager_contactListPanel");
		addPaginationToTable(contactsListPanel, "classicMode_refreshContactManager", true);

		Object analystMessagePanel = find("analystMessagesPanel");
		addPaginationToTable(analystMessagePanel, "surveyAnalystMessagesRefresh", true);
		
		Object analystMessagePanel2 = find("analystMessagesPanel_unregistered");
		addPaginationToTable(analystMessagePanel2, "surveyAnalystMessagesRefresh_unregistered", true);
		
		Object receiveConsolePanel = find("receiveConsolePanel");
		addPaginationToTable(receiveConsolePanel, "updatePages_receiveConsole", true);
		
		Object sendConsole_messageListPanel = find("sendConsole_messageListPanel");
		addPaginationToTable(sendConsole_messageListPanel, "updatePages_sendConsoleMessageList", true);
	}
	
	public void updatePages_sendConsoleMessageList() {
		Object table = find(COMPONENT_SEND_CONSOLE_MESSAGE_LIST);
		List<? extends Message> listContents = getListContents(table, Message.class);
		int count = listContents.size();
		int pageNumber = getListCurrentPage(table);
		int listLimit = getListLimit(table);
		removeAll(table);
		int fromIndex = (pageNumber - 1) * listLimit;
		int toIndex = Math.min(fromIndex + listLimit, count);
		for(Message message : listContents.subList(fromIndex, toIndex)) {
			add(table, sendConsole_getRow(message));
		}
		updatePageNumber(getParent(table), count, pageNumber, listLimit);
	}
	
	public void updatePages_receiveConsole() {
		Object table = find(COMPONENT_RECEIVE_CONSOLE_MESSAGE_LIST);
		List<? extends Message> listContents = getListContents(table, Message.class);
		int count = listContents.size();
		int pageNumber = getListCurrentPage(table);
		int listLimit = getListLimit(table);
		removeAll(table);
		int fromIndex = (pageNumber - 1) * listLimit;
		int toIndex = Math.min(fromIndex + listLimit, count);
		for(Message message : listContents.subList(fromIndex, toIndex)) {
			add(table, receiveConsole_getRow(message));
		}
		updatePageNumber(getParent(table), count, pageNumber, listLimit);
	}

	public void surveyAnalystMessagesRefresh() {
		Object table = find(COMPONENT_ANALYST_MESSAGES);
		List<Message> listContents = getListContents(table, Message.class);
		int count = listContents.size();
		int pageNumber = getListCurrentPage(table);
		int listLimit = getListLimit(table);
		removeAll(table);
		int fromIndex = (pageNumber - 1) * listLimit;
		int toIndex = Math.min(fromIndex + listLimit, count);
		for(Message message : listContents.subList(fromIndex, toIndex)) {
			Contact c = contactFactory.getFromMsisdn(message.getSenderMsisdn());
			String displayName;
			if(c == null) displayName = message.getSenderMsisdn();
			else displayName = c.getDisplayName();
			add(table, getAnalystRow(message, displayName));
		}
		updatePageNumber(getParent(table), count, pageNumber, listLimit);
	}
	public void surveyAnalystMessagesRefresh_unregistered() {
		Object table = find(COMPONENT_ANALYST_MESSAGES_UNREGISTERED);
		List<Message> listContents = getListContents(table, Message.class);
		int count = listContents.size();
		int pageNumber = getListCurrentPage(table);
		int listLimit = getListLimit(table);
		removeAll(table);
		int fromIndex = (pageNumber - 1) * listLimit;
		int toIndex = Math.min(fromIndex + listLimit, count);
		for(Message message : listContents.subList(fromIndex, toIndex)) {
			String displayName = message.getSenderMsisdn();
			add(table, getAnalystRow(message, displayName));
		}
		updatePageNumber(getParent(table), count, pageNumber, listLimit);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Object> List<T> getListContents(Object table, Class<T> clazz) {
		return (List<T>)getProperty(table, "listContents");
	}
	
	private void advancedMode_initListsForPaging() {
		//Entries per page
		setListLimit(contactListComponent);
		setListLimit(messageListComponent);
		setListLimit(filterListComponent);
		//Current page
		setListPageNumber(1, contactListComponent);
		setListPageNumber(1, messageListComponent);
		setListPageNumber(1, filterListComponent);
		//Count
		//setListElementCount(1, messageListComponent);
		setListElementCount(contactFactory.getAllContacts().size(), filterListComponent);
		//Actions
		setMethod(contactListComponent, "updateContactList");
		setMethod(filterListComponent, "updateMessageHistoryFilter");
		setMethod(messageListComponent, "updateMessageList");
	}

	public void setMethod(Object component, String methodName) {
		LOG.trace("ENTER");
		LOG.debug("Method [" + methodName + "]");
		try {
			putProperty(component, PROPERTY_ACTION, getClass().getDeclaredMethod(methodName, new Class[]{}));
		} catch (SecurityException e) {
			LOG.debug("", e);
		} catch (NoSuchMethodException e) {
			LOG.debug("", e);
		}
		LOG.trace("EXIT");
	}
	
	public void setListLimit(Object list) {
		putProperty(list, PROPERTY_ENTRIES_PER_PAGE, RESULTS_PER_PAGE_DEFAULT);
	}

	public void nextPage(Object list, Object panel) {
		int pageNumber = (Integer) getProperty(list, PROPERTY_CURRENT_PAGE);
		putProperty(list, PROPERTY_CURRENT_PAGE, pageNumber + 1);
		executeAction(getMethod(list));
	}
	
	public Method getMethod(Object list) {
		return (Method) getProperty(list, PROPERTY_ACTION);
	}

	public void executeAction(Method method) {
		LOG.trace("ENTER");
		LOG.debug("Invoking method [" + method + "]");
		try {
			method.invoke(this, new Object[] {});
		} catch (IllegalArgumentException e) {
			LOG.debug("", e);
		} catch (IllegalAccessException e) {
			LOG.debug("", e);
		} catch (InvocationTargetException e) {
			LOG.debug("", e);
		}
		LOG.trace("EXIT");
	}

	public void previousPage(Object list, Object panel) {
		int pageNumber = (Integer) getProperty(list, PROPERTY_CURRENT_PAGE);
		putProperty(list, PROPERTY_CURRENT_PAGE, pageNumber - 1);
		executeAction(getMethod(list));
	}
	
	private void addEmailsTab(Object tabbedPane) {
		int index = 4;
		if (find(TAB_HOME) == null) index--;
		if (find(TAB_KEYWORD_MANAGER) == null) index--;
		Object emailTab = loadComponentFromFile(UI_FILE_EMAILS_TAB);
		Object pnEmail = find(emailTab, COMPONENT_PN_EMAIL);
		
		// Add the paging panel
		// TODO this should be done with placeholder + call to addPagination
		Object pagePanel = loadComponentFromFile(UI_FILE_PAGE_PANEL);
		setChoice(pagePanel, HALIGN, RIGHT);
		add(pnEmail, pagePanel, 2);
		setPageMethods(pnEmail, COMPONENT_EMAIL_LIST, pagePanel);

		add(tabbedPane, emailTab, index);
		
		emailListComponent = find(COMPONENT_EMAIL_LIST);
		setListLimit(emailListComponent);
		setListPageNumber(1, emailListComponent);
		setListElementCount(emailFactory.getEmailCount(), emailListComponent);
		setMethod(emailListComponent, "updateEmailList");
	}
	
	private void addMessagesTab(Object tabbedPane) {
		Object messagesTab = loadComponentFromFile(UI_FILE_MESSAGES_TAB);
		Object pnBottom = find(messagesTab, COMPONENT_PN_BOTTOM);
		Object pnFilter = find(messagesTab, COMPONENT_PN_FILTER);
		String listName = COMPONENT_MESSAGE_LIST;
		Object pagePanel = loadComponentFromFile(UI_FILE_PAGE_PANEL);
		add(pnBottom, pagePanel, 0);
		setPageMethods(find(messagesTab, COMPONENT_PN_MESSAGE_LIST), listName, pagePanel);
		pagePanel = loadComponentFromFile(UI_FILE_PAGE_PANEL);
		listName = COMPONENT_FILTER_LIST;
		add(pnFilter, pagePanel);
		setPageMethods(pnFilter, listName, pagePanel);
		
		//Date
		setMethod(find(messagesTab, COMPONENT_TF_START_DATE), "messageHistory_dateChanged");
		setMethod(find(messagesTab, COMPONENT_TF_END_DATE), "messageHistory_dateChanged");
		
		add(tabbedPane, messagesTab);
	}

	/**
	 * Loads the contacts tab into the tabbed pane.
	 * @param tabbedPane the pane to add the tab to
	 */
	private void addContactsTab(Object tabbedPane) {
		Object contactsTab = loadComponentFromFile(UI_FILE_CONTACTS_TAB);
		Object pnContacts = find(contactsTab, COMPONENT_PN_CONTACTS);
		String listName = COMPONENT_CONTACT_MANAGER_CONTACT_LIST;
		Object pagePanel = loadComponentFromFile(UI_FILE_PAGE_PANEL);
		add(pnContacts, pagePanel, 0);
		setPageMethods(pnContacts, listName, pagePanel);
		add(tabbedPane, contactsTab);
	}
	
	/**
	 * Sets up a table for pagination, including adding the page controls, setting the
	 * page-turn methods etc.
	 * @param tableContainer The parent container of the table component to be paginated.
	 * @param listMethod The method to call when the list's page is turned.
	 * @param rightAlign Choose whether the controls are right or left aligned WRT the table
	 */
	private final void addPaginationToTable(Object tableContainer, String listMethod, boolean rightAlign) {
		Object pagePanel = loadComponentFromFile(UI_FILE_PAGE_PANEL);
		Object placeholder = find(tableContainer, "pageControlsPanel");
		int index = getIndex(getParent(placeholder), placeholder);
		if(rightAlign) setChoice(pagePanel, "halign", "right");
		add(getParent(placeholder), pagePanel, index);
		remove(placeholder);
		// Find the table to paginate
		Object table = null;
		for(Object o : getItems(tableContainer)) {
			if(getClass(o).equals("table")) table = o;
		}
		// If we've found the table, apply the pagination controls to it.
		if(table != null) {
			setListLimit(table);
			setListPageNumber(1, table);
			setMethod(table, listMethod);
			String listName = getString(table, "name");
			setPageMethods(tableContainer, listName, pagePanel);
		}
	}

	// FIXME this could be private if it wasn't used in forms tab.  Should probably be abstracted
	public void setPageMethods(Object root, String listName, Object pagePanel) {
		Object btPrev = find(pagePanel, COMPONENT_BT_PREVIOUS_PAGE);
		Object btNext = find(pagePanel, COMPONENT_BT_NEXT_PAGE);
		setMethod(btPrev, ATTRIBUTE_ACTION, "previousPage(" + listName + ",pagePanel)", root, this);
		setMethod(btNext, ATTRIBUTE_ACTION, "nextPage(" + listName + ",pagePanel)", root, this);
	}

	private void addSendTab(Object tabbedPane) {
		Object sendTab = loadComponentFromFile(UI_FILE_SEND_CONSOLE_TAB);
		Object pnSend = find(sendTab, COMPONENT_PN_SEND);
		MessagePanelController messagePanelController = new MessagePanelController(this);
		Object pnMessage = messagePanelController.getPanel();
		add(pnSend, pnMessage, 0);
		messagePanelController.setSendButtonMethod(this, sendTab, "sendConsole_sendSms(tfMessage, sendConsole_loneRecipient, sendConsole_groupTree, sendConsole_modemList)");
		add(tabbedPane, sendTab);
	}

	/**
	 * Show the message details dialog.
	 */
	public void showMessageDetails(Object list) {
		Object selected = getSelectedItem(list);
		if (selected != null) {
			Message message = getMessage(selected);
			Object details = loadComponentFromFile(UI_FILE_MSG_DETAILS_FORM);
			String senderDisplayName = getSenderDisplayValue(message);
			String recipientDisplayName = getRecipientDisplayValue(message);
			String status = getMessageStatusAsString(message);
			String date = InternationalisationUtils.getDatetimeFormat().format(message.getDate());
			String content = message.getTextContent();
			
			setText(find(details, "tfStatus"), status);
			setText(find(details, "tfSender"), senderDisplayName);
			setText(find(details, "tfRecipient"), recipientDisplayName);
			setText(find(details, "tfDate"), date);
			setText(find(details, "tfContent"), content);
			
			add(details);
		}
	}

	/**
	 * Gets the string to display for the recipient of a message.
	 * @param message
	 * @return This will be the name of the contact who received the message, or the recipient's phone number if they are not a contact.
	 */
	private String getRecipientDisplayValue(Message message) {
		Contact recipient = contactFactory.getFromMsisdn(message.getRecipientMsisdn());
		String recipientDisplayName = recipient != null ? recipient.getDisplayName() : message.getRecipientMsisdn();
		return recipientDisplayName;
	}

	/**
	 * Gets the string to display for the sender of a message.
	 * @param message
	 * @return This will be the name of the contact who sent the message, or the sender's phone number if they are not a contact.
	 */
	private String getSenderDisplayValue(Message message) {
		Contact sender = contactFactory.getFromMsisdn(message.getSenderMsisdn());
		String senderDisplayName = sender != null ? sender.getDisplayName() : message.getSenderMsisdn();
		return senderDisplayName;
	}
	
	/** 
	 * Switch mode from CLASSIC to ADVANCED or vice-versa.
	 */
	public void doSwitchMode() {
		LOG.trace("ENTER");
		boolean newMode = !this.classicMode;
		LOG.debug("Mode [" + newMode + "]");
		savePropertiesBeforeChangingMode(newMode);
		reloadUI(false);
		LOG.trace("EXIT");
	}

	/**
	 * Request the modem manager to send an SMS message.
	 * 
	 * @param messageText The message content to be sent.
	 * @param sendConsoleLoneRecipient Checkbox specifying whether to send to a specific recipient rather than a whole group.
	 * @param sendConsoleGroupTree Tree showing all the groups.
	 * @param sendConsoleHandsetList List of attached and active SMS modems.
	 */
	public void sendConsole_sendSms(Object tfMessage, Object sendConsoleLoneRecipient, Object sendConsoleGroupTree, Object sendConsoleHandsetList) {
		LOG.trace("ENTER");
		// We need to work out if the user has requested specific handsets for these messages.
		// If he has, we should send specifically on these handsets, otherwise we should round-
		// robin.
		Object selectedHandset = getSelectedItem(sendConsoleHandsetList);

		String messageText = getText(tfMessage);
		
		LOG.debug("Message [" + messageText + "]");
		
		// If no text has been entered in the relevant field, warn the user rather than sending empty messages.
		if (messageText.length() == 0) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_BLANK_TEXT));
			LOG.trace("EXIT");
			return;
		}
		// Check if the user has selected "Send to Lone Recipient" option.  If this is the case,
		// the message is only to be sent to the number specified in the lone recipient
		// textfield.  Obviously if no number has been specified in this field, then we can't
		// send them a message.  Again, in this case we alert the user to his mistake.
		if (!isEnabled(sendConsoleGroupTree)) {
			LOG.debug("User decided to send a message directly to a number.");
			String toMsisdn = getText(sendConsoleLoneRecipient);
			LOG.debug("Number [" + toMsisdn + "]");
			if (toMsisdn.length() == 0) {
				alert(InternationalisationUtils.getI18NString(MESSAGE_BLANK_PHONE_NUMBER));
				LOG.trace("EXIT");
				return;
			}
			// It's a one-off message to a single recipient!  If any phones have
			// been selected from the list, we should send it from the first one.
			if (selectedHandset == null || getIndex(sendConsoleHandsetList, selectedHandset) == 0) {
				LOG.debug("User didn't select the desired phone, leaving to frontlineSMS deal with it.");
				// Here, we have not chosen a specific phone to send to.  In this case, we just
				// pass it up to the frontline controller to deal with.
				frontlineController.sendTextMessage(toMsisdn, messageText);
			} else {
				LOG.debug("Sending message using phone [" + getDeviceHandler(selectedHandset) + "]");
				// The message is to be sent to the first selected phone on the handset list.  Once
				// we have sent the message, we need to disable the "Lone Recipient" form.
				Message m = Message.createOutgoingMessage(System.currentTimeMillis(), "", toMsisdn, messageText);
				messageFactory.saveMessage(m);
				getDeviceHandler(selectedHandset).sendSMS(m);
				deactivate(sendConsoleLoneRecipient);
			}
		} else {
			// The user has opted to send this message to one or more groups in the Group Tree.
			// We need to determine the Contacts in the UNION of all selected groups, and send
			// the message to these contacts.  If no group has been selected, alert the user to
			// his mistake.
			Object selectedItem = getSelectedItem(sendConsoleGroupTree);
			if (selectedItem == null) {
				alert(InternationalisationUtils.getI18NString(MESSAGE_NO_GROUP_SELECTED));
				LOG.trace("EXIT");
				return;
			}
			LOG.debug("User decided to send a message to groups.");
			Set<Contact> toContacts = new HashSet<Contact>();
			Group toGroup = getGroup(selectedItem);
			LOG.debug("Getting contacts from Group [" + toGroup.getName() + "]");
			boolean hasMembers = toGroup.getAllMembersCount() > 0;
			for (Contact c : toGroup.getAllMembers()) {
				if (c.isActive()) {
					LOG.debug("Adding contact [" + c.getName() + "] to the send list.");
					toContacts.add(c);
				}
			}
			
			if (toContacts.size() == 0 ) {
				LOG.debug("No contacts to send, or selected groups contain only dormants.");
				String key = hasMembers ? MESSAGE_ONLY_DORMANTS : MESSAGE_GROUP_NO_MEMBERS;
				alert(InternationalisationUtils.getI18NString(key));
				LOG.trace("EXIT");
				return;
			}
			
			for (Contact c : toContacts) {
				// It's a one-off message to a single recipient!  If any phones have
				// been selected from the list, we should send it from the first one.
				if (selectedHandset == null || getIndex(sendConsoleHandsetList, selectedHandset) == 0) {
					LOG.debug("User didn't select the desired phone, leaving to frontlineSMS deal with it.");
					// Here, we have not chosen a specific phone to send to.  In this case, we just
					// pass it up to the frontline controller to deal with.
					frontlineController.sendTextMessage(c.getMsisdn(), messageText);
				} else {
					LOG.debug("Sending message using phone [" + getDeviceHandler(selectedHandset) + "]");
					// The message is to be sent to the first selected phone on the handset list.  Once
					// we have sent the message, we need to disable the "Lone Recipient" form.
					Message m = Message.createOutgoingMessage(System.currentTimeMillis(), "", c.getMsisdn(), messageText);
					messageFactory.saveMessage(m);
					getDeviceHandler(selectedHandset).sendSMS(m);
				}
				
			}
		}
		sendConsole_refreshMessageList();
		LOG.trace("EXIT");
	}
	
	public void sendConsole_selectionChanged(Object tree) {
		Object sel = getSelectedItem(tree);
		Group g = getGroup(sel);
		numberToSend = 0;
		TreeSet<Contact> toSend = new TreeSet<Contact>();
		for (Contact c : g.getAllMembers()) {
			if (c.isActive()) {
				toSend.add(c);
			}
		}
		numberToSend = toSend.size();
		updateCost();
	}

	/**
	 * Toggles the lone recipient form's enabled/disabled status on the Send Console.
	 * 
	 * @param text The string captured in the group name text field.
	 * @param groupList The list to be activated or deactivated.
	 */
	public void sendConsole_loneRecipientToggle(String text, Object groupList) {
		if(!text.equals("")) {			
			deactivate(groupList);
		} else {
			activate(groupList);
		}
	}
	
	public void classicMode_groupSelectionChage() {
		// We've changed group, so we need to reset the page number of the
		// group members' list to 1
		Object contactList = find(COMPONENT_GROUP_MANAGER_CONTACT_LIST);
		setListPageNumber(1, contactList);
		// Now run the standard ContactManager refresh
		classicMode_refreshContactManager();
	}
	
	public void classicMode_refreshContactManager() {
		Object groupList = find(COMPONENT_GROUP_MANAGER_GROUP_LIST);
		Object contactList = find(COMPONENT_GROUP_MANAGER_CONTACT_LIST);
		removeAll(contactList);

		int count = 0;
		int pageNumber = getListCurrentPage(contactList);
		int listLimit = getListLimit(contactList);
		
		// To re-populate the group's member list, we must first locate
		// the component.  Once we have done this, we remove all current
		// contents, and add all members of the selected group.
		Group selectedGroup;
		// Set whether or not the "delete group" button should be enabled
		boolean enableDeleteButton = false;
		if(groupList != null) {
			selectedGroup = getGroup(getSelectedItem(groupList));
			
			boolean isRealGroup = selectedGroup != null && !isDefaultGroup(selectedGroup);
			
			if (selectedGroup != null) {
				enableDeleteButton = isRealGroup;
				count = selectedGroup.getAllMembersCount();
				List<? extends Contact> groupMembers = selectedGroup.getAllMembers((pageNumber - 1) * listLimit, listLimit);
				for(Contact c : groupMembers) {
					add(contactList, getRow(c));
				}
				activate(find(COMPONENT_GROUP_MANAGER_CREATE_NEW_CONTACT));
			} else {
				deactivate(find(COMPONENT_GROUP_MANAGER_CREATE_NEW_CONTACT));
			}
			setBoolean(find(COMPONENT_MI_SEND_SMS), VISIBLE, selectedGroup != null);
			setBoolean(find(COMPONENT_MI_DELETE), VISIBLE, isRealGroup);
			setBoolean(find(COMPONENT_MI_NEW_CONTACT), VISIBLE, isRealGroup);
			if (!isRealGroup) {
				deactivate(find(COMPONENT_GROUP_MANAGER_CREATE_NEW_CONTACT));
			}
		} else selectedGroup = null;
		updatePageNumber(getParent(contactList), count, pageNumber, listLimit);
		
		setEnabled(find("btDeleteGroup"), enableDeleteButton);
		
		deactivate(find(COMPONENT_GROUP_MANAGER_DELETE_CONTACTS_BUTTON));
		deactivate(find(COMPONENT_GROUP_MANAGER_SEND_SMS_BUTTON));

		Object contactDetails = find(COMPONENT_GROUP_MANAGER_CONTACT_DETAILS);
		// If we're editing a NEW contact, then we want them to be
		// added to the group selected when we finally click to save
		// them.  Therefore iff we were already editing a new contact
		// we nmust attach the newly-selected group to the contact
		// details form.
		if (getAttachedObject(contactDetails) instanceof Group) {
			setAttachedObject(contactDetails, selectedGroup);
		} else {
			contactDetails_hide(contactDetails);
		}
	}
	
	/**
	 * Checks if the supplied group is a real group, or just one of the default groups
	 * used for visualization.
	 * @param group
	 * @return <code>true</code> if the supplied {@link Group} is one of the synthetic groups; <code>false</code> otherwise. 
	 */
	private boolean isDefaultGroup(Group group) {
		return group == this.rootGroup || group == this.ungroupedContacts || group == this.unnamedContacts;
	}

	/**
	 * Applies a text filter to the contact list and updates the list.
	 * 
	 * @param contactFilter The new filter.
	 */
	public void filterContacts(String contactFilter) {
		// We set the contactFilter variable.  When updateContactList is called, the contactFilter
		// variable will be used to select a subsection of the relevant contacts.
		setListPageNumber(1, contactListComponent);
		
		if (contactFilter.length() == 0) {
			updateContactList();
			return;
		}
		
		removeAll(contactListComponent);
		
		LinkedHashMap<String, Contact> contacts = getContactsFromSelectedGroups(groupListComponent);
		
		Pattern pattern = Pattern.compile("(" + Pattern.quote(contactFilter.toLowerCase()) + ").*");
		for (String key : contacts.keySet()) {
			Contact con = contacts.get(key);
			//FIX 0000501
			for (String names : con.getName().split("\\s")) {
				if (pattern.matcher(names.toLowerCase()).matches()) {
					add(contactListComponent, getRow(con));
					break;
				}
			}
		}
		setListElementCount(1, contactListComponent);
		updatePageNumber(contactListComponent, find(TAB_CONTACT_MANAGER));
	}

	/** 
	 * Refreshes the keyword list on the reply manager. 
	 */
	private void replyManager_refreshKeywordList() {
		Object replyManagerListComponent = find(COMPONENT_REPLY_MANAGER_LIST);
		removeAll(replyManagerListComponent);
		for (KeywordAction action : keywordActionFactory.getReplyActions()) {
			add(replyManagerListComponent, getReplyManagerRow(action));
		}
		replyManager_enableButtons(getSelectedItems(replyManagerListComponent).length > 0);
		setEnabled(find(COMPONENT_REPLY_MANAGER_CREATE_BUTTON), true);
	}

	/** 
	 * In advanced mode, updates the list of keywords in the Keyword Manager.  
	 * <br>Has no effect in classic mode.
	 */
	private void updateKeywordList() {
		int selectedIndex = getSelectedIndex(keywordListComponent);
		removeAll(keywordListComponent);
		Object newKeyword = createListItem(InternationalisationUtils.getI18NString(ACTION_ADD_KEYWORD), null);
		setIcon(newKeyword, Icon.KEYWORD_NEW);
		add(keywordListComponent, newKeyword);
		for(Keyword keyword : keywordFactory.getAllKeywords()) {
			add(keywordListComponent, createListItem(keyword));
		}
		if (selectedIndex >= getItems(keywordListComponent).length || selectedIndex == -1) {
			selectedIndex = 0;
		}
		setSelectedIndex(keywordListComponent, selectedIndex);
		showSelectedKeyword();
	}

	public void keywordShowAdvancedView() {
		Object divider = find(find(TAB_KEYWORD_MANAGER), COMPONENT_KEYWORDS_DIVIDER);
		if (getItems(divider).length >= 2) {
			remove(getItems(divider)[getItems(divider).length - 1]);
		}
		Object panel = loadComponentFromFile(UI_FILE_KEYWORDS_ADVANCED_VIEW);
		Object table = find(panel, COMPONENT_ACTION_LIST);
		Keyword keyword = getKeyword(getSelectedItem(keywordListComponent));
		String key = keyword.getKeywordString().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeywordString();
		setText(panel, InternationalisationUtils.getI18NString(COMMON_KEYWORD_ACTIONS_OF, key));
		for (KeywordAction action : keyword.getActions()) {
			add(table, getRow(action));
		}
		enableKeywordActionFields(table, find(panel, COMPONENT_KEY_ACT_PANEL));
		add(divider, panel);
	}
	
	public void autoReplyChanged(String reply, Object cbAutoReply) {
		setSelected(cbAutoReply, reply.length() > 0);
	}
	
	public void showSelectedKeyword() {
		int index = getSelectedIndex(keywordListComponent);
		Object selected = getSelectedItem(keywordListComponent);
		Object divider = find(find(TAB_KEYWORD_MANAGER), COMPONENT_KEYWORDS_DIVIDER);
		if (getItems(divider).length >= 2) {
			remove(getItems(divider)[getItems(divider).length - 1]);
		}
		if (index == 0) {
			//Add keyword selected
			Object panel = loadComponentFromFile(UI_FILE_KEYWORDS_SIMPLE_VIEW);
			fillGroups(panel);
			Object btSave = find(panel, COMPONENT_BT_SAVE);
			setText(btSave, InternationalisationUtils.getI18NString(ACTION_CREATE));
			setBoolean(find(panel,COMPONENT_PN_TIP), VISIBLE, false);
			add(divider, panel);
		} else if (index > 0) {
			//An existent keyword is selected, let's check if it is simple or advanced.
			Keyword keyword = (Keyword) getAttachedObject(selected);
			Collection<KeywordAction> actions = keyword.getActions();
			boolean simple = actions.size() <= 3;
			if (simple) {
				int previousType = -1;
				for (KeywordAction action : actions) {
					int type = action.getType();
					if (type != KeywordAction.TYPE_REPLY
							&& type != KeywordAction.TYPE_JOIN
							&& type != KeywordAction.TYPE_LEAVE) {
						simple = false;
						break;
					}
					
					if (action.getEndDate() != DEFAULT_END_DATE) {
						simple = false;
						break;
					}
					
					if (type == previousType) {
						simple = false;
						break;
					}
					
					previousType = type;
				}
			}
			if (simple) {
				Object panel = loadComponentFromFile(UI_FILE_KEYWORDS_SIMPLE_VIEW);
				//Fill every field
				fillGroups(panel);
				Object tfKeyword = find(panel, COMPONENT_TF_KEYWORD);
				setEnabled(tfKeyword, false);
				String key = keyword.getKeywordString().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeywordString();
				setText(tfKeyword, key);
				for (KeywordAction action : actions) {
					int type = action.getType();
					if (type == KeywordAction.TYPE_REPLY) {
						Object cbReply = find(panel, COMPONENT_CB_AUTO_REPLY);
						Object tfReply = find(panel, COMPONENT_TF_AUTO_REPLY);
						setSelected(cbReply, true);
						setText(tfReply, action.getUnformattedReplyText());
					} else if (type == KeywordAction.TYPE_JOIN) {
						Object checkboxJoin = find(panel, COMPONENT_CB_JOIN_GROUP);
						Object cbJoinGroup = find(panel, COMPONENT_CB_GROUPS_TO_JOIN);
						for (int i = 0; i < getItems(cbJoinGroup).length; i++) {
							Group g = (Group) getAttachedObject(getItems(cbJoinGroup)[i]);
							if (g.equals(action.getGroup())) {
								setInteger(cbJoinGroup, SELECTED, i);
								break;
							}
						}
						setSelected(checkboxJoin, true);
					} else if (type == KeywordAction.TYPE_LEAVE) {
						Object checkboxLeave = find(panel, COMPONENT_CB_LEAVE_GROUP);
						Object cbLeaveGroup = find(panel, COMPONENT_CB_GROUPS_TO_LEAVE);
						for (int i = 0; i < getItems(cbLeaveGroup).length; i++) {
							Group g = (Group) getAttachedObject(getItems(cbLeaveGroup)[i]);
							if (g.equals(action.getGroup())) {
								setInteger(cbLeaveGroup, SELECTED, i);
								break;
							}
						}
						setSelected(checkboxLeave, true);
					}
				}
				
				setBoolean(find(panel, COMPONENT_BT_CLEAR), VISIBLE, false);
				add(divider, panel);
			} else {
				Object panel = loadComponentFromFile(UI_FILE_KEYWORDS_ADVANCED_VIEW);
				Object table = find(panel, COMPONENT_ACTION_LIST);
				String key = keyword.getKeywordString().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeywordString();
				setText(panel, InternationalisationUtils.getI18NString(COMMON_KEYWORD_ACTIONS_OF, key));
				//Fill every field
				for (KeywordAction action : actions) {
					add(table, getRow(action));
				}
				add(divider, panel);
				enableKeywordActionFields(table, find(panel, COMPONENT_KEY_ACT_PANEL));
			}
		}
		enableKeywordFields(find(find(TAB_KEYWORD_MANAGER), COMPONENT_KEY_PANEL));
	}
	
	
	
	private void fillGroups(Object panel) {
		Object cbJoin = find(panel, COMPONENT_CB_GROUPS_TO_JOIN);
		Object cbLeave = find(panel, COMPONENT_CB_GROUPS_TO_LEAVE);
		Object cbJoinGroup = find(panel, COMPONENT_CB_JOIN_GROUP);
		Object cbLeaveGroup = find(panel, COMPONENT_CB_LEAVE_GROUP);
		List<Group> groups = getAllGroups();
		for (Group g : groups) {
			Object item = createComboBoxChoice(g);
			add(cbJoin, item);
			add(cbLeave, item);
		}
		if (groups.size() == 0) {
			setEnabled(cbJoinGroup, false);
			setEnabled(cbJoin, false);
			setEnabled(cbLeaveGroup , false);
			setEnabled(cbLeave, false);
		} else {
			setSelectedIndex(cbJoin, 0);
			setSelectedIndex(cbLeave, 0);
		}
	}

	private Object createComboBoxChoice(Group g) {
		Object item = createComboboxChoice(g.getName(), g);
		setIcon(item, Icon.GROUP);
		return item;
	}

	private Object createListItem(KeywordAction survey) {
		String start = InternationalisationUtils.getDateFormat().format(new Date(survey.getStartDate()));
		String end = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
		if (survey.getEndDate() != DEFAULT_END_DATE) {
			end = InternationalisationUtils.getDateFormat().format(new Date(survey.getEndDate()));
		} 
		String key = survey.getKeyword().getKeywordString().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : survey.getKeyword().getKeywordString();
		Object item = createListItem(
				key
				+ " (" + start + " - " + end + ")",
				survey);
		setIcon(item, Icon.SURVEY);
		return item;
	}
	
	/**
	 * This method should be called when the current selection in the Survey Manager's survey list
	 * changes. This will update the displayed survey details to those of the newly-selected survey.
	 * @param surveyList
	 * @param surveyDetails
	 */
	public void surveyManager_selectionChanged(Object surveyList, Object surveyDetails) {
		KeywordAction selectedSurvey = getKeywordAction(getSelectedItem(surveyList));
		surveyManager_updateSurveyDetails(surveyDetails, selectedSurvey, isEnabled(surveyDetails));
	}

	/**
	 * Deactivates the surveyDetails pane on the survey manager.
	 * @param surveyList
	 * @param surveyDetails
	 */
	public void editSurveyDetails(Object surveyList, Object surveyDetails) {
		KeywordAction selectedSurvey = getKeywordAction(getSelectedItem(surveyList));
		surveyManager_updateSurveyDetails(surveyDetails, selectedSurvey, true);
		requestFocus(find(surveyDetails, COMPONENT_SURVEY_MANAGER_SURVEY_DESCRIPTION));
	}

	/**
	 * Deactivates the survey edit window.
	 * @param surveyDetails
	 */
	public void cancelEditSurveyDetails(Object surveyDetails) {
		surveyManager_updateSurveyDetails(surveyDetails, getKeywordAction(surveyDetails), false);
	}

	private void addDatePanel(Object dialog) {
		Object datePanel = loadComponentFromFile(UI_FILE_DATE_PANEL);
		//Adds to the end of the panel, before the button
		add(dialog, datePanel, getItems(dialog).length - 2);
	}
	
	/**
	 * Activates the survey edit window and clears its contents so that a new survey can be entered.
	 * @param surveyList
	 * @param surveyDetails
	 */
	public void newSurveyDetails(Object surveyList, Object surveyDetails) {
		surveyManager_updateSurveyDetails(surveyDetails, null, true);
		requestFocus(find(surveyDetails, COMPONENT_SURVEY_MANAGER_SURVEY_KEYWORD));
	}

	/**
	 * Updates the Survey Manager's survey details pane with details of the survey attached to the supplied keyword.
	 * @param surveyDetails The survey details pane UI component.
	 * @param keyword The keyword the survey applies to
	 * @param allowEdit indicates whether the details pane should be enabled for editing
	 */
	private void surveyManager_updateSurveyDetails(Object surveyDetails, KeywordAction act, boolean allowEdit) {
		setAttachedObject(surveyDetails, act);
		Keyword keyword = act == null ? null : act.getKeyword();
		setText(find(surveyDetails, COMPONENT_SURVEY_MANAGER_SURVEY_KEYWORD), keyword == null ? "" : keyword.getKeywordString());
		setText(find(surveyDetails, COMPONENT_SURVEY_MANAGER_SURVEY_DESCRIPTION), keyword == null ? "" : keyword.getDescription());
		
		setText(find(surveyDetails, COMPONENT_TF_START_DATE), keyword == null ? "" : InternationalisationUtils.getDateFormat().format(act.getStartDate()));
		
		Object endDate = find(surveyDetails, COMPONENT_TF_END_DATE);
		String toSet = "";
		if (keyword != null) {
			if (act.getEndDate() == DEFAULT_END_DATE) {
				toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
			} else {
				toSet = InternationalisationUtils.getDateFormat().format(act.getEndDate());
			}
		}
		setText(endDate, toSet);
		
		if (allowEdit) {
			activate(surveyDetails);
			setEnabled(find(surveyDetails, COMPONENT_SURVEY_MANAGER_SURVEY_KEYWORD), keyword == null);
			setEnabled(find(COMPONENT_SURVEY_MANAGER_NEW_BUTTON), false);
			setEnabled(find(COMPONENT_SURVEY_MANAGER_EDIT_BUTTON), false);
			setEnabled(find(COMPONENT_SURVEY_MANAGER_DELETE_BUTTON), false);
		} else {
			deactivate(surveyDetails);
			setEnabled(find(COMPONENT_SURVEY_MANAGER_NEW_BUTTON), true);
			setEnabled(find(COMPONENT_SURVEY_MANAGER_EDIT_BUTTON), keyword != null);
			setEnabled(find(COMPONENT_SURVEY_MANAGER_DELETE_BUTTON), keyword != null);
		}
	}

	/**
	 * Creates a new survey, or update if it already exists.
	 * 
	 * @param surveyDetails
	 * @param live
	 */
	public void surveyManager_saveSurveyDetails(Object surveyDetails) {
		LOG.trace("ENTER");
		KeywordAction action = getKeywordAction(surveyDetails);
		String description = getText(find(surveyDetails, COMPONENT_SURVEY_MANAGER_SURVEY_DESCRIPTION));
		String startDate = getText(find(surveyDetails, COMPONENT_TF_START_DATE));
		String endDate = getText(find(surveyDetails, COMPONENT_TF_END_DATE));
		LOG.debug("Description [" + description + "]");
		LOG.debug("Start Date [" + startDate + "]");
		LOG.debug("End Date [" + endDate + "]");
		if (startDate.equals("")) {
			LOG.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
			startDate = InternationalisationUtils.getDefaultStartDate();
		}
		long start;
		long end;
		try {
			Date ds = InternationalisationUtils.parseDate(startDate); 
			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
				Date de = InternationalisationUtils.parseDate(endDate);
				if (!Utils.validateDates(ds, de)) {
					LOG.debug("Start date is not before the end date");
					alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
					LOG.trace("EXIT");
					return;
				}
				end = de.getTime();
			} else {
				end = DEFAULT_END_DATE;
			}
			start = ds.getTime();
		} catch (ParseException e) {
			LOG.debug("Wrong format for date", e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
			LOG.trace("EXIT");
			return;
		} 
		if (action == null) {
			String newWordString = getText(find(surveyDetails, COMPONENT_SURVEY_MANAGER_SURVEY_KEYWORD));
			Keyword keyword;
			try {
				keyword = createKeywordsHierarchically(newWordString, description);
			} catch (DuplicateKeyException e) {
				alert(InternationalisationUtils.getI18NString(MESSAGE_KEYWORD_EXISTS));
				LOG.trace("EXIT");
				return;
			}
			LOG.debug("Creating survey for keyword [" + newWordString + "]");
			KeywordAction newKeywordAction = KeywordAction.createSurveyAction(keyword, start, end);
			keywordActionFactory.saveKeywordAction(newKeywordAction);
		} else {
			// HAHA they cannae change the keyword!
			Keyword keyword = action.getKeyword();
			LOG.debug("Updating keyword [" + keyword.getKeywordString() + "]");
			keyword.setDescription(description);
			LOG.debug("Updating action [" + action + "]. Setting new values!");
			action.setStartDate(start);
			action.setEndDate(end);
		}
		surveyManager_refresh();
		surveyManager_updateSurveyDetails(surveyDetails, null, false);
		LOG.trace("EXIT");
	}

	/**
	 * Updates the group list in all different tabs.
	 * This method can be called either for classic or advanced mode.
	 */
	void updateGroupList() {
		Object groupListComponent = getGroupTreeComponent();
		
		Object selected = getSelectedItem(groupListComponent);
		
		removeAll(groupListComponent);
		add(groupListComponent, getNode(this.rootGroup, true));

		setSelected(selected, groupListComponent);
		
		if (!classicMode) updateContactList();
	}

	/**
	 * Gets the Thinlet TREE component displaying the tree of {@link Group}s.
	 * @return
	 */
	private Object getGroupTreeComponent() {
		if (this.classicMode) {
			return find(COMPONENT_GROUP_MANAGER_GROUP_LIST);
		} else {
			return this.groupListComponent;
		}
	}
	
	/**
	 * Selects the supplied object. If not found, none is selected.
	 * 
	 * @param selected
	 * @param groupListComponent
	 */
	private void setSelected(Object selected, Object groupListComponent) {
		for (Object o : getItems(groupListComponent)) {
			if (selected != null) {
				if (isAttachment(selected, Group.class) && isAttachment(o, Group.class)) {
					Group sel = getGroup(selected);
					Group oo = getGroup(o);
					if (oo.getName().equals(sel.getName())) {
						setSelected(o, true);
						return;
					}
				}
				if (isAttachment(selected, Contact.class) && isAttachment(o, Contact.class)) {
					Contact sel = getContact(selected);
					Contact oo = getContact(o);
					if (oo.getName().equals(sel.getName())) {
						setSelected(o, true);
						return;
					}
				}
			}
			setSelected(selected, o);
		}
	}

	/**
	 * Update the message list inside the message log tab.
	 * This only works for advanced mode.
	 */
	@SuppressWarnings("unchecked")
	public void updateMessageList() {
		Object messageHistoryTab = find(currentTab);
		Class filterClass = getMessageHistoryFilterType(messageHistoryTab);
		Object filterList;
		if(filterClass == Group.class) {
			filterList = find("messageHistory_groupList");
		} else filterList = filterListComponent;
		Object selectedItem = getSelectedItem(filterList);

		removeAll(messageListComponent);
		int count = 0;
		if (selectedItem == null) {
			//Nothing selected
			setListPageNumber(1, messageListComponent);
			numberToSend = 0;
		} else {
			int messageType;
			boolean showSentMessages = isSelected(showSentMessagesComponent);
			boolean showReceivedMessages = isSelected(showReceivedMessagesComponent);
			if (showSentMessages && showReceivedMessages) { 
				messageType = Message.TYPE_ALL;
			} else if (showSentMessages) {
				messageType = Message.TYPE_OUTBOUND;
			} else messageType = Message.TYPE_RECEIVED;
			Object header = get(messageListComponent, ThinletText.HEADER);
			Object tableColumn = getSelectedItem(header);
			Message.Field field = Message.Field.DATE;
			Order order = Order.DESCENDING;
			if (tableColumn != null) {
				field = (Message.Field) getProperty(tableColumn, PROPERTY_FIELD);
				order = get(tableColumn, ThinletText.SORT).equals(ThinletText.ASCENT) ? Order.ASCENDING : Order.DESCENDING;
			}
			int limit = getListLimit(messageListComponent);
			int pageNumber = getListCurrentPage(messageListComponent);
			//ALL messages
			int selectedIndex = getSelectedIndex(filterList);
			if (selectedIndex == 0) {

				List<Message> allMessages = messageFactory.getAllMessages(messageType, field, order, messageHistoryStart, messageHistoryEnd, (pageNumber - 1) * limit, limit);
				for (Message m : allMessages) {
					add(messageListComponent, getRow(m));
				}
				count = messageFactory.getMessageCount(messageType, messageHistoryStart, messageHistoryEnd);
				numberToSend = messageFactory.getSMSCount(messageHistoryStart, messageHistoryEnd);
			} else {
				if(filterClass == Contact.class) {
					// Contact selected
					Contact c = getContact(selectedItem);
					for (Message m : messageFactory.getMessagesForMsisdn(messageType, c.getMsisdn(), field, order, messageHistoryStart, messageHistoryEnd, (pageNumber - 1) * limit, limit)) {
						add(messageListComponent, getRow(m));
					}
					count = messageFactory.getMessageCountForMsisdn(messageType, c.getMsisdn(), messageHistoryStart, messageHistoryEnd);
					numberToSend = messageFactory.getSMSCountForMsisdn(c.getMsisdn(), messageHistoryStart, messageHistoryEnd);
				} else if(filterClass == Group.class) {
					// A Group was selected
					List<Group> groups = new ArrayList<Group>();
					getGroupsRecursivelyDown(groups, getGroup(selectedItem));
					for (Message m : messageFactory.getMessagesForGroups(messageType, groups, field, order, messageHistoryStart, messageHistoryEnd, (pageNumber - 1) * limit, limit)) {
						add(messageListComponent, getRow(m));
					}
					count = messageFactory.getMessageCountForGroups(messageType, groups, messageHistoryStart, messageHistoryEnd);
					numberToSend = messageFactory.getSMSCountForGroups(groups, messageHistoryStart, messageHistoryEnd);
				} else /* (filterClass == Keyword.class) */ {
					// Keyword Selected
					Keyword k = getKeyword(selectedItem);
					for (Message m : messageFactory.getMessagesForKeyword(messageType, k, field, order, messageHistoryStart, messageHistoryEnd, (pageNumber - 1) * limit, limit)) {
						add(messageListComponent, getRow(m));
					}
					count = messageFactory.getMessageCount(messageType, k, messageHistoryStart, messageHistoryEnd);
					numberToSend = messageFactory.getSMSCountForKeyword(k, messageHistoryStart, messageHistoryEnd);
				}
			}
		}
		setListElementCount(count, messageListComponent);
		updatePageNumber(messageListComponent, getParent(messageListComponent));
		updateMessageHistoryCost();
		setEnabled(messageListComponent, selectedItem != null && getItems(messageListComponent).length > 0);
	}

	/**
	 * Update the message history filter.
	 */
	private void updateMessageHistoryFilter() {
		// Filter List specific stuff can be moved into contacts section.
		removeAll(filterListComponent);
		
		Object allMessages = createListItem(InternationalisationUtils.getI18NString(COMMON_ALL_MESSAGES), null);
		setIcon(allMessages, Icon.SMS_HISTORY);
		add(filterListComponent, allMessages);

		Object groupListComponent = find("messageHistory_groupList");
		Class<?> filterClass = getMessageHistoryFilterType(find(currentTab));
		if (filterClass == Contact.class) {
			//Contacts
			int limit = getListLimit(filterListComponent);
			int pageNumber = getListCurrentPage(filterListComponent);
			setListElementCount(contactFactory.getContactCount(), filterListComponent);
			for (Contact c : contactFactory.getAllContacts((pageNumber - 1) * limit, limit)) {
				add(filterListComponent, createListItem(c));
			}
		} else if (filterClass == Group.class) {
			// Populate GROUPS tree
			removeAll(groupListComponent);
			add(groupListComponent, getNode(this.rootGroup, true));
		} else {
			//Keywords
			setListElementCount(keywordFactory.getTotalKeywordCount(), filterListComponent);
			for (Keyword k : keywordFactory.getAllKeywords()) {
				add(filterListComponent, createListItem(k));
			}
		}
		setBoolean(filterListComponent, VISIBLE, filterClass != Group.class);
		setBoolean(groupListComponent, VISIBLE, filterClass == Group.class);
		// Group tree and contact list doesn't need paging, so hide the paging controls. 
		setBoolean(find(getParent(filterListComponent), "pagePanel"), VISIBLE, filterClass == Contact.class);

		updatePageNumber(filterListComponent, getParent(filterListComponent));
		updateMessageList();
	}
	
	/**
	 * Gets the selected filter type for the message history, i.e. Contact, Group or Keyword.
	 * @return {@link Contact}, {@link Group} or {@link Keyword}, depending which is set for the message filter.
	 */
	private Class<?> getMessageHistoryFilterType(Object messageHistoryTab) {
		if(isSelected(find(messageHistoryTab, COMPONENT_CB_CONTACTS))) return Contact.class;
		else if(isSelected(find(messageHistoryTab, COMPONENT_CB_GROUPS))) return Group.class;
		else return Keyword.class;
	}
	
	/**
	 * Update the email list inside the email log tab.
	 * This only works for advanced mode.
	 */
	public void updateEmailList() {
		Object header = get(emailListComponent, ThinletText.HEADER);
		Object tableColumn = getSelectedItem(header);
		Email.Field field = null;
		Order order = Order.DESCENDING;
		if (tableColumn != null) {
			field = (Email.Field) getProperty(tableColumn, PROPERTY_FIELD);
			order = get(tableColumn, ThinletText.SORT).equals(ThinletText.ASCENT) ? Order.ASCENDING : Order.DESCENDING;
		}
		int limit = getListLimit(emailListComponent);
		int pageNumber = getListCurrentPage(emailListComponent);
		Collection<Email> emails = emailFactory.getEmailsWithLimit(field, order, (pageNumber - 1) * limit, limit);
		updateEmailList(emails, emailListComponent);
	}

	
	public int getListCurrentPage(Object list) {
		int pageNumber = (Integer) getProperty(list, PROPERTY_CURRENT_PAGE);
		return pageNumber;
	}

	public void updatePageNumber(Object list, Object panel) {
		int pageNumber = getListCurrentPage(list);
		int limit = getListLimit(list);
		int count = getListElementCount(list);
		updatePageNumber(panel, count, pageNumber, limit);
	}

	/**
	 * Update the page number displayed in a pagination control.
	 * @param panel Panel containing page controls
	 * @param count Total number of items in list
	 * @param pageNumber The current page number that we are showing
	 * @param limit Maximum visble items in the list
	 */
	private void updatePageNumber(Object panel, int count, int pageNumber, int limit) {
		setText(find(panel, COMPONENT_LB_PAGE_NUMBER), String.valueOf(pageNumber));
		
		// Calculate the total number of pages, making sure there is at least one.
		int pages = count / limit;
		if ((count % limit) != 0) {
			pages++;
		}
		pages = Math.max(pages, 1);
		setText(find(panel, COMPONENT_LB_NUMBER_OF_PAGES), String.valueOf(pages));
		
		setEnabled(find(panel, COMPONENT_BT_PREVIOUS_PAGE), pageNumber > 1);
		setEnabled(find(panel, COMPONENT_BT_NEXT_PAGE), pageNumber != pages);
	}

	public int getListElementCount(Object list) {
		int count = (Integer) getProperty(list, PROPERTY_COUNT);
		return count;
	}
	
	/**
	 * Repopulates the contact list according to the current filter.
	 */
	void updateContactList() {
		// To repopulate the contact list, we must first locate it and remove the current
		// contents.  Once we've done that, work out what should now be displayed in it,
		// and add them all.
		removeAll(contactListComponent);
		// If we have only selected one of the 'system' groups, we need to disable the
		// delete button - it's not possible to delete the root group, and the other 2
		// special groups.
		Group group = getGroup(getSelectedItem(groupListComponent));
		
		if (group != null) {
			int limit = getListLimit(contactListComponent);
			int pageNumber = getListCurrentPage(contactListComponent);
			List<? extends Contact> contacts = group.getAllMembers((pageNumber - 1) * limit, limit);

			int count = group.getAllMembersCount();
			setListElementCount(count, contactListComponent);

			for (Contact con : contacts) {
				add(contactListComponent, getRow(con));
			}
			updatePageNumber(contactListComponent, find(TAB_CONTACT_MANAGER));
			enabledButtonsAfterSelection(contactListComponent);
		}
	}

	private void getGroupsRecursivelyUp(List<Group> groups, Group g) {
		groups.add(g);
		Group parent = g.getParent();
		if (!parent.equals(this.rootGroup)) {
			getGroupsRecursivelyUp(groups, parent);
		}
	}
	
	private void getGroupsRecursivelyDown(List<Group> groups, Group g) {
		groups.add(g);
		for (Group subGroup : g.getDirectSubGroups()) {
			getGroupsRecursivelyDown(groups, subGroup);
		}
	}
	
	/**
	 * Disables editing of the contact details pane. 
	 * This method affects only classic mode view.
	 * 
	 * @param contactDetails
	 */
	public void discardContactDetails(Object contactDetails) {
		contactDetails_hide(contactDetails);
		activate(find("groupManager_contactListPanel"));
		activate(find("groupManager_groupList"));
		activate(find("groupManager_toolbar"));
	}

	/**
	 * Updates or create a contact with the details added by the user. <br>
	 * This method is used by advanced mode, and also Contact Merge
	 * TODO this method should be transactional
	 * @param contactDetailsDialog
	 */
	public void saveContactDetailsAdvancedView(Object contactDetailsDialog) {
		LOG.trace("ENTER");
		Object attachment = getAttachedObject(contactDetailsDialog);
		Contact contact = null;
		if (attachment != null) {
			contact = (Contact)attachment;
			LOG.debug("Attachment is a contact [" + contact.getName() + "]");
		}
		String name = contactDetails_getName(contactDetailsDialog);
		String msisdn = contactDetails_getMobileMsisdn(contactDetailsDialog);
		String otherMsisdn = contactDetails_getOtherMsisdn(contactDetailsDialog);
		String emailAddress = contactDetails_getEmailAddress(contactDetailsDialog);
		String notes = contactDetails_getNotes(contactDetailsDialog);
		boolean isActive = contactDetails_getActive(contactDetailsDialog);
		
		try {
			if (name.equals("")) name = InternationalisationUtils.getI18NString(UNKNOWN_NAME);
			if (contact == null) {
				LOG.debug("Creating a new contact [" + name + ", " + msisdn + "]");
				contact = new Contact(name, msisdn, otherMsisdn, emailAddress, notes, isActive);
				contactFactory.saveContact(contact);
			} else {
				// If this is not a new contact, we still need to update all details
				// that would otherwise be set by the constructor called in the block
				// above.
				LOG.debug("Editing contact [" + contact.getName() + "]. Setting new values!");
				contact.setMsisdn(msisdn);
				contact.setName(name);
				contact.setOtherMsisdn(otherMsisdn);
				contact.setEmailAddress(emailAddress);
				contact.setNotes(notes);
				contact.setActive(isActive);
				this.contactFactory.updateContact(contact);
			}

			Object groupsList = find(contactDetailsDialog, COMPONENT_CONTACT_GROUP);
			HashSet<Group> selectedGroups = new HashSet<Group>();
			for (Object o : getSelectedItems(groupsList)) {
				if (isAttachment(o, Group.class)) {
					selectedGroups.add(getGroup(o));
				}
			}
			
			// Remove the contact from any groups he has left
			for (Group g : contact.getGroups()) {
				if(!selectedGroups.contains(g)) {
					g.removeContact(contact);
					this.groupDao.updateGroup(g);
				}
			}
			
			// Add this contact from any groups they were added to
			for (Group g : selectedGroups) {
				LOG.debug("Adding contact to group [" + g.getName() + "]");
				if(g.addContact(contact)) {
					this.groupDao.updateGroup(g);
				}
			}

			// Refresh the Contacts tab, and make sure that the group and contact who were previously selected are still selected
			updateGroupList();
		} catch(DuplicateKeyException ex) {
			LOG.debug("There is already a contact with this mobile number - cannot save!", ex);
			showMergeContactDialog(contact, contactDetailsDialog);
		} finally {
			removeDialog(contactDetailsDialog);
		}
		LOG.trace("EXIT");
	}
	


	/**
	 * Update the contact details regarding its groups. The supplied list contains all groups
	 * the contact must be in, after this update. Sometimes we need to remove contact from
	 * other groups.
	 * 
	 * @param contact The person to be update.
	 * @param groups The list of groups the contact must be in.
	 * @param removeFromOldOnes 
	 * @return 
	 */
	public List<Group> updateGroupsForContact(Contact contact, List<Group> groups, boolean removeFromOldOnes) {
		LinkedList<Group> toAdd = new LinkedList<Group>(); //List of groups from where the contact must be added.
		LinkedList<Group> toRemove = new LinkedList<Group>(); //List of groups from where the contact must be removed.
		Collection<Group> actualList = contact.getGroups(); //current groups
		LinkedList<Group> ret = new LinkedList<Group>();
		for (Group update : groups) {
			if (!isDefaultGroup(update)) {
				boolean found = false;
				for (Group actual : actualList) {
					if (update.equals(actual)) {
						found = true;
						break;
					}
				}
				if (!found) {
					toAdd.add(update);
				} else {
					toRemove.add(update);
				}
			}
		}

		if (removeFromOldOnes) {
			for (Group toBeRemoved : toRemove) {
				actualList.remove(toBeRemoved);
			}

			for (Group g : actualList) {
				if(g.removeContact(contact)) {
					this.groupDao.updateGroup(g);
				}
				ret.add(g);
			}
		}

		for (Group a : toAdd) {
			if(a.addContact(contact)) {
				this.groupDao.updateGroup(a);
			}
			ret.add(a);
		}
		return ret;
	}
	
	/**
	 * Saves the new or edited details found in the contact details pane.
	 * <br><b>This method only affects the classic mode.</b>
	 * 
	 * @param contactDetails
	 */
	public void saveContactDetails(Object contactDetails) {
		LOG.trace("ENTER");
		Object attachment = getAttachedObject(contactDetails);
		Contact contact = null;
		Group group = null;
		if (attachment instanceof Contact) {
			contact = (Contact)attachment;
			LOG.debug("Attachment is a contact [" + contact.getName() + "]");
		} else if (attachment instanceof Group) {
			group = (Group)attachment;
			LOG.debug("Attachment is a group [" + group.getName() + "]");
		}

		// If the user is entering a new Contact, there will be no Contact object
		// attached to this component.  In this case, we will need to create a contact
		// and add it to the contacts set.
		String name = contactDetails_getName(contactDetails);
		String msisdn = contactDetails_getMobileMsisdn(contactDetails);
		String otherMsisdn = contactDetails_getOtherMsisdn(contactDetails);
		String emailAddress = contactDetails_getEmailAddress(contactDetails);
		String notes = contactDetails_getNotes(contactDetails);
		boolean isActive = contactDetails_getActive(contactDetails);
		
		if (msisdn.equals("")) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_PHONE_BLANK));
			LOG.trace("EXIT");
			return;
		}
		
		try {
			if (name.equals("")) {
				name = InternationalisationUtils.getI18NString(UNKNOWN_NAME);
			}
			if (contact == null) {
				LOG.debug("Creating a new contact [" + msisdn + "]");
				LOG.debug("Contact name [" + name + "]");
				contact = new Contact(name, msisdn, otherMsisdn, emailAddress, notes, isActive);
				contactFactory.saveContact(contact);
				group.addContact(contact);
				this.groupDao.updateGroup(group);
				updateGroup(group, getNodeForGroup(getGroupTreeComponent(), group));
				Object contactList = find(COMPONENT_GROUP_MANAGER_CONTACT_LIST);
				add(contactList, getRow(contact));
				
				activate(find("groupManager_contactListPanel"));
				activate(find("groupManager_groupList"));
				activate(find("groupManager_toolbar"));
			} else {
				// If this is not a new contact, we still need to update all details
				// that would otherwise be set by the constructor called in the block
				// above.
				LOG.debug("Editing contact [" + contact.getName() + "]. Setting new values!");
				contact.setMsisdn(msisdn);
				contact.setName(name);
				contact.setOtherMsisdn(otherMsisdn);
				contact.setEmailAddress(emailAddress);
				contact.setNotes(notes);
				contact.setActive(isActive);
				Object contactList = find(COMPONENT_GROUP_MANAGER_CONTACT_LIST);
				int index = -1;
				for (Object o : getItems(contactList)) {
					Contact c = getContact(o);
					if (c.equals(contact)) {
						index = getIndex(contactList, o);
						remove(o);
						break;
					}
				}
				add(contactList, getRow(contact), index);
			}
			updateGroup(this.rootGroup, getNodeForGroup(getGroupTreeComponent(), this.rootGroup));
			updateGroup(this.unnamedContacts, getNodeForGroup(getGroupTreeComponent(), this.unnamedContacts));
			updateGroup(this.ungroupedContacts, getNodeForGroup(getGroupTreeComponent(), this.ungroupedContacts));
			contactDetails_hide(contactDetails);
		} catch(DuplicateKeyException ex) {
			LOG.debug("There is already a contact with this mobile number - cannot save!", ex);
			showMergeContactDialog(contact, contactDetails);
		}
		LOG.trace("EXIT");
	}

	/**
	 * Get the current state of the active component.
	 * 
	 * @param contactDetails
	 * @return 
	 */
	private boolean contactDetails_getActive(Object contactDetails) {
		return isSelected(find(contactDetails, COMPONENT_RADIO_BUTTON_ACTIVE));
	}

	/**
	 * Set the current state of the active/dormant component.
	 * 
	 * @param contactDetails
	 * @param active
	 */
	private void contactDetails_setActive(Object contactDetails, boolean active) {
		setSelected(find(contactDetails, COMPONENT_RADIO_BUTTON_ACTIVE), active);
		setSelected(find(contactDetails, COMPONENT_CONTACT_DORMANT), !active);
		if (active) {
			setIcon(find(contactDetails, COMPONENT_LABEL_STATUS), Icon.ACTIVE);
		} else {
			setIcon(find(contactDetails, COMPONENT_LABEL_STATUS), Icon.DORMANT);
		}
	}

	/**
	 * Enables the create new contact details panel, to allow the user to inform the 
	 * contact information. This method affects the classic mode.
	 * 
	 * @param groupList
	 * @param contactDetails
	 */
	public void groupManager_createNewContact(Object groupList, Object contactDetails) {
		contactDetails_show(contactDetails, null, getGroup(getSelectedItem(groupList)));
	}

	/**
	 * Shows the new contact dialog. This method affects the advanced mode.
	 */
	public void showNewContactDialog(Object tree) {
		Object createDialog = loadComponentFromFile(UI_FILE_CREATE_CONTACT_FORM);
		Object list = find(createDialog, COMPONENT_NEW_CONTACT_GROUP_LIST);
		Group sel = getGroup(getSelectedItem(tree));
		List<Group> allGroups = getAllGroups();
		for (Group g : allGroups) {
			Object item = createListItem(g.getName(), g);
			setIcon(item, Icon.GROUP);
			setSelected(item, g.equals(sel));
			add(list, item);
		}
		add(createDialog);
	}
	/**
	 * Shows the message history for the selected contact or group.
	 * @param component group list or contact list
	 */
	public void showMessageHistory(Object component) {
		Object attachment = getAttachedObject(getSelectedItem(component));
		
		boolean isGroup = attachment instanceof Group;
		boolean isContact = attachment instanceof Contact;
		boolean isKeyword = attachment instanceof Keyword;

		changeTab(TAB_MESSAGE_HISTORY);
		
		// Select the correct radio option
		setSelected(find("cbContacts"), isContact);
		setSelected(find("cbGroups"), isGroup);
		setSelected(find("cbKeywords"), isKeyword);
		messageHistory_filterChanged();
		
		Object list = null;
		// Calculate page number
		if(isGroup) {
			// Turn the page to the correct one for this group.
			// FIXME what if the results per page is changed for the history table?
			list = find("messageHistory_groupList");
//			pageNumber = groupFactory.getPageNumber((Group)attachment, getListLimit(list));
		} else if(isContact) {
			list = filterListComponent;
			int pageNumber = contactFactory.getPageNumber((Contact)attachment, getListLimit(list));
			setListPageNumber(pageNumber, list);
		} else if(isKeyword) {
			list = filterListComponent;
			int pageNumber = keywordFactory.getPageNumber((Keyword)attachment, getListLimit(list));
			setListPageNumber(pageNumber, list);
		}
		updateMessageHistoryFilter();
		
		// Find which list item should be selected
		boolean recurse = TREE.equals(getClass(list));
		Object next = getNextItem(list, get(list, ":comp"), recurse);
		while(next != null && !getAttachedObject(next).equals(attachment)) {
			next = getNextItem(list, next, recurse);
		}
		// Little fix for groups - it seems that getNextItem doesn't return the root of the
		// tree, so we never get a positive match.
		if(next == null) next = getItem(list, 0);
		setSelectedItem(list, next);
		messageHistory_selectionChanged();
	}
	
	
	public void classicMode_refreshMessageHistory() {
		Object table = find(COMPONENT_HISTORY_MESSAGE_LIST);
		List<Message> listContents = getListContents(table, Message.class);
		int count = listContents.size();
		int pageNumber = getListCurrentPage(table);
		int listLimit = getListLimit(table);
		removeAll(table);
		int fromIndex = (pageNumber - 1) * listLimit;
		int toIndex = Math.min(fromIndex + listLimit, count);
		for(Message message : listContents.subList(fromIndex, toIndex)) {
			add(table, getRow(message));
		}
		updatePageNumber(getParent(table), count, pageNumber, listLimit);
	}
	
	public void classicMode_showMessageHistory(Object component) {
		Contact contact = getContact(getSelectedItem(component));
		Object messageHistoryDialog = loadComponentFromFile(UI_FILE_HISTORY_FORM);
		setAttachedObject(messageHistoryDialog, contact);
		setText(messageHistoryDialog, InternationalisationUtils.getI18NString(COMMON_MESSAGE_HISTORY_OF, contact.getName()));
		add(messageHistoryDialog);

		addPaginationToTable(find("pnClassicHistory"), "classicMode_refreshMessageHistory", true);

		Object historyList = find(messageHistoryDialog, COMPONENT_HISTORY_MESSAGE_LIST);
		Object header = get(historyList, ThinletText.HEADER);
		initMessageTableForSorting(header);

		classicMode_sortMessageHistory(null);
	}

	
	public void classicMode_sortMessageHistory(Object toggle) {
		System.out.println("UiGeneratorController.classicMode_updateMessageHistory()");
		Object messageHistoryDialog = find("historyDialog");
		Object historyList = find(messageHistoryDialog, COMPONENT_HISTORY_MESSAGE_LIST);
		
		Contact contact = getContact(messageHistoryDialog);
		
		Object header = get(historyList, ThinletText.HEADER);
		System.out.println("header: " + header);
		Object tableColumn = getSelectedItem(header);
		System.out.println("tableColumn: " + tableColumn);
		Message.Field field = null;
		Order order = Order.DESCENDING;
		
		if(tableColumn != null) {
			field = (Message.Field)getProperty(tableColumn, PROPERTY_FIELD);
			order = get(tableColumn, ThinletText.SORT).equals(ThinletText.ASCENT) ? Order.ASCENDING : Order.DESCENDING;
		}
		System.out.println("field: " + field);

		Object sentToggle = find(messageHistoryDialog, "historySentMessagesToggle");
		Object receiveToggle = find(messageHistoryDialog, "historyReceivedMessagesToggle");
		boolean showSent = isSelected(sentToggle);
		boolean showReceived = isSelected(receiveToggle);
		if(!showSent && !showReceived) {
			// They've deselected both!  We should reselect the one they didn't just toggle
			// as otherwise there is nothing to display :D
			if(toggle == sentToggle) {
				setSelected(receiveToggle, true);
			} else setSelected(sentToggle, true);
		}
		int type;
		if(!showSent) type = Message.TYPE_RECEIVED;
		else if(!showReceived) type = Message.TYPE_OUTBOUND;
		else type = Message.TYPE_ALL;
		
		List<? extends Message> messages = messageFactory.getMessagesForMsisdn(
				type,
				contact.getMsisdn(), 
				field, 
				order, 
				messageHistoryStart, 
				messageHistoryEnd);
		setListContents(historyList, messages);
		setListPageNumber(1, historyList);
		setListElementCount(messages.size(), historyList);
		
		System.out.println("UiGeneratorController.classicMode_showMessageHistory()");
		System.out.println("\tSet element count: " + messages.size());
		
		classicMode_refreshMessageHistory();
	}
	

	/**
	 * Shows contact dialog to allow edition of the selected contact.
	 * <br>This method affects the advanced mode.
	 * @param list
	 */
	public void showContactDetails(Object list) {
		Object selected = getSelectedItem(list);
		if (isAttachment(selected, Contact.class)) {
			showNewContactDialog(getContact(selected));
		}
	}

	/**
	 * Show the form to allow merging between a previously-created contact, and
	 * an attempted-newly-created contact.
	 * 
	 * TODO if we work out a good-looking way of doing this, we should implement it.  Currently this just warns the user that a contact with this number already exists.
	 */
	private void showMergeContactDialog(Contact oldContact, Object createContactForm) {
		if(true) alert(InternationalisationUtils.getI18NString(MESSAGE_EXISTENT_CONTACT));
		else {
			// FIXME attach the old form, so we can always put it back on the screen if we choose!
			Object mergeForm = loadComponentFromFile("/ui/dialog/mergeContactDialog.xml");
			setAttachedObject(mergeForm, oldContact);
			
			setText(find(mergeForm, "contact_mobileMsisdn"), oldContact.getMsisdn());
			mergeDialog_setValues(createContactForm, mergeForm, "contact_name", oldContact.getName());
			mergeDialog_setValues(createContactForm, mergeForm, "contact_otherMsisdn", oldContact.getOtherMsisdn());
			mergeDialog_setValues(createContactForm, mergeForm, "contact_emailAddress", oldContact.getEmailAddress());
			mergeDialog_setValues(createContactForm, mergeForm, "contact_notes", oldContact.getNotes());
			
			add(mergeForm);
		}
	}
	
	private void mergeDialog_setValues(Object createContactForm, Object mergeForm, String componentName, String oldText) {
		Object oldContactComponent = find(createContactForm, componentName);
		Object newContactComponent = find(mergeForm, componentName);
		Object newOldContactComponent = find(mergeForm, "old_" + componentName);
		String newText = getText(oldContactComponent);
		newText = newText.trim();
		oldText = oldText.trim();
		if("".equals(newText) || "".equals(oldText) || oldText.equals(newText)) {
			remove(newOldContactComponent);
			setColspan(newContactComponent, 3);
			if("".equals(newText)) newText = oldText;
		} else {
			setText(newOldContactComponent, oldText);
		}
		setText(newContactComponent, newText);
	}

	/**
	 * Shows the contact dialog. If the contact is null, then all fields are blank since
	 * it's a new contact. Otherwise we set the fields with the contact details, leaving it
	 * for editing.
	 * <br>This method affects the advanced mode.
	 * @param list
	 */
	public void showNewContactDialog(Contact contact) {
		Object createDialog = loadComponentFromFile(UI_FILE_CREATE_CONTACT_FORM);
		Object list = find(createDialog, COMPONENT_NEW_CONTACT_GROUP_LIST);
		setAttachedObject(createDialog, contact);
		List<Group> allGroups = getAllGroups();
		for (Group g : allGroups) {
			Object item = createListItem(g.getName(), g);
			setIcon(item, Icon.GROUP);
			add(list, item);
		}
		if (contact != null) {
			String name = "";
			if (!contact.getName().equals(InternationalisationUtils.getI18NString(UNKNOWN_NAME))) {
				name = contact.getName();
			}
			contactDetails_setName(createDialog, name);
			contactDetails_setMobileMsisdn(createDialog, contact.getMsisdn());
			contactDetails_setOtherMsisdn(createDialog, contact.getOtherMsisdn());
			contactDetails_setEmailAddress(createDialog, contact.getEmailAddress());
			contactDetails_setNotes(createDialog, contact.getNotes());
			contactDetails_setActive(createDialog, contact.isActive());
			
			for (Group g : contact.getGroups()) {
				for (Object item : getItems(list)) {
					if (isAttachment(item, Group.class) && getGroup(item).equals(g)) {
						setSelected(item, true);
						break;
					}
				}
			}
		}
		add(createDialog);
	}

	/**
	 * Enables or disables the buttons on the Contacts tab (advanced mode).
	 * 
	 * @param contactList
	 */
	public void enabledButtonsAfterSelection(Object contactList) {
		boolean enabled = getSelectedItems(contactList).length > 0;
		setEnabled(find(COMPONENT_DELETE_NEW_CONTACT), enabled);
		setEnabled(find(COMPONENT_VIEW_CONTACT_BUTTON), enabled);
		setEnabled(find(COMPONENT_SEND_SMS_BUTTON), enabled);
	}

	/**
	 * Adds selected contacts to group.
	 * 
	 * @param item The item holding the destination group.
	 * @param type 
	 * <li> 0 to get selected items from contact list in the advanced view
	 * <li> 1 to get selected items from contact list in the classic view
	 */
	public void addToGroup(Object item, int type) {
		LOG.trace("ENTER");
		LOG.debug("Type [" + type + "]");
		Object[] selected = null;
		if (type == 0) {
			selected = getSelectedItems(contactListComponent);
		} else {
			selected = getSelectedItems(find(COMPONENT_GROUP_MANAGER_CONTACT_LIST));
		}
		// Add to the selected groups...
		Group destination = getGroup(item);
		// Let's check all the selected items.  Any that are groups should be added to!
		for (Object component : selected) {
			if (isAttachment(component, Contact.class)) {
				Contact contact = getContact(component);
				LOG.debug("Adding Contact [" + contact.getName() + "] to [" + destination + "]");
				if(destination.addContact(contact)) {
					groupDao.updateGroup(destination);
				}
			}
		}
		updateGroupsTree(destination);
		LOG.trace("EXIT");
	}

	/**
	 * Shows the delete option dialog, which asks the user if he/she wants to remove
	 * the selected contacts from database.
	 * 
	 * @param list
	 */
	public void showDeleteOptionDialog(Object list) {
		LOG.trace("ENTER");
		Object selected = getSelectedItem(list);
		if (selected != null) {
			Group g = getGroup(selected);
			if (!isDefaultGroup(g)) {
				Object deleteDialog = loadComponentFromFile(UI_FILE_DELETE_OPTION_DIALOG_FORM);
				add(deleteDialog);
			}
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * Remove selected groups and contacts.
	 * 
	 * @param button
	 * @param dialog
	 */
	public void removeSelectedFromGroupList(final Object button, Object dialog) {
		LOG.trace("ENTER");
		setStatus(InternationalisationUtils.getI18NString(MESSAGE_REMOVING_GROUPS_AND_CONTACTS));
		final Object[] selected;
		if (!classicMode) {
			selected = getSelectedItems(groupListComponent);
		} else {
			selected = getSelectedItems(find(COMPONENT_GROUP_MANAGER_GROUP_LIST));
		}
		initProgress();
		setProgressMax(selected.length);
		if (dialog != null) {
			removeDialog(dialog);
		}
		LOG.debug("Starting thread to remove groups and contacts...");
		new Thread(){
			public void run() {
				for (Object o : selected) {
					incProgress();
					Group group = getGroupFromSelectedNode(o);
					if(!isDefaultGroup(group)) {
						boolean removeContactsAlso = false;
						if (button != null) {
							removeContactsAlso = getString(button, Thinlet.NAME).equals(COMPONENT_BUTTON_YES);
						}
						LOG.debug("Selected Group [" + group.getName() + "]");
						LOG.debug("Remove Contacts from database [" + removeContactsAlso + "]");
						if (!isDefaultGroup(group)) {
							//Inside a default group
							LOG.debug("Removing group [" + group.getName() + "] from database");
							groupDao.deleteGroup(group, removeContactsAlso);
						} else {
							if (removeContactsAlso) {
								LOG.debug("Group not destroyable, removing contacts...");
								for (Contact c : group.getDirectMembers()) {
									LOG.debug("Removing contact [" + c.getName() + "] from database");
									contactFactory.deleteContact(c);
								}
							}
						}
						remove(o);
					}
				}
				if (classicMode) {
					updateGroupList();
					classicMode_refreshContactManager();
				} else {
					Object sms = find(getParent(groupListComponent), "sendSMSButtonGroupSide");
					setEnabled(sms, getSelectedItems(groupListComponent).length > 0);
					
					removeAll(contactListComponent);
					setListPageNumber(1, contactListComponent);
					setListElementCount(1, contactListComponent);
				}
				finishProgress(InternationalisationUtils.getI18NString(MESSAGE_GROUPS_AND_CONTACTS_DELETED));
			}
		}.start();
		LOG.trace("EXIT");
	}

	/**
	 * Removes selected keyword.
	 */
	public void removeSelectedFromKeywordList() {
		removeConfirmationDialog();
		Object selected = getSelectedItem(keywordListComponent);
		Keyword key = (Keyword) getAttachedObject(selected);
		keywordFactory.deleteKeyword(key);
		remove(selected);
		setSelectedIndex(keywordListComponent, 0);
		showSelectedKeyword();
	}

	/**
	 * Removes selected keyword action.
	 */
	public void removeSelectedFromKeywordActionsList() {
		removeConfirmationDialog();
		Object list = find(COMPONENT_ACTION_LIST);
		Object selectedKey = getSelectedItem(keywordListComponent);
		Keyword key = (Keyword) getAttachedObject(selectedKey);
		Object selected = getSelectedItem(list);
		KeywordAction keyAction = (KeywordAction) getAttachedObject(selected);
		key.removeAction(keyAction);
		remove(selected);
		enableKeywordActionFields(list, find(COMPONENT_KEY_ACT_PANEL));
	}
	
	public void sendConsole_removeMessages() {
		removeSelectedFromMessageList(find(COMPONENT_SEND_CONSOLE_MESSAGE_LIST));
	}
	
	public void messageTracker_removeMessages() {
		removeSelectedFromMessageList(find(COMPONENT_MESSAGE_TRACKER_FAILED_MESSAGE_LIST));
	}
	
	public void receiveConsole_removeMessages() {
		removeSelectedFromMessageList(find(COMPONENT_RECEIVE_CONSOLE_MESSAGE_LIST));
	}
	
	public void messagesTab_removeMessages() {
		LOG.trace("ENTER");
		removeConfirmationDialog();
		setStatus(InternationalisationUtils.getI18NString(MESSAGE_REMOVING_MESSAGES));
		initProgress();
		final Object[] selected = getSelectedItems(messageListComponent);
		setProgressMax(selected.length);
		LOG.debug("Starting thread to remove messages...");
		new Thread(){
			public void run() {
				int numberRemoved = 0;
				for(Object o : selected) {
					incProgress();
					Message toBeRemoved = getMessage(o);
					LOG.debug("Message [" + toBeRemoved + "]");
					int status = toBeRemoved.getStatus();
					if (status != Message.STATUS_PENDING) {
						LOG.debug("Removing Message [" + toBeRemoved + "] from database.");
						if (status == Message.STATUS_OUTBOX) {
							phoneManager.removeFromOutbox(toBeRemoved);
						}
						numberToSend -= toBeRemoved.getNumberOfSMS();
						messageFactory.deleteMessage(toBeRemoved);
						numberRemoved++;
					} else {
						LOG.debug("Message status is [" + toBeRemoved.getStatus() + "], so we do not remove!");
					}
				}
				updatePageAfterDeletion(numberRemoved, messageListComponent);
				if (numberRemoved > 0) {
					updateMessageList();
				}
				finishProgress(InternationalisationUtils.getI18NString(MESSAGE_MESSAGES_DELETED));
			}
		}.start();
		LOG.trace("EXIT");
	}
	
	/**
	 * Removes the selected messages and updates the list with the supplied page number afterwards.
	 * 
	 * @param pageNumber
	 * @param resultsPerPage
	 * @param object
	 */
	public void removeSelectedFromMessageList(Object object) {
		LOG.trace("ENTER");
		removeConfirmationDialog();
		setStatus(InternationalisationUtils.getI18NString(MESSAGE_REMOVING_MESSAGES));
		initProgress();
		final Object[] selected = getSelectedItems(object);
		setProgressMax(selected.length);
		LOG.debug("Starting thread to remove messages...");
		new Thread(){
			public void run() {
				for(Object o : selected) {
					incProgress();
					Message toBeRemoved = getMessage(o);
					LOG.debug("Message [" + toBeRemoved + "]");
					int status = toBeRemoved.getStatus();
					if (status != Message.STATUS_PENDING) {
						LOG.debug("Removing Message [" + toBeRemoved + "] from database.");
						if (status == Message.STATUS_OUTBOX) {
							phoneManager.removeFromOutbox(toBeRemoved);
						}
						messageFactory.deleteMessage(toBeRemoved);
					} else {
						LOG.debug("Message status is [" + toBeRemoved.getStatus() + "], so we do not remove!");
					}
				}
				refreshMessageLists();
				finishProgress(InternationalisationUtils.getI18NString(MESSAGE_MESSAGES_DELETED));
			}
		}.start();
		LOG.trace("EXIT");
	}

	/**
	 * Removes the selected emails
	 */
	public void removeSelectedFromEmailList() {
		LOG.trace("ENTER");
		removeConfirmationDialog();
		setStatus(InternationalisationUtils.getI18NString(MESSAGE_REMOVING_EMAILS));
		initProgress();
		final Object[] selected = getSelectedItems(emailListComponent);
		setProgressMax(selected.length);
		LOG.debug("Starting thread to remove e-mails...");
		new Thread(){
			public void run() {
				int numberRemoved = 0;
				for (Object o : selected) {
					incProgress();
					Email toBeRemoved = getEmail(o);
					LOG.debug("E-mail [" + toBeRemoved + "]");
					int status = toBeRemoved.getStatus();
					if (status != Email.STATUS_PENDING 
							&& status != Email.STATUS_RETRYING) {
						LOG.debug("Removing from database..");
						if (status == Email.STATUS_OUTBOX) {
							emailManager.removeFromOutbox(toBeRemoved);
						}
						emailFactory.deleteEmail(toBeRemoved);
						numberRemoved++;
					} else {
						LOG.debug("E-mail status is [" + toBeRemoved.getStatus() + "]. Do not remove...");
					}
				}
				updatePageAfterDeletion(numberRemoved, emailListComponent);
				if (numberRemoved > 0) {
					updateEmailList();
				}
				finishProgress(InternationalisationUtils.getI18NString(MESSAGE_EMAILS_DELETED));
			}
		}.start();
		LOG.trace("EXIT");
	}
	
	public void setListElementCount(int count, Object list) {
		putProperty(list, PROPERTY_COUNT, count);
	}

	public void setListPageNumber(int page, Object list) {
		putProperty(list, PROPERTY_CURRENT_PAGE, page);
	}
	
	/**
	 * Enables or disables menu options in a List Component's popup list
	 * and toolbar.  These enablements are based on whether any items in
	 * the list are selected, and if they are, on the nature of these
	 * items.
	 * @param list 
	 * @param popup 
	 * @param toolbar 
	 */
	public void enableOptions(Object list, Object popup, Object toolbar) {
		Object[] selectedItems = getSelectedItems(list);
		boolean hasSelection = selectedItems.length > 0;
		
		if (popup != null) {
			// If nothing is selected, hide the popup menu
			setBoolean(popup, Thinlet.VISIBLE, hasSelection);
			
			Object att = getAttachedObject(selectedItems[0]);
			// If we are looking at a list of messages, there are certain popup menu items that
			// should or shouldn't be enabled, depending on the type of messages we have selected.
			if (hasSelection && att instanceof Message) {
				/** Check to see whether there are any "received messages" selected */
				boolean receivedMessagesSelected = false;
				for(Object selectedComponent : selectedItems) {
					Object attachment = getAttachedObject(selectedComponent);
					if(attachment instanceof Message && ((Message)attachment).getType()==Message.TYPE_RECEIVED) {
						receivedMessagesSelected = true;
					}
				}
				
				for (Object popupMenuItem : getItems(popup)) {
					String popupMenuItemName = getName(popupMenuItem);
					boolean visible = hasSelection;
					if(popupMenuItemName.equals("miReply")) {
						visible = receivedMessagesSelected;
					}
					setVisible(popupMenuItem, visible);
				}
			}
		}
		
		if (toolbar != null && !toolbar.equals(popup)) {
			for (Object o : getItems(toolbar)) {
				setEnabled(o, hasSelection);
			}
		}
	}
	
	/**
	 * Re-Sends the selected messages and updates the list with the supplied page number afterwards.
	 * 
	 * @param pageNumber
	 * @param resultsPerPage
	 * @param object
	 */
	public void resendSelectedFromMessageList(Object object) {
		Object[] selected = getSelectedItems(object);
		for (Object o : selected) {
			Message toBeReSent = getMessage(o);
			int status = toBeReSent.getStatus();
			if (status == Message.STATUS_FAILED) {
				toBeReSent.setSenderMsisdn("");
				toBeReSent.setRetriesRemaining(Message.MAX_RETRIES);
				phoneManager.sendSMS(toBeReSent);
			} else if (status == Message.STATUS_DELIVERED || status == Message.STATUS_SENT) {
				frontlineController.sendTextMessage(toBeReSent.getRecipientMsisdn(), toBeReSent.getTextContent());
			}
		}
	}
	
	/**
	 * Re-Sends the selected emails
	 */
	public void resendSelectedFromEmailList() {
		Object[] selected = getSelectedItems(emailListComponent);
		for (Object o : selected) {
			Email toBeReSent = getEmail(o);
			int status = toBeReSent.getStatus();
			if (status == Email.STATUS_FAILED) {
				emailManager.sendEmail(toBeReSent);
			} else if (status == Email.STATUS_SENT ) {
				Email newEmail = new Email(toBeReSent.getEmailFrom(), toBeReSent.getEmailRecipients(), toBeReSent.getEmailSubject(), toBeReSent.getEmailContent());
				emailFactory.saveEmail(newEmail);
				emailManager.sendEmail(newEmail);
			}
		}
	}

	public int getListLimit(Object list) {
		int limit = (Integer) getProperty(list, PROPERTY_ENTRIES_PER_PAGE);
		return limit;
	}
	
	/**
	 * Removes the selected accounts.
	 */
	public void removeSelectedFromAccountList() {
		LOG.trace("ENTER");
		removeConfirmationDialog();
		Object list = find(COMPONENT_ACCOUNTS_LIST);
		Object[] selected = getSelectedItems(list);
		for (Object o : selected) {
			EmailAccount acc = (EmailAccount) getAttachedObject(o);
			LOG.debug("Removing Account [" + acc.getAccountName() + "]");
			emailManager.serverRemoved(acc);
			emailAccountFactory.deleteEmailAccount(acc);
			remove(o);
		}
		LOG.trace("EXIT");
	}
	
	/** If the confirmation dialog exists, remove it */
	public void removeConfirmationDialog() {
		Object confirm = find(COMPONENT_CONFIRM_DIALOG);
		if (confirm != null) removeDialog(confirm);
	}

	/**
	 * Removes the selected accounts.
	 */
	public void removeSelectedFromSmsHttpServicesList() {
		LOG.trace("ENTER");
		removeConfirmationDialog();
		Object list = find(COMPONENT_ACCOUNTS_LIST);
		Object[] selected = getSelectedItems(list);
		for (Object o : selected) {
			SmsInternetService service = (SmsInternetService) getAttachedObject(o);
			LOG.debug("Removing Account [" + SmsInternetServiceSettingsHandler.getProviderName(service.getClass()) + " - " + service.getIdentifier() + "]");
			phoneManager.removeSmsInternetService(service);
			// FIXME delete this service from the database
			remove(o);
		}
		refreshPhonesViews();
		LOG.trace("EXIT");
	}
	
	/**
	 * Sets the progress bar maximum value.
	 * 
	 * @param max The desired maximum value.
	 */
	private synchronized void setProgressMax(int max) {
		setInteger(progressBarComponent, Thinlet.MAXIMUM, max);
	}

	/**
	 * Increments the progress bar value.
	 */
	private synchronized void incProgress() {
		setInteger(progressBarComponent, Thinlet.VALUE, getInteger(progressBarComponent, Thinlet.VALUE) + 1);
	}

	/**
	 * Sets the progress bar to invisible and set the status bar with the supplied message.
	 * 
	 * @param status
	 */
	private synchronized void finishProgress(String status) {
		setStatus(status);
		setBoolean(progressBarComponent, Thinlet.VISIBLE, false);
	}

	/**
	 * Initialises the progress bar and make it visible.
	 */
	private synchronized void initProgress() {
		setBoolean(progressBarComponent, Thinlet.VISIBLE, true);
		setInteger(progressBarComponent, Thinlet.VALUE, 0);
		setInteger(progressBarComponent, Thinlet.MINIMUM, 0);
	}

	/**
	 * Creates a new group with the supplied name.
	 * 
	 * @param newGroupName The desired group name.
	 * @param dialog the dialog holding the information to where we should create this new group.
	 */
	public void createNewGroup(String newGroupName, Object dialog) {
		// The selected parent group should be attached to this dialog.  Get it,
		// create the new group, update the group list and then remove the dialog.
		Group selectedParentGroup = getGroup(dialog);
		doGroupCreation(newGroupName, dialog, selectedParentGroup);		
	}

	/**
	 * Creates a group with the supplied name and inside the supplied parent .
	 * 
	 * @param newGroupName The desired group name.
	 * @param dialog The dialog to be removed after the operation.
	 * @param selectedParentGroup
	 */
	private void doGroupCreation(String newGroupName, Object dialog, Group selectedParentGroup) {
		LOG.trace("ENTER");
		if(LOG.isDebugEnabled()) {
			String parentGroupName = selectedParentGroup == null ? "null" : selectedParentGroup.getName();
			LOG.debug("Parent group [" + parentGroupName + "]");
		}
		if(selectedParentGroup == rootGroup) {
			selectedParentGroup = null;
		}
		if (selectedParentGroup == this.unnamedContacts || selectedParentGroup == this.ungroupedContacts) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_IMPOSSIBLE_TO_CREATE_A_GROUP_HERE));
			if (dialog != null) remove(dialog);
			return;
		}
		LOG.debug("Group Name [" + newGroupName + "]");
		try {
			if(LOG.isDebugEnabled()) LOG.debug("Creating group with name: " + newGroupName + " and parent: " + selectedParentGroup);
			
			Group g = new Group(selectedParentGroup, newGroupName);
			groupDao.saveGroup(g);
			
			// Now we've saved the group, add it to the groups tree displayed in the contacts manager
			Object groupListComponent = getGroupTreeComponent();
			Object parentNode = getNodeForGroup(groupListComponent, selectedParentGroup);
			add(parentNode, getNode(g, true));
			
			if (dialog != null) remove(dialog);
			LOG.debug("Group created successfully!");
		} catch (DuplicateKeyException e) {
			LOG.debug(InternationalisationUtils.getI18NString(MESSAGE_GROUP_ALREADY_EXISTS), e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_GROUP_ALREADY_EXISTS));
		}
		LOG.trace("EXIT");
	}

	/**
	 * Gets the node we are currently displaying for a group.
	 * @param component
	 * @param group
	 * @return
	 */
	private Object getNodeForGroup(Object component, Group group) {
		if(group == null) {
			group = rootGroup;
		}
		Object ret = null;
		for (Object o : getItems(component)) {
			Group g = getGroup(o);
			if (g.equals(group)) {
				ret = o;
				break;
			} else {
				ret = getNodeForGroup(o, group);
				if (ret != null) break;
			}
		}
		return ret;
	}
	
	/** Shows a general dialog asking the user to confirm his action. */
	public void showConfirmationDialog(String methodToBeCalled){
		showConfirmationDialog(methodToBeCalled, this);
	}
	
	/** Shows a general dialog asking the user to confirm his action. */
	public void showConfirmationDialog(String methodToBeCalled, Object handler){
		Object conf = loadComponentFromFile(UI_FILE_CONFIRMATION_DIALOG_FORM);
		setMethod(find(conf, COMPONENT_BT_CONTINUE), ATTRIBUTE_ACTION, methodToBeCalled, conf, handler);
		add(conf);
	}
	
	/** Shows a general dialog asking the user to confirm his action. */
	public void showConfirmationDialog(String methodToBeCalled, Object handler, String confirmationMessageKey) {
		Object conf = loadComponentFromFile(UI_FILE_CONFIRMATION_DIALOG_FORM);
		setMethod(find(conf, COMPONENT_BT_CONTINUE), ATTRIBUTE_ACTION, methodToBeCalled, conf, handler);
		setText(find(conf, "lbText"), InternationalisationUtils.getI18NString(confirmationMessageKey));
		add(conf);
	}
	
	/**
	 * Shows the export wizard dialog, according to the supplied type.
	 * @param list The list to get selected items from.
	 * @param type The desired type (0 for Contacts, 1 for Messages and 2 for Keywords) // FIXME these types should be more clearly defined
	 */
	public void showExportWizard(Object list, String type){
		new ImportExportUiController(this, this.contactFactory, this.messageFactory, this.keywordFactory).showWizard(true, list, type);
	}
	
	/**
	 * Shows the export wizard dialog, according to the supplied type.
	 * @param type The desired type (0 for Contacts, 1 for Messages and 2 for Keywords) // FIXME these types should be more clearly defined
	 */
	public void showExportWizard(String type){
		new ImportExportUiController(this, this.contactFactory, this.messageFactory, this.keywordFactory).showWizard(true, type);
	}
	
	/**
	 * Shows the import wizard dialog, according to the supplied type.
	 * @param list The list to get selected items from.
	 * @param type The desired type (0 for Contacts, 1 for Messages and 2 for Keywords) // FIXME these types should be more clearly defined
	 */
	public void showImportWizard(Object list, String type){
		new ImportExportUiController(this, this.contactFactory, this.messageFactory, this.keywordFactory).showWizard(false, list, type);
	}
	
	/**
	 * Shows the import wizard dialog, according to the supplied type.
	 * @param type The desired type (0 for Contacts, 1 for Messages and 2 for Keywords) // FIXME these types should be more clearly defined
	 */
	public void showImportWizard(String type){
		new ImportExportUiController(this, this.contactFactory, this.messageFactory, this.keywordFactory).showWizard(false, type);
	}
	
	/**
	 * Shows the new group dialog.
	 * 
	 * @param groupList
	 */
	public void showNewGroupDialog(Object groupList) {
		Object newGroupForm = loadComponentFromFile(UI_FILE_NEW_GROUP_FORM);
		setAttachedObject(newGroupForm, getGroupFromSelectedNode(getSelectedItem(groupList)));
		add(newGroupForm);

	}

	/**
	 * Shows the pending message dialog.
	 * 
	 * @param messages
	 */
	private void showPendingMessages(Collection<Message> messages) {
		Object pendingMsgForm = loadComponentFromFile(UI_FILE_PENDING_MESSAGES_FORM);
		Object list = find(pendingMsgForm, COMPONENT_PENDING_LIST);
		for (Message m : messages) {
			add(list, getRowForPending(m));
		}
		add(pendingMsgForm);
	}

	/**
	 * Remove the selected items from the supplied list.
	 * 
	 * @param recipientList
	 */
	public void removeSelectedFromRecipientList(Object recipientList, Object dialog) {
		for (Object selected : getSelectedItems(recipientList)) {
			numberToSend--;
			remove(selected);
		}
	}

	/**
	 * Clear the messages tables from survey analyst tab.
	 */
	private void cleanSurveyAnalystMessages() {
		Object fromRegistered = find(COMPONENT_ANALYST_MESSAGES);
		Object fromUnregistered = find(COMPONENT_ANALYST_MESSAGES_UNREGISTERED);
		removeAll(fromRegistered);
		removeAll(fromUnregistered);
	}

	/**
	 * Populates the pop up menu with all groups create by users.
	 * 
	 * @param popUp
	 * @param list
	 */
	public void populateGroups(Object popUp, Object list) {
		Object[] selectedItems = getSelectedItems(list);
		setBoolean(popUp, VISIBLE, getSelectedItems(list).length > 0);
		if (selectedItems.length == 0) {
			//Nothing selected
			boolean none = true;
			for (Object o : getItems(popUp)) {
				if (getString(o, NAME).equals(COMPONENT_NEW_GROUP)
						|| getString(o, NAME).equals("miNewContact")) {
					setVisible(o, true);
					none = false;
				} else {
					setVisible(o, false);
				}
			}
			
			setBoolean(popUp, VISIBLE, !none);
		} else if (getAttachedObject(selectedItems[0]) instanceof Contact) {
			for (Object o : getItems(popUp)) {
				String name = getString(o, NAME);
				if (name.equals(COMPONENT_MENU_ITEM_MSG_HISTORY) 
						|| name.equals(COMPONENT_MENU_ITEM_VIEW_CONTACT)) {
					setVisible(o, getSelectedItems(list).length == 1);
				} else if (!name.equals(COMPONENT_GROUPS_MENU)) {
					setVisible(o, true);
				}
			}
			Object menu = find(popUp, COMPONENT_GROUPS_MENU);
			removeAll(menu);
			List<Group> allGroups = getAllGroups();
			for (Group g : allGroups) {
				Object menuItem = Thinlet.create(Thinlet.MENUITEM);
				setText(menuItem, InternationalisationUtils.getI18NString(COMMON_GROUP) + "'" + g.getName() + "'");
				setIcon(menuItem, Icon.GROUP);
				setAttachedObject(menuItem, g);
				if (list.equals(contactListComponent)) {
					setMethod(menuItem, Thinlet.ATTRIBUTE_ACTION, "addToGroup(this, 0)", menu, this);
				} else {
					setMethod(menuItem, Thinlet.ATTRIBUTE_ACTION, "addToGroup(this, 1)", menu, this);
				}
				add(menu, menuItem);
			}
			setBoolean(menu, VISIBLE, allGroups.size() != 0);
			String menuName = InternationalisationUtils.getI18NString(ACTION_ADD_TO_GROUP);
			setText(menu, menuName);
			
			Object menuRemove = find(popUp, "groupsMenuRemove");
			if (menuRemove != null) {
				Contact c = getContact(getSelectedItem(list));
				removeAll(menuRemove);
				Collection<Group> groups = c.getGroups();
				for (Group g : groups) {
					Object menuItem = Thinlet.create(Thinlet.MENUITEM);
					setText(menuItem, InternationalisationUtils.getI18NString(COMMON_GROUP) + "'" + g.getName() + "'");
					setIcon(menuItem, Icon.GROUP);
					setAttachedObject(menuItem, g);
					setMethod(menuItem, Thinlet.ATTRIBUTE_ACTION, "removeFromGroup(this)", menuRemove, this);
					add(menuRemove, menuItem);
				}
				setBoolean(menuRemove, VISIBLE, groups.size() != 0);
			}
		} else {
			Group g = getGroup(getSelectedItem(list));
			//GROUPS OR BOTH
			for (Object o : getItems(popUp)) {
				String name = getString(o, NAME);
				if (COMPONENT_NEW_GROUP.equals(name) 
						|| COMPONENT_MI_SEND_SMS.equals(name)
						|| COMPONENT_MI_DELETE.equals(name)
						|| COMPONENT_MENU_ITEM_MSG_HISTORY.equals(name)
						|| "miNewContact".equals(name)) {
					setVisible(o, true);
				} else {
					setVisible(o, false);
				}
				if (COMPONENT_MI_DELETE.equals(name)) {
					setVisible(o, !isDefaultGroup(g));
				}
				
				if (COMPONENT_NEW_GROUP.equals(name)) {
					setVisible(o, g!=this.unnamedContacts && g!=this.ungroupedContacts);
				}
			}
		}
	}

	/**
	 * Removes the contacts selected in the contacts list from the group which is selected in the groups tree.
	 * @param selectedGroup A set of thinlet components with group members attached to them.
	 */
	public void removeFromGroup(Object selectedGroup) {
		Group g = getGroup(selectedGroup);
		int total = getItems(contactListComponent).length;
		Contact c = getContact(getSelectedItem(contactListComponent));
		if(g.removeContact(c)) {
			this.groupDao.updateGroup(g);
		}
		remove(getSelectedItem(contactListComponent));
		updateContactListAfterDeletion(new Object[] {c}, total);
		updateGroupsTree(g);
	}

	/**
	 * Updates the groups tree on the contacts tab.  This method should be called when the membership
	 * of a group has changed.  The method will attempt to only update {@link Group}s whose memebership
	 * may have changed.
	 * @param g The group whose membership has changed.
	 */
	private void updateGroupsTree(Group g) {
		Object groupTreeComponent = getGroupTreeComponent();

		updateGroup(this.rootGroup, getNodeForGroup(groupTreeComponent, this.rootGroup));
		updateGroup(this.ungroupedContacts, getNodeForGroup(groupTreeComponent, this.ungroupedContacts));
		updateGroup(this.unnamedContacts, getNodeForGroup(groupTreeComponent, this.unnamedContacts));
		updateGroup(g, getNodeForGroup(groupTreeComponent, g));
	}

	/**
	 * Updates the message lists on <b>Survey Analyst</b> tab.
	 * 
	 * @param analystKeywordList
	 */
	public void updateAnalystMessageLists(Object analystKeywordList) {
		KeywordAction survey = getKeywordAction(getSelectedItem(analystKeywordList));
		setEnabled(find("surveyAnalyst_exportButton"), survey != null);
		Object fromRegistered = find(COMPONENT_ANALYST_MESSAGES);
		Object fromUnregistered = find(COMPONENT_ANALYST_MESSAGES_UNREGISTERED);
		List<Message> messages = new ArrayList<Message>();
		List<Message> messages_unregistered = new ArrayList<Message>();
		cleanSurveyAnalystMessages();
		for (Message m : messageFactory.getMessagesForAction(survey)) {
			Contact sender = contactFactory.getFromMsisdn(m.getSenderMsisdn());
			if (sender != null) {
				messages.add(m);
			} else {
				messages_unregistered.add(m);
			}
		}
		setListContents(fromRegistered, messages);
		setListContents(fromUnregistered, messages_unregistered);

		surveyAnalystMessagesRefresh();
		surveyAnalystMessagesRefresh_unregistered();
	}
	
	public void setListContents(Object table, List<? extends Object> listContents) {
		putProperty(table, "listContents", listContents);		
	}
	
	/**
	 * Method invoked when the group/contacts tree selection changes. 
	 * <br>This method updated the contact list according to the new selection.
	 * 
	 * @param tree
	 */
	public void selectionChanged(Object tree, Object panel) {
		LOG.trace("ENTER");
		setText(find(COMPONENT_CONTACT_MANAGER_CONTACT_FILTER), "");
		setListPageNumber(1, contactListComponent);
		//FIX Mantis entry 0000499
		Group g = getGroup(getSelectedItem(tree));
		String toSet = InternationalisationUtils.getI18NString(COMMON_CONTACTS_IN_GROUP, g.getName());
		setText(panel, toSet);
		
		Object deleteButton = find(getParent(tree), "deleteButton");
		setEnabled(deleteButton, !isDefaultGroup(g));
		
		Object sms = find(getParent(tree), "sendSMSButtonGroupSide");
		setEnabled(sms, g != null);
		
		updateContactList();
		LOG.trace("EXIT");
	}

	/**
	 * Gets all the selected contacts to show in the contact list.
	 * 
	 * @param tree
	 * @return
	 */
	private LinkedHashMap<String, Contact> getContactsFromSelectedGroups(Object tree) {
		LinkedHashMap<String, Contact> toBeShown = new LinkedHashMap<String, Contact>();
		if (isSelected(getItems(tree)[0])) {
			//Root group selected
			//Show everyone
			for (Contact c : contactFactory.getAllContacts()) {
				toBeShown.put(c.getMsisdn(), c);
			}
		} else {
			for (Object o : getSelectedItems(tree)) {
				for(Contact c : getGroup(o).getAllMembers()) {
					toBeShown.put(c.getMsisdn(), c);
				}
			}
		}
		
		return toBeShown;
	}

	public void messageHistory_enableSend(Object popUp, boolean isKeyword) {
		boolean toSet = getSelectedIndex(filterListComponent) > 0;
		toSet = toSet && !isKeyword;
		setBoolean(popUp, VISIBLE, toSet);
	}
	
	/**
	 * Shows the compose message dialog, populating the list with the selection of the 
	 * supplied list.
	 * 
	 * @param list
	 */
	public void show_composeMessageForm(Object list) {
		LOG.trace("ENTER");
		// Build up a list of selected recipients, and then pass this to
		// the message composition form.
		Set<Object> recipients = new HashSet<Object>();
		boolean hasMembers = false;
		for (Object selectedComponent : getSelectedItems(list)) {
			Object attachedItem = getAttachedObject(selectedComponent);
			
			if(attachedItem == null) {
				/** skip null items TODO is this necessary with instanceof */
			} else if (attachedItem instanceof Contact) {
				Contact contact = (Contact)attachedItem;
				LOG.debug("Adding contact [" + contact.getName() + "] to the send list.");
				recipients.add(getContact(selectedComponent));
			} else if (attachedItem instanceof Group) {
				LOG.debug("Getting contacts from Group [" + getGroup(selectedComponent).getName() + "]");
				Group group = (Group)attachedItem;
				for (Contact c : group.getAllMembers()) {
					hasMembers = true;
					if (c.isActive()) {
						LOG.debug("Adding contact [" + c.getName() + "] to the send list.");
						recipients.add(c);
					}
				}
			} else if (attachedItem instanceof Message) {
				Message message = (Message) attachedItem;
				// We should only attempt to reply to messages we have received - otherwise
				// we could end up texting ourselves a lot!
				if(message.getType() == Message.TYPE_RECEIVED) {
					String senderMsisdn = message.getSenderMsisdn();
					Contact contact = contactFactory.getFromMsisdn(senderMsisdn);
					if(contact != null) {
						recipients.add(contact);
					} else {
						recipients.add(senderMsisdn);
					}
				}
			}
		}
		
		if (recipients.size() == 0) {
			LOG.debug("No contacts to send, or selected groups contain only dormants.");
			String key = hasMembers ? MESSAGE_ONLY_DORMANTS : MESSAGE_GROUP_NO_MEMBERS;
			alert(InternationalisationUtils.getI18NString(key));
			LOG.trace("EXIT");
			return;
		}
		
		numberToSend = recipients.size();
		
		Object dialog = loadComponentFromFile(UI_FILE_COMPOSE_MESSAGE_FORM);
		Object to = find(dialog, COMPONENT_COMPOSE_MESSAGE_RECIPIENT_LIST);

		for(Object recipient : recipients) {
			if(recipient instanceof Contact) {
				add(to, createListItem((Contact)recipient));
			} else if(recipient instanceof String) {
				// FIXME set a suitable icon for this phone number list item
				add(to, createListItem((String)recipient, recipient));
			}
		}
		
		MessagePanelController messagePanelController = new MessagePanelController(this);
		// We need to add the message panel to the dialog before setting the send button method
		add(dialog, messagePanelController.getPanel());
		messagePanelController.setSendButtonMethod(this, dialog, "sendMessage(composeMessageDialog, composeMessage_to, tfMessage)");
		add(dialog);
		
		LOG.trace("EXIT");
	}

	/**
	 * This method sends a message for all contacts in the supplied list.
	 * 
	 * @param composeMessageDialog
	 * @param recipientList The list with all contacts.
	 * @param messageContent The desired message.
	 */
	public void sendMessage(Object composeMessageDialog, Object recipientList, Object messageContent) {
		String messageText = getText(messageContent);
		for (Object o : getItems(recipientList)) {
			Object attachedObject = getAttachedObject(o);
			if(attachedObject == null) {
				// Do nothing
				// TODO check this is necessary
			} else if(attachedObject instanceof Contact) {
				Contact c = (Contact)attachedObject;
				frontlineController.sendTextMessage(c.getMsisdn(), messageText);
			} else if(attachedObject instanceof String) {
				// Attached object is a phone number
				frontlineController.sendTextMessage((String)attachedObject, messageText);
			}
		}
		remove(composeMessageDialog);
	}

	/**
	 * Removes the selected contacts.
	 * 
	 * @param groupList
	 * @param contactList
	 */
	public void groupManager_deleteSelectedContacts() {
		LOG.trace("ENTER");
		removeConfirmationDialog();
		Object groupList = find(COMPONENT_GROUP_MANAGER_GROUP_LIST);
		Object contactList = find(COMPONENT_GROUP_MANAGER_CONTACT_LIST);
		Object toSelect = getSelectedItem(groupList);
		Group currentGroup = getGroup(toSelect);
		for (Object o : getSelectedItems(contactList)) {
			Contact contact = getContact(o);
			if (isDefaultGroup(currentGroup)) {
				contactFactory.deleteContact(contact);
			} else {
				if(currentGroup.removeContact(contact)) {
					this.groupDao.updateGroup(currentGroup);
				}
			}
			remove(o);
		}
		contactDetails_hide(find(COMPONENT_GROUP_MANAGER_CONTACT_DETAILS));
		updateGroupList();
		LOG.trace("EXIT");
	}

	/**
	 * Removes the selected contacts of the supplied contact list component.
	 * 
	 * @param contactList The list, which contains the contacts to be removed.
	 */
	public void deleteSelectedContacts() {
		LOG.trace("ENTER");
		removeConfirmationDialog();
		setStatus(InternationalisationUtils.getI18NString(MESSAGE_REMOVING_CONTACTS));
		initProgress();
		final Object[] selected = getSelectedItems(contactListComponent);
		setProgressMax(selected.length);
		LOG.debug("Starting thread to remove contacts...");
		new Thread(){
			public void run() {
				int total = getItems(contactListComponent).length;
				HashMap<Integer, Group> toUpdate = new HashMap<Integer, Group>();
				toUpdate.put(ungroupedContacts.hashCode(), ungroupedContacts);
				toUpdate.put(ungroupedContacts.hashCode(), ungroupedContacts);
				toUpdate.put(unnamedContacts.hashCode(), unnamedContacts);
				for (Object o : selected) {
					incProgress();
					Contact contact = getContact(o);
					LOG.debug("Deleting contact [" + contact.getName() + "]");
					for (Group g : contact.getGroups()) {
						toUpdate.put(g.hashCode(), g);
					}
					contactFactory.deleteContact(contact);
					remove(o);
				}
				for (Integer key : toUpdate.keySet()) {
					Group g = toUpdate.get(key);
					updateGroup(g, getNodeForGroup(getGroupTreeComponent(), g));
				}
				updateContactListAfterDeletion(selected, total);
				finishProgress(InternationalisationUtils.getI18NString(MESSAGE_CONTACTS_DELETED));
			}
		}.start();
		LOG.trace("EXIT");
	}

	/**
	 * 
	 */
	void refreshPhonesViews() {
		if (currentTab.equals(TAB_ADVANCED_PHONE_MANAGER)) {
			LOG.debug("Refreshing phones tab");
			this.phoneTabController.refreshPhonesViews();
		} else if(currentTab.equals(TAB_SEND_CONSOLE)) {
			LOG.debug("Refreshing send tab (phone list)");
			sendConsole_refreshModemList();
		} else if(currentTab.equals(TAB_RECEIVE_CONSOLE)) {
			receiveConsole_updateConnectedPhones();
		}
	}
	/**
	 * Updates all message lists, depending on the view mode.
	 */
	private void refreshMessageLists() {
		if (classicMode) {
			sendConsole_refreshMessageList(false);
			messageTracker_refresh();
			receiveConsole_refresh();
		}
	}

	private void addMessageToList(Message message) {
		LOG.trace("ENTER");
		LOG.debug("Message [" + message + "]");
		Object messageTab = find(currentTab);
		Object sel = getSelectedItem(filterListComponent);
		boolean sent = isSelected(showSentMessagesComponent);
		boolean received = isSelected(showReceivedMessagesComponent);
		if (sel != null && ((sent && message.getType() == Message.TYPE_OUTBOUND) || (received && message.getType() == Message.TYPE_RECEIVED))) {
			boolean toAdd = false;
			if (getSelectedIndex(filterListComponent) == 0) {
				toAdd = true;
			} else {
				if (isSelected(find(messageTab, COMPONENT_CB_CONTACTS))) {
					Contact c = getContact(sel);
					LOG.debug("Contact selected [" + c.getName() + "]");
					if (message.getSenderMsisdn().endsWith(c.getMsisdn()) 
							|| message.getRecipientMsisdn().endsWith(c.getMsisdn())) {
						toAdd = true;
					}
				} else if (isSelected(find(messageTab, COMPONENT_CB_GROUPS))) {
					Group g = getGroup(sel);
					LOG.debug("Group selected [" + g.getName() + "]");
					if (g.equals(this.rootGroup)) {
						toAdd = true;
					} else {
						List<Group> groups = new ArrayList<Group>();
						getGroupsRecursivelyUp(groups, g);
						Contact sender = contactFactory.getFromMsisdn(message.getSenderMsisdn());
						Contact receiver = contactFactory.getFromMsisdn(message.getRecipientMsisdn());
						for (Group gg : groups) {
							if ( (sender != null && sender.isMemberOf(gg)) 
									|| (receiver != null && receiver.isMemberOf(gg))) {
								toAdd = true;
								break;
							}
						}
					}
				} else {
					Keyword selected = getKeyword(sel);
					LOG.debug("Keyword selected [" + selected.getKeywordString() + "]");
					Keyword keyword = keywordFactory.getFromMessageText(message.getTextContent());
					toAdd = selected.equals(keyword);
				}
			}
			if (toAdd) {
				LOG.debug("Time to try to add this message to list...");
				if (getItems(messageListComponent).length < getListLimit(messageListComponent)) {
					LOG.debug("There's space! Adding...");
					add(messageListComponent, getRow(message));
					setEnabled(messageListComponent, true);
					if (message.getType() == Message.TYPE_OUTBOUND) {
						numberToSend += message.getNumberOfSMS();
						updateMessageHistoryCost();
					}
				}
				if (message.getStatus() == Message.STATUS_OUTBOX) {
					setListElementCount(getListElementCount(messageListComponent) + 1, messageListComponent);
				}
				updatePageNumber(messageListComponent, getParent(messageListComponent));
			}
		}
		LOG.trace("EXIT");
	}

	public void messageHistory_dateChanged() {
		Object messagesTab = find(currentTab);
		Object tfStart = find(messagesTab, COMPONENT_TF_START_DATE);
		Object tfEnd = find(messagesTab, COMPONENT_TF_END_DATE);
		
		String startDate = getText(tfStart);
		String endDate = getText(tfEnd);
		
		Long newStart = messageHistoryStart;
		Long newEnd = messageHistoryEnd;
		
		try {
			Date s = InternationalisationUtils.parseDate(startDate);
			newStart = s.getTime();
		} catch (ParseException e1) {
			newStart = null;
		}
		
		try {
			Date e = InternationalisationUtils.parseDate(endDate);
			newEnd = e.getTime() + MILLIS_PER_DAY;
		} catch (ParseException e) {
			newEnd = null;
		}
		
		if (newStart != messageHistoryStart 
				|| newEnd != messageHistoryEnd) {
			messageHistoryStart = newStart;
			messageHistoryEnd = newEnd;
			updateMessageList();
		}
	}
	
	private void updateMessageHistoryCost() {
		Object messageTab = find(currentTab);
		setText(find(messageTab, COMPONENT_LB_MSGS_NUMBER), String.valueOf(numberToSend));		
		setText(find(messageTab, COMPONENT_LB_COST), InternationalisationUtils.formatCurrency(this.getCostPerSms() * numberToSend));
	}

	/**
	 * Method called when there is a change in the selection of Sent and Received messages.
	 * 
	 * @param checkbox
	 * @param list
	 */
	public void toggleMessageListOptions(Object checkbox, Object list) {
		Object showSentMessagesComponent;
		Object showReceivedMessagesComponent;
		if (list.equals(messageListComponent)) {
			showSentMessagesComponent = this.showSentMessagesComponent;
			showReceivedMessagesComponent = this.showReceivedMessagesComponent;
		} else {
			showSentMessagesComponent = find(COMPONENT_HISTORY_SENT_MESSAGES_TOGGLE);
			showReceivedMessagesComponent = find(COMPONENT_HISTORY_RECEIVED_MESSAGES_TOGGLE);
		}
		boolean showSentMessages = isSelected(showSentMessagesComponent);
		boolean showReceivedMessages = isSelected(showReceivedMessagesComponent);

		// One needs to be on, so if both have just been switched off, we need to turn the other back on.
		if (!showSentMessages && !showReceivedMessages) {
			if(checkbox == showSentMessagesComponent) {
				setSelected(showReceivedMessagesComponent, true);
			}
			else {
				setSelected(showSentMessagesComponent, true);
			}
		}
		if (list.equals(messageListComponent)) {
			setListPageNumber(1, list);
			updateMessageList();
		}
		
	}

	/**
	 * Method called when some buttons are pressed.
	 * 
	 * @param o The object, which was clicked.
	 */
	public void onClick(Object o) {
		LOG.trace("ENTER");
		String name = getString(o, Thinlet.NAME);
		LOG.debug("Component [" + name + "]");
		if (name.equals(COMPONENT_REPLY_MANAGER_LIST)) {
			Object replyManagerDetails = find(COMPONENT_REPLY_MANAGER_DETAILS);
			if (isEnabled(replyManagerDetails)) {
				replyManager_showReplyDetails(getKeywordAction(getSelectedItem(find(COMPONENT_REPLY_MANAGER_LIST))));
			} else {
				replyManager_enableButtons(true);
			}
		} else if (name.equals(COMPONENT_REPLY_MANAGER_EDIT_BUTTON)) {
			replyManager_enableButtons(false);
			replyManager_showReplyDetails(getKeywordAction(getSelectedItem(find(COMPONENT_REPLY_MANAGER_LIST))));
			Object replyManagerDetails = find(COMPONENT_REPLY_MANAGER_DETAILS);
			requestFocus(find(replyManagerDetails, COMPONENT_REPLY_MANAGER_REPLY_TEXT));
		} else if (name.equals(COMPONENT_REPLY_MANAGER_CREATE_BUTTON)) {
			replyManager_enableButtons(false);
			replyManager_showReplyDetails(null);
			Object replyManagerDetails = find(COMPONENT_REPLY_MANAGER_DETAILS);
			activate(replyManagerDetails);
			requestFocus(find(replyManagerDetails, COMPONENT_REPLY_MANAGER_KEYWORD));
		} 
		LOG.trace("EXIT");
	}
	
	/**
	 * Removes selected auto-replies.
	 */
	public void replyManager_delete() {
		LOG.trace("ENTER");
		removeConfirmationDialog();
		Object replyManager_keywordList = find(COMPONENT_REPLY_MANAGER_LIST);
		setStatus(InternationalisationUtils.getI18NString(MESSAGE_REMOVING_KEYWORD_ACTIONS));
		final Object[] selected = getSelectedItems(replyManager_keywordList);
		initProgress();
		setProgressMax(selected.length);
		LOG.debug("Starting thread to remove auto-replies...");
		new Thread(){
			public void run() {
				for (Object o : selected) {
					incProgress();
					if (isAttachment(o, KeywordAction.class)) {
						KeywordAction action = getKeywordAction(o);
						LOG.debug("Removing action [" + action + "]");
						keywordActionFactory.deleteKeywordAction(action);
					} 
				}
				replyManager_refreshKeywordList();
				finishProgress(InternationalisationUtils.getI18NString(MESSAGE_KEYWORD_ACTIONS_DELETED));
			}
		}.start();
		LOG.trace("EXIT");
	}

	/**
	 * Enables or disables the buttons inside <b>Replies</b> tab.
	 * 
	 * @param enabled
	 */
	private void replyManager_enableButtons(boolean enabled) {
		setEnabled(find(COMPONENT_REPLY_MANAGER_CREATE_BUTTON), enabled);
		setEnabled(find(COMPONENT_REPLY_MANAGER_EDIT_BUTTON), enabled);
		setEnabled(find(COMPONENT_REPLY_MANAGER_DELETE_BUTTON), enabled);
		setEnabled(find(COMPONENT_REPLY_MANAGER_EXPORT_BUTTON), enabled);		
	}

	/**
	 * Method called when the user first click on the end date textfield and the value is set to undefined.
	 * 
	 * @param o
	 */
	public void removeUndefinedString(Object o) {
		if (getText(o).equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
			setText(o, "");
		}
	}
	
	/**
	 * Shows the supplied auto reply for editing.
	 * 
	 * @param action The reply to be edited or null if we are creating a new one.
	 */
	private void replyManager_showReplyDetails(KeywordAction action) {
		Object replyManagerDetails = find(COMPONENT_REPLY_MANAGER_DETAILS);
		setAttachedObject(replyManagerDetails, action);
		if (action == null) {
			deactivate(replyManagerDetails);
			setText(find(replyManagerDetails, COMPONENT_REPLY_MANAGER_REPLY_TEXT), "");
			setText(find(replyManagerDetails, COMPONENT_REPLY_MANAGER_KEYWORD), "");
		} else {
			Keyword keyword = action.getKeyword();
			Object keywordTextfield = find(replyManagerDetails, COMPONENT_REPLY_MANAGER_KEYWORD);
			setText(keywordTextfield, keyword.getKeywordString());
			setText(find(replyManagerDetails, COMPONENT_REPLY_MANAGER_REPLY_TEXT), KeywordAction.KeywordUtils.getReplyText(action, DEMO_SENDER, DEMO_SENDER_MSISDN, DEMO_MESSAGE_TEXT_INCOMING, DEMO_MESSAGE_KEYWORD));
			activate(replyManagerDetails);
			deactivate(keywordTextfield);
		}
		setText(find(replyManagerDetails, COMPONENT_TF_START_DATE), action == null ? "" : InternationalisationUtils.getDateFormat().format(action.getStartDate()));
		Object endDate = find(replyManagerDetails, COMPONENT_TF_END_DATE);
		String toSet = "";
		if (action != null) {
			if (action.getEndDate() == DEFAULT_END_DATE) {
				toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
			} else {
				toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
			}
		}
		setText(endDate, toSet);
	}

	/**
	 * Clean all auto reply details from the panel.
	 */
	public void replyManagerDetails_cancel() {
		replyManager_showReplyDetails(null);
		replyManager_enableButtons(getSelectedItems(find(COMPONENT_REPLY_MANAGER_LIST)).length > 0);
		setEnabled(find(COMPONENT_REPLY_MANAGER_CREATE_BUTTON), true);
	}

	/**
	 * Method called when the Update button (from <b>Replies</b> tab) is pressed.
	 * <br> This method updates a reply if there was one being edited or create a new one, based on information entered by the user.
	 * 
	 * @param replyManagerDetails
	 * @param live
	 */
	public void replyManagerDetails_update(Object replyManagerDetails) {
		KeywordAction action = getKeywordAction(replyManagerDetails);
		String replyText = getText((find(replyManagerDetails, COMPONENT_REPLY_MANAGER_REPLY_TEXT)));
		String startDate = getText(find(replyManagerDetails, COMPONENT_TF_START_DATE));
		String endDate = getText(find(replyManagerDetails, COMPONENT_TF_END_DATE));
		LOG.debug("Reply Text [" + replyText + "]");
		LOG.debug("Start Date [" + startDate + "]");
		LOG.debug("End Date [" + endDate + "]");
		if (startDate.equals("")) {
			LOG.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
			startDate = InternationalisationUtils.getDefaultStartDate();
		}
		long start;
		long end;
		try {
			Date ds = InternationalisationUtils.parseDate(startDate); 
			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
				Date de = InternationalisationUtils.parseDate(endDate);
				if (!Utils.validateDates(ds, de)) {
					LOG.debug("Start date is not before the end date");
					alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
					LOG.trace("EXIT");
					return;
				}
				end = de.getTime();
			} else {
				end = DEFAULT_END_DATE;
			}
			start = ds.getTime();
		} catch (ParseException e) {
			LOG.debug("Wrong format for date", e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
			LOG.trace("EXIT");
			return;
		} 
		if (action != null) {
			LOG.debug("Editing action [" + action + "]. Setting new values!");
			action.setReplyText(replyText);
			action.setStartDate(start);
			action.setEndDate(end);
		} else {
			String newWordString = getText(find(replyManagerDetails, COMPONENT_REPLY_MANAGER_KEYWORD));
			LOG.debug("Creating new auto_reply");
			try {
				action = KeywordAction.createReplyAction(createKeywordsHierarchically(newWordString, ""), replyText, start, end);
				keywordActionFactory.saveKeywordAction(action);
			} catch (DuplicateKeyException e) {
				alert(InternationalisationUtils.getI18NString(MESSAGE_KEYWORD_EXISTS));
				LOG.trace("EXIT");
				return;
			}
		}
		replyManager_refreshKeywordList();
		replyManager_showReplyDetails(null);
		LOG.trace("EXIT");
	}

	/**
	 * Updates the status bar with the supplied string.
	 * 
	 * @param status
	 */
	synchronized void setStatus(String status) {
		LOG.debug("Status Text [" + status + "]");
		setString(statusBarComponent, TEXT, status);
	}

	/** @see #showGroupSelecter(Object, String, String, Object) */
	private void showGroupSelecter(Object actionObject, String title, String callbackMethodName) {
		showGroupSelecter(actionObject, false, title, callbackMethodName, this);
	}
	
	/**
	 * Shows the group selecter dialog, which is used for JOIN/LEAVE group actions.
	 * @param actionObject The object to be edited, or null if we are creating one.
	 * @param title
	 * @param callbackMethodName
	 */
	public void showGroupSelecter(Object actionObject, boolean addDatePanel, String title, String callbackMethodName, ThinletUiEventHandler eventHandler) {
		System.out.println("UiGeneratorController.showGroupSelecter()");
		System.out.println("actionObject: " + actionObject);
		System.out.println("title: " + title);
		Object selecter = loadComponentFromFile(UI_FILE_GROUP_SELECTER, eventHandler);
		//Adds the date panel to it
		if(addDatePanel) {
			addDatePanel(selecter);
		}
		setAttachedObject(selecter, actionObject);
		setText(find(selecter, COMPONENT_GROUP_SELECTER_TITLE), title);
		Object list = find(selecter, COMPONENT_GROUP_SELECTER_GROUP_LIST);
		List<Group> userGroups = getAllGroups();
		if (userGroups.size() == 0) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_NO_GROUP_CREATED_BY_USERS));
			return;
		}
		for (Group g : userGroups) {
			Object item = createListItem(g.getName(), g);
			setIcon(item, Icon.GROUP);
			if (actionObject instanceof KeywordAction) {
				KeywordAction action = (KeywordAction) actionObject;
				if (g.getName().equals(action.getGroup().getName())) {
					setSelected(item, true);
				}
			}
			add(list, item);
		}
		if (addDatePanel && actionObject instanceof KeywordAction) {
			System.out.println("UiGeneratorController.showGroupSelecter() : ADDING THE DATES COMPONENT.");
			KeywordAction action = (KeywordAction) actionObject;
			setText(find(selecter, COMPONENT_TF_START_DATE), action == null ? "" : InternationalisationUtils.getDateFormat().format(action.getStartDate()));
			Object endDate = find(selecter, COMPONENT_TF_END_DATE);
			String toSet = "";
			if (action != null) {
				if (action.getEndDate() == DEFAULT_END_DATE) {
					toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
				} else {
					toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
				}
			}
			setText(endDate, toSet);
		}
		setMethod(find(selecter, COMPONENT_GROUP_SELECTER_OK_BUTTON), ATTRIBUTE_ACTION, callbackMethodName, selecter, eventHandler);
		add(selecter);
	}
	
	/**
	 * Shows the contact selected dialog.
	 * <br> Note that this method includes all contacts to the list.
	 * 
	 * @param title
	 * @param callbackMethodName
	 */
	private void showContactSelecter(String title, String callbackMethodName, Object dialog) {
		showContactSelecter(title, callbackMethodName, dialog, this);
	}

	/**
	 * Shows a dialog for selecting contacts.
	 * @param title
	 * @param callbackMethodName
	 * @param attachment
	 * @param handler
	 */
	public void showContactSelecter(String title, String callbackMethodName, Object attachment, ThinletUiEventHandler handler) {
		Object selecter = loadComponentFromFile(UI_FILE_CONTACT_SELECTER, handler);
		setText(find(selecter, COMPONENT_CONTACT_SELECTER_TITLE), title);
		Object contactList = find(selecter, COMPONENT_CONTACT_SELECTER_CONTACT_LIST);
		setMethod(contactList, Thinlet.PERFORM, callbackMethodName, selecter, handler);
		setMethod(find(selecter, COMPONENT_CONTACT_SELECTER_OK_BUTTON), ATTRIBUTE_ACTION, callbackMethodName, selecter, handler);
		if (attachment != null) {
			setAttachedObject(selecter, attachment);
		}
		Object pagePanel = loadComponentFromFile(UI_FILE_PAGE_PANEL);
		setChoice(pagePanel, HALIGN, RIGHT);
		add(selecter, pagePanel, getItems(selecter).length - 1);
		setPageMethods(selecter, COMPONENT_CONTACT_SELECTER_CONTACT_LIST, pagePanel);
		add(selecter);
		
		setListLimit(contactList);
		setListPageNumber(1, contactList);
		setMethod(contactList, "updateContactSelecterList");
		setListElementCount(contactFactory.getAllContacts().size(), contactList);
		
		Object list = find(COMPONENT_CONTACT_SELECTER_CONTACT_LIST);
		int limit = getListLimit(list);
		int pageNumber = getListCurrentPage(list);
		List<? extends Contact> contacts = contactFactory.getAllContacts((pageNumber - 1) * limit, limit);
		removeAll(list);
		for (Contact c : contacts) {
			add(list, createListItem(c));
		}
		updatePageNumber(list, find(COMPONENT_CONTACT_SELECTER));
	}
	
	/**
	 * Method invoked when the user decides to send a message specifically to one contact on the <b>Send</b> tab.
	 */
	public void selectMessageRecipient() {
		showContactSelecter(InternationalisationUtils.getI18NString(SENTENCE_SELECT_MESSAGE_RECIPIENT_TITLE), "sendConsole_setLoneRecipientTextfield(contactSelecter_contactList, contactSelecter)", null);
	}
	
	public void selectSenderNumber() {
		showContactSelecter(InternationalisationUtils.getI18NString(COMMON_SENDER_NUMBER), "smsHttpSettings_setTextfield(contactSelecter_contactList, contactSelecter)", null);
	}
	
	public void selectEditSenderNumber() {
		showContactSelecter(InternationalisationUtils.getI18NString(COMMON_SENDER_NUMBER), "smsHttpSettingsEdit_setTextfield(contactSelecter_contactList, contactSelecter)", null);
	}
	
	/**
	 * Method invoked when the user decides to send a mail specifically to one contact.
	 */
	public void selectMailRecipient(Object dialog) {
		showContactSelecter(InternationalisationUtils.getI18NString(SENTENCE_SELECT_MESSAGE_RECIPIENT_TITLE), "mail_setRecipient(contactSelecter_contactList, contactSelecter)", dialog);
	}
	
	/**
	 * Sets the phone number of the selected contact.
	 * 
	 * @param contactSelecter_contactList
	 * @param dialog
	 */
	public void mail_setRecipient(Object contactSelecter_contactList, Object dialog) {
		LOG.trace("ENTER");
		Object emailDialog = getAttachedObject(dialog);
		Object recipientTextfield = find(emailDialog, COMPONENT_TF_RECIPIENT);
		Object selectedItem = getSelectedItem(contactSelecter_contactList);
		if (selectedItem == null) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_NO_CONTACT_SELECTED));
			LOG.trace("EXIT");
			return;
		}
		Contact selectedContact = getContact(selectedItem);
		String currentText = getText(recipientTextfield);
		LOG.debug("Recipients begin [" + currentText + "]");
		if (!currentText.equals("")) {
			currentText += ";";
		}
		currentText += selectedContact.getEmailAddress();
		LOG.debug("Recipients final [" + currentText + "]");
		setText(recipientTextfield, currentText);
		remove(dialog);
		LOG.trace("EXIT");
	}
	
	/**
	 * Sets the phone number of the selected contact.
	 * 
	 * @param contactSelecter_contactList
	 * @param dialog
	 */
	public void smsHttpSettings_setTextfield(Object contactSelecter_contactList, Object dialog) {
		Object obj = find("smsHttpDialog");
		Object tf = find(obj, "tfAccountSender");
		Object selectedItem = getSelectedItem(contactSelecter_contactList);
		if (selectedItem == null) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_NO_CONTACT_SELECTED));
			return;
		}
		Contact selectedContact = getContact(selectedItem);
		setText(tf, selectedContact.getMsisdn());
		remove(dialog);
	}
	
	/**
	 * Sets the phone number of the selected contact.
	 * 
	 * @param contactSelecter_contactList
	 * @param dialog
	 */
	public void smsHttpSettingsEdit_setTextfield(Object contactSelecter_contactList, Object dialog) {
		Object obj = find("editSmsHttp");
		Object tf = find(obj, "tfAccountSender");
		Object selectedItem = getSelectedItem(contactSelecter_contactList);
		if (selectedItem == null) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_NO_CONTACT_SELECTED));
			return;
		}
		Contact selectedContact = getContact(selectedItem);
		setText(tf, selectedContact.getMsisdn());
		remove(dialog);
	}
	
	/**
	 * Sets the phone number of the selected contact.
	 * 
	 * @param contactSelecter_contactList
	 * @param dialog
	 */
	public void sendConsole_setLoneRecipientTextfield(Object contactSelecter_contactList, Object dialog) {
		Object sendConsoleLoneRecipientTextfield = find(COMPONENT_SEND_CONSOLE_LONE_RECIPIENT);
		Object selectedItem = getSelectedItem(contactSelecter_contactList);
		if (selectedItem == null) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_NO_CONTACT_SELECTED));
			return;
		}
		Contact selectedContact = getContact(selectedItem);
		setText(sendConsoleLoneRecipientTextfield, selectedContact.getMsisdn());
		remove(dialog);
		sendConsole_loneRecipientToggle(selectedContact.getMsisdn(), find(COMPONENT_SEND_CONSOLE_GROUP_TREE));
		numberToSend = 1;
		updateCost();
	}
	/**
	 * Shows the new auto reply action dialog.
	 * 
	 * @param keywordList
	 */
	public void show_newKActionReplyForm(Object keywordList) {
		// Load the reply form from file.  We then attach the keyword we're working on to
		// the form so that it can be retrieved later for actioning.  Also, we can set the
		// title of the loaded form to remind the user which keyword they are adding a
		// reply to.
		Object autoReplyForm = loadComponentFromFile(UI_FILE_NEW_KACTION_REPLY_FORM);
		
		Object pnMessage = new MessagePanelController(this).getPanel();
		// FIX 0000542 FIXME this comment is not useful - what is the fix?  or more importantly, what is the function of this code?
		Object pnBottom = find(pnMessage, COMPONENT_PN_BOTTOM);
		remove(getItem(pnBottom, 0));
		Object senderPanel = loadComponentFromFile(UI_FILE_SENDER_NAME_PANEL);
		add(pnBottom, senderPanel, 0);
		add(autoReplyForm, pnMessage, getItems(autoReplyForm).length - 3);
		setMethod(find(senderPanel, COMPONENT_BT_SENDER_NAME), ATTRIBUTE_ACTION, "addConstantToCommand(tfMessage.text, tfMessage, 0)", autoReplyForm, this);
		setMethod(find(senderPanel, "btSenderNumber"), ATTRIBUTE_ACTION, "addConstantToCommand(tfMessage.text, tfMessage, 1)", autoReplyForm, this);
		// FIX 0000542 FIXME this comment is not useful - what is the fix?  or more importantly, what is the function of this code?
		setMethod(find(autoReplyForm, COMPONENT_BT_SAVE), ATTRIBUTE_ACTION, "do_newKActionReply(autoReplyForm, tfMessage.text)", autoReplyForm, this);

		//Adds the date panel to it
		addDatePanel(autoReplyForm);
		Keyword keyword = getKeyword(getSelectedItem(keywordList));
		setAttachedObject(autoReplyForm, keyword);
		add(autoReplyForm);
		numberToSend = 1;
	}

	/**
	 * Shows the new auto reply action dialog for editing purpose.
	 * 
	 * @param action
	 */
	private void show_newKActionReplyFormForEdition(KeywordAction action) {
		// Load the reply form from file.  We then attach the keyword we're working on to
		// the form so that it can be retrieved later for actioning.  Also, we can set the
		// title of the loaded form to remind the user which keyword they are adding a
		// reply to.
		Object autoReplyForm = loadComponentFromFile(UI_FILE_NEW_KACTION_REPLY_FORM);
		
		MessagePanelController messagePanelController = new MessagePanelController(this);
		Object pnMessage = messagePanelController.getPanel();
		// FIX 0000542
		Object pnBottom = find(pnMessage, COMPONENT_PN_BOTTOM);
		remove(getItem(pnBottom, 0));
		Object senderPanel = loadComponentFromFile(UI_FILE_SENDER_NAME_PANEL);
		add(pnBottom, senderPanel, 0);
		add(autoReplyForm, pnMessage, getItems(autoReplyForm).length - 3);
		setMethod(find(senderPanel, COMPONENT_BT_SENDER_NAME), ATTRIBUTE_ACTION, "addConstantToCommand(tfMessage.text, tfMessage, 0)", autoReplyForm, this);
		setMethod(find(senderPanel, "btSenderNumber"), ATTRIBUTE_ACTION, "addConstantToCommand(tfMessage.text, tfMessage, 1)", autoReplyForm, this);
		// FIX 0000542
		
		//Adds the date panel to it
		addDatePanel(autoReplyForm);
		
		setMethod(find(autoReplyForm, COMPONENT_BT_SAVE), ATTRIBUTE_ACTION, "do_newKActionReply(autoReplyForm, tfMessage.text)", autoReplyForm, this);
		
		setAttachedObject(autoReplyForm, action);
		
		setText(find(autoReplyForm, COMPONENT_TF_MESSAGE), action.getUnformattedReplyText());
		messagePanelController.messageChanged(action.getUnformattedReplyText());
		
		setText(find(autoReplyForm, COMPONENT_TF_START_DATE), action == null ? "" : InternationalisationUtils.getDateFormat().format(action.getStartDate()));
		Object endDate = find(autoReplyForm, COMPONENT_TF_END_DATE);
		String toSet = "";
		if (action != null) {
			if (action.getEndDate() == DEFAULT_END_DATE) {
				toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
			} else {
				toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
			}
		}
		setText(endDate, toSet);
		add(autoReplyForm);
		numberToSend = 1;
	}
	
	/**
	 * Event fired when the popup menu (in the keyword manager tab) is shown.
	 * If there is no keyword listed in the tree, the only option allowed is
	 * to create one. Otherwise, all components are allowed.
	 */
	public void enableKeywordFields(Object component) {
		LOG.trace("ENTER");
		int selected = getSelectedIndex(keywordListComponent);
		String field = getClass(component) == PANEL ? Thinlet.ENABLED : Thinlet.VISIBLE;
		if (selected <= 0) {
			LOG.debug("Nothing selected, so we only allow keyword creation.");
			for (Object o : getItems(component)) {
				String name = getString(o, Thinlet.NAME);
				if (name == null)
					continue;
				if (!name.equals(COMPONENT_MENU_ITEM_CREATE)) {
					setBoolean(o, field, false);
				} else {
					setBoolean(o, field, true);
				}
			}
		} else {
			//Keyword selected
			for (Object o : getItems(component)) {
				setBoolean(o, field, true);
			}
		}
		LOG.trace("EXIT");
	}

	/**
	 * Event fired when the popup menu (in the keyword manager tab) is shown.
	 * If there is no keyword action listed in the table, the only option allowed is
	 * to create one. Otherwise, all components are allowed.
	 */
	public void enableKeywordActionFields(Object table, Object component) {
		LOG.trace("ENTER");
		int selected = getSelectedIndex(table);
		String field = getClass(component) == PANEL ? Thinlet.ENABLED : Thinlet.VISIBLE;
		if (selected < 0) {
			LOG.debug("Nothing selected, so we only allow keyword action creation.");
			for (Object o : getItems(component)) {
				String name = getString(o, Thinlet.NAME);
				if (name == null)
					continue;
				if (!name.equals(COMPONENT_MENU_ITEM_CREATE)
						&& !name.equals(COMPONENT_CB_ACTION_TYPE)) {
					setBoolean(o, field, false);
				} else {
					setBoolean(o, field, true);
				}
			}
		} else {
			//Keyword action selected
			for (Object o : getItems(component)) {
				setBoolean(o, field, true);
			}
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * Shows the new join group action dialog.
	 * 
	 * @param keywordList
	 */
	public void show_newKActionJoinForm(Object keywordList) {
		Keyword keyword = getKeyword(getSelectedItem(keywordList));
		showGroupSelecter(keyword, InternationalisationUtils.getI18NString(COMMON_KEYWORD) + " \"" + keyword.getKeyword() + "\" " + InternationalisationUtils.getI18NString(COMMON_AUTO_JOIN_GROUP) + ":", "do_newKActionJoin(groupSelecter, groupSelecter_groupList)");
	}
	
	/**
	 * Shows the new leave group action dialog.
	 * 
	 * @param keywordList
	 */
	public void show_newKActionLeaveForm(Object keywordList) {
		Keyword keyword = getKeyword(getSelectedItem(keywordList));
		showGroupSelecter(keyword, InternationalisationUtils.getI18NString(COMMON_KEYWORD) + " \"" + keyword.getKeyword() + "\" " + InternationalisationUtils.getI18NString(COMMON_AUTO_LEAVE_GROUP) + ":", "do_newKActionLeave(groupSelecter, groupSelecter_groupList)");
	}
	/**
	 * Shows the new external command action dialog.
	 * 
	 * @param keywordList
	 */
	public void show_newKActionExternalCmdForm(Object keywordList) {
		LOG.trace("ENTER");
		Keyword keyword = getKeyword(getSelectedItem(keywordList));
		LOG.debug("External command for keyword [" + keyword.getKeywordString() + "]");
		Object externalCmdForm = loadComponentFromFile(UI_FILE_NEW_KACTION_EXTERNAL_COMMAND_FORM);
		//Adds the date panel to it
		addDatePanel(externalCmdForm);
		setAttachedObject(externalCmdForm, keyword);
		Object list = find(externalCmdForm, COMPONENT_EXTERNAL_COMMAND_GROUP_LIST);
		List<Group> userGroups = getAllGroups();
		for (Group g : userGroups) {
			LOG.debug("Adding group [" + g.getName() + "] to list");
			Object item = createListItem(g.getName(), g);
			setIcon(item, Icon.GROUP);
			add(list, item);
		}
		add(externalCmdForm);
		LOG.trace("EXIT");
	}
	
	/**
     *  0 - Auto Reply
     *  1 - Auto Forward
     *  2 - Join Group
     *  3 - Leave Group
     *  4 - Survey
     *  5 - E-mail
     *  6 - External Command
     */
	public void keywordTab_createAction(int index) {
		switch (index) {
		case 0:
			show_newKActionReplyForm(keywordListComponent);
			break;
		case 1:
			show_newKActionForwardForm(keywordListComponent);
			break;
		case 2:
			show_newKActionJoinForm(keywordListComponent);
			break;
		case 3:
			show_newKActionLeaveForm(keywordListComponent);
			break;
		case 4:
			show_newKActionSurveyForm(keywordListComponent);
			break;
		case 5:
			show_newKActionEmailForm(keywordListComponent);
			break;
		case 6:
			show_newKActionExternalCmdForm(keywordListComponent);
			break;
		}
	}
	
	public void keywordTab_newAction(Object combo) {
		keywordTab_createAction(getSelectedIndex(combo));
	}
	
	/**
	 * Shows the new email action dialog.
	 * 
	 * @param keywordList
	 */
	public void show_newKActionEmailForm(Object keywordList) {
		LOG.trace("ENTER");
		Keyword keyword = getKeyword(getSelectedItem(keywordList));
		Object emailForm = loadComponentFromFile(UI_FILE_NEW_KACTION_EMAIL_FORM);
		setAttachedObject(emailForm, keyword);
		//Adds the date panel to it
		addDatePanel(emailForm);
		Object list = find(emailForm, COMPONENT_MAIL_LIST);
		for (EmailAccount acc : emailAccountFactory.getAllEmailAccounts()) {
			LOG.debug("Adding existent e-mail account [" + acc.getAccountName() + "] to list");
			Object item = createListItem(acc.getAccountName(), acc);
			setIcon(item, Icon.SERVER);
			add(list, item);
		}
		add(emailForm);
		LOG.trace("EXIT");
	}
	
	/**
	 * Shows the new survey action dialog.
	 * 
	 * @param keywordList
	 */
	public void show_newKActionSurveyForm(Object keywordList) {
		Keyword keyword = getKeyword(getSelectedItem(keywordList));
		Object surveyForm = loadComponentFromFile(UI_FILE_NEW_KACTION_SURVEY_FORM);
		//Adds the date panel to it
		addDatePanel(surveyForm);
		setAttachedObject(surveyForm, keyword);
		add(surveyForm);
	}
	
	/**
	 * Shows the new survey action dialog for edition purpose.
	 * 
	 * @param keywordList
	 */
	private void show_newKActionSurveyFormForEdition(KeywordAction action) {
		Object surveyForm = loadComponentFromFile(UI_FILE_NEW_KACTION_SURVEY_FORM);
		//Adds the date panel to it
		addDatePanel(surveyForm);
		setAttachedObject(surveyForm, action);
		
		setText(find(surveyForm, COMPONENT_TF_START_DATE), action == null ? "" : InternationalisationUtils.getDateFormat().format(action.getStartDate()));
		Object endDate = find(surveyForm, COMPONENT_TF_END_DATE);
		String toSet = "";
		if (action != null) {
			if (action.getEndDate() == DEFAULT_END_DATE) {
				toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
			} else {
				toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
			}
		}
		setText(endDate, toSet);
		add(surveyForm);
	}
	
	/**
	 * Shows the new email action dialog.
	 * 
	 * @param keywordList
	 */
	private void show_newKActionEmailFormForEdition(KeywordAction action) {
		Object emailForm = loadComponentFromFile(UI_FILE_NEW_KACTION_EMAIL_FORM);
		//Adds the date panel to it
		addDatePanel(emailForm);
		setAttachedObject(emailForm, action);
		Object list = find(emailForm, COMPONENT_MAIL_LIST);
		for (EmailAccount acc : emailAccountFactory.getAllEmailAccounts()) {
			LOG.debug("Adding existent e-mail account [" + acc.getAccountName() + "] to list");
			Object item = createListItem(acc.getAccountName(), acc);
			setIcon(item, Icon.SERVER);
			add(list, item);
			if (acc.equals(action.getEmailAccount())) {
				LOG.debug("Selecting the current account for this e-mail [" + acc.getAccountName() + "]");
				setSelected(item, true);
			}
		}
		setText(find(emailForm, COMPONENT_TF_SUBJECT), action.getEmailSubject());
		setText(find(emailForm, COMPONENT_TF_MESSAGE), action.getUnformattedReplyText());
		setText(find(emailForm, COMPONENT_TF_RECIPIENT), action.getEmailRecipients());
		
		setText(find(emailForm, COMPONENT_TF_START_DATE), action == null ? "" : InternationalisationUtils.getDateFormat().format(action.getStartDate()));
		Object endDate = find(emailForm, COMPONENT_TF_END_DATE);
		String toSet = "";
		if (action != null) {
			if (action.getEndDate() == DEFAULT_END_DATE) {
				toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
			} else {
				toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
			}
		}
		setText(endDate, toSet);
		add(emailForm);
		LOG.trace("EXIT");
	}

	/**
	 * Shows the email accounts settings dialog.
	 */
	public void showEmailAccountsSettings() {
		Object emailForm = getEmailAccountForm();
		add(emailForm);
	}
	
	public void finishEmailManagement(Object dialog) {
		Object att = getAttachedObject(dialog);
		if (att != null) {
			Object list = find(att, COMPONENT_ACCOUNTS_LIST);
			removeAll(list);
			for (EmailAccount acc : emailAccountFactory.getAllEmailAccounts()) {
				Object item = createListItem(acc.getAccountName(), acc);
				setIcon(item, Icon.SERVER);
				add(list, item);
			}
		}
		removeDialog(dialog);
	}
	
	// TODO implementation-specific methods will be removed
	public void smsHttpIntelliSms_enableReceiving(Object panel, boolean selected) {
		setEnabled(panel, selected);
		for (Object child: getItems(panel)) {
			setEnabled(child, selected);
		}
	}
	
	/**
	 * Shows the email accounts settings dialog.
	 */
	public void showEmailAccountsSettings(Object dialog) {
		Object emailForm = getEmailAccountForm();
		setAttachedObject(emailForm, dialog);
		add(emailForm);
	}

	/**
	 * @return
	 */
	private Object getEmailAccountForm() {
		Object emailForm = loadComponentFromFile(UI_FILE_EMAIL_ACCOUNTS_SETTINGS_FORM);
		Object table = find(emailForm, COMPONENT_ACCOUNTS_LIST);
		for (EmailAccount acc : emailAccountFactory.getAllEmailAccounts()) {
			add(table, getRow(acc));
		}
		return emailForm;
	}
	
	/**
	 * Shows the sms services accounts (IntelliSMS) settings dialog.
	 * FIXME confirm this is actually used still
	 */
	public void showSmsHttpIntelliSmsServicesSettings() {
		//TODO Load the accounts from databse
		Object smsServicesForm = loadComponentFromFile(UI_FILE_SMS_SERVICES_ACCOUNTS_INTELLISMS_SETTINGS_FORM);
		/*Object table = find(smsServicesForm, COMPONENT_ACCOUNTS_LIST);
		for (SmsHttpServiceAccount acc : smsHttpServiceAccountFactory.getAllSmsHttpServiceAccounts()) {
			add(table, getRow(acc));
		}*/
		add(smsServicesForm);
	}
	
	/**
	 * Method called when the user changes the task type.
	 */
	public void taskTypeChanged(Object dialog, Object selected) {
		String name = getString(selected, Thinlet.NAME);
		Object label = find(dialog, COMPONENT_LB_TEXT);
		if (name.equalsIgnoreCase(COMPONENT_RB_HTTP)) {
			//HTTP
			setText(label, InternationalisationUtils.getI18NString(COMMON_URL));
			setIcon(label, Icon.ACTION_HTTP_REQUEST);
		} else {
			//CMD
			setText(label, InternationalisationUtils.getI18NString(COMMON_COMMAND));
			setIcon(label, Icon.ACTION_EXTERNAL_COMMAND);
		}
	}
	
	public void showDateSelecter(Object textField) {
		LOG.trace("ENTER");
		try {
			new DateSelecter(this, textField).showSelecter();
		} catch (IOException e) {
			LOG.error("Error parsing file for dateSelecter", e);
			LOG.trace("EXIT");
			throw new RuntimeException(e);
		}
		LOG.trace("EXIT");
	}
	
	public void replyManager_showDateSelecter(Object panel, String type) {
		LOG.trace("ENTER");
		Object textField = type.equals("s") ? find(panel, COMPONENT_TF_START_DATE) : find(panel, COMPONENT_TF_END_DATE);
		try {
			new DateSelecter(this, textField).showSelecter();
		} catch (IOException e) {
			LOG.error("Error parsing file for dateSelecter", e);
			LOG.trace("EXIT");
			throw new RuntimeException(e);
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * This method is called when the save button is pressed in the new mail account dialog. 
	 
	 * @param dialog
	 */
	public void saveEmailAccount(Object dialog) {
		LOG.trace("ENTER");
		String server = getText(find(dialog, COMPONENT_TF_MAIL_SERVER));
		String accountName = getText(find(dialog, COMPONENT_TF_ACCOUNT));
		String password = getText(find(dialog, COMPONENT_TF_ACCOUNT_PASS));
		boolean useSSL = isSelected(find(dialog, COMPONENT_CB_USE_SSL));
		String portAsString = getText(find(dialog, COMPONENT_TF_ACCOUNT_SERVER_PORT));
		
		int serverPort;
		try {
			serverPort = Integer.parseInt(portAsString);
		} catch (NumberFormatException e1) {
			if (useSSL) serverPort = EmailAccount.DEFAULT_SMTPS_PORT;
			else serverPort = EmailAccount.DEFAULT_SMTP_PORT;
		}
		
		Object table = find(dialog, COMPONENT_ACCOUNTS_LIST);
		
		LOG.debug("Server [" + server + "]");
		LOG.debug("Account [" + accountName + "]");
		LOG.debug("Account Server Port [" + serverPort + "]");
		LOG.debug("SSL [" + useSSL + "]");
		
		if (accountName.equals("")) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_ACCOUNT_NAME_BLANK));
			LOG.trace("EXIT");
			return;
		}
		
		try {
			Object att = getAttachedObject(dialog);
			if (att == null || !(att instanceof EmailAccount)) {
				LOG.debug("Testing connection to [" + server + "]");
				if (EmailSender.testConnection(server, accountName, serverPort, password, useSSL)) {
					LOG.debug("Connection was successful, creating account [" + accountName + "]");
					EmailAccount account = new EmailAccount(accountName, server, serverPort, password, useSSL);
					emailAccountFactory.saveEmailAccount(account);
					add(table, getRow(account));
					cleanEmailAccountFields(dialog);
				} else {
					LOG.debug("Connection failed.");
					Object connectWarning = loadComponentFromFile(UI_FILE_CONNECTION_WARNING_FORM);
					setAttachedObject(connectWarning, dialog);
					add(connectWarning);
				}
			} else if (att instanceof EmailAccount) {
				EmailAccount acc = (EmailAccount) att;
				acc.setAccountName(accountName);
				acc.setAccountPassword(password);
				acc.setAccountServer(server);
				acc.setUseSSL(useSSL);
				acc.setAccountServerPort(serverPort);
				
				Object tableToAdd = find(find("emailConfigDialog"), COMPONENT_ACCOUNTS_LIST);
				int index = getSelectedIndex(tableToAdd);
				remove(getSelectedItem(tableToAdd));
				add(tableToAdd, getRow(acc), index);
				
				setSelectedIndex(tableToAdd, index);
				
				removeDialog(dialog);
			}
			
		} catch (DuplicateKeyException e) {
			LOG.debug(InternationalisationUtils.getI18NString(MESSAGE_ACCOUNT_NAME_ALREADY_EXISTS), e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_ACCOUNT_NAME_ALREADY_EXISTS));
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * After failing to connect to the email server, the user has an option to
	 * create the account anyway. This method handles this action. 
	 * 
	 * @param currentDialog
	 */
	public void createAccount(Object currentDialog) {
		LOG.trace("ENTER");
		removeDialog(currentDialog);
		LOG.debug("Creating account anyway!");
		Object accountDialog = getAttachedObject(currentDialog);
		String server = getText(find(accountDialog, COMPONENT_TF_MAIL_SERVER));
		String accountName = getText(find(accountDialog, COMPONENT_TF_ACCOUNT));
		String password = getText(find(accountDialog, COMPONENT_TF_ACCOUNT_PASS));
		boolean useSSL = isSelected(find(accountDialog, COMPONENT_CB_USE_SSL));
		String portAsString = getText(find(accountDialog, COMPONENT_TF_ACCOUNT_SERVER_PORT));
		
		int serverPort;
		try {
			serverPort = Integer.parseInt(portAsString);
		} catch (NumberFormatException e1) {
			if (useSSL) serverPort = EmailAccount.DEFAULT_SMTPS_PORT;
			else serverPort = EmailAccount.DEFAULT_SMTP_PORT;
		}
		
		Object table = find(accountDialog, COMPONENT_ACCOUNTS_LIST);
		
		LOG.debug("Server Name [" + server + "]");
		LOG.debug("Account Name [" + accountName + "]");
		LOG.debug("Account Server Port [" + serverPort + "]");
		LOG.debug("SSL [" + useSSL + "]");
		EmailAccount acc;
		try {
			acc = new EmailAccount(accountName, server, serverPort, password, useSSL);
			emailAccountFactory.saveEmailAccount(acc);
		} catch (DuplicateKeyException e) {
			LOG.debug("Account already exists", e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_ACCOUNT_NAME_ALREADY_EXISTS));
			LOG.trace("EXIT");
			return;
		}
		LOG.debug("Account [" + acc.getAccountName() + "] created!");
		add(table, getRow(acc));
		cleanEmailAccountFields(accountDialog);
		LOG.trace("EXIT");
	}

	private void cleanEmailAccountFields(Object accountDialog) {
		setText(find(accountDialog, COMPONENT_TF_MAIL_SERVER), "");
		setText(find(accountDialog, COMPONENT_TF_ACCOUNT), "");
		setText(find(accountDialog, COMPONENT_TF_ACCOUNT_PASS), "");
		setText(find(accountDialog, COMPONENT_TF_ACCOUNT_SERVER_PORT), "");
		setSelected(find(accountDialog, COMPONENT_CB_USE_SSL), true);
	}
	
	/**
	 * Shows the new external command action dialog for edition.
	 * 
	 * @param keywordList
	 */
	private void show_newKActionExternalCmdFormForEdition(KeywordAction action) {
		LOG.trace("ENTER");
		Object externalCmdForm = loadComponentFromFile(UI_FILE_NEW_KACTION_EXTERNAL_COMMAND_FORM);
		//Adds the date panel to it
		addDatePanel(externalCmdForm);
		Object list = find(externalCmdForm, COMPONENT_EXTERNAL_COMMAND_GROUP_LIST);
		List<Group> userGroups = getAllGroups();
		for (Group g : userGroups) {
			LOG.debug("Adding group [" + g.getName() + "] to list");
			Object item = createListItem(g.getName(), g);
			setIcon(item, Icon.GROUP);
			add(list, item);
		}
		setAttachedObject(externalCmdForm, action);
		//COMMAND TYPE
		setSelected(find(externalCmdForm, COMPONENT_RB_TYPE_HTTP), action.getExternalCommandType() == KeywordAction.EXTERNAL_HTTP_REQUEST);
		setSelected(find(externalCmdForm, COMPONENT_RB_TYPE_COMMAND_LINE), action.getExternalCommandType() == KeywordAction.EXTERNAL_COMMAND_LINE);
		
		//COMMAND
		setText(find(externalCmdForm, COMPONENT_TF_COMMAND), action.getUnformattedCommand());
		
		Object pnResponse = find(externalCmdForm, COMPONENT_PN_RESPONSE);
		//RESPONSE TYPE
		if (action.getExternalCommandResponseType() == KeywordAction.EXTERNAL_RESPONSE_PLAIN_TEXT) {
			LOG.debug("Setting up dialog for PLAIN TEXT response.");
			setSelected(find(externalCmdForm, COMPONENT_RB_PLAIN_TEXT), true);
			setSelected(find(externalCmdForm, COMPONENT_RB_FRONTLINE_COMMANDS), false);
			setSelected(find(externalCmdForm, COMPONENT_RB_NO_RESPONSE), false);
			
			activate(pnResponse);
			deactivate(list);
			//RESPONSE PANEL
			setText(find(externalCmdForm, COMPONENT_TF_MESSAGE), action.getUnformattedCommandText());
			int responseActionType = action.getCommandResponseActionType();
			setSelected(find(externalCmdForm, COMPONENT_CB_AUTO_REPLY),
						responseActionType == KeywordAction.TYPE_REPLY || responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD);
		
			if (responseActionType == KeywordAction.TYPE_FORWARD || responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD) {
				setSelected(find(externalCmdForm, COMPONENT_CB_FORWARD), true);
				activate(list);
				//Select group
				Group g = action.getGroup();
				for (Object item : getItems(list)) {
					Group it = getGroup(item);
					if (it.equals(g)) {
						LOG.debug("Selecting group [" + g.getName() + "].");
						setSelected(item, true);
						break;
					}
				}
			}
		} else if (action.getExternalCommandResponseType() == KeywordAction.EXTERNAL_RESPONSE_LIST_COMMANDS) {
			LOG.debug("Setting up dialog for LIST COMMANDS response.");
			setSelected(find(externalCmdForm, COMPONENT_RB_PLAIN_TEXT), false);
			setSelected(find(externalCmdForm, COMPONENT_RB_FRONTLINE_COMMANDS), true);
			setSelected(find(externalCmdForm, COMPONENT_RB_NO_RESPONSE), false);
			deactivate(pnResponse);
		} else {
			LOG.debug("Setting up dialog for NO response.");
			setSelected(find(externalCmdForm, COMPONENT_RB_PLAIN_TEXT), false);
			setSelected(find(externalCmdForm, COMPONENT_RB_FRONTLINE_COMMANDS), false);
			setSelected(find(externalCmdForm, COMPONENT_RB_NO_RESPONSE), true);
			deactivate(pnResponse);
		}
		
		//START and END dates
		setString(find(externalCmdForm, COMPONENT_TF_START_DATE), TEXT, InternationalisationUtils.getDateFormat().format(action.getStartDate()));
		Object endDate = find(externalCmdForm, COMPONENT_TF_END_DATE);
		String toSet = "";
		if (action.getEndDate() == DEFAULT_END_DATE) {
			toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
		} else {
			toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
		}
		setText(endDate, toSet);
		add(externalCmdForm);
		LOG.trace("EXIT");
	}
	
	/**
	 * Activates or deactivates the supplied panel according to user selection.
	 * 
	 * @param list
	 * @param selected
	 */
	public void controlExternalCommandResponseType(Object list, boolean selected) {
		if (selected) {
			activate(list);
		} else {
			deactivate(list);
		}
	}
	
	/**
	 * Shows the new forward message action dialog.
	 * 
	 * @param keywordList
	 */
	public void show_newKActionForwardForm(Object keywordList) {
		Keyword keyword = getKeyword(getSelectedItem(keywordList));
		Object forwardForm = loadComponentFromFile(UI_FILE_NEW_KACTION_FORWARD_FORM);
		//Adds the date panel to it
		addDatePanel(forwardForm);
		setAttachedObject(forwardForm, keyword);
		setText(find(forwardForm, COMPONENT_FORWARD_FORM_TITLE), InternationalisationUtils.getI18NString(COMMON_AUTO_FORWARD_FOR_KEYWORD) + " '" + keyword.getKeywordString() + "' " + InternationalisationUtils.getI18NString(COMMON_TO_GROUP) + ":");
		Object list = find(forwardForm, COMPONENT_FORWARD_FORM_GROUP_LIST);
		List<Group> userGroups = getAllGroups();
		for (Group g : userGroups) {
			Object item = createListItem(g.getName(), g);
			setIcon(item, Icon.GROUP);
			add(list, item);
		}
		add(forwardForm);
	}

	/**
	 * Shows the forward message action dialog for editing purpose.
	 * 
	 * @param action
	 */
	private void show_newKActionForwardFormForEdition(KeywordAction action) {
		Keyword keyword = action.getKeyword();
		Object forwardForm = loadComponentFromFile(UI_FILE_NEW_KACTION_FORWARD_FORM);
		//Adds the date panel to it
		addDatePanel(forwardForm);
		setAttachedObject(forwardForm, action);
		setString(find(forwardForm, COMPONENT_FORWARD_FORM_TITLE), TEXT, InternationalisationUtils.getI18NString(COMMON_AUTO_FORWARD_FOR_KEYWORD) + " '" + keyword.getKeywordString() + "' " + InternationalisationUtils.getI18NString(COMMON_TO_GROUP) + ":");
		Object list = find(forwardForm, COMPONENT_FORWARD_FORM_GROUP_LIST);
		List<Group> userGroups = getAllGroups();
		for (Group g : userGroups) {
			Object item = createListItem(g.getName(), g);
			setIcon(item, Icon.GROUP);
			if (g.getName().equals(action.getGroup().getName())) {
				setSelected(item, true);
			}
			add(list, item);
		}
		setText(find(forwardForm, COMPONENT_FORWARD_FORM_TEXTAREA), action.getUnformattedForwardText());
		
		setText(find(forwardForm, COMPONENT_TF_START_DATE), action == null ? "" : InternationalisationUtils.getDateFormat().format(action.getStartDate()));
		Object endDate = find(forwardForm, COMPONENT_TF_END_DATE);
		String toSet = "";
		if (action != null) {
			if (action.getEndDate() == DEFAULT_END_DATE) {
				toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
			} else {
				toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
			}
		}
		setText(endDate, toSet);
		add(forwardForm);
	}
	
	public void showEmailAccountDialog(Object list) {
		Object selected = getSelectedItem(list);
		if (selected != null) {
			EmailAccount acc = (EmailAccount) getAttachedObject(selected);
			showEmailAccountDialog(acc);
		}
	}
	
	/**
	 * Event fired when the view phone details action is chosen.
	 */
	public void showEmailAccountDialog(EmailAccount acc) {
		Object settingsDialog = loadComponentFromFile(UI_FILE_EMAIL_ACCOUNT_FORM);
		setText(settingsDialog, InternationalisationUtils.getI18NString(COMMON_EDITING_EMAIL_ACCOUNT, acc.getAccountName()));
		
		Object tfServer = find(settingsDialog, COMPONENT_TF_MAIL_SERVER);
		Object tfAccountName = find(settingsDialog, COMPONENT_TF_ACCOUNT);
		Object tfPassword = find(settingsDialog, COMPONENT_TF_ACCOUNT_PASS);
		Object cbUseSSL = find(settingsDialog, COMPONENT_CB_USE_SSL);
		Object tfPort = find(settingsDialog, COMPONENT_TF_ACCOUNT_SERVER_PORT);
		
		setText(tfServer, acc.getAccountServer());
		setText(tfAccountName, acc.getAccountName());
		setText(tfPassword, acc.getAccountPassword());
		setSelected(cbUseSSL, acc.useSsl());
		setText(tfPort, String.valueOf(acc.getAccountServerPort()));
		
		setAttachedObject(settingsDialog, acc);
		add(settingsDialog);
	}

	/**
	 * This method is used to show an export dialog, where the user can select the
	 * desired place to create the export file.
	 */
	public void show_exportDialogForm(Object o) {
		String name = getString(o, Thinlet.NAME);
		Object exportDialog = loadComponentFromFile(UI_FILE_EXPORT_DIALOG_FORM);
		setAttachedObject(exportDialog, name);
		add(exportDialog);
	}
	
	/**
	 * Creates a new forward message action.
	 */
	public void do_newKActionForward(Object forwardDialog, Object groupList, String forwardText) {
		LOG.trace("ENTER");
		Group group = getGroup(getSelectedItem(groupList));
		if (group != null) {
			String startDate = getString(find(forwardDialog, COMPONENT_TF_START_DATE), Thinlet.TEXT);
			String endDate = getString(find(forwardDialog, COMPONENT_TF_END_DATE), Thinlet.TEXT);
			LOG.debug("Start Date [" + startDate + "]");
			LOG.debug("End Date [" + endDate + "]");
			if (startDate.equals("")) {
				LOG.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
				startDate = InternationalisationUtils.getDefaultStartDate();
			}
			long start;
			long end;
			try {
				Date ds = InternationalisationUtils.parseDate(startDate); 
				if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
					Date de = InternationalisationUtils.parseDate(endDate);
					if (!Utils.validateDates(ds, de)) {
						LOG.debug("Start date is not before the end date");
						alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
						LOG.trace("EXIT");
						return;
					}
					end = de.getTime();
				} else {
					end = DEFAULT_END_DATE;
				}
				start = ds.getTime();
			} catch (ParseException e) {
				LOG.debug("Wrong format for date", e);
				alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
				LOG.trace("EXIT");
				return;
			} 
			KeywordAction action;
			boolean isNew = false;
			if (isAttachment(forwardDialog, KeywordAction.class)) {
				action = getKeywordAction(forwardDialog);
				LOG.debug("Editing action [" + action + "]. Setting new values!");
				action.setGroup(group);
				action.setForwardText(forwardText);
				action.setStartDate(start);
				action.setEndDate(end);
			} else {
				isNew = true;
				Keyword keyword = getKeyword(forwardDialog);
				LOG.debug("Creating action for keyword [" + keyword.getKeywordString() + "]");
				action = KeywordAction.createForwardAction(keyword, group, forwardText, start, end);
				keywordActionFactory.saveKeywordAction(action);
			}
			updateKeywordActionList(action, isNew);
			remove(forwardDialog);
		} else {
			alert(InternationalisationUtils.getI18NString(MESSAGE_NO_GROUP_SELECTED_TO_FWD));
		}
		LOG.trace("EXIT");
	}

	/**
	 * Creates a email message action.
	 */
	public void do_newKActionEmail(Object mailDialog, Object mailList) {
		LOG.trace("ENTER");
		String message = getText(find(mailDialog, COMPONENT_TF_MESSAGE));
		String recipients = getText(find(mailDialog, COMPONENT_TF_RECIPIENT));
		String subject = getText(find(mailDialog, COMPONENT_TF_SUBJECT));
		LOG.debug("Message [" + message + "]");
		LOG.debug("Recipients [" + recipients + "]");
		LOG.debug("Subject [" + subject + "]");
		if (recipients.equals("") || recipients.equals(";")) {
			LOG.debug("No valid recipients.");
			alert(InternationalisationUtils.getI18NString(MESSAGE_BLANK_RECIPIENTS));
			return;
		}
		EmailAccount account = (EmailAccount) getAttachedObject(getSelectedItem(mailList));
		if (account == null) {
			LOG.debug("No account selected to send the e-mail from.");
			alert(InternationalisationUtils.getI18NString(MESSAGE_NO_ACCOUNT_SELECTED_TO_SEND_FROM));
			return;
		}
		LOG.debug("Account [" + account.getAccountName() + "]");
		String startDate = getString(find(mailDialog, COMPONENT_TF_START_DATE), Thinlet.TEXT);
		String endDate = getString(find(mailDialog, COMPONENT_TF_END_DATE), Thinlet.TEXT);
		LOG.debug("Start Date [" + startDate + "]");
		LOG.debug("End Date [" + endDate + "]");
		if (startDate.equals("")) {
			LOG.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
			startDate = InternationalisationUtils.getDefaultStartDate();
		}
		long start;
		long end;
		try {
			Date ds = InternationalisationUtils.parseDate(startDate); 
			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
				Date de = InternationalisationUtils.parseDate(endDate);
				if (!Utils.validateDates(ds, de)) {
					LOG.debug("Start date is not before the end date");
					alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
					LOG.trace("EXIT");
					return;
				}
				end = de.getTime();
			} else {
				end = DEFAULT_END_DATE;
			}
			start = ds.getTime();
		} catch (ParseException e) {
			LOG.debug("Wrong format for date", e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
			LOG.trace("EXIT");
			return;
		} 
		KeywordAction action = null;
		boolean isNew = false;
		if (isAttachment(mailDialog, KeywordAction.class)) {
			action = getKeywordAction(mailDialog);
			LOG.debug("We are editing action [" + action + "]. Setting new values.");
			action.setEmailAccount(account);
			action.setReplyText(message);
			action.setEmailRecipients(recipients);
			action.setEmailSubject(subject);
			action.setStartDate(start);
			action.setEndDate(end);
		} else {
			isNew = true;
			Keyword keyword = getKeyword(mailDialog);
			LOG.debug("Creating new action  for keyword[" + keyword.getKeywordString() + "].");
			action = KeywordAction.createEmailAction(keyword, message, account, recipients, subject,start, end);
			keywordActionFactory.saveKeywordAction(action);
		}
		updateKeywordActionList(action, isNew);
		remove(mailDialog);
		LOG.trace("EXIT");
	}
	
	private void updateKeywordActionList(KeywordAction action, boolean isNew) {
		Object table = find(COMPONENT_ACTION_LIST);
		if (isNew) {
			add(table, getRow(action));
		} else {
			int index = -1;
			for (Object o : getItems(table)) {
				KeywordAction a = getKeywordAction(o);
				if (a.equals(action)) {
					index = getIndex(table, o);
					remove(o);
				}
			}
			add(table, getRow(action), index);
		}
	}

	/**
	 * Creates a new forward message action.
	 */
	public void do_newKActionExternalCommand(Object externalCommandDialog) {
		LOG.trace("ENTER");
		String startDate = getString(find(externalCommandDialog, COMPONENT_TF_START_DATE), Thinlet.TEXT);
		String endDate = getString(find(externalCommandDialog, COMPONENT_TF_END_DATE), Thinlet.TEXT);
		LOG.debug("Start Date [" + startDate + "]");
		LOG.debug("End Date [" + endDate + "]");
		if (startDate.equals("")) {
			LOG.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
			startDate = InternationalisationUtils.getDefaultStartDate();
		}
		long start;
		long end;
		try {
			Date ds = InternationalisationUtils.parseDate(startDate); 
			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
				Date de = InternationalisationUtils.parseDate(endDate);
				if (!Utils.validateDates(ds, de)) {
					LOG.debug("Start date is not before the end date");
					alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
					LOG.trace("EXIT");
					return;
				}
				end = de.getTime();
			} else {
				end = DEFAULT_END_DATE;
			}
			start = ds.getTime();
		} catch (ParseException e) {
			LOG.debug("Wrong format for date", e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
			LOG.trace("EXIT");
			return;
		} 
		int commandType = isSelected(find(externalCommandDialog, COMPONENT_RB_TYPE_HTTP)) ? KeywordAction.EXTERNAL_HTTP_REQUEST : KeywordAction.EXTERNAL_COMMAND_LINE;
		String commandLine = getText(find(externalCommandDialog, COMPONENT_TF_COMMAND));
		int responseType = KeywordAction.EXTERNAL_RESPONSE_DONT_WAIT;
		if (isSelected(find(externalCommandDialog, COMPONENT_RB_PLAIN_TEXT))) {
			responseType = KeywordAction.EXTERNAL_RESPONSE_PLAIN_TEXT;
		} else if (isSelected(find(externalCommandDialog, COMPONENT_RB_FRONTLINE_COMMANDS))) {
			responseType = KeywordAction.EXTERNAL_RESPONSE_LIST_COMMANDS;
		}
		
		LOG.debug("Command type [" + commandType + "]");
		LOG.debug("Command [" + commandLine + "]");
		LOG.debug("Response type [" + responseType + "]");
		
		Group group = null;
		String message = null;
		int responseActionType = KeywordAction.EXTERNAL_DO_NOTHING; 
		if (responseType == KeywordAction.EXTERNAL_RESPONSE_PLAIN_TEXT) {
			boolean reply = isSelected(find(externalCommandDialog, COMPONENT_CB_AUTO_REPLY));
			boolean fwd = isSelected(find(externalCommandDialog, COMPONENT_CB_FORWARD));
			
			if (reply && fwd) {
				responseActionType = KeywordAction.EXTERNAL_REPLY_AND_FORWARD;
			} else if (reply) {
				responseActionType = KeywordAction.TYPE_REPLY;
			} else if (fwd) {
				responseActionType = KeywordAction.TYPE_FORWARD;
			}
			LOG.debug("Response Action type [" + responseActionType + "]");
			if (responseActionType == KeywordAction.TYPE_REPLY 
					|| responseActionType == KeywordAction.TYPE_FORWARD
					|| responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD) {
				message = getText(find(externalCommandDialog, COMPONENT_TF_MESSAGE));
				LOG.debug("Message [" + message + "]");
			}
			if (responseActionType == KeywordAction.TYPE_FORWARD 
					|| responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD) {
				group = getGroup(getSelectedItem(find(externalCommandDialog, COMPONENT_EXTERNAL_COMMAND_GROUP_LIST)));
				if (group == null) {
					LOG.debug("No group selected to forward");
					alert(InternationalisationUtils.getI18NString(MESSAGE_NO_GROUP_SELECTED_TO_FWD));
					LOG.trace("EXIT");
					return;
				}
				LOG.debug("Group [" + group.getName() + "]");
			}
		}
		KeywordAction action = null;
		boolean isNew = false;
		if (isAttachment(externalCommandDialog, KeywordAction.class)) {
			//Editing
			action = getKeywordAction(externalCommandDialog);
			LOG.debug("We are editing action [" + action + "]. Setting new values.");
			if (group != null) {
				action.setGroup(group);
			}
			action.setCommandLine(commandLine);
			action.setExternalCommandType(commandType);
			action.setExternalCommandResponseType(responseType);
			action.setCommandResponseActionType(responseActionType);
			action.setCommandText(message);
			action.setStartDate(start);
			action.setEndDate(end);
		} else {
			isNew = true;
			Keyword keyword = getKeyword(externalCommandDialog);
			LOG.debug("Creating new keyword action for keyword [" + keyword.getKeywordString() + "]");
			action = KeywordAction.createExternalCommandAction(
					keyword,
					commandLine,
					commandType,
					responseType,
					responseActionType,
					message,
					group,
					start,
					end
			);
			keywordActionFactory.saveKeywordAction(action);
		}
		updateKeywordActionList(action, isNew);
		remove(externalCommandDialog);
		LOG.trace("EXIT");
	}
	
	/**
	 * Method called when the user has selected the edit option inside the Keywords tab.
	 * 
	 * @param tree
	 */
	public void keywordManager_edit(Object tree) {
		LOG.trace("ENTER");
		Object selectedObj = getSelectedItem(tree);
		if (isAttachment(selectedObj, KeywordAction.class)) {
			//KEYWORD ACTION EDITION
			KeywordAction action = getKeywordAction(selectedObj);
			LOG.debug("Editing keyword action [" + action + "]");
			showActionEditDialog(action);
		} else {
			Keyword keyword = getKeyword(selectedObj);
			//KEYWORD EDITION
			LOG.debug("Editing keyword [" + keyword.getKeywordString() + "]");
			showKeywordDialogForEdition(keyword);
		} 
		LOG.trace("EXIT");
	}
	
	/**
	 * Method called when the user has finished to edit a keyword.
	 * 
	 * @param dialog The dialog, which is holding the current reference to the keyword being edited.
	 * @param desc The new description for the keyword.
	 */
	public void finishKeywordEdition(Object dialog, String desc) {
		LOG.trace("ENTER");
		Keyword key = getKeyword(dialog);
		LOG.debug("New description [" + desc + "] for keyword [" + key.getKeywordString() + "]");
		key.setDescription(desc);
		removeDialog(dialog);
		LOG.trace("EXIT");
	}
	
	/**
	 * This method invokes the correct edit dialog according to the supplied action type.
	 * 
	 * @param action
	 */
	private void showActionEditDialog(KeywordAction action) {
		switch (action.getType()) {
			case KeywordAction.TYPE_FORWARD:
				show_newKActionForwardFormForEdition(action);
				break;
			case KeywordAction.TYPE_JOIN: 
				showGroupSelecter(action, InternationalisationUtils.getI18NString(COMMON_KEYWORD) + " \"" + action.getKeyword().getKeyword()+ "\" " + InternationalisationUtils.getI18NString(COMMON_AUTO_LEAVE_GROUP) + ":", "do_newKActionJoin(groupSelecter, groupSelecter_groupList)");
				break;
			case KeywordAction.TYPE_LEAVE: 
				showGroupSelecter(action, InternationalisationUtils.getI18NString(COMMON_KEYWORD) + " \"" + action.getKeyword().getKeyword()+ "\" " + InternationalisationUtils.getI18NString(COMMON_AUTO_LEAVE_GROUP) + ":", "do_newKActionLeave(groupSelecter, groupSelecter_groupList)");
				break;
			case KeywordAction.TYPE_REPLY:
				show_newKActionReplyFormForEdition(action);
				break;
			case KeywordAction.TYPE_EXTERNAL_CMD:
				show_newKActionExternalCmdFormForEdition(action);
				break;
			case KeywordAction.TYPE_EMAIL:
				show_newKActionEmailFormForEdition(action);
				break;
			case KeywordAction.TYPE_SURVEY:
				show_newKActionSurveyFormForEdition(action);
				break;
		}
	}
	
	/**
	 * Shows the keyword dialog for edit purpose.
	 * 
	 * @param keyword The object to be edited.
	 */
	private void showKeywordDialogForEdition(Keyword keyword) {
		String key = keyword.getKeywordString().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeywordString();
		String title = InternationalisationUtils.getI18NString(COMMON_EDITING_KEYWORD, key);
		Object keywordForm = loadComponentFromFile(UI_FILE_NEW_KEYWORD_FORM);
		setAttachedObject(keywordForm, keyword);
		setString(find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_TITLE), TEXT, title);
		// Pre-populate the keyword textfield with currently-selected keyword string so that
		// a sub-keyword can easily be created.  Append a space to save the user from having
		// to do it!
		Object textField = find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_KEYWORD);
		Object textFieldDescription = find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_DESCRIPTION);
		setString(textField, TEXT, key);
		setBoolean(textField, Thinlet.ENABLED, false);
		if (keyword.getDescription() != null) 
			setText(textFieldDescription, keyword.getDescription());
		String method = "finishKeywordEdition(newKeywordForm, newKeywordForm_description.text)";
		setMethod(find(keywordForm, COMPONENT_NEW_KEYWORD_BUTTON_DONE), Thinlet.ATTRIBUTE_ACTION, method, keywordForm, this);
		add(keywordForm);
	}
	/**
	 * Creates a new auto reply action.
	 */
	public void do_newKActionReply(Object replyDialog, String replyText) {
		LOG.trace("ENTER");
		String startDate = getString(find(replyDialog, COMPONENT_TF_START_DATE), Thinlet.TEXT);
		String endDate = getString(find(replyDialog, COMPONENT_TF_END_DATE), Thinlet.TEXT);
		LOG.debug("Start Date [" + startDate + "]");
		LOG.debug("End Date [" + endDate + "]");
		if (startDate.equals("")) {
			LOG.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
			startDate = InternationalisationUtils.getDefaultStartDate();
		}
		long start;
		long end;
		try {
			Date ds = InternationalisationUtils.parseDate(startDate); 
			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
				Date de = InternationalisationUtils.parseDate(endDate);
				if (!Utils.validateDates(ds, de)) {
					LOG.debug("Start date is not before the end date");
					alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
					LOG.trace("EXIT");
					return;
				}
				end = de.getTime();
			} else {
				end = DEFAULT_END_DATE;
			}
			start = ds.getTime();
		} catch (ParseException e) {
			LOG.debug("Wrong format for date", e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
			LOG.trace("EXIT");
			return;
		} 
		boolean isNew = false;
		KeywordAction action;
		if (isAttachment(replyDialog, KeywordAction.class)) {
			action = getKeywordAction(replyDialog);
			LOG.debug("Editing action [" + action + "]. Setting new values!");
			action.setReplyText(replyText);
			action.setStartDate(start);
			action.setEndDate(end);
		} else {
			isNew = true;
			Keyword keyword = getKeyword(replyDialog);
			LOG.debug("Creating action for keyword [" + keyword.getKeywordString() + "].");
			action = KeywordAction.createReplyAction(keyword, replyText, start, end);
			keywordActionFactory.saveKeywordAction(action);
		}
		updateKeywordActionList(action, isNew);
		remove(replyDialog);
		LOG.trace("EXIT");
	}

	/**
	 * Creates a new survey action.
	 */
	public void do_newKActionSurvey(Object surveyDialog) {
		LOG.trace("ENTER");
		String startDate = getString(find(surveyDialog, COMPONENT_TF_START_DATE), Thinlet.TEXT);
		String endDate = getString(find(surveyDialog, COMPONENT_TF_END_DATE), Thinlet.TEXT);
		LOG.debug("Start Date [" + startDate + "]");
		LOG.debug("End Date [" + endDate + "]");
		if (startDate.equals("")) {
			LOG.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
			startDate = InternationalisationUtils.getDefaultStartDate();
		}
		long start;
		long end;
		try {
			Date ds = InternationalisationUtils.parseDate(startDate); 
			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
				Date de = InternationalisationUtils.parseDate(endDate);
				if (!Utils.validateDates(ds, de)) {
					LOG.debug("Start date is not before the end date");
					alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
					LOG.trace("EXIT");
					return;
				}
				end = de.getTime();
			} else {
				end = DEFAULT_END_DATE;
			}
			start = ds.getTime();
		} catch (ParseException e) {
			LOG.debug("Wrong format for date", e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
			LOG.trace("EXIT");
			return;
		} 
		boolean isNew = false;
		KeywordAction action;
		if (isAttachment(surveyDialog, KeywordAction.class)) {
			action = getKeywordAction(surveyDialog);
			LOG.debug("Editing action [" + action + "]. Setting new values!");
			action.setStartDate(start);
			action.setEndDate(end);
		} else {
			isNew = true;
			Keyword keyword = getKeyword(surveyDialog);
			LOG.debug("Creating action for keyword [" + keyword.getKeywordString() + "].");
			action = KeywordAction.createSurveyAction(keyword, start, end);
			keywordActionFactory.saveKeywordAction(action);
		}
		updateKeywordActionList(action, isNew);
		remove(surveyDialog);
		LOG.trace("EXIT");
	}
	
	/**
	 * Adds the $sender to the text, allowing the user to forward the sender.
	 */
	public void addSenderToForwardMessage(String currentText, Object textArea) {
		setText(textArea, currentText + ' ' + CsvUtils.MARKER_SENDER_NAME);
	}

	/**
	 * Adds the $content to the text, allowing the user to forward the message content.
	 */
	public void addMsgContentToForwardMessage(String currentText, Object textArea) {
		setText(textArea, currentText + ' ' + CsvUtils.MARKER_MESSAGE_CONTENT);
	}
	/**
	 * Creates a new join group action.
	 */
	public void do_newKActionJoin(Object groupSelecterDialog, Object groupList) {
		createActionLeaveOrJoin(groupSelecterDialog, groupList, true);
	}

	/**
	 * Creates an action to leave or join group, according to supplied information.
	 * 
	 * @param groupSelecterDialog
	 * @param groupList
	 * @param join
	 */
	private void createActionLeaveOrJoin(Object groupSelecterDialog,
			Object groupList, boolean join) {
		LOG.trace("ENTER");
		LOG.debug("Join [" + join + "]");
		Group group = getGroup(getSelectedItem(groupList));
		if (group == null) {
			LOG.debug("No group selected");
			alert(InternationalisationUtils.getI18NString(MESSAGE_NO_GROUP_SELECTED_TO_FWD));
			LOG.trace("EXIT");
			return;
		}
		String startDate = getString(find(groupSelecterDialog, COMPONENT_TF_START_DATE), Thinlet.TEXT);
		String endDate = getString(find(groupSelecterDialog, COMPONENT_TF_END_DATE), Thinlet.TEXT);
		LOG.debug("Start Date [" + startDate + "]");
		LOG.debug("End Date [" + endDate + "]");
		if (startDate.equals("")) {
			LOG.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
			startDate = InternationalisationUtils.getDefaultStartDate();
		}
		long start;
		long end;
		try {
			Date ds = InternationalisationUtils.parseDate(startDate); 
			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
				Date de = InternationalisationUtils.parseDate(endDate);
				if (!Utils.validateDates(ds, de)) {
					LOG.debug("Start date is not before the end date");
					alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
					LOG.trace("EXIT");
					return;
				}
				end = de.getTime();
			} else {
				end = DEFAULT_END_DATE;
			}
			start = ds.getTime();
		} catch (ParseException e) {
			LOG.debug("Wrong format for date", e);
			alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
			LOG.trace("EXIT");
			return;
		} 
		KeywordAction action;
		boolean isNew = false;;
		if (isAttachment(groupSelecterDialog, KeywordAction.class)) {
			action = getKeywordAction(groupSelecterDialog);
			LOG.debug("Editing action [" + action + "]. Setting new values!");
			action.setGroup(group);
			action.setStartDate(start);
			action.setEndDate(end);
		} else {
			isNew  = true;
			Keyword keyword = getKeyword(groupSelecterDialog);
			LOG.debug("Creating action for keyword [" + keyword.getKeywordString() + "].");
			if (join) {
				action = KeywordAction.createGroupJoinAction(keyword, group, start, end);
				keywordActionFactory.saveKeywordAction(action);
			} else {
				action = KeywordAction.createGroupLeaveAction(keyword, group, start, end);
				keywordActionFactory.saveKeywordAction(action);
			}
		}
		updateKeywordActionList(action, isNew);
		remove(groupSelecterDialog);
		LOG.trace("EXIT");
	}
	
	/*
	 * Presumably this should be part of the messaging panel controller 
	 */
	public void updateCost() {
		if (currentTab.equals(TAB_MESSAGE_HISTORY)) {
			updateMessageHistoryCost();
		}
	}

	// FIXME fire this on textfield lostFocus or textfield execution (<return> pressed)
	public void costChanged(String cost) {
		if (cost.length() == 0) this.setCostPerSms(0);
		else {
			try {
				double costPerSMS = (InternationalisationUtils.parseCurrency(cost))/* * Utils.TIMES_TO_INT*/;//FIXME this will likely give some very odd costs - needs adjusting for moving decimal point.
				this.setCostPerSms(costPerSMS);
			} catch (ParseException e) {
				alert("Did not understand currency value: " + cost + ".  Should be of the form: " + InternationalisationUtils.formatCurrency(123456.789)); // TODO i18n
			} 
		}
		updateCost();
	}
	
	/**
	 * Method called when an event is fired and should be added to the event list on the home tab.
	 * @param newEvent New instance of {@link Event} to be added to the list.
	 * TODO this should be handled by the {@link HomeTabController}
	 */
	public void newEvent(Event newEvent) {
		// TODO addition of the item to the event list should be done in the HomeTabController
		Object eventListComponent = find(COMPONENT_EVENTS_LIST);
		if(eventListComponent != null) {
			if (getItems(eventListComponent).length >= HomeTabController.EVENTS_LIMIT) {
				remove(getItem(eventListComponent, 0));
			}
			add(eventListComponent, getRow(newEvent));
		}
	}
	
	private Object getRow(Event newEvent) {
		Object row = createTableRow(newEvent);
		String icon = null;
		switch(newEvent.getType()) {
		case Event.TYPE_INCOMING_MESSAGE:
			icon = Icon.SMS_RECEIVE;
			break;
		case Event.TYPE_OUTGOING_MESSAGE:
			icon = Icon.SMS_SEND;
			break;
		case Event.TYPE_OUTGOING_MESSAGE_FAILED:
			icon = Icon.SMS_SEND_FAILURE;
			break;
		case Event.TYPE_OUTGOING_EMAIL:
			icon = Icon.EMAIL_SEND;
			break;
		case Event.TYPE_PHONE_CONNECTED:
			icon = Icon.PHONE_CONNECTED;
			break;
		case Event.TYPE_SMS_INTERNET_SERVICE_CONNECTED:
			icon = Icon.SMS_INTERNET_SERVICE_CONNECTED;
			break;
		case Event.TYPE_SMS_INTERNET_SERVICE_RECEIVING_FAILED:
			icon = Icon.SMS_INTERNET_SERVICE_RECEIVING_FAILED;
			break;
		}
		
		Object cell = createTableCell("");
		setIcon(cell, icon);
		add(row, cell);
		add(row, createTableCell(newEvent.getDescription()));
		add(row, createTableCell(InternationalisationUtils.getDatetimeFormat().format(newEvent.getTime())));
		return row;
	}

	/**
	 * Adds a constant substitution marker to the text of an email action's text area (a thinlet component).
	 * 
	 * @param type The index of the constant that should be inserted
	 * <li> 0 for Sender name
	 * <li> 1 for Sender number
	 * <li> 2 for Message Content
	 * <li> 3 for Keyword
	 * <li> 4 for Command Response
	 * <li> 5 for SMS id
	 */
	public void addConstantToCommand(String currentText, Object textArea, int type) {
		LOG.trace("ENTER");
		String toAdd = "";
		switch (type) {
			case 0:
				toAdd = CsvUtils.MARKER_SENDER_NAME;
				break;
			case 1:
				toAdd = CsvUtils.MARKER_SENDER_NUMBER;
				break;
			case 2:
				toAdd = CsvUtils.MARKER_MESSAGE_CONTENT;
				break;
			case 3:
				toAdd = CsvUtils.MARKER_KEYWORD_KEY;
				break;
			case 4:
				toAdd = CsvUtils.MARKER_COMMAND_RESPONSE;
				break;
			case 5:
				toAdd = CsvUtils.MARKER_SMS_ID;
				break;
		}
		LOG.debug("Setting [" + currentText + toAdd + "] to component [" + textArea + "]");
		setText(textArea, currentText + toAdd);
		setFocus(textArea);
		LOG.trace("EXIT");
	}

	public void setEmailFocusOwner(Object obj) {
		emailTabFocusOwner = obj;
	}
	
	public void addConstantToEmailDialog(Object tfSubject, Object tfMessage, int type) {
		Object toSet = tfMessage;
		Object focused = emailTabFocusOwner;
		if (focused.equals(tfSubject)) {
			toSet = tfSubject;
		}
		addConstantToCommand(getText(toSet), toSet, type);
	}
	
	/**
	 * Method invoked when the status for actions changes.
	 * 
	 * @param panel
	 * @param live
	 */
	public void statusChanged(Object panel, boolean live) {
		Object att = getAttachedObject(panel);
		Object startTextField = find(panel, COMPONENT_TF_START_DATE);
		Object endTextField = find(panel, COMPONENT_TF_END_DATE);
		if (att != null) {
			if (live) {
				setText(startTextField, InternationalisationUtils.getDefaultStartDate());
				if (getString(endTextField, Thinlet.TEXT).equals(InternationalisationUtils.getDefaultStartDate())) {
					setText(endTextField, "");
				}
			} else {
				setText(endTextField, InternationalisationUtils.getDefaultStartDate());
			}
		}
	}
	/**
	 * Creates a new leave group action.
	 */
	public void do_newKActionLeave(Object groupSelecterDialog, Object groupList) {
		createActionLeaveOrJoin(groupSelecterDialog, groupList, false);
	}

	/**
	 * Shows the new keyword dialog.
	 * 
	 * @param keywordList
	 */
	public void show_createKeywordForm(Object keywordList) {
		showNewKeywordForm(getKeyword(getSelectedItem(keywordList)));
	}

	/**
	 * Create a new keyword with the supplied information (newKeyword and description).
	 * 
	 * @param formPanel The panel to be removed from the application.
	 * @param newKeyword The desired keyword.
	 * @param description The description for this new keyword.
	 */
	public void do_createKeyword(Object formPanel, String newKeyword, String description) {
		LOG.trace("ENTER");
		LOG.debug("Creating keyword [" + newKeyword + "] with description [" + description + "]");
		Keyword keyword;
		try {
			keyword = createKeywordsHierarchically(newKeyword, description);
		} catch (DuplicateKeyException e) {
			alert(InternationalisationUtils.getI18NString(MESSAGE_KEYWORD_EXISTS));
			LOG.trace("EXIT");
			return;
		}
		updateKeywordList();
		remove(formPanel);
		LOG.trace("EXIT");
	}

	/**
	 * Shows the new keyword dialog.
	 * 
	 * @param keyword
	 */
	private void showNewKeywordForm(Keyword keyword) {
		String title = "Create new keyword.";
		Object keywordForm = loadComponentFromFile(UI_FILE_NEW_KEYWORD_FORM);
		setAttachedObject(keywordForm, keyword);
		setString(find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_TITLE), TEXT, title);
		// Pre-populate the keyword textfield with currently-selected keyword string so that
		// a sub-keyword can easily be created.  Append a space to save the user from having
		// to do it!
		if (keyword != null) setString(find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_KEYWORD), TEXT, keyword.getKeywordString() + ' ');
		add(keywordForm);
	}

	public boolean hasSomethingToDoBeforeExit() {
		LOG.trace("ENTER");
		Collection<Message> pending = messageFactory.getMessages(Message.TYPE_OUTBOUND, new Integer[] {Message.STATUS_PENDING});
		LOG.debug("Pending Messages size [" + pending.size() + "]");
		if (pending.size() > 0) {
			showPendingMessages(pending);
			LOG.trace("EXIT");
			return true;
		} else {
			saveWindowSize();
			LOG.trace("EXIT");
			return false;
		}
	}
	
	public void close() {
		LOG.trace("ENTER");
		Collection<Message> pending = messageFactory.getMessages(Message.TYPE_OUTBOUND, new Integer[] {Message.STATUS_PENDING});
		LOG.debug("Pending Messages size [" + pending.size() + "]");
		if (pending.size() > 0) {
			showPendingMessages(pending);
		} else {
			exit();
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * Method called when the user make the final decision to close the app.
	 */
	public void exit() {
		for (Message m : messageFactory.getMessages(Message.TYPE_OUTBOUND, new Integer[] {Message.STATUS_PENDING})) {
			m.setStatus(Message.STATUS_OUTBOX);
		}
		saveWindowSize();
		frameLauncher.dispose();
		System.exit(0);
	}

	/**
	 * Writes to the property file the current window size.
	 */
	private void saveWindowSize() {
		UiProperties uiProperties = UiProperties.getInstance();
		uiProperties.setWindowState(frameLauncher.getExtendedState() == Frame.MAXIMIZED_BOTH,
				frameLauncher.getBounds().width, frameLauncher.getBounds().height);
		uiProperties.saveToDisk();
	}
	
	/**
	 * Writes to the property file the current window size.
	 */
	private void savePropertiesBeforeChangingMode(boolean newMode) {
		UiProperties uiProperties = UiProperties.getInstance();
		uiProperties.setViewModeClassic(!newMode);
		uiProperties.setWindowState(frameLauncher.getExtendedState() == Frame.MAXIMIZED_BOTH,
				frameLauncher.getBounds().width, frameLauncher.getBounds().height);
		uiProperties.saveToDisk();
	}
	
	
	/**
	 * Checks if the object attached to a component is of a specific class.
	 * @param component
	 * @param clazz
	 * @return
	 */
	private boolean isAttachment(Object component, Class<?> clazz) {
		Object object = getAttachedObject(component);
		return object != null && object.getClass().equals(clazz);
	}
	
	/**
	 * Gets the Message instance attached to the supplied component.
	 * 
	 * @param component
	 * @return The Message instance.
	 */
	private Message getMessage(Object component) {
		return (Message) getAttachedObject(component);
	}

	/**
	 * Gets the EMail instance attached to the supplied component.
	 * 
	 * @param component
	 * @return The Email instance.
	 */
	private Email getEmail(Object component) {
		return (Email) getAttachedObject(component);
	}
	
	/**
	 * Gets the Contact instance attached to the supplied component.
	 * 
	 * @param component
	 * @return The Contact instance.
	 */
	public Contact getContact(Object component) {
		return (Contact) getAttachedObject(component);
	}
	
	/**
	 * Gets the Group instance attached to the supplied component.
	 * 
	 * @param component
	 * @return The Group instance.
	 */
	public Group getGroup(Object component) {
		return (Group) getAttachedObject(component);
	}
	/**
	 * Gets the PhoneHandler instance attached to the supplied component.
	 * 
	 * @param component
	 * @return The PhoneHandler instance.
	 */
	private SmsDevice getDeviceHandler(Object component) {
		return (SmsDevice) getAttachedObject(component);
	}

	/**
	 * Returns the keyword attached to the supplied component.
	 * 
	 * @param component
	 * @return
	 */
	private Keyword getKeyword(Object component) {
		Object obj = getAttachedObject(component);
		if (obj instanceof Keyword) return (Keyword)obj;
		else if (obj instanceof KeywordAction) return ((KeywordAction)obj).getKeyword();
		else if (obj == null) return null;
		else throw new RuntimeException();
	}
	/**
	 * Returns the keyword action attached to the supplied component.
	 * 
	 * @param component
	 * @return
	 */
	KeywordAction getKeywordAction(Object component) {
		Object obj = getAttachedObject(component);
		if (obj == null) return null;
		else if (obj instanceof KeywordAction) {
			return (KeywordAction)obj;	
		} else throw new RuntimeException();
	}

	/**
	 * Get's the group from the selected node of the groups list
	 * @param selected
	 * @return
	 */
	private Group getGroupFromSelectedNode(Object selected) {
		while (selected != null && !isAttachment(selected, Group.class)) selected = getParent(selected);
		if (selected == null) return null;
		return getGroup(selected);
	}

	/**
	 * Creates a row for the supplied keyword action.
	 * 
	 * @param action
	 * @return
	 */
	private Object getReplyManagerRow(KeywordAction action) {
		Object row = createTableRow(action);
		Keyword keyword = action.getKeyword();
		if (action.isAlive()) {
			add(row, createTableCell(InternationalisationUtils.getI18NString(COMMON_LIVE)));
		} else {
			add(row, createTableCell(InternationalisationUtils.getI18NString(COMMON_DORMANT)));
		}
		String key = keyword.getKeywordString().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeywordString();
		add(row, createTableCell(key));
		add(row, createTableCell(KeywordAction.KeywordUtils.getReplyText(action, DEMO_SENDER, DEMO_SENDER_MSISDN, DEMO_MESSAGE_TEXT_INCOMING, DEMO_MESSAGE_KEYWORD)));
		add(row, createTableCell(InternationalisationUtils.getDateFormat().format(action.getStartDate())));
		if (action.getEndDate() != DEFAULT_END_DATE) add(row, createTableCell(InternationalisationUtils.getDateFormat().format(action.getEndDate())));
		else add(row, createTableCell(InternationalisationUtils.getI18NString(COMMON_UNDEFINED)));
		add(row, createTableCell(Integer.toString(action.getCounter())));
		return row;
	}
	
	/**
	 * Creates a row for the supplied keyword.
	 * 
	 * @param action
	 * @return
	 */
	private Object surveyManager_getRow(KeywordAction action) {
		Object surveyRow = createTableRow(action);
		Keyword keyword = action.getKeyword();
		if (action.isAlive()) {
			add(surveyRow, createTableCell(InternationalisationUtils.getI18NString(COMMON_LIVE)));
		} else {
			add(surveyRow, createTableCell(InternationalisationUtils.getI18NString(COMMON_DORMANT)));
		}
		String key = keyword.getKeywordString().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeywordString();
		add(surveyRow, createTableCell(key));
		add(surveyRow, createTableCell(keyword.getDescription()));
		add(surveyRow, createTableCell(InternationalisationUtils.getDateFormat().format(action.getStartDate())));
		if (action.getEndDate() != DEFAULT_END_DATE) add(surveyRow, createTableCell(InternationalisationUtils.getDateFormat().format(action.getEndDate())));
		else add(surveyRow, createTableCell(InternationalisationUtils.getI18NString(COMMON_UNDEFINED)));
		add(surveyRow, createTableCell(Integer.toString(action.getCounter())));
		return surveyRow;
	}
	/**
	 * Creates a node for the supplied group, creating nodes for its sub-groups and contacts as well.
	 * 
	 * @param group The group to be put into a node.
	 * @param showContactsNumber set <code>true</code> to show the number of contacts per group in the node's text or <code>false</code> otherwise
	 *   TODO removing this argument, and treating it as always <code>false</code> speeds up the contact tab a lot
	 * @return
	 */
	public Object getNode(Group group, boolean showContactsNumber) {
		LOG.trace("ENTER");
		
		LOG.debug("Group [" + group.getName() + "]");
		
		String toSet = group.getName();
		if (showContactsNumber) {
			toSet += " (" + group.getAllMembers().size() + ")";
		}
		
		Object node = createNode(toSet, group);

		if ((getBoolean(node, EXPANDED) && group.hasDescendants()) || group == this.rootGroup) {
			setIcon(node, Icon.FOLDER_OPEN);
		} else {
			setIcon(node, Icon.FOLDER_CLOSED);
		}
		
		if (group.equals(this.unnamedContacts)) {
			setString(node, TOOLTIP, InternationalisationUtils.getI18NString(TOOLTIP_UNNAMED_GROUP));
		} else if(group.equals(this.ungroupedContacts)) {
			setString(node, TOOLTIP, InternationalisationUtils.getI18NString(TOOLTIP_UNGROUPED_GROUP));
		} 
		
		if (group == rootGroup) {
			add(node, getNode(this.ungroupedContacts, showContactsNumber));
			add(node, getNode(this.unnamedContacts, showContactsNumber));
		}
		
		// Add subgroup components to this node
		for (Group subGroup : group.getDirectSubGroups()) {
			Object groupNode = getNode(subGroup, showContactsNumber);
			add(node, groupNode);
		}
		LOG.trace("EXIT");
		return node;
	}
	
	/**
	 * Method called when group manager contact list component changes its selection. 
	 * 
	 * @param contactList The contact list, which selection had changed.
	 * @param contactDetails The panel to show the selected contact details.
	 */
	public void groupManager_contactListSelectionChange(Object contactList, Object contactDetails) {
		activate(find(COMPONENT_GROUP_MANAGER_DELETE_CONTACTS_BUTTON));
		activate(find(COMPONENT_GROUP_MANAGER_SEND_SMS_BUTTON));
		Contact contact = getContact(getSelectedItem(contactList));
		contactDetails_show(contactDetails, contact, null);
	}

	/**
	 * Hides the contact details from contact manager tab (classic mode).
	 * 
	 * @param contactDetails
	 */
	private void contactDetails_hide(Object contactDetails) {
		deactivate(contactDetails);

		setAttachedObject(contactDetails, null);

		contactDetails_setName(contactDetails, "");
		contactDetails_setMobileMsisdn(contactDetails, "");
		contactDetails_setOtherMsisdn(contactDetails, "");
		contactDetails_setEmailAddress(contactDetails, "");
		contactDetails_setNotes(contactDetails, "");
		contactDetails_setActive(contactDetails, true);
	}

	/**
	 * Update the icon for active/dormant.
	 * @param radioButton
	 * @param label
	 */
	public void updateIconActive(Object radioButton, Object label) {
		String icon;
		if (getString(radioButton, Thinlet.NAME).equals(COMPONENT_RADIO_BUTTON_ACTIVE)) {
			icon = Icon.ACTIVE;
		} else {
			icon = Icon.DORMANT;
		}
		setIcon(label, icon);
	}

	/**
	 * Sets the contact values to the fields, allowing user to edit. Enable all the fileds for edition.
	 * <br> If contact is null, it means that we are creating a new contact,
	 * then we just set the fields to their default values.
	 * 
	 * @param contactDetails
	 * @param contact The contact to be shown, or null if it is a creation.
	 * @param group If contact is not null, we will create the contact into this group.
	 */
	private void contactDetails_show(Object contactDetails, Contact contact, Group group) {
		if (contact != null) setAttachedObject(contactDetails, contact);
		else setAttachedObject(contactDetails, group);

		String name;
		String mobileMsisdn;
		String otherMsisdn;
		String emailAddress;
		String notes;
		boolean active;
		
		if (contact != null) {
			name = contact.getName();
			mobileMsisdn = contact.getMsisdn();
			otherMsisdn = contact.getOtherMsisdn();
			emailAddress = contact.getEmailAddress();
			notes = contact.getNotes();
			active = contact.isActive();
		} else {
			name = "";
			mobileMsisdn = "";
			otherMsisdn = "";
			emailAddress = "";
			notes = "";
			active = true;
			
			// Disable the contacts pane, as we're creating a new one
			deactivate(find("groupManager_contactListPanel"));
			deactivate(find("groupManager_groupList"));
			deactivate(find("groupManager_toolbar"));
			
			// Focus on the first input box for the new contact
			setFocus(find(contactDetails, "contact_name"));
		}
		contactDetails_setName(contactDetails, name);
		contactDetails_setMobileMsisdn(contactDetails, mobileMsisdn);
		contactDetails_setOtherMsisdn(contactDetails, otherMsisdn);
		contactDetails_setEmailAddress(contactDetails, emailAddress);
		contactDetails_setNotes(contactDetails, notes);
		contactDetails_setActive(contactDetails, active);
		activate(contactDetails);
	}

	/**
	 * Deletes the survey list that is selected in the list.  The survey is deleted from the
	 * system, not just from the UI. 
	 * @param surveyList
	 */
	public void surveyManager_deleteSelected() {
		removeConfirmationDialog();
		Object surveyList = find(COMPONENT_SURVEY_LIST);
		Object selected = getSelectedItem(surveyList);
		if (selected != null) {
			KeywordAction surveyAction = getKeywordAction(selected);
			keywordActionFactory.deleteKeywordAction(surveyAction);
			surveyManager_refresh();
		}
	}

	/** 
	 * Refresh the display of the Survey Manager.
	 */
	private void surveyManager_refresh() {
		Object surveyListComponent = find(COMPONENT_SURVEY_LIST);
		removeAll(surveyListComponent);
		for(KeywordAction action : keywordActionFactory.getSurveysActions()) {
			add(surveyListComponent, surveyManager_getRow(action));
		}
		surveyManager_updateSurveyDetails(find(COMPONENT_SURVEY_DETAILS), null, false);
	}

	public void keywordTab_doSave(Object panel) {
		LOG.trace("ENTER");
		int index = getSelectedIndex(keywordListComponent);
		long startDate;
		try {
			startDate = InternationalisationUtils.parseDate(InternationalisationUtils.getDefaultStartDate()).getTime();
		} catch (ParseException e) {
			LOG.debug("We never should get this", e);
			LOG.trace("EXIT");
			return;
		}
		String reply;
		Group join;
		Group leave;
		Keyword keyword;
		if (index == 0) {
			//Adding keyword as well as actions
			String newkeyword = getText(find(panel, COMPONENT_TF_KEYWORD));
			try {
				keyword = createKeywordsHierarchically(newkeyword, "");
			} catch (DuplicateKeyException e) {
				alert(InternationalisationUtils.getI18NString(MESSAGE_KEYWORD_EXISTS));
				LOG.trace("EXIT");
				return;
			}
			updateKeywordList();
			reply = keywordSimple_getAutoReply(panel);
			join = keywordSimple_getJoin(panel);
			leave = keywordSimple_getLeave(panel);
		} else {
			//Editing an existent one.
			Object selected = getSelectedItem(keywordListComponent);
			keyword = getKeyword(selected);
			reply = keywordSimple_getAutoReply(panel);
			KeywordAction found = Utils.findKeywordAction(keyword, KeywordAction.TYPE_REPLY);
			if (found != null) {
				if (reply == null) {
					keywordActionFactory.deleteKeywordAction(found);
				} else {
					found.setReplyText(reply);
					//We set null to don't add it in the end
					reply = null;
				}
			}
			join = keywordSimple_getJoin(panel);
			found = Utils.findKeywordAction(keyword, KeywordAction.TYPE_JOIN);
			if (found != null) {
				if (reply == null) {
					keywordActionFactory.deleteKeywordAction(found);
				} else {
					found.setGroup(join);
					//We set null to don't add it in the end
					join = null;
				}
			}
			leave = keywordSimple_getLeave(panel);
			found = Utils.findKeywordAction(keyword, KeywordAction.TYPE_LEAVE);
			if (found != null) {
				if (reply == null) {
					keywordActionFactory.deleteKeywordAction(found);
				} else {
					found.setGroup(leave);
					//We set null to don't add it in the end
					leave = null;
				}
			}
		}
		if (reply != null) {
			KeywordAction action = KeywordAction.createReplyAction(keyword, reply, startDate, DEFAULT_END_DATE);
			keywordActionFactory.saveKeywordAction(action);
		}
		if (join != null) {
			KeywordAction action = KeywordAction.createGroupJoinAction(keyword, join, startDate, DEFAULT_END_DATE);
			keywordActionFactory.saveKeywordAction(action);
		}
		if (leave != null) {
			KeywordAction action = KeywordAction.createGroupLeaveAction(keyword, leave, startDate, DEFAULT_END_DATE);
			keywordActionFactory.saveKeywordAction(action);
		}
		setStatus(InternationalisationUtils.getI18NString(MESSAGE_KEYWORD_SAVED));
		if (index == 0) {
			keywordTab_doClear(panel);
		}
		LOG.trace("EXIT");
	}
	
	private String keywordSimple_getAutoReply(Object panel) {
		String ret = null;
		if (isSelected(find(panel, COMPONENT_CB_AUTO_REPLY))) {
			ret = getText(find(panel, COMPONENT_TF_AUTO_REPLY));
		}
		return ret;
	}

	public void classicMode_enableOptionsMessageList(Object popup, Object list) {
		setVisible(popup, getSelectedItems(list).length > 0);
	}
	
	private Group keywordSimple_getJoin(Object panel) {
		Group ret = null;
		if (isSelected(find(panel, COMPONENT_CB_JOIN_GROUP))) {
			ret = (Group) getAttachedObject(getSelectedItem(find(panel, COMPONENT_CB_GROUPS_TO_JOIN)));
		}
		return ret;
	}
	
	private Group keywordSimple_getLeave(Object panel) {
		Group ret = null;
		if (isSelected(find(panel, COMPONENT_CB_LEAVE_GROUP))) {
			ret = (Group) getAttachedObject(getSelectedItem(find(panel, COMPONENT_CB_GROUPS_TO_LEAVE)));
		}
		return ret;
	}
	
	public void keywordTab_doClear(Object panel) {
		setText(find(panel, COMPONENT_TF_KEYWORD), "");
        setSelected(find(panel, COMPONENT_CB_AUTO_REPLY), false);
        setText(find(panel, COMPONENT_TF_AUTO_REPLY), "");
        setSelected(find(panel, COMPONENT_CB_JOIN_GROUP), false);
        setSelectedIndex(find(panel, COMPONENT_CB_GROUPS_TO_JOIN), 0);
        setSelected(find(panel, COMPONENT_CB_LEAVE_GROUP), false);
        setSelectedIndex(find(panel, COMPONENT_CB_GROUPS_TO_LEAVE), 0);
	}
	
	/** 
	 * Refreshes the display of the Survey Analyst
	 */
	private void surveyAnalyst_refresh() {
		if(classicMode) {
			Object surveyAnalystKeywordList = find(COMPONENT_ANALYST_KEYWORD_LIST);
			removeAll(surveyAnalystKeywordList);
			for (KeywordAction act : keywordActionFactory.getSurveysActions()) {
				add(surveyAnalystKeywordList, createListItem(act));
			}
			cleanSurveyAnalystMessages();
			setEnabled(find("surveyAnalyst_exportButton"), false);
		}
	}

	/**
	 * Updates the message lists inside the <b>Message Tracker</b> tab.
	 */
	public void messageTracker_refresh() {
		messageTracker_refreshPendingMessageList();
		messageTracker_refreshFailedMessageList();
	}
	
	private static final Integer[] MESSAGE_TRACKER_PENDING_STATUSES = new Integer[]{Message.STATUS_DRAFT, Message.STATUS_OUTBOX, Message.STATUS_PENDING};
	private static final Integer[] MESSAGE_TRACKER_FAILED_STATUSES = new Integer[]{Message.STATUS_SENT, Message.STATUS_KEEP_TRYING, Message.STATUS_DELIVERED, Message.STATUS_ABORTED, Message.STATUS_UNKNOWN, Message.STATUS_FAILED};
	
	/**
	 * Updates the pending message list inside the <b>Message Tracker</b> tab.
	 */
	private void messageTracker_refreshPendingMessageList() {
		messageTracker_updateMessageList(COMPONENT_MESSAGE_TRACKER_PENDING_MESSAGE_LIST, MESSAGE_TRACKER_PENDING_STATUSES);
	}
	
	/**
	 * Updates the failed message list inside the <b>Message Tracker</b> tab.
	 */
	private void messageTracker_refreshFailedMessageList() {
		messageTracker_updateMessageList(COMPONENT_MESSAGE_TRACKER_FAILED_MESSAGE_LIST, MESSAGE_TRACKER_FAILED_STATUSES);
	}
	
	private final void messageTracker_updateMessageList(String listComponentName, Integer[] messageStati) {
		LOG.trace("UiGeneratorController.messageTracker_updateMessageList()");
		Object messageList = find(listComponentName);
		removeAll(messageList);
		int listLimit = getListLimit(messageList);
		int count = messageFactory.getMessageCount(Message.TYPE_OUTBOUND, messageStati);
		int currentPage = getListCurrentPage(messageList);
		setListElementCount(count, messageList);
		for (Message m : messageFactory.getMessagesForStati(Message.TYPE_OUTBOUND, messageStati,
							Message.Field.DATE, Order.DESCENDING, (currentPage-1) * listLimit, listLimit)) {
			add(messageList, messageTracker_getRow(m));
		}
		updatePageNumber(getParent(messageList), count, currentPage, listLimit);
	}
	
	/**
	 * Creates a row for the supplied message for the <b>Message Tracker</b> tab.
	 * 
	 * @param action
	 * @return
	 */
	private Object messageTracker_getRow(Message message) {
		Object row = createTableRow(message);

		String recipientDisplayName = getRecipientDisplayValue(message);

		String senderDisplayName = getSenderDisplayValue(message);
		
		add(row, createTableCell(getMessageStatusAsString(message)));
		add(row, createTableCell(InternationalisationUtils.getDatetimeFormat().format(message.getDate())));
		add(row, createTableCell(senderDisplayName));
		add(row, createTableCell(recipientDisplayName));
		add(row, createTableCell(message.getTextContent()));

		return row;
	}

	/**
	 * Updates the received message list inside <b>Receive Console</b> tab.
	 */
	public void receiveConsole_refresh() {
		receiveConsole_updateConnectedPhones();
		Object receiveConsole_messageList = find(COMPONENT_RECEIVE_CONSOLE_MESSAGE_LIST);
		List<? extends Message> messages = messageFactory.getMessages(Message.TYPE_RECEIVED, Message.Field.DATE, Order.DESCENDING);
		setListContents(receiveConsole_messageList, messages);
		setListPageNumber(1, receiveConsole_messageList);
		setListElementCount(messages.size(), receiveConsole_messageList);
		updatePages_receiveConsole();
	}
	/**
	 * Creates a row for the supplied message for the <b>Receive Console</b> tab.
	 * 
	 * @param message
	 * @return
	 */
	private Object receiveConsole_getRow(Message message) {
		Object row = createTableRow(message);

		Contact sender = contactFactory.getFromMsisdn(message.getSenderMsisdn());

		String senderDisplayName = null;

		if (sender == null) {
			senderDisplayName = message.getSenderMsisdn();
		} else {
			senderDisplayName = sender.getDisplayName();
		}

		String recipientDisplayName = getRecipientDisplayValue(message);
		
		add(row, createTableCell(InternationalisationUtils.getDatetimeFormat().format(message.getDate())));
		add(row, createTableCell(senderDisplayName));
		add(row, createTableCell(recipientDisplayName));
		add(row, createTableCell(message.getTextContent()));

		return row;
	}

	/**
	 * Updates everything on the <b>Send</b> tab.
	 */
	private void sendConsole_refresh() {
		// The send console needs to be updated.  Therefore we need to update:
		//  - sendConsole_groupTree
		//  - sendConsole_modemList
		//  - sendConsole_resultList
		sendConsole_refreshGroupTree();
		sendConsole_refreshModemList();
		sendConsole_refreshMessageList();
	}

	/**
	 * Updates the group tree on the <b>Send</b> tab.
	 */
	private void sendConsole_refreshGroupTree() {
		Object sendConsole_groupList = find(COMPONENT_SEND_CONSOLE_GROUP_TREE);
		removeAll(sendConsole_groupList);
		add(sendConsole_groupList, getNode(this.rootGroup, true));
	}

	/**
	 * @param dev
	 * @return
	 */
	private boolean isSmsModem(SmsDevice dev) {
		return dev instanceof SmsModem;
	}
	
	/**
	 * Updates the phone list on the <b>Send</b> tab.
	 */
	private void sendConsole_refreshModemList() {
		Object sendConsole_modemList = find(COMPONENT_SEND_CONSOLE_MODEM_LIST);
		int index = getSelectedIndex(sendConsole_modemList);
		
		removeAll(sendConsole_modemList);
		Object row = createTableRow(null);
		add(row, createTableCell(InternationalisationUtils.getI18NString(COMMON_ALL)));
		add(sendConsole_modemList, row);
		for (SmsDevice dev : phoneManager.getAllPhones()) {
			if (dev.isConnected() && dev.isUseForSending()) 
				add(sendConsole_modemList, sendConsole_getRow(dev));
		}
		
		setSelectedIndex(sendConsole_modemList, index);
	}
	
	public void sendConsole_refreshMessageList() {
		sendConsole_refreshMessageList(true);
	}
	
	/**
	 * Updates the message list on the <b>Send</b> tab.
	 */
	private void sendConsole_refreshMessageList(boolean goToFirstPage) {
		Object sendConsole_messageList = find(COMPONENT_SEND_CONSOLE_MESSAGE_LIST);
		List<? extends Message> listContents = messageFactory.getMessages(Message.TYPE_OUTBOUND, Message.Field.DATE, Order.DESCENDING);
		setListContents(sendConsole_messageList, listContents);
		setListElementCount(listContents.size(), sendConsole_messageList);
		if(goToFirstPage) setListPageNumber(1, sendConsole_messageList);
		
		updatePages_sendConsoleMessageList();
	}
	/**
	 * Creates a row for the supplied message for the <b>Send</b> tab.
	 * 
	 * @param message
	 * @return
	 */
	private Object sendConsole_getRow(Message message) {
		Object row = createTableRow(message);

		String recipientDisplayName = getRecipientDisplayValue(message);

		String senderDisplayName = getSenderDisplayValue(message);
		
		add(row, createTableCell(getMessageStatusAsString(message)));
		add(row, createTableCell(InternationalisationUtils.getDatetimeFormat().format(message.getDate())));
		add(row, createTableCell(senderDisplayName));
		add(row, createTableCell(recipientDisplayName));
		add(row, createTableCell(message.getTextContent()));

		return row;
	}
	
	/**
	 * Get the status of a {@link Message} as a {@link String}.
	 * @param message
	 * @return {@link String} representation of the status.
	 */
	public static final String getMessageStatusAsString(Message message) {
		switch(message.getStatus()) {
			case Message.STATUS_DRAFT:
				return "(draft)";
			case Message.STATUS_RECEIVED:
				return InternationalisationUtils.getI18NString(COMMON_RECEIVED);
			case Message.STATUS_OUTBOX:
				return InternationalisationUtils.getI18NString(COMMON_OUTBOX);
			case Message.STATUS_PENDING:
				return InternationalisationUtils.getI18NString(COMMON_PENDING);
			case Message.STATUS_SENT:
				return InternationalisationUtils.getI18NString(COMMON_SENT);
			case Message.STATUS_DELIVERED:
				return InternationalisationUtils.getI18NString(COMMON_DELIVERED);
			case Message.STATUS_KEEP_TRYING:
				return InternationalisationUtils.getI18NString(COMMON_RETRYING);
			case Message.STATUS_ABORTED:
				return "(aborted)";
			case Message.STATUS_FAILED:
				return InternationalisationUtils.getI18NString(COMMON_FAILED);
			case Message.STATUS_UNKNOWN:
			default:
				return "(unknown)";
		}
	}
	
	/**
	 * Get the status of a {@link Email} as a {@link String}.
	 * @param email
	 * @return {@link String} representation of the status.
	 */
	public static final String getEmailStatusAsString(Email email) {
		switch(email.getStatus()) {
		case Email.STATUS_OUTBOX:
			return InternationalisationUtils.getI18NString(COMMON_OUTBOX);
		case Email.STATUS_PENDING:
			return InternationalisationUtils.getI18NString(COMMON_PENDING);
		case Email.STATUS_SENT:
			return InternationalisationUtils.getI18NString(COMMON_SENT);
		case Email.STATUS_RETRYING:
			return InternationalisationUtils.getI18NString(COMMON_RETRYING);
		case Email.STATUS_FAILED:
			return InternationalisationUtils.getI18NString(COMMON_FAILED);
		default:
			return "(unknown)";
		}
	}
	
	/**
	 * Creates a row for the supplied phone for the <b>Send</b> tab.
	 * 
	 * @param modem
	 * @return
	 */
	private Object sendConsole_getRow(SmsDevice dev) {
		Object row = createTableRow(dev);

		if (isSmsModem(dev)) {
			SmsModem modem = (SmsModem) dev;
			add(row, createTableCell(modem.getPort()));
			add(row, createTableCell(Utils.getManufacturerAndModel(modem.getManufacturer(), modem.getModel())));
			add(row, createTableCell(modem.getMsisdn()));
		} else {
			SmsInternetService service = (SmsInternetService) dev;
			add(row, createTableCell("port?"));
			add(row, createTableCell(SmsInternetServiceSettingsHandler.getProviderName(service.getClass()) + " - " + service.getIdentifier()));
			add(row, createTableCell("from msisdn?"));
		}

		return row;
	}

	/**
	 * Set the current name component.
	 * 
	 * @param contactDetails
	 * @param name
	 */
	private void contactDetails_setName(Object contactDetails, String name) {
		setText(find(contactDetails, COMPONENT_CONTACT_NAME), name);
	}

	/**
	 * Set the current phone number component.
	 * 
	 * @param contactDetails
	 * @param msisdn
	 */
	private void contactDetails_setMobileMsisdn(Object contactDetails, String msisdn) {
		setText(find(contactDetails, COMPONENT_CONTACT_MOBILE_MSISDN), msisdn);
	}

	/**
	 * Set the current other phone number component.
	 * 
	 * @param contactDetails
	 * @param msisdn
	 */
	private void contactDetails_setOtherMsisdn(Object contactDetails, String msisdn) {
		setText(find(contactDetails, COMPONENT_CONTACT_OTHER_MSISDN), msisdn);
	}

	/**
	 * Set the current email address component.
	 * 
	 * @param contactDetails
	 * @param email
	 */
	private void contactDetails_setEmailAddress(Object contactDetails, String emailAddress) {
		setText(find(contactDetails, COMPONENT_CONTACT_EMAIL_ADDRESS), emailAddress);
	}

	/**
	 * Set the current notes component.
	 * 
	 * @param contactDetails
	 * @param notes
	 */
	private void contactDetails_setNotes(Object contactDetails, String notes) {
		setText(find(contactDetails, COMPONENT_CONTACT_NOTES), notes);
	}

	/**
	 * Gets the displayed name for a contact on the Contact Manager's Contact
	 * Details section.
	 * @param contactDetails
	 * @return
	 */
	private String contactDetails_getName(Object contactDetails) {
		return getText(find(contactDetails, COMPONENT_CONTACT_NAME));
	}

	/**
	 * Gets the displayed msisdn for a contact on the Contact Manager's Contact
	 * Details section.
	 * @param contactDetails
	 * @return
	 */
	private String contactDetails_getMobileMsisdn(Object contactDetails) {
		return getText(find(contactDetails, COMPONENT_CONTACT_MOBILE_MSISDN));
	}

	/**
	 * Gets the displayed alternate msisdn for a contact on the Contact Manager's 
	 * Contact Details section.
	 * @param contactDetails
	 * @return
	 */
	private String contactDetails_getOtherMsisdn(Object contactDetails) {
		return getText(find(contactDetails, COMPONENT_CONTACT_OTHER_MSISDN));
	}

	/**
	 * Gets the displayed email for a contact on the Contact Manager's Contact
	 * Details section.
	 * @param contactDetails
	 * @return
	 */
	private String contactDetails_getEmailAddress(Object contactDetails) {
		return getText(find(contactDetails, COMPONENT_CONTACT_EMAIL_ADDRESS));
	}

	/**
	 * Gets the displayed notes for a contact on the Contact Manager's Contact
	 * Details section.
	 * @param contactDetails
	 * @return
	 */
	private String contactDetails_getNotes(Object contactDetails) {
		return getText(find(contactDetails, COMPONENT_CONTACT_NOTES));
	}

	/**
	 * Handles expansion changes to a group list - a group's icon changes
	 * depending on whether it is expanded or collapsed and whether it
	 * has subgroups and members or not.
	 * @param groupList
	 */
	public void groupList_expansionChanged(Object groupList) {
		for (Object o : getItems(groupList)) {
			if (isAttachment(o, Group.class)) {
				if(getBoolean(o, EXPANDED) && getGroup(o).hasDescendants()) {
					// Set the icon to EXPANDED, and set children icons too!
					setIcon(o, Icon.FOLDER_OPEN);
					groupList_expansionChanged(o);
				} else {
					// Set the icon to CLOSED
					setIcon(o, Icon.FOLDER_CLOSED);
				}
			}
		}
	}

	/**
	 * Gets a short description of a keyword-action.
	 * @param action The keyword action to get the description.
	 * @return The description of the supplied keyword action.
	 */
	private String getActionDescription(KeywordAction action) {
		StringBuilder ret = new StringBuilder("");
		switch (action.getType()) {
			case KeywordAction.TYPE_FORWARD:
				ret.append(InternationalisationUtils.getI18NString(ACTION_FORWARD));
				ret.append(": \"");
				ret.append(KeywordAction.KeywordUtils.getForwardText(action, DEMO_SENDER, DEMO_SENDER.getMsisdn(), action.getKeyword().getKeywordString() +  DEMO_MESSAGE_TEXT_INCOMING));
				ret.append("\" ");
				ret.append(InternationalisationUtils.getI18NString(COMMON_TO_GROUP));
				ret.append(": \"");
				ret.append(action.getGroup().getName());
				ret.append("\"");
				break;
			case KeywordAction.TYPE_JOIN:
				ret.append(InternationalisationUtils.getI18NString(COMMON_JOIN));
				ret.append(": ");
				ret.append(action.getGroup().getName());
				break;
			case KeywordAction.TYPE_LEAVE:
				ret.append(InternationalisationUtils.getI18NString(COMMON_LEAVE));
				ret.append(": ");
				ret.append(action.getGroup().getName());
				break;
			case KeywordAction.TYPE_REPLY:
				ret.append(InternationalisationUtils.getI18NString(COMMON_REPLY));
				ret.append(": ");
				ret.append(KeywordAction.KeywordUtils.getReplyText(action, DEMO_SENDER, DEMO_SENDER.getMsisdn(), DEMO_MESSAGE_TEXT_INCOMING, DEMO_MESSAGE_KEYWORD));
				break;
			case KeywordAction.TYPE_SURVEY:
				ret.append(InternationalisationUtils.getI18NString(COMMON_SURVEY));
				break;
			case KeywordAction.TYPE_EXTERNAL_CMD:
				if (action.getExternalCommandType() == KeywordAction.EXTERNAL_HTTP_REQUEST) {
					ret.append(InternationalisationUtils.getI18NString(COMMON_HTTP_REQUEST));
				} else {
					ret.append(InternationalisationUtils.getI18NString(COMMON_EXTERNAL_COMMAND));
				}
				break;
			case KeywordAction.TYPE_EMAIL:
				ret.append(InternationalisationUtils.getI18NString(COMMON_E_MAIL));
				ret.append(": ");
				ret.append(KeywordAction.KeywordUtils.getReplyText(action, DEMO_SENDER, DEMO_SENDER.getMsisdn(), action.getKeyword().getKeywordString() + DEMO_MESSAGE_TEXT_INCOMING, DEMO_MESSAGE_KEYWORD));
				break;
		}
		return ret.toString();
	}

	public void table_addCells(Object tableRow, String[] cellContents) {
		for(String s : cellContents) add(tableRow, createTableCell(s));
	}

	/**
	 * Creates a list item Thinlet UI Component for the supplied keyword.  The keyword
	 * object is attached to the component, and the component's icon is set appropriately.
	 * @param keyword
	 * @return
	 */
	private Object createListItem(Keyword keyword) {
		String key = keyword.getKeywordString().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeywordString();
		Object listItem = createListItem(
				key,
				keyword);
		setIcon(listItem, Icon.KEYWORD);
		return listItem;
	}

	/**
	 * Creates a list item Thinlet UI Component for the supplied contact.  The contact
	 * object is attached to the component, and the component's icon is set appropriately.
	 * @param contact
	 * @return
	 */
	private Object createListItem(Contact contact) {
		Object listItem = createListItem(contact.getName() + " (" + contact.getMsisdn() + ")", contact);
		setIcon(listItem, Icon.CONTACT);
		return listItem;
	}

	/**
	 * Repopulates a UI list with details of a list of emails.
	 */
	private void updateEmailList(Collection<Email> emails, Object emailListComponent) {
		removeAll(emailListComponent);
		
		for (Email email : emails) {
			Object row = getRow(email);
			add(emailListComponent, row);
		}
				
		updatePageNumber(emailListComponent, find(TAB_EMAIL_LOG));
		enableOptions(emailListComponent, null, find(COMPONENT_EMAILS_TOOLBAR));
	}
	
	/**
	 * Creates a Thinlet UI table row containing details of a contact.
	 * @param contact
	 * @return
	 */
	private Object getRow(Contact contact) {
		Object row = createTableRow(contact);
		
		Object cell = createTableCell("");
		if (contact.isActive()) {
			setIcon(cell, Icon.TICK);
		} else {
			setIcon(cell, Icon.CANCEL);
		}
		setChoice(cell, ALIGNMENT, CENTER);
		add(row, cell);
		
		String name = contact.getName();
		add(row, createTableCell(name));

		add(row, createTableCell(contact.getMsisdn()));
		add(row, createTableCell(contact.getEmailAddress()));
		String groups = Utils.contactGroupsAsString(contact, DEFAULT_GROUPS_DELIMITER);
		add(row, createTableCell(groups));
		return row;
	}

	/**
	 * Creates a Thinlet UI table row containing details of a keyword action.
	 * @param contact
	 * @return
	 */
	private Object getRow(KeywordAction action) {
		Object row = createTableRow(action);
		String icon;
		switch(action.getType()) {
		case KeywordAction.TYPE_FORWARD:
			icon = Icon.ACTION_FORWARD;
			break;
		case KeywordAction.TYPE_JOIN:
			icon = Icon.ACTION_JOIN;
			break;
		case KeywordAction.TYPE_LEAVE:
			icon = Icon.ACTION_LEAVE;
			break;
		case KeywordAction.TYPE_REPLY:
			icon = Icon.ACTION_REPLY;
			break;
		case KeywordAction.TYPE_EXTERNAL_CMD:
			if (action.getExternalCommandType() == KeywordAction.EXTERNAL_COMMAND_LINE) 
				icon = Icon.ACTION_EXTERNAL_COMMAND;
			else 
				icon = Icon.ACTION_HTTP_REQUEST;
			break;
		case KeywordAction.TYPE_EMAIL:
			icon = Icon.ACTION_EMAIL;
			break;
		default:
			icon = Icon.SURVEY;
		break;
		}
		
		Object cell = createTableCell("");
		setIcon(cell, icon);
		add(row, cell);
		add(row, createTableCell(getActionDescription(action)));
		add(row, createTableCell(InternationalisationUtils.getDateFormat().format(action.getStartDate())));
		if (action.getEndDate() != DEFAULT_END_DATE) {
			add(row, createTableCell(InternationalisationUtils.getDateFormat().format(action.getEndDate())));
		} else {
			add(row, createTableCell(InternationalisationUtils.getI18NString(COMMON_UNDEFINED)));
		}
		cell = createTableCell("");
		setIcon(cell, action.isAlive() ? Icon.TICK : Icon.CANCEL);
		setChoice(cell, ALIGNMENT, CENTER);
		add(row, cell);
		add(row, createTableCell(action.getCounter()));
		return row;
	}
	
	/**
	 * Creates a Thinlet UI table row with details of a received SMS message
	 * in the appropriate format for the Survey Analyst.
	 * @param message
	 * @param sender
	 * @return
	 */
	private Object getAnalystRow(Message message, String sender) {
		Object row = createTableRow(message);
		add(row, createTableCell(InternationalisationUtils.getDatetimeFormat().format(message.getDate())));
		add(row, createTableCell(sender));
		Keyword key = getKeyword(getSelectedItem(find(COMPONENT_ANALYST_KEYWORD_LIST)));
		String content = message.getTextContent();
		content = content.substring(key.getKeywordString().length());
		if (content.equals("")) content = "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">";
		add(row, createTableCell(content));
		return row;
	}

	/**
	 * Creates a Thinlet UI table row containing details of an SMS message.
	 * @param message
	 * @return
	 */
	private Object getRow(Message message) {
		Object row = createTableRow(message);

		String icon;
		if (message.getType() == Message.TYPE_RECEIVED) {
			icon = Icon.SMS_RECEIVE;
		} else {
			icon = Icon.SMS_SEND;
		}

		Object iconCell = createTableCell("");
		setIcon(iconCell, icon);
		add(row, iconCell);
		add(row, createTableCell(getMessageStatusAsString(message)));
		add(row, createTableCell(InternationalisationUtils.getDatetimeFormat().format(message.getDate())));
		add(row, createTableCell(message.getSenderMsisdn()));
		add(row, createTableCell(message.getRecipientMsisdn()));
		add(row, createTableCell(message.getTextContent()));
		return row;
	}

	/**
	 * Creates a Thinlet UI table row containing details of an e-mail account.
	 * @param message
	 * @return
	 */
	private Object getRow(EmailAccount acc) {
		Object row = createTableRow(acc);

		Object iconCell = createTableCell("");
		setIcon(iconCell, acc.useSsl() ? Icon.SSL : Icon.NO_SSL);
		add(row, iconCell);
		add(row, createTableCell(acc.getAccountServer()));
		add(row, createTableCell(acc.getAccountName()));
		
		return row;
	}
	
	/**
	 * Creates a Thinlet UI table row containing details of an SMS message.
	 * @param message
	 * @return
	 */
	private Object getRowForPending(Message message) {
		Object row = createTableRow(message);

		String senderDisplayName = getSenderDisplayValue(message);
		String recipientDisplayName = getRecipientDisplayValue(message);
		
		add(row, createTableCell(senderDisplayName));
		add(row, createTableCell(recipientDisplayName));
		add(row, createTableCell(message.getTextContent()));

		return row;
	}
	
	/**
	 * Creates a Thinlet UI table row containing details of an Email.
	 * @param email
	 * @return
	 */
	private Object getRow(Email email) {
		Object row = createTableRow(email);

		add(row, createTableCell(getEmailStatusAsString(email)));
		if (email.getDate() == DEFAULT_END_DATE) {
			add(row, createTableCell(""));
		} else {
			add(row, createTableCell(InternationalisationUtils.getDatetimeFormat().format(email.getDate())));
		}
		add(row, createTableCell(email.getEmailFrom().getAccountName()));
		add(row, createTableCell(email.getEmailRecipients()));
		add(row, createTableCell(email.getEmailSubject()));
		add(row, createTableCell(email.getEmailContent()));

		return row;
	}
	
	/**
	 * Called when the current tab is changed; handles the tab-specific refreshments that need to be made.
	 * @param tabbedPane
	 */
	public void tabSelectionChanged(Object tabbedPane) {
		LOG.trace("ENTER");
		Object newTab = getSelectedItem(tabbedPane);
		currentTab = getString(newTab, NAME);
		LOG.debug("Current tab [" + currentTab + "]");
		if (currentTab == null) return;
		if (currentTab.equals(TAB_CONTACT_MANAGER)) {
			updateGroupList();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_CONTACT_MANAGER_LOADED));
		} else if (currentTab.equals(TAB_SURVEY_MANAGER)) {
			surveyManager_refresh();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_SURVEY_MANAGER_LOADED));
		} else if (currentTab.equals(TAB_SEND_CONSOLE)) {
			sendConsole_refresh();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_SEND_CONSOLE_LOADED));
		} else if (currentTab.equals(TAB_RECEIVE_CONSOLE)) {
			receiveConsole_refresh();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_MESSAGES_LOADED));
		} else if (currentTab.equals(TAB_ADVANCED_PHONE_MANAGER)) {
			this.phoneTabController.refreshPhonesViews();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_MODEM_LIST_UPDATED));
		} else if (currentTab.equals(TAB_MESSAGE_TRACKER)) {
			messageTracker_refresh();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_MESSAGES_LOADED));
		} else if (currentTab.equals(TAB_REPLY_MANAGER)) {
			replyManager_refreshKeywordList();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_REPLY_MANAGER_LOADED));
		} else if (currentTab.equals(TAB_SURVEY_ANALYST)) {
			surveyAnalyst_refresh();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_SURVEY_ANALYST_LOADED));
		} else if (currentTab.equals(TAB_GROUP_MANAGER)) {
			updateGroupList();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_GROUP_MANAGER_LOADED));
		} else if (currentTab.equals(TAB_MESSAGE_HISTORY)) {
			updateMessageHistoryFilter();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_MESSAGES_LOADED));
		} else if (currentTab.equals(TAB_KEYWORD_MANAGER)) {
			updateKeywordList();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_KEYWORDS_LOADED));
		} else if (currentTab.equals(TAB_EMAIL_LOG)) {
			updateEmailList();
			setStatus(InternationalisationUtils.getI18NString(MESSAGE_EMAILS_LOADED));
		}
		LOG.trace("EXIT");
	}

	public synchronized void outgoingEmailEvent(EmailSender sender, Email email) {
		LOG.trace("ENTER");
		LOG.debug("E-mail [" + email.getEmailContent() + "], Status [" + email.getStatus() + "]");
		if (currentTab.equals(TAB_EMAIL_LOG)) {
			LOG.debug("Refreshing e-mail list");
			int index = -1;
			for (int i = 0; i < getItems(emailListComponent).length; i++) {
				Email e = getEmail(getItem(emailListComponent, i));
				if (e.equals(email)) {
					index = i;
					break;
				}
			}
			if (index != -1) {
				//Updating
				remove(getItem(emailListComponent, index));
				add(emailListComponent, getRow(email), index);
			} else {
				int limit = getListLimit(emailListComponent);
				//Adding
				if (getItems(emailListComponent).length < limit && email.getStatus() == Email.STATUS_OUTBOX) {
					add(emailListComponent, getRow(email));
				}
				if (email.getStatus() == Email.STATUS_OUTBOX) {
					setListElementCount(getListElementCount(emailListComponent) + 1, emailListComponent);
				}
				updatePageNumber(emailListComponent, find(TAB_EMAIL_LOG));
			}
		}
		if (email.getStatus() == Email.STATUS_SENT) {
			newEvent(new Event(Event.TYPE_OUTGOING_EMAIL, InternationalisationUtils.getI18NString(COMMON_E_MAIL) + ": " + email.getEmailContent()));
		}
		LOG.trace("EXIT");
	}

	private void updateGroup(Group group, Object node) {
		if (getBoolean(node, EXPANDED) && group.hasDescendants())
			setIcon(node, Icon.FOLDER_OPEN);
		else 
			setIcon(node, Icon.FOLDER_CLOSED);
	}
	
	public synchronized void contactAddedToGroup(Contact contact, Group group) {
		if (currentTab.equals(TAB_CONTACT_MANAGER)) {
			addToContactList(contact, group);
			updateTree(group);
		}
	}

	private void addToContactList(Contact contact, Group group) {
		List<Group> selectedGroupsFromTree = new ArrayList<Group>();
		for (Object o : getSelectedItems(groupListComponent)) {
			Group g = getGroup(o);
			selectedGroupsFromTree.add(g);
		}
		
		if (selectedGroupsFromTree.contains(group)) {
			int limit = getListLimit(contactListComponent);
			//Adding
			if (getItems(contactListComponent).length < limit) {
				add(contactListComponent, getRow(contact));
			}
			setListElementCount(getListElementCount(contactListComponent) + 1, contactListComponent);
			updatePageNumber(contactListComponent, find(TAB_CONTACT_MANAGER));
		}
	}

	public void messageHistory_filterChanged() {
		setListPageNumber(1, filterListComponent);
		setListElementCount(1, filterListComponent);
		updateMessageHistoryFilter();
	}
	
	public void messageHistory_selectionChanged() {
		setListPageNumber(1, messageListComponent);
		updateMessageList();
	}
	
	private void updateTree(Group group) {
		Object node = getNodeForGroup(groupListComponent, group); //Only advanced mode
		updateGroup(group, node);
	}

	public synchronized void contactRemovedFromGroup(Contact contact, Group group) {
		if (currentTab.equals(TAB_CONTACT_MANAGER)) {
			removeFromContactList(contact, group);
			updateTree(group);
		}
	}

	private void removeFromContactList(Contact contact, Group group) {
		List<Group> selectedGroupsFromTree = new ArrayList<Group>();
		for (Object o : getSelectedItems(groupListComponent)) {
			Group g = getGroup(o);
			selectedGroupsFromTree.add(g);
		}
		
		if (selectedGroupsFromTree.contains(group)) {
			for (Object o : getItems(contactListComponent)) {
				Contact c = getContact(o);
				if (c.equals(contact)) {
					remove(o);
					break;
				}
			}
			int limit = getListLimit(contactListComponent);
			int count = getListElementCount(contactListComponent);
			if (getItems(contactListComponent).length == 1) {
				int page = getListCurrentPage(contactListComponent);
				int pages = count / limit;
				if ((count % limit) != 0) {
					pages++;
				}
				if (page == pages && page != 1) {
					//Last page
					page--;
					setListPageNumber(page, contactListComponent);
				} 
			}
			setListElementCount(getListElementCount(contactListComponent) - 1, contactListComponent);
			updateContactList();
		}
	}

	public synchronized void keywordActionExecuted(KeywordAction action) {
		if (currentTab.equals(TAB_KEYWORD_MANAGER)) {
			Object keyTab = find(currentTab);
			Object pnKeywordActionsAdvanced = find(keyTab, COMPONENT_PN_KEYWORD_ACTIONS_ADVANCED);
			if (pnKeywordActionsAdvanced != null) {
				Object actionsList = find(pnKeywordActionsAdvanced, COMPONENT_ACTION_LIST);
				int index = -1;
				for (Object act : getItems(actionsList)) {
					KeywordAction a = getKeywordAction(act);
					if (a.equals(action)) {
						index = getIndex(actionsList, act);
						remove(act);
						break;
					}
				}
				if (index != -1) {
					add(actionsList, getRow(action), index);
				}
			}
		}
	}
	
	public void changeLanguage(Object menuItem) {
		AppProperties appProperties = AppProperties.getInstance();
		appProperties.setLanguageFilename(getAttachedObject(menuItem).toString());
		appProperties.saveToDisk();
		reloadUI(false);
	}
	
	private void addLanguageMenu(Object menu) {
		for(LanguageBundle languageBundle : InternationalisationUtils.getLanguageBundles()) {
			Object menuitem = create(MENUITEM);
			setText(menuitem, languageBundle.getLanguage());
			setIcon(menuitem, getFlagIcon(languageBundle));
			setMethod(menuitem, ATTRIBUTE_ACTION, "changeLanguage(this)", menu, this);
			
			setAttachedObject(menuitem, languageBundle.getFilename());
			add(menu, menuitem);
		}
	}
	
	public void showAboutScreen() {
		Object about = loadComponentFromFile(UI_FILE_ABOUT_PANEL);
		String version = InternationalisationUtils.getI18NString(FrontlineSMSConstants.I18N_APP_VERSION, BuildProperties.getInstance().getVersion());
		setText(find(about, "version"), version);
		add(about);
	}

	public synchronized void incomingMessageEvent(Message message) {
		LOG.trace("ENTER");
		if (currentTab.equals(TAB_MESSAGE_HISTORY)) {
			addMessageToList(message);
		}
		newEvent(new Event(Event.TYPE_INCOMING_MESSAGE, InternationalisationUtils.getI18NString(COMMON_MESSAGE) + ": " + message.getTextContent()));
		setStatus(InternationalisationUtils.getI18NString(MESSAGE_MESSAGE_RECEIVED));
		LOG.trace("EXIT");
	}

	public synchronized void outgoingMessageEvent(Message message) {
		LOG.trace("ENTER");
		LOG.debug("Message [" + message.getTextContent() + "], Status [" + message.getStatus() + "]");
		if (currentTab.equals(TAB_MESSAGE_HISTORY)) {
			LOG.debug("Refreshing message list");
			int index = -1;
			for (int i = 0; i < getItems(messageListComponent).length; i++) {
				Message e = getMessage(getItem(messageListComponent, i));
				if (e.equals(message)) {
					index = i;
					remove(getItem(messageListComponent, i));
					break;
				}
			}
			if (index != -1) {
				//Updating
				add(messageListComponent, getRow(message), index);
			} else {
				addMessageToList(message);
			}
		} 
		if (message.getStatus() == Message.STATUS_SENT) {
			newEvent(new Event(Event.TYPE_OUTGOING_MESSAGE, InternationalisationUtils.getI18NString(COMMON_MESSAGE) + ": " + message.getTextContent()));
		} else if (message.getStatus() == Message.STATUS_FAILED) {
			newEvent(new Event(Event.TYPE_OUTGOING_MESSAGE_FAILED, InternationalisationUtils.getI18NString(COMMON_MESSAGE) + ": " + message.getTextContent()));
		}
		LOG.trace("ENTER");
	}
	
	public void switchToPhonesTab() {
		changeTab(TAB_ADVANCED_PHONE_MANAGER);
	}
	
	public void tabsChanged(Object menuItem) {
		Object tabbedPane = find(COMPONENT_TABBED_PANE);
		String name = getName(menuItem);
		boolean selected = isSelected(menuItem);
		String tabName = "";
		if (selected) {
			// Add a tab
			if (name.equals(COMPONENT_MI_HOME)) {
				add(new HomeTabController(this).getTab());
				tabName = "hometab";
			} else if (name.equals(COMPONENT_MI_KEYWORD)) {
				addKeywordsTab(tabbedPane);
				tabName = "keywordstab";
			} else if (name.equals(COMPONENT_MI_EMAIL)) {
				addEmailsTab(tabbedPane);
				tabName = "emailstab";
			}
		} else {
			Object tab = null;
			// Remove a tab
			if (name.equals(COMPONENT_MI_HOME)) {
				tab = find(tabbedPane, TAB_HOME);
				tabName = "hometab";
			} else if (name.equals(COMPONENT_MI_KEYWORD)) {
				tab = find(tabbedPane, TAB_KEYWORD_MANAGER);
				tabName = "keywordstab";
			} else if (name.equals(COMPONENT_MI_EMAIL)) {
				tab = find(tabbedPane, TAB_EMAIL_LOG);
				tabName = "emailstab";
			}
			if (tab != null) {
				remove(tab);
				setInteger(tabbedPane, SELECTED, 0);
			}
		}
		
		// Save tab visibility to disk
		UiProperties uiProperties = UiProperties.getInstance();
		uiProperties.setTabVisible(tabName, selected);
		uiProperties.saveToDisk();
	}
	
	private void changeTab(String tabName) {
		Object tabbedPane = find(COMPONENT_TABBED_PANE);
		Object tab = find(tabName);
		// The following method is taken from the inside of Thinlet.handleMouseEvent().
		// Had to extend the visibility of a number of methods to make this possible.
		int current = getIndex(tabbedPane, tab);
		setInteger(tabbedPane, SELECTED, current, 0);
		checkOffset(tabbedPane);
		
		tabSelectionChanged(tabbedPane);
		repaint(tabbedPane);
	}
	
	public void showUserDetailsDialog() {
		Object detailsDialog = loadComponentFromFile(UI_FILE_USER_DETAILS_DIALOG);
		add(detailsDialog);
	}
	
	public void reportError(Object dialog) {
		removeDialog(dialog);
		final String userName = getText(find(dialog, "tfName"));
		final String userEmail = getText(find(dialog, "tfEmail"));
		new Thread("ERROR_REPORT") {
			public void run() {
				try {
					ErrorUtils.sendLogs(userName, userEmail, true);
					String success = InternationalisationUtils.getI18NString(MESSAGE_LOG_FILES_SENT);
					LOG.debug(success);
					alert(success);
					setStatus(success);
				} catch (MessagingException e) {
					String msg = InternationalisationUtils.getI18NString(MESSAGE_FAILED_TO_SEND_REPORT);
					LOG.debug(msg, e);
					setStatus(msg);
					alert(msg);
					// Show user the option to save the logs.zip to a place they know!
					String dir = ErrorUtils.showFileChooserForSavingZippedLogs();
					if (dir != null) {
						try {
							ErrorUtils.copyLogsZippedToDir(dir);
						} catch (IOException e1) {
							LOG.debug("", e1);
							String first = InternationalisationUtils.getI18NString(MESSAGE_FAILED_TO_COPY_LOGS);
							String second = InternationalisationUtils.getI18NString(MESSAGE_LOGS_LOCATED_IN);
							setStatus(first.replace(ARG_VALUE, ZIPPED_LOGS_FILENAME) + "." + second.replace(ARG_VALUE, ResourceUtils.getConfigDirectoryPath() + "logs") + ".");
							alert(first.replace(ARG_VALUE, ZIPPED_LOGS_FILENAME) + "." + second.replace(ARG_VALUE, ResourceUtils.getConfigDirectoryPath() + "logs") + ".");
						}
						msg = InternationalisationUtils.getI18NString(MESSAGE_LOGS_SAVED_PLEASE_REPORT);
						setStatus(msg);
						alert(msg);
					}
				} catch (IOException e) {
					// Problem writing logs.zip
					LOG.debug("", e);
					try {
						ErrorUtils.sendToFrontlineSupport(userName, userEmail, null);
					} catch (MessagingException e1) {
						LOG.debug("", e1);
					}
				} finally {
					File input = new File(ResourceUtils.getConfigDirectoryPath() + ZIPPED_LOGS_FILENAME);
					if (input.exists()) {
						input.deleteOnExit();
					}
				}
			}
		}.start();
	}
	
	private void receiveConsole_updateConnectedPhones() {
		if(classicMode && currentTab.equals(TAB_RECEIVE_CONSOLE)) {
			Object numberLabel = find("lbReceiveConsoleConnectedPhones");
			int receivePhones = 0;
			for (SmsDevice dev : phoneManager.getAllPhones()) {
				if (isSmsModem(dev)) {
					SmsModem modem = (SmsModem) dev;
					if(modem.isUseForReceiving()) ++receivePhones;
				}
			}
			setText(numberLabel, Integer.toString(receivePhones));
		}
	}

	/**
	 * @param numberRemoved
	 */
	private void updatePageAfterDeletion(int numberRemoved, Object list) {
		int limit = getListLimit(list);
		int count = getListElementCount(list);
		if (numberRemoved == getItems(list).length) {
			int page = getListCurrentPage(list);
			int pages = count / limit;
			if ((count % limit) != 0) {
				pages++;
			}
			if (page == pages && page != 1) {
				//Last page
				page--;
				setListPageNumber(page, list);
			} 
		}
		setListElementCount(count - numberRemoved, list);
	}

	private void updateContactListAfterDeletion(final Object[] selected, int total) {
		int limit = getListLimit(contactListComponent);
		int count = getListElementCount(contactListComponent);
		if (selected.length == total) {
			int page = getListCurrentPage(contactListComponent);
			int pages = count / limit;
			if ((count % limit) != 0) {
				pages++;
			}
			if (page == pages && page != 1) {
				//Last page
				page--;
				setListPageNumber(page, contactListComponent);
			} 
		}
		setListElementCount(count - selected.length, contactListComponent);
		updateContactList();
	}
	
	/**
	 * Show dialog for editing Forms settings.
	 */
    public void showFormsSettings() {
        Object dialog = loadComponentFromFile("/ui/dialog/formsSettings.xml");
        add(dialog);
    }
    
    /**
     * Show the dialog for viewing and editing settings for {@link SmsInternetServiceSettingsHandler}.
     */
    public void showSmsInternetServiceSettings() {
    	new SmsInternetServiceSettingsHandler(this).showSettingsDialog();
    }

//    /**
//     * Show the dialog for viewing and editing settings for {@link SmsInternetServiceSettingsHandler}.
//     */
//    public void showDatabaseSettings() {
//    	new DatabaseSettingsHandler(this).showDatabaseConfigDialog();
//    }
	
	public void showConfigurationLocationDialog() {
		Object dialog = loadComponentFromFile("/ui/dialog/configurationLocation.xml");
		setText(find(dialog, "tfConfigurationLocation"), ResourceUtils.getConfigDirectoryPath());
		add(dialog);
	}

//> ACCESSORS
	/** @return {@link #phoneManager} */
	public SmsDeviceManager getPhoneManager() {
		return this.phoneManager;
	}
	/** @return {@link #phoneDetailsManager} */
	public SmsModemSettingsDao getPhoneDetailsManager() {
		return phoneDetailsManager;
	}
	
	/** return {@link #classicMode} */
	public boolean isClassicView() {
		return this.classicMode;
	}
	
	/** @return Cost set per SMS message */
	public double getCostPerSms() {
		return UiProperties.getInstance().getCostPerSms();
	}
	/** @param costPerSMS new value for {@link #costPerSMS} */
	private void setCostPerSms(double costPerSms) {
		UiProperties properties = UiProperties.getInstance();
		properties.setCostPerSms(costPerSms);
		properties.saveToDisk();
	}
	
	/** @return the current tab as an object component */
	public Object getCurrentTab() {
		return this.find(this.currentTab);
	}

	/** @return {@link #rootGroup} */
	public Group getRootGroup() {
		return this.rootGroup;
	}

	/** @return the {@link Frame} attached to this thinlet window */
	public Frame getFrameLauncher() {
		return this.frameLauncher;
	}
	
	/**
	 * Method used to get a list of all groups (created by users) from the system, starting from the root one.
	 * @return The list of all groups created by the user.
	 */
	private List<Group> getAllGroups() {
		return this.groupDao.getAllGroups();
	}
    
	public SmsInternetServiceSettingsDao getSmsInternetServiceSettingsDao() {
		return frontlineController.getSmsInternetServiceSettingsDao();
	}
	
	public Collection<SmsInternetService> getSmsInternetServices() {
		return this.frontlineController.getSmsInternetServices();
	}
	
	public void addSmsInternetService(SmsInternetService smsInternetService) {
		this.phoneManager.addSmsInternetService(smsInternetService);
	}

	/**
	 * 
	 * @param keywordHierarchy
	 * @param description
	 * @return
	 * @throws DuplicateKeyException If by some strange chance, the keyword we are trying to create already exists.
	 */
	@Transactional
	private Keyword createKeywordsHierarchically(String keywordHierarchy, String description) throws DuplicateKeyException {
		Keyword parent = this.keywordFactory.getFromMessageText(keywordHierarchy);
		if(parent != null) {
			keywordHierarchy = keywordHierarchy.substring(parent.getKeywordString().length()).trim();
		}
		
		String[] keywordTexts = keywordHierarchy.split(" ");
		for (int i = 0; i < keywordTexts.length; i++) {
			String keywordText = keywordTexts[i];
			Keyword keyword = new Keyword(parent, keywordText, "");
			
			// If this is the final keyword in the hierarchy, we need to add a description to it
			if(i == keywordTexts.length-1) {
				keyword.setDescription(description);
			}
			
			this.keywordFactory.saveKeyword(keyword);
			parent = keyword;
		}
		return parent;
	}
}
