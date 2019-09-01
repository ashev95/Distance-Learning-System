package com.dls.base.controller;

class TempResourceNotLoadedException extends RuntimeException {

	public TempResourceNotLoadedException(String filepath) {
		super("could not upload temp file" + " '" + filepath + "'.");
	}
}