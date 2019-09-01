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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;

@RestController
public class FormTrainingPlanRestController
{
	private final PersonRepository personRepository;
	private final StatusRepository statusRepository;
	private final MoveRepository moveRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final LessonRepository lessonRepository;
	private final TestRepository testRepository;
	private final PlanResponseRepository planResponseRepository;
	private final CourseRepository courseRepository;
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
	FormCourseRestController formCourseRestController;

	@Autowired
    FormTrainingPlanRestController(PersonRepository personRepository, StatusRepository statusRepository, MoveRepository moveRepository, TemplateLifeCycleRepository templateLifeCycleRepository, LessonRepository lessonRepository, TestRepository testRepository, PlanResponseRepository planResponseRepository, CourseRepository courseRepository, CourseResponseRepository courseResponseRepository) {
		this.personRepository = personRepository;
		this.testRepository = testRepository;
		this.statusRepository = statusRepository;
		this.moveRepository = moveRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.lessonRepository = lessonRepository;
		this.planResponseRepository = planResponseRepository;
		this.courseRepository = courseRepository;
		this.courseResponseRepository = courseResponseRepository;
	}

	@GetMapping(value = "form/trainingplan/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		PlanResponse planResponse = planResponseRepository.findByPlanResponseId(id);
		if (!accessUtils.canReadCard(planResponse)){
			throw new Exception("Отсутствуют права на чтение карточки");
		}
		FormTemplate formTemplate = getForm(planResponse);
		return formTemplate;
	}

	public FormTemplate getForm(PlanResponse planResponse) throws Exception {
		FormTemplate formTemplate = new FormTemplate();
		switch (planResponse.getResponseClass()){
			case "lesson":
				TemplateLesson templateLesson = (lessonRepository.findByLessonId(planResponse.getResponseId())).getTemplateLesson();
				formTemplate.attributes = utils.getFormAttributes(templateLesson);
				//
				formTemplate.tabTitle = templateLesson.getName();
				formTemplate.template = templateLesson.getClass().getSimpleName().toLowerCase();
				break;
			case "test":
				TemplateTest templateTest = (testRepository.findByTestId(planResponse.getResponseId())).getTemplateTest();
				formTemplate.attributes = utils.getFormAttributes(templateTest);
				//
				formTemplate.tabTitle = templateTest.getName();
				formTemplate.template = templateTest.getClass().getSimpleName().toLowerCase();
				break;
			case "course":
				TemplateCourse templateCourse = (courseRepository.findByCourseId(planResponse.getResponseId())).getTemplateCourse();
				formTemplate.attributes = utils.getFormAttributes(templateCourse);
				//
				formTemplate.tabTitle = templateCourse.getName();
				formTemplate.template = templateCourse.getClass().getSimpleName().toLowerCase();
				break;
			default:
				throw new Exception("Не определён тип дочернего мероприятия");
		}
		//
		HashMap<String, FormAttribute> planResponseAttributes = utils.getFormAttributes(planResponse);
		formTemplate.attributes.put("original_id", planResponseAttributes.get("id"));
		FormAttribute formAttribute = new FormAttribute();
		formAttribute.name = "original_type";
		formAttribute.type = "string";
		formAttribute.value = "trainingplan";
		formAttribute.title = "original_type";
		formTemplate.attributes.put("original_type", formAttribute);
		formTemplate.attributes.put("original_status", planResponseAttributes.get("status"));
		//
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PostMapping(value = "form/trainingplan/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		PlanResponse updatePlanResponse = planResponseRepository.findByPlanResponseId(id);
		//if (!accessUtils.canEditCard(updatePlanResponse)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Date d = new Date();
		switch (updatePlanResponse.getResponseClass()){
			case "lesson":
				formLessonRestController.toStatus(updatePlanResponse.getResponseId(), "canceled");
				break;
			case "test":
				formTestRestController.toStatus(updatePlanResponse.getResponseId(), "canceled");
				break;
			case "course":
				formCourseRestController.toStatus(updatePlanResponse.getResponseId(), "canceled");
				break;
			default:
				throw new Exception("Не определён тип дочернего мероприятия");
		}
		formTemplate = getFormByIdentifier(updatePlanResponse.getId().toString());
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