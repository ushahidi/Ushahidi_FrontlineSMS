package com.ushahidi.plugins.mapping.managers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.events.EntitySavedNotification;
import net.frontlinesms.data.events.EntityUpdatedNotification;
import net.frontlinesms.events.FrontlineEventNotification;

import net.frontlinesms.plugins.textforms.TextFormsPluginController;
import net.frontlinesms.plugins.textforms.data.domain.TextForm;
import net.frontlinesms.plugins.textforms.data.domain.TextFormResponse;
import net.frontlinesms.plugins.textforms.data.domain.questions.Question;
import net.frontlinesms.plugins.textforms.data.domain.questions.QuestionType;
import net.frontlinesms.plugins.textforms.data.domain.answers.Answer;
import net.frontlinesms.plugins.textforms.data.repository.QuestionDao;
import net.frontlinesms.plugins.textforms.data.repository.QuestionFactory;
import net.frontlinesms.plugins.textforms.data.repository.TextFormDao;
import net.frontlinesms.plugins.textforms.data.repository.TextFormResponseDao;

/**
 * TextFormsManager
 * @author dalezak
 *
 */
public class TextFormsManager extends Manager {

	public static MappingLogger LOG = MappingLogger.getLogger(TextFormsManager.class);	
	
	private final MappingPluginController pluginController;
	private final QuestionDao questionDao;
	private final TextFormDao textformDao;
	private final TextFormResponseDao textformResponseDao;
	
	private final CategoryDao categoryDao;
	private final LocationDao locationDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;

	private final String textformName;
	
	/**
	 * TextFormsManager
	 * @param frontlineController FrontlineSMS
	 * @param pluginController FrontlineSMS
	 * @param appContext ApplicationContext
	 */
	public TextFormsManager(FrontlineSMS frontlineController, MappingPluginController pluginController) {
		frontlineController.getEventBus().registerObserver(this);
		this.pluginController = pluginController;
		this.categoryDao = pluginController.getCategoryDao();
		this.locationDao = pluginController.getLocationDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();

		TextFormsPluginController textformsPluginController = getPluginController(frontlineController, TextFormsPluginController.class);
		this.questionDao = textformsPluginController.getQuestionDao();
		this.textformDao = textformsPluginController.getTextFormDao();
		this.textformResponseDao = textformsPluginController.getTextFormResponseDao();
		
		this.textformName = MappingMessages.getIncidentReport();
	}
	
	/**
	 * Handle incoming FrontlineEventNotification
	 */
	public void notify(FrontlineEventNotification notification) {
		if (notification instanceof EntitySavedNotification<?>) {
			EntitySavedNotification<?> entitySavedNotification = (EntitySavedNotification<?>)notification;
			if (entitySavedNotification.getDatabaseEntity() instanceof Answer<?>) {
				Answer<?> answer = (Answer<?>)entitySavedNotification.getDatabaseEntity();
				Question question = answer.getQuestion();
				LOG.error("Answer Received [%s, %s, %s, %s]", question.getName(), question.getKeyword(), question.getType(), answer.getAnswerValue());
			}
			else if (entitySavedNotification.getDatabaseEntity() instanceof TextFormResponse) {
				TextFormResponse textformResponse = (TextFormResponse)entitySavedNotification.getDatabaseEntity();
				if (textformResponse.isCompleted()) {
					LOG.debug("TextForm '%s' IS completed", textformResponse.getTextFormName());
					saveIncidentFromSurveryResponse(textformResponse);
				}
				else {
					LOG.debug("TextForm '%s' NOT completed", textformResponse.getTextFormName());
				}
			}
		}
		else if (notification instanceof EntityUpdatedNotification<?>) {
			EntityUpdatedNotification<?> entityUpdatedNotification = (EntityUpdatedNotification<?>)notification;
			if (entityUpdatedNotification.getDatabaseEntity() instanceof TextFormResponse) {
				TextFormResponse textformResponse = (TextFormResponse)entityUpdatedNotification.getDatabaseEntity();
				if (textformResponse.isCompleted()) {
					LOG.debug("TextForm '%s' IS completed", textformResponse.getTextFormName());
					saveIncidentFromSurveryResponse(textformResponse);
				}
				else {
					LOG.debug("TextForm '%s' NOT completed", textformResponse.getTextFormName());
				}
			}
		}
	}
	
	/**
	 * Save incident from survery response
	 * @param textformResponse TextFormResponse
	 */
	private void saveIncidentFromSurveryResponse(TextFormResponse textformResponse) {
		Incident incident = incidentDao.getIncidentByTextFormResponse(textformResponse);
		if (incident == null) {
			incident = new Incident();
		}
		incident.setMarked(true);
		incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
		incident.setTextFormResponse(textformResponse);
		Contact contact = textformResponse.getContact();
		if (contact != null) {
			incident.setFirstName(contact.getName());
			incident.setEmailAddress(contact.getEmailAddress());
		}
		for (Answer<?> answer : textformResponse.getAnswers()) {
			LOG.debug("%s = %s", answer.getQuestionName(), answer.getAnswerValue());
			if (answer.getQuestionName().equalsIgnoreCase(MappingMessages.getTitle())) {
				//TODO use answer.getAnswerValue()
				incident.setTitle(answer.getMessage().getTextContent());
			}
			else if (answer.getQuestionName().equalsIgnoreCase(MappingMessages.getDescription())) {
				//TODO use answer.getAnswerValue()
				incident.setDescription(answer.getMessage().getTextContent());
			}
			else if (answer.getQuestionName().equalsIgnoreCase(MappingMessages.getDate())) {
				try {
					DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					incident.setIncidentDate(format.parse(answer.getAnswerValue()));
				} 
				catch (ParseException ex) {
					LOG.error("Error parsing date: %s", ex);
				}
			}
			else if (answer.getQuestionName().equalsIgnoreCase(MappingMessages.getCategories())) {
				//TODO improve this double for-loop
				for(String categoryTitle : answer.getAnswerValue().split(",")) {
					for(Category category : categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())) {
						if (category.getTitle().equalsIgnoreCase(categoryTitle)) {
							incident.addCategory(category);
							break;
						}
					}
				}
			}
			else if (answer.getQuestionName().equalsIgnoreCase(MappingMessages.getLocation())) {
				for(Location location : locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {
					if (location.getName().equalsIgnoreCase(answer.getAnswerValue())) {
						incident.setLocation(location);
						break;
					}
				}
			}
		}
		try {
			incidentDao.saveIncident(incident);
			LOG.debug("New Incident Created: %s", incident.getTitle());
			pluginController.setStatus(MappingMessages.getIncidentCreatedFromTextForm());
			pluginController.refreshIncidentMap();
			pluginController.refreshIncidentReports();
		} 
		catch (DuplicateKeyException ex) {
			LOG.error("DuplicateKeyException: %s", ex);
		}
	}
	
	/**
	 * Create Ushahidi-friendly textform questions
	 */
	public boolean addTextFormQuestions() {
		LOG.debug("createUshahidiQuestions");
		try {
			List<Question> questions = new ArrayList<Question>();
			//TITLE
			createPlainTextQuestion(questions, MappingMessages.getTitle(), MappingMessages.getTitleKeyword(), MappingMessages.getTitleInfo());
			//DESCRIPTION
			createPlainTextQuestion(questions, MappingMessages.getDescription(), MappingMessages.getDescriptionKeyword(), MappingMessages.getDescriptionInfo());
			//DATE
			createDateQuestion(questions, MappingMessages.getDate(), MappingMessages.getDateKeyword(), MappingMessages.getDateInfo());
			//CATEGORIES
			List<String> categories = new ArrayList<String>();
			for(Category category: this.categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
				categories.add(category.getTitle());
			}
			createChecklistQuestion(questions, MappingMessages.getCategories(), MappingMessages.getCategoriesKeyword(), MappingMessages.getCategoriesInfo(), categories.toArray(new String[categories.size()]));
			//LOCATION
			List<String> locations = new ArrayList<String>();
			for(Location location: this.locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {
				if (location.getName() != null && location.getName().length() > 0 && location.getName().equalsIgnoreCase("unknown") == false) {
					locations.add(location.getName());
				}
			}
			createChecklistQuestion(questions, MappingMessages.getLocation(), MappingMessages.getLocationKeyword(), MappingMessages.getLocationInfo(), locations.toArray(new String[locations.size()]));
			
			TextForm textform = new TextForm(textformName, "report", questions);
			this.textformDao.saveTextForm(textform);
			LOG.debug("TextForm Created: %s", textform.getName());
			return true;	
		}
		catch(Exception ex){
			LOG.error("Exception in addTextFormQuestions", ex);
		}
		return false;
	}
	
	public boolean addTextFormAnswers(String title) {
		try {
			
			return true;
		}
		catch (Exception ex){
			LOG.error("Exception in addTextFormAnswers", ex);
		}
		return false;
	}
	
	public TextFormResponseDao getTextFormResponseDao() {
		return textformResponseDao;
	}
	
	protected Question createPlainTextQuestion(List<Question> questions, String name, String keyword, String infoSnippet) {
		return createQuestion(questions, name, keyword, infoSnippet, QuestionType.PLAINTEXT, null);
	}
	
	protected Question createDateQuestion(List<Question> questions, String name, String keyword, String infoSnippet) {
		return createQuestion(questions, name, keyword, infoSnippet, QuestionType.DATE, null);
	}
	
	protected Question createIntegerQuestion(List<Question> questions, String name, String keyword, String infoSnippet) {
		return createQuestion(questions, name, keyword, infoSnippet, QuestionType.INTEGER, null);
	}
	
	protected Question createBooleanQuestion(List<Question> questions, String name, String keyword, String infoSnippet) {
		return createQuestion(questions, name, keyword, infoSnippet, QuestionType.BOOLEAN, null);
	}
	
	protected Question createMultiChoiceQuestion(List<Question> questions, String name, String keyword, String infoSnippet, String [] choices) {
		return createQuestion(questions, name, keyword, infoSnippet, QuestionType.MULTICHOICE, choices);
	}
	
	protected Question createChecklistQuestion(List<Question> questions, String name, String keyword, String infoSnippet, String [] choices) {
		return createQuestion(questions, name, keyword, infoSnippet, QuestionType.CHECKLIST, choices);
	}
	
	/**
	 * Create Ushahidi-specific Operator questions
	 * @param name name
	 * @param keyword keyword
	 * @param infoSnippet info snippet
	 * @param type operator type
	 * @param choices list of choices
	 */
	protected Question createQuestion(List<Question> questions, String name, String keyword, String infoSnippet, String type, String [] choices) {
		try {
			List<String> choiceList = choices != null ? Arrays.asList(choices) : null;
			Question question = QuestionFactory.createQuestion(name, keyword, infoSnippet, type, null, choiceList);
			this.questionDao.saveQuestion(question);
			LOG.debug("Question Created [%s, %s, %s]", question.getName(), question.getKeyword(), question.getType());
			questions.add(question);
			return question;
		} 
		catch (DuplicateKeyException ex) {
			LOG.error("Question Loaded [%s, %s, %s]", name, keyword, type);
			Question question = this.questionDao.getQuestionForKeyword(keyword);
			questions.add(question);
			return question;
		}
		catch (Exception ex) {
			Question question = this.questionDao.getQuestionForKeyword(keyword);
			LOG.error("Question Loaded [%s, %s, %s]", name, keyword, type);
			return question;
		}
	}
}