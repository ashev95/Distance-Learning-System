//>>built
define("dojox/mobile/dh/JsonContentHandler","dojo/_base/kernel dojo/_base/array dojo/_base/declare dojo/_base/lang dojo/_base/Deferred dojo/json dojo/dom-construct".split(" "),function(p,l,u,n,r,t,v){return u("dojox.mobile.dh.JsonContentHandler",null,{parse:function(b,g,d){var a,e=v.create("DIV");g.insertBefore(e,d);this._ws=[];this._req=[];var c=t.parse(b);return r.when(this._loadPrereqs(c),n.hitch(this,function(){a=this._instantiate(c,e);a.style.visibility="hidden";l.forEach(this._ws,function(a){!a._started&&
a.startup&&a.startup()});this._ws=null;return a.id}))},_loadPrereqs:function(b){var g=new r;b=this._collectRequires(b);if(0===b.length)return!0;if(p.require)return l.forEach(b,function(b){p.require(b)}),!0;b=l.map(b,function(b){return b.replace(/\./g,"/")});require(b,function(){g.resolve(!0)});return g},_collectRequires:function(b){var g=b["class"],d;for(d in b)if("@"!=d.charAt(0)&&"children"!==d){var a=g||d.replace(/:.*/,"");this._req.push(a);if(a)for(var a=g?[b]:n.isArray(b[d])?b[d]:[b[d]],e=0;e<
a.length;e++)if(!g)this._collectRequires(a[e]);else if(a[e].children)for(var c=0;c<a[e].children.length;c++)this._collectRequires(a[e].children[c])}return this._req},_instantiate:function(b,g,d){var a,e=b["class"],c;for(c in b)if("@"!=c.charAt(0)&&"children"!==c){var l=n.getObject(e||c.replace(/:.*/,""));if(l)for(var p=l.prototype,m=e?[b]:n.isArray(b[c])?b[c]:[b[c]],k=0;k<m.length;k++){a={};for(var f in m[k])if("@"==f.charAt(0)){var h=m[k][f];f=f.substring(1);var q=typeof p[f];n.isArray(p[f])?a[f]=
h.split(/\s*,\s*/):"string"===q?a[f]=h:"number"===q?a[f]=h-0:"boolean"===q?a[f]="false"!==h:"object"===q?a[f]=t.parse(h):"function"===q&&(a[f]=n.getObject(h,!1)||new Function(h))}a=new l(a,g);g&&this._ws.push(a);d&&a.placeAt(d.containerNode||d.domNode);if(!e)this._instantiate(m[k],null,a);else if(m[k].children)for(h=0;h<m[k].children.length;h++)this._instantiate(m[k].children[h],null,a)}}return a&&a.domNode}})});
//# sourceMappingURL=JsonContentHandler.js.map