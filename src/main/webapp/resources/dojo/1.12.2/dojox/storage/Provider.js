//>>built
define("dojox/storage/Provider",["dojo/_base/array","dojo/_base/declare","dojo/_base/lang"],function(f,g,h){return g("dojox.storage.Provider",null,{constructor:function(){},SUCCESS:"success",FAILED:"failed",PENDING:"pending",SIZE_NOT_AVAILABLE:"Size not available",SIZE_NO_LIMIT:"No size limit",DEFAULT_NAMESPACE:"default",onHideSettingsUI:null,initialize:function(){console.warn("dojox.storage.initialize not implemented")},isAvailable:function(){console.warn("dojox.storage.isAvailable not implemented")},
put:function(a,b,c,d){console.warn("dojox.storage.put not implemented")},get:function(a,b){console.warn("dojox.storage.get not implemented")},hasKey:function(a,b){return!!this.get(a,b)},getKeys:function(a){console.warn("dojox.storage.getKeys not implemented")},clear:function(a){console.warn("dojox.storage.clear not implemented")},remove:function(a,b){console.warn("dojox.storage.remove not implemented")},getNamespaces:function(){console.warn("dojox.storage.getNamespaces not implemented")},isPermanent:function(){console.warn("dojox.storage.isPermanent not implemented")},
getMaximumSize:function(){console.warn("dojox.storage.getMaximumSize not implemented")},putMultiple:function(a,b,c,d){for(var e=0;e<a.length;e++)dojox.storage.put(a[e],b[e],c,d)},getMultiple:function(a,b){for(var c=[],d=0;d<a.length;d++)c.push(dojox.storage.get(a[d],b));return c},removeMultiple:function(a,b){for(var c=0;c<a.length;c++)dojox.storage.remove(a[c],b)},isValidKeyArray:function(a){return null!==a&&void 0!==a&&h.isArray(a)?!f.some(a,function(a){return!this.isValidKey(a)},this):!1},hasSettingsUI:function(){return!1},
showSettingsUI:function(){console.warn("dojox.storage.showSettingsUI not implemented")},hideSettingsUI:function(){console.warn("dojox.storage.hideSettingsUI not implemented")},isValidKey:function(a){return null===a||void 0===a?!1:/^[0-9A-Za-z_]*$/.test(a)},getResourceList:function(){return[]}})});
//# sourceMappingURL=Provider.js.map