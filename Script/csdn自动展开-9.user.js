// ==UserScript==
// @name         CSDN自动展开
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/61
// @author       Sky
// @run-at       document-start
// @match        *blog.csdn.net*
// @grant        none
// ==/UserScript==
/*
* @name: CSDN自动展开
* @Author: Sky
* @version: 1.2
* @description: 自动展开CSDN博客文章
* @include: blog.csdn.net
* @createTime: 2020-11-24 12:00
* @updateTime: 2021-5-15 17:00
*/
(function(){const key=encodeURIComponent('CSDN自动展开:执行判断');if(window[key]){return;}try{window[key]=true;document.addEventListener("readystatechange",()=>{const css=document.createElement('style');css.innerText='.article_content{height:auto!important;}.readall_box,.feed-Sign-span,.aside-header-fixed,#operate,.btn_open_app_prompt_div, .wap-shadowbox{display:none!important;}';document.head.appendChild(css);},{'passive':true,'once':true});}catch(err){console.log('CSDN自动展开：',err);}})();