package com.dls.base.controller.resource;

class ResourceNotLoadedException extends RuntimeException {

	public ResourceNotLoadedException(String filepath) {
		super("could not upload file" + " '" + filepath + "'.");
	}
}