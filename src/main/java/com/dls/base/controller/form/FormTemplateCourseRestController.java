package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.TemplateCourseValidator;
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
public class FormTemplateCourseRestController
{

	private final TemplateCourseRepository templateCourseRepository;
	private final PersonRepository personRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final MoveRepository moveRepository;
	private final StatusRepository statusRepository;
	private final TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository;
	private final BlockRepository blockRepository;


	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	TemplateCourseValidator templateCourseValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	FormTemplateCourseRestController(TemplateCourseRepository templateCourseRepository, PersonRepository personRepository, TemplateLifeCycleRepository templateLifeCycleRepository, MoveRepository moveRepository, StatusRepository statusRepository, TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository, BlockRepository blockRepository) {
		this.templateCourseRepository = templateCourseRepository;
		this.personRepository = personRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.moveRepository = moveRepository;
		this.statusRepository = statusRepository;
		this.templateCourseTemplateResponseRepository = templateCourseTemplateResponseRepository;
		this.blockRepository = blockRepository;
	}

	@GetMapping(value = "form/templatecourse/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		TemplateCourse templateCourse;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			templateCourse = new TemplateCourse();
			templateCourse.setAuthor(accessUtils.getCurrentPerson());
			TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(templateCourse.getClass().getSimpleName().toLowerCase());
			LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
			templateCourse.setStatus(lifeCycle.getInitStatus());
		}else{
			templateCourse = templateCourseRepository.findByTemplateCourseId(id);
			if (!accessUtils.canReadCard(templateCourse)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(templateCourse);
		return formTemplate;
	}

	@GetMapping(value = "form/templatecourse/children/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getChildFormByIdentifier(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		Set<TemplateCourse> templateCourseSet = templateCourseRepository.findByParentId(id);
		if (templateCourseSet.size() > 0){
			TemplateCourse templateCourse = templateCourseSet.iterator().next();
			if (!accessUtils.canReadCard(templateCourse)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
			return getForm(templateCourse);
		}else{
			return null;
		}
	}

	public FormTemplate getForm(TemplateCourse templateCourse) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templateCourse);
		formTemplate.tabTitle = templateCourse.getName();
		formTemplate.template = templateCourse.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/templatecourse/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody TemplateCourse templateCourse, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		templateCourse.setAuthor(personRepository.findByPersonId(templateCourse.getAuthor().getId()));
		templateCourse.setStatus(statusRepository.findByStatusId(templateCourse.getStatus().getId()));
		if (!accessUtils.canEditCard(templateCourse)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateCourse.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + templateCourse.getClass().getSimpleName().toLowerCase() + ", id = " + templateCourse.getId() + "]");
		}
		templateCourseValidator.validate(templateCourse, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateCourse newTemplateCourse = new TemplateCourse();
		newTemplateCourse.setDescription(templateCourse.getDescription());
		newTemplateCourse.setStatus(templateCourse.getStatus());
		newTemplateCourse.setName(templateCourse.getName());
		newTemplateCourse.setVersion(1L);
		newTemplateCourse.setAuthor(templateCourse.getAuthor());
		newTemplateCourse.setDateCreate(new Date());
		TemplateCourse savedTemplateCourse = templateCourseRepository.save(newTemplateCourse);
		formTemplate = getFormByIdentifier(savedTemplateCourse.getId().toString());
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

	@PutMapping(value = "form/templatecourse/new_version/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createFormVersion(@PathVariable long id) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateCourse templateCourse = templateCourseRepository.findByTemplateCourseId(id);
		if (templateCourseRepository.findByParentId(templateCourse.getId()).size() > 0){
			throw new Exception("Карточка с новой версией шаблона уже существует");
		}
		if (!accessUtils.canEditCard(templateCourse)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templateCourse.getStatus().getCode().equals("active")){
			throw new Exception("Некорректный статус [class = " + templateCourse.getClass().getSimpleName().toLowerCase() + ", id = " + templateCourse.getId() + "]");
		}
		TemplateCourse newTemplateCourse = new TemplateCourse();
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(templateCourse.getClass().getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		newTemplateCourse.setStatus(lifeCycle.getInitStatus());
		newTemplateCourse.setName(templateCourse.getName());
		newTemplateCourse.setDescription(templateCourse.getDescription());
		newTemplateCourse.setParent(templateCourse);
		newTemplateCourse.setVersion(templateCourse.getVersion() + 1);
		newTemplateCourse.setDateCreate(new Date());
		newTemplateCourse.setAuthor(accessUtils.getCurrentPerson());
		TemplateCourse savedTemplateCourse = templateCourseRepository.save(newTemplateCourse);

		for (Block block : blockRepository.findBlockByTemplateCourseId(templateCourse.getId())){
			Block block1 = new Block();
			block1.setType(block.getType());
			block1.setPosition(block.getPosition());
			block1.setParentClass(newTemplateCourse.getClass().getSimpleName().toLowerCase());
			block1.setParentId(newTemplateCourse.getId());
			Block savedBlock = blockRepository.save(block1);

			for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByBlockId(block.getId())){
				TemplateCourseTemplateResponse newTemplateCourseTemplateResponse = new TemplateCourseTemplateResponse();
				newTemplateCourseTemplateResponse.setTemplateCourse(savedTemplateCourse);
				newTemplateCourseTemplateResponse.setPosition(templateCourseTemplateResponse.getPosition());
				newTemplateCourseTemplateResponse.setTemplateResponseId(templateCourseTemplateResponse.getTemplateResponseId());
				newTemplateCourseTemplateResponse.setTemplateResponseClass(templateCourseTemplateResponse.getTemplateResponseClass());
				newTemplateCourseTemplateResponse.setBlock(savedBlock);
				TemplateCourseTemplateResponse savedNewTemplateCourseTemplateResponse = templateCourseTemplateResponseRepository.save(newTemplateCourseTemplateResponse);
			}

		}
		formTemplate = getFormByIdentifier(savedTemplateCourse.getId().toString());
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

	@PostMapping(value = "form/templatecourse/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody TemplateCourse templateCourse, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateCourse updateTemplateCourse = templateCourseRepository.findByTemplateCourseId(id);
		if (!accessUtils.canEditCard(updateTemplateCourse)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		updateTemplateCourse.setName(templateCourse.getName());
		updateTemplateCourse.setDescription(templateCourse.getDescription());
		Person author = personRepository.findByPersonId(templateCourse.getAuthor().getId());
		updateTemplateCourse.setAuthor(author);
		templateCourse.setStatus(statusRepository.findByStatusId(templateCourse.getStatus().getId()));
		if (!templateCourse.getStatus().getCode().equals("draft")) {
			throw new Exception("Некорректный статус [class = " + templateCourse.getClass().getSimpleName().toLowerCase() + ", id = " + templateCourse.getId() + "]");
		}
		templateCourseValidator.validate(updateTemplateCourse, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplateCourse savedTemplateCourse = templateCourseRepository.save(updateTemplateCourse);
		formTemplate = getFormByIdentifier(savedTemplateCourse.getId().toString());
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

	@PostMapping(value = "form/templatecourse/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplateCourse updateTemplateCourse = templateCourseRepository.findByTemplateCourseId(id);
		//if (!accessUtils.canEditCard(updateTemplateCourse)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updateTemplateCourse.getClass().getSimpleName().toLowerCase(), updateTemplateCourse.getStatus().getId(), toStatus.getId());
		updateTemplateCourse.setStatus(move.getToStatus());
		if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("active")){
			utils.checkTemplate(updateTemplateCourse, true);
			if (updateTemplateCourse.getParent() != null){
				toStatus(updateTemplateCourse.getParent().getId(), "duplicate");
			}
		}
		TemplateCourse savedTemplateCourse = templateCourseRepository.save(updateTemplateCourse);
		formTemplate = getFormByIdentifier(savedTemplateCourse.getId().toString());
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