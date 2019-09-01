package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.TemplateLessonValidator;
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
public class FormTemplateLessonRestController
{

	private final TemplateLessonRepository templateLessonRepository;
	private final PersonRepository personRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final MoveRepository moveRepository;
	private final StatusRepository statusRepository;
	private final ResourceRepository resourceRepository;
	private final CategoryRepository categoryRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	TemplateLessonValidator templateLessonValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	FormTemplateLessonRestController(TemplateLessonRepository templateLessonRepository, PersonRepository personRepository, TemplateLifeCycleRepository templateLifeCycleRepository, MoveRepository moveRepository, StatusRepository statusRepository, ResourceRepository resourceRepository, CategoryRepository categoryRepository) {
		this.templateLessonRepository = templateLessonRepository;
		this.personRepository = personRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.moveRepository = moveRepository;
		this.statusRepository = statusRepository;
		this.resourceRepository = resourceRepository;
		this.categoryRepository = categoryRepository;
	}

	@GetMapping(value = "form/templatelesson/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		TemplateLesson templateLesson;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			templateLesson = new TemplateLesson();
			templateLesson.setAuthor(accessUtils.getCurrentPerson());
			TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(templateLesson.getClass().getSimpleName().toLowerCase());
			LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
			templateLesson.setStatus(lifeCycle.getInitStatus());
		}else{
			templateLesson = templateLessonRepository.findByTemplateLessonId(id);
			if (!accessUtils.canReadCard(templateLesson)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(templateLesson);
		return formTemplate;
	}

	@GetMapping(value = "form/templatelesson/children/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getChildFormByIdentifier(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		Set<TemplateLesson> templateLessonSet = templateLessonRepository.findByParentId(id);
		if (templateLessonSet.size() > 0){
			TemplateLesson templateLesson = templateLessonSet.iterator().next();
			if (!accessUtils.canReadCard(templateLesson)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
			return getForm(templateLesson);
		}else{
			return null;
		}
	}

	public FormTemplate getForm(TemplateLesson templateLesson) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templateLesson);
		formTemplate.tabTitle = templateLesson.getName();
		formTemplate.template = templateLesson.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/templatelesson/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody TemplateLesson templateLesson, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		templateLesson.setAuthor(personRepository.findByPersonId(templateLesson.getAuthor().getId()));
		templateLesson.setStatus(statusRepository.findByStatusId(templateLesson.getStatus().getId()));
		templateLesson.setCategory(categoryRepository.findByCategoryId(templateLesson.getCategory().getId()));
		if (!accessUtils.canEditCard(templateLesson)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateLesson.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + templateLesson.getClass().getSimpleName().toLowerCase() + ", id = " + templateLesson.getId() + "]");
		}
		templateLessonValidator.validate(templateLesson, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateLesson newTemplateLesson = new TemplateLesson();
		newTemplateLesson.setDescription(templateLesson.getDescription());
		newTemplateLesson.setStatus(statusRepository.findByStatusId(templateLesson.getStatus().getId()));
		newTemplateLesson.setCategory(templateLesson.getCategory());
		newTemplateLesson.setName(templateLesson.getName());
		newTemplateLesson.setVersion(1L);
		newTemplateLesson.setAuthor(templateLesson.getAuthor());
		newTemplateLesson.setDateCreate(new Date());
		TemplateLesson savedTemplateLesson = templateLessonRepository.save(newTemplateLesson);
		formTemplate = getFormByIdentifier(savedTemplateLesson.getId().toString());
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

	@PutMapping(value = "form/templatelesson/new_version/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createFormVersion(@PathVariable long id) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(id);
		if (templateLessonRepository.findByParentId(templateLesson.getId()).size() > 0){
			throw new Exception("Карточка с новой версией шаблона уже существует");
		}
		if (!accessUtils.canEditCard(templateLesson)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateLesson.getStatus().getCode().equals("active")){
			throw new Exception("Некорректный статус [class = " + templateLesson.getClass().getSimpleName().toLowerCase() + ", id = " + templateLesson.getId() + "]");
		}
		TemplateLesson newTemplateLesson = new TemplateLesson();
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(templateLesson.getClass().getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		newTemplateLesson.setStatus(lifeCycle.getInitStatus());
		newTemplateLesson.setCategory(templateLesson.getCategory());
		newTemplateLesson.setName(templateLesson.getName());
		newTemplateLesson.setDescription(templateLesson.getDescription());
		newTemplateLesson.setParent(templateLesson);
		newTemplateLesson.setVersion(templateLesson.getVersion() + 1);
		newTemplateLesson.setAuthor(accessUtils.getCurrentPerson());
		newTemplateLesson.setDateCreate(new Date());
		TemplateLesson savedTemplateLesson = templateLessonRepository.save(newTemplateLesson);
		for (Resource resource : resourceRepository.findByEntityClassAndEntityId(savedTemplateLesson.getParent().getClass().getSimpleName().toLowerCase(), savedTemplateLesson.getParent().getId())){
			Resource newResource = utils.getResourceClone(resource);
			newResource.setEntityId(savedTemplateLesson.getId());
			newResource.setEntityClass(savedTemplateLesson.getClass().getSimpleName().toLowerCase());
			resourceRepository.save(newResource);
		}
		formTemplate = getFormByIdentifier(savedTemplateLesson.getId().toString());
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

	@PostMapping(value = "form/templatelesson/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody TemplateLesson templateLesson, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateLesson updateTemplateLesson = templateLessonRepository.findByTemplateLessonId(id);
		updateTemplateLesson.setName(templateLesson.getName());
		updateTemplateLesson.setDescription(templateLesson.getDescription());
		Person author = personRepository.findByPersonId(templateLesson.getAuthor().getId());
		updateTemplateLesson.setAuthor(author);
		templateLesson.setStatus(statusRepository.findByStatusId(templateLesson.getStatus().getId()));
		templateLesson.setCategory(categoryRepository.findByCategoryId(templateLesson.getCategory().getId()));
		updateTemplateLesson.setCategory(templateLesson.getCategory());
		if (!accessUtils.canEditCard(templateLesson)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateLesson.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + templateLesson.getClass().getSimpleName().toLowerCase() + ", id = " + templateLesson.getId() + "]");
		}
		templateLessonValidator.validate(updateTemplateLesson, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateLesson savedTemplateLesson = templateLessonRepository.save(updateTemplateLesson);
		formTemplate = getFormByIdentifier(savedTemplateLesson.getId().toString());
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

	@PostMapping(value = "form/templatelesson/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateLesson updateTemplateLesson = templateLessonRepository.findByTemplateLessonId(id);
		//if (!accessUtils.canEditCard(updateTemplateLesson)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updateTemplateLesson.getClass().getSimpleName().toLowerCase(), updateTemplateLesson.getStatus().getId(), toStatus.getId());
		updateTemplateLesson.setStatus(move.getToStatus());
		if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("active")){
			utils.checkTemplate(updateTemplateLesson, true);
			if (updateTemplateLesson.getParent() != null){
				toStatus(updateTemplateLesson.getParent().getId(), "duplicate");
			}
		}
		TemplateLesson savedTemplateLesson = templateLessonRepository.save(updateTemplateLesson);
		formTemplate = getFormByIdentifier(savedTemplateLesson.getId().toString());
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