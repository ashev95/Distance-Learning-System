define([
    "dojo/_base/declare",
    "dijit/_WidgetBase",
    "dijit/_TemplatedMixin",
    "dijit/_WidgetsInTemplateMixin",
    "dojo/parser",
    "dojo/ready",

    "dojo/data/ItemFileReadStore",
    "dijit/tree/ForestStoreModel",
    "dijit/Tree",

    "dojo/request",

    "widgets/FormWidget",
    "widgets/GridWidget",
    "dojo/query",
    "dojo/dom-class",
    "dijit/ConfirmDialog",
    "dijit/Dialog",
    "dojo/store/JsonRest",

    "dijit/layout/ContentPane",

    "dijit/form/Button",

    "dojo/on",

    "dojo/request/xhr",

    "dijit/form/TextBox",

    "dijit/form/NumberTextBox",

    "dijit/form/RadioButton",

    "dijit/form/CheckBox",

    "dijit/form/Select",

    "dijit/layout/BorderContainer",

    "dijit/layout/AccordionContainer",

    "dijit/layout/TabContainer",

    "dijit/ProgressBar",

    "dojox/widget/Standby",

    "widgets/ResourceWidget",

    "dojo/dom",

    "dojo/cookie",

    "dojo/_base/json",

    "dojo/_base/fx",

    "dojo/dom-style",

    "dojo/aspect",

    "dojox/timing",

    "dojo/text!./template/dialogs/blockEdit.html",

    "dojo/text!./config/view_config.json",
    "dojo/text!./config/form_config.json",

    "dojo/text!./template/AppWidget.html"

], function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, parser, ready, ItemFileReadStore, ForestStoreModel, Tree, request, FormWidget, GridWidget,
            query, domClass, ConfirmDialog, Dialog, JsonRest, ContentPane, Button, on, xhr, TextBox, NumberTextBox, RadioButton, CheckBox, Select, BorderContainer, AccordionContainer, TabContainer,
            ProgressBar, Standby, ResourceWidget, dom, cookie, json, fx, domStyle, aspect, timing, formBlockEdit, viewConfigText, formConfigText, template
){
    declare("AppWidget", [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {

        templateString: template,
        _newTabId: 0,
        tabContainer: undefined,
        logout: "",
        username: "",
        extraContainer: undefined,
        viewConfig: json.fromJson(viewConfigText),
        formConfig: json.fromJson(formConfigText),
        formBlockEdit: formBlockEdit,

        processDialog: null,
        progressBar: null,

        standby: null,

        currentUser: null,

        _getNewTabId: function (){
            return (this._newTabId++);
        },

        _getTabById: function (array, value) {
            var foundEntry;
            array.some(function(entry) {
                if (entry.id == value) {
                    foundEntry = entry;
                    return true;
                }
            });
            return foundEntry;
        },

        openTabGrid: function (item) {
            var appWidget = this;
            var newTabId = 'view_' + item.type + '_' + item.uid;
            var tab = this._getTabById(this.tabContainer.getChildren(), newTabId);
            var url = 'view/' + item.uid;
            if (!tab){
                request(url, {
                    handleAs: "json"
                }).then(function(response){
                    tab = new GridWidget({
                        id: newTabId,
                        title: item.title, //item.name[0],
                        url: url,
                        closable: true,
                        response: response,
                        isNew: false,
                        appWidget: appWidget
                    });

                    appWidget.tabContainer.addChild(tab);
                    appWidget.drawTabHeader(appWidget.tabContainer, newTabId);
                    appWidget.tabContainer.selectChild(tab);
                });
            }else{
                this.tabContainer.selectChild(tab);
            }
        },

        drawTabHeader: function(tabContainer, newTabId){
            var t = query(".dijitTabInner.dijitTabContent.dijitTab", tabContainer.domNode);
            t.some(function(entry) {
                var tt = query("#" + newTabId, entry.domNode);
                if (tt[0]){
                    domClass.add(entry, "myDijitTab");
                }
            });
            /* Для вкладки с гридом добавим класс со стилем */
            /* НАЧАЛО */
            /*
             var t = query(".dijitTabInner.dijitTabContent.dijitTab", appWidget.tabContainer.domNode);
             t.some(function(entry) {
             var tt = query("#" + newTabId, entry.domNode);
             if (tt[0]){
             domClass.add(entry, "myDijitTab");
             }
             });
             */
            /* КОНЕЦ */
        },

        openTabForm: function (isNew, item, callbackNotification){
            if (!item.cardId && !!item.id){
                item.cardId = dojo.clone(item.id);
            }
            /*
             if (item.cardId && item.cardId[0]){
             if(item.cardId[0].toString().indexOf('_')){
             item.cardId[0] = item.cardId[0].toString().split('_').pop();
             }
             }
             if (item.parent && item.parent.id && item.parent.id[0]){
             if(item.parent.id[0].toString().indexOf('_')){
             item.parent.id[0] = item.parent.id[0].toString().split('_').pop();
             }
             }
             */
            var url = 'form/' + item.type[0];
            if (isNew){
                if (item.parentTemplateItem){
                    url = url + '/' + item.parentTemplateItem.type + '/' + item.parentTemplateItem.id + '/' + '0';
                }else{
                    url = url + '/' + '0';
                }
            }else{
                url = url + '/' + item.cardId[0];
            }
            var newTabId = '';
            var tab = undefined;
            var thisContext = this;
            if (isNew){
                request((url), {
                    method: "get",
                    handleAs: "json"
                }).then(function(response){
                    var newTabId = 'new_form_' + item.type[0] + '_' + thisContext._getNewTabId();
                    var params = {
                        id: newTabId,
                        title: item.name[0],
                        closable: true,
                        response: response,
                        isNew: isNew,
                        template:item.type[0],
                        editMode: (!!item.editMode),
                        appWidget: thisContext,
                        parent: (item.parent)
                    };
                    tab = new FormWidget(params);
                    thisContext.tabContainer.addChild(tab);
                    thisContext.drawTabHeader(thisContext.tabContainer, newTabId);
                    thisContext.tabContainer.selectChild(tab);
                }, function (err){
                    thisContext.getPromptDialog(null, ("Ошибка при создании карточки: " + (err.message || "Возникла неизвестная ошибка. Пожалуйста, обратитесь к администратору системы."))).show();
                });
            }else{
                newTabId = 'form_' + item.type[0] + '_' + item.cardId[0];
                if (item.reopen){
                    tab = this._getTabById(this.tabContainer.getChildren(), item.tabId);
                }else{
                    tab = this._getTabById(this.tabContainer.getChildren(), newTabId);
                }
                if (!tab || item.editMode || item.reopen){
                    new JsonRest({
                        target: 'form/' + item.type[0]
                    }).get(item.cardId[0]).then(function(response){
                        if (tab){
                            var index = thisContext.tabContainer.getIndexOfChild(tab);
                            thisContext.closeTab(tab);
                            var params = {
                                id: newTabId,
                                title: item.name[0],
                                closable: true,
                                response: response,
                                isNew: false,
                                template:item.type[0],
                                editMode: (!!item.editMode),
                                appWidget: thisContext,
                                parent: (item.parent)
                            };
                            tab = new FormWidget(params);
                            thisContext.tabContainer.addChild(tab, index);
                            thisContext.drawTabHeader(thisContext.tabContainer, newTabId);
                            thisContext.tabContainer.selectChild(tab);
                        }else{
                            var params = {
                                id: newTabId,
                                title: item.name[0],
                                closable: true,
                                response: response,
                                isNew: false,
                                template: item.type[0],
                                editMode: (!!item.editMode),
                                appWidget: thisContext,
                                parent: (item.parent)
                            };
                            tab = new FormWidget(params);
                            thisContext.tabContainer.addChild(tab);
                            thisContext.drawTabHeader(thisContext.tabContainer, newTabId);
                            thisContext.tabContainer.selectChild(tab);
                        }
                    }, function (err){
                        thisContext.getPromptDialog(null, "Ошибка при открытии карточки: " + (err.message || "Возникла неизвестная ошибка. Пожалуйста, обратитесь к администратору системы.")).show();
                    });
                }else{
                    this.tabContainer.selectChild(tab);
                }
            }
            if (callbackNotification){
                callbackNotification();
            }
        },

        refreshGrids: function (){
            this.tabContainer.getChildren().some(function(entry) {
                if (entry.baseClass == "GridWidget"){
                    entry.gridView.customRefresh()
                }
            });
        },

        _buildSidebarTree: function(type){
            return (new Tree({
                model:new ForestStoreModel({
                    store: new ItemFileReadStore({url: 'view/sidebar/' + type}),
                    childrenAttrs: ["children"]
                }),
                showRoot:false,
                openOnClick:true,
                autoExpand: true,
                onClick: function (item, node, evt) {
                    switch (this.type){
                        case 'extra':
                            //item.uid = item.uid;
                            break;
                        default:
                            item.uid = '';
                    }
                    item.type = this.type;
                    var rootElement = item.uid[0].split('/')[0];
                    var title = this.path[this.path.length - 2].name[0] + '\\' + this.path[this.path.length - 1].name[0];
                    if (rootElement == 'report'){
                        //
                        //var response = this.appWidget.getDataByUrl(item.uid[0]);
                        //this.appWidget.downloadReport(item.uid[0]/*, "report_test"*/);
                        this.appWidget.openTabForm(false, {cardId: {0: 0}, type: {0:item.uid[0].split('/')[1]}, name: {0: title}, editMode: false});
                        //
                    }else{
                        item.title = title;
                        this.appWidget.openTabGrid(item);
                    }
                },
                getIconClass: function(item, opened){return '';},
                appWidget: this,
                type: type
            }));
        },

        buildReport: function(formWidget, button){
            var thisContext = this;
            if (formWidget.validate()){
                var sendData = {};
                switch (formWidget.template){
                    case "student_info":
                        sendData.group = formWidget.component_group.getValue().id.value;
                        sendData.format = formWidget.component_format.getValue();
                        break;
                    case "student_completed_trainings":
                        sendData.person = formWidget.component_student.getValue().id.value;
                        sendData.format = formWidget.component_format.getValue();
                        break;
                    default:
                        thisContext.getPromptDialog(null, "Некорректный вид отчёта").show();
                        return;
                }
                var uri = 'form/' + formWidget.template + '/' + 'build';
                thisContext.updateProgressBarDialog(1, 100);
                thisContext.postData(uri, sendData, function(errorText){thisContext.hideProgressBarDialog(null, 1); button.setDisabled(false); thisContext.getPromptDialog(null, errorText).show();}, function(response){thisContext.hideProgressBarDialog(100, 100); thisContext.downloadReport('form/report/get/' + response.fileName + '/' + response.format); button.setDisabled(false);});
            }else{
                button.setDisabled(false);
            }
        },

        createForm: function(params){
            if (!!params.selRows && !!params.selRows[0]){
                if(params.selRows[0].canBeParent && params.canCreateChild){
                    //create child
                    var thisContext = this;
                    if (params.selRows[0].skipDialog){
                        this.openTabForm(true, {type: {0: params.type}, name: {0: params.title}, editMode: true, parent: params.selRows[0], parentTemplateItem: params.parentTemplateItem});
                    }else{
                        var dialog = thisContext.getConfirmDialog(null,
                            "Создать дочерним?",
                            {ok: function(){
                                thisContext.appWidget.openTabForm(true, {type: {0: params.type}, name: {0: params.title}, editMode: true, parent: params.selRows[0], parentTemplateItem: params.parentTemplateItem});
                            },
                                cancel: function(){
                                    if (thisContext.openOnCancel){
                                        thisContext.appWidget.openTabForm(true, {type: {0:params.type}, name: {0:params.title}, editMode:true, parentTemplateItem: params.parentTemplateItem});
                                    }
                                }}
                        );
                        dialog.openOnCancel = params.openOnCancel;
                        dialog.show();
                    }
                }else{
                    //create parent
                    this.openTabForm(true, {type: {0:params.type}, name: {0:params.title}, editMode:true, parentTemplateItem: params.parentTemplateItem});
                }
            }else{
                //create parent
                this.openTabForm(true, {type: {0:params.type}, name: {0:params.title}, editMode:true, parentTemplateItem: params.parentTemplateItem});
            }
        },

        downloadReport: function (uri, name) {
            window.open(uri);
            /*
             return;
             var link = document.createElement("a");
             link.download = name;
             link.href = uri;
             document.body.appendChild(link);
             link.click();
             document.body.removeChild(link);
             delete link;
             */
        },

        createFormVersion: function(gridItem, gridWidget){
            var thisContext = this;
            var checkResponse = this.getDataByUrl('form/' + gridItem.type[0] + '/children/' + gridItem.cardId[0]);
            if (!!checkResponse){
                gridWidget.getButtonByType("new_version").setDisabled(false);
                thisContext.getPromptDialog(null, "Карточка с новой версией шаблона уже существует").show();
                return;
            }
            new JsonRest({
                target: "form/" + gridItem.type[0] + '/new_version'
            }).put(
                null,
                {id: gridItem.cardId[0]}
            ).then(function(response){
                function callbackNotificationSuccess(){
                    thisContext.showNotification("Карточка успешно сохранена");
                }
                function callbackNotificationError(){
                    thisContext.showNotification("Ошибка при сохранении");
                }
                function callbackError(postProcessMessage){
                    thisContext.hideProgressBarDialog(null, 100);
                    gridWidget.getButtonByType("new_version").setDisabled(false);
                    var dialog = thisContext.getPromptDialog(null, postProcessMessage);
                    dialog.show();
                    callbackNotificationError();
                };
                function callbackSuccess(){
                    thisContext.hideProgressBarDialog(100, 100);
                    var params = {
                        type: {0:response.template},
                        name: {0:response.tabTitle},
                        editMode:true,
                        reopen: true,
                        id: {0: response.attributes.id.value},
                        tabId: thisContext.id
                    };
                    thisContext.openTabForm(false, params, callbackNotificationSuccess);
                    gridWidget.getButtonByType("new_version").setDisabled(false);
                    //Обновить все открытые представления
                    thisContext.refreshGrids();
                };
                if (response.errorText){
                    callbackError(response.errorText);
                }else{
                    callbackSuccess(response);
                }
            }, function (err){
                // Handle the error condition
                gridWidget.getButtonByType("new_version").setDisabled(false);
                thisContext.hideProgressBarDialog(100, 100);
            }, function(evt){
                // Handle a progress event from the request if the
                // browser supports XHR2
            });
        },

        getPromptDialog: function(title, content){
            var dialog = dijit.byId('custom_dialog');
            if (!dialog){
                dialog = new Dialog({
                    id : 'custom_dialog'
                });
            }
            dialog.set('title', (title || "Сообщение"));
            dialog.set('content', content);
            dialog.set('style', "min-width: 300px");
            return dialog;
        },

        getConfirmDialog: function(title, content, callbackObject){
            var dialog = new ConfirmDialog({
                title: (title || "Сообщение"),
                content: content,
                style: "min-width: 300px",
                appWidget:this
            });
            dialog.set("buttonOk","Да");
            dialog.set("buttonCancel","Нет");
            if (callbackObject.ok){
                dialog.on('execute', function() {
                    dialog.destroyDescendants(false);
                    dialog.destroy();
                    callbackObject.ok();
                });
            }
            if (callbackObject.cancel){
                dialog.on('cancel', function() {
                    dialog.destroyDescendants(false);
                    dialog.destroy();
                    callbackObject.cancel();
                });
            }
            return dialog;
        },

        save: function (url, data, isNew, callbackError, callbackSuccess){
            if (isNew){
                new JsonRest({
                    target: url,
                }).put(
                    data,
                    {id: 0}
                ).then(function(response){
                    callbackSuccess();
                }, function (err){
                    // Handle the error condition
                    callbackError();
                }, function(evt){
                    // Handle a progress event from the request if the
                    // browser supports XHR2
                });
            }else{
                request(url, {
                    method: "post",
                    data: JSON.stringify(data),
                    handleAs: "json",
                    headers: { "Content-Type": "application/json; charset=uft-8" }
                }).then(function(response){
                    callbackSuccess();
                }, function (err){
                    // Handle the error condition
                    callbackError();
                }, function(evt){
                    // Handle a progress event from the request if the
                    // browser supports XHR2
                });
            }
        },

        postData: function (url, data, callbackError, callbackSuccess){
            request(url, {
                method: "post",
                data: JSON.stringify(data),
                handleAs: "json",
                headers: { "Content-Type": "application/json; charset=uft-8" }
            }).then(function(response){
                callbackSuccess(response);
            }, function (err){
                var errText = "Возникла неизвестная ошибка. Необходимо связаться с администратором системы";
                if (err){
                    if (err.response){
                        if (err.response.text){
                            errText = err.response.text;
                        }
                    }
                }
                // Handle the error condition
                callbackError(errText);
            }, function(evt){
                // Handle a progress event from the request if the
                // browser supports XHR2
            });
        },

        saveForm: function(formWidget, callbackErrorParam, callbackSuccessParam){
            var thisContext = this;
            var glFormWidget = formWidget;
            function callbackNotificationSuccess(){
                thisContext.showNotification("Карточка успешно сохранена");
            }
            function callbackNotificationError(){
                thisContext.showNotification("Ошибка при сохранении");
            }
            function callbackError(postProcessMessage){
                thisContext.hideProgressBarDialog(null, 100);
                glFormWidget.getButtonByType("save").setDisabled(false);
                var dialog = thisContext.getPromptDialog(null, postProcessMessage);
                dialog.show();
                callbackNotificationError();
            }
            var useInputCallback = (!!callbackErrorParam && !!callbackSuccessParam);
            if (formWidget.validate()){
                var sendData = formWidget.getFormObject();
                function callback(response){
                    function callbackSuccess(){
                        thisContext.hideProgressBarDialog(100, 100);
                        var params = {
                            type: {0:response.template},
                            name: {0:response.tabTitle},
                            editMode:true,
                            reopen: glFormWidget.isNew,
                            id: {0: response.attributes.id.value},
                            tabId: glFormWidget.id
                        };
                        thisContext.openTabForm(false, params, callbackNotificationSuccess);
                        //Обновить все открытые представления
                        thisContext.refreshGrids();
                    }
                    if (useInputCallback){
                        formWidget.postProcess(response, callbackErrorParam, callbackSuccessParam);
                    }else{
                        formWidget.postProcess(response, callbackError, callbackSuccess);
                    }
                }
                thisContext.updateProgressBarDialog(1, 100);
                if (formWidget.isNew){
                    new JsonRest({
                        target: "form/" + formWidget.template
                    }).put(
                        sendData,
                        {id: 0}
                    ).then(function(response){
                        if (response.errorText){
                            if (useInputCallback){
                                callbackErrorParam(response.errorText);
                            }else{
                                callbackError(response.errorText);
                            }
                            return;
                        }
                        callback(response);
                    }, function (err){
                        // Handle the error condition
                        if (useInputCallback){
                            callbackErrorParam(err.message);
                        }else{
                            callbackError(err.message);
                        }
                    }, function(evt){
                        // Handle a progress event from the request if the
                        // browser supports XHR2
                    });
                }else{
                    request("form/" + formWidget.template + "/" + formWidget.response.attributes.id.value, {
                        method: "post",
                        data: JSON.stringify(sendData),
                        handleAs: "json",
                        headers: { "Content-Type": "application/json; charset=uft-8" }
                    }).then(function(response){
                        if (response.errorText){
                            if (useInputCallback){
                                callbackErrorParam(response.errorText);
                            }else{
                                callbackError(response.errorText);
                            }
                            return;
                        }
                        callback(response);
                    }, function (err){
                        // Handle the error condition
                        if (useInputCallback){
                            callbackErrorParam(err);
                        }else{
                            glFormWidget.getButtonByType("save").setDisabled(false);
                            thisContext.getForm().appWidget.hideProgressBarDialog(100, 100);
                        }
                    }, function(evt){
                        // Handle a progress event from the request if the
                        // browser supports XHR2
                    });
                }
            }else{
                if (useInputCallback){
                    callbackErrorParam();
                }else{
                    formWidget.getButtonByType("save").setDisabled(false);
                }
            }
        },

        changeStatus: function(formWidget, targetStatus, save, item, callBackFunctionSuccess, callBackFunctionError, urlParam, sendDataParam){
            var thisContext = this;
            var glFormWidget = formWidget;
            if (!save){
                var sendData = {};
                function callback(response){
                    function callbackNotificationSuccess(){
                        thisContext.showNotification("Статус изменён успешно");
                    }
                    function callbackNotificationError(){
                        thisContext.showNotification("Состояние карточки изменилось. Необходимо закрыть и открыть карточку повторно");
                    }
                    function callbackError(postProcessMessage){
                        thisContext.hideProgressBarDialog(null, 100);
                        glFormWidget.getButtonByType("change_status").setDisabled(false);
                        var dialog = thisContext.getPromptDialog(null, postProcessMessage);
                        dialog.show();
                        callbackNotificationError();
                    }
                    function callbackSuccess(){
                        thisContext.hideProgressBarDialog(100, 100);
                        if (formWidget.baseClass === "FormWidget"){
                            thisContext.openTabForm(false, {
                                type: {0:(response.attributes.original_type ? response.attributes.original_type.value : response.template)},
                                name: {0:response.tabTitle},
                                editMode: false,
                                reopen: true,
                                id: {0: (response.attributes.original_id ? response.attributes.original_id.value : response.attributes.id.value)},
                                tabId: glFormWidget.id
                            }, callbackNotificationSuccess);
                            //Обновить все открытые представления
                            thisContext.refreshGrids();
                        }else{ //Представление
                            thisContext.openTabForm(false, {
                                type: {0:glFormWidget.formWidget.response.template},
                                name: {0:glFormWidget.formWidget.response.tabTitle},
                                editMode: glFormWidget.formWidget.editMode,
                                reopen: true,
                                id: {0: glFormWidget.formWidget.response.attributes.id.value},
                                tabId: glFormWidget.formWidget.id
                            });
                        }
                    }
                    if (formWidget.baseClass === "FormWidget"){
                        callbackSuccess();
                        //formWidget.postProcess(response, callbackError, callbackSuccess);
                    }else{ //Представление
                        callbackSuccess();
                    }
                };
                function callbackQueryError(postProcessMessage){
                    thisContext.hideProgressBarDialog(null, 100);
                    glFormWidget.getButtonByType("change_status").setDisabled(false);
                    var dialog = thisContext.getPromptDialog(null, postProcessMessage);
                    dialog.show();
                    thisContext.showNotification("Состояние карточки изменилось. Необходимо закрыть и открыть карточку повторно");
                }
                if (callBackFunctionSuccess && callBackFunctionError){
                    callback = callBackFunctionSuccess;
                    callbackQueryError = callBackFunctionError;
                }
                thisContext.updateProgressBarDialog(1, 100);
                var url = "";
                if (urlParam){
                    url = urlParam;
                }else{
                    if (item){ //из представления
                        url = "form/" + item.realType[0] + "/" + item.realCardId[0] + "/status/" + targetStatus;
                    }else{
                        var id = formWidget.response.attributes.id.value;
                        if (formWidget.response.attributes.original_id){
                            id = formWidget.response.attributes.original_id.value;
                        }
                        url = "form/" + formWidget.template + "/" + id + "/status/" + targetStatus;
                    }
                }

                request(url, {
                    method: "post",
                    data: JSON.stringify((sendDataParam ? sendDataParam : sendData)),
                    handleAs: "json",
                    headers: { "Content-Type": "application/json; charset=uft-8" }
                }).then(function(response){
                    if (response){
                        if (response.errorText){
                            var dialog = thisContext.getPromptDialog(null, response.errorText);
                            dialog.show();
                            return;
                        }
                        callback(response);
                    }else{
                        thisContext.hideProgressBarDialog(null, 100);
                        thisContext.getPromptDialog(null, "Состояние карточки изменилось. Необходимо закрыть и затем открыть карточку повторно.").show();
                    }
                }, function (err){
                    var errText = "Состояние карточки изменилось. Необходимо закрыть и открыть карточку повторно";
                    if (err){
                        if (err.response){
                            if (err.response.text){
                                errText = err.response.text;
                            }
                        }
                    }
                    // Handle the error condition
                    callbackQueryError(errText);
                }, function(evt){
                    // Handle a progress event from the request if the
                    // browser supports XHR2
                });
            }else{
                function callbackNotificationError(){
                    thisContext.showNotification("Ошибка при сохранении карточки");
                }
                function callbackError(postProcessMessage){
                    thisContext.hideProgressBarDialog(null, 100);
                    glFormWidget.getButtonByType("change_status").setDisabled(false);
                    var dialog = thisContext.getPromptDialog(null, postProcessMessage);
                    dialog.show();
                    callbackNotificationError();
                };
                function callbackSuccess(){
                    thisContext.hideProgressBarDialog(100, 100);
                    //Обновить все открытые представления
                    thisContext.refreshGrids();
                    //Смена статуса после сохранения карточки
                    thisContext.changeStatus(formWidget, targetStatus, false);
                };
                thisContext.saveForm(formWidget, callbackError, callbackSuccess);
            }
        },

        refreshSidebar: function(type){
            switch (type){
                case 'extra':
                    this.extraContainer.destroyDescendants()
                    this.extraContainer.addChild(this._buildSidebarTree(type));
                    break;
                default:
            }
        },

        /*
         deleteForm: function(formWidget){
         var dialog = new ConfirmDialog({
         title: "Сообщение",
         content: "Вы действительно хотите удалить элемент?",
         style: "width: 300px",
         appWidget:this
         });
         dialog.set("buttonOk","Да");
         dialog.set("buttonCancel","Нет");
         dialog.on('execute', function() {
         var thisContext = this;
         new JsonRest({
         target: "form/" + formWidget.template
         }).remove(formWidget.response.attributes.id.value).then(function(response){
         thisContext.appWidget.closeTab(formWidget);
         //Обновить все открытые представления
         thisContext.appWidget.refreshGrids();
         }, function (err){
         getPromptDialog(null, ("Ошибка удаления: " + (err.responseText || ""))).show();
         });
         });
         dialog.on('cancel', function() {});dialog.show();
         },
         */

        containsFormTabId: function(viewItemArray){
            var tab = null;
            var thisContext = this;
            viewItemArray.some(function(item) {
                var tmpTab = thisContext._getTabById(thisContext.tabContainer.getChildren(), 'form_' + item.type[0] + '_' + item.cardId[0]);
                if (tmpTab){
                    tab = tmpTab;
                    return;
                }
            });
            return tab;
        },

        deleteFormInView: function(selectedItems, refreshView){
            var firstTab = this.containsFormTabId(selectedItems);
            var isEmbView = false;
            if (!!refreshView){
                if (!!refreshView.thisWidget){
                    if (!!refreshView.thisWidget.formWidget){
                        isEmbView = true;
                    }
                }
            }
            if (firstTab && !isEmbView){
                this.getPromptDialog(null, "Ошибка удаления: открыта вкладка удаляемой карточки " + firstTab.title).show();
            }else{
                var v = [];
                selectedItems.some(function(entry) {
                    if (entry.realType && entry.realCardId){
                        v.push({template: entry.realType[0], id: entry.realCardId[0]});
                    }else{
                        v.push({template: entry.type[0], id: entry.cardId[0]});
                    }
                });
                var thisContext = this;
                this.getConfirmDialog(null,
                    "Вы действительно хотите удалить " + (selectedItems.length > 1 ? "выделенные карточки" : "выделенную карточку") + "?",
                    {ok: function(){
                        request("form/remove", {
                            method: "post",
                            data: JSON.stringify(v),
                            headers: { "Content-Type": "application/json; charset=uft-8" },
                            handleAs: "json"
                        }).then(function(response){
                            if (response.errorText){
                                var dialog = thisContext.getPromptDialog(null, response.errorText);
                                dialog.show();
                                return;
                            }
                            thisContext.showNotification((selectedItems.length > 1 ? "Выделенные карточки успешно удалены" : "Выделенная карточка успешно удалена"));
                            if (refreshView){
                                refreshView.customRefresh(); //Обновить только переданное представление
                            }else{
                                thisContext.refreshGrids(); //Обновить все открытые представления
                            }
                        }, function (err){
                            thisContext.getPromptDialog(null, ("Ошибка удаления: " + (err.responseText || ""))).show();
                        });
                    },
                        cancel: function(){}}
                ).show();
            }
        },

        editForm: function(formWidget){
            this.openTabForm(false, {type: {0:formWidget.template}, id: {0:formWidget.response.attributes.id.value}, name: {0:formWidget.title}, editMode: true})
        },

        closeTab: function(tab){
            var cur = tab;
            var prev = tab;
            while (cur.declaredClass != "dijit.layout.TabContainer"){
                prev = cur;
                cur = cur.getParent();
            }
            var tabContainer = cur;
            var tab = prev;
            tabContainer.removeChild(tab);
            tab.destroy();
        },

        getDataByUrl: function(url){
            var thisContext = this;
            var result = null;
            request(url, {
                method: "get",
                sync: true,
                handleAs: "json"
            }).then(function(response){
                result = response;
            }, function (err){
                var errText = "Возникла неизвестная ошибка. Необходимо связаться с администратором системы";
                if (err){
                    if (err.response){
                        if (err.response.text){
                            errText = err.response.text;
                        }
                    }
                }
                // Handle the error condition
                thisContext.getPromptDialog(null, errText).show();
            }, function(evt){
                // Handle a progress event from the request if the
                // browser supports XHR2
            });
            return result;
        },

        showFormDialog: function(formWidget, removeParentFormWidget){
            var saveBtn = formWidget.getButtonByType("save");
            domStyle.set(saveBtn.domNode, "display", "none");
            var saveBtnParam = formWidget.getButtonParamByType("save");
            var cp = new ContentPane({
                style:{overflow: "hidden"},
                class: 'fixFormDialogBlock'
            });
            if (formWidget.editMode){
                var cp1 = new ContentPane({style:{padding:"0px", border: "1px solid #759dc0", width: '100%', height: '90%'}});
                cp1.addChild(formWidget);
                var cpBtnOk = new ContentPane({class: "dialogBtn"});
                var btnOk = new Button({label: "ОК"});
                cpBtnOk.addChild(btnOk);
                var cpBtnCancel = new ContentPane({class: "dialogBtn"});
                var btnCancel = new Button({label: "Отмена"});
                cpBtnCancel.addChild(btnCancel);
                var cp2 = new ContentPane({style:{padding:"0px", width: '100%', height: '100%'}});
                cp2.addChild(cpBtnOk);
                cp2.addChild(cpBtnCancel);
            }else{
                cp.addChild(formWidget);
            }
            if (formWidget.editMode){
                cp.addChild(cp1);
                cp.addChild(cp2);
            }
            var dialog = new Dialog({
                content: cp,
                style: {height: 'auto%'},
                closable: (!formWidget.editMode),
                title: (formWidget.title ? formWidget.title : "")
            });
            var thisContext = this;
            if (formWidget.editMode){
                on(btnOk, "click", function(e){
                    var thisContextBtn = this;
                    function callbackError(errorMessage){
                        thisContextBtn.setDisabled(false);
                        thisContext.hideProgressBarDialog(null, 100);
                        if (errorMessage){
                            var dialog1 = thisContext.getPromptDialog(null, errorMessage);
                            dialog1.show();
                        }
                    };
                    function callbackSuccess(){
                        thisContext.hideProgressBarDialog(100, 100);
                        formWidget.embView.gridView.customRefresh();
                        if (removeParentFormWidget){
                            formWidget.parentFormWidget.destroyDescendants(false);
                            formWidget.parentFormWidget.destroy();
                        }
                        dialog.destroyDescendants(false);
                        dialog.destroy();
                    };
                    this.setDisabled(true);
                    var f = function (formWidget, callbackError, callbackSuccess) {eval("(function(formWidget, callbackError, callbackSuccess){" + saveBtnParam.onClick + "})").call(thisContext, formWidget, callbackError, callbackSuccess);};
                    f(formWidget, callbackError, callbackSuccess);
                });
                on(btnCancel, "click", function(e){
                    if (removeParentFormWidget){
                        formWidget.parentFormWidget.destroyDescendants(false);
                        formWidget.parentFormWidget.destroy();
                    }
                    dialog.destroyDescendants(false);
                    dialog.destroy();
                });
            }
            dialog.on('cancel', function() {
                if (removeParentFormWidget){
                    formWidget.parentFormWidget.destroyDescendants(false);
                    formWidget.parentFormWidget.destroy();
                }
                dialog.destroyDescendants(false);
                dialog.destroy();
            });
            dialog.show();
            dialog.resize();
        },

        showCustomDialog: function (url, multiple, callback, transformCallback, title){
            var thisContext = this;
            request(url, {
                handleAs: "json"
            }).then(function(response){
                var selected = undefined;
                var cp = new ContentPane({style:{overflow: "hidden"}});
                var cp1 = new ContentPane({style:{padding:"0px", border: "1px solid #759dc0"}});
                var cp2 = new ContentPane({style:{padding:"0px"}});
                var tree = new Tree({
                    model: new ForestStoreModel({
                        store: new ItemFileReadStore({data:response.data}),
                        childrenAttrs: ["children"]
                    }),
                    class:"dialogGridView",
                    showRoot:false,
                    openOnClick:true,
                    onClick: function (item, node, evt) {
                        if (multiple){
                            selected = this.selectedItems;
                        }else{
                            selected = item;
                        }
                    },
                    getIconClass: function(item, opened){return '';}
                });
                cp1.addChild(tree);
                var cpBtnOk = new ContentPane({class: "dialogBtn"});
                var btnOk = new Button({label: "ОК"});
                cpBtnOk.addChild(btnOk);
                var cpBtnCancel = new ContentPane({class: "dialogBtn"});
                var btnCancel = new Button({label: "Отмена"});
                cpBtnCancel.addChild(btnCancel);
                cp2.addChild(cpBtnOk);
                cp2.addChild(cpBtnCancel);
                cp.addChild(cp1);
                cp.addChild(cp2);
                var dialog = new Dialog({
                    title: ((!!title) ? title : ""),
                    style: "min-width: 300px",
                    content: cp,
                    closable: false
                });
                on(btnOk, "click", function(e){
                    var isValid = (multiple ? selected.length : !!selected);
                    if (!isValid){
                        thisContext.getPromptDialog(null, "Необходимо выбрать карточку").show();
                        return;
                    }else{
                        dialog.destroy();
                        callback(transformCallback(selected));
                    }
                });
                on(btnCancel, "click", function(e){dialog.destroy();});
                dialog.on('cancel', function() {
                    dialog.destroyDescendants(false);
                    dialog.destroy();
                });
                dialog.show();

            });

        },

        _preparePersonVariantData: function(tabContainer){
            var result = [];
            tabContainer.getChildren().some(function(tab) {
                var test = {};
                test.id = tab.id.split('_')[1];
                test.personIdToVariantNumber = [];
                dojo.query(".dijitSelect", tab.domNode).forEach(function(select, index, arr){
                    var select = dijit.byId(select.attributes.widgetid.value);
                    test.personIdToVariantNumber.push({personId:select.attr('name').split('_')[3], variantNumber: select.getValue()});
                });
                result.push(test);
            });
            return result;
        },

        _prepareAnswerData: function(tab){
            var result = [];
            query("input:checked", tab.domNode).forEach(function(node, index, arr){
                var tmpArr = node.id.split('_');
                result.push({questionId: tmpArr[1], answerId: tmpArr[3]});
            });
            return result;
        },

        _prepareCurrentQuestionData: function(tab){
            var result = null;
            query("input", tab.domNode).forEach(function(node, index, arr){
                var tmpArr = node.id.split('_');
                result = tmpArr[1];
                return;
            });
            return result;
        },

        _prepareAnswerFullData: function(tabContainer){
            var resultArr = [];
            tabContainer.getChildren().some(function(tab) {
                var result = [];
                query("input:checked", tab.domNode).forEach(function(node, index, arr){
                    var tmpArr = node.id.split('_');
                    result.push({questionId: tmpArr[1], answerId: tmpArr[3]});
                });
                resultArr.push(result);
            });
            return resultArr;
        },

        _validateAnswerFullData: function(resultArr){
            var result = [];
            query("input:checked", tab.domNode).forEach(function(node, index, arr){
                var tmpArr = node.id.split('_');
                result.push({questionId: tmpArr[1], answerId: tmpArr[3]});
            });
            return result;
        },

        _validateTestForm: function(tabContainer){
            var thisContext = this;
            var isValid = true;
            tabContainer.getChildren().some(function(tab) {
                if(!tab.get('disabled')){
                    if (thisContext._prepareAnswerData(tab).length == 0){
                        isValid = false;
                    }
                }
            });
            return isValid;
        },

        showBlockChangeDialog: function (viewWidget, item){
            var glViewWidget = viewWidget;
            var cp = new ContentPane({style:{overflow: "hidden"}});
            var cp1 = new ContentPane({style:{padding:"0px", textAlign: "center"}});
            var cp2 = new ContentPane({style:{padding:"0px"}});

            var template = this.formBlockEdit;
            cp1.set('content', template);

            var block = dijit.byId('block_dialog', cp1);
            block.setValue(item.block[0])

            var radio = dijit.byId('type_' + item.blockType[0], cp1);
            radio.setChecked(true);

            var cpBtnOk = new ContentPane({
                class: "dialogBtn"
            });
            var btnOk = new Button({
                label: "ОК"
            });
            cpBtnOk.addChild(btnOk);
            var cpBtnCancel = new ContentPane({
                class: "dialogBtn"
            });
            var btnCancel = new Button({
                label: "Отмена"
            });
            cpBtnCancel.addChild(btnCancel);
            cp2.addChild(cpBtnOk);
            cp2.addChild(cpBtnCancel);
            cp.addChild(cp1);
            cp.addChild(cp2);
            var dialog = new Dialog({
                title: "Изменение блока",
                style: "min-width: 300px",
                content: cp,
                closable: false
            });

            var thisContext = this;
            on(btnOk, "click", function(e){
                var blockWidget = dijit.byId("block_dialog");
                var typeWidget = dijit.byId("type_dialog");
                if (blockWidget.validate() && !!typeWidget.attr("value").type){
                    var url = "form/" + item.realType[0] + "/" + item.realCardId[0] + "/to_block/" + blockWidget.getValue() + '/to_block_type/' + typeWidget.attr("value").type;
                    dialog.destroyDescendants(false);
                    dialog.destroy();
                    request(url, {
                        method: "post",
                        data: JSON.stringify({}),
                        handleAs: "json",
                        headers: { "Content-Type": "application/json; charset=uft-8" }
                    }).then(function(response){
                        glViewWidget.gridView.customRefresh();
                    }, function (err){
                        thisContext.getPromptDialog(null, "Ошибка при изменении блока: " + (err.responseText || "")).show();
                    });
                }
            });
            on(btnCancel, "click", function(e){
                dialog.destroyDescendants(false);
                dialog.destroy();
            });
            dialog.on('cancel', function() {
                dialog.destroyDescendants(false);
                dialog.destroy();
            });
            dialog.show();
        },

        showSelectPersonVariantDialog: function(params){
            var thisContext = this;
            params.clickedItem.setDisabled(true);
            function callbackErrorParam(){
                params.clickedItem.setDisabled(false);
                thisContext.getPromptDialog(null, "Возникла неизвестная ошибка. Необходимо связаться с администратором системы").show();
            }
            function callbackSuccessParam(){
                var responseGet = thisContext.getDataByUrl('form/' + params.changeStatusParams.form.template + '/' + params.changeStatusParams.form.response.attributes.id.value);
                var url = 'form/person/test/variants/' + responseGet.attributes.group.value.id.value + '/' + responseGet.template + '/' + responseGet.attributes.id.value;
                var responseGet2 = thisContext.getDataByUrl(url);
                if (responseGet2.hashMapTemplateTestArrayList.length > 0){
                    //
                    var cp = new ContentPane({style: {overflow: 'hidden'}});
                    var cp1 = new ContentPane({});
                    var cp2 = new ContentPane({});
                    var tc = new TabContainer({
                        id: 'tabContainerForPersonVariant',
                        tabPosition: "top",
                        style: {width: '100%', height: '75%'}
                    });

                    cp1.addChild(tc);
                    var cpBtnOk = new ContentPane({class: "dialogBtn"});
                    var btnOk = new Button({label: "ОК"});
                    cpBtnOk.addChild(btnOk);
                    var cpBtnCancel = new ContentPane({class: "dialogBtn"});
                    var btnCancel = new Button({label: "Отмена"});
                    cpBtnCancel.addChild(btnCancel);
                    cp2.addChild(cpBtnOk);
                    cp2.addChild(cpBtnCancel);
                    cp.addChild(cp1);
                    cp.addChild(cp2);

                    //

                    responseGet2.hashMapTemplateTestArrayList.some(function(templateTest) {

                        var tab = new ContentPane({
                            title: templateTest.name.value,
                            id: 'templateTest_' + templateTest.id.value
                        });

                        var variantOptions = [];
                        templateTest.variant_list.value.some(function(variant) {
                            variantOptions.push({label: 'Вариант ' + variant.number.value,value: variant.number.value});
                        });

                        var table = dojo.create("table", {style: {width: '100%'}});

                        responseGet2.hashMapPersonArrayList.some(function(person) {

                            var label = dojo.create("label");
                            label.innerHTML = person.surname.value + " " + person.name.value + " " + person.middlename.value;

                            var select = new Select({
                                name: 'templateTest_' + templateTest.id.value + '_select_' + person.id.value,
                                options: dojo.clone(variantOptions)
                            });

                            var tr = dojo.create("tr");
                            var td1 = dojo.create("td", {style: {width: '100%'}});
                            var td2 = dojo.create("td");

                            dojo.place(label, td1);
                            dojo.place(select.domNode, td2);
                            dojo.place(td1, tr);
                            dojo.place(td2, tr);
                            dojo.place(tr, table);

                        });

                        tab.set('content', table);

                        tc.addChild(tab);

                    });
                    //

                    var dialog = new Dialog({
                        content: cp,
                        closable: (!params.editMode),
                        style: {width: '30%'}
                    });
                    on(btnOk, "click", function(e){

                        var formWidget = params.changeStatusParams.form;
                        var targetStatus = params.changeStatusParams.status;

                        var sendData = thisContext._preparePersonVariantData(tc);
                        thisContext.changeStatus(formWidget, targetStatus, false, null, null, null, null, sendData);
                        dialog.destroyDescendants(false);
                        dialog.destroy();
                    });
                    on(btnCancel, "click", function(e){
                        params.clickedItem.setDisabled(false);
                        thisContext.hideProgressBarDialog(null, 100);
                        dialog.destroyDescendants(false);
                        dialog.destroy();
                    });
                    dialog.on('cancel', function() {
                        params.clickedItem.setDisabled(false);
                        thisContext.hideProgressBarDialog(null, 100);
                        dialog.destroyDescendants(false);
                        dialog.destroy();
                    });
                    dialog.show();
                    //
                }else{
                    params.changeStatusParams.form.response = responseGet;
                    thisContext.changeStatus(params.changeStatusParams.form, params.changeStatusParams.status, false);
                }

            }
            this.saveForm(params.changeStatusParams.form, callbackErrorParam, callbackSuccessParam);
        },

        showTestDialog: function (params){

            var thisContext = this;
            var result = this.getDataByUrl('form/' + params.type + "/" + params.editMode + '/' + params.cardId);
            var testVariantPersonId = result.attributes.original_id.value;
            var deprecateChangeAnswerCount = result.attributes.deprecateChangeAnswerCount.value;
            var deprecateChangeAnswer = (deprecateChangeAnswerCount > 0);
            var byOrder = result.attributes.byOrder.value;
            var timeLimit = (result.attributes.timeLimit.value > 0);
            var statusNew = false;

            var lastTime = result.attributes.original_lasttime.value;

            var statusCode = result.attributes.original_status.value.code.value;
            if (params.editMode){
                if (statusCode !== "assigned" && statusCode !== "in_progress"){
                    return;
                }else{
                    statusNew = (statusCode === "assigned");
                }
            }
            var currentQuestionIdProperty = result.attributes.original_testvariantperson.value.currentTemplateTestQuestion.value.id.value;
            var checkTestTimer = null;

            var questions = result.attributes.variant.value.attributes.questions.value;
            //
            var cp = new ContentPane({style: {overflow: 'hidden'}});
            var cp1 = new ContentPane({});
            var cp2 = new ContentPane({});
            var tc = new TabContainer({
                id: 'tabContainerForTest',
                tabPosition: "bottom",
                deprecateChangeAnswerCount: [],
                deprecateChangeAnswerVisit: [],
                pageIdToQuestion:[],
                questionIdToPageId:[],
                pageIdToDisabled:[],
                style: {width: '100%', height: '75%'}
            });

            var currentTime = lastTime;
            function updateTimer(longValue){
                if (timeLimit){
                    currentTime = longValue;
                    timeLabel.innerHTML = 'Оставшееся время: ' + thisContext.msToTime(longValue);
                }
            }

            var timer = new timing.Timer(1000);
            timer.onTick = function(){
                currentTime = currentTime - 1000;
                if (currentTime < 0){
                    currentTime = 0;
                    if (params.editMode){
                        if (checkTestTimer){
                            clearTimeout(checkTestTimer);
                        }
                        dialog.destroyDescendants(false);
                        dialog.destroy();
                        if (timeLimit){
                            timer.stop();
                        }
                        thisContext.getPromptDialog(null, "Время прохождения теста истекло").show();
                    }
                }
                updateTimer(currentTime);
                console.log('Обновление таймера активно');
            };

            var timeLabel = dojo.create('label');
            var timerCp = new ContentPane({
                content: timeLabel,
                style: {display: (timeLimit ? "block" : "none")}
            });

            cp1.addChild(timerCp);

            cp1.addChild(tc);

            if (!params.editMode){
                updateTimer(currentTime);
            }

            var cpBtnOk = new ContentPane({class: "dialogBtn"});
            var btnOk = new Button({label: "ОК", style: {display: (params.editMode ? 'block' : 'none')}});
            cpBtnOk.addChild(btnOk);
            var cpBtnCancel = new ContentPane({class: "dialogBtn"});
            var btnCancel = new Button({label: "Отмена", style: {display: (params.editMode ? 'block' : 'none')}});
            cpBtnCancel.addChild(btnCancel);
            cp2.addChild(cpBtnOk);
            cp2.addChild(cpBtnCancel);
            cp.addChild(cp1);
            cp.addChild(cp2);

            var activate = 0;

            questions.some(function(question) {

                var formWidget = {
                    isNew: false,
                    editMode: false,
                    response: dojo.clone(question),
                    template: question.template
                };

                var questionId = "question_" + question.attributes.id.value;
                var questionText = question.attributes.content.value;
                var answerNodeContent = "<H1>" + questionText + "</H1><br class='forRemove'><br class='forRemove'><div data-dojo-type='widgets/ResourceWidget' data-dojo-props='formWidget:" + JSON.stringify(formWidget) + "' style='padding-left:\"8px\"; height: \"auto\"; text-align: \"center\";'></div>";
                var questionCp = new ContentPane({
                    id: questionId,
                    content: answerNodeContent//,
                    //style: {width: '100%', height: '100%'}
                });
                if (query('.ResourceWidget', questionCp.domNode)[0].firstElementChild.style.display === "none"){
                    query('.forRemove', questionCp.domNode).forEach(function(node, index, arr){
                        dojo.destroy(node);
                    });
                }
                var answers = question.attributes.answers.value;
                var answersNode = "";
                answers.some(function(answer) {
                    var answerId = 'question_' + question.attributes.id.value + '_answer_' + answer.attributes.id.value;
                    var checked = answer.attributes.checked.value;
                    var answerText = answer.attributes.content.value;
                    var color = (params.editMode ? "" : (answer.attributes.correct.value ? "color:green" : "color:red"));
                    var answerNodeContent1 = "<br><br><input " + (params.editMode ? "" : "disabled") + " id='" + answerId + "' name='" + answerId + "' data-dojo-type='dijit/form/CheckBox' " + (checked ? "checked" : "") + " /> <label style='" + color + "' for='" + answerId + "'>" + answerText + "</label>";
                    answersNode += answerNodeContent1;
                });
                var answerCp = new ContentPane({
                    content: answersNode//,
                    //style: {width: '100%', height: '100%'}
                });
                var firstAnswerBr= dojo.query('br', answerCp.domNode)[0];
                if (firstAnswerBr){
                    dojo.destroy(firstAnswerBr);
                }
                var questionNumber = question.attributes.number.value;

                var contentCp = new ContentPane({
                    title: questionNumber,
                    style: {width: '100%', height: '100%'}
                });

                contentCp.addChild(questionCp);
                contentCp.addChild(answerCp);

                tc.addChild(contentCp);

                if (statusNew){
                    tc.deprecateChangeAnswerCount[contentCp.id] = deprecateChangeAnswerCount;
                    tc.deprecateChangeAnswerVisit[contentCp.id] = false;
                }else{
                    tc.deprecateChangeAnswerCount[contentCp.id] = question.attributes.question.value.currentDeprecateChangeAnswerCounter.value;
                    tc.deprecateChangeAnswerVisit[contentCp.id] = (question.attributes.question.value.visited.value);
                }
                tc.pageIdToQuestion[contentCp.id] = question;
                
                tc.questionIdToPageId[question.attributes.id.value] = contentCp.id;
                
                if (currentQuestionIdProperty == question.attributes.id.value){
                    tc.selectChild(contentCp);
                }
                tc.pageIdToDisabled[contentCp.id] = false;
                if (deprecateChangeAnswerCount > 0){
                    if (question.attributes.question.value.currentDeprecateChangeAnswerCounter.value == 0){
                        if (params.editMode){
                            contentCp.set('disabled', true);
                        }
                        tc.pageIdToDisabled[contentCp.id] = true;
                    }
                }

            });

            function setTabTitle(tc, contentCp){
                if (byOrder || deprecateChangeAnswer){
                    if (!tc.pageIdToDisabled[contentCp.id] && !tc.deprecateChangeAnswerVisit[contentCp.id]){
                        domStyle.set(dijit.byId('tabContainerForTest_tablist_' + contentCp.id).domNode, "backgroundColor", "#98d898");
                    }else{
                        domStyle.set(dijit.byId('tabContainerForTest_tablist_' + contentCp.id).domNode, "backgroundColor", "#d89898");
                    }
                }
                var title = tc.pageIdToQuestion[contentCp.id].attributes.number.value;
                if (byOrder){
                    //Nothing
                }else if (deprecateChangeAnswer){
                    title = title + ' (' + tc.deprecateChangeAnswerCount[contentCp.id] + ')';
                }
                dojo.byId('tabContainerForTest_tablist_' + contentCp.id).innerHTML = title;
            }

            function tabContainerPostProcessing(){
                //if (params.editMode){
                    var tc = dijit.byId('tabContainerForTest');
                    var byOrderClickedFounded = 0;
                    query('.dijitTabInner', tc.domNode).forEach(function(node, i){
                        var tabId = node.attributes.widgetid.value.split('tabContainerForTest_tablist_')[1];
                        var tab = dijit.byId(tabId);
                        if (statusNew){
                            if (byOrder && i > 1){
                                if (params.editMode){
                                    tab.set('disabled', true);
                                }
                                tc.pageIdToDisabled[tab.id] = true;
                            }
                        }else{
                            if (byOrder){
                                if (tc.questionIdToPageId[currentQuestionIdProperty] == tab.id) {
                                    byOrderClickedFounded++;
                                    if (params.editMode) {
                                      tab.set('disabled', false);
                                    }
                                    tc.pageIdToDisabled[tab.id] = false;
                                }else{
                                    if (byOrderClickedFounded == 1){
                                        byOrderClickedFounded++;
                                        if (params.editMode) {
                                            tab.set('disabled', false);
                                        }
                                        tc.pageIdToDisabled[tab.id] = false;
                                    }else{
                                        if (params.editMode) {
                                            tab.set('disabled', true);
                                        }
                                        tc.pageIdToDisabled[tab.id] = true;
                                    }
                                }
                            }
                        }
                        setTabTitle(tc, tab);
                        if (params.editMode){
                            on(node, "click", function(e){
                                var clicked = dijit.byId(e.currentTarget.attributes.widgetid.value);
                                var clickedId = clicked.id.split('tabContainerForTest_tablist_')[1];
                                var clicked = dijit.byId(clickedId);
                                var current = tc.selectedChildWidget;
                                var currentId = current.id;
                                if (clickedId == currentId){
                                    e.stopPropagation();
                                    return;
                                }
                                if (clicked.disabled){
                                    e.stopPropagation();
                                    return;
                                }

                                function sendAnswers () {
                                    if (params.editMode){
                                        if (byOrder){
                                            if (params.editMode) {
                                                current.set('disabled', true);
                                            }
                                            tc.pageIdToDisabled[current.id] = true;
                                            setTabTitle(tc, current);
                                            var curTabFounded = false;
                                            var needStop = false;
                                            tc.getChildren().some(function(tab) {
                                                if (tab.id == clicked.id){
                                                    curTabFounded = true;
                                                }else{
                                                    if (curTabFounded){
                                                        if (!needStop){
                                                            if (params.editMode) {
                                                                tab.set('disabled', false);
                                                            }
                                                            tc.pageIdToDisabled[tab.id] = false;
                                                            setTabTitle(tc, tab);
                                                            needStop = true;
                                                        }
                                                    }
                                                }
                                            });
                                        }else if (deprecateChangeAnswer){
                                            if (!tc.deprecateChangeAnswerVisit[currentId]){
                                                tc.deprecateChangeAnswerVisit[currentId] = true;
                                            }else{
                                                tc.deprecateChangeAnswerCount[currentId] = tc.deprecateChangeAnswerCount[currentId] - 1;
                                                if (tc.deprecateChangeAnswerCount[currentId] == 0){
                                                    if (params.editMode) {
                                                        current.set('disabled', true);
                                                    }
                                                    tc.pageIdToDisabled[current.id] = true;
                                                }
                                            }
                                            setTabTitle(tc, current);
                                        }

                                        var sendData = thisContext._prepareAnswerData(current);
                                        var questionId = thisContext._prepareCurrentQuestionData(current);
                                        var clickedQuestionId = thisContext._prepareCurrentQuestionData(clicked);
                                        var finalSendData = {
                                            answerContainers: sendData,
                                            deprecateChangeAnswerCount: tc.deprecateChangeAnswerCount[currentId],
                                            clickedQuestionId: clickedQuestionId
                                        };
                                        request("form/trainingtestvariant/" + testVariantPersonId + "/" + questionId + "/answer", {
                                            method: "post",
                                            data: JSON.stringify(finalSendData),
                                            handleAs: "json",
                                            headers: { "Content-Type": "application/json; charset=uft-8" }
                                        }).then(function(response){
                                            console.log('Промежуточные результаты теста сохранены');
                                        }, function (err){
                                            console.log('Промежуточные результаты теста не удалось сохранить');
                                        });
                                    }
                                }

                                var sendData = thisContext._prepareAnswerData(current);
                                if (sendData.length == 0 && byOrder){
                                    e.stopPropagation();
                                    thisContext.getConfirmDialog(null,
                                        "Ответить позже на данный вопрос будет невозможно. Уверены, что хотите пропустить вопрос?",
                                        {
                                            ok: function(){
                                                tc.selectChild(clicked);
                                                sendAnswers();
                                            },
                                            cancel: function(){
                                                e.stopPropagation();
                                                return;
                                            }
                                        }
                                    ).show();
                                }else{
                                    sendAnswers();
                                }

                            })
                        }
                    });
                //}
            }

            var dialog = new Dialog({
                content: cp,
                closable: (!params.editMode),
                style: {width: '100%', height: '100%'}
            });
            on(btnOk, "click", function(e){
                thisContext.getConfirmDialog(null,
                    "Уверены, что хотите завершить тестирование?",
                    {
                        ok: function(){

                            var tc = dijit.byId('tabContainerForTest');
                            //var questions = thisContext._prepareAnswerFullData(tc);
                            var current = tc.selectedChildWidget;
                            var questions = thisContext._prepareAnswerData(current)

                            var callbackDialogSuccess = function(){
                                var callbackSuccess = function(){
                                    thisContext.hideProgressBarDialog(null, 100);
                                    thisContext.showNotification("Тестирование завершено успешно");
                                    if (checkTestTimer){
                                        clearTimeout(checkTestTimer);
                                    }
                                    if (timeLimit){
                                        timer.stop();
                                    }
                                    dialog.destroyDescendants(false);
                                    dialog.destroy();
                                };
                                var callbackError = function(){
                                    thisContext.hideProgressBarDialog(null, 100);
                                    thisContext.showNotification("Ошибка при завершении тестирования");
                                };
                                thisContext.changeStatus(this, 'completed', false, {realType: params.type, realCardId: params.cardId}, callbackSuccess, callbackError, 'form/' + params.type + '/completedtesting/' + tc.pageIdToQuestion[current.id].attributes.id.value + '/' + params.cardId, questions);
                            };
                            if (thisContext._validateTestForm(tc)){
                                callbackDialogSuccess();
                            }else{
                                thisContext.getConfirmDialog(null,
                                    "Не на все вопросы были даны ответы. Хотите завершить тестирование досрочно?",
                                    {
                                        ok: function(){
                                            callbackDialogSuccess();
                                        },
                                        cancel: function(){

                                        }
                                    }
                                ).show();
                            }

                        },
                        cancel: function(){

                        }
                    }
                ).show();
            });
            on(btnCancel, "click", function(e){
                if (checkTestTimer){
                    clearTimeout(checkTestTimer);
                }
                if (timeLimit){
                    timer.stop();
                }
                dialog.destroyDescendants(false);
                dialog.destroy();
            });
            dialog.on('cancel', function() {
                if (checkTestTimer){
                    clearTimeout(checkTestTimer);
                }
                if (timeLimit){
                    timer.stop();
                }
                dialog.destroyDescendants(false);
                dialog.destroy();
            });
            //
            function checkTest(){
                dojo.xhrGet({
                    url: 'form/trainingtestvariant/checking/' + testVariantPersonId,
                    load: function(data){
                        var lastTime = data.split('<Long>')[1].split('</Long>')[0];
                        updateTimer(lastTime);
                    },
                    error: function(request, ioArgs) {
                        if (checkTestTimer){
                            clearTimeout(checkTestTimer);
                        }
                        dialog.destroyDescendants(false);
                        dialog.destroy();
                        if (timeLimit){
                            timer.stop();
                        }
                        thisContext.getPromptDialog(null, "Время прохождения теста истекло").show();
                    }
                });
                console.log('Проверка доступности теста активна');
                checkTestTimer = setTimeout(checkTest, 30000);
            }
            //
            if (params.editMode){
                var thisContext = this;
                this.getConfirmDialog(null,
                    (statusNew ? "Начать тестирование?" : "Продолжить тестирование?"),
                    {
                        ok: function(){
                            if (statusCode === "assigned"){
                                var callbackSuccess = function(){
                                    thisContext.hideProgressBarDialog(null, 100);
                                    thisContext.showNotification("Статус изменён успешно");
                                    dialog.show();
                                    checkTest();
                                    if (timeLimit){
                                        timer.start();
                                    }
                                    tabContainerPostProcessing();
                                };
                                var callbackError = function(){
                                    thisContext.hideProgressBarDialog(null, 100);
                                    thisContext.showNotification("Состояние карточки изменилось. Необходимо закрыть и открыть карточку повторно");
                                };
                                thisContext.changeStatus(this, 'in_progress', false, {realType: params.type, realCardId: params.cardId}, callbackSuccess, callbackError);
                            }else{
                                dialog.show();
                                checkTest();
                                if (timeLimit){
                                    timer.start();
                                }
                                tabContainerPostProcessing();
                            }
                        },
                        cancel: function(){
                            dialog.destroyDescendants(false);
                            dialog.destroy();
                        }
                    }
                ).show();
            }else{
                dialog.show();
                if (timeLimit){
                    timer.start();
                }
                tabContainerPostProcessing();
            }

        },

        msToTime: function(s) {
            var ms = s % 1000;
            s = (s - ms) / 1000;
            var secs = s % 60;
            s = (s - secs) / 60;
            var mins = s % 60;
            var hrs = (s - mins) / 60;

            secs = ((secs + '').length < 2 ? '0' + secs : secs);
            mins = ((mins + '').length < 2 ? '0' + mins : mins);
            hrs = ((hrs + '').length < 2 ? '0' + hrs : hrs);

            return hrs + ':' + mins + ':' + secs;// + '.' + ms;
        },

        showPasswordDialog: function (formWidget){
            var glFormWidget = formWidget;
            var cp = new ContentPane({style:{overflow: "hidden"}});
            var cp1 = new ContentPane({style:{padding:"0px", border: "1px solid #759dc0"}});
            var cp2 = new ContentPane({style:{padding:"0px"}});
            var field = new TextBox({
                style: {width: "99%"}
            });
            cp1.addChild(field);
            var cpBtnOk = new ContentPane({
                class: "dialogBtn"
            });
            var btnOk = new Button({
                label: "ОК"
            });
            cpBtnOk.addChild(btnOk);
            var cpBtnCancel = new ContentPane({
                class: "dialogBtn"
            });
            var btnCancel = new Button({
                label: "Отмена"
            });
            cpBtnCancel.addChild(btnCancel);
            cp2.addChild(cpBtnOk);
            cp2.addChild(cpBtnCancel);
            cp.addChild(cp1);
            cp.addChild(cp2);
            var dialog = new Dialog({
                title: "Изменение пароля",
                style: "min-width: 300px",
                content: cp,
                closable: false
            });
            var thisContext = this;
            on(btnOk, "click", function(e){
                if (field.value.trim() === ''){
                    thisContext.getPromptDialog(null, "Необходимо ввести пароль").show();
                }else{
                    if (field.value.trim().length < 5 || field.value.trim().length > 25){
                        thisContext.getPromptDialog(null, "Пароль должен содержать от 5 до 25 символов").show();
                        return;
                    }
                    dialog.destroy();
                    var sendData = glFormWidget.getFormObject();
                    sendData.password = field.value.trim();
                    request("form/changePassword", {
                        method: "post",
                        data: JSON.stringify(sendData),
                        handleAs: "json",
                        headers: { "Content-Type": "application/json; charset=uft-8" }
                    }).then(function(response){
                        if (response.errorText){
                            var dialog = thisContext.getPromptDialog(null, response.errorText);
                            dialog.show();
                            return;
                        }
                        thisContext.openTabForm(false, {
                            type: {0:response.template},
                            name: {0:response.tabTitle},
                            editMode:true,
                            reopen: glFormWidget.isNew,
                            id: {0: response.attributes.id.value},
                            tabId: glFormWidget.id
                        });
                        thisContext.getPromptDialog(null, "Пароль успешно изменён").show();
                    }, function (err){
                        thisContext.getPromptDialog(null, "Ошибка при изменении пароля: " + (err.responseText || "")).show();
                    });
                }
            });
            on(btnCancel, "click", function(e){
                dialog.destroy();
            });
            dialog.on('cancel', function() {});
            dialog.show();
        },

        createProgressBarDialog: function (progress, maximum){
            if (!this.processDialog){
                var cp = new ContentPane({style:{overflow: "hidden"}});
                this.progressBar = new ProgressBar({
                    style: "min-width: 300px",
                    progress: progress,
                    maximum: maximum
                });
                cp.attr('content', this.progressBar);
                this.processDialog = new Dialog({
                    title: 'Загрузка',
                    style: "min-width: 300px",
                    content: cp,
                    closable: false
                });
                this.progressBar.update({progress: progress, maximum: maximum});
            }
        },

        /*
         showStandBy: function(){
         if (!this.standby){
         this.standby = new Standby({target: this.tabContainer.id});
         document.body.appendChild(this.standby.domNode);
         }
         this.standby.show();
         },

         hideStandBy: function(){
         this.standby.hide();
         },
         */

        updateProgressBarDialog: function (progress, maximum){
            if (!this.processDialog){
                this.createProgressBarDialog(progress, maximum);
            }
            this.progressBar.update({progress: progress});
            if (!this.processDialog.open){
                this.processDialog.show();
            }
        },

        hideProgressBarDialog: function (progress, maximum){
            if (!this.processDialog){
                this.createProgressBarDialog(progress, maximum);
            }
            if (this.processDialog.open){
                if (progress){
                    this.progressBar.update({progress: progress});
                }
                this.processDialog.hide();
            }
        },

        postCreate: function(){
            this.extraContainer.addChild(this._buildSidebarTree('extra'));
            this.logout.href = this.logout.baseURI + "/logout";
            var thisContext = this;
            request('form/currentUser', {
                method: "get",
                handleAs: "json"
            }).then(function(response){
                thisContext.currentUser = response;
                thisContext.username.innerText = response.attributes.surname.value + ' ' + response.attributes.name.value[0] + '. ' + response.attributes.middlename.value[0] + '.';
                on(thisContext.username, "click", function(e){
                    e.preventDefault();
                    thisContext.openTabForm(false, {
                        type: {0: response.template},
                        name: {0: response.tabTitle},
                        editMode:false,
                        id: {0: response.attributes.id.value}
                    });
                });
            });
            this.inherited(arguments);
        },

        currentUserHasAtLeastRole: function(checkRoleCodeString){
            var result = false;
            var roleCodes = checkRoleCodeString.split(',');
            var thisContext = this;
            roleCodes.some(function(role_) {
                if (thisContext.hasRole(thisContext.currentUser.attributes.roles.value, role_)){
                    result = true;
                    return true;
                }
            });
            return result;
        },

        hasRole: function(roles, roleCode){
            var result = false;
            roles.some(function(role_) {
                if (role_.code == roleCode) {
                    result = true;
                    return true;
                }
            });
            return result;
        },

        showNotification: function(info){
            var fadeTarget = dojo.byId('fadeTarget');
            fadeTarget.innerText = info;
            var timerId1 = setTimeout(function tick() {
                clearInterval(timerId1);
                fx.fadeIn({node: fadeTarget, duration: 2000}).play();
                var timerId2 = setTimeout(function tick() {
                    clearInterval(timerId2);
                    fx.fadeOut({node: fadeTarget, duration: 2000}).play();
                }, 3000);
            }, 500);
        }

    });
});