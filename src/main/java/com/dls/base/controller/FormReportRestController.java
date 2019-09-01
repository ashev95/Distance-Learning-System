package com.dls.base.controller;

import com.dls.base.entity.Group;
import com.dls.base.entity.Person;
import com.dls.base.reports.ReportGenerator;
import com.dls.base.repository.*;
import com.dls.base.request.ReportContainer;
import com.dls.base.request.ReportResponseContainer;
import com.dls.base.ui.form.FormAttribute;
import com.dls.base.ui.form.FormTemplate;
import com.dls.base.utils.*;
import net.sf.jasperreports.engine.JRException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.dls.base.utils.Constant.*;

@RestController
public class FormReportRestController
{

	private final StatusRepository statusRepository;
	private final PersonRepository personRepository;
	private final GroupRepository groupRepository;
	private final TemplateLessonRepository templateLessonRepository;
	private final TemplateTestRepository templateTestRepository;
	private final TemplateCourseRepository templateCourseRepository;
	private final TemplatePlanRepository templatePlanRepository;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	@Autowired
	ReportGenerator reportGenerator;

	@Autowired
	StudentInfoRepository studentInfoRepository;

	@Autowired
	TrainingInfoRepository trainingInfoRepository;

	@Autowired
	FormReportRestController(StatusRepository statusRepository, PersonRepository personRepository, GroupRepository groupRepository, TemplateLessonRepository templateLessonRepository, TemplateTestRepository templateTestRepository, TemplateCourseRepository templateCourseRepository, TemplatePlanRepository templatePlanRepository) throws Exception {
		this.statusRepository = statusRepository;
		this.personRepository = personRepository;
		this.groupRepository = groupRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.templateTestRepository = templateTestRepository;
		this.templateCourseRepository = templateCourseRepository;
		this.templatePlanRepository = templatePlanRepository;
	}

	@GetMapping(value = "form/student_info/0", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getNewFormByIdentifier() {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.tabTitle = "Общие сведения о студентах группы";
		formTemplate.template = "student_info";
		formTemplate.attributes = new HashMap<String, FormAttribute>();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@GetMapping(value = "form/student_completed_trainings/0", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public FormTemplate getNewFormByIdentifier1() {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.tabTitle = "Сведения о тестировании студента";
		formTemplate.template = "student_completed_trainings";
		formTemplate.attributes = new HashMap<String, FormAttribute>();
		formTemplate.currentUserAttributes = utils.getFormAttributes(accessUtils.getCurrentPerson());
		return formTemplate;
	}

	@PostMapping(value = "form/student_info/build", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity buildReportStudentInfo(@RequestBody ReportContainer reportContainer) throws Exception {
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			String reportName = "studentInfo";
			String templateName = "templates/student_info.jrxml";
			Byte reportFormat = reportContainer.format;
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(buildReport(reportName, templateName, reportFormat, reportContainer));
		}
		return null;
	}

	@PostMapping(value = "form/student_completed_trainings/build", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity buildReportStudentCompletedTrainings(@RequestBody ReportContainer reportContainer) throws Exception {
		if (accessUtils.hasAtLeastRole(new ArrayListBuilder()
				.add(ROLE_ADMINISTRATOR)
				.add(ROLE_CURATOR)
				.getArrayList())) {
			String reportName = "studentCompletedTrainings";
			String templateName = "templates/student_completed_trainings.jrxml";
			Byte reportFormat = reportContainer.format;
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(buildReport(reportName, templateName, reportFormat, reportContainer));
		}
		return null;
	}

	public ReportResponseContainer buildReport(String reportName, String templateName, Byte reportFormat, ReportContainer reportContainer) throws Exception {
		String newTempResourceFileName = null;
		String newTempResourceDirName = utils.getNewTempResourceDirName();
		String fullReportName = reportName;
		ReportResponseContainer reportResponseContainer = new ReportResponseContainer();
		switch (reportFormat){
			case 0:
				fullReportName = fullReportName + ".pdf";
				reportResponseContainer.format = "pdf";
				break;
			case 1:
				fullReportName = fullReportName + ".xls";
				reportResponseContainer.format = "xls";
				break;
			case 2:
				fullReportName = fullReportName + ".doc";
				reportResponseContainer.format = "doc";
				break;
			default:
				throw new Exception("Указан некорректный формат");
		}
		File newTempResourceDir = new File(newTempResourceDirName);
		if (!newTempResourceDir.exists()){
			if (!newTempResourceDir.mkdirs()){
				throw new TempResourceNotLoadedException(newTempResourceDir.getAbsolutePath());
			}
		}
		newTempResourceFileName = utils.getNewTempResourceFileName(fullReportName);
		String newTempResourceFilePath = newTempResourceDirName + File.separatorChar + newTempResourceFileName;
		ArrayList dataList = null;
		HashMapBuilder hashMapBuilder = new HashMapBuilder();
		switch (templateName){
			case "templates/student_info.jrxml":
				Group group = groupRepository.findByGroupId(reportContainer.group);
				dataList = (ArrayList)studentInfoRepository.collect(group.getId());
				hashMapBuilder.put("GROUP_NAME", group.getName());
				break;
			case "templates/student_completed_trainings.jrxml":
				Person person = personRepository.findByPersonId(reportContainer.person);
				dataList = (ArrayList)trainingInfoRepository.collect(person.getId());
				hashMapBuilder.put("STUDENT_NAME", person.getSurname() + " " + person.getName() + " " + person.getMiddlename());
				break;
				default:
					throw new Exception("Указан неверный шаблон");
		}
		File file = reportGenerator.create(newTempResourceFilePath, templateName, hashMapBuilder.getHashMap(), dataList);
		reportResponseContainer.fileName = newTempResourceFileName;
		return reportResponseContainer;
	}

	@GetMapping(value = "form/report/get/{reportName}/{format}")
	public ResponseEntity<byte[]> getReport(@PathVariable String reportName, @PathVariable String format) throws Exception {
		String newTempResourceDirName = utils.getNewTempResourceDirName();
		File file = new File(newTempResourceDirName + File.separatorChar + reportName);
		byte[] data = new byte[0];
		try {
			data = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Tika tika = new Tika();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf(tika.detect(data)));
		headers.setCacheControl(CacheControl.noCache().getHeaderValue());
		headers.setContentDisposition(ContentDisposition.parse("filename=" + file.getName()));
		ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(data, headers, HttpStatus.OK);
		return responseEntity;
	}


}