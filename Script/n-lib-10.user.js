// ==UserScript==
// @name         N-lib
// @namespace    https://viayoo.com/
// @version      0.1
// @homepageURL  https://app.viayoo.com/addons/25
// @author       Nihility
// @run-at       document-start
// @match        *
// @grant        none
// ==/UserScript==
`
@name N-lib
@version 0.0.3
`
.replace("", (_, __, config_text) => {
const parse = config_text => {
const config_item_regex = new RegExp(`@(\\S+)\\b\\s+([^@\\s]+([^\\r\\n\\S]+[^@\\s]+)*)`, 'gm');
return Object.assign({ include: ['.*'], exclude: [], match: [] },
(config_text.match(config_item_regex) || [])
.map(config_item => config_item.replace(/^\s*@/, '').split(/\s+/))
.reduce((config, item) => {
(config[item[0]] = config[item[0]] || []).push(item[1]);
return config;
}, {}));
};

const once = (config = parse(``)) => fn => {
const include_ptn = new RegExp('^(' + config.include.concat(config.match).join('|') + ')$');
const exclude_ptn = new RegExp('^(' + config.exclude.join('|') + ')$');
if (!window.location.href.match(include_ptn) ||
window.location.href.match(exclude_ptn)) {
return;
}

const hash_code = str => Array.from(str)
.reduce((hash, ch) => (hash << 5) - hash + ch.charCodeAt(), 0);
window.__loaded_scripts__ = window.__loaded_scripts__ || {};
const key = config.name || hash_code(fn.toString());
if (!(key in window.__loaded_scripts__)) {
try {
fn.call(config);
} catch (e) {
window.__loaded_scripts__[key] = e;
console.log(`script "${key}" load failed!`);
}
window.__loaded_scripts__[key] = config;
console.log(`script "${key}" load successful!`);
}
};

once(parse(config_text))(() => {
function exec(x)  {
if (typeof x === 'function') {
once()(x);
} else if (Array.isArray(x)) {
once(parse(x[0]))(x[1]);
}
}
(window.__load_scripts__ || []).forEach(exec);
window.__load_scripts__ = { push: exec };
});

return _;
}),0;