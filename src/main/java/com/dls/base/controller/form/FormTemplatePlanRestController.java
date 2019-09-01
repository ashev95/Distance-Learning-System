package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.TemplatePlanValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.tags.form.FormTag;

import java.util.Date;
import java.util.Set;

@RestController
public class FormTemplatePlanRestController
{

	private final TemplatePlanRepository templatePlanRepository;
	private final PersonRepository personRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final MoveRepository moveRepository;
	private final StatusRepository statusRepository;
	private final TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository;
	private final BlockRepository blockRepository;


	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	TemplatePlanValidator templatePlanValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	FormTemplatePlanRestController(TemplatePlanRepository templatePlanRepository, PersonRepository personRepository, TemplateLifeCycleRepository templateLifeCycleRepository, MoveRepository moveRepository, StatusRepository statusRepository, TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository, BlockRepository blockRepository) {
		this.templatePlanRepository = templatePlanRepository;
		this.personRepository = personRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.moveRepository = moveRepository;
		this.statusRepository = statusRepository;
		this.templatePlanTemplateResponseRepository = templatePlanTemplateResponseRepository;
		this.blockRepository = blockRepository;
	}

	@GetMapping(value = "form/templateplan/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		TemplatePlan templatePlan;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			templatePlan = new TemplatePlan();
			templatePlan.setAuthor(accessUtils.getCurrentPerson());
			TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(templatePlan.getClass().getSimpleName().toLowerCase());
			LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
			templatePlan.setStatus(lifeCycle.getInitStatus());
		}else{
			templatePlan = templatePlanRepository.findByTemplatePlanId(id);
			if (!accessUtils.canReadCard(templatePlan)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(templatePlan);
		return formTemplate;
	}

	@GetMapping(value = "form/templateplan/children/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getChildFormByIdentifier(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		Set<TemplatePlan> templatePlanSet = templatePlanRepository.findByParentId(id);
		if (templatePlanSet.size() > 0){
			TemplatePlan templatePlan = templatePlanSet.iterator().next();
			if (!accessUtils.canReadCard(templatePlan)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
			return getForm(templatePlan);
		}else{
			return null;
		}
	}

	public FormTemplate getForm(TemplatePlan templatePlan) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(templatePlan);
		formTemplate.tabTitle = templatePlan.getName();
		formTemplate.template = templatePlan.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/templateplan/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody TemplatePlan templatePlan, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		templatePlan.setAuthor(personRepository.findByPersonId(templatePlan.getAuthor().getId()));
		templatePlan.setStatus(statusRepository.findByStatusId(templatePlan.getStatus().getId()));
		if (!accessUtils.canEditCard(templatePlan)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templatePlan.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + templatePlan.getClass().getSimpleName().toLowerCase() + ", id = " + templatePlan.getId() + "]");
		}
		templatePlanValidator.validate(templatePlan, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplatePlan newTemplatePlan = new TemplatePlan();
		newTemplatePlan.setDescription(templatePlan.getDescription());
		newTemplatePlan.setStatus(statusRepository.findByStatusId(templatePlan.getStatus().getId()));
		newTemplatePlan.setName(templatePlan.getName());
		newTemplatePlan.setVersion(1L);
		newTemplatePlan.setAuthor(templatePlan.getAuthor());
		newTemplatePlan.setDateCreate(new Date());
		TemplatePlan savedTemplatePlan = templatePlanRepository.save(newTemplatePlan);
		formTemplate = getFormByIdentifier(savedTemplatePlan.getId().toString());
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

	@PutMapping(value = "form/templateplan/new_version/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createFormVersion(@PathVariable long id) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplatePlan templatePlan = templatePlanRepository.findByTemplatePlanId(id);
		if (templatePlanRepository.findByParentId(templatePlan.getId()).size() > 0){
			throw new Exception("Карточка с новой версией шаблона уже существует");
		}
		if (!accessUtils.canEditCard(templatePlan)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!templatePlan.getStatus().getCode().equals("active")){
			throw new Exception("Некорректный статус [class = " + templatePlan.getClass().getSimpleName().toLowerCase() + ", id = " + templatePlan.getId() + "]");
		}
		TemplatePlan newTemplatePlan = new TemplatePlan();
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(templatePlan.getClass().getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		newTemplatePlan.setStatus(lifeCycle.getInitStatus());
		newTemplatePlan.setName(templatePlan.getName());
		newTemplatePlan.setDescription(templatePlan.getDescription());
		newTemplatePlan.setParent(templatePlan);
		newTemplatePlan.setVersion(templatePlan.getVersion() + 1);
		newTemplatePlan.setAuthor(accessUtils.getCurrentPerson());
		newTemplatePlan.setDateCreate(new Date());
		TemplatePlan savedTemplatePlan = templatePlanRepository.save(newTemplatePlan);

		for (Block block : blockRepository.findBlockByTemplatePlanId(templatePlan.getId())){
			Block block1 = new Block();
			block1.setType(block.getType());
			block1.setPosition(block.getPosition());
			block1.setParentClass(newTemplatePlan.getClass().getSimpleName().toLowerCase());
			block1.setParentId(newTemplatePlan.getId());
			Block savedBlock = blockRepository.save(block1);

			for (TemplatePlanTemplateResponse templatePlanTemplateResponse : templatePlanTemplateResponseRepository.findTemplatePlanTemplateResponseByBlockId(block.getId())){
				TemplatePlanTemplateResponse newTemplatePlanTemplateResponse = new TemplatePlanTemplateResponse();
				newTemplatePlanTemplateResponse.setTemplatePlan(savedTemplatePlan);
				newTemplatePlanTemplateResponse.setPosition(templatePlanTemplateResponse.getPosition());
				newTemplatePlanTemplateResponse.setTemplateResponseId(templatePlanTemplateResponse.getTemplateResponseId());
				newTemplatePlanTemplateResponse.setTemplateResponseClass(templatePlanTemplateResponse.getTemplateResponseClass());
				newTemplatePlanTemplateResponse.setBlock(savedBlock);
				TemplatePlanTemplateResponse savedNewTemplatePlanTemplateResponse = templatePlanTemplateResponseRepository.save(newTemplatePlanTemplateResponse);
			}

		}

		formTemplate = getFormByIdentifier(savedTemplatePlan.getId().toString());
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

	@PostMapping(value = "form/templateplan/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody TemplatePlan templatePlan, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplatePlan updateTemplatePlan = templatePlanRepository.findByTemplatePlanId(id);
		if (!accessUtils.canEditCard(updateTemplatePlan)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		updateTemplatePlan.setName(templatePlan.getName());
		updateTemplatePlan.setDescription(templatePlan.getDescription());
		Person author = personRepository.findByPersonId(templatePlan.getAuthor().getId());
		updateTemplatePlan.setAuthor(author);
		templatePlan.setStatus(statusRepository.findByStatusId(templatePlan.getStatus().getId()));
		if (!templatePlan.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + templatePlan.getClass().getSimpleName().toLowerCase() + ", id = " + templatePlan.getId() + "]");
		}
		templatePlanValidator.validate(updateTemplatePlan, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		TemplatePlan savedTemplatePlan = templatePlanRepository.save(updateTemplatePlan);
		formTemplate = getFormByIdentifier(savedTemplatePlan.getId().toString());
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

	@PostMapping(value = "form/templateplan/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		TemplatePlan updateTemplatePlan = templatePlanRepository.findByTemplatePlanId(id);
		//if (!accessUtils.canEditCard(updateTemplatePlan)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updateTemplatePlan.getClass().getSimpleName().toLowerCase(), updateTemplatePlan.getStatus().getId(), toStatus.getId());
		updateTemplatePlan.setStatus(move.getToStatus());
		if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("active")){
			utils.checkTemplate(updateTemplatePlan, true);
			if (updateTemplatePlan.getParent() != null){
				toStatus(updateTemplatePlan.getParent().getId(), "duplicate");
			}
		}
		TemplatePlan savedTemplatePlan = templatePlanRepository.save(updateTemplatePlan);
		formTemplate = getFormByIdentifier(savedTemplatePlan.getId().toString());
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