// ==UserScript==
// @name         蓝奏云重定向+记住密码
// @namespace    https://greasyfork.org/zh-CN/scripts?set=589091
// @version      1.37
// @description  自动将所有蓝奏云链接重定向至lanzn.com。自动记住并填写蓝奏云密码。
// @author       呆呆
// @include      *.lanosso.com/*
// @include      *.lanzn.com/*
// @include      *.lanzog.com/*
// @include      *.lanpw.com/*
// @include      *.lanpv.com/*
// @include      *.lanzv.com/*
// @include      *://*.lanz*.com/*
// @include      *://lanz*.com/*
// @grant        GM_registerMenuCommand
// @grant        GM_setValue
// @grant        GM_getValue
// @grant        GM_listValues
// @grant        GM_deleteValue
// @grant        GM_notification
// @run-at       document-end
// @require      https://cdn.jsdelivr.net/npm/sweetalert2@11
// @downloadURL https://update.greasyfork.org/scripts/488847/%E8%93%9D%E5%A5%8F%E4%BA%91%E9%87%8D%E5%AE%9A%E5%90%91%2B%E8%AE%B0%E4%BD%8F%E5%AF%86%E7%A0%81.user.js
// @updateURL https://update.greasyfork.org/scripts/488847/%E8%93%9D%E5%A5%8F%E4%BA%91%E9%87%8D%E5%AE%9A%E5%90%91%2B%E8%AE%B0%E4%BD%8F%E5%AF%86%E7%A0%81.meta.js
// ==/UserScript==

function Toast(text) {
    if (typeof(window.via) == "object") window.via.toast(text);
    else if (typeof(window.mbrowser) == "object") window.mbrowser.showToast(text);
}
// 获取当前网页链接
var currentUrl = window.location.href.split('?')[0];
// 检查当前网址
if (!currentUrl.startsWith('https://www.lanzn.com/')) {
    // 替换域名
    var newUrl = currentUrl.replace(window.location.hostname, 'www.lanzn.com');
    Toast("重定向中....");
    // 重新访问新链接
    window.location.href = newUrl;
}

document.querySelector('div.fbox').textContent = "会员文件，需要开桌面模式下载";

/*
 * 蓝奏云网盘增强
 */
// 滚动条事件
function windowScroll(fn1) {
    var beforeScrollTop = document.documentElement.scrollTop,
        fn = fn1 || function() {};
    setTimeout(function() {
        window.addEventListener('scroll', function(e) {
            var afterScrollTop = document.documentElement.scrollTop,
                delta = afterScrollTop - beforeScrollTop;
            if (delta == 0) return false;
            fn(delta > 0 ? 'down' : 'up', e);
            beforeScrollTop = afterScrollTop;
        }, false);
    }, 1000)
}
// 自动显示更多文件
function fileMoreS() {
    windowScroll(function(direction, e) {
        if (direction === 'down') {
            let scrollTop = document.documentElement.scrollTop || window.pageYOffset || document.body.scrollTop;
            let scrollDelta = 500;
            if (document.documentElement.scrollHeight <= document.documentElement.clientHeight + scrollTop + scrollDelta) {
                let filemore = document.getElementById('filemore');
                if (filemore && filemore.style.display != 'none') {
                    if (filemore.textContent.indexOf('更多') > -1) {
                        filemore.click();
                    }
                }
            }
        }
    });
}
setTimeout(function() {
    if (document.getElementById('infos')) {
        fileMoreS();
    }
}, 500);
/* * * */

// 使用GM_registerMenuCommand添加管理密码的菜单命令
GM_registerMenuCommand('查看密码', function() {
    // 获取所有存储的键
    const allKeys = GM_listValues();
    if (allKeys.length === 0 || (allKeys.length === 1 && !allKeys[0])) {
        Swal.fire({
            position: "top",
            icon: "question",
            title: "没有存储的密码",
            showConfirmButton: false,
            timer: 1000,
        });
        return;
    }
    // 存储所有存储的数据
    const allStoredData = {};
    // 遍历所有键，并获取对应的值
    allKeys.forEach(key => {
        const value = GM_getValue(key);
        allStoredData[key] = value;
    });
    // 构建展示消息
    let message = '';
    Object.entries(allStoredData).forEach(([key, value]) => {
        if (value.title !== undefined) message += `${value.title} (${key})：${value.password}\n┈┈┈┈┈┈┈┈┈┈┈┈\n`;
    });
    // 显示消息
    alert(message);
});

// 使用GM_registerMenuCommand添加管理密码的菜单命令
GM_registerMenuCommand('🔒管理密码', function() {
    // 获取所有存储的键值对
    const allKeys = GM_listValues();
    if (allKeys.length === 0 || (allKeys.length === 1 && !allKeys[0])) {
        Swal.fire({
            position: "top",
            icon: "question",
            title: "没有存储的密码",
            showConfirmButton: false,
            timer: 1000,
        });
        return;
    }
    const allStoredData = {};
    allKeys.forEach(key => {
        const value = GM_getValue(key);
        allStoredData[key] = value;
    });

    // 构建展示消息
    let message = '';
    Object.entries(allStoredData).forEach(([key, value]) => {
        if (value.title !== undefined) message += `${value.title} (${key})：${value.password}\n`;
    });

    // 提示用户编辑密码列表
    Swal.fire({
        title: '使用\'换行 \\n \'分割',
        input: 'textarea',
        inputValue: message,

        // 设置input属性
        inputAttributes: {
            autocapitalize: 'off',
            style: 'font-size: 12px;'
        },
        showCancelButton: true,
        cancelButtonText: '取消',
        confirmButtonText: '保存',
        showLoaderOnConfirm: true, // 当为loading的时候取消confirm按钮并显示加载组件
        preConfirm: (editedList) => {
            if (editedList === '') {
                Swal.fire({
                    title: "确定清空密码?",
                    icon: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#3085d6",
                    cancelButtonColor: "#d33",
                    cancelButtonText: '取消',
                    confirmButtonText: "确定"
                }).then((result) => {
                    if (result.isConfirmed) {
                        const allKeys = GM_listValues();
                        allKeys.forEach(key => {
                            GM_deleteValue(key);
                        });
                        Swal.fire({
                            position: "top",
                            icon: "success",
                            title: "已清空",
                            showConfirmButton: false,
                            timer: 1000,
                        });
                    }
                });
            } else if (editedList !== null) {
                // 清空所有存储的密码
                const allKeys = GM_listValues();
                allKeys.forEach(key => {
                    GM_deleteValue(key);
                });

                // 将编辑后的列表转换为标准格式并存储
                let editedLines = editedList.split('\n');
                const linePattern = /\((.*?)\)/;
                editedLines.forEach(line => {
                    const urlMatch = line.match(linePattern);
                    if (urlMatch) {
                        const title = line.substring(0, urlMatch.index).trim();
                        const url = urlMatch[1].trim();
                        const password = line.substring(line.indexOf('：') + 1).trim();
                        GM_setValue(url, {
                            title, password
                        });
                    }
                });
                Swal.fire({
                    position: "top",
                    icon: "success",
                    title: "密码列表已更新!",
                    showConfirmButton: false,
                    timer: 1000,
                });
            }
        },
        // 如果设置为false则不允许点击对话框以外的背景来关闭对话框
        allowOutsideClick: () => Swal.isLoading(),
    }).then((result) => {
        if (!result.isConfirmed) {
            Swal.fire({
                position: "top",
                icon: "error",
                title: "已取消",
                showConfirmButton: false,
                timer: 1000,
            });
        }
    });
});

// 获取文本框元素
const inputElement = document.getElementById('pwd');
// 获取按钮元素
const subButton = document.getElementById('sub');
const passwddivButton = document.querySelector('.passwddiv-btn');
// 获取存储的值
const storedCredentials = GM_getValue(currentUrl);

// 检查是否存在存储的值
if (storedCredentials) {
    // 如果有存储的数据，将其填充到文本框中
    inputElement.value = storedCredentials.password;
    // 检查是否存在.passwddiv-btn元素
    if (passwddivButton) {
        // 如果存在，点击.passwddiv-btn
        passwddivButton.click();
    } else {
        // 否则点击sub
        subButton.click();
    }
}

// 自动下载
const selectorsToClick = [
  'a.appa',
  'a[href="javascript:filego();"]',
  'a[href^="/tp/"]'
];

// 遍历每个选择器并点击匹配的元素
selectorsToClick.forEach(selector => {
  document.querySelectorAll(selector).forEach(link => link.click());
});

// 检查下载按钮并点击的函数
function clickDownloadButton() {
    var downloadButton = $(".mh a[target='_blank']");
    if (downloadButton.length > 0) {
        downloadButton[0].click();
    }
}
// 使用 MutationObserver 观察 DOM 中的变化
var observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
        if (mutation.addedNodes.length > 0) {
            clickDownloadButton();
        }
    });
});
// 开始观察目标节点的配置变化
var targetNode = document.querySelector(".mh");
if (targetNode) {
    observer.observe(targetNode, { childList: true, subtree: true });
}

const submitButton = document.getElementById('submit');
if (submitButton && inputElement) {
    submitButton.click();
    if (storedCredentials) {
        inputElement.value = storedCredentials.password;
        if (passwddivButton) {
            passwddivButton.click();
        } else {
            subButton.click();
        }
        clickDownloadButton();
    } else Toast('需要密码');
}

/*
 * 蓝奏云自动点击下载
 * 大萌主
 */

function extractAndNavigateURL() {
  // 获取整个页面的 HTML 源码，包括 script 标签中的内容
  var htmlSource = document.documentElement.innerHTML;
  // 使用正则表达式匹配并提取 vkjxld 和 hyggid 变量的值
  var vkjxldMatch = htmlSource.match(/\nvar vkjxld\s*=\s*['"]([^'"]+)['"];/);
  var hyggidMatch = htmlSource.match(/var hyggid\s*=\s*['"]([^'"]+)['"];/);
  // 确保匹配并提取成功
  if (vkjxldMatch && hyggidMatch) {
    // 分别获取匹配到的值
    var vkjxldValue = vkjxldMatch[1];
    var hyggidValue = hyggidMatch[1];
    // 拼接得到完整 URL
    var completeURL = vkjxldValue + hyggidValue;
    // 在当前标签页打开拼接好的 URL
    window.location.href = completeURL;
  } else {
    (function() {
      // 获取整个页面的 HTML 源码，包括 script 标签中的内容
      var htmlSource = document.documentElement.innerHTML;
      // 使用正则表达式匹配并提取 link 变量的值
      var urlptMatch = htmlSource.match(/var urlpt\s*=\s*['"]([^'"]+)['"];/);
      var linkMatch = htmlSource.match(/var link\s*=\s*['"]([^'"]+)['"];/);
      if (urlptMatch && linkMatch) {
        // 获取当前页面的域名，包括协议部分
        let urlptValue = urlptMatch[1];
        if(urlptValue === '/' ) urlptValue = window.location.origin;
        // 获取匹配到的 link 变量的值
        var linkValue = linkMatch[1];
        // 拼接当前域名和 link 变量的值
        var completeURL = urlptValue + '/' + linkValue;
        // 在当前标签页打开拼接好的 URL
        window.location.href = completeURL;
      } else {
      }
    })();
  }
}
extractAndNavigateURL();

/* * * */

inputElement.addEventListener('input', function() {
    let pageTitle = document.title;
    let forTitle = 0;
    // 如果当前标题是'文件'，则循环获取新标题
    const titleInterval = setInterval(function() {
        pageTitle = document.title;
        if (pageTitle !== '文件' || forTitle > 15) {
            clearInterval(titleInterval);
            const credentials = {
                title: pageTitle,
                password: inputElement.value
            };
            // 将对象存储在GM存储中
            GM_setValue(currentUrl, credentials);
            clickDownloadButton();
        }
        forTitle++;
    }, 1000);
});