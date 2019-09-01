package com.dls.base.validator;

import com.dls.base.entity.Group;
import com.dls.base.repository.GroupRepository;
import com.dls.base.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class GroupValidator implements Validator {

	@Autowired
	private GroupRepository groupRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return Group.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Group group = (Group) o;
        if (group.getName().trim().isEmpty()){
            errors.rejectValue("name", "form.group.name.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(group.getName()).matches()){
            errors.rejectValue("name", "form.group.name.concrete");
        }
        if (group.getCurator() == null){
            errors.rejectValue("curator", "form.group.curator.required");
        }
        if (groupRepository.findByNameAndCuratorId(group.getName(), group.getCurator().getId()) != null){
            if (groupRepository.findByNameAndCuratorId(group.getName(), group.getCurator().getId()).equals(group.getId())){
                errors.rejectValue("name", "form.group.name.exist");
            }
		}
    }
}
