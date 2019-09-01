package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.ui.form.FormAttribute;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.MoveUtils;
import com.dls.base.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;

@RestController
public class FormTrainingLessonRestController
{

	private final LessonPersonRepository lessonPersonRepository;
	private final PersonRepository personRepository;
	private final StatusRepository statusRepository;
	private final MoveRepository moveRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final LessonRepository lessonRepository;
	private final CourseResponseRepository courseResponseRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	MoveUtils moveUtils;

	@Autowired
    FormTrainingLessonRestController(LessonPersonRepository lessonPersonRepository, PersonRepository personRepository, StatusRepository statusRepository, MoveRepository moveRepository, TemplateLifeCycleRepository templateLifeCycleRepository, LessonRepository lessonRepository, CourseResponseRepository courseResponseRepository) {
		this.lessonPersonRepository = lessonPersonRepository;
		this.personRepository = personRepository;
		this.statusRepository = statusRepository;
		this.moveRepository = moveRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.lessonRepository = lessonRepository;
		this.courseResponseRepository = courseResponseRepository;
	}

	@GetMapping(value = "form/traininglesson/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		LessonPerson lessonPerson;
		Long id = Long.parseLong(identifier);
		lessonPerson = lessonPersonRepository.findByLessonPersonId(id);
		if (!accessUtils.canReadCard(lessonPerson)){
			throw new Exception("Отсутствуют права на чтение карточки");
		}
		FormTemplate formTemplate = getForm(lessonPerson);
		return formTemplate;
	}

	public FormTemplate getForm(LessonPerson lessonPerson) {
		FormTemplate formTemplate = new FormTemplate();
		TemplateLesson templateLesson = lessonPerson.getLesson().getTemplateLesson();
		formTemplate.attributes = utils.getFormAttributes(templateLesson);
		//
		HashMap<String, FormAttribute> lessonPersonAttributes = utils.getFormAttributes(lessonPerson);
		formTemplate.attributes.put("original_id", lessonPersonAttributes.get("id"));
		FormAttribute formAttribute = new FormAttribute();
		formAttribute.name = "original_type";
		formAttribute.type = "string";
		formAttribute.value = "traininglesson";
		formAttribute.title = "original_type";
		formTemplate.attributes.put("original_type", formAttribute);
		formTemplate.attributes.put("original_status", lessonPersonAttributes.get("status"));
		//
		formTemplate.tabTitle = templateLesson.getName();
		formTemplate.template = templateLesson.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PostMapping(value = "form/traininglesson/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		LessonPerson updateLessonPerson = lessonPersonRepository.findByLessonPersonId(id);
		//if (!accessUtils.canEditCard(updateLessonPerson)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updateLessonPerson.getClass().getSimpleName().toLowerCase(), updateLessonPerson.getStatus().getId(), toStatus.getId());
		updateLessonPerson.setStatus(move.getToStatus());
		if (move.getToStatus().getCode().equals("studied") || move.getToStatus().getCode().equals("canceled")){
			Date d = new Date();
			updateLessonPerson.setDateEnd(d);
		}
		LessonPerson savedLessonPerson = lessonPersonRepository.save(updateLessonPerson);
		if (move.getFromStatus().getCode().equals("assigned") && (move.getToStatus().getCode().equals("studied") || move.getToStatus().getCode().equals("canceled"))){
			moveUtils.processTrainingParents(savedLessonPerson);
		}
		formTemplate = getFormByIdentifier(savedLessonPerson.getId().toString());
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