/**
 * 
 */
package net.frontlinesms.plugins.forms.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import thinlet.Thinlet;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.Group;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.plugins.forms.FormsPluginController;
import net.frontlinesms.plugins.forms.data.domain.*;
import net.frontlinesms.plugins.forms.data.repository.*;
import net.frontlinesms.plugins.forms.data.repository.memory.*;
import net.frontlinesms.plugins.forms.ui.components.*;
import net.frontlinesms.plugins.PluginController;
import net.frontlinesms.ui.FrontlineUI;
import net.frontlinesms.ui.Icon;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

/**
 * Thinlet controller class for the FrontlineSMS Forms plugin.
 * @author Alex
 */
public class FormsThinletTabController {
//> CONSTANTS
	/** XML file containing forms pane for viewing results of a form */
	protected static final String UI_FILE_RESULTS_VIEW = "/ui/plugins/forms/formsTab_resultsView.xml";
	/** XML file containing dialog for exporting form data */
	private static final String UI_FILE_FORM_EXPORT_DIALOG = "/ui/plugins/forms/formExportDialogForm.xml";
	/** XML file containing dialog for choosing which contacts to send a form to */
	private static final String XML_CHOOSE_CONTACTS = "/ui/plugins/forms/chooseContacts.xml";
	
	/** The name of the Forms tab */
	private static final String TAB_FORMS = ":forms";
	public static final String COMMON_FORM_NAME = "form.name";
	public static final String COMMON_FORMS_EDITOR = "form.editor";
	public static final String MESSAGE_FORM_NAME_BLANK = "form.contact.unknown";
	public static final String FORMS_FIELD_CURRENCY = "form.field.currency";
	/** Component name of the forms list */
	private static final String FORMS_LIST_COMPONENT_NAME = "formsList";
	
//> INSTANCE PROPERTIES
	/** Logging object */
	private final Logger LOG = Utils.getLogger(this.getClass());
	/** The {@link PluginController} that owns this class. */
	private final FormsPluginController pluginController;
	/** The {@link UiGeneratorController} that shows the tab. */
	private final UiGeneratorController uiController;
	// FIXME work out what this is here for
	private Object formResultsComponent;
	// FIXME rename this, and work out where it should be coming from!
	private ContactDao contactFactory;
	// FIXME rename this, and work out where it should be coming from!
	private final FormDao formsDao = new InMemoryFormDao();
	// FIXME rename this, and work out where it should be coming from!
	private final FormResponseDao formResponseDao = new InMemoryFormResponseDao();
	
//> CONSTRUCTORS
	/**
	 * Create a new instance of this class.
	 * @param pluginController
	 * @param uiController
	 */
	public FormsThinletTabController(FormsPluginController pluginController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.uiController = uiController;
		this.contactFactory = pluginController.getFrontlineController().getContactDao();
	}
	
//> PASS-THROUGH METHODS TO UI CONTROLLER
	/** @see UiGeneratorController#showHelpPage(String) */
	public void showHelpPage(String page) {
		uiController.showHelpPage(page);
	}
	
	/** @see UiGeneratorController#showConfirmationDialog(String) */
	public void showConfirmationDialog(String methodToBeCalled) {
		this.uiController.showConfirmationDialog(methodToBeCalled, this);
	}
	
	/** @see UiGeneratorController#groupList_expansionChanged(Object) */
	public void groupList_expansionChanged(Object groupList) {
		this.uiController.groupList_expansionChanged(groupList);
	}
	
	/** @see UiGeneratorController#removeDialog(Object) */
	public void removeDialog(Object dialog) {
		this.uiController.removeDialog(dialog);
	}
	
//> THINLET EVENT METHODS
	/** Show the dialog for exporting form results. */
	public void showFormExportDialog() {
		uiController.add(uiController.loadComponentFromFile(UI_FILE_FORM_EXPORT_DIALOG, this));
	}
	
	/** Show the AWT Forms Editor window */
	public void showFormsEditor() {
		VisualForm form = new VisualForm();
		form = FormsUiController.getInstance().showFormsEditor(uiController.getFrameLauncher(), form);
		if (form != null) {
			saveFormInformation(form);
		}
	}

	/**
	 * Called when the user has selected a different item on the forms tree.
	 * @param formsList
	 */
	public void formsTab_selectionChanged(Object formsList) {
		Object sel = uiController.getAttachedObject(uiController.getSelectedItem(formsList));
		Form selectedForm = getForm(sel);
		
		if (selectedForm != null) {
			if (selectedForm.isFinalised()) {
				showResultsPanel(selectedForm);
			}
		} else {
			//Nothing selected
			Object formsTab = uiController.getCurrentTab();
			Object pnRight = uiController.find(formsTab, "pnRight");
			uiController.removeAll(pnRight);
		}
		formsTab_enabledFields(null, formsList, uiController.find(uiController.getParent(formsList), "toolbar"));
	}
	
	/**
	 * Show the GUI to edit a form.
	 * @param list Reference to the Forms tree object.
	 */
	public void editSelected(Object list) {
		Object selectedComponent = uiController.getSelectedItem(list);
		if (selectedComponent != null) {
			Form f = getForm(uiController.getAttachedObject(selectedComponent));
			VisualForm visualForm = VisualForm.getVisualForm(f);
			List<PreviewComponent> old = new ArrayList<PreviewComponent>();
			old.addAll(visualForm.getComponents());
			visualForm = FormsUiController.getInstance().showFormsEditor(uiController.getFrameLauncher(), visualForm);
			if (visualForm != null) {
				if (!visualForm.getName().equals(f.getName())) {
					f.setName(visualForm.getName());
				}
				updateForm(old, visualForm.getComponents(), f);
				addToFormsList(f);
				formsTab_selectionChanged(list);
			}
		}
	}
	
	/**
	 * Shows a selecter for assigning a {@link Group} to a {@link Form}
	 * @param formsList
	 */
	public void showGroupSelecter(Object formsList) {
		Form selectedForm = getForm(uiController.getAttachedObject(uiController.getSelectedItem(formsList)));
		System.out.println("FormsThinletTabController.showGroupSelecter() : " + selectedForm);
		if(selectedForm != null) {
			// FIXME i18n
			uiController.showGroupSelecter(selectedForm, false, "Choose a group", "setSelectedGroup(groupSelecter, groupSelecter_groupList)", this);
		}
	}

	/**
	 * @param groupSelecter
	 * @param groupList
	 */
	public void setSelectedGroup(Object groupSelecter, Object groupList) {
		System.out.println("FormsThinletTabController.setSelectedGroup()");
		Form form = getForm(uiController.getAttachedObject(groupSelecter));
		System.out.println("Form: " + form);
		Group group = uiController.getGroup(uiController.getSelectedItem(groupList));
		System.out.println("Group: " + group);
		if(group != null) {
			form.setPermittedGroup(group);
			removeDialog(groupSelecter);
			refreshFormsList();
		}
	}
	
	/**
	 * Attempt to send the form selected in the forms list
	 * @param formsList the forms list component
	 */
	public void sendSelected(Object formsList) {
		Form selectedForm = getForm(uiController.getAttachedObject(uiController.getSelectedItem(formsList)));
		if(selectedForm != null) {
			// check the form has a group set
			if(selectedForm.getPermittedGroup() == null) {
				// The form has no group set, so we should explain that this needs to be done.
				// FIXME i18n
				uiController.alert("You must set a group for this form.\nYou can do this by right-clicking on the form.");
			} else if(!selectedForm.isFinalised()) { // check the form is finalised.
				// if form is not finalised, warn that it will be!
				uiController.showConfirmationDialog("showSendSelectionDialog", this, "form.confirm.finalise");
			} else {
				// show dialog for selecting group members to send the form to
				showSendSelectionDialog();
			}
		}
	}
	
	/**
	 * Show dialog for selecting users to send a form to.  If the form is not finalised, it will be
	 * finalised within this method.
	 */
	public void showSendSelectionDialog() {
		uiController.removeConfirmationDialog();
		
		Object selectionAttachment = getSelectionAttachment();
		Form form = getForm(selectionAttachment);
		if(form != null) {
			// if form is not finalised, finalise it now
			if(!form.isFinalised()) {
				formsDao.finaliseForm(form);
			}
			
			// show selection dialog for Contacts in the form's group
			Object chooseContactsDialog = uiController.loadComponentFromFile(XML_CHOOSE_CONTACTS, this);
			uiController.setAttachedObject(chooseContactsDialog, form);
			uiController.add(uiController.find(chooseContactsDialog, "lsContacts"),
					uiController.getNode(form.getPermittedGroup(), false));
			uiController.add(chooseContactsDialog);
		}
	}
	
	/**
	 * Send a form to the contacts selected in the dialog.
	 * @param pnFormChooseContacts Dialog containing the contact selection
	 * @param lsContacts list of contacts & groups
	 */
	public void sendForm(Object pnFormChooseContacts, Object lsContacts) {
		// Work out which contacts we should be sending the form to
		Object[] selectedItems = uiController.getSelectedItems(lsContacts);
		Form form = getForm(uiController.getAttachedObject(pnFormChooseContacts));
		if(selectedItems.length > 0) {
			HashSet<Contact> selectedContacts = new HashSet<Contact>();
			for(Object o : selectedItems) {
				Object attachment = uiController.getAttachedObject(o);
				if(attachment instanceof Contact) {
					selectedContacts.add((Contact)attachment);
				} else if(attachment instanceof Group) {
					Group g = (Group)attachment;
					selectedContacts.addAll(g.getDirectMembers());
				}
			}
		
			// Issue the send command to the plugin controller
			this.pluginController.sendForm(form, selectedContacts);

			// FIXME i18n
			uiController.alert("Your form '" + form.getName() + "' has been sent to " + selectedContacts.size() + " contacts.");
			
			uiController.removeDialog(pnFormChooseContacts);
		}
	}
	
	/** Finds the forms list and deletes the selected item. */
	public void deleteSelected() {
		Object formsList = uiController.find(FORMS_LIST_COMPONENT_NAME);
		Object selectedComponent = uiController.getSelectedItem(formsList);
		Form selectedForm = getForm(uiController.getAttachedObject(selectedComponent));
		if(selectedForm != null) {
			this.formsDao.deleteForm(selectedForm);
			uiController.remove(selectedComponent);
		}
		// Now remove the confirmation dialog.
		uiController.removeConfirmationDialog();
	}
	
	/**
	 * Duplicates the selected form.
	 * @param formsList
	 */
	public void duplicateSelected(Object formsList) {
		Object selectedItem = uiController.getAttachedObject(uiController.getSelectedItem(formsList));
		Form selected = getForm(selectedItem);
		Form clone = new Form(selected.getName() + '*');
		for (FormField oldField : selected.getFields()) {
			FormField newField = new FormField(clone, oldField.getType(), oldField.getLabel());
			clone.addField(newField, oldField.getPosition());
		}
		addToFormsList(clone);
	}
	
	/**
	 * Form selection has changed, so decide which toolbar and popup options should be available considering the current selection.
	 * @param popUp The popup menu of the forms list
	 * @param list The list of forms whose selection has changed
	 * @param toolbar The button bar at the bottom of the forms list
	 */
	public void formsTab_enabledFields(Object popUp, Object list, Object toolbar) {
		Object attachedObject = uiController.getAttachedObject(uiController.getSelectedItem(list));

		enableMenuOptions(toolbar, attachedObject);
		enableMenuOptions(popUp, attachedObject);
	}
	
	/**
	 * Enable menu options for the supplied menu component.
	 * @param menuComponent Menu component, a button bar or popup menu
	 * @param selectedAttachment The object attached to the selected object of the control that this menu applied to
	 */
	private void enableMenuOptions(Object menuComponent, Object selectedAttachment) {
		Form selectedForm = getForm(selectedAttachment);
		for (Object o : uiController.getItems(menuComponent)) {
			String name = uiController.getName(o);
			if(name != null) { 
				if (name.contains("Delete")) {
					// Tricky to remove the component for a form when the field is selected.  If someone wants to
					// solve that, they're welcome to enable delete here for FormFields
					uiController.setEnabled(o, selectedAttachment instanceof Form);
				} else if (name.contains("Edit")) {
					uiController.setEnabled(o, selectedForm != null && !selectedForm.isFinalised());
				} else if (name.contains("New")) {
					uiController.setEnabled(o, true);
				} else {
					uiController.setEnabled(o, selectedForm != null);
			}
		}
	}
	}
	
	/**
	 * Update the results for the selected form, taking into account the
	 * page number as well.
	 * FIXME confirm this is called from XML as otherwise we can make it private
	 */
	private void formsTab_updateResults() {
		Object selectionAttachment = getSelectionAttachment();
		Form selected = getForm(selectionAttachment);
		int limit = uiController.getListLimit(formResultsComponent);
		int pageNumber = uiController.getListCurrentPage(formResultsComponent);
		uiController.removeAll(formResultsComponent);
		
		if (selected != null) {
			for (FormResponse response : formResponseDao.getFormResponses(selected, (pageNumber - 1) * limit, limit)) {
				Object row = getRow(response);
				uiController.add(formResultsComponent, row);
			}
		}
		
		uiController.updatePageNumber(formResultsComponent, uiController.find(TAB_FORMS));
	}
	
	public void showFormConfirmationDialog(String methodToBeCalled){
		uiController.showConfirmationDialog(methodToBeCalled, this);
	}

//> THINLET EVENT HELPER METHODS

	/** @return the forms list component */
	private Object getFormsList() {
		return uiController.find(FormsThinletTabController.FORMS_LIST_COMPONENT_NAME);
	}
	
	/** Reload and refresh the list of forms */
	private synchronized void refreshFormsList() {
		Object formList = getFormsList();
		uiController.removeAll(formList);
		for(Form f : formsDao.getAllForms()) {
			uiController.add(formList, getNode(f));
		}
	}
	
	/** @return the form or form field attached to the selection of the forms list */
	private Object getSelectionAttachment() {
		return uiController.getAttachedObject(uiController.getSelectedItem(uiController.find(FORMS_LIST_COMPONENT_NAME)));
	}
	
	/** Given a {@link VisualForm}, the form edit window, this saves its details. */
	private void saveFormInformation(VisualForm visualForm) {
		Form form = new Form(visualForm.getName());
		for (PreviewComponent comp : visualForm.getComponents()) {
			FormFieldType fieldType = FComponent.getFieldType(comp.getComponent().getClass());
			FormField newField = new FormField(form, fieldType, comp.getComponent().getLabel());
			form.addField(newField);
		}
		formsDao.saveForm(form);
		addToFormsList(form);
	}

	private void addToFormsList(Form form) {
		Object list = uiController.find(FORMS_LIST_COMPONENT_NAME);
		int index = -1;
		int indexToSelect = uiController.getSelectedIndex(list);
		for (Object o : uiController.getItems(list)) {
			if (uiController.getAttachedObject(o).equals(form)) {
				index = uiController.getIndex(list, o);
				break;
			}
		}
		if (index != -1) {
			uiController.remove(uiController.getItem(list, index));
		}
		uiController.add(list, getNode(form), index);
		uiController.setSelectedIndex(list, indexToSelect);
	}
	
	private void updateForm(List<PreviewComponent> old, List<PreviewComponent> newComp, Form f) {
		//Let's remove from database the ones the user removed
		List<PreviewComponent> toRemove = new ArrayList<PreviewComponent>();
		for (PreviewComponent c : old) {
			if (!newComp.contains(c)) {
				f.removeField(c.getFormField());
				toRemove.add(c);
			}
		}
		// Compare the lists
		for (PreviewComponent c : newComp) {
			if (c.getFormField() != null) {
				FormField ff = c.getFormField();
				if (ff.getPosition() != newComp.indexOf(c)) {
					ff.setPosition(newComp.indexOf(c));
				}
				ff.setLabel(c.getComponent().getLabel());
			} else {
				FormFieldType fieldType = FComponent.getFieldType(c.getComponent().getClass());
				FormField newField = new FormField(f, fieldType, c.getComponent().getLabel());
				f.addField(newField, newComp.indexOf(c));
			}
		}
	}
	
	/** Adds the result panel to the forms tab. */
	private void addFormResultsPanel() {
		Object formsTab = uiController.getCurrentTab();
		Object pnRight = uiController.find(formsTab, "pnRight");
		uiController.removeAll(pnRight);
		Object resultsView = uiController.loadComponentFromFile(UI_FILE_RESULTS_VIEW, this);
		Object pagePanel = uiController.loadComponentFromFile(FrontlineUI.UI_FILE_PAGE_PANEL, this);
		Object placeholder = uiController.find(resultsView, "pageControlsPanel");
		int index = uiController.getIndex(uiController.getParent(placeholder), placeholder);
		uiController.add(uiController.getParent(placeholder), pagePanel, index);
		uiController.remove(placeholder);
		uiController.add(pnRight, resultsView);
		uiController.setPageMethods(formsTab, "formResultsList", pagePanel);
		formResultsComponent = uiController.find(resultsView, "formResultsList");
		uiController.setListLimit(formResultsComponent);
		uiController.setListPageNumber(1, formResultsComponent);
		uiController.setMethod(formResultsComponent, "formsTab_updateResults");
	}

	/**
	 * @param selected
	 */
	private void showResultsPanel(Form selected) {
		addFormResultsPanel();
		Object pagePanel = uiController.find(uiController.getCurrentTab(), "pagePanel");
		uiController.setBoolean(pagePanel, Thinlet.VISIBLE, true);
		Object pnResults = uiController.find("pnFormResults");
		uiController.setInteger(pnResults, "columns", 2);
		
		int count = selected == null ? 0 : formResponseDao.getFormResponseCount(selected);
		form_createColumns(selected);
		uiController.setListPageNumber(1, formResultsComponent);
		uiController.setListElementCount(count, formResultsComponent);
		formsTab_updateResults();
		
		uiController.setEnabled(formResultsComponent, selected != null && uiController.getItems(formResultsComponent).length > 0);
		uiController.setEnabled(uiController.find("btExportFormResults"), selected != null && uiController.getItems(formResultsComponent).length > 0);
	}

	/**
	 * @param selectedAttachment Screen component's selectedItem's attached item
	 * @return a {@link Form} if a form or formfield was selected; <code>null</code> otherwise.
	 */
	private Form getForm(Object selectedAttachment) {
		Form selected = null; 
		if (selectedAttachment != null) {
			if (selectedAttachment instanceof Form) {
				selected = (Form) selectedAttachment;
			} else if(selectedAttachment instanceof FormField) {
				selected = ((FormField) selectedAttachment).getForm();
			} else {
				throw new IllegalStateException("Unrecognized attachment type: " + selectedAttachment.getClass());
			}
		}
		return selected;
	}
	
	private Object getRow(FormResponse response) {
		Object row = uiController.createTableRow(response);
		Contact sender = contactFactory.getFromMsisdn(response.getSubmitter());
		String senderDisplayName = sender != null ? sender.getDisplayName() : response.getSubmitter();
		uiController.add(row, uiController.createTableCell(senderDisplayName));
		for (ResponseValue result : response.getResults()) {
			uiController.add(row, uiController.createTableCell(result.toString()));
		}
		return row;
	}
	
	/**
	 * Creates a node for the supplied group, creating nodes for its sub-groups and contacts as well.
	 * 
	 * @param group The group to be put into a node.
	 * @return
	 */
	private Object getNode(Form form) {
		LOG.trace("ENTER");
		// Create the node for this form
		
		LOG.debug("Form [" + form.getName() + "]");
		
		Image icon = getIcon(form.isFinalised() ? FormIcon.FORM_FINALISED: FormIcon.FORM);
		Object node = uiController.createNode(form.getName(), form);
		uiController.setIcon(node, Thinlet.ICON, icon);

		// Create a node showing the group for this form
		Group g = form.getPermittedGroup();
		// FIXME i18n
		String groupName = g == null ? "(not set)" : g.getName();
		// FIXME i18n
		Object groupNode = uiController.createNode("Group: " + groupName, null);
		uiController.setIcon(groupNode, Icon.GROUP);
		uiController.add(node, groupNode);
		
		for (FormField field : form.getFields()) {
			Object child = uiController.createNode(field.getLabel(), field);
			uiController.setIcon(child, Thinlet.ICON, getIcon(field.getType()));
			uiController.add(node, child);
		}
		LOG.trace("EXIT");
		return node;
	}
	
	private Object getRow(String aggregatedValue, int hits, FormField selected) {
		Object row = uiController.createTableRow(null);
		String value = aggregatedValue;
		if (selected.getType().equals(CheckBox.class)) {
			boolean sel = Boolean.valueOf(value);
			Object cell = uiController.createTableCell("");
			uiController.setIcon(cell, sel ? Icon.TICK : Icon.CANCEL);
			uiController.add(row, cell);
		} else {
			uiController.add(row, uiController.createTableCell(value));
		}
		uiController.add(row, uiController.createTableCell(hits));
		return row;
	}

	private void form_createColumns(Form selected) {
		Object resultsTable = uiController.find("formResultsList");
		Object header = uiController.get(resultsTable, Thinlet.HEADER);
		uiController.removeAll(header);
		if (selected != null) {
			// FIXME check if this constant can be removed from frontlinesmsconstants class
			Object column = uiController.createColumn(InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_SUBMITTER), null);
			uiController.setInteger(column, "width", 100);
			uiController.setIcon(column, Icon.PHONE_CONNECTED);
			uiController.add(header, column);
			// For some reason we have a number column
			int count = 0;
			for (FormField field : selected.getFields()) {
				if(field.getType().hasValue()) {
					column = uiController.createColumn(field.getLabel(), new Integer(++count));
					uiController.setInteger(column, "width", 100);
					uiController.setIcon(column, getIcon(field.getType()));
					uiController.add(header, column);
				}
			}
		}
	}
	
//> TEMPORARY METHODS THAT NEED SORTING OUT
	/**
	 * Gets an icon with the specified name.
	 * @param iconPath
	 * @return currently this returns <code>null</code> - needs to be implemented!
	 */
	private Image getIcon(String iconPath) {
		return this.uiController.getIcon(iconPath);
	}
	
	private void sendForm(Form formToSend, String msisdn, int formsSmsPort) {
		// TODO Auto-generated method stub
	}

	private void sendSMS(String targetNumber, String messageContent) {
		this.pluginController.getFrontlineController().sendTextMessage(targetNumber, messageContent);
	}
	
	/**
	 * Gets the icon for a particular {@link FComponent}.
	 * @param fieldType
	 * @return icon to use for a particular {@link FComponent}.
	 */
	public Image getIcon(FormFieldType fieldType) {
		if(fieldType == FormFieldType.CHECK_BOX)			return getIcon(FormIcon.CHECKBOX);
		if(fieldType == FormFieldType.CURRENCY_FIELD)		return getIcon(FormIcon.CURRENCY_FIELD);
		if(fieldType == FormFieldType.DATE_FIELD)			return getIcon(FormIcon.DATE_FIELD);
		if(fieldType == FormFieldType.EMAIL_FIELD)			return getIcon(FormIcon.EMAIL_FIELD);
		if(fieldType == FormFieldType.NUMERIC_TEXT_FIELD)	return getIcon(FormIcon.NUMERIC_TEXT_FIELD);
		if(fieldType == FormFieldType.PASSWORD_FIELD) 		return getIcon(FormIcon.PASSWORD_FIELD);
		if(fieldType == FormFieldType.PHONE_NUMBER_FIELD) 	return getIcon(FormIcon.PHONE_NUMBER_FIELD);
		if(fieldType == FormFieldType.TEXT_AREA)			return getIcon(FormIcon.TEXT_AREA);
		if(fieldType == FormFieldType.TEXT_FIELD) 			return getIcon(FormIcon.TEXT_FIELD);
		if(fieldType == FormFieldType.TIME_FIELD) 			return getIcon(FormIcon.TIME_FIELD);
		if(fieldType == FormFieldType.TRUNCATED_TEXT) 		return getIcon(FormIcon.TRUNCATED_TEXT);
		if(fieldType == FormFieldType.WRAPPED_TEXT) 		return getIcon(FormIcon.WRAPPED_TEXT);
		throw new IllegalStateException("No icon is mapped for field type: " + fieldType);
	}
}
