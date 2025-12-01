// ==UserScript==
// @name         Quark Download
// @namespace    http://tampermonkey.net/
// @version      0.2
// @description  点击鼠标中键直接下载夸克网盘内容，无需下载客户端
// @author       Xav1erW
// @match        http*://pan.quark.cn/*
// @icon         data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==
// @grant        none
// @license      MIT
// @downloadURL https://update.greasyfork.org/scripts/448558/Quark%20Download.user.js
// @updateURL https://update.greasyfork.org/scripts/448558/Quark%20Download.meta.js
// ==/UserScript==

async function genDownloadLink(fileid) {
    const rawData = await fetch("https://drive.quark.cn/1/clouddrive/file/download?pr=ucpro&fr=pc", {
        "headers": {
            "accept": "application/json, text/plain, */*",
            "accept-language": "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
            "content-type": "application/json;charset=UTF-8",
            "sec-ch-ua": "\" Not;A Brand\";v=\"99\", \"Microsoft Edge\";v=\"103\", \"Chromium\";v=\"103\"",
            "sec-ch-ua-mobile": "?0",
            "sec-ch-ua-platform": "\"Windows\"",
            "sec-fetch-dest": "empty",
            "sec-fetch-mode": "cors",
            "sec-fetch-site": "same-site"
        },
        "referrer": "https://pan.quark.cn/",
        "referrerPolicy": "strict-origin-when-cross-origin",
        "body": `{\"fids\":[\"${fileid}\"]}`,
        "method": "POST",
        "mode": "cors",
        "credentials": "include"
    });
    const data = await rawData.json();
    const link = data.data[0].download_url
    console.log(link)
    return link;
}

function handleClick(node) {
    // 如果点击鼠标中键
    const fileID = node.getAttribute('data-row-key')
    console.log(fileID)
    genDownloadLink(fileID).then(function (link) {
        window.open(link, '_blank');
    });
}
(function () {
    'use strict';
    window.onmousedown = (e) => {
        if (e.target.className === 'filename' && e.button === 1) {
            handleClick(e.target.parentNode.parentNode);
        }
        else if (e.target.className.includes('filename-text') && e.button === 1) {
            handleClick(e.target.parentNode.parentNode.parentNode.parentNode);
        }
    }
})();