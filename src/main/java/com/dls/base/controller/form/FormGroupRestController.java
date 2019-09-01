package com.dls.base.controller.form;

import com.dls.base.entity.Group;
import com.dls.base.entity.Person;
import com.dls.base.repository.GroupRepository;
import com.dls.base.repository.PersonRepository;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.GroupValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class FormGroupRestController
{

	private final PersonRepository personRepository;
	private final GroupRepository groupRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	GroupValidator groupValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	FormGroupRestController(PersonRepository personRepository, GroupRepository groupRepository) {
		this.personRepository = personRepository;
		this.groupRepository = groupRepository;
	}

	@GetMapping(value = "form/group/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		Group group;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			group = new Group();
			group.setCurator(accessUtils.getCurrentPerson());
		}else{
			group = groupRepository.findByGroupId(id);
			if (!accessUtils.canReadCard(group)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(group);
		return formTemplate;
	}

	public FormTemplate getForm(Group group) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(group);
		formTemplate.tabTitle = group.getName();
		formTemplate.template = group.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/group/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody Group group, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		group.setCurator(personRepository.findByPersonId(group.getCurator().getId()));
		if (!accessUtils.canEditCard(group)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		groupValidator.validate(group, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Group newGroup = new Group();
		newGroup.setName(group.getName());
		newGroup.setCurator(group.getCurator());
		Group savedGroup = groupRepository.save(newGroup);
		formTemplate = getFormByIdentifier(savedGroup.getId().toString());
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

	@PostMapping(value = "form/group/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody Group group, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		Group updateGroup = groupRepository.findByGroupId(id);
		updateGroup.setName(group.getName());
		Person curator = personRepository.findByPersonId(group.getCurator().getId());
		updateGroup.setCurator(curator);
		if (!accessUtils.canEditCard(updateGroup)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		groupValidator.validate(updateGroup, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Group savedGroup = groupRepository.save(updateGroup);
		formTemplate = getFormByIdentifier(savedGroup.getId().toString());
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