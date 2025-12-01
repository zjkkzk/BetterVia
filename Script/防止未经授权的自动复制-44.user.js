// ==UserScript==
// @name         防止未经授权的自动复制
// @version      23
// @description  在非选词复制时显示小红点提示用户以防止未经授权的自动复制。
// @grant        GM_setClipboard
// @grant        GM_registerMenuCommand
// @grant        GM_setValue
// @grant        GM_getValue
// @run-at       document-start
// @match        *://*/*
// @namespace    https://greasyfork.org/users/452911
// @downloadURL https://update.greasyfork.org/scripts/461625/%E9%98%B2%E6%AD%A2%E6%9C%AA%E7%BB%8F%E6%8E%88%E6%9D%83%E7%9A%84%E8%87%AA%E5%8A%A8%E5%A4%8D%E5%88%B6.user.js
// @updateURL https://update.greasyfork.org/scripts/461625/%E9%98%B2%E6%AD%A2%E6%9C%AA%E7%BB%8F%E6%8E%88%E6%9D%83%E7%9A%84%E8%87%AA%E5%8A%A8%E5%A4%8D%E5%88%B6.meta.js
// ==/UserScript==

(function() {
  'use strict';

  const domain = window.location.hostname;
  let isEnabled = GM_getValue(domain, true); // 默认启用

  function toggleEnabled() {
    isEnabled = !isEnabled;
    GM_setValue(domain, isEnabled);
    alert(`脚本现在${isEnabled ? "已启用" : "已禁用"}于 ${domain}`);
  }

  GM_registerMenuCommand(isEnabled ? `禁用复制监听` : `启用复制监听`, toggleEnabled);

  if (!isEnabled) return; // 如果脚本被禁用，则不执行以下代码

  let hasCopied = false;
  let timeoutId = null;
  let dot = null;

  const handleCopy = function(event) {
    event.preventDefault();
    const selection = window.getSelection().toString();
    if (!hasCopied && selection.trim().length > 0) {
      hasCopied = true;
      createDot(selection);
    }
  };

  const createDot = function(selection) {
    dot = document.createElement('div');
    dot.style.width = '20px';
    dot.style.height = '20px';
    dot.style.zIndex = '9999';
    dot.style.background = 'rgba(255, 0, 0, 0.2)';
    dot.style.borderRadius = '50%';
    dot.style.position = 'fixed';
    dot.style.top = '50%';
    dot.style.right = '10px';
    dot.style.transform = 'translateY(-50%)';
    dot.style.cursor = 'pointer';
    dot.addEventListener('click', function() {
      const shouldCopy = confirm(selection);
      if (shouldCopy) {
        if (typeof GM_setClipboard === "function") {
          GM_setClipboard(selection);
        } else {
          copyToClipboard(selection);
        }
      }
      document.body.removeChild(dot);
      hasCopied = false;
    });
    document.body.appendChild(dot);

    timeoutId = setTimeout(function() {
      if (dot && dot.parentNode) {
        document.body.removeChild(dot);
      }
      hasCopied = false;
      timeoutId = null;
    }, 4000);
  };

  document.addEventListener('selectionchange', handleSelectionChange);

  const copyToClipboard = function(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
  };

  navigator.clipboard.writeText = function(text) {
    return new Promise((resolve, reject) => {
      copyToClipboard(text);
      resolve();
    });
  };

  document.addEventListener('copy', handleCopy, { capture: true });

  window.addEventListener('beforeunload', function() {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
  });

  let startX = null;
  let startY = null;

  document.addEventListener('touchstart', function(e) {
    startX = e.touches[0].clientX;
    startY = e.touches[0].clientY;
  });

  document.addEventListener('touchmove', function(e) {
    const currentX = e.touches[0].clientX;
    const currentY = e.touches[0].clientY;
    const diffX = Math.abs(currentX - startX);
    const diffY = Math.abs(currentY - startY);

    if ((diffX > 10 || diffY > 10) && dot) {
      document.body.removeChild(dot);
      hasCopied = false;
      if (timeoutId) {
        clearTimeout(timeoutId);
        timeoutId = null;
      }
    }
  });

  function handleSelectionChange() {
    if (window.getSelection().toString().trim().length === 0) {
      document.addEventListener('copy', handleCopy, { capture: true });
    } else {
      document.removeEventListener('copy', handleCopy, { capture: true });
    }
  }
})();