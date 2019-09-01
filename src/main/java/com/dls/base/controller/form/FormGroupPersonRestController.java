package com.dls.base.controller.form;

import com.dls.base.entity.GroupPerson;
import com.dls.base.repository.GroupPersonRepository;
import com.dls.base.repository.GroupRepository;
import com.dls.base.repository.PersonRepository;
import com.dls.base.request.GroupPersonContainer;
import com.dls.base.utils.AccessUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Access;
import java.util.List;

@RestController
public class FormGroupPersonRestController
{

	private final GroupPersonRepository groupPersonRepository;
	private final PersonRepository personRepository;
	private final GroupRepository groupRepository;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
    FormGroupPersonRestController(GroupPersonRepository groupPersonRepository, PersonRepository personRepository, GroupRepository groupRepository) {
		this.groupPersonRepository = groupPersonRepository;
		this.personRepository = personRepository;
		this.groupRepository = groupRepository;
	}

	@PutMapping(value = "form/groupperson/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody final List<GroupPersonContainer> personContainerList) throws Exception {
		try{
		for (GroupPersonContainer personContainer : personContainerList){
			if (groupPersonRepository.findByGroupAndPersonId(personContainer.groupId, personContainer.personId) == null){
				GroupPerson groupPerson = new GroupPerson();
				groupPerson.setGroup(groupRepository.findByGroupId(personContainer.groupId));
				groupPerson.setPerson(personRepository.findByPersonId(personContainer.personId));
				if (!accessUtils.canEditCard(groupPerson)){
					throw new Exception("Отсутствуют права на создание/изменение карточки");
				}
				GroupPerson savedGroupPerson = groupPersonRepository.save(groupPerson);
			}
		}
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.build();
	}

}