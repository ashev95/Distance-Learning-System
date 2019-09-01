package com.dls.base.controller.entity;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
class PersonNotFoundException extends RuntimeException {

	public PersonNotFoundException(long id) {
		super("could not find person '" + id + "'.");
	}
}