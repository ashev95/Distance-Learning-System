define([
    "dojo/_base/declare",
    "dijit/_WidgetBase",
    "dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",

	"dojo/request",
	"widgets/GridWidget",
    "dijit/MenuBar",
    "dijit/MenuItem",
    "dojo/dom-style",
    "dojo/dom-form",
    "dojo/query",
    "dojo/dom-construct",
    "dijit/form/TextBox",
    "dijit/form/NumberTextBox",
    "dijit/form/MultiSelect",
    "dijit/layout/ContentPane",
    "dijit/form/Button",
    "dijit/form/DropDownButton",
    "dijit/TooltipDialog",
    "dijit/DropDownMenu",
    "dijit/MenuSeparator",
    "dijit/PopupMenuItem",
    "dojo/dom-attr",
    "dijit/form/Form",
    "dijit/form/Select",
    "widgets/ValidationRoleTextAreaWidget",
    "widgets/ValidationAttributeWidget",
    "widgets/ValidationMultiAttributeTextAreaWidget",
    "widgets/ResourceWidget",

    "dojo/text!./template/forms/personEdit.html",
    "dojo/text!./template/forms/personRead.html",

    "dojo/text!./template/forms/groupEdit.html",
    "dojo/text!./template/forms/groupRead.html",

    "dojo/text!./template/forms/templateLessonEdit.html",
    "dojo/text!./template/forms/templateLessonRead.html",

    "dojo/text!./template/forms/lessonEdit.html",
    "dojo/text!./template/forms/lessonRead.html",

    "dojo/text!./template/forms/courseEdit.html",
    "dojo/text!./template/forms/courseRead.html",

    "dojo/text!./template/forms/planEdit.html",
    "dojo/text!./template/forms/planRead.html",

    "dojo/text!./template/forms/trainingLessonRead.html",

    "dojo/text!./template/forms/testEdit.html",
    "dojo/text!./template/forms/testRead.html",

    "dojo/text!./template/forms/templateTestEdit.html",
    "dojo/text!./template/forms/templateTestRead.html",

    "dojo/text!./template/forms/templateTestVariantEdit.html",
    "dojo/text!./template/forms/templateTestVariantRead.html",

    "dojo/text!./template/forms/templateTestQuestionEdit.html",
    "dojo/text!./template/forms/templateTestQuestionRead.html",

    "dojo/text!./template/forms/templateTestAnswerEdit.html",
    "dojo/text!./template/forms/templateTestAnswerRead.html",

    "dojo/text!./template/forms/templateCourseEdit.html",
    "dojo/text!./template/forms/templateCourseRead.html",

    "dojo/text!./template/forms/templatePlanEdit.html",
    "dojo/text!./template/forms/templatePlanRead.html",

    "dojo/text!./template/forms/categoryEdit.html",
    "dojo/text!./template/forms/categoryRead.html",

    "dojo/text!./template/forms/student_infoRead.html",
    "dojo/text!./template/forms/student_completed_trainingsRead.html",

    "dojox/validate/web", "dojox/validate/us", "dojox/validate/check"
	
], function(
            declare,
            _WidgetBase,
            _TemplatedMixin,
            _WidgetsInTemplateMixin,

            request,
            GridWidget,
            MenuBar,
            MenuItem,
            domStyle,
            domForm,
            query,
            domConstruct,
            TextBox,
            NumberTextBox,
            MultiSelect,
            ContentPane,
            Button,
            DropDownButton,
            TooltipDialog,
            DropDownMenu,
            MenuSeparator,
            PopupMenuItem,
            domAttr,
            Form,
            Select,
            ValidationRoleTextAreaWidget,
            ValidationAttributeWidget,
            ValidationMultiAttributeTextAreaWidget,
            ResourceWidget,

            formPersonEdit,
            formPersonRead,

            formGroupEdit,
            formGroupRead,

            formTemplateLessonEdit,
            formTemplateLessonRead,

            formLessonEdit,
            formLessonRead,

            formCourseEdit,
            formCourseRead,

            formPlanEdit,
            formPlanRead,

            formTrainingLessonRead,

            formTestEdit,
            formTestRead,

            formTemplateTestEdit,
            formTemplateTestRead,

            formTemplateTestVariantEdit,
            formTemplateTestVariantRead,

            formTemplateTestQuestionEdit,
            formTemplateTestQuestionRead,

            formTemplateTestAnswerEdit,
            formTemplateTestAnswerRead,

            formTemplateCourseEdit,
            formTemplateCourseRead,

            formTemplatePlanEdit,
            formTemplatePlanRead,

            formCategoryEdit,
            formCategoryRead,

            formStudentInfoRead,
            formStudentCompletedTrainingsRead,

            validate
    ){
    return declare("FormWidget", [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
		
		response: {type:'undefined'},
		uid: "",
		type: "",
        baseClass: 'FormWidget',
		appWidget: undefined,
		isNew: false,
        template: undefined,
        templateString: undefined,
        editMode: false,
        menuBar: undefined,
        form: undefined,
        embViewResponse: undefined,
        embView: undefined,
        parentFormWidget: undefined,

        listOfTemplates: {
            personEdit:formPersonEdit,
            personRead:formPersonRead,

            groupEdit:formGroupEdit,
            groupRead:formGroupRead,

            templatelessonEdit:formTemplateLessonEdit,
            templatelessonRead:formTemplateLessonRead,

            lessonEdit:formLessonEdit,
            lessonRead:formLessonRead,

            courseEdit:formCourseEdit,
            courseRead:formCourseRead,

            planEdit:formPlanEdit,
            planRead:formPlanRead,

            traininglessonRead:formTrainingLessonRead,

            testEdit:formTestEdit,
            testRead:formTestRead,

            templatetestEdit:formTemplateTestEdit,
            templatetestRead:formTemplateTestRead,

            templatetestvariantEdit:formTemplateTestVariantEdit,
            templatetestvariantRead:formTemplateTestVariantRead,

            templatetestquestionEdit:formTemplateTestQuestionEdit,
            templatetestquestionRead:formTemplateTestQuestionRead,

            templatetestanswerEdit:formTemplateTestAnswerEdit,
            templatetestanswerRead:formTemplateTestAnswerRead,

            templatecourseEdit:formTemplateCourseEdit,
            templatecourseRead:formTemplateCourseRead,

            templateplanEdit:formTemplatePlanEdit,
            templateplanRead:formTemplatePlanRead,

            categoryEdit: formCategoryEdit,
            categoryRead: formCategoryRead,

            student_infoRead: formStudentInfoRead,
            student_completed_trainingsRead: formStudentCompletedTrainingsRead

        },

        listOfEmbeddedViewUrls:{
            templatetest : 'embedded_view/templatetestvariant/by_parent/',
            templatecourse : 'embedded_view/templateresponse/by_templatecourse/',
            templateplan : 'embedded_view/templateresponse/by_templateplan/',
            group : 'embedded_view/person/by_group/',
            lesson : 'embedded_view/person/by_lesson/',
            test : 'embedded_view/person/by_testvariant/',
            course : 'embedded_view/response/by_course/',
            plan : 'embedded_view/response/by_plan/'
        },

        getButtonByType: function(type){
		    var foundedItem = null;
            this.menuBar.getChildren().some(function(item) {
                if (item.btnType === type){
                    foundedItem = item;
                }
            });
            return foundedItem;
        },

        getButtonParamByType: function(type){
            var foundedItem = null;
            if (!!this.response.buttons && !!this.response.buttons[0]){
                 this.response.buttons.reverse().some(function(button) {
                    if (button.btnType === type){
                        foundedItem = dojo.clone(button);
                    }
                 });

            }
            return foundedItem;
        },

        getFormObject: function() {
            var formObject = domForm.toObject(this.form.domNode);
            var newFormObject = dojo.clone(formObject);
            var thisContext = this;
            var attributesObject = dojo.clone(thisContext.appWidget.formConfig.template[thisContext.template].attributes);
            var attributesArray = Object.entries(attributesObject);
            attributesArray.some(function(attributeConfig) {
                var attrName = attributeConfig[0];
                var attrProp = attributeConfig[1];
                switch (attrProp.type){
                    case 'roles':
                        if (thisContext['component_' + attrName] && thisContext['component_' + attrName].baseClass){
                            var widget = thisContext['component_' + attrName];
                            if (widget.baseClass.split(' ').indexOf('ValidationRoleTextAreaWidget') >= 0){
                                newFormObject[attrName] = widget.getValueArray();
                            }
                        }
                        break;
                    case 'person':
                    case 'group':
                    case 'status':
                    case 'category':
                        if (!newFormObject[attrName] || typeof newFormObject[attrName] !== 'object'){
                            if (thisContext['component_' + attrName] && thisContext['component_' + attrName].baseClass){
                                var widget = thisContext['component_' + attrName];
                                if (widget.baseClass.split(' ').indexOf('ValidationAttributeWidget') >= 0){
                                    newFormObject[attrName] = new Object();
                                    if (widget.getValue()){
                                        newFormObject[attrName].id = widget.getValue().id.value;
                                    }else{
                                        newFormObject[attrName].id = 0;
                                    }
                                }
                            }else{
                                newFormObject[attrName] = new Object();
                                newFormObject[attrName].id = thisContext.response.attributes[attrName].value.id.value;
                            }
                        }
                        break;
                    case 'boolean':
                        if (thisContext['component_' + attrName] && thisContext['component_' + attrName].baseClass){
                            newFormObject[attrName] = (newFormObject[attrName] == null ? false : thisContext['component_' + attrName].checked);
                        }
                        break;
                    case 'date':
                        newFormObject[attrName] = new Date(thisContext.response.attributes[attrName].value);
                        break;
                    case 'id':
                        if (!newFormObject[attrName]){
                            newFormObject[attrName] = 0;
                            if (thisContext.response.attributes[attrName]){
                                if(thisContext.response.attributes[attrName].value){
                                    newFormObject[attrName] = thisContext.response.attributes[attrName].value;
                                }
                            }
                        }
                        break;
                    case 'parent':
                        var parentAttrName = 'parent';
                        switch (thisContext.template){
                            case 'templatetestvariant':
                                parentAttrName = 'templateTest';
                                break;
                            case 'templatetestquestion':
                                parentAttrName = 'templateTestVariant';
                                break;
                            case 'templatetestanswer':
                                parentAttrName = 'templateTestQuestion';
                                break;
                        }
                        if (!newFormObject[parentAttrName]){
                            newFormObject[parentAttrName] = new Object();
                            newFormObject[parentAttrName].id = thisContext.parentFormWidget.response.attributes.id.value;
                        }
                        break;
                }

            });
            switch (thisContext.template){
                case 'templatelesson':
                case 'templatetestquestion':
                    if (!newFormObject["unids"]){
                        newFormObject["unids"] = thisContext.component_resource.getAllUnids();
                    }
                    break;
                case 'lesson':
                    if (!newFormObject["templateLesson"]){
                        newFormObject["templateLesson"] = {id: thisContext.response.attributes.templateLesson.value.id.value};
                    }
                    break;
                case 'course':
                    if (!newFormObject["templateCourse"]){
                        newFormObject["templateCourse"] = {id: thisContext.response.attributes.templateCourse.value.id.value};
                    }
                    break;
                case 'test':
                    if (!newFormObject["templateTest"]){
                        newFormObject["templateTest"] = {id: thisContext.response.attributes.templateTest.value.id.value};
                    }
                    break;
                case 'plan':
                    if (!newFormObject["templatePlan"]){
                        newFormObject["templatePlan"] = {id: thisContext.response.attributes.templatePlan.value.id.value};
                    }
                    break;
            }
            if (this.isNew && !!this.parent && !!this.parent.cardId) {
                switch (thisContext.template){
                    case 'templatelesson':
                    case 'templatecourse':
                    case 'templatetest':
                    case 'templateplan':
                        newFormObject["parent"] = {id: this.parent.cardId[0]};
                        break;
                }
            }
            return newFormObject;
        },

        postProcess: function(response, callbackError, callbackSuccess){
            switch (this.template){
                case 'templatelesson':
                case 'templatetestquestion':
                    var resourceWidgetNode = query("*[class=ResourceWidget]", this.form.domNode)[0];
                    var resourceWidget = dijit.byId(resourceWidgetNode.id);
                    resourceWidget.uploadFiles(response, callbackError, callbackSuccess);
                    break;
                default:
                    callbackSuccess();
            }
        },

        validate: function (){
		    return (this.form.validate());
        },

        computeHideFunction: function (){
            this.menuBar.getChildren().some(function(button) {
                if (button.computeHF()){
                    domStyle.set(button.domNode, "display", "none");
                }else{
                    domStyle.set(button.domNode, "display", "block");
                }
            });
        },

		buildRendering: function(){
            this.templateString = this.listOfTemplates[this.template + (this.editMode ? 'Edit' : 'Read')];
            //
            var url = this.listOfEmbeddedViewUrls[this.template];
            if (url){
                //Get data for embedded view
                var parentId = 0;
                if (!this.isNew){
                    parentId = this.params.response.attributes.id.value;
                }
                url += parentId;
                var thisContext = this;
                request(url, {
                    method: "get",
                    sync: true,
                    handleAs: "json"
                }).then(function(response){
                    thisContext.embViewResponse = response;
                    thisContext.embViewResponse.url = url;
                });
            }
            //
            this.inherited(arguments);
		},

		postCreate: function(){
		    this.inherited(arguments);
            //
            var thisContext = this;
            var attributesObject = dojo.clone(thisContext.appWidget.formConfig.template[thisContext.template].attributes);
            var attributesArray = Object.entries(attributesObject);
            attributesArray.some(function(attributeConfig){
                var attrName = attributeConfig[0];
                var attrProp = attributeConfig[1];
                if (thisContext['label_' + attrName]){
                    if (!!thisContext.editMode){
                        thisContext['label_' + attrName].for = attrName;
                    }
                    thisContext['label_' + attrName].innerText = thisContext.appWidget.formConfig.template[thisContext.template].attributes[attrName].label + ":";
                }
                switch (attrProp.type){
                    case 'roles':
                        if (!!thisContext.editMode){
                            thisContext['component_' + attrName].setValueArray(thisContext.response.attributes[attrName].value);
                        }else{
                            var v = [];
                            thisContext.response.attributes[attrName].value.some(function(obj) {
                                v.push(obj.name);
                            });
                            thisContext['component_' + attrName].innerHTML = v.join('<br>');
                        }
                        break;
                    case 'username':
                        if (!!thisContext.editMode){
                            if (!thisContext.isNew){
                                domAttr.set(thisContext['component_' + attrName], "readOnly", "readOnly");
                                domStyle.set(thisContext['component_' + attrName].domNode, "border", "0px");
                            }
                        }
                        break;
                    case 'person':
                        if (thisContext['component_' + attrName] && thisContext['component_' + attrName].baseClass){
                            if (thisContext.response.attributes[attrName].value){
                                thisContext['component_' + attrName].setValue(thisContext.response.attributes[attrName].value);
                                if (!!thisContext.appWidget.formConfig.template[thisContext.template].attributes[attrName].editByRole){
                                    if (!thisContext.appWidget.currentUserHasAtLeastRole('ADMINISTRATOR')){
                                        if (thisContext['component_' + attrName + '_button_1']){
                                            domStyle.set(thisContext['component_' + attrName + '_button_1'].domNode, "display", "none");
                                        }
                                        if (thisContext['component_' + attrName + '_button_2']){
                                            domStyle.set(thisContext['component_' + attrName + '_button_2'].domNode, "display", "none");
                                        }
                                    }
                                }
                            }
                        }else{
                            if (thisContext.response.attributes[attrName].value){
                                thisContext['component_' + attrName].innerHTML = (thisContext.response.attributes[attrName].value.surname.value + " " + thisContext.response.attributes[attrName].value.name.value + " " + thisContext.response.attributes[attrName].value.middlename.value);
                            }
                        }
                        break;
                    case 'group':
                    case 'status':
                    case 'category':
                        if (thisContext['component_' + attrName] && thisContext['component_' + attrName].baseClass){
                            if (thisContext.response.attributes[attrName].value){
                                thisContext['component_' + attrName].setValue(thisContext.response.attributes[attrName].value);
                            }
                        }else{
                            if (thisContext.response.attributes[attrName].value){
                                thisContext['component_' + attrName].innerHTML = thisContext.response.attributes[attrName].value.name.value;
                            }
                        }
                        break;
                    case 'gender':
                        debugger;
                        if (thisContext['component_' + attrName] && thisContext['component_' + attrName].baseClass){
                            thisContext['component_' + attrName].setValue(thisContext.response.attributes[attrName].value);
                        }else{
                            thisContext['component_' + attrName].innerHTML = (thisContext.response.attributes[attrName].value == 0 ? "Мужской" : "Женский");
                        }
                        break;
                    case 'boolean':
                        if (thisContext['component_' + attrName] && !thisContext['component_' + attrName].baseClass){
                            thisContext['component_' + attrName].innerHTML = (thisContext.response.attributes[attrName].value ? "Да" : "Нет");
                        }
                        if (thisContext.editMode && thisContext['component_' + attrName]){
                            if (thisContext.template == 'templatetest' && attrName == 'byOrder'){
                                dojo.query('.hide1', thisContext.domNode)[0].style.display = (thisContext['component_' + attrName].checked ? 'none' : '');
                                if (thisContext['component_deprecateChangeAnswerCount']){
                                    thisContext['component_deprecateChangeAnswerCount'].setValue(thisContext.response.attributes.deprecateChangeAnswerCount.value);
                                }
                            }
                        }else if(!thisContext.editMode && thisContext['component_' + attrName]){
                            if (thisContext.template == 'templatetest' && attrName == 'byOrder'){
                                dojo.query('.hide1', thisContext.domNode)[0].style.display = (thisContext.response.attributes[attrName].value ? 'none' : '');
                            }
                        }
                        break;
                    case 'date':
                        if (thisContext['component_' + attrName]){
                            if (thisContext.response.attributes[attrName]){
                                thisContext['component_' + attrName].innerHTML = (thisContext.response.attributes[attrName].value ? thisContext.response.attributes[attrName].value : "Не определена");
                            }
                        }
                        break;
                    case 'string':
                        switch (thisContext.template){
                            case 'templatelesson':
                            case 'templatetest':
                            case 'templatecourse':
                            case 'templateplan':
                                if (thisContext.isNew && !!thisContext.response.attributes.parent && !!thisContext.parent && !!thisContext.parent.realName && thisContext.editMode && (attrName == 'name' || attrName == 'description')){
                                    thisContext['component_' + attrName].setValue(thisContext.parent.realName);
                                    thisContext['component_' + attrName].setValue(thisContext.parent.realDescription);
                                    domAttr.set(thisContext['component_' + attrName], "readOnly", "readOnly");
                                    domStyle.set(thisContext['component_' + attrName].domNode, "border", "0px");
                                }else if (!thisContext.isNew && thisContext.editMode && !!thisContext.response.attributes.parent.value){
                                    domAttr.set(thisContext['component_' + attrName], "readOnly", "readOnly");
                                    domStyle.set(thisContext['component_' + attrName].domNode, "border", "0px");
                                }
                                break;
                        }
                        break;
                }
            });
            //
            var pMenuBar = new MenuBar({
                style: {display: (this.parentFormWidget ? "none" : "block")}
            });
            this.menuBar = pMenuBar;

            if (this.description && this.response.data.description){
                if (this.editMode){
                    this.description.attr("value", this.response.data.description)
                }else{
                    this.description.innerHTML = this.response.data.description;
                }
            }

            var thisContext = this;
            if (this.appWidget.formConfig.template[this.template]){
                this.response.buttons = this.appWidget.formConfig.template[this.template].buttons;
            }

            if (!!this.response.buttons && !!this.response.buttons[0]){
                dojo.clone(this.response.buttons).reverse().some(function(button) {
                    pMenuBar.addChild(
                        new MenuItem({
                            btnType: button.btnType,
                            label: button.label,
                            iconClass: button.iconClass,
                            onClick: function (item) {eval("(function(item){" + button.onClick + "})").call(thisContext, this);},
                            appWidget: this.appWidget,
                            computeHF: function (items) {return eval("(function(){" + button.hideFunction + "})").call(thisContext, items);}
                        })
                    );
                });
                this.computeHideFunction();
            }

            pMenuBar.placeAt(this.domNode, 'first');
		}
		
    });
	
});