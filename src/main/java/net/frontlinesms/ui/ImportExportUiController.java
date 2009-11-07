/**
 * 
 */
package net.frontlinesms.ui;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import net.frontlinesms.Utils;
import net.frontlinesms.csv.CsvExporter;
import net.frontlinesms.csv.CsvImporter;
import net.frontlinesms.csv.CsvRowFormat;
import net.frontlinesms.csv.CsvUtils;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.Keyword;
import net.frontlinesms.data.domain.Message;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.data.repository.KeywordDao;
import net.frontlinesms.data.repository.MessageDao;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

/**
 * UI Methods for Importing and Exporting data from FrontlineSMS.
 * @author Alex
 */
public class ImportExportUiController implements ThinletUiEventHandler {
//> STATIC CONSTANTS
	
//> I18N KEYS
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_NO_FILENAME = "message.filename.blank";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_NO_FIELD_SELECTED = "message.no.field.selected";
	
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_EXPORTING_SELECTED_CONTACTS = "message.exporting.selected.contacts";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_EXPORTING_SELECTED_KEYWORDS = "message.exporting.selected.keywords";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_EXPORTING_SELECTED_MESSAGES = "message.exporting.selected.messages";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_EXPORT_TASK_FAILED = "message.export.failed";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_EXPORT_TASK_SUCCESSFUL = "message.export.successful";

	/** I18n Text Key: TODO document */
	private static final String MESSAGE_IMPORTING_SELECTED_CONTACTS = "message.importing.selected.contacts";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_IMPORTING_SELECTED_KEYWORDS = "message.importing.selected.keywords";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_IMPORTING_SELECTED_MESSAGES = "message.importing.selected.messages";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_IMPORT_TASK_FAILED = "message.import.failed";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_IMPORT_TASK_SUCCESSFUL = "message.import.successful";
	
//> THINLET LAYOUT DEFINITION FILES
	/** UI XML File Path: This is the outline for the dialog for EXPORTING */
	private static final String UI_FILE_EXPORT_WIZARD_FORM = "/ui/core/importexport/exportWizardForm.xml";
	/** UI XML File Path: This is the outline for the dialog for IMPORTING */
	private static final String UI_FILE_IMPORT_WIZARD_FORM = "/ui/core/importexport/importWizardForm.xml";
	/** UI XML File Path: TODO document */
	private static final String UI_FILE_OPTIONS_PANEL_CONTACT = "/ui/core/importexport/pnContactDetails.xml";
	/** UI XML File Path: TODO document */
	private static final String UI_FILE_OPTIONS_PANEL_MESSAGE = "/ui/core/importexport/pnMessageDetails.xml";
	/** UI XML File Path: TODO document */
	private static final String UI_FILE_OPTIONS_PANEL_KEYWORD = "/ui/core/importexport/pnKeywordDetails.xml";
	
//> THINLET COMPONENT NAMES
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_NOTES = "cbNotes";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_EMAIL = "cbEmail";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_OTHER_PHONE = "cbOtherPhone";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_PHONE = "cbPhone";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_NAME = "cbName";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_STATUS = "cbStatus";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_TYPE = "cbType";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_KEYWORD = "cbKeyword";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_CONTENT = "cbContent";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_RECIPIENT = "cbRecipient";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_SENDER = "cbSender";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_DATE = "cbDate";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_DESCRIPTION = "cbDescription";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_CONTACT_NOTES = "cbContactNotes";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_CONTACT_EMAIL = "cbContactEmail";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_CONTACT_OTHER_NUMBER = "cbContactOtherNumber";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_CONTACT_NAME = "cbContactName";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_RECEIVED = "cbReceived";
	/** Thinlet Component Name: TODO document */
	private static final String COMPONENT_CB_SENT = "cbSent";
	
//> STATIC CONSTANTS
	/** Export entity type: {@link Contact} */
	private static final String TYPE_CONTACT = "contacts";
	/** Export entity type: {@link Message} */
	private static final String TYPE_MESSAGE = "messages";
	/** Export entity type: {@link Keyword} */
	@SuppressWarnings("unused")
	private static final String TYPE_KEYWORD = "keywords";

//> INSTANCE PROPERTIES
	/** Logging object */
	private final Logger log = Utils.getLogger(this.getClass());
	/** Data access object for {@link Contact}s */
	private final ContactDao contactDao;
	/** Data access object for {@link Message}s */
	private final MessageDao messageDao;
	/** Data access object for {@link Keyword}s */
	private final KeywordDao keywordDao;
	/** The {@link UiGeneratorController} that shows the tab. */
	private final UiGeneratorController uiController;
	
	/** Dialog for gathering details of the export or import */
	private Object wizardDialog;

	/** Marks whether we are importing or exporting.  <code>true</code> indicates export, <code>false</code> indicates import. */
	private boolean export;
	/** The type of object we are dealing with, one of {@link #TYPE_CONTACT}, {@link #TYPE_KEYWORD}, {@link #TYPE_MESSAGE}. */
	private String type;
	/** The objects we are exporting - a selection of thinlet components with attached {@link Contact}s, {@link Keyword}s or {@link Message}s */
	private Object attachedObject;

//> CONSTRUCTORS
	/**
	 * Create a new instance of this controller.
	 * @param uiController 
	 * @param contactDao 
	 * @param messageDao 
	 * @param keywordDao 
	 */
	public ImportExportUiController(UiGeneratorController uiController, ContactDao contactDao, MessageDao messageDao, KeywordDao keywordDao) {
		this.uiController = uiController;
		this.contactDao = contactDao;
		this.messageDao = messageDao;
		this.keywordDao = keywordDao;
	}
	
//> ACCESSORS
	/** @return The type of object we are dealing with, one of {@link #TYPE_CONTACT}, {@link #TYPE_KEYWORD}, {@link #TYPE_MESSAGE}. */
	private String getType() {
		return this.type;
	}

//> UI SHOW METHODS
	/**
	 * Shows the export wizard dialog, according to the supplied type.
	 * @param export 
	 * @param list The list to get selected items from.
	 * @param type The desired type ({@link #TYPE_CONTACT} for Contacts, {@link #TYPE_MESSAGE} for Messages and {@link #TYPE_KEYWORD} for Keywords)
	 */
	public void showWizard(boolean export, Object list, String type){
		Object[] selected = uiController.getSelectedItems(list);
		if (selected.length == 0) {
			// If there are no highlighted items to export, don't do anything
			return;
		}

		init(export, type, selected);
		_showWizard();
	}
	
	/**
	 * Shows the export wizard dialog, according to the supplied type.
	 * @param export 
	 * @param type The desired type ({@link #TYPE_CONTACT} for Contacts, {@link #TYPE_MESSAGE} for Messages and {@link #TYPE_KEYWORD} for Keywords)
	 */
	public void showWizard(boolean export, String type){
		init(export, type, null);
		_showWizard();
	}
	
//> PUBLIC UI METHODS
	/**
	 * Setup the details of the dialog.
	 * @param export value for {@link #export}
	 * @param type value for {@link #type}
	 * @param attachedObject value for {@link #attachedObject}
	 */
	private void init(boolean export, String type, Object attachedObject) {
		this.export = export;
		this.type = type;
		this.attachedObject = attachedObject;
	}
	
	/**
	 * Executes the import action.
	 * @param dataPath The path to the file to import data from.
	 */
	public void doImport(String dataPath) {
		log.trace("ENTER");
		// Make sure that a file has been selected to import from
		if (dataPath.equals("")) {
			log.debug("dataPath is blank.");
			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_FILENAME));
			log.trace("EXIT");
			return;
		}
		
		try {
			// Do the import
			if(getType().equals(TYPE_CONTACT)) {
				CsvRowFormat rowFormat = getRowFormatForContact();
				CsvImporter.importContacts(new File(dataPath), this.contactDao, rowFormat);
			} else throw new IllegalStateException("Import is not supported for: " + getType());
			uiController.setStatus(InternationalisationUtils.getI18NString(MESSAGE_IMPORT_TASK_SUCCESSFUL));
			uiController.removeDialog(wizardDialog);
		} catch(Exception ex) {
			log.debug(InternationalisationUtils.getI18NString(MESSAGE_IMPORT_TASK_FAILED), ex);
			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_IMPORT_TASK_FAILED) + ": " + ex.getMessage());
		}
		log.trace("EXIT");
	}
	
	/**
	 * Executes the export action.
	 * @param dataPath The path to the file to export data to.
	 */
	public void doExport(String dataPath) {
		log.trace("ENTER");
		// Make sure that a file has been selected to export to.
		if (dataPath.equals("")) {
			log.debug("dataPath is blank.");
			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_FILENAME));
			log.trace("EXIT");
			return;
		}
		log.debug("Filename is [" + dataPath + "] before [" + CsvExporter.CSV_EXTENSION + "] check.");
		if (!dataPath.endsWith(CsvExporter.CSV_EXTENSION)) {
			dataPath += CsvExporter.CSV_EXTENSION;
		}
		log.debug("Filename is [" + dataPath + "] after [" + CsvExporter.CSV_EXTENSION + "] check.");
		
		try {
			if (this.attachedObject != null) {
				log.debug("Exporting selected objects...");
				doExportSelected(wizardDialog, dataPath, (Object[])this.attachedObject);
			} else if (getType().equals(TYPE_CONTACT)) {
				//CONTACTS
				log.debug("Exporting all contacts..");
				exportContacts(this.contactDao.getAllContacts(), dataPath);
			} else if (getType() == TYPE_MESSAGE) {
				//MESSAGES
				log.debug("Exporting all messages..");
				exportMessages(this.messageDao.getAllMessages(), dataPath);
			} else {
				//KEYWORDS
				log.debug("Exporting all keywords..");
				exportKeywords(this.keywordDao.getAllKeywords(), dataPath);
			}

			uiController.removeDialog(wizardDialog);
		} catch(IOException ex) {
			log.debug(InternationalisationUtils.getI18NString(MESSAGE_EXPORT_TASK_FAILED), ex);
			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_EXPORT_TASK_FAILED) + ": " + ex.getMessage());
		} finally {
			log.trace("EXIT");
		}
	}

//> INSTANCE HELPER METHODS
	/**
	 * Export the supplied {@link Message}s using settings set in {@link #wizardDialog}.
	 * @param messages The messages to export
	 * @param filename The file to export the contacts to
	 * @throws IOException 
	 */
	private void exportMessages(List<Message> messages, String filename) throws IOException {
		CsvRowFormat rowFormat = getRowFormatForMessage();
		if (!rowFormat.hasMarkers()) {
			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_FIELD_SELECTED));
			log.trace("EXIT");
			return;
		}
		if(log.isDebugEnabled()) log.debug("Row Format: " + rowFormat);
		CsvExporter.exportMessages(new File(filename), messages, rowFormat, contactDao);
		uiController.setStatus(InternationalisationUtils.getI18NString(MESSAGE_EXPORT_TASK_SUCCESSFUL));
	}
	
	/**
	 * Export the supplied contacts using settings set in {@link #wizardDialog}.
	 * @param contacts The contacts to export
	 * @param filename The file to export the contacts to
	 * @throws IOException 
	 */
	private void exportContacts(List<Contact> contacts, String filename) throws IOException {
		CsvRowFormat rowFormat = getRowFormatForContact();
		
		if (!rowFormat.hasMarkers()) {
			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_FIELD_SELECTED));
			log.trace("EXIT");
			return;
		}
		
		log.debug("Row Format [" + rowFormat + "]");
		
		CsvExporter.exportContacts(new File(filename), contacts, rowFormat);
		uiController.setStatus(InternationalisationUtils.getI18NString(MESSAGE_EXPORT_TASK_SUCCESSFUL));
	}
	
	/**
	 * Export the supplied keywords using the settings in {@link #wizardDialog}.
	 * @param keywords keywords to export
	 * @param filename file to save to
	 * @throws IOException 
	 */
	private void exportKeywords(List<Keyword> keywords, String filename) throws IOException {
		//KEYWORDS
		log.debug("Exporting all keywords..");
		
		int messageType = getMessageType();
		CsvRowFormat rowFormat = getRowFormatForKeyword(messageType);
		if (!rowFormat.hasMarkers()) {
			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_FIELD_SELECTED));
			log.trace("EXIT");
			return;
		}
		log.debug("Row Format [" + rowFormat + "]");
		CsvExporter.exportKeywords(new File(filename), keywords, rowFormat, this.contactDao, this.messageDao, messageType);
		uiController.setStatus(InternationalisationUtils.getI18NString(MESSAGE_EXPORT_TASK_SUCCESSFUL));
	}
	
	/**
	 * Gets the title to use for the title of Export wizard
	 * @return i18n key for fetching the title of the wizard
	 */
	private String getWizardTitleI18nKey() {
		if(this.export) {
			if (getType().equals(TYPE_CONTACT)) {
				return MESSAGE_EXPORTING_SELECTED_CONTACTS;
			} else if (type.equals(TYPE_MESSAGE)) {
				return MESSAGE_EXPORTING_SELECTED_MESSAGES;
			} else {
				return MESSAGE_EXPORTING_SELECTED_KEYWORDS;
			}
		} else {
			if (getType().equals(TYPE_CONTACT)) {
				return MESSAGE_IMPORTING_SELECTED_CONTACTS;
			} else if (type.equals(TYPE_MESSAGE)) {
				return MESSAGE_IMPORTING_SELECTED_MESSAGES;
			} else {
				return MESSAGE_IMPORTING_SELECTED_KEYWORDS;
			}
		}
	}
	
	/** Show the wizard for importing or exporting a particular type of entity. */
	private void _showWizard() {
		// Load the correct export wizard pane
		String uiFile;
		if (getType().equals(TYPE_CONTACT)) {
			uiFile = UI_FILE_OPTIONS_PANEL_CONTACT;
		} else if (type.equals(TYPE_MESSAGE)) {
			uiFile = UI_FILE_OPTIONS_PANEL_MESSAGE;
		} else {
			uiFile = UI_FILE_OPTIONS_PANEL_KEYWORD;
		}
		
		// Load the import/export wizard, and save it to the class reference
		this.wizardDialog = uiController.loadComponentFromFile(this.export ? UI_FILE_EXPORT_WIZARD_FORM : UI_FILE_IMPORT_WIZARD_FORM, this);
		
		uiController.setAttachedObject(this.wizardDialog, attachedObject);
		
		String titleI18nKey = getWizardTitleI18nKey();
		uiController.setText(this.wizardDialog, InternationalisationUtils.getI18NString(titleI18nKey));
		uiController.add(this.wizardDialog, uiController.loadComponentFromFile(uiFile, this), 1);

		// Add the wizard to the Thinlet controller
		uiController.add(this.wizardDialog);
	}
	
	/**
	 * Gets the objects attached to the selected Thinlet components.
	 * @param <T> Class of the selected objects
	 * @param selectedClass Class of the selected Objects
	 * @param selected Array of selected Thinlet components
	 * @return List of the attached objects from the selected components
	 */
	@SuppressWarnings("unchecked")
	private <T extends Object> List<T> getSelected(Class<T> selectedClass, Object[] selected) {
		List<T> objects = new LinkedList<T>();
		for (Object o : selected) {
			objects.add((T) this.uiController.getAttachedObject(o));
		}
		return objects;
	}
	
	/**
	 * Exports information about the previous selected objects.
	 * 
	 * @param exportDialog Holds the information regarding the export row format (Contact.Name, Contact.Sender, etc).
	 * @param filename The file to be created with the export data.
	 * @param selected The selected objects to be exported.
	 * @throws IOException 
	 */
	private void doExportSelected(Object exportDialog, String filename, Object[] selected) throws IOException {
		log.trace("ENTER");
		if (getType().equals(TYPE_CONTACT)) {
			//CONTACTS
			log.debug("Exporting selected contacts...");
			exportContacts(getSelected(Contact.class, selected), filename);
		} else if (getType().equals(TYPE_MESSAGE)) {
			//MESSAGES
			log.debug("Exporting selected messages...");
			exportMessages(getSelected(Message.class, selected), filename);
		} else {
			//KEYWORDS
			log.debug("Exporting selected keywords...");
			exportKeywords(getSelected(Keyword.class, selected), filename);
		}
		log.trace("EXIT");
	}

	/**
	 * Get the type of {@link Message} that has been selected to export.
	 * @return {@link Message#TYPE_ALL}, {@link Message#TYPE_ALL}, {@link Message#TYPE_ALL} or -1
	 * TODO why is this allowed to return -1?  Is this possible?
	 */
	private final int getMessageType() {
		boolean sent = isChecked(COMPONENT_CB_SENT);
		boolean received = isChecked(COMPONENT_CB_RECEIVED);
		
		int type = -1;
		if (sent && received) { 
			type = Message.TYPE_ALL;
		} else if (sent) {
			type = Message.TYPE_OUTBOUND;
		} else if (received) {
			type = Message.TYPE_RECEIVED;
		}

		if(log.isDebugEnabled()) log.debug("Message Type: " + type);
		
		return type;
	}
	
	/**
	 * Creates an export row format for keywords.
	 * @param type Type of {@link Message} to export, e.g. {@link Message#TYPE_RECEIVED}
	 * @return The row format for exporting {@link Keyword}s to CSV
	 */
	private CsvRowFormat getRowFormatForKeyword(int type) {
		CsvRowFormat rowFormat = new CsvRowFormat();
		addMarker(rowFormat, CsvUtils.MARKER_KEYWORD_KEY, COMPONENT_CB_KEYWORD);
		addMarker(rowFormat, CsvUtils.MARKER_KEYWORD_DESCRIPTION, COMPONENT_CB_DESCRIPTION);

		if (type != -1) {
			if (type == Message.TYPE_ALL) {
				rowFormat.addMarker(CsvUtils.MARKER_MESSAGE_TYPE);
			}
			addMarker(rowFormat, CsvUtils.MARKER_MESSAGE_DATE, COMPONENT_CB_DATE);
			addMarker(rowFormat, CsvUtils.MARKER_MESSAGE_CONTENT, COMPONENT_CB_CONTENT);
			addMarker(rowFormat, CsvUtils.MARKER_SENDER_NUMBER, COMPONENT_CB_SENDER);
			addMarker(rowFormat, CsvUtils.MARKER_RECIPIENT_NUMBER, COMPONENT_CB_RECIPIENT);
			addMarker(rowFormat, CsvUtils.MARKER_CONTACT_NAME, COMPONENT_CB_CONTACT_NAME);
			addMarker(rowFormat, CsvUtils.MARKER_CONTACT_OTHER_PHONE, COMPONENT_CB_CONTACT_OTHER_NUMBER);
			addMarker(rowFormat, CsvUtils.MARKER_CONTACT_EMAIL, COMPONENT_CB_CONTACT_EMAIL);
			addMarker(rowFormat, CsvUtils.MARKER_CONTACT_NOTES, COMPONENT_CB_CONTACT_NOTES);
		}
		return rowFormat;
	}
	
	/**
	 * Checks if a Thinlet checkbox component is checked.
	 * @param checkboxComponentName The name of the checkbox component.
	 * @return <code>true</code> if the checkbox is checked
	 */
	private boolean isChecked(String checkboxComponentName) {
		assert (this.wizardDialog != null) : "The exportDialog property is currently null.  Should be set when the dialog is displayed.";
		Object cbComponent = uiController.find(wizardDialog, checkboxComponentName);
		assert (cbComponent != null) : "The checkbox component could not be found with name: " + checkboxComponentName;
		return this.uiController.isSelected(cbComponent);
	}
	
	/**
	 * Adds a marker to the {@link CsvRowFormat} iff the checkbox is checked.
	 * @param rowFormat
	 * @param marker
	 * @param checkboxComponentName
	 */
	private void addMarker(CsvRowFormat rowFormat, String marker, String checkboxComponentName) {
		if(isChecked(checkboxComponentName)) {
			rowFormat.addMarker(marker);
		}
	}
	
	/**
	 * Creates an export row format for messages.
	 * @return {@link CsvRowFormat} for message, reflecting the settings in {@link #wizardDialog}
	 */
	private CsvRowFormat getRowFormatForMessage() {
		CsvRowFormat rowFormat = new CsvRowFormat();
		addMarker(rowFormat, CsvUtils.MARKER_MESSAGE_TYPE, COMPONENT_CB_TYPE);
		addMarker(rowFormat, CsvUtils.MARKER_MESSAGE_STATUS, COMPONENT_CB_STATUS);
		addMarker(rowFormat, CsvUtils.MARKER_MESSAGE_DATE, COMPONENT_CB_DATE);
		addMarker(rowFormat, CsvUtils.MARKER_MESSAGE_CONTENT, COMPONENT_CB_CONTENT);
		addMarker(rowFormat, CsvUtils.MARKER_SENDER_NUMBER, COMPONENT_CB_SENDER);
		addMarker(rowFormat, CsvUtils.MARKER_RECIPIENT_NUMBER, COMPONENT_CB_RECIPIENT);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_NAME, COMPONENT_CB_CONTACT_NAME);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_OTHER_PHONE, COMPONENT_CB_CONTACT_OTHER_NUMBER);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_EMAIL, COMPONENT_CB_EMAIL);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_NOTES, COMPONENT_CB_NOTES);
		return rowFormat;
	}
	
	/**
	 * Creates an export row format for {@link Contact}s.
	 * @return {@link CsvRowFormat} for contacts, reflecting the settings in {@link #wizardDialog}
	 */
	private CsvRowFormat getRowFormatForContact() {
		CsvRowFormat rowFormat = new CsvRowFormat();
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_NAME, COMPONENT_CB_NAME);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_PHONE, COMPONENT_CB_PHONE);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_OTHER_PHONE, COMPONENT_CB_OTHER_PHONE);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_EMAIL, COMPONENT_CB_EMAIL);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_STATUS, COMPONENT_CB_STATUS);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_NOTES, COMPONENT_CB_NOTES);
		addMarker(rowFormat, CsvUtils.MARKER_CONTACT_GROUPS, UiGeneratorControllerConstants.COMPONENT_CB_GROUPS);
		return rowFormat;
	}
	
//> UI PASS-THRU METHODS
	/** @param dialog the dialog to remove
	 * @see UiGeneratorController#remove(Object) */
	public void removeDialog(Object dialog) {
		this.uiController.remove(dialog);
	}
	
	/** @param textFieldToBeSet Thinlet textfield whose value will be set with the selected file
	 * @see FrontlineUI#showOpenModeFileChooser(Object) */
	public void showOpenModeFileChooser(Object textFieldToBeSet) {
		this.uiController.showOpenModeFileChooser(textFieldToBeSet);
	}
	
	/** @param textFieldToBeSet Thinlet textfield whose value will be set with the selected file
	 * @see FrontlineUI#showOpenModeFileChooser(Object) */
	public void showSaveModeFileChooser(Object textFieldToBeSet) {
		this.uiController.showSaveModeFileChooser(textFieldToBeSet);
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}