/**
 * 
 */
package net.frontlinesms.plugins.forms;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.Utils;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.Message;
import net.frontlinesms.listener.IncomingMessageListener;
import net.frontlinesms.plugins.PluginController;
import net.frontlinesms.plugins.PluginInitialisationException;
import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.domain.FormResponse;
import net.frontlinesms.plugins.forms.data.domain.ResponseValue;
import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;
import net.frontlinesms.plugins.forms.data.repository.FormDao;
import net.frontlinesms.plugins.forms.request.DataSubmissionRequest;
import net.frontlinesms.plugins.forms.request.FormsRequestDescription;
import net.frontlinesms.plugins.forms.request.NewFormRequest;
import net.frontlinesms.plugins.forms.request.SubmittedFormData;
import net.frontlinesms.plugins.forms.response.FormsResponseDescription;
import net.frontlinesms.plugins.forms.response.NewFormsResponse;
import net.frontlinesms.plugins.forms.response.SubmittedDataResponse;
import net.frontlinesms.plugins.forms.ui.FormsThinletTabController;
import net.frontlinesms.resources.PropertySet;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.UiGeneratorController;

/**
 * Controller for the FrontlineForms plugin.
 * @author Alex
 */
public class FormsPluginController implements PluginController, IncomingMessageListener {
//> CONSTANTS
	/** Filename and path of the XML for the FrontlineForms tab. */
	private static final String XML_FORMS_TAB = "/ui/plugins/forms/formsTab.xml";
	
//> INSTANCE PROPERTIES
	/** Logging object */
	private Logger log = Utils.getLogger(this.getClass());
	/** the {@link FrontlineSMS} instance that this plugin is attached to */
	private FrontlineSMS frontlineController;
	/** the {@link FormsMessageHandler} for processing incoming and outgoing messages */
	private FormsMessageHandler formsMessageHandler;
	/** DAO for forms */
	private FormDao formsDao;
	/** DAO for form responses */
	private FormResponseDao formResponseDao;
	
//> CONFIG METHODS
	/** @see PluginController#getSpringConfigPath() */
	public String getSpringConfigPath() {
		return "classpath:net/frontlinesms/plugins/forms/frontlineforms-spring-hibernate.xml";
	}
	
	/** @see PluginController#getHibernateConfigPath() */
	public String getHibernateConfigPath() {
		return "classpath:net/frontlinesms/plugins/forms/frontlineforms.hibernate.cfg.xml";
	}
	
	/** @see PluginController#getName() */
	public String getName() {
		return "Forms";
	}
	
	/** @see PluginController#init(FrontlineSMS, ApplicationContext) */
	public void init(FrontlineSMS frontlineController, ApplicationContext applicationContext) throws PluginInitialisationException {
		this.frontlineController = frontlineController;
		this.frontlineController.addIncomingMessageListener(this);
		
		try {
			this.formsDao = (FormDao) applicationContext.getBean("formDao");
			this.formResponseDao = (FormResponseDao) applicationContext.getBean("formResponseDao");
			
			String handlerClassName = FormsProperties.getInstance().getHandlerClassName();
			this.formsMessageHandler = (FormsMessageHandler) Class.forName(handlerClassName).newInstance();
		} catch(Throwable t) {
			log.warn("Unable to load form handler class.", t);
			throw new PluginInitialisationException(t);
		}
	}

	/** @see PluginController#getTab(UiGeneratorController)  */
	public Object getTab(UiGeneratorController uiController) {
		FormsThinletTabController tabController = new FormsThinletTabController(this, uiController);
		tabController.setContactDao(this.frontlineController.getContactDao());
		tabController.setFormsDao(formsDao);
		tabController.setFormResponseDao(formResponseDao);

		Object formsTab = uiController.loadComponentFromFile(XML_FORMS_TAB, tabController);
		tabController.setTabComponent(formsTab);
		
		tabController.refresh();
		
		return formsTab;
	}

	/** @return {@link #frontlineController} */
	public FrontlineSMS getFrontlineController() {
		return this.frontlineController;
	}

	/** Process a new message coming into the system. */
	public void incomingMessageEvent(Message message) {
		try {
			FormsRequestDescription request = this.formsMessageHandler.handleIncomingMessage(message);
			
			FormsResponseDescription response;
			if(request instanceof DataSubmissionRequest) {
				response = handleDataSubmissionRequest((DataSubmissionRequest)request, message);
			} else if(request instanceof NewFormRequest) {
				response = handleNewFormRequest((NewFormRequest)request, message);
			} else {
				throw new IllegalStateException("Unknown form request description type: " + request);
			}
			
			// If there is a response to send back to the form submitter, then process it
			if(response != null) {
				Collection<Message> responseMessages = this.formsMessageHandler.handleOutgoingMessage(response);
				log.info("Sending forms response.  Response messages: " + responseMessages.size());
				for(Message responseMessage : responseMessages) {
					// Make sure that the response is sent to the correct recipient!
					responseMessage.setRecipientMsisdn(message.getSenderMsisdn());
					if(request.getSmsPort() != null) {
						responseMessage.setRecipientSmsPort(request.getSmsPort());
					}
					this.frontlineController.sendMessage(responseMessage);
				}
				log.trace("Response messages sent.");
			}
		} catch (Throwable t) {
			log.info("There was a problem handling incoming message as forms message.", t);
		}
	}

//> PRIVATE HELPER METHODS
	/**
	 * Handles a request of type: {@link DataSubmissionRequest}
	 * @param request 
	 * @param message 
	 * @return a response of type {@link SubmittedDataResponse}
	 */
	private SubmittedDataResponse handleDataSubmissionRequest(DataSubmissionRequest request, Message message) {
		/** List of data IDs of the successfully processed responses */
		Collection<SubmittedFormData> dataIds = new HashSet<SubmittedFormData>();
		for(SubmittedFormData submittedData : request.getSubmittedData()) {
			Form form = this.formsDao.getFromMobileId(submittedData.getFormMobileId());
			if(form == null) {
				log.warn("No form found for submitted data with dataId: " + submittedData.getDataId());
				continue;
			}
			
			List<ResponseValue> responseValues = submittedData.getDataValues();
			if(form.getEditableFieldCount() != responseValues.size()) {
				log.info("Editable field count mismatch: submitted " + responseValues.size() + "/" + form.getEditableFieldCount());
				continue;
			}
			
			this.formResponseDao.saveResponse(new FormResponse(message, form, responseValues));
			dataIds.add(submittedData);
		}
		
		return new SubmittedDataResponse(dataIds);		
	}
	
	/**
	 * Handles a request of type {@link NewFormRequest}
	 * @param request
	 * @param message
	 * @return a response of type {@link NewFormsResponse}
	 */
	private NewFormsResponse handleNewFormRequest(NewFormRequest request, Message message) {
		String senderMsisdn = message.getSenderMsisdn();
		Contact contact = this.frontlineController.getContactDao().getFromMsisdn(senderMsisdn);
		if(contact == null) {
			// i18n the contact name
			contact = new Contact("Unknown Forms Submitter", senderMsisdn, null, null, null, true);
			try {
				this.frontlineController.getContactDao().saveContact(contact);
			} catch(DuplicateKeyException ex) {
				// Seems like the contact was created by someone else while we were saving him.  Try to
				// fetch him again
				contact = this.frontlineController.getContactDao().getFromMsisdn(senderMsisdn);
			}
		}
		Collection<Form> newForms = this.formsDao.getFormsForUser(contact, ((NewFormRequest)request).getCurrentFormMobileIds());
		return new NewFormsResponse(contact, newForms);
	}

	/**
	 * Send a form to a collection of contacts.
	 * @param form the form to send
	 * @param contacts the contacts to send the form to
	 */
	public void sendForm(Form form, Collection<Contact> contacts) {
		// TODO if it is possible, we could send forms directly to people here
		// Send a text SMS to each contact informing them that a new form is available.
		
		// FIXME i18n this
		String messageContent = "There is a new form available: " + form.getName();
		for(Contact c : contacts) {
			this.frontlineController.sendTextMessage(c.getMsisdn(), messageContent);
		}
	}

	/**
	 * Set {@link #formResponseDao}
	 * @param formResponseDao new value for {@link #formResponseDao}
	 */
	@Required
	public void setFormResponseDao(FormResponseDao formResponseDao) {
		this.formResponseDao = formResponseDao;
	}
	
	/**
	 * Set {@link #formsDao}
	 * @param formsDao new value for {@link #formsDao}
	 */
	@Required
	public void setFormsDao(FormDao formsDao) {
		this.formsDao = formsDao;
	}

	public void initializePluginData() {
		// TODO Auto-generated method stub
		
	}
}
