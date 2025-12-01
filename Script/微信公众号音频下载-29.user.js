// ==UserScript==
// @name         微信公众号音频下载
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/2
// @author       谷花泰
// @run-at       document-start
// @match        *mp.weixin.qq.com*
// @grant        none
// ==/UserScript==
/*
* @name: 微信公众号音频下载
* @Author: 谷花泰
* @version: 1.0
* @description: 让你可以直接下载保存喜欢的公众号音频，存着离线也能听。
* @include: mp.weixin.qq.com
* @createTime: 2019-10-04 01:47:08
* @updateTime: 2019-10-08 17:24:27
*/
(window.__load_scripts__ = window.__load_scripts__ || []).push(() => {

(function () {
/* 判断是否该执行 */
const whiteList = ['mp.weixin.qq.com'];
const hostname = window.location.hostname;
const key = encodeURIComponent('谷花泰:微信公众号音频下载:执行判断');

if (whiteList.indexOf(hostname) < 0 || window[key]) {
return;
};

window[key] = true;

/* 开始执行代码 */
class WxAudioDownload {
constructor() {
this._voiceIds = window.reportVoiceid;
this._qqMusicIds = Array.from(document.querySelectorAll('qqmusic[musictype="1"]'), elm => {
return elm.getAttribute('otherid');
});
this._kugouMusicIds = Array.from(document.querySelectorAll('qqmusic[musictype="2"]'), elm => {
return elm.getAttribute('otherid');
});
this.appendElement();
};
/* 普通音频 */
getVoiceInfo(voiceId) {
return new Promise((resolve, reject) => {
const voiceUrl = `https://res.wx.qq.com/voice/getvoice?mediaid=${voiceId}`;
const voiceTitle = document.querySelector(`#voice_title_${voiceId}_0`).innerText;
const voiceAuthor = document.querySelector(`#voice_author_${voiceId}_0`).innerText;
const fileName = `${voiceTitle}--${voiceAuthor}.mp3`;
resolve({
url: voiceUrl,
fileName
});
});
};
/* QQ音乐 */
getQQMusicInfo(musicId) {
return new Promise((resolve, reject) => {
seajs.use("biz_wap/utils/ajax.js", ajax => {
const api = `https://mp.weixin.qq.com/mp/qqmusic?action=get_song_info&song_mid=${musicId}`;
ajax({
url: api,
type: "GET",
dataType: "json",
success(res) {
if (200 == res.http_code) {
const musicData = JSON.parse(res.resp_data);
const musicName = musicData.songlist[0].song_name;
const musicAuthor = musicData.songlist[0].singer_name;
const fileName = `${musicName}--${musicAuthor}.mp3`;
const musicUrl = `${musicData.songlist[0].song_play_url}&__wxtag__=${musicId}`;
resolve({
url: musicUrl,
fileName
});
};
},
error(err) {
reject(err);
}
});
});
});
};
/* 酷狗音乐 */
getKugouMusicInfo(musicId) {
return new Promise((resolve, reject) => {
seajs.use("biz_wap/utils/ajax.js", ajax => {
const params = [
{
akey: musicId,
albumid: ""
}
];
const api = `https://mp.weixin.qq.com/mp/getkugousong?params=${encodeURIComponent(JSON.stringify(params))}`;
ajax({
url: api,
type: "GET",
dataType: "json",
success(res) {
const musicTag = document.querySelector(`qqmusic[otherid="${musicId}"]`);
const musicName = musicTag.getAttribute('music_name');
const musicAuthor = musicTag.getAttribute('singer');
const fileName = `${musicName}--${musicAuthor}.mp3`;
const musicUrl = `${res.data[0].url}?&__wxtag__=${musicId}`;
resolve({
url: musicUrl,
fileName
});
},
error(err) {
reject(err);
}
})
});
});
};
/* 下载函数 */
downloadFile({ url, fileName }) {
let _fileName = fileName;
const a = document.createElement('a');
a.download = _fileName;
a.href = url;
a.style.display = "none";
document.body.appendChild(a);
a.click();
document.body.removeChild(a);
};
/* 把下载按钮添加到音乐后 */
appendElement() {
const mpvoiceElms = document.querySelectorAll('mpvoice');
const musicElms = document.querySelectorAll('qqmusic');

/* 普通音频 */
Array.from(mpvoiceElms, elm => {
const voiceId = elm.getAttribute('voice_encode_fileid');
const playerElm = elm.parentNode.querySelector('mpvoice+span');
const downloadElm = this.createDownloadElement(this.getVoiceInfo, voiceId);
this.insertAfter(downloadElm, playerElm);
});

/* QQ音乐与酷狗音乐 */
Array.from(musicElms, elm => {
const musicId = elm.getAttribute('otherid');
/* 1为QQ音乐，2为酷狗音乐 */
const type = Number(elm.getAttribute('musictype'));
const playerElm = elm.parentNode.querySelector('qqmusic+span');
/* 获取函数的名字 */
let getInfoName = 'getQQMusicInfo';
if (type === 1) {
getInfoName = 'getQQMusicInfo';
} else if (type === 2) {
getInfoName = 'getKugouMusicInfo';
};
const downloadElm = this.createDownloadElement(this[getInfoName], musicId);
this.insertAfter(downloadElm, playerElm);
});
};
/* 创建下载按钮 */
createDownloadElement(getDownloadInfo, audioId) {
const div = document.createElement('div');
div.setAttribute('style', `
width: 100% !important;
height: 40px;
text-align: center;
font-size: 16px;
line-height: 40px;
color: #333;
background-color: #fdfdfd;
border-radius: 2px;
font-weight: normal !important;
border: 1px solid #e2e2e2;
`);
div.innerText = '下载此音乐';
div.onclick = () => {
getDownloadInfo(audioId).then(res => {
const { url, fileName } = res;
if (!url) {
alert('恭喜，啥也没有');
return;
};
this.downloadFile({ url, fileName });
}).catch(err => console.log(err));
};
return div;
};
/*把节点插入到某节点后面*/
insertAfter(newElement, targetElement) {
const parentElement = targetElement.parentNode;
if (parentElement.lastChild == targetElement) {
parentElement.appendChild(newElement);
} else {
const next = targetElement.nextSibling;
parentElement.insertBefore(newElement, next);
};
};
};
/* 监听节点加载 */
const maxTime = 5000;
const timeInterval = 100;
let pastTime = 0;
const timerId = setInterval(() => {
pastTime += timeInterval;
if (pastTime < 1000) {
return;
};
const qqmusic = document.querySelector('qqmusic');
const mpvoice = document.querySelector('mpvoice');
if (qqmusic || mpvoice || maxTime < pastTime) {
new WxAudioDownload();
clearInterval(timerId);
return;
};
}, timeInterval);
})();

});