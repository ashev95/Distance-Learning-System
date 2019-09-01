package com.dls.base.controller;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.service.SecurityService;
import com.dls.base.utils.Constant;
import com.dls.base.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

@Controller
public class MainController
{

	private final PersonRepository personRepository;
	private final RoleRepository roleRepository;
	private final SecurityService securityService;
	private final GroupRepository groupRepository;
	private final CategoryRepository categoryRepository;
	private final StatusRepository statusRepository;
	private final MoveRepository moveRepository;
	private final LifeCycleRepository lifeCycleRepository;
	private final TemplateLifeCycleRepository templateLifeCycleRepository;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	MainController(PersonRepository personRepository, RoleRepository roleRepository, SecurityService securityService, Utils utils, GroupRepository groupRepository, CategoryRepository categoryRepository, StatusRepository statusRepository, MoveRepository moveRepository, LifeCycleRepository lifeCycleRepository, TemplateLifeCycleRepository templateLifeCycleRepository) {
		this.personRepository = personRepository;
		this.roleRepository = roleRepository;
		this.securityService = securityService;
		this.utils = utils;
		this.groupRepository = groupRepository;
		this.categoryRepository = categoryRepository;
		this.statusRepository = statusRepository;
		this.moveRepository = moveRepository;
		this.lifeCycleRepository = lifeCycleRepository;
		this.templateLifeCycleRepository = templateLifeCycleRepository;
	}

	@Autowired
	private final Utils utils;

	@GetMapping(value = {"/","/login"})
	public String login()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			return "redirect:/dls";
		}
		return "login";
	}

	@GetMapping(value = "/logout")
	public String logout()
	{
		return "redirect:/login";
	}

	@GetMapping(value = "/dls/logout")
	public String logout1()
	{
		return "redirect:/logout";
	}

	@GetMapping(value = "/dls")
	public String main()
	{
		return "dls";
	}

	@GetMapping(value = "/expired")
	public String loginDuplicate()
	{
		return "expired";
	}

	@GetMapping(value = "/session_checking/{windowName}")
	public ResponseEntity sessionChecking(HttpServletRequest request, @PathVariable String windowName)
	{
		HttpSession session = request.getSession();
		if (!windowName.equals(session.getAttribute("windowName"))){
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.build();
	}

	@Value("#{appProperties['roles.admin.code']}")
	private String rolesAdminCode;

	@PostConstruct
	public void postConstruct() {

		//��������� �� ������
		if (utils.getAppProperty("properties.read").equals("true")) {
			Role role;
			Person person;
			//������ ����, ���� �� ���
			if (this.roleRepository.findAll().size() == 0){
				for (String key : utils.getRoles().keySet()){
					role = new Role();
					role.setName(utils.getRoles().get(key));
					role.setCode(key);
					this.roleRepository.save(role);
				}
			}
			//������ ��������������, ���� �����������
			if (this.personRepository.findByRoleCode(rolesAdminCode).size() == 0){
				person = new Person();
				person.setUsername(utils.getAppProperty("admin.username"));
				person.setPassword(new BCryptPasswordEncoder().encode(utils.getAppProperty("admin.password")));
				person.setEmail(utils.getAppProperty("admin.email"));
				person.setSurname(utils.getAppProperty("admin.surname"));
				person.setName(utils.getAppProperty("admin.name"));
				person.setMiddlename(utils.getAppProperty("admin.middlename"));
				person.setGender(Constant.GENDER_MALE);
				person.setAdditionally("");
				person = this.personRepository.save(person);

				//��������� ���� ��������������
				role = this.roleRepository.findByRoleCode(rolesAdminCode);
				Set<Role> adminRoles = new HashSet<>();
				adminRoles.add(role);
				person.setRoles(adminRoles);
				this.personRepository.save(person);

			}

			if (statusRepository.findAll().size() == 0){
				Status statusDraft = new Status();
				statusDraft.setName("Черновик");
				statusDraft.setCode("draft");
				statusDraft.setDescription("Черновик");
				statusRepository.save(statusDraft);

				Status statusInProgress = new Status();
				statusInProgress.setName("В процессе");
				statusInProgress.setCode("in_progress");
				statusInProgress.setDescription("В процессе");
				statusRepository.save(statusInProgress);

				Status statusCompleted = new Status();
				statusCompleted.setName("Завершён");
				statusCompleted.setCode("completed");
				statusCompleted.setDescription("Завершён");
				statusRepository.save(statusCompleted);

				Status statusAssigned = new Status();
				statusAssigned.setName("Назначен");
				statusAssigned.setCode("assigned");
				statusAssigned.setDescription("Назначен");
				statusRepository.save(statusAssigned);

				Status statusStudied = new Status();
				statusStudied.setName("Изучен");
				statusStudied.setCode("studied");
				statusStudied.setDescription("Изучен");
				statusRepository.save(statusStudied);

				Status statusCanceled = new Status();
				statusCanceled.setName("Отменён");
				statusCanceled.setCode("canceled");
				statusCanceled.setDescription("Отменён");
				statusRepository.save(statusCanceled);

				Status statusActive = new Status();
				statusActive.setName("Активен");
				statusActive.setCode("active");
				statusActive.setDescription("Активен");
				statusRepository.save(statusActive);

				Status statusDuplicate = new Status();
				statusDuplicate.setName("Дубликат");
				statusDuplicate.setCode("duplicate");
				statusDuplicate.setDescription("Дубликат");
				statusRepository.save(statusDuplicate);

				Status statusExpired = new Status();
				statusExpired.setName("Время прохождения истекло");
				statusExpired.setCode("expired");
				statusExpired.setDescription("Время прохождения истекло");
				statusRepository.save(statusExpired);

				if (moveRepository.findAll().size() == 0){
					LifeCycle lifeCycleLesson = new LifeCycle();
					lifeCycleLesson.setName("ЖЦ для урока");
					lifeCycleLesson.setDescription("ЖЦ для урока");
					lifeCycleLesson.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCycleLesson);

					Move move = new Move();
					move.setLifeCycle(lifeCycleLesson);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusInProgress);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleLesson);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCompleted);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleLesson);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCanceled);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCycleLesson = new TemplateLifeCycle();
					templateLifeCycleLesson.setTemplateClass(Lesson.class.getSimpleName().toLowerCase());
					templateLifeCycleLesson.setLifeCycle(lifeCycleLesson);
					templateLifeCycleRepository.save(templateLifeCycleLesson);

					//

					LifeCycle lifeCycleTrainingLesson = new LifeCycle();
					lifeCycleTrainingLesson.setName("ЖЦ для прохождения урока");
					lifeCycleTrainingLesson.setDescription("ЖЦ для прохождения урока");
					lifeCycleTrainingLesson.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCycleTrainingLesson);

					move = new Move();
					move.setLifeCycle(lifeCycleTrainingLesson);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusAssigned);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTrainingLesson);
					move.setFromStatus(statusAssigned);
					move.setToStatus(statusStudied);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTrainingLesson);
					move.setFromStatus(statusAssigned);
					move.setToStatus(statusCanceled);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCycleTrainingLesson = new TemplateLifeCycle();
					templateLifeCycleTrainingLesson.setTemplateClass(LessonPerson.class.getSimpleName().toLowerCase());
					templateLifeCycleTrainingLesson.setLifeCycle(lifeCycleTrainingLesson);
					templateLifeCycleRepository.save(templateLifeCycleTrainingLesson);

					//

					LifeCycle lifeCycleTest = new LifeCycle();
					lifeCycleTest.setName("ЖЦ для теста");
					lifeCycleTest.setDescription("ЖЦ для теста");
					lifeCycleTest.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCycleTest);

					move = new Move();
					move.setLifeCycle(lifeCycleTest);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusInProgress);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTest);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCompleted);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTest);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCanceled);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCycleTest = new TemplateLifeCycle();
					templateLifeCycleTest.setTemplateClass(Test.class.getSimpleName().toLowerCase());
					templateLifeCycleTest.setLifeCycle(lifeCycleTest);
					templateLifeCycleRepository.save(templateLifeCycleTest);

					//

					LifeCycle lifeCycleTrainingTest = new LifeCycle();
					lifeCycleTrainingTest.setName("ЖЦ для прохождения теста");
					lifeCycleTrainingTest.setDescription("ЖЦ для прохождения теста");
					lifeCycleTrainingTest.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCycleTrainingTest);

					move = new Move();
					move.setLifeCycle(lifeCycleTrainingTest);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusAssigned);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTrainingTest);
					move.setFromStatus(statusAssigned);
					move.setToStatus(statusInProgress);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTrainingTest);
					move.setFromStatus(statusAssigned);
					move.setToStatus(statusCanceled);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTrainingTest);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCompleted);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTrainingTest);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusExpired);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTrainingTest);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCanceled);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCycleTrainingTest = new TemplateLifeCycle();
					templateLifeCycleTrainingTest.setTemplateClass(TestVariantPerson.class.getSimpleName().toLowerCase());
					templateLifeCycleTrainingTest.setLifeCycle(lifeCycleTrainingTest);
					templateLifeCycleRepository.save(templateLifeCycleTrainingTest);

					//

					LifeCycle lifeCycleCourse = new LifeCycle();
					lifeCycleCourse.setName("ЖЦ для курса");
					lifeCycleCourse.setDescription("ЖЦ для курса");
					lifeCycleCourse.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCycleCourse);

					move = new Move();
					move.setLifeCycle(lifeCycleCourse);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusInProgress);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleCourse);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCompleted);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleCourse);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCanceled);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCycleCourse = new TemplateLifeCycle();
					templateLifeCycleCourse.setTemplateClass(Course.class.getSimpleName().toLowerCase());
					templateLifeCycleCourse.setLifeCycle(lifeCycleCourse);
					templateLifeCycleRepository.save(templateLifeCycleCourse);

					//

					LifeCycle lifeCyclePlan = new LifeCycle();
					lifeCyclePlan.setName("ЖЦ для плана");
					lifeCyclePlan.setDescription("ЖЦ для плана");
					lifeCyclePlan.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCyclePlan);

					move = new Move();
					move.setLifeCycle(lifeCyclePlan);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusInProgress);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCyclePlan);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCompleted);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCyclePlan);
					move.setFromStatus(statusInProgress);
					move.setToStatus(statusCanceled);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCyclePlan = new TemplateLifeCycle();
					templateLifeCyclePlan.setTemplateClass(Plan.class.getSimpleName().toLowerCase());
					templateLifeCyclePlan.setLifeCycle(lifeCyclePlan);
					templateLifeCycleRepository.save(templateLifeCyclePlan);

					//

					LifeCycle lifeCycleTemplateLesson = new LifeCycle();
					lifeCycleTemplateLesson.setName("ЖЦ для шаблона урока");
					lifeCycleTemplateLesson.setDescription("ЖЦ для шаблона урока");
					lifeCycleTemplateLesson.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCycleTemplateLesson);

					move = new Move();
					move.setLifeCycle(lifeCycleTemplateLesson);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusActive);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTemplateLesson);
					move.setFromStatus(statusActive);
					move.setToStatus(statusDuplicate);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCycleTemplateLesson = new TemplateLifeCycle();
					templateLifeCycleTemplateLesson.setTemplateClass(TemplateLesson.class.getSimpleName().toLowerCase());
					templateLifeCycleTemplateLesson.setLifeCycle(lifeCycleTemplateLesson);
					templateLifeCycleRepository.save(templateLifeCycleTemplateLesson);

					//

					LifeCycle lifeCycleTemplateTest = new LifeCycle();
					lifeCycleTemplateTest.setName("ЖЦ для шаблона теста");
					lifeCycleTemplateTest.setDescription("ЖЦ для шаблона теста");
					lifeCycleTemplateTest.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCycleTemplateTest);

					move = new Move();
					move.setLifeCycle(lifeCycleTemplateTest);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusActive);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTemplateTest);
					move.setFromStatus(statusActive);
					move.setToStatus(statusDuplicate);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCycleTemplateTest = new TemplateLifeCycle();
					templateLifeCycleTemplateTest.setTemplateClass(TemplateTest.class.getSimpleName().toLowerCase());
					templateLifeCycleTemplateTest.setLifeCycle(lifeCycleTemplateTest);
					templateLifeCycleRepository.save(templateLifeCycleTemplateTest);

					//

					LifeCycle lifeCycleTemplateCourse = new LifeCycle();
					lifeCycleTemplateCourse.setName("ЖЦ для шаблона курса");
					lifeCycleTemplateCourse.setDescription("ЖЦ для шаблона курса");
					lifeCycleTemplateCourse.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCycleTemplateCourse);

					move = new Move();
					move.setLifeCycle(lifeCycleTemplateCourse);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusActive);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTemplateCourse);
					move.setFromStatus(statusActive);
					move.setToStatus(statusDuplicate);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCycleTemplateCourse = new TemplateLifeCycle();
					templateLifeCycleTemplateCourse.setTemplateClass(TemplateCourse.class.getSimpleName().toLowerCase());
					templateLifeCycleTemplateCourse.setLifeCycle(lifeCycleTemplateCourse);
					templateLifeCycleRepository.save(templateLifeCycleTemplateCourse);

					//

					LifeCycle lifeCycleTemplatePlan = new LifeCycle();
					lifeCycleTemplatePlan.setName("ЖЦ для шаблона плана");
					lifeCycleTemplatePlan.setDescription("ЖЦ для шаблона плана");
					lifeCycleTemplatePlan.setInitStatus(statusDraft);
					lifeCycleRepository.save(lifeCycleTemplatePlan);

					move = new Move();
					move.setLifeCycle(lifeCycleTemplatePlan);
					move.setFromStatus(statusDraft);
					move.setToStatus(statusActive);
					moveRepository.save(move);

					move = new Move();
					move.setLifeCycle(lifeCycleTemplatePlan);
					move.setFromStatus(statusActive);
					move.setToStatus(statusDuplicate);
					moveRepository.save(move);

					TemplateLifeCycle templateLifeCycleTemplatePlan = new TemplateLifeCycle();
					templateLifeCycleTemplatePlan.setTemplateClass(TemplatePlan.class.getSimpleName().toLowerCase());
					templateLifeCycleTemplatePlan.setLifeCycle(lifeCycleTemplatePlan);
					templateLifeCycleRepository.save(templateLifeCycleTemplatePlan);

				}

			}

		}

		/*
		Person curator = new Person();
		curator.setUsername("Curator");
		curator.setPassword(new BCryptPasswordEncoder().encode("123"));
		curator.setEmail("qwe@qwe.qwe");
		curator.setSurname("Curator");
		curator.setName("Curator");
		curator.setMiddlename("Curator");
		curator = this.personRepository.save(curator);

		Person person1 = new Person();
		person1.setUsername("Person1");
		person1.setPassword(new BCryptPasswordEncoder().encode("123"));
		person1.setEmail("qwe@qwe.qwe");
		person1.setSurname("Person1");
		person1.setName("Person1");
		person1.setMiddlename("Person1");
		person1 = this.personRepository.save(person1);

		Person person2 = new Person();
		person2.setUsername("Person2");
		person2.setPassword(new BCryptPasswordEncoder().encode("123"));
		person2.setEmail("qwe@qwe.qwe");
		person2.setSurname("Person2");
		person2.setName("Person2");
		person2.setMiddlename("Person2");
		person2 = this.personRepository.save(person2);
		 */

		/*
		Person curator = new Person();
		curator.setUsername("Curator");
		curator.setPassword(new BCryptPasswordEncoder().encode("123"));
		curator.setEmail("qwe@qwe.qwe");
		curator.setSurname("Curator");
		curator.setName("Curator");
		curator.setMiddlename("Curator");
		curator = this.personRepository.save(curator);

		Person person1 = new Person();
		person1.setUsername("Person1");
		person1.setPassword(new BCryptPasswordEncoder().encode("123"));
		person1.setEmail("qwe@qwe.qwe");
		person1.setSurname("Person1");
		person1.setName("Person1");
		person1.setMiddlename("Person1");
		person1 = this.personRepository.save(person1);

		Person person2 = new Person();
		person2.setUsername("Person2");
		person2.setPassword(new BCryptPasswordEncoder().encode("123"));
		person2.setEmail("qwe@qwe.qwe");
		person2.setSurname("Person2");
		person2.setName("Person2");
		person2.setMiddlename("Person2");
		person2 = this.personRepository.save(person2);

		HashSet<Person> persons = new HashSet<Person>();
		persons.add(person1);
		persons.add(person2);

		Group group = new Group();
		group.setName("Group");
		group.setCurator(curator);
		group.setPersons(persons);
		group = groupRepository.save(group);

		//Получим пользователей с самого начала, по id

		Person curator1 = personRepository.findByPersonId(curator.getId());

		Person person11 = personRepository.findByPersonId(person1.getId());
		Person person22 = personRepository.findByPersonId(person2.getId());

		HashSet<Person> persons1 = new HashSet<Person>();
		persons1.add(person11);
		persons1.add(person22);

		Group group1 = groupRepository.findByGroupId(group.getId());

		group1.setName("Updated Group");
		group1.setCurator(curator1);
		group1.setPersons(persons1);

		//И сохраним
		group1 = groupRepository.save(group1);

		int stop = 6;
		 */

	}

}