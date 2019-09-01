package com.dls.base.controller.form;

import com.dls.base.entity.Block;
import com.dls.base.entity.TemplatePlan;
import com.dls.base.entity.TemplatePlanTemplateResponse;
import com.dls.base.repository.BlockRepository;
import com.dls.base.repository.TemplatePlanRepository;
import com.dls.base.repository.TemplatePlanTemplateResponseRepository;
import com.dls.base.request.TemplatePlanTemplateResponseContainer;
import com.dls.base.ui.form.FormTemplate;
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
public class FormTemplatePlanTemplateResponseRestController
{

	private final TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository;
	private final TemplatePlanRepository templatePlanRepository;
	private final BlockRepository blockRepository;

	@Autowired
	FormTemplatePlanTemplateResponseRestController(TemplatePlanTemplateResponseRepository templatePlanTemplateResponseRepository, TemplatePlanRepository templatePlanRepository, BlockRepository blockRepository) {
		this.templatePlanTemplateResponseRepository = templatePlanTemplateResponseRepository;
		this.templatePlanRepository = templatePlanRepository;
		this.blockRepository = blockRepository;
	}

	@Autowired
	AccessUtils accessUtils;

	@PutMapping(value = "form/templateplantemplateresponse/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity createForm(@PathVariable long id, @RequestBody final List<TemplatePlanTemplateResponseContainer> containerList) throws Exception {
		try{
		for (TemplatePlanTemplateResponseContainer container : containerList){
			TemplatePlan templatePlan = templatePlanRepository.findByTemplatePlanId(container.templatePlanId);
			if (templatePlanTemplateResponseRepository.findTemplatePlanTemplateResponseByTemplatePlanIdAndTemplateResponseClassAndTemplateResponseId(templatePlan.getId(), container.templateResponseClass, container.templateResponseId) == null){
				Block block = blockRepository.findBlockByTemplatePlanIdAndBlockPosition(templatePlan.getId(), 1L);
				if (block == null){
					block = new Block();
					block.setParentClass(templatePlan.getClass().getSimpleName().toLowerCase());
					block.setParentId(templatePlan.getId());
					block.setPosition(1L);
					block.setType(Constant.BLOCK_TYPE_SERIAL);
					Block savedBlock = blockRepository.save(block);
					block = savedBlock;
				}
				TemplatePlanTemplateResponse templatePlanTemplateResponse = new TemplatePlanTemplateResponse();
				templatePlanTemplateResponse.setTemplatePlan(templatePlanRepository.findByTemplatePlanId(container.templatePlanId));
				templatePlanTemplateResponse.setTemplateResponseClass(container.templateResponseClass);
				templatePlanTemplateResponse.setTemplateResponseId(container.templateResponseId);
				templatePlanTemplateResponse.setPosition(templatePlanTemplateResponseRepository.findLastPositionByTemplatePlanId(templatePlan.getId()) + 1);
				templatePlanTemplateResponse.setBlock(block);
				if (!accessUtils.canEditCard(templatePlanTemplateResponse)){
					throw new Exception("Отсутствуют права на создание/изменение карточки");
				}
				TemplatePlanTemplateResponse savedTemplatePlanTemplateResponse = templatePlanTemplateResponseRepository.save(templatePlanTemplateResponse);
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

	@PostMapping(value = "form/templateplantemplateresponse/{id}/to_block/{position}/to_block_type/{blockType}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateFormToBlock(@PathVariable long id, @PathVariable long position, @PathVariable byte blockType) throws Exception {
		FormTemplate formTemplate = null;
		try{
		if (position < 1){
			throw new Exception("Некорректный номер блока");
		}
		if (blockType != Constant.BLOCK_TYPE_SERIAL && blockType != Constant.BLOCK_TYPE_PARALLEL){
			throw new Exception("Некорректный тип блока");
		}
		TemplatePlanTemplateResponse updateTemplatePlanTemplateResponse = templatePlanTemplateResponseRepository.findByTemplatePlanTemplateResponseId(id);
		if (!accessUtils.canEditCard(updateTemplatePlanTemplateResponse)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!updateTemplatePlanTemplateResponse.getTemplatePlan().getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + updateTemplatePlanTemplateResponse.getTemplatePlan().getClass().getSimpleName().toLowerCase() + ", id = " + updateTemplatePlanTemplateResponse.getTemplatePlan().getId() + "]");
		}
		TemplatePlan templatePlan = templatePlanRepository.findTemplatePlanByTemplatePlanTemplateResponseId(id);

		Block block = blockRepository.findBlockByTemplateBlockTemplatePlanTemplateResponseId(id);

		if (templatePlanTemplateResponseRepository.findTemplatePlanTemplateResponseByTemplatePlanIdAndBlockPosition(block.getParentId(), block.getPosition()).size() > 1){
			if (block.getPosition().equals(position)){
				block.setPosition(position);
				block.setType(blockType);
				blockRepository.save(block);
			}else{
				block = blockRepository.findBlockByTemplatePlanIdAndBlockPosition(block.getParentId(), position);
				if (block == null){
					block = new Block();
					block.setParentClass(templatePlan.getClass().getSimpleName().toLowerCase());
					block.setParentId(templatePlan.getId());
					block.setPosition(position);
				}
				block.setType(blockType);
				blockRepository.save(block);
				updateTemplatePlanTemplateResponse.setBlock(block);
				templatePlanTemplateResponseRepository.save(updateTemplatePlanTemplateResponse);
			}
		}else{
			Block oldBlock = block;
			block = blockRepository.findBlockByTemplatePlanIdAndBlockPosition(block.getParentId(), position);
			if (block == null){
				block = new Block();
				block.setParentClass(templatePlan.getClass().getSimpleName().toLowerCase());
				block.setParentId(templatePlan.getId());
				block.setPosition(position);
				block.setType(blockType);
				Block savedBlock = blockRepository.save(block);
				updateTemplatePlanTemplateResponse.setBlock(savedBlock);
				templatePlanTemplateResponseRepository.save(updateTemplatePlanTemplateResponse);
				blockRepository.delete(oldBlock);
			}else{
				block.setPosition(position);
				block.setType(blockType);
				Block savedBlock = blockRepository.save(block);
				updateTemplatePlanTemplateResponse.setBlock(savedBlock);
				templatePlanTemplateResponseRepository.save(updateTemplatePlanTemplateResponse);
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

	@PostMapping(value = "form/templateplantemplateresponse/up/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateFormUp(@PathVariable long id) throws Exception {
		try{
		TemplatePlanTemplateResponse updateTemplatePlanTemplateResponse = templatePlanTemplateResponseRepository.findByTemplatePlanTemplateResponseId(id);
		if (!accessUtils.canEditCard(updateTemplatePlanTemplateResponse)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!updateTemplatePlanTemplateResponse.getTemplatePlan().getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + updateTemplatePlanTemplateResponse.getTemplatePlan().getClass().getSimpleName().toLowerCase() + ", id = " + updateTemplatePlanTemplateResponse.getTemplatePlan().getId() + "]");
		}
		Set<TemplatePlanTemplateResponse> templatePlanTemplateResponseSet = templatePlanTemplateResponseRepository.findTemplatePlanTemplateResponseByTemplatePlanId(updateTemplatePlanTemplateResponse.getTemplatePlan().getId());
		TemplatePlanTemplateResponse templatePlanTemplateResponseTop = null;
		for (TemplatePlanTemplateResponse templatePlanTemplateResponse : templatePlanTemplateResponseSet){
			if (templatePlanTemplateResponseTop != null){
				if (templatePlanTemplateResponse.getId().equals(updateTemplatePlanTemplateResponse.getId())){
					break;
				}
			}
			templatePlanTemplateResponseTop = templatePlanTemplateResponse;
		}
		if (templatePlanTemplateResponseTop != null){
			Long tmpPos = templatePlanTemplateResponseTop.getPosition();
			templatePlanTemplateResponseTop.setPosition(updateTemplatePlanTemplateResponse.getPosition());
			updateTemplatePlanTemplateResponse.setPosition(tmpPos);
			TemplatePlanTemplateResponse savedUpdateTemplatePlanTemplateResponse = templatePlanTemplateResponseRepository.save(updateTemplatePlanTemplateResponse);
			TemplatePlanTemplateResponse savedTemplatePlanTemplateResponseTop = templatePlanTemplateResponseRepository.save(templatePlanTemplateResponseTop);
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

	@PostMapping(value = "form/templateplantemplateresponse/down/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public ResponseEntity updateFormDown(@PathVariable long id) throws Exception {
		try{
		TemplatePlanTemplateResponse updateTemplatePlanTemplateResponse = templatePlanTemplateResponseRepository.findByTemplatePlanTemplateResponseId(id);
		if (!accessUtils.canEditCard(updateTemplatePlanTemplateResponse)){
			throw new Exception("Отсутствуют права на создание/изменение карточки");
		}
		if (!updateTemplatePlanTemplateResponse.getTemplatePlan().getStatus().getCode().equals("draft")){
			throw new Exception("Некорректный статус [class = " + updateTemplatePlanTemplateResponse.getTemplatePlan().getClass().getSimpleName().toLowerCase() + ", id = " + updateTemplatePlanTemplateResponse.getTemplatePlan().getId() + "]");
		}
		Set<TemplatePlanTemplateResponse> templatePlanTemplateResponseSet = templatePlanTemplateResponseRepository.findTemplatePlanTemplateResponseByTemplatePlanId(updateTemplatePlanTemplateResponse.getTemplatePlan().getId());
		TemplatePlanTemplateResponse templatePlanTemplateResponseBottom = null;
		boolean founded = false;
		for (TemplatePlanTemplateResponse templatePlanTemplateResponse : templatePlanTemplateResponseSet){
			if (!founded){
				if (templatePlanTemplateResponse.getId().equals(updateTemplatePlanTemplateResponse.getId())){
					founded = true;
				}
			}else{
				templatePlanTemplateResponseBottom = templatePlanTemplateResponse;
				break;
			}
		}
		if (templatePlanTemplateResponseBottom != null){
			Long tmpPos = templatePlanTemplateResponseBottom.getPosition();
			templatePlanTemplateResponseBottom.setPosition(updateTemplatePlanTemplateResponse.getPosition());
			updateTemplatePlanTemplateResponse.setPosition(tmpPos);
			TemplatePlanTemplateResponse savedUpdateTemplatePlanTemplateResponse = templatePlanTemplateResponseRepository.save(updateTemplatePlanTemplateResponse);
			TemplatePlanTemplateResponse savedTemplatePlanTemplateResponseTop = templatePlanTemplateResponseRepository.save(templatePlanTemplateResponseBottom);
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