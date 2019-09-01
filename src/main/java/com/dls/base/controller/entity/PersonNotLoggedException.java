package com.dls.base.controller.entity;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class PersonNotLoggedException extends UsernameNotFoundException {

    public PersonNotLoggedException(String msg) {
		super(msg);
    }
}