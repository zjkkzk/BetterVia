// ==UserScript==
// @name         长按触发鼠标悬停
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/39
// @author       Sky
// @run-at       document-start
// @match        *
// @grant        none
// ==/UserScript==
/*
* @name: 长按触发鼠标悬停
* @Author: Sky
* @version: 1.5
* @description: 通过长按展开鼠标悬停菜单
* @include: *
* @createTime: 2020-3-4 03:00
* @updateTime: 2020-5-4 19:45
*/
(function(){
const key = encodeURIComponent('长按触发鼠标悬停:执行判断'),
flag = navigator.userAgent.indexOf('Android') < 0 && navigator.userAgent.indexOf('iPhone') < 0;
if(!flag || window[key]){return;}
try {
window[key] = true;
const evt = new Event('mouseover', {'bubbles':true, 'cancelable':true});
document.addEventListener('touchstart', function(e){
e.target.dispatchEvent(evt);
}, {'passive':true, 'capture':true});
} catch(err){console.log('长按触发鼠标悬停：', err);}
})();