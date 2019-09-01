package com.dls.base.validator;

import com.dls.base.entity.TemplateTestAnswer;
import com.dls.base.repository.TemplateTestAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TemplateTestAnswerValidator implements Validator {

    @Autowired
    private TemplateTestAnswerRepository templateTestAnswerRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return TemplateTestAnswer.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TemplateTestAnswer templateTestAnswer = (TemplateTestAnswer) o;
        if (templateTestAnswer.getNumber() == null){
            errors.rejectValue("number", "form.templatetestanswer.number.required");
        }
        if (templateTestAnswer.getNumber() <= 0L && templateTestAnswer.getNumber() >= 100L){
            errors.rejectValue("number", "form.templatetestanswer.number.concrete");
        }
        Set<TemplateTestAnswer> templateTestAnswerList = templateTestAnswerRepository.findByTemplateTestQuestionIdAndNumber(templateTestAnswer.getTemplateTestQuestion().getId(), templateTestAnswer.getNumber());
        if (templateTestAnswerList.size() > 0){
            if (templateTestAnswerList.size() == 1){
                if (!templateTestAnswer.getId().equals(templateTestAnswerList.iterator().next().getId())){
                    errors.rejectValue("number", "form.templatetestanswer.number.exist");
                }
            }else{
                errors.rejectValue("number", "form.templatetestanswer.number.exist");
            }
        }
        if (templateTestAnswer.getContent() == null){
            errors.rejectValue("content", "form.templatetestanswer.content.empty");
        }
        if (templateTestAnswer.getContent().isEmpty()){
            errors.rejectValue("content", "form.templatetestanswer.content.empty");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templateTestAnswer.getContent()).matches()){
            errors.rejectValue("content", "form.templatetestanswer.content.concrete");
        }
        if (templateTestAnswer.getAuthor() == null){
            errors.rejectValue("author", "form.templatetestanswer.author.required");
        }
    }
}
