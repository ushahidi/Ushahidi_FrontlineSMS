package com.ushahidi.plugins.mapping.managers;

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

import net.frontlinesms.plugins.surveys.SurveysPluginController;
import net.frontlinesms.plugins.surveys.data.domain.Survey;
import net.frontlinesms.plugins.surveys.data.domain.SurveyResponse;
import net.frontlinesms.plugins.surveys.data.domain.questions.Question;
import net.frontlinesms.plugins.surveys.data.domain.questions.QuestionType;
import net.frontlinesms.plugins.surveys.data.domain.answers.Answer;
import net.frontlinesms.plugins.surveys.data.repository.QuestionDao;
import net.frontlinesms.plugins.surveys.data.repository.QuestionFactory;
import net.frontlinesms.plugins.surveys.data.repository.SurveyDao;

/**
 * SurveysManager
 * @author dalezak
 *
 */
public class SurveysManager extends Manager {

	public static MappingLogger LOG = MappingLogger.getLogger(SurveysManager.class);	
	
	private final MappingPluginController pluginController;
	private final QuestionDao questionDao;
	private final SurveyDao surveyDao;
	
	private final CategoryDao categoryDao;
	private final LocationDao locationDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;

	private final String surveyName;
	
	/**
	 * SurveysManager
	 * @param frontlineController FrontlineSMS
	 * @param pluginController FrontlineSMS
	 * @param appContext ApplicationContext
	 */
	public SurveysManager(FrontlineSMS frontlineController, MappingPluginController pluginController) {
		frontlineController.getEventBus().registerObserver(this);
		this.pluginController = pluginController;
		this.categoryDao = pluginController.getCategoryDao();
		this.locationDao = pluginController.getLocationDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();

		SurveysPluginController surveysPluginController = getPluginController(frontlineController, SurveysPluginController.class);
		this.questionDao = surveysPluginController.getQuestionDao();
		this.surveyDao = surveysPluginController.getSurveyDao();
		
		this.surveyName = MappingMessages.getIncidentReport();
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
			else if (entitySavedNotification.getDatabaseEntity() instanceof SurveyResponse) {
				SurveyResponse surveyResponse = (SurveyResponse)entitySavedNotification.getDatabaseEntity();
				if (surveyResponse.isCompleted()) {
					LOG.debug("Survey '%s' IS completed", surveyResponse.getSurveyName());
				}
				else {
					LOG.debug("Survey '%s' NOT completed", surveyResponse.getSurveyName());
				}
				Incident incident = new Incident();
				incident.setMarked(true);
				incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
				incident.setSurveyResponse(surveyResponse);
				Contact contact = surveyResponse.getContact();
				if (contact != null) {
					incident.setFirstName(contact.getName());
					incident.setEmailAddress(contact.getEmailAddress());
				}
				//TODO update incident properties
				try {
					incidentDao.saveIncident(incident);
					LOG.debug("New Incident Created: %s", incident.getTitle());
					pluginController.setStatus(MappingMessages.getIncidentCreatedFromSurvey());
					pluginController.refreshIncidentMap();
					pluginController.refreshIncidentReports();
				} 
				catch (DuplicateKeyException ex) {
					LOG.error("DuplicateKeyException: %s", ex);
				}
			}
		}
		else if (notification instanceof EntityUpdatedNotification<?>) {
			EntityUpdatedNotification<?> entityUpdatedNotification = (EntityUpdatedNotification<?>)notification;
			if (entityUpdatedNotification.getDatabaseEntity() instanceof SurveyResponse) {
				SurveyResponse surveyResponse = (SurveyResponse)entityUpdatedNotification.getDatabaseEntity();
				if (surveyResponse.isCompleted()) {
					LOG.debug("Survey '%s' IS completed", surveyResponse.getSurveyName());
				}
				else {
					LOG.debug("Survey '%s' NOT completed", surveyResponse.getSurveyName());
				}
				Incident incident = incidentDao.getIncidentBySurveyResponse(surveyResponse);
				if (incident != null) {
					//TODO update incident properties
					try {
						incidentDao.updateIncident(incident);
						LOG.debug("Incident Updated: %s", incident.getTitle());
						pluginController.setStatus(MappingMessages.getIncidentUpdatedFromSurvey());
						pluginController.refreshIncidentMap();
						pluginController.refreshIncidentReports();
					} 
					catch (DuplicateKeyException ex) {
						LOG.error("DuplicateKeyException: %s", ex);
					}
				}
				else {
					LOG.error("Incident is NULL for Survey: %s", surveyResponse.getSurveyName());
				}
			}
		}
	}
	
	/**
	 * Create Ushahidi-friendly survey questions
	 */
	public boolean addSurveyQuestions() {
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
			for(Category category: this.categoryDao.getAllCategories(this.mappingSetupDao.getDefaultSetup())){
				categories.add(category.getTitle());
			}
			createChecklistQuestion(questions, MappingMessages.getCategories(), MappingMessages.getCategoriesKeyword(), MappingMessages.getCategoriesInfo(), categories.toArray(new String[categories.size()]));
			//LOCATION
			List<String> locations = new ArrayList<String>();
			for(Location location: this.locationDao.getAllLocations(this.mappingSetupDao.getDefaultSetup())) {
				if (location.getName() != null && location.getName().length() > 0 && location.getName().equalsIgnoreCase("unknown") == false) {
					locations.add(location.getName());
				}
			}
			createChecklistQuestion(questions, MappingMessages.getLocation(), MappingMessages.getLocationKeyword(), MappingMessages.getLocationInfo(), locations.toArray(new String[locations.size()]));
			
			Survey survey = new Survey(surveyName, "ushahidi", questions);
			this.surveyDao.saveSurvey(survey);
			LOG.debug("Survey Created: %s", survey.getName());
			return true;	
		}
		catch(Exception ex){
			LOG.error("Exception in addSurveyQuestions", ex);
		}
		return false;
	}
	
	public boolean addSurveyAnswers(String title) {
		try {
			
			return true;
		}
		catch (Exception ex){
			LOG.error("Exception in addSurveyAnswers", ex);
		}
		return false;
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