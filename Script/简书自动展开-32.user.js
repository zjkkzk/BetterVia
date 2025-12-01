// ==UserScript==
// @name         简书自动展开
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/69
// @author       Sky
// @run-at       document-start
// @match        *www.jianshu.com*
// @grant        none
// ==/UserScript==
/*
* @name: 简书自动展开
* @Author: Sky
* @version: 1.1
* @description: 自动展开CSDN博客文章
* @include: www.jianshu.com
* @createTime: 2021-6-25 0:40
* @updateTime: 2021-7-6 22:50
*/
(window.__load_scripts__ = window.__load_scripts__ || []).push(() => {

(function(){const key=encodeURIComponent('简书自动展开:执行判断');if(window[key]){return;}try{window[key]=true;const css=document.createElement('style');css.innerText='.collapse-free-content{height:auto!important;}.header-wrap,.call-app-btn,[aria-label="amc-ad"],[aria-label="wph-ad"],.recommend-ad,.collapse-tips,.collapse-free-content:after{display:none!important;}';document.head.appendChild(css);let checkCount=0;var checkTimer=setInterval(function(){if(++checkCount>10){clearInterval(checkTimer);}let BJHbtn=document.querySelectorAll('.download-app-guidance div.wrap-item-btn')[1];if(BJHbtn){BJHbtn.dispatchEvent(new Event('click',{'bubbles':true,'cancelable':true}));clearInterval(checkTimer);}},666);}catch(err){console.log('简书自动展开：',err);}})();

});