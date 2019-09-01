package com.dls.base.controller.form;

import com.dls.base.entity.Person;
import com.dls.base.entity.TemplateTestAnswer;
import com.dls.base.entity.TemplateTestQuestion;
import com.dls.base.repository.PersonRepository;
import com.dls.base.repository.TemplateTestAnswerRepository;
import com.dls.base.repository.TemplateTestQuestionRepository;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.TemplateTestAnswerValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class FormTemplateTestAnswerRestController
{

	private final TemplateTestAnswerRepository templateTestAnswerRepository;
	private final PersonRepository personRepository;
	private final TemplateTestQuestionRepository templateTestQuestionRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	TemplateTestAnswerValidator templateTestAnswerValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
    FormTemplateTestAnswerRestController(TemplateTestAnswerRepository templateTestAnswerRepository, PersonRepository personRepository, TemplateTestQuestionRepository templateTestQuestionRepository) {
		this.templateTestAnswerRepository = templateTestAnswerRepository;
		this.personRepository = personRepository;
		this.templateTestQuestionRepository = templateTestQuestionRepository;
	}

	@GetMapping(value = "form/templatetestanswer/templatetestquestion/{templateTestQuestionId}/0", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getNewFormByIdentifier(@PathVariable Long templateTestQuestionId) {
		TemplateTestAnswer templateTestAnswer;
		templateTestAnswer = new TemplateTestAnswer();
		templateTestAnswer.setNumber(templateTestAnswerRepository.findLastPositionByTemplateTestQuestionId(templateTestQuestionId) + 1);
		templateTestAnswer.setContent("");
		templateTestAnswer.setCorrect(false);
		templateTestAnswer.setAuthor(accessUtils.getCurrentPerson());
		FormTemplate formTemplate = getForm(templateTestAnswer);
		return formTemplate;
	}

	@GetMapping(value = "form/templatetestanswer/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		TemplateTestAnswer templateTestAnswer;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			templateTestAnswer = new TemplateTestAnswer();
			templateTestAnswer.setNumber(0L);
			templateTestAnswer.setContent("");
			templateTestAnswer.setCorrect(false);
			templateTestAnswer.setAuthor(accessUtils.getCurrentPerson());
		}else{
			templateTestAnswer = templateTestAnswerRepository.findByTemplateTestAnswerId(id);
			if (!accessUtils.canReadCard(templateTestAnswer)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(templateTestAnswer);
		return formTemplate;
	}

	public FormTemplate getForm(TemplateTestAnswer templateTestAnswer) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templateTestAnswer);
		formTemplate.tabTitle = templateTestAnswer.getNumber().toString();
		formTemplate.template = templateTestAnswer.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/templatetestanswer/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody TemplateTestAnswer templateTestAnswer, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		templateTestAnswer.setAuthor(personRepository.findByPersonId(templateTestAnswer.getAuthor().getId()));
		templateTestAnswer.setTemplateTestQuestion(templateTestQuestionRepository.findByTemplateTestQuestionId(templateTestAnswer.getTemplateTestQuestion().getId()));
		if (!accessUtils.canEditCard(templateTestAnswer)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateTestAnswer.getTemplateTestQuestion().getTemplateTestVariant().getTemplateTest().getStatus().getCode().equals("draft")){
			throw new Exception("incorrect status for [class = " + templateTestAnswer.getTemplateTestQuestion().getTemplateTestVariant().getTemplateTest().getClass().getSimpleName().toLowerCase() + ", id = " + templateTestAnswer.getTemplateTestQuestion().getTemplateTestVariant().getTemplateTest().getId() + "]");
		}
		templateTestAnswerValidator.validate(templateTestAnswer, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateTestAnswer newTemplateTestVariant = new TemplateTestAnswer();
		newTemplateTestVariant.setNumber(templateTestAnswer.getNumber());
		newTemplateTestVariant.setContent(templateTestAnswer.getContent());
		newTemplateTestVariant.setCorrect(templateTestAnswer.getCorrect());
		newTemplateTestVariant.setAuthor(templateTestAnswer.getAuthor());
		newTemplateTestVariant.setTemplateTestQuestion(templateTestAnswer.getTemplateTestQuestion());
		TemplateTestAnswer savedTemplateTestVariant = templateTestAnswerRepository.save(newTemplateTestVariant);
		formTemplate = getFormByIdentifier(savedTemplateTestVariant.getId().toString());
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

	@PostMapping(value = "form/templatetestanswer/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody TemplateTestAnswer templateTestAnswer, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateTestAnswer updateTemplateTestAnswer = templateTestAnswerRepository.findByTemplateTestAnswerId(id);
		updateTemplateTestAnswer.setNumber(templateTestAnswer.getNumber());
		updateTemplateTestAnswer.setContent(templateTestAnswer.getContent());
		updateTemplateTestAnswer.setCorrect(templateTestAnswer.getCorrect());
		Person author = personRepository.findByPersonId(templateTestAnswer.getAuthor().getId());
		updateTemplateTestAnswer.setAuthor(author);
		TemplateTestQuestion parent = templateTestQuestionRepository.findByTemplateTestQuestionId(templateTestAnswer.getTemplateTestQuestion().getId());
		updateTemplateTestAnswer.setTemplateTestQuestion(parent);
		if (!accessUtils.canEditCard(updateTemplateTestAnswer)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!updateTemplateTestAnswer.getTemplateTestQuestion().getTemplateTestVariant().getTemplateTest().getStatus().getCode().equals("draft")){
			throw new Exception("incorrect status for [class = " + updateTemplateTestAnswer.getTemplateTestQuestion().getTemplateTestVariant().getTemplateTest().getClass().getSimpleName().toLowerCase() + ", id = " + updateTemplateTestAnswer.getTemplateTestQuestion().getTemplateTestVariant().getTemplateTest().getId() + "]");
		}
		templateTestAnswerValidator.validate(updateTemplateTestAnswer, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateTestAnswer savedTemplateTestVariant = templateTestAnswerRepository.save(updateTemplateTestAnswer);
		formTemplate = getFormByIdentifier(savedTemplateTestVariant.getId().toString());
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