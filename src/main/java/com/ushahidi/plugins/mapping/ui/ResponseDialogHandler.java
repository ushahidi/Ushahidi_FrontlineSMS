package com.ushahidi.plugins.mapping.ui;

import java.util.Date;

import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.domain.FormField;
import net.frontlinesms.plugins.forms.data.domain.FormResponse;
import net.frontlinesms.plugins.forms.data.domain.ResponseValue;
import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;
import net.frontlinesms.plugins.textforms.data.domain.TextFormResponse;
import net.frontlinesms.plugins.textforms.data.domain.answers.Answer;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

@SuppressWarnings({ "unused", "serial" })
public class ResponseDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler {
	
	private static MappingLogger LOG = MappingLogger.getLogger(ResponseDialogHandler.class);
	
	private static final String UI_DIALOG_XML = "/ui/plugins/mapping/responseDialog.xml";

	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final ContactDao contactDao;
	private FormResponseDao formResponseDao;
	
	private final Object mainDialog;
	private final UIFields fields;
	private class UIFields extends Fields {
		public UIFields(UiGeneratorController uiController, Object parent) {
			super(uiController, parent);
		}
		public Object txtContactName;
		public Object txtContactPhone;
		public Object txtLocation;
		public Object txtCoordinates;
		public Object tblResponses;
		public Object txtDate;
		public Object txtMessage;
		public Object lblMessage;
		public Object lblDate;
	}
	
	public ResponseDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.contactDao = pluginController.getContactDao();
			
		this.mainDialog = ui.loadComponentFromFile(UI_DIALOG_XML, this);
		this.fields = new UIFields(ui, mainDialog);
	}
	
	public void setFormResponseDao(FormResponseDao formResponseDao) {
		this.formResponseDao = formResponseDao;
	}
	
	public void showDialog(TextFormResponse textformResponse, Location location) {
		LOG.debug("showDialog: %s", textformResponse);
		ui.setAttachedObject(mainDialog, textformResponse);
		ui.setText(fields.txtContactName, textformResponse.getContactName());
		ui.setText(fields.txtContactPhone, textformResponse.getContactPhoneNumber());
		ui.setText(fields.txtLocation, location.getName());
		ui.setText(fields.txtCoordinates, location.getCoordinates());
		ui.setText(fields.txtDate, textformResponse.getStartedString());
		ui.removeAll(fields.tblResponses);
		for(Answer<?> answer : textformResponse.getAnswers()) {
			ui.add(fields.tblResponses, getRow(answer.getQuestionName(), answer.getAnswerValue()));
		}
		ui.setVisible(fields.txtMessage, false);
		ui.setVisible(fields.lblMessage, false);
		ui.setVisible(fields.tblResponses, true);
		ui.setVisible(fields.lblDate, true);
		ui.setVisible(fields.txtDate, true);
		ui.setIcon(mainDialog, "/icons/textform.png");
		ui.setText(mainDialog, MappingMessages.getTextFormResponse());
		ui.add(mainDialog);
	}
	
	public void showDialog(FormResponse formResponse, Location location, Form form) {
		LOG.debug("showDialog: %s", formResponse);
		ui.setAttachedObject(mainDialog, formResponse);
		Contact contact = contactDao.getFromMsisdn(formResponse.getSubmitter());
		if (contact != null) {
			ui.setText(fields.txtContactName, contact.getName());
		}
		else {
			ui.setText(fields.txtContactName, "");
		}
		ui.setText(fields.txtContactPhone, formResponse.getSubmitter());
		ui.setText(fields.txtLocation, location.getName());
		ui.setText(fields.txtCoordinates, location.getCoordinates());
		ui.setText(fields.txtDate, "");
		ui.removeAll(fields.tblResponses);
		try {
			int index = 0;
			for (FormField formField : formResponse.getParentForm().getFields()) {
				if (formField.getType().hasValue()) {
					ResponseValue value = formResponse.getResults().get(index);
					ui.add(fields.tblResponses, getRow(formField.getLabel(), value.toString()));
				}
				index++;
			}	
		}
		catch (LazyInitializationException ex) {
			//TODO fix this LazyInitializationException
			ex.printStackTrace();
			int index = 1;
			for(ResponseValue value : formResponse.getResults()) {
				ui.add(fields.tblResponses, getRow(String.format("Field %d", index), value.toString()));
				index++;
			}
		}
		ui.setVisible(fields.txtMessage, false);
		ui.setVisible(fields.lblMessage, false);
		ui.setVisible(fields.tblResponses, true);
		ui.setVisible(fields.lblDate, false);
		ui.setVisible(fields.txtDate, false);
		ui.setIcon(mainDialog, "/icons/form.png");
		ui.setText(mainDialog, MappingMessages.getFormResponse());
		ui.add(mainDialog);
	}
	
	public void showDialog(FrontlineMessage message, Location location) {
		LOG.debug("showDialog: %s", message);
		ui.setAttachedObject(mainDialog, message);
		Contact contact = contactDao.getFromMsisdn(message.getSenderMsisdn());
		if (contact != null) {
			ui.setText(fields.txtContactName, contact.getName());
		}
		else {
			ui.setText(fields.txtContactName, "");
		}
		ui.setText(fields.txtContactPhone, message.getSenderMsisdn());
		ui.setText(fields.txtLocation, location.getName());
		ui.setText(fields.txtCoordinates, location.getCoordinates());
		ui.setText(fields.txtDate, InternationalisationUtils.getDatetimeFormat().format(new Date(message.getDate())));
		ui.setText(fields.txtMessage, message.getTextContent());
		ui.setVisible(fields.txtMessage, true);
		ui.setVisible(fields.lblMessage, true);
		ui.setVisible(fields.tblResponses, false);
		ui.setVisible(fields.lblDate, true);
		ui.setVisible(fields.txtDate, true);
		ui.setIcon(mainDialog, "/icons/sms.png");
		ui.setText(mainDialog, MappingMessages.getMessageReceived());
		ui.add(mainDialog);
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
	
	private Object getRow(String label, String value){
		Object row = createTableRow();
		createTableCell(row, label);
		createTableCell(row, value);
		return row;
	}
	
}