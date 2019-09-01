package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.ArrayListBuilder;
import com.dls.base.utils.MoveUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.PlanValidator;
import com.dls.base.validator.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Set;

import static com.dls.base.utils.Constant.ROLE_ADMINISTRATOR;

@RestController
public class FormPlanRestController
{

	private final PlanRepository planRepository;
	private final PersonRepository personRepository;
	private final LifeCycleRepository lifeCycleRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final TemplatePlanRepository templatePlanRepository;
	private final MoveRepository moveRepository;
	private final StatusRepository statusRepository;
	private final GroupRepository groupRepository;
	private final GroupPersonRepository groupPersonRepository;
	private final TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository;
	private final TemplateLessonRepository templateLessonRepository;
	private final TemplateTestRepository templateTestRepository;
	private final LessonRepository lessonRepository;
	private final TestRepository testRepository;
	private final PlanResponseRepository planResponseRepository;
	private final TemplateCourseRepository templateCourseRepository;
	private final CourseRepository courseRepository;
	private final TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository;
	private final CourseResponseRepository courseResponseRepository;
	private final BlockRepository blockRepository;


	@Autowired
	FormLessonRestController formLessonRestController;

	@Autowired
	FormTestRestController formTestRestController;

	@Autowired
	FormCourseRestController formCourseRestController;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	MoveUtils moveUtils;
	
	@Autowired
	PlanValidator planValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
    FormPlanRestController(PlanRepository planRepository, PersonRepository personRepository, LifeCycleRepository lifeCycleRepository, TemplateLifeCycleRepository templateLifeCycleRepository, TemplatePlanRepository templatePlanRepository, MoveRepository moveRepository, StatusRepository statusRepository, GroupRepository groupRepository, GroupPersonRepository groupPersonRepository, TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository, TemplateLessonRepository templateLessonRepository, TemplateTestRepository templateTestRepository, LessonRepository lessonRepository, TestRepository testRepository, PlanResponseRepository planResponseRepository, TemplateCourseRepository templateCourseRepository, CourseRepository courseRepository, TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository, CourseResponseRepository courseResponseRepository, BlockRepository blockRepository) {
		this.planRepository = planRepository;
		this.personRepository = personRepository;
		this.lifeCycleRepository = lifeCycleRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.templatePlanRepository = templatePlanRepository;
		this.moveRepository = moveRepository;
		this.statusRepository = statusRepository;
		this.groupRepository = groupRepository;
		this.groupPersonRepository = groupPersonRepository;
		this.templatePlanTemplateResponseRepository = templatePlanTemplateResponseRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.templateTestRepository = templateTestRepository;
		this.lessonRepository = lessonRepository;
		this.testRepository = testRepository;
		this.planResponseRepository = planResponseRepository;
		this.templateCourseRepository = templateCourseRepository;
		this.courseRepository = courseRepository;
		this.templateCourseTemplateResponseRepository = templateCourseTemplateResponseRepository;
		this.courseResponseRepository = courseResponseRepository;
		this.blockRepository = blockRepository;
	}

	@GetMapping(value = "form/plan/templateplan/{templatePlanId}/0", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getNewFormByIdentifier(@PathVariable Long templatePlanId) {
		Plan plan = new Plan();
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(plan.getClass().getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		plan.setTemplatePlan(templatePlanRepository.findByTemplatePlanId(templatePlanId));
		plan.setStatus(lifeCycle.getInitStatus());
		plan.setCurator(accessUtils.getCurrentPerson());
		plan.setAuthor(accessUtils.getCurrentPerson());
		FormTemplate formTemplate = getForm(plan);
		return formTemplate;
	}

	@GetMapping(value = "form/plan/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable Long id) throws Exception {
		Plan plan = planRepository.findByPlanId(id);
		if (!accessUtils.canReadCard(plan)){
			throw new Exception("Отсутствуют права на чтение карточки");
		}
		FormTemplate formTemplate = getForm(plan);
		return formTemplate;
	}

	public FormTemplate getForm(Plan plan) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(plan);
		formTemplate.tabTitle = plan.getName();
		formTemplate.template = plan.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/plan/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody Plan plan, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		plan.setTemplatePlan(templatePlanRepository.findByTemplatePlanId(plan.getTemplatePlan().getId()));
		plan.setGroup(groupRepository.findByGroupId(plan.getGroup().getId()));
		plan.setStatus(statusRepository.findByStatusId(plan.getStatus().getId()));
		plan.setCurator(personRepository.findByPersonId(plan.getCurator().getId()));
		plan.setAuthor(personRepository.findByPersonId(plan.getAuthor().getId()));
		if (!accessUtils.canEditCard(plan)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!plan.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + plan.getClass().getSimpleName().toLowerCase() + ", id = " + plan.getId() + "]");
		}
		planValidator.validate(plan, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Plan newPlan = new Plan();
		newPlan.setName(plan.getName());
		newPlan.setDescription(plan.getDescription());
		newPlan.setTemplatePlan(plan.getTemplatePlan());
		newPlan.setStatus(plan.getStatus());
		newPlan.setGroup(plan.getGroup());
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			newPlan.setCurator(plan.getCurator());
		}else{
			newPlan.setCurator(accessUtils.getCurrentPerson());
		}
		newPlan.setAuthor(plan.getAuthor());
		newPlan.setDateCreate(new Date());
		Plan savedPlan = planRepository.save(newPlan);

		for (Block block : blockRepository.findBlockByTemplatePlanId(savedPlan.getTemplatePlan().getId())){
			Block block1 = new Block();
			block1.setParentId(savedPlan.getId());
			block1.setParentClass(savedPlan.getClass().getSimpleName().toLowerCase());
			block1.setPosition(block.getPosition());
			block1.setType(block.getType());
			blockRepository.save(block1);
			Set<TemplatePlanTemplateResponse> templatePlanTemplateResponseSet = templatePlanTemplateResponseRepository.findTemplatePlanTemplateResponseByBlockId(block.getId());
			for (TemplatePlanTemplateResponse templatePlanTemplateResponse : templatePlanTemplateResponseSet){
				PlanResponse planResponse = new PlanResponse();
				planResponse.setPosition(templatePlanTemplateResponse.getPosition());
				planResponse.setPlan(savedPlan);
				planResponse.setBlock(block1);
				switch (templatePlanTemplateResponse.getTemplateResponseClass()){
					case "templatelesson":
						TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(templatePlanTemplateResponse.getTemplateResponseId());
						Lesson newLesson = new Lesson();
						newLesson.setName(templateLesson.getName());
						newLesson.setDescription(templateLesson.getDescription());
						newLesson.setTemplateLesson(templateLesson);
						newLesson.setStatus(statusRepository.findByStatusCode("draft"));
						newLesson.setGroup(savedPlan.getGroup());
						newLesson.setCurator(savedPlan.getCurator());
						Lesson savedLesson = lessonRepository.save(newLesson);
						//
						formLessonRestController.createLessonPersonFull(savedLesson);
						//
						planResponse.setResponseClass(newLesson.getClass().getSimpleName().toLowerCase());
						planResponse.setResponseId(savedLesson.getId());
						break;
					case "templatetest":
						TemplateTest templateTest = templateTestRepository.findByTemplateTestId(templatePlanTemplateResponse.getTemplateResponseId());
						Test newTest = new Test();
						newTest.setName(templateTest.getName());
						newTest.setDescription(templateTest.getDescription());
						newTest.setTemplateTest(templateTest);
						newTest.setStatus(statusRepository.findByStatusCode("draft"));
						newTest.setGroup(savedPlan.getGroup());
						newTest.setCurator(savedPlan.getCurator());
						Test savedTest = testRepository.save(newTest);
						//
						formTestRestController.createTestVariantFull(savedTest);
						//
						planResponse.setResponseClass(newTest.getClass().getSimpleName().toLowerCase());
						planResponse.setResponseId(savedTest.getId());
						break;
					case "templatecourse":
						TemplateCourse templateCourse = templateCourseRepository.findByTemplateCourseId(templatePlanTemplateResponse.getTemplateResponseId());
						Course newCourse = new Course();
						newCourse.setName(templateCourse.getName());
						newCourse.setDescription(templateCourse.getDescription());
						newCourse.setTemplateCourse(templateCourse);
						newCourse.setStatus(statusRepository.findByStatusCode("draft"));
						newCourse.setGroup(savedPlan.getGroup());
						newCourse.setCurator(savedPlan.getCurator());
						Course savedCourse = courseRepository.save(newCourse);
						//
						for (Block block2 : blockRepository.findBlockByTemplateCourseId(savedCourse.getTemplateCourse().getId())){
							Block block3 = new Block();
							block3.setParentId(savedCourse.getId());
							block3.setParentClass(savedCourse.getClass().getSimpleName().toLowerCase());
							block3.setPosition(block2.getPosition());
							block3.setType(block2.getType());
							blockRepository.save(block3);
							Set<TemplateCourseTemplateResponse> templateCourseTemplateResponseSet = templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByBlockId(block2.getId());
							for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseSet){
								CourseResponse courseResponse = new CourseResponse();
								courseResponse.setPosition(templateCourseTemplateResponse.getPosition());
								courseResponse.setCourse(savedCourse);
								courseResponse.setBlock(block3);
								switch (templateCourseTemplateResponse.getTemplateResponseClass()){
									case "templatelesson":
										TemplateLesson templateLesson1 = templateLessonRepository.findByTemplateLessonId(templateCourseTemplateResponse.getTemplateResponseId());
										Lesson newLesson1 = new Lesson();
										newLesson1.setName(templateLesson1.getName());
										newLesson1.setDescription(templateLesson1.getDescription());
										newLesson1.setTemplateLesson(templateLesson1);
										newLesson1.setStatus(statusRepository.findByStatusCode("draft"));
										newLesson1.setGroup(savedCourse.getGroup());
										newLesson1.setCurator(savedCourse.getCurator());
										Lesson savedLesson1 = lessonRepository.save(newLesson1);
										//
										formLessonRestController.createLessonPersonFull(savedLesson1);
										//
										courseResponse.setResponseClass(newLesson1.getClass().getSimpleName().toLowerCase());
										courseResponse.setResponseId(savedLesson1.getId());
										break;
									case "templatetest":
										TemplateTest templateTest1 = templateTestRepository.findByTemplateTestId(templateCourseTemplateResponse.getTemplateResponseId());
										Test newTest1 = new Test();
										newTest1.setName(templateTest1.getName());
										newTest1.setDescription(templateTest1.getDescription());
										newTest1.setTemplateTest(templateTest1);
										newTest1.setStatus(statusRepository.findByStatusCode("draft"));
										newTest1.setGroup(savedCourse.getGroup());
										newTest1.setCurator(savedCourse.getCurator());
										Test savedTest1 = testRepository.save(newTest1);
										//
										formTestRestController.createTestVariantFull(savedTest1);
										//
										courseResponse.setResponseClass(newTest1.getClass().getSimpleName().toLowerCase());
										courseResponse.setResponseId(savedTest1.getId());
										break;
									default:
										throw new Exception("Не определён тип мероприятия");
								}
								courseResponseRepository.save(courseResponse);
							}
						}
						//
						planResponse.setResponseClass(newCourse.getClass().getSimpleName().toLowerCase());
						planResponse.setResponseId(savedCourse.getId());
						break;
				}
				planResponseRepository.save(planResponse);
			}
		}
		formTemplate = getFormByIdentifier(savedPlan.getId());
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

	@PostMapping(value = "form/plan/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody Plan plan, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		Plan updatePlan = planRepository.findByPlanId(id);
		if (!accessUtils.canEditCard(updatePlan)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		updatePlan.setName(plan.getName());
		updatePlan.setDescription(plan.getDescription());
		Person curator = personRepository.findByPersonId(plan.getCurator().getId());
		updatePlan.setCurator(curator);
		Person author = personRepository.findByPersonId(plan.getAuthor().getId());
		updatePlan.setAuthor(author);
		Status status = statusRepository.findByStatusId(plan.getStatus().getId());
		updatePlan.setStatus(status);
		Group oldGroup = updatePlan.getGroup();
		Group group = groupRepository.findByGroupId(plan.getGroup().getId());
		updatePlan.setGroup(group);
		plan.setStatus(statusRepository.findByStatusId(plan.getStatus().getId()));
		if (!plan.getStatus().getCode().equals("draft")){
			throw new Exception("incorrect status for [class = " + plan.getClass().getSimpleName().toLowerCase() + ", id = " + plan.getId() + "]");
		}
		planValidator.validate(updatePlan, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Plan savedPlan = planRepository.save(updatePlan);
		//
		if (!oldGroup.getId().equals(savedPlan.getGroup().getId())){
			Set<PlanResponse> planResponseSet = planResponseRepository.findByPlanId(savedPlan.getId());
			for (PlanResponse planResponse : planResponseSet){
				switch (planResponse.getResponseClass()){
					case "test":
						Test test = testRepository.findByTestId(planResponse.getResponseId());
						test.setGroup(updatePlan.getGroup());
						Test savedTest = testRepository.save(test);
						//
						formTestRestController.reCreateTestVariantFull(savedTest);
						//
						break;
					case "lesson":
						Lesson lesson = lessonRepository.findByLessonId(planResponse.getResponseId());
						lesson.setGroup(updatePlan.getGroup());
						Lesson savedLesson = lessonRepository.save(lesson);
						//
						formLessonRestController.reCreateLessonPersonFull(savedLesson);
						//
						break;
					case "course":
						Set<CourseResponse> courseResponseSet = courseResponseRepository.findByCourseId(planResponse.getResponseId());
						for (CourseResponse courseResponse : courseResponseSet){
							switch (courseResponse.getResponseClass()){
								case "test":
									Test test1 = testRepository.findByTestId(courseResponse.getResponseId());
									test1.setGroup(updatePlan.getGroup());
									Test savedTest1 = testRepository.save(test1);
									//
									formTestRestController.reCreateTestVariantFull(savedTest1);
									//
									break;
								case "lesson":
									Lesson lesson1 = lessonRepository.findByLessonId(courseResponse.getResponseId());
									lesson1.setGroup(updatePlan.getGroup());
									Lesson savedLesson1 = lessonRepository.save(lesson1);
									//
									formLessonRestController.reCreateLessonPersonFull(savedLesson1);
									//
									break;
							}
						}
						break;
				}
			}
		}
		//
		formTemplate = getFormByIdentifier(savedPlan.getId());
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

	@PostMapping(value = "form/plan/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		Plan updatePlan = planRepository.findByPlanId(id);
		//if (!accessUtils.canEditCard(updatePlan)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updatePlan.getClass().getSimpleName().toLowerCase(), updatePlan.getStatus().getId(), toStatus.getId());
		updatePlan.setStatus(move.getToStatus());
		if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("in_progress")){
			if (groupPersonRepository.findByGroupId(updatePlan.getGroup().getId()).size() == 0){
				throw new Exception("Отсутствуют учащиеся");
			}
			updatePlan.setDateStart(new Date());
		}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("canceled")){
			updatePlan.setDateEnd(new Date());
		}
		Plan savedPlan = planRepository.save(updatePlan);
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(Lesson.class.getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();

		if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("in_progress")){
			utils.checkTemplate(updatePlan, true);
			moveUtils.startFirstBlock(savedPlan);
		}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("canceled")){
			Set<PlanResponse> planResponseSet = planResponseRepository.findByPlanId(savedPlan.getId());
			for (PlanResponse planResponse : planResponseSet){
				switch (planResponse.getResponseClass()){
					case "lesson":
						formLessonRestController.toStatus(planResponse.getResponseId(), "canceled");
						break;
					case "test":
						formTestRestController.toStatus(planResponse.getResponseId(), "canceled");
						break;
					case "course":
						formCourseRestController.toStatus(planResponse.getResponseId(), "canceled");
						break;
					default:
						throw new Exception("Не определён тип дочернего мероприятия");
				}
			}
		}
		formTemplate = getFormByIdentifier(savedPlan.getId());
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