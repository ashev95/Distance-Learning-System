package com.dls.base.controller.form;

import com.dls.base.entity.Category;
import com.dls.base.repository.CategoryRepository;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.CategoryValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class FormCategoryRestController
{

	private final CategoryRepository categoryRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	CategoryValidator categoryValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
    FormCategoryRestController(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@GetMapping(value = "form/category/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		Category category;
		Long id = Long.parseLong(identifier);
		if (id == 0){
			category = new Category();
		}else{
			category = categoryRepository.findByCategoryId(id);
			if (!accessUtils.canReadCard(category)){
				throw new Exception("Отсутствуют права на чтение карточки");
			}
		}
		FormTemplate formTemplate = getForm(category);
		return formTemplate;
	}

	public FormTemplate getForm(Category category) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(category);
		formTemplate.tabTitle = category.getName();
		formTemplate.template = category.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/category/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody Category category, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		if (!accessUtils.canEditCard(category)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		categoryValidator.validate(category, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Category newCategory = new Category();
		newCategory.setName(category.getName());
		newCategory.setDescription(category.getDescription());
		Category savedCategory = categoryRepository.save(newCategory);
		formTemplate = getFormByIdentifier(savedCategory.getId().toString());
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(formTemplate);
	}

	@PostMapping(value = "form/category/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody Category category, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		if (!accessUtils.canEditCard(category)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		categoryValidator.validate(category, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Category updateCategory = categoryRepository.findByCategoryId(id);
		updateCategory.setName(category.getName());
		updateCategory.setDescription(category.getDescription());
		Category savedCategory = categoryRepository.save(updateCategory);
		formTemplate = getFormByIdentifier(savedCategory.getId().toString());
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(formTemplate);
	}

}