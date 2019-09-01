define([
    "dojo/_base/declare",
	"dijit/layout/ContentPane",
	"dojox/grid/TreeGrid",
	"dojo/data/ItemFileReadStore",
	"dijit/tree/ForestStoreModel",
	"dijit/MenuBar",
	"dijit/MenuItem",
	"dijit/ConfirmDialog",
	"dojo/request",
	"dojo/dom-style",
    "dijit/form/Button",
    "dijit/form/TextBox",
    "dojo/keys",
	"dojo/on",
    "dojox/data/AndOrReadStore"
	
], function(declare, ContentPane, TreeGrid, ItemFileReadStore, ForestStoreModel, MenuBar, MenuItem, ConfirmDialog, request, domStyle, Button, TextBox, keys, on, AndOrReadStore){
    return declare([ContentPane], {
		id: "",
		url: "",
		baseClass: 'GridWidget',
		appWidget: undefined,
		type: "",
		newFormTitle: "",
		newFormType: "",
		response: undefined,
		menuBar: undefined,
		gridView: undefined,
        formWidget: undefined,
		searchPanel: undefined,

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

        getButtonByType: function(type){
            var foundedItem = null;
            this.menuBar.getChildren().some(function(item) {
                if (item.btnType === type){
                    foundedItem = item;
                }
            });
            return foundedItem;
        },

		computeHideFunction: function (){
			var selRows = this.gridView.selection.getSelected();
			if (!selRows[0]){
                selRows = [];
            }
            this.menuBar.getChildren().some(function(button) {
                domStyle.set(button.domNode, "display", (button.computeHF(selRows) ? "none" : "block"));
            });
		},

		buildRendering: function(){
            this.inherited(arguments);

            var thisContext = this;
			var styleClass = "gridView";

            if (this.formWidget){
				//В карточке
                styleClass = "gridEmbView";
                this.response = this.formWidget.embViewResponse;
                this.url = this.formWidget.embViewResponse.url;
                this.appWidget = this.formWidget.appWidget;
                this.isNew = this.formWidget.isNew;
			}

            var response = this.response;
			var gridView = new TreeGrid({
				treeModel: new ForestStoreModel({
					store: new AndOrReadStore({data:dojo.clone(response.data)}),
					childrenAttrs: ['children']
				}),
                autoExpand: true,
				structure: response.structure,
				defaultOpen: true,
				class: styleClass,
                customRefresh: function(){
                    var thisContext = this;
                    request(this.thisWidget.url, {
                        handleAs: "json"
                    }).then(function(response){
                        var restoreFocus = (thisContext.selection.selectedIndex && thisContext.selection.selectedIndex != -1);
                        var focusIndex = -1;
                        if (restoreFocus){
                            focusIndex = thisContext.selection.selectedIndex;
                        }
                        thisContext.selection.clear();
                        thisContext.setModel(
                            new ForestStoreModel({
                                store: new AndOrReadStore({
                                    data: dojo.clone(response.data)
                                }),
                                childrenAttrs: ['children']
                            })
                        );
                        if (restoreFocus){
                            thisContext.selection.clear();
                            thisContext.selection.setSelected(focusIndex, true);
                            thisContext.render();
                        }
                        thisContext.thisWidget.computeHideFunction();
                    });
                },
                customRestore: function(){
                    var thisContext = this;
                    var newData = dojo.clone(thisContext.savedData);
                    thisContext.setModel(
                        new ForestStoreModel({
                            store: new AndOrReadStore({
                                data: newData
                            }),
                            childrenAttrs: ['children']
                        })
                    );
                },
                doSearch: function(formula){
                    var thisContext = this;
                    thisContext.customRestore();
                    thisContext.store.fetch({
                        query: { complexQuery: formula},
                        onComplete: function(items, request){
                            var newData = dojo.clone(thisContext.savedData);
                            newData.items = items;
                            thisContext.setModel(
                                new ForestStoreModel({
                                    store: new AndOrReadStore({
                                        data: newData
                                    }),
                                    childrenAttrs: ['children']
                                })
                            );
                            /*
                             var store = new AndOrReadStore({
                             data: newData
                             });
                             */

                            //thisContext.store.close();
                            //thisContext.setStore(store);
                            //thisContext.render();
                            /*
                             thisContext.selection.clear();
                             thisContext.savedData.items = items;
                             thisContext.setModel(
                             new ForestStoreModel({
                             store: new AndOrReadStore({
                             data: thisContext.savedData
                             }),
                             childrenAttrs: ['children']
                             })
                             );
                             thisContext.thisWidget.computeHideFunction();
                             */
                        },
                        queryOptions: {deep:true}
                    });
                },
				onClick: function(evt){
					this.thisWidget.computeHideFunction();
				},
				onDblClick: function(evt){
					var selRows = this.selection.getSelected();
					if (!!selRows && !!selRows[0]){
						selRows[0].editMode = false;
						if (selRows[0].type){
							var onDblClick = thisContext.appWidget.viewConfig.template[thisContext.response.uid].onDblClick;
							if (onDblClick){
                                var f = function (item) {eval("(function(item){" + onDblClick + "})").call(thisContext, item);};
                                f(selRows[0]);
							}else{
                                if (this.thisWidget.formWidget){
                                    //Из встроенного представления
                                    var selected = selRows[0];
                                    this.appWidget.showFormDialog(new FormWidget({
                                        id: selected.id[0],
                                        title: selected.name[0],
                                        response: this.appWidget.getDataByUrl('form/' + selected.type[0] + '/' + selected.cardId[0]),
                                        isNew: false,
                                        template: selected.type[0],
                                        editMode: false,
                                        appWidget: this.appWidget,
                                        parentFormWidget: this.thisWidget.formWidget,
                                        embView: this.thisWidget
                                    }));
                                }else{
                                    //Из главного представления
                                    this.appWidget.openTabForm(false, selRows[0]);
                                }
							}
						}
					}
				},
				thisWidget: this,
				appWidget: this.appWidget,
				newFormType: this.newFormType,
				newFormTitle: this.newFormTitle,
                savedData: dojo.clone(response.data)
				
			});

			this.gridView = gridView;

            dojo.connect(this.gridView, "onStyleRow", function(row){
                var item = thisContext.gridView.getItem(row.index);
                if (item){
                    var rowCustomStyle = thisContext.gridView.store.getValue(item, "rowCustomStyle", null);
                    if (rowCustomStyle){
                        row.customStyles += rowCustomStyle;
					}
				}
            });

			var pMenuBar = new MenuBar({});
			this.menuBar = pMenuBar;

			if (this.appWidget.viewConfig.template[this.response.uid]){
                this.response.buttons = this.appWidget.viewConfig.template[this.response.uid].buttons;
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
                            computeHF: function (items) {return eval("(function(selectedRows){" + button.hideFunction + "})").call(thisContext, items);}
                        })
                    );
                });
			}
			this.computeHideFunction();

			//build search panel - START
            var table = dojo.create("table", {style: {width: '100%'}});
            var tr = dojo.create("tr");
            var td1 = dojo.create("td", {style: {width: '100%'}});
            var td2 = dojo.create("td");
            var td3 = dojo.create("td");
            var searchTextBox = new TextBox({
                style: {width: '100%'}
            });

            function search(){
                var sourceFormula = searchTextBox.getValue();
                var newFormula = sourceFormula;
                thisContext.gridView.structure.some(function(item) {
                    newFormula = newFormula.split('[' + item.name + ']').join(item.field);
                    newFormula = newFormula.split('=').join(":");
                    newFormula = newFormula.split(' И ').join(" AND ");
                    newFormula = newFormula.split(' ИЛИ ').join(" OR ");
                });
                thisContext.gridView.doSearch(newFormula);
            }

            on(searchTextBox, "keypress", function(e){
                if (e.keyCode == 13){
                    search();
                    e.stopPropagation();
                    return;
                }
            });
            var searchBtn = new Button({
                label: 'Поиск',
                onClick: function(){
                    search();
                }
            });
            var searchResetBtn = new Button({
                label: 'Х',
                onClick: function(){
                    thisContext.gridView.customRestore();
                }
            });

            dojo.place(searchTextBox.domNode, td1);
            dojo.place(searchBtn.domNode, td2);
            dojo.place(searchResetBtn.domNode, td3);
            dojo.place(td1, tr);
            dojo.place(td2, tr);
            dojo.place(td3, tr);
            dojo.place(tr, table);

            var searchPanel = new ContentPane({
                content: table
            });

			this.searchPanel = searchPanel;

            //build search panel - END

            this.addChild(pMenuBar);
			this.addChild(searchPanel);
			this.addChild(gridView);

		}
    });
});