package com.dls.base.validator;

import com.dls.base.entity.TemplateTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class TemplateTestValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return TemplateTest.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TemplateTest templateTest = (TemplateTest) o;
        if (templateTest.getName().trim().isEmpty()){
            errors.rejectValue("name", "form.templatetest.name.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templateTest.getName()).matches()){
            errors.rejectValue("name", "form.templatetest.name.concrete");
        }
        if (templateTest.getDescription().trim().isEmpty()){
            errors.rejectValue("description", "form.templatetest.description.required");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templateTest.getDescription()).matches()){
            errors.rejectValue("description", "form.templatetest.description.concrete");
        }
        if (!templateTest.getByOrder()){
            if (templateTest.getDeprecateChangeAnswerCount() == null){
                errors.rejectValue("deprecateChangeAnswerCount", "form.templatetest.deprecateChangeAnswerCount.concrete");
            }
            if (templateTest.getDeprecateChangeAnswerCount() < 0 || templateTest.getDeprecateChangeAnswerCount() > 100){
                errors.rejectValue("deprecateChangeAnswerCount", "form.templatetest.deprecateChangeAnswerCount.concrete");
            }
        }
        if (templateTest.getTimeLimit() < 0 || templateTest.getTimeLimit() > 10000){
            errors.rejectValue("timeLimit", "form.templatetest.timeLimit.concrete");
        }
        if (templateTest.getAuthor() == null){
            errors.rejectValue("author", "form.templatetest.author.required");
        }
    }
}
