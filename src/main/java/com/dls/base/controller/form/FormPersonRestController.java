package com.dls.base.controller.form;

import com.dls.base.entity.Person;
import com.dls.base.repository.PersonRepository;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.PersonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class FormPersonRestController
{

	private final PersonRepository personRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	PersonValidator personValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	FormPersonRestController(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	@GetMapping(value = "form/currentUser", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByCurrentUsername() throws Exception {
		FormTemplate formTemplate = getFormByIdentifier(accessUtils.getCurrentPerson().getId().toString());
		return formTemplate;
	}

	@PostMapping(value = "form/changePassword", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@RequestBody Person person) throws Exception {
		FormTemplate formTemplate = null;
		try{
		if (personRepository.findByUsername(person.getUsername()) == null){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage("form.user.username.missed", null, null)));
		}
		Person updatePerson = personRepository.findByUsername(person.getUsername());
		if (!accessUtils.canEditCard(updatePerson)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (person.getPassword().length() < 5 || person.getPassword().length() > 25){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage("form.user.password.concrete", null, null)));
		}
		updatePerson.setPassword(new BCryptPasswordEncoder().encode(person.getPassword()));
		Person savedPerson = personRepository.save(updatePerson);
		formTemplate = getFormByIdentifier(savedPerson.getId().toString());
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

	@GetMapping(value = "form/person/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		Person person;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			person = new Person();
		}else{
			person = personRepository.findByPersonId(id);
			if (!accessUtils.canReadCard(person)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(person);
		return formTemplate;
	}

	public FormTemplate getForm(Person person) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(person);
		formTemplate.tabTitle = (person.getSurname() + " " + person.getName() + " " + person.getMiddlename());
		formTemplate.template = person.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/person/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody Person person, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		if (!accessUtils.canEditCard(person)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		personValidator.validate(person, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Person newPerson = new Person();
		newPerson.setUsername(person.getUsername());
		newPerson.setEmail(person.getEmail());
		newPerson.setSurname(person.getSurname());
		newPerson.setName(person.getName());
		newPerson.setMiddlename(person.getMiddlename());
		newPerson.setGender(person.getGender());
		newPerson.setAdditionally(person.getAdditionally());
		newPerson.setPassword(new BCryptPasswordEncoder().encode(utils.getAppProperty("default.user.password")));
		newPerson.setRoles(person.getRoles());
		Person savedPerson = personRepository.save(newPerson);
		formTemplate = getFormByIdentifier(savedPerson.getId().toString());
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

	@PostMapping(value = "form/person/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody Person person, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		if (!accessUtils.canEditCard(person)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		personValidator.validate(person, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Person updatePerson = personRepository.findByPersonId(id);
		updatePerson.setEmail(person.getEmail());
		updatePerson.setSurname(person.getSurname());
		updatePerson.setName(person.getName());
		updatePerson.setMiddlename(person.getMiddlename());
		updatePerson.setGender(person.getGender());
		updatePerson.setAdditionally(person.getAdditionally());
		updatePerson.setRoles(person.getRoles());
		Person savedPerson = personRepository.save(updatePerson);
		formTemplate = getFormByIdentifier(savedPerson.getId().toString());
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