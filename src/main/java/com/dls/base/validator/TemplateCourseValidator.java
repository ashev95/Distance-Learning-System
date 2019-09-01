package com.dls.base.validator;

import com.dls.base.entity.TemplateCourse;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class TemplateCourseValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return TemplateCourse.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TemplateCourse templateCourse = (TemplateCourse) o;
        if (templateCourse.getName().trim().isEmpty()){
            errors.rejectValue("name", "form.templatecourse.name.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templateCourse.getName()).matches()){
            errors.rejectValue("name", "form.templatecourse.name.concrete");
        }
        if (templateCourse.getDescription().trim().isEmpty()){
            errors.rejectValue("description", "form.templatecourse.description.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templateCourse.getDescription()).matches()){
            errors.rejectValue("description", "form.templatecourse.description.concrete");
        }
        if (templateCourse.getAuthor() == null){
            errors.rejectValue("author", "form.templatecourse.author.required");
        }
    }
}
