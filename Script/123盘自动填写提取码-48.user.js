// ==UserScript==
// @name         123盘自动填写提取码
// @namespace    http://www.123pan.com
// @version      1.17
// @description  自动填写提取码
// @match        https://www.123pan.com/*
// @match        https://www.123pan.cn/*
// @match        https://www.123912.com/*
// @match        https://www.123865.com/*
// @grant        none
// @downloadURL https://update.greasyfork.org/scripts/489660/123%E7%9B%98%E8%87%AA%E5%8A%A8%E5%A1%AB%E5%86%99%E6%8F%90%E5%8F%96%E7%A0%81.user.js
// @updateURL https://update.greasyfork.org/scripts/489660/123%E7%9B%98%E8%87%AA%E5%8A%A8%E5%A1%AB%E5%86%99%E6%8F%90%E5%8F%96%E7%A0%81.meta.js
// ==/UserScript==

(function() {
    'use strict';

    if (window.location.hostname !== 'www.123pan.com') {
        const newUrl = window.location.href.replace(window.location.hostname, 'www.123pan.com');
        window.location.href = newUrl;
    }

    let currentUrl = decodeURIComponent(window.location.href);
    let regex1 = /https:\/\/www\.123(pan|912)\.co(m|n)\/s\/[0-9a-zA-Z]+-[0-9a-zA-Z]+(\.html)?/;
    if (!currentUrl.includes('pwd=')) {

        if (regex1.test(currentUrl)) {
            let str1 = regex1.exec(currentUrl)[0];
            currentUrl = currentUrl.replace(str1, '');

            let regex2 = /[0-9a-zA-Z]{4}/g;
            let match = regex2.exec(currentUrl);
            if (!match) return;
            let str2 = match[0];

            let newLink = str1 + '?pwd=' + str2;

            window.location.href = newLink;

        }
    } else {

        let pwd = new URLSearchParams(window.location.search).get("pwd");

        let shareKey = window.location.pathname.match(/\w+-\w+/)[0];
        localStorage.setItem("shareKey", shareKey);

        localStorage.setItem("SharePwd", pwd);

        const element = document.querySelector(".appBottomBtn");
        if (!element) window.location.reload();
    }
})();