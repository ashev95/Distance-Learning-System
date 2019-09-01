package com.dls.base.validator;

import com.dls.base.entity.TemplateTestVariant;
import com.dls.base.repository.TemplateTestVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;

@Component
public class TemplateTestVariantValidator implements Validator {

    @Autowired
    private TemplateTestVariantRepository templateTestVariantRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return TemplateTestVariant.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TemplateTestVariant templateTestVariant = (TemplateTestVariant) o;
        if (templateTestVariant.getNumber() == null){
            errors.rejectValue("number", "form.templatetestvariant.number.required");
        }
        if (templateTestVariant.getNumber() <= 0L && templateTestVariant.getNumber() >= 100L){
            errors.rejectValue("number", "form.templatetestvariant.number.concrete");
        }
        Set<TemplateTestVariant> templateTestVariantList = templateTestVariantRepository.findByTemplateTestIdAndNumber(templateTestVariant.getTemplateTest().getId(), templateTestVariant.getNumber());
        if (templateTestVariantList.size() > 0){
            if (templateTestVariantList.size() == 1){
                if (!templateTestVariant.getId().equals(templateTestVariantList.iterator().next().getId())){
                    errors.rejectValue("number", "form.templatetestvariant.number.exist");
                }
            }else{
                errors.rejectValue("number", "form.templatetestvariant.number.exist");
            }
        }
        if (templateTestVariant.getAuthor() == null){
            errors.rejectValue("author", "form.templatetestvariant.author.required");
        }
    }
}
