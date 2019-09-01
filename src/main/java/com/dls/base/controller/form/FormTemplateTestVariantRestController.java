package com.dls.base.controller.form;

import com.dls.base.entity.Person;
import com.dls.base.entity.TemplateTest;
import com.dls.base.entity.TemplateTestVariant;
import com.dls.base.repository.PersonRepository;
import com.dls.base.repository.TemplateTestRepository;
import com.dls.base.repository.TemplateTestVariantRepository;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.TemplateTestVariantValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class FormTemplateTestVariantRestController
{

	private final TemplateTestVariantRepository templateTestVariantRepository;
	private final PersonRepository personRepository;
	private final TemplateTestRepository templateTestRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	TemplateTestVariantValidator templateTestVariantValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
    FormTemplateTestVariantRestController(TemplateTestVariantRepository templateTestVariantRepository, PersonRepository personRepository, TemplateTestRepository templateTestRepository) {
		this.templateTestVariantRepository = templateTestVariantRepository;
		this.personRepository = personRepository;
		this.templateTestRepository = templateTestRepository;
	}

	@GetMapping(value = "form/templatetestvariant/templatetest/{templateTestId}/0", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getNewFormByIdentifier(@PathVariable Long templateTestId) {
		TemplateTestVariant templateTestVariant;
		templateTestVariant = new TemplateTestVariant();
		templateTestVariant.setNumber(templateTestVariantRepository.findLastPositionByTemplateTestId(templateTestId) + 1);
		templateTestVariant.setAuthor(accessUtils.getCurrentPerson());
		FormTemplate formTemplate = getForm(templateTestVariant);
		return formTemplate;
	}

	@GetMapping(value = "form/templatetestvariant/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		TemplateTestVariant templateTestVariant;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			templateTestVariant = new TemplateTestVariant();
			templateTestVariant.setNumber(0L);
			templateTestVariant.setAuthor(accessUtils.getCurrentPerson());
		}else{
			templateTestVariant = templateTestVariantRepository.findByTemplateTestVariantId(id);
			if (!accessUtils.canReadCard(templateTestVariant)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(templateTestVariant);
		return formTemplate;
	}

	public FormTemplate getForm(TemplateTestVariant templateTestVariant) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templateTestVariant);
		formTemplate.tabTitle = templateTestVariant.getNumber().toString();
		formTemplate.template = templateTestVariant.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/templatetestvariant/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody TemplateTestVariant templateTestVariant, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		templateTestVariant.setAuthor(personRepository.findByPersonId(templateTestVariant.getAuthor().getId()));
		templateTestVariant.setTemplateTest(templateTestRepository.findByTemplateTestId(templateTestVariant.getTemplateTest().getId()));
		if (!accessUtils.canEditCard(templateTestVariant)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateTestVariant.getTemplateTest().getStatus().getCode().equals("draft")){
			throw new Exception("incorrect status for [class = " + templateTestVariant.getTemplateTest().getClass().getSimpleName().toLowerCase() + ", id = " + templateTestVariant.getTemplateTest().getId() + "]");
		}
		templateTestVariantValidator.validate(templateTestVariant, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateTestVariant newTemplateTestVariant = new TemplateTestVariant();
		newTemplateTestVariant.setNumber(templateTestVariant.getNumber());
		newTemplateTestVariant.setAuthor(templateTestVariant.getAuthor());
		newTemplateTestVariant.setTemplateTest(templateTestVariant.getTemplateTest());
		TemplateTestVariant savedTemplateTestVariant = templateTestVariantRepository.save(newTemplateTestVariant);
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

	@PostMapping(value = "form/templatetestvariant/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody TemplateTestVariant templateTestVariant, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateTestVariant updateTemplateTestVariant = templateTestVariantRepository.findByTemplateTestVariantId(id);
		updateTemplateTestVariant.setNumber(templateTestVariant.getNumber());
		Person author = personRepository.findByPersonId(templateTestVariant.getAuthor().getId());
		updateTemplateTestVariant.setAuthor(author);
		TemplateTest parent = templateTestRepository.findByTemplateTestId(templateTestVariant.getTemplateTest().getId());
		updateTemplateTestVariant.setTemplateTest(parent);
		if (!accessUtils.canEditCard(updateTemplateTestVariant)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!updateTemplateTestVariant.getTemplateTest().getStatus().getCode().equals("draft")){
			throw new Exception("incorrect status for [class = " + updateTemplateTestVariant.getTemplateTest().getClass().getSimpleName().toLowerCase() + ", id = " + updateTemplateTestVariant.getTemplateTest().getId() + "]");
		}
		templateTestVariantValidator.validate(updateTemplateTestVariant, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateTestVariant savedTemplateTestVariant = templateTestVariantRepository.save(updateTemplateTestVariant);
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