define([
"dojo/_base/declare",
"dojo/_base/lang",
"dijit/form/SimpleTextarea",
"dijit/form/ValidationTextBox"
 ], function(declare, lang, SimpleTextarea, ValidationTextBox) {

  return declare('ValidationAttributeTextAreaWidget', [SimpleTextarea, ValidationTextBox], {
    valueArray:[],
    clearValueArray: function(){
      this.set("value", "");
      this.valueArray = [];
    },
    _objectToArray: function(object){
        var result = [];
        var newObject = dojo.clone(object);
        Object.entries(newObject).some(function(item) {
          result[item[0]] = item[1];
        });
        return result;
    },
    setValueArray: function(attribute){
        var valueArr = this._objectToArray(attribute);
        var v = [];
        valueArr.some(function(obj) {
            v.push(obj.name);
        });
        this.set("value", v.join("\n"));
        this.valueArray = valueArr;
    },
    getValueArray: function(){
        return this.valueArray;
    },
    constructor: function(params){
      this.constraints = {};
      this.baseClass += ' ValidationAttributeTextAreaWidget';
    },
      templateString: "<div style='border: 0px; padding: 0px;' class=\"dijit dijitReset dijitInline dijitLeft\"\n\tid=\"widget_${id}\" role=\"presentation\"\n\t><div style='height: 45px;' class='dijitReset dijitValidationContainer'\n\t\t><input class=\"dijitReset dijitInputField dijitValidationIcon dijitValidationInner\" value=\"&#935; \" type=\"text\" tabIndex=\"-1\" readonly=\"readonly\" role=\"presentation\"\n\t/></div\n\t><div style='padding: 0px;' class=\"dijitReset dijitInputField dijitInputContainer\"\n\t\t><textarea ${!nameAttrSetting} data-dojo-attach-point='focusNode,containerNode,textbox' autocomplete='off' style='width: 100%; resize: none;'></textarea></div\n></div>\n",
    validator: function(value, constraints) {
      return (new RegExp("^(?:" + this._computeRegexp(constraints) + ")"+(this.required?"":"?")+"$",["m"])).test(value) &&
        (!this.required || !this._isEmpty(value)) &&
        (this._isEmpty(value) || this.parse(value, constraints) !== undefined); // Boolean
    }
    })
})