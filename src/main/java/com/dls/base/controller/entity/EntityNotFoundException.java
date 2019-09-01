package com.dls.base.controller.entity;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
class EntityNotFoundException extends RuntimeException {

	public EntityNotFoundException(String template, long id) {
		super("could not find" + template + " '" + id + "'.");
	}
}