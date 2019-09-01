package com.dls.base.controller.form;

import com.dls.base.entity.Person;
import com.dls.base.entity.TemplateTestQuestion;
import com.dls.base.entity.TemplateTestVariant;
import com.dls.base.repository.PersonRepository;
import com.dls.base.repository.TemplateTestQuestionRepository;
import com.dls.base.repository.TemplateTestVariantRepository;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.TemplateTestQuestionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class FormTemplateTestQuestionRestController
{

	private final TemplateTestQuestionRepository templateTestQuestionRepository;
	private final PersonRepository personRepository;
	private final TemplateTestVariantRepository templateTestVariantRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	TemplateTestQuestionValidator templateTestQuestionValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
    FormTemplateTestQuestionRestController(TemplateTestQuestionRepository templateTestQuestionRepository, PersonRepository personRepository, TemplateTestVariantRepository templateTestVariantRepository) {
		this.templateTestQuestionRepository = templateTestQuestionRepository;
		this.personRepository = personRepository;
		this.templateTestVariantRepository = templateTestVariantRepository;
	}

	@GetMapping(value = "form/templatetestquestion/templatetestvariant/{templateTestVariantId}/0", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getNewFormByIdentifier(@PathVariable Long templateTestVariantId) {
		TemplateTestQuestion templateTestQuestion;
		templateTestQuestion = new TemplateTestQuestion();
		templateTestQuestion.setNumber(templateTestQuestionRepository.findLastPositionByTemplateTestVariantId(templateTestVariantId) + 1);
		templateTestQuestion.setContent("");
		templateTestQuestion.setScorePositive(0L);
		templateTestQuestion.setScoreNegative(0L);
		templateTestQuestion.setAuthor(accessUtils.getCurrentPerson());
		FormTemplate formTemplate = getForm(templateTestQuestion);
		return formTemplate;
	}

	@GetMapping(value = "form/templatetestquestion/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		TemplateTestQuestion templateTestQuestion;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			templateTestQuestion = new TemplateTestQuestion();
			templateTestQuestion.setNumber(0L);
			templateTestQuestion.setContent("");
			templateTestQuestion.setScorePositive(0L);
			templateTestQuestion.setScoreNegative(0L);
			templateTestQuestion.setAuthor(accessUtils.getCurrentPerson());
		}else{
			templateTestQuestion = templateTestQuestionRepository.findByTemplateTestQuestionId(id);
			if (!accessUtils.canReadCard(templateTestQuestion)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(templateTestQuestion);
		return formTemplate;
	}

	public FormTemplate getForm(TemplateTestQuestion templateTestQuestion) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templateTestQuestion);
		formTemplate.tabTitle = templateTestQuestion.getNumber().toString();
		formTemplate.template = templateTestQuestion.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/templatetestquestion/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody TemplateTestQuestion templateTestQuestion, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		templateTestQuestion.setAuthor(personRepository.findByPersonId(templateTestQuestion.getAuthor().getId()));
		templateTestQuestion.setTemplateTestVariant(templateTestVariantRepository.findByTemplateTestVariantId(templateTestQuestion.getTemplateTestVariant().getId()));
		if (!accessUtils.canEditCard(templateTestQuestion)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateTestQuestion.getTemplateTestVariant().getTemplateTest().getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + templateTestQuestion.getTemplateTestVariant().getTemplateTest().getClass().getSimpleName().toLowerCase() + ", id = " + templateTestQuestion.getTemplateTestVariant().getTemplateTest().getId() + "]");
		}
		templateTestQuestionValidator.validate(templateTestQuestion, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateTestQuestion newTemplateTestVariant = new TemplateTestQuestion();
		newTemplateTestVariant.setNumber(templateTestQuestion.getNumber());
		newTemplateTestVariant.setContent(templateTestQuestion.getContent());
		newTemplateTestVariant.setScorePositive(templateTestQuestion.getScorePositive());
		newTemplateTestVariant.setScoreNegative(templateTestQuestion.getScoreNegative());
		newTemplateTestVariant.setAuthor(templateTestQuestion.getAuthor());
		newTemplateTestVariant.setTemplateTestVariant(templateTestQuestion.getTemplateTestVariant());
		TemplateTestQuestion savedTemplateTestVariant = templateTestQuestionRepository.save(newTemplateTestVariant);
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

	@PostMapping(value = "form/templatetestquestion/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody TemplateTestQuestion templateTestQuestion, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateTestQuestion updateTemplateTestQuestion = templateTestQuestionRepository.findByTemplateTestQuestionId(id);
		updateTemplateTestQuestion.setNumber(templateTestQuestion.getNumber());
		updateTemplateTestQuestion.setScorePositive(templateTestQuestion.getScorePositive());
		updateTemplateTestQuestion.setScoreNegative(templateTestQuestion.getScoreNegative());
		updateTemplateTestQuestion.setContent(templateTestQuestion.getContent());
		Person author = personRepository.findByPersonId(templateTestQuestion.getAuthor().getId());
		updateTemplateTestQuestion.setAuthor(author);
		TemplateTestVariant parent = templateTestVariantRepository.findByTemplateTestVariantId(templateTestQuestion.getTemplateTestVariant().getId());
		updateTemplateTestQuestion.setTemplateTestVariant(parent);
		if (!accessUtils.canEditCard(updateTemplateTestQuestion)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!updateTemplateTestQuestion.getTemplateTestVariant().getTemplateTest().getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + updateTemplateTestQuestion.getTemplateTestVariant().getTemplateTest().getClass().getSimpleName().toLowerCase() + ", id = " + updateTemplateTestQuestion.getTemplateTestVariant().getTemplateTest().getId() + "]");
		}
		templateTestQuestionValidator.validate(updateTemplateTestQuestion, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateTestQuestion savedTemplateTestVariant = templateTestQuestionRepository.save(updateTemplateTestQuestion);
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