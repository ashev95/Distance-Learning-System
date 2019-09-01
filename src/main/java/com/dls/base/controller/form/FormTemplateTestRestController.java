package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.TemplateTestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Set;

@RestController
public class FormTemplateTestRestController
{

	private final TemplateTestRepository templateTestRepository;
	private final PersonRepository personRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final MoveRepository moveRepository;
	private final StatusRepository statusRepository;
	private final ResourceRepository resourceRepository;
	private final TemplateTestVariantRepository templateTestVariantRepository;
	private final TemplateTestQuestionRepository templateTestQuestionRepository;
	private final TemplateTestAnswerRepository templateTestAnswerRepository;
	private final CategoryRepository categoryRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	TemplateTestValidator templateTestValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	FormTemplateTestRestController(TemplateTestRepository templateTestRepository, PersonRepository personRepository, TemplateLifeCycleRepository templateLifeCycleRepository, MoveRepository moveRepository, StatusRepository statusRepository, ResourceRepository resourceRepository, TemplateTestVariantRepository templateTestVariantRepository, TemplateTestQuestionRepository templateTestQuestionRepository, TemplateTestAnswerRepository templateTestAnswerRepository, CategoryRepository categoryRepository) {
		this.templateTestRepository = templateTestRepository;
		this.personRepository = personRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.moveRepository = moveRepository;
		this.statusRepository = statusRepository;
		this.resourceRepository = resourceRepository;
		this.templateTestVariantRepository = templateTestVariantRepository;
		this.templateTestQuestionRepository = templateTestQuestionRepository;
		this.templateTestAnswerRepository = templateTestAnswerRepository;
		this.categoryRepository = categoryRepository;
	}

	@GetMapping(value = "form/templatetest/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		TemplateTest templateTest;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			templateTest = new TemplateTest();
			templateTest.setAuthor(accessUtils.getCurrentPerson());
			templateTest.setByOrder(false);
			templateTest.setDeprecateChangeAnswerCount(0L);
			templateTest.setTimeLimit(0L);
			TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(templateTest.getClass().getSimpleName().toLowerCase());
			LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
			templateTest.setStatus(lifeCycle.getInitStatus());
		}else{
			templateTest = templateTestRepository.findByTemplateTestId(id);
			if (!accessUtils.canReadCard(templateTest)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(templateTest);
		return formTemplate;
	}

	@GetMapping(value = "form/templatetest/children/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getChildFormByIdentifier(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		Set<TemplateTest> templateTestSet = templateTestRepository.findByParentId(id);
		if (templateTestSet.size() > 0){
			TemplateTest templateTest = templateTestSet.iterator().next();
			if (!accessUtils.canReadCard(templateTest)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
			return getForm(templateTest);
		}else{
			return null;
		}
	}

	public FormTemplate getForm(TemplateTest templateTest) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templateTest);
		formTemplate.tabTitle = templateTest.getName();
		formTemplate.template = templateTest.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/templatetest/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody TemplateTest templateTest, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		templateTest.setAuthor(personRepository.findByPersonId(templateTest.getAuthor().getId()));
		templateTest.setStatus(statusRepository.findByStatusId(templateTest.getStatus().getId()));
		templateTest.setCategory(categoryRepository.findByCategoryId(templateTest.getCategory().getId()));
		if (!accessUtils.canEditCard(templateTest)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateTest.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + templateTest.getClass().getSimpleName().toLowerCase() + ", id = " + templateTest.getId() + "]");
		}
		templateTestValidator.validate(templateTest, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateTest newTemplateTest = new TemplateTest();
		newTemplateTest.setDescription(templateTest.getDescription());
		newTemplateTest.setStatus(statusRepository.findByStatusId(templateTest.getStatus().getId()));
		newTemplateTest.setCategory(templateTest.getCategory());
		newTemplateTest.setName(templateTest.getName());
		newTemplateTest.setVersion(1L);
		newTemplateTest.setAuthor(templateTest.getAuthor());
		newTemplateTest.setDateCreate(new Date());
		if (templateTest.getByOrder()){
			newTemplateTest.setByOrder(templateTest.getByOrder());
		}else{
			newTemplateTest.setByOrder(templateTest.getByOrder());
			newTemplateTest.setDeprecateChangeAnswerCount(templateTest.getDeprecateChangeAnswerCount());
		}
		newTemplateTest.setTimeLimit(templateTest.getTimeLimit());
		TemplateTest savedTemplateTest = templateTestRepository.save(newTemplateTest);
		formTemplate = getFormByIdentifier(savedTemplateTest.getId().toString());
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

	@PutMapping(value = "form/templatetest/new_version/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createFormVersion(@PathVariable long id) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateTest templateTest = templateTestRepository.findByTemplateTestId(id);
		if (!accessUtils.canEditCard(templateTest)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (templateTestRepository.findByParentId(templateTest.getId()).size() > 0){
			throw new Exception("Карточка с новой версией шаблона уже существует");
		}
		if (!templateTest.getStatus().getCode().equals("active")){
			throw new Exception("Некорректный статус [class = " + templateTest.getClass().getSimpleName().toLowerCase() + ", id = " + templateTest.getId() + "]");
		}
		TemplateTest newTemplateTest = new TemplateTest();
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(templateTest.getClass().getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		newTemplateTest.setStatus(lifeCycle.getInitStatus());
		newTemplateTest.setCategory(templateTest.getCategory());
		newTemplateTest.setName(templateTest.getName());
		newTemplateTest.setDescription(templateTest.getDescription());
		newTemplateTest.setParent(templateTest);
		newTemplateTest.setVersion(templateTest.getVersion() + 1);
		newTemplateTest.setAuthor(accessUtils.getCurrentPerson());
		newTemplateTest.setDateCreate(new Date());
		newTemplateTest.setByOrder(templateTest.getByOrder());
		newTemplateTest.setDeprecateChangeAnswerCount(templateTest.getDeprecateChangeAnswerCount());
		newTemplateTest.setTimeLimit(templateTest.getTimeLimit());
		TemplateTest savedTemplateTest = templateTestRepository.save(newTemplateTest);
		for (TemplateTestVariant templateTestVariant : templateTestVariantRepository.findByTemplateTestId(templateTest.getId())){
			TemplateTestVariant newTemplateTestVariant = new TemplateTestVariant();
			newTemplateTestVariant.setTemplateTest(savedTemplateTest);
			newTemplateTestVariant.setAuthor(accessUtils.getCurrentPerson());
			newTemplateTestVariant.setNumber(templateTestVariant.getNumber());
			TemplateTestVariant savedNewTemplateTestVariant = templateTestVariantRepository.save(newTemplateTestVariant);
			for (TemplateTestQuestion templateTestQuestion : templateTestQuestionRepository.findByTemplateTestVariantId(templateTestVariant.getId())){
				TemplateTestQuestion newTemplateTestQuestion = new TemplateTestQuestion();
				newTemplateTestQuestion.setTemplateTestVariant(savedNewTemplateTestVariant);
				newTemplateTestQuestion.setAuthor(accessUtils.getCurrentPerson());
				newTemplateTestQuestion.setNumber(templateTestQuestion.getNumber());
				newTemplateTestQuestion.setContent(templateTestQuestion.getContent());
				newTemplateTestQuestion.setScorePositive(templateTestQuestion.getScorePositive());
				newTemplateTestQuestion.setScoreNegative(templateTestQuestion.getScoreNegative());
				TemplateTestQuestion savedNewTemplateTestQuestion = templateTestQuestionRepository.save(newTemplateTestQuestion);
				for (Resource resource : resourceRepository.findByEntityClassAndEntityId(templateTestQuestion.getClass().getSimpleName().toLowerCase(), templateTestQuestion.getId())){
					Resource newResource = utils.getResourceClone(resource);
					newResource.setEntityId(savedNewTemplateTestQuestion.getId());
					newResource.setEntityClass(savedNewTemplateTestQuestion.getClass().getSimpleName().toLowerCase());
					resourceRepository.save(newResource);
				}
				for (TemplateTestAnswer templateTestAnswer : templateTestAnswerRepository.findByTemplateTestQuestionId(templateTestQuestion.getId())){
					TemplateTestAnswer newTemplateTestAnswer = new TemplateTestAnswer();
					newTemplateTestAnswer.setTemplateTestQuestion(savedNewTemplateTestQuestion);
					newTemplateTestAnswer.setCorrect(templateTestAnswer.getCorrect());
					newTemplateTestAnswer.setAuthor(accessUtils.getCurrentPerson());
					newTemplateTestAnswer.setContent(templateTestAnswer.getContent());
					newTemplateTestAnswer.setNumber(templateTestAnswer.getNumber());
					TemplateTestAnswer savedNewTemplateTestAnswer = templateTestAnswerRepository.save(newTemplateTestAnswer);
				}
			}
		}
		formTemplate = getFormByIdentifier(savedTemplateTest.getId().toString());
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

	@PostMapping(value = "form/templatetest/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody TemplateTest templateTest, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateTest updateTemplateTest = templateTestRepository.findByTemplateTestId(id);
		updateTemplateTest.setName(templateTest.getName());
		updateTemplateTest.setDescription(templateTest.getDescription());
		Person author = personRepository.findByPersonId(templateTest.getAuthor().getId());
		updateTemplateTest.setAuthor(author);
		templateTest.setStatus(statusRepository.findByStatusId(templateTest.getStatus().getId()));
		templateTest.setCategory(categoryRepository.findByCategoryId(templateTest.getCategory().getId()));
		updateTemplateTest.setCategory(templateTest.getCategory());
		if (!accessUtils.canEditCard(updateTemplateTest)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateTest.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + templateTest.getClass().getSimpleName().toLowerCase() + ", id = " + templateTest.getId() + "]");
		}
		templateTestValidator.validate(updateTemplateTest, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		if (templateTest.getByOrder()){
			updateTemplateTest.setByOrder(templateTest.getByOrder());
		}else{
			updateTemplateTest.setByOrder(templateTest.getByOrder());
			updateTemplateTest.setDeprecateChangeAnswerCount(templateTest.getDeprecateChangeAnswerCount());
		}
		updateTemplateTest.setTimeLimit(templateTest.getTimeLimit());
		TemplateTest savedTemplateTest = templateTestRepository.save(updateTemplateTest);
		formTemplate = getFormByIdentifier(savedTemplateTest.getId().toString());
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

	@PostMapping(value = "form/templatetest/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateTest updateTemplateTest = templateTestRepository.findByTemplateTestId(id);
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updateTemplateTest.getClass().getSimpleName().toLowerCase(), updateTemplateTest.getStatus().getId(), toStatus.getId());
		updateTemplateTest.setStatus(move.getToStatus());
		//if (!accessUtils.canEditCard(updateTemplateTest)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("active")){
			utils.checkTemplate(updateTemplateTest, true);
			if (updateTemplateTest.getParent() != null){
				toStatus(updateTemplateTest.getParent().getId(), "duplicate");
			}
		}
		TemplateTest savedTemplateTest = templateTestRepository.save(updateTemplateTest);
		formTemplate = getFormByIdentifier(savedTemplateTest.getId().toString());
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

}