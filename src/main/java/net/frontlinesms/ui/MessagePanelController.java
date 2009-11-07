/**
 * 
 */
package net.frontlinesms.ui;

// TODO Remove static imports
import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_NO_CONTACT_SELECTED;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_ESTIMATED_MONEY;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_FIRST;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_MSG_NUMBER;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_REMAINING_CHARS;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_SECOND;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_THIRD;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_MESSAGE;
import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_RECIPIENT;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.Message;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.apache.log4j.Logger;
import org.smslib.util.GsmAlphabet;

/**
 * Controller for a panel which allows sending of text SMS messages
 * @author Alex
 */
public class MessagePanelController implements ThinletUiEventHandler {
//> STATIC CONSTANTS
	/** UI XML File Path: the panel containing the messaging controls */
	protected static final String UI_FILE_MESSAGE_PANEL = "/ui/dialog/messagePanel.xml";
	
//> THINLET COMPONENTS
	/** Thinlet component name: Button to send message */
	private static final String COMPONENT_BT_SEND = "btSend";

//> INSTANCE PROPERTIES
	/** Logging obhect */
	private final Logger LOG = Utils.getLogger(this.getClass());
	/** The {@link UiGeneratorController} that shows the tab. */
	private final UiGeneratorController uiController;
	/** The parent component */
	private final Object messagePanel;

//> CONSTRUCTORS
	/**
	 * @param uiController
	 */
	public MessagePanelController(UiGeneratorController uiController) {
		this.uiController = uiController;
		this.messagePanel = uiController.loadComponentFromFile(UI_FILE_MESSAGE_PANEL, this);
	}

	/** @return {@link #messagePanel} */
	public Object getPanel() {
		return this.messagePanel;
	}

//> ACCESSORS
	/** Sets the method called by the send button at the bottom of the compose message panel */
	public void setSendButtonMethod(ThinletUiEventHandler eventHandler, Object rootComponent, String methodCall) {
		Object sendButton = uiController.find(this.messagePanel, COMPONENT_BT_SEND);
		uiController.setAction(sendButton, methodCall, rootComponent, eventHandler);
	}
	
//> THINLET UI METHODS
	/**
	 * Extract message details from the controls in the panel, and send an SMS.
	 */
	public void send() {
		String recipient = uiController.getText(uiController.find("tfRecipient"));
		String message = uiController.getText(uiController.find("tfMessage"));
		
		if (recipient.equals("")) {
			uiController.alert(InternationalisationUtils.getI18NString(FrontlineSMSConstants.MESSAGE_BLANK_PHONE_NUMBER));
			return;
		} 
		this.uiController.frontlineController.sendTextMessage(recipient, message);
		clearMessageComponent();
	}
	
	/**
	 * Event triggered when the message details have changed
	 * @param panel TODO this should be removed
	 * @param text the new text value for the message body
	 * 
	 */
	public void messageChanged(String text) {
		int textLength = text.length();
		if (textLength == 0) {
			clearMessageComponent();
			return;
		}
		Object sendButton = uiController.find(this.messagePanel, COMPONENT_BT_SEND);
		if (sendButton != null) uiController.setEnabled(sendButton, true);
		
		boolean areAllCharactersValidGSM = GsmAlphabet.areAllCharactersValidGSM(text);

		int total;
		if(areAllCharactersValidGSM) total = Message.SMS_MULTIPART_LENGTH_LIMIT * Message.SMS_LIMIT;
		else total = Message.SMS_MULTIPART_LENGTH_LIMIT_UCS2 * Message.SMS_LIMIT;
		
		if (textLength > total) {
			uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_REMAINING_CHARS), "0");
			Object tfMessage = uiController.find(this.messagePanel, COMPONENT_TF_MESSAGE);
			uiController.setText(tfMessage, text.substring(0, textLength - 1));
		} else {
			int singleMessageCharacterLimit;
			int multipartMessageCharacterLimit;
			if(areAllCharactersValidGSM) {
				singleMessageCharacterLimit = Message.SMS_LENGTH_LIMIT;
				multipartMessageCharacterLimit = Message.SMS_MULTIPART_LENGTH_LIMIT;
			} else {
				// It appears there are some unicode-only characters here.  We should therefore
				// treat this message as if it will be sent as unicode.
				singleMessageCharacterLimit = Message.SMS_LENGTH_LIMIT_UCS2;
				multipartMessageCharacterLimit = Message.SMS_MULTIPART_LENGTH_LIMIT_UCS2;
			}
			
			int numberOfMsgs;
			int remaining;
			if (textLength <= singleMessageCharacterLimit) {
				//First message
				remaining = (textLength % singleMessageCharacterLimit) == 0 ? 0
						: singleMessageCharacterLimit - (textLength % singleMessageCharacterLimit);
				numberOfMsgs = textLength == 0 ? 0 : 1;
			} else if (textLength <= (2*multipartMessageCharacterLimit)) {
				numberOfMsgs = 2;
				int charCount = textLength - multipartMessageCharacterLimit;
				remaining = (charCount % multipartMessageCharacterLimit) == 0 ? 0
						: multipartMessageCharacterLimit - (charCount % multipartMessageCharacterLimit);
			} else {
				numberOfMsgs = 3;
				int charCount = textLength - (2*multipartMessageCharacterLimit);
				remaining = (charCount % multipartMessageCharacterLimit) == 0 ? 0
						: multipartMessageCharacterLimit - (charCount % multipartMessageCharacterLimit);
			}

			uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_REMAINING_CHARS), String.valueOf(remaining));
			uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_MSG_NUMBER), String.valueOf(numberOfMsgs));
			uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_FIRST), Icon.SMS_DISABLED);
			uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_SECOND), Icon.SMS_DISABLED);
			uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_THIRD), Icon.SMS_DISABLED);
			if (numberOfMsgs >= 1) uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_FIRST), Icon.SMS);
			if (numberOfMsgs >= 2) uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_SECOND), Icon.SMS);
			if (numberOfMsgs >= 3) uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_THIRD), Icon.SMS);
			
			double value = numberOfMsgs * uiController.getCostPerSms() * uiController.numberToSend;
			uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_ESTIMATED_MONEY), InternationalisationUtils.formatCurrency(value));
		}
	}
	
	/**
	 * Sets the phone number of the selected contact.
	 * 
	 * @param contactSelecter_contactList
	 * @param dialog
	 */
	public void homeScreen_setRecipientTextfield(Object contactSelecter_contactList, Object dialog) {
		Object tfRecipient = uiController.find(this.messagePanel, COMPONENT_TF_RECIPIENT);
		Object selectedItem = uiController.getSelectedItem(contactSelecter_contactList);
		if (selectedItem == null) {
			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_CONTACT_SELECTED));
			return;
		}
		Contact selectedContact = uiController.getContact(selectedItem);
		uiController.setText(tfRecipient, selectedContact.getMsisdn());
		uiController.remove(dialog);
		uiController.numberToSend = 1;
		uiController.updateCost();
	}

//> INSTANCE HELPER METHODS
	/**
	 * Clear the details of a message component.
	 * At some point, this should be nicely refactored so that a message component has its own controller.
	 * @param panel
	 */
	private void clearMessageComponent() {
		uiController.setText(uiController.find(this.messagePanel, COMPONENT_TF_MESSAGE), "");
		uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_REMAINING_CHARS), String.valueOf(Message.SMS_LENGTH_LIMIT));
		uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_MSG_NUMBER), "0");
		uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_FIRST), Icon.SMS_DISABLED);
		uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_SECOND), Icon.SMS_DISABLED);
		uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_THIRD), Icon.SMS_DISABLED);
		uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_ESTIMATED_MONEY), InternationalisationUtils.formatCurrency(0));
		Object sendButton = uiController.find(this.messagePanel, COMPONENT_BT_SEND);
		if (sendButton != null) uiController.setEnabled(sendButton, false);
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
