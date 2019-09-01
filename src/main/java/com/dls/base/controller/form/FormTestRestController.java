package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.request.PersonVariantContainerConfirmed;
import com.dls.base.request.PersonVariantContainerItemConfirmed;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.ArrayListBuilder;
import com.dls.base.utils.MoveUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.TestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.dls.base.utils.Constant.ROLE_ADMINISTRATOR;

@RestController
public class FormTestRestController
{

	private final TestRepository testRepository;
	private final PersonRepository personRepository;
	private final LifeCycleRepository lifeCycleRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final TemplateTestRepository templateTestRepository;
	private final MoveRepository moveRepository;
	private final StatusRepository statusRepository;
	private final GroupRepository groupRepository;
	private final GroupPersonRepository groupPersonRepository;
	private final TestVariantPersonRepository testVariantPersonRepository;
	private final TemplateTestVariantRepository templateTestVariantRepository;
	private final TemplateTestQuestionRepository templateTestQuestionRepository;
	private final TemplateTestAnswerRepository templateTestAnswerRepository;
	private final TestQuestionRepository testQuestionRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	MoveUtils moveUtils;

	@Autowired
	TestValidator testValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
    FormTestRestController(TestRepository testRepository, PersonRepository personRepository, LifeCycleRepository lifeCycleRepository, TemplateLifeCycleRepository templateLifeCycleRepository, TemplateTestRepository templateTestRepository, MoveRepository moveRepository, StatusRepository statusRepository, GroupRepository groupRepository, GroupPersonRepository groupPersonRepository, TestVariantPersonRepository testVariantPersonRepository, TemplateTestVariantRepository templateTestVariantRepository, TemplateTestQuestionRepository templateTestQuestionRepository, TemplateTestAnswerRepository templateTestAnswerRepository, TestQuestionRepository testQuestionRepository) {
		this.testRepository = testRepository;
		this.personRepository = personRepository;
		this.lifeCycleRepository = lifeCycleRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.templateTestRepository= templateTestRepository;
		this.moveRepository = moveRepository;
		this.statusRepository = statusRepository;
		this.groupRepository = groupRepository;
		this.groupPersonRepository = groupPersonRepository;
		this.testVariantPersonRepository = testVariantPersonRepository;
		this.templateTestVariantRepository = templateTestVariantRepository;
		this.templateTestQuestionRepository = templateTestQuestionRepository;
		this.templateTestAnswerRepository = templateTestAnswerRepository;
		this.testQuestionRepository = testQuestionRepository;
	}

	@GetMapping(value = "form/test/templatetest/{templateTestId}/0", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getNewFormByIdentifier(@PathVariable Long templateTestId) {
		Test test = new Test();
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(test.getClass().getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		test.setTemplateTest(templateTestRepository.findByTemplateTestId(templateTestId));
		test.setStatus(lifeCycle.getInitStatus());
		test.setCurator(accessUtils.getCurrentPerson());
		test.setAuthor(accessUtils.getCurrentPerson());
		FormTemplate formTemplate = getForm(test);
		return formTemplate;
	}

	@GetMapping(value = "form/test/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable Long id) throws Exception {
		Test test = testRepository.findByTestId(id);
		if (!accessUtils.canReadCard(test)){
			throw new Exception("Отсутствуют права на чтение карточки");
		}
		FormTemplate formTemplate = getForm(test);
		return formTemplate;
	}

	public FormTemplate getForm(Test test) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(test);
		formTemplate.tabTitle = test.getName();
		formTemplate.template = test.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/test/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody Test test, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		test.setTemplateTest(templateTestRepository.findByTemplateTestId(test.getTemplateTest().getId()));
		test.setGroup(groupRepository.findByGroupId(test.getGroup().getId()));
		test.setStatus(statusRepository.findByStatusId(test.getStatus().getId()));
		test.setCurator(personRepository.findByPersonId(test.getCurator().getId()));
		test.setAuthor(personRepository.findByPersonId(test.getAuthor().getId()));
		if (!accessUtils.canEditCard(test)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!test.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + test.getClass().getSimpleName().toLowerCase() + ", id = " + test.getId() + "]");
		}
		testValidator.validate(test, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Test newTest = new Test();
		newTest.setName(test.getName());
		newTest.setDescription(test.getDescription());
		newTest.setTemplateTest(test.getTemplateTest());
		newTest.setStatus(test.getStatus());
		newTest.setGroup(test.getGroup());
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			newTest.setCurator(test.getCurator());
		}else{
			newTest.setCurator(accessUtils.getCurrentPerson());
		}
		newTest.setAuthor(test.getAuthor());
		newTest.setDateCreate(new Date());
		Test savedTest = testRepository.save(newTest);
		//
		createTestVariantFull(savedTest);
		//
		formTemplate = getFormByIdentifier(savedTest.getId());
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

	@PostMapping(value = "form/test/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody Test test, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		Test updateTest = testRepository.findByTestId(id);
		updateTest.setName(test.getName());
		updateTest.setDescription(test.getDescription());
		Person curator = personRepository.findByPersonId(test.getCurator().getId());
		updateTest.setCurator(curator);
		Person author = personRepository.findByPersonId(test.getAuthor().getId());
		updateTest.setAuthor(author);
		Status status = statusRepository.findByStatusId(test.getStatus().getId());
		updateTest.setStatus(status);
		Group oldGroup = updateTest.getGroup();
		Group group = groupRepository.findByGroupId(test.getGroup().getId());
		updateTest.setGroup(group);
		test.setStatus(statusRepository.findByStatusId(test.getStatus().getId()));
		if (!accessUtils.canEditCard(updateTest)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!test.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + test.getClass().getSimpleName().toLowerCase() + ", id = " + test.getId() + "]");
		}
		testValidator.validate(updateTest, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Test savedTest = testRepository.save(updateTest);
		//
		if (!oldGroup.getId().equals(updateTest.getGroup().getId())){
			reCreateTestVariantFull(savedTest);
		}
		//
		formTemplate = getFormByIdentifier(savedTest.getId());
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

	public void reCreateTestVariantFull(Test test){
		removeTestVariantFull(test);
		createTestVariantFull(test);
	}

	public void removeTestVariantFull(Test test){
		for (TestVariantPerson testVariantPerson : testVariantPersonRepository.findByTestId(test.getId())){
			for (TestQuestion testQuestion : testQuestionRepository.findByTestVariantPersonId(testVariantPerson.getId())){
				testQuestionRepository.delete(testQuestion);
			}
			testVariantPersonRepository.delete(testVariantPerson);
		}
	}

	public void createTestVariantFull(Test test){
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(TestVariantPerson.class.getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		Set<TemplateTestVariant> templateTestVariantSet = templateTestVariantRepository.findByTemplateTestId(test.getTemplateTest().getId());
		Iterator iterator = templateTestVariantSet.iterator();
		for (GroupPerson groupPerson : groupPersonRepository.findByGroupId(test.getGroup().getId())){
			TemplateTestVariant templateTestVariant = (TemplateTestVariant)iterator.next();
			createTestVariant(test, groupPerson.getPerson(), templateTestVariant, lifeCycle);
			if (!iterator.hasNext()){
				iterator = templateTestVariantSet.iterator();
			}
		}
	}

	public TestVariantPerson createTestVariant(Test test, Person person, TemplateTestVariant templateTestVariant, LifeCycle lifeCycle){
		TestVariantPerson testVariantPerson = new TestVariantPerson();
		testVariantPerson.setTest(test);
		testVariantPerson.setTemplateTestVariant(templateTestVariant);
		testVariantPerson.setPerson(person);
		testVariantPerson.setStatus(lifeCycle.getInitStatus());
		testVariantPersonRepository.save(testVariantPerson);
		//
		TemplateTestQuestion firstTemplateTestQuestion = null;
		for (TemplateTestQuestion templateTestQuestion : templateTestQuestionRepository.findByTemplateTestVariantId(testVariantPerson.getTemplateTestVariant().getId())){
			TestQuestion testQuestion = new TestQuestion();
			testQuestion.setTestVariantPerson(testVariantPerson);
			testQuestion.setCurrentDeprecateChangeAnswerCounter(testVariantPerson.getTemplateTestVariant().getTemplateTest().getDeprecateChangeAnswerCount());
			testQuestion.setNumber(templateTestQuestion.getNumber());
			testQuestion.setVisited(false);
			testQuestionRepository.save(testQuestion);
			if (firstTemplateTestQuestion == null){
				firstTemplateTestQuestion = templateTestQuestion;
			}
		}
		//
		testVariantPerson.setCurrentTemplateTestQuestion(firstTemplateTestQuestion);
		TestVariantPerson savedVariantPerson = testVariantPersonRepository.save(testVariantPerson);
		return savedVariantPerson;
	}

	@PostMapping(value = "form/test/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try {
		Test updateTest = testRepository.findByTestId(id);
		//if (!accessUtils.canEditCard(updateTest)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updateTest.getClass().getSimpleName().toLowerCase(), updateTest.getStatus().getId(), toStatus.getId());
		if (move != null){
			updateTest.setStatus(move.getToStatus());
			if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("in_progress")){
				utils.checkTemplate(updateTest, true);
				updateTest.setDateStart(new Date());
			}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("canceled")){
				updateTest.setDateEnd(new Date());
			}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("expired")){
				updateTest.setDateEnd(new Date());
			}else if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("canceled")){
				updateTest.setDateStart(new Date());
				updateTest.setDateEnd(new Date());
			}
			Test savedTest = testRepository.save(updateTest);
			if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("in_progress")){
				Move move1 = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(TestVariantPerson.class.getSimpleName().toLowerCase(), statusRepository.findByStatusCode("draft").getId(), statusRepository.findByStatusCode("assigned").getId());
				if (testVariantPersonRepository.findByTestId(updateTest.getId()).size() == 0){
					throw new Exception("Отсутствуют учащиеся");
				}
				for (TestVariantPerson testVariantPerson : testVariantPersonRepository.findByTestId(updateTest.getId())){
					if (testVariantPerson.getStatus().getCode().equals("draft")){
						testVariantPerson.setStatus(move1.getToStatus());
						//testVariantPerson.setDateStart(new Date());
						testVariantPersonRepository.save(testVariantPerson);
					}
				}
				//TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(TestVariantPerson.class.getSimpleName().toLowerCase());
				//LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
				/*
				for (PersonVariantContainerConfirmed personVariantContainerConfirmed : personVariantContainerConfirmedList){
					TemplateTestQuestion templateTestQuestion = templateTestQuestionRepository.findByTemplateTestQuestionId(personVariantContainerConfirmed.id);
					for (PersonVariantContainerItemConfirmed personVariantContainerItemConfirmed : personVariantContainerConfirmed.personIdToVariantNumber){
						createAndAssignTestVariant(updateTest, personRepository.findByPersonId(personVariantContainerItemConfirmed.personId), templateTestVariantRepository.findByTemplateTestIdAndNumber(updateTest.getTemplateTest().getId(), personVariantContainerItemConfirmed.variantNumber).iterator().next(), lifeCycle);
					}
				}
				 */
				/*
				Set<TemplateTestVariant> templateTestVariantSet = templateTestVariantRepository.findByTemplateTestId(savedTest.getTemplateTest().getId());
				Iterator iterator = templateTestVariantSet.iterator();
				for (GroupPerson groupPerson : groupPersonRepository.findByGroupId(updateTest.getGroup().getId())){
					TemplateTestVariant templateTestVariant = (TemplateTestVariant)iterator.next();
					createTestVariant(updateTest, groupPerson.getPerson(), templateTestVariant, lifeCycle);
					//
					if (!iterator.hasNext()){
						iterator = templateTestVariantSet.iterator();
					}
				}
				 */
			}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("canceled")){
				Move move1 = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(TestVariantPerson.class.getSimpleName().toLowerCase(), statusRepository.findByStatusCode("assigned").getId(), statusRepository.findByStatusCode("canceled").getId());
				for (TestVariantPerson testVariantPerson : testVariantPersonRepository.findByTestId(updateTest.getId())){
					if (testVariantPerson.getStatus().getCode().equals("assigned") || testVariantPerson.getStatus().getCode().equals("in_progress")){
						testVariantPerson.setStatus(move1.getToStatus());
						testVariantPerson.setDateEnd(new Date());
						testVariantPersonRepository.save(testVariantPerson);
					}
				}
			}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("expired")){
				Move move1 = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(TestVariantPerson.class.getSimpleName().toLowerCase(), statusRepository.findByStatusCode("assigned").getId(), statusRepository.findByStatusCode("canceled").getId());
				for (TestVariantPerson testVariantPerson : testVariantPersonRepository.findByTestId(updateTest.getId())){
					if (testVariantPerson.getStatus().getCode().equals("in_progress")){
						testVariantPerson.setStatus(move1.getToStatus());
						testVariantPerson.setDateEnd(new Date());
						testVariantPersonRepository.save(testVariantPerson);
					}
				}
			}
			if (move.getToStatus().getCode().equals("canceled") || move.getToStatus().getCode().equals("completed") || move.getToStatus().getCode().equals("expired")){
				moveUtils.processTrainingParents(savedTest);
			}
			formTemplate = getFormByIdentifier(savedTest.getId());
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(formTemplate);
		}
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return null;
	}

}