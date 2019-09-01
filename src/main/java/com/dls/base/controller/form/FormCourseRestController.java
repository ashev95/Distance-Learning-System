package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.ArrayListBuilder;
import com.dls.base.utils.MoveUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.CourseValidator;
import com.dls.base.validator.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.dls.base.utils.Constant.ROLE_ADMINISTRATOR;

@RestController
public class FormCourseRestController
{

	private final CourseRepository courseRepository;
	private final PersonRepository personRepository;
	private final LifeCycleRepository lifeCycleRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final TemplateCourseRepository templateCourseRepository;
	private final MoveRepository moveRepository;
	private final StatusRepository statusRepository;
	private final GroupRepository groupRepository;
	private final GroupPersonRepository groupPersonRepository;
	private final TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository;
	private final TemplateLessonRepository templateLessonRepository;
	private final TemplateTestRepository templateTestRepository;
	private final LessonRepository lessonRepository;
	private final TestRepository testRepository;
	private final CourseResponseRepository courseResponseRepository;
	private final BlockRepository blockRepository;
	private final TemplateTestVariantRepository templateTestVariantRepository;


	@Autowired
	FormLessonRestController formLessonRestController;

	@Autowired
	FormTestRestController formTestRestController;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	MoveUtils moveUtils;
	
	@Autowired
	CourseValidator courseValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
    FormCourseRestController(CourseRepository courseRepository, PersonRepository personRepository, LifeCycleRepository lifeCycleRepository, TemplateLifeCycleRepository templateLifeCycleRepository, TemplateCourseRepository templateCourseRepository, MoveRepository moveRepository, StatusRepository statusRepository, GroupRepository groupRepository, GroupPersonRepository groupPersonRepository, TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository, TemplateLessonRepository templateLessonRepository, TemplateTestRepository templateTestRepository, LessonRepository lessonRepository, TestRepository testRepository, CourseResponseRepository courseResponseRepository, BlockRepository blockRepository, TemplateTestVariantRepository templateTestVariantRepository) {
		this.courseRepository = courseRepository;
		this.personRepository = personRepository;
		this.lifeCycleRepository = lifeCycleRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.templateCourseRepository = templateCourseRepository;
		this.moveRepository = moveRepository;
		this.statusRepository = statusRepository;
		this.groupRepository = groupRepository;
		this.groupPersonRepository = groupPersonRepository;
		this.templateCourseTemplateResponseRepository = templateCourseTemplateResponseRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.templateTestRepository = templateTestRepository;
		this.lessonRepository = lessonRepository;
		this.testRepository = testRepository;
		this.courseResponseRepository = courseResponseRepository;
		this.blockRepository = blockRepository;
		this.templateTestVariantRepository = templateTestVariantRepository;
	}

	@GetMapping(value = "form/course/templatecourse/{templateCourseId}/0", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getNewFormByIdentifier(@PathVariable Long templateCourseId) {
		Course course = new Course();
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(course.getClass().getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		course.setTemplateCourse(templateCourseRepository.findByTemplateCourseId(templateCourseId));
		course.setStatus(lifeCycle.getInitStatus());
		course.setCurator(accessUtils.getCurrentPerson());
		course.setAuthor(accessUtils.getCurrentPerson());
		FormTemplate formTemplate = getForm(course);
		return formTemplate;
	}

	@GetMapping(value = "form/course/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable Long id) throws Exception {
		Course course = courseRepository.findByCourseId(id);
		if (!accessUtils.canReadCard(course)){
			throw new Exception("Отсутствуют права на чтение карточки");
		}
		FormTemplate formTemplate = getForm(course);
		return formTemplate;
	}

	public FormTemplate getForm(Course course) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(course);
		formTemplate.tabTitle = course.getName();
		formTemplate.template = course.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/course/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody Course course, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		course.setTemplateCourse(templateCourseRepository.findByTemplateCourseId(course.getTemplateCourse().getId()));
		course.setGroup(groupRepository.findByGroupId(course.getGroup().getId()));
		course.setStatus(statusRepository.findByStatusId(course.getStatus().getId()));
		course.setCurator(personRepository.findByPersonId(course.getCurator().getId()));
		course.setAuthor(personRepository.findByPersonId(course.getAuthor().getId()));
		if (!accessUtils.canEditCard(course)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!course.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + course.getClass().getSimpleName().toLowerCase() + ", id = " + course.getId() + "]");
		}
		courseValidator.validate(course, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Course newCourse = new Course();
		newCourse.setName(course.getName());
		newCourse.setDescription(course.getDescription());
		newCourse.setTemplateCourse(course.getTemplateCourse());
		newCourse.setStatus(course.getStatus());
		newCourse.setGroup(course.getGroup());
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			newCourse.setCurator(course.getCurator());
		}else{
			newCourse.setCurator(accessUtils.getCurrentPerson());
		}
		newCourse.setAuthor(course.getAuthor());
		newCourse.setDateCreate(new Date());
		Course savedCourse = courseRepository.save(newCourse);
		for (Block block : blockRepository.findBlockByTemplateCourseId(savedCourse.getTemplateCourse().getId())){
			Block block1 = new Block();
			block1.setParentId(savedCourse.getId());
			block1.setParentClass(savedCourse.getClass().getSimpleName().toLowerCase());
			block1.setPosition(block.getPosition());
			block1.setType(block.getType());
			blockRepository.save(block1);
			Set<TemplateCourseTemplateResponse> templateCourseTemplateResponseSet = templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByBlockId(block.getId());
			for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseSet){
				CourseResponse courseResponse = new CourseResponse();
				courseResponse.setPosition(templateCourseTemplateResponse.getPosition());
				courseResponse.setCourse(savedCourse);
				courseResponse.setBlock(block1);
				switch (templateCourseTemplateResponse.getTemplateResponseClass()){
					case "templatelesson":
						TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(templateCourseTemplateResponse.getTemplateResponseId());
						Lesson newLesson = new Lesson();
						newLesson.setName(templateLesson.getName());
						newLesson.setDescription(templateLesson.getDescription());
						newLesson.setTemplateLesson(templateLesson);
						newLesson.setStatus(statusRepository.findByStatusCode("draft"));
						newLesson.setGroup(savedCourse.getGroup());
						newLesson.setCurator(savedCourse.getCurator());
						Lesson savedLesson = lessonRepository.save(newLesson);
						//
						formLessonRestController.createLessonPersonFull(savedLesson);
						//
						courseResponse.setResponseClass(newLesson.getClass().getSimpleName().toLowerCase());
						courseResponse.setResponseId(savedLesson.getId());
						break;
					case "templatetest":
						TemplateTest templateTest = templateTestRepository.findByTemplateTestId(templateCourseTemplateResponse.getTemplateResponseId());
						Test newTest = new Test();
						newTest.setName(templateTest.getName());
						newTest.setDescription(templateTest.getDescription());
						newTest.setTemplateTest(templateTest);
						newTest.setStatus(statusRepository.findByStatusCode("draft"));
						newTest.setGroup(savedCourse.getGroup());
						newTest.setCurator(savedCourse.getCurator());
						Test savedTest = testRepository.save(newTest);
						//
						formTestRestController.createTestVariantFull(savedTest);
						//
						courseResponse.setResponseClass(newTest.getClass().getSimpleName().toLowerCase());
						courseResponse.setResponseId(savedTest.getId());
						break;
					default:
						throw new Exception("Не определён тип мероприятия");
				}
				courseResponseRepository.save(courseResponse);
			}
		}
		formTemplate = getFormByIdentifier(savedCourse.getId());
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

	@PostMapping(value = "form/course/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody Course course, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		Course updateCourse = courseRepository.findByCourseId(id);
		updateCourse.setName(course.getName());
		updateCourse.setDescription(course.getDescription());
		Person curator = personRepository.findByPersonId(course.getCurator().getId());
		updateCourse.setCurator(curator);
		Person author = personRepository.findByPersonId(course.getAuthor().getId());
		updateCourse.setAuthor(author);
		Status status = statusRepository.findByStatusId(course.getStatus().getId());
		updateCourse.setStatus(status);
		Group oldGroup = updateCourse.getGroup();
		Group group = groupRepository.findByGroupId(course.getGroup().getId());
		updateCourse.setGroup(group);
		course.setStatus(statusRepository.findByStatusId(course.getStatus().getId()));
		if (!accessUtils.canEditCard(course)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!course.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + course.getClass().getSimpleName().toLowerCase() + ", id = " + course.getId() + "]");
		}
		courseValidator.validate(updateCourse, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Course savedCourse = courseRepository.save(updateCourse);
		//
		if (!oldGroup.getId().equals(updateCourse.getGroup().getId())){
			Set<CourseResponse> courseResponseSet = courseResponseRepository.findByCourseId(savedCourse.getId());
			for (CourseResponse courseResponse : courseResponseSet){
				switch (courseResponse.getResponseClass()){
					case "test":
						Test test = testRepository.findByTestId(courseResponse.getResponseId());
						test.setGroup(updateCourse.getGroup());
						Test savedTest = testRepository.save(test);
						//
						formTestRestController.reCreateTestVariantFull(savedTest);
						//
						break;
					case "lesson":
						Lesson lesson = lessonRepository.findByLessonId(courseResponse.getResponseId());
						lesson.setGroup(updateCourse.getGroup());
						Lesson savedLesson = lessonRepository.save(lesson);
						//
						formLessonRestController.reCreateLessonPersonFull(savedLesson);
						//
						break;
				}
			}
		}
		//
		formTemplate = getFormByIdentifier(savedCourse.getId());
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

	@PostMapping(value = "form/course/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		Course updateCourse = courseRepository.findByCourseId(id);
		//if (!accessUtils.canEditCard(updateCourse)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updateCourse.getClass().getSimpleName().toLowerCase(), updateCourse.getStatus().getId(), toStatus.getId());
		if (move != null){
			updateCourse.setStatus(move.getToStatus());
			if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("in_progress")){
				utils.checkTemplate(updateCourse, true);
				if (groupPersonRepository.findByGroupId(updateCourse.getGroup().getId()).size() == 0){
					throw new Exception("Отсутствуют учащиеся");
				}
				updateCourse.setDateStart(new Date());
			}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("canceled")){
				updateCourse.setDateEnd(new Date());
			}else if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("canceled")){
				updateCourse.setDateStart(new Date());
				updateCourse.setDateEnd(new Date());
			}
			Course savedCourse = courseRepository.save(updateCourse);
			TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(Lesson.class.getSimpleName().toLowerCase());
			LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();

			if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("in_progress")){
				moveUtils.startFirstBlock(savedCourse);
			}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("canceled")){
				Set<CourseResponse> courseResponseSet = courseResponseRepository.findByCourseId(savedCourse.getId());
				for (CourseResponse courseResponse : courseResponseSet){
					switch (courseResponse.getResponseClass()){
						case "lesson":
							formLessonRestController.toStatus(courseResponse.getResponseId(), "canceled");
							break;
						case "test":
							formTestRestController.toStatus(courseResponse.getResponseId(), "canceled");
							break;
						default:
							throw new Exception("Не определён тип дочернего мероприятия");
					}
				}
			}
			formTemplate = getFormByIdentifier(savedCourse.getId());
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(formTemplate);
		}
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return null;
	}

}