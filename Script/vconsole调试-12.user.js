// ==UserScript==
// @name         vconsole调试
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/13
// @author       谷花泰
// @run-at       document-start
// @match        *
// @grant        none
// ==/UserScript==
/*
* @name: vconsole调试
* @Author: 腾讯
* @version: 1.0
* @description: 让你可以在手机调试网页
* @include: *
* @createTime: 2019-10-11 08:38:28
* @updateTime: 2019-10-13 14:55:19
*/
(function () {
/* 判断是否该执行 */
const key = encodeURIComponent('谷花泰:vconsole调试:执行判断');
if (window[key]) {
return;
};
window[key] = true;

/* 开始执行代码 */
const script = document.createElement('script');
script.src = 'https://cdn.bootcss.com/vConsole/3.3.4/vconsole.min.js';
document.head.appendChild(script);
setTimeout(() => {
new VConsole()
}, 800);
})();