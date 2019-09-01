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
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Set;

import static com.dls.base.utils.Constant.*;

@RestController
@RequestMapping("dialog")
public class DialogRestController
{

	public static final String DIALOG_CONFIG_PATH = "/WEB-INF/dialog_config/";

	private final RoleRepository roleRepository;
	private final PersonRepository personRepository;
	private final GroupRepository groupRepository;
	private final TemplateLessonRepository templateLessonRepository;
	private final TemplateTestRepository templateTestRepository;
	private final TemplateCourseRepository templateCourseRepository;
	private final TemplatePlanRepository templatePlanRepository;
	private final TemplateTestVariantRepository templateTestVariantRepository;
	private final TestVariantPersonRepository testVariantPersonRepository;
	private final CategoryRepository categoryRepository;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	DialogRestController(RoleRepository roleRepository, PersonRepository personRepository, GroupRepository groupRepository, TemplateLessonRepository templateLessonRepository, TemplateTestRepository templateTestRepository, TemplateCourseRepository templateCourseRepository, TemplatePlanRepository templatePlanRepository, TemplateTestVariantRepository templateTestVariantRepository, TestVariantPersonRepository testVariantPersonRepository, CategoryRepository categoryRepository) throws Exception {
		this.roleRepository = roleRepository;
		this.personRepository = personRepository;
		this.groupRepository = groupRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.templateTestRepository = templateTestRepository;
		this.templateCourseRepository = templateCourseRepository;
		this.templatePlanRepository = templatePlanRepository;
		this.templateTestVariantRepository = templateTestVariantRepository;
		this.testVariantPersonRepository = testVariantPersonRepository;
		this.categoryRepository = categoryRepository;
	}

	@GetMapping(value = "/role/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getRoleAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(DIALOG_CONFIG_PATH + "role_all.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Role role : roleRepository.findAll()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = role.getName();
				viewEntity.type = role.getClass().getSimpleName().toLowerCase();
				viewEntity.code = role.getCode();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/person/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPersonAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(DIALOG_CONFIG_PATH + "person_active.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findActive()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = (person.getSurname() + " " + person.getName() + " " + person.getMiddlename());
				viewEntity.type = person.getClass().getSimpleName().toLowerCase();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/person/by_role/{roleCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPersonByRole(@PathVariable String roleCode) throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(DIALOG_CONFIG_PATH + "person_by_role.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Person person : personRepository.findByRoleCode(roleCode.toUpperCase())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = (person.getSurname() + " " + person.getName() + " " + person.getMiddlename());
				viewEntity.type = person.getClass().getSimpleName().toLowerCase();
				viewEntity.cardId = person.getId();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/group/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getGroupAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(DIALOG_CONFIG_PATH + "group_active.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Group group : groupRepository.findAll()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = group.getName();
				viewEntity.type = group.getClass().getSimpleName().toLowerCase();
				viewEntity.cardId = group.getId();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/group/active_by_current_curator", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getGroupActiveByCurrentCurator() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(DIALOG_CONFIG_PATH + "group_active_by_current_curator.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			for (Group group : groupRepository.findByCurator(accessUtils.getCurrentPerson())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = group.getName();
				viewEntity.type = group.getClass().getSimpleName().toLowerCase();
				viewEntity.cardId = group.getId();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/category/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getCategoryAll() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(DIALOG_CONFIG_PATH + "category_all.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.getArrayList())) {
			int increment = 0;
			for (Category category : categoryRepository.findAll()) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.name = category.getName();
				viewEntity.type = category.getClass().getSimpleName().toLowerCase();
				viewEntity.cardId = category.getId();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "/templatelesson/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateLessonWithStatusActive() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate("/WEB-INF/view_config/extra/templatelesson_active.xml", true);
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

	@GetMapping(value = "/templatetest/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateTestWithStatusActive() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate("/WEB-INF/view_config/extra/templatetest_active.xml", true);
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

	@GetMapping(value = "/templatelesson/by_category", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateLessonByCategory() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(DIALOG_CONFIG_PATH + "templatelesson_by_category.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
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

	@GetMapping(value = "/templatetest/by_category", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateTestByCategory() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate(DIALOG_CONFIG_PATH + "templatetest_by_category.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
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

	@GetMapping(value = "/templatecourse/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateCourseWithStatusActive() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate("/WEB-INF/view_config/extra/templatecourse_active.xml", true);
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

	@GetMapping(value = "/templateplan/active", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplatePlanWithStatusActive() throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate("/WEB-INF/view_config/extra/templateplan_active.xml", true);
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

	@GetMapping(value = "/templatetestvariant/{testVariantPersonId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateTestVariantByTest(@PathVariable Long testVariantPersonId) throws Exception {
		ViewTemplate viewTemplate = utils.getViewTemplate("/WEB-INF/view_config/extra/templatetestvariant_by_test.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			TestVariantPerson testVariantPerson = testVariantPersonRepository.findByTestVariantPersonId(testVariantPersonId);
			int increment = 0;
			for (TemplateTestVariant templateTestVariant : templateTestVariantRepository.findByTemplateTestId(testVariantPerson.getTemplateTestVariant().getTemplateTest().getId())) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = templateTestVariant.getId();
				viewEntity.name = "Вариант " + templateTestVariant.getNumber();
				viewEntity.type = templateTestVariant.getClass().getSimpleName().toLowerCase();
				viewTemplate.data.items.add(viewEntity);
			}
		}
		return utils.getJsonString(viewTemplate);
	}

}