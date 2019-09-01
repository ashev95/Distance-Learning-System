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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import static com.dls.base.utils.Constant.*;

@RestController
@RequestMapping("embedded_view")
public class EmbeddedViewRestController
{

	public static final String EMBEDDED_VIEW_CONFIG_PATH = "/WEB-INF/embedded_view_config/";

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

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	EmbeddedViewRestController(PersonRepository personRepository,
							   GroupRepository groupRepository,
							   GroupPersonRepository groupPersonRepository, TemplateLessonRepository templateLessonRepository,
							   LessonRepository lessonRepository, TemplateTestRepository templateTestRepository,
							   TemplateTestVariantRepository templateTestVariantRepository,
							   TemplateTestQuestionRepository templateTestQuestionRepository,
							   TemplateTestAnswerRepository templateTestAnswerRepository,
							   TemplateCourseRepository templateCourseRepository,
							   TemplatePlanRepository templatePlanRepository, TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository, TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository, LessonPersonRepository lessonPersonRepository, TestRepository testRepository, TestVariantPersonRepository testVariantPersonRepository, CourseRepository courseRepository, CourseResponseRepository courseResponseRepository, PlanRepository planRepository, PlanResponseRepository planResponseRepository) throws Exception {
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
	}

	@GetMapping(value = "templatetestvariant/by_parent/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateTestVariantByParent(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		ViewTemplate viewTemplate = utils.getViewTemplate(EMBEDDED_VIEW_CONFIG_PATH + "templatetestvariant_by_parent.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			TemplateTest templateTest = templateTestRepository.findByTemplateTestId(id);
			for (TemplateTestVariant templateTestVariant : templateTestVariantRepository.findByTemplateTestId(id)) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.id = ++increment;
				viewEntity.cardId = templateTestVariant.getId();
				viewEntity.name = "Вариант " + templateTestVariant.getNumber().toString();
				viewEntity.type = templateTestVariant.getClass().getSimpleName().toLowerCase();
				//
				if (templateTest != null) {
					viewEntity.parentCardId = templateTest.getId();
					viewEntity.parentType = templateTest.getClass().getSimpleName().toLowerCase();
				}
				//
				viewTemplate.data.items.add(viewEntity);
				if (templateTestQuestionRepository.findByTemplateTestVariantId(templateTestVariant.getId()).size() > 0) {
					viewEntity.children = new ArrayList<ViewEntity>();
					for (TemplateTestQuestion templateTestQuestion : templateTestQuestionRepository.findByTemplateTestVariantId(templateTestVariant.getId())) {
						ViewEntity viewEntity1 = new ViewEntity();
						viewEntity1.id = ++increment;
						viewEntity1.cardId = templateTestQuestion.getId();
						viewEntity1.name = "Вопрос " + templateTestQuestion.getNumber().toString();
						viewEntity1.type = templateTestQuestion.getClass().getSimpleName().toLowerCase();
						viewEntity1.correctScore = templateTestQuestion.getScorePositive().toString();
						viewEntity1.incorrectScore = templateTestQuestion.getScoreNegative().toString();
						//
						viewEntity1.parentCardId = viewEntity.cardId;
						viewEntity1.parentType = viewEntity.type;
						//
						viewEntity.children.add(viewEntity1);
						if (templateTestAnswerRepository.findByTemplateTestQuestionId(templateTestQuestion.getId()).size() > 0) {
							viewEntity1.children = new ArrayList<ViewEntity>();
							for (TemplateTestAnswer templateTestAnswer : templateTestAnswerRepository.findByTemplateTestQuestionId(templateTestQuestion.getId())) {
								ViewEntity viewEntity2 = new ViewEntity();
								viewEntity2.id = ++increment;
								viewEntity2.cardId = templateTestAnswer.getId();
								viewEntity2.name = "Ответ " + templateTestAnswer.getNumber().toString();
								viewEntity2.type = templateTestAnswer.getClass().getSimpleName().toLowerCase();
								viewEntity2.correct = (templateTestAnswer.getCorrect() ? "+" : "");
								//
								viewEntity2.parentCardId = viewEntity1.cardId;
								viewEntity2.parentType = viewEntity1.type;
								//
								viewEntity1.children.add(viewEntity2);
							}
						}
					}
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "templateresponse/by_templatecourse/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateResponseByTemplateCourse(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		ViewTemplate viewTemplate = utils.getViewTemplate(EMBEDDED_VIEW_CONFIG_PATH + "templateresponse_by_templatecourse.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 1;
			Set<TemplateCourseTemplateResponse> templateCourseTemplateResponseSet = templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByTemplateCourseId(id);
			if (templateCourseTemplateResponseSet.size() > 0) {
				HashMap<Long, Long> blocks = new HashMap<Long, Long>();
				for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseSet) {
					blocks.put(templateCourseTemplateResponse.getBlock().getPosition(), templateCourseTemplateResponse.getBlock().getPosition());
				}
				for (Long block : blocks.keySet()){
					ViewEntity viewEntity2 = new ViewEntity();
					viewEntity2.id = increment++;//123templatePlanTemplateResponse.getPosition();
					viewEntity2.type = "block";
					viewEntity2.block = block;
					ViewEntity prev = null;
					for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseSet) {
						if (templateCourseTemplateResponse.getBlock().getPosition().equals(block)){
							ViewEntity viewEntity = new ViewEntity();
							viewEntity.id = increment++;//123templatePlanTemplateResponse.getPosition();
							viewEntity.realCardId = templateCourseTemplateResponse.getId();
							viewEntity.realType = templateCourseTemplateResponse.getClass().getSimpleName().toLowerCase();
							viewEntity.block = templateCourseTemplateResponse.getBlock().getPosition();
							viewEntity.blockType = templateCourseTemplateResponse.getBlock().getType();
							viewEntity2.name = "Блок " + block + " - " + (templateCourseTemplateResponse.getBlock().getType().equals(Constant.BLOCK_TYPE_SERIAL) ? " Последовательный" : "Параллельный");
							switch (templateCourseTemplateResponse.getTemplateResponseClass()) {
								case "templatelesson":
									TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(templateCourseTemplateResponse.getTemplateResponseId());
									viewEntity.cardId = templateLesson.getId();
									viewEntity.name = "Урок: " + templateLesson.getName() + " (Версия " + templateLesson.getVersion() + ")";
									viewEntity.type = templateLesson.getClass().getSimpleName().toLowerCase();
									break;
								case "templatetest":
									TemplateTest templateTest = templateTestRepository.findByTemplateTestId(templateCourseTemplateResponse.getTemplateResponseId());
									viewEntity.cardId = templateTest.getId();
									viewEntity.name = "Тест: " + templateTest.getName() + " (Версия " + templateTest.getVersion() + ")";
									viewEntity.type = templateTest.getClass().getSimpleName().toLowerCase();
									break;
								default:
									throw new Exception("Не определён тип мероприятия");
							}
							if (prev != null){
								viewEntity.prevId = prev.id;
								viewEntity.prevCardId = prev.cardId;
								viewEntity.prevType = prev.type;

								prev.nextId = viewEntity.id;
								prev.nextCardId = viewEntity.cardId;
								prev.nextType = viewEntity.type;
							}
							prev = viewEntity;
							if (viewEntity2.children == null){
								viewEntity2.children = new ArrayList<ViewEntity>();
							}
							viewEntity2.children.add(viewEntity);
						}
					}
					viewTemplate.data.items.add(viewEntity2);
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "templateresponse/by_templateplan/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getTemplateResponseByTemplatePlan(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		ViewTemplate viewTemplate = utils.getViewTemplate(EMBEDDED_VIEW_CONFIG_PATH + "templateresponse_by_templateplan.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 1;
			Set<TemplatePlanTemplateResponse> templatePlanTemplateResponseSet = templatePlanTemplateResponseRepository.findTemplatePlanTemplateResponseByTemplatePlanId(id);
			if (templatePlanTemplateResponseSet.size() > 0) {
				HashMap<Long, Long> blocks = new HashMap<Long, Long>();
				for (TemplatePlanTemplateResponse templatePlanTemplateResponse : templatePlanTemplateResponseSet) {
					blocks.put(templatePlanTemplateResponse.getBlock().getPosition(), templatePlanTemplateResponse.getBlock().getPosition());
				}
				for (Long block : blocks.keySet()){
					ViewEntity viewEntity2 = new ViewEntity();
					viewEntity2.id = increment++;//123templatePlanTemplateResponse.getPosition();
					viewEntity2.type = "block";
					viewEntity2.children = new ArrayList<ViewEntity>();
					viewEntity2.block = block;
					ViewEntity prev = null;
					for (TemplatePlanTemplateResponse templatePlanTemplateResponse : templatePlanTemplateResponseSet) {
						if (templatePlanTemplateResponse.getBlock().getPosition().equals(block)){
							ViewEntity viewEntity = new ViewEntity();
							viewEntity.id = increment++;//123templatePlanTemplateResponse.getPosition();
							viewEntity.realCardId = templatePlanTemplateResponse.getId();
							viewEntity.realType = templatePlanTemplateResponse.getClass().getSimpleName().toLowerCase();
							viewEntity.block = templatePlanTemplateResponse.getBlock().getPosition();
							viewEntity.blockType = templatePlanTemplateResponse.getBlock().getType();
							viewEntity2.name = "Блок " + block + " - " + (templatePlanTemplateResponse.getBlock().getType().equals(Constant.BLOCK_TYPE_SERIAL) ? " Последовательный" : "Параллельный");
							switch (templatePlanTemplateResponse.getTemplateResponseClass()) {
								case "templatelesson":
									TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(templatePlanTemplateResponse.getTemplateResponseId());
									viewEntity.cardId = templateLesson.getId();
									viewEntity.name = "Урок: " + templateLesson.getName() + " (Версия " + templateLesson.getVersion() + ")";
									viewEntity.type = templateLesson.getClass().getSimpleName().toLowerCase();
									break;
								case "templatetest":
									TemplateTest templateTest = templateTestRepository.findByTemplateTestId(templatePlanTemplateResponse.getTemplateResponseId());
									viewEntity.cardId = templateTest.getId();
									viewEntity.name = "Тест: " + templateTest.getName() + " (Версия " + templateTest.getVersion() + ")";
									viewEntity.type = templateTest.getClass().getSimpleName().toLowerCase();
									break;
								case "templatecourse":
									TemplateCourse templateCourse = templateCourseRepository.findByTemplateCourseId(templatePlanTemplateResponse.getTemplateResponseId());
									viewEntity.cardId = templateCourse.getId();
									viewEntity.name = "Курс: " + templateCourse.getName() + " (Версия " + templateCourse.getVersion() + ")";
									viewEntity.type = templateCourse.getClass().getSimpleName().toLowerCase();
									break;
								default:
									throw new Exception("Не определён тип мероприятия");
							}
							if (prev != null){
								viewEntity.prevId = prev.id;
								viewEntity.prevCardId = prev.cardId;
								viewEntity.prevType = prev.type;

								prev.nextId = viewEntity.id;
								prev.nextCardId = viewEntity.cardId;
								prev.nextType = viewEntity.type;
							}
							prev = viewEntity;
							viewEntity2.children.add(viewEntity);
							if (viewEntity.type.equals("templatecourse")){
								Set<TemplateCourseTemplateResponse> templateCourseTemplateResponseSet = templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByTemplateCourseId(templatePlanTemplateResponse.getTemplateResponseId());
								if (templateCourseTemplateResponseSet.size() > 0) {
									HashMap<Long, Long> blocks1 = new HashMap<Long, Long>();
									for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseSet) {
										blocks1.put(templateCourseTemplateResponse.getBlock().getPosition(), templateCourseTemplateResponse.getBlock().getPosition());
									}
									for (Long block1 : blocks1.keySet()){
										ViewEntity viewEntity4 = new ViewEntity();
										viewEntity4.id = increment++;//123templatePlanTemplateResponse.getPosition();
										viewEntity4.type = "block";
										viewEntity4.block = block;
										viewEntity4.rowCustomStyle = "background-color: #cecece";
										ViewEntity prev1 = null;
										for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseSet) {
											if (templateCourseTemplateResponse.getBlock().getPosition().equals(block1)){
												ViewEntity viewEntity3 = new ViewEntity();
												viewEntity3.id = increment++;//123templatePlanTemplateResponse.getPosition();
												viewEntity3.realCardId = templateCourseTemplateResponse.getId();
												viewEntity3.realType = templateCourseTemplateResponse.getClass().getSimpleName().toLowerCase();
												viewEntity3.block = templateCourseTemplateResponse.getBlock().getPosition();
												viewEntity3.blockType = templateCourseTemplateResponse.getBlock().getType();
												viewEntity3.rowCustomStyle = "background-color: #cecece";
												viewEntity4.name = "Блок " + block1 + " - " + (templateCourseTemplateResponse.getBlock().getType().equals(Constant.BLOCK_TYPE_SERIAL) ? " Последовательный" : "Параллельный");
												switch (templateCourseTemplateResponse.getTemplateResponseClass()) {
													case "templatelesson":
														TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(templateCourseTemplateResponse.getTemplateResponseId());
														viewEntity3.cardId = templateLesson.getId();
														viewEntity3.name = "Урок: " + templateLesson.getName() + " (Версия " + templateLesson.getVersion() + ")";
														viewEntity3.type = templateLesson.getClass().getSimpleName().toLowerCase();
														break;
													case "templatetest":
														TemplateTest templateTest = templateTestRepository.findByTemplateTestId(templateCourseTemplateResponse.getTemplateResponseId());
														viewEntity3.cardId = templateTest.getId();
														viewEntity3.name = "Тест: " + templateTest.getName() + " (Версия " + templateTest.getVersion() + ")";
														viewEntity3.type = templateTest.getClass().getSimpleName().toLowerCase();
														break;
													default:
														throw new Exception("Не определён тип мероприятия");
												}
												if (prev1 != null){
													viewEntity3.prevId = prev1.id;
													viewEntity3.prevCardId = prev1.cardId;
													viewEntity3.prevType = prev1.type;

													prev1.nextId = viewEntity3.id;
													prev1.nextCardId = viewEntity3.cardId;
													prev1.nextType = viewEntity3.type;
												}
												prev1 = viewEntity3;
												if (viewEntity4.children == null){
													viewEntity4.children = new ArrayList<ViewEntity>();
												}
												viewEntity4.children.add(viewEntity3);
											}
										}
										if (viewEntity.children == null){
											viewEntity.children = new ArrayList<ViewEntity>();
										}
										viewEntity.children.add(viewEntity4);
									}
								}
							}
						}
					}
					viewTemplate.data.items.add(viewEntity2);
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "person/by_group/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPersonByGroup(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		ViewTemplate viewTemplate = utils.getViewTemplate(EMBEDDED_VIEW_CONFIG_PATH + "person_by_group.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			Set<GroupPerson> groupPersonSet = groupPersonRepository.findByGroupId(id);
			if (groupPersonSet.size() > 0) {
				for (GroupPerson groupPerson : groupPersonSet) {
					Person person = personRepository.findByPersonId(groupPerson.getPerson().getId());
					ViewEntity viewEntity = new ViewEntity();
					viewEntity.id = ++increment;
					viewEntity.cardId = person.getId();
					viewEntity.name = person.getName();
					viewEntity.type = person.getClass().getSimpleName().toLowerCase();
					viewEntity.realCardId = groupPerson.getId();
					viewEntity.realType = groupPerson.getClass().getSimpleName().toLowerCase();
					viewTemplate.data.items.add(viewEntity);
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "person/by_lesson/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPersonByLesson(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		ViewTemplate viewTemplate = utils.getViewTemplate(EMBEDDED_VIEW_CONFIG_PATH + "person_by_lesson.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			Set<LessonPerson> lessonPersonSet = lessonPersonRepository.findByLessonId(id);
			if (lessonPersonSet.size() > 0) {
				for (LessonPerson lessonPerson : lessonPersonSet) {
					Person person = personRepository.findByPersonId(lessonPerson.getPerson().getId());
					ViewEntity viewEntity = new ViewEntity();
					viewEntity.id = ++increment;
					viewEntity.cardId = person.getId();
					viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
					viewEntity.type = person.getClass().getSimpleName().toLowerCase();
					viewEntity.realCardId = lessonPerson.getId();
					viewEntity.realType = "traininglesson";
					viewEntity.realStatusCode = lessonPerson.getStatus().getCode();
					viewEntity.realStatusName = lessonPerson.getStatus().getName();
					if (lessonPerson.getDateEnd() != null) {
						viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
								.format(lessonPerson.getDateEnd());
					}
					viewTemplate.data.items.add(viewEntity);
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "response/by_course/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getResponseByCourse(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		ViewTemplate viewTemplate = utils.getViewTemplate(EMBEDDED_VIEW_CONFIG_PATH + "response_by_course.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 1;
			Set<CourseResponse> CourseResponseSet = courseResponseRepository.findByCourseId(id);
			if (CourseResponseSet.size() > 0) {
				HashMap<Long, Long> blocks = new HashMap<Long, Long>();
				for (CourseResponse CourseResponse : CourseResponseSet) {
					blocks.put(CourseResponse.getBlock().getPosition(), CourseResponse.getBlock().getPosition());
				}
				for (Long block : blocks.keySet()){
					ViewEntity viewEntity2 = new ViewEntity();
					viewEntity2.id = increment++;//123PlanResponse.getPosition();
					viewEntity2.type = "block";
					viewEntity2.children = new ArrayList<ViewEntity>();
					viewEntity2.block = block;
					ViewEntity prev = null;
					for (CourseResponse CourseResponse : CourseResponseSet) {
						if (CourseResponse.getBlock().getPosition().equals(block)){
							ViewEntity viewEntity = new ViewEntity();
							viewEntity.id = increment++;//123PlanResponse.getPosition();
							viewEntity.cardId = CourseResponse.getResponseId();
							viewEntity.type = CourseResponse.getResponseClass();
							viewEntity.realCardId = CourseResponse.getId();
							viewEntity.realType = "trainingcourse";
							viewEntity.block = CourseResponse.getBlock().getPosition();
							viewEntity.blockType = CourseResponse.getBlock().getType();
							viewEntity2.name = "Блок " + block + " - " + (CourseResponse.getBlock().getType().equals(Constant.BLOCK_TYPE_SERIAL) ? " Последовательный" : "Параллельный");
							Date dateStart1 = null;
							Date dateEnd1 = null;
							switch (CourseResponse.getResponseClass()) {
								case "lesson":
									Lesson lesson = lessonRepository.findByLessonId(CourseResponse.getResponseId());
									viewEntity.name = "Урок: " + lesson.getName();
									viewEntity.realStatusName = lesson.getStatus().getName();
									viewEntity.realStatusCode = lesson.getStatus().getCode();
									viewEntity.template = lesson.getTemplateLesson().getName() + " (Версия " + lesson.getTemplateLesson().getVersion() + ")";
									dateStart1 = lesson.getDateStart();
									dateEnd1 = lesson.getDateEnd();
									//
									Set<LessonPerson> lessonPersonSet = lessonPersonRepository.findByLessonId(lesson.getId());
									if (lessonPersonSet.size() > 0) {
										for (LessonPerson lessonPerson : lessonPersonSet) {
											Person person = personRepository.findByPersonId(lessonPerson.getPerson().getId());
											ViewEntity viewEntity3 = new ViewEntity();
											viewEntity3.id = increment++;
											viewEntity3.cardId = person.getId();
											viewEntity3.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
											viewEntity3.type = person.getClass().getSimpleName().toLowerCase();
											viewEntity3.realCardId = lessonPerson.getId();
											viewEntity3.realType = "traininglesson";
											viewEntity3.realStatusCode = lessonPerson.getStatus().getCode();
											viewEntity3.realStatusName = lessonPerson.getStatus().getName();
											if (lessonPerson.getDateEnd() != null) {
												viewEntity3.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
														.format(lessonPerson.getDateEnd());
											}
											if (viewEntity.children == null){
												viewEntity.children = new ArrayList<ViewEntity>();
											}
											viewEntity.children.add(viewEntity3);
										}
									}
									//
									break;
								case "test":
									Test test = testRepository.findByTestId(CourseResponse.getResponseId());
									viewEntity.name = "Тест: " + test.getName();
									viewEntity.realStatusName = test.getStatus().getName();
									viewEntity.realStatusCode = test.getStatus().getCode();
									viewEntity.template = test.getTemplateTest().getName() + " (Версия " + test.getTemplateTest().getVersion() + ")";
									dateStart1 = test.getDateStart();
									dateEnd1 = test.getDateEnd();
									//
									Set<TestVariantPerson> testVariantPersonSet = testVariantPersonRepository.findByTestId(test.getId());
									if (testVariantPersonSet.size() > 0) {
										for (TestVariantPerson testVariantPerson : testVariantPersonSet) {
											Person person = personRepository.findByPersonId(testVariantPerson.getPerson().getId());
											ViewEntity viewEntity4 = new ViewEntity();
											viewEntity4.id = increment++;
											viewEntity4.cardId = person.getId();
											viewEntity4.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename() + " (вариант " + testVariantPerson.getTemplateTestVariant().getNumber() + ")";
											viewEntity4.type = person.getClass().getSimpleName().toLowerCase();
											viewEntity4.realCardId = testVariantPerson.getId();
											viewEntity4.realType = "trainingtestvariant";
											viewEntity4.realStatusCode = testVariantPerson.getStatus().getCode();
											viewEntity4.realStatusName = testVariantPerson.getStatus().getName();
											if (testVariantPerson.getStatus().getCode().equals("completed")){
												viewEntity4.correctAnswerRatio = testVariantPerson.getCorrectAnswerCount() + "/" + (testVariantPerson.getCorrectAnswerCount() + testVariantPerson.getIncorrectAnswerCount());
												viewEntity4.scoreRatio = testVariantPerson.getTotalScore() + "/" + testVariantPerson.getAvailableScore();
											}
											if (testVariantPerson.getDateStart() != null) {
												viewEntity4.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
														.format(testVariantPerson.getDateStart());
											}
											if (testVariantPerson.getDateEnd() != null) {
												viewEntity4.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
														.format(testVariantPerson.getDateEnd());
											}
											if (viewEntity.children == null){
												viewEntity.children = new ArrayList<ViewEntity>();
											}
											viewEntity.children.add(viewEntity4);
										}
									}
									//
									break;
								default:
									throw new Exception("Не определён тип мероприятия");
							}
							if (dateStart1 != null) {
								viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
										.format(dateStart1);
							}
							if (dateEnd1 != null) {
								viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
										.format(dateEnd1);
							}
							if (prev != null){
								viewEntity.prevId = prev.id;
								viewEntity.prevCardId = prev.cardId;
								viewEntity.prevType = prev.type;

								prev.nextId = viewEntity.id;
								prev.nextCardId = viewEntity.cardId;
								prev.nextType = viewEntity.type;
							}
							prev = viewEntity;
							viewEntity2.children.add(viewEntity);
						}
					}
					viewTemplate.data.items.add(viewEntity2);
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "response/by_plan/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getResponseByPlan(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		ViewTemplate viewTemplate = utils.getViewTemplate(EMBEDDED_VIEW_CONFIG_PATH + "response_by_plan.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 1;
			Set<PlanResponse> planResponseSet = planResponseRepository.findPlanResponseByPlanId(id);
			if (planResponseSet.size() > 0) {
				HashMap<Long, Long> blocks = new HashMap<Long, Long>();
				for (PlanResponse planResponse : planResponseSet) {
					blocks.put(planResponse.getBlock().getPosition(), planResponse.getBlock().getPosition());
				}
				for (Long block : blocks.keySet()){
					ViewEntity viewEntity2 = new ViewEntity();
					viewEntity2.id = increment++;//123planResponse.getPosition();
					viewEntity2.type = "block";
					viewEntity2.children = new ArrayList<ViewEntity>();
					viewEntity2.block = block;
					ViewEntity prev = null;
					for (PlanResponse planResponse : planResponseSet) {
						if (planResponse.getBlock().getPosition().equals(block)){
							ViewEntity viewEntity = new ViewEntity();
							viewEntity.id = increment++;//123planResponse.getPosition();
							viewEntity.cardId = planResponse.getResponseId();
							viewEntity.type = planResponse.getResponseClass();
							viewEntity.realCardId = planResponse.getId();
							viewEntity.realType = "trainingplan";
							viewEntity.block = planResponse.getBlock().getPosition();
							viewEntity.blockType = planResponse.getBlock().getType();
							viewEntity2.name = "Блок " + block + " - " + (planResponse.getBlock().getType().equals(Constant.BLOCK_TYPE_SERIAL) ? " Последовательный" : "Параллельный");
							Date dateStart = null;
							Date dateEnd = null;
							switch (planResponse.getResponseClass()) {
								case "lesson":
									Lesson lesson = lessonRepository.findByLessonId(planResponse.getResponseId());
									viewEntity.name = "Урок: " + lesson.getName();
									viewEntity.realStatusName = lesson.getStatus().getName();
									viewEntity.realStatusCode = lesson.getStatus().getCode();
									viewEntity.template = lesson.getTemplateLesson().getName() + " (Версия " + lesson.getTemplateLesson().getVersion() + ")";
									dateStart = lesson.getDateStart();
									dateEnd = lesson.getDateEnd();
									//
									Set<LessonPerson> lessonPersonSet = lessonPersonRepository.findByLessonId(lesson.getId());
									if (lessonPersonSet.size() > 0) {
										for (LessonPerson lessonPerson : lessonPersonSet) {
											Person person = personRepository.findByPersonId(lessonPerson.getPerson().getId());
											ViewEntity viewEntity3 = new ViewEntity();
											viewEntity3.id = increment++;
											viewEntity3.cardId = person.getId();
											viewEntity3.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
											viewEntity3.type = person.getClass().getSimpleName().toLowerCase();
											viewEntity3.realCardId = lessonPerson.getId();
											viewEntity3.realType = "traininglesson";
											viewEntity3.realStatusCode = lessonPerson.getStatus().getCode();
											viewEntity3.realStatusName = lessonPerson.getStatus().getName();
											if (lessonPerson.getDateEnd() != null) {
												viewEntity3.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
														.format(lessonPerson.getDateEnd());
											}
											if (viewEntity.children == null){
												viewEntity.children = new ArrayList<ViewEntity>();
											}
											viewEntity.children.add(viewEntity3);
										}
									}
									//
									break;
								case "test":
									Test test = testRepository.findByTestId(planResponse.getResponseId());
									viewEntity.name = "Тест: " + test.getName();
									viewEntity.realStatusName = test.getStatus().getName();
									viewEntity.realStatusCode = test.getStatus().getCode();
									viewEntity.template = test.getTemplateTest().getName() + " (Версия " + test.getTemplateTest().getVersion() + ")";
									dateStart = test.getDateStart();
									dateEnd = test.getDateEnd();
									//
									Set<TestVariantPerson> testVariantPersonSet = testVariantPersonRepository.findByTestId(test.getId());
									if (testVariantPersonSet.size() > 0) {
										for (TestVariantPerson testVariantPerson : testVariantPersonSet) {
											Person person = personRepository.findByPersonId(testVariantPerson.getPerson().getId());
											ViewEntity viewEntity4 = new ViewEntity();
											viewEntity4.id = increment++;
											viewEntity4.cardId = person.getId();
											viewEntity4.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename() + " (вариант " + testVariantPerson.getTemplateTestVariant().getNumber() + ")";
											viewEntity4.type = person.getClass().getSimpleName().toLowerCase();
											viewEntity4.realCardId = testVariantPerson.getId();
											viewEntity4.realType = "trainingtestvariant";
											viewEntity4.realStatusCode = testVariantPerson.getStatus().getCode();
											viewEntity4.realStatusName = testVariantPerson.getStatus().getName();
											if (testVariantPerson.getStatus().getCode().equals("completed")){
												viewEntity4.correctAnswerRatio = testVariantPerson.getCorrectAnswerCount() + "/" + (testVariantPerson.getCorrectAnswerCount() + testVariantPerson.getIncorrectAnswerCount());
												viewEntity4.scoreRatio = testVariantPerson.getTotalScore() + "/" + testVariantPerson.getAvailableScore();
											}
											if (testVariantPerson.getDateStart() != null) {
												viewEntity4.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
														.format(testVariantPerson.getDateStart());
											}
											if (testVariantPerson.getDateEnd() != null) {
												viewEntity4.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
														.format(testVariantPerson.getDateEnd());
											}
											if (viewEntity.children == null){
												viewEntity.children = new ArrayList<ViewEntity>();
											}
											viewEntity.children.add(viewEntity4);
										}
									}
									//
									break;
								case "course":
									Course course = courseRepository.findByCourseId(planResponse.getResponseId());
									viewEntity.name = "Курс: " + course.getName();
									viewEntity.realStatusName = course.getStatus().getName();
									viewEntity.realStatusCode = course.getStatus().getCode();
									viewEntity.template = course.getTemplateCourse().getName() + " (Версия " + course.getTemplateCourse().getVersion() + ")";
									dateStart = course.getDateStart();
									dateEnd = course.getDateEnd();
									break;
								default:
									throw new Exception("Не определён тип мероприятия");
							}
							if (dateStart != null) {
								viewEntity.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
										.format(dateStart);
							}
							if (dateEnd != null) {
								viewEntity.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
										.format(dateEnd);
							}
							if (prev != null){
								viewEntity.prevId = prev.id;
								viewEntity.prevCardId = prev.cardId;
								viewEntity.prevType = prev.type;

								prev.nextId = viewEntity.id;
								prev.nextCardId = viewEntity.cardId;
								prev.nextType = viewEntity.type;
							}
							prev = viewEntity;
							viewEntity2.children.add(viewEntity);
							if (viewEntity.type.equals("course")){
								Set<CourseResponse> courseResponseSet = courseResponseRepository.findByCourseId(planResponse.getResponseId());
								if (courseResponseSet.size() > 0) {
									viewEntity.children = new ArrayList<ViewEntity>();
									HashMap<Long, Long> blocks1 = new HashMap<Long, Long>();
									for (CourseResponse courseResponse : courseResponseSet) {
										blocks1.put(courseResponse.getBlock().getPosition(), courseResponse.getBlock().getPosition());
									}
									for (Long block1 : blocks1.keySet()){
										ViewEntity viewEntity4 = new ViewEntity();
										viewEntity4.id = increment++;//123planResponse.getPosition();
										viewEntity4.type = "block";
										viewEntity4.block = block;
										//viewEntity4.rowCustomStyle = "background-color: #cecece";
										ViewEntity prev1 = null;
										for (CourseResponse courseResponse : courseResponseSet) {
											if (courseResponse.getBlock().getPosition().equals(block1)){
												ViewEntity viewEntity3 = new ViewEntity();
												viewEntity3.id = increment++;//123planResponse.getPosition();
												viewEntity3.cardId = courseResponse.getResponseId();
												viewEntity3.type = courseResponse.getResponseClass();
												viewEntity3.realCardId = courseResponse.getId();
												viewEntity3.realType = "trainingcourse";
												viewEntity3.block = courseResponse.getBlock().getType();
												viewEntity3.blockType = courseResponse.getBlock().getType();
												//viewEntity3.rowCustomStyle = "background-color: #cecece";
												viewEntity4.name = "Блок " + block1 + " - " + (courseResponse.getBlock().getType().equals(Constant.BLOCK_TYPE_SERIAL) ? " Последовательный" : "Параллельный");
												Date dateStart1 = null;
												Date dateEnd1 = null;
												switch (courseResponse.getResponseClass()) {
													case "lesson":
														Lesson lesson = lessonRepository.findByLessonId(courseResponse.getResponseId());
														viewEntity3.name = "Урок: " + lesson.getName();
														viewEntity3.realStatusName = lesson.getStatus().getName();
														viewEntity3.realStatusCode = lesson.getStatus().getCode();
														viewEntity3.template = lesson.getTemplateLesson().getName() + " (Версия " + lesson.getTemplateLesson().getVersion() + ")";
														dateStart1 = lesson.getDateStart();
														dateEnd1 = lesson.getDateEnd();
														//
														Set<LessonPerson> lessonPersonSet = lessonPersonRepository.findByLessonId(lesson.getId());
														if (lessonPersonSet.size() > 0) {
															for (LessonPerson lessonPerson : lessonPersonSet) {
																Person person = personRepository.findByPersonId(lessonPerson.getPerson().getId());
																ViewEntity viewEntity5 = new ViewEntity();
																viewEntity5.id = increment++;
																viewEntity5.cardId = person.getId();
																viewEntity5.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
																viewEntity5.type = person.getClass().getSimpleName().toLowerCase();
																viewEntity5.realCardId = lessonPerson.getId();
																viewEntity5.realType = "traininglesson";
																viewEntity5.realStatusCode = lessonPerson.getStatus().getCode();
																viewEntity5.realStatusName = lessonPerson.getStatus().getName();
																if (lessonPerson.getDateEnd() != null) {
																	viewEntity5.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
																			.format(lessonPerson.getDateEnd());
																}
																if (viewEntity3.children == null){
																	viewEntity3.children = new ArrayList<ViewEntity>();
																}
																viewEntity3.children.add(viewEntity5);
															}
														}
														//
														break;
													case "test":
														Test test = testRepository.findByTestId(courseResponse.getResponseId());
														viewEntity3.name = "Тест: " + test.getName();
														viewEntity3.realStatusName = test.getStatus().getName();
														viewEntity3.realStatusCode = test.getStatus().getCode();
														viewEntity3.template = test.getTemplateTest().getName() + " (Версия " + test.getTemplateTest().getVersion() + ")";
														dateStart1 = test.getDateStart();
														dateEnd1 = test.getDateEnd();
														//
														Set<TestVariantPerson> testVariantPersonSet = testVariantPersonRepository.findByTestId(test.getId());
														if (testVariantPersonSet.size() > 0) {
															for (TestVariantPerson testVariantPerson : testVariantPersonSet) {
																Person person = personRepository.findByPersonId(testVariantPerson.getPerson().getId());
																ViewEntity viewEntity6 = new ViewEntity();
																viewEntity6.id = increment++;
																viewEntity6.cardId = person.getId();
																viewEntity6.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename() + " (вариант " + testVariantPerson.getTemplateTestVariant().getNumber() + ")";
																viewEntity6.type = person.getClass().getSimpleName().toLowerCase();
																viewEntity6.realCardId = testVariantPerson.getId();
																viewEntity6.realType = "trainingtestvariant";
																viewEntity6.realStatusCode = testVariantPerson.getStatus().getCode();
																viewEntity6.realStatusName = testVariantPerson.getStatus().getName();
																if (testVariantPerson.getStatus().getCode().equals("completed")){
																	viewEntity6.correctAnswerRatio = testVariantPerson.getCorrectAnswerCount() + "/" + (testVariantPerson.getCorrectAnswerCount() + testVariantPerson.getIncorrectAnswerCount());
																	viewEntity6.scoreRatio = testVariantPerson.getTotalScore() + "/" + testVariantPerson.getAvailableScore();
																}
																if (testVariantPerson.getDateStart() != null) {
																	viewEntity6.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
																			.format(testVariantPerson.getDateStart());
																}
																if (testVariantPerson.getDateEnd() != null) {
																	viewEntity6.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
																			.format(testVariantPerson.getDateEnd());
																}
																if (viewEntity3.children == null){
																	viewEntity3.children = new ArrayList<ViewEntity>();
																}
																viewEntity3.children.add(viewEntity6);
															}
														}
														//
														break;
													default:
														throw new Exception("Не определён тип мероприятия");
												}
												if (dateStart1 != null) {
													viewEntity3.realStartDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
															.format(dateStart1);
												}
												if (dateEnd1 != null) {
													viewEntity3.realEndDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
															.format(dateEnd1);
												}
												if (prev1 != null){
													viewEntity3.prevId = prev1.id;
													viewEntity3.prevCardId = prev1.cardId;
													viewEntity3.prevType = prev1.type;

													prev1.nextId = viewEntity3.id;
													prev1.nextCardId = viewEntity3.cardId;
													prev1.nextType = viewEntity3.type;
												}
												prev1 = viewEntity3;
												if (viewEntity4.children == null){
													viewEntity4.children = new ArrayList<ViewEntity>();
												}
												viewEntity4.children.add(viewEntity3);
											}
										}
										viewEntity.children.add(viewEntity4);
									}
								}
							}
						}
					}
					viewTemplate.data.items.add(viewEntity2);
				}
			}
		}
		return utils.getJsonString(viewTemplate);
	}

	@GetMapping(value = "person/by_testvariant/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getPersonByTestVariant(@PathVariable String identifier) throws Exception {
		Long id = Long.parseLong(identifier);
		ViewTemplate viewTemplate = utils.getViewTemplate(EMBEDDED_VIEW_CONFIG_PATH + "person_by_testvariant.xml", true);
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			int increment = 0;
			Set<TestVariantPerson> testVariantPersonSet = testVariantPersonRepository.findByTestId(id);
			if (testVariantPersonSet.size() > 0) {
				for (TestVariantPerson testVariantPerson : testVariantPersonSet) {
					Person person = personRepository.findByPersonId(testVariantPerson.getPerson().getId());
					ViewEntity viewEntity = new ViewEntity();
					viewEntity.id = ++increment;
					viewEntity.cardId = person.getId();
					viewEntity.name = person.getSurname() + " " + person.getName() + " " + person.getMiddlename();
					viewEntity.type = person.getClass().getSimpleName().toLowerCase();
					viewEntity.realCardId = testVariantPerson.getId();
					viewEntity.realType = "trainingtestvariant";
					viewEntity.variant = testVariantPerson.getTemplateTestVariant().getNumber();
					viewEntity.realStatusCode = testVariantPerson.getStatus().getCode();
					viewEntity.realStatusName = testVariantPerson.getStatus().getName();
					if (testVariantPerson.getStatus().getCode().equals("completed")){
						viewEntity.correctAnswerRatio = testVariantPerson.getCorrectAnswerCount() + "/" + (testVariantPerson.getCorrectAnswerCount() + testVariantPerson.getIncorrectAnswerCount());
						viewEntity.scoreRatio = testVariantPerson.getTotalScore() + "/" + testVariantPerson.getAvailableScore();
					}
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
		}
		return utils.getJsonString(viewTemplate);
	}

}