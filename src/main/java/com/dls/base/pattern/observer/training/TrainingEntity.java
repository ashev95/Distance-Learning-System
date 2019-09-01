package com.dls.base.pattern.observer.training;

import com.dls.base.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public class TrainingEntity {

    JpaRepository jpaRepository = null;
    Object object = null;

    public TrainingEntity(JpaRepository jpaRepository, Object object){
        this.jpaRepository = jpaRepository;
        this.object = object;
    }

    public void setStatus(Status status) throws Exception {
        switch (object.getClass().getSimpleName().toLowerCase()){
            case "lesson":
                Lesson lesson = (Lesson) object;
                lesson.setStatus(status);
                break;
            case "test":
                Test test = (Test) object;
                test.setStatus(status);
                break;
            case "course":
                Course course = (Course) object;
                course.setStatus(status);
                break;
            case "plan":
                Plan plan = (Plan) object;
                plan.setStatus(status);
                break;
                default:
                    throw new Exception("Не определён тип мероприятия");
        }
    }

    public void save() {
        jpaRepository.save(object);
    }

    public void delete() {
        jpaRepository.delete(object);
    }

    public Object getObject() {
        return object;
    }
}
