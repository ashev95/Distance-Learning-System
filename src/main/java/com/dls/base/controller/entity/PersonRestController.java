package com.dls.base.controller.entity;

import com.dls.base.entity.Person;
import com.dls.base.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/person")
public class PersonRestController
{

	private final PersonRepository personRepository;

	@Autowired
	PersonRestController(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public List<Person> getAll() {
		List<Person> persons = this.personRepository.findAll();
		return persons;
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	Person getById(@PathVariable long id) {
		return this.personRepository.findByPersonId(id);
	}

	/*
	@PostMapping()
	public Person save(@RequestBody Person person) {
		return personRepository.save(person);
	}

	@PutMapping(value = "/{id}")
	public Person update(@PathVariable long id, @RequestBody Person person) {
		this.validate(id);
		return personRepository.save(person);
	}

	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable long id) {
		this.validate(id);
		personRepository.deleteById(id);
	}
	 */

}