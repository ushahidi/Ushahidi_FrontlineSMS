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
import net.frontlinesms.plugins.surveys.data.domain.SurveyResponse;
import net.frontlinesms.plugins.surveys.data.domain.answers.Answer;
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
	private final Object txtContactName;
	private final Object txtContactPhone;
	private final Object txtLocation;
	private final Object txtCoordinates;
	private final Object tblResponses;
	private final Object txtDate;
	private final Object txtMessage;
	private final Object lblMessage;
	
	public ResponseDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.contactDao = pluginController.getContactDao();
			
		this.mainDialog = ui.loadComponentFromFile(UI_DIALOG_XML, this);
		this.txtContactName = this.ui.find(this.mainDialog, "txtContactName");
		this.txtContactPhone = this.ui.find(this.mainDialog, "txtContactPhone");
		this.txtLocation = this.ui.find(this.mainDialog, "txtLocation");
		this.txtCoordinates = this.ui.find(this.mainDialog, "txtCoordinates");
		this.tblResponses = this.ui.find(this.mainDialog, "tblResponses");
		this.txtDate = this.ui.find(this.mainDialog, "txtDate");
		this.txtMessage = this.ui.find(this.mainDialog, "txtMessage");
		this.lblMessage = this.ui.find(this.mainDialog, "lblMessage");
	}
	
	public void setFormResponseDao(FormResponseDao formResponseDao) {
		this.formResponseDao = formResponseDao;
	}
	
	public void showDialog(SurveyResponse surveyResponse, Location location) {
		LOG.debug("showDialog: %s", surveyResponse);
		ui.setAttachedObject(mainDialog, surveyResponse);
		ui.setText(txtContactName, surveyResponse.getContactName());
		ui.setText(txtContactPhone, surveyResponse.getContactPhoneNumber());
		ui.setText(txtLocation, location.getName());
		ui.setText(txtCoordinates, location.getCoordinates());
		ui.setText(txtDate, surveyResponse.getStartedString());
		ui.removeAll(tblResponses);
		for(Answer<?> answer : surveyResponse.getAnswers()) {
			ui.add(tblResponses, getRow(answer.getQuestionName(), answer.getAnswerValue()));
		}
		ui.setVisible(txtMessage, false);
		ui.setVisible(lblMessage, false);
		ui.setVisible(tblResponses, true);
		ui.setIcon(mainDialog, "/icons/survey.png");
		ui.setText(mainDialog, MappingMessages.getSurveyResponse());
		ui.add(mainDialog);
	}
	
	public void showDialog(FormResponse formResponse, Location location, Form form) {
		LOG.debug("showDialog: %s", formResponse);
		ui.setAttachedObject(mainDialog, formResponse);
		Contact contact = contactDao.getFromMsisdn(formResponse.getSubmitter());
		if (contact != null) {
			ui.setText(txtContactName, contact.getName());
		}
		else {
			ui.setText(txtContactName, "");
		}
		ui.setText(txtContactPhone, formResponse.getSubmitter());
		ui.setText(txtLocation, location.getName());
		ui.setText(txtCoordinates, location.getCoordinates());
		ui.setText(txtDate, "");
		ui.removeAll(tblResponses);
		try {
			int index = 0;
			for (FormField formField : formResponse.getParentForm().getFields()) {
				if (formField.getType().hasValue()) {
					ResponseValue value = formResponse.getResults().get(index);
					ui.add(tblResponses, getRow(formField.getLabel(), value.toString()));
				}
				index++;
			}	
		}
		catch (LazyInitializationException ex) {
			//TODO fix this LazyInitializationException
			int index = 1;
			for(ResponseValue value : formResponse.getResults()) {
				ui.add(tblResponses, getRow(String.format("Field %d", index), value.toString()));
				index++;
			}
		}
		ui.setVisible(txtMessage, false);
		ui.setVisible(lblMessage, false);
		ui.setVisible(tblResponses, true);
		ui.setIcon(mainDialog, "/icons/form.png");
		ui.setText(mainDialog, MappingMessages.getFormResponse());
		ui.add(mainDialog);
	}
	
	public void showDialog(FrontlineMessage message, Location location) {
		LOG.debug("showDialog: %s", message);
		ui.setAttachedObject(mainDialog, message);
		Contact contact = contactDao.getFromMsisdn(message.getSenderMsisdn());
		if (contact != null) {
			ui.setText(txtContactName, contact.getName());
		}
		else {
			ui.setText(txtContactName, "");
		}
		ui.setText(txtContactPhone, message.getSenderMsisdn());
		ui.setText(txtLocation, location.getName());
		ui.setText(txtCoordinates, location.getCoordinates());
		ui.setText(txtDate, InternationalisationUtils.getDatetimeFormat().format(new Date(message.getDate())));
		ui.setText(txtMessage, message.getTextContent());
		ui.setVisible(txtMessage, true);
		ui.setVisible(lblMessage, true);
		ui.setVisible(tblResponses, false);
		ui.setIcon(mainDialog, "/icons/form.png");
		ui.setText(mainDialog, MappingMessages.getFormResponse());
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