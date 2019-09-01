package com.dls.base.validator;

import com.dls.base.entity.Course;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class CourseValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Course.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Course course = (Course) o;
        if (course.getName().trim().isEmpty()){
            errors.rejectValue("name", "form.course.name.required");
        }
		if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(course.getName()).matches()){
			errors.rejectValue("name", "form.course.name.concrete");
		}
        if (course.getDescription().trim().isEmpty()){
            errors.rejectValue("description", "form.course.description.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(course.getDescription()).matches()){
            errors.rejectValue("name", "form.course.description.concrete");
        }
        if (course.getTemplateCourse() == null){
            errors.rejectValue("templatecourse", "form.course.templatecourse.required");
        }
        if (course.getStatus() == null){
            errors.rejectValue("status", "form.course.status.required");
        }
        if (!course.getStatus().getCode().equals("draft")){
            errors.rejectValue("status", "form.course.status.change.deprecate");
        }
        if (course.getGroup() == null){
            errors.rejectValue("group", "form.course.group.required");
        }
        if (course.getCurator() == null){
            errors.rejectValue("curator", "form.course.curator.required");
        }
    }
}
