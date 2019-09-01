package com.dls.base.controller.entity;

import com.dls.base.entity.Group;
import com.dls.base.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupRestController
{

	private final GroupRepository groupRepository;

	@Autowired
    GroupRestController(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public List<Group> getAll() {
		List<Group> groups = this.groupRepository.findAll();
		return groups;
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	Group getById(@PathVariable long id) {
		return this.groupRepository.findByGroupId(id);
	}

}