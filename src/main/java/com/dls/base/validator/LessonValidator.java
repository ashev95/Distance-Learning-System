package com.dls.base.validator;

import com.dls.base.entity.Lesson;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class LessonValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Lesson.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Lesson lesson = (Lesson) o;
        if (lesson.getName().trim().isEmpty()){
            errors.rejectValue("name", "form.lesson.name.required");
        }
		if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(lesson.getName()).matches()){
			errors.rejectValue("name", "form.lesson.name.concrete");
		}
        if (lesson.getDescription().trim().isEmpty()){
            errors.rejectValue("description", "form.lesson.description.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(lesson.getDescription()).matches()){
            errors.rejectValue("name", "form.lesson.description.concrete");
        }
        if (lesson.getTemplateLesson() == null){
            errors.rejectValue("templatelesson", "form.lesson.templatelesson.required");
        }
        if (lesson.getStatus() == null){
            errors.rejectValue("status", "form.lesson.status.required");
        }
        if (!lesson.getStatus().getCode().equals("draft")){
            errors.rejectValue("status", "form.lesson.status.change.deprecate");
        }
        if (lesson.getGroup() == null){
            errors.rejectValue("group", "form.lesson.group.required");
        }
        if (lesson.getCurator() == null){
            errors.rejectValue("curator", "form.lesson.curator.required");
        }
    }
}
