package com.ushahidi.plugins.mapping.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.utils.MappingLogger;
import com.ushahidi.plugins.mapping.utils.MappingMessages;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.events.EntitySavedNotification;
import net.frontlinesms.events.FrontlineEventNotification;

import net.frontlinesms.plugins.surveys.SurveysPluginController;
import net.frontlinesms.plugins.surveys.data.domain.Survey;
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
	
	private final QuestionDao questionDao;
	private final SurveyDao surveyDao;
	
	private final CategoryDao categoryDao;
	private final LocationDao locationDao;
	private final MappingSetupDao mappingSetupDao;
	
	private SurveysPluginController resourceMapperPluginController;
	
	private final Map<String, Question> questionDictionary = new HashMap<String, Question>();
	
	/**
	 * SurveysManager
	 * @param frontlineController FrontlineSMS
	 * @param pluginController FrontlineSMS
	 * @param appContext ApplicationContext
	 */
	public SurveysManager(FrontlineSMS frontlineController, MappingPluginController pluginController) {
		frontlineController.getEventBus().registerObserver(this);
		
		this.resourceMapperPluginController = getPluginController(frontlineController, SurveysPluginController.class);
		
		this.categoryDao = pluginController.getCategoryDao();
		this.locationDao = pluginController.getLocationDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();

		this.questionDao = resourceMapperPluginController.getQuestionDao();
		this.surveyDao = resourceMapperPluginController.getSurveyDao();
	}
	
	/**
	 * Create Ushahidi-friendly Operator questions
	 */
	public boolean addUshahidiQuestions() {
		LOG.debug("createUshahidiQuestions");
		try {
			List<Question> questions = new ArrayList<Question>();
			Question question = null;
			//DETAILS
			question = addOperatorQuestion(MappingMessages.getTitle(), MappingMessages.getTitleKeyword(), MappingMessages.getTitleInfo(), QuestionType.PLAINTEXT);
			if(question != null) questions.add(question);
			
			question = addOperatorQuestion(MappingMessages.getDescription(), MappingMessages.getDescriptionKeyword(), MappingMessages.getDescriptionInfo(), QuestionType.PLAINTEXT);
			if(question != null) questions.add(question);
			//DATE
			question = addOperatorQuestion(MappingMessages.getDate(), MappingMessages.getDateKeyword(), MappingMessages.getDateInfo(), QuestionType.DATE);
			if(question != null) questions.add(question);
			//CATEGORIES
			List<String> categories = new ArrayList<String>();
			for(Category category: this.categoryDao.getAllCategories(this.mappingSetupDao.getDefaultSetup())){
				categories.add(category.getTitle());
			}
			question = addOperatorQuestion(MappingMessages.getCategories(), MappingMessages.getCategoriesKeyword(), MappingMessages.getCategoriesInfo(), QuestionType.CHECKLIST, 
								categories.toArray(new String[categories.size()]));
			if(question != null) questions.add(question);
			//LOCATION
			List<String> locations = new ArrayList<String>();
			for(Location location: this.locationDao.getAllLocations(this.mappingSetupDao.getDefaultSetup())) {
				if (location.getName() != null && location.getName().length() > 0 && location.getName().equalsIgnoreCase("unknown") == false) {
					locations.add(location.getName());
				}
			}
			question = addOperatorQuestion(MappingMessages.getLocation(), MappingMessages.getLocationKeyword(), MappingMessages.getLocationInfo(), QuestionType.MULTICHOICE, 
								locations.toArray(new String[locations.size()]));
			if(question != null) questions.add(question);
			//NEWS
			question = addOperatorQuestion(MappingMessages.getNews(), MappingMessages.getNewsKeyword(), MappingMessages.getNewsInfo(), QuestionType.PLAINTEXT);
			if(question != null) questions.add(question);
			//VIDEO
			question = addOperatorQuestion(MappingMessages.getVideo(), MappingMessages.getVideoKeyword(), MappingMessages.getVideoInfo(), QuestionType.PLAINTEXT);
			if(question != null) questions.add(question);
			//CONTACT
			question = addOperatorQuestion(MappingMessages.getFirstName(), MappingMessages.getFirstNameKeyword(), MappingMessages.getFirstNameInfo(), QuestionType.PLAINTEXT);
			if(question != null) questions.add(question);
			
			question = addOperatorQuestion(MappingMessages.getLastName(), MappingMessages.getLastNameKeyword(), MappingMessages.getLastNameInfo(), QuestionType.PLAINTEXT);
			if(question != null) questions.add(question);
			
			question = addOperatorQuestion(MappingMessages.getEmail(), MappingMessages.getEmailKeyword(), MappingMessages.getEmailInfo(), QuestionType.PLAINTEXT);
			if(question != null) questions.add(question);
			
			Survey survey = new Survey(MappingMessages.getIncidentReport(), "ushahidi", questions);
			this.surveyDao.saveSurvey(survey);
			return true;	
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Handle incoming FrontlineEventNotification
	 */
	@SuppressWarnings("unchecked")
	public void notify(FrontlineEventNotification notification) {
		if (notification instanceof EntitySavedNotification) {
			EntitySavedNotification entitySavedNotification = (EntitySavedNotification)notification;
			if (entitySavedNotification != null) {
				if (entitySavedNotification.getDatabaseEntity() instanceof Answer) {
					Answer questionResponse = (Answer)entitySavedNotification.getDatabaseEntity();
					if (questionDictionary.containsKey(questionResponse.getQuestionKeyword())) {
						Question question = questionResponse.getQuestion();
						LOG.error("Ushahidi Question Received [%s, %s, %s, %s]", question.getName(), question.getKeyword(), question.getType(), questionResponse.getAnswerValue());
						
						
					}
				}	
			}
		}
	}
	
	/**
	 * Create Ushahidi-specific Operator questions
	 * @param name name
	 * @param keyword keyword
	 * @param infoSnippet info snippet
	 * @param type operator type
	 * @param schema operator schema
	 */
	private Question addOperatorQuestion(String name, String keyword, String infoSnippet, String type) {
		return addOperatorQuestion(name, keyword, infoSnippet, type, null);
	}
	
	/**
	 * Create Ushahidi-specific Operator questions
	 * @param name name
	 * @param keyword keyword
	 * @param infoSnippet info snippet
	 * @param type operator type
	 * @param schema operator schema
	 * @param choices list of choices
	 */
	private Question addOperatorQuestion(String name, String keyword, String infoSnippet, String type, String [] choices) {
		try {
			List<String> choiceList = choices != null ? Arrays.asList(choices) : null;
			Question question = QuestionFactory.createQuestion(name, keyword, infoSnippet, type, null, choiceList);
			this.questionDao.saveQuestion(question);
			LOG.debug("Question Created [%s, %s, %s]", question.getName(), question.getKeyword(), question.getType());
			this.questionDictionary.put(keyword, question);
			return question;
		} 
		catch (DuplicateKeyException ex) {
			LOG.error("Question Loaded [%s, %s, %s]", name, keyword, type);
			Question question = this.questionDao.getQuestionForKeyword(keyword);
			this.questionDictionary.put(keyword, question);
			return question;
		}
		catch (Exception ex) {
			Question question = this.questionDao.getQuestionForKeyword(keyword);
			LOG.error("Question Loaded [%s, %s, %s]", name, keyword, type);
			this.questionDictionary.put(keyword, question);
			return question;
		}
	}
}