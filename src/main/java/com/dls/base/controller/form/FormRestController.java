package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.request.PersonVariantContainer;
import com.dls.base.request.RemoveContainer;
import com.dls.base.ui.form.FormAttribute;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.MoveUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class FormRestController
{

	private final PersonRepository personRepository;
	private final GroupRepository groupRepository;
	private final GroupPersonRepository groupPersonRepository;
	private final TemplateLessonRepository templateLessonRepository;
	private final TemplateTestRepository templateTestRepository;
	private final TemplateTestVariantRepository templateTestVariantRepository;
	private final TemplateTestQuestionRepository templateTestQuestionRepository;
	private final TemplateTestAnswerRepository templateTestAnswerRepository;
	private final TemplateCourseRepository templateCourseRepository;
	private final TemplatePlanRepository templatePlanRepository;
	private final TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository;
	private final TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository;
	private final LessonRepository lessonRepository;
	private final TestRepository testRepository;
	private final CourseRepository courseRepository;
	private final PlanRepository planRepository;
	private final BlockRepository blockRepository;
	private final CategoryRepository categoryRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	MoveUtils moveUtils;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	FormRestController(PersonRepository personRepository, GroupRepository groupRepository,
					   GroupPersonRepository groupPersonRepository, TemplateLessonRepository templateLessonRepository,
					   TemplateTestRepository templateTestRepository,
					   TemplateTestVariantRepository templateTestVariantRepository,
					   TemplateTestQuestionRepository templateTestQuestionRepository,
					   TemplateTestAnswerRepository templateTestAnswerRepository,
					   TemplateCourseRepository templateCourseRepository,
					   TemplatePlanRepository templatePlanRepository,
					   TemplateCourseTemplateResponseRepository templateCourseTemplateResponse, TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository, LessonRepository lessonRepository, TestRepository testRepository, CourseRepository courseRepository, PlanRepository planRepository, BlockRepository blockRepository, CategoryRepository categoryRepository) {
		this.personRepository = personRepository;
		this.groupRepository = groupRepository;
		this.groupPersonRepository = groupPersonRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.templateTestRepository = templateTestRepository;
		this.templateTestVariantRepository = templateTestVariantRepository;
		this.templateTestQuestionRepository = templateTestQuestionRepository;
		this.templateTestAnswerRepository = templateTestAnswerRepository;
		this.templateCourseRepository = templateCourseRepository;
		this.templatePlanRepository = templatePlanRepository;
		this.templateCourseTemplateResponseRepository= templateCourseTemplateResponse;
		this.templatePlanTemplateResponseRepository = templatePlanTemplateResponseRepository;
		this.lessonRepository = lessonRepository;
		this.testRepository = testRepository;
		this.courseRepository = courseRepository;
		this.planRepository = planRepository;
		this.blockRepository = blockRepository;
		this.categoryRepository = categoryRepository;
	}

	@PostMapping(value = "form/remove", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity removeFormList(@RequestBody final List<RemoveContainer> entities) throws Exception {
		try{
		for (int i = 0; i < entities.size(); i++){
			RemoveContainer removeContainer = entities.get(i);
			switch (removeContainer.template){
				case "person" :
					Person person = personRepository.findByPersonId(removeContainer.id);
					if (person != null){
						if (!accessUtils.canEditCard(person)){
							throw new Exception("Отсутствуют права на удаление карточки");
						}
						if (groupRepository.findByPersonId(person.getId()).size() > 0){
							return ResponseEntity
									.status(HttpStatus.OK)
									.body(new ErrorMessage(messageSource.getMessage("form.user.hasGroup", null, null)));
						}
						if (utils.rolesToMap(person.getRoles()).containsKey(utils.getAppProperty("roles.admin.code"))){
							return ResponseEntity
									.status(HttpStatus.OK)
									.body(new ErrorMessage(messageSource.getMessage("form.user.roles.admin.remove", null, null)));
						}
						personRepository.deleteById(person.getId());
					}
					break;
				case "group" :
					Group group = groupRepository.findByGroupId(removeContainer.id);
					if (group != null){
						if (!accessUtils.canEditCard(group)){
							throw new Exception("Отсутствуют права на удаление карточки");
						}
						if (groupPersonRepository.findByGroupId(group.getId()).size() > 0){
							return ResponseEntity
									.status(HttpStatus.OK)
									.body(new ErrorMessage(messageSource.getMessage("form.group.hasStudents", null, null)));
						}
						try {
							groupRepository.delete(group);
						}catch (Exception e){
							return ResponseEntity
									.status(HttpStatus.OK)
									.body(new ErrorMessage(messageSource.getMessage("form.group.usesInProcess", null, null)));
						}
					}
					break;
				case "groupperson" :
					GroupPerson groupPerson = groupPersonRepository.findByGroupPersonId(removeContainer.id);
					if (groupPerson != null){
						if (!accessUtils.canEditCard(groupPerson)){
							throw new Exception("Отсутствуют права на удаление карточки");
						}
						groupPersonRepository.deleteById(groupPerson.getId());
					}
					break;
				case "templatelesson" :
					TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(removeContainer.id);
					if (templateLesson != null){
						moveUtils.deleteTrainingChilds(templateLesson);
					}
					break;
				case "templatetest" :
					TemplateTest templateTest = templateTestRepository.findByTemplateTestId(removeContainer.id);
					if (templateTest != null){
						moveUtils.deleteTrainingChilds(templateTest);
					}
					break;
				case "templatecourse" :
					TemplateCourse templateCourse = templateCourseRepository.findByTemplateCourseId(removeContainer.id);
					if (templateCourse != null){
						moveUtils.deleteTrainingChilds(templateCourse);
					}
					break;
				case "templateplan" :
					TemplatePlan templatePlan = templatePlanRepository.findByTemplatePlanId(removeContainer.id);
					if (templatePlan != null){
						moveUtils.deleteTrainingChilds(templatePlan);
					}
					break;
				case "templatetestvariant" :
					TemplateTestVariant templateTestVariant = templateTestVariantRepository.findByTemplateTestVariantId(removeContainer.id);
					if (templateTestVariant != null){
						if (!accessUtils.canEditCard(templateTestVariant)){
							throw new Exception("Отсутствуют права на удаление карточки");
						}
						if (templateTestQuestionRepository.findByTemplateTestVariantId(templateTestVariant.getId()).size() > 0){
							return ResponseEntity
									.status(HttpStatus.OK)
									.body(new ErrorMessage(messageSource.getMessage("form.templatetestvariant.hasQuestion", null, null)));
						}
						templateTestVariantRepository.deleteById(templateTestVariant.getId());
					}
					break;
				case "templatetestquestion" :
					TemplateTestQuestion templateTestQuestion = templateTestQuestionRepository.findByTemplateTestQuestionId(removeContainer.id);
					if (templateTestQuestion != null){
						if (!accessUtils.canEditCard(templateTestQuestion)){
							throw new Exception("Отсутствуют права на удаление карточки");
						}
						if (templateTestAnswerRepository.findByTemplateTestQuestionId(templateTestQuestion.getId()).size() > 0){
							return ResponseEntity
									.status(HttpStatus.OK)
									.body(new ErrorMessage(messageSource.getMessage("form.templatetestquestion.hasAnswer", null, null)));
						}
						templateTestQuestionRepository.deleteById(templateTestQuestion.getId());
					}
					break;
				case "templatetestanswer" :
					TemplateTestAnswer templateTestAnswer = templateTestAnswerRepository.findByTemplateTestAnswerId(removeContainer.id);
					if (templateTestAnswer != null){
						if (!accessUtils.canEditCard(templateTestAnswer)){
							throw new Exception("Отсутствуют права на удаление карточки");
						}
						templateTestAnswerRepository.deleteById(templateTestAnswer.getId());
					}
					break;
				case "templatecoursetemplateresponse" :
					TemplateCourseTemplateResponse templateCourseTemplateResponse = templateCourseTemplateResponseRepository.findByTemplateCourseTemplateResponseId(removeContainer.id);
					if (templateCourseTemplateResponse != null){
						if (!accessUtils.canEditCard(templateCourseTemplateResponse)){
							throw new Exception("Отсутствуют права на удаление карточки");
						}
						Block block = templateCourseTemplateResponse.getBlock();
						templateCourseTemplateResponseRepository.deleteById(templateCourseTemplateResponse.getId());
						if (templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByBlockId(block.getId()).size() == 0){
							blockRepository.delete(block);
						}
					}
					break;
				case "templateplantemplateresponse" :
					TemplatePlanTemplateResponse templatePlanTemplateResponse = templatePlanTemplateResponseRepository.findByTemplatePlanTemplateResponseId(removeContainer.id);
					if (templatePlanTemplateResponse != null){
						if (!accessUtils.canEditCard(templatePlanTemplateResponse)){
							throw new Exception("Отсутствуют права на удаление карточки");
						}
						templatePlanTemplateResponseRepository.deleteById(templatePlanTemplateResponse.getId());
					}
					break;
				case "lesson" :
					Lesson lesson = lessonRepository.findByLessonId(removeContainer.id);
					if (lesson != null){
						moveUtils.deleteTrainingChilds(lesson);
					}
					break;
				case "test" :
					Test test = testRepository.findByTestId(removeContainer.id);
					if (test != null){
						moveUtils.deleteTrainingChilds(test);
					}
					break;
				case "course" :
					Course course = courseRepository.findByCourseId(removeContainer.id);
					if (course != null){
						moveUtils.deleteTrainingChilds(course);
					}
					break;
				case "plan" :
					Plan plan = planRepository.findByPlanId(removeContainer.id);
					if (plan != null){
						moveUtils.deleteTrainingChilds(plan);
					}
					break;
				case "category" :
					Category category = categoryRepository.findByCategoryId(removeContainer.id);
					if (category != null){
						if (templateLessonRepository.findByCategoryId(removeContainer.id).size() > 0){
							throw new Exception("Невозможно удалить категорию, т.к. имеются привязанные шаблоны уроков");
						}
						if (templateTestRepository.findByCategoryId(removeContainer.id).size() > 0){
							throw new Exception("Невозможно удалить категорию, т.к. имеются привязанные шаблоны тестов");
						}
						moveUtils.deleteTrainingChilds(category);
					}
					break;
				default:
					break;
			}
		}
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(new Message("OK"));
	}

	@GetMapping(value = "form/person/test/variants/{groupId}/{entityClass}/{entityId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity getFormPersonTestVariants(@PathVariable long groupId, @PathVariable String entityClass, @PathVariable long entityId) throws Exception {
		PersonVariantContainer personVariantContainer = null;
		try{
		Long planId = 0L;
		Long courseId = 0L;
		Long testId = 0L;
		switch (entityClass){
			case "plan":
				planId = entityId;
				break;
			case "course":
				courseId = entityId;
				break;
			case "test":
				testId = entityId;
				break;
			default:
				throw new Exception("Неверный тим мероприятия");
		}
		Set<Person> personSet = personRepository.findActiveStudentsByGroupId(groupId);
		if (personSet.size() == 0){
			throw new Exception("Отсутствуют пользователи с ролью обучающихся");
		}
		Set<TemplateTest> templateTestSet = templateTestRepository.findByParentIds(planId, courseId, testId);

		ArrayList<HashMap<String, FormAttribute>> hashMapPersonArrayList = new ArrayList<HashMap<String, FormAttribute>>();
		for (Person person : personSet){
			HashMap<String, FormAttribute> formAttributes = utils.getFormAttributes(person);
			hashMapPersonArrayList.add(formAttributes);
		}

		ArrayList<HashMap<String, FormAttribute>> hashMapTemplateTestArrayList = new ArrayList<HashMap<String, FormAttribute>>();

		for (TemplateTest templateTest : templateTestSet){
			HashMap <String, FormAttribute> templateTestFormAttributeList = utils.getFormAttributes(templateTest);
			//
			FormAttribute formAttribute = new FormAttribute();
			formAttribute.name = "variants";
			formAttribute.title = "варианты";
			formAttribute.type = "variants";
			//
			ArrayList<HashMap<String, FormAttribute>> hashMapVariantArrayList = new ArrayList<HashMap<String, FormAttribute>>();
			Set <TemplateTestVariant> templateTestVariantSet = templateTestVariantRepository.findByTemplateTestId(templateTest.getId());
			for (TemplateTestVariant templateTestVariant : templateTestVariantSet){
				HashMap<String, FormAttribute> formAttributes = utils.getFormAttributes(templateTestVariant);
				hashMapVariantArrayList.add(formAttributes);
			}
			formAttribute.value = hashMapVariantArrayList;
			templateTestFormAttributeList.put("variant_list", formAttribute);
			hashMapTemplateTestArrayList.add(templateTestFormAttributeList);
		}

		personVariantContainer = new PersonVariantContainer();
		personVariantContainer.hashMapPersonArrayList = hashMapPersonArrayList;
		personVariantContainer.hashMapTemplateTestArrayList = hashMapTemplateTestArrayList;
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(personVariantContainer);
	}

}