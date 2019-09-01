package com.dls.base.controller.form;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.ArrayListBuilder;
import com.dls.base.utils.MoveUtils;
import com.dls.base.utils.Utils;
import com.dls.base.validator.ErrorMessage;
import com.dls.base.validator.LessonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import static com.dls.base.utils.Constant.ROLE_ADMINISTRATOR;

@RestController
public class FormLessonRestController
{

	private final LessonRepository lessonRepository;
	private final PersonRepository personRepository;
	private final LifeCycleRepository lifeCycleRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;
	private final TemplateLessonRepository templateLessonRepository;
	private final MoveRepository moveRepository;
	private final StatusRepository statusRepository;
	private final GroupRepository groupRepository;
	private final GroupPersonRepository groupPersonRepository;
	private final LessonPersonRepository lessonPersonRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;
	
	@Autowired
	MoveUtils moveUtils;

	@Autowired
	LessonValidator lessonValidator;

	@Autowired
	private MessageSource messageSource;

	@Autowired
    FormLessonRestController(LessonRepository lessonRepository, PersonRepository personRepository, LifeCycleRepository lifeCycleRepository, TemplateLifeCycleRepository templateLifeCycleRepository, TemplateLessonRepository templateLessonRepository, MoveRepository moveRepository, StatusRepository statusRepository, GroupRepository groupRepository, GroupPersonRepository groupPersonRepository, LessonPersonRepository lessonPersonRepository) {
		this.lessonRepository = lessonRepository;
		this.personRepository = personRepository;
		this.lifeCycleRepository = lifeCycleRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.moveRepository = moveRepository;
		this.statusRepository = statusRepository;
		this.groupRepository = groupRepository;
		this.groupPersonRepository = groupPersonRepository;
		this.lessonPersonRepository = lessonPersonRepository;
	}

	@GetMapping(value = "form/lesson/templatelesson/{templateLessonId}/0", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getNewFormByIdentifier(@PathVariable Long templateLessonId) {
		Lesson lesson = new Lesson();
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(lesson.getClass().getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		lesson.setTemplateLesson(templateLessonRepository.findByTemplateLessonId(templateLessonId));
		lesson.setStatus(lifeCycle.getInitStatus());
		lesson.setCurator(accessUtils.getCurrentPerson());
		lesson.setAuthor(accessUtils.getCurrentPerson());
		FormTemplate formTemplate = getForm(lesson);
		return formTemplate;
	}

	@GetMapping(value = "form/lesson/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getFormByIdentifier(@PathVariable Long id) throws Exception {
		Lesson lesson = lessonRepository.findByLessonId(id);
		if (!accessUtils.canReadCard(lesson)){
			throw new Exception("Отсутствуют права на чтение карточки");
		}
		FormTemplate formTemplate = getForm(lesson);
		return formTemplate;
	}

	public FormTemplate getForm(Lesson lesson) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.attributes = utils.getFormAttributes(lesson);
		formTemplate.tabTitle = lesson.getName();
		formTemplate.template = lesson.getClass().getSimpleName().toLowerCase();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PutMapping(value = "form/lesson/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody Lesson lesson, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		lesson.setTemplateLesson(templateLessonRepository.findByTemplateLessonId(lesson.getTemplateLesson().getId()));
		lesson.setGroup(groupRepository.findByGroupId(lesson.getGroup().getId()));
		lesson.setStatus(statusRepository.findByStatusId(lesson.getStatus().getId()));
		lesson.setCurator(personRepository.findByPersonId(lesson.getCurator().getId()));
		lesson.setAuthor(personRepository.findByPersonId(lesson.getAuthor().getId()));
		if (!accessUtils.canEditCard(lesson)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!lesson.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + lesson.getClass().getSimpleName().toLowerCase() + ", id = " + lesson.getId() + "]");
		}
		lessonValidator.validate(lesson, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Lesson newLesson = new Lesson();
		newLesson.setName(lesson.getName());
		newLesson.setDescription(lesson.getDescription());
		newLesson.setTemplateLesson(lesson.getTemplateLesson());
		newLesson.setStatus(lesson.getStatus());
		newLesson.setGroup(lesson.getGroup());
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.getArrayList())) {
			newLesson.setCurator(lesson.getCurator());
		}else{
			newLesson.setCurator(accessUtils.getCurrentPerson());
		}
		newLesson.setAuthor(lesson.getAuthor());
		newLesson.setDateCreate(new Date());
		Lesson savedLesson = lessonRepository.save(newLesson);
		//
			createLessonPersonFull(savedLesson);
		//
		formTemplate = getFormByIdentifier(savedLesson.getId());
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

	public void reCreateLessonPersonFull(Lesson lesson){
		removeLessonPersonFull(lesson);
		createLessonPersonFull(lesson);
	}

	public void removeLessonPersonFull(Lesson lesson){
		for (LessonPerson lessonPerson : lessonPersonRepository.findByLessonId(lesson.getId())){
			lessonPersonRepository.delete(lessonPerson);
		}
	}

	public void createLessonPersonFull(Lesson lesson){
		TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(LessonPerson.class.getSimpleName().toLowerCase());
		LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
		for (GroupPerson groupPerson : groupPersonRepository.findByGroupId(lesson.getGroup().getId())){
			LessonPerson lessonPerson = new LessonPerson();
			lessonPerson.setLesson(lesson);
			lessonPerson.setPerson(groupPerson.getPerson());
			lessonPerson.setStatus(lifeCycle.getInitStatus());
			lessonPersonRepository.save(lessonPerson);
		}
	}



	@PostMapping(value = "form/lesson/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateForm(@PathVariable long id, @RequestBody Lesson lesson, BindingResult bindingResult) throws Exception {
		FormTemplate formTemplate = null;
		try{
		Lesson updateLesson = lessonRepository.findByLessonId(id);
		updateLesson.setName(lesson.getName());
		updateLesson.setDescription(lesson.getDescription());
		Person curator = personRepository.findByPersonId(lesson.getCurator().getId());
		updateLesson.setCurator(curator);
		Person author = personRepository.findByPersonId(lesson.getAuthor().getId());
		updateLesson.setAuthor(author);
		Status status = statusRepository.findByStatusId(lesson.getStatus().getId());
		updateLesson.setStatus(status);
		Group oldGroup = updateLesson.getGroup();
		Group group = groupRepository.findByGroupId(lesson.getGroup().getId());
		updateLesson.setGroup(group);
		lesson.setStatus(statusRepository.findByStatusId(lesson.getStatus().getId()));
		if (!accessUtils.canEditCard(lesson)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!lesson.getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + lesson.getClass().getSimpleName().toLowerCase() + ", id = " + lesson.getId() + "]");
		}
		lessonValidator.validate(updateLesson, bindingResult);
		if (bindingResult.hasErrors()){
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new ErrorMessage(messageSource.getMessage(bindingResult.getFieldError(), null)));
		}
		Lesson savedLesson = lessonRepository.save(updateLesson);
		//
		if (!oldGroup.getId().equals(savedLesson.getGroup().getId())){
			reCreateLessonPersonFull(savedLesson);
		}
		//
		formTemplate = getFormByIdentifier(savedLesson.getId());
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

	@PostMapping(value = "form/lesson/{id}/status/{statusCode}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity toStatus(@PathVariable long id, @PathVariable String statusCode) throws Exception {
		FormTemplate formTemplate = null;
		try{
		Lesson updateLesson = lessonRepository.findByLessonId(id);
		//if (!accessUtils.canEditCard(updateLesson)){
		//	throw new Exception("Отсутствуют права на создание/изменение карточки");
		//}
		Status toStatus = statusRepository.findByStatusCode(statusCode);
		Move move = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(updateLesson.getClass().getSimpleName().toLowerCase(), updateLesson.getStatus().getId(), toStatus.getId());
		if (move != null){
			updateLesson.setStatus(move.getToStatus());
			if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("in_progress")){
				utils.checkTemplate(updateLesson, true);
				updateLesson.setDateStart(new Date());
			}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("canceled")){
				updateLesson.setDateEnd(new Date());
			}else if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("canceled")){
				updateLesson.setDateStart(new Date());
				updateLesson.setDateEnd(new Date());
			}
			Lesson savedLesson = lessonRepository.save(updateLesson);
			TemplateLifeCycle templateLifeCycle = templateLifeCycleRepository.findByTemplateClass(LessonPerson.class.getSimpleName().toLowerCase());
			LifeCycle lifeCycle = templateLifeCycle.getLifeCycle();
			if (move.getFromStatus().getCode().equals("draft") && move.getToStatus().getCode().equals("in_progress")){
				Move move1 = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(LessonPerson.class.getSimpleName().toLowerCase(), statusRepository.findByStatusCode("draft").getId(), statusRepository.findByStatusCode("assigned").getId());
				if (groupPersonRepository.findByGroupId(updateLesson.getGroup().getId()).size() == 0){
					throw new Exception("Отсутствуют учащиеся");
				}
				for (LessonPerson lessonPerson : lessonPersonRepository.findByLessonId(savedLesson.getId())){
					if (lessonPerson.getStatus().getCode().equals("draft")){
						lessonPerson.setStatus(move1.getToStatus());
						lessonPersonRepository.save(lessonPerson);
					}
				}
			}else if (move.getFromStatus().getCode().equals("in_progress") && move.getToStatus().getCode().equals("canceled")){
				Move move1 = moveRepository.findByTemplateClassAndFromStatusIdAndToStatusId(LessonPerson.class.getSimpleName().toLowerCase(), statusRepository.findByStatusCode("assigned").getId(), statusRepository.findByStatusCode("canceled").getId());
				for (LessonPerson lessonPerson : lessonPersonRepository.findByLessonIdAndLessonPersonStatusCode(updateLesson.getId(), "assigned")){
					if (lessonPerson.getStatus().getCode().equals("assigned")){
						lessonPerson.setStatus(move1.getToStatus());
						lessonPerson.setDateEnd(new Date());
						lessonPersonRepository.save(lessonPerson);
					}
				}
			}
			if (move.getToStatus().getCode().equals("canceled") || move.getToStatus().getCode().equals("completed")){
				moveUtils.processTrainingParents(savedLesson);
			}
			formTemplate = getFormByIdentifier(savedLesson.getId());
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(formTemplate);
		}
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return null;
	}

}