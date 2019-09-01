package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.request.AnswerContainer;
import com.dls.base.request.TestContainer;
import com.dls.base.ui.form.FormAttribute;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.MoveUtils;
import com.dls.base.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class FormTrainingTestVariantRestController
{

	private final TestVariantPersonRepository testVariantPersonRepository;
	private final PersonRepository personRepository;
	private final StatusRepository statusRepository;
	private final MoveRepository moveRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final TestRepository testRepository;
	private final TemplateTestQuestionRepository templateTestQuestionRepository;
	private final TemplateTestAnswerRepository templateTestAnswerRepository;
	private final TestAnswerRepository testAnswerRepository;
	private final TestQuestionRepository testQuestionRepository;
	private final TemplateTestVariantRepository templateTestVariantRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	MoveUtils moveUtils;

	@Autowired
	FormTestRestController formTestRestController;

	@Autowired
    FormTrainingTestVariantRestController(TestVariantPersonRepository testVariantPersonRepository, PersonRepository personRepository, StatusRepository statusRepository, MoveRepository moveRepository, TemplateLifeCycleRepository templateLifeCycleRepository, TestRepository testRepository, TemplateTestQuestionRepository templateTestQuestionRepository, TemplateTestAnswerRepository templateTestAnswerRepository, TestAnswerRepository testAnswerRepository, TestQuestionRepository testQuestionRepository, TemplateTestVariantRepository templateTestVariantRepository) {
		this.testVariantPersonRepository = testVariantPersonRepository;
		this.personRepository = personRepository;
		this.statusRepository = statusRepository;
		this.moveRepository = moveRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.testRepository = testRepository;
		this.templateTestQuestionRepository = templateTestQuestionRepository;
		this.templateTestAnswerRepository = templateTestAnswerRepository;
		this.testAnswerRepository = testAnswerRepository;
		this.testQuestionRepository = testQuestionRepository;
		this.templateTestVariantRepository = templateTestVariantRepository;
	}

	@GetMapping(value = "form/trainingtestvariant/checking/{identifier}")
	public ResponseEntity testChecking(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		TestVariantPerson testVariantPerson = testVariantPersonRepository.findByTestVariantPersonId(id);
		if (!accessUtils.canReadCard(testVariantPerson)){
			throw new Exception("Отсутствуют права на чтение карточки");
		}
		if (!testVariantPerson.getStatus().getCode().equals("in_progress")){
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.build();
		}
		long timeDifference = 0L;
		Date currentDate = new Date();
		if (testVariantPerson.getTemplateTestVariant().getTemplateTest().getTimeLimit() > 0){
			long ONE_MINUTE_IN_MILLIS = 60000;
			Calendar date = Calendar.getInstance();
			date.setTime(testVariantPerson.getDateStart());
			Date futureDate = new Date(date.getTimeInMillis() + ((testVariantPerson.getTemplateTestVariant().getTemplateTest().getTimeLimit()) * ONE_MINUTE_IN_MILLIS));
			if (currentDate.compareTo(futureDate) > 0){
				toStatus(testVariantPerson.getId(), "expired");
				return ResponseEntity
						.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.build();
			}
			timeDifference = futureDate.getTime() - currentDate.getTime();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(timeDifference);
	}

	@GetMapping(value = "form/trainingtestvariant/{editMode}/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier, @PathVariable boolean editMode) throws Exception {
		Long id = Long.parseLong(identifier);
		TestVariantPerson testVariantPerson = testVariantPersonRepository.findByTestVariantPersonId(id);
		if (!accessUtils.canReadCard(testVariantPerson)){
			throw new Exception("Отсутствуют права на чтение карточки");
		}
		FormTemplate formTemplate = getForm(testVariantPerson, editMode);
		return formTemplate;
	}

	public FormTemplate getForm(TestVariantPerson testVariantPerson, boolean editMode) {
		FormTemplate formTemplate = new FormTemplate();
		TemplateTest templateTest = testVariantPerson.getTest().getTemplateTest();
		formTemplate.attributes = utils.getFormAttributes(templateTest);
		//
		FormAttribute formAttributeTestVariantPerson = new FormAttribute();
		formAttributeTestVariantPerson.name = "testvariantperson";
		formAttributeTestVariantPerson.title = "карточка прохождения теста";
		formAttributeTestVariantPerson.type = "testvariantperson";
		formAttributeTestVariantPerson.value = utils.getFormAttributes(testVariantPerson);
		//
		long lastTime = 0;
		Date currentDate = new Date();
		if (testVariantPerson.getTemplateTestVariant().getTemplateTest().getTimeLimit() > 0){
			if (testVariantPerson.getStatus().getCode().equals("in_progress")){
				long ONE_MINUTE_IN_MILLIS = 60000;
				Calendar date = Calendar.getInstance();
				date.setTime(testVariantPerson.getDateStart());
				Date futureDate = new Date(date.getTimeInMillis() + ((testVariantPerson.getTemplateTestVariant().getTemplateTest().getTimeLimit()) * ONE_MINUTE_IN_MILLIS));
				if (testVariantPerson.getStatus().getCode().equals("assigned")){
					lastTime = futureDate.getTime();
				}else {
					long timeDifference = futureDate.getTime() - currentDate.getTime();
					if (timeDifference > 0){
						lastTime = timeDifference;
					}
				}
			}
		}

		//
		FormAttribute formAttributeLastTime = new FormAttribute();
		formAttributeLastTime.name = "lasttime";
		formAttributeLastTime.title = "Оставшееся время";
		formAttributeLastTime.type = "lasttime";
		formAttributeLastTime.value = lastTime;
		//

		HashMap<String, FormAttribute> testVariantPersonAttributes = utils.getFormAttributes(testVariantPerson);
		formTemplate.attributes.put("original_id", testVariantPersonAttributes.get("id"));
		FormAttribute formAttribute = new FormAttribute();
		formAttribute.name = "original_type";
		formAttribute.type = "string";
		formAttribute.value = "trainingtest";
		formAttribute.title = "original_type";
		formTemplate.attributes.put("original_type", formAttribute);
		formTemplate.attributes.put("original_status", testVariantPersonAttributes.get("status"));
		formTemplate.attributes.put("original_testvariantperson", formAttributeTestVariantPerson);
		formTemplate.attributes.put("original_lasttime", formAttributeLastTime);
		//
		FormAttribute formAttributeVariant = new FormAttribute();
		formAttributeVariant.name = "variant";
		formAttributeVariant.title = "вариант";
		formAttributeVariant.type = "variant";
		formAttributeVariant.value = getFormVariant(testVariantPerson.getTemplateTestVariant(), testVariantPerson);
		//
		formTemplate.attributes.put("variant", formAttributeVariant);
		formTemplate.tabTitle = templateTest.getName();
		formTemplate.template = TemplateTestQuestion.class.getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());

		if (editMode){
			for (FormTemplate formTemplate1 : (ArrayList<FormTemplate>)((FormTemplate)formTemplate.attributes.get("variant").value).attributes.get("questions").value){
				for (FormTemplate formTemplate2 : (ArrayList<FormTemplate>)formTemplate1.attributes.get("answers").value){
					formTemplate2.attributes.remove("correct");
				}
			}
		}

		return formTemplate;
	}

	public FormTemplate getFormVariant(TemplateTestVariant templateTestVariant, TestVariantPerson testVariantPerson) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templateTestVariant);
		formTemplate.tabTitle = "Вариант " + templateTestVariant.getNumber();
		formTemplate.template = templateTestVariant.getClass().getSimpleName().toLowerCase();
		//
		FormAttribute formAttribute = new FormAttribute();
		formAttribute.name = "questions";
		formAttribute.title = "вопросы";
		formAttribute.type = "questions";
		//
		ArrayList questions = new ArrayList();
		for (TemplateTestQuestion templateTestQuestion : templateTestQuestionRepository.findByTemplateTestVariantId(templateTestVariant.getId())){
			questions.add(getFormQuestion(templateTestQuestion, testVariantPerson));
		}
		//
		formAttribute.value = questions;
		//
		formTemplate.attributes.put("questions", formAttribute);
		return formTemplate;
	}

	public FormTemplate getFormAnswer(TemplateTestAnswer templateTestAnswer, TestAnswer testAnswer) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templateTestAnswer);
		//
		FormAttribute formAttribute = new FormAttribute();
		formAttribute.name = "checked";
		formAttribute.title = "выбран";
		formAttribute.type = "boolean";
		formAttribute.value = (testAnswer != null);
		formTemplate.attributes.put("checked", formAttribute);
		//
		formTemplate.tabTitle = "Ответ " + templateTestAnswer.getNumber();
		formTemplate.template = templateTestAnswer.getClass().getSimpleName().toLowerCase();
		return formTemplate;
	}

	public FormTemplate getFormQuestion(TemplateTestQuestion templateTestQuestion, TestVariantPerson testVariantPerson) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templateTestQuestion);
		//
		TestQuestion testQuestion = testQuestionRepository.findByTestVariantPersonIdAndTestQuestionNumber(testVariantPerson.getId(), templateTestQuestion.getNumber());
		FormAttribute formAttribute1 = new FormAttribute();
		formAttribute1.name = "параметры вопроса " + testQuestion.getNumber();
		formAttribute1.title = "параметры вопроса " + testQuestion.getNumber();
		formAttribute1.type = testQuestion.getClass().getSimpleName().toLowerCase();
		formAttribute1.value = utils.getFormAttributes(testQuestion);
		formTemplate.attributes.put("question", formAttribute1);
		//
		formTemplate.tabTitle = "Вопрос " + templateTestQuestion.getNumber();
		formTemplate.template = templateTestQuestion.getClass().getSimpleName().toLowerCase();
		//
		FormAttribute formAttribute = new FormAttribute();
		formAttribute.name = "answers";
		formAttribute.title = "ответы";
		formAttribute.type = "answers";
		//
		ArrayList answers = new ArrayList();
		for (TemplateTestAnswer templateTestAnswer : templateTestAnswerRepository.findByTemplateTestQuestionId(templateTestQuestion.getId())){
			answers.add(getFormAnswer(templateTestAnswer, testAnswerRepository.findByTestVariantPersonIdAndTestQuestionNumberAndTestAnswerNumber(testVariantPerson.getId(), templateTestQuestion.getNumber(), templateTestAnswer.getNumber())));
		}
		//
		formAttribute.value = answers;
		//
		formTemplate.attributes.put("answers", formAttribute);
		return formTemplate;
	}

	public boolean isCorrectQuestion(TestQuestion testQuestion){
		TemplateTestQuestion templateTestQuestion = templateTestQuestionRepository.findByTemplateTestVariantIdAndNumber(testQuestion.getTestVariantPerson().getTemplateTestVariant().getId(), testQuestion.getNumber()).iterator().next();
		for (TemplateTestAnswer templateTestAnswer : templateTestAnswerRepository.findByTemplateTestQuestionId(templateTestQuestion.getId())){
			TestAnswer selectedAnswer = testAnswerRepository.findByTestVariantPersonIdAndTestQuestionNumberAndTestAnswerNumber(testQuestion.getTestVariantPerson().getId(), testQuestion.getNumber(), templateTestAnswer.getNumber());
			if (templateTestAnswer.getCorrect()){
				if (selectedAnswer == null){
					return false;
				}
			}else{
				if (selectedAnswer != null){
					return false;
				}
			}
		}
		return true;
	}

	public void setTestVariantResult(TestVariantPerson testVariantPerson){
		long correct = 0;
		long incorrect = 0;
		long score = 0;
		long availableScore = 0;

		for (TestQuestion testQuestion : testQuestionRepository.findByTestVariantPersonId(testVariantPerson.getId())){
			TemplateTestQuestion templateTestQuestion = templateTestQuestionRepository.findByTemplateTestVariantIdAndNumber(testVariantPerson.getTemplateTestVariant().getId(), testQuestion.getNumber()).iterator().next();
			availableScore = availableScore + templateTestQuestion.getScorePositive();
			if (isCorrectQuestion(testQuestion)){
				correct++;
				score = score + templateTestQuestion.getScorePositive();
			}else{
				incorrect++;
				score = score - templateTestQuestion.getScoreNegative();
			}
		}

		testVariantPerson.setCorrectAnswerCount(correct);
		testVariantPerson.setIncorrectAnswerCount(incorrect);
		testVariantPerson.setTotalScore(score);
		testVariantPerson.setAvailableScore(availableScore);

	}

	@PostMapping(value = "form/trainingtestvariant/{id}/change_variant/{variantId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity changeVariant(@PathVariable long id, @PathVariable Long variantId) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TestVariantPerson updateTestVariantPerson = testVariantPersonRepository.findByTestVariantPersonId(id);
		if (!accessUtils.canEditCard(updateTestVariantPerson)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!(updateTestVariantPerson.getStatus().getCode().equals("draft") || updateTestVariantPerson.getStatus().getCode().equals("assigned"))){
			throw new Exception("Некорректный статус [class = " + updateTestVariantPerson.getClass().getSimpleName().toLowerCase() + ", id = " + updateTestVariantPerson.getId() + "]");
		}
		if (updateTestVariantPerson.getTemplateTestVariant().getId().equals(variantId)){
			throw new Exception("Выбранный вариант уже назначен выбранному пользователю");
		}
		TemplateTestVariant newTemplateTestVariant = templateTestVariantRepository.findByTemplateTestVariantId(variantId);
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(TestVariantPerson.class.getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		Test test = updateTestVariantPerson.getTest();
		TestVariantPerson savedTestVariantPerson = formTestRestController.createTestVariant(test, updateTestVariantPerson.getPerson(), newTemplateTestVariant, lifeCycle);
		//
		if (updateTestVariantPerson.getStatus().getCode().equals("assigned")){
			Move move1 = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(TestVariantPerson.class.getSimpleName().toLowerCase(), statusRepository.findByStatusCode("draft").getId(), statusRepository.findByStatusCode("assigned").getId());
			savedTestVariantPerson.setStatus(move1.getToStatus());
			savedTestVariantPerson.setDateStart(new Date());
			savedTestVariantPerson = testVariantPersonRepository.save(savedTestVariantPerson);
		}
		//
		for (TestQuestion testQuestion : testQuestionRepository.findByTestVariantPersonId(updateTestVariantPerson.getId())){
			testQuestionRepository.delete(testQuestion);
		}
		testVariantPersonRepository.delete(updateTestVariantPerson);
		formTemplate = getFormByIdentifier(savedTestVariantPerson.getId().toString(), true);
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(formTemplate);
	}

	@PostMapping(value = "form/trainingtestvariant/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TestVariantPerson updateTestVariantPerson = testVariantPersonRepository.findByTestVariantPersonId(id);
		//if (!accessUtils.canEditCard(updateTestVariantPerson)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updateTestVariantPerson.getClass().getSimpleName().toLowerCase(), updateTestVariantPerson.getStatus().getId(), toStatus.getId());
		TestVariantPerson savedTestVariantPerson = null;
		if (move != null){
			if (move.getFromStatus().getCode().equals("assigned") && move.getToStatus().getCode().equals("in_progress")){
				updateTestVariantPerson.setStatus(move.getToStatus());
				updateTestVariantPerson.setDateStart(new Date());
			}else if ((move.getFromStatus().getCode().equals("assigned") || move.getFromStatus().getCode().equals("in_progress")) && (move.getToStatus().getCode().equals("canceled") || move.getToStatus().getCode().equals("completed") || move.getToStatus().getCode().equals("expired"))){
				updateTestVariantPerson.setStatus(move.getToStatus());
				updateTestVariantPerson.setDateEnd(new Date());
				if (move.getToStatus().getCode().equals("completed")){
					setTestVariantResult(updateTestVariantPerson);
				}
			}
			savedTestVariantPerson = testVariantPersonRepository.save(updateTestVariantPerson);
			if (move.getToStatus().getCode().equals("canceled") || move.getToStatus().getCode().equals("completed") || move.getToStatus().getCode().equals("expired")){
				moveUtils.processTrainingParents(savedTestVariantPerson);
			}
		}else{
			if (updateTestVariantPerson.getStatus().getCode().equals("in_progress") && updateTestVariantPerson.getDateStart() != null){
				savedTestVariantPerson = updateTestVariantPerson;
			}
		}
		formTemplate = getFormByIdentifier(savedTestVariantPerson.getId().toString(), true);
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(formTemplate);
	}

	@PostMapping(value = "form/trainingtestvariant/completedtesting/{templateTestQuestionId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatusCompleteTesting(@PathVariable long id, @PathVariable long templateTestQuestionId, @RequestBody List<AnswerContainer> answerContainerList) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TestVariantPerson testVariantPerson = testVariantPersonRepository.findByTestVariantPersonId(id);
		if (!accessUtils.canEditCard(testVariantPerson)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!testVariantPerson.getTest().getStatus().getCode().equals("in_progress")){
			throw new Exception("Некорректный статус [class = " + testVariantPerson.getTest().getClass().getSimpleName().toLowerCase() + ", id = " + testVariantPerson.getTest().getId() + "]");
		}
		//
		TemplateTestQuestion templateTestQuestion = templateTestQuestionRepository.findByTemplateTestQuestionId(templateTestQuestionId);
		if (testVariantPerson.getTest().getTemplateTest().getByOrder()){
			//Nothing
		}else if (testVariantPerson.getTest().getTemplateTest().getDeprecateChangeAnswerCount() > 0){
			TestQuestion testQuestion = testQuestionRepository.findByTestVariantPersonIdAndTestQuestionNumber(testVariantPerson.getId(), templateTestQuestion.getNumber());
			if (testQuestion.getCurrentDeprecateChangeAnswerCounter() <= 0){
				throw new Exception("Некорректные данные");
			}
		}
		saveAnswerByAnswerContainer(answerContainerList, templateTestQuestion, testVariantPerson);
		//
		toStatus(id, "completed");
		formTemplate = getFormByIdentifier(testVariantPerson.getId().toString(), false);
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(formTemplate);
	}

	public void saveAnswerByAnswerContainer(List<AnswerContainer> answerContainers, TemplateTestQuestion templateTestQuestion, TestVariantPerson testVariantPerson) throws Exception {
		for (AnswerContainer answerContainer : answerContainers){
			if (!templateTestQuestion.getId().equals(answerContainer.questionId)){
				throw new Exception("Некорректная структура данных");
			}
		}
		TestQuestion testQuestion = testQuestionRepository.findByTestVariantPersonIdAndTestQuestionNumber(testVariantPerson.getId(), templateTestQuestion.getNumber());
		Set<TestAnswer> testAnswerSet = testAnswerRepository.findByTestVariantPersonIdAndTestQuestionNumber(testVariantPerson.getId(), templateTestQuestion.getNumber());
		for (TestAnswer testAnswer : testAnswerSet){
			testAnswerRepository.delete(testAnswer);
		}
		if (answerContainers.size() > 0){
			HashMap<Long, Long> answerMap = utils.answerContainerToMap(answerContainers);
			for (Long answerId : answerMap.values()){
				TemplateTestAnswer templateTestAnswer = templateTestAnswerRepository.findByTemplateTestAnswerId(answerId);
				TestAnswer saveTestAnswer = new TestAnswer();
				saveTestAnswer.setNumber(templateTestAnswer.getNumber());
				saveTestAnswer.setTestQuestion(testQuestion);
				testAnswerRepository.save(saveTestAnswer);
			}
		}
	}

	@PostMapping(value = "form/trainingtestvariant/{testVariantPersonId}/{templateTestQuestionId}/answer", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity saveAnswer(@PathVariable long testVariantPersonId, @PathVariable long templateTestQuestionId, @RequestBody final TestContainer testContainer) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TestVariantPerson testVariantPerson = testVariantPersonRepository.findByTestVariantPersonId(testVariantPersonId);
		if (!accessUtils.canEditCard(testVariantPerson)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!testVariantPerson.getTest().getStatus().getCode().equals("in_progress")){
			throw new Exception("Некорректный статус [class = " + testVariantPerson.getTest().getClass().getSimpleName().toLowerCase() + ", id = " + testVariantPerson.getTest().getId() + "]");
		}
		TemplateTestQuestion templateTestQuestion = templateTestQuestionRepository.findByTemplateTestQuestionId(templateTestQuestionId);
		TestQuestion testQuestion = testQuestionRepository.findByTestVariantPersonIdAndTestQuestionNumber(testVariantPerson.getId(), templateTestQuestion.getNumber());
		if (testVariantPerson.getTemplateTestVariant().getTemplateTest().getByOrder()){
			if (!testVariantPerson.getCurrentTemplateTestQuestion().getId().equals(templateTestQuestion.getId())){
				return ResponseEntity
						.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.build();
			}
		}else if (testVariantPerson.getTemplateTestVariant().getTemplateTest().getDeprecateChangeAnswerCount() > 0){
			if (testQuestion.getCurrentDeprecateChangeAnswerCounter() == 0){
				return ResponseEntity
						.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.build();
			}
		}
		//
		saveAnswerByAnswerContainer(testContainer.answerContainers, templateTestQuestion, testVariantPerson);
		//
		if (testQuestion.getTestVariantPerson().getTest().getTemplateTest().getByOrder()){
			//Nothing
		} else if (testQuestion.getTestVariantPerson().getTest().getTemplateTest().getDeprecateChangeAnswerCount() > 0){
			if (testQuestion.getCurrentDeprecateChangeAnswerCounter().equals(testQuestion.getTestVariantPerson().getTemplateTestVariant().getTemplateTest().getDeprecateChangeAnswerCount())){
				if (testQuestion.getVisited()){
					testQuestion.setCurrentDeprecateChangeAnswerCounter(testContainer.deprecateChangeAnswerCount);
				}else{
					testQuestion.setVisited(true);
				}
			}else{
				testQuestion.setCurrentDeprecateChangeAnswerCounter(testContainer.deprecateChangeAnswerCount);
			}
			testQuestionRepository.save(testQuestion);
		}
		TemplateTestQuestion templateTestQuestion1 = templateTestQuestionRepository.findByTemplateTestQuestionId(testContainer.clickedQuestionId);
		testVariantPerson.setCurrentTemplateTestQuestion(templateTestQuestion1);
		testVariantPersonRepository.save(testVariantPerson);
		//
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.build();
	}

}