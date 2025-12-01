package com.jiguro.bettervia;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import org.json.*;

import android.content.ClipboardManager;

public class Hook implements IXposedHookLoadPackage {

	/* ================== 模块基本信息 ================== */
	private static final String MODULE_VERSION_NAME = "1.2.0";
	private static final int MODULE_VERSION_CODE = 20251202;

	/* ================== 上下文和组件引用 ================== */
	private static Activity Context = null; // 主界面 Activity
	private static Object moduleButtonRef = null; // 设置按钮对象

	/* ================== 功能开关配置键 ================== */
	// 核心功能开关
	private static final String KEY_WHITELIST = "enable_whitelist_hook";
	private static final String KEY_B_HOOK = "enable_b_hook";
	private static final String KEY_COMPONENT_BLOCK = "component_block_settings";
	private static final String KEY_BLOCK_STARTUP_MESSAGE = "block_startup_message";
	private static final String KEY_BLOCK_GOOGLE_SERVICES = "block_google_services";

	// 界面美化功能
	private static final String KEY_EYE_PROTECTION = "eye_protection_mode";
	private static final String KEY_EYE_TEMPERATURE = "eye_protection_temperature";
	private static final String KEY_EYE_TEXTURE = "eye_protection_texture";
	private static final String KEY_HOMEPAGE_BG = "homepage_background_image";
	private static final String KEY_HOMEPAGE_MASK_A = "homepage_mask_alpha";
	private static final String KEY_HOMEPAGE_MASK_C = "homepage_mask_color";
	private static final String KEY_HIDE_STATUS_BAR = "hide_status_bar";
	private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
	private static final String KEY_SCREENSHOT_PROTECTION = "screenshot_protection";

	// 网络和资源功能
	private static final String KEY_NETWORK_SOURCE = "network_source";
	private static final String KEY_AUTO_UPDATE = "auto_update";
	private static final String KEY_HOMEPAGE_THEME = "homepage_theme_settings";
	private static final String KEY_CURRENT_THEME = "current_homepage_theme";
	private static final String KEY_SCRIPT_REPOSITORY = "script_repository_settings";
	private static final String KEY_AD_BLOCK_RULES = "ad_block_rules_settings";
	private static final String KEY_SEARCH_COMMANDS = "search_commands_settings";
	private static final String KEY_COOKIE_MANAGEMENT = "cookie_management_settings";

	// 其他
	private static final String KEY_USER_AGENT = "user_agent_settings";

	/* ================== 运行时状态缓存 ================== */
	// 功能开关状态
	private static boolean whitelistHookEnabled = true;
	private static boolean bHookEnabled = false;
	private static boolean eyeProtectionEnabled = false;
	private static boolean blockGoogleServicesEnabled = false;
	private static boolean blockStartupMessageEnabled = false;
	private static boolean screenshotProtectionEnabled = false;
	private static boolean keepScreenOnEnabled = false;
	private static boolean hideStatusBarEnabled = false;
	private static boolean autoUpdateEnabled = true;

	// 配置参数
	private static int eyeTemperature = 50;
	private static int eyeTexture = 0;
	private static String homepageBgPath = "";
	private static int homepageMaskAlpha = 120;
	private static int homepageMaskColor = 0x80000000;

	/* ================== Hook 引用管理 ================== */
	private static XC_MethodHook.Unhook whitelistHook = null;
	private static XC_MethodHook.Unhook bHook = null;
	private static XC_MethodHook.Unhook componentHook = null;
	private static XC_MethodHook.Unhook activityHook = null;
	private static XC_MethodHook.Unhook firebaseAnalyticsHook = null;
	private static XC_MethodHook.Unhook googleAnalyticsHook = null;
	private static XC_MethodHook.Unhook screenshotProtectionHook = null;
	private static XC_MethodHook.Unhook keepScreenOnHook = null;
	private static XC_MethodHook.Unhook hideStatusBarHook = null;

	/* ================== 组件屏蔽配置 ================== */
	private static final String[] COMPONENT_KEYS = {"block_update", // 0: 检查更新
			"block_telegram", // 1: Telegram
			"block_qq", // 2: QQ
			"block_email", // 3: 邮件
			"block_wechat", // 4: 微信
			"block_donate", // 5: 捐助我们
			"block_assist", // 6: 协助翻译
			"block_agreement", // 7: 协议
			"block_privacy", // 8: 隐私
			"block_opensource", // 9: 开源
			"block_icp" // 10: 备案
	};

	/* ================== 网络源配置 ================== */
	private static final String NETWORK_SOURCE_GITEE = "gitee";
	private static final String NETWORK_SOURCE_GITHUB = "github";
	private static final String DEFAULT_NETWORK_SOURCE = NETWORK_SOURCE_GITEE;

	// 主题和更新URL
	private static final String GITEE_THEMES_JSON_URL = "https://gitee.com/jiguro/BetterVia/raw/master/themes.json";
	private static final String GITHUB_THEMES_JSON_URL = "https://raw.githubusercontent.com/JiGuroLGC/BetterVia/main/Theme/themes.json";
	private static final String GITHUB_UPDATE_URL = "https://raw.githubusercontent.com/JiGuroLGC/BetterVia/main/update.json";
	private static final String GITEE_UPDATE_URL = "https://gitee.com/JiGuro/BetterVia/raw/master/update.json";

	/* ================== 数据缓存和状态管理 ================== */
	// 主题数据
	private static List<ThemeInfo> loadedThemes = new ArrayList<>();
	private static boolean themesLoaded = false;
	private static boolean themesLoading = false;

	// Activity状态管理
	private static Map<Activity, View> overlayViews = new WeakHashMap<>();
	private static Map<Activity, Boolean> screenOnActivities = new WeakHashMap<>();
	private static Map<Activity, Boolean> statusBarHiddenActivities = new WeakHashMap<>();
	private static Map<Activity, Runnable> statusBarRehideRunnables = new WeakHashMap<>();
	private static final int REHIDE_DELAY = 3000;

	// Cookie管理
	private static final String DEFAULT_THEME_ID = "default";
	private static final String COOKIE_TABLE_NAME = "cookies";

	// 主题数据结构
	private static class ThemeInfo {
		String id;
		Map<String, String> nameMap;
		Map<String, String> authorMap;
		String previewUrl;
		Map<String, String> htmlUrls;
		Map<String, String> cssUrls;

		ThemeInfo(String id, Map<String, String> nameMap, Map<String, String> authorMap, String previewUrl,
				Map<String, String> htmlUrls, Map<String, String> cssUrls) {
			this.id = id;
			this.nameMap = nameMap;
			this.authorMap = authorMap;
			this.previewUrl = previewUrl;
			this.htmlUrls = htmlUrls;
			this.cssUrls = cssUrls;
		}

		// 获取当前语言的主题名称
		String getName(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return nameMap.getOrDefault(langCode, nameMap.get("zh-CN"));
		}

		// 获取当前语言的主题作者
		String getAuthor(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return authorMap.getOrDefault(langCode, authorMap.get("zh-CN"));
		}

		// 添加JSON构造函数
		static ThemeInfo fromJSON(JSONObject json) throws JSONException {
			String id = json.getString("id");

			// 解析多语言名称
			Map<String, String> nameMap = new HashMap<>();
			JSONObject names = json.getJSONObject("names");
			Iterator<String> nameKeys = names.keys();
			while (nameKeys.hasNext()) {
				String lang = nameKeys.next();
				nameMap.put(lang, names.getString(lang));
			}

			// 解析多语言作者
			Map<String, String> authorMap = new HashMap<>();
			JSONObject authors = json.getJSONObject("authors");
			Iterator<String> authorKeys = authors.keys();
			while (authorKeys.hasNext()) {
				String lang = authorKeys.next();
				authorMap.put(lang, authors.getString(lang));
			}

			String previewUrl = json.getString("previewUrl");

			// 解析HTML URLs
			Map<String, String> htmlUrls = new HashMap<>();
			JSONObject htmls = json.getJSONObject("htmlUrls");
			Iterator<String> htmlKeys = htmls.keys();
			while (htmlKeys.hasNext()) {
				String pkg = htmlKeys.next();
				htmlUrls.put(pkg, htmls.getString(pkg));
			}

			// 解析CSS URLs
			Map<String, String> cssUrls = new HashMap<>();
			JSONObject csss = json.getJSONObject("cssUrls");
			Iterator<String> cssKeys = csss.keys();
			while (cssKeys.hasNext()) {
				String pkg = cssKeys.next();
				cssUrls.put(pkg, csss.getString(pkg));
			}

			return new ThemeInfo(id, nameMap, authorMap, previewUrl, htmlUrls, cssUrls);
		}

		// 获取当前语言代码
		private String getLanguageCode(Context ctx) {
			String saved = getSavedLanguageStatic(ctx);
			if ("auto".equals(saved)) {
				// 获取系统语言
				Locale locale;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					locale = ctx.getResources().getConfiguration().getLocales().get(0);
				} else {
					locale = ctx.getResources().getConfiguration().locale;
				}

				if (Locale.SIMPLIFIED_CHINESE.equals(locale)) {
					return "zh-CN";
				} else if (Locale.TRADITIONAL_CHINESE.equals(locale)) {
					return "zh-TW";
				} else if (Locale.ENGLISH.equals(locale)) {
					return "en";
				}
				return "zh-CN"; // 默认简体中文
			}
			return saved;
		}
	}

	/* ================== 入口 ================== */
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam pkg) throws Throwable {
		// 通过自己Hook自己进行激活校验
		if (pkg.packageName.equals("com.jiguro.bettervia")) {
			try {
				Class<?> clazz = pkg.classLoader.loadClass("com.jiguro.bettervia.ModuleStatus"); // 加载校验类
				java.lang.reflect.Field field = clazz.getDeclaredField("activated");
				field.setAccessible(true);
				field.setBoolean(null, true);
			} catch (Throwable ignored) {
			}
			return;
		}

		if (pkg.packageName.equals("mark.via")) {
			handleViaApp(pkg);
		} else if (pkg.packageName.equals("mark.via.gp")) {
			handleViaGpApp(pkg);
		}
	}

	/* =========================================================
	 * Via 国内版主处理逻辑
	 * ======================================================= */
	private void handleViaApp(final XC_LoadPackage.LoadPackageParam param) {
		XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam attachParam) throws Throwable {
				final Context ctx = (Context) attachParam.args[0];
				final ClassLoader cl = ctx.getClassLoader();

				/* ================== 阻断检查 ================== */
				if (shouldBlockHook(ctx, param.packageName)) {
					showBlockedToast(ctx);
					return;
				}

				/* ================== Activity上下文获取 ================== */
				if (Context == null) {
					XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							if (Context == null) {
								Context = (Activity) param.thisObject;
								// 只有在未屏蔽启动提示时才显示消息
								if (!getPrefBoolean(ctx, KEY_BLOCK_STARTUP_MESSAGE, false)) {
									jiguroMessage(getLocalizedString(ctx, "hook_success_message"));
								}
								XposedBridge.log("得到 ActivityContext");
							}
						}
					});
				}

				/* ================== 功能模块初始化 ================== */

				// 护眼模式初始化
				eyeProtectionEnabled = getPrefBoolean(ctx, KEY_EYE_PROTECTION, false);
				eyeTemperature = getPrefInt(ctx, KEY_EYE_TEMPERATURE, 50);
				eyeTexture = getPrefInt(ctx, KEY_EYE_TEXTURE, 0);
				setEyeProtectionMode(ctx, cl, eyeProtectionEnabled);

				// 白名单功能初始化
				whitelistHookEnabled = getPrefBoolean(ctx, KEY_WHITELIST, true);
				setWhitelistHook(ctx, cl, whitelistHookEnabled);

				// b方法Hook初始化
				bHookEnabled = getPrefBoolean(ctx, KEY_B_HOOK, false);
				setBHook(ctx, cl, bHookEnabled);

				// 组件屏蔽Hook初始化
				setComponentBlockHook(ctx, cl, true);

				// 截屏防护初始化
				screenshotProtectionEnabled = getPrefBoolean(ctx, KEY_SCREENSHOT_PROTECTION, false);
				setScreenshotProtection(ctx, cl, screenshotProtectionEnabled);

				// 隐藏状态栏初始化
				hideStatusBarEnabled = getPrefBoolean(ctx, KEY_HIDE_STATUS_BAR, false);
				setHideStatusBar(ctx, cl, hideStatusBarEnabled);

				/* ================== 界面美化功能 ================== */

				// 主页背景设置
				homepageBgPath = getPrefString(ctx, KEY_HOMEPAGE_BG, "");
				homepageMaskAlpha = getPrefInt(ctx, KEY_HOMEPAGE_MASK_A, 120);
				homepageMaskColor = getPrefInt(ctx, KEY_HOMEPAGE_MASK_C, 0x80000000);
				if (!homepageBgPath.equals("")) {
					hookHomepageBgWithMask(ctx, cl, homepageBgPath, homepageMaskColor);
				}

				/* ================== 网络服务功能 ================== */

				// Google服务拦截初始化
				boolean blockGoogleServices = getPrefBoolean(ctx, KEY_BLOCK_GOOGLE_SERVICES, false);
				setGoogleServicesInterceptHook(ctx, cl, blockGoogleServices);

				// 自动更新检查
				autoUpdateEnabled = getPrefBoolean(ctx, KEY_AUTO_UPDATE, true);
				if (autoUpdateEnabled) {
					checkUpdateOnStart(ctx);
				}

				/* ================== 核心功能Hook ================== */

				// 清空u方法逻辑（白名单功能）
				if (whitelistHookEnabled) {
					XposedHelpers.findAndHookMethod("k.a.a0.i.k", cl, "u", "k.a.a0.i.a", new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.setResult(null);
							XposedBridge.log("已清空 mark.via 的 u 方法逻辑");
						}
					});
				}

				// 屏幕常亮功能
				keepScreenOnEnabled = getPrefBoolean(ctx, KEY_KEEP_SCREEN_ON, false);
				setKeepScreenOn(ctx, cl, keepScreenOnEnabled);

				/* ================== 界面设置功能 ================== */

				// 设置列表添加"模块"按钮
				XposedHelpers.findAndHookMethod("k.a.m0.f7", cl, "f3", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						List<Object> orig = (List<Object>) param.getResult();
						if (orig == null)
							orig = new ArrayList<>();
						List<Object> nList = new ArrayList<>(orig);

						Class<?> yClass = XposedHelpers.findClass("e.h.g.g.y", cl);
						String txt = getLocalizedString(ctx, "module_settings");
						Object btn = XposedHelpers.newInstance(yClass, 1000, txt);
						moduleButtonRef = btn;
						nList.add(btn);

						param.setResult(nList);
						XposedBridge.log("已在 mark.via 设置列表添加模块按钮");
					}
				});

				// 拦截设置列表点击事件
				XposedHelpers.findAndHookMethod("e.h.g.g.a0$a", cl, "a", View.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Object clicked = XposedHelpers.getObjectField(param.thisObject, "d");
						if (clicked == null)
							return;
						int id = XposedHelpers.getIntField(clicked, "b");
						if (id == 1000) {
							XposedBridge.log("模块按钮被点击");
							showSettingsDialog(ctx);
						}
					}
				});

				/* ================== Activity生命周期管理 ================== */

				// Activity结果透传处理
				XposedHelpers.findAndHookMethod(Activity.class, "onActivityResult", int.class, int.class, Intent.class,
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								int req = (Integer) param.args[0];
								int res = (Integer) param.args[1];
								Intent data = (Intent) param.args[2];
								// 统一交给我们自己写的方法处理
								handleActivityResult(req, res, data, (Activity) param.thisObject);
							}
						});

				// Activity销毁时清理资源
				XposedHelpers.findAndHookMethod(Activity.class, "onDestroy", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity = (Activity) param.thisObject;
						screenOnActivities.remove(activity);
						statusBarHiddenActivities.remove(activity);

						// 清理重新隐藏任务
						Runnable rehideRunnable = statusBarRehideRunnables.get(activity);
						if (rehideRunnable != null) {
							View decorView = activity.getWindow().getDecorView();
							decorView.removeCallbacks(rehideRunnable);
							statusBarRehideRunnables.remove(activity);
						}
					}
				});

				/* ================== 语言设置应用 ================== */

				// 启动时应用上次语言设置
				final String last = getSavedLanguage(ctx);
				if (!"auto".equals(last)) {
					updateViaLocale(ctx, last);
				}
			}
		});
	}

	/* =========================================================
	 * Via GP 版主处理逻辑
	 * ======================================================= */
	private void handleViaGpApp(final XC_LoadPackage.LoadPackageParam param) {
		XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam attachParam) throws Throwable {
				final Context ctx = (Context) attachParam.args[0];
				final ClassLoader cl = ctx.getClassLoader();

				/* ================== 阻断检查 ================== */
				if (shouldBlockHook(ctx, param.packageName)) {
					showBlockedToast(ctx);
					return;
				}

				/* ================== Activity上下文获取 ================== */
				if (Context == null) {
					XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							if (Context == null) {
								Context = (Activity) param.thisObject;
								// 只有在未屏蔽启动提示时才显示消息
								if (!getPrefBoolean(ctx, KEY_BLOCK_STARTUP_MESSAGE, false)) {
									jiguroMessage(getLocalizedString(ctx, "hook_success_message"));
								}
								XposedBridge.log("得到 ActivityContext");
							}
						}
					});
				}

				/* ================== 功能模块初始化 ================== */

				// 护眼模式初始化
				eyeProtectionEnabled = getPrefBoolean(ctx, KEY_EYE_PROTECTION, false);
				eyeTemperature = getPrefInt(ctx, KEY_EYE_TEMPERATURE, 50);
				eyeTexture = getPrefInt(ctx, KEY_EYE_TEXTURE, 0);
				setEyeProtectionMode(ctx, cl, eyeProtectionEnabled);

				// 白名单功能初始化
				whitelistHookEnabled = getPrefBoolean(ctx, KEY_WHITELIST, true);
				setWhitelistHook(ctx, cl, whitelistHookEnabled);

				// b方法Hook初始化
				bHookEnabled = getPrefBoolean(ctx, KEY_B_HOOK, false);
				setBHook(ctx, cl, bHookEnabled);

				// 组件屏蔽Hook初始化
				setComponentBlockHook(ctx, cl, true);

				// 截屏防护初始化
				screenshotProtectionEnabled = getPrefBoolean(ctx, KEY_SCREENSHOT_PROTECTION, false);
				setScreenshotProtection(ctx, cl, screenshotProtectionEnabled);

				// 隐藏状态栏初始化
				hideStatusBarEnabled = getPrefBoolean(ctx, KEY_HIDE_STATUS_BAR, false);
				setHideStatusBar(ctx, cl, hideStatusBarEnabled);

				/* ================== 界面美化功能 ================== */

				// 主页背景设置
				homepageBgPath = getPrefString(ctx, KEY_HOMEPAGE_BG, "");
				homepageMaskAlpha = getPrefInt(ctx, KEY_HOMEPAGE_MASK_A, 120);
				homepageMaskColor = getPrefInt(ctx, KEY_HOMEPAGE_MASK_C, 0x80000000);
				if (!homepageBgPath.equals("")) {
					hookHomepageBgWithMask(ctx, cl, homepageBgPath, homepageMaskColor);
				}

				/* ================== 网络服务功能 ================== */

				// Google服务拦截初始化
				boolean blockGoogleServices = getPrefBoolean(ctx, KEY_BLOCK_GOOGLE_SERVICES, false);
				setGoogleServicesInterceptHook(ctx, cl, blockGoogleServices);

				// 自动更新检查
				autoUpdateEnabled = getPrefBoolean(ctx, KEY_AUTO_UPDATE, true);
				if (autoUpdateEnabled) {
					checkUpdateOnStart(ctx);
				}

				/* ================== 核心功能Hook ================== */

				// 清空u方法逻辑（白名单功能）
				if (whitelistHookEnabled) {
					XposedHelpers.findAndHookMethod("k.a.a0.i.k", cl, "u", "k.a.a0.i.a", new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.setResult(null);
							XposedBridge.log("已清空 mark.via.gp 的 u 方法逻辑");
						}
					});
				}

				// 屏幕常亮功能
				keepScreenOnEnabled = getPrefBoolean(ctx, KEY_KEEP_SCREEN_ON, false);
				setKeepScreenOn(ctx, cl, keepScreenOnEnabled);

				/* ================== 界面设置功能 ================== */

				// 设置列表添加"模块"按钮
				XposedHelpers.findAndHookMethod("k.a.m0.f7", cl, "f3", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						List<Object> orig = (List<Object>) param.getResult();
						if (orig == null)
							orig = new ArrayList<>();
						List<Object> nList = new ArrayList<>(orig);

						Class<?> yClass = XposedHelpers.findClass("e.h.g.g.y", cl);
						String txt = getLocalizedString(ctx, "module_settings");
						Object btn = XposedHelpers.newInstance(yClass, 1000, txt);
						moduleButtonRef = btn;
						nList.add(btn);

						param.setResult(nList);
						XposedBridge.log("已在 mark.via.gp 设置列表添加模块按钮");
					}
				});

				// 拦截设置列表点击事件
				XposedHelpers.findAndHookMethod("e.h.g.g.a0$a", cl, "a", View.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Object clicked = XposedHelpers.getObjectField(param.thisObject, "d");
						if (clicked == null)
							return;
						int id = XposedHelpers.getIntField(clicked, "b");
						if (id == 1000) {
							showSettingsDialog(ctx);
						}
					}
				});

				/* ================== Activity生命周期管理 ================== */

				// Activity结果透传处理
				XposedHelpers.findAndHookMethod(Activity.class, "onActivityResult", int.class, int.class, Intent.class,
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								int req = (Integer) param.args[0];
								int res = (Integer) param.args[1];
								Intent data = (Intent) param.args[2];
								// 统一交给我们自己写的方法处理
								handleActivityResult(req, res, data, (Activity) param.thisObject);
							}
						});

				// Activity销毁时清理资源
				XposedHelpers.findAndHookMethod(Activity.class, "onDestroy", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity = (Activity) param.thisObject;
						screenOnActivities.remove(activity);
						statusBarHiddenActivities.remove(activity);

						// 清理重新隐藏任务
						Runnable rehideRunnable = statusBarRehideRunnables.get(activity);
						if (rehideRunnable != null) {
							View decorView = activity.getWindow().getDecorView();
							decorView.removeCallbacks(rehideRunnable);
							statusBarRehideRunnables.remove(activity);
						}
					}
				});

				/* ================== 语言设置应用 ================== */

				// 启动时应用上次语言设置
				final String last = getSavedLanguage(ctx);
				if (!"auto".equals(last)) {
					updateViaLocale(ctx, last);
				}
			}
		});
	}

	/* =========================================================
	 * 显示设置对话框（主界面）
	 * ======================================================= */
	private void showSettingsDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				/* ================== 对话框根布局初始化 ================== */
				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));

				// 设置圆角背景
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);

				/* ================== 标题区域 ================== */
				// 主标题
				TextView title = new TextView(act);
				title.setText("BetterVia");
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
				title.setTextColor(0xFF6200EE); // 主题紫色
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				title.setPadding(0, 0, 0, dp(act, 16));
				root.addView(title);

				// 副标题
				TextView subtitle = new TextView(act);
				subtitle.setText(getLocalizedString(ctx, "module_settings_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);

				/* ================== 核心功能开关区域 ================== */

				// 1. 白名单开关
				addSwitch(root, act, getLocalizedString(ctx, "whitelist_switch"),
						getLocalizedString(ctx, "whitelist_hint"), KEY_WHITELIST, true, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_WHITELIST, true);
								setWhitelistHook(ctx, act.getClassLoader(), on);
							}
						});

				// 2. b方法开关
				addSwitch(root, act, getLocalizedString(ctx, "b_hook_switch"), getLocalizedString(ctx, "b_hook_hint"),
						KEY_B_HOOK, false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_B_HOOK, false);
								setBHook(ctx, act.getClassLoader(), on);
							}
						});

				// 3. Google服务拦截开关
				addSwitch(root, act, getLocalizedString(ctx, "block_google_switch"),
						getLocalizedString(ctx, "block_google_hint"), KEY_BLOCK_GOOGLE_SERVICES, false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_BLOCK_GOOGLE_SERVICES, false);
								setGoogleServicesInterceptHook(ctx, act.getClassLoader(), on);
							}
						});

				/* ================== 隐私和安全功能区域 ================== */

				// 4. 截屏防护开关
				addSwitch(root, act, getLocalizedString(ctx, "screenshot_protection_switch"),
						getLocalizedString(ctx, "screenshot_protection_hint"), KEY_SCREENSHOT_PROTECTION, false,
						new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_SCREENSHOT_PROTECTION, false);
								setScreenshotProtection(ctx, act.getClassLoader(), on);
							}
						});

				// 5. 屏蔽启动提示开关
				addSwitch(root, act, getLocalizedString(ctx, "block_startup_message_switch"),
						getLocalizedString(ctx, "block_startup_message_hint"), KEY_BLOCK_STARTUP_MESSAGE, false,
						new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_BLOCK_STARTUP_MESSAGE, false);
								blockStartupMessageEnabled = on;
								putPrefBoolean(ctx, KEY_BLOCK_STARTUP_MESSAGE, on);
							}
						});

				// 6. 屏幕常亮开关
				addSwitch(root, act, getLocalizedString(ctx, "keep_screen_on_switch"),
						getLocalizedString(ctx, "keep_screen_on_hint"), KEY_KEEP_SCREEN_ON, false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_KEEP_SCREEN_ON, false);
								setKeepScreenOn(ctx, act.getClassLoader(), on);
							}
						});

				// 7. 隐藏状态栏开关
				addSwitch(root, act, getLocalizedString(ctx, "hide_status_bar_switch"),
						getLocalizedString(ctx, "hide_status_bar_hint"), KEY_HIDE_STATUS_BAR, false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_HIDE_STATUS_BAR, false);
								setHideStatusBar(ctx, act.getClassLoader(), on);
							}
						});

				/* ================== 视觉和体验功能区域 ================== */

				// 8. 护眼模式开关
				addSwitch(root, act, getLocalizedString(ctx, "eye_protection_switch"),
						getLocalizedString(ctx, "eye_protection_hint"), KEY_EYE_PROTECTION, false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_EYE_PROTECTION, false);
								setEyeProtectionMode(ctx, act.getClassLoader(), on);
							}
						});

				// 9. 护眼调节配置
				addEyeProtectionConfig(root, act, ctx);

				/* ================== 内容管理功能区域 ================== */

				// 10. 组件屏蔽设置项
				addComponentBlockItem(root, act, ctx);

				// 11. 主页主题设置项
				addHomepageThemeItem(root, act, ctx);

				// 12. 脚本仓库设置项
				addScriptRepositoryItem(root, act, ctx);

				// 13. 广告走开设置项
				addAdBlockRulesItem(root, act, ctx);

				// 14. 搜索指令设置项
				addSearchCommandsItem(root, act, ctx);

				// 16. 浏览器标识设置项
				addUserAgentItem(root, act, ctx);

				// 15. Cookie管理设置项
				addCookieManagementItem(root, act, ctx);

				// 16. 资源界面美化设置项
				addImagePickerItem(root, act, ctx);

				/* ================== 分割线 ================== */
				View div = new View(act);
				LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						dp(act, 1));
				divLp.setMargins(0, dp(act, 12), 0, dp(act, 12));
				div.setLayoutParams(divLp);
				div.setBackgroundColor(0xFFDDDDDD);
				root.addView(div);

				/* ================== 语言和网络设置区域 ================== */

				// 17. 语言选择行
				LinearLayout langRow = new LinearLayout(act);
				langRow.setOrientation(LinearLayout.HORIZONTAL);
				langRow.setGravity(Gravity.CENTER_VERTICAL);

				TextView langTitle = new TextView(act);
				langTitle.setText(getLocalizedString(ctx, "language_title"));
				langTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				langTitle.setTextColor(Color.BLACK);
				langRow.addView(langTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

				// 当前语言选择器
				final String[] langValues = {"auto", "zh-CN", "zh-TW", "en"};
				final String[] langItems = new String[]{getLocalizedString(ctx, "language_auto"), "简体中文", "繁體中文",
						"English"};
				String savedLang = getSavedLanguage(ctx);
				int langIdx = 0;
				for (int i = 0; i < langValues.length; i++)
					if (langValues[i].equals(savedLang)) {
						langIdx = i;
						break;
					}
				final TextView langSelector = new TextView(act);
				langSelector.setText(langItems[langIdx]);
				langSelector.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				langSelector.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				langSelector.setBackground(getRoundBg(act, 0xFFE0E0E0, 12));
				langSelector.setTextColor(0xFF000000);
				langRow.addView(langSelector);
				root.addView(langRow);

				// 语言设置提示
				TextView langHintTv = new TextView(act);
				langHintTv.setText(getLocalizedString(ctx, "language_hint"));
				langHintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				langHintTv.setTextColor(0xFF666666);
				langHintTv.setPadding(0, dp(act, 4), 0, dp(act, 12));
				root.addView(langHintTv);

				/* ================== 网络源选择区域 ================== */

				// 18. 网络源选择行
				LinearLayout sourceRow = new LinearLayout(act);
				sourceRow.setOrientation(LinearLayout.HORIZONTAL);
				sourceRow.setGravity(Gravity.CENTER_VERTICAL);

				TextView sourceTitle = new TextView(act);
				sourceTitle.setText(getLocalizedString(ctx, "network_source_title"));
				sourceTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				sourceTitle.setTextColor(Color.BLACK);
				sourceRow.addView(sourceTitle,
						new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

				// 当前网络源选择器
				final String[] sourceValues = {NETWORK_SOURCE_GITEE, NETWORK_SOURCE_GITHUB};
				final String[] sourceItems = new String[]{"Gitee", "GitHub"};
				String savedSource = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE);
				int sourceIdx = savedSource.equals(NETWORK_SOURCE_GITEE) ? 0 : 1;
				final TextView sourceSelector = new TextView(act);
				sourceSelector.setText(sourceItems[sourceIdx]);
				sourceSelector.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				sourceSelector.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				sourceSelector.setBackground(getRoundBg(act, 0xFFE0E0E0, 12));
				sourceSelector.setTextColor(0xFF000000);
				sourceRow.addView(sourceSelector);
				root.addView(sourceRow);

				// 网络源提示
				TextView sourceHintTv = new TextView(act);
				sourceHintTv.setText(getLocalizedString(ctx, "network_source_hint"));
				sourceHintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				sourceHintTv.setTextColor(0xFF666666);
				sourceHintTv.setPadding(0, dp(act, 4), 0, dp(act, 12));
				root.addView(sourceHintTv);

				// 网络源点击事件
				sourceSelector.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showSourcePopup(ctx, sourceSelector, sourceValues, sourceItems, new SourceSelectedCallback() {
							@Override
							public void onSelected(int pos) {
								String selectedSource = sourceValues[pos];
								putPrefString(ctx, KEY_NETWORK_SOURCE, selectedSource);
								sourceSelector.setText(sourceItems[pos]);

								// 切换网络源后重新加载主题
								themesLoaded = false;
								loadedThemes.clear();

								Toast.makeText(ctx,
										getLocalizedString(ctx, "network_source_changed") + " " + sourceItems[pos],
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				});

				/* ================== 关于区域 ================== */
				LinearLayout aboutRow = new LinearLayout(act);
				aboutRow.setOrientation(LinearLayout.HORIZONTAL);
				aboutRow.setGravity(Gravity.CENTER_VERTICAL);

				TextView aboutTitle = new TextView(act);
				aboutTitle.setText(getLocalizedString(ctx, "about_title"));
				aboutTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				aboutTitle.setTextColor(Color.BLACK);
				aboutRow.addView(aboutTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

				// 查看按钮
				TextView aboutBtn = new TextView(act);
				aboutBtn.setText(getLocalizedString(ctx, "about_view"));
				aboutBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				aboutBtn.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				aboutBtn.setBackground(getRoundBg(act, 0xFFE0E0E0, 8));
				aboutBtn.setTextColor(0xFF000000);
				aboutBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showAboutDialog(ctx);
					}
				});
				aboutRow.addView(aboutBtn);

				root.addView(aboutRow);

				// 关于提示
				TextView aboutHintTv = new TextView(act);
				aboutHintTv.setText(getLocalizedString(ctx, "about_hint"));
				aboutHintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				aboutHintTv.setTextColor(0xFF666666);
				aboutHintTv.setPadding(0, dp(act, 4), 0, dp(act, 12));
				root.addView(aboutHintTv);

				/* ================== 更新设置区域 ================== */

				// 19. 自动检查更新开关
				addSwitch(root, act, getLocalizedString(ctx, "auto_update_switch"),
						getLocalizedString(ctx, "auto_update_hint"), KEY_AUTO_UPDATE, true, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_AUTO_UPDATE, true);
								autoUpdateEnabled = on;
								putPrefBoolean(ctx, KEY_AUTO_UPDATE, on);
							}
						});

				/* ================== 确定按钮区域 ================== */

				// 20. 确定按钮
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				GradientDrawable btnBg = new GradientDrawable();
				btnBg.setColor(0xFF6200EE);
				btnBg.setCornerRadius(dp(act, 12));
				ok.setBackground(btnBg);
				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				okLp.topMargin = dp(act, 16);
				root.addView(ok, okLp);

				scrollRoot.addView(root);

				/* ================== 对话框创建和显示 ================== */

				final AlertDialog[] dialogRef = new AlertDialog[1];
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setView(scrollRoot);
				dialogRef[0] = builder.create();

				// 设置对话框窗口属性
				Window win = dialogRef[0].getWindow();
				if (win != null) {
					win.setBackgroundDrawableResource(android.R.color.transparent);
					GradientDrawable round = new GradientDrawable();
					round.setColor(Color.WHITE);
					round.setCornerRadius(dp(act, 24));
					win.setBackgroundDrawable(round);
					win.setGravity(Gravity.CENTER);
					win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				}

				// 确定按钮点击事件
				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialogRef[0].dismiss();
					}
				});

				// 语言选择器点击事件
				langSelector.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showLangPopup(ctx, langSelector, langValues, langItems, new LangSelectedCallback() {
							@Override
							public void onSelected(int pos) {
								saveLanguageSetting(ctx, langValues[pos]);
								showLanguageChangeToast(ctx, pos);
								refreshModuleButtonText(ctx);
								langSelector.setText(langItems[pos]);
							}
						});
					}
				});

				dialogRef[0].show();
			}
		});
	}

	/* =========================================================
	 * 添加组件屏蔽设置项
	 * ======================================================= */
	private void addComponentBlockItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(getLocalizedString(ctx, "component_block_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		// 使用TextView代替Switch
		TextView configBtn = new TextView(ctx);
		configBtn.setText(getLocalizedString(ctx, "component_block_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showComponentBlockDialog(ctx);
			}
		});
		hor.addView(configBtn);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(getLocalizedString(ctx, "component_block_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	/* =========================================================
	 * 显示组件屏蔽配置对话框
	 * ======================================================= */
	private void showComponentBlockDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);

				// 标题
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "component_block_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				title.setTextColor(Color.BLACK);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 16);
				root.addView(title, titleLp);

				// 添加所有组件的勾选框
				final Map<String, CheckBox> checkboxes = new HashMap<>();
				String[] componentNames = getComponentNames(ctx);

				for (int i = 0; i < COMPONENT_KEYS.length; i++) {
					CheckBox cb = new CheckBox(act);
					cb.setText(componentNames[i]);
					cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					cb.setTextColor(Color.BLACK);
					cb.setChecked(getPrefBoolean(ctx, COMPONENT_KEYS[i], false));

					LinearLayout.LayoutParams cbLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					cbLp.bottomMargin = dp(act, 8);
					root.addView(cb, cbLp);

					checkboxes.put(COMPONENT_KEYS[i], cb);
				}

				// 确定按钮
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				GradientDrawable btnBg = new GradientDrawable();
				btnBg.setColor(0xFF6200EE);
				btnBg.setCornerRadius(dp(act, 12));
				ok.setBackground(btnBg);
				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				okLp.topMargin = dp(act, 16);
				root.addView(ok, okLp);

				scrollRoot.addView(root);

				// 创建对话框
				final AlertDialog dialog = new AlertDialog.Builder(act).setView(scrollRoot).create();

				// 设置对话框样式
				Window win = dialog.getWindow();
				if (win != null) {
					win.setBackgroundDrawableResource(android.R.color.transparent);
					GradientDrawable round = new GradientDrawable();
					round.setColor(Color.WHITE);
					round.setCornerRadius(dp(act, 24));
					win.setBackgroundDrawable(round);
					win.setGravity(Gravity.CENTER);
					win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				}

				// 确定按钮点击事件
				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 保存所有设置
						for (Map.Entry<String, CheckBox> entry : checkboxes.entrySet()) {
							putPrefBoolean(ctx, entry.getKey(), entry.getValue().isChecked());
						}
						Toast.makeText(ctx, getLocalizedString(ctx, "component_block_saved"), Toast.LENGTH_SHORT)
								.show();
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});
	}

	/* =========================================================
	 * 组件屏蔽 Hook
	 * ======================================================= */
	private void setComponentBlockHook(final Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (componentHook == null) {
				componentHook = XposedHelpers.findAndHookMethod("java.util.ArrayList", null, "add", Object.class,
						new XC_MethodHook() {
							@Override
							protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
								if (!isCalledFromK6A2())
									return;

								Object item = param.args[0];
								if (item == null)
									return;
								if (!"e.h.g.g.n".equals(item.getClass().getName()))
									return;

								int type = XposedHelpers.getIntField(item, "b"); // 类型常量
								String[] componentNames = getComponentNames(ctx); // 同下标对应
								int index = mapTypeToIndex(type); // 见下方辅助方法
								if (index < 0)
									return; // 未知类型，不屏蔽

								boolean block = getPrefBoolean(ctx, COMPONENT_KEYS[index], false);
								if (block) {
									XposedBridge.log("组件屏蔽：阻止类型 " + type + " → " + componentNames[index]);
									param.setResult(false);
								}
							}
						});
				XposedBridge.log("组件屏蔽 Hook 已启用（按类型常量）");
			}
		} else {
			if (componentHook != null) {
				componentHook.unhook();
				componentHook = null;
				XposedBridge.log("组件屏蔽 Hook 已停用");
			}
		}
	}

	/* 辅助：把 type 常量映射到 COMPONENT_KEYS 下标 --------- */
	private int mapTypeToIndex(int type) {
		switch (type) {
			case 12 :
				return 0; // 检查更新
			case 5 :
				return 1; // Telegram
			case 6 :
				return 2; // QQ
			case 13 :
				return 3; // 邮件
			case 14 :
				return 4; // 微信
			case 7 :
				return 5; // 捐助我们
			case 4 :
				return 6; // 协助翻译
			case 2 :
				return 7; // 协议
			case 3 :
				return 8; // 隐私
			case 1 :
				return 9; // 开源
			case 16 :
				return 10; // 备案
			default :
				return -1; // 未知
		}
	}

	/* =========================================================
	* 判断当前调用栈是否来自 k.a.m0.f6.X1()
	* ======================================================= */
	private boolean isCalledFromK6A2() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement el : stack) {
			if ("k.a.m0.f6".equals(el.getClassName()) && "X1".equals(el.getMethodName())) {
				return true;
			}
		}
		return false;
	}

	/* =========================================================
	* 获取组件名称数组
	* ======================================================= */
	private String[] getComponentNames(Context ctx) {
		return new String[]{getLocalizedString(ctx, "component_update"), // 检查更新
				getLocalizedString(ctx, "component_telegram"), // Telegram
				getLocalizedString(ctx, "component_qq"), // QQ
				getLocalizedString(ctx, "component_email"), // 邮件
				getLocalizedString(ctx, "component_wechat"), // 微信
				getLocalizedString(ctx, "component_donate"), // 捐助我们（修正）
				getLocalizedString(ctx, "component_assist"), // 协助翻译
				getLocalizedString(ctx, "component_agreement"), // 协议
				getLocalizedString(ctx, "component_privacy"), // 隐私
				getLocalizedString(ctx, "component_opensource"), // 开源
				getLocalizedString(ctx, "component_icp") // 备案
		};
	}

	private interface LangSelectedCallback {
		void onSelected(int pos);
	}

	private void showLangPopup(final Context ctx, View anchor, final String[] values, String[] items,
			final LangSelectedCallback callback) {
		final ListView list = new ListView(ctx);
		list.setDivider(null);

		// 创建自定义适配器，确保单行显示
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, items) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view.findViewById(android.R.id.text1);

				// 确保文字不换行
				textView.setSingleLine(true);
				textView.setEllipsize(TextUtils.TruncateAt.END);

				// 设置内边距
				textView.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));

				return view;
			}
		};
		list.setAdapter(adapter);

		// 计算合适的宽度：取 anchor 宽度和屏幕宽度的较大值，但不超过屏幕宽度的 80%
		DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
		int screenWidth = metrics.widthPixels;
		int maxWidth = (int) (screenWidth * 0.8);
		int anchorWidth = anchor.getWidth();

		// 设置 PopupWindow 宽度：至少为 anchor 宽度，但不超过最大宽度
		int popupWidth = Math.max(anchorWidth, dp(ctx, 200)); // 最小 200dp
		popupWidth = Math.min(popupWidth, maxWidth);

		final PopupWindow pop = new PopupWindow(list, popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
		pop.setOutsideTouchable(true);
		pop.setFocusable(true);
		pop.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				callback.onSelected(position);
				pop.dismiss();
			}
		});

		/* 圆角背景 */
		GradientDrawable bg = getRoundBg(ctx, Color.WHITE, 12);
		bg.setStroke(dp(ctx, 1), 0xFFE0E0E0); // 添加边框
		list.setBackground(bg);
		list.setPadding(0, dp(ctx, 4), 0, dp(ctx, 4)); // 减少内边距

		/* 从 anchor 下方弹出 */
		pop.showAsDropDown(anchor, 0, dp(ctx, 4));
	}

	private interface SourceSelectedCallback {
		void onSelected(int pos);
	}

	private void showSourcePopup(final Context ctx, View anchor, final String[] values, String[] items,
			final SourceSelectedCallback callback) {
		// 实现与showLangPopup类似的弹出菜单
		final ListView list = new ListView(ctx);
		list.setDivider(null);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, items) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view.findViewById(android.R.id.text1);
				textView.setSingleLine(true);
				textView.setEllipsize(TextUtils.TruncateAt.END);
				textView.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
				return view;
			}
		};
		list.setAdapter(adapter);

		int popupWidth = Math.max(anchor.getWidth(), dp(ctx, 200));
		final PopupWindow pop = new PopupWindow(list, popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
		pop.setOutsideTouchable(true);
		pop.setFocusable(true);
		pop.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				callback.onSelected(position);
				pop.dismiss();
			}
		});

		GradientDrawable bg = getRoundBg(ctx, Color.WHITE, 12);
		bg.setStroke(dp(ctx, 1), 0xFFE0E0E0);
		list.setBackground(bg);
		list.setPadding(0, dp(ctx, 4), 0, dp(ctx, 4));

		pop.showAsDropDown(anchor, 0, dp(ctx, 4));
	}

	/* =========================================================
	 * 开关控件构造器
	 * ======================================================= */
	private void addSwitch(LinearLayout parent, final Context ctx, String title, String hint, final String prefKey,
			boolean defVal, final Runnable onChange) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(title);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		Switch sw = new Switch(ctx);
		sw.setChecked(getPrefBoolean(ctx, prefKey, defVal));
		sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				putPrefBoolean(ctx, prefKey, isChecked);
				if (onChange != null)
					onChange.run();
			}
		});
		hor.addView(sw);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(hint);
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	/* =========================================================
	 * b方法 Hook 动态开关
	 * ======================================================= */
	private void setBHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (bHook == null) {
				bHook = XposedHelpers.findAndHookMethod("k.a.a0.n.c", cl, "b", int.class, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						param.setResult(true);
						XposedBridge.log("b方法 Hook 生效：b(int) 方法返回 true");
					}
				});
				XposedBridge.log("b方法 Hook 已启用");
			}
		} else {
			if (bHook != null) {
				bHook.unhook();
				bHook = null;
				XposedBridge.log("b方法 Hook 已停用");
			}
		}
		bHookEnabled = on;
		putPrefBoolean(ctx, KEY_B_HOOK, on);
	}

	/* =========================================================
	 * 白名单 Hook 动态开关
	 * ======================================================= */
	private void setWhitelistHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (whitelistHook == null) {
				whitelistHook = XposedHelpers.findAndHookMethod("k.a.a0.i.k", cl, "u", "k.a.a0.i.a",
						new XC_MethodHook() {
							@Override
							protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
								param.setResult(null);
								XposedBridge.log("白名单 Hook 生效：u 方法被清空");
							}
						});
				XposedBridge.log("白名单 Hook 已启用");
			}
		} else {
			if (whitelistHook != null) {
				whitelistHook.unhook();
				whitelistHook = null;
				XposedBridge.log("白名单 Hook 已停用");
			}
		}
		whitelistHookEnabled = on;
		putPrefBoolean(ctx, KEY_WHITELIST, on);
	}

	/* =========================================================
	 * 护眼模式动态开关
	 * ======================================================= */
	private void setEyeProtectionMode(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (activityHook == null) {
				activityHook = XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class,
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								if (!eyeProtectionEnabled)
									return;

								final Activity activity = (Activity) param.thisObject;
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										addEyeProtectionOverlay(activity, getPrefInt(activity, KEY_EYE_TEMPERATURE, 50),
												getPrefInt(activity, KEY_EYE_TEXTURE, 0));
									}
								});
							}
						});
				XposedBridge.log("护眼模式 Hook 已启用");
			}
		} else {
			if (activityHook != null) {
				activityHook.unhook();
				activityHook = null;
				XposedBridge.log("护眼模式 Hook 已停用");
			}
			removeAllEyeProtectionOverlays();
		}
		eyeProtectionEnabled = on;
		putPrefBoolean(ctx, KEY_EYE_PROTECTION, on);
	}

	/* =========================================================
	 * 添加护眼遮罩层（支持色温和纹理）
	 * ======================================================= */
	private void addEyeProtectionOverlay(Activity activity, final int temperature, final int texture) {
		try {
			ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();

			// 检查是否已存在遮罩
			View existingOverlay = overlayViews.get(activity);
			if (existingOverlay != null) {
				rootView.removeView(existingOverlay);
			}

			// 创建遮罩View
			View overlay = new View(activity) {
				@Override
				protected void onDraw(Canvas canvas) {
					super.onDraw(canvas);

					// 绘制色温遮罩
					int color = calculateTemperatureColor(temperature);
					canvas.drawColor(color);

					// 绘制纸质纹理
					if (texture > 0) {
						drawPaperTexture(canvas, texture);
					}
				}
			};

			overlay.setTag("eye_protection_overlay");

			// 设置布局参数
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);

			// 关键：设置不拦截触摸事件
			overlay.setClickable(false);
			overlay.setFocusable(false);
			overlay.setFocusableInTouchMode(false);

			rootView.addView(overlay, params);
			overlayViews.put(activity, overlay);

			XposedBridge.log("已为 " + activity.getClass().getSimpleName() + " 添加护眼遮罩");

		} catch (Exception e) {
			XposedBridge.log("添加护眼遮罩失败: " + e);
		}
	}

	/* =========================================================
	 * 计算色温颜色
	 * ======================================================= */
	private int calculateTemperatureColor(int temperature) {
		// 从无色到淡黄色，保持高对比度
		float ratio = temperature / 100.0f;

		// 使用较低的透明度，确保文字清晰
		int alpha = (int) (0x40 * ratio); // 从0%到25%透明度

		// 这种颜色既提供暖色调，又不会降低文字对比度
		int r = (int) (255 * ratio);
		int g = (int) (245 * ratio);
		int b = (int) (200 * ratio);

		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}

	/* =========================================================
	 * 绘制纸质纹理
	 * ======================================================= */
	private void drawPaperTexture(Canvas canvas, int textureLevel) {
		Paint paint = new Paint();
		paint.setColor(0x20FFFFFF); // 半透明白色噪点

		Random random = new Random(12345); // 固定种子保证一致性
		int density = textureLevel / 5; // 控制噪点密度

		for (int i = 0; i < density; i++) {
			float x = random.nextFloat() * canvas.getWidth();
			float y = random.nextFloat() * canvas.getHeight();
			float radius = random.nextFloat() * 2 + 1;
			canvas.drawCircle(x, y, radius, paint);
		}

		// 添加轻微磨砂效果
		if (textureLevel > 50) {
			paint.setColor(0x10FFFFFF);
			for (int i = 0; i < textureLevel / 10; i++) {
				float x = random.nextFloat() * canvas.getWidth();
				float y = random.nextFloat() * canvas.getHeight();
				float radius = random.nextFloat() * 3 + 2;
				canvas.drawCircle(x, y, radius, paint);
			}
		}
	}

	/* =========================================================
	 * 更新指定Activity的护眼遮罩
	 * ======================================================= */
	private void updateEyeProtectionOverlay(Activity activity, int temperature, int texture) {
		View overlay = overlayViews.get(activity);
		if (overlay != null) {
			overlay.invalidate(); // 触发重绘
		} else if (eyeProtectionEnabled) {
			// 如果还没有遮罩但护眼模式开启，则创建
			addEyeProtectionOverlay(activity, temperature, texture);
		}
	}

	/* =========================================================
	 * 更新所有Activity的护眼遮罩
	 * ======================================================= */
	private void updateAllEyeProtectionOverlays(int temperature, int texture) {
		for (Map.Entry<Activity, View> entry : overlayViews.entrySet()) {
			Activity activity = entry.getKey();
			if (!activity.isFinishing() && !activity.isDestroyed()) {
				updateEyeProtectionOverlay(activity, temperature, texture);
			}
		}
	}

	/* =========================================================
	 * 移除所有护眼遮罩
	 * ======================================================= */
	private void removeAllEyeProtectionOverlays() {
		for (Map.Entry<Activity, View> entry : overlayViews.entrySet()) {
			Activity activity = entry.getKey();
			View overlay = entry.getValue();
			if (!activity.isFinishing() && !activity.isDestroyed()) {
				ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
				rootView.removeView(overlay);
			}
		}
		overlayViews.clear();
		XposedBridge.log("已移除所有护眼遮罩");
	}

	/* =========================================================
	* 添加护眼调节配置项
	* ======================================================= */
	private void addEyeProtectionConfig(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(getLocalizedString(ctx, "eye_protection_config"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		// 配置按钮
		TextView configBtn = new TextView(ctx);
		configBtn.setText(getLocalizedString(ctx, "eye_protection_config_btn"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showEyeProtectionConfigDialog(ctx);
			}
		});
		hor.addView(configBtn);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(getLocalizedString(ctx, "eye_protection_config_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	/* =========================================================
	 * 显示护眼调节配置对话框
	 * ======================================================= */
	private void showEyeProtectionConfigDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				// 加载保存的设置
				final int savedTemperature = getPrefInt(ctx, KEY_EYE_TEMPERATURE, 50);
				final int savedTexture = getPrefInt(ctx, KEY_EYE_TEXTURE, 0);

				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);

				// 大标题
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "eye_protection_config_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(Color.BLACK);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);

				// 小标题
				TextView subtitle = new TextView(act);
				subtitle.setText(getLocalizedString(ctx, "eye_protection_config_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams subtitleLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				subtitleLp.bottomMargin = dp(act, 16);
				root.addView(subtitle, subtitleLp);

				// ========== 先创建滑块变量 ==========
				final SeekBar[] tempSeekBarRef = new SeekBar[1];
				final SeekBar[] textureSeekBarRef = new SeekBar[1];
				final View[] previewOverlayRef = new View[1];

				// ========== 预览区域 ==========
				LinearLayout previewContainer = new LinearLayout(act);
				previewContainer.setOrientation(LinearLayout.VERTICAL);
				previewContainer.setPadding(0, 0, 0, dp(act, 16));

				TextView previewTitle = new TextView(act);
				previewTitle.setText(getLocalizedString(ctx, "eye_protection_preview_title"));
				previewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				previewTitle.setTextColor(Color.BLACK);
				previewTitle.setTypeface(null, Typeface.BOLD);
				previewContainer.addView(previewTitle);

				// 预览内容区域
				FrameLayout previewContent = new FrameLayout(act);
				previewContent.setLayoutParams(
						new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 80)));
				previewContent.setBackgroundColor(Color.WHITE);
				previewContent.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));

				// 添加边框以便更好地区分
				GradientDrawable previewBg = new GradientDrawable();
				previewBg.setColor(Color.WHITE);
				previewBg.setStroke(dp(act, 1), 0xFFE0E0E0);
				previewBg.setCornerRadius(dp(act, 8));
				previewContent.setBackground(previewBg);

				// 示例文字
				TextView sampleText = new TextView(act);
				sampleText.setText(getLocalizedString(ctx, "eye_protection_sample_text"));
				sampleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				sampleText.setTextColor(Color.BLACK);
				FrameLayout.LayoutParams textLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				textLp.gravity = Gravity.CENTER;
				previewContent.addView(sampleText, textLp);

				// 预览遮罩层
				final View previewOverlay = new View(act) {
					@Override
					protected void onDraw(Canvas canvas) {
						super.onDraw(canvas);

						// 绘制色温遮罩
						if (tempSeekBarRef[0] != null) {
							int color = calculateTemperatureColor(tempSeekBarRef[0].getProgress());
							canvas.drawColor(color);
						}

						// 绘制纸质纹理
						if (textureSeekBarRef[0] != null && textureSeekBarRef[0].getProgress() > 0) {
							drawPaperTexturePreview(canvas, textureSeekBarRef[0].getProgress(), getWidth(),
									getHeight());
						}
					}
				};
				previewOverlay.setClickable(false);
				previewOverlay.setFocusable(false);
				FrameLayout.LayoutParams overlayLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
				previewContent.addView(previewOverlay, overlayLp);
				previewOverlayRef[0] = previewOverlay;

				previewContainer.addView(previewContent);
				root.addView(previewContainer);
				// ========== 预览区域结束 ==========

				// 色温调节
				LinearLayout tempContainer = new LinearLayout(act);
				tempContainer.setOrientation(LinearLayout.VERTICAL);
				tempContainer.setPadding(0, 0, 0, dp(act, 16));

				TextView tempTitle = new TextView(act);
				tempTitle.setText(getLocalizedString(ctx, "eye_protection_temperature"));
				tempTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				tempTitle.setTextColor(Color.BLACK);
				tempTitle.setTypeface(null, Typeface.BOLD);
				tempContainer.addView(tempTitle);

				// 色温滑块
				final SeekBar tempSeekBar = new SeekBar(act);
				tempSeekBar.setMax(100);
				tempSeekBar.setProgress(savedTemperature);
				tempSeekBarRef[0] = tempSeekBar; // 保存引用
				tempContainer.addView(tempSeekBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				// 色温标签行
				LinearLayout tempLabels = new LinearLayout(act);
				tempLabels.setOrientation(LinearLayout.HORIZONTAL);

				TextView coldLabel = new TextView(act);
				coldLabel.setText(getLocalizedString(ctx, "eye_protection_cold"));
				coldLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				coldLabel.setTextColor(0xFF666666);
				LinearLayout.LayoutParams coldLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1);
				tempLabels.addView(coldLabel, coldLp);

				TextView warmLabel = new TextView(act);
				warmLabel.setText(getLocalizedString(ctx, "eye_protection_warm"));
				warmLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				warmLabel.setTextColor(0xFF666666);
				warmLabel.setGravity(Gravity.END);
				LinearLayout.LayoutParams warmLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1);
				tempLabels.addView(warmLabel, warmLp);

				tempContainer.addView(tempLabels, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				// 添加色温调节容器到根布局
				root.addView(tempContainer);

				// 纸质纹理调节
				LinearLayout textureContainer = new LinearLayout(act);
				textureContainer.setOrientation(LinearLayout.VERTICAL);
				textureContainer.setPadding(0, 0, 0, dp(act, 16));

				TextView textureTitle = new TextView(act);
				textureTitle.setText(getLocalizedString(ctx, "eye_protection_texture"));
				textureTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				textureTitle.setTextColor(Color.BLACK);
				textureTitle.setTypeface(null, Typeface.BOLD);
				textureContainer.addView(textureTitle);

				// 纹理滑块
				final SeekBar textureSeekBar = new SeekBar(act);
				textureSeekBar.setMax(100);
				textureSeekBar.setProgress(savedTexture);
				textureSeekBarRef[0] = textureSeekBar; // 保存引用
				textureContainer.addView(textureSeekBar, new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

				// 纹理标签行
				LinearLayout textureLabels = new LinearLayout(act);
				textureLabels.setOrientation(LinearLayout.HORIZONTAL);

				TextView smoothLabel = new TextView(act);
				smoothLabel.setText(getLocalizedString(ctx, "eye_protection_smooth"));
				smoothLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				smoothLabel.setTextColor(0xFF666666);
				LinearLayout.LayoutParams smoothLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1);
				textureLabels.addView(smoothLabel, smoothLp);

				TextView roughLabel = new TextView(act);
				roughLabel.setText(getLocalizedString(ctx, "eye_protection_rough"));
				roughLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				roughLabel.setTextColor(0xFF666666);
				roughLabel.setGravity(Gravity.END);
				LinearLayout.LayoutParams roughLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1);
				textureLabels.addView(roughLabel, roughLp);

				textureContainer.addView(textureLabels, new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

				// 添加纸质纹理调节容器到根布局
				root.addView(textureContainer);

				// 预览提示
				TextView previewHint = new TextView(act);
				previewHint.setText(getLocalizedString(ctx, "eye_protection_preview_hint"));
				previewHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				previewHint.setTextColor(0xFF888888);
				previewHint.setGravity(Gravity.CENTER);
				previewHint.setTypeface(null, Typeface.ITALIC);
				LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				hintLp.topMargin = dp(act, 8);
				hintLp.bottomMargin = dp(act, 16);
				root.addView(previewHint, hintLp);

				// 确定按钮
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				GradientDrawable btnBg = new GradientDrawable();
				btnBg.setColor(0xFF6200EE);
				btnBg.setCornerRadius(dp(act, 12));
				ok.setBackground(btnBg);
				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				okLp.topMargin = dp(act, 8);
				root.addView(ok, okLp);

				scrollRoot.addView(root);

				// 创建对话框
				final AlertDialog dialog = new AlertDialog.Builder(act).setView(scrollRoot).create();

				// 设置对话框样式
				Window win = dialog.getWindow();
				if (win != null) {
					win.setBackgroundDrawableResource(android.R.color.transparent);
					GradientDrawable round = new GradientDrawable();
					round.setColor(Color.WHITE);
					round.setCornerRadius(dp(act, 24));
					win.setBackgroundDrawable(round);
					win.setGravity(Gravity.CENTER);
					win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				}

				// 实时预览功能
				SeekBar.OnSeekBarChangeListener previewListener = new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if (fromUser && previewOverlayRef[0] != null) {
							// 只更新预览区域，不更新屏幕遮罩
							previewOverlayRef[0].invalidate(); // 触发预览区域重绘
						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				};

				tempSeekBar.setOnSeekBarChangeListener(previewListener);
				textureSeekBar.setOnSeekBarChangeListener(previewListener);

				// 确定按钮点击事件
				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 保存设置
						int newTemperature = tempSeekBar.getProgress();
						int newTexture = textureSeekBar.getProgress();

						putPrefInt(ctx, KEY_EYE_TEMPERATURE, newTemperature);
						putPrefInt(ctx, KEY_EYE_TEXTURE, newTexture);

						// 更新所有Activity的遮罩（只在确定时应用，不在预览时应用）
						updateAllEyeProtectionOverlays(newTemperature, newTexture);

						Toast.makeText(ctx, getLocalizedString(ctx, "eye_protection_config_saved"), Toast.LENGTH_SHORT)
								.show();
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});
	}

	/* =========================================================
	* 绘制预览用的纸质纹理
	* ======================================================= */
	private void drawPaperTexturePreview(Canvas canvas, int textureLevel, int width, int height) {
		Paint paint = new Paint();
		paint.setColor(0x20FFFFFF); // 半透明白色噪点

		Random random = new Random(12345); // 固定种子保证一致性
		int density = textureLevel / 3; // 控制噪点密度，比全屏密度高

		// 根据预览区域大小调整噪点数量
		int pointCount = (width * height) / 1000 * density / 10;

		for (int i = 0; i < pointCount; i++) {
			float x = random.nextFloat() * width;
			float y = random.nextFloat() * height;
			float radius = random.nextFloat() * 1.5f + 0.5f; // 更小的半径
			canvas.drawCircle(x, y, radius, paint);
		}

		// 添加轻微磨砂效果
		if (textureLevel > 50) {
			paint.setColor(0x10FFFFFF);
			for (int i = 0; i < pointCount / 2; i++) {
				float x = random.nextFloat() * width;
				float y = random.nextFloat() * height;
				float radius = random.nextFloat() * 2 + 1;
				canvas.drawCircle(x, y, radius, paint);
			}
		}
	}

	/* =========================================================
	 * Google服务拦截 Hook 动态开关（精确版）
	 * ======================================================= */
	private void setGoogleServicesInterceptHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			XposedBridge.log("开始启用Google服务拦截（精确版）...");

			// 启用所有Google服务拦截
			try {
				setFirebaseAnalyticsHook(ctx, cl, true);
			} catch (Exception e) {
				XposedBridge.log("Firebase Analytics拦截启用失败: " + e);
			}

			try {
				setAppMeasurementHook(ctx, cl, true);
			} catch (Exception e) {
				XposedBridge.log("AppMeasurement拦截启用失败: " + e);
			}

			XposedBridge.log("Google服务拦截启用完成");
		} else {
			XposedBridge.log("开始停用Google服务拦截...");

			// 停用所有Google服务拦截
			try {
				setFirebaseAnalyticsHook(ctx, cl, false);
			} catch (Exception e) {
				XposedBridge.log("Firebase Analytics拦截停用失败: " + e);
			}

			try {
				setAppMeasurementHook(ctx, cl, false);
			} catch (Exception e) {
				XposedBridge.log("AppMeasurement拦截停用失败: " + e);
			}

			XposedBridge.log("Google服务拦截停用完成");
		}
		blockGoogleServicesEnabled = on;
		putPrefBoolean(ctx, KEY_BLOCK_GOOGLE_SERVICES, on);
	}

	/* =========================================================
	 * Firebase Analytics 精确拦截
	 * ======================================================= */
	private void setFirebaseAnalyticsHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (firebaseAnalyticsHook == null) {
				try {
					Class<?> firebaseAnalyticsClass = XposedHelpers
							.findClassIfExists("com.google.firebase.analytics.FirebaseAnalytics", cl);
					if (firebaseAnalyticsClass != null) {
						// 精确拦截混淆后的 a(String, Bundle) 方法
						firebaseAnalyticsHook = XposedHelpers.findAndHookMethod(firebaseAnalyticsClass, "a",
								String.class, Bundle.class, new XC_MethodHook() {
									@Override
									protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
										XposedBridge.log("拦截Firebase Analytics事件: " + param.args[0]);
										// 直接阻止事件上报，不执行任何操作
									}
								});
						XposedBridge.log("Firebase Analytics精确拦截已启用");
					}
				} catch (Exception e) {
					XposedBridge.log("Firebase Analytics精确拦截设置失败: " + e);
				}
			}
		} else {
			if (firebaseAnalyticsHook != null) {
				firebaseAnalyticsHook.unhook();
				firebaseAnalyticsHook = null;
				XposedBridge.log("Firebase Analytics拦截已停用");
			}
		}
	}

	/* =========================================================
	 * AppMeasurement 精确拦截
	 * ======================================================= */
	private void setAppMeasurementHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (googleAnalyticsHook == null) {
				try {
					Class<?> appMeasurementClass = XposedHelpers
							.findClassIfExists("com.google.android.gms.measurement.AppMeasurement", cl);
					if (appMeasurementClass != null) {
						// 精确拦截 logEventInternal 方法
						googleAnalyticsHook = XposedHelpers.findAndHookMethod(appMeasurementClass, "logEventInternal",
								String.class, String.class, Bundle.class, new XC_MethodHook() {
									@Override
									protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
										XposedBridge.log("拦截AppMeasurement事件: " + param.args[0] + ", " + param.args[1]);
										// 直接阻止事件上报
									}
								});

						XposedBridge.log("AppMeasurement精确拦截已启用");
					}
				} catch (Exception e) {
					XposedBridge.log("AppMeasurement精确拦截设置失败: " + e);
				}
			}
		} else {
			if (googleAnalyticsHook != null) {
				googleAnalyticsHook.unhook();
				googleAnalyticsHook = null;
				XposedBridge.log("AppMeasurement拦截已停用");
			}
		}
	}

	/* =========================================================
	* 添加搜索指令设置项
	* ======================================================= */
	private void addSearchCommandsItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(getLocalizedString(ctx, "search_commands_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		// 配置按钮
		TextView configBtn = new TextView(ctx);
		configBtn.setText(getLocalizedString(ctx, "search_commands_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSearchCommandsDialog(ctx);
			}
		});
		hor.addView(configBtn);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(getLocalizedString(ctx, "search_commands_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	/* =========================================================
	 * 显示搜索指令对话框（最终优化版）
	 * ======================================================= */
	private void showSearchCommandsDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				final Dialog dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);

				FrameLayout dialogContainer = new FrameLayout(act);
				GradientDrawable containerBg = new GradientDrawable();
				containerBg.setColor(Color.WHITE);
				containerBg.setCornerRadius(dp(act, 24));
				dialogContainer.setBackground(containerBg);

				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(0, 0, 0, 0); // 移除内边距
				scrollRoot.setClipToPadding(false); // 确保圆角不被裁剪

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));

				// 标题
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "search_commands_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);

				// 副标题
				TextView subtitle = new TextView(act);
				subtitle.setText(getLocalizedString(ctx, "search_commands_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);

				// 指令数据
				final String[][] commands = {{"javascript:via.cmd(257);", "command_bookmark"},
						{"javascript:via.cmd(514);", "command_search"}, {"javascript:via.cmd(515);", "command_unknown"},
						{"javascript:via.cmd(516);", "command_print"}, {"javascript:via.cmd(517);", "command_adblock"},
						{"v://log", "command_log"}, {"v://home", "command_home"}, {"v://skins", "command_skins"},
						{"v://about", "command_about"}, {"v://search", "command_search_page"},
						{"v://offline", "command_offline"}, {"v://history", "command_history"},
						{"v://scanner", "command_scanner"}, {"v://bookmarks", "command_bookmarks_page"},
						{"v://downloader", "command_downloader"}, {"v://readaloud", "command_readaloud"},
						{"v://translator/translate?text=", "command_translator"},
						{"history://", "command_history_page"}, {"folder://", "command_folder"}};

				// 创建指令列表容器
				LinearLayout commandsContainer = new LinearLayout(act);
				commandsContainer.setOrientation(LinearLayout.VERTICAL);

				for (int i = 0; i < commands.length; i++) {
					final String[] command = commands[i];

					// 每个指令的容器 - 使用水平布局
					LinearLayout commandContainer = new LinearLayout(act);
					commandContainer.setOrientation(LinearLayout.HORIZONTAL);
					commandContainer.setGravity(Gravity.CENTER_VERTICAL);
					commandContainer.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));

					// 设置圆角背景
					GradientDrawable commandBg = new GradientDrawable();
					commandBg.setColor(0xFFF8F9FA);
					commandBg.setStroke(dp(act, 1), 0xFFE9ECEF);
					commandBg.setCornerRadius(dp(act, 12));
					commandContainer.setBackground(commandBg);

					// 左侧内容区域（命令和描述）
					LinearLayout leftContent = new LinearLayout(act);
					leftContent.setOrientation(LinearLayout.VERTICAL);
					LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0,
							ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
					leftContent.setLayoutParams(leftParams);

					// 命令文本
					TextView commandText = new TextView(act);
					commandText.setText(command[0]);
					commandText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
					commandText.setTextColor(0xFF2C3E50);
					commandText.setTypeface(Typeface.MONOSPACE);
					commandText.setSingleLine(true);
					commandText.setEllipsize(TextUtils.TruncateAt.MIDDLE);
					commandText.setPadding(0, 0, dp(act, 8), 0);
					leftContent.addView(commandText);

					// 指令描述
					TextView descText = new TextView(act);
					descText.setText(getLocalizedString(ctx, command[1]));
					descText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
					descText.setTextColor(0xFF7F8C8D);
					descText.setPadding(0, dp(act, 4), 0, 0);
					leftContent.addView(descText);

					commandContainer.addView(leftContent);

					// 复制按钮
					Button copyBtn = new Button(act);
					copyBtn.setText(getLocalizedString(ctx, "command_copy"));
					copyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
					copyBtn.setTextColor(Color.WHITE);
					copyBtn.setPadding(dp(act, 10), dp(act, 4), dp(act, 10), dp(act, 4));
					copyBtn.setMinHeight(dp(act, 28));
					copyBtn.setMinWidth(dp(act, 52));

					// 按钮背景
					GradientDrawable btnBg = new GradientDrawable();
					btnBg.setColor(0xFF3498DB);
					btnBg.setCornerRadius(dp(act, 6));
					copyBtn.setBackground(btnBg);

					// 移除默认按钮背景
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						copyBtn.setStateListAnimator(null);
					}

					// 使用相对布局参数使按钮垂直居中
					LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					btnLp.gravity = Gravity.CENTER_VERTICAL;
					commandContainer.addView(copyBtn, btnLp);

					// 添加到指令容器
					LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					itemLp.bottomMargin = dp(act, 8);
					commandsContainer.addView(commandContainer, itemLp);

					final int index = i;
					copyBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							copyToClipboard(act, commands[index][0]);
							Toast.makeText(act, getLocalizedString(ctx, "command_copied"), Toast.LENGTH_SHORT).show();
						}
					});
				}

				root.addView(commandsContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				// 确定按钮
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				ok.setPadding(0, dp(act, 14), 0, dp(act, 14));
				ok.setBackground(getRoundBg(act, 0xFF6200EE, 12));

				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				okLp.topMargin = dp(act, 16);
				root.addView(ok, okLp);

				scrollRoot.addView(root);
				dialogContainer.addView(scrollRoot);
				dialog.setContentView(dialogContainer);

				// 设置对话框窗口属性 - 模仿护眼模式
				Window window = dialog.getWindow();
				if (window != null) {
					// 设置透明背景，确保圆角可见
					window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

					DisplayMetrics metrics = new DisplayMetrics();
					act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int width = (int) (metrics.widthPixels * 0.9); 
					int height = (int) (metrics.heightPixels * 0.8);

					WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
					layoutParams.copyFrom(window.getAttributes());
					layoutParams.width = width;
					layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度自适应
					layoutParams.gravity = Gravity.CENTER;

					window.setAttributes(layoutParams);

					// 确保圆角不被裁剪
					window.setClipToOutline(true);
				}

				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});
	}

	/* =========================================================
	* 添加主页主题设置项
	* ======================================================= */
	private void addHomepageThemeItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(getLocalizedString(ctx, "homepage_theme_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		// 配置按钮
		TextView configBtn = new TextView(ctx);
		configBtn.setText(getLocalizedString(ctx, "homepage_theme_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHomepageThemeDialog(ctx);
			}
		});
		hor.addView(configBtn);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(getLocalizedString(ctx, "homepage_theme_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	/* =========================================================
	 * 显示主页主题对话框
	 * ======================================================= */
	private void showHomepageThemeDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				final Dialog dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);

				// 根容器
				FrameLayout dialogContainer = new FrameLayout(act);
				GradientDrawable containerBg = new GradientDrawable();
				containerBg.setColor(Color.WHITE);
				containerBg.setCornerRadius(dp(act, 24));
				dialogContainer.setBackground(containerBg);

				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(0, 0, 0, 0);

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));

				// 标题
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "homepage_theme_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);

				// 副标题
				TextView subtitle = new TextView(act);
				subtitle.setText(getLocalizedString(ctx, "homepage_theme_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);

				// 主题列表容器
				final LinearLayout themesContainer = new LinearLayout(act);
				themesContainer.setOrientation(LinearLayout.VERTICAL);
				themesContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				// 空数据/错误提示
				final LinearLayout emptyStateContainer = new LinearLayout(act);
				emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
				emptyStateContainer.setGravity(Gravity.CENTER);
				emptyStateContainer.setPadding(0, dp(act, 48), 0, dp(act, 48));
				emptyStateContainer.setVisibility(View.GONE);

				// 错误图标
				final ImageView errorIcon = new ImageView(act);
				errorIcon.setImageResource(android.R.drawable.ic_menu_report_image);
				errorIcon.setColorFilter(0xFF888888);
				errorIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(act, 64), dp(act, 64));
				iconLp.gravity = Gravity.CENTER;
				iconLp.bottomMargin = dp(act, 16);
				emptyStateContainer.addView(errorIcon, iconLp);

				// 错误信息
				final TextView emptyStateText = new TextView(act);
				emptyStateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				emptyStateText.setTextColor(0xFF888888);
				emptyStateText.setGravity(Gravity.CENTER);
				emptyStateText.setPadding(dp(act, 32), 0, dp(act, 32), 0);
				emptyStateContainer.addView(emptyStateText);

				root.addView(themesContainer);
				root.addView(emptyStateContainer);

				// 按钮容器 - 水平排列确定和编辑按钮
				LinearLayout buttonContainer = new LinearLayout(act);
				buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
				buttonContainer.setGravity(Gravity.CENTER);
				buttonContainer.setPadding(0, dp(act, 16), 0, dp(act, 8));

				// 确定按钮 - 宽度缩小一半
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				ok.setPadding(0, dp(act, 14), 0, dp(act, 14));
				ok.setBackground(getRoundBg(act, 0xFF6200EE, 12));

				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1.0f);
				okLp.rightMargin = dp(act, 8);
				buttonContainer.addView(ok, okLp);

				// 编辑按钮 - 与确定按钮一致
				Button edit = new Button(act);
				edit.setText(getLocalizedString(ctx, "homepage_theme_edit"));
				edit.setTextColor(Color.WHITE);
				edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				edit.setTypeface(null, Typeface.BOLD);
				edit.setPadding(0, dp(act, 14), 0, dp(act, 14));
				edit.setBackground(getRoundBg(act, 0xFF6200EE, 12));

				LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1.0f);
				editLp.leftMargin = dp(act, 8);
				buttonContainer.addView(edit, editLp);

				root.addView(buttonContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				scrollRoot.addView(root);
				dialogContainer.addView(scrollRoot);
				dialog.setContentView(dialogContainer);

				// 设置对话框窗口属性
				Window window = dialog.getWindow();
				if (window != null) {
					window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					DisplayMetrics metrics = new DisplayMetrics();
					act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int width = (int) (metrics.widthPixels * 0.9);
					WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
					layoutParams.copyFrom(window.getAttributes());
					layoutParams.width = width;
					layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
					layoutParams.gravity = Gravity.CENTER;
					window.setAttributes(layoutParams);
					window.setClipToOutline(true);
				}

				// 确定按钮点击事件
				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				// 编辑按钮点击事件
				edit.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						showThemeEditorDialog(ctx);
					}
				});

				// 初始显示加载状态
				showLoadingState(act, ctx, themesContainer, emptyStateContainer, emptyStateText);

				// 在显示对话框时检查是否需要从网络加载主题
				if (!themesLoaded && !themesLoading) {
					loadThemesFromNetwork(ctx, new ThemesLoadCallback() {
						@Override
						public void onThemesLoaded(List<ThemeInfo> themes) {
							loadedThemes = themes;
							themesLoaded = true;
							themesLoading = false;

							// 刷新主题列表
							if (act != null && !act.isFinishing()) {
								act.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										refreshThemesList(act, ctx, themesContainer, emptyStateContainer,
												emptyStateText);
									}
								});
							}
						}

						@Override
						public void onLoadFailed(final String error) {
							themesLoading = false;
							themesLoaded = true; // 标记为已加载，但加载失败
							loadedThemes = new ArrayList<>(); // 清空主题列表

							if (act != null && !act.isFinishing()) {
								act.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										showErrorState(act, ctx, themesContainer, emptyStateContainer, emptyStateText,
												error);
									}
								});
							}
						}
					});
				} else {
					// 如果已经加载或正在加载，直接显示主题
					refreshThemesList(act, ctx, themesContainer, emptyStateContainer, emptyStateText);
				}

				dialog.show();
			}
		});
	}

	/* =========================================================
	 * 显示加载状态
	 * ======================================================= */
	private void showLoadingState(Activity act, Context ctx, LinearLayout themesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText) {
		themesContainer.removeAllViews();
		themesContainer.setVisibility(View.GONE);

		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(getLocalizedString(ctx, "themes_loading"));

		// 隐藏错误图标，显示加载动画
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.GONE);
			}
		}

		// 添加加载动画
		ProgressBar progressBar = new ProgressBar(act);
		progressBar.setIndeterminate(true);
		LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(dp(act, 48), dp(act, 48));
		progressLp.gravity = Gravity.CENTER;
		progressLp.bottomMargin = dp(act, 16);
		emptyStateContainer.addView(progressBar, 0, progressLp);
	}

	/* =========================================================
	 * 显示错误状态
	 * ======================================================= */
	private void showErrorState(Activity act, Context ctx, LinearLayout themesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, String error) {
		themesContainer.removeAllViews();
		themesContainer.setVisibility(View.GONE);

		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(
				getLocalizedString(ctx, "themes_load_failed") + "\n" + getLocalizedString(ctx, "check_network"));

		// 显示错误图标
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.VISIBLE);
			} else if (child instanceof ProgressBar) {
				emptyStateContainer.removeView(child); // 移除加载动画
			}
		}

		Toast.makeText(ctx, getLocalizedString(ctx, "themes_load_failed") + ": " + error, Toast.LENGTH_SHORT).show();
	}
	/* =========================================================
	 * 创建主题卡片
	 * ======================================================= */
	private LinearLayout createThemeCard(final Activity act, final Context ctx, final ThemeInfo theme) {
		// 主题卡片容器
		LinearLayout themeCard = new LinearLayout(act);
		themeCard.setOrientation(LinearLayout.VERTICAL);
		themeCard.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

		GradientDrawable cardBg = new GradientDrawable();
		cardBg.setColor(0xFFF8F9FA);
		cardBg.setStroke(dp(act, 1), 0xFFE9ECEF);
		cardBg.setCornerRadius(dp(act, 12));
		themeCard.setBackground(cardBg);

		// 主题预览图片容器
		FrameLayout imageContainer = new FrameLayout(act);
		imageContainer
				.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 150)));
		imageContainer.setBackground(getRoundBg(act, 0xFFE0E0E0, 8));

		// 加载中提示
		final ProgressBar loadingSpinner = new ProgressBar(act);
		loadingSpinner.setIndeterminate(true);
		FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		spinnerParams.gravity = Gravity.CENTER;
		imageContainer.addView(loadingSpinner, spinnerParams);

		// 主题预览图片
		final ImageView previewImage = new ImageView(act);
		previewImage.setLayoutParams(
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
		previewImage.setVisibility(View.GONE);
		imageContainer.addView(previewImage);

		themeCard.addView(imageContainer);

		// 异步加载图片
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL url = new URL(theme.previewUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setDoInput(true);
					connection.setConnectTimeout(10000);
					connection.setReadTimeout(10000);
					connection.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

					if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
						InputStream input = connection.getInputStream();
						final Bitmap bitmap = BitmapFactory.decodeStream(input);
						input.close();

						if (bitmap != null) {
							act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									previewImage.setImageBitmap(bitmap);
									previewImage.setVisibility(View.VISIBLE);
									loadingSpinner.setVisibility(View.GONE);
								}
							});
						}
					}
					connection.disconnect();
				} catch (Exception e) {
					act.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// 创建合适大小的错误图标
							int iconSize = dp(act, 48); // 使用48dp的图标
							Bitmap errorBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
							Canvas canvas = new Canvas(errorBitmap);

							// 绘制背景
							Paint backgroundPaint = new Paint();
							backgroundPaint.setColor(0xFFE0E0E0);
							canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2, backgroundPaint);

							// 绘制图标
							Drawable errorIcon = act.getResources().getDrawable(android.R.drawable.ic_menu_gallery);
							errorIcon.setBounds(iconSize / 4, iconSize / 4, iconSize * 3 / 4, iconSize * 3 / 4);
							errorIcon.draw(canvas);

							previewImage.setImageBitmap(errorBitmap);
							previewImage.setScaleType(ImageView.ScaleType.CENTER); // 居中显示，不拉伸
							previewImage.setColorFilter(0xFF888888);
							previewImage.setVisibility(View.VISIBLE);
							loadingSpinner.setVisibility(View.GONE);
						}
					});
				}
			}
		}).start();

		// 主题信息
		LinearLayout infoLayout = new LinearLayout(act);
		infoLayout.setOrientation(LinearLayout.VERTICAL);
		infoLayout.setPadding(0, dp(act, 8), 0, 0);

		// 主题名称
		TextView themeName = new TextView(act);
		themeName.setText(theme.getName(ctx));
		themeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		themeName.setTextColor(Color.BLACK);
		themeName.setTypeface(null, Typeface.BOLD);
		infoLayout.addView(themeName);

		// 作者信息
		TextView themeAuthor = new TextView(act);
		themeAuthor.setText(getLocalizedString(ctx, "homepage_theme_by") + " " + theme.getAuthor(ctx));
		themeAuthor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		themeAuthor.setTextColor(0xFF666666);
		infoLayout.addView(themeAuthor);

		themeCard.addView(infoLayout);

		// 点击事件
		themeCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showThemeApplyDialog(ctx, theme);
			}
		});

		return themeCard;
	}

	private interface ThemesLoadCallback {
		void onThemesLoaded(List<ThemeInfo> themes);
		void onLoadFailed(String error);
	}

	private void loadThemesFromNetwork(final Context ctx, final ThemesLoadCallback callback) {
		themesLoading = true;

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String networkSource = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE);
					String jsonUrl = networkSource.equals(NETWORK_SOURCE_GITEE)
							? GITEE_THEMES_JSON_URL
							: GITHUB_THEMES_JSON_URL;

					URL url = new URL(jsonUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(15000);
					connection.setReadTimeout(15000);
					connection.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

					if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
						InputStream inputStream = connection.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						reader.close();

						JSONObject json = new JSONObject(response.toString());
						JSONArray themesArray = json.getJSONArray("themes");

						List<ThemeInfo> themes = new ArrayList<>();
						for (int i = 0; i < themesArray.length(); i++) {
							JSONObject themeJson = themesArray.getJSONObject(i);
							ThemeInfo theme = ThemeInfo.fromJSON(themeJson);
							themes.add(theme);
						}

						callback.onThemesLoaded(themes);
					} else {
						callback.onLoadFailed("HTTP " + connection.getResponseCode());
					}

					connection.disconnect();
				} catch (Exception e) {
					callback.onLoadFailed(e.getMessage());
				}
			}
		}).start();
	}

	/* =========================================================
	* 刷新主题列表
	* ======================================================= */
	private void refreshThemesList(final Activity act, final Context ctx, LinearLayout themesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText) {
		themesContainer.removeAllViews();

		if (themesLoading) {
			showLoadingState(act, ctx, themesContainer, emptyStateContainer, emptyStateText);
			return;
		}

		if (loadedThemes == null || loadedThemes.isEmpty()) {
			showErrorState(act, ctx, themesContainer, emptyStateContainer, emptyStateText, "No themes available");
			return;
		}

		themesContainer.setVisibility(View.VISIBLE);
		emptyStateContainer.setVisibility(View.GONE);

		for (final ThemeInfo theme : loadedThemes) {
			LinearLayout themeCard = createThemeCard(act, ctx, theme);
			LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			cardLp.bottomMargin = dp(ctx, 12);
			themesContainer.addView(themeCard, cardLp);
		}
	}

	/* =========================================================
	* 显示主题编辑器对话框
	* ======================================================= */
	private void showThemeEditorDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				final Dialog dialog = new Dialog(act, android.R.style.Theme_NoTitleBar_Fullscreen);
				dialog.setCancelable(true);
				// 根布局 - 亮色背景
				LinearLayout rootLayout = new LinearLayout(act);
				rootLayout.setOrientation(LinearLayout.VERTICAL);
				rootLayout.setBackgroundColor(Color.WHITE);
				// 标题栏 - 亮色设计
				RelativeLayout titleBar = new RelativeLayout(act);
				titleBar.setBackgroundColor(0xFFF5F5F5);
				titleBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
				titleBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				// 返回按钮
				ImageButton backButton = new ImageButton(act);
				backButton.setImageResource(android.R.drawable.ic_menu_revert);
				backButton.setBackgroundResource(android.R.color.transparent);
				backButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				backButton.setPadding(dp(act, 8), dp(act, 8), dp(act, 8), dp(act, 8));
				backButton.setColorFilter(0xFF000000); // 黑色图标
				RelativeLayout.LayoutParams backButtonLp = new RelativeLayout.LayoutParams(dp(act, 48), dp(act, 48));
				backButtonLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				backButtonLp.addRule(RelativeLayout.CENTER_VERTICAL);
				titleBar.addView(backButton, backButtonLp);
				// 标题（已适配多语言，保留）
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "homepage_theme_editor_title"));
				title.setTextColor(Color.BLACK);
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				title.setTypeface(null, Typeface.BOLD);
				RelativeLayout.LayoutParams titleLp = new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.addRule(RelativeLayout.CENTER_IN_PARENT);
				titleBar.addView(title, titleLp);
				rootLayout.addView(titleBar);
				// 内容区域
				LinearLayout contentLayout = new LinearLayout(act);
				contentLayout.setOrientation(LinearLayout.VERTICAL);
				contentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				contentLayout.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				// 文件选择标签
				TextView fileLabel = new TextView(act);
				fileLabel.setText(getLocalizedString(ctx, "theme_editor_select_file"));
				fileLabel.setTextColor(0xFF333333);
				fileLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				fileLabel.setPadding(0, 0, 0, dp(act, 8));
				contentLayout.addView(fileLabel);
				// 文件选择按钮组（文件名是实际文件，无需多语言）
				LinearLayout fileButtonGroup = new LinearLayout(act);
				fileButtonGroup.setOrientation(LinearLayout.HORIZONTAL);
				fileButtonGroup.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				final Button htmlButton = new Button(act);
				htmlButton.setText("homepage2.html"); // 文件名固定，无需适配
				htmlButton.setTextColor(Color.WHITE);
				htmlButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				htmlButton.setBackground(getRoundBg(act, 0xFF6200EE, 6));
				htmlButton.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				LinearLayout.LayoutParams htmlLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1.0f);
				htmlLp.rightMargin = dp(act, 8);
				fileButtonGroup.addView(htmlButton, htmlLp);
				final Button cssButton = new Button(act);
				cssButton.setText("homepage.css"); // 文件名固定，无需适配
				cssButton.setTextColor(0xFF666666);
				cssButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				cssButton.setBackground(getRoundBg(act, 0xFFE0E0E0, 6));
				cssButton.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				LinearLayout.LayoutParams cssLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1.0f);
				fileButtonGroup.addView(cssButton, cssLp);
				contentLayout.addView(fileButtonGroup);
				// 代码编辑区域标签（多语言适配）
				TextView editorLabel = new TextView(act);
				editorLabel.setText(getLocalizedString(ctx, "theme_editor_edit_content"));
				editorLabel.setTextColor(0xFF333333);
				editorLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				editorLabel.setPadding(0, dp(act, 16), 0, dp(act, 8));
				contentLayout.addView(editorLabel);
				// 编辑框容器 - 使用ScrollView确保可滚动
				final ScrollView editorScroll = new ScrollView(act);
				editorScroll
						.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
				final EditText codeEditor = new EditText(act);
				codeEditor.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				codeEditor.setTypeface(Typeface.MONOSPACE);
				codeEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				codeEditor.setTextColor(Color.BLACK);
				codeEditor.setBackground(getRoundBg(act, 0xFFF5F5F5, 8));
				codeEditor.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));
				codeEditor.setSingleLine(false);
				codeEditor.setGravity(Gravity.TOP);
				codeEditor.setMinLines(20);
				editorScroll.addView(codeEditor);
				contentLayout.addView(editorScroll);
				// 底部按钮栏（复用已有多语言key）
				LinearLayout buttonBar = new LinearLayout(act);
				buttonBar.setOrientation(LinearLayout.HORIZONTAL);
				buttonBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				buttonBar.setPadding(0, dp(act, 16), 0, 0);
				Button cancelButton = new Button(act);
				cancelButton.setText(getLocalizedString(ctx, "dialog_cancel")); // 复用取消按钮多语言
				cancelButton.setTextColor(0xFF666666);
				cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				cancelButton.setBackground(getRoundBg(act, 0xFFE0E0E0, 8));
				cancelButton.setPadding(dp(act, 24), dp(act, 12), dp(act, 24), dp(act, 12));
				LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				cancelLp.rightMargin = dp(act, 8);
				buttonBar.addView(cancelButton, cancelLp);
				Button saveButton = new Button(act);
				saveButton.setText(getLocalizedString(ctx, "dialog_ok")); // 复用确定按钮多语言
				saveButton.setTextColor(Color.WHITE);
				saveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				saveButton.setBackground(getRoundBg(act, 0xFF6200EE, 8));
				saveButton.setPadding(dp(act, 24), dp(act, 12), dp(act, 24), dp(act, 12));
				LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1.0f);
				buttonBar.addView(saveButton, saveLp);
				contentLayout.addView(buttonBar);
				rootLayout.addView(contentLayout);
				dialog.setContentView(rootLayout);
				// 当前选中的文件
				final String[] currentFile = {"homepage2.html"};
				// 加载初始文件内容
				loadFileContent(act, "homepage2.html", codeEditor, editorScroll, true);
				// 文件切换事件
				// HTML 按钮点击事件
				htmlButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 保存当前文件状态
						saveCurrentEditorState(currentFile[0], codeEditor, editorScroll);

						currentFile[0] = "homepage2.html";
						htmlButton.setTextColor(Color.WHITE);
						htmlButton.setBackground(getRoundBg(act, 0xFF6200EE, 6));
						cssButton.setTextColor(0xFF666666);
						cssButton.setBackground(getRoundBg(act, 0xFFE0E0E0, 6));

						// 从缓存加载，而不是从文件
						loadFileContent(act, "homepage2.html", codeEditor, editorScroll, true);
					}
				});

				// CSS 按钮点击事件  
				cssButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 保存当前文件状态
						saveCurrentEditorState(currentFile[0], codeEditor, editorScroll);

						currentFile[0] = "homepage.css";
						cssButton.setTextColor(Color.WHITE);
						cssButton.setBackground(getRoundBg(act, 0xFF6200EE, 6));
						htmlButton.setTextColor(0xFF666666);
						htmlButton.setBackground(getRoundBg(act, 0xFFE0E0E0, 6));

						// 从缓存加载
						loadFileContent(act, "homepage.css", codeEditor, editorScroll, true);
					}
				});

				/* ================== 添加编辑器滚动监听 ================== */
				// 在创建编辑器后添加滚动监听，实时更新滚动位置
				editorScroll.getViewTreeObserver()
						.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
							@Override
							public void onScrollChanged() {
								// 实时更新当前文件的滚动位置到缓存
								if (currentFile[0] != null && editorStateCache.containsKey(currentFile[0])) {
									EditorState oldState = editorStateCache.get(currentFile[0]);
									editorStateCache.put(currentFile[0],
											new EditorState(oldState.content, editorScroll.getScrollY()));
								}
							}
						});

				/* ================== 对话框关闭时清理缓存 ================== */
				// 在对话框的 dismiss 监听中清理缓存
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						// 清理超过10分钟的缓存
						long currentTime = System.currentTimeMillis();
						Iterator<Map.Entry<String, EditorState>> it = editorStateCache.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<String, EditorState> entry = it.next();
							if (currentTime - entry.getValue().timestamp > 10 * 60 * 1000) { // 10分钟
								it.remove();
							}
						}
					}
				});
				// 返回按钮事件
				backButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				// 取消按钮事件
				cancelButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				// 保存事件
				saveButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 保存到文件
						saveFileContent(act, currentFile[0], codeEditor.getText().toString());

						// 更新缓存中的内容为保存后的状态
						if (editorScroll != null) {
							int scrollY = editorScroll.getScrollY();
							editorStateCache.put(currentFile[0],
									new EditorState(codeEditor.getText().toString(), scrollY));
						}

						Toast.makeText(act, getLocalizedString(ctx, "theme_editor_save_success"), Toast.LENGTH_SHORT)
								.show();
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}

	/* =========================================================
	 * 显示主题应用确认对话框
	 * ======================================================= */
	private void showThemeApplyDialog(final Context ctx, final ThemeInfo theme) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle(getLocalizedString(ctx, "homepage_theme_apply_title"));
				builder.setMessage(
						getLocalizedString(ctx, "homepage_theme_apply_message") + " \"" + theme.getName(ctx) + "\"?");

				builder.setPositiveButton(getLocalizedString(ctx, "homepage_theme_apply"),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								applyHomepageTheme(ctx, theme);
							}
						});

				builder.setNegativeButton(getLocalizedString(ctx, "dialog_cancel"), null);

				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
	}

	/* =========================================================
	* 应用主页主题
	* ======================================================= */
	private void applyHomepageTheme(final Context ctx, final ThemeInfo theme) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 根据当前包名确定文件路径和URL
					String packageName = ctx.getPackageName();
					String filesDir = "/data/user/0/" + packageName + "/files/";

					// 获取对应包名的URL
					String htmlUrl = theme.htmlUrls.get(packageName);
					String cssUrl = theme.cssUrls.get(packageName);

					if (htmlUrl == null || cssUrl == null) {
						// 如果没有特定包名的URL，使用第一个可用的URL
						htmlUrl = theme.htmlUrls.values().iterator().next();
						cssUrl = theme.cssUrls.values().iterator().next();
					}

					// 下载并保存HTML文件
					final boolean htmlSuccess = downloadAndSaveFile(htmlUrl, filesDir + "homepage2.html");
					// 下载并保存CSS文件
					final boolean cssSuccess = downloadAndSaveFile(cssUrl, filesDir + "homepage.css");

					// 在主线程显示结果
					((Activity) Context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (htmlSuccess && cssSuccess) {
								// 保存当前主题ID
								putPrefString(ctx, KEY_CURRENT_THEME, theme.id);

								Toast.makeText(ctx, getLocalizedString(ctx, "homepage_theme_apply_success"),
										Toast.LENGTH_LONG).show();

								// 重启Via
								restartVia(ctx);
							} else {
								Toast.makeText(ctx, getLocalizedString(ctx, "homepage_theme_apply_failed"),
										Toast.LENGTH_LONG).show();
							}
						}
					});

				} catch (Exception e) {
					XposedBridge.log("应用主题失败: " + e);
					((Activity) Context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(ctx, getLocalizedString(ctx, "homepage_theme_apply_error"),
									Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}

	/* =========================================================
	 * 下载并保存文件
	 * ======================================================= */
	private boolean downloadAndSaveFile(String urlString, String filePath) {
		HttpURLConnection connection = null;
		FileOutputStream outputStream = null;

		try {
			URL url = new URL(urlString);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream inputStream = connection.getInputStream();
				File file = new File(filePath);
				outputStream = new FileOutputStream(file);

				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				outputStream.flush();
				return true;
			}
		} catch (Exception e) {
			XposedBridge.log("下载文件失败: " + e);
		} finally {
			try {
				if (outputStream != null)
					outputStream.close();
				if (connection != null)
					connection.disconnect();
			} catch (Exception e) {
				XposedBridge.log("关闭流失败: " + e);
			}
		}
		return false;
	}

	/* =========================================================
	 * 重启Via
	 * ======================================================= */
	private void restartVia(Context ctx) {
		try {
			// 获取启动Intent
			Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
			if (intent != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

				// 结束当前进程
				android.os.Process.killProcess(android.os.Process.myPid());

				// 启动新进程
				ctx.startActivity(intent);
			}
		} catch (Exception e) {
			XposedBridge.log("重启Via失败: " + e);
		}
	}

	/* =========================================================
	* 截屏防护 Hook 动态开关
	* ======================================================= */
	private void setScreenshotProtection(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (screenshotProtectionHook == null) {
				// Hook Activity的onCreate方法，在Activity创建时设置FLAG_SECURE
				screenshotProtectionHook = XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class,
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								Activity activity = (Activity) param.thisObject;
								if (screenshotProtectionEnabled) {
									activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
											WindowManager.LayoutParams.FLAG_SECURE);
									XposedBridge.log("已为 " + activity.getClass().getSimpleName() + " 启用截屏防护");
								}
							}
						});
				XposedBridge.log("截屏防护 Hook 已启用");
			}
		} else {
			if (screenshotProtectionHook != null) {
				screenshotProtectionHook.unhook();
				screenshotProtectionHook = null;
				XposedBridge.log("截屏防护 Hook 已停用");

				// 移除所有Activity的FLAG_SECURE标志
				removeScreenshotProtection();
			}
		}
		screenshotProtectionEnabled = on;
		putPrefBoolean(ctx, KEY_SCREENSHOT_PROTECTION, on);
	}

	/* =========================================================
	 * 移除所有Activity的截屏防护
	 * ======================================================= */
	private void removeScreenshotProtection() {
		for (final Activity activity : overlayViews.keySet()) {
			if (!activity.isFinishing() && !activity.isDestroyed()) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
					}
				});
			}
		}
		XposedBridge.log("已移除所有Activity的截屏防护");
	}

	/* =========================================================
	* 屏幕常亮 Hook 动态开关
	* ======================================================= */
	private void setKeepScreenOn(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (keepScreenOnHook == null) {
				// Hook Activity的onCreate方法，在Activity创建时设置FLAG_KEEP_SCREEN_ON
				keepScreenOnHook = XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class,
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								final Activity activity = (Activity) param.thisObject;
								if (keepScreenOnEnabled) {
									activity.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											activity.getWindow()
													.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
											screenOnActivities.put(activity, true);
											XposedBridge.log("已为 " + activity.getClass().getSimpleName() + " 启用屏幕常亮");
										}
									});
								}
							}
						});
				XposedBridge.log("屏幕常亮 Hook 已启用");
			}
		} else {
			if (keepScreenOnHook != null) {
				keepScreenOnHook.unhook();
				keepScreenOnHook = null;
				XposedBridge.log("屏幕常亮 Hook 已停用");

				// 移除所有Activity的FLAG_KEEP_SCREEN_ON标志
				removeKeepScreenOn();
			}
		}
		keepScreenOnEnabled = on;
		putPrefBoolean(ctx, KEY_KEEP_SCREEN_ON, on);
	}

	/* =========================================================
	 * 移除所有Activity的屏幕常亮设置
	 * ======================================================= */
	private void removeKeepScreenOn() {
		for (final Activity activity : screenOnActivities.keySet()) {
			if (!activity.isFinishing() && !activity.isDestroyed()) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					}
				});
			}
		}
		screenOnActivities.clear();
		XposedBridge.log("已移除所有Activity的屏幕常亮设置");
	}

	/* =========================================================
	 * 修改脚本仓库设置项点击事件
	 * ======================================================= */
	private void addScriptRepositoryItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(getLocalizedString(ctx, "script_repository_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		// 配置按钮
		TextView configBtn = new TextView(ctx);
		configBtn.setText(getLocalizedString(ctx, "script_repository_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showScriptRepositoryDialog(ctx);
			}
		});
		hor.addView(configBtn);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(getLocalizedString(ctx, "script_repository_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	/* =========================================================
	 * 脚本信息类
	 * ======================================================= */
	private static class ScriptInfo {
		String id;
		Map<String, String> nameMap; // 语言代码 -> 名称
		Map<String, String> descriptionMap; // 语言代码 -> 简介
		Map<String, String> detailMap; // 语言代码 -> 详细描述
		Map<String, String> downloadUrls; // 渠道名称 -> 下载链接
		String category;

		ScriptInfo(String id, Map<String, String> nameMap, Map<String, String> descriptionMap,
				Map<String, String> detailMap, Map<String, String> downloadUrls, String category) {
			this.id = id;
			this.nameMap = nameMap;
			this.descriptionMap = descriptionMap;
			this.detailMap = detailMap;
			this.downloadUrls = downloadUrls;
			this.category = category;
		}

		String getName(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return nameMap.getOrDefault(langCode, nameMap.get("zh-CN"));
		}

		String getDescription(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return descriptionMap.getOrDefault(langCode, descriptionMap.get("zh-CN"));
		}

		String getDetail(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return detailMap.getOrDefault(langCode, detailMap.get("zh-CN"));
		}

		private String getLanguageCode(Context ctx) {
			String saved = getSavedLanguageStatic(ctx);
			if ("auto".equals(saved)) {
				Locale locale;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					locale = ctx.getResources().getConfiguration().getLocales().get(0);
				} else {
					locale = ctx.getResources().getConfiguration().locale;
				}

				if (Locale.SIMPLIFIED_CHINESE.equals(locale)) {
					return "zh-CN";
				} else if (Locale.TRADITIONAL_CHINESE.equals(locale)) {
					return "zh-TW";
				} else if (Locale.ENGLISH.equals(locale)) {
					return "en";
				}
				return "zh-CN";
			}
			return saved;
		}

		static ScriptInfo fromJSON(JSONObject json) throws JSONException {
			String id = json.getString("id");

			// 解析多语言名称
			Map<String, String> nameMap = new HashMap<>();
			JSONObject names = json.getJSONObject("names");
			Iterator<String> nameKeys = names.keys();
			while (nameKeys.hasNext()) {
				String lang = nameKeys.next();
				nameMap.put(lang, names.getString(lang));
			}

			// 解析多语言简介
			Map<String, String> descriptionMap = new HashMap<>();
			JSONObject descriptions = json.getJSONObject("descriptions");
			Iterator<String> descKeys = descriptions.keys();
			while (descKeys.hasNext()) {
				String lang = descKeys.next();
				descriptionMap.put(lang, descriptions.getString(lang));
			}

			// 解析多语言详细描述
			Map<String, String> detailMap = new HashMap<>();
			JSONObject details = json.getJSONObject("details");
			Iterator<String> detailKeys = details.keys();
			while (detailKeys.hasNext()) {
				String lang = detailKeys.next();
				detailMap.put(lang, details.getString(lang));
			}

			// 解析下载链接
			Map<String, String> downloadUrls = new HashMap<>();
			JSONObject downloads = json.getJSONObject("downloadUrls");
			Iterator<String> downloadKeys = downloads.keys();
			while (downloadKeys.hasNext()) {
				String channel = downloadKeys.next();
				downloadUrls.put(channel, downloads.getString(channel));
			}

			String category = json.getString("category");

			return new ScriptInfo(id, nameMap, descriptionMap, detailMap, downloadUrls, category);
		}
	}

	/* =========================================================
	 * 显示脚本仓库对话框（添加搜索功能）
	 * ======================================================= */
	private void showScriptRepositoryDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				final Dialog dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);

				// 根容器
				FrameLayout dialogContainer = new FrameLayout(act);
				GradientDrawable containerBg = new GradientDrawable();
				containerBg.setColor(Color.WHITE);
				containerBg.setCornerRadius(dp(act, 24));
				dialogContainer.setBackground(containerBg);

				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(0, 0, 0, 0);

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));

				// 标题
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "script_repository_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);

				// 副标题
				TextView subtitle = new TextView(act);
				subtitle.setText(getLocalizedString(ctx, "script_repository_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 16));
				root.addView(subtitle);

				// ========== 新增搜索框 ==========
				LinearLayout searchContainer = new LinearLayout(act);
				searchContainer.setOrientation(LinearLayout.VERTICAL);
				searchContainer.setPadding(0, 0, 0, dp(act, 16));

				LinearLayout searchRow = new LinearLayout(act);
				searchRow.setOrientation(LinearLayout.HORIZONTAL);
				searchRow.setGravity(Gravity.CENTER_VERTICAL);

				final EditText searchEdit = new EditText(act);
				searchEdit.setHint(getLocalizedString(ctx, "script_search_hint"));
				searchEdit.setTextColor(Color.BLACK);
				searchEdit.setHintTextColor(0xFF888888);
				searchEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				searchEdit.setBackground(getRoundBg(act, 0xFFF5F5F5, 8));
				searchEdit.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
				LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				searchLp.rightMargin = dp(act, 8);
				searchRow.addView(searchEdit, searchLp);

				Button searchButton = new Button(act);
				searchButton.setText(getLocalizedString(ctx, "script_search_button"));
				searchButton.setTextColor(Color.WHITE);
				searchButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				searchButton.setBackground(getRoundBg(act, 0xFF6200EE, 8));
				searchButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
				searchRow.addView(searchButton);

				searchContainer.addView(searchRow);

				// ========== 新增收录条数显示 ==========
				final TextView scriptCountText = new TextView(act);
				scriptCountText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				scriptCountText.setTextColor(0xFF888888);
				scriptCountText.setPadding(dp(act, 4), dp(act, 4), 0, 0);
				searchContainer.addView(scriptCountText);

				root.addView(searchContainer);
				// ========== 搜索框结束 ==========

				// 脚本列表容器
				final LinearLayout scriptsContainer = new LinearLayout(act);
				scriptsContainer.setOrientation(LinearLayout.VERTICAL);
				scriptsContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				// 空数据/错误提示
				final LinearLayout emptyStateContainer = new LinearLayout(act);
				emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
				emptyStateContainer.setGravity(Gravity.CENTER);
				emptyStateContainer.setPadding(0, dp(act, 48), 0, dp(act, 48));
				emptyStateContainer.setVisibility(View.GONE);

				// 错误图标
				final ImageView errorIcon = new ImageView(act);
				errorIcon.setImageResource(android.R.drawable.ic_menu_report_image);
				errorIcon.setColorFilter(0xFF888888);
				errorIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(act, 64), dp(act, 64));
				iconLp.gravity = Gravity.CENTER;
				iconLp.bottomMargin = dp(act, 16);
				emptyStateContainer.addView(errorIcon, iconLp);

				// 错误信息
				final TextView emptyStateText = new TextView(act);
				emptyStateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				emptyStateText.setTextColor(0xFF888888);
				emptyStateText.setGravity(Gravity.CENTER);
				emptyStateText.setPadding(dp(act, 32), 0, dp(act, 32), 0);
				emptyStateText.setText(getLocalizedString(ctx, "scripts_loading"));
				emptyStateContainer.addView(emptyStateText);

				root.addView(scriptsContainer);
				root.addView(emptyStateContainer);

				// 确定按钮
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				ok.setPadding(0, dp(act, 14), 0, dp(act, 14));
				ok.setBackground(getRoundBg(act, 0xFF6200EE, 12));

				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				okLp.topMargin = dp(act, 16);
				root.addView(ok, okLp);

				scrollRoot.addView(root);
				dialogContainer.addView(scrollRoot);
				dialog.setContentView(dialogContainer);

				// 设置对话框窗口属性
				Window window = dialog.getWindow();
				if (window != null) {
					window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					DisplayMetrics metrics = new DisplayMetrics();
					act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int width = (int) (metrics.widthPixels * 0.9);
					WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
					layoutParams.copyFrom(window.getAttributes());
					layoutParams.width = width;
					layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
					layoutParams.gravity = Gravity.CENTER;
					window.setAttributes(layoutParams);
					window.setClipToOutline(true);
				}

				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				// 存储当前脚本列表用于搜索
				final List<ScriptInfo>[] allScripts = new List[]{new ArrayList<ScriptInfo>()};

				// 更新收录条数显示的方法
				final Runnable updateScriptCount = new Runnable() {
					@Override
					public void run() {
						if (allScripts[0] != null && !allScripts[0].isEmpty()) {
							String countText = String.format(getLocalizedString(ctx, "script_total_count"),
									allScripts[0].size());
							scriptCountText.setText(countText);
						} else {
							scriptCountText.setText(getLocalizedString(ctx, "script_loading_count"));
						}
					}
				};

				// 搜索按钮点击事件
				searchButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String query = searchEdit.getText().toString().trim().toLowerCase();
						// 点击搜索按钮时显示Toast
						filterScripts(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText, allScripts[0],
								query, true);
					}
				});

				// 搜索框文本变化实时搜索（不显示Toast）
				searchEdit.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						String query = s.toString().trim().toLowerCase();
						// 实时搜索时不显示Toast
						filterScripts(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText, allScripts[0],
								query, false);

						// 更新收录条数显示
						if (allScripts[0] != null && !allScripts[0].isEmpty()) {
							List<ScriptInfo> filteredScripts = new ArrayList<>();
							for (ScriptInfo script : allScripts[0]) {
								String name = script.getName(ctx).toLowerCase();
								String description = script.getDescription(ctx).toLowerCase();
								String category = script.category.toLowerCase();

								if (name.contains(query) || description.contains(query) || category.contains(query)) {
									filteredScripts.add(script);
								}
							}
							String countText;
							if (query.isEmpty()) {
								countText = String.format(getLocalizedString(ctx, "script_total_count"),
										allScripts[0].size());
							} else {
								countText = String.format(getLocalizedString(ctx, "script_filtered_count"),
										filteredScripts.size(), allScripts[0].size());
							}
							scriptCountText.setText(countText);
						}
					}

					@Override
					public void afterTextChanged(Editable s) {
					}
				});

				// 初始显示加载状态
				showScriptsLoadingState(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText);

				// 加载脚本数据
				loadScriptsFromNetwork(ctx, new ScriptsLoadCallback() {
					@Override
					public void onScriptsLoaded(final List<ScriptInfo> scripts) {
						allScripts[0] = scripts; // 保存完整脚本列表
						if (act != null && !act.isFinishing()) {
							act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									refreshScriptsList(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText,
											scripts);
									// 更新收录条数显示
									updateScriptCount.run();
								}
							});
						}
					}

					@Override
					public void onLoadFailed(final String error) {
						if (act != null && !act.isFinishing()) {
							act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									showScriptsErrorState(act, ctx, scriptsContainer, emptyStateContainer,
											emptyStateText, error);
									scriptCountText.setText(getLocalizedString(ctx, "script_load_failed_count"));
								}
							});
						}
					}
				});

				dialog.show();
			}
		});
	}

	/* =========================================================
	 * 加载脚本数据
	 * ======================================================= */
	private void loadScriptsFromNetwork(final Context ctx, final ScriptsLoadCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String networkSource = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE);
					String scriptsUrl = networkSource.equals(NETWORK_SOURCE_GITEE)
							? "https://gitee.com/jiguro/BetterVia/raw/master/scripts.json"
							: "https://raw.githubusercontent.com/JiGuroLGC/BetterVia/main/scripts.json";

					URL url = new URL(scriptsUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(15000);
					connection.setReadTimeout(15000);
					connection.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

					if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
						InputStream inputStream = connection.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						reader.close();

						JSONObject json = new JSONObject(response.toString());
						JSONArray scriptsArray = json.getJSONArray("scripts");

						List<ScriptInfo> scripts = new ArrayList<>();
						for (int i = 0; i < scriptsArray.length(); i++) {
							JSONObject scriptJson = scriptsArray.getJSONObject(i);
							ScriptInfo script = ScriptInfo.fromJSON(scriptJson);
							scripts.add(script);
						}

						callback.onScriptsLoaded(scripts);
					} else {
						callback.onLoadFailed("HTTP " + connection.getResponseCode());
					}

					connection.disconnect();
				} catch (Exception e) {
					callback.onLoadFailed(e.getMessage());
				}
			}
		}).start();
	}

	/* =========================================================
	 * 过滤脚本列表
	 * @param showToast 是否显示搜索结果数量的Toast
	 * ======================================================= */
	private void filterScripts(Activity act, Context ctx, LinearLayout scriptsContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, List<ScriptInfo> allScripts, String query,
			boolean showToast) {
		if (allScripts == null || allScripts.isEmpty()) {
			showScriptsErrorState(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText,
					getLocalizedString(ctx, "no_scripts_available"));
			return;
		}

		if (query.isEmpty()) {
			// 如果搜索为空，显示所有脚本
			refreshScriptsList(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText, allScripts);
			if (showToast) {
				Toast.makeText(act, String.format(getLocalizedString(ctx, "script_show_all"), allScripts.size()),
						Toast.LENGTH_SHORT).show();
			}
			return;
		}

		// 过滤脚本
		List<ScriptInfo> filteredScripts = new ArrayList<>();
		for (ScriptInfo script : allScripts) {
			// 搜索脚本名称、描述、分类
			String name = script.getName(ctx).toLowerCase();
			String description = script.getDescription(ctx).toLowerCase();
			String category = script.category.toLowerCase();

			if (name.contains(query) || description.contains(query) || category.contains(query)) {
				filteredScripts.add(script);
			}
		}

		if (filteredScripts.isEmpty()) {
			// 显示无结果状态
			scriptsContainer.removeAllViews();
			scriptsContainer.setVisibility(View.GONE);

			emptyStateContainer.setVisibility(View.VISIBLE);
			emptyStateText.setText(String.format(getLocalizedString(ctx, "script_search_no_results"), query));

			// 显示搜索图标
			for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
				View child = emptyStateContainer.getChildAt(i);
				if (child instanceof ImageView) {
					child.setVisibility(View.VISIBLE);
					((ImageView) child).setImageResource(android.R.drawable.ic_search_category_default);
				} else if (child instanceof ProgressBar) {
					emptyStateContainer.removeView(child);
				}
			}

			if (showToast) {
				Toast.makeText(act, getLocalizedString(ctx, "script_search_no_results_toast"), Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			// 显示过滤后的结果
			refreshScriptsList(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText, filteredScripts);

			// 只在点击搜索按钮时显示Toast
			if (showToast) {
				Toast.makeText(act,
						String.format(getLocalizedString(ctx, "script_search_results"), filteredScripts.size()),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/* =========================================================
	 * 显示脚本加载状态
	 * ======================================================= */
	private void showScriptsLoadingState(Activity act, Context ctx, LinearLayout scriptsContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText) {
		scriptsContainer.removeAllViews();
		scriptsContainer.setVisibility(View.GONE);

		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(getLocalizedString(ctx, "scripts_loading"));

		// 隐藏错误图标，显示加载动画
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.GONE);
			}
		}

		// 添加加载动画
		ProgressBar progressBar = new ProgressBar(act);
		progressBar.setIndeterminate(true);
		LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(dp(act, 48), dp(act, 48));
		progressLp.gravity = Gravity.CENTER;
		progressLp.bottomMargin = dp(act, 16);
		emptyStateContainer.addView(progressBar, 0, progressLp);
	}

	/* =========================================================
	 * 显示脚本错误状态
	 * ======================================================= */
	private void showScriptsErrorState(Activity act, Context ctx, LinearLayout scriptsContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, String error) {
		scriptsContainer.removeAllViews();
		scriptsContainer.setVisibility(View.GONE);

		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(
				getLocalizedString(ctx, "scripts_load_failed") + "\n" + getLocalizedString(ctx, "check_network"));

		// 显示错误图标
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.VISIBLE);
			} else if (child instanceof ProgressBar) {
				emptyStateContainer.removeView(child); // 移除加载动画
			}
		}

		Toast.makeText(ctx, getLocalizedString(ctx, "scripts_load_failed") + ": " + error, Toast.LENGTH_SHORT).show();
	}

	/* =========================================================
	 * 刷新脚本列表
	 * ======================================================= */
	private void refreshScriptsList(final Activity act, final Context ctx, LinearLayout scriptsContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, List<ScriptInfo> scripts) {
		scriptsContainer.removeAllViews();

		if (scripts == null || scripts.isEmpty()) {
			showScriptsErrorState(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText,
					"No scripts available");
			return;
		}

		scriptsContainer.setVisibility(View.VISIBLE);
		emptyStateContainer.setVisibility(View.GONE);

		// 按分类分组
		Map<String, List<ScriptInfo>> categorizedScripts = new HashMap<>();
		for (ScriptInfo script : scripts) {
			String category = script.category;
			if (!categorizedScripts.containsKey(category)) {
				categorizedScripts.put(category, new ArrayList<ScriptInfo>());
			}
			categorizedScripts.get(category).add(script);
		}

		// 添加分类标题和脚本卡片
		for (Map.Entry<String, List<ScriptInfo>> entry : categorizedScripts.entrySet()) {
			String category = entry.getKey();
			List<ScriptInfo> categoryScripts = entry.getValue();

			// 分类标题
			TextView categoryTitle = new TextView(act);
			categoryTitle.setText(category);
			categoryTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			categoryTitle.setTextColor(0xFF6200EE);
			categoryTitle.setTypeface(null, Typeface.BOLD);
			categoryTitle.setPadding(0, dp(act, 16), 0, dp(act, 8));
			scriptsContainer.addView(categoryTitle);

			// 该分类下的脚本
			for (final ScriptInfo script : categoryScripts) {
				LinearLayout scriptCard = createScriptCard(act, ctx, script);
				LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				cardLp.bottomMargin = dp(ctx, 12);
				scriptsContainer.addView(scriptCard, cardLp);
			}
		}
	}

	/* =========================================================
	 * 创建脚本卡片
	 * ======================================================= */
	private LinearLayout createScriptCard(final Activity act, final Context ctx, final ScriptInfo script) {
		// 脚本卡片容器
		LinearLayout scriptCard = new LinearLayout(act);
		scriptCard.setOrientation(LinearLayout.VERTICAL);
		scriptCard.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

		GradientDrawable cardBg = new GradientDrawable();
		cardBg.setColor(0xFFF8F9FA);
		cardBg.setStroke(dp(act, 1), 0xFFE9ECEF);
		cardBg.setCornerRadius(dp(act, 12));
		scriptCard.setBackground(cardBg);

		// 脚本名称
		TextView scriptName = new TextView(act);
		scriptName.setText(script.getName(ctx));
		scriptName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		scriptName.setTextColor(Color.BLACK);
		scriptName.setTypeface(null, Typeface.BOLD);
		scriptCard.addView(scriptName);

		// 脚本简介
		TextView scriptDescription = new TextView(act);
		scriptDescription.setText(script.getDescription(ctx));
		scriptDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		scriptDescription.setTextColor(0xFF666666);
		scriptDescription.setPadding(0, dp(act, 8), 0, 0);
		scriptCard.addView(scriptDescription);

		// 点击事件
		scriptCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showScriptDetailDialog(ctx, script);
			}
		});

		return scriptCard;
	}

	/* =========================================================
	 * 显示脚本详情对话框
	 * ======================================================= */
	private void showScriptDetailDialog(final Context ctx, final ScriptInfo script) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// 创建对话框并保存引用
				final AlertDialog[] detailDialogRef = new AlertDialog[1];
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle(script.getName(ctx));

				ScrollView scrollView = new ScrollView(act);
				LinearLayout layout = new LinearLayout(act);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));

				// 详细描述
				TextView detailText = new TextView(act);
				detailText.setText(script.getDetail(ctx));
				detailText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				detailText.setTextColor(Color.BLACK);
				detailText.setLineSpacing(dp(act, 4), 1.2f);
				layout.addView(detailText);

				// 下载渠道按钮容器
				LinearLayout buttonContainer = new LinearLayout(act);
				buttonContainer.setOrientation(LinearLayout.VERTICAL);
				buttonContainer.setPadding(0, dp(act, 16), 0, 0);

				// 添加下载渠道按钮
				for (final Map.Entry<String, String> entry : script.downloadUrls.entrySet()) {
					Button downloadBtn = new Button(act);
					downloadBtn.setText(entry.getKey());
					downloadBtn.setTextColor(Color.WHITE);
					downloadBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					downloadBtn.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
					downloadBtn.setBackground(getRoundBg(act, 0xFF6200EE, 8));

					LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					btnLp.bottomMargin = dp(act, 8);
					buttonContainer.addView(downloadBtn, btnLp);

					downloadBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								// 1. 先关闭脚本详情对话框
								if (detailDialogRef[0] != null) {
									detailDialogRef[0].dismiss();
								}

								// 2. 延迟一小段时间后打开链接（确保对话框完全关闭）
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										try {
											Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getValue()));
											act.startActivity(intent);

											// 3. 显示提示Toast
											Toast.makeText(act, getLocalizedString(ctx, "script_opened_in_via"),
													Toast.LENGTH_LONG).show();
										} catch (Exception e) {
											Toast.makeText(act, getLocalizedString(ctx, "cannot_open_download_link"),
													Toast.LENGTH_SHORT).show();
										}
									}
								}, 100); // 100ms延迟确保对话框完全关闭

							} catch (Exception e) {
								Toast.makeText(act, getLocalizedString(ctx, "cannot_open_download_link"),
										Toast.LENGTH_SHORT).show();
							}
						}
					});
				}

				layout.addView(buttonContainer);
				scrollView.addView(layout);
				builder.setView(scrollView);

				builder.setNegativeButton(getLocalizedString(ctx, "dialog_cancel"), null);

				// 创建并显示对话框
				detailDialogRef[0] = builder.create();
				detailDialogRef[0].show();
			}
		});
	}

	/* =========================================================
	 * 脚本加载回调接口
	 * ======================================================= */
	private interface ScriptsLoadCallback {
		void onScriptsLoaded(List<ScriptInfo> scripts);
		void onLoadFailed(String error);
	}

	/* =========================================================
	* 添加广告走开设置项
	* ======================================================= */
	private void addAdBlockRulesItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(getLocalizedString(ctx, "ad_block_rules_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		// 配置按钮
		TextView configBtn = new TextView(ctx);
		configBtn.setText(getLocalizedString(ctx, "ad_block_rules_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAdBlockRulesDialog(ctx);
			}
		});
		hor.addView(configBtn);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(getLocalizedString(ctx, "ad_block_rules_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	/* =========================================================
	 * 显示广告走开规则对话框
	 * ======================================================= */
	private void showAdBlockRulesDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				final Dialog dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);

				// 根容器
				FrameLayout dialogContainer = new FrameLayout(act);
				GradientDrawable containerBg = new GradientDrawable();
				containerBg.setColor(Color.WHITE);
				containerBg.setCornerRadius(dp(act, 24));
				dialogContainer.setBackground(containerBg);

				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(0, 0, 0, 0);

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));

				// 标题
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "ad_block_rules_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);

				// 副标题
				TextView subtitle = new TextView(act);
				subtitle.setText(getLocalizedString(ctx, "ad_block_rules_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);

				// 规则列表容器
				final LinearLayout rulesContainer = new LinearLayout(act);
				rulesContainer.setOrientation(LinearLayout.VERTICAL);
				rulesContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				// 空数据/错误提示
				final LinearLayout emptyStateContainer = new LinearLayout(act);
				emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
				emptyStateContainer.setGravity(Gravity.CENTER);
				emptyStateContainer.setPadding(0, dp(act, 48), 0, dp(act, 48));
				emptyStateContainer.setVisibility(View.GONE);

				// 错误图标
				final ImageView errorIcon = new ImageView(act);
				errorIcon.setImageResource(android.R.drawable.ic_menu_report_image);
				errorIcon.setColorFilter(0xFF888888);
				errorIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(act, 64), dp(act, 64));
				iconLp.gravity = Gravity.CENTER;
				iconLp.bottomMargin = dp(act, 16);
				emptyStateContainer.addView(errorIcon, iconLp);

				// 错误信息
				final TextView emptyStateText = new TextView(act);
				emptyStateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				emptyStateText.setTextColor(0xFF888888);
				emptyStateText.setGravity(Gravity.CENTER);
				emptyStateText.setPadding(dp(act, 32), 0, dp(act, 32), 0);
				emptyStateContainer.addView(emptyStateText);

				root.addView(rulesContainer);
				root.addView(emptyStateContainer);

				// 确定按钮
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				ok.setPadding(0, dp(act, 14), 0, dp(act, 14));
				ok.setBackground(getRoundBg(act, 0xFF6200EE, 12));

				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				okLp.topMargin = dp(act, 16);
				root.addView(ok, okLp);

				scrollRoot.addView(root);
				dialogContainer.addView(scrollRoot);
				dialog.setContentView(dialogContainer);

				// 设置对话框窗口属性
				Window window = dialog.getWindow();
				if (window != null) {
					window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					DisplayMetrics metrics = new DisplayMetrics();
					act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int width = (int) (metrics.widthPixels * 0.9);
					WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
					layoutParams.copyFrom(window.getAttributes());
					layoutParams.width = width;
					layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
					layoutParams.gravity = Gravity.CENTER;
					window.setAttributes(layoutParams);
					window.setClipToOutline(true);
				}

				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				// 初始显示加载状态
				showRulesLoadingState(act, ctx, rulesContainer, emptyStateContainer, emptyStateText);

				// 加载规则数据
				loadRulesFromNetwork(ctx, new RulesLoadCallback() {
					@Override
					public void onRulesLoaded(final List<RuleInfo> rules) {
						if (act != null && !act.isFinishing()) {
							act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									refreshRulesList(act, ctx, rulesContainer, emptyStateContainer, emptyStateText,
											rules);
								}
							});
						}
					}

					@Override
					public void onLoadFailed(final String error) {
						if (act != null && !act.isFinishing()) {
							act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									showRulesErrorState(act, ctx, rulesContainer, emptyStateContainer, emptyStateText,
											error);
								}
							});
						}
					}
				});

				dialog.show();
			}
		});
	}

	/* =========================================================
	 * 规则信息类
	 * ======================================================= */
	private static class RuleInfo {
		String id;
		Map<String, String> nameMap; // 语言代码 -> 名称
		Map<String, String> descriptionMap; // 语言代码 -> 简介
		Map<String, String> detailMap; // 语言代码 -> 详细描述
		Map<String, String> downloadUrls; // 渠道名称 -> 下载链接
		String category;
		String author;
		String homepage;

		RuleInfo(String id, Map<String, String> nameMap, Map<String, String> descriptionMap,
				Map<String, String> detailMap, Map<String, String> downloadUrls, String category, String author,
				String homepage) {
			this.id = id;
			this.nameMap = nameMap;
			this.descriptionMap = descriptionMap;
			this.detailMap = detailMap;
			this.downloadUrls = downloadUrls;
			this.category = category;
			this.author = author;
			this.homepage = homepage;
		}

		String getName(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return nameMap.getOrDefault(langCode, nameMap.get("zh-CN"));
		}

		String getDescription(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return descriptionMap.getOrDefault(langCode, descriptionMap.get("zh-CN"));
		}

		String getDetail(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return detailMap.getOrDefault(langCode, detailMap.get("zh-CN"));
		}

		private String getLanguageCode(Context ctx) {
			String saved = getSavedLanguageStatic(ctx);
			if ("auto".equals(saved)) {
				Locale locale;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					locale = ctx.getResources().getConfiguration().getLocales().get(0);
				} else {
					locale = ctx.getResources().getConfiguration().locale;
				}

				if (Locale.SIMPLIFIED_CHINESE.equals(locale)) {
					return "zh-CN";
				} else if (Locale.TRADITIONAL_CHINESE.equals(locale)) {
					return "zh-TW";
				} else if (Locale.ENGLISH.equals(locale)) {
					return "en";
				}
				return "zh-CN";
			}
			return saved;
		}

		static RuleInfo fromJSON(JSONObject json) throws JSONException {
			String id = json.getString("id");

			// 解析多语言名称
			Map<String, String> nameMap = new HashMap<>();
			JSONObject names = json.getJSONObject("names");
			Iterator<String> nameKeys = names.keys();
			while (nameKeys.hasNext()) {
				String lang = nameKeys.next();
				nameMap.put(lang, names.getString(lang));
			}

			// 解析多语言简介
			Map<String, String> descriptionMap = new HashMap<>();
			JSONObject descriptions = json.getJSONObject("descriptions");
			Iterator<String> descKeys = descriptions.keys();
			while (descKeys.hasNext()) {
				String lang = descKeys.next();
				descriptionMap.put(lang, descriptions.getString(lang));
			}

			// 解析多语言详细描述
			Map<String, String> detailMap = new HashMap<>();
			JSONObject details = json.getJSONObject("details");
			Iterator<String> detailKeys = details.keys();
			while (detailKeys.hasNext()) {
				String lang = detailKeys.next();
				detailMap.put(lang, details.getString(lang));
			}

			// 解析下载链接
			Map<String, String> downloadUrls = new HashMap<>();
			JSONObject downloads = json.getJSONObject("downloadUrls");
			Iterator<String> downloadKeys = downloads.keys();
			while (downloadKeys.hasNext()) {
				String channel = downloadKeys.next();
				downloadUrls.put(channel, downloads.getString(channel));
			}

			String category = json.getString("category");
			String author = json.optString("author", "");
			String homepage = json.optString("homepage", "");

			return new RuleInfo(id, nameMap, descriptionMap, detailMap, downloadUrls, category, author, homepage);
		}
	}

	/* =========================================================
	 * 加载规则数据
	 * ======================================================= */
	private void loadRulesFromNetwork(final Context ctx, final RulesLoadCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String networkSource = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE);
					String rulesUrl = networkSource.equals(NETWORK_SOURCE_GITEE)
							? "https://gitee.com/jiguro/BetterVia/raw/master/adblock.json"
							: "https://raw.githubusercontent.com/JiGuroLGC/BetterVia/main/adblock.json";

					URL url = new URL(rulesUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(15000);
					connection.setReadTimeout(15000);
					connection.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

					if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
						InputStream inputStream = connection.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						reader.close();

						JSONObject json = new JSONObject(response.toString());
						JSONArray rulesArray = json.getJSONArray("rules");

						List<RuleInfo> rules = new ArrayList<>();
						for (int i = 0; i < rulesArray.length(); i++) {
							JSONObject ruleJson = rulesArray.getJSONObject(i);
							RuleInfo rule = RuleInfo.fromJSON(ruleJson);
							rules.add(rule);
						}

						callback.onRulesLoaded(rules);
					} else {
						callback.onLoadFailed("HTTP " + connection.getResponseCode());
					}

					connection.disconnect();
				} catch (Exception e) {
					callback.onLoadFailed(e.getMessage());
				}
			}
		}).start();
	}

	/* =========================================================
	 * 显示规则加载状态
	 * ======================================================= */
	private void showRulesLoadingState(Activity act, Context ctx, LinearLayout rulesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText) {
		rulesContainer.removeAllViews();
		rulesContainer.setVisibility(View.GONE);

		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(getLocalizedString(ctx, "rules_loading"));

		// 隐藏错误图标，显示加载动画
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.GONE);
			}
		}

		// 添加加载动画
		ProgressBar progressBar = new ProgressBar(act);
		progressBar.setIndeterminate(true);
		LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(dp(act, 48), dp(act, 48));
		progressLp.gravity = Gravity.CENTER;
		progressLp.bottomMargin = dp(act, 16);
		emptyStateContainer.addView(progressBar, 0, progressLp);
	}

	/* =========================================================
	 * 显示规则错误状态
	 * ======================================================= */
	private void showRulesErrorState(Activity act, Context ctx, LinearLayout rulesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, String error) {
		rulesContainer.removeAllViews();
		rulesContainer.setVisibility(View.GONE);

		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(
				getLocalizedString(ctx, "rules_load_failed") + "\n" + getLocalizedString(ctx, "check_network"));

		// 显示错误图标
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.VISIBLE);
			} else if (child instanceof ProgressBar) {
				emptyStateContainer.removeView(child); // 移除加载动画
			}
		}

		Toast.makeText(ctx, getLocalizedString(ctx, "rules_load_failed") + ": " + error, Toast.LENGTH_SHORT).show();
	}

	/* =========================================================
	 * 刷新规则列表
	 * ======================================================= */
	private void refreshRulesList(final Activity act, final Context ctx, LinearLayout rulesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, List<RuleInfo> rules) {
		rulesContainer.removeAllViews();

		if (rules == null || rules.isEmpty()) {
			showRulesErrorState(act, ctx, rulesContainer, emptyStateContainer, emptyStateText, "No rules available");
			return;
		}

		rulesContainer.setVisibility(View.VISIBLE);
		emptyStateContainer.setVisibility(View.GONE);

		// 按分类分组
		Map<String, List<RuleInfo>> categorizedRules = new HashMap<>();
		for (RuleInfo rule : rules) {
			String category = rule.category;
			if (!categorizedRules.containsKey(category)) {
				categorizedRules.put(category, new ArrayList<RuleInfo>());
			}
			categorizedRules.get(category).add(rule);
		}

		// 添加分类标题和规则卡片
		for (Map.Entry<String, List<RuleInfo>> entry : categorizedRules.entrySet()) {
			String category = entry.getKey();
			List<RuleInfo> categoryRules = entry.getValue();

			// 分类标题
			TextView categoryTitle = new TextView(act);
			categoryTitle.setText(getCategoryDisplayName(ctx, category));
			categoryTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			categoryTitle.setTextColor(0xFF6200EE);
			categoryTitle.setTypeface(null, Typeface.BOLD);
			categoryTitle.setPadding(0, dp(act, 16), 0, dp(act, 8));
			rulesContainer.addView(categoryTitle);

			// 该分类下的规则
			for (final RuleInfo rule : categoryRules) {
				LinearLayout ruleCard = createRuleCard(act, ctx, rule);
				LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				cardLp.bottomMargin = dp(ctx, 12);
				rulesContainer.addView(ruleCard, cardLp);
			}
		}
	}

	/* =========================================================
	 * 获取分类显示名称
	 * ======================================================= */
	private String getCategoryDisplayName(Context ctx, String category) {
		if ("small".equals(category)) {
			return getLocalizedString(ctx, "rules_category_small");
		} else if ("large".equals(category)) {
			return getLocalizedString(ctx, "rules_category_large");
		}
		return category;
	}

	/* =========================================================
	 * 创建规则卡片
	 * ======================================================= */
	private LinearLayout createRuleCard(final Activity act, final Context ctx, final RuleInfo rule) {
		// 规则卡片容器
		LinearLayout ruleCard = new LinearLayout(act);
		ruleCard.setOrientation(LinearLayout.VERTICAL);
		ruleCard.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

		GradientDrawable cardBg = new GradientDrawable();
		cardBg.setColor(0xFFF8F9FA);
		cardBg.setStroke(dp(act, 1), 0xFFE9ECEF);
		cardBg.setCornerRadius(dp(act, 12));
		ruleCard.setBackground(cardBg);

		// 规则名称
		TextView ruleName = new TextView(act);
		ruleName.setText(rule.getName(ctx));
		ruleName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		ruleName.setTextColor(Color.BLACK);
		ruleName.setTypeface(null, Typeface.BOLD);
		ruleCard.addView(ruleName);

		// 规则简介
		TextView ruleDescription = new TextView(act);
		ruleDescription.setText(rule.getDescription(ctx));
		ruleDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		ruleDescription.setTextColor(0xFF666666);
		ruleDescription.setPadding(0, dp(act, 8), 0, 0);
		ruleCard.addView(ruleDescription);

		// 点击事件
		ruleCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showRuleDetailDialog(ctx, rule);
			}
		});

		return ruleCard;
	}

	/* =========================================================
	 * 显示规则详情对话框
	 * ======================================================= */
	private void showRuleDetailDialog(final Context ctx, final RuleInfo rule) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// 创建对话框并保存引用
				final AlertDialog[] detailDialogRef = new AlertDialog[1];
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle(rule.getName(ctx));

				ScrollView scrollView = new ScrollView(act);
				LinearLayout layout = new LinearLayout(act);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));

				// 详细描述
				TextView detailText = new TextView(act);
				detailText.setText(rule.getDetail(ctx));
				detailText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				detailText.setTextColor(Color.BLACK);
				detailText.setLineSpacing(dp(act, 4), 1.2f);
				layout.addView(detailText);

				// 作者信息（如果有）
				if (rule.author != null && !rule.author.isEmpty()) {
					TextView authorText = new TextView(act);
					authorText.setText(getLocalizedString(ctx, "rule_author") + ": " + rule.author);
					authorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					authorText.setTextColor(0xFF666666);
					authorText.setPadding(0, dp(act, 8), 0, 0);
					layout.addView(authorText);
				}

				// 主页链接（如果有且可点击）
				if (rule.homepage != null && !rule.homepage.isEmpty()) {
					TextView homepageText = new TextView(act);
					homepageText.setText(getLocalizedString(ctx, "rule_homepage") + ": " + rule.homepage);
					homepageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					homepageText.setTextColor(0xFF6200EE);
					homepageText.setPadding(0, dp(act, 8), 0, 0);
					homepageText.setPaintFlags(homepageText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
					homepageText.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rule.homepage));
								act.startActivity(intent);
							} catch (Exception e) {
								Toast.makeText(act, getLocalizedString(ctx, "cannot_open_homepage"), Toast.LENGTH_SHORT)
										.show();
							}
						}
					});
					layout.addView(homepageText);
				}

				// 下载渠道按钮容器
				LinearLayout buttonContainer = new LinearLayout(act);
				buttonContainer.setOrientation(LinearLayout.VERTICAL);
				buttonContainer.setPadding(0, dp(act, 16), 0, 0);

				// 添加下载渠道按钮
				int channelIndex = 1;
				for (final Map.Entry<String, String> entry : rule.downloadUrls.entrySet()) {
					Button downloadBtn = new Button(act);
					downloadBtn.setText(
							getLocalizedString(ctx, "rule_channel") + " " + channelIndex + " - " + entry.getKey());
					downloadBtn.setTextColor(Color.WHITE);
					downloadBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					downloadBtn.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
					downloadBtn.setBackground(getRoundBg(act, 0xFF6200EE, 8));

					LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					btnLp.bottomMargin = dp(act, 8);
					buttonContainer.addView(downloadBtn, btnLp);

					downloadBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							copyToClipboard(act, entry.getValue());
							Toast.makeText(act, getLocalizedString(ctx, "rule_link_copied"), Toast.LENGTH_SHORT).show();
						}
					});

					channelIndex++;
				}

				layout.addView(buttonContainer);
				scrollView.addView(layout);
				builder.setView(scrollView);

				builder.setNegativeButton(getLocalizedString(ctx, "dialog_cancel"), null);

				// 创建并显示对话框
				detailDialogRef[0] = builder.create();
				detailDialogRef[0].show();
			}
		});
	}

	/* =========================================================
	 * 规则加载回调接口
	 * ======================================================= */
	private interface RulesLoadCallback {
		void onRulesLoaded(List<RuleInfo> rules);
		void onLoadFailed(String error);
	}

	/* =========================================================
	 * 隐藏状态栏 Hook 动态开关
	 * ======================================================= */
	private void setHideStatusBar(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (hideStatusBarHook == null) {
				// Hook Activity的onCreate方法，在Activity创建时隐藏状态栏
				hideStatusBarHook = XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class,
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								final Activity activity = (Activity) param.thisObject;
								if (hideStatusBarEnabled) {
									activity.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											setupStatusBarHiding(activity);
											statusBarHiddenActivities.put(activity, true);
											XposedBridge.log("已为 " + activity.getClass().getSimpleName() + " 设置状态栏隐藏");
										}
									});
								}
							}
						});
				XposedBridge.log("隐藏状态栏 Hook 已启用");
			}
		} else {
			if (hideStatusBarHook != null) {
				hideStatusBarHook.unhook();
				hideStatusBarHook = null;
				XposedBridge.log("隐藏状态栏 Hook 已停用");

				// 恢复所有Activity的状态栏显示
				restoreStatusBar();
			}
		}
		hideStatusBarEnabled = on;
		putPrefBoolean(ctx, KEY_HIDE_STATUS_BAR, on);
	}

	/* =========================================================
	* 设置状态栏隐藏（持续监听 + 自动重新隐藏）
	* ======================================================= */
	private void setupStatusBarHiding(final Activity activity) {
		try {
			if (activity.isFinishing() || activity.isDestroyed())
				return;

			final View decorView = activity.getWindow().getDecorView();

			// 1. 立即隐藏一次
			hideStatusBarImmediate(activity);

			// 2. 监听系统 UI 可见性变化
			decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int visibility) {
					// 如果状态栏变为可见（用户下滑）
					if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
						// 延迟重新隐藏，避免与系统冲突
						decorView.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (!activity.isFinishing() && !activity.isDestroyed() && hideStatusBarEnabled) {
									hideStatusBarImmediate(activity);
									// 关键：重新注册监听，防止系统清除
									setupStatusBarHiding(activity);
								}
							}
						}, 100);
					}
				}
			});
		} catch (Exception e) {
			XposedBridge.log("setupStatusBarHiding 失败: " + e);
		}
	}

	/* =========================================================
	 * 立即隐藏状态栏
	 * ======================================================= */
	private void hideStatusBarImmediate(Activity activity) {
		try {
			if (activity.isFinishing() || activity.isDestroyed()) {
				return;
			}

			View decorView = activity.getWindow().getDecorView();

			// 使用沉浸式全屏标志组合
			int flags = decorView.getSystemUiVisibility();
			flags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
			flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

			decorView.setSystemUiVisibility(flags);

			// 同时设置Window标志
			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

		} catch (Exception e) {
			XposedBridge.log("立即隐藏状态栏失败: " + e);
		}
	}

	/* =========================================================
	 * 恢复状态栏显示
	 * ======================================================= */
	private void restoreStatusBar() {
		for (final Activity activity : statusBarHiddenActivities.keySet()) {
			if (!activity.isFinishing() && !activity.isDestroyed()) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							View decorView = activity.getWindow().getDecorView();

							// 移除监听器
							decorView.setOnSystemUiVisibilityChangeListener(null);

							// 取消重新隐藏任务
							Runnable rehideRunnable = statusBarRehideRunnables.get(activity);
							if (rehideRunnable != null) {
								decorView.removeCallbacks(rehideRunnable);
								statusBarRehideRunnables.remove(activity);
							}

							// 恢复系统UI可见性
							int flags = decorView.getSystemUiVisibility();
							flags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
							flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
							flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
							flags &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

							decorView.setSystemUiVisibility(flags);

							// 清除WindowManager的全屏标志
							activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

						} catch (Exception e) {
							XposedBridge.log("恢复状态栏失败: " + e);
						}
					}
				});
			}
		}
		statusBarHiddenActivities.clear();
		statusBarRehideRunnables.clear();
		XposedBridge.log("已恢复所有Activity的状态栏显示");
	}

	/* =========================================================
	* 添加Cookie管理设置项
	* ======================================================= */
	private void addCookieManagementItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(getLocalizedString(ctx, "cookie_management_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		// 配置按钮
		TextView configBtn = new TextView(ctx);
		configBtn.setText(getLocalizedString(ctx, "cookie_management_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showCookieManagementDialog(ctx);
			}
		});
		hor.addView(configBtn);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(getLocalizedString(ctx, "cookie_management_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	/* =========================================================
	 * 显示Cookie管理对话框（亮色UI，支持滑动和搜索）
	 * ======================================================= */
	private void showCookieManagementDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				final Dialog dialog = new Dialog(act, android.R.style.Theme_NoTitleBar_Fullscreen);
				dialog.setCancelable(true);
				// 根布局 - 亮色背景
				LinearLayout rootLayout = new LinearLayout(act);
				rootLayout.setOrientation(LinearLayout.VERTICAL);
				rootLayout.setBackgroundColor(Color.WHITE);
				// 标题栏
				RelativeLayout titleBar = new RelativeLayout(act);
				titleBar.setBackgroundColor(0xFFF5F5F5);
				titleBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
				titleBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				// 返回按钮
				ImageButton backButton = new ImageButton(act);
				backButton.setImageResource(android.R.drawable.ic_menu_revert);
				backButton.setBackgroundResource(android.R.color.transparent);
				backButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				backButton.setPadding(dp(act, 8), dp(act, 8), dp(act, 8), dp(act, 8));
				backButton.setColorFilter(0xFF000000);
				RelativeLayout.LayoutParams backButtonLp = new RelativeLayout.LayoutParams(dp(act, 48), dp(act, 48));
				backButtonLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				backButtonLp.addRule(RelativeLayout.CENTER_VERTICAL);
				titleBar.addView(backButton, backButtonLp);
				// 标题
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "cookie_manager_dialog_title"));
				title.setTextColor(Color.BLACK);
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				title.setTypeface(null, Typeface.BOLD);
				RelativeLayout.LayoutParams titleLp = new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.addRule(RelativeLayout.CENTER_IN_PARENT);
				titleBar.addView(title, titleLp);
				// 刷新按钮
				ImageButton refreshButton = new ImageButton(act);
				refreshButton.setImageResource(android.R.drawable.ic_menu_rotate);
				refreshButton.setBackgroundResource(android.R.color.transparent);
				refreshButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				refreshButton.setPadding(dp(act, 8), dp(act, 8), dp(act, 8), dp(act, 8));
				refreshButton.setColorFilter(0xFF000000);
				RelativeLayout.LayoutParams refreshButtonLp = new RelativeLayout.LayoutParams(dp(act, 48), dp(act, 48));
				refreshButtonLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				refreshButtonLp.addRule(RelativeLayout.CENTER_VERTICAL);
				titleBar.addView(refreshButton, refreshButtonLp);
				rootLayout.addView(titleBar);
				// 内容区域
				LinearLayout contentLayout = new LinearLayout(act);
				contentLayout.setOrientation(LinearLayout.VERTICAL);
				contentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				contentLayout.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				// 搜索栏
				LinearLayout searchBar = new LinearLayout(act);
				searchBar.setOrientation(LinearLayout.HORIZONTAL);
				searchBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				searchBar.setPadding(0, 0, 0, dp(act, 12));
				final EditText searchEdit = new EditText(act);
				searchEdit.setHint(getLocalizedString(ctx, "cookie_manager_search_hint"));
				searchEdit.setTextColor(Color.BLACK);
				searchEdit.setHintTextColor(0xFF888888);
				searchEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				searchEdit.setBackground(getRoundBg(act, 0xFFF0F0F0, 8));
				searchEdit.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
				LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				searchLp.rightMargin = dp(act, 8);
				searchBar.addView(searchEdit, searchLp);
				Button searchButton = new Button(act);
				searchButton.setText(getLocalizedString(ctx, "cookie_manager_search_btn"));
				searchButton.setTextColor(Color.WHITE);
				searchButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				searchButton.setBackground(getRoundBg(act, 0xFF6200EE, 8));
				searchButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
				searchBar.addView(searchButton);
				contentLayout.addView(searchBar);

				FrameLayout listAndLoadingContainer = new FrameLayout(act);
				listAndLoadingContainer
						.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f)); // 占满剩余高度
				// 1. 加载提示容器（放在 FrameLayout 中，与列表重叠）
				final LinearLayout loadingContainer = new LinearLayout(act);
				loadingContainer.setOrientation(LinearLayout.VERTICAL);
				loadingContainer.setGravity(Gravity.CENTER);
				loadingContainer.setPadding(0, dp(act, 48), 0, dp(act, 48));
				loadingContainer.setVisibility(View.VISIBLE); // 初始显示
				// 加载动画
				ProgressBar progressBar = new ProgressBar(act);
				progressBar.setIndeterminate(true);
				LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(dp(act, 48), dp(act, 48));
				progressLp.gravity = Gravity.CENTER;
				progressLp.bottomMargin = dp(act, 16);
				loadingContainer.addView(progressBar, progressLp);
				// 加载文本
				TextView loadingText = new TextView(act);
				loadingText.setText(getLocalizedString(ctx, "cookie_manager_loading"));
				loadingText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				loadingText.setTextColor(0xFF888888);
				loadingText.setGravity(Gravity.CENTER);
				loadingContainer.addView(loadingText);
				// 添加到 contentLayout（与 scrollView 同级）
				contentLayout.addView(loadingContainer);
				// 2. Cookie列表容器（放在 FrameLayout 中，与加载提示重叠）
				final ScrollView scrollView = new ScrollView(act);
				scrollView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				scrollView.setVisibility(View.GONE); // 初始隐藏
				final LinearLayout listContainer = new LinearLayout(act);
				listContainer.setOrientation(LinearLayout.VERTICAL);
				listContainer.setPadding(0, 0, 0, 0);
				scrollView.addView(listContainer);
				listAndLoadingContainer.addView(scrollView); // 加入 FrameLayout
				// 将 FrameLayout 加入内容区域
				contentLayout.addView(listAndLoadingContainer);

				// 底部按钮栏
				LinearLayout buttonBar = new LinearLayout(act);
				buttonBar.setOrientation(LinearLayout.HORIZONTAL);
				buttonBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				buttonBar.setPadding(0, dp(act, 12), 0, 0);
				final Button deleteButton = new Button(act);
				deleteButton.setText(getLocalizedString(ctx, "cookie_manager_delete_selected"));
				deleteButton.setTextColor(Color.WHITE);
				deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				deleteButton.setBackground(getRoundBg(act, 0xFFE53935, 8));
				deleteButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
				deleteButton.setEnabled(false);
				LinearLayout.LayoutParams deleteLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				deleteLp.rightMargin = dp(act, 8);
				buttonBar.addView(deleteButton, deleteLp);
				Button closeButton = new Button(act);
				closeButton.setText(getLocalizedString(ctx, "dialog_close"));
				closeButton.setTextColor(Color.WHITE);
				closeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				closeButton.setBackground(getRoundBg(act, 0xFF6200EE, 8));
				closeButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
				LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				buttonBar.addView(closeButton, closeLp);
				contentLayout.addView(buttonBar);
				rootLayout.addView(contentLayout);
				dialog.setContentView(rootLayout);
				// 存储当前显示的Cookie列表用于搜索
				final List<CookieItem>[] currentCookieList = new List[]{new ArrayList<CookieItem>()};
				// 异步加载Cookie数据
				new Thread(new Runnable() {
					@Override
					public void run() {
						final List<CookieItem> cookieItems = loadCookieData(ctx);
						currentCookieList[0] = cookieItems;
						act.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// 隐藏加载提示
								loadingContainer.setVisibility(View.GONE);
								// 显示列表并填充数据
								scrollView.setVisibility(View.VISIBLE);
								populateCookieList(act, listContainer, cookieItems, deleteButton, scrollView);
							}
						});
					}
				}).start();

				// 返回按钮事件
				backButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				// 刷新按钮事件
				refreshButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 1. 显示加载提示（与初次加载完全一致）
						loadingContainer.setVisibility(View.VISIBLE);
						// 2. 隐藏列表（避免加载时显示旧数据）
						scrollView.setVisibility(View.GONE);
						new Thread(new Runnable() {
							@Override
							public void run() {
								final List<CookieItem> refreshedData = loadCookieData(ctx);
								currentCookieList[0] = refreshedData;
								act.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// 3. 隐藏加载提示
										loadingContainer.setVisibility(View.GONE);
										// 4. 显示列表并填充新数据
										scrollView.setVisibility(View.VISIBLE);
										listContainer.removeAllViews(); // 清空旧数据
										populateCookieList(act, listContainer, refreshedData, deleteButton, scrollView);
										Toast.makeText(act, getLocalizedString(ctx, "cookie_management_refreshed"),
												Toast.LENGTH_SHORT).show();
									}
								});
							}
						}).start();
					}
				});
				// 搜索按钮事件
				searchButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final String query = searchEdit.getText().toString().trim().toLowerCase();
						if (query.isEmpty()) {
							// 如果搜索为空，显示所有
							populateCookieList(act, listContainer, currentCookieList[0], deleteButton, scrollView);
							return;
						}
						// 过滤Cookie列表
						List<CookieItem> filteredList = new ArrayList<CookieItem>();
						for (CookieItem item : currentCookieList[0]) {
							if ((item.host_key != null && item.host_key.toLowerCase().contains(query))
									|| (item.name != null && item.name.toLowerCase().contains(query))
									|| (item.value != null && item.value.toLowerCase().contains(query))) {
								filteredList.add(item);
							}
						}
						populateCookieList(act, listContainer, filteredList, deleteButton, scrollView);
						// 多语言适配：替换硬编码Toast文本
						String resultMsg = String.format(getLocalizedString(act, "cookie_search_result"),
								filteredList.size());
						Toast.makeText(act, resultMsg, Toast.LENGTH_SHORT).show();
					}
				});

				// 删除按钮事件
				deleteButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showDeleteConfirmDialog(act, ctx, listContainer, deleteButton, scrollView);
					}
				});
				// 关闭按钮事件
				closeButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}

	/* =========================================================
	 * 显示Cookie详情对话框
	 * ======================================================= */
	private void showCookieDetailDialog(final Activity act, final CookieItem cookie) {
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		// 替换硬编码标题为多语言key
		builder.setTitle(getLocalizedString(act, "cookie_detail_dialog_title"));
		// 创建滚动视图
		ScrollView scrollView = new ScrollView(act);
		LinearLayout layout = new LinearLayout(act);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
		// 基本信息区域 - 多语言适配
		TextView basicInfoTitle = new TextView(act);
		basicInfoTitle.setText(getLocalizedString(act, "cookie_detail_basic_info"));
		basicInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		basicInfoTitle.setTextColor(Color.BLACK);
		basicInfoTitle.setTypeface(null, Typeface.BOLD);
		basicInfoTitle.setPadding(0, 0, 0, dp(act, 12));
		layout.addView(basicInfoTitle);
		// 可编辑字段 - 替换硬编码标签为多语言key
		final EditText hostKeyEdit = addEditableField(layout, act, getLocalizedString(act, "cookie_field_host_key"),
				cookie.host_key != null ? cookie.host_key : "");
		final EditText nameEdit = addEditableField(layout, act, getLocalizedString(act, "cookie_field_name"),
				cookie.name != null ? cookie.name : "");
		final EditText valueEdit = addEditableField(layout, act, getLocalizedString(act, "cookie_field_value"),
				cookie.value != null ? cookie.value : "");
		final EditText pathEdit = addEditableField(layout, act, getLocalizedString(act, "cookie_field_path"),
				cookie.path != null ? cookie.path : "");
		// 时间信息区域 - 多语言适配
		TextView timeInfoTitle = new TextView(act);
		timeInfoTitle.setText(getLocalizedString(act, "cookie_detail_time_info"));
		timeInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		timeInfoTitle.setTextColor(Color.BLACK);
		timeInfoTitle.setTypeface(null, Typeface.BOLD);
		timeInfoTitle.setPadding(0, dp(act, 16), 0, dp(act, 12));
		layout.addView(timeInfoTitle);
		// 时间信息（只读）- 替换硬编码标签为多语言key
		addReadOnlyField(layout, act, getLocalizedString(act, "cookie_field_creation_time"),
				cookie.creation_utc > 0
						? formatTimestamp(cookie.creation_utc)
						: getLocalizedString(act, "cookie_field_unknown"));
		addReadOnlyField(layout, act, getLocalizedString(act, "cookie_field_last_access"),
				cookie.last_access_utc > 0
						? formatTimestamp(cookie.last_access_utc)
						: getLocalizedString(act, "cookie_field_unknown"));
		addReadOnlyField(layout, act, getLocalizedString(act, "cookie_field_expires"),
				cookie.expires_utc > 0
						? formatTimestamp(cookie.expires_utc)
						: getLocalizedString(act, "cookie_field_session"));
		addReadOnlyField(layout, act, getLocalizedString(act, "cookie_field_last_update"),
				cookie.last_update_utc > 0
						? formatTimestamp(cookie.last_update_utc)
						: getLocalizedString(act, "cookie_field_unknown"));
		// 安全信息区域 - 多语言适配
		TextView securityInfoTitle = new TextView(act);
		securityInfoTitle.setText(getLocalizedString(act, "cookie_detail_security_info"));
		securityInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		securityInfoTitle.setTextColor(Color.BLACK);
		securityInfoTitle.setTypeface(null, Typeface.BOLD);
		securityInfoTitle.setPadding(0, dp(act, 16), 0, dp(act, 12));
		layout.addView(securityInfoTitle);
		// 安全设置（可编辑的复选框）- 替换硬编码标签为多语言key
		final CheckBox secureCheckbox = addCheckboxField(layout, act, getLocalizedString(act, "cookie_field_secure"),
				cookie.is_secure);
		final CheckBox httpOnlyCheckbox = addCheckboxField(layout, act,
				getLocalizedString(act, "cookie_field_httponly"), cookie.is_httponly);
		final CheckBox persistentCheckbox = addCheckboxField(layout, act,
				getLocalizedString(act, "cookie_field_persistent"), cookie.is_persistent);
		final CheckBox hasExpiresCheckbox = addCheckboxField(layout, act,
				getLocalizedString(act, "cookie_field_has_expires"), cookie.has_expires);
		// 高级信息区域（只读）- 多语言适配
		TextView advancedInfoTitle = new TextView(act);
		advancedInfoTitle.setText(getLocalizedString(act, "cookie_detail_advanced_info"));
		advancedInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		advancedInfoTitle.setTextColor(Color.BLACK);
		advancedInfoTitle.setTypeface(null, Typeface.BOLD);
		advancedInfoTitle.setPadding(0, dp(act, 16), 0, dp(act, 12));
		layout.addView(advancedInfoTitle);
		addReadOnlyField(layout, act, getLocalizedString(act, "cookie_field_priority"),
				String.valueOf(cookie.priority));
		addReadOnlyField(layout, act, getLocalizedString(act, "cookie_field_samesite"),
				getSameSiteText(act, cookie.samesite)); // 传入Context适配多语言
		addReadOnlyField(layout, act, getLocalizedString(act, "cookie_field_source_port"),
				cookie.source_port > 0
						? String.valueOf(cookie.source_port)
						: getLocalizedString(act, "cookie_field_default"));
		addReadOnlyField(layout, act, getLocalizedString(act, "cookie_field_source_type"),
				getSourceTypeText(act, cookie.source_type)); // 传入Context适配多语言
		scrollView.addView(layout);
		builder.setView(scrollView);
		// 按钮文字复用现有多语言key
		builder.setPositiveButton(getLocalizedString(act, "dialog_ok"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 更新Cookie信息
				cookie.host_key = hostKeyEdit.getText().toString();
				cookie.name = nameEdit.getText().toString();
				cookie.value = valueEdit.getText().toString();
				cookie.path = pathEdit.getText().toString();
				cookie.is_secure = secureCheckbox.isChecked();
				cookie.is_httponly = httpOnlyCheckbox.isChecked();
				cookie.is_persistent = persistentCheckbox.isChecked();
				cookie.has_expires = hasExpiresCheckbox.isChecked();
				// 设置最后更新时间
				cookie.last_update_utc = System.currentTimeMillis() / 1000;
				// 保存到数据库
				updateCookieInDatabase(act, cookie);
				// 提示文字多语言适配
				Toast.makeText(act, getLocalizedString(act, "cookie_save_success"), Toast.LENGTH_SHORT).show();
			}
		});
		builder.setNeutralButton(getLocalizedString(act, "dialog_cancel"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

	/* =========================================================
	 * 智能刷新：只移除被删除的Cookie条目
	 * ======================================================= */
	private void removeDeletedCookieFromList(final Activity act, final LinearLayout listContainer,
			final Button deleteButton, final ScrollView scrollView, final CookieItem deletedCookie) {
		// 遍历列表容器，找到并移除被删除的Cookie对应的视图
		for (int i = 0; i < listContainer.getChildCount(); i++) {
			View child = listContainer.getChildAt(i);
			// 检查是否是Cookie条目（通过Tag关联）
			if (child.getTag() instanceof CookieItem) {
				CookieItem item = (CookieItem) child.getTag();
				// 匹配被删除的Cookie
				if (itemMatchesDeleted(item, deletedCookie)) {
					// 从界面移除
					listContainer.removeViewAt(i);
					// 修复：传入 act 作为 Context 参数（act 是 Context 子类）
					updateDeleteButtonState(act, listContainer, deleteButton);
					XposedBridge.log("已从列表移除被删除的Cookie: " + item.name);
					break;
				}
			}
		}
		// 如果列表为空，显示空状态
		if (listContainer.getChildCount() == 0) {
			showEmptyCookieListState(act, listContainer);
		}
	}

	/* =========================================================
	 * 匹配被删除的Cookie项
	 * ======================================================= */
	private boolean itemMatchesDeleted(CookieItem item, CookieItem deletedCookie) {
		return item.creation_utc == deletedCookie.creation_utc && safeEquals(item.host_key, deletedCookie.host_key)
				&& safeEquals(item.name, deletedCookie.name);
	}

	/* =========================================================
	 * 安全的字符串比较
	 * ======================================================= */
	private boolean safeEquals(String str1, String str2) {
		if (str1 == null && str2 == null)
			return true;
		if (str1 == null || str2 == null)
			return false;
		return str1.equals(str2);
	}

	private void updateDeleteButtonState(Context ctx, LinearLayout listContainer, Button deleteButton) { // 新增ctx参数
		int selectedCount = 0;
		// 统计当前选中的条目数量
		for (int i = 0; i < listContainer.getChildCount(); i++) {
			View child = listContainer.getChildAt(i);
			if (child.getTag() instanceof CookieItem) {
				CookieItem item = (CookieItem) child.getTag();
				if (item.selected) {
					selectedCount++;
				}
			}
		}
		// 更新按钮状态 - 移除计数显示
		deleteButton.setEnabled(selectedCount > 0);
		deleteButton.setText(getLocalizedString(ctx, "cookie_manager_delete_selected")); // 用传入的ctx
	}

	/* =========================================================
	 * 显示空列表状态
	 * ======================================================= */
	private void showEmptyCookieListState(final Activity act, final LinearLayout listContainer) {
		listContainer.removeAllViews();
		TextView emptyText = new TextView(act);
		// 用 act 代替 ctx（Activity 是 Context 子类）
		emptyText.setText(getLocalizedString(act, "cookie_manager_empty"));
		emptyText.setTextColor(0xFF888888);
		emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		emptyText.setGravity(Gravity.CENTER);
		emptyText.setPadding(0, dp(act, 32), 0, dp(act, 32));
		listContainer.addView(emptyText);
	}

	/* =========================================================
	 * 查找Cookie列表容器（改进版）
	 * ======================================================= */
	private LinearLayout findCookieListContainer(View view) {
		// 1. 如果当前是LinearLayout，先检查自身是否包含CookieItem子视图
		if (view instanceof LinearLayout) {
			LinearLayout layout = (LinearLayout) view;
			for (int i = 0; i < layout.getChildCount(); i++) {
				View child = layout.getChildAt(i);
				if (child.getTag() instanceof CookieItem) {
					return layout; // 找到包含CookieItem的LinearLayout，直接返回
				}
			}
		}
		// 2. 递归遍历所有子视图
		if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				LinearLayout result = findCookieListContainer(viewGroup.getChildAt(i));
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/* =========================================================
	 * 查找Cookie列表的ScrollView
	 * ======================================================= */
	private ScrollView findCookieListScrollView(View view) {
		if (view instanceof ScrollView) {
			ScrollView scrollView = (ScrollView) view;
			// 验证子视图是否是Cookie列表容器
			if (scrollView.getChildCount() > 0) {
				View child = scrollView.getChildAt(0);
				if (child instanceof LinearLayout) {
					LinearLayout container = (LinearLayout) child;
					if (container.getChildCount() > 0) {
						View firstChild = container.getChildAt(0);
						if (firstChild instanceof LinearLayout && firstChild.getTag() instanceof CookieItem) {
							return scrollView;
						}
					}
				}
			}
			return null;
		}

		if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				ScrollView result = findCookieListScrollView(viewGroup.getChildAt(i));
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	// ========== 修复后的辅助方法：安全查找删除按钮 ==========
	private Button findDeleteButton(Context ctx, View view) { // 新增 ctx 参数
		// 1. 如果当前视图是Button，验证文本
		if (view instanceof Button) {
			Button button = (Button) view;
			String buttonText = button.getText().toString();
			// 匹配"删除选中"或"删除选中 (X)"格式（使用传入的ctx）
			if (buttonText.startsWith(getLocalizedString(ctx, "cookie_manager_delete_selected"))) {
				return button;
			}
			return null;
		}
		// 2. 如果是ViewGroup，递归查找子视图
		if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				Button result = findDeleteButton(ctx, viewGroup.getChildAt(i)); // 递归时传入ctx
				if (result != null) {
					return result;
				}
			}
		}
		// 3. 不是目标按钮，返回null
		return null;
	}

	/* =========================================================
	 * 格式化时间戳
	 * ======================================================= */
	private String formatTimestamp(long timestamp) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp * 1000));
		} catch (Exception e) {
			return "Time format error";
		}
	}

	/* =========================================================
	 * 获取SameSite文本描述（多语言适配）
	 * ======================================================= */
	private String getSameSiteText(Context ctx, int samesite) {
		switch (samesite) {
			case 0 :
				return getLocalizedString(ctx, "cookie_samesite_none");
			case 1 :
				return getLocalizedString(ctx, "cookie_samesite_lax");
			case 2 :
				return getLocalizedString(ctx, "cookie_samesite_strict");
			default :
				return String.format(getLocalizedString(ctx, "cookie_samesite_unknown"), samesite);
		}
	}

	/* =========================================================
	 * 获取源类型文本描述
	 * ======================================================= */
	private String getSourceTypeText(Context ctx, int sourceType) {
		switch (sourceType) {
			case 0 :
				return getLocalizedString(ctx, "cookie_source_type_none");
			case 1 :
				return getLocalizedString(ctx, "cookie_source_type_http");
			case 2 :
				return getLocalizedString(ctx, "cookie_source_type_https");
			case 3 :
				return getLocalizedString(ctx, "cookie_source_type_file");
			default :
				return String.format(getLocalizedString(ctx, "cookie_source_type_unknown"), sourceType);
		}
	}

	/* =========================================================
	 * 添加可编辑字段
	 * ======================================================= */
	private EditText addEditableField(LinearLayout parent, Context ctx, String label, String value) {
		LinearLayout rowLayout = new LinearLayout(ctx);
		rowLayout.setOrientation(LinearLayout.HORIZONTAL);
		rowLayout.setPadding(0, 0, 0, dp(ctx, 12));

		TextView labelView = new TextView(ctx);
		labelView.setText(label);
		labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		labelView.setTextColor(Color.BLACK);
		labelView.setTypeface(null, Typeface.BOLD);
		LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(dp(ctx, 120),
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayout.addView(labelView, labelParams);

		final EditText editText = new EditText(ctx);
		editText.setText(value != null ? value : "");
		editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		editText.setTextColor(Color.BLACK);
		editText.setBackground(getRoundBg(ctx, 0xFFF0F0F0, 4));
		editText.setPadding(dp(ctx, 8), dp(ctx, 6), dp(ctx, 8), dp(ctx, 6));
		editText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

		rowLayout.addView(editText);
		parent.addView(rowLayout);

		return editText;
	}

	/* =========================================================
	 * 添加只读字段
	 * ======================================================= */
	private void addReadOnlyField(LinearLayout parent, Context ctx, String label, String value) {
		LinearLayout rowLayout = new LinearLayout(ctx);
		rowLayout.setOrientation(LinearLayout.HORIZONTAL);
		rowLayout.setPadding(0, 0, 0, dp(ctx, 8));

		TextView labelView = new TextView(ctx);
		labelView.setText(label);
		labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		labelView.setTextColor(0xFF666666);
		labelView.setTypeface(null, Typeface.BOLD);
		LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(dp(ctx, 120),
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayout.addView(labelView, labelParams);

		TextView valueView = new TextView(ctx);
		valueView.setText(value != null ? value : "N/A");
		valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		valueView.setTextColor(0xFF666666);
		valueView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

		rowLayout.addView(valueView);
		parent.addView(rowLayout);
	}

	/* =========================================================
	 * 添加复选框字段
	 * ======================================================= */
	private CheckBox addCheckboxField(LinearLayout parent, Context ctx, String label, boolean checked) {
		LinearLayout rowLayout = new LinearLayout(ctx);
		rowLayout.setOrientation(LinearLayout.HORIZONTAL);
		rowLayout.setPadding(0, 0, 0, dp(ctx, 12));

		TextView labelView = new TextView(ctx);
		labelView.setText(label);
		labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		labelView.setTextColor(Color.BLACK);
		labelView.setTypeface(null, Typeface.BOLD);
		LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(dp(ctx, 120),
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayout.addView(labelView, labelParams);

		final CheckBox checkBox = new CheckBox(ctx);
		checkBox.setChecked(checked);
		checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		checkBox.setTextColor(Color.BLACK);
		checkBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

		rowLayout.addView(checkBox);
		parent.addView(rowLayout);

		return checkBox;
	}

	/* =========================================================
	 * 更新Cookie到数据库
	 * ======================================================= */
	private void updateCookieInDatabase(Context ctx, CookieItem cookie) {
		SQLiteDatabase db = null;

		try {
			String cookiePath = getCookieFilePath(ctx);
			db = SQLiteDatabase.openDatabase(cookiePath, null, SQLiteDatabase.OPEN_READWRITE);

			ContentValues values = new ContentValues();
			values.put("host_key", cookie.host_key);
			values.put("name", cookie.name);
			values.put("value", cookie.value);
			values.put("path", cookie.path);
			values.put("is_secure", cookie.is_secure ? 1 : 0);
			values.put("is_httponly", cookie.is_httponly ? 1 : 0);
			values.put("is_persistent", cookie.is_persistent ? 1 : 0);
			values.put("last_update_utc", cookie.last_update_utc);

			String whereClause = "creation_utc = ? AND host_key = ? AND name = ?";
			String[] whereArgs = {String.valueOf(cookie.creation_utc), cookie.host_key, cookie.name};

			db.update(COOKIE_TABLE_NAME, values, whereClause, whereArgs);

		} catch (Exception e) {
			XposedBridge.log("更新Cookie失败: " + e);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	/* =========================================================
	* 填充Cookie列表（支持滑动和选择 + 全多语言适配）
	* ======================================================= */
	private void populateCookieList(final Activity act, LinearLayout container, List<CookieItem> cookieItems,
			final Button deleteButton, final ScrollView scrollView) {
		container.removeAllViews();
		if (cookieItems.isEmpty()) {
			TextView emptyText = new TextView(act);
			emptyText.setText(getLocalizedString(act, "cookie_manager_empty"));
			emptyText.setTextColor(0xFF888888);
			emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			emptyText.setGravity(Gravity.CENTER);
			emptyText.setPadding(0, dp(act, 32), 0, dp(act, 32));
			container.addView(emptyText);
			deleteButton.setEnabled(false);
			return;
		}
		// 添加选择状态计数器
		final int[] selectedCount = {0};
		for (int i = 0; i < cookieItems.size(); i++) {
			final CookieItem item = cookieItems.get(i);
			// 创建Cookie项布局
			LinearLayout itemLayout = new LinearLayout(act);
			itemLayout.setOrientation(LinearLayout.VERTICAL);
			itemLayout.setBackground(getRoundBg(act, 0xFFF8F9FA, 6));
			itemLayout.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
			LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			itemLp.bottomMargin = dp(act, 8);
			container.addView(itemLayout, itemLp);
			// ========== 关联CookieItem到视图Tag ==========
			itemLayout.setTag(item);
			// ======================================================
			// 第一行：域名和选择框
			LinearLayout firstRow = new LinearLayout(act);
			firstRow.setOrientation(LinearLayout.HORIZONTAL);
			firstRow.setGravity(Gravity.CENTER_VERTICAL);
			// 选择框
			final CheckBox selectCheckbox = new CheckBox(act);
			selectCheckbox.setChecked(item.selected);
			selectCheckbox.setScaleX(0.8f);
			selectCheckbox.setScaleY(0.8f);
			firstRow.addView(selectCheckbox);
			// 域名（替换硬编码"未知域名"为多语言）
			TextView domainText = new TextView(act);
			String domain = item.host_key != null && !item.host_key.isEmpty()
					? item.host_key
					: getLocalizedString(act, "cookie_unknown_domain");
			domainText.setText(domain);
			domainText.setTextColor(Color.BLACK);
			domainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			domainText.setTypeface(null, Typeface.BOLD);
			domainText.setEllipsize(TextUtils.TruncateAt.END);
			domainText.setSingleLine(true);
			domainText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
			firstRow.addView(domainText);
			itemLayout.addView(firstRow);
			// 第二行：Cookie名称和值预览（全多语言替换）
			LinearLayout secondRow = new LinearLayout(act);
			secondRow.setOrientation(LinearLayout.VERTICAL);
			secondRow.setPadding(dp(act, 24), dp(act, 4), 0, 0);
			// Cookie名称（替换硬编码"名称: "和"未知"）
			TextView nameText = new TextView(act);
			String nameLabel = getLocalizedString(act, "cookie_field_name_label");
			String nameValue = item.name != null && !item.name.isEmpty()
					? item.name
					: getLocalizedString(act, "cookie_field_unknown");
			nameText.setText(nameLabel + nameValue);
			nameText.setTextColor(0xFF666666);
			nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
			secondRow.addView(nameText);
			// Cookie值预览（替换硬编码"值: "和"无值"）
			TextView valueText = new TextView(act);
			String valueLabel = getLocalizedString(act, "cookie_field_value_label");
			String valueRaw = item.value != null && !item.value.isEmpty()
					? item.value
					: getLocalizedString(act, "cookie_no_value");
			// 长度超过30字符时截断
			String valueDisplay = valueRaw.length() > 30 ? valueRaw.substring(0, 30) + "..." : valueRaw;
			valueText.setText(valueLabel + valueDisplay);
			valueText.setTextColor(0xFF666666);
			valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
			valueText.setPadding(0, dp(act, 2), 0, 0);
			secondRow.addView(valueText);
			itemLayout.addView(secondRow);
			// 点击事件 - 显示详情
			itemLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showCookieDetailDialog(act, item);
				}
			});
			// 长按事件 - 选择/取消选择
			itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					item.selected = !item.selected;
					selectCheckbox.setChecked(item.selected);
					selectedCount[0] += item.selected ? 1 : -1;
					// 更新删除按钮状态（多语言文本）
					deleteButton.setEnabled(selectedCount[0] > 0);
					deleteButton.setText(getLocalizedString(act, "cookie_manager_delete_selected"));
					return true;
				}
			});
			// 选择框的点击事件
			selectCheckbox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					item.selected = selectCheckbox.isChecked();
					selectedCount[0] += item.selected ? 1 : -1;
					// 更新删除按钮状态（多语言文本）
					deleteButton.setEnabled(selectedCount[0] > 0);
					deleteButton.setText(getLocalizedString(act, "cookie_manager_delete_selected"));
				}
			});
		}
		// 滚动到顶部
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.scrollTo(0, 0);
			}
		});
	}

	/* =========================================================
	 * 显示删除确认对话框
	 * ======================================================= */
	private void showDeleteConfirmDialog(final Activity act, final Context ctx, final LinearLayout listContainer,
			final Button deleteButton, final ScrollView scrollView) {
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setTitle(getLocalizedString(ctx, "cookie_delete_confirm_title"));
		builder.setMessage(getLocalizedString(ctx, "cookie_delete_confirm_msg"));

		builder.setPositiveButton(getLocalizedString(ctx, "cookie_manager_delete_btn"),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 执行删除操作
						deleteSelectedCookies(act, listContainer, deleteButton, scrollView);
					}
				});

		builder.setNegativeButton(getLocalizedString(ctx, "dialog_cancel"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.show();
	}

	/* =========================================================
	 * 删除选中的Cookie
	 * ======================================================= */
	private void deleteSelectedCookies(final Activity act, final LinearLayout listContainer, final Button deleteButton,
			final ScrollView scrollView) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SQLiteDatabase db = null;
				final List<CookieItem> deletedItems = new ArrayList<>();

				try {
					String cookiePath = getCookieFilePath(act);
					db = SQLiteDatabase.openDatabase(cookiePath, null, SQLiteDatabase.OPEN_READWRITE);
					db.beginTransaction();

					try {
						// 收集要删除的Cookie项
						for (int i = 0; i < listContainer.getChildCount(); i++) {
							View child = listContainer.getChildAt(i);
							if (child instanceof LinearLayout && child.getTag() instanceof CookieItem) {
								CookieItem item = (CookieItem) child.getTag();
								if (item.selected) {
									// 从数据库删除
									String whereClause = "creation_utc = ? AND host_key = ? AND name = ?";
									String[] whereArgs = {String.valueOf(item.creation_utc), item.host_key, item.name};
									db.delete(COOKIE_TABLE_NAME, whereClause, whereArgs);
									deletedItems.add(item);
								}
							}
						}
						db.setTransactionSuccessful();
					} finally {
						db.endTransaction();
					}

				} catch (Exception e) {
					XposedBridge.log("批量删除Cookie失败: " + e);
				} finally {
					if (db != null) {
						db.close();
					}
				}

				final int finalCount = deletedItems.size();
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (finalCount > 0) {
							// 逐个移除被删除的条目
							for (CookieItem deletedItem : deletedItems) {
								removeDeletedCookieFromList(act, listContainer, deleteButton, scrollView, deletedItem);
							}

							Toast.makeText(act, "已删除选中的Cookie", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(act, "没有选中要删除的Cookie", Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}).start();
	}
	/* ================== 编辑器状态缓存 ================== */
	private static Map<String, EditorState> editorStateCache = new HashMap<>();

	private static class EditorState {
		String content;
		int scrollY;
		long timestamp;

		EditorState(String content, int scrollY) {
			this.content = content;
			this.scrollY = scrollY;
			this.timestamp = System.currentTimeMillis();
		}
	}

	/* ================== 修改 loadFileContent 方法 ================== */
	private void loadFileContent(final Context ctx, final String fileName, final EditText editor,
			final ScrollView scrollView, final boolean fromCache) {
		// 如果从缓存加载且缓存存在
		if (fromCache && editorStateCache.containsKey(fileName)) {
			final EditorState state = editorStateCache.get(fileName);
			editor.setText(state.content);
			// 延迟设置滚动位置，确保文本已加载完成
			editor.post(new Runnable() {
				@Override
				public void run() {
					if (scrollView != null) {
						scrollView.scrollTo(0, state.scrollY);
					}
				}
			});
			return;
		}

		// 否则从文件加载
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String filePath = "/data/user/0/" + ctx.getPackageName() + "/files/" + fileName;
					File file = new File(filePath);
					final StringBuilder content = new StringBuilder();

					if (file.exists()) {
						BufferedReader reader = new BufferedReader(new FileReader(file));
						String line;
						while ((line = reader.readLine()) != null) {
							content.append(line).append("\n");
						}
						reader.close();
					} else {
						content.append("// 文件不存在，将创建新文件\n");
					}

					((Activity) ctx).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							editor.setText(content.toString());
							// 保存到缓存
							editorStateCache.put(fileName, new EditorState(content.toString(), 0));
						}
					});

				} catch (final Exception e) {
					((Activity) ctx).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							editor.setText("加载失败: " + e.getMessage());
						}
					});
				}
			}
		}).start();
	}

	/* ================== 保存当前编辑状态到缓存 ================== */
	private void saveCurrentEditorState(String fileName, EditText editor, ScrollView scrollView) {
		if (editor != null && scrollView != null) {
			String content = editor.getText().toString();
			int scrollY = scrollView.getScrollY();
			editorStateCache.put(fileName, new EditorState(content, scrollY));
		}
	}
	/* =========================================================
	 * 保存文件内容
	 * ======================================================= */
	private void saveFileContent(final Context ctx, final String fileName, final String content) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String filePath = "/data/user/0/" + ctx.getPackageName() + "/files/" + fileName;
					File file = new File(filePath);

					FileWriter writer = new FileWriter(file);
					writer.write(content);
					writer.close();

				} catch (Exception e) {
					XposedBridge.log("保存文件失败: " + e);
				}
			}
		}).start();
	}

	/* =========================================================
	 * 加载Cookie数据
	 * ======================================================= */
	private List<CookieItem> loadCookieData(Context ctx) {
		List<CookieItem> cookieItems = new ArrayList<CookieItem>();
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			String cookiePath = getCookieFilePath(ctx);
			File cookieFile = new File(cookiePath);

			if (!cookieFile.exists()) {
				return cookieItems;
			}

			db = SQLiteDatabase.openDatabase(cookiePath, null, SQLiteDatabase.OPEN_READONLY);

			// 检查表是否存在
			Cursor tableCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
					new String[]{COOKIE_TABLE_NAME});
			if (!tableCursor.moveToFirst()) {
				tableCursor.close();
				return cookieItems;
			}
			tableCursor.close();

			// 查询数据
			cursor = db.query(COOKIE_TABLE_NAME, null, null, null, null, null, "host_key, name");

			if (cursor != null && cursor.moveToFirst()) {
				do {
					try {
						CookieItem item = new CookieItem();

						// 安全地读取字段
						item.creation_utc = getLongSafe(cursor, "creation_utc");
						item.host_key = getStringSafe(cursor, "host_key");
						item.name = getStringSafe(cursor, "name");
						item.value = getStringSafe(cursor, "value");
						item.path = getStringSafe(cursor, "path");
						item.expires_utc = getLongSafe(cursor, "expires_utc");
						item.is_secure = getIntSafe(cursor, "is_secure") == 1;
						item.is_httponly = getIntSafe(cursor, "is_httponly") == 1;
						item.last_access_utc = getLongSafe(cursor, "last_access_utc");
						item.is_persistent = getIntSafe(cursor, "is_persistent") == 1;

						cookieItems.add(item);
					} catch (Exception e) {
						// 跳过错误的记录
					}
				} while (cursor.moveToNext());
			}

		} catch (Exception e) {
			XposedBridge.log("读取Cookie数据失败: " + e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return cookieItems;
	}

	/* =========================================================
	 * 安全的数据库字段读取方法
	 * ======================================================= */
	private String getStringSafe(Cursor cursor, String columnName) {
		try {
			int columnIndex = cursor.getColumnIndex(columnName);
			if (columnIndex == -1)
				return "";
			return cursor.getString(columnIndex);
		} catch (Exception e) {
			return "";
		}
	}

	private long getLongSafe(Cursor cursor, String columnName) {
		try {
			int columnIndex = cursor.getColumnIndex(columnName);
			if (columnIndex == -1)
				return 0;
			return cursor.getLong(columnIndex);
		} catch (Exception e) {
			return 0;
		}
	}

	private int getIntSafe(Cursor cursor, String columnName) {
		try {
			int columnIndex = cursor.getColumnIndex(columnName);
			if (columnIndex == -1)
				return 0;
			return cursor.getInt(columnIndex);
		} catch (Exception e) {
			return 0;
		}
	}

	/* =========================================================
	 * Cookie数据项类（完整版本）
	 * ======================================================= */
	private static class CookieItem {
		// 基本字段
		long creation_utc;
		String host_key;
		String name;
		String value;
		String path;
		long expires_utc;
		boolean is_secure;
		boolean is_httponly;
		long last_access_utc;
		boolean is_persistent;

		// 新增字段
		String top_frame_site_key;
		String encrypted_value;
		boolean has_expires;
		int priority;
		int samesite;
		int source_scheme;
		int source_port;
		long last_update_utc; // 添加缺失的字段
		int source_type;
		boolean has_cross_site_ancestor;

		// UI相关字段
		boolean selected;

		CookieItem() {
			this.selected = false;
			this.creation_utc = 0;
			this.expires_utc = 0;
			this.last_access_utc = 0;
			this.last_update_utc = 0; // 初始化
			this.is_secure = false;
			this.is_httponly = false;
			this.is_persistent = false;
			this.has_expires = false;
			this.priority = 0;
			this.samesite = 0;
			this.source_scheme = 0;
			this.source_port = 0;
			this.source_type = 0;
			this.has_cross_site_ancestor = false;

			// 字符串字段初始化为空字符串
			this.host_key = "";
			this.name = "";
			this.value = "";
			this.path = "";
			this.top_frame_site_key = "";
			this.encrypted_value = "";
		}
	}

	/* =========================================================
	 * 获取Cookie文件路径
	 * ======================================================= */
	private String getCookieFilePath(Context ctx) {
		String packageName = ctx.getPackageName();
		return "/data/user/0/" + packageName + "/app_webview/Default/Cookies";
	}

	/* =========================================================
	 * 资源界面美化 入口
	 * ======================================================= */
	private void addImagePickerItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(getLocalizedString(ctx, "homepage_bg_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		TextView configBtn = new TextView(ctx);
		configBtn.setText(getLocalizedString(ctx, "homepage_bg_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHomepageBeautyDialog(ctx);
			}
		});
		hor.addView(configBtn);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(getLocalizedString(ctx, "homepage_bg_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	private void showHomepageBeautyDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				/* 读取保存的值 - 现在分开存储透明度和RGB */
				homepageBgPath = getPrefString(ctx, KEY_HOMEPAGE_BG, "");
				homepageMaskAlpha = getPrefInt(ctx, KEY_HOMEPAGE_MASK_A, 120);
				// 只存储RGB部分，不包含透明度
				int savedRgb = getPrefInt(ctx, KEY_HOMEPAGE_MASK_C, 0x000000);

				final Dialog dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);

				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);

				/* 大标题 */
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "homepage_bg_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(Color.BLACK);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);

				/* 副标题 */
				TextView subtitle = new TextView(act);
				subtitle.setText(getLocalizedString(ctx, "homepage_bg_dialog_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 16));
				root.addView(subtitle);

				/* 预览区域容器 */
				final FrameLayout previewContainer = new FrameLayout(act);
				LinearLayout.LayoutParams preLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						dp(act, 150));
				preLp.bottomMargin = dp(act, 16);
				previewContainer.setLayoutParams(preLp);
				GradientDrawable preBg = new GradientDrawable();
				preBg.setColor(0xFFF5F5F5);
				preBg.setStroke(dp(act, 1), 0xFFE0E0E0);
				preBg.setCornerRadius(dp(act, 12));
				previewContainer.setBackground(preBg);

				final ImageView imageView = new ImageView(act);
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				final View maskView = new View(act);
				maskView.setClickable(false);
				previewContainer.addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				previewContainer.addView(maskView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));

				root.addView(previewContainer);

				/* 刷新预览 - 使用合并后的颜色 */
				refreshPreview(ctx, imageView, maskView, homepageMaskAlpha, savedRgb);

				/* 选择图片按钮 */
				Button pickBtn = new Button(act);
				pickBtn.setText(getLocalizedString(ctx, "homepage_bg_pick_btn"));
				pickBtn.setTextColor(Color.WHITE);
				pickBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				pickBtn.setTypeface(null, Typeface.BOLD);
				pickBtn.setBackground(getRoundBg(act, 0xFF6200EE, 12));
				root.addView(pickBtn);

				/* 遮罩透明度调节 */
				TextView alphaTitle = new TextView(act);
				alphaTitle.setText(getLocalizedString(ctx, "homepage_bg_mask_alpha"));
				alphaTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				alphaTitle.setTextColor(Color.BLACK);
				alphaTitle.setTypeface(null, Typeface.BOLD);
				alphaTitle.setPadding(0, dp(act, 16), 0, 0);
				root.addView(alphaTitle);

				final SeekBar alphaSeek = new SeekBar(act);
				alphaSeek.setMax(255);
				alphaSeek.setProgress(homepageMaskAlpha);
				root.addView(alphaSeek);

				/* 遮罩颜色调节（RGB输入框） */
				TextView colorTitle = new TextView(act);
				colorTitle.setText(getLocalizedString(ctx, "homepage_bg_mask_color_rgb"));
				colorTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				colorTitle.setTextColor(Color.BLACK);
				colorTitle.setTypeface(null, Typeface.BOLD);
				colorTitle.setPadding(0, dp(act, 12), 0, 0);
				root.addView(colorTitle);

				// RGB输入框容器 - 移除确认按钮
				LinearLayout rgbContainer = new LinearLayout(act);
				rgbContainer.setOrientation(LinearLayout.VERTICAL);

				final EditText rgbEdit = new EditText(act);
				rgbEdit.setHint("#RRGGBB");
				rgbEdit.setText(colorToRgbString(savedRgb)); // 只显示RGB部分
				rgbEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				rgbEdit.setBackground(getRoundBg(act, 0xFFF5F5F5, 4));
				rgbEdit.setPadding(dp(act, 8), dp(act, 8), dp(act, 8), dp(act, 8));
				LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				rgbContainer.addView(rgbEdit, editLp);

				// 移除确认按钮，添加输入提示
				TextView rgbHint = new TextView(act);
				rgbHint.setText(getLocalizedString(ctx, "homepage_bg_mask_color_hint"));
				rgbHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				rgbHint.setTextColor(0xFF888888);
				rgbHint.setPadding(dp(act, 4), dp(act, 4), 0, 0);
				rgbContainer.addView(rgbHint);

				root.addView(rgbContainer);

				/* 实时预览监听 */
				SeekBar.OnSeekBarChangeListener alphaListener = new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if (fromUser) {
							homepageMaskAlpha = progress;
							// 实时更新预览
							String rgbStr = rgbEdit.getText().toString();
							int rgbColor = parseRgbColor(rgbStr, 0); // 解析时不带透明度
							refreshPreview(ctx, imageView, maskView, homepageMaskAlpha, rgbColor);
						}
					}
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				};
				alphaSeek.setOnSeekBarChangeListener(alphaListener);

				// RGB输入框文本变化监听
				rgbEdit.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						String rgbStr = s.toString();
						int rgbColor = parseRgbColor(rgbStr, 0); // 解析时不带透明度
						// 实时更新预览
						refreshPreview(ctx, imageView, maskView, homepageMaskAlpha, rgbColor);
					}
				});

				/* 确定/取消 */
				LinearLayout btnRow = new LinearLayout(act);
				btnRow.setOrientation(LinearLayout.HORIZONTAL);
				btnRow.setGravity(Gravity.CENTER);
				btnRow.setPadding(0, dp(act, 24), 0, 0);

				Button cancel = new Button(act);
				cancel.setText(getLocalizedString(ctx, "dialog_cancel"));
				cancel.setTextColor(0xFF6200EE);
				cancel.setBackground(getRoundBg(act, 0xFFE0E0E0, 12));
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setBackground(getRoundBg(act, 0xFF6200EE, 12));

				LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1);
				btnLp.rightMargin = dp(act, 8);
				btnRow.addView(cancel, btnLp);
				btnLp.leftMargin = dp(act, 8);
				btnRow.addView(ok, btnLp);

				root.addView(btnRow);
				scrollRoot.addView(root);
				dialog.setContentView(scrollRoot);

				Window win = dialog.getWindow();
				if (win != null) {
					win.setBackgroundDrawableResource(android.R.color.transparent);
					GradientDrawable round = new GradientDrawable();
					round.setColor(Color.WHITE);
					round.setCornerRadius(dp(act, 24));
					win.setBackgroundDrawable(round);
					win.setGravity(Gravity.CENTER);
					win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				}

				/* 事件 */
				cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 验证颜色格式
						String rgbStr = rgbEdit.getText().toString();
						int rgbColor = parseRgbColor(rgbStr, 0);

						// 检查颜色是否有效 - 修复验证逻辑
						if (rgbStr.trim().length() > 0 && !isValidRgbColor(rgbStr)) {
							// 颜色格式无效
							Toast.makeText(ctx, getLocalizedString(ctx, "homepage_bg_mask_color_invalid"),
									Toast.LENGTH_SHORT).show();
							return; // 阻止对话框关闭
						}

						// 保存设置 - 分开存储透明度和RGB
						putPrefInt(ctx, KEY_HOMEPAGE_MASK_A, homepageMaskAlpha);
						putPrefInt(ctx, KEY_HOMEPAGE_MASK_C, rgbColor); // 只保存RGB部分

						Toast.makeText(ctx, getLocalizedString(ctx, "homepage_bg_saved"), Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				});

				pickBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(Intent.ACTION_GET_CONTENT);
						i.setType("image/*");
						i.addCategory(Intent.CATEGORY_OPENABLE);
						act.startActivityForResult(
								Intent.createChooser(i, getLocalizedString(ctx, "homepage_bg_pick_title")), 0x1002);
					}
				});

				dialog.show();
			}
		});
	}

	/**
	* 验证RGB颜色字符串格式是否有效
	*/
	private boolean isValidRgbColor(String rgbStr) {
		if (rgbStr == null || rgbStr.trim().isEmpty()) {
			return true; // 空字符串是允许的（使用默认值）
		}

		String colorStr = rgbStr.trim();
		if (!colorStr.startsWith("#")) {
			colorStr = "#" + colorStr;
		}

		// 验证格式 #RGB
		return colorStr.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
	}

	/* =========================================================
	 * 刷新预览（使用正确的颜色格式）
	 * ======================================================= */
	private void refreshPreview(Context ctx, ImageView iv, View mask, int alpha, int rgbColor) {
		// 设置背景图片
		if (homepageBgPath != null && new File(homepageBgPath).exists()) {
			Bitmap bmp = BitmapFactory.decodeFile(homepageBgPath);
			if (bmp != null) {
				iv.setImageBitmap(bmp);
			} else {
				iv.setBackgroundColor(0xFFD0D0D0);
				iv.setImageBitmap(null);
			}
		} else {
			iv.setBackgroundColor(0xFFD0D0D0);
			iv.setImageBitmap(null);
		}

		// 设置遮罩颜色（Android使用的是ARGB格式）
		int finalColor = (alpha << 24) | (rgbColor & 0x00FFFFFF);
		mask.setBackgroundColor(finalColor);
	}

	private void hookHomepageBgWithMask(final Context ctx, ClassLoader cl, final String imgPath, final int maskColor) {
		XposedHelpers.findAndHookMethod("k.a.a0.l.c", cl, "g", Context.class, List.class, boolean.class,
				new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						String uri = (String) param.getResult();
						File html = new File(Uri.parse(uri).getPath());
						if (!html.exists())
							return;

						StringBuilder htmlSb = new StringBuilder();
						BufferedReader br = null;
						try {
							br = new BufferedReader(new FileReader(html));
							String line;
							while ((line = br.readLine()) != null)
								htmlSb.append(line).append("\n");
						} finally {
							if (br != null)
								try {
									br.close();
								} catch (Exception ignored) {
								}
						}

						String originalHtml = htmlSb.toString();

						// 获取分开存储的透明度和颜色
						int alpha = getPrefInt(ctx, KEY_HOMEPAGE_MASK_A, 120);
						int rgbColor = getPrefInt(ctx, KEY_HOMEPAGE_MASK_C, 0x000000);

						// 调试信息
						XposedBridge.log("BetterVia: Alpha: " + alpha + ", RGB: " + Integer.toHexString(rgbColor));

						// 使用正确的颜色格式
						String cssColor = colorToCssString(alpha, rgbColor);
						// 或者使用 RGBA 格式（推荐，更兼容）
						// String cssColor = colorToRgbaString(alpha, rgbColor);

						XposedBridge.log("BetterVia: CSS Color: " + cssColor);

						// 构建背景样式
						String backgroundStyle;
						if (imgPath != null && !imgPath.isEmpty() && new File(imgPath).exists()) {
							String encodedPath = imgPath.replace("'", "\\'").replace("\\", "\\\\");
							backgroundStyle = "background:url('file:///" + encodedPath
									+ "') no-repeat center/cover fixed;";
						} else {
							backgroundStyle = "background-color:#F0F0F0;";
						}

						// 使用正确的颜色格式
						String maskStyle = "background:" + cssColor + ";";

						String newBodyContent = "<body style=\"" + backgroundStyle + "\">"
								+ "<div style='position:fixed;top:0;left:0;right:0;bottom:0;" + maskStyle
								+ "z-index:0;'></div>" + "<div style='position:relative;z-index:1;'>";

						String modifiedHtml = originalHtml.replace("<body>", newBodyContent).replace("</body>",
								"</div></body>");

						FileWriter fw = null;
						try {
							fw = new FileWriter(html);
							fw.write(modifiedHtml);
							XposedBridge.log("BetterVia: Successfully applied background with correct color format");
						} catch (Exception e) {
							XposedBridge.log("BetterVia: Error writing modified HTML: " + e);
						} finally {
							if (fw != null)
								try {
									fw.close();
								} catch (Exception ignored) {
								}
						}
					}
				});
	}

	/* =========================================================
	 * 统一处理ActivityResult（旧IO，无lambda）
	 * ======================================================= */
	private void handleActivityResult(int requestCode, int resultCode, Intent data, final Activity activity) {
		if (resultCode != Activity.RESULT_OK || data == null)
			return;

		if (requestCode == 0x1002) {
			/* 资源界面美化背景图 */
			Uri uri = data.getData();
			if (saveUserImage(activity, uri)) {
				homepageBgPath = getPrefString(activity, KEY_HOMEPAGE_BG, "");

				// 强制刷新预览
				if (Context != null && Context instanceof Activity) {
					((Activity) Context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// 这里可以添加逻辑来刷新打开的对话框
							Toast.makeText(activity, getLocalizedString(activity, "homepage_bg_set_ok"),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}
	}

	/* =========================================================
	 * 保存用户选择的背景图（旧IO，Java7）
	 * ======================================================= */
	private boolean saveUserImage(Activity act, Uri uri) {
		if (uri == null)
			return false;
		InputStream in = null;
		FileOutputStream out = null;
		try {
			File outFile = new File(act.getFilesDir(), "homepage_bg.jpg");
			in = act.getContentResolver().openInputStream(uri);
			out = new FileOutputStream(outFile);
			byte[] buf = new byte[8192];
			int len;
			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
			//记录路径
			homepageBgPath = outFile.getAbsolutePath();
			putPrefString(act, KEY_HOMEPAGE_BG, homepageBgPath);
			return true;
		} catch (Exception e) {
			XposedBridge.log("saveUserImage error: " + e);
			return false;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception ignored) {
				}
			if (out != null)
				try {
					out.close();
				} catch (Exception ignored) {
				}
		}
	}

	/**
	 * 颜色值转RGB字符串
	 */
	/**
	 * 将 Android 颜色值转换为 CSS rgba() 字符串
	 */
	private String colorToCssString(int alpha, int rgbColor) {
		int r = (rgbColor >> 16) & 0xFF;
		int g = (rgbColor >> 8) & 0xFF;
		int b = rgbColor & 0xFF;
		float alphaFloat = alpha / 255.0f;
		return String.format(Locale.US, "rgba(%d, %d, %d, %.2f)", r, g, b, alphaFloat);
	}

	/**
	 * 解析用户输入的 RGB 颜色字符串（#RRGGBB）
	 */
	private int parseRgbColor(String rgbStr, int defaultAlpha) {
		if (rgbStr == null || rgbStr.trim().isEmpty()) {
			return 0xFFFFFF; // 默认白色
		}

		String colorStr = rgbStr.trim();
		if (!colorStr.startsWith("#")) {
			colorStr = "#" + colorStr;
		}

		try {
			if (colorStr.length() == 7) { // #RRGGBB
				return Color.parseColor(colorStr) & 0x00FFFFFF;
			}
		} catch (Exception e) {
			// 解析失败返回白色
		}
		return 0xFFFFFF; // 默认白色
	}

	/**
	* 颜色值转RGB字符串（只显示RGB部分）
	*/
	private String colorToRgbString(int color) {
		// 只显示RGB部分，去掉透明度
		return String.format("#%06X", color & 0x00FFFFFF);
	}

	/* =========================================================
	* 添加浏览器标识设置项
	* ======================================================= */
	private void addUserAgentItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));

		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);

		TextView tv = new TextView(ctx);
		tv.setText(getLocalizedString(ctx, "user_agent_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		// 配置按钮
		TextView configBtn = new TextView(ctx);
		configBtn.setText(getLocalizedString(ctx, "user_agent_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showUserAgentDialog(ctx);
			}
		});
		hor.addView(configBtn);

		TextView hintTv = new TextView(ctx);
		hintTv.setText(getLocalizedString(ctx, "user_agent_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);

		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}

	/* =========================================================
	 * 显示浏览器标识对话框
	 * ======================================================= */
	private void showUserAgentDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				final Dialog dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);

				// 根容器
				FrameLayout dialogContainer = new FrameLayout(act);
				GradientDrawable containerBg = new GradientDrawable();
				containerBg.setColor(Color.WHITE);
				containerBg.setCornerRadius(dp(act, 24));
				dialogContainer.setBackground(containerBg);

				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(0, 0, 0, 0);

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));

				// 标题
				TextView title = new TextView(act);
				title.setText(getLocalizedString(ctx, "user_agent_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);

				// 副标题
				TextView subtitle = new TextView(act);
				subtitle.setText(getLocalizedString(ctx, "user_agent_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 16));
				root.addView(subtitle);

				// UA列表容器
				final LinearLayout uaContainer = new LinearLayout(act);
				uaContainer.setOrientation(LinearLayout.VERTICAL);
				uaContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				root.addView(uaContainer);

				// 确定按钮
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				ok.setPadding(0, dp(act, 14), 0, dp(act, 14));
				ok.setBackground(getRoundBg(act, 0xFF6200EE, 12));

				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				okLp.topMargin = dp(act, 16);
				root.addView(ok, okLp);

				scrollRoot.addView(root);
				dialogContainer.addView(scrollRoot);
				dialog.setContentView(dialogContainer);

				// 设置对话框窗口属性
				Window window = dialog.getWindow();
				if (window != null) {
					window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					DisplayMetrics metrics = new DisplayMetrics();
					act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int width = (int) (metrics.widthPixels * 0.9);
					WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
					layoutParams.copyFrom(window.getAttributes());
					layoutParams.width = width;
					layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
					layoutParams.gravity = Gravity.CENTER;
					window.setAttributes(layoutParams);
					window.setClipToOutline(true);
				}

				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				// 填充UA列表
				populateUserAgentList(act, ctx, uaContainer);

				dialog.show();
			}
		});
	}

	/* =========================================================
	 * 填充浏览器标识列表
	 * ======================================================= */
	private void populateUserAgentList(final Activity act, final Context ctx, LinearLayout container) {
		container.removeAllViews();

		// 获取个性化UA列表
		List<UserAgentInfo> uaList = getPersonalizedUserAgents(act);

		for (final UserAgentInfo uaInfo : uaList) {
			// 每个UA的容器
			LinearLayout uaItem = new LinearLayout(act);
			uaItem.setOrientation(LinearLayout.VERTICAL);
			uaItem.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));

			GradientDrawable itemBg = new GradientDrawable();
			itemBg.setColor(0xFFF8F9FA);
			itemBg.setStroke(dp(act, 1), 0xFFE9ECEF);
			itemBg.setCornerRadius(dp(act, 12));
			uaItem.setBackground(itemBg);

			LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			itemLp.bottomMargin = dp(act, 8);
			container.addView(uaItem, itemLp);

			// 浏览器名称
			TextView browserName = new TextView(act);
			browserName.setText(uaInfo.browserName);
			browserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			browserName.setTextColor(Color.BLACK);
			browserName.setTypeface(null, Typeface.BOLD);
			uaItem.addView(browserName);

			// UA文本（单行显示，中间省略）
			final TextView uaText = new TextView(act);
			uaText.setText(uaInfo.userAgent);
			uaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			uaText.setTextColor(0xFF666666);
			uaText.setSingleLine(true);
			uaText.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			uaText.setPadding(0, dp(act, 8), 0, dp(act, 8));
			uaItem.addView(uaText);

			// 复制按钮
			Button copyBtn = new Button(act);
			copyBtn.setText(getLocalizedString(ctx, "user_agent_copy"));
			copyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
			copyBtn.setTextColor(Color.WHITE);
			copyBtn.setPadding(dp(act, 12), dp(act, 4), dp(act, 12), dp(act, 4));
			copyBtn.setMinHeight(dp(act, 28));

			// 按钮背景
			GradientDrawable btnBg = new GradientDrawable();
			btnBg.setColor(0xFF3498DB);
			btnBg.setCornerRadius(dp(act, 6));
			copyBtn.setBackground(btnBg);

			// 设置按钮宽度
			LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			btnLp.gravity = Gravity.END;
			uaItem.addView(copyBtn, btnLp);

			// 复制按钮点击事件
			copyBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					copyToClipboard(act, uaInfo.userAgent);
					Toast.makeText(act, getLocalizedString(ctx, "user_agent_copied"), Toast.LENGTH_SHORT).show();
				}
			});

			// 点击整个项也可以复制
			uaItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					copyToClipboard(act, uaInfo.userAgent);
					Toast.makeText(act, getLocalizedString(ctx, "user_agent_copied"), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	/* =========================================================
	 * UA信息类
	 * ======================================================= */
	private static class UserAgentInfo {
		String browserName;
		String userAgent;

		UserAgentInfo(String browserName, String userAgent) {
			this.browserName = browserName;
			this.userAgent = userAgent;
		}
	}

	/* =========================================================
	 * 获取个性化UA列表
	 * ======================================================= */
	private List<UserAgentInfo> getPersonalizedUserAgents(Context ctx) {
		List<UserAgentInfo> uaList = new ArrayList<>();

		// 获取设备信息用于个性化
		String deviceModel = Build.MODEL;
		String androidVersion = "Android " + Build.VERSION.RELEASE;
		String buildVersion = Build.DISPLAY;

		// 如果buildVersion为空，使用默认值
		if (buildVersion == null || buildVersion.isEmpty()) {
			buildVersion = "PKQ1.181007.001";
		}

		// 个性化UA模板
		String[] uaTemplates = {
				"百度: Mozilla/5.0 (Linux; {android_version}; {device_model} Build/{build_version}; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/97.0.4692.98 Mobile Safari/537.36 T7/13.59 SP-engine/2.98.0 baiduboxapp/13.59.0.10 (Baidu; P1 12) NABar/1.0",
				"小米浏览器: Mozilla/5.0 (Linux; U; {android_version}; zh_CN; {device_model} Build/{build_version}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.119 Mobile Safari/537.36 XiaoMi/MiuiBrowser/19.2.820324",
				"华为浏览器: Mozilla/5.0 (Linux; {android_version}; HarmonyOS; {device_model}; HMSCore 5.3.0.312) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.93 HuaweiBrowser/11.1.1.310 Mobile Safari/537.36",
				"UC浏览器: Mozilla/5.0 (Linux; U; {android_version}; zh_CN; {device_model} Build/{build_version}) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/100.0.4896.58 UCBrowser/17.5.0.1381 Mobile Safari/537.36",
				"Edge浏览器: Mozilla/5.0 (Linux; {android_version}; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36 EdgA/134.0.0.0",
				"QQ浏览器: Mozilla/5.0 (Linux; U; {android_version}; zh_CN; {device_model} Build/{build_version}) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/109.0.5414.86 MQQBrowser/16.1 Mobile Safari/537.36 COVC/046915",
				"夸克浏览器: Mozilla/5.0 (Linux; U; {android_version}; zh_CN; {device_model} Build/{build_version}) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/100.0.4896.58 Quark/7.9.6.781 Mobile Safari/537.36",
				"360浏览器: Mozilla/5.0 (Linux; {android_version}; {device_model} Build/{build_version}; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/62.0.3202.97 Mobile Safari/537.36",
				"简单搜索: Mozilla/5.0 (Linux; {android_version}; {device_model} Build/{build_version}; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 danSearchCraft Chrome/76.0.3809.89 Mobile Safari/537.36",
				"Chrome: Mozilla/5.0 (Linux; {android_version}; {device_model}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.111 Mobile Safari/537.36",
				"微信: Mozilla/5.0 (Linux; {android_version}; {device_model} Build/{build_version}; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/78.0.3904.62 XWEB/2893 MMWEBSDK/20210601 Mobile Safari/537.36 MMWEBID/9453 MicroMessenger/8.0.9.1940(0x28000951) Process/toolsmp WeChat/arm64 Weixin NetType/4G Language/zh_CN ABI/arm64",
				"iPhone: Mozilla/5.0 (iPhone; CPU iPhone OS 18_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.4 Mobile/15E148 Safari/604.1",
				"淘宝浏览器: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.11 TaoBrowser/2.0 Safari/536.11"};

		for (String template : uaTemplates) {
			String[] parts = template.split(": ", 2);
			if (parts.length == 2) {
				String browserName = parts[0];
				String uaTemplate = parts[1];

				// 个性化替换
				String personalizedUA = uaTemplate.replace("{android_version}", androidVersion)
						.replace("{device_model}", deviceModel).replace("{build_version}", buildVersion);

				uaList.add(new UserAgentInfo(browserName, personalizedUA));
			}
		}

		return uaList;
	}

	/* =========================================================
	 * 持久化工具
	 * ======================================================= */
	private boolean getPrefBoolean(Context ctx, String key, boolean def) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			return (boolean) XposedHelpers.callMethod(sp, "getBoolean", key, def);
		} catch (Exception e) {
			return def;
		}
	}

	private void putPrefBoolean(Context ctx, String key, boolean value) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			Object ed = XposedHelpers.callMethod(sp, "edit");
			XposedHelpers.callMethod(ed, "putBoolean", key, value);
			XposedHelpers.callMethod(ed, "apply");
		} catch (Exception e) {
			XposedBridge.log("putBoolean 失败: " + e);
		}
	}

	private void saveLanguageSetting(Context ctx, String lang) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			Object ed = XposedHelpers.callMethod(sp, "edit");
			XposedHelpers.callMethod(ed, "putString", "preferred_language", lang);
			XposedHelpers.callMethod(ed, "apply");
		} catch (Exception e) {
			XposedBridge.log("保存语言失败: " + e);
		}
	}

	private String getSavedLanguage(Context ctx) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			return (String) XposedHelpers.callMethod(sp, "getString", "preferred_language", "auto");
		} catch (Exception e) {
			return "auto";
		}
	}

	/* =========================================================
	* 字符串持久化工具
	* ======================================================= */
	private void putPrefString(Context ctx, String key, String value) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			Object ed = XposedHelpers.callMethod(sp, "edit");
			XposedHelpers.callMethod(ed, "putString", key, value);
			XposedHelpers.callMethod(ed, "apply");
		} catch (Exception e) {
			XposedBridge.log("putString 失败: " + e);
		}
	}

	private String getPrefString(Context ctx, String key, String def) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			return (String) XposedHelpers.callMethod(sp, "getString", key, def);
		} catch (Exception e) {
			return def;
		}
	}

	/* =========================================================
	* 静态方法：获取保存的语言设置
	* ======================================================= */
	private static String getSavedLanguageStatic(Context ctx) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			return (String) XposedHelpers.callMethod(sp, "getString", "preferred_language", "auto");
		} catch (Exception e) {
			return "auto";
		}
	}

	/* =========================================================
	* 复制文本到剪贴板
	* ======================================================= */
	private void copyToClipboard(Context ctx, String text) {
		try {
			ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Via Command", text);
			clipboard.setPrimaryClip(clip);
		} catch (Exception e) {
			XposedBridge.log("复制到剪贴板失败: " + e);
		}
	}

	private GradientDrawable getRoundBg(Context ctx, int color, int radiusDp) {
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(color);
		gd.setCornerRadius(dp(ctx, radiusDp));
		return gd;
	}

	private void updateViaLocale(Context ctx, String lang) {
		try {
			Locale newLoc;
			switch (lang) {
				case "zh-CN" :
					newLoc = Locale.SIMPLIFIED_CHINESE;
					break;
				case "zh-TW" :
					newLoc = Locale.TRADITIONAL_CHINESE;
					break;
				case "en" :
					newLoc = Locale.ENGLISH;
					break;
				default :
					newLoc = Locale.getDefault();
					break;
			}
			Resources res = ctx.getResources();
			Configuration cfg = res.getConfiguration();
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
				cfg.setLocale(newLoc);
			} else {
				cfg.locale = newLoc;
			}
			res.updateConfiguration(cfg, res.getDisplayMetrics());
			XposedBridge.log("Via 语言环境已切换: " + newLoc.toString());
		} catch (Exception e) {
			XposedBridge.log("切换 Locale 失败: " + e);
		}
	}

	private void refreshModuleButtonText(Context ctx) {
		if (moduleButtonRef == null)
			return;
		try {
			String newText = getLocalizedString(ctx, "module_settings");
			XposedHelpers.setObjectField(moduleButtonRef, "a", newText);
			XposedBridge.log("模块按钮文字已刷新: " + newText);
		} catch (Exception e) {
			XposedBridge.log("刷新按钮文字失败: " + e);
		}
	}

	private void showLanguageChangeToast(Context ctx, int which) {
		String key;
		switch (which) {
			case 0 :
				key = "toast_language_auto";
				break;
			case 1 :
				key = "toast_language_zh_cn";
				break;
			case 2 :
				key = "toast_language_zh_tw";
				break;
			case 3 :
				key = "toast_language_en";
				break;
			default :
				return;
		}
		Toast.makeText(ctx, getLocalizedString(ctx, key), Toast.LENGTH_SHORT).show();
	}

	private void jiguroMessage(String msg) {
		try {
			Toast.makeText(Context, msg, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			XposedBridge.log("Toast 异常: " + e);
		}
	}

	private int dp(Context ctx, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
	}

	private int getPrefInt(Context ctx, String key, int def) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			return (int) XposedHelpers.callMethod(sp, "getInt", key, def);
		} catch (Exception e) {
			return def;
		}
	}

	private void putPrefInt(Context ctx, String key, int value) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			Object ed = XposedHelpers.callMethod(sp, "edit");
			XposedHelpers.callMethod(ed, "putInt", key, value);
			XposedHelpers.callMethod(ed, "apply");
		} catch (Exception e) {
			XposedBridge.log("putInt 失败: " + e);
		}
	}

	/* =========================================================
	 * 启动时检查更新
	 * ======================================================= */
	private void checkUpdateOnStart(final Context ctx) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 延迟3秒检查，避免影响启动速度
					Thread.sleep(3000);
					checkUpdate(ctx);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/* =========================================================
	 * 检查更新
	 * ======================================================= */
	private void checkUpdate(final Context ctx) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String networkSource = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE);
					String updateUrl = networkSource.equals(NETWORK_SOURCE_GITEE)
							? GITEE_UPDATE_URL
							: GITHUB_UPDATE_URL;

					URL url = new URL(updateUrl);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(8000);
					conn.setReadTimeout(8000);
					conn.setRequestMethod("GET");

					if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
						return;
					}

					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					br.close();
					conn.disconnect();

					String jsonResponse = sb.toString();
					JSONObject json = new JSONObject(jsonResponse);
					final String remoteVersion = json.getString("versionName");
					final String apkUrl = json.getString("apkUrl");

					// 获取更新日志
					final String updateLog;
					try {
						JSONObject updateLogJson = json.getJSONObject("updateLog");
						String currentLang = getCurrentLanguageCode(ctx);
						if (updateLogJson.has(currentLang)) {
							updateLog = updateLogJson.getString(currentLang);
						} else {
							updateLog = updateLogJson.getString("en");
						}
					} catch (JSONException e) {
						updateLog = json.getString("updateLog");
					}

					// 使用MainActivity中的简单字符串比较逻辑
					String localVersion = MODULE_VERSION_NAME;

					// 直接比较版本字符串是否相等
					if (!remoteVersion.equals(localVersion)) {
						// 在主线程显示更新对话框
						if (Context != null && Context instanceof Activity) {
							((Activity) Context).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									showUpdateDialog(ctx, remoteVersion, updateLog, apkUrl);
								}
							});
						}
					}
				} catch (Exception e) {
					// 静默处理更新检查异常
				}
			}
		}).start();
	}

	/* =========================================================
	 * 获取当前语言代码
	 * ======================================================= */
	private String getCurrentLanguageCode(Context ctx) {
		String saved = getSavedLanguage(ctx);
		if ("auto".equals(saved)) {
			Locale locale;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				locale = ctx.getResources().getConfiguration().getLocales().get(0);
			} else {
				locale = ctx.getResources().getConfiguration().locale;
			}

			if (Locale.SIMPLIFIED_CHINESE.equals(locale)) {
				return "zh-CN";
			} else if (Locale.TRADITIONAL_CHINESE.equals(locale)) {
				return "zh-TW";
			}
			return "en";
		}
		return saved;
	}

	/* =========================================================
	 * 显示更新对话框
	 * ======================================================= */
	private void showUpdateDialog(final Context ctx, final String version, final String updateLog,
			final String apkUrl) {
		if (Context == null || !(Context instanceof Activity))
			return;

		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// 创建ScrollView作为根容器
				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

				// 主布局容器
				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);

				/* ========== 添加大标题 ========== */
				TextView title = new TextView(act);
				title.setText("BetterVia");
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
				title.setTextColor(0xFF6200EE); // 主题紫色
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.START); // 靠左对齐
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);

				/* ========== 版本信息 ========== */
				TextView versionTitle = new TextView(act);
				versionTitle.setText(String.format(getLocalizedString(ctx, "new_version_found"), version));
				versionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				versionTitle.setTextColor(Color.BLACK);
				versionTitle.setTypeface(null, Typeface.BOLD);
				versionTitle.setGravity(Gravity.START);
				LinearLayout.LayoutParams versionLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				versionLp.bottomMargin = dp(act, 16);
				root.addView(versionTitle, versionLp);

				/* ========== 更新日志标题 ========== */
				TextView logTitle = new TextView(act);
				logTitle.setText(getLocalizedString(ctx, "update_log_title"));
				logTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				logTitle.setTextColor(0xFF666666);
				logTitle.setTypeface(null, Typeface.BOLD);
				logTitle.setGravity(Gravity.START);
				LinearLayout.LayoutParams logTitleLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				logTitleLp.bottomMargin = dp(act, 8);
				root.addView(logTitle, logTitleLp);

				/* ========== 更新日志内容 ========== */
				// 创建日志内容的容器，添加内边距和背景
				LinearLayout logContainer = new LinearLayout(act);
				logContainer.setOrientation(LinearLayout.VERTICAL);
				logContainer.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

				// 设置日志容器的背景
				GradientDrawable logBg = new GradientDrawable();
				logBg.setColor(0xFFF8F9FA); // 浅灰色背景
				logBg.setCornerRadius(dp(act, 12));
				logContainer.setBackground(logBg);

				LinearLayout.LayoutParams containerLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				containerLp.bottomMargin = dp(act, 20);
				root.addView(logContainer, containerLp);

				// 更新日志文本
				TextView logContent = new TextView(act);
				logContent.setText(updateLog);
				logContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				logContent.setTextColor(0xFF444444);
				logContent.setLineSpacing(dp(act, 4), 1.2f); // 增加行间距
				logContent.setGravity(Gravity.START);
				logContainer.addView(logContent);

				/* ========== 按钮区域 ========== */
				LinearLayout buttonLayout = new LinearLayout(act);
				buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
				buttonLayout.setGravity(Gravity.CENTER);

				// 以后再说按钮
				Button laterButton = new Button(act);
				laterButton.setText(getLocalizedString(ctx, "later"));
				laterButton.setTextColor(0xFF6200EE); // 主题紫色文字
				laterButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				laterButton.setTypeface(null, Typeface.BOLD);
				GradientDrawable laterBg = new GradientDrawable();
				laterBg.setColor(0xFFEEEEEE); // 浅灰色背景
				laterBg.setCornerRadius(dp(act, 12));
				laterButton.setBackground(laterBg);
				laterButton.setPadding(dp(act, 24), dp(act, 12), dp(act, 24), dp(act, 12));

				LinearLayout.LayoutParams laterLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				laterLp.rightMargin = dp(act, 8);
				buttonLayout.addView(laterButton, laterLp);

				// 立即下载按钮
				Button downloadButton = new Button(act);
				downloadButton.setText(getLocalizedString(ctx, "download_now"));
				downloadButton.setTextColor(Color.WHITE);
				downloadButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				downloadButton.setTypeface(null, Typeface.BOLD);
				GradientDrawable downloadBg = new GradientDrawable();
				downloadBg.setColor(0xFF6200EE); // 主题紫色背景
				downloadBg.setCornerRadius(dp(act, 12));
				downloadButton.setBackground(downloadBg);
				downloadButton.setPadding(dp(act, 24), dp(act, 12), dp(act, 24), dp(act, 12));

				LinearLayout.LayoutParams downloadLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				downloadLp.leftMargin = dp(act, 8);
				buttonLayout.addView(downloadButton, downloadLp);

				root.addView(buttonLayout);

				scrollRoot.addView(root);

				/* ========== 创建对话框 ========== */
				final AlertDialog dialog = new AlertDialog.Builder(act).setView(scrollRoot).setCancelable(false)
						.create();

				// 设置对话框窗口属性 - 保持原有的圆角样式
				Window win = dialog.getWindow();
				if (win != null) {
					win.setBackgroundDrawableResource(android.R.color.transparent);
					GradientDrawable round = new GradientDrawable();
					round.setColor(Color.WHITE);
					round.setCornerRadius(dp(act, 24));
					win.setBackgroundDrawable(round);
					win.setGravity(Gravity.CENTER);
					win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				}

				/* ========== 按钮点击事件 ========== */
				laterButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				downloadButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
							act.startActivity(intent);
							dialog.dismiss();
						} catch (Exception e) {
							Toast.makeText(act, getLocalizedString(ctx, "cannot_open_download_link"),
									Toast.LENGTH_SHORT).show();
						}
					}
				});

				dialog.show();
			}
		});
	}

	/* =========================================================
	 * 显示关于对话框
	 * ======================================================= */
	private void showAboutDialog(final Context ctx) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;

				final Dialog dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);

				// 根容器
				FrameLayout dialogContainer = new FrameLayout(act);
				GradientDrawable containerBg = new GradientDrawable();
				containerBg.setColor(Color.WHITE);
				containerBg.setCornerRadius(dp(act, 24));
				dialogContainer.setBackground(containerBg);

				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));

				// 大标题
				TextView title = new TextView(act);
				title.setText("BetterVia");
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
				title.setTextColor(0xFF6200EE);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);

				// 副标题
				TextView subtitle = new TextView(act);
				subtitle.setText(getLocalizedString(ctx, "about_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);

				/* ========== 模块信息部分 ========== */
				TextView moduleTitle = new TextView(act);
				moduleTitle.setText(getLocalizedString(ctx, "about_module_title"));
				moduleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				moduleTitle.setTextColor(Color.BLACK);
				moduleTitle.setTypeface(null, Typeface.BOLD);
				moduleTitle.setPadding(0, 0, 0, dp(act, 12));
				root.addView(moduleTitle);

				// 版本信息
				addAboutItem(root, act, getLocalizedString(ctx, "about_version"), MODULE_VERSION_NAME);

				// 作者信息
				addAboutItem(root, act, getLocalizedString(ctx, "about_author"), "JiGuro");

				// GitHub仓库
				addClickableAboutItem(root, act, getLocalizedString(ctx, "about_github"),
						"https://github.com/JiGuroLGC/BetterVia", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								openUrl(act, "https://github.com/JiGuroLGC/BetterVia");
								Toast.makeText(act, getLocalizedString(ctx, "start_url_message"), Toast.LENGTH_SHORT)
										.show();
							}
						});

				// Gitee仓库
				addClickableAboutItem(root, act, getLocalizedString(ctx, "about_gitee"),
						"https://gitee.com/jiguro/BetterVia", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								openUrl(act, "https://gitee.com/jiguro/BetterVia");
								Toast.makeText(act, getLocalizedString(ctx, "start_url_message"), Toast.LENGTH_SHORT)
										.show();
							}
						});

				// Xposed模块仓库
				addClickableAboutItem(root, act, getLocalizedString(ctx, "about_xposed"),
						getLocalizedString(ctx, "about_xposed_repo"), new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								openUrl(act, "https://modules.lsposed.org/module/com.jiguro.bettervia");
								Toast.makeText(act, getLocalizedString(ctx, "start_url_message"), Toast.LENGTH_SHORT)
										.show();
							}
						});

				/* ========== 更新日志部分 ========== */
				TextView updateTitle = new TextView(act);
				updateTitle.setText(getLocalizedString(ctx, "about_update_title"));
				updateTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				updateTitle.setTextColor(Color.BLACK);
				updateTitle.setTypeface(null, Typeface.BOLD);
				updateTitle.setPadding(0, dp(act, 24), 0, dp(act, 12));
				root.addView(updateTitle);

				// 更新日志内容
				LinearLayout updateContainer = new LinearLayout(act);
				updateContainer.setOrientation(LinearLayout.VERTICAL);
				updateContainer.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));
				GradientDrawable updateBg = new GradientDrawable();
				updateBg.setColor(0xFFF8F9FA);
				updateBg.setCornerRadius(dp(act, 8));
				updateContainer.setBackground(updateBg);

				String[] updateLogs = {getLocalizedString(ctx, "about_update_log1"),
						getLocalizedString(ctx, "about_update_log2"), getLocalizedString(ctx, "about_update_log3"),
						getLocalizedString(ctx, "about_update_log4")};

				for (String log : updateLogs) {
					TextView logItem = new TextView(act);
					logItem.setText("• " + log);
					logItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					logItem.setTextColor(0xFF444444);
					logItem.setPadding(0, dp(act, 4), 0, dp(act, 4));
					updateContainer.addView(logItem);
				}

				root.addView(updateContainer);

				/* ========== 鸣谢部分 ========== */
				TextView thanksTitle = new TextView(act);
				thanksTitle.setText(getLocalizedString(ctx, "about_thanks_title"));
				thanksTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				thanksTitle.setTextColor(Color.BLACK);
				thanksTitle.setTypeface(null, Typeface.BOLD);
				thanksTitle.setPadding(0, dp(act, 24), 0, dp(act, 12));
				root.addView(thanksTitle);

				// 鸣谢内容
				addAboutItem(root, act, "", getLocalizedString(ctx, "about_thanks_content"));

				// 酷安链接
				addClickableAboutItem(root, act, "", "酷安 @半烟半雨溪桥畔", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						openUrl(act, "https://gitee.com/daybreak166/ViaBrowserCollection");
						Toast.makeText(act, getLocalizedString(ctx, "start_url_message"), Toast.LENGTH_SHORT).show();
					}
				});

				addAboutItem(root, act, "", getLocalizedString(ctx, "about_thanks_others"));

				// 确定按钮
				Button ok = new Button(act);
				ok.setText(getLocalizedString(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				GradientDrawable btnBg = new GradientDrawable();
				btnBg.setColor(0xFF6200EE);
				btnBg.setCornerRadius(dp(act, 12));
				ok.setBackground(btnBg);
				ok.setPadding(0, dp(act, 14), 0, dp(act, 14));

				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				okLp.topMargin = dp(act, 16);
				root.addView(ok, okLp);

				scrollRoot.addView(root);
				dialogContainer.addView(scrollRoot);
				dialog.setContentView(dialogContainer);

				// 设置对话框窗口属性
				Window window = dialog.getWindow();
				if (window != null) {
					window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					DisplayMetrics metrics = new DisplayMetrics();
					act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int width = (int) (metrics.widthPixels * 0.9);
					WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
					layoutParams.copyFrom(window.getAttributes());
					layoutParams.width = width;
					layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
					layoutParams.gravity = Gravity.CENTER;
					window.setAttributes(layoutParams);
				}

				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});
	}

	/* =========================================================
	 * 添加关于项（普通文本）
	 * ======================================================= */
	private void addAboutItem(LinearLayout parent, Activity act, String label, String value) {
		LinearLayout row = new LinearLayout(act);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setPadding(0, dp(act, 6), 0, dp(act, 6));

		if (!label.isEmpty()) {
			TextView labelView = new TextView(act);
			labelView.setText(label);
			labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			labelView.setTextColor(0xFF666666);
			labelView.setTypeface(null, Typeface.BOLD);
			LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(dp(act, 80),
					ViewGroup.LayoutParams.WRAP_CONTENT);
			row.addView(labelView, labelLp);
		}

		TextView valueView = new TextView(act);
		valueView.setText(value);
		valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		valueView.setTextColor(0xFF333333);
		row.addView(valueView);

		parent.addView(row);
	}

	/* =========================================================
	 * 添加可点击的关于项
	 * ======================================================= */
	private void addClickableAboutItem(LinearLayout parent, Activity act, String label, String value,
			View.OnClickListener listener) {
		LinearLayout row = new LinearLayout(act);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setPadding(0, dp(act, 6), 0, dp(act, 6));
		row.setOnClickListener(listener);

		if (!label.isEmpty()) {
			TextView labelView = new TextView(act);
			labelView.setText(label);
			labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			labelView.setTextColor(0xFF666666);
			labelView.setTypeface(null, Typeface.BOLD);
			LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(dp(act, 80),
					ViewGroup.LayoutParams.WRAP_CONTENT);
			row.addView(labelView, labelLp);
		}

		TextView valueView = new TextView(act);
		valueView.setText(value);
		valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		valueView.setTextColor(0xFF6200EE);
		valueView.setPaintFlags(valueView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		row.addView(valueView);

		parent.addView(row);
	}

	/* =========================================================
	 * 打开URL
	 * ======================================================= */
	private void openUrl(Context ctx, String url) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			ctx.startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(ctx, getLocalizedString(ctx, "cannot_open_url"), Toast.LENGTH_SHORT).show();
		}
	}

	/* =========================================================
	 * 多语言映射
	 * ======================================================= */
	private String getLocalizedString(Context ctx, String key) {
		Locale loc = getUserLocale(ctx);
		String lang = loc.getLanguage();
		String country = loc.getCountry();

		/* ================== 通用界面文本 ================== */
		if ("module_settings".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "模块" : "模組";
			}
			return "Module";
		}
		if ("dialog_ok".equals(key)) {
			return "zh".equals(lang) ? "确定" : "OK";
		}
		if ("dialog_cancel".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "取消" : "取消";
			}
			return "Cancel";
		}
		if ("dialog_back".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "返回" : "返回";
			}
			return "Back";
		}
		if ("dialog_close".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "关闭" : "關閉";
			}
			return "Close";
		}

		/* ================== 语言设置相关 ================== */
		if ("language_title".equals(key)) {
			return "zh".equals(lang) ? "语言设置" : "Language";
		}
		if ("language_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "设置模块界面和功能提示的语言" : "設置模組界面和功能提示的語言";
			}
			return "Set the language for module interface and feature hints";
		}
		if ("language_auto".equals(key)) {
			return "zh".equals(lang) ? "自动选择语言" : "Auto Select Language";
		}
		if ("language_zh_cn".equals(key)) {
			return "zh".equals(lang) ? "简体中文" : "Simplified Chinese";
		}
		if ("language_zh_tw".equals(key)) {
			return "zh".equals(lang) ? "繁體中文" : "Traditional Chinese";
		}
		if ("language_en".equals(key)) {
			return "English";
		}
		if ("toast_language_auto".equals(key)) {
			return "zh".equals(lang) ? "已设置为自动选择语言" : "Set to auto select language";
		}
		if ("toast_language_zh_cn".equals(key)) {
			return "zh".equals(lang) ? "已设置为简体中文" : "Set to Simplified Chinese";
		}
		if ("toast_language_zh_tw".equals(key)) {
			return "zh".equals(lang) ? "已設置為繁體中文" : "Set to Traditional Chinese";
		}
		if ("toast_language_en".equals(key)) {
			return "Set to English";
		}

		/* ================== 核心功能开关 ================== */
		if ("whitelist_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "解除白名单限制" : "解除白名單限制";
			}
			return "Bypass Whitelist";
		}
		if ("whitelist_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "解除某些网站的资源嗅探、广告拦截和脚本限制" : "解除某些網站的資源嗅探、廣告攔截和腳本限制";
			}
			return "Unblock resource sniffing, ad blocking and script restrictions for certain websites";
		}
		if ("b_hook_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "开启实验模式" : "開啟實驗模式";
			}
			return "Experimental Mode";
		}
		if ("b_hook_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "显示隐藏的实验模式入口" : "顯示隱藏的實驗模式入口";
			}
			return "Show hidden experimental mode entrance";
		}
		if ("hook_success_message".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "領域展開，りょういきてんかい !" : "領域展開，りょういきてんかい !";
			}
			return "Field Expansion，りょういきてんかい !";
		}

		/* ================== 组件屏蔽相关 ================== */
		if ("component_block_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "屏蔽组件" : "屏蔽組件";
			}
			return "Block Components";
		}
		if ("component_block_config".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "配置" : "配置";
			}
			return "Configure";
		}
		if ("component_block_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "点击配置要屏蔽的组件" : "點擊配置要屏蔽的組件";
			}
			return "Click to configure components to block";
		}
		if ("component_block_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "选择要屏蔽的组件" : "選擇要屏蔽的組件";
			}
			return "Select Components to Block";
		}
		if ("component_block_saved".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "设置已保存" : "設置已保存";
			}
			return "Settings saved";
		}

		// 组件名称
		if ("component_update".equals(key))
			return "检查更新";
		if ("component_telegram".equals(key))
			return "加入 Telegram 群组";
		if ("component_qq".equals(key))
			return "加入 QQ 群组";
		if ("component_email".equals(key))
			return "通过邮件联系我";
		if ("component_wechat".equals(key))
			return "微信公众号";
		if ("component_donate".equals(key))
			return "捐助我们";
		if ("component_assist".equals(key))
			return "协助翻译";
		if ("component_agreement".equals(key))
			return "使用协议";
		if ("component_privacy".equals(key))
			return "隐私政策";
		if ("component_opensource".equals(key))
			return "开源许可协议";
		if ("component_icp".equals(key))
			return "备案号";

		/* ================== 护眼模式相关 ================== */
		if ("eye_protection_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "护眼模式" : "護眼模式";
			}
			return "Eye Protection Mode";
		}
		if ("eye_protection_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "屏幕偏暖，减少蓝光对眼睛的伤害" : "屏幕偏暖，減少藍光對眼睛的傷害";
			}
			return "The screen is warmer to reduce the damage of blue light to the eyes";
		}
		if ("eye_protection_config".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "护眼调节" : "護眼調節";
			}
			return "Eye Protection Adjust";
		}
		if ("eye_protection_config_btn".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "配置" : "配置";
			}
			return "Configure";
		}
		if ("eye_protection_config_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "点击调节色温和纸质纹理" : "點擊調節色溫和紙質紋理";
			}
			return "Click to adjust temperature and paper texture";
		}
		if ("eye_protection_config_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "护眼调节" : "護眼調節";
			}
			return "Eye Protection Adjust";
		}
		if ("eye_protection_config_subtitle".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "护眼模式设置" : "護眼模式設置";
			}
			return "Eye Protection Settings";
		}
		if ("eye_protection_temperature".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "色温调节" : "色溫調節";
			}
			return "Color Temperature";
		}
		if ("eye_protection_texture".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "纸质纹理" : "紙質紋理";
			}
			return "Paper Texture";
		}
		if ("eye_protection_cold".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "偏冷" : "偏冷";
			}
			return "Cool";
		}
		if ("eye_protection_warm".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "偏暖" : "偏暖";
			}
			return "Warm";
		}
		if ("eye_protection_smooth".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "光滑" : "光滑";
			}
			return "Smooth";
		}
		if ("eye_protection_rough".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "粗糙" : "粗糙";
			}
			return "Rough";
		}
		if ("eye_protection_preview_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "效果预览" : "效果預覽";
			}
			return "Preview";
		}
		if ("eye_protection_sample_text".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "心随长风去，吹散万里云。" : "心隨長風去，吹散萬里雲。";
			}
			return "This is sample text";
		}
		if ("eye_protection_preview_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "开启护眼模式后可实时预览效果" : "開啟護眼模式後可實時預覽效果";
			}
			return "Real-time preview available when eye protection is enabled";
		}
		if ("eye_protection_config_saved".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "护眼设置已保存" : "護眼設置已保存";
			}
			return "Eye protection settings saved";
		}

		/* ================== 隐私和安全功能 ================== */
		if ("block_google_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "超级隐身" : "超級隱身";
			}
			return "Super Stealth";
		}
		if ("block_google_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "阻止收集用户隐私数据，增强安全性" : "阻止收集用戶隱私數據，增強安全性";
			}
			return "Prevent the collection of user private data and enhance security";
		}
		if ("block_startup_message_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "屏蔽启动提示" : "屏蔽啟動提示";
			}
			return "Block Startup Message";
		}
		if ("block_startup_message_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "启动时不显示领域展开提示" : "啟動時不顯示領域展開提示";
			}
			return "Don't show the field expansion message on startup";
		}
		if ("screenshot_protection_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "截屏防护" : "截屏防護";
			}
			return "Screenshot Protection";
		}
		if ("screenshot_protection_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "禁止第三方应用截屏或录屏，保护隐私" : "禁止第三方應用截屏或錄屏，保護隱私";
			}
			return "Prevent third-party apps from taking screenshots or recording screen to protect privacy";
		}
		if ("keep_screen_on_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "屏幕常亮" : "屏幕常亮";
			}
			return "Keep Screen On";
		}
		if ("keep_screen_on_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "防止屏幕自动息屏，但会增加耗电" : "防止屏幕自動息屏，但會增加耗電";
			}
			return "Prevent screen from turning off automatically, but will increase battery consumption";
		}
		if ("hide_status_bar_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "隐藏状态栏" : "隱藏狀態欄";
			}
			return "Hide Status Bar";
		}
		if ("hide_status_bar_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "隐藏屏幕上方的状态栏，下滑即可恢复" : "隱藏屏幕上方的狀態欄，下滑即可恢復";
			}
			return "Hide the status bar at the top of the screen and slide it down to restore it";
		}

		/* ================== 搜索指令相关 ================== */
		if ("search_commands_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "搜索指令" : "搜尋指令";
			}
			return "Search Commands";
		}
		if ("search_commands_config".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "查看" : "查看";
			}
			return "View";
		}
		if ("search_commands_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "查看所有Via搜索指令" : "查看所有Via搜尋指令";
			}
			return "View all Via search commands";
		}
		if ("search_commands_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "Via搜索指令大全" : "Via搜尋指令大全";
			}
			return "Via Search Commands";
		}
		if ("search_commands_subtitle".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "以下指令可在Via浏览器地址栏中使用" : "以下指令可在Via瀏覽器地址欄中使用";
			}
			return "The following commands can be used in Via browser address bar";
		}
		if ("command_copy".equals(key)) {
			return "zh".equals(lang) ? "复制" : "Copy";
		}
		if ("command_copied".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "已复制到剪贴板" : "已複製到剪貼簿";
			}
			return "Copied to clipboard";
		}

		// 搜索指令描述
		if ("command_bookmark".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开书签" : "開啟書籤籤";
			}
			return "Open bookmarks";
		}
		if ("command_search".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开搜索框" : "開啟搜尋框";
			}
			return "Open search box";
		}
		if ("command_unknown".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "功能未知" : "功能未知";
			}
			return "Unknown function";
		}
		if ("command_print".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打印当前网页" : "列印當前網頁";
			}
			return "Print current page";
		}
		if ("command_adblock".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "拦截广告" : "攔截廣告";
			}
			return "Block ads";
		}
		if ("command_log".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开日志" : "開啟日誌";
			}
			return "Open logs";
		}
		if ("command_home".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开主页" : "開啟主頁";
			}
			return "Open home page";
		}
		if ("command_skins".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开皮肤" : "開啟皮膚";
			}
			return "Open skins";
		}
		if ("command_about".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开关于" : "開啟關於";
			}
			return "Open about";
		}
		if ("command_search_page".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开搜索页面" : "開啟搜尋頁面";
			}
			return "Open search page";
		}
		if ("command_offline".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开离线窗口" : "開啟離線視窗";
			}
			return "Open offline window";
		}
		if ("command_history".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开历史" : "開啟歷史";
			}
			return "Open history";
		}
		if ("command_scanner".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "扫二维码" : "掃描QR碼";
			}
			return "Scan QR code";
		}
		if ("command_bookmarks_page".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开书签页面" : "開啟書籤籤頁面";
			}
			return "Open bookmarks page";
		}
		if ("command_downloader".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开下载管理" : "開啟下載管理";
			}
			return "Open download manager";
		}
		if ("command_readaloud".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "阅读控制器" : "閱讀控制器";
			}
			return "Reading controller";
		}
		if ("command_translator".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "翻译文本" : "翻譯文字";
			}
			return "Translate text";
		}
		if ("command_history_page".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开历史页面" : "開啟歷史頁面";
			}
			return "Open history page";
		}
		if ("command_folder".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "打开书签文件夹" : "開啟書籤籤資料夾";
			}
			return "Open bookmarks folder";
		}

		/* ================== 主页主题相关 ================== */
		if ("homepage_theme_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "主页主题" : "主頁主題";
			}
			return "Homepage Theme";
		}
		if ("homepage_theme_config".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "配置" : "配置";
			}
			return "Configure";
		}
		if ("homepage_theme_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "自定义Via浏览器主页外观" : "自定義Via瀏覽器主頁外觀";
			}
			return "Customize Via browser homepage appearance";
		}
		if ("homepage_theme_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "主页主题" : "主頁主題";
			}
			return "Homepage Themes";
		}
		if ("homepage_theme_subtitle".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "丰富的主题资源" : "豐富的主題資源";
			}
			return "Rich theme resources";
		}
		if ("homepage_theme_by".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "作者：" : "作者：";
			}
			return "By ";
		}
		if ("homepage_theme_developing".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "正在开发中..." : "正在開發中...";
			}
			return "Developing...";
		}
		if ("homepage_theme_apply_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "应用主题" : "應用主題";
			}
			return "Apply Theme";
		}
		if ("homepage_theme_apply_message".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "您是否要应用主题" : "您是否要應用主題";
			}
			return "Do you want to apply the theme";
		}
		if ("homepage_theme_apply".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "应用" : "應用";
			}
			return "Apply";
		}
		if ("homepage_theme_apply_success".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "主题应用成功，重启Via后生效" : "主題應用成功，重啟Via後生效";
			}
			return "Theme applied successfully, changes effective after restart Via";
		}
		if ("homepage_theme_apply_failed".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "主题应用失败，请检查网络连接" : "主題應用失敗，請檢查網路連接";
			}
			return "Theme application failed, please check network connection";
		}
		if ("homepage_theme_apply_error".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "应用主题时发生错误" : "應用主題時發生錯誤";
			}
			return "Error occurred while applying theme";
		}
		if ("homepage_theme_edit".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "编辑" : "編輯";
			}
			return "Edit";
		}
		if ("homepage_theme_editor_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "主题编辑器" : "主題編輯器";
			}
			return "Theme Editor";
		}
		if ("theme_editor_select_file".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "选择编辑的文件:" : "選擇編輯的文件:";
			}
			return "Select file to edit:";
		}
		if ("theme_editor_edit_content".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "编辑内容:" : "編輯內容:";
			}
			return "Edit content:";
		}
		if ("theme_editor_save_success".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "保存成功" : "保存成功";
			}
			return "Save successful";
		}

		if ("homepage_theme_save".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "保存" : "保存";
			}
			return "Save";
		}
		if ("homepage_theme_save_success".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "保存成功" : "保存成功";
			}
			return "Save successful";
		}
		if ("homepage_theme_save_error".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "保存失败" : "保存失敗";
			}
			return "Save failed";
		}
		if ("homepage_theme_file_not_found".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "文件不存在" : "文件不存在";
			}
			return "File not found";
		}
		if ("homepage_theme_load_error".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "加载错误" : "加載錯誤";
			}
			return "Load error";
		}
		if ("homepage_theme_edit_file".equals(key)) {
			return "编辑文件:";
		}
		if ("homepage_theme_edit_content".equals(key)) {
			return "编辑内容:";
		}

		/* ================== 脚本仓库相关 ================== */
		if ("script_repository_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "脚本仓库" : "腳本倉庫";
			}
			return "Script Repository";
		}
		if ("script_repository_config".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "配置" : "配置";
			}
			return "Configure";
		}
		if ("script_repository_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "下载浏览器脚本插件" : "下載瀏覽器腳本插件";
			}
			return "Download browser script plugins";
		}
		if ("script_repository_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "脚本仓库" : "腳本倉庫";
			}
			return "Script Repository";
		}
		if ("script_repository_subtitle".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "丰富的用户脚本资源，但不保证可用" : "豐富的用戶腳本資源，但不保證可用";
			}
			return "Rich user script resources, but availability is not guaranteed";
		}
		if ("scripts_loading".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "正在加载脚本..." : "正在加載腳本...";
			}
			return "Loading scripts...";
		}
		if ("scripts_load_failed".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "脚本加载失败" : "腳本加載失敗";
			}
			return "Failed to load scripts";
		}
		if ("script_opened_in_via".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "已打开脚本，现在您可以继续浏览，刷新网页即可批量安装" : "已打開腳本，現在您可以繼續瀏覽，刷新網頁即可批量安裝";
			}
			return "Script opened in Via, refresh the page to install";
		}
		if ("script_search_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "搜索脚本名称或描述..." : "搜尋腳本名稱或描述...";
			}
			return "Search script name or description...";
		}
		if ("script_search_button".equals(key)) {
			return "zh".equals(lang) ? "搜索" : "Search";
		}
		if ("script_search_no_results".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "未找到包含\"%s\"的脚本" : "未找到包含\"%s\"的腳本";
			}
			return "No scripts found containing \"%s\"";
		}
		if ("script_search_no_results_toast".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "未找到相关脚本" : "未找到相關腳本";
			}
			return "No related scripts found";
		}
		if ("script_search_results".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "找到 %d 个脚本" : "找到 %d 個腳本";
			}
			return "Found %d scripts";
		}
		if ("script_show_all".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "显示全部 %d 个脚本" : "顯示全部 %d 個腳本";
			}
			return "Showing all %d scripts";
		}
		if ("no_scripts_available".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "没有可用的脚本" : "沒有可用的腳本";
			}
			return "No scripts available";
		}
		if ("script_total_count".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "共收录 %d 个脚本" : "共收錄 %d 個腳本";
			}
			return "Total %d scripts";
		}
		if ("script_filtered_count".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "筛选出 %d/%d 个脚本" : "篩選出 %d/%d 個腳本";
			}
			return "Filtered %d/%d scripts";
		}
		if ("script_loading_count".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "正在加载脚本..." : "正在加載腳本...";
			}
			return "Loading scripts...";
		}
		if ("script_load_failed_count".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "加载失败" : "加載失敗";
			}
			return "Load failed";
		}

		/* ================== 广告拦截规则相关 ================== */
		if ("ad_block_rules_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "广告走开" : "廣告走開";
			}
			return "Ad Block Rules";
		}
		if ("ad_block_rules_config".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "配置" : "配置";
			}
			return "Configure";
		}
		if ("ad_block_rules_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "点击配置广告拦截规则" : "點擊配置廣告攔截規則";
			}
			return "Click to configure ad blocking rules";
		}
		if ("ad_block_rules_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "广告拦截规则" : "廣告攔截規則";
			}
			return "Ad Block Rules";
		}
		if ("ad_block_rules_subtitle".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "选择适合的广告拦截规则" : "選擇適合的廣告攔截規則";
			}
			return "Select suitable ad blocking rules";
		}
		if ("rules_loading".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "加载规则中..." : "加載規則中...";
			}
			return "Loading rules...";
		}
		if ("rules_load_failed".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "规则加载失败" : "規則加載失敗";
			}
			return "Failed to load rules";
		}
		if ("rules_category_small".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "小型规则" : "小型規則";
			}
			return "Small Rules";
		}
		if ("rules_category_large".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "大型规则" : "大型規則";
			}
			return "Large Rules";
		}
		if ("rule_author".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "作者" : "作者";
			}
			return "Author";
		}
		if ("rule_homepage".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "主页" : "主頁";
			}
			return "Homepage";
		}
		if ("rule_channel".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "渠道" : "渠道";
			}
			return "Channel";
		}
		if ("rule_link_copied".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "链接已复制到剪贴板" : "連結已複製到剪貼板";
			}
			return "Link copied to clipboard";
		}
		if ("cannot_open_homepage".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "无法打开主页链接" : "無法開啟主頁連結";
			}
			return "Cannot open homepage link";
		}

		/* ================== 资源界面美化相关 ================== */
		if ("homepage_bg_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "资源界面美化" : "資源界面美化";
			}
			return "Resource UI Beauty";
		}
		if ("homepage_bg_config".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "配置" : "配置";
			}
			return "Pick";
		}
		if ("homepage_bg_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "为日志/资源页设置背景图" : "為日誌/資源頁設定背景圖";
			}
			return "Set background for log/resource page";
		}
		if ("homepage_bg_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "资源界面美化" : "資源界面美化";
			}
			return "Resource UI Beauty";
		}
		if ("homepage_bg_dialog_subtitle".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "调整背景图与遮罩，让文字更清晰" : "調整背景圖與遮罩，讓文字更清晰";
			}
			return "Adjust background & mask to keep text clear";
		}
		if ("homepage_bg_pick_btn".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "选择图片" : "選擇圖片";
			}
			return "Select Image";
		}
		if ("homepage_bg_mask_alpha".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "遮罩透明度" : "遮罩透明度";
			}
			return "Mask Opacity";
		}
		if ("homepage_bg_mask_color".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "遮罩色相" : "遮罩色相";
			}
			return "Mask Hue";
		}
		if ("homepage_bg_mask_color_rgb".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "遮罩颜色（RGB）" : "遮罩顏色（RGB）";
			}
			return "Mask Color (RGB)";
		}
		if ("homepage_bg_mask_color_confirm".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "确认" : "確認";
			}
			return "Confirm";
		}
		if ("homepage_bg_mask_color_invalid".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "颜色格式无效，请使用#RRGGBB格式" : "顏色格式無效，請使用#RRGGBB格式";
			}
			return "Invalid color format, please use #RRGGBB";
		}
		if ("homepage_bg_mask_color_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "输入RGB颜色代码（如#FFFFFF表示白色）" : "輸入RGB顏色代碼（如#FFFFFF表示白色）";
			}
			return "Enter RGB color code (e.g. #FFFFFF for white)";
		}
		if ("homepage_bg_set_ok".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "背景图已设置" : "背景圖已設定";
			}
			return "Background image set";
		}
		if ("homepage_bg_set_fail".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "设置失败" : "設定失敗";
			}
			return "Set failed";
		}
		if ("homepage_bg_saved".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "设置已保存" : "設定已儲存";
			}
			return "Settings saved";
		}

		/* ================== Cookie管理相关 ================== */
		if ("cookie_management_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "Cookie管理" : "Cookie管理";
			}
			return "Cookie Management";
		}
		if ("cookie_management_config".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "管理" : "管理";
			}
			return "Manage";
		}
		if ("cookie_management_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "查看和管理浏览器Cookie" : "查看和管理瀏覽器Cookie";
			}
			return "View and manage browser cookies";
		}
		// 2. Cookie管理器主对话框
		if ("cookie_manager_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "Cookie管理器" : "Cookie管理器";
			}
			return "Cookie Manager";
		}
		if ("cookie_manager_search_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "搜索域名或Cookie名称..." : "搜尋網域或Cookie名稱...";
			}
			return "Search domain or cookie name...";
		}
		if ("cookie_manager_search_btn".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "搜索" : "搜尋";
			}
			return "Search";
		}
		if ("cookie_manager_loading".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "正在加载Cookie数据..." : "正在加載Cookie數據...";
			}
			return "Loading cookie data...";
		}
		if ("cookie_manager_empty".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "没有找到Cookie数据" : "沒有找到Cookie數據";
			}
			return "No cookie data found";
		}
		if ("cookie_management_refreshed".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "刷新成功" : "刷新成功";
			}
			return "Refresh successful";
		}
		if ("cookie_search_result".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "找到 %d 个结果" : "找到 %d 個結果";
			}
			return "Found %d results";
		}
		if ("cookie_manager_delete_selected".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "删除选中" : "刪除選中";
			}
			return "Delete Selected";
		}
		// 3. Cookie详情对话框
		if ("cookie_detail_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "Cookie详情" : "Cookie詳情";
			}
			return "Cookie Details";
		}
		// 详情字段标签
		if ("cookie_field_host_key".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "域名:" : "網域:";
			}
			return "Host:";
		}
		if ("cookie_field_name".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "名称:" : "名稱:";
			}
			return "Name:";
		}
		if ("cookie_field_value".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "值:" : "值:";
			}
			return "Value:";
		}
		if ("cookie_field_path".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "路径:" : "路徑:";
			}
			return "Path:";
		}
		if ("cookie_field_creation_time".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "创建时间:" : "建立時間:";
			}
			return "Creation Time:";
		}
		if ("cookie_field_last_access".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "最后访问:" : "最後訪問:";
			}
			return "Last Access:";
		}
		if ("cookie_field_expires".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "过期时间:" : "過期時間:";
			}
			return "Expires:";
		}
		if ("cookie_field_last_update".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "最后更新:" : "最後更新:";
			}
			return "Last Update:";
		}
		if ("cookie_field_secure".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "安全连接(HTTPS):" : "安全連接(HTTPS):";
			}
			return "Secure (HTTPS):";
		}
		if ("cookie_field_httponly".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "HTTP Only:" : "HTTP Only:";
			}
			return "HTTP Only:";
		}
		if ("cookie_field_persistent".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "持久化:" : "持久化:";
			}
			return "Persistent:";
		}
		if ("cookie_field_has_expires".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "有过期时间:" : "有過期時間:";
			}
			return "Has Expires:";
		}
		if ("cookie_field_priority".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "优先级:" : "優先級:";
			}
			return "Priority:";
		}
		if ("cookie_field_samesite".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "SameSite:" : "SameSite:";
			}
			return "SameSite:";
		}
		if ("cookie_field_source_port".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "源端口:" : "來源端口:";
			}
			return "Source Port:";
		}
		if ("cookie_field_source_type".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "源类型:" : "來源類型:";
			}
			return "Source Type:";
		}
		// 4. 操作提示
		if ("cookie_save_success".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "Cookie已保存" : "Cookie已保存";
			}
			return "Cookie saved successfully";
		}
		if ("cookie_delete_confirm_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "确认删除" : "確認刪除";
			}
			return "Confirm Delete";
		}
		if ("cookie_delete_confirm_msg".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "确定要删除选中的Cookie吗？此操作不可撤销。" : "確定要刪除選中的Cookie嗎？此操作不可撤銷。";
			}
			return "Are you sure you want to delete the selected cookies? This operation cannot be undone.";
		}
		if ("cookie_delete_no_selected".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "没有选中要删除的Cookie" : "沒有選中要刪除的Cookie";
			}
			return "No cookies selected for deletion";
		}
		if ("cookie_delete_error".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "删除Cookie时发生错误" : "刪除Cookie時發生錯誤";
			}
			return "Error occurred while deleting cookies";
		}
		if ("cookie_detail_basic_info".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "基本信息" : "基本資訊";
			}
			return "Basic Information";
		}
		if ("cookie_detail_time_info".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "时间信息" : "時間資訊";
			}
			return "Time Information";
		}
		if ("cookie_detail_security_info".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "安全信息" : "安全資訊";
			}
			return "Security Information";
		}
		if ("cookie_detail_advanced_info".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "高级信息" : "進階資訊";
			}
			return "Advanced Information";
		}

		// 字段值描述
		if ("cookie_field_unknown".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "未知" : "未知";
			}
			return "Unknown";
		}
		if ("cookie_field_session".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "会话Cookie" : "工作階段Cookie";
			}
			return "Session Cookie";
		}
		if ("cookie_field_default".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "默认" : "預設";
			}
			return "Default";
		}

		// SameSite描述
		if ("cookie_samesite_none".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "未设置" : "未設定";
			}
			return "Not Set";
		}
		if ("cookie_samesite_lax".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "宽松模式 (Lax)" : "寬鬆模式 (Lax)";
			}
			return "Lax Mode";
		}
		if ("cookie_samesite_strict".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "严格模式 (Strict)" : "嚴格模式 (Strict)";
			}
			return "Strict Mode";
		}
		if ("cookie_samesite_unknown".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "未知 (%d)" : "未知 (%d)";
			}
			return "Unknown (%d)";
		}

		// 源类型描述
		if ("cookie_source_type_none".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "未设置" : "未設定";
			}
			return "Not Set";
		}
		if ("cookie_source_type_http".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "HTTP" : "HTTP";
			}
			return "HTTP";
		}
		if ("cookie_source_type_https".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "HTTPS" : "HTTPS";
			}
			return "HTTPS";
		}
		if ("cookie_source_type_file".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "文件" : "檔案";
			}
			return "File";
		}
		if ("cookie_source_type_unknown".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "未知 (%d)" : "未知 (%d)";
			}
			return "Unknown (%d)";
		}
		if ("cookie_unknown_domain".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "未知域名" : "未知網域";
			}
			return "Unknown Domain";
		}
		// 列表项名称标签（带冒号和空格，保持原格式）
		if ("cookie_field_name_label".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "名称: " : "名稱: ";
			}
			return "Name: ";
		}
		// 列表项值标签（带冒号和空格，保持原格式）
		if ("cookie_field_value_label".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "值: " : "值: ";
			}
			return "Value: ";
		}
		// 列表项值为空时显示
		if ("cookie_no_value".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "无值" : "無值";
			}
			return "No Value";
		}
		if ("cookie_manager_delete_btn".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "确定" : "確定";
			}
			return "Delete";
		}

		/* ================== 网络和更新相关 ================== */
		if ("network_source_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "网络源" : "網路源";
			}
			return "Network Source";
		}
		if ("network_source_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "选择主题和脚本等资源的下载源" : "選擇主題和腳本等資源的下載源";
			}
			return "Select the download source for themes, scripts and other resources";
		}
		if ("network_source_changed".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "网络源已切换至" : "網路源已切換至";
			}
			return "Network source changed to";
		}
		if ("auto_update_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "自动检查更新" : "自動檢查更新";
			}
			return "Auto Check Updates";
		}
		if ("auto_update_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "启动时自动检查模块更新" : "啟動時自動檢查模組更新";
			}
			return "Automatically check for module updates on startup";
		}
		if ("new_version_found".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "发现新版本 %s" : "發現新版本 %s";
			}
			return "New Version %s Found";
		}
		if ("download_now".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "立即下载" : "立即下載";
			}
			return "Download";
		}
		if ("later".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "以后再说" : "以後再說";
			}
			return "Later";
		}
		if ("cannot_open_download_link".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "无法打开下载链接" : "無法開啟下載連結";
			}
			return "Cannot open download link";
		}
		if ("update_log_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "更新内容" : "更新內容";
			}
			return "Update Log";
		}
		if ("module_settings_subtitle".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "让Via变得更好" : "讓Via變得更好";
			}
			return "Make Via Better";
		}
		if ("themes_loading".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "正在加载主题..." : "正在加載主題...";
			}
			return "Loading themes...";
		}
		if ("themes_load_failed".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "主题加载失败" : "主題加載失敗";
			}
			return "Failed to load themes";
		}
		if ("check_network".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "请检查网络连接后重试" : "請檢查網路連接後重試";
			}
			return "Please check your network connection and try again";
		}

		/* ================== 浏览器标识相关 ================== */
		if ("user_agent_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "浏览器标识" : "瀏覽器標識";
			}
			return "User Agent";
		}
		if ("user_agent_config".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "查看" : "查看";
			}
			return "View";
		}
		if ("user_agent_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "查看和复制各种浏览器的User-Agent" : "查看和複製各種瀏覽器的User-Agent";
			}
			return "View and copy User-Agents for various browsers";
		}
		if ("user_agent_dialog_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "浏览器标识大全" : "瀏覽器標識大全";
			}
			return "User Agent Collection";
		}
		if ("user_agent_subtitle".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "已根据您的设备信息个性化调整" : "已根據您的設備信息個性化調整";
			}
			return "Personalized based on your device information";
		}
		if ("user_agent_copy".equals(key)) {
			return "zh".equals(lang) ? "复制" : "Copy";
		}
		if ("user_agent_copied".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "UA已复制到剪贴板" : "UA已複製到剪貼簿";
			}
			return "UA copied to clipboard";
		}

		/* ================== 开发和错误提示 ================== */
		if ("development_toast".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "正在开发中，敬请期待" : "正在開發中，敬請期待";
			}
			return "Under development, stay tuned";
		}
		if ("just_trust_me_switch".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "SSL证书绕过" : "SSL證書繞過";
			}
			return "SSL Certificate Bypass";
		}
		if ("just_trust_me_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "绕过SSL证书验证，用于调试和抓包" : "繞過SSL證書驗證，用於調試和抓包";
			}
			return "Bypass SSL certificate verification for debugging and packet capture";
		}
		/* ================== 关于相关多语言 ================== */
		if ("about_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "关于" : "關於";
			}
			return "About";
		}
		if ("about_view".equals(key)) {
			return "zh".equals(lang) ? "查看" : "View";
		}
		if ("about_hint".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "查看模块信息和更新日志" : "查看模組信息和更新日誌";
			}
			return "View module information and update log";
		}
		if ("about_subtitle".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "让Via变得更好" : "讓Via變得更好";
			}
			return "Make Via Better";
		}
		if ("about_module_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "模块" : "模組";
			}
			return "Module";
		}
		if ("about_version".equals(key)) {
			return "zh".equals(lang) ? "版本" : "Version";
		}
		if ("about_author".equals(key)) {
			return "zh".equals(lang) ? "作者" : "Author";
		}
		if ("about_github".equals(key)) {
			return "GitHub";
		}
		if ("about_gitee".equals(key)) {
			return "Gitee";
		}
		if ("about_xposed".equals(key)) {
			return "Xposed Repo";
		}
		if ("about_xposed_repo".equals(key)) {
			return "zh".equals(lang) ? "Xposed模块仓库" : "Xposed Module Repository";
		}
		if ("start_url_message".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "链接已打开" : "鏈接已打開";
			}
			return "Link is open";
		}
		if ("about_update_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "更新" : "更新";
			}
			return "Update";
		}
		if ("about_update_log1".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "修复了在某些机型上导致崩溃的问题" : "修復了在某些機型上導致崩潰的問題";
			}
			return "Fixed crash issues on some devices";
		}
		if ("about_update_log2".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "新增了Cookie管理器" : "新增了Cookie管理器";
			}
			return "Added Cookie Manager";
		}
		if ("about_update_log3".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "添加了Via内部更新检测功能" : "添加了Via內部更新檢測功能";
			}
			return "Added Via internal update detection";
		}
		if ("about_update_log4".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "完善了主页主题、脚本仓库、广告走开等一系列功能" : "完善了主頁主題、腳本倉庫、廣告走開等一系列功能";
			}
			return "Improved homepage themes, script repository, ad blocking and other features";
		}
		if ("about_thanks_title".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "鸣谢" : "鳴謝";
			}
			return "Thanks";
		}
		if ("about_thanks_content".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "AIDE+, AndroidIDE" : "AIDE+, AndroidIDE";
			}
			return "AIDE+, AndroidIDE";
		}
		if ("about_thanks_others".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "因人数过多，不一一列举，详见模块各版面详细作者" : "因人數過多，不一一列舉，詳見模塊各版面詳細作者";
			}
			return "Because there are too many people, we will not list them one by one. For details, please see the detailed authors in each section of the module";
		}
		if ("cannot_open_url".equals(key)) {
			if ("zh".equals(lang)) {
				return "CN".equals(country) ? "无法打开链接" : "無法開啟連結";
			}
			return "Cannot open link";
		}

		return "";
	}

	private Locale getUserLocale(Context ctx) {
		String saved = getSavedLanguage(ctx);
		if ("auto".equals(saved) || saved.isEmpty()) {
			Configuration cfg = ctx.getResources().getConfiguration();
			return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
					? cfg.getLocales().get(0)
					: cfg.locale;
		}
		switch (saved) {
			case "zh-CN" :
				return Locale.SIMPLIFIED_CHINESE;
			case "zh-TW" :
				return Locale.TRADITIONAL_CHINESE;
			case "en" :
				return Locale.ENGLISH;
			default :
				return Locale.getDefault();
		}
	}
}

