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
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;

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
	 * Handle incoming FrontlineEventNotification
	 */
	public void notify(FrontlineEventNotification notification) {
		if (notification instanceof EntitySavedNotification<?>) {
			EntitySavedNotification<?> entitySavedNotification = (EntitySavedNotification<?>)notification;
			if (entitySavedNotification != null) {
				if (entitySavedNotification.getDatabaseEntity() instanceof Answer<?>) {
					Answer<?> answer = (Answer<?>)entitySavedNotification.getDatabaseEntity();
					if (questionDictionary.containsKey(answer.getQuestionKeyword())) {
						Question question = answer.getQuestion();
						LOG.error("Ushahidi Question Received [%s, %s, %s, %s]", question.getName(), question.getKeyword(), question.getType(), answer.getAnswerValue());
					}
				}	
			}
		}
	}
	
	/**
	 * Create Ushahidi-friendly survey questions
	 */
	public boolean addSurveyQuestions(String surveyName) {
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
			
			Survey survey = new Survey(MappingMessages.getIncidentReport(), surveyName, questions);
			this.surveyDao.saveSurvey(survey);
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
			this.questionDictionary.put(keyword, question);
			questions.add(question);
			return question;
		} 
		catch (DuplicateKeyException ex) {
			LOG.error("Question Loaded [%s, %s, %s]", name, keyword, type);
			Question question = this.questionDao.getQuestionForKeyword(keyword);
			this.questionDictionary.put(keyword, question);
			questions.add(question);
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