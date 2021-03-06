package com.dls.base.service;

import com.dls.base.controller.entity.PersonNotLoggedException;
import com.dls.base.entity.Person;
import com.dls.base.entity.Role;
import com.dls.base.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

public class PersonDetailsServiceImpl implements UserDetailsService {

    @Autowired
	private PersonRepository personRepository;

    @Autowired
    private MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        Person person = personRepository.findByUsername (username);
        if (person == null){
            throw new PersonNotLoggedException(messageSource.getMessage(new DefaultMessageSourceResolvable("PersonNotFound"), null));
        }
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : person.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getCode()));
        }
        return new org.springframework.security.core.userdetails.User(person.getUsername(), person.getPassword(), grantedAuthorities);
    }
}
