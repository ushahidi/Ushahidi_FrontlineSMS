/**
 * 
 */
package net.frontlinesms.ui;

/**
 * Constants from {@link UiGeneratorController} which we can hopefully be rid of soon.
 * 
 * @author Alex
 */
public class UiGeneratorControllerConstants {
	
//> UI FILES
	protected static final String UI_FILE_HOME = "/ui/frontline.xml";
	protected static final String UI_FILE_DATE_PANEL = "/ui/dialog/datePanel.xml";
	protected static final String UI_FILE_PENDING_MESSAGES_FORM = "/ui/dialog/pendingMessagesDialog.xml";
	protected static final String UI_FILE_COMPOSE_MESSAGE_FORM = "/ui/dialog/composeMessageForm.xml";
	protected static final String UI_FILE_GROUP_SELECTER = "/ui/dialog/groupSelecter.xml";
	protected static final String UI_FILE_CONTACT_SELECTER = "/ui/dialog/contactSelecter.xml";
	protected static final String UI_FILE_NEW_KEYWORD_FORM = "/ui/dialog/newKeywordForm.xml";
	protected static final String UI_FILE_NEW_KACTION_REPLY_FORM = "/ui/dialog/newKActionReplyForm.xml";
	protected static final String UI_FILE_NEW_KACTION_FORWARD_FORM = "/ui/dialog/newKActionForwardForm.xml";
	protected static final String UI_FILE_NEW_KACTION_EXTERNAL_COMMAND_FORM = "/ui/dialog/externalCommandDialog.xml";
	protected static final String UI_FILE_NEW_KACTION_EMAIL_FORM = "/ui/dialog/emailDialog.xml";
	protected static final String UI_FILE_NEW_KACTION_SURVEY_FORM = "/ui/dialog/surveyDialog.xml";
	protected static final String UI_FILE_EDIT_KEYWORD_LIST_FORM = "/ui/dialog/editKeywordListDialog.xml";
	protected static final String UI_FILE_EMAIL_ACCOUNTS_SETTINGS_FORM = "/ui/dialog/emailServerConfigDialog.xml";
	protected static final String UI_FILE_SMS_SERVICES_ACCOUNTS_SETTINGS_FORM = "/ui/dialog/smsHttpServerConfigDialog.xml";
	protected static final String UI_FILE_SMS_SERVICES_ACCOUNTS_INTELLISMS_SETTINGS_FORM = "/ui/dialog/smsHttpServerIntelliSmsConfigDialog.xml";
	protected static final String UI_FILE_EXPORT_DIALOG_FORM = "/ui/dialog/exportDialogForm.xml";
	protected static final String UI_FILE_SMS_HTTP_SERVICE_SETTINGS_FORM = "/ui/dialog/smsHttpServiceSettings.xml";
	protected static final String UI_FILE_EMAIL_ACCOUNT_FORM = "/ui/dialog/emailAccountSettings.xml";
	protected static final String UI_FILE_CONNECTION_WARNING_FORM = "/ui/dialog/connectionWarningForm.xml";
	protected static final String UI_FILE_CONFIRMATION_DIALOG_FORM = "/ui/dialog/confirmationDialog.xml";
	protected static final String UI_FILE_HISTORY_FORM = "/ui/dialog/historyForm.xml";
	// FIXME this should probably be abstracted via a getter in UIGC or similar
	public static final String UI_FILE_PAGE_PANEL = "/ui/dialog/pagePanel.xml";
	protected static final String UI_FILE_ABOUT_PANEL = "/ui/dialog/about.xml";
	protected static final String UI_FILE_SENDER_NAME_PANEL = "/ui/dialog/senderNamePanel.xml";
	protected static final String UI_FILE_MSG_DETAILS_FORM = "/ui/dialog/messageDetailsDialog.xml";
	protected static final String UI_FILE_INCOMING_NUMBER_SETTINGS_FORM = "/ui/dialog/incomingNumberSettingsDialog.xml";
	protected static final String UI_FILE_USER_DETAILS_DIALOG = "/ui/dialog/userDetailsDialog.xml";
	
	//keyword panel
	protected static final String UI_FILE_KEYWORDS_TAB = "/ui/advanced/keywordsTab.xml";
	protected static final String UI_FILE_KEYWORDS_SIMPLE_VIEW = "/ui/advanced/keywordSimpleView.xml";
	protected static final String UI_FILE_KEYWORDS_ADVANCED_VIEW = "/ui/advanced/keywordAdvancedView.xml";
	
	protected static final String UI_FILE_MESSAGES_TAB = "/ui/advanced/messagesTab.xml";
	protected static final String UI_FILE_EMAILS_TAB = "/ui/advanced/emailsTab.xml";
	
	//Classic view
	protected static final String UI_FILE_SURVEY_MANAGER_TAB = "/ui/classic/surveyManagerTab.xml";
	protected static final String UI_FILE_SURVEY_ANALYST_TAB = "/ui/classic/surveyAnalystTab.xml";
	protected static final String UI_FILE_SEND_CONSOLE_TAB = "/ui/classic/sendConsoleTab.xml";
	protected static final String UI_FILE_MESSAGE_TRACKER_TAB = "/ui/classic/messageTrackerTab.xml";
	protected static final String UI_FILE_RECEIVE_CONSOLE_TAB = "/ui/classic/receiveConsoleTab.xml";
	protected static final String UI_FILE_REPLY_MANAGER_TAB = "/ui/classic/replyManagerTab.xml";
	
//> TAB NAMES
	/** The name of the Contact Manager tab */
	static final String TAB_CONTACT_MANAGER = ":contactManager";
	/** The name of the Group Manager tab */
	static final String TAB_GROUP_MANAGER = ":groupManager";
	/** The name of the Message Log tab */
	static final String TAB_MESSAGE_HISTORY = ":messageHistory";
	/** The name of the Email Log tab */
	static final String TAB_EMAIL_LOG = ":emailLog";
	/** The name of the Home tab */
	static final String TAB_HOME = ":home";
	/** The name of the Keyword Manager tab */
	static final String TAB_KEYWORD_MANAGER = ":keywordManager";
	/** The name of the Phone Manager tab in advanced view */
	static final String TAB_ADVANCED_PHONE_MANAGER = ":advancedPhoneManager";
	/** The name of the Survey Manager tab */
	static final String TAB_SURVEY_MANAGER = ":surveyManager";
	/** The name of the Survey Analyst tab */
	static final String TAB_SURVEY_ANALYST = ":surveyAnalyst";
	/** The name of the Send Console tab */
	static final String TAB_SEND_CONSOLE = ":sendConsole";
	/** The name of the Receive Console tab */
	static final String TAB_RECEIVE_CONSOLE = ":receiveConsole";
	/** The name of the Message Tracker tab */
	static final String TAB_MESSAGE_TRACKER = ":messageTracker";
	/** The name of the Reply Manager tab */
	static final String TAB_REPLY_MANAGER = ":replyManager";
	
//> COMPONENT NAMES
	/** 
	 * Component naming conventions:
	 *  <li> <code>tf</code> means TextField
	 *  <li> <code>pn</code> means Panel
	 *  <li> <code>cb</code> means CheckBox or ComboBox
	 *  <li> <code>rb</code> means RadioButton
	 *  <li> <code>bt</code> means Button
	 *  <li> <code>lb</code> means Label
	 *  <li> <code>mi</code> means MenuItem
	 */
	static final String COMPONENT_MI_EMAIL = "miEmail";
	static final String COMPONENT_MI_KEYWORD = "miKeyword";
	static final String COMPONENT_MI_HOME = "miHome";
	static final String COMPONENT_MI_NEW_CONTACT = "miNewContact";
	static final String COMPONENT_EVENTS_LIST = "eventsList";
	static final String COMPONENT_SURVEY_MANAGER_NEW_BUTTON = "surveyManager_newButton";
	static final String COMPONENT_BT_CONTINUE = "btContinue";
	static final String COMPONENT_CONFIRM_DIALOG = "confirmDialog";
	static final String COMPONENT_BT_SENDER_NAME = "btSenderName";
	static final String COMPONENT_LB_COST = "lbCost";
	static final String COMPONENT_LB_MSGS_NUMBER = "lbMsgsNumber";
	static final String COMPONENT_CB_CONTACTS = "cbContacts";
	static final String COMPONENT_PN_KEYWORD_ACTIONS_ADVANCED = "pnKeywordActionsAdvanced";
	static final String COMPONENT_CONTACT_SELECTER = "contactSelecter";
	static final String COMPONENT_CONTACT_MANAGER_CONTACT_FILTER = "contactManager_contactFilter";
	static final String COMPONENT_LB_PAGE_NUMBER = "lbPageNumber";
	static final String COMPONENT_LB_NUMBER_OF_PAGES = "lbNumberOfPages";
	static final String COMPONENT_EMAILS_TOOLBAR = "emails_toolbar";
	static final String COMPONENT_BT_NEXT_PAGE = "btNextPage";
	static final String COMPONENT_BT_PREVIOUS_PAGE = "btPreviousPage";
	static final String COMPONENT_PN_MESSAGE_LIST = "pnMessageList";
	static final String COMPONENT_FILTER_LIST = "filterList";
	static final String COMPONENT_PN_FILTER = "pnFilter";
	static final String COMPONENT_MI_SEND_SMS = "miSendSMS";
	static final String COMPONENT_NEW_GROUP = "newGroup";
	static final String COMPONENT_PN_EMAIL = "pnEmail";
	static final String COMPONENT_PN_BOTTOM = "pnBottom";
	static final String COMPONENT_PN_CONTACTS = "pnContacts";
	static final String COMPONENT_TF_COST_PER_SMS = "tfCostPerSMS";
	static final String COMPONENT_LB_COST_PER_SMS_PREFIX = "lbCostPerSmsPrefix";
	static final String COMPONENT_LB_COST_PER_SMS_SUFFIX = "lbCostPerSmsSuffix";
	static final String COMPONENT_LB_ESTIMATED_MONEY = "lbEstimatedMoney";
	static final String COMPONENT_LB_THIRD = "lbThird";
	static final String COMPONENT_LB_SECOND = "lbSecond";
	static final String COMPONENT_LB_FIRST = "lbFirst";
	static final String COMPONENT_LB_MSG_NUMBER = "lbMsgNumber";
	static final String COMPONENT_LB_REMAINING_CHARS = "lbRemainingChars";
	static final String COMPONENT_PN_MESSAGE = "pnMessage";
	@Deprecated
	static final String COMPONENT_BT_SEND = "btSend";
	static final String COMPONENT_PN_SEND = "pnSend";
	static final String COMPONENT_LB_HOME_TAB_LOGO = "lbHomeTabLogo";
	static final String COMPONENT_CB_HOME_TAB_LOGO_VISIBLE = "cbHomeTabLogoVisible";
	static final String COMPONENT_TF_IMAGE_SOURCE = "tfImageSource";
	static final String COMPONENT_KEY_ACT_PANEL = "keyActPanel";
	static final String COMPONENT_BT_CLEAR = "btClear";
	static final String COMPONENT_CB_LEAVE_GROUP = "cbLeaveGroup";
	static final String COMPONENT_CB_GROUPS_TO_LEAVE = "cbGroupsToLeave";
	static final String COMPONENT_CB_JOIN_GROUP = "cbJoinGroup";
	static final String COMPONENT_CB_GROUPS_TO_JOIN = "cbGroupsToJoin";
	static final String COMPONENT_TF_AUTO_REPLY = "tfAutoReply";
	static final String COMPONENT_TF_KEYWORD = "tfKeyword";
	static final String COMPONENT_PN_TIP = "pnTip";
	static final String COMPONENT_BT_SAVE = "btSave";
	static final String COMPONENT_ACTION_LIST = "actionList";
	static final String COMPONENT_KEYWORDS_DIVIDER = "keywordsDivider";
	static final String COMPONENT_CB_ACTION_TYPE = "cbActionType";
	static final String COMPONENT_ACCOUNTS_LIST = "accountsList";
	static final String COMPONENT_CB_FREQUENCY = "cbFrequency";
	static final String COMPONENT_TF_TEXT = "tfText";
	static final String COMPONENT_TF_END_TIME = "tfEndTime";
	static final String COMPONENT_TF_START_TIME = "tfStartTime";
	static final String COMPONENT_RB_HTTP = "rbHTTP";
	static final String COMPONENT_LB_TEXT = "lbText";
	static final String COMPONENT_BT_EDIT = "btEdit";
	static final String COMPONENT_LB_LIST = "lbList";
	static final String COMPONENT_GROUP_LIST = "groupList";
	static final String COMPONENT_CONTACT_LIST = "contactList";
	static final String COMPONENT_LIST = "list";
	static final String COMPONENT_PENDING_LIST = "pendingList";
	static final String COMPONENT_EMAIL_LIST = "emailList";
	static final String COMPONENT_BT_DELETE = "btDelete";
	static final String COMPONENT_MI_DELETE = "miDelete";
	static final String COMPONENT_MI_EDIT = "miEdit";
	static final String COMPONENT_KEY_PANEL = "keyPanel";
	static final String COMPONENT_CB_USE_SSL = "cbUseSSL";
	static final String COMPONENT_TF_ACCOUNT_PASS = "tfAccountPass";
	static final String COMPONENT_TF_ACCOUNT_SERVER_PORT = "tfPort";
	static final String COMPONENT_TF_ACCOUNT = "tfAccount";
	static final String COMPONENT_TF_MAIL_SERVER = "tfMailServer";
	static final String COMPONENT_TF_SUBJECT = "tfSubject";
	static final String COMPONENT_TF_RECIPIENT = "tfRecipient";
	static final String COMPONENT_MAIL_LIST = "accountsList";
	static final String COMPONENT_RB_NO_RESPONSE = "rbNoResponse";
	static final String COMPONENT_PN_RESPONSE = "pnResponse";
	static final String COMPONENT_RB_TYPE_COMMAND_LINE = "rbTypeCL";
	static final String COMPONENT_TF_MESSAGE = "tfMessage";
	static final String COMPONENT_CB_FORWARD = "cbForward";
	static final String COMPONENT_CB_AUTO_REPLY = "cbAutoReply";
	static final String COMPONENT_RB_FRONTLINE_COMMANDS = "rbFrontlineCommands";
	static final String COMPONENT_RB_PLAIN_TEXT = "rbPlainText";
	static final String COMPONENT_TF_COMMAND = "tfCommand";
	static final String COMPONENT_RB_TYPE_HTTP = "rbTypeHTTP";
	static final String COMPONENT_EXTERNAL_COMMAND_GROUP_LIST = COMPONENT_GROUP_LIST;
	static final String COMPONENT_TF_END_DATE = "tfEndDate";
	static final String COMPONENT_TF_START_DATE = "tfStartDate";
	static final String COMPONENT_SEND_CONSOLE_MESSAGE_LIST = "sendConsole_messageList";
	static final String COMPONENT_RECEIVE_CONSOLE_MESSAGE_LIST = "receiveConsole_messageList";
	static final String COMPONENT_MESSAGE_TRACKER_FAILED_MESSAGE_LIST = "messageTracker_failedMessageList";
	static final String COMPONENT_MESSAGE_TRACKER_PENDING_MESSAGE_LIST = "messageTracker_pendingMessageList";
	static final String COMPONENT_RADIO_BUTTON_ACTIVE = "rb_active";
	static final String COMPONENT_TABBED_PANE = "tabbedPane";
	static final String COMPONENT_NEW_KEYWORD_FORM_KEYWORD = "newKeywordForm_keyword";
	static final String COMPONENT_NEW_KEYWORD_FORM_DESCRIPTION = "newKeywordForm_description";
	static final String COMPONENT_NEW_KEYWORD_BUTTON_DONE = "btDone";
	static final String COMPONENT_NEW_KEYWORD_FORM_TITLE = "newKeywordForm_title";
	static final String COMPONENT_FORWARD_FORM_GROUP_LIST = "forwardForm_groupList";
	static final String COMPONENT_FORWARD_FORM_TITLE = "forwardForm_title";
	static final String COMPONENT_FORWARD_FORM_TEXTAREA = "forward";
	static final String COMPONENT_SEND_CONSOLE_GROUP_TREE = "sendConsole_groupTree";
	static final String COMPONENT_SEND_CONSOLE_LONE_RECIPIENT = "sendConsole_loneRecipient";
	static final String COMPONENT_CONTACT_SELECTER_OK_BUTTON = "contactSelecter_okButton";
	static final String COMPONENT_CONTACT_SELECTER_CONTACT_LIST = "contactSelecter_contactList";
	static final String COMPONENT_CONTACT_SELECTER_TITLE = "contactSelecter_title";
	static final String COMPONENT_GROUPS_MENU = "groupsMenu";
	static final String COMPONENT_BUTTON_YES = "btYes";
	static final String COMPONENT_DELETE_NEW_CONTACT = "deleteNewContact";
	static final String COMPONENT_LABEL_STATUS = "lbStatus";
	static final String COMPONENT_SEND_CONSOLE_MODEM_LIST = "sendConsole_modemList";
	static final String COMPONENT_SURVEY_MANAGER_DELETE_BUTTON = "surveyManager_deleteButton";
	static final String COMPONENT_SURVEY_MANAGER_EDIT_BUTTON = "surveyManager_editButton";
	static final String COMPONENT_SURVEY_MANAGER_SURVEY_DESCRIPTION = "surveyManager_surveyDescription";
	static final String COMPONENT_SURVEY_MANAGER_SURVEY_KEYWORD = "surveyManager_surveyKeyword";
	static final String COMPONENT_GROUP_MANAGER_CONTACT_DETAILS = "groupManager_contactDetails";
	static final String COMPONENT_GROUP_MANAGER_DELETE_CONTACTS_BUTTON = "groupManager_deleteContactsButton";
	static final String COMPONENT_GROUP_MANAGER_SEND_SMS_BUTTON = "groupManager_sendSMSButton";
	static final String COMPONENT_GROUP_MANAGER_CREATE_NEW_CONTACT = "group_manager_createNewContact";
	static final String COMPONENT_GROUP_MANAGER_CONTACT_LIST = "groupManager_contactList";
	static final String COMPONENT_MENU_SWITCH_MODE = "menu_switchMode";
	static final String COMPONENT_ANALYST_MESSAGES_UNREGISTERED = "analystMessages_unregistered";
	static final String COMPONENT_ANALYST_MESSAGES = "analystMessages";
	static final String COMPONENT_MENU_ITEM_VIEW_CONTACT = "viewContact";
	static final String COMPONENT_HISTORY_MESSAGE_LIST = "history_messageList";
	static final String COMPONENT_HISTORY_RECEIVED_MESSAGES_TOGGLE = "historyReceivedMessagesToggle";
	static final String COMPONENT_HISTORY_SENT_MESSAGES_TOGGLE = "historySentMessagesToggle";
	static final String COMPONENT_MENU_ITEM_MSG_HISTORY = "msg_history";
	static final String COMPONENT_NEW_CONTACT_GROUP_LIST = "newContact_groupList";
	static final String COMPONENT_MENU_ITEM_CREATE = "miCreate";
	static final String COMPONENT_STATUS_BAR = "statusBar";
	static final String COMPONENT_PROGRESS_BAR = "progressBar";
	static final String COMPONENT_CONTACT_MANAGER_GROUP_TREE = "contactManager_groupList";
	static final String COMPONENT_CONTACT_MANAGER_CONTACT_LIST = "contactManager_contactList";
	static final String COMPONENT_MESSAGE_LIST = "messageList";
	static final String COMPONENT_COMPOSE_MESSAGE_RECIPIENT_LIST = "composeMessage_to";
	static final String COMPONENT_RECEIVED_MESSAGES_TOGGLE = "receivedMessagesToggle";
	static final String COMPONENT_SENT_MESSAGES_TOGGLE = "sentMessagesToggle";
	static final String COMPONENT_KEYWORD_LIST = "keywordList";
	static final String COMPONENT_SURVEY_LIST = "surveyManager_surveyList";
	static final String COMPONENT_SURVEY_DETAILS = "surveyManager_surveyDetails";
	static final String COMPONENT_ANALYST_KEYWORD_LIST = "analystKeywordList";
	static final String COMPONENT_REPLY_MANAGER_LIST = "replyManager_keywordList";
	static final String COMPONENT_REPLY_MANAGER_DETAILS = "replyManager_keywordDetails";
	static final String COMPONENT_REPLY_MANAGER_REPLY_TEXT = "replyManager_replyText";
	static final String COMPONENT_REPLY_MANAGER_KEYWORD = "replyManager_keyword";
	static final String COMPONENT_REPLY_MANAGER_EDIT_BUTTON = "replyManager_editButton";
	static final String COMPONENT_REPLY_MANAGER_EXPORT_BUTTON = "replyManager_exportButton";
	static final String COMPONENT_REPLY_MANAGER_DELETE_BUTTON = "replyManager_deleteButton";
	static final String COMPONENT_REPLY_MANAGER_CREATE_BUTTON = "replyManager_createButton";
	static final String COMPONENT_GROUP_SELECTER_GROUP_LIST = "groupSelecter_groupList";
	static final String COMPONENT_GROUP_SELECTER_OK_BUTTON = "groupSelecter_okButton";
	static final String COMPONENT_GROUP_SELECTER_TITLE = "groupSelecter_title";
	static final String COMPONENT_VIEW_CONTACT_BUTTON = "viewContactButton";
	static final String COMPONENT_SEND_SMS_BUTTON = "sendSMSButton";
	static final String COMPONENT_CONTACT_NAME = "contact_name";
	static final String COMPONENT_CONTACT_MOBILE_MSISDN = "contact_mobileMsisdn";
	static final String COMPONENT_CONTACT_OTHER_MSISDN = "contact_otherMsisdn";
	static final String COMPONENT_CONTACT_EMAIL_ADDRESS = "contact_emailAddress";
	static final String COMPONENT_CONTACT_NOTES = "contact_notes";
	static final String COMPONENT_CONTACT_DORMANT = "rb_dormant";
	/** Thinlet Component Name: TODO document */
	static final String COMPONENT_CB_GROUPS = "cbGroups";
}
