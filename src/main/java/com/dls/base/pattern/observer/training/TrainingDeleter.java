package com.dls.base.pattern.observer.training;

import com.dls.base.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrainingDeleter {

    List<TrainingEntity> trainingEntityList = new ArrayList<TrainingEntity>();

    public TrainingEntity getTrainingEntityOrNew(JpaRepository jpaRepository, Object object) throws Exception{
        TrainingEntity trainingEntity = getTrainingEntity(jpaRepository, object) ;
        if (trainingEntity == null){
            trainingEntity = new TrainingEntity(jpaRepository, object);
            add(trainingEntity);
        }
        return trainingEntity;
    }

    private TrainingEntity getTrainingEntity(JpaRepository jpaRepository, Object object) throws Exception {
        for (TrainingEntity trainingEntity : trainingEntityList){
            if (trainingEntity.jpaRepository.getClass().getSimpleName().equalsIgnoreCase(jpaRepository.getClass().getSimpleName()) &&
                    trainingEntity.object.getClass().getSimpleName().equalsIgnoreCase(object.getClass().getSimpleName())){
                switch (trainingEntity.object.getClass().getSimpleName().toLowerCase()){
                    case "lesson":
                        Lesson lesson = (Lesson) trainingEntity.object;
                        Lesson lesson1 = (Lesson) object;
                        if (lesson.getId().equals(lesson1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "test":
                        Test test = (Test) trainingEntity.object;
                        Test test1 = (Test) object;
                        if (test.getId().equals(test1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "course":
                        Course course = (Course) trainingEntity.object;
                        Course course1 = (Course) object;
                        if (course.getId().equals(course1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "courseresponse":
                        CourseResponse courseResponse = (CourseResponse) trainingEntity.object;
                        CourseResponse courseResponse1 = (CourseResponse) object;
                        if (courseResponse.getId().equals(courseResponse1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "plan":
                        Plan plan = (Plan) trainingEntity.object;
                        Plan plan1 = (Plan) object;
                        if (plan.getId().equals(plan1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "planresponse":
                        PlanResponse planResponse = (PlanResponse) trainingEntity.object;
                        PlanResponse planResponse1 = (PlanResponse) object;
                        if (planResponse.getId().equals(planResponse1.getId())){
                            return trainingEntity;
                        }
                        break;

                    case "templatelesson":
                        TemplateLesson templateLesson = (TemplateLesson) trainingEntity.object;
                        TemplateLesson templateLesson1 = (TemplateLesson) object;
                        if (templateLesson.getId().equals(templateLesson1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "templatetest":
                        TemplateTest templateTest = (TemplateTest) trainingEntity.object;
                        TemplateTest templateTest1 = (TemplateTest) object;
                        if (templateTest.getId().equals(templateTest1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "templatecourse":
                        TemplateCourse templateCourse = (TemplateCourse) trainingEntity.object;
                        TemplateCourse templateCourse1 = (TemplateCourse) object;
                        if (templateCourse.getId().equals(templateCourse1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "templatecoursetemplateresponse":
                        TemplateCourseTemplateResponse templateCourseTemplateResponse = (TemplateCourseTemplateResponse) trainingEntity.object;
                        TemplateCourseTemplateResponse templateCourseTemplateResponse1 = (TemplateCourseTemplateResponse) object;
                        if (templateCourseTemplateResponse.getId().equals(templateCourseTemplateResponse1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "templateplan":
                        TemplatePlan templatePlan = (TemplatePlan) trainingEntity.object;
                        TemplatePlan templatePlan1 = (TemplatePlan) object;
                        if (templatePlan.getId().equals(templatePlan1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "templateplantemplateresponse":
                        TemplatePlanTemplateResponse templatePlanTemplateResponse = (TemplatePlanTemplateResponse) trainingEntity.object;
                        TemplatePlanTemplateResponse templatePlanTemplateResponse1 = (TemplatePlanTemplateResponse) object;
                        if (templatePlanTemplateResponse.getId().equals(templatePlanTemplateResponse1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "templatetestvariant":
                        TemplateTestVariant templateTestVariant = (TemplateTestVariant) trainingEntity.object;
                        TemplateTestVariant templateTestVariant1 = (TemplateTestVariant) object;
                        if (templateTestVariant.getId().equals(templateTestVariant1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "templatetestquestion":
                        TemplateTestQuestion templateTestQuestion = (TemplateTestQuestion) trainingEntity.object;
                        TemplateTestQuestion templateTestQuestion1 = (TemplateTestQuestion) object;
                        if (templateTestQuestion.getId().equals(templateTestQuestion1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "templatetestanswer":
                        TemplateTestAnswer templateTestAnswer = (TemplateTestAnswer) trainingEntity.object;
                        TemplateTestAnswer templateTestAnswer1 = (TemplateTestAnswer) object;
                        if (templateTestAnswer.getId().equals(templateTestAnswer1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "block":
                        Block block = (Block) trainingEntity.object;
                        Block block1 = (Block) object;
                        if (block.getId().equals(block1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "testvariantperson":
                        TestVariantPerson testVariantPerson = (TestVariantPerson) trainingEntity.object;
                        TestVariantPerson testVariantPerson1 = (TestVariantPerson) object;
                        if (testVariantPerson.getId().equals(testVariantPerson1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "testquestion":
                        TestQuestion testQuestion = (TestQuestion) trainingEntity.object;
                        TestQuestion testQuestion1 = (TestQuestion) object;
                        if (testQuestion.getId().equals(testQuestion1.getId())){
                            return trainingEntity;
                        }
                        break;
                    case "lessonperson":
                        LessonPerson lessonPerson = (LessonPerson) trainingEntity.object;
                        LessonPerson lessonPerson1 = (LessonPerson) object;
                        if (lessonPerson.getId().equals(lessonPerson1.getId())){
                            return trainingEntity;
                        }
                        break;
                    default:
                        throw new Exception("Не определён тим мероприятия");
                }
            }
        }
        return null;
    }

    public void add(TrainingEntity trainingEntity) {
        trainingEntityList.add(trainingEntity);
    }

    public void remove(TrainingEntity trainingEntity) {
        trainingEntityList.remove(trainingEntity);
    }

    public void deleteAll() {
        for (TrainingEntity trainingEntity : trainingEntityList){
            trainingEntity.delete();
        }
    }
}
