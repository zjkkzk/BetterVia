// ==UserScript==
// @name         强制缩放
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/5
// @author       谷花泰
// @run-at       document-start
// @match        *
// @grant        none
// ==/UserScript==
/*
* @name: 强制缩放
* @Author: 谷花泰
* @version: 1.0
* @description: 让所有网页都可以缩放
* @include: *
* @createTime: 2019-10-04 01:47:08
* @updateTime: 2019-10-09 14:46:02
*/
(function () {
/* 判断是否该执行 */
const key = encodeURIComponent('谷花泰:强制缩放:执行判断');

if (window[key]) {
return;
};

window[key] = true;

/* 开始执行代码 */
const meta = document.createElement('meta');
meta.setAttribute('name', 'viewport');
meta.setAttribute('content', 'width=device-width, initial-scale=1, user-scalable=yes');
document.head.appendChild(meta);
})();