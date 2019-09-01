package com.dls.base.validator;

import com.dls.base.entity.Category;
import com.dls.base.repository.CategoryRepository;
import com.dls.base.utils.Constant;
import com.dls.base.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class CategoryValidator implements Validator {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
	private Utils utils;

    @Override
    public boolean supports(Class<?> aClass) {
        return Category.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Category category = (Category) o;
		if (category.getName().trim().isEmpty()){
			errors.rejectValue("name", "form.category.name.required");
		}
		if (!Pattern.compile("^.{1,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(category.getName()).matches()){
			errors.rejectValue("name", "form.category.name.concrete");
		}
		if (category.getName().trim().isEmpty()){
			errors.rejectValue("name", "form.category.name.required");
		}
		if (category.getName().equals(Constant.EMPTY_CATEGORY_NAME)){
			errors.rejectValue("name", "form.category.name.deprecated");
		}
		if (categoryRepository.findByName(category.getName()) != null){
			if (!categoryRepository.findByName(category.getName()).getId().equals(category.getId())){
				errors.rejectValue("name", "form.category.name.exist");
			}
		}
		if (category.getDescription() != null){
			if (!category.getDescription().isEmpty()){
				if (!Pattern.compile("^.{0,255}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(category.getDescription()).matches()){
					errors.rejectValue("description", "form.category.description.concrete");
				}
			}
		}
    }
}
