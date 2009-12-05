/**
 * 
 */
package net.frontlinesms.ui;

// TODO remove static imports
import static net.frontlinesms.FrontlineSMSConstants.ACTION_ADD_TO_GROUP;
import static net.frontlinesms.FrontlineSMSConstants.COMMON_CONTACTS_IN_GROUP;
import static net.frontlinesms.FrontlineSMSConstants.COMMON_GROUP;
import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_CONTACTS_DELETED;
import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_EXISTENT_CONTACT;
import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_GROUPS_AND_CONTACTS_DELETED;
import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_GROUP_ALREADY_EXISTS;
import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_IMPOSSIBLE_TO_CREATE_A_GROUP_HERE;
import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_PHONE_BLANK;
import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_REMOVING_CONTACTS;
import static net.frontlinesms.FrontlineSMSConstants.UNKNOWN_NAME;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_BUTTON_YES;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_DORMANT;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_EMAIL_ADDRESS;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_MANAGER_CONTACT_FILTER;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_MANAGER_CONTACT_LIST;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_MANAGER_GROUP_TREE;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_MOBILE_MSISDN;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_NAME;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_NOTES;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_OTHER_MSISDN;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_DELETE_NEW_CONTACT;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_GROUPS_MENU;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_GROUP_MANAGER_CONTACT_LIST;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LABEL_STATUS;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MENU_ITEM_MSG_HISTORY;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MENU_ITEM_VIEW_CONTACT;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MI_DELETE;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MI_SEND_SMS;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_NEW_CONTACT_GROUP_LIST;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_NEW_GROUP;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_PN_CONTACTS;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_RADIO_BUTTON_ACTIVE;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_SEND_SMS_BUTTON;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_VIEW_CONTACT_BUTTON;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.TAB_CONTACT_MANAGER;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.UI_FILE_PAGE_PANEL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import net.frontlinesms.Utils;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.Group;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.data.repository.GroupDao;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.apache.log4j.Logger;

import thinlet.Thinlet;

/**
 * Event handler for the Contacts tab and associated dialogs.
 * @author Alex
 */
public class ContactsTabController implements ThinletUiEventHandler {
//> STATIC CONSTANTS
	/** UI XML File Path: the Home Tab itself */
	private static final String UI_FILE_CONTACTS_TAB = "/ui/core/contacts/contactsTab.xml";
	/** UI XML File Path: Edit and Create dialog for {@link Contact} objects */
	private static final String UI_FILE_CREATE_CONTACT_FORM = "/ui/core/contacts/dgEditContact.xml";
	private static final String UI_FILE_DELETE_OPTION_DIALOG_FORM = "/ui/dialog/deleteOptionDialogForm.xml"; // TODO move this to the correct path
	private static final String UI_FILE_NEW_GROUP_FORM = "/ui/dialog/newGroupForm.xml"; // TODO move this to the correct path
	
//> INSTANCE PROPERTIES
	/** Logging object */
	private final Logger LOG = Utils.getLogger(this.getClass()); // FIXME rename to log
	/** The {@link UiGeneratorController} that shows the tab. */
	private final UiGeneratorController uiController;
	/** The UI tab component */
	private final Object tabComponent;
	
//> DATA ACCESS OBJECTS
	/** Data access object for {@link Group}s */
	private final GroupDao groupDao;
	/** Data access object for {@link Contact}s */
	private final ContactDao contactDao;
	
//> CACHED THINLET UI COMPONENTS
	/** UI Component: group tree.  This is cached here to save searching for it later. */
	private Object groupListComponent;
	/** UI Component component: contact list.  This is cached here to save searching for it later. */
	private Object contactListComponent;

//> CONSTRUCTORS
	/**
	 * Create a new instance of this class.
	 * @param uiController value for {@link #uiController}
	 * @param contactDao {@link #contactDao}
	 * @param groupDao {@link #groupDao}
	 */
	public ContactsTabController(UiGeneratorController uiController, ContactDao contactDao, GroupDao groupDao) {
		this.uiController = uiController;
		this.contactDao = contactDao;
		this.groupDao = groupDao;
		
		this.tabComponent = uiController.loadComponentFromFile(UI_FILE_CONTACTS_TAB, this);
	}

//> ACCESSORS
	/**
	 * Load the home tab from the XML, and initialise relevant fields.
	 * @return a newly-initialised instance of the home tab
	 */
	public Object getTab() {
		initialiseTab();
		return this.tabComponent;
	}
	
	/** Refreshes the data displayed in the tab. */
	public void refresh() {
		updateGroupList();
	}

//> UI METHODS
	/**
	 * Shows the delete option dialog, which asks the user if he/she wants to remove
	 * the selected contacts from database.
	 * @param list
	 */
	public void showDeleteOptionDialog(Object list) {
		LOG.trace("ENTER");
		Object selected = this.uiController.getSelectedItem(list);
		if (selected != null) {
			Group g = this.uiController.getGroup(selected);
			if (!this.uiController.isDefaultGroup(g)) {
				Object deleteDialog = uiController.loadComponentFromFile(UI_FILE_DELETE_OPTION_DIALOG_FORM, this);
				uiController.add(deleteDialog);
			}
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * Method invoked when the group/contacts tree selection changes. 
	 * <br>This method updated the contact list according to the new selection.
	 * @param tree
	 * @param panel 
	 */
	public void selectionChanged(Object tree, Object panel) {
		LOG.trace("ENTER");
		this.uiController.setText(this.uiController.find(COMPONENT_CONTACT_MANAGER_CONTACT_FILTER), "");
		this.uiController.setListPageNumber(1, contactListComponent);
		//FIX Mantis entry 0000499
		Group g = this.uiController.getGroup(this.uiController.getSelectedItem(tree));
		String toSet = InternationalisationUtils.getI18NString(COMMON_CONTACTS_IN_GROUP, g.getName());
		this.uiController.setText(panel, toSet);
		
		Object deleteButton = this.uiController.find(this.uiController.getParent(tree), "deleteButton");
		this.uiController.setEnabled(deleteButton, !this.uiController.isDefaultGroup(g));
		
		Object sms = this.uiController.find(this.uiController.getParent(tree), "sendSMSButtonGroupSide");
		this.uiController.setEnabled(sms, g != null);
		
		updateContactList();
		LOG.trace("EXIT");
	}

	/**
	 * Shows contact dialog to allow edition of the selected contact.
	 * <br>This method affects the advanced mode.
	 * @param list
	 */
	public void showContactDetails(Object list) {
		Object selected = this.uiController.getSelectedItem(list);
		if (this.uiController.isAttachment(selected, Contact.class)) {
			showContactDetails(this.uiController.getContact(selected));
		}
	}
	
	/**
	 * Populates the pop up menu with all groups create by users.
	 * 
	 * @param popUp
	 * @param list
	 */
	public void populateGroups(Object popUp, Object list) {
		Object[] selectedItems = this.uiController.getSelectedItems(list);
		this.uiController.setVisible(popUp, this.uiController.getSelectedItems(list).length > 0);
		if (selectedItems.length == 0) {
			//Nothing selected
			boolean none = true;
			for (Object o : this.uiController.getItems(popUp)) {
				if (this.uiController.getName(o).equals(COMPONENT_NEW_GROUP)
						|| this.uiController.getName(o).equals("miNewContact")) {
					this.uiController.setVisible(o, true);
					none = false;
				} else {
					this.uiController.setVisible(o, false);
				}
			}
			this.uiController.setVisible(popUp, !none);
		} else if (this.uiController.getAttachedObject(selectedItems[0]) instanceof Contact) {
			for (Object o : this.uiController.getItems(popUp)) {
				String name = this.uiController.getName(o);
				if (name.equals(COMPONENT_MENU_ITEM_MSG_HISTORY) 
						|| name.equals(COMPONENT_MENU_ITEM_VIEW_CONTACT)) {
					this.uiController.setVisible(o, this.uiController.getSelectedItems(list).length == 1);
				} else if (!name.equals(COMPONENT_GROUPS_MENU)) {
					this.uiController.setVisible(o, true);
				}
			}
			Object menu = this.uiController.find(popUp, COMPONENT_GROUPS_MENU);
			this.uiController.removeAll(menu);
			List<Group> allGroups = this.groupDao.getAllGroups();
			for (Group g : allGroups) {
				Object menuItem = Thinlet.create(Thinlet.MENUITEM);
				this.uiController.setText(menuItem, InternationalisationUtils.getI18NString(COMMON_GROUP) + "'" + g.getName() + "'");
				this.uiController.setIcon(menuItem, Icon.GROUP);
				this.uiController.setAttachedObject(menuItem, g);
				if (list.equals(contactListComponent)) {
					this.uiController.setAction(menuItem, "addToGroup(this, 0)", menu, this);
				} else {
					this.uiController.setAction(menuItem, "addToGroup(this, 1)", menu, this);
				}
				this.uiController.add(menu, menuItem);
			}
			this.uiController.setVisible(menu, allGroups.size() != 0);
			String menuName = InternationalisationUtils.getI18NString(ACTION_ADD_TO_GROUP);
			this.uiController.setText(menu, menuName);
			
			Object menuRemove = this.uiController.find(popUp, "groupsMenuRemove");
			if (menuRemove != null) {
				Contact c = this.uiController.getContact(this.uiController.getSelectedItem(list));
				this.uiController.removeAll(menuRemove);
				Collection<Group> groups = c.getGroups();
				for (Group g : groups) {
					Object menuItem = Thinlet.create(Thinlet.MENUITEM);
					this.uiController.setText(menuItem, g.getName());
					this.uiController.setIcon(menuItem, Icon.GROUP);
					this.uiController.setAttachedObject(menuItem, g);
					this.uiController.setAction(menuItem, "removeFromGroup(this)", menuRemove, this);
					this.uiController.add(menuRemove, menuItem);
				}
				this.uiController.setEnabled(menuRemove, groups.size() != 0);
			}
		} else {
			Group g = this.uiController.getGroup(this.uiController.getSelectedItem(list));
			//GROUPS OR BOTH
			for (Object o : this.uiController.getItems(popUp)) {
				String name = this.uiController.getName(o);
				if (COMPONENT_NEW_GROUP.equals(name) 
						|| COMPONENT_MI_SEND_SMS.equals(name)
						|| COMPONENT_MI_DELETE.equals(name)
						|| COMPONENT_MENU_ITEM_MSG_HISTORY.equals(name)
						|| "miNewContact".equals(name)) {
					this.uiController.setVisible(o, true);
				} else {
					this.uiController.setVisible(o, false);
				}
				if (COMPONENT_MI_DELETE.equals(name)) {
					this.uiController.setVisible(o, !this.uiController.isDefaultGroup(g));
				}
				
				if (COMPONENT_NEW_GROUP.equals(name)) {
					this.uiController.setVisible(o, g!=this.uiController.unnamedContacts && g!=this.uiController.ungroupedContacts);
				}
			}
		}
	}
	
	/**
	 * Shows the new group dialog.
	 * 
	 * @param groupList
	 */
	public void showNewGroupDialog(Object groupList) {
		Object newGroupForm = this.uiController.loadComponentFromFile(UI_FILE_NEW_GROUP_FORM, this);
		this.uiController.setAttachedObject(newGroupForm, this.uiController.getGroupFromSelectedNode(this.uiController.getSelectedItem(groupList)));
		this.uiController.add(newGroupForm);
	}

	/**
	 * Shows the edit contact dialog. If the contact is null, then all fields are blank since
	 * it's a new contact. Otherwise we set the fields with the contact details, leaving it
	 * for editing.
	 * @param contact 
	 */
	private void showContactDetails(Contact contact) {
		Object createDialog = this.uiController.loadComponentFromFile(UI_FILE_CREATE_CONTACT_FORM, this);
		this.uiController.setAttachedObject(createDialog, contact);
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

			Object groupList = this.uiController.find(createDialog, COMPONENT_NEW_CONTACT_GROUP_LIST);
			for (Group g : contact.getGroups()) {
				Object item = this.uiController.createListItem(g.getName(), g);
				this.uiController.setIcon(item, Icon.GROUP);
				this.uiController.add(groupList, item);
			}
		}
		this.uiController.add(createDialog);
	}

	/**
	 * Shows the new contact dialog. This method affects the advanced mode.
	 * @param tree 
	 */
	public void showNewContactDialog() {
		Object createDialog = this.uiController.loadComponentFromFile(UI_FILE_CREATE_CONTACT_FORM, this);
		Object list = this.uiController.find(createDialog, COMPONENT_NEW_CONTACT_GROUP_LIST);
		Group sel = this.uiController.getGroup(this.uiController.getSelectedItem(this.groupListComponent));
		List<Group> allGroups = this.groupDao.getAllGroups();
		for (Group g : allGroups) {
			Object item = this.uiController.createListItem(g.getName(), g);
			this.uiController.setIcon(item, Icon.GROUP);
			this.uiController.setSelected(item, g.equals(sel));
			this.uiController.add(list, item);
		}
		this.uiController.add(createDialog);
	}

	/**
	 * Applies a text filter to the contact list and updates the list.
	 * 
	 * @param contactFilter The new filter.
	 */
	public void filterContacts(String contactFilter) {
		// We set the contactFilter variable.  When updateContactList is called, the contactFilter
		// variable will be used to select a subsection of the relevant contacts.
		this.uiController.setListPageNumber(1, contactListComponent);
		
		if (contactFilter.length() == 0) {
			updateContactList();
			return;
		}
		
		this.uiController.removeAll(contactListComponent);
		
		LinkedHashMap<String, Contact> contacts = getContactsFromSelectedGroups(groupListComponent);
		
		Pattern pattern = Pattern.compile("(" + Pattern.quote(contactFilter.toLowerCase()) + ").*");
		for (String key : contacts.keySet()) {
			Contact con = contacts.get(key);
			//FIX 0000501
			for (String names : con.getName().split("\\s")) {
				if (pattern.matcher(names.toLowerCase()).matches()) {
					this.uiController.add(contactListComponent, this.uiController.getRow(con));
					break;
				}
			}
		}
		this.uiController.setListElementCount(1, contactListComponent);
		this.uiController.updatePageNumber(contactListComponent, this.uiController.find(TAB_CONTACT_MANAGER));
	}

	/**
	 * Enables or disables the buttons on the Contacts tab (advanced mode).
	 * @param contactList
	 */
	public void enabledButtonsAfterSelection(Object contactList) {
		boolean enabled = this.uiController.getSelectedItems(contactList).length > 0;
		this.uiController.setEnabled(this.uiController.find(COMPONENT_DELETE_NEW_CONTACT), enabled);
		this.uiController.setEnabled(this.uiController.find(COMPONENT_VIEW_CONTACT_BUTTON), enabled);
		this.uiController.setEnabled(this.uiController.find(COMPONENT_SEND_SMS_BUTTON), enabled);
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
			selected = this.uiController.getSelectedItems(contactListComponent);
		} else {
			selected = this.uiController.getSelectedItems(this.uiController.find(COMPONENT_GROUP_MANAGER_CONTACT_LIST));
		}
		// Add to the selected groups...
		Group destination = this.uiController.getGroup(item);
		// Let's check all the selected items.  Any that are groups should be added to!
		for (Object component : selected) {
			if (this.uiController.isAttachment(component, Contact.class)) {
				Contact contact = this.uiController.getContact(component);
				LOG.debug("Adding Contact [" + contact.getName() + "] to [" + destination + "]");
				if(destination.addDirectMember(contact)) {
					groupDao.updateGroup(destination);
				}
			}
		}
		updateGroupList();
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
		final Object[] selected;
		selected = this.uiController.getSelectedItems(groupListComponent);
		if (dialog != null) {
			this.uiController.removeDialog(dialog);
		}
		for (Object o : selected) {
			Group group = uiController.getGroupFromSelectedNode(o);
			if(!uiController.isDefaultGroup(group)) {
				boolean removeContactsAlso = false;
				if (button != null) {
					removeContactsAlso = uiController.getName(button).equals(COMPONENT_BUTTON_YES);
				}
				LOG.debug("Selected Group [" + group.getName() + "]");
				LOG.debug("Remove Contacts from database [" + removeContactsAlso + "]");
				if (!uiController.isDefaultGroup(group)) {
					//Inside a default group
					LOG.debug("Removing group [" + group.getName() + "] from database");
					groupDao.deleteGroup(group, removeContactsAlso);
				} else {
					if (removeContactsAlso) {
						LOG.debug("Group not destroyable, removing contacts...");
						for (Contact c : group.getDirectMembers()) {
							LOG.debug("Removing contact [" + c.getName() + "] from database");
							contactDao.deleteContact(c);
						}
					}
				}
			}
		}
		Object sms = uiController.find(uiController.getParent(groupListComponent), "sendSMSButtonGroupSide");
		uiController.setEnabled(sms, uiController.getSelectedItems(groupListComponent).length > 0);
		uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_GROUPS_AND_CONTACTS_DELETED));
		refresh();
		LOG.trace("EXIT");
	}

	/**
	 * Updates or create a contact with the details added by the user. <br>
	 * This method is used by advanced mode, and also Contact Merge
	 * TODO this method should be transactional
	 * @param contactDetailsDialog
	 */
	public void saveContactDetailsAdvancedView(Object contactDetailsDialog) {
		LOG.trace("ENTER");
		Object attachment = this.uiController.getAttachedObject(contactDetailsDialog);
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
				this.contactDao.saveContact(contact);
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
				this.contactDao.updateContact(contact);
			}

			// Refresh the Contacts tab, and make sure that the group and contact who were previously selected are still selected
			updateGroupList();
		} catch(DuplicateKeyException ex) {
			LOG.debug("There is already a contact with this mobile number - cannot save!", ex);
			showMergeContactDialog(contact, contactDetailsDialog);
		} finally {
			this.uiController.removeDialog(contactDetailsDialog);
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * Saves the new or edited details found in the contact details pane.
	 * <br><b>This method only affects the classic mode.</b>
	 * 
	 * @param contactDetails
	 */
	public void saveContactDetails(Object contactDetails) {
		LOG.trace("ENTER");
		Object attachment = this.uiController.getAttachedObject(contactDetails);
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
			this.uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_PHONE_BLANK));
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
				contactDao.saveContact(contact);
				group.addDirectMember(contact);
				this.groupDao.updateGroup(group);
				updateGroup(group, getNodeForGroup(getGroupTreeComponent(), group));
				Object contactList = this.uiController.find(COMPONENT_GROUP_MANAGER_CONTACT_LIST);
				this.uiController.add(contactList, this.uiController.getRow(contact));
				
				this.uiController.activate(this.uiController.find("groupManager_contactListPanel"));
				this.uiController.activate(this.uiController.find("groupManager_groupList"));
				this.uiController.activate(this.uiController.find("groupManager_toolbar"));
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
				Object contactList = this.uiController.find(COMPONENT_GROUP_MANAGER_CONTACT_LIST);
				int index = -1;
				for (Object o : this.uiController.getItems(contactList)) {
					Contact c = this.uiController.getContact(o);
					if (c.equals(contact)) {
						index = this.uiController.getIndex(contactList, o);
						this.uiController.remove(o);
						break;
					}
				}
				this.uiController.add(contactList, this.uiController.getRow(contact), index);
			}
			updateGroup(this.uiController.rootGroup, getNodeForGroup(getGroupTreeComponent(), this.uiController.rootGroup));
			updateGroup(this.uiController.unnamedContacts, getNodeForGroup(getGroupTreeComponent(), this.uiController.unnamedContacts));
			updateGroup(this.uiController.ungroupedContacts, getNodeForGroup(getGroupTreeComponent(), this.uiController.ungroupedContacts));
		} catch(DuplicateKeyException ex) {
			LOG.debug("There is already a contact with this mobile number - cannot save!", ex);
			showMergeContactDialog(contact, contactDetails);
		}
		LOG.trace("EXIT");
	}

	public synchronized void contactRemovedFromGroup(Contact contact, Group group) {
		if (this.uiController.getCurrentTab().equals(TAB_CONTACT_MANAGER)) {
			removeFromContactList(contact, group);
			updateTree(group);
		}
	}

	/**
	 * Removes the contacts selected in the contacts list from the group which is selected in the groups tree.
	 * @param selectedGroup A set of thinlet components with group members attached to them.
	 */
	public void removeFromGroup(Object selectedGroup) {
		Group g = this.uiController.getGroup(selectedGroup);
		Contact c = this.uiController.getContact(this.uiController.getSelectedItem(contactListComponent));
		if(g.removeContact(c)) {
			this.groupDao.updateGroup(g);
		}
		this.refresh();
	}

	/** Removes the selected contacts of the supplied contact list component. */
	public void deleteSelectedContacts() {
		LOG.trace("ENTER");
		this.uiController.removeConfirmationDialog();
		this.uiController.setStatus(InternationalisationUtils.getI18NString(MESSAGE_REMOVING_CONTACTS));
		final Object[] selected = this.uiController.getSelectedItems(contactListComponent);
		for (Object o : selected) {
			Contact contact = uiController.getContact(o);
			LOG.debug("Deleting contact [" + contact.getName() + "]");
			for (Group g : contact.getGroups()) {
			}
			contactDao.deleteContact(contact);
		}
		uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_CONTACTS_DELETED));
		refresh();
		LOG.trace("EXIT");
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
		Group selectedParentGroup = this.uiController.getGroup(dialog);
		doGroupCreation(newGroupName, dialog, selectedParentGroup);		
	}

	/**
	 * Update the icon for active/dormant.
	 * @param radioButton
	 * @param label
	 */
	public void updateIconActive(Object radioButton, Object label) {
		String icon;
		if (this.uiController.getName(radioButton).equals(COMPONENT_RADIO_BUTTON_ACTIVE)) {
			icon = Icon.ACTIVE;
		} else {
			icon = Icon.DORMANT;
		}
		this.uiController.setIcon(label, icon);
	}
	
//> PRIVATE UI HELPER METHODS
	/**
	 * @param name The name of the component to find
	 * @return a thinlet compoenent within {@link #tabComponent}.
	 */
	private Object find(String name) {
		return this.uiController.find(this.tabComponent, name);
	}
	/**
	 * Gets the node we are currently displaying for a group.
	 * @param component
	 * @param group
	 * @return
	 */
	private Object getNodeForGroup(Object component, Group group) {
		if(group == null) {
			group = this.uiController.rootGroup;
		}
		Object ret = null;
		for (Object o : this.uiController.getItems(component)) {
			Group g = this.uiController.getGroup(o);
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
	/**
	 * @param tree
	 * @return all the selected contacts to show in the contact list
	 */
	private LinkedHashMap<String, Contact> getContactsFromSelectedGroups(Object tree) {
		LinkedHashMap<String, Contact> toBeShown = new LinkedHashMap<String, Contact>();
		if (this.uiController.isSelected(this.uiController.getItems(tree)[0])) {
			//Root group selected
			//Show everyone
			for (Contact c : contactDao.getAllContacts()) {
				toBeShown.put(c.getMsisdn(), c);
			}
		} else {
			for (Object o : this.uiController.getSelectedItems(tree)) {
				for(Contact c : this.uiController.getGroup(o).getAllMembers()) {
					toBeShown.put(c.getMsisdn(), c);
				}
			}
		}
		
		return toBeShown;
	}
	/** @return {@link #groupListComponent} - the Thinlet TREE component displaying the tree of {@link Group}s. */
	private Object getGroupTreeComponent() {
		return this.groupListComponent;
	}
	/**
	 * Show the form to allow merging between a previously-created contact, and an attempted-newly-created contact.
	 * TODO if we work out a good-looking way of doing this, we should implement it.  Currently this just warns the user that a contact with this number already exists.
	 */
	private void showMergeContactDialog(Contact oldContact, Object createContactForm) { // FIXME remove arguments from this method
		this.uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_EXISTENT_CONTACT));
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
		if(selectedParentGroup == this.uiController.rootGroup) {
			selectedParentGroup = null;
		}
		if (selectedParentGroup == this.uiController.unnamedContacts || selectedParentGroup == this.uiController.ungroupedContacts) {
			this.uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_IMPOSSIBLE_TO_CREATE_A_GROUP_HERE));
			if (dialog != null) this.uiController.remove(dialog);
			return;
		}
		LOG.debug("Group Name [" + newGroupName + "]");
		try {
			if(LOG.isDebugEnabled()) LOG.debug("Creating group with name: " + newGroupName + " and parent: " + selectedParentGroup);
			
			Group g = new Group(selectedParentGroup, newGroupName);
			this.groupDao.saveGroup(g);
			
			// Now we've saved the group, add it to the groups tree displayed in the contacts manager
			Object groupListComponent = getGroupTreeComponent();
			Object parentNode = getNodeForGroup(groupListComponent, selectedParentGroup);
			this.uiController.add(parentNode, this.uiController.getNode(g, true));
			
			if (dialog != null) this.uiController.remove(dialog);
			LOG.debug("Group created successfully!");
		} catch (DuplicateKeyException e) {
			LOG.debug("A group with this name already exists.", e);
			this.uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_GROUP_ALREADY_EXISTS));
		}
		LOG.trace("EXIT");
	}
	private void updateTree(Group group) {
		Object node = getNodeForGroup(groupListComponent, group); //Only advanced mode
		updateGroup(group, node);
	}
	/**
	 * Set the current name component.
	 * @param contactDetails
	 * @param name
	 */
	private void contactDetails_setName(Object contactDetails, String name) {
		this.uiController.setText(this.uiController.find(contactDetails, COMPONENT_CONTACT_NAME), name);
	}

	/**
	 * Set the current phone number component.
	 * @param contactDetails
	 * @param msisdn
	 */
	private void contactDetails_setMobileMsisdn(Object contactDetails, String msisdn) {
		this.uiController.setText(this.uiController.find(contactDetails, COMPONENT_CONTACT_MOBILE_MSISDN), msisdn);
	}

	/**
	 * Set the current other phone number component.
	 * @param contactDetails
	 * @param msisdn
	 */
	private void contactDetails_setOtherMsisdn(Object contactDetails, String msisdn) {
		this.uiController.setText(this.uiController.find(contactDetails, COMPONENT_CONTACT_OTHER_MSISDN), msisdn);
	}

	/**
	 * Set the current email address component.
	 * @param contactDetails
	 * @param emailAddress
	 */
	private void contactDetails_setEmailAddress(Object contactDetails, String emailAddress) {
		this.uiController.setText(this.uiController.find(contactDetails, COMPONENT_CONTACT_EMAIL_ADDRESS), emailAddress);
	}
	/**
	 * Set the current notes component.
	 * @param contactDetails
	 * @param notes
	 */
	private void contactDetails_setNotes(Object contactDetails, String notes) {
		this.uiController.setText(this.uiController.find(contactDetails, COMPONENT_CONTACT_NOTES), notes);
	}
	/**
	 * Set the current state of the active/dormant component.
	 * @param contactDetails
	 * @param active
	 */
	private void contactDetails_setActive(Object contactDetails, boolean active) {
		this.uiController.setSelected(this.uiController.find(contactDetails, COMPONENT_RADIO_BUTTON_ACTIVE), active);
		this.uiController.setSelected(this.uiController.find(contactDetails, COMPONENT_CONTACT_DORMANT), !active);
		if (active) {
			this.uiController.setIcon(this.uiController.find(contactDetails, COMPONENT_LABEL_STATUS), Icon.ACTIVE);
		} else {
			this.uiController.setIcon(this.uiController.find(contactDetails, COMPONENT_LABEL_STATUS), Icon.DORMANT);
		}
	}
	/**
	 * @param contactDetails
	 * @return the current state of the active component 
	 */
	private boolean contactDetails_getActive(Object contactDetails) {
		return this.uiController.isSelected(this.uiController.find(contactDetails, COMPONENT_RADIO_BUTTON_ACTIVE));
	}
	/**
	 * @param contactDetails
	 * @return the displayed name for a contact on the Contact Manager's Contact Details section
	 */
	private String contactDetails_getName(Object contactDetails) {
		return this.uiController.getText(this.uiController.find(contactDetails, COMPONENT_CONTACT_NAME));
	}
	/**
	 * @param contactDetails
	 * @return the displayed msisdn for a contact on the Contact Manager's Contact Details section
	 */
	private String contactDetails_getMobileMsisdn(Object contactDetails) {
		return this.uiController.getText(this.uiController.find(contactDetails, COMPONENT_CONTACT_MOBILE_MSISDN));
	}
	/**
	 * @param contactDetails
	 * @return the displayed alternate msisdn for a contact on the Contact Manager's Contact Details section
	 */
	private String contactDetails_getOtherMsisdn(Object contactDetails) {
		return this.uiController.getText(this.uiController.find(contactDetails, COMPONENT_CONTACT_OTHER_MSISDN));
	}
	/**
	 * @param contactDetails
	 * @return the displayed email for a contact on the Contact Manager's Contact Details section
	 */
	private String contactDetails_getEmailAddress(Object contactDetails) {
		return this.uiController.getText(this.uiController.find(contactDetails, COMPONENT_CONTACT_EMAIL_ADDRESS));
	}
	/**
	 * @param contactDetails
	 * @return the displayed notes for a contact on the Contact Manager's Contact Details section
	 */
	private String contactDetails_getNotes(Object contactDetails) {
		return this.uiController.getText(this.uiController.find(contactDetails, COMPONENT_CONTACT_NOTES));
	}
	private void removeFromContactList(Contact contact, Group group) {
		List<Group> selectedGroupsFromTree = new ArrayList<Group>();
		for (Object o : this.uiController.getSelectedItems(groupListComponent)) {
			Group g = this.uiController.getGroup(o);
			selectedGroupsFromTree.add(g);
		}
		
		if (selectedGroupsFromTree.contains(group)) {
			for (Object o : this.uiController.getItems(contactListComponent)) {
				Contact c = this.uiController.getContact(o);
				if (c.equals(contact)) {
					this.uiController.remove(o);
					break;
				}
			}
			int limit = this.uiController.getListLimit(contactListComponent);
			int count = this.uiController.getListElementCount(contactListComponent);
			if (this.uiController.getItems(contactListComponent).length == 1) {
				int page = this.uiController.getListCurrentPage(contactListComponent);
				int pages = count / limit;
				if ((count % limit) != 0) {
					pages++;
				}
				if (page == pages && page != 1) {
					//Last page
					page--;
					this.uiController.setListPageNumber(page, contactListComponent);
				} 
			}
			this.uiController.setListElementCount(this.uiController.getListElementCount(contactListComponent) - 1, contactListComponent);
			updateContactList();
		}
	}
	/** Repopulates the contact list according to the current filter. */
	private void updateContactList() {
		// To repopulate the contact list, we must first locate it and remove the current
		// contents.  Once we've done that, work out what should now be displayed in it,
		// and add them all.
		this.uiController.removeAll(contactListComponent);
		// If we have only selected one of the 'system' groups, we need to disable the
		// delete button - it's not possible to delete the root group, and the other 2
		// special groups.
		Group group = this.uiController.getGroup(this.uiController.getSelectedItem(groupListComponent));
		
		if (group != null) {
			int limit = this.uiController.getListLimit(contactListComponent);
			int pageNumber = this.uiController.getListCurrentPage(contactListComponent);
			List<? extends Contact> contacts = group.getAllMembers((pageNumber - 1) * limit, limit);

			int count = group.getAllMembersCount();
			this.uiController.setListElementCount(count, contactListComponent);

			for (Contact con : contacts) {
				this.uiController.add(contactListComponent, this.uiController.getRow(con));
			}
			this.uiController.updatePageNumber(contactListComponent, this.uiController.find(TAB_CONTACT_MANAGER));
			enabledButtonsAfterSelection(contactListComponent);
		}
	}

	/** Updates the group tree. */
	private void updateGroupList() {
		Object groupListComponent = getGroupTreeComponent();
		
		Object selected = this.uiController.getSelectedItem(groupListComponent);
		
		this.uiController.removeAll(groupListComponent);
		this.uiController.add(groupListComponent, this.uiController.getNode(this.uiController.rootGroup, true));

		this.uiController.setSelected(selected, groupListComponent);
		
		updateContactList();
	}

	private void updateGroup(Group group, Object node) {
		if (this.uiController.getBoolean(node, Thinlet.EXPANDED) && group.hasDescendants())
			this.uiController.setIcon(node, Icon.FOLDER_OPEN);
		else 
			this.uiController.setIcon(node, Icon.FOLDER_CLOSED);
	}
	
//> EVENT HANDLER METHODS
	void addToContactList(Contact contact, Group group) {
		List<Group> selectedGroupsFromTree = new ArrayList<Group>();
		for (Object o : this.uiController.getSelectedItems(groupListComponent)) {
			Group g = this.uiController.getGroup(o);
			selectedGroupsFromTree.add(g);
		}
		
		if (selectedGroupsFromTree.contains(group)) {
			int limit = this.uiController.getListLimit(contactListComponent);
			//Adding
			if (this.uiController.getItems(contactListComponent).length < limit) {
				this.uiController.add(contactListComponent, this.uiController.getRow(contact));
			}
			this.uiController.setListElementCount(this.uiController.getListElementCount(contactListComponent) + 1, contactListComponent);
			this.uiController.updatePageNumber(contactListComponent, this.uiController.find(TAB_CONTACT_MANAGER));
		}
		
		updateTree(group);
	}
	
//> UI PASS-THROUGH METHODS TO UiGC
	/**
	 * Remove the supplied dialog from view.
	 * @param dialog the dialog to remove
	 * @see UiGeneratorController#removeDialog(Object)
	 */
	public void removeDialog(Object dialog) {
		this.uiController.removeDialog(dialog);
	}
	/** @see UiGeneratorController#groupList_expansionChanged(Object) */
	public void groupList_expansionChanged(Object groupList) {
		this.uiController.groupList_expansionChanged(groupList);
	}
	/**
	 * Shows the compose message dialog, populating the list with the selection of the 
	 * supplied list.
	 * @param list
	 */
	public void show_composeMessageForm(Object list) {
		this.uiController.show_composeMessageForm(list);
	}
	/**
	 * Shows the message history for the selected contact or group.
	 * @param component group list or contact list
	 */
	public void showMessageHistory(Object component) {
		this.uiController.showMessageHistory(component);
	}
	/**
	 * @param page The file name of the help page
	 * @see UiGeneratorController#showHelpPage(String)
	 */
	public void showHelpPage(String page) {
		this.uiController.showHelpPage(page);
	}
	/** Shows a general dialog asking the user to confirm his action. 
	 * @param methodToBeCalled The name and optionally the signature of the method to be called 
	 * @see UiGeneratorController#showConfirmationDialog(String) */
	public void showConfirmationDialog(String methodToBeCalled){
		this.uiController.showConfirmationDialog(methodToBeCalled, this);
	}
	/**
	 * Shows the export wizard dialog, according to the supplied type.
	 * @param list The list to get selected items from.
	 * @param type the name of the type to export
	 */
	public void showExportWizard(Object list, String type) {
		this.uiController.showExportWizard(list, type); // TODO We could hard-code this type here - we'll always be exporting contacts from here.
	}

//> INSTANCE HELPER METHODS
	/** Initialise dynamic contents of the tab component. */
	private void initialiseTab() {
		Object pnContacts = this.uiController.find(this.tabComponent, COMPONENT_PN_CONTACTS);
		String listName = COMPONENT_CONTACT_MANAGER_CONTACT_LIST;
		Object pagePanel = uiController.loadComponentFromFile(UI_FILE_PAGE_PANEL, this);
		uiController.add(pnContacts, pagePanel, 0);
		uiController.setPageMethods(pnContacts, listName, pagePanel);
		
		// Cache Thinlet UI components
		groupListComponent = this.find(COMPONENT_CONTACT_MANAGER_GROUP_TREE);
		contactListComponent = this.find(COMPONENT_CONTACT_MANAGER_CONTACT_LIST);

		//Entries per page
		this.uiController.setListLimit(contactListComponent);
		//Current page
		this.uiController.setListPageNumber(1, contactListComponent);
		//Actions
		this.uiController.setMethod(contactListComponent, "updateContactList");
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
