// ==UserScript==
// @name         安全安装
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/28
// @author       Nihility
// @run-at       document-start
// @match        *
// @grant        none
// ==/UserScript==
`
@name safe-install
@version 0.0.2

@confirm 1
`
.replace("", (_, __, config_text) => {
(window.__load_scripts__ = window.__load_scripts__ || []).push([config_text,
function () {

const addon = window.via && window.via.addon.bind(window.via);
if (typeof addon !== 'function') {
return;
}

const confirmp = 'confirm' in this ? +this.confirm : 1;
function addon_hook(base64) {
const json = atob(base64);
const code_base64 = (json.match(/"code":"([^"]+)"/) || '')[1];
if (!code_base64) {
return addon(base64);
}

const addon_code = decodeURIComponent(escape(atob(code_base64)));
const using_NLib = addon_code.match('window.__load_scripts__');
if (using_NLib) {
addon(base64);
} else {
if (confirmp && confirm('安全安装？') === false) {
return addon(base64);
}

const head_body = addon_code.match(/^\s*(\/\*[\s\S]*?\*\/)?\s*([\s\S]*)/);
let wrap_code = head_body[1] || '';
wrap_code += '\n(window.__load_scripts__ = window.__load_scripts__ || []).push(() => {\n\n';
wrap_code += (head_body[2] || '').trim();
wrap_code += '\n\n});';

const new_json = json.replace(/"code":"([^"]+)"/,
`"code":"${btoa(unescape(encodeURIComponent(wrap_code)))}"`);
try {
addon(btoa(new_json));
} catch (e) {
console.log('safe-install: ', e);
throw e;
}
}
};

const via_copy = {};
for (let i in window.via) {
if (typeof window.via[i] === 'function') {
via_copy[i] = window.via[i].bind(window.via);
} else {
via_copy[i] = window.via[i];
}
}
via_copy.addon = addon_hook.bind(window.via);

window.via = via_copy;

}]);return _;}),0;