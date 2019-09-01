package com.dls.base.validator;

import com.dls.base.entity.TemplateLesson;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class TemplateLessonValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return TemplateLesson.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TemplateLesson templateLesson = (TemplateLesson) o;
        if (templateLesson.getName().trim().isEmpty()){
            errors.rejectValue("name", "form.templatelesson.name.required");
        }
		if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templateLesson.getName()).matches()){
			errors.rejectValue("name", "form.templatelesson.name.concrete");
		}
        if (templateLesson.getDescription().trim().isEmpty()){
            errors.rejectValue("description", "form.templatelesson.description.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templateLesson.getDescription()).matches()){
            errors.rejectValue("description", "form.templatelesson.description.concrete");
        }
        if (templateLesson.getAuthor() == null){
            errors.rejectValue("author", "form.templatelesson.author.required");
        }
    }
}
