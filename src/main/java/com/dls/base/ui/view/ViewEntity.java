package com.dls.base.ui.view;

import java.io.Serializable;
import java.util.ArrayList;

public class ViewEntity implements Serializable {

	public long id;
	public long cardId;
	public String name;
	public String type;
	public String code;
	public ArrayList<ViewEntity> children;

	//Вышестоящая карточка в древовидном представлении
	public long parentCardId;
	public String parentType;

	//Настоящая сущность, отображающая карточку
	public long realCardId;
	public String realType;

	//
	public String realStatusCode;
	public String realStatusName;

	public String realStartDate;
	public String realEndDate;

	public String realName;
	public String realDescription;

	public long prevId;
	public long prevCardId;
	public String prevType;

	public long prevRealCardId;
	public String prevRealType;

	public long nextId;
	public long nextCardId;
	public String nextType;

	public long nextRealCardId;
	public String nextRealType;

	public String rowCustomStyle;

	public long block;
	public long blockType;

	public long variant;

	public String correctAnswerRatio;
	public String scoreRatio;

	public String correct;

	public String correctScore;
	public String incorrectScore;

	public String template;

}
