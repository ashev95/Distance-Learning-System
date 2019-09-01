package com.dls.base.utils;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

import static com.dls.base.utils.Constant.*;

public class AccessUtils {

	private final PersonRepository personRepository;
	private final LessonRepository lessonRepository;
	private final TestRepository testRepository;
	private final  TemplateLessonRepository templateLessonRepository;
	private final  TemplateTestRepository templateTestRepository;
	private final  TemplateCourseRepository templateCourseRepository;

	@Autowired
	public AccessUtils(PersonRepository personRepository, LessonRepository lessonRepository, TestRepository testRepository, TemplateLessonRepository templateLessonRepository, TemplateTestRepository templateTestRepository, TemplateCourseRepository templateCourseRepository){
		this.personRepository = personRepository;
		this.lessonRepository = lessonRepository;
		this.testRepository = testRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.templateTestRepository = templateTestRepository;
		this.templateCourseRepository = templateCourseRepository;
	}

	public Person getCurrentPerson(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null){
			return personRepository.findByRoleCode(ROLE_ADMINISTRATOR).iterator().next();
		}
		return personRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
	}

	public boolean hasAtLeastRole(Collection <String> roles){
		Person currentPerson = this.getCurrentPerson();
		if (roles != null){
			for (String role : roles){
				for (Role role1 : currentPerson.getRoles()){
					if (role.equalsIgnoreCase(role1.getCode())){
						return true;
					}
				}
			}
		}else{
			return true;
		}
		return false;
	}

	public boolean canEditCard(Object o) throws Exception {
		switch (o.getClass().getSimpleName().toLowerCase()){
			case "course":
				Course course = (Course) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						course.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "courseresponse":
				CourseResponse courseResponse = (CourseResponse) o;
				switch (courseResponse.getResponseClass()){
					case "lesson":
						Lesson lesson = (lessonRepository.findByLessonId(courseResponse.getResponseId()));
						return (this.canEditCard(lesson));
					case "test":
						Test test = (testRepository.findByTestId(courseResponse.getResponseId()));
						return (this.canEditCard(test));
					default:
						throw new Exception("Не определён тип дочернего мероприятия");
				}
			case "group":
				Group group = (Group) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						group.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "groupperson":
				GroupPerson groupPerson = (GroupPerson) o;
				return (this.canEditCard(groupPerson.getGroup()));
			case "lesson":
				Lesson lesson = (Lesson) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						lesson.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "lessonperson":
				LessonPerson lessonPerson = (LessonPerson) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						lessonPerson.getLesson().getCurator().getId().equals(this.getCurrentPerson().getId())
						|| lessonPerson.getPerson().getId().equals(this.getCurrentPerson().getId()));
			case "person":
				Person person = (Person) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()));
			case "plan":
				Plan plan = (Plan) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						plan.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "planresponse":
				PlanResponse planResponse = (PlanResponse) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						planResponse.getPlan().getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "templatecourse":
				TemplateCourse templateCourse = (TemplateCourse) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.getArrayList()));
			case "templatecoursetemplateresponse":
				TemplateCourseTemplateResponse templateCourseTemplateResponse = (TemplateCourseTemplateResponse) o;
				switch (templateCourseTemplateResponse.getTemplateResponseClass()){
					case "templatelesson":
						TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(templateCourseTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateLesson));
					case "templatetest":
						TemplateTest templateTest = templateTestRepository.findByTemplateTestId(templateCourseTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateTest));
					default:
						throw new Exception("Не определён тип мероприятия");
				}
			case "templatelesson":
				TemplateLesson templateLesson = (TemplateLesson) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.getArrayList()));
			case "templateplan":
				TemplatePlan templatePlan = (TemplatePlan) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.getArrayList()));
			case "templateplantemplateresponse":
				TemplatePlanTemplateResponse templatePlanTemplateResponse = (TemplatePlanTemplateResponse) o;
				switch (templatePlanTemplateResponse.getTemplateResponseClass()){
					case "templatelesson":
						TemplateLesson templateLesson1 = templateLessonRepository.findByTemplateLessonId(templatePlanTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateLesson1));
					case "templatetest":
						TemplateTest templateTest1 = templateTestRepository.findByTemplateTestId(templatePlanTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateTest1));
					case "templatecourse":
						TemplateCourse templateCourse1 = templateCourseRepository.findByTemplateCourseId(templatePlanTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateCourse1));
					default:
						throw new Exception("Не определён тип мероприятия");
				}
			case "templatetest":
				TemplateTest templateTest = (TemplateTest) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.getArrayList()));
			case "templatetestanswer":
				TemplateTestAnswer templateTestAnswer = (TemplateTestAnswer) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.getArrayList()));
			case "templatetestquestion":
				TemplateTestQuestion templateTestQuestion = (TemplateTestQuestion) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.getArrayList()));
			case "templatetestvariant":
				TemplateTestVariant templateTestVariant = (TemplateTestVariant) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.getArrayList()));
			case "test":
				Test test = (Test) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						test.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "testanswer":
				TestAnswer testAnswer = (TestAnswer) o;
				return (this.canEditCard(testAnswer.getTestQuestion().getTestVariantPerson()));
			case "testvariantperson":
				TestVariantPerson testVariantPerson = (TestVariantPerson) o;
				return (this.canEditCard(testVariantPerson.getTest())
						|| testVariantPerson.getPerson().getId().equals(this.getCurrentPerson().getId()));
			case "category":
				Category category = (Category) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()));
			default:
				throw new Exception("Неизвестный тип карточки:" + o.getClass().getSimpleName().toLowerCase());
		}
	}

	public boolean canReadCard(Object o) throws Exception {
		switch (o.getClass().getSimpleName().toLowerCase()){
			case "course":
				Course course = (Course) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						course.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "courseresponse":
				CourseResponse courseResponse = (CourseResponse) o;
				switch (courseResponse.getResponseClass()){
					case "lesson":
						Lesson lesson = (lessonRepository.findByLessonId(courseResponse.getResponseId()));
						return (this.canEditCard(lesson));
					case "test":
						Test test = (testRepository.findByTestId(courseResponse.getResponseId()));
						return (this.canEditCard(test));
					default:
						throw new Exception("Не определён тип дочернего мероприятия");
				}
			case "group":
				Group group = (Group) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_CURATOR)
						.getArrayList()) ||
						group.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "groupperson":
				GroupPerson groupPerson = (GroupPerson) o;
				return (this.canEditCard(groupPerson.getGroup()));
			case "lesson":
				Lesson lesson = (Lesson) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						lesson.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "lessonperson":
				LessonPerson lessonPerson = (LessonPerson) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						lessonPerson.getLesson().getCurator().getId().equals(this.getCurrentPerson().getId())
						|| lessonPerson.getPerson().getId().equals(this.getCurrentPerson().getId()));
			case "person":
				Person person = (Person) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.add(ROLE_CURATOR)
						.add(ROLE_STUDENT)
						.getArrayList()));
			case "plan":
				Plan plan = (Plan) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						plan.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "planresponse":
				PlanResponse planResponse = (PlanResponse) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						planResponse.getPlan().getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "templatecourse":
				TemplateCourse templateCourse = (TemplateCourse) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.add(ROLE_CURATOR)
						.getArrayList()));
			case "templatecoursetemplateresponse":
				TemplateCourseTemplateResponse templateCourseTemplateResponse = (TemplateCourseTemplateResponse) o;
				switch (templateCourseTemplateResponse.getTemplateResponseClass()){
					case "templatelesson":
						TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(templateCourseTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateLesson));
					case "templatetest":
						TemplateTest templateTest = templateTestRepository.findByTemplateTestId(templateCourseTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateTest));
					default:
						throw new Exception("Не определён тип мероприятия");
				}
			case "templatelesson":
				TemplateLesson templateLesson = (TemplateLesson) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.add(ROLE_CURATOR)
						.getArrayList()));
			case "templateplan":
				TemplatePlan templatePlan = (TemplatePlan) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.add(ROLE_CURATOR)
						.getArrayList()));
			case "templateplantemplateresponse":
				TemplatePlanTemplateResponse templatePlanTemplateResponse = (TemplatePlanTemplateResponse) o;
				switch (templatePlanTemplateResponse.getTemplateResponseClass()){
					case "templatelesson":
						TemplateLesson templateLesson1 = templateLessonRepository.findByTemplateLessonId(templatePlanTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateLesson1));
					case "templatetest":
						TemplateTest templateTest1 = templateTestRepository.findByTemplateTestId(templatePlanTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateTest1));
					case "templatecourse":
						TemplateCourse templateCourse1 = templateCourseRepository.findByTemplateCourseId(templatePlanTemplateResponse.getTemplateResponseId());
						return (this.canEditCard(templateCourse1));
					default:
						throw new Exception("Не определён тип мероприятия");
				}
			case "templatetest":
				TemplateTest templateTest = (TemplateTest) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.add(ROLE_CURATOR)
						.getArrayList()));
			case "templatetestanswer":
				TemplateTestAnswer templateTestAnswer = (TemplateTestAnswer) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.add(ROLE_CURATOR)
						.getArrayList()));
			case "templatetestquestion":
				TemplateTestQuestion templateTestQuestion = (TemplateTestQuestion) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.add(ROLE_CURATOR)
						.getArrayList()));
			case "templatetestvariant":
				TemplateTestVariant templateTestVariant = (TemplateTestVariant) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.add(ROLE_CURATOR)
						.getArrayList()));
			case "test":
				Test test = (Test) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.getArrayList()) ||
						test.getCurator().getId().equals(this.getCurrentPerson().getId()));
			case "testanswer":
				TestAnswer testAnswer = (TestAnswer) o;
				return (this.canEditCard(testAnswer.getTestQuestion().getTestVariantPerson()));
			case "testvariantperson":
				TestVariantPerson testVariantPerson = (TestVariantPerson) o;
				return (this.canEditCard(testVariantPerson.getTest())
				|| testVariantPerson.getPerson().getId().equals(this.getCurrentPerson().getId()));
			case "category":
				Category category = (Category) o;
				return (this.hasAtLeastRole(new ArrayListBuilder()
						.add(ROLE_ADMINISTRATOR)
						.add(ROLE_ADMINISTRATOR_OF_EDUCATIONAL_PROCESS)
						.getArrayList()));
			default:
				throw new Exception("Неизвестный тип карточки");
		}
	}

}
