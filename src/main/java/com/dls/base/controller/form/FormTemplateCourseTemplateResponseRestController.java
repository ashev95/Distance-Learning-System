package com.dls.base.controller.form;

import com.dls.base.entity.Block;
import com.dls.base.entity.TemplateCourse;
import com.dls.base.entity.TemplateCourseTemplateResponse;
import com.dls.base.repository.BlockRepository;
import com.dls.base.repository.TemplateCourseRepository;
import com.dls.base.repository.TemplateCourseTemplateResponseRepository;
import com.dls.base.request.TemplateCourseTemplateResponseContainer;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
public class FormTemplateCourseTemplateResponseRestController
{

	private final TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository;
	private final TemplateCourseRepository templateCourseRepository;
	private final BlockRepository blockRepository;

	@Autowired
	FormTemplateCourseTemplateResponseRestController(TemplateCourseTemplateResponseRepository templateCourseTemplateResponseRepository, TemplateCourseRepository templateCourseRepository, BlockRepository blockRepository) {
		this.templateCourseTemplateResponseRepository = templateCourseTemplateResponseRepository;
		this.templateCourseRepository = templateCourseRepository;
		this.blockRepository = blockRepository;
	}

	@Autowired
	AccessUtils accessUtils;

	@PutMapping(value = "form/templatecoursetemplateresponse/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody final List<TemplateCourseTemplateResponseContainer> containerList) throws Exception {
		try{
			for (TemplateCourseTemplateResponseContainer container : containerList){
				TemplateCourse templateCourse = templateCourseRepository.findByTemplateCourseId(container.templateCourseId);
				if (templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByTemplateCourseIdAndTemplateResponseClassAndTemplateResponseId(templateCourse.getId(), container.templateResponseClass, container.templateResponseId) == null){
					Block block = blockRepository.findBlockByTemplateCourseIdAndBlockPosition(templateCourse.getId(), 1L);
					if (block == null){
						block = new Block();
						block.setParentClass(templateCourse.getClass().getSimpleName().toLowerCase());
						block.setParentId(templateCourse.getId());
						block.setPosition(1L);
						block.setType(Constant.BLOCK_TYPE_SERIAL);
						Block savedBlock = blockRepository.save(block);
						block = savedBlock;
					}
					TemplateCourseTemplateResponse templateCourseTemplateResponse = new TemplateCourseTemplateResponse();
					templateCourseTemplateResponse.setTemplateCourse(templateCourseRepository.findByTemplateCourseId(container.templateCourseId));
					templateCourseTemplateResponse.setTemplateResponseClass(container.templateResponseClass);
					templateCourseTemplateResponse.setTemplateResponseId(container.templateResponseId);
					templateCourseTemplateResponse.setPosition(templateCourseTemplateResponseRepository.findLastPositionByTemplateCourseId(templateCourse.getId()) + 1);
					templateCourseTemplateResponse.setBlock(block);
					if (!accessUtils.canEditCard(templateCourseTemplateResponse)){
						throw new Exception("Отсутствуют права на создание/изменение карточки");
					}
					TemplateCourseTemplateResponse savedTemplateCourseTemplateResponse = templateCourseTemplateResponseRepository.save(templateCourseTemplateResponse);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.build();
	}

	@PostMapping(value = "form/templatecoursetemplateresponse/{id}/to_block/{position}/to_block_type/{blockType}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateFormToBlock(@PathVariable long id, @PathVariable long position, @PathVariable byte blockType) throws Exception {
		try{
		if (position < 1){
			throw new Exception("Некорректный номер блока");
		}
		if (blockType != Constant.BLOCK_TYPE_SERIAL && blockType != Constant.BLOCK_TYPE_PARALLEL){
			throw new Exception("Некорректный тип блока");
		}
		TemplateCourseTemplateResponse updateTemplateCourseTemplateResponse = templateCourseTemplateResponseRepository.findByTemplateCourseTemplateResponseId(id);
		if (!accessUtils.canEditCard(updateTemplateCourseTemplateResponse)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!updateTemplateCourseTemplateResponse.getTemplateCourse().getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + updateTemplateCourseTemplateResponse.getTemplateCourse().getClass().getSimpleName().toLowerCase() + ", id = " + updateTemplateCourseTemplateResponse.getTemplateCourse().getId() + "]");
		}
		TemplateCourse templateCourse = templateCourseRepository.findTemplateCourseByTemplateCourseTemplateResponseId(id);

		Block block = blockRepository.findBlockByTemplateBlockTemplateCourseTemplateResponseId(id);

		if (templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByTemplateCourseIdAndBlockPosition(block.getParentId(), block.getPosition()).size() > 1){
			if (block.getPosition().equals(position)){
				block.setPosition(position);
				block.setType(blockType);
				blockRepository.save(block);
			}else{
				block = blockRepository.findBlockByTemplateCourseIdAndBlockPosition(block.getParentId(), position);
				if (block == null){
					block = new Block();
					block.setParentClass(templateCourse.getClass().getSimpleName().toLowerCase());
					block.setParentId(templateCourse.getId());
					block.setPosition(position);
				}
				block.setType(blockType);
				blockRepository.save(block);
				updateTemplateCourseTemplateResponse.setBlock(block);
				templateCourseTemplateResponseRepository.save(updateTemplateCourseTemplateResponse);
			}
		}else{
			Block oldBlock = block;
			block = blockRepository.findBlockByTemplateCourseIdAndBlockPosition(block.getParentId(), position);
			if (block == null){
				block = new Block();
				block.setParentClass(templateCourse.getClass().getSimpleName().toLowerCase());
				block.setParentId(templateCourse.getId());
				block.setPosition(position);
				block.setType(blockType);
				Block savedBlock = blockRepository.save(block);
				updateTemplateCourseTemplateResponse.setBlock(savedBlock);
				templateCourseTemplateResponseRepository.save(updateTemplateCourseTemplateResponse);
				blockRepository.delete(oldBlock);
			}else{
				block.setPosition(position);
				block.setType(blockType);
				Block savedBlock = blockRepository.save(block);
				updateTemplateCourseTemplateResponse.setBlock(savedBlock);
				templateCourseTemplateResponseRepository.save(updateTemplateCourseTemplateResponse);
				if (!oldBlock.getPosition().equals(position)){
					blockRepository.delete(oldBlock);
				}
			}
		}
	}catch (Exception e){
			e.printStackTrace();
	return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(e.getMessage());
}
		return ResponseEntity
				.status(HttpStatus.OK)
				.build();
	}

	@PostMapping(value = "form/templatecoursetemplateresponse/up/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateFormUp(@PathVariable long id) throws Exception {
		try{
		TemplateCourseTemplateResponse updateTemplateCourseTemplateResponse = templateCourseTemplateResponseRepository.findByTemplateCourseTemplateResponseId(id);
		if (!accessUtils.canEditCard(updateTemplateCourseTemplateResponse)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!updateTemplateCourseTemplateResponse.getTemplateCourse().getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + updateTemplateCourseTemplateResponse.getTemplateCourse().getClass().getSimpleName().toLowerCase() + ", id = " + updateTemplateCourseTemplateResponse.getTemplateCourse().getId() + "]");
		}
		Set<TemplateCourseTemplateResponse> templateCourseTemplateResponseSet = templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByTemplateCourseId(updateTemplateCourseTemplateResponse.getTemplateCourse().getId());
		TemplateCourseTemplateResponse templateCourseTemplateResponseTop = null;
		for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseSet){
			if (templateCourseTemplateResponseTop != null){
				if (templateCourseTemplateResponse.getId().equals(updateTemplateCourseTemplateResponse.getId())){
					break;
				}
			}
			templateCourseTemplateResponseTop = templateCourseTemplateResponse;
		}
		if (templateCourseTemplateResponseTop != null){
			Long tmpPos = templateCourseTemplateResponseTop.getPosition();
			templateCourseTemplateResponseTop.setPosition(updateTemplateCourseTemplateResponse.getPosition());
			updateTemplateCourseTemplateResponse.setPosition(tmpPos);
			TemplateCourseTemplateResponse savedUpdateTemplateCourseTemplateResponse = templateCourseTemplateResponseRepository.save(updateTemplateCourseTemplateResponse);
			TemplateCourseTemplateResponse savedTemplateCourseTemplateResponseTop = templateCourseTemplateResponseRepository.save(templateCourseTemplateResponseTop);
		}
	}catch (Exception e){
			e.printStackTrace();
	return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(e.getMessage());
}
		return ResponseEntity
				.status(HttpStatus.OK)
				.build();
	}

	@PostMapping(value = "form/templatecoursetemplateresponse/down/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateFormDown(@PathVariable long id) throws Exception {
		try{
		TemplateCourseTemplateResponse updateTemplateCourseTemplateResponse = templateCourseTemplateResponseRepository.findByTemplateCourseTemplateResponseId(id);
		if (!accessUtils.canEditCard(updateTemplateCourseTemplateResponse)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!updateTemplateCourseTemplateResponse.getTemplateCourse().getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + updateTemplateCourseTemplateResponse.getTemplateCourse().getClass().getSimpleName().toLowerCase() + ", id = " + updateTemplateCourseTemplateResponse.getTemplateCourse().getId() + "]");
		}
		Set<TemplateCourseTemplateResponse> templateCourseTemplateResponseSet = templateCourseTemplateResponseRepository.findTemplateCourseTemplateResponseByTemplateCourseId(updateTemplateCourseTemplateResponse.getTemplateCourse().getId());
		TemplateCourseTemplateResponse templateCourseTemplateResponseBottom = null;
		boolean founded = false;
		for (TemplateCourseTemplateResponse templateCourseTemplateResponse : templateCourseTemplateResponseSet){
			if (!founded){
				if (templateCourseTemplateResponse.getId().equals(updateTemplateCourseTemplateResponse.getId())){
					founded = true;
				}
			}else{
				templateCourseTemplateResponseBottom = templateCourseTemplateResponse;
				break;
			}
		}
		if (templateCourseTemplateResponseBottom != null){
			Long tmpPos = templateCourseTemplateResponseBottom.getPosition();
			templateCourseTemplateResponseBottom.setPosition(updateTemplateCourseTemplateResponse.getPosition());
			updateTemplateCourseTemplateResponse.setPosition(tmpPos);
			TemplateCourseTemplateResponse savedUpdateTemplateCourseTemplateResponse = templateCourseTemplateResponseRepository.save(updateTemplateCourseTemplateResponse);
			TemplateCourseTemplateResponse savedTemplateCourseTemplateResponseTop = templateCourseTemplateResponseRepository.save(templateCourseTemplateResponseBottom);
		}
	}catch (Exception e){
			e.printStackTrace();
	return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(e.getMessage());
}
		return ResponseEntity
				.status(HttpStatus.OK)
				.build();
	}

}