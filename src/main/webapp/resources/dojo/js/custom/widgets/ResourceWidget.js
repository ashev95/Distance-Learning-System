define([
    "dojo/_base/declare",
    "dijit/_WidgetBase",
    "dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",

    "dijit/form/DropDownButton",
    "dijit/TooltipDialog",
    "dijit/DropDownMenu",
    "dijit/MenuSeparator",
    "dijit/PopupMenuItem",
    "dijit/MenuItem",
    "dijit/form/Button",

    "dijit/layout/ContentPane",

    "dijit/popup",

    "dojo/dom-attr",

    "dojo/dom-style",

    "dojo/query",

    "dijit/Editor",
    "dijit/_editor/plugins/AlwaysShowToolbar",
    "dijit/_editor/plugins/TextColor",
    "dijit/_editor/plugins/LinkDialog",
    "dijit/_editor/plugins/FontChoice",

    "dojox/form/Uploader",

    "dojox/form/uploader/FileList",

    "dijit/Dialog",

    "dojo/dom-construct",

    "dojo/on",

    "dojo/request",

    "dojo/aspect",

    "dojo/text!./template/ResourceWidgetEdit.html",
    "dojo/text!./template/ResourceWidgetRead.html"

], function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin,
            DropDownButton, TooltipDialog, DropDownMenu, MenuSeparator, PopupMenuItem, MenuItem, Button, ContentPane,
            popup, domAttr, domStyle, query, Editor, AlwaysShowToolbar, TextColor, LinkDialog, FontChoice, Uploader, FileList, Dialog,
            domConstruct, on, request, aspect, templateEdit, templateRead

    ){
    return declare("ResourceWidget", [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {

        baseClass: 'ResourceWidget',
        templateString:null,
        missComponents: [], /* ['text', 'image', 'audio', 'video', 'file'] */
        
        getForm: function () {
            return this.formWidget;
        },
        
		buildRendering: function(){
            //code here
            if (this.formWidget.editMode){
                this.templateString = templateEdit;
            }else{
                this.templateString = templateRead;
            }
            this.inherited(arguments);
		},

        missComponent: function(name){
		    var miss = false;
            this.missComponents.some(function(component) {
                if (name === component){
                    miss = true;
                    return;
                }
            });
            return miss;
        },

		postCreate: function(){
            //code here
            var thisContext = this;
            if (this.formWidget.editMode){
                if (!this.missComponent('text')){
                    on(this.textResourceController, "click", function(e){
                        var editor = new Editor({
                            //height: '25%',
                            extraPlugins: [AlwaysShowToolbar, 'foreColor', 'hiliteColor', 'createLink', 'unlink', 'fontName', 'fontSize', 'formatBlock']
                        });
                        thisContext.prepareClick(thisContext, editor);
                    });
                }else{
                    domStyle.set(this.textResourceController.domNode, 'display','none');
                }
                if (!this.missComponent('video')){
                    on(this.videoResourceController, "click", function(e){
                        thisContext.prepareVideoResource();
                    });
                }else{
                    domStyle.set(this.videoResourceController.domNode, 'display','none');
                }
                if (!this.missComponent('image')){
                    on(this.imageResourceController, "click", function(e){
                        thisContext.prepareImageResource();
                    });
                }else{
                    domStyle.set(this.imageResourceController.domNode, 'display','none');
                }
                if (!this.missComponent('audio')){
                    on(this.audioResourceController, "click", function(e){
                        thisContext.prepareAudioResource();
                    });
                }else{
                    domStyle.set(this.audioResourceController.domNode, 'display','none');
                }
                if (!this.missComponent('file')){
                    on(this.fileResourceController, "click", function(e){
                        thisContext.prepareFileResource();
                    });
                }else{
                    domStyle.set(this.fileResourceController.domNode, 'display','none');
                }
                if (!this.formWidget.isNew){
                    this.fill(this.formWidget, this.formWidget.editMode);
                    this.recalcMoveButtons();
                }
            }else{
                this.fill(this.formWidget, this.formWidget.editMode);
            }
		},

        fill: function(formWidget, editMode){
		    var thisContext = this;
		    var editMode1 = editMode;
            request(('upload/' + (formWidget.template === "traininglesson" ? "templatelesson" : formWidget.template) + '/' + formWidget.response.attributes.id.value), {
                method: "get",
                sync: true,
                handleAs: "json"
            }).then(function(topResponse){
                if (!editMode1 && topResponse.length == 0){
                    domStyle.set(thisContext.resourceWidgetContainer.domNode, 'display','none');
                }else{
                    for (var i = 0; i < topResponse.length; i++){
                        var response = topResponse[i];
                        var thisContext1 = thisContext;
                        if (response.type.indexOf('text/') == 0){
                            request("uploadText/" + response.id, {
                                method: "get",
                                sync: true,
                                handleAs: "json"
                            }).then(function(response1){
                                var editor = null;
                                if (editMode1){
                                    editor = new Editor({
                                        //height: '25%',
                                        //width: '90%',
                                        extraPlugins: [AlwaysShowToolbar, 'foreColor', 'hiliteColor', 'createLink', 'unlink', 'fontName', 'fontSize', 'formatBlock']
                                    });
                                    thisContext1.prepareClick(thisContext1, editor);
                                    //
                                    editor.setAttribute("value", response1.messageText);
                                    var componentResource = editor.getParent().getParent();
                                    componentResource.attr('unid', response.id);
                                }else{
                                    editor = new ContentPane({});
                                    editor.attr('content', response1.messageText);
                                    thisContext1.resourceWidgetContainer.addChild(editor);
                                    thisContext1.resourceWidgetContainer.addChild(new ContentPane({height:'10px'}));
                                }
                            }, function (err){
                                thisContext1.formWidget.appWidget.getPromptDialog(null, ("Ошибка при создании карточки: " + (err.message || ""))).show();
                            });
                        }else if (response.type.indexOf('image/') >= 0){
                            if (editMode1){
                                var uploader = thisContext.prepareImageResource();
                                var resourceComponent = uploader.getParent().getParent();
                                thisContext.prepareImageViewer(resourceComponent, '/upload/' + response.value, response, true);
                                resourceComponent.attr('unid', response.value);
                            }else{
                                var link = domConstruct.create("a", {style: {color: 'black'}});
                                domAttr.set(link, 'title', response.name);
                                domAttr.set(link, 'href', '#');
                                on(link, "click", function(e){
                                    e.preventDefault();
                                    var cp = new ContentPane({style:{overflow: "hidden"}});
                                    var image = domConstruct.create("img", null);
                                    domAttr.set(image, 'src', this.firstElementChild.src);
                                    cp.attr('content', image);
                                    var dialog = new Dialog({
                                        title: this.title,
                                        style: "",
                                        content: cp
                                    });
                                    dialog.show();
                                });
                                var image = domConstruct.create("img", null, link);
                                domAttr.set(image, 'width', '100px');
                                domAttr.set(image, 'height', '100px');
                                domAttr.set(image, 'src', '/upload/' + response.value + '?dummy=' + thisContext.getRandom());
                                domAttr.set(image, 'alt', '');
                                var p = domConstruct.create("p", {style: {margin: '0px'}}, link);
                                p.innerText = p.innerText = response.name.substring(0, 100) + (response.name.length > 101 ? "..." : "");
                                var cpSpace = new ContentPane({height:'10px'});
                                domConstruct.place(link, thisContext.resourceWidgetContainer.domNode);
                                domConstruct.place(cpSpace.domNode, thisContext.resourceWidgetContainer.domNode);
                            }
                        }else if (response.type.indexOf('video/') >= 0){
                            if (editMode1){
                                var uploader = thisContext.prepareVideoResource();
                                var resourceComponent = uploader.getParent().getParent();
                                thisContext.prepareVideoViewer(resourceComponent, '/upload/' + response.value, response, true);
                                resourceComponent.attr('unid', response.value);
                            }else{
                                var video = domConstruct.create("video", null, null);
                                domAttr.set(video, 'width', '30%');
                                domAttr.set(video, 'src', '/upload/' + response.value + '?dummy=' + thisContext.getRandom());
                                domAttr.set(video, 'type', 'video/' + (response ? response.extension.split(' ')[0] : response.name.split('.')[response.name.split('.').length - 1]));
                                domAttr.set(video, 'codecs', 'avc1.42E01E, mp4a.40.2');
                                domAttr.set(video, 'controls', 'controls');
                                var p = domConstruct.create("p", {style: {margin: '0px'}}, video);
                                p.innerText = p.innerText = response.name.substring(0, 100) + (response.name.length > 101 ? "..." : "");
                                var cpSpace = new ContentPane({height:'10px'});
                                domConstruct.place(video, thisContext.resourceWidgetContainer.domNode);
                                domConstruct.place(cpSpace.domNode, thisContext.resourceWidgetContainer.domNode);
                            }
                        }else if (response.type.indexOf('application/octet-stream') >= 0){
                            if (editMode1){
                                var uploader = thisContext.prepareAudioResource();
                                var resourceComponent = uploader.getParent().getParent();
                                thisContext.prepareAudioViewer(resourceComponent, '/upload/' + response.value, response, true);
                                resourceComponent.attr('unid', response.value);
                            }else{
                                var audio = domConstruct.create("audio", null, null);
                                domAttr.set(audio, 'src', '/upload/' + response.value + '?dummy=' + thisContext.getRandom());
                                domAttr.set(audio, 'type', 'audio/' + (response ? response.extension.split(' ')[0] : response.name.split('.')[response.name.split('.').length - 1]));
                                domAttr.set(audio, 'controls', 'controls');
                                var p = domConstruct.create("p", {style: {margin: '0px'}}, audio);
                                p.innerText = p.innerText = response.name.substring(0, 100) + (response.name.length > 101 ? "..." : "");
                                var cpSpace = new ContentPane({height:'10px'});
                                domConstruct.place(audio, thisContext.resourceWidgetContainer.domNode);
                                domConstruct.place(cpSpace.domNode, thisContext.resourceWidgetContainer.domNode);
                            }
                        }else{
                            if (editMode1){
                                var uploader = thisContext.prepareFileResource();
                                var resourceComponent = uploader.getParent().getParent();
                                thisContext.prepareFileViewer(resourceComponent, '/upload/' + response.value, response);
                                resourceComponent.attr('unid', response.value);
                            }else{
                                var file = new Button({
                                    label: "Сохранить на диск",
                                    onClick: function(){
                                        window.location = '/upload/' + response.value;
                                    }
                                });
                                var p = domConstruct.create("p", {style: {margin: '0px'}}, file.domNode);
                                p.innerText = p.innerText = response.name.substring(0, 100) + (response.name.length > 101 ? "..." : "");
                                var cpSpace = new ContentPane({height:'10px'});
                                domConstruct.place(file.domNode, thisContext.resourceWidgetContainer.domNode);
                                domConstruct.place(cpSpace.domNode, thisContext.resourceWidgetContainer.domNode);
                            }
                        }
                    }
                }
            }, function (err){
                formWidget.appWidget.getPromptDialog(null, ("Ошибка при создании карточки: " + (err.message || ""))).show();
            });
        },

        prepareImageResource: function(){
            var thisContext = this;
            var uploader = new Uploader({
                label: "Выберите изображение...",
                multiple: false,
                uploadOnSelect: false,
                url: "/upload",
                name: "fileUploadResource",
                uploader_type: 'image'
            });

            aspect.after(uploader._inputs, "push", function(method, args) {
                args[0].accept = "image/jpeg, image/png, image/gif";
            });

            on(uploader, "change", function(data){
                var files = this.focusNode.files;
                var file = files[0];
                var src = URL.createObjectURL(file);
                var resourceComponent = this.getParent().getParent();
                resourceComponent.attr('unid', "");
                var response = {
                    name: file.name
                };
                thisContext.prepareImageViewer(resourceComponent, src, file, false);
            });

            thisContext.prepareClick(thisContext, uploader);
            return uploader;
        },

        prepareImageViewer: function(resourceComponent, src, response, useRandom){
            var viewer = resourceComponent.getChildren()[2];
            if (viewer.domNode.innerHTML.length > 0){
                viewer.domNode.innerHTML = "";
            }
            var link = domConstruct.create("a", {style: {color: 'black'}});
            domAttr.set(link, 'title', response.name);
            domAttr.set(link, 'href', '#');
            on(link, "click", function(e){
                e.preventDefault();
                var cp = new ContentPane({style:{overflow: "auto"}});
                var image = domConstruct.create("img", null);
                domAttr.set(image, 'src', this.firstElementChild.src);
                cp.attr('content', image);
                var dialog = new Dialog({
                    title: this.title,
                    style: "",
                    content: cp
                });
                dialog.show();
            });
            var image = domConstruct.create("img", null, link);
            domAttr.set(image, 'width', '100px');
            domAttr.set(image, 'height', '100px');
            domAttr.set(image, 'src', src + (useRandom ? '?dummy=' + this.getRandom() : ''));
            domAttr.set(image, 'alt', '');
            var p = domConstruct.create("p", {style: {margin: '0px'}}, link);
            p.innerText = p.innerText = response.name.substring(0, 100) + (response.name.length > 101 ? "..." : "");;
            viewer.attr('content', link);
        },

        prepareVideoResource: function(){
            var thisContext = this;

            var uploader = new Uploader({
                label: "Выберите видео...",
                multiple: false,
                uploadOnSelect: false,
                url: "/upload",
                name: "fileUploadResource",
                uploader_type: 'video'
            });

            aspect.after(uploader._inputs, "push", function(method, args) {
                args[0].accept = "video/*";
            });

            on(uploader, "change", function(data){
                var files = this.focusNode.files;
                var file = files[0];
                var src = URL.createObjectURL(file);
                var resourceComponent = this.getParent().getParent();
                resourceComponent.attr('unid', "");
                var response = {
                    name: file.name,
                    extension: ""
                };
                var tmp = file.type.split('/');
                response.extension = tmp[tmp.length - 1];
                thisContext.prepareVideoViewer(resourceComponent, src, response, false);
            });

            thisContext.prepareClick(thisContext, uploader);
            return uploader;
        },

        prepareVideoViewer: function(resourceComponent, src, response, useRandom){
            var viewer = resourceComponent.getChildren()[2];
            if (viewer.domNode.innerHTML.length > 0){
                viewer.domNode.innerHTML = "";
            }
            var video = domConstruct.create("video", null, null);
            domAttr.set(video, 'width', '30%');
            domAttr.set(video, 'src', src + (useRandom ? '?dummy=' + this.getRandom() : ''));
            domAttr.set(video, 'type', 'video/' + (response ? response.extension.split(' ')[0] : response.name.split('.')[response.name.split('.').length - 1]));
            domAttr.set(video, 'codecs', 'avc1.42E01E, mp4a.40.2');
            domAttr.set(video, 'controls', 'controls');
            viewer.attr('content', video);
            var p = domConstruct.create("p", {style: {margin: '0px'}}, viewer.domNode);
            p.innerText = p.innerText = response.name.substring(0, 100) + (response.name.length > 101 ? "..." : "");;
        },

        prepareAudioResource: function(){
            var thisContext = this;
            var uploader = new Uploader({
                label: "Выберите аудио...",
                multiple: false,
                uploadOnSelect: false,
                url: "/upload",
                name: "fileUploadResource",
                uploader_type: 'audio'
            });

            aspect.after(uploader._inputs, "push", function(method, args) {
                args[0].accept = "audio/*";
            });

            on(uploader, "change", function(data){
                var files = this.focusNode.files;
                var file = files[0];
                var src = URL.createObjectURL(file);
                var resourceComponent = this.getParent().getParent();
                resourceComponent.attr('unid', "");
                var response = {
                    name: file.name,
                    extension: ""
                };
                var tmp = file.type.split('/');
                response.extension = tmp[tmp.length - 1];
                thisContext.prepareAudioViewer(resourceComponent, src, response, false);
            });

            thisContext.prepareClick(thisContext, uploader);
            return uploader;
        },

        prepareAudioViewer: function(resourceComponent, src, response, useRandom){
            var viewer = resourceComponent.getChildren()[2];
            if (viewer.domNode.innerHTML.length > 0){
                viewer.domNode.innerHTML = "";
            }
            var audio = domConstruct.create("audio", null, null);
            domAttr.set(audio, 'src', src + (useRandom ? '?dummy=' + this.getRandom() : ''));
            domAttr.set(audio, 'type', 'audio/' + (response ? response.extension.split(' ')[0] : response.name.split('.')[response.name.split('.').length - 1]));
            domAttr.set(audio, 'controls', 'controls');
            viewer.attr('content', audio);
            var p = domConstruct.create("p", {style: {margin: '0px'}}, viewer.domNode);
            p.innerText = p.innerText = response.name.substring(0, 100) + (response.name.length > 101 ? "..." : "");;
        },

        prepareFileResource: function(){
            var thisContext = this;
            var uploader = new Uploader({
                label: "Выберите файл...",
                multiple: false,
                uploadOnSelect: false,
                url: "/upload",
                name: "fileUploadResource",
                uploader_type: 'file'
            });

            aspect.after(uploader._inputs, "push", function(method, args) {
                args[0].accept = "*";
            });

            on(uploader, "change", function(data){
                var files = this.focusNode.files;
                var file = files[0];
                var src = URL.createObjectURL(file);
                var resourceComponent = this.getParent().getParent();
                resourceComponent.attr('unid', "");
                var response = {
                    name: file.name
                };
                thisContext.prepareFileViewer(resourceComponent, src, response);
            });

            thisContext.prepareClick(thisContext, uploader);
            return uploader;
        },

        prepareFileViewer: function(resourceComponent, src, response){
            var viewer = resourceComponent.getChildren()[2];
            if (viewer.domNode.innerHTML.length > 0){
                viewer.domNode.innerHTML = "";
            }
            var file = new Button({
                label: "Сохранить на диск",
                onClick: function(){
                    var link = document.createElement("a");
                    link.download = response.name;
                    link.href = src;
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                }
            });
            viewer.attr('content', file);
            var p = domConstruct.create("p", {style: {margin: '0px'}}, viewer.domNode);
            p.innerText = p.innerText = response.name.substring(0, 100) + (response.name.length > 101 ? "..." : "");;
        },

        prepareClick: function(context, component){
            var componentResource = new ContentPane({
                class: 'componentResource'
            });
            var cpSpace = new ContentPane({height:'10px'});

            var cpArrows = new ContentPane({
                style: {float: 'left'}
            });
            var cpArrowUp = new ContentPane({});
            var btnUp = new Button({
                label: "⬆",
                onClick: function(){
                    var resourceWidget = this.getParent().getParent().getParent();
                    var resourceWidgetContainer = resourceWidget.getParent();
                    var childs = resourceWidgetContainer.getChildren();
                    var founded = false;
                    var beforeRes = null;
                    var beforeResSpace = null;
                    var nextResSpace = null;
                    for (var i = 0; i < childs.length; i++){
                        if (founded){
                            nextResSpace = childs[i];
                            break;
                        }else{
                            if (beforeRes){
                                if (beforeResSpace){
                                    if (childs[i].id == resourceWidget.id){
                                        founded = true;
                                    }else{
                                        beforeRes = beforeResSpace;
                                        beforeResSpace = childs[i];
                                    }
                                }else{
                                    beforeResSpace = childs[i];
                                }
                            }else{
                                beforeRes = childs[i];
                            }
                        }
                    }
                    domConstruct.place(resourceWidget.domNode, beforeRes.domNode, 'before');
                    domConstruct.place(nextResSpace.domNode, beforeRes.domNode, 'before');
                    dijit.byId(this.getParent().getParent().getParent().getParent().getParent().id).recalcMoveButtons();
                }
            });
            cpArrowUp.addChild(btnUp);
            var cpArrowDown = new ContentPane({});
            var btnDown = new Button({
                label: "⬇",
                onClick: function(){
                    var resourceWidget = this.getParent().getParent().getParent();
                    var resourceWidgetContainer = resourceWidget.getParent();
                    var childs = resourceWidgetContainer.getChildren();
                    var founded = false;
                    var prevResSpace = null;
                    var nextRes = null;
                    var nextResSpace = null;
                    for (var i = 0; i < childs.length; i++){
                        if (founded){
                            if (prevResSpace){
                                if (nextRes){
                                    nextResSpace = childs[i];
                                    break;
                                }else{
                                    nextRes = childs[i];
                                }
                            }else{
                                prevResSpace = childs[i];
                            }
                        }else{
                            if (childs[i].id == resourceWidget.id){
                                founded = true;
                            }
                        }
                    }
                    domConstruct.place(prevResSpace.domNode, nextResSpace.domNode, 'after');
                    domConstruct.place(resourceWidget.domNode, nextResSpace.domNode, 'after');
                    dijit.byId(this.getParent().getParent().getParent().getParent().getParent().id).recalcMoveButtons();
                }
            });
            cpArrowDown.addChild(btnDown);
            cpArrows.addChild(cpArrowUp);
            cpArrows.addChild(cpArrowDown);
            var cp1 = new ContentPane({
                style: {float: 'left'}
            });
            cp1.addChild(component);
            var cp2 = new ContentPane({});
            var btnRemove = new Button({
                label: "X",
                onClick: function(){
                    context.resourceWidgetContainer.removeChild(componentResource);
                    context.resourceWidgetContainer.removeChild(cpSpace);
                    componentResource.destroy();
                    cpSpace.destroy();
                }
            });
            cp2.addChild(btnRemove);
            var cp3 = new ContentPane({});
            cpArrows.addChild(cp2);
            componentResource.addChild(cpArrows);
            componentResource.addChild(cp1);
            //componentResource.addChild(cp2);
            componentResource.addChild(cp3);
            context.resourceWidgetContainer.addChild(componentResource);
            context.resourceWidgetContainer.addChild(cpSpace);
            popup.close(context.tooltipDialog);
        },

        recalcMoveButtons: function(){
            var resources = query(".componentResource", this.resourceWidgetContainer.domNode);
            for (var i = 0; i < resources.length; i++){
                var resourceWidget = dijit.byId(resources[i].id);
                if (i == 0){
                    domStyle.set(resourceWidget.getChildren()[0].getChildren()[0].domNode, 'display','none');
                    domStyle.set(resourceWidget.getChildren()[0].getChildren()[1].domNode, 'display','block');
                }else if(i == (resources.length - 1)){
                    domStyle.set(resourceWidget.getChildren()[0].getChildren()[0].domNode, 'display','block');
                    domStyle.set(resourceWidget.getChildren()[0].getChildren()[1].domNode, 'display','none');
                }else{
                    domStyle.set(resourceWidget.getChildren()[0].getChildren()[0].domNode, 'display','block');
                    domStyle.set(resourceWidget.getChildren()[0].getChildren()[1].domNode, 'display','block');
                }
            }
        },

        isTextResource: function(resourceWidget){
            return resourceWidget.baseClass == "dijitContentPane" && resourceWidget.getChildren().length > 1 && resourceWidget.getChildren()[1].getChildren().length > 0 && resourceWidget.getChildren()[1].getChildren()[0].baseClass == "dijitEditor";
        },

        isEmptyTextResource: function(resourceWidget){
            return (resourceWidget.getChildren()[1].getChildren()[0].value.trim() === "");
        },

        uploadFiles: function(response, callbackError, callbackSuccess){
            var errorText = null;
            var resources = query(".componentResource", this.resourceWidgetContainer.domNode);
            var thisContext = this;
            var resourceWidgetList = [];
            var i = 0;
            resources.some(function(resource) {
                var resourceWidget = dijit.byId(resource.id);
                if (thisContext.isTextResource(resourceWidget)){
                    if (thisContext.isEmptyTextResource(resourceWidget)){
                        errorText = "Текстовый компонент не может быть пустым. Необходимо ввести текст или удалить компонент";
                        return;
                    }
                }else{
                    if (!((!!resourceWidget.unid && resourceWidget !== '') || (resourceWidget.getChildren()[1].getChildren()[0].getFileList().length > 0))){
                        errorText = "Компонент не может быть пустым. Необходимо выбрать файл ресурса или удалить компонент";
                        return;
                    }
                }
                resourceWidget.position_temp = ++i;
                resourceWidget.template_temp = thisContext.formWidget.template;
                resourceWidget.entityId_temp = response.attributes.id.value;
                resourceWidgetList.push(resourceWidget);
            });
            if (errorText){
                callbackError(errorText);
                return;
            }
            this.recursiveRunner(resourceWidgetList, 0, (100 / resourceWidgetList.length), response, callbackError, callbackSuccess);
            return errorText;
        },

        syncFiles: function (callbackError, callbackSuccess){
            var resources = query(".componentResource", this.resourceWidgetContainer.domNode);
            var unidList = [];
            resources.some(function(resource) {
                var resourceWidget = dijit.byId(resource.id);
                if (resourceWidget.unid && resourceWidget.unid !== ""){
                    unidList.push(resourceWidget.unid);
                }
            });
            if (!this.formWidget.isNew){
                request("upload/syncFiles/" + this.formWidget.params.template + "/" + this.formWidget.params.response.attributes.id.value, {
                    method: "post",
                    sync: true,
                    data: JSON.stringify(unidList),
                    handleAs: "json",
                    headers: { "Content-Type": "application/json; charset=uft-8" }
                }).then(function(response){
                    callbackSuccess();
                }, function (err){
                    callbackError(err.message);
                });
            }else{
                callbackSuccess();
            }
        },

        recursiveRunner: function (resourceWidgetList, curValue, curStepValue, response, callbackError, callbackSuccess){
            var thisContext = this;
            if (resourceWidgetList.length === 0){
                this.syncFiles(callbackError, callbackSuccess);
                return;
            }
            var resourceWidget = resourceWidgetList.pop();
            if (thisContext.isTextResource(resourceWidget)){
                if (resourceWidget.unid && resourceWidget.unid !== ""){
                    request("upload/updateText/" + resourceWidget.unid + '/' + resourceWidget.position_temp, {
                        method: "post",
                        sync: true,
                        data: resourceWidget.getChildren()[1].getChildren()[0].value,
                        handleAs: "json",
                        headers: { "Content-Type": "text/plain; charset=uft-8" }
                    }).then(function(response){
                        resourceWidget.attr('unid', response.messageText);
                        thisContext.recursiveRunner(resourceWidgetList, curValue, curStepValue, response, callbackError, callbackSuccess);
                    }, function (err){
                        callbackError(err.message);
                    });
                }else{
                    request("uploadText/" + resourceWidget.template_temp + '/' + resourceWidget.entityId_temp + '/' + resourceWidget.position_temp, {
                        method: "post",
                        sync: true,
                        data: resourceWidget.getChildren()[1].getChildren()[0].value,
                        handleAs: "json",
                        headers: { "Content-Type": "text/plain; charset=uft-8" }
                    }).then(function(response){
                        resourceWidget.attr('unid', response.messageText);
                        thisContext.recursiveRunner(resourceWidgetList, curValue, curStepValue, response, callbackError, callbackSuccess);
                    }, function (err){
                        callbackError(err.message);
                    });
                }
            }else{
                if (resourceWidget.unid && resourceWidget.unid !== ""){
                    request("upload/updatePosition/" + resourceWidget.unid + '/' + resourceWidget.position_temp, {
                        method: "post",
                        sync: true,
                        handleAs: "json"
                    }).then(function(response){
                        resourceWidget.attr('unid', response.messageText);
                        thisContext.recursiveRunner(resourceWidgetList, curValue, curStepValue, response, callbackError, callbackSuccess);
                    }, function (err){
                        callbackError(err.message);
                    });
                }else{
                    var uploader = resourceWidget.getChildren()[1].getChildren()[0];
                    thisContext.setCompleteEvent(uploader, curValue, curStepValue, resourceWidgetList, callbackError, callbackSuccess);
                    uploader.setAttribute('url', uploader.url + '/' + resourceWidget.template_temp + '/' + resourceWidget.entityId_temp + '/' + resourceWidget.position_temp);
                    uploader.upload();
                }
            }

        },

        setCompleteEvent: function(uploader, curValue, curStepValue, resourceWidgetList, callbackError, callbackSuccess){
            var thisContext = this;
            var glCurValue = curValue;
            var glCurStepValue = curStepValue;
            var glResourceWidgetList = resourceWidgetList;
            var glCallbackError = callbackError;
            var glCallbackSuccess = callbackSuccess;
            switch (uploader.uploader_type){
                case 'image':
                    on(uploader, "complete", function(response){
                        var thisContext1 = this;
                        if (!response.name){
                            request("uploadName/" + response.messageText, {
                                method: "get",
                                sync: true,
                                handleAs: "json"
                            }).then(function(response1){
                                response.name = response1.messageText;
                                var resourceComponent = thisContext1.getParent().getParent();
                                resourceComponent.attr('unid', response.messageText);
                                thisContext.prepareImageViewer(resourceComponent, '/upload/' + response.messageText, response, true);
                                glCurValue += glCurStepValue;
                                thisContext.getForm().appWidget.updateProgressBarDialog(glCurValue, 100);
                                thisContext.recursiveRunner(glResourceWidgetList, glCurValue, glCurStepValue, response, glCallbackError, glCallbackSuccess);
                            }, function (err){
                                response.name = "";
                                glCallbackError(err);
                            });
                        }
                    });
                    on(uploader, "error", function(response){
                        var thisContext1 = this;
                        glCallbackError("Ошибка загрузки данных. Убедитесь, что файл не превышает 10 мегабайт");
                    });
                    break;
                case 'video':
                    on(uploader, "complete", function(response){
                        var thisContext1 = this;
                        if (!response.name){
                            request("uploadName/" + response.messageText, {
                                method: "get",
                                sync: true,
                                handleAs: "json"
                            }).then(function(response1){
                                response.name = response1.messageText;
                                var resourceComponent = thisContext1.getParent().getParent();
                                resourceComponent.attr('unid', response.messageText);
                                thisContext.prepareVideoViewer(resourceComponent, '/upload/' + response.messageText, response, true);
                            }, function (err){
                                response.name = "";
                                glCallbackError(err);
                            });
                        }
                        request("uploadExtension/" + response.messageText, {
                            method: "get",
                            sync: true,
                            handleAs: "json"
                        }).then(function(response1){
                            response.extension = response1.messageText;
                            glCurValue += glCurStepValue;
                            thisContext.getForm().appWidget.updateProgressBarDialog(glCurValue, 100);
                            thisContext.recursiveRunner(glResourceWidgetList, glCurValue, glCurStepValue, response, glCallbackError, glCallbackSuccess);
                        }, function (err){
                            response.extension = "";
                            glCallbackError(err);
                        });
                    });
                    on(uploader, "error", function(response){
                        var thisContext1 = this;
                        glCallbackError("Ошибка загрузки данных. Убедитесь, что файл не превышает 10 мегабайт");
                    });
                    break;
                case 'audio':
                    on(uploader, "complete", function(response){
                        var thisContext1 = this;
                        if (!response.name){
                            request("uploadName/" + response.messageText, {
                                method: "get",
                                sync: true,
                                handleAs: "json"
                            }).then(function(response1){
                                response.name = response1.messageText;
                                var resourceComponent = thisContext1.getParent().getParent();
                                resourceComponent.attr('unid', response.messageText);
                                thisContext.prepareAudioViewer(resourceComponent, '/upload/' + response.messageText, response, true);
                            }, function (err){
                                response.name = "";
                                glCallbackError(err);
                            });
                        }
                        request("uploadExtension/" + response.messageText, {
                            method: "get",
                            sync: true,
                            handleAs: "json"
                        }).then(function(response1){
                            response.extension = response1.messageText;
                            glCurValue += glCurStepValue;
                            thisContext.getForm().appWidget.updateProgressBarDialog(glCurValue, 100);
                            thisContext.recursiveRunner(glResourceWidgetList, glCurValue, glCurStepValue, response, glCallbackError, glCallbackSuccess);
                        }, function (err){
                            response.extension = "";
                            glCallbackError(err);
                        });
                    });
                    on(uploader, "error", function(response){
                        var thisContext1 = this;
                        glCallbackError("Ошибка загрузки данных. Убедитесь, что файл не превышает 10 мегабайт");
                    });
                    break;
                case 'file':
                    on(uploader, "complete", function(response){
                        var thisContext1 = this;
                        if (!response.name){
                            request("uploadName/" + response.messageText, {
                                method: "get",
                                sync: true,
                                handleAs: "json"
                            }).then(function(response1){
                                response.name = response1.messageText;
                                var resourceComponent = thisContext1.getParent().getParent();
                                resourceComponent.attr('unid', response.messageText);
                                thisContext.prepareFileViewer(resourceComponent, '/upload/' + response.messageText, response);
                                glCurValue += glCurStepValue;
                                thisContext.getForm().appWidget.updateProgressBarDialog(glCurValue, 100);
                                thisContext.recursiveRunner(glResourceWidgetList, glCurValue, glCurStepValue, response, glCallbackError, glCallbackSuccess);
                            }, function (err){
                                response.name = "";
                                glCallbackError(err);
                            });
                        }
                    });
                    on(uploader, "error", function(response){
                        var thisContext1 = this;
                        glCallbackError("Ошибка загрузки данных. Убедитесь, что файл не превышает 10 мегабайт");
                    });
                    break;
            }
        },

        getAllUnids: function(){
            var unids = [];
            var c = 0;
            var resources = query(".componentResource", this.resourceWidgetContainer.domNode);
            resources.some(function(resource) {
                var resourceWidget = dijit.byId(resource.id);
                unids.push({position: ++c, unid: resourceWidget.unid});
            });
            return unids;
        },

		getRandom: function(){
            return (Math.floor(Math.random() * 1000));
        }

    });
	
});