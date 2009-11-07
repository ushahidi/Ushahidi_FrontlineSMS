/**
 * 
 */
package net.frontlinesms.ui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;

import net.frontlinesms.CommUtils;
import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.SmsModemSettings;
import net.frontlinesms.data.repository.SmsModemSettingsDao;
import net.frontlinesms.smsdevice.SmsDevice;
import net.frontlinesms.smsdevice.SmsDeviceEventListener;
import net.frontlinesms.smsdevice.SmsDeviceManager;
import net.frontlinesms.smsdevice.SmsDeviceStatus;
import net.frontlinesms.smsdevice.SmsModem;
import net.frontlinesms.smsdevice.SmsModemStatus;
import net.frontlinesms.smsdevice.internet.SmsInternetService;
import net.frontlinesms.smsdevice.internet.SmsInternetServiceStatus;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.apache.log4j.Logger;
import org.smslib.AbstractATHandler;
import org.smslib.handler.CATHandler;

import serial.CommPortIdentifier;
import serial.NoSuchPortException;

/**
 * Event handler for the Phones tab and associated dialogs
 * @author Alex
 */
public class PhoneTabController implements SmsDeviceEventListener, ThinletUiEventHandler {
//> STATIC CONSTANTS
	/** The fully-qualified name of the default {@link CATHandler} class. */
	private static final String DEFAULT_CAT_HANDLER_CLASS_NAME = CATHandler.class.getName();
	/** {@link Comparator} used for sorting {@link SmsDevice}s into a friendly order. */
	private static final Comparator<? super SmsDevice> SMS_DEVICE_COMPARATOR = new Comparator<SmsDevice>() {
			public int compare(SmsDevice one, SmsDevice tother) {
				boolean oneIsModem = one instanceof SmsModem;
				boolean totherIsModem = tother instanceof SmsModem;
				if(!oneIsModem && !totherIsModem) return 0;
				if(!totherIsModem) return 1;
				if(!oneIsModem) return -1;
				int comparison = ((SmsModem)one).getPort().compareTo(((SmsModem)tother).getPort());
				return comparison;
			}};
	
//> THINLET UI LAYOUT FILES
	/** UI XML File Path: the Phones Tab itself */
	private static final String UI_FILE_PHONES_TAB = "/ui/core/phones/phonesTab.xml";
	/** UI XML File Path: phone settings dialog TODO what is this dialog for? */
	private static final String UI_FILE_MODEM_SETTINGS_DIALOG = "/ui/core/phones/dgModemSettings.xml";
	/** UI XML File Path: phone config dialog TODO what is this dialog for? */
	private static final String UI_FILE_MODEM_MANUAL_CONFIG_DIALOG = "/ui/core/phones/dgModemManualConfig.xml";
	
//> I18n TEXT KEYS
	/** I18n Text Key: TODO */
	private static final String COMMON_PHONE_CONNECTED = "common.phone.connected";
	/** I18n Text Key: TODO */
	private static final String COMMON_SETTINGS_FOR_PHONE = "common.settings.for.phone";
	/** I18n Text Key: TODO */
	private static final String COMMON_SMS_INTERNET_SERVICE_CONNECTED = "common.sms.internet.service.connected";
	/** I18n Text Key: TODO */
	private static final String COMMON_SMS_INTERNET_SERVICE_RECEIVING_FAILED = "common.sms.internet.service.receiving.failed";
	/** I18n Text Key: TODO */
	private static final String MESSAGE_INVALID_BAUD_RATE = "message.invalid.baud.rate";
	/** I18n Text Key: TODO */
	private static final String MESSAGE_PORT_NOT_FOUND = "message.port.not.found";
	/** I18n Text Key: TODO */
	private static final String MESSAGE_PORT_ALREADY_CONNECTED = "message.port.already.connected";
	/** I18n Text Key: TODO */
	private static final String MESSAGE_PHONE = "common.phone";
	/** I18n Text Key: The requested port is already in use. */
	private static final String I18N_PORT_IN_USE = "com.port.inuse";
	
//> THINLET UI COMPONENT NAMES
	/** UI Compoenent name: TODO */
	private static final String COMPONENT_PHONE_SENDING = "cbSending";
	/** UI Compoenent name: TODO */
	private static final String COMPONENT_PHONE_RECEIVING = "cbReceiving";
	/** UI Compoenent name: TODO */
	private static final String COMPONENT_PHONE_DELETE = "cbDeleteMsgs";
	/** UI Compoenent name: TODO */
	private static final String COMPONENT_PHONE_DELIVERY_REPORTS = "cbUseDeliveryReports";
	/** UI Compoenent name: TODO */
	private static final String COMPONENT_RB_PHONE_DETAILS_ENABLE = "rbPhoneDetailsEnable";
	/** UI Compoenent name: TODO */
	private static final String COMPONENT_PN_PHONE_SETTINGS = "pnPhoneSettings";
	/** UI Compoenent name: TODO */
	private static final String COMPONENT_PHONE_MANAGER_MODEM_LIST = "phoneManager_modemList";
	/** UI Compoenent name: TODO */
	private static final String COMPONENT_PHONE_MANAGER_MODEM_LIST_ERROR = "phoneManager_modemListError";

//> INSTANCE PROPERTIES
	/** Logging object */
	private final Logger log = Utils.getLogger(this.getClass());
	/** Object to synchronize on before updating the phones list.  This is to prevent the list being rewritten by two different sources at the same time. */
	private final Object PHONES_LIST_SYNCH_OBJECT = new Object();
	/** The {@link UiGeneratorController} that shows the tab. */
	private final UiGeneratorController uiController;
	/** The manager of {@link SmsDevice}s */
	private final SmsDeviceManager phoneManager;
	/** Data Access Object for {@link SmsModemSettings}s */
	private final SmsModemSettingsDao phoneDetailsManager;
	/** The UI tab component */
	private final Object tabComponent;

//> CONSTRUCTORS
	/**
	 * Create a new instance of this class.
	 * @param uiController value for {@link #uiController}
	 */
	public PhoneTabController(UiGeneratorController uiController) {
		this.uiController = uiController;
		this.phoneManager = uiController.getPhoneManager();
		this.phoneDetailsManager = uiController.getPhoneDetailsManager();
		this.tabComponent = uiController.loadComponentFromFile(UI_FILE_PHONES_TAB, this);
	}

//> ACCESSORS
	/**
	 * Load the home tab from the XML, and initialise relevant fields.
	 * @return a newly-initialised instance of the home tab
	 */
	public Object getTab() {
		return this.tabComponent;
	}
	
	/** @return the compoenent containing the list of connected devices */
	private Object getModemListComponent() {
		return uiController.find(this.tabComponent, COMPONENT_PHONE_MANAGER_MODEM_LIST);
	}
	
//> THINLET UI METHODS
	
	/**
	 * Event fired when the view phone details action is fired. 
	 * @param list the thinlet UI list containing details of the devices
	 */
	public void showPhoneSettingsDialog(Object list) {
		Object selected = uiController.getSelectedItem(list);
		if (selected != null) {
			SmsDevice selectedPhone = uiController.getAttachedObject(selected, SmsDevice.class);
			if (selectedPhone instanceof SmsModem) {
				if (!((SmsModem) selectedPhone).isDisconnecting())
					showPhoneSettingsDialog((SmsModem) selectedPhone, false);
			} else {
				// Http service.
				SmsInternetServiceSettingsHandler serviceHandler = new SmsInternetServiceSettingsHandler(this.uiController);
				serviceHandler.showConfigureService((SmsInternetService) selectedPhone, null);
			}
		}
	}
	
	/**
	 * Event fired when the view phone details action is chosen.
	 * @param phone The phone we are showing settings for
	 * @param isNewPhone <code>true</code> TODO <if this phone has previously connected (i.e. not first time it has connected)> OR <if this phone has just connected (e.g. may have connected before, but not today)> 
	 */
	public void showPhoneSettingsDialog(SmsModem phone, boolean isNewPhone) {
		Object phoneSettingsDialog = this.uiController.loadComponentFromFile(UI_FILE_MODEM_SETTINGS_DIALOG, this);
		this.uiController.setText(phoneSettingsDialog, InternationalisationUtils.getI18NString(COMMON_SETTINGS_FOR_PHONE) + " '" + phone.getModel() + "'");
		
		if(!isNewPhone) {
			boolean useForSending = phone.isUseForSending();
			boolean useForReceiving = phone.isUseForReceiving();
			
			if(useForSending || useForReceiving) {
				this.uiController.setSelected(this.uiController.find(phoneSettingsDialog, COMPONENT_PHONE_SENDING), useForSending);
				Object cbDeliveryReports = this.uiController.find(phoneSettingsDialog, COMPONENT_PHONE_DELIVERY_REPORTS);
				uiController.setEnabled(cbDeliveryReports, useForSending);
				uiController.setSelected(cbDeliveryReports, phone.isUseDeliveryReports());
				uiController.setSelected(this.uiController.find(phoneSettingsDialog, COMPONENT_PHONE_RECEIVING), useForReceiving);
				Object cbDeleteMessages = this.uiController.find(phoneSettingsDialog, COMPONENT_PHONE_DELETE);
				this.uiController.setEnabled(cbDeleteMessages, useForReceiving);
				this.uiController.setSelected(cbDeleteMessages, phone.isDeleteMessagesAfterReceiving());
			} else {
				uiController.setSelected(uiController.find(phoneSettingsDialog, "rbPhoneDetailsDisable"), true);
				uiController.setSelected(uiController.find(phoneSettingsDialog, COMPONENT_RB_PHONE_DETAILS_ENABLE), false);
				uiController.deactivate(uiController.find(phoneSettingsDialog, COMPONENT_PN_PHONE_SETTINGS));
			}
		}
		
		if(!phone.supportsReceive()) {
			// If this phone does not support SMS receiving, we need to pass this info onto
			// the user.  We also want to gray out the options for receiving.
			uiController.setEnabled(uiController.find(phoneSettingsDialog, COMPONENT_PHONE_RECEIVING), false);
			uiController.setEnabled(uiController.find(phoneSettingsDialog, COMPONENT_PHONE_DELETE), false);
		} else {
			// No error, so remove the error message.
			uiController.remove(uiController.find(phoneSettingsDialog, "lbReceiveNotSupported"));
		}
		
		uiController.setAttachedObject(phoneSettingsDialog, phone);
		uiController.add(phoneSettingsDialog);
	}
	
	/**
	 * Enables or disables the <b>Edit Phone Settings</b>. 
	 * <br>If the supplied list is not empty, then the option is enabled. Otherwise, it is disabled.
	 * TODO would be good to know when this event is triggered
	 * @param list
	 * @param menuItem
	 */
	public void editPhoneEnabled(Object list, Object menuItem) {
		uiController.setVisible(menuItem, uiController.getSelectedItem(list) != null);
	}

	/**
	 * Disconnect from a specific {@link SmsDevice}.
	 * @param list The list of connected {@link SmsDevice}s in the Phones tab.
	 */
	public void disconnectFromSelected(Object list) {
		SmsDevice dev = uiController.getAttachedObject(uiController.getSelectedItem(list), SmsDevice.class); 
		phoneManager.disconnect(dev);
		refreshPhonesViews();
	}
	
	/**
	 * Stop detection of the {@link SmsDevice} on a specific port.
	 * @param list The list of ports which are currently being probed for connected {@link SmsDevice}s.
	 */
	public void stopDetection(Object list) {
		SmsDevice dev = uiController.getAttachedObject(uiController.getSelectedItem(list), SmsDevice.class);
		if (dev instanceof SmsModem) {
			SmsModem modem = (SmsModem) dev;
			phoneManager.stopDetection(modem.getPort());
			
		}
		refreshPhonesViews();
	}
	
	/**
	 * Action triggered when an item in the unconnected phones/ports list is selected.  This method
	 * enables and disables relevant items in the contextual popupmenu for the selected port.
	 * @param popUp
	 * @param list
	 */
	public void phoneManager_enabledFields(Object popUp, Object list) {
		Object selected = uiController.getSelectedItem(list);
		if (selected == null) {
			uiController.setVisible(popUp, false);
		} else {
			SmsDevice dev = uiController.getAttachedObject(selected, SmsDevice.class);
			if (dev instanceof SmsModem) {
				SmsModem modem = (SmsModem) dev;
				uiController.setVisible(uiController.find(popUp, "miEditPhone"), false);
				uiController.setVisible(uiController.find(popUp, "miAutoConnect"), !modem.isDetecting() && !modem.isTryToConnect());
				uiController.setVisible(uiController.find(popUp, "miManualConnection"), !modem.isDetecting() && !modem.isTryToConnect());
				uiController.setVisible(uiController.find(popUp, "miCancelDetection"), modem.isDetecting());
			} else {
				for (Object o : uiController.getItems(popUp)) {
					uiController.setVisible(o, uiController.getName(o).equals("miEditPhone") || uiController.getName(o).equals("miAutoConnect"));
				}
			}
		}
	}
	
	/**
	 * Attempt to automatically connect to the phone handler currently selected in the list of
	 * unconnected handsets.
	 */
	public void connectToSelectedPhoneHandler() {
		Object modemListError = uiController.find(COMPONENT_PHONE_MANAGER_MODEM_LIST_ERROR);
		SmsDevice selectedPhoneHandler = uiController.getAttachedObject(uiController.getSelectedItem(modemListError), SmsDevice.class);
		if(selectedPhoneHandler != null) {
			if (selectedPhoneHandler instanceof SmsModem) {
				SmsModem modem = (SmsModem) selectedPhoneHandler;
				try {
					phoneManager.requestConnect(modem.getPort());
				} catch (NoSuchPortException ex) {
					log.info("", ex);
				}
			} else {
				phoneManager.addSmsInternetService((SmsInternetService) selectedPhoneHandler);
			}
		}
	}
	
	/**
	 * Show the dialog for connecting a phone with manual configuration.
	 * @param list TODO what is this list, and why is it necessary?  Could surely just find it
	 */
	public void showPhoneConfigDialog(Object list) {
		Object configDialog = uiController.loadComponentFromFile(UI_FILE_MODEM_MANUAL_CONFIG_DIALOG, this);
		
		Object portList = uiController.find(configDialog, "lbPortName");
		Enumeration<CommPortIdentifier> commPortEnumeration = CommUtils.getPortIdentifiers();
		while (commPortEnumeration.hasMoreElements()) {
			CommPortIdentifier commPortIdentifier = commPortEnumeration.nextElement();
			uiController.add(portList, uiController.createComboboxChoice(commPortIdentifier.getName(), null));
		}
		
		Object handlerList = uiController.find(configDialog, "lbCATHandlers");
		int trimLength = DEFAULT_CAT_HANDLER_CLASS_NAME.length() + 1;
		
		for (Class<? extends AbstractATHandler> handler : AbstractATHandler.getHandlers()) {
			String handlerName = handler.getName();
			if(handlerName.equals(DEFAULT_CAT_HANDLER_CLASS_NAME)) handlerName = "<default>";
			else handlerName = handlerName.substring(trimLength);
			uiController.add(handlerList, uiController.createComboboxChoice(handlerName, handler));
		}
		
		Object selected = uiController.getSelectedItem(list);
		SmsDevice selectedPhone = uiController.getAttachedObject(selected, SmsDevice.class);
		if (selectedPhone instanceof SmsModem) {
			SmsModem modem = (SmsModem) selectedPhone;
			uiController.setText(uiController.find(configDialog,"lbPortName"), modem.getPort());
			uiController.setText(uiController.find(configDialog,"lbBaudRate"), String.valueOf(modem.getBaudRate()));
		}
		
		uiController.add(configDialog);
	}
	
	/**
	 * Connect to a phone using the manual settings provided in the {@link #showPhoneConfigDialog(Object)}.
	 * @param phoneConfigDialog
	 */
	public void connectToPhone(Object phoneConfigDialog) {
		String baudRateAsString = uiController.getText(uiController.find(phoneConfigDialog,"lbBaudRate"));
		String requestedPortName = uiController.getText(uiController.find(phoneConfigDialog,"lbPortName"));
		if (!phoneManager.hasPhoneConnected(requestedPortName)) {
			try {
				String preferredCATHandler = uiController.getText(uiController.find(phoneConfigDialog,"lbCATHandlers"));
				if(phoneManager.requestConnect(
						requestedPortName,
						Integer.parseInt(baudRateAsString),
						preferredCATHandler)) {
					uiController.remove(phoneConfigDialog);
				} else {
					uiController.alert(InternationalisationUtils.getI18NString(I18N_PORT_IN_USE));
				}
			} catch (NumberFormatException e) {
				uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_INVALID_BAUD_RATE, baudRateAsString));
			} catch (NoSuchPortException e) {
				uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_PORT_NOT_FOUND, requestedPortName));
			}
		} else {
			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_PORT_ALREADY_CONNECTED, requestedPortName));
		}
	}

	/** Starts the phone auto-detector. */
	public void phoneManager_detectModems() {
		phoneManager.refreshPhoneList(true);
	}
	
	/**
	 * called when one of the SMS devices (phones or http senders) has a change in status,
	 * such as detection, connection, disconnecting, running out of batteries, etc.
	 * see PhoneHandler.STATUS_CODE_MESSAGES[smsDeviceEventCode] to get the relevant messages
	 * @param device
	 * @param deviceStatus
	 */
	public void smsDeviceEvent(SmsDevice device, SmsDeviceStatus deviceStatus) {
		log.trace("ENTER");
		
		// Handle modems first
		if (device instanceof SmsModem) {
			SmsModem activeDevice = (SmsModem) device;
			uiController.setStatus(InternationalisationUtils.getI18NString(MESSAGE_PHONE) + ": " + activeDevice.getPort() + ' ' + getSmsDeviceStatusAsString(device));
			if (deviceStatus.equals(SmsModemStatus.CONNECTED)) {
				log.debug("Phone is connected. Try to read details from database!");
				String serial = activeDevice.getSerial();
				SmsModemSettings settings = phoneDetailsManager.getSmsModemSettings(serial);
				
				// If this is the first time we've attached this phone, or no settings were
				// saved last time, we should show the settings dialog automatically
				if(settings == null) {
					log.debug("User need to make setting related this phone.");
					showPhoneSettingsDialog(activeDevice, true);
				} else {
					activeDevice.setUseForSending(settings.useForSending());
					activeDevice.setUseDeliveryReports(settings.useDeliveryReports());

					if(activeDevice.supportsReceive()) {
						activeDevice.setUseForReceiving(settings.useForReceiving());
						activeDevice.setDeleteMessagesAfterReceiving(settings.deleteMessagesAfterReceiving());
					}
				}

				uiController.newEvent(new Event(Event.TYPE_PHONE_CONNECTED, InternationalisationUtils.getI18NString(COMMON_PHONE_CONNECTED) + ": " + activeDevice.getModel()));
			}
		} else {
			SmsInternetService service = (SmsInternetService) device;
			// TODO document why newEvent is called here, and why it is only called for certain statuses.
			if (deviceStatus.equals(SmsInternetServiceStatus.CONNECTED)) {
				uiController.newEvent(new Event(
						Event.TYPE_SMS_INTERNET_SERVICE_CONNECTED,
						InternationalisationUtils.getI18NString(COMMON_SMS_INTERNET_SERVICE_CONNECTED) 
						+ ": " + SmsInternetServiceSettingsHandler.getProviderName(service.getClass()) + " - " + service.getIdentifier()));
			} else if (deviceStatus.equals(SmsInternetServiceStatus.RECEIVING_FAILED)) {
				uiController.newEvent(new Event(
						Event.TYPE_SMS_INTERNET_SERVICE_RECEIVING_FAILED,
						SmsInternetServiceSettingsHandler.getProviderName(service.getClass()) + " - " + service.getIdentifier()
						+ ": " + InternationalisationUtils.getI18NString(COMMON_SMS_INTERNET_SERVICE_RECEIVING_FAILED)));
			}
		}
		refreshPhonesViews();
		log.trace("EXIT");
	}
	
	public void phoneManagerDetailsUse(Object phoneSettingsDialog, Object radioButton) {
		Object pnPhoneSettings = uiController.find(phoneSettingsDialog, COMPONENT_PN_PHONE_SETTINGS);
		if(COMPONENT_RB_PHONE_DETAILS_ENABLE.equals(uiController.getName(radioButton))) {
			uiController.activate(pnPhoneSettings);
			// If this phone does not support SMS receiving, we need to pass this info onto
			// the user.  We also want to gray out the options for receiving.
			SmsModem modem = uiController.getAttachedObject(phoneSettingsDialog, SmsModem.class);
			if(!modem.supportsReceive()) {
				uiController.setEnabled(uiController.find(pnPhoneSettings, COMPONENT_PHONE_RECEIVING), false);
				uiController.setEnabled(uiController.find(pnPhoneSettings, COMPONENT_PHONE_DELETE), false);
			}
		} else uiController.deactivate(pnPhoneSettings);
	}
	
	public void phoneManagerDetailsCheckboxChanged(Object checkbox) {
		uiController.setEnabled(uiController.getNextItem(uiController.getParent(checkbox), checkbox, false), uiController.isSelected(checkbox));
	}
	
	/**
	 * Event fired when the view phone details action is chosen.  We save the details
	 * of the phone to the database.
	 */
	public void updatePhoneDetails(Object dialog) {
		SmsModem phone = uiController.getAttachedObject(dialog, SmsModem.class);
		String serial = phone.getSerial();

		boolean useForSending;
		boolean useDeliveryReports;
		boolean useForReceiving;
		boolean deleteMessagesAfterReceiving;
		if(uiController.isSelected(uiController.find(dialog, COMPONENT_RB_PHONE_DETAILS_ENABLE))) {
			useForSending = uiController.isSelected(uiController.find(dialog, COMPONENT_PHONE_SENDING));
			useDeliveryReports = uiController.isSelected(uiController.find(dialog, COMPONENT_PHONE_DELIVERY_REPORTS));
			useForReceiving = uiController.isSelected(uiController.find(dialog, COMPONENT_PHONE_RECEIVING));
			deleteMessagesAfterReceiving = uiController.isSelected(uiController.find(dialog, COMPONENT_PHONE_DELETE));
		} else {
			useForSending = false;
			useDeliveryReports = false;
			useForReceiving = false;
			deleteMessagesAfterReceiving = false;
		}
		
		phone.setUseForSending(useForSending);
		phone.setUseDeliveryReports(useDeliveryReports);
		if(phone.supportsReceive()) {
			phone.setUseForReceiving(useForReceiving);
			phone.setDeleteMessagesAfterReceiving(deleteMessagesAfterReceiving);
		} else {
			useForReceiving = false;
			deleteMessagesAfterReceiving = false;
		}
		
		SmsModemSettings settings = this.phoneDetailsManager.getSmsModemSettings(serial);
		if(settings != null) {
			settings.setDeleteMessagesAfterReceiving(deleteMessagesAfterReceiving);
			settings.setUseDeliveryReports(useDeliveryReports);
			settings.setUseForReceiving(useForReceiving);
			settings.setUseForSending(useForSending);
			this.phoneDetailsManager.updateSmsModemSettings(settings);
		} else {
			settings = new SmsModemSettings(serial, useForSending, useForReceiving, deleteMessagesAfterReceiving, useDeliveryReports);
			this.phoneDetailsManager.saveSmsModemSettings(settings);
		}
		
		// Phone settings may have changed.  As we're now displaying these on the table, we
		// need to update the table.
		refreshPhonesViews();
		
		removeDialog(dialog);
	}
	
//> UI PASSTHROUGH METHODS
	/**
	 * @param page page to show
	 * @see UiGeneratorController#showHelpPage(String)
	 */
	public void showHelpPage(String page) {
		this.uiController.showHelpPage(page);
	}

//> INSTANCE HELPER METHODS
	/** 
	 * Refreshes the list of PhoneHandlers displayed on the PhoneManager tab.
	 */
	public void refreshPhonesViews() {
		synchronized (PHONES_LIST_SYNCH_OBJECT) {
			Object modemListError = uiController.find(COMPONENT_PHONE_MANAGER_MODEM_LIST_ERROR);
			// cache the selected item so we can reselect it when we've finished!
			int index = uiController.getSelectedIndex(modemListError);
			
			int indexTop = uiController.getSelectedIndex(getModemListComponent());
			
			uiController.removeAll(getModemListComponent());
			uiController.removeAll(modemListError);

			SmsDevice[] smsDevices = phoneManager.getAllPhones().toArray(new SmsDevice[0]);
			
			System.out.println();
			System.out.println("----------");
			System.out.println("Before sorting: ");
			for (int i = 0; i < smsDevices.length; i++) {
				SmsDevice smsDevice = smsDevices[i];
				System.out.println(i + ":\t" + smsDevice);
			}
			
			// Sort the SmsDevices by port name
			Arrays.sort(smsDevices, SMS_DEVICE_COMPARATOR);

			System.out.println("----------");
			System.out.println("After sorting: ");
			for (int i = 0; i < smsDevices.length; i++) {
				SmsDevice smsDevice = smsDevices[i];
				System.out.println(i + ":\t" + smsDevice);
			}
			System.out.println("----------");
			
			// Add the SmsDevices to the relevant tables
			for (SmsDevice dev : smsDevices) {
				if (dev.isConnected()) {
					uiController.add(getModemListComponent(), getTableRow(dev, true));
				} else {
					uiController.add(modemListError, getTableRow(dev, false));
				}
			}

			uiController.setSelectedIndex(getModemListComponent(), indexTop);
			uiController.setSelectedIndex(modemListError, index);
		}
	}
	
	/**
	 * Gets a table row Thinlet UI Component for the supplied handset.  This table row
	 * is designed for display on the Phone Manager's handset list.  The PhoneHandler
	 * object is attached to the component, and the component's icon is set appropriately.
	 * @param dev
	 * @param isConnected <code>true</code> if the supplied device is to appear in the "connected" list; <code>false</code> otherwise
	 * @return Thinlet table row component detailing the supplied device.
	 */
	private Object getTableRow(SmsDevice dev, boolean isConnected) {
		Object row = uiController.createTableRow(dev);

// FIXME these now share getMsisdn() code.
		String statusString, fromMsisdn, makeAndModel, serial, port, baudRate;
		String icon;
		if (dev instanceof SmsModem) {
			SmsModem handset = (SmsModem) dev;
			if(handset.isConnected()) {
				icon = Icon.LED_GREEN;
			} else if(handset.isPhonePresent()) {
				icon = Icon.LED_AMBER;
			} else {
				icon = Icon.LED_RED;
			}

			port = handset.getPort();
			baudRate = Integer.toString(handset.getBaudRate());
			fromMsisdn = handset.getMsisdn();
			makeAndModel = Utils.getManufacturerAndModel(handset.getManufacturer(), handset.getModel());
			serial = handset.getSerial();
			
			statusString = getSmsDeviceStatusAsString(handset);
		} else {
			SmsInternetService serv = (SmsInternetService) dev;
			if(serv.isConnected()) {
				icon = Icon.LED_GREEN;
			} else {
				icon = Icon.LED_RED;
			}
			
			port = serv.isEncrypted() ? "HTTPS" : "HTTP";
			baudRate = "";
			fromMsisdn = serv.getMsisdn();
			makeAndModel = SmsInternetServiceSettingsHandler.getProviderName(serv.getClass());
			serial = serv.getIdentifier();
			statusString = getSmsDeviceStatusAsString(serv);
		}

		// Add "status cell" - "traffic light" showing how functional the device is
		Object statusCell = uiController.createTableCell("");
		uiController.setIcon(statusCell, icon);
		uiController.add(row, statusCell);

		uiController.table_addCells(row, new String[]{port, baudRate, fromMsisdn, makeAndModel, serial});
		
		if (isConnected) {
			Object useForSendingCell = uiController.createTableCell("");
			if (dev.isUseForSending()) uiController.setIcon(useForSendingCell, Icon.TICK);
			uiController.add(row, useForSendingCell);
			Object useForReceiveCell = uiController.createTableCell("");
			if (dev.isUseForReceiving()) uiController.setIcon(useForReceiveCell, Icon.TICK);
			uiController.add(row, useForReceiveCell);
		}
		uiController.add(row, uiController.createTableCell(statusString));
		
		return row;
	}
	
//> UI PASSTHROUGH METHODS
	/**
	 * Removes a dialog from the UI
	 * @param dialog dialog to remove 
	 * @see UiGeneratorController#removeDialog(Object)
	 */
	public void removeDialog(Object dialog) {
		this.uiController.removeDialog(dialog);
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/**
	 * Gets the status of an {@link SmsDevice} as an internationalised {@link String}.
	 * @param device
	 * @return An internationalised {@link String} describing the status of the {@link SmsDevice}.
	 */
	private static String getSmsDeviceStatusAsString(SmsDevice device) {
		return InternationalisationUtils.getI18NString(device.getStatus().getI18nKey(), device.getStatusDetail());
	}
}
