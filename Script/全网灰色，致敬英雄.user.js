// ==UserScript==
// @name         全网灰色，致敬英雄
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/43
// @author       谷花泰
// @run-at       document-start
// @match        *
// @grant        none
// ==/UserScript==
/*
* @name: 全网灰色，致敬英雄
* @Author: 谷花泰
* @version: 1.0
* @description: 使全网页变灰色
* @include: *
* @createTime: 2020-04-04 00:22:22
* @updateTime: 2020-04-04 00:33:12
*/
(window.__load_scripts__ = window.__load_scripts__ || []).push(() => {

(function () {
/* 判断是否该执行 */
/* 网址黑名单制，遇到这些域名不执行 */
const blackList = ['example.com'];

const hostname = window.location.hostname;
const key = encodeURIComponent('谷花泰:全网灰色，致敬英雄:执行判断');
const isBlack = blackList.some(keyword => {
if (hostname.match(keyword)) {
return true;
};
return false;
});

if (isBlack || window[key]) {
return;
};
window[key] = true;

/* 开始执行代码 */
class GrayBackground {
constructor() {
this.init();
};
init() {
this.addStyle();
this.selectAllNodes(node => {
if (!document.querySelector('style#via-gray')) {
this.addStyle();
};
});
};
addStyle() {
const styleElm = document.createElement('style');
styleElm.id = 'via-gray';
styleElm.innerHTML = `
html {
-webkit-filter: grayscale(100%);
filter:progid:DXImageTransform.Microsoft.BasicImage(graysale=1);
}
body {
filter: gray;
}
`;
document.head.appendChild(styleElm);
};
selectAllNodes(callback = () => { }) {
const allNodes = document.querySelectorAll('*');
Array.from(allNodes, node => {
callback(node);
});
this.observe({
targetNode: document.documentElement,
config: {
attributes: false
},
callback(mutations, observer) {
const allNodes = document.querySelectorAll('*');
Array.from(allNodes, node => {
callback(node);
});
}
});
};
observe({ targetNode, config = {}, callback = () => { } }) {
if (!targetNode) {
return;
};

config = Object.assign({
attributes: true,
childList: true,
subtree: true
}, config);

const observer = new MutationObserver(callback);
observer.observe(targetNode, config);
};
};
try {
new GrayBackground();
} catch (err) {
console.log('via插件：全网灰色运行出错');
};
})();

});