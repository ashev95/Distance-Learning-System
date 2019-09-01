package com.dls.base.pattern.observer.training;

import com.dls.base.entity.Course;
import com.dls.base.entity.Lesson;
import com.dls.base.entity.Plan;
import com.dls.base.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public class TrainingSaver {

    List<TrainingEntity> trainingEntityList = new ArrayList<TrainingEntity>();

    public TrainingEntity getTrainingEntityOrNew(JpaRepository jpaRepository, Object object) throws Exception{
        TrainingEntity trainingEntity = getTrainingEntity(jpaRepository, object) ;
        if (trainingEntity == null){
            trainingEntity = new TrainingEntity(jpaRepository, object);
            add(trainingEntity);
        }
        return trainingEntity;
    }

    public TrainingEntity getTrainingEntity(JpaRepository jpaRepository, Object object) throws Exception {
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
                    case "plan":
                        Plan plan = (Plan) trainingEntity.object;
                        Plan plan1 = (Plan) object;
                        if (plan.getId().equals(plan1.getId())){
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

    public void saveAll() {
        for (TrainingEntity trainingEntity : trainingEntityList){
            trainingEntity.save();
        }
    }
}
