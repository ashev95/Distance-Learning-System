package com.dls.base.utils;

import com.dls.base.controller.form.FormCourseRestController;
import com.dls.base.controller.form.FormLessonRestController;
import com.dls.base.controller.form.FormTestRestController;
import com.dls.base.entity.*;
import com.dls.base.pattern.observer.training.TrainingDeleter;
import com.dls.base.pattern.observer.training.TrainingEntity;
import com.dls.base.pattern.observer.training.TrainingSaver;
import com.dls.base.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class MoveUtils {


	private final LessonPersonRepository lessonPersonRepository;
	private final CourseResponseRepository courseResponseRepository;
	private final LessonRepository lessonRepository;
	private final TestRepository testRepository;
	private final TestVariantPersonRepository testVariantPersonRepository;
	private final MoveRepository moveRepository;
	private final StatusRepository statusRepository;
	private final CourseRepository courseRepository;
	private final PlanResponseRepository planResponseRepository;
	private final PlanRepository planRepository;
	private final TemplateLessonRepository templateLessonRepository;
	private final TemplateTestRepository templateTestRepository;
	private final TemplateCourseRepository templateCourseRepository;
	private final TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository;
	private final TemplatePlanRepository templatePlanRepository;
	private final TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository;
	private final TemplateTestVariantRepository templateTestVariantRepository;
	private final TemplateTestQuestionRepository templateTestQuestionRepository;
	private final TemplateTestAnswerRepository templateTestAnswerRepository;
	private final BlockRepository blockRepository;
	private final CategoryRepository categoryRepository;
	private final TestQuestionRepository testQuestionRepository;
	private final TestAnswerRepository testAnswerRepository;

	@Autowired
	public MoveUtils(LessonPersonRepository lessonPersonRepository, CourseResponseRepository courseResponseRepository, LessonRepository lessonRepository, TestRepository testRepository, TestVariantPersonRepository testVariantPersonRepository, MoveRepository moveRepository, StatusRepository statusRepository, CourseRepository courseRepository, PlanResponseRepository planResponseRepository, PlanRepository planRepository, TemplateLessonRepository templateLessonRepository, TemplateTestRepository templateTestRepository, TemplateCourseRepository templateCourseRepository, TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository, TemplatePlanRepository templatePlanRepository, TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository, TemplateTestVariantRepository templateTestVariantRepository, TemplateTestQuestionRepository templateTestQuestionRepository, TemplateTestAnswerRepository templateTestAnswerRepository, BlockRepository blockRepository, CategoryRepository categoryRepository, TestQuestionRepository testQuestionRepository, TestAnswerRepository testAnswerRepository) {
		this.lessonPersonRepository = lessonPersonRepository;
		this.courseResponseRepository = courseResponseRepository;
		this.lessonRepository = lessonRepository;
		this.testRepository = testRepository;
		this.testVariantPersonRepository = testVariantPersonRepository;
		this.moveRepository = moveRepository;
		this.statusRepository = statusRepository;
		this.courseRepository = courseRepository;
		this.planResponseRepository = planResponseRepository;
		this.planRepository = planRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.templateTestRepository = templateTestRepository;
		this.templateCourseRepository = templateCourseRepository;
		this.templateCourseTemplateResponseRepository = templateCourseTemplateResponseRepository;
		this.templatePlanRepository = templatePlanRepository;
		this.templatePlanTemplateResponseRepository = templatePlanTemplateResponseRepository;
		this.templateTestVariantRepository = templateTestVariantRepository;
		this.templateTestQuestionRepository = templateTestQuestionRepository;
		this.templateTestAnswerRepository = templateTestAnswerRepository;
		this.blockRepository = blockRepository;
		this.categoryRepository = categoryRepository;
		this.testQuestionRepository = testQuestionRepository;
		this.testAnswerRepository = testAnswerRepository;
	}

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	FormLessonRestController formLessonRestController;

	@Autowired
	FormTestRestController formTestRestController;

	@Autowired
	FormCourseRestController formCourseRestController;

	public void processTrainingParents(Object childTrainingObject) throws Exception {
		TrainingSaver trainingSaver = new TrainingSaver();
		processTrainingParentRecursive(childTrainingObject, trainingSaver);
		trainingSaver.saveAll();
	}

	private boolean processTrainingParentRecursive(Object childTrainingObject, TrainingSaver trainingSaver) throws Exception {
		switch (childTrainingObject.getClass().getSimpleName().toLowerCase()){
			case "lesson" :
				Lesson lesson2 = (Lesson)childTrainingObject;
				CourseResponse courseResponse3 = courseResponseRepository.findByResponseClassAndResponseId(lesson2.getClass().getSimpleName().toLowerCase(), lesson2.getId());
				if (courseResponse3 != null){
					processTrainingParentRecursive(courseResponse3, trainingSaver);
				}else{
					PlanResponse planResponse = planResponseRepository.findByResponseClassAndResponseId(lesson2.getClass().getSimpleName().toLowerCase(), lesson2.getId());
					if (planResponse != null){
						processTrainingParentRecursive(planResponse, trainingSaver);
					}
				}
				break;
			case "test" :
				Test test2 = (Test)childTrainingObject;
				CourseResponse courseResponse4 = courseResponseRepository.findByResponseClassAndResponseId(test2.getClass().getSimpleName().toLowerCase(), test2.getId());
				if (courseResponse4 != null){
					processTrainingParentRecursive(courseResponse4, trainingSaver);
				}else{
					PlanResponse planResponse = planResponseRepository.findByResponseClassAndResponseId(test2.getClass().getSimpleName().toLowerCase(), test2.getId());
					if (planResponse != null){
						processTrainingParentRecursive(planResponse, trainingSaver);
					}
				}
				break;
			case "lessonperson" :
				Lesson lesson = ((LessonPerson)childTrainingObject).getLesson();
				for (LessonPerson lessonPerson : lessonPersonRepository.findByLessonId(lesson.getId())){
					if (!(lessonPerson.getStatus().getCode().equals("studied") || lessonPerson.getStatus().getCode().equals("canceled"))){
						return false;
					}
				}
				Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(
						lesson.getClass().getSimpleName().toLowerCase(),
						lesson.getStatus().getId(),
						statusRepository.findByStatusCode("completed").getId()
				);
				lesson.setDateEnd(new Date());
				TrainingEntity trainingEntity = trainingSaver.getTrainingEntityOrNew(lessonRepository, lesson);
				trainingEntity.setStatus(move.getToStatus());
				CourseResponse courseResponse = courseResponseRepository.findByResponseClassAndResponseId(lesson.getClass().getSimpleName().toLowerCase(), lesson.getId());
				if (courseResponse != null){
					if (!processTrainingParentRecursive(courseResponse, trainingSaver)){
						return false;
					}
				}
				PlanResponse planResponse = planResponseRepository.findByResponseClassAndResponseId(lesson.getClass().getSimpleName().toLowerCase(), lesson.getId());
				if (planResponse != null){
					if (!processTrainingParentRecursive(planResponse, trainingSaver)){
						return false;
					}
				}
				break;
			case "testvariantperson" :
				Test test = ((TestVariantPerson)childTrainingObject).getTest();
				for (TestVariantPerson testVariantPerson : testVariantPersonRepository.findByTestId(test.getId())){
					if (!(testVariantPerson.getStatus().getCode().equals("completed") || testVariantPerson.getStatus().getCode().equals("canceled") || testVariantPerson.getStatus().getCode().equals("expired"))){
						return false;
					}
				}
				Move move1 = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(
						test.getClass().getSimpleName().toLowerCase(),
						test.getStatus().getId(),
						statusRepository.findByStatusCode("completed").getId()
				);
				test.setDateEnd(new Date());
				TrainingEntity trainingEntity1 = trainingSaver.getTrainingEntityOrNew(testRepository, test);
				trainingEntity1.setStatus(move1.getToStatus());
				CourseResponse courseResponse1 = courseResponseRepository.findByResponseClassAndResponseId(test.getClass().getSimpleName().toLowerCase(), test.getId());
				if (courseResponse1 != null){
					if (!processTrainingParentRecursive(courseResponse1, trainingSaver)){
						return false;
					}
				}
				PlanResponse planResponse1 = planResponseRepository.findByResponseClassAndResponseId(test.getClass().getSimpleName().toLowerCase(), test.getId());
				if (planResponse1 != null){
					if (!processTrainingParentRecursive(planResponse1, trainingSaver)){
						return false;
					}
				}
				break;
			case "block":
				Block block2 = (Block)childTrainingObject;
				if (!blockIsFinished(block2, trainingSaver)){
					startBlock(block2, trainingSaver);
					return false;
				}
				switch (block2.getParentClass()){
					case "course":
						Course course = courseRepository.findByCourseId(block2.getParentId());
						if (!processTrainingParentRecursive(course, trainingSaver)){
							return false;
						}
						break;
					case "plan":
						Plan plan = planRepository.findByPlanId(block2.getParentId());
						if (!processTrainingParentRecursive(plan, trainingSaver)){
							return false;
						}
						break;
					default:
						throw new Exception("Не определён тип родительского мероприятия");
				}
				break;
			case "courseresponse" :
				CourseResponse courseResponse2 = ((CourseResponse)childTrainingObject);
				if (!processTrainingParentRecursive(courseResponse2.getBlock(), trainingSaver)){
					return false;
				}
				break;
			case "planresponse" :
				PlanResponse planResponse2 = ((PlanResponse)childTrainingObject);
				if (!processTrainingParentRecursive(planResponse2.getBlock(), trainingSaver)){
					return false;
				}
				break;
			case "course" :
				Course course1 = (Course) childTrainingObject;
				if (!trainingIsFinished(course1, trainingSaver)){
					return false;
				}
				if (!(course1.getStatus().getCode().equals("completed") || course1.getStatus().getCode().equals("canceled"))){
					Move move2 = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(
							course1.getClass().getSimpleName().toLowerCase(),
							course1.getStatus().getId(),
							statusRepository.findByStatusCode("completed").getId()
					);
					course1.setDateEnd(new Date());
					course1.setStatus(move2.getToStatus());
					TrainingEntity trainingEntity2 = trainingSaver.getTrainingEntityOrNew(courseRepository, course1);
					trainingEntity2.setStatus(move2.getToStatus());
					//
					processTrainingParentRecursive(course1, trainingSaver);
				}
				PlanResponse planResponse6 = planResponseRepository.findByResponseClassAndResponseId(course1.getClass().getSimpleName().toLowerCase(), course1.getId());
				if (planResponse6 != null){
					processTrainingParentRecursive(planResponse6, trainingSaver);
				}
				break;
			case "plan" :
				Plan plan = (Plan) childTrainingObject;
				if (!trainingIsFinished(plan, trainingSaver)){
					return false;
				}
				if (!(plan.getStatus().getCode().equals("completed") || plan.getStatus().getCode().equals("canceled"))){
					Move move2 = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(
							plan.getClass().getSimpleName().toLowerCase(),
							plan.getStatus().getId(),
							statusRepository.findByStatusCode("completed").getId()
					);
					plan.setDateEnd(new Date());
					plan.setStatus(move2.getToStatus());
					TrainingEntity trainingEntity2 = trainingSaver.getTrainingEntityOrNew(planRepository, plan);
					trainingEntity2.setStatus(move2.getToStatus());
				}
				break;
			default:
				throw new Exception("Не определён тип дочернего мероприятия");
		}

		return true;
	}

	public boolean blockIsFinished(Block block, TrainingSaver trainingSaver) throws Exception {
		switch (block.getParentClass()){
			case "course":
				Set<CourseResponse> courseResponseSet = courseResponseRepository.findCourseResponseByBlockId(block.getId());
				for (CourseResponse courseResponse : courseResponseSet){
					if (!trainingIsFinished(courseResponse, trainingSaver)){
						return false;
					}
				}
				break;
			case "plan":
				Set<PlanResponse> planResponseSet = planResponseRepository.findPlanResponseByBlockId(block.getId());
				for (PlanResponse planResponse : planResponseSet){
					if (!trainingIsFinished(planResponse, trainingSaver)){
						return false;
					}
				}
				break;
			default:
				throw new Exception("Не определён тип родительского мероприятия");
		}
		return true;
	}

	public void activateTrainingResponse(Object o) throws Exception {
		switch (o.getClass().getSimpleName().toLowerCase()){
			case "courseresponse":
				CourseResponse courseResponse = (CourseResponse)o;
				switch (courseResponse.getResponseClass()){
					case "lesson":
						formLessonRestController.toStatus(courseResponse.getResponseId(), "in_progress");
						break;
					case "test":
						formTestRestController.toStatus(courseResponse.getResponseId(), "in_progress");
						break;
					default:
						throw new Exception("Не определён тип дочернего мероприятия");
				}
				break;
			case "planresponse":
				PlanResponse planResponse = (PlanResponse) o;
				switch (planResponse.getResponseClass()){
					case "lesson":
						formLessonRestController.toStatus(planResponse.getResponseId(), "in_progress");
						break;
					case "test":
						formTestRestController.toStatus(planResponse.getResponseId(), "in_progress");
						break;
					case "course":
						formCourseRestController.toStatus(planResponse.getResponseId(), "in_progress");
						break;
					default:
						throw new Exception("Не определён тип дочернего мероприятия");
				}
				break;
			default:
				throw new Exception("Не определён тип дочернего мероприятия");
		}
	}

	private boolean trainingIsFinished(Object response, TrainingSaver trainingSaver) throws Exception {
		switch (response.getClass().getSimpleName().toLowerCase()){
			case "courseresponse":
				CourseResponse courseResponse = (CourseResponse)response;
				switch (courseResponse.getResponseClass().toLowerCase()){
					case "lesson":
						Lesson lesson = lessonRepository.findByLessonId(courseResponse.getResponseId());
						TrainingEntity trainingEntity = trainingSaver.getTrainingEntity(lessonRepository, lesson);
						if (trainingEntity != null){
							lesson = (Lesson) trainingEntity.getObject();
						}
						return (lesson.getStatus().getCode().equals("completed") || lesson.getStatus().getCode().equals("canceled"));
					case "test":
						Test test = testRepository.findByTestId(courseResponse.getResponseId());
						TrainingEntity trainingEntity1 = trainingSaver.getTrainingEntity(testRepository, test);
						if (trainingEntity1 != null){
							test = (Test) trainingEntity1.getObject();
						}
						return (test.getStatus().getCode().equals("completed") || test.getStatus().getCode().equals("canceled") || test.getStatus().getCode().equals("expired"));
					default:
						throw new Exception("Не определён тип мероприятия");
				}
			case "planresponse":
				PlanResponse planResponse = (PlanResponse)response;
				switch (planResponse.getResponseClass().toLowerCase()){
					case "lesson":
						Lesson lesson = lessonRepository.findByLessonId(planResponse.getResponseId());
						TrainingEntity trainingEntity = trainingSaver.getTrainingEntity(lessonRepository, lesson);
						if (trainingEntity != null){
							lesson = (Lesson) trainingEntity.getObject();
						}
						return (lesson.getStatus().getCode().equals("completed") || lesson.getStatus().getCode().equals("canceled"));
					case "test":
						Test test = testRepository.findByTestId(planResponse.getResponseId());
						TrainingEntity trainingEntity1 = trainingSaver.getTrainingEntity(testRepository, test);
						if (trainingEntity1 != null){
							test = (Test) trainingEntity1.getObject();
						}
						return (test.getStatus().getCode().equals("completed") || test.getStatus().getCode().equals("canceled") || test.getStatus().getCode().equals("expired"));
					case "course":
						Course course = courseRepository.findByCourseId(planResponse.getResponseId());
						TrainingEntity trainingEntity2 = trainingSaver.getTrainingEntity(courseRepository, course);
						if (trainingEntity2 != null){
							course = (Course) trainingEntity2.getObject();
						}
						return (course.getStatus().getCode().equals("completed") || course.getStatus().getCode().equals("canceled"));
					default:
						throw new Exception("Не определён тип мероприятия");
				}
			case "course":
				Course course = (Course) response;
				Set<Block> blockSet = blockRepository.findBlockByCourseId(course.getId());
				if (blockSet.size() == 0){
					throw new Exception("Не определены дочерние блоки");
				}
				for (Block block : blockSet){
					if (!blockIsFinished(block, trainingSaver)){
						startBlock(block, trainingSaver);
						return false;
					}
				}
				return true;
			case "plan":
				Plan plan = (Plan) response;
				Set<Block> blockSet1 = blockRepository.findBlockByPlanId(plan.getId());
				if (blockSet1.size() == 0){
					throw new Exception("Не определены дочерние блоки");
				}
				for (Block block : blockSet1){
					if (!blockIsFinished(block, trainingSaver)){
						startBlock(block, trainingSaver);
						return false;
					}
				}
				return true;
			default:
				throw new Exception("Не определён тип дочернего мероприятия");
		}
	}

	private Status getTrainingStatus(Object response, TrainingSaver trainingSaver) throws Exception {
		switch (response.getClass().getSimpleName().toLowerCase()){
			case "courseresponse":
				CourseResponse courseResponse = (CourseResponse) response;
				switch (courseResponse.getResponseClass()){
					case "lesson":
						Lesson lesson = lessonRepository.findByLessonId(courseResponse.getResponseId());
						TrainingEntity trainingEntity = trainingSaver.getTrainingEntity(lessonRepository, lesson);
						if (trainingEntity != null){
							lesson = (Lesson) trainingEntity.getObject();
						}
						return lesson.getStatus();
					case "test":
						Test test = testRepository.findByTestId(courseResponse.getResponseId());
						TrainingEntity trainingEntity1 = trainingSaver.getTrainingEntity(testRepository, test);
						if (trainingEntity1 != null){
							test = (Test) trainingEntity1.getObject();
						}
						return test.getStatus();
					default:
						throw new Exception("Не определён тип дочернего мероприятия");
				}
			case "planresponse":
				PlanResponse planResponse = (PlanResponse) response;
				switch (planResponse.getResponseClass()){
					case "lesson":
						Lesson lesson = lessonRepository.findByLessonId(planResponse.getResponseId());
						TrainingEntity trainingEntity = trainingSaver.getTrainingEntity(lessonRepository, lesson);
						if (trainingEntity != null){
							lesson = (Lesson) trainingEntity.getObject();
						}
						return lesson.getStatus();
					case "test":
						Test test = testRepository.findByTestId(planResponse.getResponseId());
						TrainingEntity trainingEntity1 = trainingSaver.getTrainingEntity(testRepository, test);
						if (trainingEntity1 != null){
							test = (Test) trainingEntity1.getObject();
						}
						return test.getStatus();
					case "course":
						Course course = courseRepository.findByCourseId(planResponse.getResponseId());
						TrainingEntity trainingEntity2 = trainingSaver.getTrainingEntity(courseRepository, course);
						if (trainingEntity2 != null){
							course = (Course) trainingEntity2.getObject();
						}
						return course.getStatus();
					default:
						throw new Exception("Не определён тип дочернего мероприятия");
				}
			default:
				throw new Exception("Не определён тип дочернего мероприятия");
		}
	}

	private void startBlock(Block block, TrainingSaver trainingSaver) throws Exception {
		switch (block.getParentClass()){
			case "course":
				Set<CourseResponse> courseResponseSet = courseResponseRepository.findCourseResponseByBlockId(block.getId());
				for (CourseResponse courseResponse : courseResponseSet){
					Status trainingStatus = getTrainingStatus(courseResponse, trainingSaver);
					if (!trainingStatus.getCode().equals("draft") && !trainingStatus.getCode().equals("canceled") && !trainingStatus.getCode().equals("completed") && !trainingStatus.getCode().equals("expired")){
						break;
					}
					if (trainingStatus.getCode().equals("draft")){
						activateTrainingResponse(courseResponse);
						if (block.getType().equals(Constant.BLOCK_TYPE_SERIAL)){
							break;
						}
					}
				}
				break;
			case "plan":
				Set<PlanResponse> planResponseSet = planResponseRepository.findPlanResponseByBlockId(block.getId());
				for (PlanResponse planResponse : planResponseSet){
					Status trainingStatus = getTrainingStatus(planResponse, trainingSaver);
					if (!trainingStatus.getCode().equals("draft") && !trainingStatus.getCode().equals("canceled") && !trainingStatus.getCode().equals("completed") && !trainingStatus.getCode().equals("expired")){
						break;
					}
					if (trainingStatus.getCode().equals("draft")){
						activateTrainingResponse(planResponse);
						if (block.getType().equals(Constant.BLOCK_TYPE_SERIAL)){
							break;
						}
					}
				}
				break;
			default:
				throw new Exception("Не определён тип родительского мероприятия");
		}
	}

	public void startFirstBlock(Object o) throws Exception {
		switch (o.getClass().getSimpleName().toLowerCase()){
			case "course":
				Course course = (Course)o;
				Block firstBlock = blockRepository.findBlockByCourseId(course.getId()).iterator().next();
				startBlock(firstBlock, new TrainingSaver());
				break;
			case "plan":
				Plan plan = (Plan) o;
				Block firstBlock1 = blockRepository.findBlockByPlanId(plan.getId()).iterator().next();
				startBlock(firstBlock1, new TrainingSaver());
				break;
			default:
				throw new Exception("Не определён тип родительского мероприятия");
		}
	}

	public void deleteTrainingChilds(Object parentObject) throws Exception {
		TrainingDeleter trainingDeleter = new TrainingDeleter();
		getDeletionTrainingChildrenRecursive(parentObject, trainingDeleter);
		trainingDeleter.deleteAll();
	}

	private boolean getDeletionTrainingChildrenRecursive(Object parentObject, TrainingDeleter trainingDeleter) throws Exception {
		switch (parentObject.getClass().getSimpleName().toLowerCase()){
			case "course" :
				Course course = (Course) parentObject;
				if (!accessUtils.canEditCard(course) || !course.getStatus().getCode().equals("draft")){
					return false;
				}
				for (Block block : blockRepository.findBlockByCourseId(course.getId())){
					if (!getDeletionTrainingChildrenRecursive(block, trainingDeleter)){
						return false;
					}
				}
				/*
				for (CourseResponse courseResponse : courseResponseRepository.findByCourseId(course.getId())){
					if (!getDeletionTrainingChildrenRecursive(courseResponse, trainingDeleter)){
						return false;
					}
				}
				 */
				trainingDeleter.getTrainingEntityOrNew(courseRepository, course);
				break;
			case "plan" :
				Plan plan = (Plan) parentObject;
				if (!accessUtils.canEditCard(plan) || !plan.getStatus().getCode().equals("draft")){
					return false;
				}
				for (Block block : blockRepository.findBlockByPlanId(plan.getId())){
					if (!getDeletionTrainingChildrenRecursive(block, trainingDeleter)){
						return false;
					}
				}
				/*
				for (PlanResponse planResponse : planResponseRepository.findByPlanId(plan.getId())){
					if (!getDeletionTrainingChildrenRecursive(planResponse, trainingDeleter)){
						return false;
					}
				}
				 */
				trainingDeleter.getTrainingEntityOrNew(planRepository, plan);
				break;
			case "lesson" :
				Lesson lesson2 = (Lesson)parentObject;
				if (!accessUtils.canEditCard(lesson2) || !lesson2.getStatus().getCode().equals("draft")){
					return false;
				}
				for (LessonPerson lessonPerson : lessonPersonRepository.findByLessonId(lesson2.getId())){
					if (!getDeletionTrainingChildrenRecursive(lessonPerson, trainingDeleter)){
						return false;
					}
				}
				trainingDeleter.getTrainingEntityOrNew(lessonRepository, lesson2);
				break;
			case "test" :
				Test test2 = (Test)parentObject;
				if (!accessUtils.canEditCard(test2) || !test2.getStatus().getCode().equals("draft")){
					return false;
				}
				for (TestVariantPerson testVariantPerson : testVariantPersonRepository.findByTestId(test2.getId())){
					if (!getDeletionTrainingChildrenRecursive(testVariantPerson, trainingDeleter)){
						return false;
					}
				}
				trainingDeleter.getTrainingEntityOrNew(testRepository, test2);
				break;
			case "lessonperson" :
				LessonPerson lessonPerson = ((LessonPerson)parentObject);
				if (!accessUtils.canEditCard(lessonPerson) || !lessonPerson.getStatus().getCode().equals("draft")){
					return false;
				}
				trainingDeleter.getTrainingEntityOrNew(lessonPersonRepository, lessonPerson);
				break;
			case "testvariantperson" :
				TestVariantPerson testVariantPerson = ((TestVariantPerson)parentObject);
				if (!accessUtils.canEditCard(testVariantPerson) || !testVariantPerson.getStatus().getCode().equals("draft")){
					return false;
				}
				for (TestQuestion testQuestion : testQuestionRepository.findByTestVariantPersonId(testVariantPerson.getId())){
					if (!getDeletionTrainingChildrenRecursive(testQuestion, trainingDeleter)){
						return false;
					}
				}
				trainingDeleter.getTrainingEntityOrNew(testVariantPersonRepository, testVariantPerson);
				break;
			case "testquestion" :
				TestQuestion testQuestion = ((TestQuestion)parentObject);
				if (!accessUtils.canEditCard(testQuestion.getTestVariantPerson())){
					return false;
				}
				/*
				for (TestAnswer testAnswer : testAnswerRepository.findByTestVariantPersonIdAndTestQuestionNumber(testQuestion.getTestVariantPerson().getId(), testQuestion.getNumber())){
					if (!getDeletionTrainingChildrenRecursive(testAnswer, trainingDeleter)){
						return false;
					}
				}
				 */
				trainingDeleter.getTrainingEntityOrNew(testQuestionRepository, testQuestion);
				break;
			case "courseresponse" :
				CourseResponse courseResponse = ((CourseResponse)parentObject);
				if (!accessUtils.canEditCard(courseResponse)){
					return false;
				}
				switch (courseResponse.getResponseClass()){
					case "lesson":
						Lesson lesson = lessonRepository.findByLessonId(courseResponse.getResponseId());
						if (!getDeletionTrainingChildrenRecursive(lesson, trainingDeleter)){
							return false;
						}
						break;
					case "test":
						Test test = testRepository.findByTestId(courseResponse.getResponseId());
						if (!getDeletionTrainingChildrenRecursive(test, trainingDeleter)){
							return false;
						}
						break;
				}
				trainingDeleter.getTrainingEntityOrNew(courseResponseRepository, courseResponse);
				break;
			case "planresponse" :
				PlanResponse planResponse1 = ((PlanResponse)parentObject);
				if (!accessUtils.canEditCard(planResponse1)){
					return false;
				}
				switch (planResponse1.getResponseClass()){
					case "lesson":
						Lesson lesson1 = lessonRepository.findByLessonId(planResponse1.getResponseId());
						if (!getDeletionTrainingChildrenRecursive(lesson1, trainingDeleter)){
							return false;
						}
						break;
					case "test":
						Test test1 = testRepository.findByTestId(planResponse1.getResponseId());
						if (!getDeletionTrainingChildrenRecursive(test1, trainingDeleter)){
							return false;
						}
						break;
					case "course":
						Course course1 = courseRepository.findByCourseId(planResponse1.getResponseId());
						if (!getDeletionTrainingChildrenRecursive(course1, trainingDeleter)){
							return false;
						}
						break;
				}
				trainingDeleter.getTrainingEntityOrNew(planResponseRepository, planResponse1);
				break;

			case "templatecourse" :
				TemplateCourse templateCourse = (TemplateCourse) parentObject;
				if (!accessUtils.canEditCard(templateCourse) || !templateCourse.getStatus().getCode().equals("draft")){
					return false;
				}
				for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByTemplateCourseId(templateCourse.getId())){
					if (!getDeletionTrainingChildrenRecursive(templateCourseTemplateResponse, trainingDeleter)){
						return false;
					}
				}
				trainingDeleter.getTrainingEntityOrNew(templateCourseRepository, templateCourse);
				break;
			case "templateplan" :
				TemplatePlan templatePlan = (TemplatePlan) parentObject;
				if (!accessUtils.canEditCard(templatePlan) || !templatePlan.getStatus().getCode().equals("draft")){
					return false;
				}
				for (TemplatePlanTemplateResponse templatePlanTemplateResponse : templatePlanTemplateResponseRepository.findTemplatePlanTemplateResponseByTemplatePlanId(templatePlan.getId())){
					if (!getDeletionTrainingChildrenRecursive(templatePlanTemplateResponse, trainingDeleter)){
						return false;
					}
				}
				trainingDeleter.getTrainingEntityOrNew(templatePlanRepository, templatePlan);
				break;
			case "templatelesson" :
				TemplateLesson templateLesson2 = (TemplateLesson)parentObject;
				if (!accessUtils.canEditCard(templateLesson2) || !templateLesson2.getStatus().getCode().equals("draft")){
					return false;
				}
				trainingDeleter.getTrainingEntityOrNew(templateLessonRepository, templateLesson2);
				break;
			case "templatetest" :
				TemplateTest templateTest2 = (TemplateTest)parentObject;
				if (!accessUtils.canEditCard(templateTest2) || !templateTest2.getStatus().getCode().equals("draft")){
					return false;
				}
				for (TemplateTestVariant templateTestVariant : templateTestVariantRepository.findByTemplateTestId(templateTest2.getId())){
					if (!getDeletionTrainingChildrenRecursive(templateTestVariant, trainingDeleter)){
						return false;
					}
				}
				trainingDeleter.getTrainingEntityOrNew(templateTestRepository, templateTest2);
				break;
			case "templatecoursetemplateresponse" :
				TemplateCourseTemplateResponse templateCourseTemplateResponse = ((TemplateCourseTemplateResponse)parentObject);
				if (!accessUtils.canEditCard(templateCourseTemplateResponse)){
					return false;
				}
				/*
				switch (templateCourseTemplateResponse.getTemplateResponseClass()){
					case "templatelesson":
						TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(templateCourseTemplateResponse.getTemplateResponseId());
						break;
					case "templatetest":
						TemplateTest templateTest = templateTestRepository.findByTemplateTestId(templateCourseTemplateResponse.getTemplateResponseId());
						break;
				}
				 */
				trainingDeleter.getTrainingEntityOrNew(templateCourseTemplateResponseRepository, templateCourseTemplateResponse);
				break;
			case "templateplantemplateresponse" :
				TemplatePlanTemplateResponse templatePlanTemplateResponse1 = ((TemplatePlanTemplateResponse)parentObject);
				if (!accessUtils.canEditCard(templatePlanTemplateResponse1)){
					return false;
				}
				/*
				switch (templatePlanTemplateResponse1.getTemplateResponseClass()){
					case "templatelesson":
						TemplateLesson templateLesson1 = templateLessonRepository.findByTemplateLessonId(templatePlanTemplateResponse1.getTemplateResponseId());
						break;
					case "templatetest":
						TemplateTest templateTest1 = templateTestRepository.findByTemplateTestId(templatePlanTemplateResponse1.getTemplateResponseId());
						break;
					case "templatecourse":
						TemplateCourse templateCourse1 = templateCourseRepository.findByTemplateCourseId(templatePlanTemplateResponse1.getTemplateResponseId());
						break;
				}
				 */
				trainingDeleter.getTrainingEntityOrNew(templatePlanTemplateResponseRepository, templatePlanTemplateResponse1);
				break;
			case "templatetestvariant":
				TemplateTestVariant templateTestVariant = ((TemplateTestVariant)parentObject);
				if (!accessUtils.canEditCard(templateTestVariant)){
					return false;
				}
				for (TemplateTestQuestion templateTestQuestion : templateTestQuestionRepository.findByTemplateTestVariantId(templateTestVariant.getId())){
					if (!getDeletionTrainingChildrenRecursive(templateTestQuestion, trainingDeleter)){
						return false;
					}
				}
				trainingDeleter.getTrainingEntityOrNew(templateTestVariantRepository, templateTestVariant);
				break;
			case "templatetestquestion":
				TemplateTestQuestion templateTestQuestion = ((TemplateTestQuestion)parentObject);
				if (!accessUtils.canEditCard(templateTestQuestion)){
					return false;
				}
				for (TemplateTestAnswer templateTestAnswer : templateTestAnswerRepository.findByTemplateTestQuestionId(templateTestQuestion.getId())){
					if (!getDeletionTrainingChildrenRecursive(templateTestAnswer, trainingDeleter)){
						return false;
					}
				}
				trainingDeleter.getTrainingEntityOrNew(templateTestQuestionRepository, templateTestQuestion);
				break;
			case "templatetestanswer":
				TemplateTestAnswer templateTestAnswer = ((TemplateTestAnswer)parentObject);
				if (!accessUtils.canEditCard(templateTestAnswer)){
					return false;
				}
				trainingDeleter.getTrainingEntityOrNew(templateTestAnswerRepository, templateTestAnswer);
				break;
			case "category":
				Category category = ((Category)parentObject);
				if (!accessUtils.canEditCard(category)){
					return false;
				}
				trainingDeleter.getTrainingEntityOrNew(categoryRepository, category);
				break;
			case "block":
				Block block = ((Block)parentObject);
				switch (block.getParentClass()){
					case "course":
						for (CourseResponse courseResponse2 : courseResponseRepository.findByCourseId(block.getParentId())){
							if (!getDeletionTrainingChildrenRecursive(courseResponse2, trainingDeleter)){
								return false;
							}
						}
						break;
					case "plan":
						for (PlanResponse planResponse2 : planResponseRepository.findByPlanId(block.getParentId())){
							if (!getDeletionTrainingChildrenRecursive(planResponse2, trainingDeleter)){
								return false;
							}
						}
						break;
					default:
						throw new Exception("Не определён тип родительского мероприятия");
				}
				trainingDeleter.getTrainingEntityOrNew(blockRepository, block);
				break;
			default:
				throw new Exception("Не определён тип дочернего мероприятия");
		}

		return true;
	}

}
