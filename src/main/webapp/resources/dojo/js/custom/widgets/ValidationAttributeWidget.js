define([
"dojo/_base/declare",
"dojo/_base/lang",
"dijit/form/ValidationTextBox"
 ], function(declare, lang, ValidationTextBox) {

  return declare('ValidationAttributeWidget', [ValidationTextBox], {
    valueObject: null,
    clearValue: function(){
      this.set("value", "");
      this.valueObject = null;
    },
    setValue: function(valObj){
        this.set("value", (!!valObj.surname && !!valObj.surname.value && !!valObj.name && !!valObj.name.value && !!valObj.middlename && !!valObj.middlename.value ? (valObj.surname.value + " " + valObj.name.value + " " + valObj.middlename.value) : valObj.name.value));
        this.valueObject = valObj;
    },
    getValue: function(){
        return this.valueObject;
    },
    constructor: function(params){
      this.constraints = {};
      this.baseClass += ' ValidationAttributeWidget';
    }
    })
})