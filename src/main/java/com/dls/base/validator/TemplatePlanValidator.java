package com.dls.base.validator;

import com.dls.base.entity.TemplatePlan;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class TemplatePlanValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return TemplatePlan.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TemplatePlan templatePlan = (TemplatePlan) o;
        if (templatePlan.getName().trim().isEmpty()){
            errors.rejectValue("name", "form.templateplan.name.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templatePlan.getName()).matches()){
            errors.rejectValue("name", "form.templateplan.name.concrete");
        }
        if (templatePlan.getDescription().trim().isEmpty()){
            errors.rejectValue("description", "form.templateplan.description.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templatePlan.getDescription()).matches()){
            errors.rejectValue("description", "form.templateplan.description.concrete");
        }
        if (templatePlan.getAuthor() == null){
            errors.rejectValue("author", "form.templateplan.author.required");
        }
    }
}
