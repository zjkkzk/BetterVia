// ==UserScript==
// @name         自由编辑页面文字
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/21
// @author       Dnomd343
// @run-at       document-start
// @match        *
// @grant        none
// ==/UserScript==
/*
* @name: 自由编辑页面文字
* @Author: Dnomd343
* @version: 1.1
* @description: 随时启动或停止编辑
* @include: *
* @createTime: 2019-10-25 01:22:34
* @updateTime: 2022-04-03 00:56:21
*/
var freeEditFlag = true;
var freeEditIcon = document.createElement("div");
freeEditIcon.setAttribute("style", `
font-size: 0vw !important;
width: 6vw !important;
height: 6vw !important;
background-color: rgba(30,30,30,0.1) !important;
box-shadow: 0px 0px 1px rgba(0,0,0,0) !important;
position: fixed !important;
bottom: 10vh !important;
right: 3vw !important;
border-radius: 100% !important;
`);

freeEditIcon.onclick = function() {
if (freeEditFlag) {
javascript: document.body.contentEditable = 'true';
document.designMode = 'on';
freeEditFlag = false;
} else {
javascript: document.body.contentEditable = 'false';
document.designMode = 'off';
freeEditFlag = true;
}
};

document.getElementsByTagName("html").item(0).appendChild(freeEditIcon);