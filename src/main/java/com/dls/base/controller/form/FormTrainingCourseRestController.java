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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;

@RestController
public class FormTrainingCourseRestController
{
	private final PersonRepository personRepository;
	private final StatusRepository statusRepository;
	private final MoveRepository moveRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final LessonRepository lessonRepository;
	private final TestRepository testRepository;
	private final CourseResponseRepository courseResponseRepository;


	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	MoveUtils moveUtils;

	@Autowired
	FormLessonRestController formLessonRestController;

	@Autowired
	FormTestRestController formTestRestController;

	@Autowired
    FormTrainingCourseRestController(PersonRepository personRepository, StatusRepository statusRepository, MoveRepository moveRepository, TemplateLifeCycleRepository templateLifeCycleRepository, LessonRepository lessonRepository, TestRepository testRepository, CourseResponseRepository courseResponseRepository) {
		this.personRepository = personRepository;
		this.testRepository = testRepository;
		this.statusRepository = statusRepository;
		this.moveRepository = moveRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.lessonRepository = lessonRepository;
		this.courseResponseRepository = courseResponseRepository;
	}

	@GetMapping(value = "form/trainingcourse/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		CourseResponse courseResponse = courseResponseRepository.findByCourseResponseId(id);
		if (!accessUtils.canReadCard(courseResponse)){
			throw new Exception("Отсутствуют права на чтение карточки");
		}
		FormTemplate formTemplate = getForm(courseResponse);
		return formTemplate;
	}

	public FormTemplate getForm(CourseResponse courseResponse) throws Exception {
		FormTemplate formTemplate = new FormTemplate();
		switch (courseResponse.getResponseClass()){
			case "lesson":
				TemplateLesson templateLesson = (lessonRepository.findByLessonId(courseResponse.getResponseId())).getTemplateLesson();
				formTemplate.attributes = utils.getFormAttributes(templateLesson);
				//
				formTemplate.tabTitle = templateLesson.getName();
				formTemplate.template = templateLesson.getClass().getSimpleName().toLowerCase();
				break;
			case "test":
				TemplateTest templateTest = (testRepository.findByTestId(courseResponse.getResponseId())).getTemplateTest();
				formTemplate.attributes = utils.getFormAttributes(templateTest);
				//
				formTemplate.tabTitle = templateTest.getName();
				formTemplate.template = templateTest.getClass().getSimpleName().toLowerCase();
				break;
			default:
				throw new Exception("Не определён тип дочернего мероприятия");
		}
		//
		HashMap<String, FormAttribute> courseResponseAttributes = utils.getFormAttributes(courseResponse);
		formTemplate.attributes.put("original_id", courseResponseAttributes.get("id"));
		FormAttribute formAttribute = new FormAttribute();
		formAttribute.name = "original_type";
		formAttribute.type = "string";
		formAttribute.value = "trainingcourse";
		formAttribute.title = "original_type";
		formTemplate.attributes.put("original_type", formAttribute);
		formTemplate.attributes.put("original_status", courseResponseAttributes.get("status"));
		//
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PostMapping(value = "form/trainingcourse/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		CourseResponse updateCourseResponse = courseResponseRepository.findByCourseResponseId(id);
		//if (!accessUtils.canEditCard(updateCourseResponse)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Date d = new Date();
		switch (updateCourseResponse.getResponseClass()){
			case "lesson":
				Lesson lesson = lessonRepository.findByLessonId(updateCourseResponse.getResponseId());
				formLessonRestController.toStatus(lesson.getId(), toStatus.getCode());
				break;
			case "test":
				Test test = testRepository.findByTestId(updateCourseResponse.getResponseId());
				formTestRestController.toStatus(test.getId(), toStatus.getCode());
				break;
			default:
				throw new Exception("Не определён тип дочернего мероприятия");
		}
		formTemplate = getFormByIdentifier(updateCourseResponse.getId().toString());
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