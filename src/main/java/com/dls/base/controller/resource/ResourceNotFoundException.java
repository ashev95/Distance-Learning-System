package com.dls.base.controller.resource;

class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String filepath) {
		super("could not find file" + " '" + filepath + "'.");
	}
}