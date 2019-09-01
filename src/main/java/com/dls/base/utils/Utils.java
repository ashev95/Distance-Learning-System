package com.dls.base.utils;

import com.dls.base.entity.*;
import com.dls.base.repository.*;
import com.dls.base.request.AnswerContainer;
import com.dls.base.ui.form.FormAttribute;
import com.dls.base.ui.view.ViewEntity;
import com.dls.base.ui.view.ViewTemplate;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

	private final TemplateTestVariantRepository templateTestVariantRepository;
	private final TemplateTestQuestionRepository templateTestQuestionRepository;
	private final TemplateTestAnswerRepository templateTestAnswerRepository;
	private final TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository;
	private final TemplateLessonRepository templateLessonRepository;
	private final TemplateTestRepository templateTestRepository;
	private final TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository;
	private final TemplateCourseRepository templateCourseRepository;
	private final ResourceRepository resourceRepository;

	@Value(value = "classpath:app.properties")
	private Resource appResource;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	AccessUtils accessUtils;

	Properties appProperties = null;

	String dataStorePath = null;

	HashMap <String, String> roles = null;

	private HashMap<String, FormAttributeParam> formAttributeParams = null;

	HashMap<String, String> extByMIME = null;

	@Autowired
	public Utils(TemplateTestVariantRepository templateTestVariantRepository, TemplateTestQuestionRepository templateTestQuestionRepository, TemplateTestAnswerRepository templateTestAnswerRepository, TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository, TemplateLessonRepository templateLessonRepository, TemplateTestRepository templateTestRepository, TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository, TemplateCourseRepository templateCourseRepository, ResourceRepository resourceRepository) {
		this.templateTestVariantRepository = templateTestVariantRepository;
		this.templateTestQuestionRepository = templateTestQuestionRepository;
		this.templateTestAnswerRepository = templateTestAnswerRepository;
		this.templateCourseTemplateResponseRepository = templateCourseTemplateResponseRepository;
		this.templateLessonRepository = templateLessonRepository;
		this.templateTestRepository = templateTestRepository;
		this.templatePlanTemplateResponseRepository = templatePlanTemplateResponseRepository;
		this.templateCourseRepository = templateCourseRepository;
		this.resourceRepository = resourceRepository;
	}

	public String getNewResourceDirName(){
		return this.getDataStorePath()
				+ File.separatorChar
				+ new SimpleDateFormat("yyyy" + File.separatorChar + "MM" + File.separatorChar + "dd")
				.format(new Date());
	}

	public String getNewTempResourceDirName(){
		return this.getTempDataStorePath()
				+ File.separatorChar
				+ new SimpleDateFormat("yyyy" + File.separatorChar + "MM" + File.separatorChar + "dd")
				.format(new Date());
	}

	public String getCurrentDateFormatted(String delimiter){
		return (new SimpleDateFormat("yyyy" + delimiter + "MM" + delimiter + "dd")
				.format(new Date()));
	}

	public Long getYear(Date date){
		return Long.parseLong(new SimpleDateFormat("yyyy")
				.format(date));
	}

	public Long getMonth(Date date){
		return Long.parseLong(new SimpleDateFormat("MM")
				.format(date));
	}

	public Long getDay(Date date){
		return Long.parseLong(new SimpleDateFormat("dd")
				.format(date));
	}

	public String getNewResourceFileName(){
		return UUID.randomUUID().toString() + ".dat";
	}

	public String getNewTempResourceFileName(String reportFileName){
		return accessUtils.getCurrentPerson().getUsername() + "_" + UUID.randomUUID().toString() + "_" + reportFileName;
	}

	public String getDataStorePath(){
		if (this.dataStorePath == null){
			dataStorePath = this.getAppProperty("dataStore.path");
		}
		return this.dataStorePath;
	}

	public String getTempDataStorePath(){
		if (this.dataStorePath == null){
			dataStorePath = this.getAppProperty("tempDataStore.path");
		}
		return this.dataStorePath;
	}

	private Properties getAppProperties(){
		if (this.appProperties == null){
			try {
				InputStream inputStream = this.appResource.getInputStream();
				this.appProperties = new Properties();
				this.appProperties.load(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.appProperties;
	}

	public String getAppProperty(String key){
		return this.getAppProperties().getProperty(key);
	}

	public HashMap<String, String> getRoles(){
		if (this.roles == null){
			HashMap<String, HashMap<String, String>> tmpRoles = new HashMap<String, HashMap<String, String>>();
			for (Object key : this.getAppProperties().keySet()){
				String k = (String) key;
				String [] arr = k.split("\\.");
				if (arr.length == 3){
					if (arr[0].equals("roles")){
						HashMap<String, String> tmpRole = new HashMap<String, String>();
						if (tmpRoles.containsKey(arr[1])){
							tmpRole = tmpRoles.get(arr[1]);
						}
						tmpRole.put(arr[2], this.getAppProperty(k));
						tmpRoles.put(arr[1], tmpRole);
					}
				}
			}
			this.roles = new HashMap<String, String>();
			for (String key : tmpRoles.keySet()){
				HashMap<String, String> tmpRole = tmpRoles.get(key);
				if (tmpRole.containsKey("code") && tmpRole.containsKey("name")){
					this.roles.put(tmpRole.get("code"), tmpRole.get("name"));
				}
			}
		}
		return this.roles;
	}

	public static String inputStreamToString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line;
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"));
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}

	public static <T> List<Field> getFields(T t) {
		List<Field> fields = new ArrayList<>();
		Class clazz = t.getClass();
		while (clazz != Object.class) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		return fields;
	}

	public static boolean isNull(Object object){
		return (object == null);
	}

	public static boolean isNumeric(String str)
	{
		try
		{
			double d = Double.parseDouble(str);
		}
		catch(NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}

	public static boolean isBoolean(String str){
		boolean res = false;
		if ((str.equals("true")) || str.equals("false")) {
			res = true;
		}
		return res;
	}

	public static HashMap<String, String> rolesToMap(Set<Role> roles){
		HashMap<String, String> map = new HashMap<String, String>();
		Iterator iterator = roles.iterator();
		while (iterator.hasNext()){
			Role role = (Role)iterator.next();
			map.put(role.getCode(), role.getName());
		}
		return map;
	}

	public static HashMap<Long, Long> answerContainerToMap(List<AnswerContainer> answers){
		HashMap<Long, Long> map = new HashMap<Long, Long>();
		Iterator iterator = answers.iterator();
		while (iterator.hasNext()){
			AnswerContainer answerContainer = (AnswerContainer) iterator.next();
			map.put(answerContainer.answerId, answerContainer.answerId);
		}
		return map;
	}

	public static HashMap<Long, String> personsToMap(Set<Person> persons){
		HashMap<Long, String> map = new HashMap<Long, String>();
		Iterator iterator = persons.iterator();
		while (iterator.hasNext()){
			Person person = (Person)iterator.next();
			map.put(person.getId(), person.getUsername());
		}
		return map;
	}

	public boolean checkTemplate(Object o, boolean checkChild) throws Exception {
		switch (o.getClass().getSimpleName().toLowerCase()){
			case "templatelesson":
				TemplateLesson templateLesson = (TemplateLesson) o;
				if (!templateLesson.getStatus().getCode().equals("active")){
					throw new Exception("Шаблон не актуален");
				}
				if (resourceRepository.findByEntityClassAndEntityId(templateLesson.getClass().getSimpleName().toLowerCase(), templateLesson.getId()).size() == 0){
					throw new Exception("Урок должен иметь хотя бы один ресурс");
				}
				break;
			case "templatetest":
				TemplateTest templateTest = (TemplateTest) o;
				if (!templateTest.getStatus().getCode().equals("active")){
					throw new Exception("Шаблон не актуален");
				}
				int variant = 0;
				for (TemplateTestVariant templateTestVariant : templateTestVariantRepository.findByTemplateTestId(templateTest.getId())){
					variant++;
					int question = 0;
					for (TemplateTestQuestion templateTestQuestion : templateTestQuestionRepository.findByTemplateTestVariantId(templateTestVariant.getId())){
						question++;
						int answer = 0;
						int correctAnswer = 0;
						for (TemplateTestAnswer templateTestAnswer : templateTestAnswerRepository.findByTemplateTestQuestionId(templateTestQuestion.getId())){
							answer++;
							if (templateTestAnswer.getCorrect()){
								correctAnswer++;
							}
						}
						if (answer == 0){
							throw new Exception("У вопроса отсутствует ответ");
						}
						if (correctAnswer == 0){
							throw new Exception("У вопроса отсутствует верный ответ");
						}
					}
					if (question == 0){
						throw new Exception("У варианта отсутствует вопрос");
					}
				}
				if (variant == 0){
					throw new Exception("У теста отсутствует вариант");
				}
				break;
			case "templatecourse":
				TemplateCourse templateCourse = (TemplateCourse) o;
				if (!templateCourse.getStatus().getCode().equals("active")){
					throw new Exception("Шаблон не актуален");
				}
				Set <TemplateCourseTemplateResponse> templateCourseTemplateResponseSet = templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByTemplateCourseId(templateCourse.getId());
				if (templateCourseTemplateResponseSet.size() == 0){
					throw new Exception("Не указаны мероприятия");
				}
				if (checkChild){
					for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseSet){
						switch (templateCourseTemplateResponse.getTemplateResponseClass()){
							case "templatelesson":
								TemplateLesson templateLesson1 = templateLessonRepository.findByTemplateLessonId(templateCourseTemplateResponse.getTemplateResponseId());
								if (!this.checkTemplate(templateLesson1, checkChild)){
									return false;
								}
								break;
							case "templatetest":
								TemplateTest templateTest1 = templateTestRepository.findByTemplateTestId(templateCourseTemplateResponse.getTemplateResponseId());
								if (!this.checkTemplate(templateTest1, checkChild)){
									return false;
								}
								break;
							default:
								throw new Exception("Не определён тип мероприятия");
						}
					}
				}
				break;
			case "templateplan":
				TemplatePlan templatePlan = (TemplatePlan) o;
				if (!templatePlan.getStatus().getCode().equals("active")){
					throw new Exception("Шаблон не актуален");
				}
				Set <TemplatePlanTemplateResponse> templatePlanTemplateResponseSet = templatePlanTemplateResponseRepository.findTemplatePlanTemplateResponseByTemplatePlanId(templatePlan.getId());
				if (templatePlanTemplateResponseSet.size() == 0){
					throw new Exception("Не указаны мероприятия");
				}
				if (checkChild){
					for (TemplatePlanTemplateResponse templatePlanTemplateResponse : templatePlanTemplateResponseSet){
						switch (templatePlanTemplateResponse.getTemplateResponseClass()){
							case "templatelesson":
								TemplateLesson templateLesson2 = templateLessonRepository.findByTemplateLessonId(templatePlanTemplateResponse.getTemplateResponseId());
								if (!this.checkTemplate(templateLesson2, checkChild)){
									return false;
								}
								break;
							case "templatetest":
								TemplateTest templateTest2 = templateTestRepository.findByTemplateTestId(templatePlanTemplateResponse.getTemplateResponseId());
								if (!this.checkTemplate(templateTest2, checkChild)){
									return false;
								}
								break;
							case "templatecourse":
								TemplateCourse templateCourse1 = templateCourseRepository.findByTemplateCourseId(templatePlanTemplateResponse.getTemplateResponseId());
								if (!this.checkTemplate(templateCourse1, checkChild)){
									return false;
								}
								break;
							default:
								throw new Exception("Не определён тип мероприятия");
						}
					}
				}
				break;
			case "lesson":
				Lesson lesson = (Lesson) o;
				if (checkChild){
					if (!this.checkTemplate(lesson.getTemplateLesson(), checkChild)){
						return false;
					}
				}
				break;
			case "test":
				Test test = (Test) o;
				if (checkChild){
					if (!this.checkTemplate(test.getTemplateTest(), checkChild)){
						return false;
					}
				}
				break;
			case "course":
				Course course = (Course) o;
				if (checkChild){
					if (!this.checkTemplate(course.getTemplateCourse(), checkChild)){
						return false;
					}
				}
				break;
			case "plan":
				Plan plan = (Plan) o;
				if (checkChild){
					if (!this.checkTemplate(plan.getTemplatePlan(), checkChild)){
						return false;
					}
				}
				break;
			default:
				throw new Exception("Не определён тип мероприятия");
		}
		return true;
	}

	private FormAttributeParam getNewFormAttributeParam(){
		return (new FormAttributeParam());
	}

	private HashMap<String, FormAttributeParam> getFormAttributeParams(){
		if (formAttributeParams == null){
			formAttributeParams = new HashMap<String, FormAttributeParam>();
			formAttributeParams.put("person",
					getNewFormAttributeParam()
							.toSkip("password")
							.toSkip("confirmpassword")
			);
			formAttributeParams.put("group",
					getNewFormAttributeParam()
							.toEx("curator")
			);
			formAttributeParams.put("templatelesson",
					getNewFormAttributeParam()
							.toEx("status")
							.toEx("parent")
							.toEx("author")
							.toEx("category")
			);
			formAttributeParams.put("templatetest",
					getNewFormAttributeParam()
							.toEx("status")
							.toEx("parent")
							.toEx("author")
							.toEx("category")
			);
			formAttributeParams.put("templatecourse",
					getNewFormAttributeParam()
							.toEx("status")
							.toEx("parent")
							.toEx("author")
			);
			formAttributeParams.put("templateplan",
					getNewFormAttributeParam()
							.toEx("status")
							.toEx("parent")
							.toEx("author")
			);
			formAttributeParams.put("templatetestvariant",
					getNewFormAttributeParam()
							.toEx("author")
							.toEx("templatetest")
			);
			formAttributeParams.put("templatetestquestion",
					getNewFormAttributeParam()
							.toEx("author")
							.toEx("templatetestvariant")
			);
			formAttributeParams.put("templatetestanswer",
					getNewFormAttributeParam()
							.toEx("author")
							.toEx("templatetestquestion")
			);
			formAttributeParams.put("lesson",
					getNewFormAttributeParam()
							.toEx("templatelesson")
							.toEx("status")
							.toEx("group")
							.toEx("curator")
							.toEx("author")
			);
			formAttributeParams.put("course",
					getNewFormAttributeParam()
							.toEx("templatecourse")
							.toEx("status")
							.toEx("group")
							.toEx("curator")
							.toEx("author")
			);
			formAttributeParams.put("plan",
					getNewFormAttributeParam()
							.toEx("templateplan")
							.toEx("status")
							.toEx("group")
							.toEx("curator")
							.toEx("author")
			);
			formAttributeParams.put("test",
					getNewFormAttributeParam()
							.toEx("templatetest")
							.toEx("status")
							.toEx("group")
							.toEx("curator")
							.toEx("author")
			);
			formAttributeParams.put("lessonperson",
					getNewFormAttributeParam()
							.toEx("lesson")
							.toEx("person")
							.toEx("status")
			);
			formAttributeParams.put("courselesson",
					getNewFormAttributeParam()
							.toEx("course")
							.toEx("person")
							.toEx("status")
			);
			formAttributeParams.put("testvariantperson",
					getNewFormAttributeParam()
							.toEx("test")
							.toEx("person")
							.toEx("status")
							.toEx("currenttemplatetestquestion")
							.toEx("clickedtemplatetestquestion")
			);
		}
		return formAttributeParams;
	}

	public HashMap<String, FormAttribute> getFormAttributes(Object entity){
		HashMap<String, FormAttribute> formAttributes = new HashMap<String, FormAttribute>();
		for (Field field : this.getFields(entity)){
			if (!(getFormAttributeParams().containsKey(entity.getClass().getSimpleName().toLowerCase()) && getFormAttributeParams().get(entity.getClass().getSimpleName().toLowerCase()).hasSkip(field.getName().toLowerCase()))){
				field.setAccessible(true);
				try {
					FormAttribute formAttribute = new FormAttribute();
					formAttribute.name = field.getName();
					formAttribute.type = field.getType().getSimpleName().toLowerCase();
					if (getFormAttributeParams().containsKey(entity.getClass().getSimpleName().toLowerCase()) && getFormAttributeParams().get(entity.getClass().getSimpleName().toLowerCase()).hasEx(field.getName().toLowerCase())){
						formAttribute.value = (field.get(entity) == null ? null : getFormAttributes(field.get(entity)));
					}else{
						if (formAttribute.type.equals("date")){
							if (field.get(entity) != null){
								formAttribute.value = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
										.format((Date)field.get(entity));
							}
						}else{
							formAttribute.value = field.get(entity);
						}
					}
					formAttribute.title = field.getName();
					formAttributes.put(field.getName(), formAttribute);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return formAttributes;
	}

	public com.dls.base.entity.Resource getResourceClone(com.dls.base.entity.Resource sourceResource) throws Exception {
		com.dls.base.entity.Resource newResource = new com.dls.base.entity.Resource();
		//
		File file = new File(sourceResource.getValue());
		byte[] data = new byte[0];
		try {
			data = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		String newResourceFilePath = null;
		try {
			String newResourceDirName = this.getNewResourceDirName();
			File newResourceDir = new File(newResourceDirName);
			if (!newResourceDir.exists()){
				if (!newResourceDir.mkdirs()){
					throw new Exception("Не удалось загрузить файл" + " '" + newResourceDir.getAbsolutePath() + "'.");
				}
			}
			String newResourceFileName = this.getNewResourceFileName();
			newResourceFilePath = newResourceDirName + File.separatorChar + newResourceFileName;
			File newResourceFile = new File(newResourceFilePath);
			newResourceFile.createNewFile();
			FileOutputStream stream = new FileOutputStream(newResourceFile);
			try {
				stream.write(data);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		newResource.setValue(newResourceFilePath);
		//
		newResource.setType(sourceResource.getType());
		newResource.setPosition(sourceResource.getPosition());
		newResource.setExtension(sourceResource.getExtension());
		newResource.setName(sourceResource.getName());
		return newResource;
	}

	public ViewTemplate getViewTemplate(String resourcePath, Boolean initItems) throws Exception {
		XmlMapper xmlMapper = new XmlMapper();
		final Resource fileResource = resourceLoader.getResource(resourcePath);
		File file = fileResource.getFile();
		String xml = this.inputStreamToString(new FileInputStream(file));
		ViewTemplate viewTemplate = xmlMapper.readValue(xml, ViewTemplate.class);
		if (initItems){
			viewTemplate.data.items = new ArrayList<ViewEntity>();
		}
		return viewTemplate;
	}


	public String getJsonString(ViewTemplate viewTemplate) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return objectMapper.writeValueAsString(viewTemplate);
	}

	public HashMap<String, String> getExtensionByMIMEMap(){
		if (extByMIME == null){
			extByMIME = new HashMap<String, String>();
			extByMIME.put("application/andrew-inset", "ez");
			extByMIME.put("application/applixware", "aw");
			extByMIME.put("application/atom+xml", "atom");
			extByMIME.put("application/atomcat+xml", "atomcat");
			extByMIME.put("application/atomsvc+xml", "atomsvc");
			extByMIME.put("application/ccxml+xml", "ccxml");
			extByMIME.put("application/cdmi-capability", "cdmia");
			extByMIME.put("application/cdmi-container", "cdmic");
			extByMIME.put("application/cdmi-domain", "cdmid");
			extByMIME.put("application/cdmi-object", "cdmio");
			extByMIME.put("application/cdmi-queue", "cdmiq");
			extByMIME.put("application/cu-seeme", "cu");
			extByMIME.put("application/davmount+xml", "davmount");
			extByMIME.put("application/docbook+xml", "dbk");
			extByMIME.put("application/dssc+der", "dssc");
			extByMIME.put("application/dssc+xml", "xdssc");
			extByMIME.put("application/ecmascript", "ecma");
			extByMIME.put("application/emma+xml", "emma");
			extByMIME.put("application/epub+zip", "epub");
			extByMIME.put("application/exi", "exi");
			extByMIME.put("application/font-tdpfr", "pfr");
			extByMIME.put("application/font-woff", "woff");
			extByMIME.put("application/gml+xml", "gml");
			extByMIME.put("application/gpx+xml", "gpx");
			extByMIME.put("application/gxf", "gxf");
			extByMIME.put("application/hyperstudio", "stk");
			extByMIME.put("application/inkml+xml", "ink inkml");
			extByMIME.put("application/ipfix", "ipfix");
			extByMIME.put("application/java-archive", "jar");
			extByMIME.put("application/java-serialized-object", "ser");
			extByMIME.put("application/java-vm", "class");
			extByMIME.put("application/javascript", "js");
			extByMIME.put("application/json", "json");
			extByMIME.put("application/jsonml+json", "jsonml");
			extByMIME.put("application/lost+xml", "lostxml");
			extByMIME.put("application/mac-binhex4 ", "hqx");
			extByMIME.put("application/mac-compactpro", "cpt");
			extByMIME.put("application/mads+xml", "mads");
			extByMIME.put("application/marc", "mrc");
			extByMIME.put("application/marcxml+xml", "mrcx");
			extByMIME.put("application/mathematica", "ma nb mb");
			extByMIME.put("application/mathml+xml", "mathml");
			extByMIME.put("application/mbox", "mbox");
			extByMIME.put("application/mediaservercontrol+xml", "mscml");
			extByMIME.put("application/metalink+xml", "metalink");
			extByMIME.put("application/metalink4+xml", "meta4");
			extByMIME.put("application/mets+xml", "mets");
			extByMIME.put("application/mods+xml", "mods");
			extByMIME.put("application/mp21", "m21 mp21");
			extByMIME.put("application/mp4", "mp4s");
			extByMIME.put("application/msword", "doc dot");
			extByMIME.put("application/mxf", "mxf");
			extByMIME.put("application/octet-stream", "bin dms lrf mar so dist distz pkg bpk dump elc deploy");
			extByMIME.put("application/oda", "oda");
			extByMIME.put("application/oebps-package+xml", "opf");
			extByMIME.put("application/ogg", "ogx");
			extByMIME.put("application/omdoc+xml", "omdoc");
			extByMIME.put("application/onenote", "onetoc onetoc2 onetmp onepkg");
			extByMIME.put("application/oxps", "oxps");
			extByMIME.put("application/patch-ops-error+xml", "xer");
			extByMIME.put("application/pdf", "pdf");
			extByMIME.put("application/pgp-encrypted", "pgp");
			extByMIME.put("application/pgp-signature", "asc sig");
			extByMIME.put("application/pics-rules", "prf");
			extByMIME.put("application/pkcs1 ", "p1 ");
			extByMIME.put("application/pkcs7-mime", "p7m p7c");
			extByMIME.put("application/pkcs7-signature", "p7s");
			extByMIME.put("application/pkcs8", "p8");
			extByMIME.put("application/pkix-attr-cert", "ac");
			extByMIME.put("application/pkix-cert", "cer");
			extByMIME.put("application/pkix-crl", "crl");
			extByMIME.put("application/pkix-pkipath", "pkipath");
			extByMIME.put("application/pkixcmp", "pki");
			extByMIME.put("application/pls+xml", "pls");
			extByMIME.put("application/postscript", "ai eps ps");
			extByMIME.put("application/prs.cww", "cww");
			extByMIME.put("application/pskc+xml", "pskcxml");
			extByMIME.put("application/rdf+xml", "rdf");
			extByMIME.put("application/reginfo+xml", "rif");
			extByMIME.put("application/relax-ng-compact-syntax", "rnc");
			extByMIME.put("application/resource-lists+xml", "rl");
			extByMIME.put("application/resource-lists-diff+xml", "rld");
			extByMIME.put("application/rls-services+xml", "rs");
			extByMIME.put("application/rpki-ghostbusters", "gbr");
			extByMIME.put("application/rpki-manifest", "mft");
			extByMIME.put("application/rpki-roa", "roa");
			extByMIME.put("application/rsd+xml", "rsd");
			extByMIME.put("application/rss+xml", "rss");
			extByMIME.put("application/rtf", "rtf");
			extByMIME.put("application/sbml+xml", "sbml");
			extByMIME.put("application/scvp-cv-request", "scq");
			extByMIME.put("application/scvp-cv-response", "scs");
			extByMIME.put("application/scvp-vp-request", "spq");
			extByMIME.put("application/scvp-vp-response", "spp");
			extByMIME.put("application/sdp", "sdp");
			extByMIME.put("application/set-payment-initiation", "setpay");
			extByMIME.put("application/set-registration-initiation", "setreg");
			extByMIME.put("application/shf+xml", "shf");
			extByMIME.put("application/smil+xml", "smi smil");
			extByMIME.put("application/sparql-query", "rq");
			extByMIME.put("application/sparql-results+xml", "srx");
			extByMIME.put("application/srgs", "gram");
			extByMIME.put("application/srgs+xml", "grxml");
			extByMIME.put("application/sru+xml", "sru");
			extByMIME.put("application/ssdl+xml", "ssdl");
			extByMIME.put("application/ssml+xml", "ssml");
			extByMIME.put("application/tei+xml", "tei teicorpus");
			extByMIME.put("application/thraud+xml", "tfi");
			extByMIME.put("application/timestamped-data", "tsd");
			extByMIME.put("application/vnd.3gpp.pic-bw-large", "plb");
			extByMIME.put("application/vnd.3gpp.pic-bw-small", "psb");
			extByMIME.put("application/vnd.3gpp.pic-bw-var", "pvb");
			extByMIME.put("application/vnd.3gpp2.tcap", "tcap");
			extByMIME.put("application/vnd.3m.post-it-notes", "pwn");
			extByMIME.put("application/vnd.accpac.simply.aso", "aso");
			extByMIME.put("application/vnd.accpac.simply.imp", "imp");
			extByMIME.put("application/vnd.acucobol", "acu");
			extByMIME.put("application/vnd.acucorp", "atc acutc");
			extByMIME.put("application/vnd.adobe.air-application-installer-package+zip", "air");
			extByMIME.put("application/vnd.adobe.formscentral.fcdt", "fcdt");
			extByMIME.put("application/vnd.adobe.fxp", "fxp fxpl");
			extByMIME.put("application/vnd.adobe.xdp+xml", "xdp");
			extByMIME.put("application/vnd.adobe.xfdf", "xfdf");
			extByMIME.put("application/vnd.ahead.space", "ahead");
			extByMIME.put("application/vnd.airzip.filesecure.azf", "azf");
			extByMIME.put("application/vnd.airzip.filesecure.azs", "azs");
			extByMIME.put("application/vnd.amazon.ebook", "azw");
			extByMIME.put("application/vnd.americandynamics.acc", "acc");
			extByMIME.put("application/vnd.amiga.ami", "ami");
			extByMIME.put("application/vnd.android.package-archive", "apk");
			extByMIME.put("application/vnd.anser-web-certificate-issue-initiation", "cii");
			extByMIME.put("application/vnd.anser-web-funds-transfer-initiation", "fti");
			extByMIME.put("application/vnd.antix.game-component", "atx");
			extByMIME.put("application/vnd.apple.installer+xml", "mpkg");
			extByMIME.put("application/vnd.apple.mpegurl", "m3u8");
			extByMIME.put("application/vnd.aristanetworks.swi", "swi");
			extByMIME.put("application/vnd.astraea-software.iota", "iota");
			extByMIME.put("application/vnd.audiograph", "aep");
			extByMIME.put("application/vnd.blueice.multipass", "mpm");
			extByMIME.put("application/vnd.bmi", "bmi");
			extByMIME.put("application/vnd.businessobjects", "rep");
			extByMIME.put("application/vnd.chemdraw+xml", "cdxml");
			extByMIME.put("application/vnd.chipnuts.karaoke-mmd", "mmd");
			extByMIME.put("application/vnd.cinderella", "cdy");
			extByMIME.put("application/vnd.claymore", "cla");
			extByMIME.put("application/vnd.cloanto.rp9", "rp9");
			extByMIME.put("application/vnd.clonk.c4group", "c4g c4d c4f c4p c4u");
			extByMIME.put("application/vnd.cluetrust.cartomobile-config", "c11amc");
			extByMIME.put("application/vnd.cluetrust.cartomobile-config-pkg", "c11amz");
			extByMIME.put("application/vnd.commonspace", "csp");
			extByMIME.put("application/vnd.contact.cmsg", "cdbcmsg");
			extByMIME.put("application/vnd.cosmocaller", "cmc");
			extByMIME.put("application/vnd.crick.clicker", "clkx");
			extByMIME.put("application/vnd.crick.clicker.keyboard", "clkk");
			extByMIME.put("application/vnd.crick.clicker.palette", "clkp");
			extByMIME.put("application/vnd.crick.clicker.template", "clkt");
			extByMIME.put("application/vnd.crick.clicker.wordbank", "clkw");
			extByMIME.put("application/vnd.criticaltools.wbs+xml", "wbs");
			extByMIME.put("application/vnd.ctc-posml", "pml");
			extByMIME.put("application/vnd.cups-ppd", "ppd");
			extByMIME.put("application/vnd.curl.car", "car");
			extByMIME.put("application/vnd.curl.pcurl", "pcurl");
			extByMIME.put("application/vnd.dart", "dart");
			extByMIME.put("application/vnd.data-vision.rdz", "rdz");
			extByMIME.put("application/vnd.dece.data", "uvf uvvf uvd uvvd");
			extByMIME.put("application/vnd.dece.ttml+xml", "uvt uvvt");
			extByMIME.put("application/vnd.dece.unspecified", "uvx uvvx");
			extByMIME.put("application/vnd.dece.zip", "uvz uvvz");
			extByMIME.put("application/vnd.denovo.fcselayout-link", "fe_launch");
			extByMIME.put("application/vnd.dna", "dna");
			extByMIME.put("application/vnd.dolby.mlp", "mlp");
			extByMIME.put("application/vnd.dpgraph", "dpg");
			extByMIME.put("application/vnd.dreamfactory", "dfac");
			extByMIME.put("application/vnd.ds-keypoint", "kpxx");
			extByMIME.put("application/vnd.dvb.ait", "ait");
			extByMIME.put("application/vnd.dvb.service", "svc");
			extByMIME.put("application/vnd.dynageo", "geo");
			extByMIME.put("application/vnd.ecowin.chart", "mag");
			extByMIME.put("application/vnd.enliven", "nml");
			extByMIME.put("application/vnd.epson.esf", "esf");
			extByMIME.put("application/vnd.epson.msf", "msf");
			extByMIME.put("application/vnd.epson.quickanime", "qam");
			extByMIME.put("application/vnd.epson.salt", "slt");
			extByMIME.put("application/vnd.epson.ssf", "ssf");
			extByMIME.put("application/vnd.eszigno3+xml", "es3 et3");
			extByMIME.put("application/vnd.ezpix-album", "ez2");
			extByMIME.put("application/vnd.ezpix-package", "ez3");
			extByMIME.put("application/vnd.fdf", "fdf");
			extByMIME.put("application/vnd.fdsn.mseed", "mseed");
			extByMIME.put("application/vnd.fdsn.seed", "seed dataless");
			extByMIME.put("application/vnd.flographit", "gph");
			extByMIME.put("application/vnd.fluxtime.clip", "ftc");
			extByMIME.put("application/vnd.framemaker", "fm frame maker book");
			extByMIME.put("application/vnd.frogans.fnc", "fnc");
			extByMIME.put("application/vnd.frogans.ltf", "ltf");
			extByMIME.put("application/vnd.fsc.weblaunch", "fsc");
			extByMIME.put("application/vnd.fujitsu.oasys", "oas");
			extByMIME.put("application/vnd.fujitsu.oasys2", "oa2");
			extByMIME.put("application/vnd.fujitsu.oasys3", "oa3");
			extByMIME.put("application/vnd.fujitsu.oasysgp", "fg5");
			extByMIME.put("application/vnd.fujitsu.oasysprs", "bh2");
			extByMIME.put("application/vnd.fujixerox.ddd", "ddd");
			extByMIME.put("application/vnd.fujixerox.docuworks", "xdw");
			extByMIME.put("application/vnd.fujixerox.docuworks.binder", "xbd");
			extByMIME.put("application/vnd.fuzzysheet", "fzs");
			extByMIME.put("application/vnd.genomatix.tuxedo", "txd");
			extByMIME.put("application/vnd.geogebra.file", "ggb");
			extByMIME.put("application/vnd.geogebra.tool", "ggt");
			extByMIME.put("application/vnd.geometry-explorer", "gex gre");
			extByMIME.put("application/vnd.geonext", "gxt");
			extByMIME.put("application/vnd.geoplan", "g2w");
			extByMIME.put("application/vnd.geospace", "g3w");
			extByMIME.put("application/vnd.gmx", "gmx");
			extByMIME.put("application/vnd.google-earth.kml+xml", "kml");
			extByMIME.put("application/vnd.google-earth.kmz", "kmz");
			extByMIME.put("application/vnd.grafeq", "gqf gqs");
			extByMIME.put("application/vnd.groove-account", "gac");
			extByMIME.put("application/vnd.groove-help", "ghf");
			extByMIME.put("application/vnd.groove-identity-message", "gim");
			extByMIME.put("application/vnd.groove-injector", "grv");
			extByMIME.put("application/vnd.groove-tool-message", "gtm");
			extByMIME.put("application/vnd.groove-tool-template", "tpl");
			extByMIME.put("application/vnd.groove-vcard", "vcg");
			extByMIME.put("application/vnd.hal+xml", "hal");
			extByMIME.put("application/vnd.handheld-entertainment+xml", "zmm");
			extByMIME.put("application/vnd.hbci", "hbci");
			extByMIME.put("application/vnd.hhe.lesson-player", "les");
			extByMIME.put("application/vnd.hp-hpgl", "hpgl");
			extByMIME.put("application/vnd.hp-hpid", "hpid");
			extByMIME.put("application/vnd.hp-hps", "hps");
			extByMIME.put("application/vnd.hp-jlyt", "jlt");
			extByMIME.put("application/vnd.hp-pcl", "pcl");
			extByMIME.put("application/vnd.hp-pclxl", "pclxl");
			extByMIME.put("application/vnd.hydrostatix.sof-data", "sfd-hdstx");
			extByMIME.put("application/vnd.ibm.minipay", "mpy");
			extByMIME.put("application/vnd.ibm.modcap", "afp listafp list382 ");
			extByMIME.put("application/vnd.ibm.rights-management", "irm");
			extByMIME.put("application/vnd.ibm.secure-container", "sc");
			extByMIME.put("application/vnd.iccprofile", "icc icm");
			extByMIME.put("application/vnd.igloader", "igl");
			extByMIME.put("application/vnd.immervision-ivp", "ivp");
			extByMIME.put("application/vnd.immervision-ivu", "ivu");
			extByMIME.put("application/vnd.insors.igm", "igm");
			extByMIME.put("application/vnd.intercon.formnet", "xpw xpx");
			extByMIME.put("application/vnd.intergeo", "i2g");
			extByMIME.put("application/vnd.intu.qbo", "qbo");
			extByMIME.put("application/vnd.intu.qfx", "qfx");
			extByMIME.put("application/vnd.ipunplugged.rcprofile", "rcprofile");
			extByMIME.put("application/vnd.irepository.package+xml", "irp");
			extByMIME.put("application/vnd.is-xpr", "xpr");
			extByMIME.put("application/vnd.isac.fcs", "fcs");
			extByMIME.put("application/vnd.jam", "jam");
			extByMIME.put("application/vnd.jcp.javame.midlet-rms", "rms");
			extByMIME.put("application/vnd.jisp", "jisp");
			extByMIME.put("application/vnd.joost.joda-archive", "joda");
			extByMIME.put("application/vnd.kahootz", "ktz ktr");
			extByMIME.put("application/vnd.kde.karbon", "karbon");
			extByMIME.put("application/vnd.kde.kchart", "chrt");
			extByMIME.put("application/vnd.kde.kformula", "kfo");
			extByMIME.put("application/vnd.kde.kivio", "flw");
			extByMIME.put("application/vnd.kde.kontour", "kon");
			extByMIME.put("application/vnd.kde.kpresenter", "kpr kpt");
			extByMIME.put("application/vnd.kde.kspread", "ksp");
			extByMIME.put("application/vnd.kde.kword", "kwd kwt");
			extByMIME.put("application/vnd.kenameaapp", "htke");
			extByMIME.put("application/vnd.kidspiration", "kia");
			extByMIME.put("application/vnd.kinar", "kne knp");
			extByMIME.put("application/vnd.koan", "skp skd skt skm");
			extByMIME.put("application/vnd.kodak-descriptor", "sse");
			extByMIME.put("application/vnd.las.las+xml", "lasxml");
			extByMIME.put("application/vnd.llamagraphics.life-balance.desktop", "lbd");
			extByMIME.put("application/vnd.llamagraphics.life-balance.exchange+xml", "lbe");
			extByMIME.put("application/vnd.lotus-1-2-3", "123");
			extByMIME.put("application/vnd.lotus-approach", "apr");
			extByMIME.put("application/vnd.lotus-freelance", "pre");
			extByMIME.put("application/vnd.lotus-notes", "nsf");
			extByMIME.put("application/vnd.lotus-organizer", "org");
			extByMIME.put("application/vnd.lotus-screencam", "scm");
			extByMIME.put("application/vnd.lotus-wordpro", "lwp");
			extByMIME.put("application/vnd.macports.portpkg", "portpkg");
			extByMIME.put("application/vnd.mcd", "mcd");
			extByMIME.put("application/vnd.medcalcdata", "mc1");
			extByMIME.put("application/vnd.mediastation.cdkey", "cdkey");
			extByMIME.put("application/vnd.mfer", "mwf");
			extByMIME.put("application/vnd.mfmp", "mfm");
			extByMIME.put("application/vnd.micrografx.flo", "flo");
			extByMIME.put("application/vnd.micrografx.igx", "igx");
			extByMIME.put("application/vnd.mif", "mif");
			extByMIME.put("application/vnd.mobius.daf", "daf");
			extByMIME.put("application/vnd.mobius.dis", "dis");
			extByMIME.put("application/vnd.mobius.mbk", "mbk");
			extByMIME.put("application/vnd.mobius.mqy", "mqy");
			extByMIME.put("application/vnd.mobius.msl", "msl");
			extByMIME.put("application/vnd.mobius.plc", "plc");
			extByMIME.put("application/vnd.mobius.txf", "txf");
			extByMIME.put("application/vnd.mophun.application", "mpn");
			extByMIME.put("application/vnd.mophun.certificate", "mpc");
			extByMIME.put("application/vnd.mozilla.xul+xml", "xul");
			extByMIME.put("application/vnd.ms-artgalry", "cil");
			extByMIME.put("application/vnd.ms-cab-compressed", "cab");
			extByMIME.put("application/vnd.ms-excel", "xls xlm xla xlc xlt xlw");
			extByMIME.put("application/vnd.ms-excel.addin.macroenabled.12", "xlam");
			extByMIME.put("application/vnd.ms-excel.sheet.binary.macroenabled.12", "xlsb");
			extByMIME.put("application/vnd.ms-excel.sheet.macroenabled.12", "xlsm");
			extByMIME.put("application/vnd.ms-excel.template.macroenabled.12", "xltm");
			extByMIME.put("application/vnd.ms-fontobject", "eot");
			extByMIME.put("application/vnd.ms-htmlhelp", "chm");
			extByMIME.put("application/vnd.ms-ims", "ims");
			extByMIME.put("application/vnd.ms-lrm", "lrm");
			extByMIME.put("application/vnd.ms-officetheme", "thmx");
			extByMIME.put("application/vnd.ms-pki.seccat", "cat");
			extByMIME.put("application/vnd.ms-pki.stl", "stl");
			extByMIME.put("application/vnd.ms-powerpoint", "ppt pps pot");
			extByMIME.put("application/vnd.ms-powerpoint.addin.macroenabled.12", "ppam");
			extByMIME.put("application/vnd.ms-powerpoint.presentation.macroenabled.12", "pptm");
			extByMIME.put("application/vnd.ms-powerpoint.slide.macroenabled.12", "sldm");
			extByMIME.put("application/vnd.ms-powerpoint.slideshow.macroenabled.12", "ppsm");
			extByMIME.put("application/vnd.ms-powerpoint.template.macroenabled.12", "potm");
			extByMIME.put("application/vnd.ms-project", "mpp mpt");
			extByMIME.put("application/vnd.ms-word.document.macroenabled.12", "docm");
			extByMIME.put("application/vnd.ms-word.template.macroenabled.12", "dotm");
			extByMIME.put("application/vnd.ms-works", "wps wks wcm wdb");
			extByMIME.put("application/vnd.ms-wpl", "wpl");
			extByMIME.put("application/vnd.ms-xpsdocument", "xps");
			extByMIME.put("application/vnd.mseq", "mseq");
			extByMIME.put("application/vnd.musician", "mus");
			extByMIME.put("application/vnd.muvee.style", "msty");
			extByMIME.put("application/vnd.mynfc", "taglet");
			extByMIME.put("application/vnd.neurolanguage.nlu", "nlu");
			extByMIME.put("application/vnd.nitf", "ntf nitf");
			extByMIME.put("application/vnd.noblenet-directory", "nnd");
			extByMIME.put("application/vnd.noblenet-sealer", "nns");
			extByMIME.put("application/vnd.noblenet-web", "nnw");
			extByMIME.put("application/vnd.nokia.n-gage.data", "ngdat");
			extByMIME.put("application/vnd.nokia.n-gage.symbian.install", "n-gage");
			extByMIME.put("application/vnd.nokia.radio-preset", "rpst");
			extByMIME.put("application/vnd.nokia.radio-presets", "rpss");
			extByMIME.put("application/vnd.novadigm.edm", "edm");
			extByMIME.put("application/vnd.novadigm.edx", "edx");
			extByMIME.put("application/vnd.novadigm.ext", "ext");
			extByMIME.put("application/vnd.oasis.opendocument.chart", "odc");
			extByMIME.put("application/vnd.oasis.opendocument.chart-template", "otc");
			extByMIME.put("application/vnd.oasis.opendocument.database", "odb");
			extByMIME.put("application/vnd.oasis.opendocument.formula", "odf");
			extByMIME.put("application/vnd.oasis.opendocument.formula-template", "odft");
			extByMIME.put("application/vnd.oasis.opendocument.graphics", "odg");
			extByMIME.put("application/vnd.oasis.opendocument.graphics-template", "otg");
			extByMIME.put("application/vnd.oasis.opendocument.image", "odi");
			extByMIME.put("application/vnd.oasis.opendocument.image-template", "oti");
			extByMIME.put("application/vnd.oasis.opendocument.presentation", "odp");
			extByMIME.put("application/vnd.oasis.opendocument.presentation-template", "otp");
			extByMIME.put("application/vnd.oasis.opendocument.spreadsheet", "ods");
			extByMIME.put("application/vnd.oasis.opendocument.spreadsheet-template", "ots");
			extByMIME.put("application/vnd.oasis.opendocument.text", "odt");
			extByMIME.put("application/vnd.oasis.opendocument.text-master", "odm");
			extByMIME.put("application/vnd.oasis.opendocument.text-template", "ott");
			extByMIME.put("application/vnd.oasis.opendocument.text-web", "oth");
			extByMIME.put("application/vnd.olpc-sugar", "xo");
			extByMIME.put("application/vnd.oma.dd2+xml", "dd2");
			extByMIME.put("application/vnd.openofficeorg.extension", "oxt");
			extByMIME.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
			extByMIME.put("application/vnd.openxmlformats-officedocument.presentationml.slide", "sldx");
			extByMIME.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx");
			extByMIME.put("application/vnd.openxmlformats-officedocument.presentationml.template", "potx");
			extByMIME.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
			extByMIME.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx");
			extByMIME.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
			extByMIME.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx");
			extByMIME.put("application/vnd.osgeo.mapguide.package", "mgp");
			extByMIME.put("application/vnd.osgi.dp", "dp");
			extByMIME.put("application/vnd.osgi.subsystem", "esa");
			extByMIME.put("application/vnd.palm", "pdb pqa oprc");
			extByMIME.put("application/vnd.pawaafile", "paw");
			extByMIME.put("application/vnd.pg.format", "str");
			extByMIME.put("application/vnd.pg.osasli", "ei6");
			extByMIME.put("application/vnd.picsel", "efif");
			extByMIME.put("application/vnd.pmi.widget", "wg");
			extByMIME.put("application/vnd.pocketlearn", "plf");
			extByMIME.put("application/vnd.powerbuilder6", "pbd");
			extByMIME.put("application/vnd.previewsystems.box", "box");
			extByMIME.put("application/vnd.proteus.magazine", "mgz");
			extByMIME.put("application/vnd.publishare-delta-tree", "qps");
			extByMIME.put("application/vnd.pvi.ptid1", "ptid");
			extByMIME.put("application/vnd.quark.quarkxpress", "qxd qxt qwd qwt qxl qxb");
			extByMIME.put("application/vnd.realvnc.bed", "bed");
			extByMIME.put("application/vnd.recordare.musicxml", "mxl");
			extByMIME.put("application/vnd.recordare.musicxml+xml", "musicxml");
			extByMIME.put("application/vnd.rig.cryptonote", "cryptonote");
			extByMIME.put("application/vnd.rim.cod", "cod");
			extByMIME.put("application/vnd.rn-realmedia", "rm");
			extByMIME.put("application/vnd.rn-realmedia-vbr", "rmvb");
			extByMIME.put("application/vnd.route66.link66+xml", "link66");
			extByMIME.put("application/vnd.sailingtracker.track", "st");
			extByMIME.put("application/vnd.seemail", "see");
			extByMIME.put("application/vnd.sema", "sema");
			extByMIME.put("application/vnd.semd", "semd");
			extByMIME.put("application/vnd.semf", "semf");
			extByMIME.put("application/vnd.shana.informed.formdata", "ifm");
			extByMIME.put("application/vnd.shana.informed.formtemplate", "itp");
			extByMIME.put("application/vnd.shana.informed.interchange", "iif");
			extByMIME.put("application/vnd.shana.informed.package", "ipk");
			extByMIME.put("application/vnd.simtech-mindmapper", "twd twds");
			extByMIME.put("application/vnd.smaf", "mmf");
			extByMIME.put("application/vnd.smart.teacher", "teacher");
			extByMIME.put("application/vnd.solent.sdkm+xml", "sdkm sdkd");
			extByMIME.put("application/vnd.spotfire.dxp", "dxp");
			extByMIME.put("application/vnd.spotfire.sfs", "sfs");
			extByMIME.put("application/vnd.stardivision.calc", "sdc");
			extByMIME.put("application/vnd.stardivision.draw", "sda");
			extByMIME.put("application/vnd.stardivision.impress", "sdd");
			extByMIME.put("application/vnd.stardivision.math", "smf");
			extByMIME.put("application/vnd.stardivision.writer", "sdw vor");
			extByMIME.put("application/vnd.stardivision.writer-global", "sgl");
			extByMIME.put("application/vnd.stepmania.package", "smzip");
			extByMIME.put("application/vnd.stepmania.stepchart", "sm");
			extByMIME.put("application/vnd.sun.xml.calc", "sxc");
			extByMIME.put("application/vnd.sun.xml.calc.template", "stc");
			extByMIME.put("application/vnd.sun.xml.draw", "sxd");
			extByMIME.put("application/vnd.sun.xml.draw.template", "std");
			extByMIME.put("application/vnd.sun.xml.impress", "sxi");
			extByMIME.put("application/vnd.sun.xml.impress.template", "sti");
			extByMIME.put("application/vnd.sun.xml.math", "sxm");
			extByMIME.put("application/vnd.sun.xml.writer", "sxw");
			extByMIME.put("application/vnd.sun.xml.writer.global", "sxg");
			extByMIME.put("application/vnd.sun.xml.writer.template", "stw");
			extByMIME.put("application/vnd.sus-calendar", "sus susp");
			extByMIME.put("application/vnd.svd", "svd");
			extByMIME.put("application/vnd.symbian.install", "sis sisx");
			extByMIME.put("application/vnd.syncml+xml", "xsm");
			extByMIME.put("application/vnd.syncml.dm+wbxml", "bdm");
			extByMIME.put("application/vnd.syncml.dm+xml", "xdm");
			extByMIME.put("application/vnd.tao.intent-module-archive", "tao");
			extByMIME.put("application/vnd.tcpdump.pcap", "pcap cap dmp");
			extByMIME.put("application/vnd.tmobile-livetv", "tmo");
			extByMIME.put("application/vnd.trid.tpt", "tpt");
			extByMIME.put("application/vnd.triscape.mxs", "mxs");
			extByMIME.put("application/vnd.trueapp", "tra");
			extByMIME.put("application/vnd.ufdl", "ufd ufdl");
			extByMIME.put("application/vnd.uiq.theme", "utz");
			extByMIME.put("application/vnd.umajin", "umj");
			extByMIME.put("application/vnd.unity", "unityweb");
			extByMIME.put("application/vnd.uoml+xml", "uoml");
			extByMIME.put("application/vnd.vcx", "vcx");
			extByMIME.put("application/vnd.visio", "vsd vst vss vsw");
			extByMIME.put("application/vnd.visionary", "vis");
			extByMIME.put("application/vnd.vsf", "vsf");
			extByMIME.put("application/vnd.wap.wbxml", "wbxml");
			extByMIME.put("application/vnd.wap.wmlc", "wmlc");
			extByMIME.put("application/vnd.wap.wmlscriptc", "wmlsc");
			extByMIME.put("application/vnd.webturbo", "wtb");
			extByMIME.put("application/vnd.wolfram.player", "nbp");
			extByMIME.put("application/vnd.wordperfect", "wpd");
			extByMIME.put("application/vnd.wqd", "wqd");
			extByMIME.put("application/vnd.wt.stf", "stf");
			extByMIME.put("application/vnd.xara", "xar");
			extByMIME.put("application/vnd.xfdl", "xfdl");
			extByMIME.put("application/vnd.yamaha.hv-dic", "hvd");
			extByMIME.put("application/vnd.yamaha.hv-script", "hvs");
			extByMIME.put("application/vnd.yamaha.hv-voice", "hvp");
			extByMIME.put("application/vnd.yamaha.openscoreformat", "osf");
			extByMIME.put("application/vnd.yamaha.openscoreformat.osfpvg+xml", "osfpvg");
			extByMIME.put("application/vnd.yamaha.smaf-audio", "saf");
			extByMIME.put("application/vnd.yamaha.smaf-phrase", "spf");
			extByMIME.put("application/vnd.yellowriver-custom-menu", "cmp");
			extByMIME.put("application/vnd.zul", "zir zirz");
			extByMIME.put("application/vnd.zzazz.deck+xml", "zaz");
			extByMIME.put("application/voicexml+xml", "vxml");
			extByMIME.put("application/widget", "wgt");
			extByMIME.put("application/winhlp", "hlp");
			extByMIME.put("application/wsdl+xml", "wsdl");
			extByMIME.put("application/wspolicy+xml", "wspolicy");
			extByMIME.put("application/x-7z-compressed", "7z");
			extByMIME.put("application/x-abiword", "abw");
			extByMIME.put("application/x-ace-compressed", "ace");
			extByMIME.put("application/x-apple-diskimage", "dmg");
			extByMIME.put("application/x-authorware-bin", "aab x32 u32 vox");
			extByMIME.put("application/x-authorware-map", "aam");
			extByMIME.put("application/x-authorware-seg", "aas");
			extByMIME.put("application/x-bcpio", "bcpio");
			extByMIME.put("application/x-bittorrent", "torrent");
			extByMIME.put("application/x-blorb", "blb blorb");
			extByMIME.put("application/x-bzip", "bz");
			extByMIME.put("application/x-bzip2", "bz2 boz");
			extByMIME.put("application/x-cbr", "cbr cba cbt cbz cb7");
			extByMIME.put("application/x-cdlink", "vcd");
			extByMIME.put("application/x-cfs-compressed", "cfs");
			extByMIME.put("application/x-chat", "chat");
			extByMIME.put("application/x-chess-pgn", "pgn");
			extByMIME.put("application/x-conference", "nsc");
			extByMIME.put("application/x-cpio", "cpio");
			extByMIME.put("application/x-csh", "csh");
			extByMIME.put("application/x-debian-package", "deb udeb");
			extByMIME.put("application/x-dgc-compressed", "dgc");
			extByMIME.put("application/x-director", "dir dcr dxr cst cct cxt w3d fgd swa");
			extByMIME.put("application/x-doom", "wad");
			extByMIME.put("application/x-dtbncx+xml", "ncx");
			extByMIME.put("application/x-dtbook+xml", "dtb");
			extByMIME.put("application/x-dtbresource+xml", "res");
			extByMIME.put("application/x-dvi", "dvi");
			extByMIME.put("application/x-envoy", "evy");
			extByMIME.put("application/x-eva", "eva");
			extByMIME.put("application/x-font-bdf", "bdf");
			extByMIME.put("application/x-font-ghostscript", "gsf");
			extByMIME.put("application/x-font-linux-psf", "psf");
			extByMIME.put("application/x-font-otf", "otf");
			extByMIME.put("application/x-font-pcf", "pcf");
			extByMIME.put("application/x-font-snf", "snf");
			extByMIME.put("application/x-font-ttf", "ttf ttc");
			extByMIME.put("application/x-font-type1", "pfa pfb pfm afm");
			extByMIME.put("application/x-freearc", "arc");
			extByMIME.put("application/x-futuresplash", "spl");
			extByMIME.put("application/x-gca-compressed", "gca");
			extByMIME.put("application/x-glulx", "ulx");
			extByMIME.put("application/x-gnumeric", "gnumeric");
			extByMIME.put("application/x-gramps-xml", "gramps");
			extByMIME.put("application/x-gtar", "gtar");
			extByMIME.put("application/x-hdf", "hdf");
			extByMIME.put("application/x-install-instructions", "install");
			extByMIME.put("application/x-iso966 -image", "iso");
			extByMIME.put("application/x-java-jnlp-file", "jnlp");
			extByMIME.put("application/x-latex", "latex");
			extByMIME.put("application/x-lzh-compressed", "lzh lha");
			extByMIME.put("application/x-mie", "mie");
			extByMIME.put("application/x-mobipocket-ebook", "prc mobi");
			extByMIME.put("application/x-ms-application", "application");
			extByMIME.put("application/x-ms-shortcut", "lnk");
			extByMIME.put("application/x-ms-wmd", "wmd");
			extByMIME.put("application/x-ms-wmz", "wmz");
			extByMIME.put("application/x-ms-xbap", "xbap");
			extByMIME.put("application/x-msaccess", "mdb");
			extByMIME.put("application/x-msbinder", "obd");
			extByMIME.put("application/x-mscardfile", "crd");
			extByMIME.put("application/x-msclip", "clp");
			extByMIME.put("application/x-msdownload", "exe dll com bat msi");
			extByMIME.put("application/x-msmediaview", "mvb m13 m14");
			extByMIME.put("application/x-msmetafile", "wmf wmz emf emz");
			extByMIME.put("application/x-msmoney", "mny");
			extByMIME.put("application/x-mspublisher", "pub");
			extByMIME.put("application/x-msschedule", "scd");
			extByMIME.put("application/x-msterminal", "trm");
			extByMIME.put("application/x-mswrite", "wri");
			extByMIME.put("application/x-netcdf", "nc cdf");
			extByMIME.put("application/x-nzb", "nzb");
			extByMIME.put("application/x-pkcs12", "p12 pfx");
			extByMIME.put("application/x-pkcs7-certificates", "p7b spc");
			extByMIME.put("application/x-pkcs7-certreqresp", "p7r");
			extByMIME.put("application/x-rar-compressed", "rar");
			extByMIME.put("application/x-research-info-systems", "ris");
			extByMIME.put("application/x-sh", "sh");
			extByMIME.put("application/x-shar", "shar");
			extByMIME.put("application/x-shockwave-flash", "swf");
			extByMIME.put("application/x-silverlight-app", "xap");
			extByMIME.put("application/x-sql", "sql");
			extByMIME.put("application/x-stuffit", "sit");
			extByMIME.put("application/x-stuffitx", "sitx");
			extByMIME.put("application/x-subrip", "srt");
			extByMIME.put("application/x-sv4cpio", "sv4cpio");
			extByMIME.put("application/x-sv4crc", "sv4crc");
			extByMIME.put("application/x-t3vm-image", "t3");
			extByMIME.put("application/x-tads", "gam");
			extByMIME.put("application/x-tar", "tar");
			extByMIME.put("application/x-tcl", "tcl");
			extByMIME.put("application/x-tex", "tex");
			extByMIME.put("application/x-tex-tfm", "tfm");
			extByMIME.put("application/x-texinfo", "texinfo texi");
			extByMIME.put("application/x-tgif", "obj");
			extByMIME.put("application/x-ustar", "ustar");
			extByMIME.put("application/x-wais-source", "src");
			extByMIME.put("application/x-x5 9-ca-cert", "der crt");
			extByMIME.put("application/x-xfig", "fig");
			extByMIME.put("application/x-xliff+xml", "xlf");
			extByMIME.put("application/x-xpinstall", "xpi");
			extByMIME.put("application/x-xz", "xz");
			extByMIME.put("application/x-zmachine", "z1 z2 z3 z4 z5 z6 z7 z8");
			extByMIME.put("application/xaml+xml", "xaml");
			extByMIME.put("application/xcap-diff+xml", "xdf");
			extByMIME.put("application/xenc+xml", "xenc");
			extByMIME.put("application/xhtml+xml", "xhtml xht");
			extByMIME.put("application/xml", "xml xsl");
			extByMIME.put("application/xml-dtd", "dtd");
			extByMIME.put("application/xop+xml", "xop");
			extByMIME.put("application/xproc+xml", "xpl");
			extByMIME.put("application/xslt+xml", "xslt");
			extByMIME.put("application/xspf+xml", "xspf");
			extByMIME.put("application/xv+xml", "mxml xhvml xvml xvm");
			extByMIME.put("application/yang", "yang");
			extByMIME.put("application/yin+xml", "yin");
			extByMIME.put("application/zip", "zip");
			extByMIME.put("audio/adpcm", "adp");
			extByMIME.put("audio/basic", "au snd");
			extByMIME.put("audio/midi", "mid midi kar rmi");
			extByMIME.put("audio/mp4", "m4a mp4a");
			extByMIME.put("audio/mpeg", "mpga mp2 mp2a mp3 m2a m3a");
			extByMIME.put("audio/ogg", "oga ogg spx");
			extByMIME.put("audio/s3m", "s3m");
			extByMIME.put("audio/silk", "sil");
			extByMIME.put("audio/vnd.dece.audio", "uva uvva");
			extByMIME.put("audio/vnd.digital-winds", "eol");
			extByMIME.put("audio/vnd.dra", "dra");
			extByMIME.put("audio/vnd.dts", "dts");
			extByMIME.put("audio/vnd.dts.hd", "dtshd");
			extByMIME.put("audio/vnd.lucent.voice", "lvp");
			extByMIME.put("audio/vnd.ms-playready.media.pya", "pya");
			extByMIME.put("audio/vnd.nuera.ecelp48  ", "ecelp48  ");
			extByMIME.put("audio/vnd.nuera.ecelp747 ", "ecelp747 ");
			extByMIME.put("audio/vnd.nuera.ecelp96  ", "ecelp96  ");
			extByMIME.put("audio/vnd.rip", "rip");
			extByMIME.put("audio/webm", "weba");
			extByMIME.put("audio/x-aac", "aac");
			extByMIME.put("audio/x-aiff", "aif aiff aifc");
			extByMIME.put("audio/x-caf", "caf");
			extByMIME.put("audio/x-flac", "flac");
			extByMIME.put("audio/x-matroska", "mka");
			extByMIME.put("audio/x-mpegurl", "m3u");
			extByMIME.put("audio/x-ms-wax", "wax");
			extByMIME.put("audio/x-ms-wma", "wma");
			extByMIME.put("audio/x-pn-realaudio", "ram ra");
			extByMIME.put("audio/x-pn-realaudio-plugin", "rmp");
			extByMIME.put("audio/x-wav", "wav");
			extByMIME.put("audio/xm", "xm");
			extByMIME.put("chemical/x-cdx", "cdx");
			extByMIME.put("chemical/x-cif", "cif");
			extByMIME.put("chemical/x-cmdf", "cmdf");
			extByMIME.put("chemical/x-cml", "cml");
			extByMIME.put("chemical/x-csml", "csml");
			extByMIME.put("chemical/x-xyz", "xyz");
			extByMIME.put("image/bmp", "bmp");
			extByMIME.put("image/cgm", "cgm");
			extByMIME.put("image/g3fax", "g3");
			extByMIME.put("image/gif", "gif");
			extByMIME.put("image/ief", "ief");
			extByMIME.put("image/jpeg", "jpeg jpg jpe");
			extByMIME.put("image/ktx", "ktx");
			extByMIME.put("image/png", "png");
			extByMIME.put("image/prs.btif", "btif");
			extByMIME.put("image/sgi", "sgi");
			extByMIME.put("image/svg+xml", "svg svgz");
			extByMIME.put("image/tiff", "tiff tif");
			extByMIME.put("image/vnd.adobe.photoshop", "psd");
			extByMIME.put("image/vnd.dece.graphic", "uvi uvvi uvg uvvg");
			extByMIME.put("image/vnd.djvu", "djvu djv");
			extByMIME.put("image/vnd.dvb.subtitle", "sub");
			extByMIME.put("image/vnd.dwg", "dwg");
			extByMIME.put("image/vnd.dxf", "dxf");
			extByMIME.put("image/vnd.fastbidsheet", "fbs");
			extByMIME.put("image/vnd.fpx", "fpx");
			extByMIME.put("image/vnd.fst", "fst");
			extByMIME.put("image/vnd.fujixerox.edmics-mmr", "mmr");
			extByMIME.put("image/vnd.fujixerox.edmics-rlc", "rlc");
			extByMIME.put("image/vnd.ms-modi", "mdi");
			extByMIME.put("image/vnd.ms-photo", "wdp");
			extByMIME.put("image/vnd.net-fpx", "npx");
			extByMIME.put("image/vnd.wap.wbmp", "wbmp");
			extByMIME.put("image/vnd.xiff", "xif");
			extByMIME.put("image/webp", "webp");
			extByMIME.put("image/x-3ds", "3ds");
			extByMIME.put("image/x-cmu-raster", "ras");
			extByMIME.put("image/x-cmx", "cmx");
			extByMIME.put("image/x-freehand", "fh fhc fh4 fh5 fh7");
			extByMIME.put("image/x-icon", "ico");
			extByMIME.put("image/x-mrsid-image", "sid");
			extByMIME.put("image/x-pcx", "pcx");
			extByMIME.put("image/x-pict", "pic pct");
			extByMIME.put("image/x-portable-anymap", "pnm");
			extByMIME.put("image/x-portable-bitmap", "pbm");
			extByMIME.put("image/x-portable-graymap", "pgm");
			extByMIME.put("image/x-portable-pixmap", "ppm");
			extByMIME.put("image/x-rgb", "rgb");
			extByMIME.put("image/x-tga", "tga");
			extByMIME.put("image/x-xbitmap", "xbm");
			extByMIME.put("image/x-xpixmap", "xpm");
			extByMIME.put("image/x-xwindowdump", "xwd");
			extByMIME.put("message/rfc822", "eml mime");
			extByMIME.put("model/iges", "igs iges");
			extByMIME.put("model/mesh", "msh mesh silo");
			extByMIME.put("model/vnd.collada+xml", "dae");
			extByMIME.put("model/vnd.dwf", "dwf");
			extByMIME.put("model/vnd.gdl", "gdl");
			extByMIME.put("model/vnd.gtw", "gtw");
			extByMIME.put("model/vnd.mts", "mts");
			extByMIME.put("model/vnd.vtu", "vtu");
			extByMIME.put("model/vrml", "wrl vrml");
			extByMIME.put("model/x3d+binary", "x3db x3dbz");
			extByMIME.put("model/x3d+vrml", "x3dv x3dvz");
			extByMIME.put("model/x3d+xml", "x3d x3dz");
			extByMIME.put("text/cache-manifest", "appcache");
			extByMIME.put("text/calendar", "ics ifb");
			extByMIME.put("text/css", "css");
			extByMIME.put("text/csv", "csv");
			extByMIME.put("text/html", "html htm");
			extByMIME.put("text/n3", "n3");
			extByMIME.put("text/plain", "txt text conf def list log in");
			extByMIME.put("text/prs.lines.tag", "dsc");
			extByMIME.put("text/richtext", "rtx");
			extByMIME.put("text/sgml", "sgml sgm");
			extByMIME.put("text/tab-separated-values", "tsv");
			extByMIME.put("text/troff", "t tr roff man me ms");
			extByMIME.put("text/turtle", "ttl");
			extByMIME.put("text/uri-list", "uri uris urls");
			extByMIME.put("text/vcard", "vcard");
			extByMIME.put("text/vnd.curl", "curl");
			extByMIME.put("text/vnd.curl.dcurl", "dcurl");
			extByMIME.put("text/vnd.curl.mcurl", "mcurl");
			extByMIME.put("text/vnd.curl.scurl", "scurl");
			extByMIME.put("text/vnd.dvb.subtitle", "sub");
			extByMIME.put("text/vnd.fly", "fly");
			extByMIME.put("text/vnd.fmi.flexstor", "flx");
			extByMIME.put("text/vnd.graphviz", "gv");
			extByMIME.put("text/vnd.in3d.3dml", "3dml");
			extByMIME.put("text/vnd.in3d.spot", "spot");
			extByMIME.put("text/vnd.sun.j2me.app-descriptor", "jad");
			extByMIME.put("text/vnd.wap.wml", "wml");
			extByMIME.put("text/vnd.wap.wmlscript", "wmls");
			extByMIME.put("text/x-asm", "s asm");
			extByMIME.put("text/x-c", "c cc cxx cpp h hh dic");
			extByMIME.put("text/x-fortran", "f for f77 f9 ");
			extByMIME.put("text/x-java-source", "java");
			extByMIME.put("text/x-nfo", "nfo");
			extByMIME.put("text/x-opml", "opml");
			extByMIME.put("text/x-pascal", "p pas");
			extByMIME.put("text/x-setext", "etx");
			extByMIME.put("text/x-sfv", "sfv");
			extByMIME.put("text/x-uuencode", "uu");
			extByMIME.put("text/x-vcalendar", "vcs");
			extByMIME.put("text/x-vcard", "vcf");
			extByMIME.put("video/3gpp", "3gp");
			extByMIME.put("video/3gpp2", "3g2");
			extByMIME.put("video/h261", "h261");
			extByMIME.put("video/h263", "h263");
			extByMIME.put("video/h264", "h264");
			extByMIME.put("video/jpeg", "jpgv");
			extByMIME.put("video/jpm", "jpm jpgm");
			extByMIME.put("video/mj2", "mj2 mjp2");
			extByMIME.put("video/mp4", "mp4 mp4v mpg4");
			extByMIME.put("video/mpeg", "mpeg mpg mpe m1v m2v");
			extByMIME.put("video/ogg", "ogv");
			extByMIME.put("video/quicktime", "qt mov");
			extByMIME.put("video/vnd.dece.hd", "uvh uvvh");
			extByMIME.put("video/vnd.dece.mobile", "uvm uvvm");
			extByMIME.put("video/vnd.dece.pd", "uvp uvvp");
			extByMIME.put("video/vnd.dece.sd", "uvs uvvs");
			extByMIME.put("video/vnd.dece.video", "uvv uvvv");
			extByMIME.put("video/vnd.dvb.file", "dvb");
			extByMIME.put("video/vnd.fvt", "fvt");
			extByMIME.put("video/vnd.mpegurl", "mxu m4u");
			extByMIME.put("video/vnd.ms-playready.media.pyv", "pyv");
			extByMIME.put("video/vnd.uvvu.mp4", "uvu uvvu");
			extByMIME.put("video/vnd.vivo", "viv");
			extByMIME.put("video/webm", "webm");
			extByMIME.put("video/x-f4v", "f4v");
			extByMIME.put("video/x-fli", "fli");
			extByMIME.put("video/x-flv", "flv");
			extByMIME.put("video/x-m4v", "m4v");
			extByMIME.put("video/x-matroska", "mkv mk3d mks");
			extByMIME.put("video/x-mng", "mng");
			extByMIME.put("video/x-ms-asf", "asf asx");
			extByMIME.put("video/x-ms-vob", "vob");
			extByMIME.put("video/x-ms-wm", "wm");
			extByMIME.put("video/x-ms-wmv", "wmv");
			extByMIME.put("video/x-ms-wmx", "wmx");
			extByMIME.put("video/x-ms-wvx", "wvx");
			extByMIME.put("video/x-msvideo", "avi");
			extByMIME.put("video/x-sgi-movie", "movie");
			extByMIME.put("video/x-smv", "smv");
			extByMIME.put("x-conference/x-cooltalk", "ice");
		}
		return extByMIME;
	}


}
