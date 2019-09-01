package com.dls.base.validator;

import com.dls.base.entity.Test;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class TestValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Test.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Test test = (Test) o;
        if (test.getName().trim().isEmpty()){
            errors.rejectValue("name", "form.test.name.required");
        }
		if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(test.getName()).matches()){
			errors.rejectValue("name", "form.test.name.concrete");
		}
        if (test.getDescription().trim().isEmpty()){
            errors.rejectValue("description", "form.test.description.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(test.getDescription()).matches()){
            errors.rejectValue("name", "form.test.description.concrete");
        }
        if (test.getTemplateTest() == null){
            errors.rejectValue("templatetest", "form.test.templatetest.required");
        }
        if (test.getStatus() == null){
            errors.rejectValue("status", "form.test.status.required");
        }
        if (!test.getStatus().getCode().equals("draft")){
            errors.rejectValue("status", "form.test.status.change.deprecate");
        }
        if (test.getGroup() == null){
            errors.rejectValue("group", "form.test.group.required");
        }
        if (test.getCurator() == null){
            errors.rejectValue("curator", "form.test.curator.required");
        }
    }
}
