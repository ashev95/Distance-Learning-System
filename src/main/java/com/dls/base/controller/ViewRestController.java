package com.dls.base.controller;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.ui.view.ViewEntity;
import com.dls.base.ui.view.ViewTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.ArrayListBuilder;
import com.dls.base.utils.Constant;
import com.dls.base.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.dls.base.utils.Constant.*;

@RestController
@RequestMapping("view")
public class ViewRestController
{

	public static final String VIEW_CONFIG_PATH = "/WEB-INF/view_config/extra/";
	
	private final PersonRepository personRepository;
	private final GroupRepository groupRepository;
	private final GroupPersonRepository groupPersonRepository;
	private final TemplateLessonRepository templateLessonRepository;
	private final LessonRepository lessonRepository;
	private final TemplateTestRepository templateTestRepository;
	private final TemplateTestVariantRepository templateTestVariantRepository;
	private final TemplateTestQuestionRepository templateTestQuestionRepository;
	private final TemplateTestAnswerRepository templateTestAnswerRepository;
	private final TemplateCourseRepository templateCourseRepository;
	private final TemplatePlanRepository templatePlanRepository;
	private final TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository;
	private final TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository;
	private final LessonPersonRepository lessonPersonRepository;
	private final TestRepository testRepository;
	private final TestVariantPersonRepository testVariantPersonRepository;
	private final CourseRepository courseRepository;
	private final CourseResponseRepository courseResponseRepository;
	private final PlanRepository planRepository;
	private final PlanResponseRepository planResponseRepository;
	private final LifeCycleRepository lifeCycleRepository;
	private final StatusRepository statusRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final CategoryRepository categoryRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	ViewRestController(PersonRepository personRepository,
					   GroupRepository groupRepository,
					   GroupPersonRepository groupPersonRepository, TemplateLessonRepository templateLessonRepository,
					   LessonRepository lessonRepository, TemplateTestRepository templateTestRepository,
					   TemplateTestVariantRepository templateTestVariantRepository,
					   TemplateTestQuestionRepository templateTestQuestionRepository,
					   TemplateTestAnswerRepository templateTestAnswerRepository,
					   TemplateCourseRepository templateCourseRepository,
					   TemplatePlanRepository templatePlanRepository, TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository, TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository, LessonPersonRepository lessonPersonRepository, TestRepository testRepository, TestVariantPersonRepository testVariantPersonRepository, CourseRepository courseRepository, CourseResponseRepository courseResponseRepository, PlanRepository planRepository, PlanResponseRepository planResponseRepository, LifeCycleRepository lifeCycleRepository, StatusRepository statusRepository, TemplateLifeCycleRepository templateLifeCycleRepository, CategoryRepository categoryRepository) throws Exception {
		this.personRepository = personRepository;
		this.groupRepository = groupRepository;
		this.groupPersonRepository = groupPersonRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.lessonRepository = lessonRepository;
		this.templateTestRepository = templateTestRepository;
		this.templateTestVariantRepository = templateTestVariantRepository;
		this.templateTestQuestionRepository = templateTestQuestionRepository;
		this.templateTestAnswerRepository = templateTestAnswerRepository;
		this.templateCourseRepository = templateCourseRepository;
		this.templatePlanRepository = templatePlanRepository;
		this.templateCourseTemplateResponseRepository = templateCourseTemplateResponseRepository;
		this.templatePlanTemplateResponseRepository = templatePlanTemplateResponseRepository;
		this.lessonPersonRepository = lessonPersonRepository;
		this.testRepository = testRepository;
		this.testVariantPersonRepository = testVariantPersonRepository;
		this.courseRepository = courseRepository;
		this.courseResponseRepository = courseResponseRepository;
		this.planRepository = planRepository;
		this.planResponseRepository = planResponseRepository;
		this.lifeCycleRepository = lifeCycleRepository;
		this.statusRepository = statusRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.categoryRepository = categoryRepository;
	}

	@GetMapping(value = "/person/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPersonAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "person_all.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())){
			int increment = 0;
			for (Person person : personRepository.findAll()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = person.getId();
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				viewEntity.type = person.getClass().getSimpleName().toLowerCase();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/person/by_role", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPersonByRole() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "person_by_role.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())){
			int increment = 0;
			for (String roleCode : utils.getRoles().keySet()){
				if (personRepository.findByRoleCode(roleCode).size() > 0){
					ViewEntity viewEntity = new ViewEntity();
					viewEntity.id = ++increment;
					viewEntity.name = utils.getRoles().get(roleCode);
					viewEntity.children = new ArrayList<ViewEntity>();
					viewTemplate.data.items.add(viewEntity);
					for (Person person : personRepository.findByRoleCode(roleCode)){
						ViewEntity viewEntity1 = new ViewEntity();
						viewEntity1.id = ++increment;
						viewEntity1.cardId = person.getId();
						viewEntity1.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
						viewEntity1.type = person.getClass().getSimpleName().toLowerCase();
						viewEntity.children.add(viewEntity1);
					}
				}
			}
			if (personRepository.findAllWithoutRole().size() > 0){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = utils.getAppProperty("user.roles.empty");
				viewEntity.children = new ArrayList<ViewEntity>();
				viewTemplate.data.items.add(viewEntity);
				for (Person person : personRepository.findAllWithoutRole()){
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = person.getId();
					viewEntity1.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
					viewEntity1.type = person.getClass().getSimpleName().toLowerCase();
					viewEntity.children.add(viewEntity1);
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/group/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getGroupAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "group_all.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Group group : groupRepository.findAll()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = group.getId();
				viewEntity.name = group.getName();
				viewEntity.type = group.getClass().getSimpleName().toLowerCase();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/group/by_curator", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getGroupByCurator() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "group_by_curator.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findByRoleCode(utils.getAppProperty("roles.curator.code"))) {
				if (groupRepository.findByCurator(person).size() > 0) {
					ViewEntity viewEntity = new ViewEntity();
					viewEntity.id = ++increment;
					viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
					viewEntity.children = new ArrayList<ViewEntity>();
					viewTemplate.data.items.add(viewEntity);
					for (Group group : groupRepository.findByCurator(person)) {
						ViewEntity viewEntity1 = new ViewEntity();
						viewEntity1.id = ++increment;
						viewEntity1.cardId = group.getId();
						viewEntity1.name = group.getName();
						viewEntity1.type = group.getClass().getSimpleName().toLowerCase();
						viewEntity.children.add(viewEntity1);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/group/my", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getGroupMy() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "group_my.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Group group : groupRepository.findByCurator(accessUtils.getCurrentPerson())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = group.getId();
				viewEntity.name = group.getName();
				viewEntity.type = group.getClass().getSimpleName().toLowerCase();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/category/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getCategoryAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "category_all.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Category category : categoryRepository.findAll()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = category.getId();
				viewEntity.name = category.getName();
				viewEntity.type = category.getClass().getSimpleName().toLowerCase();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatelesson/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateLessonWithStatusActive() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatelesson_active.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (TemplateLesson templateLesson : templateLessonRepository.findByStatusCode("active")) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = templateLesson.getId();
				viewEntity.name = templateLesson.getName() + " (Версия " + templateLesson.getVersion() + ")";
				viewEntity.type = templateLesson.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusCode = templateLesson.getStatus().getCode();
				viewEntity.realStatusName = templateLesson.getStatus().getName();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatelesson/by_author", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateLessonByAuthor() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatelesson_by_author.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (TemplateLesson templateLesson : templateLessonRepository.findByAuthorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = templateLesson.getId();
					viewEntity1.name = templateLesson.getName() + " (Версия " + templateLesson.getVersion() + ")";
					viewEntity1.type = templateLesson.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusCode = templateLesson.getStatus().getCode();
					viewEntity1.realStatusName = templateLesson.getStatus().getName();
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatelesson/by_category", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateLessonByCategory() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatelesson_by_category.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (Category category: categoryRepository.findAll()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = category.getName();
				for (TemplateLesson templateLesson : templateLessonRepository.findByStatusByCategoryId("active", category.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = templateLesson.getId();
					viewEntity1.name = templateLesson.getName() + " (Версия " + templateLesson.getVersion() + ")";
					viewEntity1.type = templateLesson.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusCode = templateLesson.getStatus().getCode();
					viewEntity1.realStatusName = templateLesson.getStatus().getName();
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
			Set<TemplateLesson> templateLessonSet = templateLessonRepository.findByStatusWithoutCategory("active");
			if (templateLessonSet.size() > 0){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = Constant.EMPTY_CATEGORY_NAME;
				for (TemplateLesson templateLesson : templateLessonSet){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = templateLesson.getId();
					viewEntity1.name = templateLesson.getName() + " (Версия " + templateLesson.getVersion() + ")";
					viewEntity1.type = templateLesson.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusCode = templateLesson.getStatus().getCode();
					viewEntity1.realStatusName = templateLesson.getStatus().getName();
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatelesson/by_version", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateLessonByVersion() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatelesson_by_version.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (TemplateLesson templateLesson : templateLessonRepository.findByVersion(1L)) {
				Set<TemplateLesson> templateLessonSet = templateLessonRepository.findRecursiveByParentId(templateLesson.getId());
				if (templateLessonSet.size() > 0) {
					ViewEntity viewEntity = new ViewEntity();
					viewEntity.id = ++increment;
					viewEntity.name = templateLesson.getName();
					viewEntity.children = new ArrayList<ViewEntity>();
					viewTemplate.data.items.add(viewEntity);
					for (TemplateLesson templateLesson1 : templateLessonSet) {
						ViewEntity viewEntity1 = new ViewEntity();
						viewEntity1.id = ++increment;
						viewEntity1.cardId = templateLesson1.getId();
						viewEntity1.name = templateLesson1.getName() + " (Версия " + templateLesson1.getVersion() + ")";
						viewEntity1.realName = templateLesson1.getName();
						viewEntity1.realDescription = templateLesson1.getDescription();
						viewEntity1.type = templateLesson1.getClass().getSimpleName().toLowerCase();
						viewEntity1.realStatusCode = templateLesson1.getStatus().getCode();
						viewEntity1.realStatusName = templateLesson1.getStatus().getName();
						viewEntity.children.add(viewEntity1);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatetest/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateTestWithStatusActive() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatetest_active.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (TemplateTest templateTest : templateTestRepository.findByStatusCode("active")) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = templateTest.getId();
				viewEntity.name = templateTest.getName() + " (Версия " + templateTest.getVersion() + ")";
				viewEntity.type = templateTest.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusCode = templateTest.getStatus().getCode();
				viewEntity.realStatusName = templateTest.getStatus().getName();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatetest/by_author", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateTestByAuthor() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatetest_by_author.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (TemplateTest templateTest : templateTestRepository.findByAuthorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = templateTest.getId();
					viewEntity1.name = templateTest.getName() + " (Версия " + templateTest.getVersion() + ")";
					viewEntity1.type = templateTest.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusCode = templateTest.getStatus().getCode();
					viewEntity1.realStatusName = templateTest.getStatus().getName();
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatetest/by_category", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateTestByCategory() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatetest_by_category.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (Category category: categoryRepository.findAll()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = category.getName();
				for (TemplateTest templateTest : templateTestRepository.findByStatusByCategoryId("active", category.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = templateTest.getId();
					viewEntity1.name = templateTest.getName() + " (Версия " + templateTest.getVersion() + ")";
					viewEntity1.type = templateTest.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusCode = templateTest.getStatus().getCode();
					viewEntity1.realStatusName = templateTest.getStatus().getName();
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
			Set<TemplateTest> templateTestSet = templateTestRepository.findByStatusWithoutCategory("active");
			if (templateTestSet.size() > 0){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = Constant.EMPTY_CATEGORY_NAME;
				for (TemplateTest templateTest : templateTestSet){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = templateTest.getId();
					viewEntity1.name = templateTest.getName() + " (Версия " + templateTest.getVersion() + ")";
					viewEntity1.type = templateTest.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusCode = templateTest.getStatus().getCode();
					viewEntity1.realStatusName = templateTest.getStatus().getName();
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatetest/by_version", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateTestByVersion() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatetest_by_version.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (TemplateTest templateTest : templateTestRepository.findByVersion(1L)) {
				Set<TemplateTest> templateTestSet = templateTestRepository.findRecursiveByParentId(templateTest.getId());
				if (templateTestSet.size() > 0) {
					ViewEntity viewEntity = new ViewEntity();
					viewEntity.id = ++increment;
					viewEntity.name = templateTest.getName();
					viewEntity.children = new ArrayList<ViewEntity>();
					viewTemplate.data.items.add(viewEntity);
					for (TemplateTest templateTest1 : templateTestSet) {
						ViewEntity viewEntity1 = new ViewEntity();
						viewEntity1.id = ++increment;
						viewEntity1.cardId = templateTest1.getId();
						viewEntity1.name = templateTest1.getName() + " (Версия " + templateTest1.getVersion() + ")";
						viewEntity1.realName = templateTest1.getName();
						viewEntity1.realDescription = templateTest1.getDescription();
						viewEntity1.type = templateTest1.getClass().getSimpleName().toLowerCase();
						viewEntity1.realStatusCode = templateTest1.getStatus().getCode();
						viewEntity1.realStatusName = templateTest1.getStatus().getName();
						viewEntity.children.add(viewEntity1);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatecourse/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateCourseWithStatusActive() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatecourse_active.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (TemplateCourse templateCourse : templateCourseRepository.findByStatusCode("active")) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = templateCourse.getId();
				viewEntity.name = templateCourse.getName() + " (Версия " + templateCourse.getVersion() + ")";
				viewEntity.type = templateCourse.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusCode = templateCourse.getStatus().getCode();
				viewEntity.realStatusName = templateCourse.getStatus().getName();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatecourse/by_author", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateCourseByAuthor() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatecourse_by_author.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (TemplateCourse templateCourse : templateCourseRepository.findByAuthorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = templateCourse.getId();
					viewEntity1.name = templateCourse.getName() + " (Версия " + templateCourse.getVersion() + ")";
					viewEntity1.type = templateCourse.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusCode = templateCourse.getStatus().getCode();
					viewEntity1.realStatusName = templateCourse.getStatus().getName();
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatecourse/by_version", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateCourseByVersion() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templatecourse_by_version.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (TemplateCourse templateCourse : templateCourseRepository.findByVersion(1L)) {
				Set<TemplateCourse> templateCourseSet = templateCourseRepository.findRecursiveByParentId(templateCourse.getId());
				if (templateCourseSet.size() > 0) {
					ViewEntity viewEntity = new ViewEntity();
					viewEntity.id = ++increment;
					viewEntity.name = templateCourse.getName();
					viewEntity.children = new ArrayList<ViewEntity>();
					viewTemplate.data.items.add(viewEntity);
					for (TemplateCourse templateCourse1 : templateCourseSet) {
						ViewEntity viewEntity1 = new ViewEntity();
						viewEntity1.id = ++increment;
						viewEntity1.cardId = templateCourse1.getId();
						viewEntity1.name = templateCourse1.getName() + " (Версия " + templateCourse1.getVersion() + ")";
						viewEntity1.realName = templateCourse1.getName();
						viewEntity1.realDescription = templateCourse1.getDescription();
						viewEntity1.type = templateCourse1.getClass().getSimpleName().toLowerCase();
						viewEntity1.realStatusCode = templateCourse1.getStatus().getCode();
						viewEntity1.realStatusName = templateCourse1.getStatus().getName();
						viewEntity.children.add(viewEntity1);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templateplan/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplatePlanWithStatusActive() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templateplan_active.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (TemplatePlan templatePlan : templatePlanRepository.findByStatusCode("active")) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = templatePlan.getId();
				viewEntity.name = templatePlan.getName() + " (Версия " + templatePlan.getVersion() + ")";
				viewEntity.type = templatePlan.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusCode = templatePlan.getStatus().getCode();
				viewEntity.realStatusName = templatePlan.getStatus().getName();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templateplan/by_author", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplatePlanByAuthor() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templateplan_by_author.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (TemplatePlan templatePlan : templatePlanRepository.findByAuthorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = templatePlan.getId();
					viewEntity1.name = templatePlan.getName() + " (Версия " + templatePlan.getVersion() + ")";
					viewEntity1.type = templatePlan.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusCode = templatePlan.getStatus().getCode();
					viewEntity1.realStatusName = templatePlan.getStatus().getName();
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templateplan/by_version", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplatePlanByVersion() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "templateplan_by_version.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (TemplatePlan templatePlan : templatePlanRepository.findByVersion(1L)) {
				Set<TemplatePlan> templatePlanSet = templatePlanRepository.findRecursiveByParentId(templatePlan.getId());
				if (templatePlanSet.size() > 0) {
					ViewEntity viewEntity = new ViewEntity();
					viewEntity.id = ++increment;
					viewEntity.name = templatePlan.getName();
					viewEntity.children = new ArrayList<ViewEntity>();
					viewTemplate.data.items.add(viewEntity);
					for (TemplatePlan templatePlan1 : templatePlanSet) {
						ViewEntity viewEntity1 = new ViewEntity();
						viewEntity1.id = ++increment;
						viewEntity1.cardId = templatePlan1.getId();
						viewEntity1.name = templatePlan1.getName() + " (Версия " + templatePlan1.getVersion() + ")";
						viewEntity1.realName = templatePlan1.getName();
						viewEntity1.realDescription = templatePlan1.getDescription();
						viewEntity1.type = templatePlan1.getClass().getSimpleName().toLowerCase();
						viewEntity1.realStatusCode = templatePlan1.getStatus().getCode();
						viewEntity1.realStatusName = templatePlan1.getStatus().getName();
						viewEntity.children.add(viewEntity1);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/lesson/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getLessonAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "lesson_all.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Lesson lesson : lessonRepository.findAllParent()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = lesson.getId();
				viewEntity.name = lesson.getName();
				viewEntity.type = lesson.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = lesson.getStatus().getName();
				viewEntity.realStatusCode = lesson.getStatus().getCode();
				if (lesson.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(lesson.getDateStart());
				}
				if (lesson.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(lesson.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/lesson/by_curator", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getLessonByCurator() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "lesson_by_curator.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (Lesson lesson : lessonRepository.findParentByCuratorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = lesson.getId();
					viewEntity1.name = lesson.getName();
					viewEntity1.type = lesson.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = lesson.getStatus().getName();
					viewEntity1.realStatusCode = lesson.getStatus().getCode();
					if (lesson.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(lesson.getDateStart());
					}
					if (lesson.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(lesson.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/lesson/by_status", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getLessonByStatus() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "lesson_by_status.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Status status : statusRepository. findAll()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = status.getName();
				for (Lesson lesson : lessonRepository.findParentByStatusId(status.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = lesson.getId();
					viewEntity1.name = lesson.getName();
					viewEntity1.type = lesson.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = lesson.getStatus().getName();
					viewEntity1.realStatusCode = lesson.getStatus().getCode();
					if (lesson.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(lesson.getDateStart());
					}
					if (lesson.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(lesson.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/lesson/by_author", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getLessonByAuthor() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "lesson_by_author.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (Lesson lesson : lessonRepository.findParentByAuthorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = lesson.getId();
					viewEntity1.name = lesson.getName();
					viewEntity1.type = lesson.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = lesson.getStatus().getName();
					viewEntity1.realStatusCode = lesson.getStatus().getCode();
					if (lesson.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(lesson.getDateStart());
					}
					if (lesson.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(lesson.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/lesson/my", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getLessonMy() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "lesson_my.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Lesson lesson : lessonRepository.findParentByCuratorId(accessUtils.getCurrentPerson().getId())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = lesson.getId();
				viewEntity.name = lesson.getName();
				viewEntity.type = lesson.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = lesson.getStatus().getName();
				viewEntity.realStatusCode = lesson.getStatus().getCode();
				if (lesson.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(lesson.getDateStart());
				}
				if (lesson.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(lesson.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/lesson/created_by_me", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getLessonCreatedByMe() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "lesson_created_by_me.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Lesson lesson : lessonRepository.findParentByAuthorId(accessUtils.getCurrentPerson().getId())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = lesson.getId();
				viewEntity.name = lesson.getName();
				viewEntity.type = lesson.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = lesson.getStatus().getName();
				viewEntity.realStatusCode = lesson.getStatus().getCode();
				if (lesson.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(lesson.getDateStart());
				}
				if (lesson.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(lesson.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/test/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTestAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "test_all.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Test test : testRepository.findAllParent()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = test.getId();
				viewEntity.name = test.getName();
				viewEntity.type = test.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = test.getStatus().getName();
				viewEntity.realStatusCode = test.getStatus().getCode();
				if (test.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(test.getDateStart());
				}
				if (test.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(test.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/test/by_curator", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTestByCurator() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "test_by_curator.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (Test test : testRepository.findParentByCuratorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = test.getId();
					viewEntity1.name = test.getName();
					viewEntity1.type = test.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = test.getStatus().getName();
					viewEntity1.realStatusCode = test.getStatus().getCode();
					if (test.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(test.getDateStart());
					}
					if (test.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(test.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/test/by_status", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTestByStatus() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "test_by_status.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Status status : statusRepository. findAll()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = status.getName();
				for (Test test : testRepository.findParentByStatusId(status.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = test.getId();
					viewEntity1.name = test.getName();
					viewEntity1.type = test.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = test.getStatus().getName();
					viewEntity1.realStatusCode = test.getStatus().getCode();
					if (test.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(test.getDateStart());
					}
					if (test.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(test.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/test/by_author", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTestByAuthor() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "test_by_author.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (Test test : testRepository.findParentByAuthorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = test.getId();
					viewEntity1.name = test.getName();
					viewEntity1.type = test.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = test.getStatus().getName();
					viewEntity1.realStatusCode = test.getStatus().getCode();
					if (test.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(test.getDateStart());
					}
					if (test.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(test.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/test/my", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTestMy() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "test_my.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Test test : testRepository.findParentByCuratorId(accessUtils.getCurrentPerson().getId())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = test.getId();
				viewEntity.name = test.getName();
				viewEntity.type = test.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = test.getStatus().getName();
				viewEntity.realStatusCode = test.getStatus().getCode();
				if (test.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(test.getDateStart());
				}
				if (test.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(test.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/test/created_by_me", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTestCreatedByMe() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "test_created_by_me.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Test test : testRepository.findParentByAuthorId(accessUtils.getCurrentPerson().getId())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = test.getId();
				viewEntity.name = test.getName();
				viewEntity.type = test.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = test.getStatus().getName();
				viewEntity.realStatusCode = test.getStatus().getCode();
				if (test.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(test.getDateStart());
				}
				if (test.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(test.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/course/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getCourseAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "course_all.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Course course : courseRepository.findAllParent()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = course.getId();
				viewEntity.name = course.getName();
				viewEntity.type = course.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = course.getStatus().getName();
				viewEntity.realStatusCode = course.getStatus().getCode();
				if (course.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(course.getDateStart());
				}
				if (course.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(course.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/course/by_curator", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getCourseByCurator() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "course_by_curator.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (Course course : courseRepository.findParentByCuratorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = course.getId();
					viewEntity1.name = course.getName();
					viewEntity1.type = course.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = course.getStatus().getName();
					viewEntity1.realStatusCode = course.getStatus().getCode();
					if (course.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(course.getDateStart());
					}
					if (course.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(course.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/course/by_status", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getCourseByStatus() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "course_by_status.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Status status : statusRepository. findAll()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = status.getName();
				for (Course course : courseRepository.findParentByStatusId(status.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = course.getId();
					viewEntity1.name = course.getName();
					viewEntity1.type = course.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = course.getStatus().getName();
					viewEntity1.realStatusCode = course.getStatus().getCode();
					if (course.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(course.getDateStart());
					}
					if (course.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(course.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/course/by_author", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getCourseByAuthor() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "course_by_author.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (Course course : courseRepository.findParentByAuthorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = course.getId();
					viewEntity1.name = course.getName();
					viewEntity1.type = course.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = course.getStatus().getName();
					viewEntity1.realStatusCode = course.getStatus().getCode();
					if (course.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(course.getDateStart());
					}
					if (course.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(course.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/course/my", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getCourseMy() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "course_my.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Course course : courseRepository.findParentByCuratorId(accessUtils.getCurrentPerson().getId())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = course.getId();
				viewEntity.name = course.getName();
				viewEntity.type = course.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = course.getStatus().getName();
				viewEntity.realStatusCode = course.getStatus().getCode();
				if (course.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(course.getDateStart());
				}
				if (course.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(course.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/course/created_by_me", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getCourseCreatedByMe() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "course_created_by_me.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Course course : courseRepository.findParentByAuthorId(accessUtils.getCurrentPerson().getId())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = course.getId();
				viewEntity.name = course.getName();
				viewEntity.type = course.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = course.getStatus().getName();
				viewEntity.realStatusCode = course.getStatus().getCode();
				if (course.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(course.getDateStart());
				}
				if (course.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(course.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/plan/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPlanAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "plan_all.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Plan plan : planRepository.findAll()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = plan.getId();
				viewEntity.name = plan.getName();
				viewEntity.type = plan.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = plan.getStatus().getName();
				viewEntity.realStatusCode = plan.getStatus().getCode();
				if (plan.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(plan.getDateStart());
				}
				if (plan.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(plan.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/plan/by_curator", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPlanByCurator() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "plan_by_curator.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (Plan plan : planRepository.findParentByCuratorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = plan.getId();
					viewEntity1.name = plan.getName();
					viewEntity1.type = plan.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = plan.getStatus().getName();
					viewEntity1.realStatusCode = plan.getStatus().getCode();
					if (plan.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(plan.getDateStart());
					}
					if (plan.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(plan.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/plan/by_status", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPlanByStatus() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "plan_by_status.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Status status : statusRepository. findAll()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = status.getName();
				for (Plan plan : planRepository.findParentByStatusId(status.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = plan.getId();
					viewEntity1.name = plan.getName();
					viewEntity1.type = plan.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = plan.getStatus().getName();
					viewEntity1.realStatusCode = plan.getStatus().getCode();
					if (plan.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(plan.getDateStart());
					}
					if (plan.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(plan.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/plan/by_author", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPlanByAuthor() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "plan_by_author.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()){
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
				for (Plan plan : planRepository.findParentByAuthorId(person.getId())){
					//
					ViewEntity viewEntity1 = new ViewEntity();
					viewEntity1.id = ++increment;
					viewEntity1.cardId = plan.getId();
					viewEntity1.name = plan.getName();
					viewEntity1.type = plan.getClass().getSimpleName().toLowerCase();
					viewEntity1.realStatusName = plan.getStatus().getName();
					viewEntity1.realStatusCode = plan.getStatus().getCode();
					if (plan.getDateStart() != null) {
						viewEntity1.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(plan.getDateStart());
					}
					if (plan.getDateEnd() != null) {
						viewEntity1.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(plan.getDateEnd());
					}
					//
					if (viewEntity.children == null){
						viewEntity.children = new ArrayList<ViewEntity>();
					}
					viewEntity.children.add(viewEntity1);
				}
				if (viewEntity.children != null){
					if (viewEntity.children.size() > 0){
						viewTemplate.data.items.add(viewEntity);
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/plan/my", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPlanMy() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "plan_my.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Plan plan : planRepository.findByCurator(accessUtils.getCurrentPerson())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = plan.getId();
				viewEntity.name = plan.getName();
				viewEntity.type = plan.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = plan.getStatus().getName();
				viewEntity.realStatusCode = plan.getStatus().getCode();
				if (plan.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(plan.getDateStart());
				}
				if (plan.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(plan.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/plan/created_by_me", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPlanCreatedByMe() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "plan_created_by_me.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Plan plan : planRepository.findByAuthor(accessUtils.getCurrentPerson())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = plan.getId();
				viewEntity.name = plan.getName();
				viewEntity.type = plan.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = plan.getStatus().getName();
				viewEntity.realStatusCode = plan.getStatus().getCode();
				if (plan.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(plan.getDateStart());
				}
				if (plan.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(plan.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "lesson/to_me", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getLessonToMe() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "lesson_to_me.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_STUDENT)
				.getArrayList())) {
			int increment = 0;
			for (LessonPerson lessonPerson : lessonPersonRepository.findByPersonIdInWorkStatus(accessUtils.getCurrentPerson().getId())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = lessonPerson.getId();
				viewEntity.name = lessonPerson.getLesson().getName();
				viewEntity.type = lessonPerson.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = lessonPerson.getStatus().getName();
				if (lessonPerson.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(lessonPerson.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "testvariant/to_me", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTestVariantToMe() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(VIEW_CONFIG_PATH + "testvariant_to_me.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_STUDENT)
				.getArrayList())) {
			int increment = 0;
			for (TestVariantPerson testVariantPerson : testVariantPersonRepository.findByPersonIdInWorkStatus(accessUtils.getCurrentPerson().getId())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = testVariantPerson.getId();
				viewEntity.name = testVariantPerson.getTest().getName();
				viewEntity.type = testVariantPerson.getClass().getSimpleName().toLowerCase();
				viewEntity.realStatusName = testVariantPerson.getStatus().getName();
				if (testVariantPerson.getDateStart() != null) {
					viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(testVariantPerson.getDateStart());
				}
				if (testVariantPerson.getDateEnd() != null) {
					viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(testVariantPerson.getDateEnd());
				}
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

}