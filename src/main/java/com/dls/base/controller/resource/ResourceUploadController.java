package com.dls.base.controller.resource;

import com.dls.base.entity.*;
import com.dls.base.file.ResourceContainer;
import com.dls.base.repository.*;
import com.dls.base.utils.Utils;
import com.dls.base.validator.Message;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@RestController
public class ResourceUploadController {

    @Autowired
    ResourceRepository resourceRepository;

    @Autowired
    Utils utils;

    @Autowired
    TemplateLessonRepository templateLessonRepository;
    @Autowired
    TemplateTestQuestionRepository templateTestQuestionRepository;

    @PostMapping(value = "/upload/{entityClass}/{entityId}/{position}", consumes = {"multipart/form-data"}, produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Message upload(@PathVariable String entityClass, @PathVariable long entityId, @PathVariable long position, @RequestParam("fileUploadResource") MultipartFile multipart) throws Exception {
        if (!isDraft(entityClass, entityId)){
            throw new Exception("incorrent status of [class = " + entityClass + ", id = " + entityId + "] for upload");
        }
        Resource resource = new Resource();
        resource.setEntityClass(entityClass.toLowerCase());
        resource.setEntityId(entityId);
        resource.setPosition(position);
        resource.setName(multipart.getOriginalFilename());
        Tika tika = new Tika();
        String newResourceFilePath = null;
        try {
            resource.setExtension(utils.getExtensionByMIMEMap().get(tika.detect(multipart.getBytes())));
            resource.setType(tika.detect(multipart.getBytes()));
            String newResourceDirName = utils.getNewResourceDirName();
            File newResourceDir = new File(newResourceDirName);
            if (!newResourceDir.exists()){
                if (!newResourceDir.mkdirs()){
                    throw new ResourceNotLoadedException(newResourceDir.getAbsolutePath());
                }
            }
            String newResourceFileName = utils.getNewResourceFileName();
            newResourceFilePath = newResourceDirName + File.separatorChar + newResourceFileName;
            File newResourceFile = new File(newResourceFilePath);
            newResourceFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(newResourceFile);
            try {
                stream.write(multipart.getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resource.setValue(newResourceFilePath);
        resourceRepository.save(resource);
        return new Message(resource.getId().toString());
    }

    @PostMapping(value = "/uploadText/{entityClass}/{entityId}/{position}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Message uploadText(@PathVariable String entityClass, @PathVariable long entityId, @PathVariable long position, @RequestBody String body) throws Exception {
        if (!isDraft(entityClass, entityId)){
            throw new Exception("incorrent status of [class = " + entityClass + ", id = " + entityId + "] for upload");
        }
        Resource resource = new Resource();
        resource.setEntityClass(entityClass.toLowerCase());
        resource.setEntityId(entityId);
        resource.setPosition(position);
        resource.setName("Текстовый ресурс");
        resource.setExtension("txt");
        resource.setType("text/plain");
        String newResourceFilePath = null;
        try {
            String newResourceDirName = utils.getNewResourceDirName();
            File newResourceDir = new File(newResourceDirName);
            if (!newResourceDir.exists()){
                if (!newResourceDir.mkdirs()){
                    throw new ResourceNotLoadedException(newResourceDir.getAbsolutePath());
                }
            }
            String newResourceFileName = utils.getNewResourceFileName();
            newResourceFilePath = newResourceDirName + File.separatorChar + newResourceFileName;
            File newResourceFile = new File(newResourceFilePath);
            newResourceFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(newResourceFile);
            try {
                stream.write(body.getBytes(StandardCharsets.UTF_8));
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resource.setValue(newResourceFilePath);
        Resource savedresource = resourceRepository.save(resource);
        return new Message(savedresource.getId().toString());
    }

    @PostMapping(value = "/upload/updatePosition/{entityResourceId}/{position}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Message update(@PathVariable long entityResourceId, @PathVariable long position) throws Exception {
        Resource resource = resourceRepository.findByFileId(entityResourceId);
        if (!isDraft(resource.getEntityClass(), resource.getEntityId())){
            throw new Exception("incorrent status of [class = " + resource.getEntityClass() + ", id = " + resource.getEntityId() + "] for upload");
        }
        resource.setPosition(position);
        Resource savedresource = resourceRepository.save(resource);
        return new Message(savedresource.getId().toString());
    }

    @PostMapping(value = "/upload/updateText/{entityResourceId}/{position}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Message updateText(@PathVariable long entityResourceId, @PathVariable long position, @RequestBody String body) throws Exception {
        Resource resource = resourceRepository.findByFileId(entityResourceId);
        if (!isDraft(resource.getEntityClass(), resource.getEntityId())){
            throw new Exception("incorrent status of [class = " + resource.getEntityClass() + ", id = " + resource.getEntityId() + "] for upload");
        }
        resource.setPosition(position);
        try {
            File resourceFilePath = new File(resource.getValue());
            if (!resourceFilePath.exists()){
                throw new ResourceNotLoadedException(resourceFilePath.getAbsolutePath());
            }
            FileOutputStream stream = new FileOutputStream(resourceFilePath);
            try {
                stream.write(body.getBytes(StandardCharsets.UTF_8));
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Resource savedresource = resourceRepository.save(resource);
        return new Message(savedresource.getId().toString());
    }

    @PostMapping(value = "/upload/syncFiles/{entityClass}/{entityId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity syncFiles(@PathVariable String entityClass, @PathVariable long entityId, @RequestBody String [] unidArray) throws Exception {
        if (!isDraft(entityClass, entityId)){
            throw new Exception("incorrent status of [class = " + entityClass + ", id = " + entityId + "] for upload");
        }
        Set<Resource> resources = resourceRepository.findByEntityClassAndEntityId(entityClass, entityId);
        for (Resource resource : resources){
            if (!Arrays.asList(unidArray).contains(resource.getId().toString())){
                resourceRepository.delete(resource);
            }
        }
        return ResponseEntity
                .status(HttpStatus.OK).build();
    }

    @GetMapping(value = "/upload/{entityClass}/{entityId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ArrayList<ResourceContainer> getResourceIDs(@PathVariable String entityClass, @PathVariable long entityId) {
        Set<Resource> resources = resourceRepository.findByEntityClassAndEntityId(entityClass, entityId);
        HashMap<Long, ResourceContainer> positionIds = new HashMap<Long, ResourceContainer>();
        for (Resource resource : resources)
        {
            ResourceContainer resourceContainer = new ResourceContainer();
            resourceContainer.position = resource.getPosition();
            resourceContainer.id = resource.getId();
            resourceContainer.type = resource.getType();
            resourceContainer.name = resource.getName();
            resourceContainer.extension = resource.getExtension();
            resourceContainer.value = resource.getId().toString();
            positionIds.put(resource.getPosition(), resourceContainer);
        }
        ArrayList<ResourceContainer> arrayList = new ArrayList();
        Map<Long, ResourceContainer> map = new TreeMap<Long, ResourceContainer>(positionIds);
        Iterator iterator = map.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            arrayList.add((ResourceContainer)entry.getValue());
        }
        return arrayList;
    }

    @GetMapping(value = "/upload/{id}")
    public ResponseEntity<byte[]> load(@PathVariable long id) {
        HttpHeaders headers = new HttpHeaders();
        Resource resource = resourceRepository.findByFileId(id);
        File file = new File(resource.getValue());
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Tika tika = new Tika();
        headers.setContentType(MediaType.valueOf(tika.detect(data)));
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        headers.setContentDisposition(ContentDisposition.parse("attachment; filename=" + resource.getName()));
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(data, headers, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping(value = "/uploadText/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Message loadText(@PathVariable long id) {
        HttpHeaders headers = new HttpHeaders();
        Resource resource = resourceRepository.findByFileId(id);
        File file = new File(resource.getValue());
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = null;
        try {
            result = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new Message(result);
    }

    @GetMapping(value = "/uploadName/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Message getName(@PathVariable long id) {
        Resource resource = resourceRepository.findByFileId(id);
        return new Message(resource.getName());
    }

    @GetMapping(value = "/uploadExtension/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Message getExtension(@PathVariable long id) {
        Resource resource = resourceRepository.findByFileId(id);
        return new Message(resource.getExtension());
    }

    private boolean isDraft(String entityClass, long entityId) throws Exception {
        switch (entityClass){
            case "templatelesson":
                TemplateLesson templateLesson = templateLessonRepository.findByTemplateLessonId(entityId);
                if (templateLesson != null){
                    return (templateLesson.getStatus().getCode().equals("draft"));
                }else{
                    new Exception("Can't found entity [class = " + entityClass + ",  id = " + entityId);
                }
                break;
            case "templatetestquestion":
                TemplateTestQuestion templateTestQuestion = templateTestQuestionRepository.findByTemplateTestQuestionId(entityId);
                if (templateTestQuestion != null){
                    return (templateTestQuestion.getTemplateTestVariant().getTemplateTest().getStatus().getCode().equals("draft"));
                }else{
                    new Exception("Can't found entity [class = " + entityClass + ",  id = " + entityId);
                }
                break;
            default:
                throw new Exception("unsupported entity class");
        }
        return false;
    }

}