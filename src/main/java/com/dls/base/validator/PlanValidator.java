package com.dls.base.validator;

import com.dls.base.entity.Plan;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class PlanValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Plan.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Plan plan = (Plan) o;
        if (plan.getName().trim().isEmpty()){
            errors.rejectValue("name", "form.plan.name.required");
        }
		if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(plan.getName()).matches()){
			errors.rejectValue("name", "form.plan.name.concrete");
		}
        if (plan.getDescription().trim().isEmpty()){
            errors.rejectValue("description", "form.plan.description.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(plan.getDescription()).matches()){
            errors.rejectValue("name", "form.plan.description.concrete");
        }
        if (plan.getTemplatePlan() == null){
            errors.rejectValue("templateplan", "form.plan.templateplan.required");
        }
        if (plan.getStatus() == null){
            errors.rejectValue("status", "form.plan.status.required");
        }
        if (!plan.getStatus().getCode().equals("draft")){
            errors.rejectValue("status", "form.plan.status.change.deprecate");
        }
        if (plan.getGroup() == null){
            errors.rejectValue("group", "form.plan.group.required");
        }
        if (plan.getCurator() == null){
            errors.rejectValue("curator", "form.plan.curator.required");
        }
    }
}
