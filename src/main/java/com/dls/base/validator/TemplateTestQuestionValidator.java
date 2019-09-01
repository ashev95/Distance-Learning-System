package com.dls.base.validator;

import com.dls.base.entity.TemplateTestQuestion;
import com.dls.base.repository.TemplateTestQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TemplateTestQuestionValidator implements Validator {

    @Autowired
    private TemplateTestQuestionRepository templateTestQuestionRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return TemplateTestQuestion.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TemplateTestQuestion templateTestQuestion = (TemplateTestQuestion) o;
        if (templateTestQuestion.getNumber() == null){
            errors.rejectValue("number", "form.templatetestquestion.number.required");
        }
        if (templateTestQuestion.getNumber() <= 0L && templateTestQuestion.getNumber() >= 100L){
            errors.rejectValue("number", "form.templatetestquestion.number.concrete");
        }
        Set<TemplateTestQuestion> templateTestQuestionList = templateTestQuestionRepository.findByTemplateTestVariantIdAndNumber(templateTestQuestion.getTemplateTestVariant().getId(), templateTestQuestion.getNumber());
        if (templateTestQuestionList.size() > 0){
            if (templateTestQuestionList.size() == 1){
                if (!templateTestQuestion.getId().equals(templateTestQuestionList.iterator().next().getId())){
                    errors.rejectValue("number", "form.templatetestquestion.number.exist");
                }
            }else{
                errors.rejectValue("number", "form.templatetestquestion.number.exist");
            }
        }
        if (templateTestQuestion.getContent() == null){
            errors.rejectValue("content", "form.templatetestquestion.content.empty");
        }
        if (templateTestQuestion.getContent().isEmpty()){
            errors.rejectValue("content", "form.templatetestquestion.content.empty");
        }
        if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(templateTestQuestion.getContent()).matches()){
            errors.rejectValue("content", "form.templatetestquestion.content.concrete");
        }
        if (templateTestQuestion.getScorePositive() == null){
            errors.rejectValue("scorePositive", "form.templatetestquestion.score.positive.empty");
        }
        if (templateTestQuestion.getScorePositive() < 0 || templateTestQuestion.getScorePositive() > 100){
            errors.rejectValue("scorePositive", "form.templatetestquestion.score.positive.concrete");
        }
        if (templateTestQuestion.getScoreNegative() == null){
            errors.rejectValue("scoreNegative", "form.templatetestquestion.score.negative.empty");
        }
        if (templateTestQuestion.getScoreNegative() < 0 || templateTestQuestion.getScoreNegative() > 100){
            errors.rejectValue("scoreNegative", "form.templatetestquestion.score.negative.concrete");
        }
        if (templateTestQuestion.getAuthor() == null){
            errors.rejectValue("author", "form.templatetestquestion.author.required");
        }
    }
}
