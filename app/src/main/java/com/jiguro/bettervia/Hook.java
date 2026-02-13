package com.jiguro.bettervia;
import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.webkit.*;
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
	private static final String MODULE_VERSION_NAME = "1.6.0";
	private static final int MODULE_VERSION_CODE = 20260213;
	private static final String SUPPORTED_VIA_VERSION = "7.0.0";
	private static volatile boolean hasShownBlockedToast = false;
	private static Activity Context = null;
	private static Object moduleButtonRef = null;
	private static String currentPackageName = "";
	private static Activity currentActivity = null;
	private static final String KEY_WHITELIST = "enable_whitelist_hook";
	private static final String KEY_BLOCK_STARTUP_MESSAGE = "block_startup_message";
	private static final String KEY_BLOCK_GOOGLE_SERVICES = "block_google_services";
	private static final String KEY_DOWNLOAD_DIALOG_SHARE = "download_dialog_share";
	private static final String KEY_EYE_PROTECTION = "eye_protection_mode";
	private static final String KEY_EYE_TEMPERATURE = "eye_protection_temperature";
	private static final String KEY_EYE_TEXTURE = "eye_protection_texture";
	private static final String KEY_HOMEPAGE_BG = "homepage_background_image";
	private static final String KEY_HOMEPAGE_MASK_A = "homepage_mask_alpha";
	private static final String KEY_HOMEPAGE_MASK_C = "homepage_mask_color";
	private static final String KEY_BLOCK_MENU_BAR = "block_menu_bar_urls";
	private static final String KEY_HIDE_STATUS_BAR = "hide_status_bar";
	private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
	private static final String KEY_SCREENSHOT_PROTECTION = "screenshot_protection";
	private static final String KEY_NETWORK_SOURCE = "network_source";
	private static final String KEY_AUTO_UPDATE = "auto_update";
	private static final String KEY_CURRENT_THEME = "current_homepage_theme";
	private static final String KEY_BACKGROUND_VIDEO = "background_video_audio";
	private static final String KEY_VERSION_CHECK_DISABLED = "version_check_disabled";
	private static final String KEY_DEVELOPER_MODE = "developer_mode";
	private static final String KEY_BLOCK_SWIPE_BACK = "block_swipe_back";
	private static final String KEY_MONET_BASE = "monet_base_version"; 
	private static final String KEY_MONET_PKG = "monet_package_name";
	private static final String KEY_MONET_VER_NAME = "monet_version_name";
	private static final String KEY_MONET_VER_CODE = "monet_version_code";
	private static final String KEY_MONET_USE_ICON = "monet_use_icon";
	private static final String KEY_MONET_MAKE_LITE = "monet_make_lite";
	private static final String KEY_MONET_SIGN_SCHEME = "monet_sign_scheme"; 
	private static final String KEY_MONET_OUTPUT_LOCATION = "monet_output_location"; 
	private static boolean whitelistHookEnabled = true;
	private static boolean eyeProtectionEnabled = false;
	private static boolean blockGoogleServicesEnabled = false;
	private static boolean blockStartupMessageEnabled = false;
	private static boolean screenshotProtectionEnabled = false;
	private static boolean keepScreenOnEnabled = false;
	private static boolean hideStatusBarEnabled = false;
	private static boolean autoUpdateEnabled = true;
	private static boolean downloadDialogShareEnabled = false;
	private static int eyeTemperature = 50;
	private static int eyeTexture = 0;
	private static String homepageBgPath = "";
	private static int homepageMaskAlpha = 120;
	private static int homepageMaskColor = 0x80000000;
	private static boolean backgroundVideoEnabled = false;
	private static boolean developerModeEnabled = false;
	private static boolean blockSwipeBackEnabled = false;
	private static PrintWriter logWriter = null;
	private static String logFilePath = null;
	private static final int MONET_BUFFER_SIZE = 8192;
	private static volatile boolean monetProcessing = false;
	private static volatile boolean monetCancelled = false;
	private static XC_MethodHook.Unhook whitelistHook = null;
	private static XC_MethodHook.Unhook componentHook = null;
	private static XC_MethodHook.Unhook activityHook = null;
	private static XC_MethodHook.Unhook firebaseAnalyticsHook = null;
	private static XC_MethodHook.Unhook googleAnalyticsHook = null;
	private static XC_MethodHook.Unhook screenshotProtectionHook = null;
	private static XC_MethodHook.Unhook keepScreenOnHook = null;
	private static XC_MethodHook.Unhook hideStatusBarHook = null;
	private static XC_MethodHook.Unhook downloadDialogShareHook = null;
	private static XC_MethodHook.Unhook backgroundVideoHook = null;
	private static XC_MethodHook.Unhook swipeBackHook = null;
	private static final String[] COMPONENT_KEYS = {"block_update", 
			"block_telegram", 
			"block_qq", 
			"block_email", 
			"block_wechat", 
			"block_donate", 
			"block_assist", 
			"block_agreement", 
			"block_privacy", 
			"block_opensource", 
			"block_icp" 
	};
	private static final String NETWORK_SOURCE_GITEE = "gitee";
	private static final String NETWORK_SOURCE_GITHUB = "github";
	private static final String DEFAULT_NETWORK_SOURCE = NETWORK_SOURCE_GITEE;
	private static final String GITEE_THEMES_JSON_URL = "https:
	private static final String GITHUB_THEMES_JSON_URL = "https:
	private static final String GITHUB_UPDATE_URL = "https:
	private static final String GITEE_UPDATE_URL = "https:
	private static final String GITEE_SHISUI_JSON_URL = "https:
	private static final String GITHUB_SHISUI_JSON_URL = "https:
	private static final String MONET_JSON_URL_GITEE = "https:
	private static final String MONET_JSON_URL_GITHUB = "https:
	private static final String MONET_TEMP_DIR = "/storage/emulated/0/Android/data/mark.via/files/BetterVia/temp/";
	private static String generatedApkPath = null;
	private static List<ThemeInfo> loadedThemes = new ArrayList<>();
	private static boolean themesLoaded = false;
	private static boolean themesLoading = false;
	private static Map<Activity, View> overlayViews = new WeakHashMap<>();
	private static Map<Activity, Boolean> screenOnActivities = new WeakHashMap<>();
	private static Map<Activity, Boolean> statusBarHiddenActivities = new WeakHashMap<>();
	private static Map<Activity, Runnable> statusBarRehideRunnables = new WeakHashMap<>();
	private static final int REHIDE_DELAY = 3000;
	private static final String DEFAULT_THEME_ID = "default";
	private static final String COOKIE_TABLE_NAME = "cookies";
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
		String getName(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return nameMap.getOrDefault(langCode, nameMap.get("zh-CN"));
		}
		String getAuthor(Context ctx) {
			String langCode = getLanguageCode(ctx);
			return authorMap.getOrDefault(langCode, authorMap.get("zh-CN"));
		}
		static ThemeInfo fromJSON(JSONObject json) throws JSONException {
			String id = json.getString("id");
			Map<String, String> nameMap = new HashMap<>();
			JSONObject names = json.getJSONObject("names");
			Iterator<String> nameKeys = names.keys();
			while (nameKeys.hasNext()) {
				String lang = nameKeys.next();
				nameMap.put(lang, names.getString(lang));
			}
			Map<String, String> authorMap = new HashMap<>();
			JSONObject authors = json.getJSONObject("authors");
			Iterator<String> authorKeys = authors.keys();
			while (authorKeys.hasNext()) {
				String lang = authorKeys.next();
				authorMap.put(lang, authors.getString(lang));
			}
			String previewUrl = json.getString("previewUrl");
			Map<String, String> htmlUrls = new HashMap<>();
			JSONObject htmls = json.getJSONObject("htmlUrls");
			Iterator<String> htmlKeys = htmls.keys();
			while (htmlKeys.hasNext()) {
				String pkg = htmlKeys.next();
				htmlUrls.put(pkg, htmls.getString(pkg));
			}
			Map<String, String> cssUrls = new HashMap<>();
			JSONObject csss = json.getJSONObject("cssUrls");
			Iterator<String> cssKeys = csss.keys();
			while (cssKeys.hasNext()) {
				String pkg = cssKeys.next();
				cssUrls.put(pkg, csss.getString(pkg));
			}
			return new ThemeInfo(id, nameMap, authorMap, previewUrl, htmlUrls, cssUrls);
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
	}
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam pkg) throws Throwable {
		if (pkg.packageName.equals("com.jiguro.bettervia")) {
			try {
				Class<?> clazz = pkg.classLoader.loadClass("com.jiguro.bettervia.ModuleStatus");
				java.lang.reflect.Field field = clazz.getDeclaredField("activated");
				field.setAccessible(true);
				field.setBoolean(null, true);
			} catch (Throwable ignored) {
			}
			return;
		}
		try {
			pkg.classLoader.loadClass("com.tuyafeng.support.crash.UnknowException");
			currentPackageName = pkg.packageName;
			handleViaApp(pkg);
		} catch (ClassNotFoundException e) {
		}
	}
	private void handleViaApp(final XC_LoadPackage.LoadPackageParam param) {
		XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam attachParam) throws Throwable {
				final Context ctx = (Context) attachParam.args[0];
				final ClassLoader cl = ctx.getClassLoader();
				XposedHelpers.findAndHookMethod(Toast.class, "makeText", Context.class, CharSequence.class, int.class,
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								if (param.getResult() == null)
									return;
								CharSequence msg = (CharSequence) param.args[1];
								if (msg == null)
									return;
								String message = msg.toString();
								if (message.contains("token") && message.contains("not valid")) {
									StackTraceElement[] stack = Thread.currentThread().getStackTrace();
									for (StackTraceElement el : stack) {
										if (el.getClassName().contains("mark.via.BrowserApp")) {
											bvLog("[BetterVia] 已屏蔽 BrowserApp 的 BadTokenException Toast: " + message);
											param.setResult(null);
											return;
										}
									}
								}
							}
						});
				if (Context == null) {
					XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							if (Context == null) {
								Context = (Activity) param.thisObject;
								checkViaVersion(ctx);
								if (!getPrefBoolean(ctx, KEY_BLOCK_STARTUP_MESSAGE, false)) {
									jiguroMessage(
											LocalizedStringProvider.getInstance().get(ctx, "hook_success_message"));
								}
								bvLog("[BetterVia] 初加载成功，得到Via活动上下文");
							}
							currentActivity = (Activity) param.thisObject;
							try {
								developerModeEnabled = getPrefBoolean((Context) param.thisObject, KEY_DEVELOPER_MODE,
										false);
								if (developerModeEnabled) {
									initLogFile();
									bvLog("[BetterVia] 初加载成功，得到Via活动上下文");
									bvLog("[BetterVia] 恢复开发者模式：日志写入初始化完成");
								}
							} catch (Throwable t) {
								XposedBridge.log("[BetterVia] 恢复开发者模式失败: " + t);
							}
						}
					});
				}
				eyeProtectionEnabled = getPrefBoolean(ctx, KEY_EYE_PROTECTION, false);
				eyeTemperature = getPrefInt(ctx, KEY_EYE_TEMPERATURE, 50);
				eyeTexture = getPrefInt(ctx, KEY_EYE_TEXTURE, 0);
				setEyeProtectionMode(ctx, cl, eyeProtectionEnabled);
				whitelistHookEnabled = getPrefBoolean(ctx, KEY_WHITELIST, true);
				setWhitelistHook(ctx, cl, whitelistHookEnabled);
				setComponentBlockHook(ctx, cl, true);
				screenshotProtectionEnabled = getPrefBoolean(ctx, KEY_SCREENSHOT_PROTECTION, false);
				setScreenshotProtection(ctx, cl, screenshotProtectionEnabled);
				hideStatusBarEnabled = getPrefBoolean(ctx, KEY_HIDE_STATUS_BAR, false);
				setHideStatusBar(ctx, cl, hideStatusBarEnabled);
				blockSwipeBackEnabled = getPrefBoolean(ctx, KEY_BLOCK_SWIPE_BACK, false);
				setBlockSwipeBack(ctx, cl, blockSwipeBackEnabled);
				homepageBgPath = getPrefString(ctx, KEY_HOMEPAGE_BG, "");
				homepageMaskAlpha = getPrefInt(ctx, KEY_HOMEPAGE_MASK_A, 120);
				homepageMaskColor = getPrefInt(ctx, KEY_HOMEPAGE_MASK_C, 0x80000000);
				if (!homepageBgPath.equals("")) {
					hookHomepageBgWithMask(ctx, cl, homepageBgPath, homepageMaskColor);
				}
				boolean blockGoogleServices = getPrefBoolean(ctx, KEY_BLOCK_GOOGLE_SERVICES, false);
				setGoogleServicesInterceptHook(ctx, cl, blockGoogleServices);
				downloadDialogShareEnabled = getPrefBoolean(ctx, KEY_DOWNLOAD_DIALOG_SHARE, false);
				if (downloadDialogShareEnabled) {
					setDownloadDialogShareHook(ctx, cl, true);
				}
				autoUpdateEnabled = getPrefBoolean(ctx, KEY_AUTO_UPDATE, true);
				if (autoUpdateEnabled) {
					checkUpdateOnStart(ctx);
				}
				if (whitelistHookEnabled) {
					XposedHelpers.findAndHookMethod("k.a.c0.i.k", cl, "u", "k.a.c0.i.a", new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.setResult(null);
							bvLog("[BetterVia] 已解除Via白名单限制");
						}
					});
				}
				keepScreenOnEnabled = getPrefBoolean(ctx, KEY_KEEP_SCREEN_ON, false);
				setKeepScreenOn(ctx, cl, keepScreenOnEnabled);
				backgroundVideoEnabled = getPrefBoolean(ctx, KEY_BACKGROUND_VIDEO, false);
				setBackgroundVideoAudio(ctx, cl, backgroundVideoEnabled);
				XposedHelpers.findAndHookMethod("k.a.o0.g7", cl, "f3", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						List<Object> orig = (List<Object>) param.getResult();
						if (orig == null)
							orig = new ArrayList<>();
						List<Object> nList = new ArrayList<>(orig);
						Class<?> yClass = XposedHelpers.findClass("e.h.g.g.y", cl);
						String txt = LocalizedStringProvider.getInstance().get(ctx, "module_settings");
						Object btn = XposedHelpers.newInstance(yClass, 1000, txt);
						moduleButtonRef = btn;
						nList.add(btn);
						param.setResult(nList);
						bvLog("[BetterVia] 已在Via设置列表中添加模块按钮");
					}
				});
				XposedHelpers.findAndHookMethod("e.h.g.g.a0$a", cl, "a", View.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Object clicked = XposedHelpers.getObjectField(param.thisObject, "d");
						if (clicked == null)
							return;
						int id = XposedHelpers.getIntField(clicked, "b");
						if (id == 1000) {
							bvLog("[BetterVia] 模块按钮被点击");
							showSettingsDialog(ctx);
						}
					}
				});
				XposedHelpers.findAndHookMethod(Activity.class, "onActivityResult", int.class, int.class, Intent.class,
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								int req = (Integer) param.args[0];
								int res = (Integer) param.args[1];
								Intent data = (Intent) param.args[2];
								handleActivityResult(req, res, data, (Activity) param.thisObject);
							}
						});
				XposedHelpers.findAndHookMethod(Activity.class, "onDestroy", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity = (Activity) param.thisObject;
						screenOnActivities.remove(activity);
						statusBarHiddenActivities.remove(activity);
						Runnable rehideRunnable = statusBarRehideRunnables.get(activity);
						if (rehideRunnable != null) {
							View decorView = activity.getWindow().getDecorView();
							decorView.removeCallbacks(rehideRunnable);
							statusBarRehideRunnables.remove(activity);
						}
						if (currentActivity == activity) {
							currentActivity = null;
						}
					}
				});
				setBlockMenuBarHook(ctx, cl, true);
				final String last = getSavedLanguage(ctx);
				if (!"auto".equals(last)) {
					updateViaLocale(ctx, last);
				}
			}
		});
	}
	private void showSettingsDialog(final Context ctx) {
		Activity activityRef = currentActivity;
		if (activityRef == null) {
			activityRef = Context;
		}
		if (activityRef == null || !(activityRef instanceof Activity)) {
			return;
		}
		final Activity act = activityRef;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed()) {
					return;
				}
				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);
				TextView title = new TextView(act);
				title.setText("BetterVia");
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
				title.setTextColor(0xFF6200EE);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				title.setPadding(0, 0, 0, dp(act, 16));
				root.addView(title);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "module_settings_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "whitelist_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "whitelist_hint"), KEY_WHITELIST, true,
						new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_WHITELIST, true);
								setWhitelistHook(ctx, act.getClassLoader(), on);
							}
						});
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "block_google_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "block_google_hint"), KEY_BLOCK_GOOGLE_SERVICES,
						false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_BLOCK_GOOGLE_SERVICES, false);
								setGoogleServicesInterceptHook(ctx, act.getClassLoader(), on);
							}
						});
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "screenshot_protection_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "screenshot_protection_hint"),
						KEY_SCREENSHOT_PROTECTION, false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_SCREENSHOT_PROTECTION, false);
								setScreenshotProtection(ctx, act.getClassLoader(), on);
							}
						});
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "block_startup_message_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "block_startup_message_hint"),
						KEY_BLOCK_STARTUP_MESSAGE, false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_BLOCK_STARTUP_MESSAGE, false);
								blockStartupMessageEnabled = on;
								putPrefBoolean(ctx, KEY_BLOCK_STARTUP_MESSAGE, on);
							}
						});
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "keep_screen_on_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "keep_screen_on_hint"), KEY_KEEP_SCREEN_ON,
						false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_KEEP_SCREEN_ON, false);
								setKeepScreenOn(ctx, act.getClassLoader(), on);
							}
						});
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "hide_status_bar_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "hide_status_bar_hint"), KEY_HIDE_STATUS_BAR,
						false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_HIDE_STATUS_BAR, false);
								setHideStatusBar(ctx, act.getClassLoader(), on);
							}
						});
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "download_dialog_share_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "download_dialog_share_hint"),
						KEY_DOWNLOAD_DIALOG_SHARE, false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_DOWNLOAD_DIALOG_SHARE, false);
								setDownloadDialogShareHook(ctx, act.getClassLoader(), on);
							}
						});
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "eye_protection_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "eye_protection_hint"), KEY_EYE_PROTECTION,
						false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_EYE_PROTECTION, false);
								setEyeProtectionMode(ctx, act.getClassLoader(), on);
							}
						});
				addEyeProtectionConfig(root, act, ctx);
				addComponentBlockItem(root, act, ctx);
				addHomepageThemeItem(root, act, ctx);
				addScriptRepositoryItem(root, act, ctx);
				addAdBlockRulesItem(root, act, ctx);
				addSearchCommandsItem(root, act, ctx);
				addUserAgentItem(root, act, ctx);
				addCookieManagementItem(root, act, ctx);
				addImagePickerItem(root, act, ctx);
				addBlockMenuBarItem(root, act, ctx);
				addMonetMomentItem(root, act, ctx);
				View div = new View(act);
				LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						dp(act, 1));
				divLp.setMargins(0, dp(act, 12), 0, dp(act, 12));
				div.setLayoutParams(divLp);
				div.setBackgroundColor(0xFFDDDDDD);
				root.addView(div);
				LinearLayout langRow = new LinearLayout(act);
				langRow.setOrientation(LinearLayout.HORIZONTAL);
				langRow.setGravity(Gravity.CENTER_VERTICAL);
				TextView langTitle = new TextView(act);
				langTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "language_title"));
				langTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				langTitle.setTextColor(Color.BLACK);
				langRow.addView(langTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
				final String[] langValues = {"auto", "zh-CN", "zh-TW", "en"};
				final String[] langItems = new String[]{LocalizedStringProvider.getInstance().get(ctx, "language_auto"),
						"简体中文", "繁體中文", "English"};
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
				TextView langHintTv = new TextView(act);
				applyClickAnim(langHintTv);
				langHintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "language_hint"));
				langHintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				langHintTv.setTextColor(0xFF666666);
				langHintTv.setPadding(0, dp(act, 4), 0, dp(act, 12));
				root.addView(langHintTv);
				LinearLayout sourceRow = new LinearLayout(act);
				sourceRow.setOrientation(LinearLayout.HORIZONTAL);
				sourceRow.setGravity(Gravity.CENTER_VERTICAL);
				TextView sourceTitle = new TextView(act);
				sourceTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "network_source_title"));
				sourceTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				sourceTitle.setTextColor(Color.BLACK);
				sourceRow.addView(sourceTitle,
						new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
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
				TextView sourceHintTv = new TextView(act);
				applyClickAnim(sourceHintTv);
				sourceHintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "network_source_hint"));
				sourceHintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				sourceHintTv.setTextColor(0xFF666666);
				sourceHintTv.setPadding(0, dp(act, 4), 0, dp(act, 12));
				root.addView(sourceHintTv);
				sourceSelector.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showSourcePopup(ctx, sourceSelector, sourceValues, sourceItems, new SourceSelectedCallback() {
							@Override
							public void onSelected(int pos) {
								String selectedSource = sourceValues[pos];
								putPrefString(ctx, KEY_NETWORK_SOURCE, selectedSource);
								sourceSelector.setText(sourceItems[pos]);
								themesLoaded = false;
								loadedThemes.clear();
								Toast.makeText(ctx,
										LocalizedStringProvider.getInstance().get(ctx, "network_source_changed") + " "
												+ sourceItems[pos],
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
				LinearLayout aboutRow = new LinearLayout(act);
				aboutRow.setOrientation(LinearLayout.HORIZONTAL);
				aboutRow.setGravity(Gravity.CENTER_VERTICAL);
				TextView aboutTitle = new TextView(act);
				aboutTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "about_title"));
				aboutTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				aboutTitle.setTextColor(Color.BLACK);
				aboutRow.addView(aboutTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
				TextView aboutBtn = new TextView(act);
				applyClickAnim(aboutBtn);
				aboutBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "about_view"));
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
				TextView aboutHintTv = new TextView(act);
				aboutHintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "about_hint"));
				aboutHintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				aboutHintTv.setTextColor(0xFF666666);
				aboutHintTv.setPadding(0, dp(act, 4), 0, dp(act, 12));
				root.addView(aboutHintTv);
				LinearLayout storageRow = new LinearLayout(act);
				storageRow.setOrientation(LinearLayout.HORIZONTAL);
				storageRow.setGravity(Gravity.CENTER_VERTICAL);
				TextView storageTitle = new TextView(act);
				storageTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "storage_item_title"));
				storageTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				storageTitle.setTextColor(Color.BLACK);
				storageRow.addView(storageTitle,
						new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
				TextView storageBtn = new TextView(act);
				applyClickAnim(storageBtn);
				storageBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "storage_manage"));
				storageBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				storageBtn.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				storageBtn.setBackground(getRoundBg(act, 0xFFE0E0E0, 8));
				storageBtn.setTextColor(0xFF000000);
				storageBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showStorageManagerDialog(act); 
					}
				});
				storageRow.addView(storageBtn);
				root.addView(storageRow);
				TextView storageHintTv = new TextView(act);
				storageHintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "storage_hint"));
				storageHintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				storageHintTv.setTextColor(0xFF666666);
				storageHintTv.setPadding(0, dp(act, 4), 0, dp(act, 12));
				root.addView(storageHintTv);
				LinearLayout shisuiRow = new LinearLayout(act);
				shisuiRow.setOrientation(LinearLayout.HORIZONTAL);
				shisuiRow.setGravity(Gravity.CENTER_VERTICAL);
				TextView shisuiTitle = new TextView(act);
				shisuiTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "shisui_title"));
				shisuiTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				shisuiTitle.setTextColor(Color.BLACK);
				shisuiRow.addView(shisuiTitle,
						new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
				TextView shisuiBtn = new TextView(act);
				applyClickAnim(shisuiBtn);
				shisuiBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "shisui_view"));
				shisuiBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				shisuiBtn.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				shisuiBtn.setBackground(getRoundBg(act, 0xFFE0E0E0, 8));
				shisuiBtn.setTextColor(0xFF000000);
				shisuiBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showShisuiDialog(ctx);
					}
				});
				shisuiRow.addView(shisuiBtn);
				root.addView(shisuiRow);
				TextView shisuiHintTv = new TextView(act);
				shisuiHintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "shisui_hint"));
				shisuiHintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				shisuiHintTv.setTextColor(0xFF666666);
				shisuiHintTv.setPadding(0, dp(act, 4), 0, dp(act, 12));
				root.addView(shisuiHintTv);
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "auto_update_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "auto_update_hint"), KEY_AUTO_UPDATE, true,
						new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_AUTO_UPDATE, true);
								autoUpdateEnabled = on;
								putPrefBoolean(ctx, KEY_AUTO_UPDATE, on);
							}
						});
				addSwitch(root, act, LocalizedStringProvider.getInstance().get(ctx, "developer_mode_switch"),
						LocalizedStringProvider.getInstance().get(ctx, "developer_mode_hint"), KEY_DEVELOPER_MODE,
						false, new Runnable() {
							@Override
							public void run() {
								boolean on = getPrefBoolean(ctx, KEY_DEVELOPER_MODE, false);
								developerModeEnabled = on;
								putPrefBoolean(ctx, KEY_DEVELOPER_MODE, on);
								if (on) {
									initLogFile();
								} else {
									closeLogFile();
								}
							}
						});
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
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
				final AlertDialog[] dialogRef = new AlertDialog[1];
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setView(scrollRoot);
				dialogRef[0] = builder.create();
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
				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialogRef[0].dismiss();
					}
				});
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
				animateDialogEntrance(root, act);
			}
		});
	}
	private void addComponentBlockItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "component_block_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "component_block_config"));
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
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "component_block_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showComponentBlockDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "component_block_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				title.setTextColor(Color.BLACK);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 16);
				root.addView(title, titleLp);
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
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
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
				final AlertDialog dialog = new AlertDialog.Builder(act).setView(scrollRoot).create();
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
				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						for (Map.Entry<String, CheckBox> entry : checkboxes.entrySet()) {
							putPrefBoolean(ctx, entry.getKey(), entry.getValue().isChecked());
						}
						Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "component_block_saved"),
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}
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
								int type = XposedHelpers.getIntField(item, "b");
								String[] componentNames = getComponentNames(ctx);
								int index = mapTypeToIndex(type);
								if (index < 0)
									return;
								boolean block = getPrefBoolean(ctx, COMPONENT_KEYS[index], false);
								if (block) {
									bvLog("[BetterVia] 组件屏蔽：阻止类型 " + type + " → " + componentNames[index]);
									param.setResult(false);
								}
							}
						});
				bvLog("[BetterVia] 组件屏蔽逻辑已启用");
			}
		} else {
			if (componentHook != null) {
				componentHook.unhook();
				componentHook = null;
				bvLog("[BetterVia] 组件屏蔽逻辑已停用");
			}
		}
	}
	private int mapTypeToIndex(int type) {
		switch (type) {
			case 12 :
				return 0;
			case 5 :
				return 1;
			case 6 :
				return 2;
			case 13 :
				return 3;
			case 14 :
				return 4;
			case 7 : 
				return 5;
			case 4 : 
				return 6;
			case 2 :
				return 7;
			case 3 :
				return 8;
			case 1 :
				return 9;
			case 16 :
				return 10;
			default :
				return -1;
		}
	}
	private boolean isCalledFromK6A2() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement el : stack) {
			if ("k.a.o0.g6".equals(el.getClassName()) && "X1".equals(el.getMethodName())) {
				return true;
			}
		}
		return false;
	}
	private String[] getComponentNames(Context ctx) {
		return new String[]{LocalizedStringProvider.getInstance().get(ctx, "component_update"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_telegram"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_qq"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_email"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_wechat"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_donate"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_assist"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_agreement"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_privacy"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_opensource"), 
				LocalizedStringProvider.getInstance().get(ctx, "component_icp") 
		};
	}
	private interface LangSelectedCallback {
		void onSelected(int pos);
	}
	private void showLangPopup(final Context ctx, View anchor, final String[] values, String[] items,
			final LangSelectedCallback callback) {
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
		DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
		int screenWidth = metrics.widthPixels;
		int maxWidth = (int) (screenWidth * 0.8);
		int anchorWidth = anchor.getWidth();
		int popupWidth = Math.max(anchorWidth, dp(ctx, 200));
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
		GradientDrawable bg = getRoundBg(ctx, Color.WHITE, 12);
		bg.setStroke(dp(ctx, 1), 0xFFE0E0E0);
		list.setBackground(bg);
		list.setPadding(0, dp(ctx, 4), 0, dp(ctx, 4));
		pop.showAsDropDown(anchor, 0, dp(ctx, 4));
	}
	private interface SourceSelectedCallback {
		void onSelected(int pos);
	}
	private void showSourcePopup(final Context ctx, View anchor, final String[] values, String[] items,
			final SourceSelectedCallback callback) {
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
	private void setWhitelistHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (whitelistHook == null) {
				whitelistHook = XposedHelpers.findAndHookMethod("k.a.c0.i.k", cl, "u", "k.a.c0.i.a",
						new XC_MethodHook() {
							@Override
							protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
								param.setResult(null);
								bvLog("[BetterVia] 成功Hook白名单方法");
							}
						});
				bvLog("[BetterVia] 已解除Via白名单限制");
			}
		} else {
			if (whitelistHook != null) {
				whitelistHook.unhook();
				whitelistHook = null;
				bvLog("[BetterVia] Via白名单限制已恢复");
			}
		}
		whitelistHookEnabled = on;
		putPrefBoolean(ctx, KEY_WHITELIST, on);
	}
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
				bvLog("[BetterVia] 护眼模式已启用");
			}
		} else {
			if (activityHook != null) {
				activityHook.unhook();
				activityHook = null;
				bvLog("[BetterVia] 护眼模式已停用");
			}
			removeAllEyeProtectionOverlays();
		}
		eyeProtectionEnabled = on;
		putPrefBoolean(ctx, KEY_EYE_PROTECTION, on);
	}
	private void addEyeProtectionOverlay(Activity activity, final int temperature, final int texture) {
		try {
			ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
			View existingOverlay = overlayViews.get(activity);
			if (existingOverlay != null) {
				rootView.removeView(existingOverlay);
			}
			View overlay = new View(activity) {
				@Override
				protected void onDraw(Canvas canvas) {
					super.onDraw(canvas);
					int color = calculateTemperatureColor(temperature);
					canvas.drawColor(color);
					if (texture > 0) {
						drawPaperTexture(canvas, texture);
					}
				}
			};
			overlay.setTag("eye_protection_overlay");
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			overlay.setClickable(false);
			overlay.setFocusable(false);
			overlay.setFocusableInTouchMode(false);
			rootView.addView(overlay, params);
			overlayViews.put(activity, overlay);
			bvLog("[BetterVia] 已为 " + activity.getClass().getSimpleName() + " 添加护眼遮罩");
		} catch (Exception e) {
			bvLog("[BetterVia] 添加护眼遮罩失败: " + e);
		}
	}
	private int calculateTemperatureColor(int temperature) {
		float ratio = temperature / 100.0f;
		int alpha = (int) (0x40 * ratio);
		int r = (int) (255 * ratio);
		int g = (int) (245 * ratio);
		int b = (int) (200 * ratio);
		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}
	private void drawPaperTexture(Canvas canvas, int textureLevel) {
		Paint paint = new Paint();
		paint.setColor(0x20FFFFFF);
		Random random = new Random(12345);
		int density = textureLevel / 5;
		for (int i = 0; i < density; i++) {
			float x = random.nextFloat() * canvas.getWidth();
			float y = random.nextFloat() * canvas.getHeight();
			float radius = random.nextFloat() * 2 + 1;
			canvas.drawCircle(x, y, radius, paint);
		}
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
	private void updateEyeProtectionOverlay(Activity activity, int temperature, int texture) {
		View overlay = overlayViews.get(activity);
		if (overlay != null) {
			overlay.invalidate();
		} else if (eyeProtectionEnabled) {
			addEyeProtectionOverlay(activity, temperature, texture);
		}
	}
	private void updateAllEyeProtectionOverlays(int temperature, int texture) {
		for (Map.Entry<Activity, View> entry : overlayViews.entrySet()) {
			Activity activity = entry.getKey();
			if (!activity.isFinishing() && !activity.isDestroyed()) {
				updateEyeProtectionOverlay(activity, temperature, texture);
			}
		}
	}
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
		bvLog("[BetterVia] 已移除所有护眼遮罩");
	}
	private void addEyeProtectionConfig(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_config"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_config_btn"));
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
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_config_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showEyeProtectionConfigDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				final int savedTemperature = getPrefInt(ctx, KEY_EYE_TEMPERATURE, 50);
				final int savedTexture = getPrefInt(ctx, KEY_EYE_TEXTURE, 0);
				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_config_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(Color.BLACK);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_config_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams subtitleLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				subtitleLp.bottomMargin = dp(act, 16);
				root.addView(subtitle, subtitleLp);
				final SeekBar[] tempSeekBarRef = new SeekBar[1];
				final SeekBar[] textureSeekBarRef = new SeekBar[1];
				final View[] previewOverlayRef = new View[1];
				LinearLayout previewContainer = new LinearLayout(act);
				previewContainer.setOrientation(LinearLayout.VERTICAL);
				previewContainer.setPadding(0, 0, 0, dp(act, 16));
				TextView previewTitle = new TextView(act);
				previewTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_preview_title"));
				previewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				previewTitle.setTextColor(Color.BLACK);
				previewTitle.setTypeface(null, Typeface.BOLD);
				previewContainer.addView(previewTitle);
				FrameLayout previewContent = new FrameLayout(act);
				previewContent.setLayoutParams(
						new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 80)));
				previewContent.setBackgroundColor(Color.WHITE);
				previewContent.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));
				GradientDrawable previewBg = new GradientDrawable();
				previewBg.setColor(Color.WHITE);
				previewBg.setStroke(dp(act, 1), 0xFFE0E0E0);
				previewBg.setCornerRadius(dp(act, 8));
				previewContent.setBackground(previewBg);
				TextView sampleText = new TextView(act);
				sampleText.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_sample_text"));
				sampleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				sampleText.setTextColor(Color.BLACK);
				FrameLayout.LayoutParams textLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				textLp.gravity = Gravity.CENTER;
				previewContent.addView(sampleText, textLp);
				final View previewOverlay = new View(act) {
					@Override
					protected void onDraw(Canvas canvas) {
						super.onDraw(canvas);
						if (tempSeekBarRef[0] != null) {
							int color = calculateTemperatureColor(tempSeekBarRef[0].getProgress());
							canvas.drawColor(color);
						}
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
				LinearLayout tempContainer = new LinearLayout(act);
				tempContainer.setOrientation(LinearLayout.VERTICAL);
				tempContainer.setPadding(0, 0, 0, dp(act, 16));
				TextView tempTitle = new TextView(act);
				tempTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_temperature"));
				tempTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				tempTitle.setTextColor(Color.BLACK);
				tempTitle.setTypeface(null, Typeface.BOLD);
				tempContainer.addView(tempTitle);
				final SeekBar tempSeekBar = new SeekBar(act);
				tempSeekBar.setMax(100);
				tempSeekBar.setProgress(savedTemperature);
				tempSeekBarRef[0] = tempSeekBar;
				tempContainer.addView(tempSeekBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				LinearLayout tempLabels = new LinearLayout(act);
				tempLabels.setOrientation(LinearLayout.HORIZONTAL);
				TextView coldLabel = new TextView(act);
				coldLabel.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_cold"));
				coldLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				coldLabel.setTextColor(0xFF666666);
				LinearLayout.LayoutParams coldLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1);
				tempLabels.addView(coldLabel, coldLp);
				TextView warmLabel = new TextView(act);
				warmLabel.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_warm"));
				warmLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				warmLabel.setTextColor(0xFF666666);
				warmLabel.setGravity(Gravity.END);
				LinearLayout.LayoutParams warmLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1);
				tempLabels.addView(warmLabel, warmLp);
				tempContainer.addView(tempLabels, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				root.addView(tempContainer);
				LinearLayout textureContainer = new LinearLayout(act);
				textureContainer.setOrientation(LinearLayout.VERTICAL);
				textureContainer.setPadding(0, 0, 0, dp(act, 16));
				TextView textureTitle = new TextView(act);
				textureTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_texture"));
				textureTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				textureTitle.setTextColor(Color.BLACK);
				textureTitle.setTypeface(null, Typeface.BOLD);
				textureContainer.addView(textureTitle);
				final SeekBar textureSeekBar = new SeekBar(act);
				textureSeekBar.setMax(100);
				textureSeekBar.setProgress(savedTexture);
				textureSeekBarRef[0] = textureSeekBar;
				textureContainer.addView(textureSeekBar, new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				LinearLayout textureLabels = new LinearLayout(act);
				textureLabels.setOrientation(LinearLayout.HORIZONTAL);
				TextView smoothLabel = new TextView(act);
				smoothLabel.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_smooth"));
				smoothLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				smoothLabel.setTextColor(0xFF666666);
				LinearLayout.LayoutParams smoothLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1);
				textureLabels.addView(smoothLabel, smoothLp);
				TextView roughLabel = new TextView(act);
				roughLabel.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_rough"));
				roughLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				roughLabel.setTextColor(0xFF666666);
				roughLabel.setGravity(Gravity.END);
				LinearLayout.LayoutParams roughLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1);
				textureLabels.addView(roughLabel, roughLp);
				textureContainer.addView(textureLabels, new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				root.addView(textureContainer);
				TextView previewHint = new TextView(act);
				previewHint.setText(LocalizedStringProvider.getInstance().get(ctx, "eye_protection_preview_hint"));
				previewHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				previewHint.setTextColor(0xFF888888);
				previewHint.setGravity(Gravity.CENTER);
				previewHint.setTypeface(null, Typeface.ITALIC);
				LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				hintLp.topMargin = dp(act, 8);
				hintLp.bottomMargin = dp(act, 16);
				root.addView(previewHint, hintLp);
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
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
				final AlertDialog dialog = new AlertDialog.Builder(act).setView(scrollRoot).create();
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
				SeekBar.OnSeekBarChangeListener previewListener = new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if (fromUser && previewOverlayRef[0] != null) {
							previewOverlayRef[0].invalidate();
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
				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int newTemperature = tempSeekBar.getProgress();
						int newTexture = textureSeekBar.getProgress();
						putPrefInt(ctx, KEY_EYE_TEMPERATURE, newTemperature);
						putPrefInt(ctx, KEY_EYE_TEXTURE, newTexture);
						updateAllEyeProtectionOverlays(newTemperature, newTexture);
						Toast.makeText(ctx,
								LocalizedStringProvider.getInstance().get(ctx, "eye_protection_config_saved"),
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}
	private void drawPaperTexturePreview(Canvas canvas, int textureLevel, int width, int height) {
		Paint paint = new Paint();
		paint.setColor(0x20FFFFFF);
		Random random = new Random(12345);
		int density = textureLevel / 3;
		int pointCount = (((width * height) / 1000) * density) / 10;
		for (int i = 0; i < pointCount; i++) {
			float x = random.nextFloat() * width;
			float y = random.nextFloat() * height;
			float radius = random.nextFloat() * 1.5f + 0.5f;
			canvas.drawCircle(x, y, radius, paint);
		}
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
	private void setGoogleServicesInterceptHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			bvLog("[BetterVia] 已启用Google个人信息收集拦截");
			try {
				setFirebaseAnalyticsHook(ctx, cl, true);
			} catch (Exception e) {
				bvLog("[BetterVia] Firebase Analytics拦截启用失败: " + e);
			}
			try {
				setAppMeasurementHook(ctx, cl, true);
			} catch (Exception e) {
				bvLog("[BetterVia] AppMeasurement拦截启用失败: " + e);
			}
			bvLog("[BetterVia] Google个人信息收集拦截完成");
		} else {
			bvLog("[BetterVia] 已停用Google个人信息收集拦截");
			try {
				setFirebaseAnalyticsHook(ctx, cl, false);
			} catch (Exception e) {
				bvLog("[BetterVia] Firebase Analytics拦截停用失败: " + e);
			}
			try {
				setAppMeasurementHook(ctx, cl, false);
			} catch (Exception e) {
				bvLog("[BetterVia] AppMeasurement拦截停用失败: " + e);
			}
			bvLog("[BetterVia] Google个人信息收集拦截停用完成");
		}
		blockGoogleServicesEnabled = on;
		putPrefBoolean(ctx, KEY_BLOCK_GOOGLE_SERVICES, on);
	}
	private void setFirebaseAnalyticsHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (firebaseAnalyticsHook == null) {
				try {
					Class<?> firebaseAnalyticsClass = XposedHelpers
							.findClassIfExists("com.google.firebase.analytics.FirebaseAnalytics", cl);
					if (firebaseAnalyticsClass != null) {
						firebaseAnalyticsHook = XposedHelpers.findAndHookMethod(firebaseAnalyticsClass, "a",
								String.class, Bundle.class, new XC_MethodHook() {
									@Override
									protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
										bvLog("[BetterVia] 拦截Firebase Analytics事件: " + param.args[0]);
									}
								});
						bvLog("[BetterVia] Firebase Analytics精确拦截已启用");
					}
				} catch (Exception e) {
					bvLog("[BetterVia] Firebase Analytics精确拦截设置失败: " + e);
				}
			}
		} else {
			if (firebaseAnalyticsHook != null) {
				firebaseAnalyticsHook.unhook();
				firebaseAnalyticsHook = null;
				bvLog("[BetterVia] Firebase Analytics拦截已停用");
			}
		}
	}
	private void setAppMeasurementHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (googleAnalyticsHook == null) {
				try {
					Class<?> appMeasurementClass = XposedHelpers
							.findClassIfExists("com.google.android.gms.measurement.AppMeasurement", cl);
					if (appMeasurementClass != null) {
						googleAnalyticsHook = XposedHelpers.findAndHookMethod(appMeasurementClass, "logEventInternal",
								String.class, String.class, Bundle.class, new XC_MethodHook() {
									@Override
									protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
										bvLog("[BetterVia] 拦截AppMeasurement事件: " + param.args[0] + ", "
												+ param.args[1]);
									}
								});
						bvLog("[BetterVia] AppMeasurement精确拦截已启用");
					}
				} catch (Exception e) {
					bvLog("[BetterVia] AppMeasurement精确拦截设置失败: " + e);
				}
			}
		} else {
			if (googleAnalyticsHook != null) {
				googleAnalyticsHook.unhook();
				googleAnalyticsHook = null;
				bvLog("[BetterVia] AppMeasurement拦截已停用");
			}
		}
	}
	private void addSearchCommandsItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "search_commands_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "search_commands_config"));
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
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "search_commands_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showSearchCommandsDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
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
				scrollRoot.setPadding(0, 0, 0, 0);
				scrollRoot.setClipToPadding(false);
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "search_commands_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "search_commands_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);
				final String[][] commands = {{"javascript:via.cmd(257);", "command_bookmark"},
						{"javascript:via.cmd(514);", "command_search"}, {"javascript:via.cmd(515);", "command_unknown"},
						{"javascript:via.cmd(516);", "command_print"}, {"javascript:via.cmd(517);", "command_adblock"},
						{"v:
						{"v:
						{"v:
						{"v:
						{"v:
						{"v:
						{"history:
				LinearLayout commandsContainer = new LinearLayout(act);
				commandsContainer.setOrientation(LinearLayout.VERTICAL);
				for (int i = 0; i < commands.length; i++) {
					final String[] command = commands[i];
					LinearLayout commandContainer = new LinearLayout(act);
					commandContainer.setOrientation(LinearLayout.HORIZONTAL);
					commandContainer.setGravity(Gravity.CENTER_VERTICAL);
					commandContainer.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
					GradientDrawable commandBg = new GradientDrawable();
					commandBg.setColor(0xFFF8F9FA);
					commandBg.setStroke(dp(act, 1), 0xFFE9ECEF);
					commandBg.setCornerRadius(dp(act, 12));
					commandContainer.setBackground(commandBg);
					LinearLayout leftContent = new LinearLayout(act);
					leftContent.setOrientation(LinearLayout.VERTICAL);
					LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0,
							ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
					leftContent.setLayoutParams(leftParams);
					TextView commandText = new TextView(act);
					commandText.setText(command[0]);
					commandText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
					commandText.setTextColor(0xFF2C3E50);
					commandText.setTypeface(Typeface.MONOSPACE);
					commandText.setSingleLine(true);
					commandText.setEllipsize(TextUtils.TruncateAt.MIDDLE);
					commandText.setPadding(0, 0, dp(act, 8), 0);
					leftContent.addView(commandText);
					TextView descText = new TextView(act);
					descText.setText(LocalizedStringProvider.getInstance().get(ctx, command[1]));
					descText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
					descText.setTextColor(0xFF7F8C8D);
					descText.setPadding(0, dp(act, 4), 0, 0);
					leftContent.addView(descText);
					commandContainer.addView(leftContent);
					Button copyBtn = new Button(act);
					applyClickAnim(copyBtn);
					copyBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "command_copy"));
					copyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
					copyBtn.setTextColor(Color.WHITE);
					copyBtn.setPadding(dp(act, 10), dp(act, 4), dp(act, 10), dp(act, 4));
					copyBtn.setMinHeight(dp(act, 28));
					copyBtn.setMinWidth(dp(act, 52));
					GradientDrawable btnBg = new GradientDrawable();
					btnBg.setColor(0xFF3498DB);
					btnBg.setCornerRadius(dp(act, 6));
					copyBtn.setBackground(btnBg);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						copyBtn.setStateListAnimator(null);
					}
					LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					btnLp.gravity = Gravity.CENTER_VERTICAL;
					commandContainer.addView(copyBtn, btnLp);
					LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					itemLp.bottomMargin = dp(act, 8);
					commandsContainer.addView(commandContainer, itemLp);
					final int index = i;
					copyBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							copyToClipboard(act, commands[index][0]);
							Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "command_copied"),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
				root.addView(commandsContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
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
				Window window = dialog.getWindow();
				if (window != null) {
					window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					DisplayMetrics metrics = new DisplayMetrics();
					act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int width = (int) (metrics.widthPixels * 0.9);
					int height = (int) (metrics.heightPixels * 0.8);
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
				dialog.show();
			}
		});
	}
	private void addHomepageThemeItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_config"));
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
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showHomepageThemeDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
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
				scrollRoot.setPadding(0, 0, 0, 0);
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);
				final LinearLayout themesContainer = new LinearLayout(act);
				themesContainer.setOrientation(LinearLayout.VERTICAL);
				themesContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				final LinearLayout emptyStateContainer = new LinearLayout(act);
				emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
				emptyStateContainer.setGravity(Gravity.CENTER);
				emptyStateContainer.setPadding(0, dp(act, 48), 0, dp(act, 48));
				emptyStateContainer.setVisibility(View.GONE);
				final ImageView errorIcon = new ImageView(act);
				errorIcon.setImageResource(android.R.drawable.ic_menu_report_image);
				errorIcon.setColorFilter(0xFF888888);
				errorIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(act, 64), dp(act, 64));
				iconLp.gravity = Gravity.CENTER;
				iconLp.bottomMargin = dp(act, 16);
				emptyStateContainer.addView(errorIcon, iconLp);
				final TextView emptyStateText = new TextView(act);
				emptyStateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				emptyStateText.setTextColor(0xFF888888);
				emptyStateText.setGravity(Gravity.CENTER);
				emptyStateText.setPadding(dp(act, 32), 0, dp(act, 32), 0);
				emptyStateContainer.addView(emptyStateText);
				root.addView(themesContainer);
				root.addView(emptyStateContainer);
				LinearLayout buttonContainer = new LinearLayout(act);
				buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
				buttonContainer.setGravity(Gravity.CENTER);
				buttonContainer.setPadding(0, dp(act, 16), 0, dp(act, 8));
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				ok.setPadding(0, dp(act, 14), 0, dp(act, 14));
				ok.setBackground(getRoundBg(act, 0xFF6200EE, 12));
				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1.0f);
				okLp.rightMargin = dp(act, 8);
				buttonContainer.addView(ok, okLp);
				Button edit = new Button(act);
				applyClickAnim(edit);
				edit.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_edit"));
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
				edit.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						showThemeEditorDialog(ctx);
					}
				});
				showLoadingState(act, ctx, themesContainer, emptyStateContainer, emptyStateText);
				if (!themesLoaded && !themesLoading) {
					loadThemesFromNetwork(ctx, new ThemesLoadCallback() {
						@Override
						public void onThemesLoaded(List<ThemeInfo> themes) {
							loadedThemes = themes;
							themesLoaded = true;
							themesLoading = false;
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
							themesLoaded = true;
							loadedThemes = new ArrayList<>();
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
					refreshThemesList(act, ctx, themesContainer, emptyStateContainer, emptyStateText);
				}
				dialog.show();
			}
		});
	}
	private void showLoadingState(Activity act, Context ctx, LinearLayout themesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText) {
		themesContainer.removeAllViews();
		themesContainer.setVisibility(View.GONE);
		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(LocalizedStringProvider.getInstance().get(ctx, "themes_loading"));
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.GONE);
			}
		}
		ProgressBar progressBar = new ProgressBar(act);
		progressBar.setIndeterminate(true);
		LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(dp(act, 48), dp(act, 48));
		progressLp.gravity = Gravity.CENTER;
		progressLp.bottomMargin = dp(act, 16);
		emptyStateContainer.addView(progressBar, 0, progressLp);
	}
	private void showErrorState(Activity act, Context ctx, LinearLayout themesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, String error) {
		themesContainer.removeAllViews();
		themesContainer.setVisibility(View.GONE);
		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(LocalizedStringProvider.getInstance().get(ctx, "themes_load_failed") + "\n"
				+ LocalizedStringProvider.getInstance().get(ctx, "check_network"));
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.VISIBLE);
			} else if (child instanceof ProgressBar) {
				emptyStateContainer.removeView(child);
			}
		}
		Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "themes_load_failed") + ": " + error,
				Toast.LENGTH_SHORT).show();
	}
	private LinearLayout createThemeCard(final Activity act, final Context ctx, final ThemeInfo theme) {
		LinearLayout themeCard = new LinearLayout(act);
		themeCard.setOrientation(LinearLayout.VERTICAL);
		themeCard.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
		GradientDrawable cardBg = new GradientDrawable();
		cardBg.setColor(0xFFF8F9FA);
		cardBg.setStroke(dp(act, 1), 0xFFE9ECEF);
		cardBg.setCornerRadius(dp(act, 12));
		themeCard.setBackground(cardBg);
		FrameLayout imageContainer = new FrameLayout(act);
		imageContainer
				.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 150)));
		imageContainer.setBackground(getRoundBg(act, 0xFFE0E0E0, 8));
		final ProgressBar loadingSpinner = new ProgressBar(act);
		loadingSpinner.setIndeterminate(true);
		FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		spinnerParams.gravity = Gravity.CENTER;
		imageContainer.addView(loadingSpinner, spinnerParams);
		final ImageView previewImage = new ImageView(act);
		previewImage.setLayoutParams(
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
		previewImage.setVisibility(View.GONE);
		imageContainer.addView(previewImage);
		themeCard.addView(imageContainer);
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
							int iconSize = dp(act, 48);
							Bitmap errorBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
							Canvas canvas = new Canvas(errorBitmap);
							Paint backgroundPaint = new Paint();
							backgroundPaint.setColor(0xFFE0E0E0);
							canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2, backgroundPaint);
							Drawable errorIcon = act.getResources().getDrawable(android.R.drawable.ic_menu_gallery);
							errorIcon.setBounds(iconSize / 4, iconSize / 4, (iconSize * 3) / 4, (iconSize * 3) / 4);
							errorIcon.draw(canvas);
							previewImage.setImageBitmap(errorBitmap);
							previewImage.setScaleType(ImageView.ScaleType.CENTER);
							previewImage.setColorFilter(0xFF888888);
							previewImage.setVisibility(View.VISIBLE);
							loadingSpinner.setVisibility(View.GONE);
						}
					});
				}
			}
		}).start();
		LinearLayout infoLayout = new LinearLayout(act);
		infoLayout.setOrientation(LinearLayout.VERTICAL);
		infoLayout.setPadding(0, dp(act, 8), 0, 0);
		TextView themeName = new TextView(act);
		themeName.setText(theme.getName(ctx));
		themeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		themeName.setTextColor(Color.BLACK);
		themeName.setTypeface(null, Typeface.BOLD);
		infoLayout.addView(themeName);
		TextView themeAuthor = new TextView(act);
		themeAuthor.setText(
				LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_by") + " " + theme.getAuthor(ctx));
		themeAuthor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		themeAuthor.setTextColor(0xFF666666);
		infoLayout.addView(themeAuthor);
		themeCard.addView(infoLayout);
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
	private void showThemeEditorDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				final Dialog dialog = new Dialog(act, android.R.style.Theme_NoTitleBar_Fullscreen);
				dialog.setCancelable(true);
				LinearLayout rootLayout = new LinearLayout(act);
				rootLayout.setOrientation(LinearLayout.VERTICAL);
				rootLayout.setBackgroundColor(Color.WHITE);
				RelativeLayout titleBar = new RelativeLayout(act);
				titleBar.setBackgroundColor(0xFFF5F5F5);
				titleBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
				titleBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
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
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_editor_title"));
				title.setTextColor(Color.BLACK);
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				title.setTypeface(null, Typeface.BOLD);
				RelativeLayout.LayoutParams titleLp = new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.addRule(RelativeLayout.CENTER_IN_PARENT);
				titleBar.addView(title, titleLp);
				rootLayout.addView(titleBar);
				LinearLayout contentLayout = new LinearLayout(act);
				contentLayout.setOrientation(LinearLayout.VERTICAL);
				contentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				contentLayout.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				TextView fileLabel = new TextView(act);
				fileLabel.setText(LocalizedStringProvider.getInstance().get(ctx, "theme_editor_select_file"));
				fileLabel.setTextColor(0xFF333333);
				fileLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				fileLabel.setPadding(0, 0, 0, dp(act, 8));
				contentLayout.addView(fileLabel);
				LinearLayout fileButtonGroup = new LinearLayout(act);
				fileButtonGroup.setOrientation(LinearLayout.HORIZONTAL);
				fileButtonGroup.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				final Button htmlButton = new Button(act);
				applyClickAnim(htmlButton);
				htmlButton.setText("homepage2.html");
				htmlButton.setTextColor(Color.WHITE);
				htmlButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				htmlButton.setBackground(getRoundBg(act, 0xFF6200EE, 6));
				htmlButton.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				LinearLayout.LayoutParams htmlLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1.0f);
				htmlLp.rightMargin = dp(act, 8);
				fileButtonGroup.addView(htmlButton, htmlLp);
				final Button cssButton = new Button(act);
				applyClickAnim(cssButton);
				cssButton.setText("homepage.css");
				cssButton.setTextColor(0xFF666666);
				cssButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				cssButton.setBackground(getRoundBg(act, 0xFFE0E0E0, 6));
				cssButton.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				LinearLayout.LayoutParams cssLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1.0f);
				fileButtonGroup.addView(cssButton, cssLp);
				contentLayout.addView(fileButtonGroup);
				TextView editorLabel = new TextView(act);
				editorLabel.setText(LocalizedStringProvider.getInstance().get(ctx, "theme_editor_edit_content"));
				editorLabel.setTextColor(0xFF333333);
				editorLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				editorLabel.setPadding(0, dp(act, 16), 0, dp(act, 8));
				contentLayout.addView(editorLabel);
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
				LinearLayout buttonBar = new LinearLayout(act);
				buttonBar.setOrientation(LinearLayout.HORIZONTAL);
				buttonBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				buttonBar.setPadding(0, dp(act, 16), 0, 0);
				Button cancelButton = new Button(act);
				applyClickAnim(cancelButton);
				cancelButton.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_cancel"));
				cancelButton.setTextColor(0xFF666666);
				cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				cancelButton.setBackground(getRoundBg(act, 0xFFE0E0E0, 8));
				cancelButton.setPadding(dp(act, 24), dp(act, 12), dp(act, 24), dp(act, 12));
				LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				cancelLp.rightMargin = dp(act, 8);
				buttonBar.addView(cancelButton, cancelLp);
				Button saveButton = new Button(act);
				applyClickAnim(saveButton);
				saveButton.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
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
				final String[] currentFile = {"homepage2.html"};
				loadFileContent(act, "homepage2.html", codeEditor, editorScroll, true);
				htmlButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						saveCurrentEditorState(currentFile[0], codeEditor, editorScroll);
						currentFile[0] = "homepage2.html";
						htmlButton.setTextColor(Color.WHITE);
						htmlButton.setBackground(getRoundBg(act, 0xFF6200EE, 6));
						cssButton.setTextColor(0xFF666666);
						cssButton.setBackground(getRoundBg(act, 0xFFE0E0E0, 6));
						loadFileContent(act, "homepage2.html", codeEditor, editorScroll, true);
					}
				});
				cssButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						saveCurrentEditorState(currentFile[0], codeEditor, editorScroll);
						currentFile[0] = "homepage.css";
						cssButton.setTextColor(Color.WHITE);
						cssButton.setBackground(getRoundBg(act, 0xFF6200EE, 6));
						htmlButton.setTextColor(0xFF666666);
						htmlButton.setBackground(getRoundBg(act, 0xFFE0E0E0, 6));
						loadFileContent(act, "homepage.css", codeEditor, editorScroll, true);
					}
				});
				editorScroll.getViewTreeObserver()
						.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
							@Override
							public void onScrollChanged() {
								if (currentFile[0] != null && editorStateCache.containsKey(currentFile[0])) {
									EditorState oldState = editorStateCache.get(currentFile[0]);
									editorStateCache.put(currentFile[0],
											new EditorState(oldState.content, editorScroll.getScrollY()));
								}
							}
						});
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						long currentTime = System.currentTimeMillis();
						Iterator<Map.Entry<String, EditorState>> it = editorStateCache.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<String, EditorState> entry = it.next();
							if (currentTime - entry.getValue().timestamp > 10 * 60 * 1000) {
								it.remove();
							}
						}
					}
				});
				backButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				cancelButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				saveButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						saveFileContent(act, currentFile[0], codeEditor.getText().toString());
						if (editorScroll != null) {
							int scrollY = editorScroll.getScrollY();
							editorStateCache.put(currentFile[0],
									new EditorState(codeEditor.getText().toString(), scrollY));
						}
						Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "theme_editor_save_success"),
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}
	private void showThemeApplyDialog(final Context ctx, final ThemeInfo theme) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_apply_title"));
				builder.setMessage(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_apply_message")
						+ " \"" + theme.getName(ctx) + "\"?");
				builder.setPositiveButton(LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_apply"),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								applyHomepageTheme(ctx, theme);
							}
						});
				builder.setNegativeButton(LocalizedStringProvider.getInstance().get(ctx, "dialog_cancel"), null);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
	}
	private void applyHomepageTheme(final Context ctx, final ThemeInfo theme) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String packageName = ctx.getPackageName();
					String filesDir = "/data/user/0/" + packageName + "/files/";
					boolean needReplacePackageName = false;
					String htmlUrl = theme.htmlUrls.get(packageName);
					String cssUrl = theme.cssUrls.get(packageName);
					if (htmlUrl == null || cssUrl == null) {
						if (theme.htmlUrls.containsKey("mark.via") && theme.cssUrls.containsKey("mark.via")) {
							htmlUrl = theme.htmlUrls.get("mark.via");
							cssUrl = theme.cssUrls.get("mark.via");
							needReplacePackageName = true;
						} else {
							htmlUrl = theme.htmlUrls.values().iterator().next();
							cssUrl = theme.cssUrls.values().iterator().next();
						}
					} else {
						if (!"mark.via".equals(packageName) && !"mark.via.gp".equals(packageName)) {
							needReplacePackageName = true;
						}
					}
					final boolean htmlSuccess = downloadAndSaveFile(htmlUrl, filesDir + "homepage2.html");
					final boolean cssSuccess = downloadAndSaveFile(cssUrl, filesDir + "homepage.css");
					final boolean replaceSuccess = (htmlSuccess && cssSuccess && needReplacePackageName)
							? replacePackageNameInFiles(filesDir + "homepage2.html", filesDir + "homepage.css",
									"mark.via", packageName)
							: true;
					((Activity) Context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (htmlSuccess && cssSuccess && replaceSuccess) {
								putPrefString(ctx, KEY_CURRENT_THEME, theme.id);
								Toast.makeText(ctx,
										LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_apply_success"),
										Toast.LENGTH_LONG).show();
								restartVia(ctx);
							} else {
								Toast.makeText(ctx,
										LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_apply_failed"),
										Toast.LENGTH_LONG).show();
							}
						}
					});
				} catch (Exception e) {
					bvLog("[BetterVia] 应用主题失败: " + e);
					((Activity) Context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(ctx,
									LocalizedStringProvider.getInstance().get(ctx, "homepage_theme_apply_error"),
									Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}
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
			bvLog("[BetterVia] 下载文件失败: " + e);
		} finally {
			try {
				if (outputStream != null)
					outputStream.close();
				if (connection != null)
					connection.disconnect();
			} catch (Exception e) {
				bvLog("[BetterVia] 关闭流失败: " + e);
			}
		}
		return false;
	}
	private boolean replacePackageNameInFiles(String htmlFilePath, String cssFilePath, String oldPackageName,
			String newPackageName) {
		boolean htmlSuccess = replacePackageNameInFile(htmlFilePath, oldPackageName, newPackageName);
		boolean cssSuccess = replacePackageNameInFile(cssFilePath, oldPackageName, newPackageName);
		if (htmlSuccess && cssSuccess) {
			bvLog("[BetterVia] 已将文件中的包名从 " + oldPackageName + " 替换为 " + newPackageName);
			return true;
		} else {
			bvLog("[BetterVia] 替换文件中的包名失败");
			return false;
		}
	}
	private boolean replacePackageNameInFile(String filePath, String oldPackageName, String newPackageName) {
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				bvLog("[BetterVia] 文件不存在: " + filePath);
				return false;
			}
			inputStream = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length()];
			inputStream.read(buffer);
			inputStream.close();
			String content = new String(buffer, "UTF-8");
			String newContent = content.replace(oldPackageName, newPackageName);
			outputStream = new FileOutputStream(file);
			outputStream.write(newContent.getBytes("UTF-8"));
			outputStream.flush();
			return true;
		} catch (Exception e) {
			bvLog("[BetterVia] 替换文件内容失败: " + e);
			return false;
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
				if (outputStream != null)
					outputStream.close();
			} catch (Exception e) {
				bvLog("[BetterVia] 关闭流失败: " + e);
			}
		}
	}
	private void restartVia(Context ctx) {
		try {
			Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
			if (intent != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				android.os.Process.killProcess(android.os.Process.myPid());
				ctx.startActivity(intent);
			}
		} catch (Exception e) {
			bvLog("[BetterVia] 重启Via失败: " + e);
		}
	}
	private void setScreenshotProtection(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (screenshotProtectionHook == null) {
				screenshotProtectionHook = XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class,
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								Activity activity = (Activity) param.thisObject;
								if (screenshotProtectionEnabled) {
									activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
											WindowManager.LayoutParams.FLAG_SECURE);
									XposedBridge
											.log("[BetterVia] 已为 " + activity.getClass().getSimpleName() + " 启用截屏防护");
								}
							}
						});
				bvLog("[BetterVia] 截屏防护已启用");
			}
		} else {
			if (screenshotProtectionHook != null) {
				screenshotProtectionHook.unhook();
				screenshotProtectionHook = null;
				bvLog("[BetterVia] 截屏防护已停用");
				removeScreenshotProtection();
			}
		}
		screenshotProtectionEnabled = on;
		putPrefBoolean(ctx, KEY_SCREENSHOT_PROTECTION, on);
	}
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
		bvLog("[BetterVia] 已移除所有Activity的截屏防护");
	}
	private void setKeepScreenOn(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (keepScreenOnHook == null) {
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
											bvLog("[BetterVia] 已为 " + activity.getClass().getSimpleName() + " 启用屏幕常亮");
										}
									});
								}
							}
						});
				bvLog("[BetterVia] 屏幕常亮已启用");
			}
		} else {
			if (keepScreenOnHook != null) {
				keepScreenOnHook.unhook();
				keepScreenOnHook = null;
				bvLog("[BetterVia] 屏幕常亮已停用");
				removeKeepScreenOn();
			}
		}
		keepScreenOnEnabled = on;
		putPrefBoolean(ctx, KEY_KEEP_SCREEN_ON, on);
	}
	private void setBackgroundVideoAudio(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (backgroundVideoHook == null) {
				try {
					Class<?> shellClass = findClassWithFallback("Shell", ctx, cl);
					if (shellClass == null) {
						bvLog("[BetterVia] 未找到Shell类，无法启用后台听视频功能");
						return;
					}
					backgroundVideoHook = XposedHelpers.findAndHookMethod(shellClass, "onPause",
							new XC_MethodReplacement() {
								@Override
								protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
									bvLog("[BetterVia] 阻止了Shell.onPause调用，保持前台状态");
									return null;
								}
							});
					XposedHelpers.findAndHookMethod(shellClass, "onWindowFocusChanged", boolean.class,
							new XC_MethodHook() {
								@Override
								protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
									param.args[0] = true;
									bvLog("[BetterVia] 强制设置窗口焦点为true");
								}
							});
					XposedHelpers.findAndHookMethod(shellClass, "isFinishing", new XC_MethodReplacement() {
						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							bvLog("[BetterVia] 强制返回isFinishing=false");
							return false;
						}
					});
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
						XposedHelpers.findAndHookMethod(shellClass, "isDestroyed", new XC_MethodReplacement() {
							@Override
							protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
								bvLog("[BetterVia] 强制返回isDestroyed=false");
								return false;
							}
						});
					}
					bvLog("[BetterVia] 后台听视频功能已启用");
				} catch (Throwable e) {
					bvLog("[BetterVia] 启用后台听视频功能失败: " + e.getMessage());
				}
			}
		} else {
			if (backgroundVideoHook != null) {
				try {
					backgroundVideoHook.unhook();
					backgroundVideoHook = null;
					bvLog("[BetterVia] 后台听视频功能已停用");
				} catch (Throwable e) {
					bvLog("[BetterVia] 停用后台听视频功能出错: " + e.getMessage());
				}
			}
		}
		backgroundVideoEnabled = on;
		putPrefBoolean(ctx, KEY_BACKGROUND_VIDEO, on);
	}
	private void injectVideoKeepAliveScript(final WebView webView) {
		if (!backgroundVideoEnabled)
			return;
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					String jsCode = "javascript:(function() {" + " 
							+ "    var originalHidden = document.hidden;"
							+ "    var originalVisibilityState = document.visibilityState;" + "    " + " 
							+ "    Object.defineProperty(document, 'hidden', {"
							+ "        get: function() { return false; }" + 
							"    });" + "    " + "    Object.defineProperty(document, 'visibilityState', {"
							+ "        get: function() { return 'visible'; }" + 
							"    });" + "    " + " 
							+ "    var originalAddEventListener = document.addEventListener;"
							+ "    document.addEventListener = function(type, listener, options) {"
							+ "        if (type === 'visibilitychange') {" + " 
							+ "            console.log('Blocked visibilitychange listener');" + "            return;"
							+ "        }" + "        originalAddEventListener.call(this, type, listener, options);"
							+ "    };" + "    " + " 
							+ "    var videos = document.getElementsByTagName('video');"
							+ "    for (var i = 0; i < videos.length; i++) {" + "        var video = videos[i];"
							+ "        if (video.paused) {" + "            video.play().catch(function(e) {});"
							+ "        }" + "        " + " 
							+ "        video.addEventListener('pause', function(e) {"
							+ "            if (!document.hidden) { 
							+ "                this.play().catch(function(e) {});" + "            }" + "        });"
							+ "    }" + "    " + "    console.log('Video keep-alive script injected');" + "})();";
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						webView.evaluateJavascript(jsCode, null);
					} else {
						webView.loadUrl(jsCode);
					}
					bvLog("[BetterVia] 已注入视频保持播放脚本");
				} catch (Exception e) {
					bvLog("[BetterVia] 注入视频脚本失败: " + e.getMessage());
				}
			}
		}, 2000);
	}
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
		bvLog("[BetterVia] 已移除所有Activity的屏幕常亮设置");
	}
	private void addScriptRepositoryItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "script_repository_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "script_repository_config"));
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
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "script_repository_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private static class ScriptInfo {
		String id;
		Map<String, String> nameMap;
		Map<String, String> descriptionMap;
		Map<String, String> detailMap;
		Map<String, String> downloadUrls;
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
			Map<String, String> nameMap = new HashMap<>();
			JSONObject names = json.getJSONObject("names");
			Iterator<String> nameKeys = names.keys();
			while (nameKeys.hasNext()) {
				String lang = nameKeys.next();
				nameMap.put(lang, names.getString(lang));
			}
			Map<String, String> descriptionMap = new HashMap<>();
			JSONObject descriptions = json.getJSONObject("descriptions");
			Iterator<String> descKeys = descriptions.keys();
			while (descKeys.hasNext()) {
				String lang = descKeys.next();
				descriptionMap.put(lang, descriptions.getString(lang));
			}
			Map<String, String> detailMap = new HashMap<>();
			JSONObject details = json.getJSONObject("details");
			Iterator<String> detailKeys = details.keys();
			while (detailKeys.hasNext()) {
				String lang = detailKeys.next();
				detailMap.put(lang, details.getString(lang));
			}
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
	private void showScriptRepositoryDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
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
				scrollRoot.setPadding(0, 0, 0, 0);
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "script_repository_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "script_repository_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 16));
				root.addView(subtitle);
				LinearLayout searchContainer = new LinearLayout(act);
				searchContainer.setOrientation(LinearLayout.VERTICAL);
				searchContainer.setPadding(0, 0, 0, dp(act, 16));
				LinearLayout searchRow = new LinearLayout(act);
				searchRow.setOrientation(LinearLayout.HORIZONTAL);
				searchRow.setGravity(Gravity.CENTER_VERTICAL);
				final EditText searchEdit = new EditText(act);
				searchEdit.setHint(LocalizedStringProvider.getInstance().get(ctx, "script_search_hint"));
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
				applyClickAnim(searchButton);
				searchButton.setText(LocalizedStringProvider.getInstance().get(ctx, "script_search_button"));
				searchButton.setTextColor(Color.WHITE);
				searchButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				searchButton.setBackground(getRoundBg(act, 0xFF6200EE, 8));
				searchButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
				searchRow.addView(searchButton);
				searchContainer.addView(searchRow);
				final TextView scriptCountText = new TextView(act);
				scriptCountText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				scriptCountText.setTextColor(0xFF888888);
				scriptCountText.setPadding(dp(act, 4), dp(act, 4), 0, 0);
				searchContainer.addView(scriptCountText);
				root.addView(searchContainer);
				final LinearLayout scriptsContainer = new LinearLayout(act);
				scriptsContainer.setOrientation(LinearLayout.VERTICAL);
				scriptsContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				final LinearLayout emptyStateContainer = new LinearLayout(act);
				emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
				emptyStateContainer.setGravity(Gravity.CENTER);
				emptyStateContainer.setPadding(0, dp(act, 48), 0, dp(act, 48));
				emptyStateContainer.setVisibility(View.GONE);
				final ImageView errorIcon = new ImageView(act);
				errorIcon.setImageResource(android.R.drawable.ic_menu_report_image);
				errorIcon.setColorFilter(0xFF888888);
				errorIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(act, 64), dp(act, 64));
				iconLp.gravity = Gravity.CENTER;
				iconLp.bottomMargin = dp(act, 16);
				emptyStateContainer.addView(errorIcon, iconLp);
				final TextView emptyStateText = new TextView(act);
				emptyStateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				emptyStateText.setTextColor(0xFF888888);
				emptyStateText.setGravity(Gravity.CENTER);
				emptyStateText.setPadding(dp(act, 32), 0, dp(act, 32), 0);
				emptyStateText.setText(LocalizedStringProvider.getInstance().get(ctx, "scripts_loading"));
				emptyStateContainer.addView(emptyStateText);
				root.addView(scriptsContainer);
				root.addView(emptyStateContainer);
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
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
				final List<ScriptInfo>[] allScripts = new List[]{new ArrayList<ScriptInfo>()};
				final Runnable updateScriptCount = new Runnable() {
					@Override
					public void run() {
						if (allScripts[0] != null && !allScripts[0].isEmpty()) {
							String countText = String.format(
									LocalizedStringProvider.getInstance().get(ctx, "script_total_count"),
									allScripts[0].size());
							scriptCountText.setText(countText);
						} else {
							scriptCountText
									.setText(LocalizedStringProvider.getInstance().get(ctx, "script_loading_count"));
						}
					}
				};
				searchButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String query = searchEdit.getText().toString().trim().toLowerCase();
						filterScripts(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText, allScripts[0],
								query, true);
					}
				});
				searchEdit.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						String query = s.toString().trim().toLowerCase();
						filterScripts(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText, allScripts[0],
								query, false);
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
								countText = String.format(
										LocalizedStringProvider.getInstance().get(ctx, "script_total_count"),
										allScripts[0].size());
							} else {
								countText = String.format(
										LocalizedStringProvider.getInstance().get(ctx, "script_filtered_count"),
										filteredScripts.size(), allScripts[0].size());
							}
							scriptCountText.setText(countText);
						}
					}
					@Override
					public void afterTextChanged(Editable s) {
					}
				});
				showScriptsLoadingState(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText);
				loadScriptsFromNetwork(ctx, new ScriptsLoadCallback() {
					@Override
					public void onScriptsLoaded(final List<ScriptInfo> scripts) {
						allScripts[0] = scripts;
						if (act != null && !act.isFinishing()) {
							act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									refreshScriptsList(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText,
											scripts);
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
									scriptCountText.setText(
											LocalizedStringProvider.getInstance().get(ctx, "script_load_failed_count"));
								}
							});
						}
					}
				});
				dialog.show();
			}
		});
	}
	private void loadScriptsFromNetwork(final Context ctx, final ScriptsLoadCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String networkSource = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE);
					String scriptsUrl = networkSource.equals(NETWORK_SOURCE_GITEE)
							? "https:
							: "https:
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
	private void filterScripts(Activity act, Context ctx, LinearLayout scriptsContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, List<ScriptInfo> allScripts, String query,
			boolean showToast) {
		if (allScripts == null || allScripts.isEmpty()) {
			showScriptsErrorState(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText,
					LocalizedStringProvider.getInstance().get(ctx, "no_scripts_available"));
			return;
		}
		if (query.isEmpty()) {
			refreshScriptsList(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText, allScripts);
			if (showToast) {
				Toast.makeText(act, String.format(LocalizedStringProvider.getInstance().get(ctx, "script_show_all"),
						allScripts.size()), Toast.LENGTH_SHORT).show();
			}
			return;
		}
		List<ScriptInfo> filteredScripts = new ArrayList<>();
		for (ScriptInfo script : allScripts) {
			String name = script.getName(ctx).toLowerCase();
			String description = script.getDescription(ctx).toLowerCase();
			String category = script.category.toLowerCase();
			if (name.contains(query) || description.contains(query) || category.contains(query)) {
				filteredScripts.add(script);
			}
		}
		if (filteredScripts.isEmpty()) {
			scriptsContainer.removeAllViews();
			scriptsContainer.setVisibility(View.GONE);
			emptyStateContainer.setVisibility(View.VISIBLE);
			emptyStateText.setText(
					String.format(LocalizedStringProvider.getInstance().get(ctx, "script_search_no_results"), query));
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
				Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "script_search_no_results_toast"),
						Toast.LENGTH_SHORT).show();
			}
		} else {
			refreshScriptsList(act, ctx, scriptsContainer, emptyStateContainer, emptyStateText, filteredScripts);
			if (showToast) {
				Toast.makeText(act,
						String.format(LocalizedStringProvider.getInstance().get(ctx, "script_search_results"),
								filteredScripts.size()),
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	private void showScriptsLoadingState(Activity act, Context ctx, LinearLayout scriptsContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText) {
		scriptsContainer.removeAllViews();
		scriptsContainer.setVisibility(View.GONE);
		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(LocalizedStringProvider.getInstance().get(ctx, "scripts_loading"));
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.GONE);
			}
		}
		ProgressBar progressBar = new ProgressBar(act);
		progressBar.setIndeterminate(true);
		LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(dp(act, 48), dp(act, 48));
		progressLp.gravity = Gravity.CENTER;
		progressLp.bottomMargin = dp(act, 16);
		emptyStateContainer.addView(progressBar, 0, progressLp);
	}
	private void showScriptsErrorState(Activity act, Context ctx, LinearLayout scriptsContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, String error) {
		scriptsContainer.removeAllViews();
		scriptsContainer.setVisibility(View.GONE);
		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(LocalizedStringProvider.getInstance().get(ctx, "scripts_load_failed") + "\n"
				+ LocalizedStringProvider.getInstance().get(ctx, "check_network"));
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.VISIBLE);
			} else if (child instanceof ProgressBar) {
				emptyStateContainer.removeView(child);
			}
		}
		Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "scripts_load_failed") + ": " + error,
				Toast.LENGTH_SHORT).show();
	}
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
		Map<String, List<ScriptInfo>> categorizedScripts = new HashMap<>();
		for (ScriptInfo script : scripts) {
			String category = script.category;
			if (!categorizedScripts.containsKey(category)) {
				categorizedScripts.put(category, new ArrayList<ScriptInfo>());
			}
			categorizedScripts.get(category).add(script);
		}
		for (Map.Entry<String, List<ScriptInfo>> entry : categorizedScripts.entrySet()) {
			String category = entry.getKey();
			List<ScriptInfo> categoryScripts = entry.getValue();
			TextView categoryTitle = new TextView(act);
			categoryTitle.setText(category);
			categoryTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			categoryTitle.setTextColor(0xFF6200EE);
			categoryTitle.setTypeface(null, Typeface.BOLD);
			categoryTitle.setPadding(0, dp(act, 16), 0, dp(act, 8));
			scriptsContainer.addView(categoryTitle);
			for (final ScriptInfo script : categoryScripts) {
				LinearLayout scriptCard = createScriptCard(act, ctx, script);
				LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				cardLp.bottomMargin = dp(ctx, 12);
				scriptsContainer.addView(scriptCard, cardLp);
			}
		}
	}
	private LinearLayout createScriptCard(final Activity act, final Context ctx, final ScriptInfo script) {
		LinearLayout scriptCard = new LinearLayout(act);
		scriptCard.setOrientation(LinearLayout.VERTICAL);
		scriptCard.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
		GradientDrawable cardBg = new GradientDrawable();
		cardBg.setColor(0xFFF8F9FA);
		cardBg.setStroke(dp(act, 1), 0xFFE9ECEF);
		cardBg.setCornerRadius(dp(act, 12));
		scriptCard.setBackground(cardBg);
		TextView scriptName = new TextView(act);
		scriptName.setText(script.getName(ctx));
		scriptName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		scriptName.setTextColor(Color.BLACK);
		scriptName.setTypeface(null, Typeface.BOLD);
		scriptCard.addView(scriptName);
		TextView scriptDescription = new TextView(act);
		scriptDescription.setText(script.getDescription(ctx));
		scriptDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		scriptDescription.setTextColor(0xFF666666);
		scriptDescription.setPadding(0, dp(act, 8), 0, 0);
		scriptCard.addView(scriptDescription);
		scriptCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showScriptDetailDialog(ctx, script);
			}
		});
		return scriptCard;
	}
	private void showScriptDetailDialog(final Context ctx, final ScriptInfo script) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final AlertDialog[] detailDialogRef = new AlertDialog[1];
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle(script.getName(ctx));
				ScrollView scrollView = new ScrollView(act);
				LinearLayout layout = new LinearLayout(act);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				TextView detailText = new TextView(act);
				detailText.setText(script.getDetail(ctx));
				detailText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				detailText.setTextColor(Color.BLACK);
				detailText.setLineSpacing(dp(act, 4), 1.2f);
				layout.addView(detailText);
				LinearLayout buttonContainer = new LinearLayout(act);
				buttonContainer.setOrientation(LinearLayout.VERTICAL);
				buttonContainer.setPadding(0, dp(act, 16), 0, 0);
				for (final Map.Entry<String, String> entry : script.downloadUrls.entrySet()) {
					Button downloadBtn = new Button(act);
					applyClickAnim(downloadBtn);
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
								if (detailDialogRef[0] != null) {
									detailDialogRef[0].dismiss();
								}
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										try {
											Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getValue()));
											act.startActivity(intent);
											Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx,
													"script_opened_in_via"), Toast.LENGTH_LONG).show();
										} catch (Exception e) {
											Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx,
													"cannot_open_download_link"), Toast.LENGTH_SHORT).show();
										}
									}
								}, 100);
							} catch (Exception e) {
								Toast.makeText(act,
										LocalizedStringProvider.getInstance().get(ctx, "cannot_open_download_link"),
										Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
				layout.addView(buttonContainer);
				scrollView.addView(layout);
				builder.setView(scrollView);
				builder.setNegativeButton(LocalizedStringProvider.getInstance().get(ctx, "dialog_cancel"), null);
				detailDialogRef[0] = builder.create();
				detailDialogRef[0].show();
			}
		});
	}
	private interface ScriptsLoadCallback {
		void onScriptsLoaded(List<ScriptInfo> scripts);
		void onLoadFailed(String error);
	}
	private void addAdBlockRulesItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "ad_block_rules_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "ad_block_rules_config"));
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
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "ad_block_rules_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showAdBlockRulesDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
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
				scrollRoot.setPadding(0, 0, 0, 0);
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "ad_block_rules_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "ad_block_rules_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);
				final LinearLayout rulesContainer = new LinearLayout(act);
				rulesContainer.setOrientation(LinearLayout.VERTICAL);
				rulesContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				final LinearLayout emptyStateContainer = new LinearLayout(act);
				emptyStateContainer.setOrientation(LinearLayout.VERTICAL);
				emptyStateContainer.setGravity(Gravity.CENTER);
				emptyStateContainer.setPadding(0, dp(act, 48), 0, dp(act, 48));
				emptyStateContainer.setVisibility(View.GONE);
				final ImageView errorIcon = new ImageView(act);
				errorIcon.setImageResource(android.R.drawable.ic_menu_report_image);
				errorIcon.setColorFilter(0xFF888888);
				errorIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(act, 64), dp(act, 64));
				iconLp.gravity = Gravity.CENTER;
				iconLp.bottomMargin = dp(act, 16);
				emptyStateContainer.addView(errorIcon, iconLp);
				final TextView emptyStateText = new TextView(act);
				emptyStateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				emptyStateText.setTextColor(0xFF888888);
				emptyStateText.setGravity(Gravity.CENTER);
				emptyStateText.setPadding(dp(act, 32), 0, dp(act, 32), 0);
				emptyStateContainer.addView(emptyStateText);
				root.addView(rulesContainer);
				root.addView(emptyStateContainer);
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
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
				showRulesLoadingState(act, ctx, rulesContainer, emptyStateContainer, emptyStateText);
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
	private static class RuleInfo {
		String id;
		Map<String, String> nameMap;
		Map<String, String> descriptionMap;
		Map<String, String> detailMap;
		Map<String, String> downloadUrls;
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
			Map<String, String> nameMap = new HashMap<>();
			JSONObject names = json.getJSONObject("names");
			Iterator<String> nameKeys = names.keys();
			while (nameKeys.hasNext()) {
				String lang = nameKeys.next();
				nameMap.put(lang, names.getString(lang));
			}
			Map<String, String> descriptionMap = new HashMap<>();
			JSONObject descriptions = json.getJSONObject("descriptions");
			Iterator<String> descKeys = descriptions.keys();
			while (descKeys.hasNext()) {
				String lang = descKeys.next();
				descriptionMap.put(lang, descriptions.getString(lang));
			}
			Map<String, String> detailMap = new HashMap<>();
			JSONObject details = json.getJSONObject("details");
			Iterator<String> detailKeys = details.keys();
			while (detailKeys.hasNext()) {
				String lang = detailKeys.next();
				detailMap.put(lang, details.getString(lang));
			}
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
	private void loadRulesFromNetwork(final Context ctx, final RulesLoadCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String networkSource = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE);
					String rulesUrl = networkSource.equals(NETWORK_SOURCE_GITEE)
							? "https:
							: "https:
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
	private void showRulesLoadingState(Activity act, Context ctx, LinearLayout rulesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText) {
		rulesContainer.removeAllViews();
		rulesContainer.setVisibility(View.GONE);
		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(LocalizedStringProvider.getInstance().get(ctx, "rules_loading"));
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.GONE);
			}
		}
		ProgressBar progressBar = new ProgressBar(act);
		progressBar.setIndeterminate(true);
		LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(dp(act, 48), dp(act, 48));
		progressLp.gravity = Gravity.CENTER;
		progressLp.bottomMargin = dp(act, 16);
		emptyStateContainer.addView(progressBar, 0, progressLp);
	}
	private void showRulesErrorState(Activity act, Context ctx, LinearLayout rulesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, String error) {
		rulesContainer.removeAllViews();
		rulesContainer.setVisibility(View.GONE);
		emptyStateContainer.setVisibility(View.VISIBLE);
		emptyStateText.setText(LocalizedStringProvider.getInstance().get(ctx, "rules_load_failed") + "\n"
				+ LocalizedStringProvider.getInstance().get(ctx, "check_network"));
		for (int i = 0; i < emptyStateContainer.getChildCount(); i++) {
			View child = emptyStateContainer.getChildAt(i);
			if (child instanceof ImageView) {
				child.setVisibility(View.VISIBLE);
			} else if (child instanceof ProgressBar) {
				emptyStateContainer.removeView(child);
			}
		}
		Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "rules_load_failed") + ": " + error,
				Toast.LENGTH_SHORT).show();
	}
	private void refreshRulesList(final Activity act, final Context ctx, LinearLayout rulesContainer,
			LinearLayout emptyStateContainer, TextView emptyStateText, List<RuleInfo> rules) {
		rulesContainer.removeAllViews();
		if (rules == null || rules.isEmpty()) {
			showRulesErrorState(act, ctx, rulesContainer, emptyStateContainer, emptyStateText, "No rules available");
			return;
		}
		rulesContainer.setVisibility(View.VISIBLE);
		emptyStateContainer.setVisibility(View.GONE);
		Map<String, List<RuleInfo>> categorizedRules = new HashMap<>();
		for (RuleInfo rule : rules) {
			String category = rule.category;
			if (!categorizedRules.containsKey(category)) {
				categorizedRules.put(category, new ArrayList<RuleInfo>());
			}
			categorizedRules.get(category).add(rule);
		}
		for (Map.Entry<String, List<RuleInfo>> entry : categorizedRules.entrySet()) {
			String category = entry.getKey();
			List<RuleInfo> categoryRules = entry.getValue();
			TextView categoryTitle = new TextView(act);
			categoryTitle.setText(getCategoryDisplayName(ctx, category));
			categoryTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			categoryTitle.setTextColor(0xFF6200EE);
			categoryTitle.setTypeface(null, Typeface.BOLD);
			categoryTitle.setPadding(0, dp(act, 16), 0, dp(act, 8));
			rulesContainer.addView(categoryTitle);
			for (final RuleInfo rule : categoryRules) {
				LinearLayout ruleCard = createRuleCard(act, ctx, rule);
				LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				cardLp.bottomMargin = dp(ctx, 12);
				rulesContainer.addView(ruleCard, cardLp);
			}
		}
	}
	private String getCategoryDisplayName(Context ctx, String category) {
		if ("small".equals(category)) {
			return LocalizedStringProvider.getInstance().get(ctx, "rules_category_small");
		} else if ("large".equals(category)) {
			return LocalizedStringProvider.getInstance().get(ctx, "rules_category_large");
		}
		return category;
	}
	private LinearLayout createRuleCard(final Activity act, final Context ctx, final RuleInfo rule) {
		LinearLayout ruleCard = new LinearLayout(act);
		ruleCard.setOrientation(LinearLayout.VERTICAL);
		ruleCard.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
		GradientDrawable cardBg = new GradientDrawable();
		cardBg.setColor(0xFFF8F9FA);
		cardBg.setStroke(dp(act, 1), 0xFFE9ECEF);
		cardBg.setCornerRadius(dp(act, 12));
		ruleCard.setBackground(cardBg);
		TextView ruleName = new TextView(act);
		ruleName.setText(rule.getName(ctx));
		ruleName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		ruleName.setTextColor(Color.BLACK);
		ruleName.setTypeface(null, Typeface.BOLD);
		ruleCard.addView(ruleName);
		TextView ruleDescription = new TextView(act);
		ruleDescription.setText(rule.getDescription(ctx));
		ruleDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		ruleDescription.setTextColor(0xFF666666);
		ruleDescription.setPadding(0, dp(act, 8), 0, 0);
		ruleCard.addView(ruleDescription);
		ruleCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showRuleDetailDialog(ctx, rule);
			}
		});
		return ruleCard;
	}
	private void showRuleDetailDialog(final Context ctx, final RuleInfo rule) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final AlertDialog[] detailDialogRef = new AlertDialog[1];
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle(rule.getName(ctx));
				ScrollView scrollView = new ScrollView(act);
				LinearLayout layout = new LinearLayout(act);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				TextView detailText = new TextView(act);
				detailText.setText(rule.getDetail(ctx));
				detailText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				detailText.setTextColor(Color.BLACK);
				detailText.setLineSpacing(dp(act, 4), 1.2f);
				layout.addView(detailText);
				if (rule.author != null && !rule.author.isEmpty()) {
					TextView authorText = new TextView(act);
					authorText.setText(
							LocalizedStringProvider.getInstance().get(ctx, "rule_author") + ": " + rule.author);
					authorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					authorText.setTextColor(0xFF666666);
					authorText.setPadding(0, dp(act, 8), 0, 0);
					layout.addView(authorText);
				}
				if (rule.homepage != null && !rule.homepage.isEmpty()) {
					TextView homepageText = new TextView(act);
					homepageText.setText(
							LocalizedStringProvider.getInstance().get(ctx, "rule_homepage") + ": " + rule.homepage);
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
								Toast.makeText(act,
										LocalizedStringProvider.getInstance().get(ctx, "cannot_open_homepage"),
										Toast.LENGTH_SHORT).show();
							}
						}
					});
					layout.addView(homepageText);
				}
				LinearLayout buttonContainer = new LinearLayout(act);
				buttonContainer.setOrientation(LinearLayout.VERTICAL);
				buttonContainer.setPadding(0, dp(act, 16), 0, 0);
				int channelIndex = 1;
				for (final Map.Entry<String, String> entry : rule.downloadUrls.entrySet()) {
					Button downloadBtn = new Button(act);
					applyClickAnim(downloadBtn);
					downloadBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "rule_channel") + " "
							+ channelIndex + " - " + entry.getKey());
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
							Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "rule_link_copied"),
									Toast.LENGTH_SHORT).show();
						}
					});
					channelIndex++;
				}
				layout.addView(buttonContainer);
				scrollView.addView(layout);
				builder.setView(scrollView);
				builder.setNegativeButton(LocalizedStringProvider.getInstance().get(ctx, "dialog_cancel"), null);
				detailDialogRef[0] = builder.create();
				detailDialogRef[0].show();
			}
		});
	}
	private interface RulesLoadCallback {
		void onRulesLoaded(List<RuleInfo> rules);
		void onLoadFailed(String error);
	}
	private void setHideStatusBar(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (hideStatusBarHook == null) {
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
											bvLog("[BetterVia] 已为 " + activity.getClass().getSimpleName() + " 设置状态栏隐藏");
										}
									});
								}
							}
						});
				bvLog("[BetterVia] 隐藏状态栏已启用");
			}
		} else {
			if (hideStatusBarHook != null) {
				hideStatusBarHook.unhook();
				hideStatusBarHook = null;
				bvLog("[BetterVia] 隐藏状态栏已停用");
				restoreStatusBar();
			}
		}
		hideStatusBarEnabled = on;
		putPrefBoolean(ctx, KEY_HIDE_STATUS_BAR, on);
	}
	private void setupStatusBarHiding(final Activity activity) {
		try {
			if (activity.isFinishing() || activity.isDestroyed())
				return;
			final View decorView = activity.getWindow().getDecorView();
			hideStatusBarImmediate(activity);
			decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int visibility) {
					if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
						decorView.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (!activity.isFinishing() && !activity.isDestroyed() && hideStatusBarEnabled) {
									hideStatusBarImmediate(activity);
									setupStatusBarHiding(activity);
								}
							}
						}, 100);
					}
				}
			});
		} catch (Exception e) {
			bvLog("[BetterVia] 滑动更新状态栏失败: " + e);
		}
	}
	private void hideStatusBarImmediate(Activity activity) {
		try {
			if (activity.isFinishing() || activity.isDestroyed()) {
				return;
			}
			View decorView = activity.getWindow().getDecorView();
			int flags = decorView.getSystemUiVisibility();
			flags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
			flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			decorView.setSystemUiVisibility(flags);
			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} catch (Exception e) {
			bvLog("[BetterVia] 立即隐藏状态栏失败: " + e);
		}
	}
	private void restoreStatusBar() {
		for (final Activity activity : statusBarHiddenActivities.keySet()) {
			if (!activity.isFinishing() && !activity.isDestroyed()) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							View decorView = activity.getWindow().getDecorView();
							decorView.setOnSystemUiVisibilityChangeListener(null);
							Runnable rehideRunnable = statusBarRehideRunnables.get(activity);
							if (rehideRunnable != null) {
								decorView.removeCallbacks(rehideRunnable);
								statusBarRehideRunnables.remove(activity);
							}
							int flags = decorView.getSystemUiVisibility();
							flags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
							flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
							flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
							flags &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
							decorView.setSystemUiVisibility(flags);
							activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						} catch (Exception e) {
							bvLog("[BetterVia] 恢复状态栏失败: " + e);
						}
					}
				});
			}
		}
		statusBarHiddenActivities.clear();
		statusBarRehideRunnables.clear();
		bvLog("[BetterVia] 已恢复所有Activity的状态栏显示");
	}
	private void setBlockSwipeBack(Context ctx, ClassLoader cl, boolean on) {
		try {
			if (on) {
				if (swipeBackHook == null) {
					bvLog("[BetterVia] 开始设置屏蔽右滑返回功能...");
					try {
						Class<?> viewPagerClass = cl.loadClass("androidx.viewpager.widget.ViewPager");
						swipeBackHook = XposedHelpers.findAndHookMethod(viewPagerClass, "canScrollHorizontally",
								int.class, new XC_MethodHook() {
									@Override
									protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
										if (!blockSwipeBackEnabled) {
											return;
										}
										int direction = (Integer) param.args[0];
										if (direction < 0) {
											bvLog("[BetterVia] ViewPager.canScrollHorizontally: 阻止向右滚动");
											param.setResult(false);
										}
									}
								});
						bvLog("[BetterVia] ✓ 屏蔽右滑返回已启用 (androidx.viewpager.widget.ViewPager)");
					} catch (ClassNotFoundException e) {
						bvLog("[BetterVia] ✗ 未找到 androidx.viewpager.widget.ViewPager");
						try {
							Class<?> viewPagerClass = cl.loadClass("android.support.v4.view.ViewPager");
							swipeBackHook = XposedHelpers.findAndHookMethod(viewPagerClass, "canScrollHorizontally",
									int.class, new XC_MethodHook() {
										@Override
										protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
											if (!blockSwipeBackEnabled) {
												return;
											}
											int direction = (Integer) param.args[0];
											if (direction < 0) {
												bvLog("[BetterVia] ViewPager.canScrollHorizontally: 阻止向右滚动");
												param.setResult(false);
											}
										}
									});
							bvLog("[BetterVia] ✓ 屏蔽右滑返回已启用 (android.support.v4.view.ViewPager)");
						} catch (ClassNotFoundException e2) {
							bvLog("[BetterVia] ✗ 未找到 android.support.v4.view.ViewPager");
						}
					}
					if (swipeBackHook == null) {
						try {
							swipeBackHook = XposedHelpers.findAndHookMethod(ViewGroup.class,
									"requestDisallowInterceptTouchEvent", boolean.class, new XC_MethodHook() {
										@Override
										protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
											if (!blockSwipeBackEnabled) {
												return;
											}
											param.args[0] = false;
											bvLog("[BetterVia] requestDisallowInterceptTouchEvent: 强制不拦截");
										}
									});
							bvLog("[BetterVia] ✓ 屏蔽右滑返回已启用 (ViewGroup.requestDisallowInterceptTouchEvent)");
						} catch (Throwable e) {
							bvLog("[BetterVia] ✗ Hook ViewGroup.requestDisallowInterceptTouchEvent 失败: " + e);
						}
					}
					if (swipeBackHook == null) {
						try {
							swipeBackHook = XposedHelpers.findAndHookMethod(ViewGroup.class, "onInterceptTouchEvent",
									MotionEvent.class, new XC_MethodHook() {
										@Override
										protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
											if (!blockSwipeBackEnabled) {
												return;
											}
											MotionEvent event = (MotionEvent) param.args[0];
											if (event.getAction() == MotionEvent.ACTION_DOWN) {
												float x = event.getX();
												if (x < dp(((View) param.thisObject).getContext(), 50)) {
													bvLog("[BetterVia] onInterceptTouchEvent: 拦截左边缘触摸");
													param.setResult(true);
												}
											}
										}
									});
							bvLog("[BetterVia] ✓ 屏蔽右滑返回已启用 (ViewGroup.onInterceptTouchEvent)");
						} catch (Throwable e) {
							bvLog("[BetterVia] ✗ Hook ViewGroup.onInterceptTouchEvent 失败: " + e);
						}
					}
					if (swipeBackHook == null) {
						try {
							swipeBackHook = XposedHelpers.findAndHookMethod(Activity.class, "onBackPressed",
									new XC_MethodHook() {
										@Override
										protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
											if (!blockSwipeBackEnabled) {
												return;
											}
											bvLog("[BetterVia] onBackPressed: 阻止返回");
											param.setResult(null);
										}
									});
							bvLog("[BetterVia] ✓ 屏蔽右滑返回已启用 (Activity.onBackPressed)");
						} catch (Throwable e) {
							bvLog("[BetterVia] ✗ Hook Activity.onBackPressed 失败: " + e);
						}
					}
					if (swipeBackHook == null) {
						bvLog("[BetterVia] ✗ 所有策略都失败，无法屏蔽右滑返回");
					}
				}
			} else {
				if (swipeBackHook != null) {
					swipeBackHook.unhook();
					swipeBackHook = null;
					bvLog("[BetterVia] 屏蔽右滑返回已停用");
				}
			}
			blockSwipeBackEnabled = on;
			putPrefBoolean(ctx, KEY_BLOCK_SWIPE_BACK, on);
		} catch (Throwable t) {
			bvLog("[BetterVia] 设置屏蔽右滑返回失败: " + t);
		}
	}
	private void addCookieManagementItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_management_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_management_config"));
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
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_management_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showCookieManagementDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				final Dialog dialog = new Dialog(act, android.R.style.Theme_NoTitleBar_Fullscreen);
				dialog.setCancelable(true);
				LinearLayout rootLayout = new LinearLayout(act);
				rootLayout.setOrientation(LinearLayout.VERTICAL);
				rootLayout.setBackgroundColor(Color.WHITE);
				RelativeLayout titleBar = new RelativeLayout(act);
				titleBar.setBackgroundColor(0xFFF5F5F5);
				titleBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
				titleBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
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
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_dialog_title"));
				title.setTextColor(Color.BLACK);
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				title.setTypeface(null, Typeface.BOLD);
				RelativeLayout.LayoutParams titleLp = new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.addRule(RelativeLayout.CENTER_IN_PARENT);
				titleBar.addView(title, titleLp);
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
				LinearLayout contentLayout = new LinearLayout(act);
				contentLayout.setOrientation(LinearLayout.VERTICAL);
				contentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				contentLayout.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				LinearLayout searchBar = new LinearLayout(act);
				searchBar.setOrientation(LinearLayout.HORIZONTAL);
				searchBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				searchBar.setPadding(0, 0, 0, dp(act, 12));
				final EditText searchEdit = new EditText(act);
				searchEdit.setHint(LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_search_hint"));
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
				applyClickAnim(searchButton);
				searchButton.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_search_btn"));
				searchButton.setTextColor(Color.WHITE);
				searchButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				searchButton.setBackground(getRoundBg(act, 0xFF6200EE, 8));
				searchButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
				searchBar.addView(searchButton);
				contentLayout.addView(searchBar);
				FrameLayout listAndLoadingContainer = new FrameLayout(act);
				listAndLoadingContainer
						.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
				final LinearLayout loadingContainer = new LinearLayout(act);
				loadingContainer.setOrientation(LinearLayout.VERTICAL);
				loadingContainer.setGravity(Gravity.CENTER);
				loadingContainer.setPadding(0, dp(act, 48), 0, dp(act, 48));
				loadingContainer.setVisibility(View.VISIBLE);
				ProgressBar progressBar = new ProgressBar(act);
				progressBar.setIndeterminate(true);
				LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(dp(act, 48), dp(act, 48));
				progressLp.gravity = Gravity.CENTER;
				progressLp.bottomMargin = dp(act, 16);
				loadingContainer.addView(progressBar, progressLp);
				TextView loadingText = new TextView(act);
				loadingText.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_loading"));
				loadingText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				loadingText.setTextColor(0xFF888888);
				loadingText.setGravity(Gravity.CENTER);
				loadingContainer.addView(loadingText);
				contentLayout.addView(loadingContainer);
				final ScrollView scrollView = new ScrollView(act);
				scrollView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				scrollView.setVisibility(View.GONE);
				final LinearLayout listContainer = new LinearLayout(act);
				listContainer.setOrientation(LinearLayout.VERTICAL);
				listContainer.setPadding(0, 0, 0, 0);
				scrollView.addView(listContainer);
				listAndLoadingContainer.addView(scrollView);
				contentLayout.addView(listAndLoadingContainer);
				LinearLayout buttonBar = new LinearLayout(act);
				buttonBar.setOrientation(LinearLayout.HORIZONTAL);
				buttonBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				buttonBar.setPadding(0, dp(act, 12), 0, 0);
				final Button deleteButton = new Button(act);
				applyClickAnim(deleteButton);
				deleteButton.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_delete_selected"));
				deleteButton.setTextColor(Color.WHITE);
				deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				deleteButton.setBackground(getRoundBg(act, 0xFFE53935, 8));
				deleteButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
				deleteButton.setEnabled(false);
				LinearLayout.LayoutParams deleteLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				deleteLp.rightMargin = dp(act, 6);
				buttonBar.addView(deleteButton, deleteLp);
				final Button selectAllButton = new Button(act);
				applyClickAnim(selectAllButton);
				selectAllButton.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_select_all"));
				selectAllButton.setTextColor(0xFF6200EE);
				selectAllButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				selectAllButton.setBackground(getRoundBg(act, 0xFFF0F0F0, 8));
				selectAllButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
				LinearLayout.LayoutParams selectAllLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				selectAllLp.leftMargin = dp(act, 6);
				selectAllLp.rightMargin = dp(act, 6);
				buttonBar.addView(selectAllButton, selectAllLp);
				Button closeButton = new Button(act);
				applyClickAnim(closeButton);
				closeButton.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_close"));
				closeButton.setTextColor(Color.WHITE);
				closeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				closeButton.setBackground(getRoundBg(act, 0xFF6200EE, 8));
				closeButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
				LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				closeLp.leftMargin = dp(act, 6);
				buttonBar.addView(closeButton, closeLp);
				contentLayout.addView(buttonBar);
				rootLayout.addView(contentLayout);
				dialog.setContentView(rootLayout);
				final List<CookieItem>[] currentCookieList = new List[]{new ArrayList<CookieItem>()};
				final List<DomainItem>[] currentDomainList = new List[]{new ArrayList<DomainItem>()};
				final boolean[] isDomainView = {true};
				final boolean[] isAllSelected = {false};
				final LinearLayout switchBar = new LinearLayout(act);
				switchBar.setOrientation(LinearLayout.HORIZONTAL);
				switchBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				switchBar.setPadding(0, 0, 0, dp(act, 12));
				switchBar.setVisibility(View.GONE);
				final Button domainViewBtn = new Button(act);
				applyClickAnim(domainViewBtn);
				domainViewBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_view_domain"));
				domainViewBtn.setTextColor(Color.WHITE);
				domainViewBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				domainViewBtn.setBackground(getRoundBg(act, 0xFF6200EE, 6));
				domainViewBtn.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				LinearLayout.LayoutParams domainViewLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				domainViewLp.rightMargin = dp(act, 4);
				switchBar.addView(domainViewBtn, domainViewLp);
				final Button listViewBtn = new Button(act);
				applyClickAnim(listViewBtn);
				listViewBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_view_list"));
				listViewBtn.setTextColor(Color.BLACK);
				listViewBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				listViewBtn.setBackground(getRoundBg(act, 0xFFF0F0F0, 6));
				listViewBtn.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
				LinearLayout.LayoutParams listViewLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				listViewLp.leftMargin = dp(act, 4);
				switchBar.addView(listViewBtn, listViewLp);
				contentLayout.addView(switchBar, contentLayout.getChildCount() - 2);
				final FrameLayout switchLoadingFrame = new FrameLayout(act);
				FrameLayout.LayoutParams frameLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
				switchLoadingFrame.setLayoutParams(frameLp);
				switchLoadingFrame.setVisibility(View.GONE);
				final LinearLayout switchLoadingContainer = new LinearLayout(act);
				switchLoadingContainer.setOrientation(LinearLayout.VERTICAL);
				switchLoadingContainer.setGravity(Gravity.CENTER);
				switchLoadingContainer.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				FrameLayout.LayoutParams containerLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
				switchLoadingFrame.addView(switchLoadingContainer, containerLp);
				ProgressBar switchProgressBar = new ProgressBar(act);
				switchProgressBar.setIndeterminate(true);
				LinearLayout.LayoutParams switchProgressLp = new LinearLayout.LayoutParams(dp(act, 64), dp(act, 64));
				switchProgressLp.gravity = Gravity.CENTER;
				switchProgressLp.bottomMargin = dp(act, 12);
				switchLoadingContainer.addView(switchProgressBar, switchProgressLp);
				TextView switchLoadingText = new TextView(act);
				switchLoadingText.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_view_switching"));
				switchLoadingText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				switchLoadingText.setTextColor(0xFF666666);
				switchLoadingText.setGravity(Gravity.CENTER);
				switchLoadingText.setTypeface(null, Typeface.NORMAL);
				switchLoadingContainer.addView(switchLoadingText);
				listAndLoadingContainer.addView(switchLoadingFrame);
				new Thread(new Runnable() {
					@Override
					public void run() {
						final List<CookieItem> cookieItems = loadCookieData(ctx);
						final List<DomainItem> domainItems = loadDomainGroupedCookieData(ctx);
						currentCookieList[0] = cookieItems;
						currentDomainList[0] = domainItems;
						act.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								loadingContainer.setVisibility(View.GONE);
								switchBar.setVisibility(View.VISIBLE);
								scrollView.setVisibility(View.VISIBLE);
								populateDomainList(act, listContainer, domainItems, deleteButton, scrollView, ctx,
										domainItems);
							}
						});
					}
				}).start();
				domainViewBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!isDomainView[0]) {
							domainViewBtn.setEnabled(false);
							listViewBtn.setEnabled(false);
							switchLoadingFrame.setVisibility(View.VISIBLE);
							scrollView.setVisibility(View.GONE);
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									act.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											isDomainView[0] = true;
											domainViewBtn.setTextColor(Color.WHITE);
											domainViewBtn.setBackground(getRoundBg(act, 0xFF6200EE, 6));
											listViewBtn.setTextColor(Color.BLACK);
											listViewBtn.setBackground(getRoundBg(act, 0xFFF0F0F0, 6));
											switchLoadingFrame.setVisibility(View.GONE);
											scrollView.setVisibility(View.VISIBLE);
											listContainer.removeAllViews();
											populateDomainList(act, listContainer, currentDomainList[0], deleteButton,
													scrollView, ctx, currentDomainList[0]);
											domainViewBtn.setEnabled(true);
											listViewBtn.setEnabled(true);
										}
									});
								}
							}, 300);
						}
					}
				});
				listViewBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isDomainView[0]) {
							domainViewBtn.setEnabled(false);
							listViewBtn.setEnabled(false);
							switchLoadingFrame.setVisibility(View.VISIBLE);
							scrollView.setVisibility(View.GONE);
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									act.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											isDomainView[0] = false;
											isAllSelected[0] = false;
											selectAllButton.setText(LocalizedStringProvider.getInstance().get(ctx,
													"cookie_manager_select_all"));
											listViewBtn.setTextColor(Color.WHITE);
											listViewBtn.setBackground(getRoundBg(act, 0xFF6200EE, 6));
											domainViewBtn.setTextColor(Color.BLACK);
											domainViewBtn.setBackground(getRoundBg(act, 0xFFF0F0F0, 6));
											switchLoadingFrame.setVisibility(View.GONE);
											scrollView.setVisibility(View.VISIBLE);
											listContainer.removeAllViews();
											populateCookieList(act, listContainer, currentCookieList[0], deleteButton,
													scrollView);
											domainViewBtn.setEnabled(true);
											listViewBtn.setEnabled(true);
										}
									});
								}
							}, 300);
						}
					}
				});
				selectAllButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						isAllSelected[0] = !isAllSelected[0];
						if (isAllSelected[0]) {
							selectAllButton.setText(
									LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_unselect_all"));
							Toast.makeText(act,
									LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_selecting"),
									Toast.LENGTH_SHORT).show();
							if (isDomainView[0]) {
								for (DomainItem item : currentDomainList[0]) {
									item.selected = true;
								}
							} else {
								for (CookieItem item : currentCookieList[0]) {
									item.selected = true;
								}
							}
							new Thread(new Runnable() {
								@Override
								public void run() {
									act.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											for (int i = 0; i < listContainer.getChildCount(); i++) {
												View child = listContainer.getChildAt(i);
												if (child instanceof LinearLayout) {
													LinearLayout itemLayout = (LinearLayout) child;
													View firstChild = itemLayout.getChildAt(0);
													if (firstChild instanceof LinearLayout) {
														CheckBox checkbox = (CheckBox) ((LinearLayout) firstChild)
																.getChildAt(0);
														if (checkbox != null) {
															checkbox.setChecked(true);
														}
														if (itemLayout.getTag() instanceof CookieItem) {
															((CookieItem) itemLayout.getTag()).selected = true;
														} else if (itemLayout.getTag() instanceof DomainItem) {
															((DomainItem) itemLayout.getTag()).selected = true;
														}
													}
												}
											}
											deleteButton.setEnabled(true);
											deleteButton.setText(LocalizedStringProvider.getInstance().get(act,
													"cookie_manager_delete_selected"));
										}
									});
								}
							}).start();
						} else {
							selectAllButton.setText(
									LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_select_all"));
							Toast.makeText(act,
									LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_unselecting"),
									Toast.LENGTH_SHORT).show();
							if (isDomainView[0]) {
								for (DomainItem item : currentDomainList[0]) {
									item.selected = false;
								}
							} else {
								for (CookieItem item : currentCookieList[0]) {
									item.selected = false;
								}
							}
							new Thread(new Runnable() {
								@Override
								public void run() {
									act.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											for (int i = 0; i < listContainer.getChildCount(); i++) {
												View child = listContainer.getChildAt(i);
												if (child instanceof LinearLayout) {
													LinearLayout itemLayout = (LinearLayout) child;
													View firstChild = itemLayout.getChildAt(0);
													if (firstChild instanceof LinearLayout) {
														CheckBox checkbox = (CheckBox) ((LinearLayout) firstChild)
																.getChildAt(0);
														if (checkbox != null) {
															checkbox.setChecked(false);
														}
														if (itemLayout.getTag() instanceof CookieItem) {
															((CookieItem) itemLayout.getTag()).selected = false;
														} else if (itemLayout.getTag() instanceof DomainItem) {
															((DomainItem) itemLayout.getTag()).selected = false;
														}
													}
												}
											}
											deleteButton.setEnabled(false);
											deleteButton.setText(LocalizedStringProvider.getInstance().get(act,
													"cookie_manager_delete_selected"));
										}
									});
								}
							}).start();
						}
					}
				});
				backButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				refreshButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						loadingContainer.setVisibility(View.VISIBLE);
						switchBar.setVisibility(View.GONE);
						scrollView.setVisibility(View.GONE);
						new Thread(new Runnable() {
							@Override
							public void run() {
								final List<CookieItem> refreshedCookieData = loadCookieData(ctx);
								final List<DomainItem> refreshedDomainData = loadDomainGroupedCookieData(ctx);
								currentCookieList[0] = refreshedCookieData;
								currentDomainList[0] = refreshedDomainData;
								act.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										loadingContainer.setVisibility(View.GONE);
										switchBar.setVisibility(View.VISIBLE);
										scrollView.setVisibility(View.VISIBLE);
										listContainer.removeAllViews();
										if (isDomainView[0]) {
											populateDomainList(act, listContainer, refreshedDomainData, deleteButton,
													scrollView, ctx, refreshedDomainData);
										} else {
											populateCookieList(act, listContainer, refreshedCookieData, deleteButton,
													scrollView);
										}
										Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx,
												"cookie_management_refreshed"), Toast.LENGTH_SHORT).show();
									}
								});
							}
						}).start();
					}
				});
				searchButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final String query = searchEdit.getText().toString().trim().toLowerCase();
						if (query.isEmpty()) {
							if (isDomainView[0]) {
								populateDomainList(act, listContainer, currentDomainList[0], deleteButton, scrollView,
										ctx);
							} else {
								populateCookieList(act, listContainer, currentCookieList[0], deleteButton, scrollView);
							}
							return;
						}
						if (isDomainView[0]) {
							List<DomainItem> filteredDomainList = new ArrayList<DomainItem>();
							for (DomainItem domainItem : currentDomainList[0]) {
								boolean domainMatch = domainItem.domain.toLowerCase().contains(query);
								boolean cookieMatch = false;
								for (CookieItem cookie : domainItem.cookies) {
									if ((cookie.name != null && cookie.name.toLowerCase().contains(query))
											|| (cookie.value != null && cookie.value.toLowerCase().contains(query))) {
										cookieMatch = true;
										break;
									}
								}
								if (domainMatch || cookieMatch) {
									filteredDomainList.add(domainItem);
								}
							}
							populateDomainList(act, listContainer, filteredDomainList, deleteButton, scrollView, ctx,
									filteredDomainList);
							String resultMsg = String.format(
									LocalizedStringProvider.getInstance().get(act, "cookie_domain_search_result"),
									filteredDomainList.size());
							Toast.makeText(act, resultMsg, Toast.LENGTH_SHORT).show();
						} else {
							List<CookieItem> filteredList = new ArrayList<CookieItem>();
							for (CookieItem item : currentCookieList[0]) {
								if ((item.host_key != null && item.host_key.toLowerCase().contains(query))
										|| (item.name != null && item.name.toLowerCase().contains(query))
										|| (item.value != null && item.value.toLowerCase().contains(query))) {
									filteredList.add(item);
								}
							}
							populateCookieList(act, listContainer, filteredList, deleteButton, scrollView);
							String resultMsg = String.format(
									LocalizedStringProvider.getInstance().get(act, "cookie_search_result"),
									filteredList.size());
							Toast.makeText(act, resultMsg, Toast.LENGTH_SHORT).show();
						}
					}
				});
				deleteButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showDeleteConfirmDialog(act, ctx, listContainer, deleteButton, scrollView, isDomainView[0]);
					}
				});
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
	private void showCookieDetailDialog(final Activity act, final CookieItem cookie) {
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setTitle(LocalizedStringProvider.getInstance().get(act, "cookie_detail_dialog_title"));
		ScrollView scrollView = new ScrollView(act);
		LinearLayout layout = new LinearLayout(act);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
		TextView basicInfoTitle = new TextView(act);
		basicInfoTitle.setText(LocalizedStringProvider.getInstance().get(act, "cookie_detail_basic_info"));
		basicInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		basicInfoTitle.setTextColor(Color.BLACK);
		basicInfoTitle.setTypeface(null, Typeface.BOLD);
		basicInfoTitle.setPadding(0, 0, 0, dp(act, 12));
		layout.addView(basicInfoTitle);
		final EditText hostKeyEdit = addEditableField(layout, act,
				LocalizedStringProvider.getInstance().get(act, "cookie_field_host_key"),
				cookie.host_key != null ? cookie.host_key : "");
		final EditText nameEdit = addEditableField(layout, act,
				LocalizedStringProvider.getInstance().get(act, "cookie_field_name"),
				cookie.name != null ? cookie.name : "");
		final EditText valueEdit = addEditableField(layout, act,
				LocalizedStringProvider.getInstance().get(act, "cookie_field_value"),
				cookie.value != null ? cookie.value : "");
		final EditText pathEdit = addEditableField(layout, act,
				LocalizedStringProvider.getInstance().get(act, "cookie_field_path"),
				cookie.path != null ? cookie.path : "");
		TextView timeInfoTitle = new TextView(act);
		timeInfoTitle.setText(LocalizedStringProvider.getInstance().get(act, "cookie_detail_time_info"));
		timeInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		timeInfoTitle.setTextColor(Color.BLACK);
		timeInfoTitle.setTypeface(null, Typeface.BOLD);
		timeInfoTitle.setPadding(0, dp(act, 16), 0, dp(act, 12));
		layout.addView(timeInfoTitle);
		addReadOnlyField(layout, act, LocalizedStringProvider.getInstance().get(act, "cookie_field_creation_time"),
				cookie.creation_utc > 0
						? formatTimestamp(cookie.creation_utc)
						: LocalizedStringProvider.getInstance().get(act, "cookie_field_unknown"));
		addReadOnlyField(layout, act, LocalizedStringProvider.getInstance().get(act, "cookie_field_last_access"),
				cookie.last_access_utc > 0
						? formatTimestamp(cookie.last_access_utc)
						: LocalizedStringProvider.getInstance().get(act, "cookie_field_unknown"));
		addReadOnlyField(layout, act, LocalizedStringProvider.getInstance().get(act, "cookie_field_expires"),
				cookie.expires_utc > 0
						? formatTimestamp(cookie.expires_utc)
						: LocalizedStringProvider.getInstance().get(act, "cookie_field_session"));
		addReadOnlyField(layout, act, LocalizedStringProvider.getInstance().get(act, "cookie_field_last_update"),
				cookie.last_update_utc > 0
						? formatTimestamp(cookie.last_update_utc)
						: LocalizedStringProvider.getInstance().get(act, "cookie_field_unknown"));
		TextView securityInfoTitle = new TextView(act);
		securityInfoTitle.setText(LocalizedStringProvider.getInstance().get(act, "cookie_detail_security_info"));
		securityInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		securityInfoTitle.setTextColor(Color.BLACK);
		securityInfoTitle.setTypeface(null, Typeface.BOLD);
		securityInfoTitle.setPadding(0, dp(act, 16), 0, dp(act, 12));
		layout.addView(securityInfoTitle);
		final CheckBox secureCheckbox = addCheckboxField(layout, act,
				LocalizedStringProvider.getInstance().get(act, "cookie_field_secure"), cookie.is_secure);
		final CheckBox httpOnlyCheckbox = addCheckboxField(layout, act,
				LocalizedStringProvider.getInstance().get(act, "cookie_field_httponly"), cookie.is_httponly);
		final CheckBox persistentCheckbox = addCheckboxField(layout, act,
				LocalizedStringProvider.getInstance().get(act, "cookie_field_persistent"), cookie.is_persistent);
		final CheckBox hasExpiresCheckbox = addCheckboxField(layout, act,
				LocalizedStringProvider.getInstance().get(act, "cookie_field_has_expires"), cookie.has_expires);
		TextView advancedInfoTitle = new TextView(act);
		advancedInfoTitle.setText(LocalizedStringProvider.getInstance().get(act, "cookie_detail_advanced_info"));
		advancedInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		advancedInfoTitle.setTextColor(Color.BLACK);
		advancedInfoTitle.setTypeface(null, Typeface.BOLD);
		advancedInfoTitle.setPadding(0, dp(act, 16), 0, dp(act, 12));
		layout.addView(advancedInfoTitle);
		addReadOnlyField(layout, act, LocalizedStringProvider.getInstance().get(act, "cookie_field_priority"),
				String.valueOf(cookie.priority));
		addReadOnlyField(layout, act, LocalizedStringProvider.getInstance().get(act, "cookie_field_samesite"),
				getSameSiteText(act, cookie.samesite));
		addReadOnlyField(layout, act, LocalizedStringProvider.getInstance().get(act, "cookie_field_source_port"),
				cookie.source_port > 0
						? String.valueOf(cookie.source_port)
						: LocalizedStringProvider.getInstance().get(act, "cookie_field_default"));
		addReadOnlyField(layout, act, LocalizedStringProvider.getInstance().get(act, "cookie_field_source_type"),
				getSourceTypeText(act, cookie.source_type));
		scrollView.addView(layout);
		builder.setView(scrollView);
		builder.setPositiveButton(LocalizedStringProvider.getInstance().get(act, "dialog_ok"),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cookie.host_key = hostKeyEdit.getText().toString();
						cookie.name = nameEdit.getText().toString();
						cookie.value = valueEdit.getText().toString();
						cookie.path = pathEdit.getText().toString();
						cookie.is_secure = secureCheckbox.isChecked();
						cookie.is_httponly = httpOnlyCheckbox.isChecked();
						cookie.is_persistent = persistentCheckbox.isChecked();
						cookie.has_expires = hasExpiresCheckbox.isChecked();
						cookie.last_update_utc = System.currentTimeMillis() / 1000;
						updateCookieInDatabase(act, cookie);
						Toast.makeText(act, LocalizedStringProvider.getInstance().get(act, "cookie_save_success"),
								Toast.LENGTH_SHORT).show();
					}
				});
		builder.setNeutralButton(LocalizedStringProvider.getInstance().get(act, "dialog_cancel"),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}
	private void removeDeletedCookieFromList(final Activity act, final LinearLayout listContainer,
			final Button deleteButton, final ScrollView scrollView, final CookieItem deletedCookie) {
		for (int i = 0; i < listContainer.getChildCount(); i++) {
			View child = listContainer.getChildAt(i);
			if (child.getTag() instanceof CookieItem) {
				CookieItem item = (CookieItem) child.getTag();
				if (itemMatchesDeleted(item, deletedCookie)) {
					listContainer.removeViewAt(i);
					updateDeleteButtonState(act, listContainer, deleteButton);
					bvLog("[BetterVia] 已从列表移除被删除的Cookie: " + item.name);
					break;
				}
			}
		}
		if (listContainer.getChildCount() == 0) {
			showEmptyCookieListState(act, listContainer);
		}
	}
	private boolean itemMatchesDeleted(CookieItem item, CookieItem deletedCookie) {
		return (item.creation_utc == deletedCookie.creation_utc && safeEquals(item.host_key, deletedCookie.host_key)
				&& safeEquals(item.name, deletedCookie.name));
	}
	private boolean safeEquals(String str1, String str2) {
		if (str1 == null && str2 == null)
			return true;
		if (str1 == null || str2 == null)
			return false;
		return str1.equals(str2);
	}
	private void updateDeleteButtonState(Context ctx, LinearLayout listContainer, Button deleteButton) {
		int selectedCount = 0;
		for (int i = 0; i < listContainer.getChildCount(); i++) {
			View child = listContainer.getChildAt(i);
			if (child.getTag() instanceof CookieItem) {
				CookieItem item = (CookieItem) child.getTag();
				if (item.selected) {
					selectedCount++;
				}
			}
		}
		deleteButton.setEnabled(selectedCount > 0);
		deleteButton.setText(LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_delete_selected"));
	}
	private void showEmptyCookieListState(final Activity act, final LinearLayout listContainer) {
		listContainer.removeAllViews();
		TextView emptyText = new TextView(act);
		emptyText.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_empty"));
		emptyText.setTextColor(0xFF888888);
		emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		emptyText.setGravity(Gravity.CENTER);
		emptyText.setPadding(0, dp(act, 32), 0, dp(act, 32));
		listContainer.addView(emptyText);
	}
	private LinearLayout findCookieListContainer(View view) {
		if (view instanceof LinearLayout) {
			LinearLayout layout = (LinearLayout) view;
			for (int i = 0; i < layout.getChildCount(); i++) {
				View child = layout.getChildAt(i);
				if (child.getTag() instanceof CookieItem) {
					return layout;
				}
			}
		}
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
	private ScrollView findCookieListScrollView(View view) {
		if (view instanceof ScrollView) {
			ScrollView scrollView = (ScrollView) view;
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
	private Button findDeleteButton(Context ctx, View view) {
		if (view instanceof Button) {
			Button button = (Button) view;
			String buttonText = button.getText().toString();
			if (buttonText
					.startsWith(LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_delete_selected"))) {
				return button;
			}
			return null;
		}
		if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				Button result = findDeleteButton(ctx, viewGroup.getChildAt(i));
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	private String formatTimestamp(long timestamp) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp * 1000));
		} catch (Exception e) {
			return "Time format error";
		}
	}
	private String getSameSiteText(Context ctx, int samesite) {
		switch (samesite) {
			case 0 :
				return LocalizedStringProvider.getInstance().get(ctx, "cookie_samesite_none");
			case 1 :
				return LocalizedStringProvider.getInstance().get(ctx, "cookie_samesite_lax");
			case 2 :
				return LocalizedStringProvider.getInstance().get(ctx, "cookie_samesite_strict");
			default :
				return String.format(LocalizedStringProvider.getInstance().get(ctx, "cookie_samesite_unknown"),
						samesite);
		}
	}
	private String getSourceTypeText(Context ctx, int sourceType) {
		switch (sourceType) {
			case 0 :
				return LocalizedStringProvider.getInstance().get(ctx, "cookie_source_type_none");
			case 1 :
				return LocalizedStringProvider.getInstance().get(ctx, "cookie_source_type_http");
			case 2 :
				return LocalizedStringProvider.getInstance().get(ctx, "cookie_source_type_https");
			case 3 :
				return LocalizedStringProvider.getInstance().get(ctx, "cookie_source_type_file");
			default :
				return String.format(LocalizedStringProvider.getInstance().get(ctx, "cookie_source_type_unknown"),
						sourceType);
		}
	}
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
			bvLog("[BetterVia] 更新Cookie失败: " + e);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
	private void populateCookieList(final Activity act, final LinearLayout container, List<CookieItem> cookieItems,
			final Button deleteButton, final ScrollView scrollView) {
		container.removeAllViews();
		if (cookieItems.isEmpty()) {
			TextView emptyText = new TextView(act);
			emptyText.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_empty"));
			emptyText.setTextColor(0xFF888888);
			emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			emptyText.setGravity(Gravity.CENTER);
			emptyText.setPadding(0, dp(act, 32), 0, dp(act, 32));
			container.addView(emptyText);
			deleteButton.setEnabled(false);
			return;
		}
		final int[] selectedCount = {0};
		for (int i = 0; i < cookieItems.size(); i++) {
			final CookieItem item = cookieItems.get(i);
			item.selected = false;
			LinearLayout itemLayout = new LinearLayout(act);
			itemLayout.setOrientation(LinearLayout.VERTICAL);
			itemLayout.setBackground(getRoundBg(act, 0xFFF8F9FA, 6));
			itemLayout.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
			LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			itemLp.bottomMargin = dp(act, 8);
			container.addView(itemLayout, itemLp);
			itemLayout.setTag(item);
			LinearLayout firstRow = new LinearLayout(act);
			firstRow.setOrientation(LinearLayout.HORIZONTAL);
			firstRow.setGravity(Gravity.CENTER_VERTICAL);
			final CheckBox selectCheckbox = new CheckBox(act);
			selectCheckbox.setChecked(item.selected);
			selectCheckbox.setScaleX(0.8f);
			selectCheckbox.setScaleY(0.8f);
			firstRow.addView(selectCheckbox);
			TextView domainText = new TextView(act);
			String domain = item.host_key != null && !item.host_key.isEmpty()
					? item.host_key
					: LocalizedStringProvider.getInstance().get(act, "cookie_unknown_domain");
			domainText.setText(domain);
			domainText.setTextColor(Color.BLACK);
			domainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			domainText.setTypeface(null, Typeface.BOLD);
			domainText.setEllipsize(TextUtils.TruncateAt.END);
			domainText.setSingleLine(true);
			domainText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
			firstRow.addView(domainText);
			itemLayout.addView(firstRow);
			LinearLayout secondRow = new LinearLayout(act);
			secondRow.setOrientation(LinearLayout.VERTICAL);
			secondRow.setPadding(dp(act, 24), dp(act, 4), 0, 0);
			TextView nameText = new TextView(act);
			String nameLabel = LocalizedStringProvider.getInstance().get(act, "cookie_field_name_label");
			String nameValue = item.name != null && !item.name.isEmpty()
					? item.name
					: LocalizedStringProvider.getInstance().get(act, "cookie_field_unknown");
			nameText.setText(nameLabel + nameValue);
			nameText.setTextColor(0xFF666666);
			nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
			secondRow.addView(nameText);
			TextView valueText = new TextView(act);
			String valueLabel = LocalizedStringProvider.getInstance().get(act, "cookie_field_value_label");
			String valueRaw = item.value != null && !item.value.isEmpty()
					? item.value
					: LocalizedStringProvider.getInstance().get(act, "cookie_no_value");
			String valueDisplay = valueRaw.length() > 30 ? valueRaw.substring(0, 30) + "..." : valueRaw;
			valueText.setText(valueLabel + valueDisplay);
			valueText.setTextColor(0xFF666666);
			valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
			valueText.setPadding(0, dp(act, 2), 0, 0);
			secondRow.addView(valueText);
			itemLayout.addView(secondRow);
			itemLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showCookieDetailDialog(act, item);
				}
			});
			itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					item.selected = !item.selected;
					selectCheckbox.setChecked(item.selected);
					int count = 0;
					for (int j = 0; j < container.getChildCount(); j++) {
						View child = container.getChildAt(j);
						if (child.getTag() instanceof CookieItem) {
							if (((CookieItem) child.getTag()).selected) {
								count++;
							}
						}
					}
					deleteButton.setEnabled(count > 0);
					deleteButton
							.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_delete_selected"));
					return true;
				}
			});
			selectCheckbox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					item.selected = selectCheckbox.isChecked();
					int count = 0;
					for (int j = 0; j < container.getChildCount(); j++) {
						View child = container.getChildAt(j);
						if (child.getTag() instanceof CookieItem) {
							if (((CookieItem) child.getTag()).selected) {
								count++;
							}
						}
					}
					deleteButton.setEnabled(count > 0);
					deleteButton
							.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_delete_selected"));
				}
			});
		}
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.scrollTo(0, 0);
			}
		});
	}
	private void populateDomainList(final Activity act, final LinearLayout container, List<DomainItem> domainItems,
			final Button deleteButton, final ScrollView scrollView, final Context ctx) {
		container.removeAllViews();
		if (domainItems.isEmpty()) {
			TextView emptyText = new TextView(act);
			emptyText.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_empty"));
			emptyText.setTextColor(0xFF888888);
			emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			emptyText.setGravity(Gravity.CENTER);
			emptyText.setPadding(0, dp(act, 32), 0, dp(act, 32));
			container.addView(emptyText);
			deleteButton.setEnabled(false);
			return;
		}
		final int[] selectedCount = {0};
		for (int i = 0; i < domainItems.size(); i++) {
			final DomainItem domainItem = domainItems.get(i);
			domainItem.selected = false;
			LinearLayout itemLayout = new LinearLayout(act);
			itemLayout.setOrientation(LinearLayout.VERTICAL);
			itemLayout.setBackground(getRoundBg(act, 0xFFF8F9FA, 6));
			itemLayout.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));
			LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			itemLp.bottomMargin = dp(act, 8);
			container.addView(itemLayout, itemLp);
			itemLayout.setTag(domainItem);
			LinearLayout firstRow = new LinearLayout(act);
			firstRow.setOrientation(LinearLayout.HORIZONTAL);
			firstRow.setGravity(Gravity.CENTER_VERTICAL);
			final CheckBox selectCheckbox = new CheckBox(act);
			selectCheckbox.setChecked(domainItem.selected);
			selectCheckbox.setScaleX(0.8f);
			selectCheckbox.setScaleY(0.8f);
			firstRow.addView(selectCheckbox);
			TextView domainText = new TextView(act);
			domainText.setText(domainItem.domain);
			domainText.setTextColor(Color.BLACK);
			domainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			domainText.setTypeface(null, Typeface.BOLD);
			domainText.setEllipsize(TextUtils.TruncateAt.END);
			domainText.setSingleLine(true);
			domainText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
			firstRow.addView(domainText);
			TextView countText = new TextView(act);
			String countLabel = LocalizedStringProvider.getInstance().get(act, "cookie_domain_count_label");
			countText.setText(String.format(countLabel, domainItem.getCookieCount()));
			countText.setTextColor(0xFF666666);
			countText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			countLp.leftMargin = dp(act, 8);
			firstRow.addView(countText, countLp);
			itemLayout.addView(firstRow);
			LinearLayout secondRow = new LinearLayout(act);
			secondRow.setOrientation(LinearLayout.VERTICAL);
			secondRow.setPadding(dp(act, 24), dp(act, 4), 0, 0);
			int previewCount = Math.min(3, domainItem.cookies.size());
			for (int j = 0; j < previewCount; j++) {
				CookieItem cookie = domainItem.cookies.get(j);
				TextView cookiePreview = new TextView(act);
				String cookieName = cookie.name != null && !cookie.name.isEmpty()
						? cookie.name
						: LocalizedStringProvider.getInstance().get(act, "cookie_field_unknown");
				String cookieValue = cookie.value != null && !cookie.value.isEmpty()
						? cookie.value
						: LocalizedStringProvider.getInstance().get(act, "cookie_no_value");
				String valueDisplay = cookieValue.length() > 20 ? cookieValue.substring(0, 20) + "..." : cookieValue;
				cookiePreview.setText("• " + cookieName + ": " + valueDisplay);
				cookiePreview.setTextColor(0xFF666666);
				cookiePreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
				secondRow.addView(cookiePreview);
			}
			if (domainItem.cookies.size() > 3) {
				TextView moreText = new TextView(act);
				int moreCount = domainItem.cookies.size() - 3;
				String moreLabel = LocalizedStringProvider.getInstance().get(act, "cookie_domain_more_label");
				moreText.setText(String.format(moreLabel, moreCount));
				moreText.setTextColor(0xFF999999);
				moreText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
				moreText.setPadding(0, dp(act, 2), 0, 0);
				secondRow.addView(moreText);
			}
			itemLayout.addView(secondRow);
			itemLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDomainCookieList(act, domainItem, ctx);
				}
			});
			itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					domainItem.selected = !domainItem.selected;
					selectCheckbox.setChecked(domainItem.selected);
					int count = 0;
					for (int j = 0; j < container.getChildCount(); j++) {
						View child = container.getChildAt(j);
						if (child.getTag() instanceof DomainItem) {
							if (((DomainItem) child.getTag()).selected) {
								count++;
							}
						}
					}
					deleteButton.setEnabled(count > 0);
					deleteButton
							.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_delete_selected"));
					return true;
				}
			});
			selectCheckbox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					domainItem.selected = selectCheckbox.isChecked();
					int count = 0;
					for (int j = 0; j < container.getChildCount(); j++) {
						View child = container.getChildAt(j);
						if (child.getTag() instanceof DomainItem) {
							if (((DomainItem) child.getTag()).selected) {
								count++;
							}
						}
					}
					deleteButton.setEnabled(count > 0);
					deleteButton
							.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_delete_selected"));
				}
			});
		}
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.scrollTo(0, 0);
			}
		});
	}
	private void populateDomainList(final Activity act, final LinearLayout container, List<DomainItem> domainItems,
			final Button deleteButton, final ScrollView scrollView, final Context ctx,
			final List<DomainItem> masterDomainList) {
		container.removeAllViews();
		if (domainItems.isEmpty()) {
			TextView emptyText = new TextView(act);
			emptyText.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_empty"));
			emptyText.setTextColor(0xFF888888);
			emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			emptyText.setGravity(Gravity.CENTER);
			emptyText.setPadding(0, dp(act, 32), 0, dp(act, 32));
			container.addView(emptyText);
			deleteButton.setEnabled(false);
			return;
		}
		final int[] selectedCount = {0};
		for (int i = 0; i < domainItems.size(); i++) {
			final DomainItem domainItem = domainItems.get(i);
			domainItem.selected = false;
			LinearLayout itemLayout = new LinearLayout(act);
			itemLayout.setOrientation(LinearLayout.VERTICAL);
			itemLayout.setBackground(getRoundBg(act, 0xFFF8F9FA, 6));
			itemLayout.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));
			LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			itemLp.bottomMargin = dp(act, 8);
			container.addView(itemLayout, itemLp);
			itemLayout.setTag(domainItem);
			LinearLayout firstRow = new LinearLayout(act);
			firstRow.setOrientation(LinearLayout.HORIZONTAL);
			firstRow.setGravity(Gravity.CENTER_VERTICAL);
			final CheckBox selectCheckbox = new CheckBox(act);
			selectCheckbox.setChecked(domainItem.selected);
			selectCheckbox.setScaleX(0.8f);
			selectCheckbox.setScaleY(0.8f);
			firstRow.addView(selectCheckbox);
			TextView domainText = new TextView(act);
			domainText.setText(domainItem.domain);
			domainText.setTextColor(Color.BLACK);
			domainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			domainText.setTypeface(null, Typeface.BOLD);
			domainText.setEllipsize(TextUtils.TruncateAt.END);
			domainText.setSingleLine(true);
			domainText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
			firstRow.addView(domainText);
			TextView countText = new TextView(act);
			String countLabel = LocalizedStringProvider.getInstance().get(act, "cookie_domain_count_label");
			countText.setText(String.format(countLabel, domainItem.getCookieCount()));
			countText.setTextColor(0xFF666666);
			countText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			countLp.leftMargin = dp(act, 8);
			firstRow.addView(countText, countLp);
			itemLayout.addView(firstRow);
			LinearLayout secondRow = new LinearLayout(act);
			secondRow.setOrientation(LinearLayout.VERTICAL);
			secondRow.setPadding(dp(act, 24), dp(act, 4), 0, 0);
			int previewCount = Math.min(3, domainItem.cookies.size());
			for (int j = 0; j < previewCount; j++) {
				CookieItem cookie = domainItem.cookies.get(j);
				TextView cookiePreview = new TextView(act);
				String cookieName = cookie.name != null && !cookie.name.isEmpty()
						? cookie.name
						: LocalizedStringProvider.getInstance().get(act, "cookie_field_unknown");
				String cookieValue = cookie.value != null && !cookie.value.isEmpty()
						? cookie.value
						: LocalizedStringProvider.getInstance().get(act, "cookie_no_value");
				String valueDisplay = cookieValue.length() > 20 ? cookieValue.substring(0, 20) + "..." : cookieValue;
				cookiePreview.setText("• " + cookieName + ": " + valueDisplay);
				cookiePreview.setTextColor(0xFF666666);
				cookiePreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
				secondRow.addView(cookiePreview);
			}
			if (domainItem.cookies.size() > 3) {
				TextView moreText = new TextView(act);
				int moreCount = domainItem.cookies.size() - 3;
				String moreLabel = LocalizedStringProvider.getInstance().get(act, "cookie_domain_more_label");
				moreText.setText(String.format(moreLabel, moreCount));
				moreText.setTextColor(0xFF999999);
				moreText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
				moreText.setPadding(0, dp(act, 2), 0, 0);
				secondRow.addView(moreText);
			}
			itemLayout.addView(secondRow);
			itemLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDomainCookieList(act, domainItem, ctx, masterDomainList);
				}
			});
			itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					domainItem.selected = !domainItem.selected;
					selectCheckbox.setChecked(domainItem.selected);
					int count = 0;
					for (int j = 0; j < container.getChildCount(); j++) {
						View child = container.getChildAt(j);
						if (child.getTag() instanceof DomainItem) {
							if (((DomainItem) child.getTag()).selected) {
								count++;
							}
						}
					}
					deleteButton.setEnabled(count > 0);
					deleteButton
							.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_delete_selected"));
					return true;
				}
			});
			selectCheckbox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					domainItem.selected = selectCheckbox.isChecked();
					int count = 0;
					for (int j = 0; j < container.getChildCount(); j++) {
						View child = container.getChildAt(j);
						if (child.getTag() instanceof DomainItem) {
							if (((DomainItem) child.getTag()).selected) {
								count++;
							}
						}
					}
					deleteButton.setEnabled(count > 0);
					deleteButton
							.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_delete_selected"));
				}
			});
		}
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.scrollTo(0, 0);
			}
		});
	}
	private void showDomainCookieList(final Activity act, final DomainItem domainItem, final Context ctx) {
		showDomainCookieList(act, domainItem, ctx, null);
	}
	private void showDomainCookieList(final Activity act, final DomainItem domainItem, final Context ctx,
			final List<DomainItem> masterDomainList) {
		if (act.isFinishing() || act.isDestroyed())
			return;
		final Dialog dialog = new Dialog(act, android.R.style.Theme_NoTitleBar_Fullscreen);
		dialog.setCancelable(true);
		LinearLayout rootLayout = new LinearLayout(act);
		rootLayout.setOrientation(LinearLayout.VERTICAL);
		rootLayout.setBackgroundColor(Color.WHITE);
		RelativeLayout titleBar = new RelativeLayout(act);
		titleBar.setBackgroundColor(0xFFF5F5F5);
		titleBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
		titleBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
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
		TextView title = new TextView(act);
		title.setText(domainItem.domain);
		title.setTextColor(Color.BLACK);
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		title.setTypeface(null, Typeface.BOLD);
		title.setEllipsize(TextUtils.TruncateAt.END);
		title.setSingleLine(true);
		RelativeLayout.LayoutParams titleLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		titleLp.addRule(RelativeLayout.CENTER_IN_PARENT);
		titleLp.leftMargin = dp(act, 60);
		titleLp.rightMargin = dp(act, 60);
		titleBar.addView(title, titleLp);
		rootLayout.addView(titleBar);
		LinearLayout contentLayout = new LinearLayout(act);
		contentLayout.setOrientation(LinearLayout.VERTICAL);
		contentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		contentLayout.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
		LinearLayout domainInfoLayout = new LinearLayout(act);
		domainInfoLayout.setOrientation(LinearLayout.HORIZONTAL);
		domainInfoLayout.setGravity(Gravity.CENTER_VERTICAL);
		domainInfoLayout.setBackground(getRoundBg(act, 0xFFF0F0F0, 6));
		domainInfoLayout.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
		domainInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		TextView domainInfoText = new TextView(act);
		String cookieCountLabel = LocalizedStringProvider.getInstance().get(act, "cookie_domain_total_count");
		domainInfoText.setText(String.format(cookieCountLabel, domainItem.getCookieCount()));
		domainInfoText.setTextColor(0xFF666666);
		domainInfoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		domainInfoLayout.addView(domainInfoText);
		contentLayout.addView(domainInfoLayout);
		final ScrollView scrollView = new ScrollView(act);
		scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
		final LinearLayout listContainer = new LinearLayout(act);
		listContainer.setOrientation(LinearLayout.VERTICAL);
		listContainer.setPadding(0, dp(act, 12), 0, 0);
		scrollView.addView(listContainer);
		contentLayout.addView(scrollView);
		LinearLayout buttonBar = new LinearLayout(act);
		buttonBar.setOrientation(LinearLayout.HORIZONTAL);
		buttonBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		buttonBar.setPadding(0, dp(act, 12), 0, 0);
		final Button deleteDomainButton = new Button(act);
		applyClickAnim(deleteDomainButton);
		deleteDomainButton.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_delete_selected"));
		deleteDomainButton.setTextColor(Color.WHITE);
		deleteDomainButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		deleteDomainButton.setBackground(getRoundBg(act, 0xFFE53935, 8));
		deleteDomainButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
		deleteDomainButton.setEnabled(false);
		LinearLayout.LayoutParams deleteDomainLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
				1.0f);
		deleteDomainLp.rightMargin = dp(act, 6);
		buttonBar.addView(deleteDomainButton, deleteDomainLp);
		final Button selectAllDomainButton = new Button(act);
		applyClickAnim(selectAllDomainButton);
		selectAllDomainButton.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_select_all"));
		selectAllDomainButton.setTextColor(0xFF6200EE);
		selectAllDomainButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		selectAllDomainButton.setBackground(getRoundBg(act, 0xFFF0F0F0, 8));
		selectAllDomainButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
		LinearLayout.LayoutParams selectAllDomainLp = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
		selectAllDomainLp.leftMargin = dp(act, 6);
		selectAllDomainLp.rightMargin = dp(act, 6);
		buttonBar.addView(selectAllDomainButton, selectAllDomainLp);
		Button closeButton = new Button(act);
		applyClickAnim(closeButton);
		closeButton.setText(LocalizedStringProvider.getInstance().get(act, "dialog_close"));
		closeButton.setTextColor(Color.WHITE);
		closeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		closeButton.setBackground(getRoundBg(act, 0xFF6200EE, 8));
		closeButton.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 8));
		LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
		closeLp.leftMargin = dp(act, 6);
		buttonBar.addView(closeButton, closeLp);
		contentLayout.addView(buttonBar);
		rootLayout.addView(contentLayout);
		dialog.setContentView(rootLayout);
		final boolean[] isDomainAllSelected = {false};
		populateCookieList(act, listContainer, domainItem.cookies, deleteDomainButton, scrollView);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		deleteDomainButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle(LocalizedStringProvider.getInstance().get(act, "cookie_delete_confirm_title"));
				builder.setMessage(LocalizedStringProvider.getInstance().get(act, "cookie_delete_confirm_msg"));
				builder.setPositiveButton(LocalizedStringProvider.getInstance().get(act, "cookie_manager_delete_btn"),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, int which) {
								deleteSelectedCookies(act, listContainer, deleteDomainButton, scrollView,
										new Runnable() {
											@Override
											public void run() {
												List<CookieItem> refreshedCookies = new ArrayList<>();
												SQLiteDatabase db = null;
												Cursor cursor = null;
												try {
													String cookiePath = getCookieFilePath(act);
													db = SQLiteDatabase.openDatabase(cookiePath, null,
															SQLiteDatabase.OPEN_READONLY);
													String selection = "host_key = ?";
													String[] selectionArgs = {domainItem.domain};
													cursor = db.query(COOKIE_TABLE_NAME, null, selection, selectionArgs,
															null, null, "name");
													if (cursor != null && cursor.moveToFirst()) {
														do {
															CookieItem item = new CookieItem();
															item.creation_utc = getLongSafe(cursor, "creation_utc");
															item.host_key = getStringSafe(cursor, "host_key");
															item.name = getStringSafe(cursor, "name");
															item.value = getStringSafe(cursor, "value");
															item.path = getStringSafe(cursor, "path");
															item.expires_utc = getLongSafe(cursor, "expires_utc");
															item.is_secure = getIntSafe(cursor, "is_secure") == 1;
															item.is_httponly = getIntSafe(cursor, "is_httponly") == 1;
															item.last_access_utc = getLongSafe(cursor,
																	"last_access_utc");
															item.is_persistent = getIntSafe(cursor,
																	"is_persistent") == 1;
															item.selected = false;
															refreshedCookies.add(item);
														} while (cursor.moveToNext());
													}
												} catch (Exception e) {
													bvLog("[BetterVia] 重新加载Cookie数据失败: " + e);
												} finally {
													if (cursor != null) {
														cursor.close();
													}
													if (db != null) {
														db.close();
													}
												}
												domainItem.cookies.clear();
												domainItem.cookies.addAll(refreshedCookies);
												if (masterDomainList != null) {
													for (DomainItem masterDomainItem : masterDomainList) {
														if (masterDomainItem.domain.equals(domainItem.domain)) {
															masterDomainItem.cookies.clear();
															masterDomainItem.cookies.addAll(refreshedCookies);
															break;
														}
													}
												}
												populateCookieList(act, listContainer, domainItem.cookies,
														deleteDomainButton, scrollView);
												if (domainItem.cookies.isEmpty()) {
													dialog.dismiss();
													Toast.makeText(act,
															String.format(
																	LocalizedStringProvider.getInstance().get(act,
																			"cookie_domain_delete_success"),
																	domainItem.domain, 0),
															Toast.LENGTH_SHORT).show();
												}
											}
										});
							}
						});
				builder.setNegativeButton(LocalizedStringProvider.getInstance().get(act, "dialog_cancel"),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				builder.show();
			}
		});
		selectAllDomainButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isDomainAllSelected[0] = !isDomainAllSelected[0];
				if (isDomainAllSelected[0]) {
					selectAllDomainButton
							.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_unselect_all"));
					Toast.makeText(act, LocalizedStringProvider.getInstance().get(act, "cookie_manager_selecting"),
							Toast.LENGTH_SHORT).show();
					for (CookieItem item : domainItem.cookies) {
						item.selected = true;
					}
					new Thread(new Runnable() {
						@Override
						public void run() {
							act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									for (int i = 0; i < listContainer.getChildCount(); i++) {
										View child = listContainer.getChildAt(i);
										if (child instanceof LinearLayout) {
											LinearLayout itemLayout = (LinearLayout) child;
											View firstChild = itemLayout.getChildAt(0);
											if (firstChild instanceof LinearLayout) {
												CheckBox checkbox = (CheckBox) ((LinearLayout) firstChild)
														.getChildAt(0);
												if (checkbox != null) {
													checkbox.setChecked(true);
												}
												if (itemLayout.getTag() instanceof CookieItem) {
													((CookieItem) itemLayout.getTag()).selected = true;
												}
											}
										}
									}
									deleteDomainButton.setEnabled(true);
									deleteDomainButton.setText(LocalizedStringProvider.getInstance().get(act,
											"cookie_manager_delete_selected"));
								}
							});
						}
					}).start();
				} else {
					selectAllDomainButton
							.setText(LocalizedStringProvider.getInstance().get(act, "cookie_manager_select_all"));
					Toast.makeText(act, LocalizedStringProvider.getInstance().get(act, "cookie_manager_unselecting"),
							Toast.LENGTH_SHORT).show();
					for (CookieItem item : domainItem.cookies) {
						item.selected = false;
					}
					new Thread(new Runnable() {
						@Override
						public void run() {
							act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									for (int i = 0; i < listContainer.getChildCount(); i++) {
										View child = listContainer.getChildAt(i);
										if (child instanceof LinearLayout) {
											LinearLayout itemLayout = (LinearLayout) child;
											View firstChild = itemLayout.getChildAt(0);
											if (firstChild instanceof LinearLayout) {
												CheckBox checkbox = (CheckBox) ((LinearLayout) firstChild)
														.getChildAt(0);
												if (checkbox != null) {
													checkbox.setChecked(false);
												}
												if (itemLayout.getTag() instanceof CookieItem) {
													((CookieItem) itemLayout.getTag()).selected = false;
												}
											}
										}
									}
									deleteDomainButton.setEnabled(false);
									deleteDomainButton.setText(LocalizedStringProvider.getInstance().get(act,
											"cookie_manager_delete_selected"));
								}
							});
						}
					}).start();
				}
			}
		});
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	private void showDeleteConfirmDialog(final Activity act, final Context ctx, final LinearLayout listContainer,
			final Button deleteButton, final ScrollView scrollView, final boolean isDomainView) {
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setTitle(LocalizedStringProvider.getInstance().get(ctx, "cookie_delete_confirm_title"));
		String confirmMsg;
		if (isDomainView) {
			confirmMsg = LocalizedStringProvider.getInstance().get(ctx, "cookie_domain_delete_selected_confirm_msg");
		} else {
			confirmMsg = LocalizedStringProvider.getInstance().get(ctx, "cookie_delete_confirm_msg");
		}
		builder.setMessage(confirmMsg);
		builder.setPositiveButton(LocalizedStringProvider.getInstance().get(ctx, "cookie_manager_delete_btn"),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (isDomainView) {
							deleteSelectedDomains(act, listContainer, deleteButton, scrollView);
						} else {
							deleteSelectedCookies(act, listContainer, deleteButton, scrollView);
						}
					}
				});
		builder.setNegativeButton(LocalizedStringProvider.getInstance().get(ctx, "dialog_cancel"),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}
	private void deleteSelectedCookies(final Activity act, final LinearLayout listContainer, final Button deleteButton,
			final ScrollView scrollView) {
		deleteSelectedCookies(act, listContainer, deleteButton, scrollView, null);
	}
	private void deleteSelectedCookies(final Activity act, final LinearLayout listContainer, final Button deleteButton,
			final ScrollView scrollView, final Runnable onCompleteCallback) {
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
						for (int i = 0; i < listContainer.getChildCount(); i++) {
							View child = listContainer.getChildAt(i);
							if (child instanceof LinearLayout && child.getTag() instanceof CookieItem) {
								CookieItem item = (CookieItem) child.getTag();
								if (item.selected) {
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
					bvLog("[BetterVia] 批量删除Cookie失败: " + e);
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
							for (CookieItem deletedItem : deletedItems) {
								removeDeletedCookieFromList(act, listContainer, deleteButton, scrollView, deletedItem);
							}
							Toast.makeText(act, LocalizedStringProvider.getInstance().get(act, "cookie_delete_success"),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(act,
									LocalizedStringProvider.getInstance().get(act, "cookie_delete_no_selected"),
									Toast.LENGTH_SHORT).show();
						}
						if (onCompleteCallback != null) {
							onCompleteCallback.run();
						}
					}
				});
			}
		}).start();
	}
	private void deleteSelectedDomains(final Activity act, final LinearLayout listContainer, final Button deleteButton,
			final ScrollView scrollView) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SQLiteDatabase db = null;
				final List<DomainItem> deletedDomains = new ArrayList<>();
				final List<CookieItem> deletedCookies = new ArrayList<>();
				try {
					String cookiePath = getCookieFilePath(act);
					db = SQLiteDatabase.openDatabase(cookiePath, null, SQLiteDatabase.OPEN_READWRITE);
					db.beginTransaction();
					try {
						for (int i = 0; i < listContainer.getChildCount(); i++) {
							View child = listContainer.getChildAt(i);
							if (child instanceof LinearLayout && child.getTag() instanceof DomainItem) {
								DomainItem domainItem = (DomainItem) child.getTag();
								if (domainItem.selected) {
									for (CookieItem cookie : domainItem.cookies) {
										String whereClause = "creation_utc = ? AND host_key = ? AND name = ?";
										String[] whereArgs = {String.valueOf(cookie.creation_utc), cookie.host_key,
												cookie.name};
										db.delete(COOKIE_TABLE_NAME, whereClause, whereArgs);
										deletedCookies.add(cookie);
									}
									deletedDomains.add(domainItem);
								}
							}
						}
						db.setTransactionSuccessful();
					} finally {
						db.endTransaction();
					}
				} catch (Exception e) {
					bvLog("[BetterVia] 批量删除域名Cookie失败: " + e);
				} finally {
					if (db != null) {
						db.close();
					}
				}
				final int finalDomainCount = deletedDomains.size();
				final int finalCookieCount = deletedCookies.size();
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (finalDomainCount > 0) {
							for (int i = listContainer.getChildCount() - 1; i >= 0; i--) {
								View child = listContainer.getChildAt(i);
								if (child instanceof LinearLayout && child.getTag() instanceof DomainItem) {
									DomainItem domainItem = (DomainItem) child.getTag();
									if (domainItem.selected) {
										listContainer.removeViewAt(i);
									}
								}
							}
							updateDeleteButtonState(act, listContainer, deleteButton);
							String successMsg = String.format(
									LocalizedStringProvider.getInstance().get(act,
											"cookie_domain_delete_selected_success"),
									finalDomainCount, finalCookieCount);
							Toast.makeText(act, successMsg, Toast.LENGTH_SHORT).show();
							if (listContainer.getChildCount() == 0) {
								showEmptyCookieListState(act, listContainer);
							}
						} else {
							Toast.makeText(act,
									LocalizedStringProvider.getInstance().get(act, "cookie_delete_no_selected"),
									Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}).start();
	}
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
	private void loadFileContent(final Context ctx, final String fileName, final EditText editor,
			final ScrollView scrollView, final boolean fromCache) {
		if (fromCache && editorStateCache.containsKey(fileName)) {
			final EditorState state = editorStateCache.get(fileName);
			editor.setText(state.content);
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
						content.append("
					}
					((Activity) ctx).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							editor.setText(content.toString());
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
	private void saveCurrentEditorState(String fileName, EditText editor, ScrollView scrollView) {
		if (editor != null && scrollView != null) {
			String content = editor.getText().toString();
			int scrollY = scrollView.getScrollY();
			editorStateCache.put(fileName, new EditorState(content, scrollY));
		}
	}
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
					bvLog("[BetterVia] 保存文件失败: " + e);
				}
			}
		}).start();
	}
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
			Cursor tableCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
					new String[]{COOKIE_TABLE_NAME});
			if (!tableCursor.moveToFirst()) {
				tableCursor.close();
				return cookieItems;
			}
			tableCursor.close();
			cursor = db.query(COOKIE_TABLE_NAME, null, null, null, null, null, "host_key, name");
			if (cursor != null && cursor.moveToFirst()) {
				do {
					try {
						CookieItem item = new CookieItem();
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
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			bvLog("[BetterVia] 读取Cookie数据失败: " + e);
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
	private List<DomainItem> loadDomainGroupedCookieData(Context ctx) {
		List<DomainItem> domainItems = new ArrayList<>();
		Map<String, DomainItem> domainMap = new HashMap<>();
		List<CookieItem> cookieItems = loadCookieData(ctx);
		for (CookieItem cookie : cookieItems) {
			String domain = cookie.host_key;
			if (domain == null || domain.isEmpty()) {
				domain = LocalizedStringProvider.getInstance().get(ctx, "cookie_unknown_domain");
			}
			DomainItem domainItem = domainMap.get(domain);
			if (domainItem == null) {
				domainItem = new DomainItem(domain);
				domainMap.put(domain, domainItem);
				domainItems.add(domainItem);
			}
			domainItem.addCookie(cookie);
		}
		Collections.sort(domainItems, new Comparator<DomainItem>() {
			@Override
			public int compare(DomainItem d1, DomainItem d2) {
				return d1.domain.compareToIgnoreCase(d2.domain);
			}
		});
		return domainItems;
	}
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
	private static class CookieItem {
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
		String top_frame_site_key;
		String encrypted_value;
		boolean has_expires;
		int priority;
		int samesite;
		int source_scheme;
		int source_port;
		long last_update_utc;
		int source_type;
		boolean has_cross_site_ancestor;
		boolean selected;
		CookieItem() {
			this.selected = false;
			this.creation_utc = 0;
			this.expires_utc = 0;
			this.last_access_utc = 0;
			this.last_update_utc = 0;
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
			this.host_key = "";
			this.name = "";
			this.value = "";
			this.path = "";
			this.top_frame_site_key = "";
			this.encrypted_value = "";
		}
	}
	private static class DomainItem {
		String domain;
		List<CookieItem> cookies;
		boolean selected;
		DomainItem(String domain) {
			this.domain = domain;
			this.cookies = new ArrayList<>();
			this.selected = false;
		}
		void addCookie(CookieItem cookie) {
			cookies.add(cookie);
		}
		int getCookieCount() {
			return cookies.size();
		}
	}
	private String getCookieFilePath(Context ctx) {
		String packageName = ctx.getPackageName();
		return "/data/user/0/" + packageName + "/app_webview/Default/Cookies";
	}
	private void addImagePickerItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_config"));
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
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showHomepageBeautyDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				homepageBgPath = getPrefString(ctx, KEY_HOMEPAGE_BG, "");
				homepageMaskAlpha = getPrefInt(ctx, KEY_HOMEPAGE_MASK_A, 120);
				int savedRgb = getPrefInt(ctx, KEY_HOMEPAGE_MASK_C, 0x000000);
				final Dialog dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);
				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(Color.BLACK);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_dialog_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 16));
				root.addView(subtitle);
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
				refreshPreview(ctx, imageView, maskView, homepageMaskAlpha, savedRgb);
				Button pickBtn = new Button(act);
				applyClickAnim(pickBtn);
				pickBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_pick_btn"));
				pickBtn.setTextColor(Color.WHITE);
				pickBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				pickBtn.setTypeface(null, Typeface.BOLD);
				pickBtn.setBackground(getRoundBg(act, 0xFF6200EE, 12));
				root.addView(pickBtn);
				TextView alphaTitle = new TextView(act);
				alphaTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_mask_alpha"));
				alphaTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				alphaTitle.setTextColor(Color.BLACK);
				alphaTitle.setTypeface(null, Typeface.BOLD);
				alphaTitle.setPadding(0, dp(act, 16), 0, 0);
				root.addView(alphaTitle);
				final SeekBar alphaSeek = new SeekBar(act);
				alphaSeek.setMax(255);
				alphaSeek.setProgress(homepageMaskAlpha);
				root.addView(alphaSeek);
				TextView colorTitle = new TextView(act);
				colorTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_mask_color_rgb"));
				colorTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				colorTitle.setTextColor(Color.BLACK);
				colorTitle.setTypeface(null, Typeface.BOLD);
				colorTitle.setPadding(0, dp(act, 12), 0, 0);
				root.addView(colorTitle);
				LinearLayout rgbContainer = new LinearLayout(act);
				rgbContainer.setOrientation(LinearLayout.VERTICAL);
				final EditText rgbEdit = new EditText(act);
				rgbEdit.setHint("#RRGGBB");
				rgbEdit.setText(colorToRgbString(savedRgb));
				rgbEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				rgbEdit.setBackground(getRoundBg(act, 0xFFF5F5F5, 4));
				rgbEdit.setPadding(dp(act, 8), dp(act, 8), dp(act, 8), dp(act, 8));
				LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				rgbContainer.addView(rgbEdit, editLp);
				TextView rgbHint = new TextView(act);
				rgbHint.setText(LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_mask_color_hint"));
				rgbHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				rgbHint.setTextColor(0xFF888888);
				rgbHint.setPadding(dp(act, 4), dp(act, 4), 0, 0);
				rgbContainer.addView(rgbHint);
				root.addView(rgbContainer);
				SeekBar.OnSeekBarChangeListener alphaListener = new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if (fromUser) {
							homepageMaskAlpha = progress;
							String rgbStr = rgbEdit.getText().toString();
							int rgbColor = parseRgbColor(rgbStr, 0);
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
						int rgbColor = parseRgbColor(rgbStr, 0);
						refreshPreview(ctx, imageView, maskView, homepageMaskAlpha, rgbColor);
					}
				});
				LinearLayout btnRow = new LinearLayout(act);
				btnRow.setOrientation(LinearLayout.HORIZONTAL);
				btnRow.setGravity(Gravity.CENTER);
				btnRow.setPadding(0, dp(act, 24), 0, 0);
				Button cancel = new Button(act);
				applyClickAnim(cancel);
				cancel.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_cancel"));
				cancel.setTextColor(0xFF6200EE);
				cancel.setBackground(getRoundBg(act, 0xFFE0E0E0, 12));
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
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
				cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String rgbStr = rgbEdit.getText().toString();
						int rgbColor = parseRgbColor(rgbStr, 0);
						if (rgbStr.trim().length() > 0 && !isValidRgbColor(rgbStr)) {
							Toast.makeText(ctx,
									LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_mask_color_invalid"),
									Toast.LENGTH_SHORT).show();
							return;
						}
						putPrefInt(ctx, KEY_HOMEPAGE_MASK_A, homepageMaskAlpha);
						putPrefInt(ctx, KEY_HOMEPAGE_MASK_C, rgbColor);
						Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "homepage_bg_saved"),
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				});
				pickBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(Intent.ACTION_GET_CONTENT);
						i.setType("image
	private boolean isValidRgbColor(String rgbStr) {
		if (rgbStr == null || rgbStr.trim().isEmpty()) {
			return true;
		}
		String colorStr = rgbStr.trim();
		if (!colorStr.startsWith("#")) {
			colorStr = "#" + colorStr;
		}
		return colorStr.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
	}
	private void refreshPreview(Context ctx, ImageView iv, View mask, int alpha, int rgbColor) {
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
		int finalColor = (alpha << 24) | (rgbColor & 0x00FFFFFF);
		mask.setBackgroundColor(finalColor);
	}
	private void hookHomepageBgWithMask(final Context ctx, ClassLoader cl, final String imgPath, final int maskColor) {
		XposedHelpers.findAndHookMethod("k.a.c0.l.c", cl, "g", Context.class, List.class, boolean.class,
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
						int alpha = getPrefInt(ctx, KEY_HOMEPAGE_MASK_A, 120);
						int rgbColor = getPrefInt(ctx, KEY_HOMEPAGE_MASK_C, 0x000000);
						bvLog("[BetterVia] 读取设置透明度: " + alpha + ", 颜色值: " + Integer.toHexString(rgbColor));
						String cssColor = colorToCssString(alpha, rgbColor);
						bvLog("[BetterVia] 最终颜色值转换: " + cssColor);
						String backgroundStyle;
						if (imgPath != null && !imgPath.isEmpty() && new File(imgPath).exists()) {
							String encodedPath = imgPath.replace("'", "\\'").replace("\\", "\\\\");
							backgroundStyle = "background:url('file:
									+ "') no-repeat center/cover fixed;";
						} else {
							backgroundStyle = "background-color:#F0F0F0;";
						}
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
							bvLog("[BetterVia] 成功应用具有正确颜色格式的背景");
						} catch (Exception e) {
							bvLog("[BetterVia] 写入修改HTML时出错: " + e);
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
	private void handleActivityResult(int requestCode, int resultCode, Intent data, final Activity activity) {
		if (resultCode != Activity.RESULT_OK || data == null)
			return;
		if (requestCode == 0x1002) {
			Uri uri = data.getData();
			if (saveUserImage(activity, uri)) {
				homepageBgPath = getPrefString(activity, KEY_HOMEPAGE_BG, "");
				if (Context != null && Context instanceof Activity) {
					((Activity) Context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(activity,
									LocalizedStringProvider.getInstance().get(activity, "homepage_bg_set_ok"),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		} else if (requestCode == 0x2001) {
			final Uri uri = data.getData();
			if (generatedApkPath != null && uri != null) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							File sourceFile = new File(generatedApkPath);
							InputStream in = null;
							OutputStream out = null;
							try {
								in = new FileInputStream(sourceFile);
								out = activity.getContentResolver().openOutputStream(uri);
								byte[] buffer = new byte[8192];
								int len;
								while ((len = in.read(buffer)) != -1) {
									out.write(buffer, 0, len);
								}
								out.flush();
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(activity, LocalizedStringProvider.getInstance().get(activity,
												"homepage_bg_saved"), Toast.LENGTH_SHORT).show();
									}
								});
							} finally {
								if (in != null) {
									try {
										in.close();
									} catch (Exception ignored) {
									}
								}
								if (out != null) {
									try {
										out.close();
									} catch (Exception ignored) {
									}
								}
							}
						} catch (final Exception e) {
							bvLog("[BetterVia] 保存APK文件失败: " + e);
							activity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(activity, "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				}).start();
			}
		}
	}
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
			homepageBgPath = outFile.getAbsolutePath();
			putPrefString(act, KEY_HOMEPAGE_BG, homepageBgPath);
			return true;
		} catch (Exception e) {
			bvLog("[BetterVia] 保存用户图片时出现错误: " + e);
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
	private String colorToCssString(int alpha, int rgbColor) {
		int r = (rgbColor >> 16) & 0xFF;
		int g = (rgbColor >> 8) & 0xFF;
		int b = rgbColor & 0xFF;
		float alphaFloat = alpha / 255.0f;
		return String.format(Locale.US, "rgba(%d, %d, %d, %.2f)", r, g, b, alphaFloat);
	}
	private int parseRgbColor(String rgbStr, int defaultAlpha) {
		if (rgbStr == null || rgbStr.trim().isEmpty()) {
			return 0xFFFFFF;
		}
		String colorStr = rgbStr.trim();
		if (!colorStr.startsWith("#")) {
			colorStr = "#" + colorStr;
		}
		try {
			if (colorStr.length() == 7) {
				return Color.parseColor(colorStr) & 0x00FFFFFF;
			}
		} catch (Exception e) {
		}
		return 0xFFFFFF;
	}
	private String colorToRgbString(int color) {
		return String.format("#%06X", color & 0x00FFFFFF);
	}
	private void addBlockMenuBarItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "block_menu_bar_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "block_menu_bar_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showBlockMenuBarDialog(ctx);
			}
		});
		hor.addView(configBtn);
		TextView hintTv = new TextView(ctx);
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "block_menu_bar_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showBlockMenuBarDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "block_menu_bar_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				title.setTextColor(Color.BLACK);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 16);
				root.addView(title, titleLp);
				TextView hint = new TextView(act);
				hint.setText(LocalizedStringProvider.getInstance().get(ctx, "block_menu_bar_dialog_hint"));
				hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				hint.setTextColor(0xFF666666);
				LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				hintLp.bottomMargin = dp(act, 12);
				root.addView(hint, hintLp);
				final EditText urlEdit = new EditText(act);
				urlEdit.setHint(LocalizedStringProvider.getInstance().get(ctx, "block_menu_bar_input_hint"));
				urlEdit.setText(getPrefString(ctx, KEY_BLOCK_MENU_BAR, ""));
				urlEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				urlEdit.setBackground(getRoundBg(act, 0xFFF5F5F5, 4));
				urlEdit.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));
				urlEdit.setMinLines(3);
				urlEdit.setGravity(Gravity.TOP | Gravity.START);
				LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				editLp.bottomMargin = dp(act, 16);
				root.addView(urlEdit, editLp);
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				GradientDrawable btnBg = new GradientDrawable();
				btnBg.setColor(0xFF6200EE);
				btnBg.setCornerRadius(dp(act, 12));
				ok.setBackground(btnBg);
				LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				root.addView(ok, okLp);
				scrollRoot.addView(root);
				final AlertDialog dialog = new AlertDialog.Builder(act).setView(scrollRoot).create();
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
				ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String urls = urlEdit.getText().toString().trim();
						putPrefString(ctx, KEY_BLOCK_MENU_BAR, urls);
						Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "block_menu_bar_saved"),
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}
	private XC_MethodHook.Unhook blockMenuBarHook = null;
	private void setBlockMenuBarHook(final Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (blockMenuBarHook == null) {
				try {
					XposedHelpers.findAndHookMethod("k.a.y.ya", cl, "Sb", int.class, int.class, String.class,
							String.class, String.class, new XC_MethodHook() {
								@Override
								protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
									String currentUrl = getCurrentUrl();
									if (currentUrl == null || currentUrl.isEmpty()) {
										return;
									}
									String blockUrls = getPrefString(ctx, KEY_BLOCK_MENU_BAR, "");
									if (blockUrls == null || blockUrls.trim().isEmpty()) {
										return;
									}
									String[] urlList = blockUrls.split(",");
									boolean shouldBlock = false;
									for (String url : urlList) {
										String trimmedUrl = url.trim();
										if (!trimmedUrl.isEmpty() && currentUrl.contains(trimmedUrl)) {
											shouldBlock = true;
											bvLog("[BetterVia] 屏蔽菜单栏: 当前URL " + currentUrl + " 匹配 " + trimmedUrl);
											break;
										}
									}
									if (shouldBlock) {
										param.setResult(null);
										bvLog("[BetterVia] 已阻止菜单栏显示");
									}
								}
							});
					bvLog("[BetterVia] 屏蔽菜单栏Hook已启用");
				} catch (Throwable e) {
					bvLog("[BetterVia] 屏蔽菜单栏Hook失败: " + e.getMessage());
				}
			}
		} else {
			if (blockMenuBarHook != null) {
				blockMenuBarHook.unhook();
				blockMenuBarHook = null;
				bvLog("[BetterVia] 屏蔽菜单栏Hook已停用");
			}
		}
	}
	private String getCurrentUrl() {
		try {
			if (Context != null && Context instanceof Activity) {
				Activity activity = (Activity) Context;
				try {
					Object webView = XposedHelpers.getObjectField(activity, "u");
					if (webView instanceof WebView) {
						String url = ((WebView) webView).getUrl();
						if (url != null && !url.isEmpty()) {
							return url;
						}
					}
				} catch (Exception e1) {
					try {
						Object webView = XposedHelpers.getObjectField(activity, "webView");
						if (webView instanceof WebView) {
							String url = ((WebView) webView).getUrl();
							if (url != null && !url.isEmpty()) {
								return url;
							}
						}
					} catch (Exception e2) {
					}
				}
				View currentFocus = activity.getCurrentFocus();
				if (currentFocus instanceof WebView) {
					String url = ((WebView) currentFocus).getUrl();
					if (url != null && !url.isEmpty()) {
						return url;
					}
				}
				ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
				WebView webView = findWebViewRecursive(decorView);
				if (webView != null) {
					String url = webView.getUrl();
					if (url != null && !url.isEmpty()) {
						return url;
					}
				}
			}
		} catch (Exception e) {
			bvLog("[BetterVia] 获取当前URL失败: " + e.getMessage());
		}
		return "";
	}
	private void addUserAgentItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "user_agent_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "user_agent_config"));
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
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "user_agent_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showUserAgentDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
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
				scrollRoot.setPadding(0, 0, 0, 0);
				LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "user_agent_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTextColor(0xFF333333);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "user_agent_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 16));
				root.addView(subtitle);
				final LinearLayout uaContainer = new LinearLayout(act);
				uaContainer.setOrientation(LinearLayout.VERTICAL);
				uaContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				root.addView(uaContainer);
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_ok"));
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
				populateUserAgentList(act, ctx, uaContainer);
				dialog.show();
			}
		});
	}
	private void populateUserAgentList(final Activity act, final Context ctx, LinearLayout container) {
		container.removeAllViews();
		List<UserAgentInfo> uaList = getPersonalizedUserAgents(act);
		for (final UserAgentInfo uaInfo : uaList) {
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
			TextView browserName = new TextView(act);
			browserName.setText(uaInfo.browserName);
			browserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			browserName.setTextColor(Color.BLACK);
			browserName.setTypeface(null, Typeface.BOLD);
			uaItem.addView(browserName);
			final TextView uaText = new TextView(act);
			uaText.setText(uaInfo.userAgent);
			uaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			uaText.setTextColor(0xFF666666);
			uaText.setSingleLine(true);
			uaText.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			uaText.setPadding(0, dp(act, 8), 0, dp(act, 8));
			uaItem.addView(uaText);
			Button copyBtn = new Button(act);
			applyClickAnim(copyBtn);
			copyBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "user_agent_copy"));
			copyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
			copyBtn.setTextColor(Color.WHITE);
			copyBtn.setPadding(dp(act, 12), dp(act, 4), dp(act, 12), dp(act, 4));
			copyBtn.setMinHeight(dp(act, 28));
			GradientDrawable btnBg = new GradientDrawable();
			btnBg.setColor(0xFF3498DB);
			btnBg.setCornerRadius(dp(act, 6));
			copyBtn.setBackground(btnBg);
			LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			btnLp.gravity = Gravity.END;
			uaItem.addView(copyBtn, btnLp);
			copyBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					copyToClipboard(act, uaInfo.userAgent);
					Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "user_agent_copied"),
							Toast.LENGTH_SHORT).show();
				}
			});
			uaItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					copyToClipboard(act, uaInfo.userAgent);
					Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "user_agent_copied"),
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	private static class UserAgentInfo {
		String browserName;
		String userAgent;
		UserAgentInfo(String browserName, String userAgent) {
			this.browserName = browserName;
			this.userAgent = userAgent;
		}
	}
	private List<UserAgentInfo> getPersonalizedUserAgents(Context ctx) {
		List<UserAgentInfo> uaList = new ArrayList<>();
		String deviceModel = Build.MODEL;
		String androidVersion = "Android " + Build.VERSION.RELEASE;
		String buildVersion = Build.DISPLAY;
		if (buildVersion == null || buildVersion.isEmpty()) {
			buildVersion = "PKQ1.181007.001";
		}
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
				String personalizedUA = uaTemplate.replace("{android_version}", androidVersion)
						.replace("{device_model}", deviceModel).replace("{build_version}", buildVersion);
				uaList.add(new UserAgentInfo(browserName, personalizedUA));
			}
		}
		return uaList;
	}
	private void setDownloadDialogShareHook(Context ctx, ClassLoader cl, boolean on) {
		if (on) {
			if (downloadDialogShareHook == null) {
				addShareButtonToDownloadDialog(ctx, cl);
				downloadDialogShareEnabled = true;
				bvLog("[BetterVia] 下载对话框分享按钮已启用");
			}
		} else {
			if (downloadDialogShareHook != null) {
				downloadDialogShareHook.unhook();
				downloadDialogShareHook = null;
				bvLog("[BetterVia] 下载对话框分享按钮已停用");
			}
			downloadDialogShareEnabled = false;
		}
		putPrefBoolean(ctx, KEY_DOWNLOAD_DIALOG_SHARE, on);
	}
	private void addShareButtonToDownloadDialog(final Context ctx, ClassLoader cl) {
		try {
			downloadDialogShareHook = XposedHelpers.findAndHookMethod(Dialog.class, "show", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					try {
						final Dialog dialog = (Dialog) param.thisObject;
						if (!downloadDialogShareEnabled) {
							return;
						}
						if (isViaDownloadDialog(dialog)) {
							new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
								@Override
								public void run() {
									try {
										addShareButtonToDialog(dialog, ctx);
									} catch (Exception e) {
										bvLog("[BetterVia] 添加分享按钮异常: " + e);
									}
								}
							}, 100);
						}
					} catch (Exception e) {
						bvLog("[BetterVia] Hook Dialog.show失败: " + e);
					}
				}
			});
			XposedHelpers.findAndHookMethod(AlertDialog.Builder.class, "create", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					try {
						final AlertDialog dialog = (AlertDialog) param.getResult();
						if (dialog == null)
							return;
						if (!downloadDialogShareEnabled) {
							return;
						}
						dialog.setOnShowListener(new DialogInterface.OnShowListener() {
							@Override
							public void onShow(DialogInterface dialogInterface) {
								try {
									if (isViaDownloadDialog(dialog)) {
										addShareButtonToDialog(dialog, ctx);
									}
								} catch (Exception e) {
									bvLog("[BetterVia] AlertDialog显示监听异常: " + e);
								}
							}
						});
					} catch (Exception e) {
						bvLog("[BetterVia] Hook AlertDialog.create失败: " + e);
					}
				}
			});
			bvLog("[BetterVia] 下载对话框分享按钮Hook已启用");
		} catch (Throwable t) {
			bvLog("[BetterVia] Hook下载对话框失败: " + t);
		}
	}
	private boolean isViaDownloadDialog(Dialog dialog) {
		try {
			View copyLinkButton = dialog.findViewById(0x7f0900c7);
			View cancelButton = dialog.findViewById(0x7f0900c6);
			View okButton = dialog.findViewById(0x7f0900cb);
			return copyLinkButton != null && cancelButton != null && okButton != null;
		} catch (Exception e) {
			return false;
		}
	}
	private ViewGroup findButtonContainerRecursive(View view) {
		if (!(view instanceof ViewGroup))
			return null;
		ViewGroup group = (ViewGroup) view;
		int buttonCount = 0;
		for (int i = 0; i < group.getChildCount(); i++) {
			View child = group.getChildAt(i);
			if (child instanceof Button) {
				buttonCount++;
			}
		}
		if (buttonCount >= 2) {
			return group;
		}
		for (int i = 0; i < group.getChildCount(); i++) {
			View child = group.getChildAt(i);
			if (child instanceof ViewGroup) {
				ViewGroup result = findButtonContainerRecursive(child);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	private void setupShareButtonClick(TextView shareButton, final Dialog dialog, final Context ctx) {
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					bvLog("[BetterVia] 分享按钮被点击");
					String[] downloadInfo = extractDownloadInfoFromDialog(dialog, ctx);
					String fileName = downloadInfo[0];
					String fileUrl = downloadInfo[1];
					String fileSize = downloadInfo[2];
					bvLog("[BetterVia] 提取到的下载信息 - 文件名: " + fileName + ", 大小: " + fileSize + ", URL: "
							+ (fileUrl.isEmpty() ? "空" : "已获取"));
					if (fileUrl.isEmpty()) {
						Toast.makeText(ctx, "无法获取下载链接", Toast.LENGTH_SHORT).show();
						return;
					}
					String shareText = createShareText(fileName, fileSize, fileUrl, ctx);
					Intent shareIntent = new Intent(Intent.ACTION_SEND);
					shareIntent.setType("text/plain");
					shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
					Intent chooser = Intent.createChooser(shareIntent, "分享下载链接");
					chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ctx.startActivity(chooser);
					Toast.makeText(ctx, "正在分享下载链接", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					bvLog("[BetterVia] 分享失败: " + e);
					Toast.makeText(ctx, "分享失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	private void insertShareButtonToContainer(ViewGroup container, TextView shareButton, Dialog dialog) {
		try {
			View copyLinkButton = dialog.findViewById(0x7f0900c7);
			if (copyLinkButton == null) {
				bvLog("[BetterVia] 未找到复制链接按钮，无法确定插入位置");
				return;
			}
			View okButton = dialog.findViewById(0x7f0900cb);
			View cancelButton = dialog.findViewById(0x7f0900c6);
			int referenceMargin = dp(dialog.getContext(), 8);
			if (okButton != null && cancelButton != null) {
				if (okButton.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
					ViewGroup.MarginLayoutParams okParams = (ViewGroup.MarginLayoutParams) okButton.getLayoutParams();
					referenceMargin = okParams.rightMargin;
					bvLog("[BetterVia] 获取到确定按钮的右边距: " + referenceMargin + "px");
				}
			}
			int copyLinkIndex = -1;
			for (int i = 0; i < container.getChildCount(); i++) {
				if (container.getChildAt(i) == copyLinkButton) {
					copyLinkIndex = i;
					break;
				}
			}
			if (copyLinkIndex == -1) {
				bvLog("[BetterVia] 复制链接按钮不在容器中");
				return;
			}
			if (container instanceof RelativeLayout) {
				bvLog("[BetterVia] 检测到RelativeLayout容器，设置分享按钮在复制链接按钮右侧，间距: " + referenceMargin + "px");
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_TOP, copyLinkButton.getId());
				params.addRule(RelativeLayout.ALIGN_BOTTOM, copyLinkButton.getId());
				params.addRule(RelativeLayout.RIGHT_OF, copyLinkButton.getId());
				if (copyLinkButton.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
					ViewGroup.MarginLayoutParams refParams = (ViewGroup.MarginLayoutParams) copyLinkButton
							.getLayoutParams();
					params.setMargins(referenceMargin, 
							refParams.topMargin, refParams.rightMargin, refParams.bottomMargin);
					params.height = refParams.height;
				} else {
					params.setMargins(referenceMargin, 0, 0, 0);
				}
				shareButton.setLayoutParams(params);
			} else if (container instanceof LinearLayout) {
				LinearLayout.LayoutParams refParams = (LinearLayout.LayoutParams) copyLinkButton.getLayoutParams();
				LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(refParams.width, refParams.height,
						refParams.weight);
				newParams.setMargins(refParams.leftMargin, refParams.topMargin, refParams.rightMargin,
						refParams.bottomMargin);
				newParams.gravity = refParams.gravity;
				shareButton.setLayoutParams(newParams);
			} else {
				ViewGroup.LayoutParams refParams = copyLinkButton.getLayoutParams();
				ViewGroup.LayoutParams newParams = new ViewGroup.LayoutParams(refParams.width, refParams.height);
				shareButton.setLayoutParams(newParams);
			}
			container.addView(shareButton, copyLinkIndex + 1);
			bvLog("[BetterVia] 分享按钮已插入到位置: " + (copyLinkIndex + 1));
			container.requestLayout();
		} catch (Exception e) {
			bvLog("[BetterVia] 插入分享按钮失败: " + e);
			try {
				ViewGroup.LayoutParams simpleParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				shareButton.setLayoutParams(simpleParams);
				container.addView(shareButton);
			} catch (Exception e2) {
				bvLog("[BetterVia] 备用方案也失败: " + e2);
			}
		}
	}
	private TextView createShareButton(Context ctx, Dialog dialog) {
		try {
			View copyLinkButton = dialog.findViewById(0x7f0900c7);
			if (copyLinkButton == null) {
				bvLog("[BetterVia] 未找到复制链接按钮，无法获取样式");
				return null;
			}
			TextView shareButton = new TextView(ctx);
			shareButton.setId(0x7f09abcd);
			shareButton.setClickable(true);
			shareButton.setFocusable(true);
			shareButton.setText(LocalizedStringProvider.getInstance().get(ctx, "download_dialog_share"));
			shareButton.setTextSize(14);
			shareButton.setGravity(Gravity.CENTER);
			if (copyLinkButton instanceof TextView) {
				TextView refTextView = (TextView) copyLinkButton;
				shareButton.setTextColor(refTextView.getTextColors());
				shareButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, refTextView.getTextSize());
			} else {
				shareButton.setTextColor(0xFF6200EE);
			}
			Drawable refBackground = copyLinkButton.getBackground();
			if (refBackground != null) {
				try {
					Drawable backgroundCopy = refBackground.getConstantState().newDrawable().mutate();
					shareButton.setBackground(backgroundCopy);
					bvLog("[BetterVia] 已创建独立的背景Drawable");
				} catch (Exception e) {
					bvLog("[BetterVia] 创建独立Drawable失败，使用原始背景: " + e);
					shareButton.setBackground(refBackground);
				}
			}
			shareButton.setPadding(copyLinkButton.getPaddingLeft(), copyLinkButton.getPaddingTop(),
					copyLinkButton.getPaddingRight(), copyLinkButton.getPaddingBottom());
			return shareButton;
		} catch (Exception e) {
			bvLog("[BetterVia] 创建分享按钮失败: " + e);
			return null;
		}
	}
	private void fixCopyLinkButtonBackground(Dialog dialog) {
		try {
			View copyLinkButton = dialog.findViewById(0x7f0900c7);
			if (copyLinkButton == null)
				return;
			Drawable background = copyLinkButton.getBackground();
			if (background != null) {
				Drawable independentBackground = background.getConstantState().newDrawable().mutate();
				copyLinkButton.setBackground(independentBackground);
				bvLog("[BetterVia] 已修复复制链接按钮的背景状态");
			}
		} catch (Exception e) {
			bvLog("[BetterVia] 修复复制链接按钮背景失败: " + e);
		}
	}
	private void addShareButtonToDialog(final Dialog dialog, final Context ctx) {
		try {
			if (dialog.findViewById(0x7f09abcd) != null) {
				bvLog("[BetterVia] 分享按钮已存在，跳过添加");
				return;
			}
			fixCopyLinkButtonBackground(dialog);
			ViewGroup buttonContainer = findButtonContainer(dialog);
			if (buttonContainer == null) {
				bvLog("[BetterVia] 未找到按钮容器");
				return;
			}
			bvLog("[BetterVia] 找到按钮容器，类型: " + buttonContainer.getClass().getSimpleName() + ", 子视图数量: "
					+ buttonContainer.getChildCount());
			logButtonInfo(buttonContainer, "添加分享按钮前");
			TextView shareButton = createShareButton(ctx, dialog);
			if (shareButton == null) {
				bvLog("[BetterVia] 创建分享按钮失败");
				return;
			}
			insertShareButtonToContainer(buttonContainer, shareButton, dialog);
			setupShareButtonClick(shareButton, dialog, ctx);
			bvLog("[BetterVia] 成功添加TextView分享按钮到下载对话框");
			logButtonInfo(buttonContainer, "添加分享按钮后");
		} catch (Exception e) {
			bvLog("[BetterVia] 添加分享按钮到对话框失败: " + e);
		}
	}
	private ViewGroup findButtonContainer(Dialog dialog) {
		try {
			View copyLinkButton = dialog.findViewById(0x7f0900c7);
			View cancelButton = dialog.findViewById(0x7f0900c6);
			View okButton = dialog.findViewById(0x7f0900cb);
			if (copyLinkButton != null) {
				ViewGroup parent = (ViewGroup) copyLinkButton.getParent();
				while (parent != null) {
					boolean hasCopyLink = parent.indexOfChild(copyLinkButton) >= 0;
					boolean hasCancel = parent.indexOfChild(cancelButton) >= 0;
					boolean hasOk = parent.indexOfChild(okButton) >= 0;
					if (hasCopyLink && hasCancel && hasOk) {
						bvLog("[BetterVia] 找到包含所有按钮的容器: " + parent.getClass().getSimpleName());
						return parent;
					}
					if (parent.getParent() instanceof ViewGroup) {
						parent = (ViewGroup) parent.getParent();
					} else {
						break;
					}
				}
			}
			View decorView = dialog.getWindow().getDecorView();
			return findHorizontalButtonContainer(decorView);
		} catch (Exception e) {
			bvLog("[BetterVia] 查找按钮容器失败: " + e);
			return null;
		}
	}
	private ViewGroup findHorizontalButtonContainer(View view) {
		if (!(view instanceof ViewGroup))
			return null;
		ViewGroup group = (ViewGroup) view;
		if (group instanceof LinearLayout) {
			LinearLayout layout = (LinearLayout) group;
			if (layout.getOrientation() == LinearLayout.HORIZONTAL) {
				int buttonCount = 0;
				for (int i = 0; i < layout.getChildCount(); i++) {
					View child = layout.getChildAt(i);
					if (child instanceof TextView && child.isClickable()) {
						buttonCount++;
					}
				}
				if (buttonCount >= 2) {
					bvLog("[BetterVia] 找到水平按钮容器，按钮数量: " + buttonCount);
					return layout;
				}
			}
		}
		for (int i = 0; i < group.getChildCount(); i++) {
			View child = group.getChildAt(i);
			ViewGroup result = findHorizontalButtonContainer(child);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	private void logButtonInfo(ViewGroup container, String stage) {
		try {
			bvLog("[BetterVia] " + stage + " - 容器类型: " + container.getClass().getSimpleName() + ", 子视图数量: "
					+ container.getChildCount());
			for (int i = 0; i < container.getChildCount(); i++) {
				View child = container.getChildAt(i);
				String info = "子视图 " + i + ": " + child.getClass().getSimpleName();
				if (child instanceof TextView) {
					TextView textView = (TextView) child;
					info += " 文本: \"" + textView.getText() + "\"";
					info += " ID: " + Integer.toHexString(child.getId());
					info += " 可点击: " + child.isClickable();
					ViewGroup.LayoutParams params = child.getLayoutParams();
					if (params instanceof LinearLayout.LayoutParams) {
						LinearLayout.LayoutParams llParams = (LinearLayout.LayoutParams) params;
						info += " 权重: " + llParams.weight;
						info += " 宽度: " + llParams.width;
					}
				}
				info += " 可见: " + (child.getVisibility() == View.VISIBLE);
				bvLog("[BetterVia] " + info);
			}
		} catch (Exception e) {
			bvLog("[BetterVia] 记录按钮信息失败: " + e);
		}
	}
	private String[] extractDownloadInfoFromDialog(Dialog dialog, Context ctx) {
		String[] info = {"未知文件", "", "未知大小"};
		try {
			View decorView = dialog.getWindow().getDecorView();
			List<TextView> textViews = findAllTextViews(decorView);
			for (TextView textView : textViews) {
				String text = textView.getText().toString().trim();
				if (TextUtils.isEmpty(text) || isButtonText(text)) {
					continue;
				}
				if (text.contains(".") && text.length() > 3) {
					info[0] = text;
				} else if (text.contains("MB") || text.contains("KB") || text.contains("GB") || text.contains("字节")
						|| text.contains("B") || text.matches(".*\\d+.*")) {
					info[2] = text;
				}
			}
			info[1] = extractUrlFromDialog(dialog);
		} catch (Exception e) {
			bvLog("[BetterVia] 提取下载信息失败: " + e);
		}
		return info;
	}
	private List<TextView> findAllTextViews(View view) {
		List<TextView> textViews = new ArrayList<>();
		if (view instanceof TextView) {
			textViews.add((TextView) view);
		} else if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0; i < group.getChildCount(); i++) {
				textViews.addAll(findAllTextViews(group.getChildAt(i)));
			}
		}
		return textViews;
	}
	private boolean isButtonText(String text) {
		String[] buttonTexts = {"确定", "取消", "复制链接", "OK", "Cancel", "Copy Link", "分享", "Share"};
		for (String buttonText : buttonTexts) {
			if (text.equals(buttonText)) {
				return true;
			}
		}
		return false;
	}
	private String extractUrlFromDialog(Dialog dialog) {
		try {
			if (Context != null && Context instanceof Activity) {
				Activity activity = (Activity) Context;
				try {
					Intent intent = activity.getIntent();
					if (intent != null) {
						android.net.Uri data = intent.getData();
						if (data != null) {
							String scheme = data.getScheme();
							if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
								bvLog("[BetterVia] 通过Intent获取到URL: " + data.toString());
								return data.toString();
							}
						}
						String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
						if (extraText != null && (extraText.startsWith("http") || extraText.startsWith("ftp"))) {
							bvLog("[BetterVia] 通过Intent Extra获取到URL: " + extraText);
							return extraText;
						}
					}
				} catch (Throwable t) {
					bvLog("[BetterVia] 尝试解析Intent失败: " + t.getMessage());
				}
				try {
					Object webView = XposedHelpers.getObjectField(activity, "u");
					if (webView instanceof WebView) {
						String url = ((WebView) webView).getUrl();
						if (url != null && !url.isEmpty()) {
							bvLog("[BetterVia] 通过WebView(u)获取到URL: " + url);
							return url;
						}
					}
				} catch (Throwable e1) {
					bvLog("[BetterVia] 字段 'u' 不存在或无法访问 (非Shell Activity): " + e1.getMessage());
					try {
						Object webView = XposedHelpers.getObjectField(activity, "webView");
						if (webView instanceof WebView) {
							String url = ((WebView) webView).getUrl();
							if (url != null && !url.isEmpty()) {
								bvLog("[BetterVia] 通过WebView(webView)获取到URL: " + url);
								return url;
							}
						}
					} catch (Throwable e2) {
					}
				}
				try {
					View currentFocus = activity.getCurrentFocus();
					if (currentFocus instanceof WebView) {
						String url = ((WebView) currentFocus).getUrl();
						if (url != null && !url.isEmpty()) {
							bvLog("[BetterVia] 通过当前焦点WebView获取到URL: " + url);
							return url;
						}
					}
					ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
					WebView webView = findWebViewRecursive(decorView);
					if (webView != null) {
						String url = webView.getUrl();
						if (url != null && !url.isEmpty()) {
							bvLog("[BetterVia] 通过遍历View获取到URL: " + url);
							return url;
						}
					}
				} catch (Throwable t) {
					bvLog("[BetterVia] UI查找WebView失败: " + t.getMessage());
				}
			}
		} catch (Throwable e) {
			bvLog("[BetterVia] 提取URL发生严重错误: " + e.getMessage());
		}
		return "";
	}
	private WebView findWebViewRecursive(View view) {
		if (view instanceof WebView) {
			return (WebView) view;
		} else if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0; i < group.getChildCount(); i++) {
				WebView webView = findWebViewRecursive(group.getChildAt(i));
				if (webView != null) {
					return webView;
				}
			}
		}
		return null;
	}
	private String createShareText(String fileName, String fileSize, String fileUrl, Context ctx) {
		if (!fileUrl.isEmpty()) {
			return fileUrl;
		} else {
			return "";
		}
	}
	private void addMonetMomentItem(LinearLayout parent, final Activity act, final Context ctx) {
		LinearLayout container = new LinearLayout(ctx);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
		LinearLayout hor = new LinearLayout(ctx);
		hor.setOrientation(LinearLayout.HORIZONTAL);
		hor.setGravity(Gravity.CENTER_VERTICAL);
		TextView tv = new TextView(ctx);
		tv.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_title"));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(Color.BLACK);
		hor.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		TextView configBtn = new TextView(ctx);
		applyClickAnim(configBtn);
		configBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_config"));
		configBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		configBtn.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
		configBtn.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8));
		configBtn.setTextColor(0xFF000000);
		configBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMonetMomentDialog(ctx);
			}
		});
		hor.addView(configBtn);
		TextView hintTv = new TextView(ctx);
		hintTv.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_hint"));
		hintTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hintTv.setTextColor(0xFF666666);
		hintTv.setPadding(0, dp(ctx, 4), 0, 0);
		container.addView(hor);
		container.addView(hintTv);
		parent.addView(container);
	}
	private void showMonetMomentDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
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
				scrollRoot.setOverScrollMode(View.OVER_SCROLL_NEVER); 
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 40), dp(act, 24), dp(act, 24));
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
				title.setTextColor(0xFF6200EE);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 6); 
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_hint"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				subtitle.setTextColor(0xFF999999); 
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 36));
				root.addView(subtitle);
				if (Build.VERSION.SDK_INT < 31) { 
					LinearLayout warningCard = new LinearLayout(act);
					warningCard.setOrientation(LinearLayout.HORIZONTAL);
					warningCard.setGravity(Gravity.CENTER_VERTICAL);
					warningCard.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
					GradientDrawable cardBg = new GradientDrawable();
					cardBg.setColor(0xFFFF5252); 
					cardBg.setCornerRadius(dp(act, 12));
					warningCard.setBackground(cardBg);
					ImageView icon = new ImageView(act);
					icon.setImageResource(android.R.drawable.stat_sys_warning);
					icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
					LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(act, 20), dp(act, 20));
					iconLp.rightMargin = dp(act, 10);
					warningCard.addView(icon, iconLp);
					TextView warningText = new TextView(act);
					warningText.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_version_warning"));
					warningText.setTextColor(Color.WHITE);
					warningText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					warningText.setTypeface(null, Typeface.BOLD);
					warningCard.addView(warningText,
							new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
					LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					cardLp.setMargins(0, 0, 0, dp(act, 16)); 
					root.addView(warningCard, cardLp);
				}
				addMonetSectionTitle(root, act, LocalizedStringProvider.getInstance().get(ctx, "monet_principle_title"),
						true);
				addMonetInfoCard(root, act, LocalizedStringProvider.getInstance().get(ctx, "monet_principle_content"));
				addMonetSectionTitle(root, act, LocalizedStringProvider.getInstance().get(ctx, "monet_notes_title"),
						false);
				addMonetInfoCard(root, act, LocalizedStringProvider.getInstance().get(ctx, "monet_notes_content"));
				addMonetSectionTitle(root, act, LocalizedStringProvider.getInstance().get(ctx, "monet_config_section"),
						false);
				LinearLayout configContainer = new LinearLayout(act);
				configContainer.setOrientation(LinearLayout.VERTICAL);
				configContainer.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12)); 
				GradientDrawable configBg = new GradientDrawable();
				configBg.setColor(0xFFF8F9FA); 
				configBg.setCornerRadius(dp(act, 8)); 
				configContainer.setBackground(configBg);
				TextView baseVerLabel = new TextView(act);
				baseVerLabel.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_base_ver"));
				baseVerLabel.setTextColor(0xFF666666); 
				baseVerLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				baseVerLabel.setTypeface(null, Typeface.BOLD);
				baseVerLabel.setPadding(0, 0, 0, dp(act, 4)); 
				configContainer.addView(baseVerLabel);
				final TextView baseVerSelector = new TextView(act);
				final int savedBaseIdx = getPrefInt(ctx, KEY_MONET_BASE, 0);
				final String[] baseItems = {LocalizedStringProvider.getInstance().get(ctx, "monet_base_cn"),
						LocalizedStringProvider.getInstance().get(ctx, "monet_base_global")};
				baseVerSelector.setText(baseItems[savedBaseIdx]);
				baseVerSelector.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				baseVerSelector.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
				baseVerSelector.setBackground(getRoundBg(act, 0xFFE0E0E0, 8)); 
				baseVerSelector.setTextColor(0xFF000000);
				LinearLayout.LayoutParams selectorLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				selectorLp.bottomMargin = dp(act, 12); 
				configContainer.addView(baseVerSelector, selectorLp);
				final CheckBox useIconCb = new CheckBox(act);
				useIconCb.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_use_icon"));
				useIconCb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				useIconCb.setTextColor(Color.BLACK);
				useIconCb.setChecked(getPrefBoolean(ctx, KEY_MONET_USE_ICON, false));
				LinearLayout.LayoutParams useIconCbLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				useIconCbLp.bottomMargin = dp(act, 8);
				configContainer.addView(useIconCb, useIconCbLp);
				final CheckBox makeLiteCb = new CheckBox(act);
				makeLiteCb.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_make_lite"));
				makeLiteCb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				makeLiteCb.setTextColor(Color.BLACK);
				makeLiteCb.setChecked(getPrefBoolean(ctx, KEY_MONET_MAKE_LITE, false));
				LinearLayout.LayoutParams makeLiteCbLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				makeLiteCbLp.bottomMargin = dp(act, 12);
				configContainer.addView(makeLiteCb, makeLiteCbLp);
				final EditText pkgEdit = addMonetInput(act, configContainer,
						LocalizedStringProvider.getInstance().get(ctx, "monet_pkg_name"),
						getPrefString(ctx, KEY_MONET_PKG, savedBaseIdx == 0 ? "mark.via" : "mark.via.gp"));
				pkgEdit.setEnabled(false); 
				pkgEdit.setTextColor(0xFF999999); 
				final String monetVerName = "7.0.0";
				final String monetVerCode = "20260211";
				final EditText verNameEdit = addMonetInput(act, configContainer,
						LocalizedStringProvider.getInstance().get(ctx, "monet_ver_name"), monetVerName);
				verNameEdit.setEnabled(false);
				verNameEdit.setTextColor(0xFF999999);
				final EditText verCodeEdit = addMonetInput(act, configContainer,
						LocalizedStringProvider.getInstance().get(ctx, "monet_ver_code"), monetVerCode);
				verCodeEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
				verCodeEdit.setEnabled(false);
				verCodeEdit.setTextColor(0xFF999999);
				TextView signSchemeLabel = new TextView(act);
				signSchemeLabel.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_sign_scheme"));
				signSchemeLabel.setTextColor(0xFF666666);
				signSchemeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				signSchemeLabel.setTypeface(null, Typeface.BOLD);
				signSchemeLabel.setPadding(0, 0, 0, dp(act, 4));
				configContainer.addView(signSchemeLabel);
				final TextView signSchemeSelector = new TextView(act);
				final int savedSignSchemeIdx = getPrefInt(ctx, KEY_MONET_SIGN_SCHEME, 0);
				final String[] signSchemeItems = {LocalizedStringProvider.getInstance().get(ctx, "monet_sign_v1v2v3"),
						LocalizedStringProvider.getInstance().get(ctx, "monet_sign_v1v2"),
						LocalizedStringProvider.getInstance().get(ctx, "monet_sign_v1v3"),
						LocalizedStringProvider.getInstance().get(ctx, "monet_sign_v1"),
						LocalizedStringProvider.getInstance().get(ctx, "monet_sign_v2v3"),
						LocalizedStringProvider.getInstance().get(ctx, "monet_sign_v2"),
						LocalizedStringProvider.getInstance().get(ctx, "monet_sign_v3")};
				signSchemeSelector.setText(signSchemeItems[savedSignSchemeIdx]);
				signSchemeSelector.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				signSchemeSelector.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
				signSchemeSelector.setBackground(getRoundBg(act, 0xFFE0E0E0, 8));
				signSchemeSelector.setTextColor(0xFF000000);
				LinearLayout.LayoutParams signSchemeLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				signSchemeLp.bottomMargin = dp(act, 12);
				configContainer.addView(signSchemeSelector, signSchemeLp);
				root.addView(configContainer);
				baseVerSelector.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showMonetBasePopup(ctx, baseVerSelector, baseItems, new SourceSelectedCallback() {
							@Override
							public void onSelected(int pos) {
								baseVerSelector.setText(baseItems[pos]);
								if (pos == 0) {
									pkgEdit.setText("mark.via");
								} else {
									pkgEdit.setText("mark.via.gp");
								}
								putPrefInt(ctx, KEY_MONET_BASE, pos);
							}
						});
					}
				});
				signSchemeSelector.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showMonetBasePopup(ctx, signSchemeSelector, signSchemeItems, new SourceSelectedCallback() {
							@Override
							public void onSelected(int pos) {
								signSchemeSelector.setText(signSchemeItems[pos]);
								putPrefInt(ctx, KEY_MONET_SIGN_SCHEME, pos);
							}
						});
					}
				});
				TextView outputLocationLabel = new TextView(act);
				outputLocationLabel.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_output_location"));
				outputLocationLabel.setTextColor(0xFF666666);
				outputLocationLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				outputLocationLabel.setTypeface(null, Typeface.BOLD);
				outputLocationLabel.setPadding(0, 0, 0, dp(act, 4));
				configContainer.addView(outputLocationLabel);
				final TextView outputLocationSelector = new TextView(act);
				final int savedOutputLocationIdx = getPrefInt(ctx, KEY_MONET_OUTPUT_LOCATION, 0);
				final String[] outputLocationItems = {
						LocalizedStringProvider.getInstance().get(ctx, "monet_output_external"),
						LocalizedStringProvider.getInstance().get(ctx, "monet_output_internal")};
				outputLocationSelector.setText(outputLocationItems[savedOutputLocationIdx]);
				outputLocationSelector.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				outputLocationSelector.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
				outputLocationSelector.setBackground(getRoundBg(act, 0xFFE0E0E0, 8));
				outputLocationSelector.setTextColor(0xFF000000);
				LinearLayout.LayoutParams outputLocationLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				configContainer.addView(outputLocationSelector, outputLocationLp);
				final TextView outputLocationDesc = new TextView(act);
				outputLocationDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
				outputLocationDesc.setTextColor(0xFF999999);
				outputLocationDesc.setPadding(0, 0, 0, dp(act, 8));
				configContainer.addView(outputLocationDesc);
				final Runnable updateOutputDesc = new Runnable() {
					@Override
					public void run() {
						int idx = getPrefInt(ctx, KEY_MONET_OUTPUT_LOCATION, 0);
						String desc = idx == 0
								? LocalizedStringProvider.getInstance().get(ctx, "monet_output_external_desc")
								: LocalizedStringProvider.getInstance().get(ctx, "monet_output_internal_desc");
						outputLocationDesc.setText(desc);
					}
				};
				updateOutputDesc.run();
				outputLocationSelector.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showMonetBasePopup(ctx, outputLocationSelector, outputLocationItems,
								new SourceSelectedCallback() {
									@Override
									public void onSelected(int pos) {
										outputLocationSelector.setText(outputLocationItems[pos]);
										putPrefInt(ctx, KEY_MONET_OUTPUT_LOCATION, pos);
										updateOutputDesc.run();
									}
								});
					}
				});
				addMonetSectionTitle(root, act, LocalizedStringProvider.getInstance().get(ctx, "monet_agreement_title"),
						false);
				TextView agreementText = new TextView(act);
				agreementText.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_agreement_content"));
				agreementText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13); 
				agreementText.setTextColor(0xFF666666);
				agreementText.setLineSpacing(dp(act, 3), 1.1f);
				root.addView(agreementText);
				final CheckBox agreeCb = new CheckBox(act);
				agreeCb.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_i_agree"));
				agreeCb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				agreeCb.setTextColor(Color.BLACK);
				LinearLayout.LayoutParams cbLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				cbLp.topMargin = dp(act, 8);
				root.addView(agreeCb, cbLp);
				LinearLayout btnRow = new LinearLayout(act);
				btnRow.setOrientation(LinearLayout.HORIZONTAL);
				btnRow.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams btnRowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				btnRowLp.topMargin = dp(act, 16);
				root.addView(btnRow, btnRowLp);
				Button cancelBtn = new Button(act);
				applyClickAnim(cancelBtn);
				cancelBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "dialog_cancel"));
				cancelBtn.setTextColor(0xFF666666);
				cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				cancelBtn.setTypeface(null, Typeface.BOLD);
				GradientDrawable cancelBg = new GradientDrawable();
				cancelBg.setColor(0xFFF0F0F0);
				cancelBg.setCornerRadius(dp(act, 12));
				cancelBtn.setBackground(cancelBg);
				cancelBtn.setPadding(0, dp(act, 14), 0, dp(act, 14));
				final Button continueBtn = new Button(act);
				applyClickAnim(continueBtn);
				continueBtn.setText(LocalizedStringProvider.getInstance().get(ctx, "monet_continue"));
				continueBtn.setTextColor(Color.WHITE);
				continueBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				continueBtn.setTypeface(null, Typeface.BOLD);
				GradientDrawable continueBg = new GradientDrawable();
				continueBg.setColor(0xFF9E9E9E);
				continueBg.setCornerRadius(dp(act, 12));
				continueBtn.setBackground(continueBg);
				continueBtn.setPadding(0, dp(act, 14), 0, dp(act, 14));
				continueBtn.setEnabled(false);
				LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
						1);
				btnLp.rightMargin = dp(act, 8);
				btnRow.addView(cancelBtn, btnLp);
				btnLp.leftMargin = dp(act, 8);
				btnRow.addView(continueBtn, btnLp);
				scrollRoot.addView(root);
				dialogContainer.addView(scrollRoot);
				dialog.setContentView(dialogContainer);
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
					if (metrics.heightPixels < dp(act, 600)) {
						layoutParams.height = (int) (metrics.heightPixels * 0.85);
					}
					layoutParams.gravity = Gravity.CENTER;
					window.setAttributes(layoutParams);
				}
				agreeCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						continueBtn.setEnabled(isChecked);
						GradientDrawable bg = new GradientDrawable();
						bg.setCornerRadius(dp(act, 12));
						if (isChecked) {
							bg.setColor(0xFF6200EE); 
						} else {
							bg.setColor(0xFF9E9E9E); 
						}
						continueBtn.setBackground(bg);
					}
				});
				cancelBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				continueBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						putPrefString(ctx, KEY_MONET_PKG, pkgEdit.getText().toString());
						putPrefString(ctx, KEY_MONET_VER_NAME, verNameEdit.getText().toString());
						putPrefString(ctx, KEY_MONET_VER_CODE, verCodeEdit.getText().toString());
						putPrefBoolean(ctx, KEY_MONET_USE_ICON, useIconCb.isChecked());
						putPrefBoolean(ctx, KEY_MONET_MAKE_LITE, makeLiteCb.isChecked());
						int signSchemeIdx = 0;
						for (int i = 0; i < signSchemeItems.length; i++) {
							if (signSchemeSelector.getText().toString().equals(signSchemeItems[i])) {
								signSchemeIdx = i;
								break;
							}
						}
						putPrefInt(ctx, KEY_MONET_SIGN_SCHEME, signSchemeIdx);
						dialog.dismiss();
						startMonetMomentProcess(ctx);
					}
				});
				dialog.show();
				animateDialogEntrance(root, act);
			}
		});
	}
	private void addMonetSectionTitle(LinearLayout parent, Context ctx, String text, boolean isFirst) {
		TextView title = new TextView(ctx);
		title.setText(text);
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		title.setTextColor(Color.BLACK);
		title.setTypeface(null, Typeface.BOLD);
		title.setPadding(0, isFirst ? 0 : dp(ctx, 24), 0, dp(ctx, 12));
		parent.addView(title);
	}
	private void addMonetInfoCard(LinearLayout parent, Context ctx, String text) {
		LinearLayout card = new LinearLayout(ctx);
		card.setOrientation(LinearLayout.VERTICAL);
		card.setPadding(dp(ctx, 12), dp(ctx, 12), dp(ctx, 12), dp(ctx, 12));
		GradientDrawable bg = new GradientDrawable();
		bg.setColor(0xFFF8F9FA); 
		bg.setCornerRadius(dp(ctx, 8)); 
		card.setBackground(bg);
		TextView content = new TextView(ctx);
		content.setText(text);
		content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
		content.setTextColor(0xFF444444); 
		content.setLineSpacing(dp(ctx, 4), 1.2f); 
		card.addView(content);
		parent.addView(card);
	}
	private EditText addMonetInput(Context ctx, LinearLayout parent, String label, String value) {
		TextView labelTv = new TextView(ctx);
		labelTv.setText(label);
		labelTv.setTextColor(0xFF666666); 
		labelTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
		labelTv.setTypeface(null, Typeface.BOLD);
		labelTv.setPadding(0, 0, 0, dp(ctx, 4));
		parent.addView(labelTv);
		EditText edit = new EditText(ctx);
		edit.setText(value);
		edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
		edit.setTextColor(Color.BLACK);
		edit.setBackground(getRoundBg(ctx, 0xFFE0E0E0, 8)); 
		edit.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.bottomMargin = dp(ctx, 12); 
		parent.addView(edit, lp);
		return edit;
	}
	private void showMonetBasePopup(final Context ctx, View anchor, String[] items,
			final SourceSelectedCallback callback) {
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
				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				return view;
			}
		};
		list.setAdapter(adapter);
		int popupWidth = Math.max(anchor.getWidth(), dp(ctx, 150));
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
	private static boolean getPrefBoolean(Context ctx, String key, boolean def) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			return (boolean) XposedHelpers.callMethod(sp, "getBoolean", key, def);
		} catch (Exception e) {
			return def;
		}
	}
	private static void putPrefBoolean(Context ctx, String key, boolean value) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			Object ed = XposedHelpers.callMethod(sp, "edit");
			XposedHelpers.callMethod(ed, "putBoolean", key, value);
			XposedHelpers.callMethod(ed, "apply");
		} catch (Exception e) {
			bvLog("[BetterVia] 写入布尔值时失败: " + e);
		}
	}
	private void saveLanguageSetting(Context ctx, String lang) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			Object ed = XposedHelpers.callMethod(sp, "edit");
			XposedHelpers.callMethod(ed, "putString", "preferred_language", lang);
			XposedHelpers.callMethod(ed, "apply");
		} catch (Exception e) {
			bvLog("[BetterVia] 保存语言失败: " + e);
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
	private Activity getActivityFrom(Context ctx) {
		try {
			if (ctx instanceof Activity) {
				return (Activity) ctx;
			}
			if (currentActivity != null) {
				return currentActivity;
			}
			if (Context != null && Context instanceof Activity) {
				return (Activity) Context;
			}
		} catch (Throwable ignored) {
		}
		return null;
	}
	private void putPrefString(Context ctx, String key, String value) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			Object ed = XposedHelpers.callMethod(sp, "edit");
			XposedHelpers.callMethod(ed, "putString", key, value);
			XposedHelpers.callMethod(ed, "apply");
		} catch (Exception e) {
			bvLog("[BetterVia] 写入字符串值时失败: " + e);
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
	private Class<?> findClassWithFallback(String simpleClassName, Context ctx, ClassLoader cl) {
		String packageName = ctx.getPackageName();
		String currentClassName = packageName + "." + simpleClassName;
		try {
			Class<?> clazz = XposedHelpers.findClass(currentClassName, cl);
			bvLog("[BetterVia] 找到类: " + currentClassName);
			return clazz;
		} catch (Throwable e) {
			bvLog("[BetterVia] 未找到类: " + currentClassName + "，尝试回退到mark.via");
		}
		String fallbackClassName = "mark.via." + simpleClassName;
		try {
			Class<?> clazz = XposedHelpers.findClass(fallbackClassName, cl);
			bvLog("[BetterVia] 使用回退类: " + fallbackClassName);
			return clazz;
		} catch (Throwable e) {
			bvLog("[BetterVia] 未找到回退类: " + fallbackClassName);
			return null;
		}
	}
	public static String getSavedLanguageStatic(Context ctx) {
		try {
			Object sp = XposedHelpers.callMethod(ctx, "getSharedPreferences", "BetterVia", Context.MODE_PRIVATE);
			return (String) XposedHelpers.callMethod(sp, "getString", "preferred_language", "auto");
		} catch (Exception e) {
			return "auto";
		}
	}
	private void copyToClipboard(Context ctx, String text) {
		try {
			ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Via Command", text);
			clipboard.setPrimaryClip(clip);
		} catch (Exception e) {
			bvLog("[BetterVia] 复制到剪贴板失败: " + e);
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
			bvLog("[BetterVia] Via语言环境已切换: " + newLoc.toString());
		} catch (Exception e) {
			bvLog("[BetterVia] 切换Locale失败: " + e);
		}
	}
	private void refreshModuleButtonText(Context ctx) {
		if (moduleButtonRef == null)
			return;
		try {
			String newText = LocalizedStringProvider.getInstance().get(ctx, "module_settings");
			XposedHelpers.setObjectField(moduleButtonRef, "a", newText);
			bvLog("[BetterVia] 模块按钮文字已刷新: " + newText);
		} catch (Exception e) {
			bvLog("[BetterVia] 刷新按钮文字失败: " + e);
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
		Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, key), Toast.LENGTH_SHORT).show();
	}
	private void jiguroMessage(String msg) {
		try {
			if (Context == null) {
				bvLog("[BetterVia] Context为null，无法显示Toast: " + msg);
				return;
			}
			Context appContext = Context.getApplicationContext();
			if (appContext == null) {
				bvLog("[BetterVia] Application Context为null，无法显示Toast: " + msg);
				return;
			}
			showToastSafely(appContext, msg);
		} catch (Exception e) {
			bvLog("[BetterVia] Toast显示异常: " + e);
		}
	}
	private void showToastSafely(final Context context, final String msg) {
		try {
			Handler mainHandler = new Handler(Looper.getMainLooper());
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					try {
						Context appContext = context.getApplicationContext();
						Toast toast = Toast.makeText(appContext, msg, Toast.LENGTH_SHORT);
						toast.show();
					} catch (Exception innerException) {
						bvLog("[BetterVia] 主线程Toast异常: " + innerException);
						bvLog("[BetterVia] Toast消息: " + msg);
					}
				}
			});
		} catch (Exception outerException) {
			bvLog("[BetterVia] 安全显示Toast失败: " + outerException);
		}
	}
	private static int dp(Context ctx, int dp) {
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
			bvLog("[BetterVia] 写入Int值失败: " + e);
		}
	}
	private void animateDialogEntrance(final ViewGroup root, final Activity act) {
		if (root == null || act == null)
			return;
		final int childCount = root.getChildCount();
		final int baseDelay = 60; 
		int delay = 0;
		if (childCount > 0) {
			final View maybeTitle = root.getChildAt(0);
			maybeTitle.setAlpha(0f);
			maybeTitle.setTranslationY(dp(act, 6));
			maybeTitle.setScaleX(0.98f);
			maybeTitle.setScaleY(0.98f);
			maybeTitle.animate().alpha(1f).translationY(0f).scaleX(1f).scaleY(1f).setStartDelay(delay).setDuration(320)
					.setInterpolator(new OvershootInterpolator(1.0f)).start();
			delay += baseDelay;
		}
		for (int i = 1; i < childCount; i++) {
			final View v = root.getChildAt(i);
			if (v == null)
				continue;
			v.setAlpha(0f);
			v.setTranslationY(dp(act, 10));
			v.animate().alpha(1f).translationY(0f).setStartDelay(delay).setDuration(220)
					.setInterpolator(new DecelerateInterpolator()).start();
			delay += baseDelay;
		}
	}
	private void applyClickAnim(final View v) {
		if (v == null)
			return;
		v.animate().cancel();
		v.setClickable(true);
		v.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN :
						view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80)
								.setInterpolator(new DecelerateInterpolator()).start();
						break;
					case MotionEvent.ACTION_UP :
					case MotionEvent.ACTION_CANCEL :
						view.animate().scaleX(1f).scaleY(1f).setDuration(120)
								.setInterpolator(new OvershootInterpolator(1.1f)).start();
						break;
				}
				return false; 
			}
		});
	}
	private void checkUpdateOnStart(final Context ctx) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
					checkUpdate(ctx);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
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
					String localVersion = MODULE_VERSION_NAME;
					if (!remoteVersion.equals(localVersion)) {
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
				}
			}
		}).start();
	}
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
	private void showUpdateDialog(final Context ctx, final String version, final String updateLog,
			final String apkUrl) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 24), dp(act, 24), dp(act, 24));
				GradientDrawable bg = new GradientDrawable();
				bg.setColor(Color.WHITE);
				bg.setCornerRadius(dp(act, 24));
				root.setBackground(bg);
				TextView title = new TextView(act);
				title.setText("BetterVia");
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
				title.setTextColor(0xFF6200EE);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.START);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView versionTitle = new TextView(act);
				versionTitle.setText(
						String.format(LocalizedStringProvider.getInstance().get(ctx, "new_version_found"), version));
				versionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				versionTitle.setTextColor(Color.BLACK);
				versionTitle.setTypeface(null, Typeface.BOLD);
				versionTitle.setGravity(Gravity.START);
				LinearLayout.LayoutParams versionLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				versionLp.bottomMargin = dp(act, 16);
				root.addView(versionTitle, versionLp);
				TextView logTitle = new TextView(act);
				logTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "update_log_title"));
				logTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				logTitle.setTextColor(0xFF666666);
				logTitle.setTypeface(null, Typeface.BOLD);
				logTitle.setGravity(Gravity.START);
				LinearLayout.LayoutParams logTitleLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				logTitleLp.bottomMargin = dp(act, 8);
				root.addView(logTitle, logTitleLp);
				LinearLayout logContainer = new LinearLayout(act);
				logContainer.setOrientation(LinearLayout.VERTICAL);
				logContainer.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				GradientDrawable logBg = new GradientDrawable();
				logBg.setColor(0xFFF8F9FA);
				logBg.setCornerRadius(dp(act, 12));
				logContainer.setBackground(logBg);
				LinearLayout.LayoutParams containerLp = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				containerLp.bottomMargin = dp(act, 20);
				root.addView(logContainer, containerLp);
				TextView logContent = new TextView(act);
				logContent.setText(updateLog);
				logContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				logContent.setTextColor(0xFF444444);
				logContent.setLineSpacing(dp(act, 4), 1.2f);
				logContent.setGravity(Gravity.START);
				logContainer.addView(logContent);
				LinearLayout buttonLayout = new LinearLayout(act);
				buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
				buttonLayout.setGravity(Gravity.CENTER);
				Button laterButton = new Button(act);
				applyClickAnim(laterButton);
				laterButton.setText(LocalizedStringProvider.getInstance().get(ctx, "later"));
				laterButton.setTextColor(0xFF6200EE);
				laterButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				laterButton.setTypeface(null, Typeface.BOLD);
				GradientDrawable laterBg = new GradientDrawable();
				laterBg.setColor(0xFFEEEEEE);
				laterBg.setCornerRadius(dp(act, 12));
				laterButton.setBackground(laterBg);
				laterButton.setPadding(dp(act, 24), dp(act, 12), dp(act, 24), dp(act, 12));
				LinearLayout.LayoutParams laterLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				laterLp.rightMargin = dp(act, 8);
				buttonLayout.addView(laterButton, laterLp);
				Button downloadButton = new Button(act);
				applyClickAnim(downloadButton);
				downloadButton.setText(LocalizedStringProvider.getInstance().get(ctx, "download_now"));
				downloadButton.setTextColor(Color.WHITE);
				downloadButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				downloadButton.setTypeface(null, Typeface.BOLD);
				GradientDrawable downloadBg = new GradientDrawable();
				downloadBg.setColor(0xFF6200EE);
				downloadBg.setCornerRadius(dp(act, 12));
				downloadButton.setBackground(downloadBg);
				downloadButton.setPadding(dp(act, 24), dp(act, 12), dp(act, 24), dp(act, 12));
				LinearLayout.LayoutParams downloadLp = new LinearLayout.LayoutParams(0,
						ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
				downloadLp.leftMargin = dp(act, 8);
				buttonLayout.addView(downloadButton, downloadLp);
				root.addView(buttonLayout);
				scrollRoot.addView(root);
				final AlertDialog dialog = new AlertDialog.Builder(act).setView(scrollRoot).setCancelable(false)
						.create();
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
							Toast.makeText(act,
									LocalizedStringProvider.getInstance().get(ctx, "cannot_open_download_link"),
									Toast.LENGTH_SHORT).show();
						}
					}
				});
				dialog.show();
			}
		});
	}
	private void showAboutDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
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
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));
				TextView title = new TextView(act);
				title.setText("BetterVia");
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
				title.setTextColor(0xFF6200EE);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				ObjectAnimator colorAnim = ObjectAnimator.ofInt(title, "textColor", 0xFF6200EE, 0xFFFF6B35, 0xFF4CD964,
						0xFF5AC8FA, 0xFF6200EE);
				colorAnim.setDuration(2000);
				colorAnim.setEvaluator(new ArgbEvaluator());
				colorAnim.setRepeatCount(ObjectAnimator.INFINITE);
				colorAnim.start();
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "about_subtitle") + " 🎉");
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);
				TextView moduleTitle = new TextView(act);
				moduleTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "about_module_title"));
				moduleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				moduleTitle.setTextColor(Color.BLACK);
				moduleTitle.setTypeface(null, Typeface.BOLD);
				moduleTitle.setPadding(0, 0, 0, dp(act, 12));
				root.addView(moduleTitle);
				addAboutItem(root, act, LocalizedStringProvider.getInstance().get(ctx, "about_version"),
						MODULE_VERSION_NAME + " 🎊");
				addAboutItem(root, act, LocalizedStringProvider.getInstance().get(ctx, "about_author"), "JiGuro 🧧");
				addClickableAboutItem(root, act, LocalizedStringProvider.getInstance().get(ctx, "about_github"),
						"https:
							@Override
							public void onClick(View v) {
								openUrl(act, "https:
								Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "start_url_message"),
										Toast.LENGTH_SHORT).show();
							}
						});
				addClickableAboutItem(root, act, LocalizedStringProvider.getInstance().get(ctx, "about_gitee"),
						"https:
							@Override
							public void onClick(View v) {
								openUrl(act, "https:
								Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "start_url_message"),
										Toast.LENGTH_SHORT).show();
							}
						});
				addClickableAboutItem(root, act, LocalizedStringProvider.getInstance().get(ctx, "about_xposed"),
						LocalizedStringProvider.getInstance().get(ctx, "about_xposed_repo"),
						new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								openUrl(act, "https:
								Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "start_url_message"),
										Toast.LENGTH_SHORT).show();
							}
						});
				TextView newYearTitle = new TextView(act);
				newYearTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "new_year_title"));
				newYearTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				newYearTitle.setTextColor(0xFFFF6B35);
				newYearTitle.setTypeface(null, Typeface.BOLD);
				newYearTitle.setGravity(Gravity.CENTER);
				newYearTitle.setPadding(0, dp(act, 24), 0, dp(act, 12));
				ObjectAnimator scaleX = ObjectAnimator.ofFloat(newYearTitle, "scaleX", 0.8f, 1.2f, 1.0f);
				ObjectAnimator scaleY = ObjectAnimator.ofFloat(newYearTitle, "scaleY", 0.8f, 1.2f, 1.0f);
				AnimatorSet scaleAnim = new AnimatorSet();
				scaleAnim.playTogether(scaleX, scaleY);
				scaleAnim.setDuration(1500);
				scaleAnim.setInterpolator(new OvershootInterpolator());
				scaleAnim.start();
				root.addView(newYearTitle);
				TextView newYearWish = new TextView(act);
				newYearWish.setText(LocalizedStringProvider.getInstance().get(ctx, "new_year_wish_text"));
				newYearWish.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				newYearWish.setTextColor(0xFF666666);
				newYearWish.setGravity(Gravity.CENTER);
				newYearWish.setLineSpacing(dp(act, 4), 1.2f);
				newYearWish.setPadding(0, 0, 0, dp(act, 16));
				root.addView(newYearWish);
				TextView updateTitle = new TextView(act);
				updateTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "about_update_title"));
				updateTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				updateTitle.setTextColor(Color.BLACK);
				updateTitle.setTypeface(null, Typeface.BOLD);
				updateTitle.setPadding(0, dp(act, 24), 0, dp(act, 12));
				root.addView(updateTitle);
				LinearLayout updateContainer = new LinearLayout(act);
				updateContainer.setOrientation(LinearLayout.VERTICAL);
				updateContainer.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));
				GradientDrawable updateBg = new GradientDrawable();
				updateBg.setColor(0xFFF8F9FA);
				updateBg.setCornerRadius(dp(act, 8));
				updateContainer.setBackground(updateBg);
				String[] updateLogs = {LocalizedStringProvider.getInstance().get(ctx, "about_update_log0"),
						LocalizedStringProvider.getInstance().get(ctx, "about_update_log1"),
						LocalizedStringProvider.getInstance().get(ctx, "about_update_log2"),
						LocalizedStringProvider.getInstance().get(ctx, "about_update_log3"),
						LocalizedStringProvider.getInstance().get(ctx, "about_update_log4"),
						LocalizedStringProvider.getInstance().get(ctx, "about_update_log5")};
				for (String log : updateLogs) {
					TextView logItem = new TextView(act);
					logItem.setText("🎯 " + log);
					logItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					logItem.setTextColor(0xFF444444);
					logItem.setPadding(0, dp(act, 4), 0, dp(act, 4));
					updateContainer.addView(logItem);
				}
				root.addView(updateContainer);
				TextView thanksTitle = new TextView(act);
				thanksTitle.setText(LocalizedStringProvider.getInstance().get(ctx, "about_thanks_title"));
				thanksTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				thanksTitle.setTextColor(Color.BLACK);
				thanksTitle.setTypeface(null, Typeface.BOLD);
				thanksTitle.setPadding(0, dp(act, 24), 0, dp(act, 12));
				root.addView(thanksTitle);
				addAboutItem(root, act, "", LocalizedStringProvider.getInstance().get(ctx, "about_thanks_content"));
				addAboutItem(root, act, "", LocalizedStringProvider.getInstance().get(ctx, "about_licence"));
				addAboutItem(root, act, "", LocalizedStringProvider.getInstance().get(ctx, "about_licence_apksig"));
				addClickableAboutItem(root, act, "", "Coolapk @半烟半雨溪桥畔", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						openUrl(act, "https:
						Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "start_url_message"),
								Toast.LENGTH_SHORT).show();
					}
				});
				addClickableAboutItem(root, act, "", "Blog @sgfox", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						openUrl(act, "https:
						Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "start_url_message"),
								Toast.LENGTH_SHORT).show();
					}
				});
				addAboutItem(root, act, "", LocalizedStringProvider.getInstance().get(ctx, "about_thanks_others"));
				Button ok = new Button(act);
				applyClickAnim(ok);
				ok.setText("🎊 " + LocalizedStringProvider.getInstance().get(ctx, "dialog_ok") + " 🎊");
				ok.setTextColor(Color.WHITE);
				ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				ok.setTypeface(null, Typeface.BOLD);
				GradientDrawable btnBg = new GradientDrawable();
				btnBg.setColor(0xFFFF6B35);
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
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx, "new_year_toast"),
								Toast.LENGTH_LONG).show();
					}
				}, 1000);
			}
		});
	}
	private void showShisuiDialog(final Context ctx) {
		final Activity act = getActivityFrom(ctx);
		if (act == null)
			return;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				final Dialog dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);
				LinearLayout dialogContainer = new LinearLayout(act);
				dialogContainer.setOrientation(LinearLayout.VERTICAL);
				GradientDrawable containerBg = new GradientDrawable();
				containerBg.setColor(Color.WHITE);
				containerBg.setCornerRadius(dp(act, 24));
				dialogContainer.setBackground(containerBg);
				ScrollView scrollRoot = new ScrollView(act);
				scrollRoot.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
				final LinearLayout root = new LinearLayout(act);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setPadding(dp(act, 24), dp(act, 28), dp(act, 24), dp(act, 24));
				TextView title = new TextView(act);
				title.setText(LocalizedStringProvider.getInstance().get(ctx, "shisui_dialog_title"));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
				title.setTextColor(0xFF6200EE);
				title.setTypeface(null, Typeface.BOLD);
				title.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				titleLp.bottomMargin = dp(act, 8);
				root.addView(title, titleLp);
				TextView subtitle = new TextView(act);
				subtitle.setText(LocalizedStringProvider.getInstance().get(ctx, "shisui_dialog_subtitle"));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				subtitle.setTextColor(0xFF666666);
				subtitle.setGravity(Gravity.CENTER);
				subtitle.setPadding(0, 0, 0, dp(act, 24));
				root.addView(subtitle);
				final TextView loadingText = new TextView(act);
				loadingText.setText(LocalizedStringProvider.getInstance().get(ctx, "shisui_loading"));
				loadingText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				loadingText.setTextColor(0xFF666666);
				loadingText.setGravity(Gravity.CENTER);
				loadingText.setPadding(0, dp(act, 24), 0, dp(act, 24));
				root.addView(loadingText);
				final LinearLayout contentContainer = new LinearLayout(act);
				contentContainer.setOrientation(LinearLayout.VERTICAL);
				root.addView(contentContainer);
				String savedSource = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE);
				final String shisuiUrl = savedSource.equals(NETWORK_SOURCE_GITEE)
						? GITEE_SHISUI_JSON_URL
						: GITHUB_SHISUI_JSON_URL;
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							URL url = new URL(shisuiUrl);
							HttpURLConnection conn = (HttpURLConnection) url.openConnection();
							conn.setRequestMethod("GET");
							conn.setConnectTimeout(10000);
							conn.setReadTimeout(15000);
							conn.connect();
							if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
								InputStream is = conn.getInputStream();
								BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
								StringBuilder sb = new StringBuilder();
								String line;
								while ((line = reader.readLine()) != null) {
									sb.append(line);
								}
								reader.close();
								is.close();
								conn.disconnect();
								final String jsonData = sb.toString();
								act.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											root.removeView(loadingText);
											JSONArray jsonArray = new JSONArray(jsonData);
											String lastYear = "";
											for (int i = 0; i < jsonArray.length(); i++) {
												JSONObject item = jsonArray.getJSONObject(i);
												final String year = item.getString("year");
												final String version = item.getString("version");
												final String content = item.getString("content");
												if (!year.equals(lastYear)) {
													TextView yearTitle = new TextView(act);
													yearTitle.setText(year);
													yearTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
													yearTitle.setTextColor(Color.BLACK);
													yearTitle.setTypeface(null, Typeface.BOLD);
													yearTitle.setPadding(0, dp(act, 16), 0, dp(act, 8));
													contentContainer.addView(yearTitle);
													lastYear = year;
												}
												LinearLayout versionContainer = new LinearLayout(act);
												versionContainer.setOrientation(LinearLayout.HORIZONTAL);
												versionContainer.setGravity(Gravity.CENTER_VERTICAL);
												TextView versionTitle = new TextView(act);
												versionTitle.setText(version);
												versionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
												versionTitle.setTextColor(0xFF6200EE);
												versionTitle.setTypeface(null, Typeface.BOLD);
												versionTitle.setPadding(0, 0, dp(act, 8), dp(act, 4));
												versionContainer.addView(versionTitle, new LinearLayout.LayoutParams(0,
														ViewGroup.LayoutParams.WRAP_CONTENT, 1));
												TextView copyBtn = new TextView(act);
												applyClickAnim(copyBtn);
												copyBtn.setText(
														LocalizedStringProvider.getInstance().get(ctx, "shisui_copy"));
												copyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
												copyBtn.setTextColor(Color.BLACK);
												copyBtn.setPadding(dp(act, 8), dp(act, 4), dp(act, 8), dp(act, 4));
												copyBtn.setBackground(getRoundBg(act, 0xFFE0E0E0, 12));
												copyBtn.setOnClickListener(new View.OnClickListener() {
													@Override
													public void onClick(View v) {
														StringBuilder copyContent = new StringBuilder();
														copyContent.append(year).append(" - ").append(version)
																.append("\n");
														copyContent.append(content.replace("\\r\\n", "\n"));
														ClipboardManager clipboard = (ClipboardManager) act
																.getSystemService(Context.CLIPBOARD_SERVICE);
														android.content.ClipData clip = android.content.ClipData
																.newPlainText("Via Shisui", copyContent.toString());
														clipboard.setPrimaryClip(clip);
														Toast.makeText(act, LocalizedStringProvider.getInstance()
																.get(ctx, "shisui_copied"), Toast.LENGTH_SHORT).show();
													}
												});
												versionContainer.addView(copyBtn);
												contentContainer.addView(versionContainer);
												String[] lines = content.split("\\\\r\\\\n");
												for (String line : lines) {
													TextView contentText = new TextView(act);
													contentText.setText(line);
													contentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
													contentText.setTextColor(0xFF333333);
													contentText.setPadding(dp(act, 8), dp(act, 4), dp(act, 8),
															dp(act, 4));
													contentContainer.addView(contentText);
												}
												if (i < jsonArray.length() - 1) {
													View divider = new View(act);
													divider.setLayoutParams(new LinearLayout.LayoutParams(
															ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 1)));
													divider.setBackgroundColor(0xFFDDDDDD);
													LinearLayout.LayoutParams dividerLp = new LinearLayout.LayoutParams(
															ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 1));
													dividerLp.setMargins(0, dp(act, 12), 0, dp(act, 12));
													divider.setLayoutParams(dividerLp);
													contentContainer.addView(divider);
												}
											}
											View finalDivider = new View(act);
											finalDivider.setLayoutParams(new LinearLayout.LayoutParams(
													ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 1)));
											finalDivider.setBackgroundColor(0xFFDDDDDD);
											LinearLayout.LayoutParams finalDividerLp = new LinearLayout.LayoutParams(
													ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 1));
											finalDividerLp.setMargins(0, dp(act, 12), 0, dp(act, 8));
											finalDivider.setLayoutParams(finalDividerLp);
											contentContainer.addView(finalDivider);
											TextView continuedText = new TextView(act);
											continuedText.setText(
													LocalizedStringProvider.getInstance().get(ctx, "to_be_continued"));
											continuedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
											continuedText.setTextColor(0xFF666666);
											continuedText.setTypeface(null, Typeface.ITALIC);
											continuedText.setGravity(Gravity.CENTER);
											continuedText.setPadding(0, 0, 0, dp(act, 16));
											contentContainer.addView(continuedText);
											LinearLayout bottomContainer = new LinearLayout(act);
											bottomContainer.setOrientation(LinearLayout.HORIZONTAL);
											bottomContainer.setGravity(Gravity.CENTER);
											TextView bottomText = new TextView(act);
											bottomText.setText(LocalizedStringProvider.getInstance().get(ctx,
													"shisui_source_credit") + " ");
											bottomText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
											bottomText.setTextColor(0xFF666666);
											bottomContainer.addView(bottomText);
											SpannableString ss = new SpannableString("sgfox");
											ClickableSpan clickableSpan = new ClickableSpan() {
												@Override
												public void onClick(View widget) {
													openUrl(act, "https:
													Toast.makeText(act, LocalizedStringProvider.getInstance().get(ctx,
															"url_opened"), Toast.LENGTH_SHORT).show();
												}
											};
											ss.setSpan(clickableSpan, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
											ss.setSpan(new ForegroundColorSpan(0xFF4285F4), 0, ss.length(),
													Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
											TextView sgfoxText = new TextView(act);
											sgfoxText.setText(ss);
											sgfoxText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
											sgfoxText.setMovementMethod(LinkMovementMethod.getInstance());
											bottomContainer.addView(sgfoxText);
											LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(
													ViewGroup.LayoutParams.WRAP_CONTENT,
													ViewGroup.LayoutParams.WRAP_CONTENT);
											bottomLp.gravity = Gravity.CENTER;
											bottomLp.topMargin = dp(act, 8);
											contentContainer.addView(bottomContainer, bottomLp);
										} catch (JSONException e) {
											loadingText.setText(LocalizedStringProvider.getInstance().get(ctx,
													"shisui_load_failed"));
										}
									}
								});
							} else {
								act.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										loadingText.setText(
												LocalizedStringProvider.getInstance().get(ctx, "shisui_load_failed"));
									}
								});
							}
						} catch (Exception e) {
							act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									loadingText.setText(
											LocalizedStringProvider.getInstance().get(ctx, "shisui_load_failed"));
								}
							});
						}
					}
				}).start();
				scrollRoot.addView(root);
				dialogContainer.addView(scrollRoot);
				Window win = dialog.getWindow();
				if (win != null) {
					win.setBackgroundDrawableResource(android.R.color.transparent);
					win.setGravity(Gravity.CENTER);
					win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				}
				dialog.setContentView(dialogContainer);
				dialog.show();
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				lp.copyFrom(dialog.getWindow().getAttributes());
				lp.width = (int) (act.getResources().getDisplayMetrics().widthPixels * 0.9);
				dialog.getWindow().setAttributes(lp);
			}
		});
	}
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
	private void openUrl(Context ctx, String url) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			ctx.startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "cannot_open_url"), Toast.LENGTH_SHORT)
					.show();
		}
	}
	private static void checkViaVersion(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
			String currentVersion = pi.versionName;
			String prefKey = KEY_VERSION_CHECK_DISABLED + "_" + currentVersion;
			boolean versionCheckDisabled = getPrefBoolean(ctx, prefKey, false);
			if (!SUPPORTED_VIA_VERSION.equals(currentVersion) && !versionCheckDisabled) {
				showVersionErrorDialog(ctx, currentVersion);
			}
		} catch (Exception e) {
			bvLog("[BetterVia] 版本检测失败: " + e.getMessage());
		}
	}
	private static void showVersionErrorDialog(final Context ctx, final String currentVersion) {
		if (Context == null || !(Context instanceof Activity))
			return;
		final Activity act = (Activity) Context;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "hook_success_message"),
						Toast.LENGTH_SHORT).show();
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle(LocalizedStringProvider.getInstance().get(ctx, "version_error_title"));
				String message = String.format(LocalizedStringProvider.getInstance().get(ctx, "version_error_message"),
						currentVersion, SUPPORTED_VIA_VERSION);
				builder.setMessage(message);
				LinearLayout layout = new LinearLayout(act);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.setPadding(dp(act, 20), dp(act, 10), dp(act, 20), dp(act, 10));
				final CheckBox checkBox = new CheckBox(act);
				checkBox.setText(LocalizedStringProvider.getInstance().get(ctx, "version_error_dont_show_again"));
				checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				layout.addView(checkBox);
				builder.setView(layout);
				builder.setNegativeButton(LocalizedStringProvider.getInstance().get(ctx, "version_error_exit"),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (checkBox.isChecked()) {
									String prefKey = KEY_VERSION_CHECK_DISABLED + "_" + currentVersion;
									putPrefBoolean(ctx, prefKey, true);
								}
								System.exit(0);
							}
						});
				builder.setPositiveButton(LocalizedStringProvider.getInstance().get(ctx, "version_error_continue"),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (checkBox.isChecked()) {
									String prefKey = KEY_VERSION_CHECK_DISABLED + "_" + currentVersion;
									putPrefBoolean(ctx, prefKey, true);
								}
								dialog.dismiss();
							}
						});
				builder.setCancelable(false);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
	}
	private static void bvLog(String message) {
		XposedBridge.log(message);
		if (developerModeEnabled && Context != null) {
			try {
				if (logWriter == null) {
					initLogFile();
				}
				if (logWriter != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
					String timestamp = sdf.format(new Date());
					logWriter.println("[" + timestamp + "] " + message);
					logWriter.flush();
				}
			} catch (Exception e) {
			}
		}
	}
	private static void initLogFile() {
		try {
			if (Context == null) {
				XposedBridge.log("[BetterVia] initLogFile: Context 为 null，延迟初始化");
				return;
			}
			Context appCtx = Context.getApplicationContext();
			if (appCtx == null)
				appCtx = Context;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
			String timestamp = sdf.format(new Date());
			File baseDir = appCtx.getExternalFilesDir(null);
			if (baseDir == null) {
				XposedBridge.log("[BetterVia] initLogFile: getExternalFilesDir 返回 null");
				return;
			}
			File logDir = new File(baseDir, "BetterVia");
			if (!logDir.exists()) {
				boolean ok = logDir.mkdirs();
				if (!ok) {
					XposedBridge.log("[BetterVia] initLogFile: 无法创建目录 " + logDir.getAbsolutePath());
				}
			}
			logFilePath = new File(logDir, "BetterVia_" + timestamp + ".log").getAbsolutePath();
			logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)), true);
			XposedBridge.log("[BetterVia] initLogFile: 日志文件已创建 -> " + logFilePath);
		} catch (Exception e) {
			XposedBridge.log("[BetterVia] initLogFile 异常: " + Log.getStackTraceString(e));
		}
	}
	private static void closeLogFile() {
		if (logWriter != null) {
			try {
				logWriter.flush();
				logWriter.close();
				XposedBridge.log("[BetterVia] closeLogFile: 日志文件已关闭");
			} catch (Exception e) {
				XposedBridge.log("[BetterVia] closeLogFile 异常: " + Log.getStackTraceString(e));
			}
			logWriter = null;
		}
		logFilePath = null;
		developerModeEnabled = false;
	}
	private static class MonetFileInfo {
		String id;
		String name;
		List<String> sources;
		String type;
		String targetPath;
		List<String> compatibleWith;
		public MonetFileInfo(String id, String name, List<String> sources, String type, String targetPath,
				List<String> compatibleWith) {
			this.id = id;
			this.name = name;
			this.sources = sources;
			this.type = type;
			this.targetPath = targetPath;
			this.compatibleWith = compatibleWith;
		}
	}
	private static class MonetProgressDialog {
		private Dialog dialog;
		private TextView statusText;
		private TextView errorText;
		private ProgressBar progressBar;
		private Button cancelButton;
		private Activity activity;
		private Context context;
		public MonetProgressDialog(Activity act, Context ctx) {
			this.activity = act;
			this.context = ctx;
			createDialog();
		}
		private void createDialog() {
			dialog = new Dialog(activity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			LinearLayout root = new LinearLayout(activity);
			root.setOrientation(LinearLayout.VERTICAL);
			root.setPadding(dp(activity, 24), dp(activity, 24), dp(activity, 24), dp(activity, 24));
			GradientDrawable bg = new GradientDrawable();
			bg.setColor(Color.WHITE);
			bg.setCornerRadius(dp(activity, 24));
			root.setBackground(bg);
			TextView title = new TextView(activity);
			title.setText(LocalizedStringProvider.getInstance().get(context, "monet_progress_title"));
			title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			title.setTextColor(0xFF6200EE);
			title.setTypeface(null, Typeface.BOLD);
			title.setGravity(Gravity.CENTER);
			LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			titleLp.bottomMargin = dp(activity, 16);
			root.addView(title, titleLp);
			progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
			progressBar.setMax(100);
			progressBar.setProgress(0);
			LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			progressLp.bottomMargin = dp(activity, 12);
			root.addView(progressBar, progressLp);
			statusText = new TextView(activity);
			statusText.setText("");
			statusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			statusText.setTextColor(0xFF666666);
			statusText.setGravity(Gravity.CENTER);
			LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			statusLp.bottomMargin = dp(activity, 8);
			root.addView(statusText, statusLp);
			errorText = new TextView(activity);
			errorText.setText("");
			errorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
			errorText.setTextColor(0xFFFF5252);
			errorText.setGravity(Gravity.CENTER);
			errorText.setVisibility(View.GONE);
			LinearLayout.LayoutParams errorLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			errorLp.bottomMargin = dp(activity, 16);
			root.addView(errorText, errorLp);
			cancelButton = new Button(activity);
			cancelButton.setText(LocalizedStringProvider.getInstance().get(context, "monet_progress_cancel"));
			cancelButton.setTextColor(Color.BLACK);
			cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			cancelButton.setTypeface(null, Typeface.BOLD);
			GradientDrawable btnBg = new GradientDrawable();
			btnBg.setColor(0xFFF0F0F0);
			btnBg.setCornerRadius(dp(activity, 12));
			cancelButton.setBackground(btnBg);
			LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			root.addView(cancelButton, btnLp);
			dialog.setContentView(root);
			Window win = dialog.getWindow();
			if (win != null) {
				win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				DisplayMetrics metrics = new DisplayMetrics();
				activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
				int width = (int) (metrics.widthPixels * 0.85);
				WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
				layoutParams.copyFrom(win.getAttributes());
				layoutParams.width = width;
				layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
				layoutParams.gravity = Gravity.CENTER;
				win.setAttributes(layoutParams);
			}
		}
		public void show() {
			dialog.show();
		}
		public void dismiss() {
			dialog.dismiss();
		}
		public void updateProgress(final String message, final int progress) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (dialog != null && dialog.isShowing()) {
						statusText.setText(message);
						progressBar.setProgress(progress);
						errorText.setVisibility(View.GONE);
					}
				}
			});
		}
		public void showError(final String errorMessage) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (dialog != null && dialog.isShowing()) {
						errorText.setText(errorMessage);
						errorText.setVisibility(View.VISIBLE);
					}
				}
			});
		}
		public void setCancelButtonListener(final View.OnClickListener listener) {
			cancelButton.setOnClickListener(listener);
		}
		public boolean isShowing() {
			return dialog != null && dialog.isShowing();
		}
	}
	private void startMonetMomentProcess(final Context ctx) {
		if (monetProcessing) {
			Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "monet_error_processing"),
					Toast.LENGTH_SHORT).show();
			return;
		}
		monetProcessing = true;
		monetCancelled = false;
		final Activity act = getActivityFrom(ctx);
		if (act == null) {
			monetProcessing = false;
			return;
		}
		final MonetProgressDialog progressDialog = new MonetProgressDialog(act, ctx);
		progressDialog.setCancelButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				monetCancelled = true;
				monetProcessing = false;
				progressDialog.dismiss();
				Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "monet_error_cancelled"),
						Toast.LENGTH_SHORT).show();
			}
		});
		progressDialog.show();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					processMonetMoment(ctx, progressDialog);
					monetProcessing = false;
				} catch (final Exception e) {
					bvLog("[BetterVia] 莫奈时刻处理异常: " + Log.getStackTraceString(e));
					monetProcessing = false;
					monetCancelled = false;
					act.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							progressDialog.showError(
									String.format(LocalizedStringProvider.getInstance().get(ctx, "monet_error_unknown"),
											e.getMessage()));
							Toast.makeText(ctx,
									String.format(LocalizedStringProvider.getInstance().get(ctx, "monet_error_unknown"),
											e.getMessage()),
									Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}
	private void processMonetMoment(final Context ctx, final MonetProgressDialog progressDialog) throws Exception {
		final Activity act = getActivityFrom(ctx);
		if (act == null) {
			throw new Exception("Activity is null");
		}
		int outputLocationIdx = getPrefInt(ctx, KEY_MONET_OUTPUT_LOCATION, 0);
		String packageName = ctx.getPackageName();
		File tempDir;
		if (outputLocationIdx == 0) {
			tempDir = new File("/storage/emulated/0/Android/data/" + packageName + "/files/BetterVia/temp/");
		} else {
			tempDir = new File("/data/user/0/" + packageName + "/files/BetterVia/temp/");
		}
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		int baseVersionIdx = getPrefInt(ctx, KEY_MONET_BASE, 0);
		boolean useIcon = getPrefBoolean(ctx, KEY_MONET_USE_ICON, false);
		boolean makeLite = getPrefBoolean(ctx, KEY_MONET_MAKE_LITE, false);
		int signScheme = getPrefInt(ctx, KEY_MONET_SIGN_SCHEME, 0);
		try {
			if (monetCancelled)
				return;
			progressDialog.updateProgress(LocalizedStringProvider.getInstance().get(ctx, "monet_status_cleaning"), 0);
			cleanMonetTempDir(tempDir);
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
			if (monetCancelled)
				return;
			progressDialog.updateProgress(LocalizedStringProvider.getInstance().get(ctx, "monet_status_loading_config"),
					5);
			JSONObject configJson = loadMonetConfigJson(ctx);
			if (monetCancelled)
				return;
			progressDialog
					.updateProgress(LocalizedStringProvider.getInstance().get(ctx, "monet_status_downloading_apk"), 10);
			MonetFileInfo selectedApk = selectApkFile(configJson, baseVersionIdx, useIcon, makeLite);
			if (selectedApk == null) {
				progressDialog.showError(LocalizedStringProvider.getInstance().get(ctx, "monet_error_select_apk"));
				throw new Exception(LocalizedStringProvider.getInstance().get(ctx, "monet_error_select_apk"));
			}
			String apkSource = selectSource(ctx, selectedApk.sources);
			File apkFile = new File(tempDir, "downloaded.apk");
			downloadFile(apkSource, apkFile, new DownloadProgressCallback() {
				@Override
				public void onProgress(long downloaded, long total) {
					String downloadedMB = String.format("%.2f", downloaded / 1024.0 / 1024.0);
					String totalMB = String.format("%.2f", total / 1024.0 / 1024.0);
					progressDialog
							.updateProgress(
									String.format(LocalizedStringProvider.getInstance().get(ctx,
											"monet_status_downloading_apk"), downloadedMB, totalMB),
									10 + (int) (downloaded * 25 / total));
				}
			});
			if (monetCancelled)
				return;
			progressDialog.updateProgress(
					LocalizedStringProvider.getInstance().get(ctx, "monet_status_downloading_sign_zip"), 40);
			MonetFileInfo keyFile = getKeyFile(configJson);
			if (keyFile == null) {
				progressDialog.showError("Key file not found in config");
				throw new Exception("Key file not found in config");
			}
			String keySource = selectSource(ctx, keyFile.sources);
			File keyZipFile = new File(tempDir, "test.zip");
			downloadFile(keySource, keyZipFile, new DownloadProgressCallback() {
				@Override
				public void onProgress(long downloaded, long total) {
					String downloadedMB = String.format("%.2f", downloaded / 1024.0 / 1024.0);
					String totalMB = String.format("%.2f", total / 1024.0 / 1024.0);
					progressDialog.updateProgress(
							String.format(
									LocalizedStringProvider.getInstance().get(ctx, "monet_status_downloading_sign_zip")
											+ " (%s/%s MB)",
									downloadedMB, totalMB),
							40 + (int) (downloaded * 10 / (total > 0 ? total : 1)));
				}
			});
			if (monetCancelled)
				return;
			progressDialog
					.updateProgress(LocalizedStringProvider.getInstance().get(ctx, "monet_status_extracting_sign"), 50);
			File keyDir = new File(tempDir, "keys");
			if (keyDir.exists()) {
				deleteDirectory(keyDir);
			}
			keyDir.mkdirs();
			extractZip(keyZipFile, keyDir);
			File pemFile = new File(keyDir, "test.x509.pem");
			File pk8File = new File(keyDir, "test.pk8");
			if (!pemFile.exists() || !pk8File.exists()) {
				progressDialog.showError("Signing files not found in zip file");
				throw new Exception("Signing files not found in zip file");
			}
			if (monetCancelled)
				return;
			progressDialog.updateProgress(LocalizedStringProvider.getInstance().get(ctx, "monet_status_renaming_apk"),
					55);
			File baseApk = new File(tempDir, "base.apk");
			if (!apkFile.renameTo(baseApk)) {
				progressDialog.showError(LocalizedStringProvider.getInstance().get(ctx, "monet_error_rename"));
				throw new Exception(LocalizedStringProvider.getInstance().get(ctx, "monet_error_rename"));
			}
			if (monetCancelled)
				return;
			final File signedApk = new File(tempDir, "Moneted.apk");
			progressDialog.updateProgress(LocalizedStringProvider.getInstance().get(ctx, "monet_status_signing"), 60);
			signApkWithApksigner(ctx, baseApk, signedApk, pemFile, pk8File, signScheme);
			generatedApkPath = signedApk.getAbsolutePath();
			if (monetCancelled)
				return;
			progressDialog.updateProgress(LocalizedStringProvider.getInstance().get(ctx, "monet_status_cleaning_temp"),
					70);
			deleteDirectory(keyDir);
			if (keyZipFile.exists()) {
				keyZipFile.delete();
			}
			if (baseApk.exists()) {
				baseApk.delete();
			}
			if (monetCancelled)
				return;
			act.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progressDialog.dismiss();
					Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "monet_status_completed"),
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("application/vnd.android.package-archive");
					intent.putExtra(Intent.EXTRA_TITLE, "Via_Moneted.apk");
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					}
					try {
						act.startActivityForResult(intent, 0x2001);
					} catch (Exception e) {
						bvLog("[BetterVia] 打开文件选择器失败: " + e.getMessage());
						Toast.makeText(ctx, LocalizedStringProvider.getInstance().get(ctx, "monet_error_open_apk"),
								Toast.LENGTH_LONG).show();
					}
				}
			});
		} catch (Exception e) {
			progressDialog.showError(e.getMessage());
			throw e;
		}
	}
	private void cleanMonetTempDir(File tempDir) throws Exception {
		if (tempDir.exists()) {
			deleteDirectory(tempDir);
		}
		tempDir.mkdirs();
	}
	private void deleteDirectory(File dir) throws Exception {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					deleteDirectory(file);
				}
			}
		}
		dir.delete();
	}
	private JSONObject loadMonetConfigJson(Context ctx) throws Exception {
		String url = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE).equals(NETWORK_SOURCE_GITEE)
				? MONET_JSON_URL_GITEE
				: MONET_JSON_URL_GITHUB;
		bvLog("[BetterVia] 加载莫奈配置: " + url);
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			URL urlObj = new URL(url);
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setRequestMethod("GET");
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new Exception("HTTP response code: " + responseCode);
			}
			is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			String jsonString = sb.toString();
			return new JSONObject(jsonString);
		} finally {
			if (is != null) {
				is.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	private String selectSource(Context ctx, List<String> sources) {
		String networkSource = getPrefString(ctx, KEY_NETWORK_SOURCE, DEFAULT_NETWORK_SOURCE);
		if (networkSource.equals(NETWORK_SOURCE_GITEE)) {
			for (String source : sources) {
				if (source.contains("gitee.com")) {
					return source;
				}
			}
		} else {
			for (String source : sources) {
				if (source.contains("github.com")) {
					return source;
				}
			}
		}
		return sources.get(0);
	}
	private MonetFileInfo selectApkFile(JSONObject configJson, int baseVersionIdx, boolean useIcon, boolean makeLite)
			throws Exception {
		try {
			JSONArray apkFiles = configJson.getJSONArray("apk_files");
			String basePrefix = baseVersionIdx == 0 ? "cn" : "gp";
			for (int i = 0; i < apkFiles.length(); i++) {
				JSONObject apkObj = apkFiles.getJSONObject(i);
				String id = apkObj.getString("id");
				if (!id.startsWith(basePrefix)) {
					continue;
				}
				boolean isNoIcon = id.contains("noicon");
				if (useIcon && isNoIcon) {
					continue;
				}
				if (!useIcon && !isNoIcon) {
					continue;
				}
				boolean isOptimize = id.contains("optimize");
				if (makeLite && !isOptimize) {
					continue;
				}
				if (!makeLite && isOptimize) {
					continue;
				}
				String name = apkObj.getString("name");
				JSONArray sourcesArray = apkObj.getJSONArray("sources");
				List<String> sources = new ArrayList<String>();
				for (int j = 0; j < sourcesArray.length(); j++) {
					sources.add(sourcesArray.getString(j));
				}
				return new MonetFileInfo(id, name, sources, "apk", "", null);
			}
		} catch (JSONException e) {
			throw new Exception("Failed to parse APK files: " + e.getMessage());
		}
		return null;
	}
	private MonetFileInfo getKeyFile(JSONObject configJson) throws Exception {
		try {
			JSONArray keyFiles = configJson.getJSONArray("key_files");
			for (int i = 0; i < keyFiles.length(); i++) {
				JSONObject keyObj = keyFiles.getJSONObject(i);
				String id = keyObj.getString("id");
				String name = keyObj.getString("name");
				JSONArray sourcesArray = keyObj.getJSONArray("sources");
				List<String> sources = new ArrayList<String>();
				for (int j = 0; j < sourcesArray.length(); j++) {
					sources.add(sourcesArray.getString(j));
				}
				return new MonetFileInfo(id, name, sources, "zip", "", null);
			}
		} catch (JSONException e) {
			throw new Exception("Failed to parse key files: " + e.getMessage());
		}
		return null;
	}
	private void signApkWithApksigner(Context ctx, File inputApk, File outputApk, File pemFile, File pk8File,
			int signScheme) throws Exception {
		bvLog("[BetterVia] 使用 apksig 库进行签名");
		boolean signV1 = (signScheme == 0 || signScheme == 1 || signScheme == 2 || signScheme == 3);
		boolean signV2 = (signScheme == 0 || signScheme == 1 || signScheme == 4 || signScheme == 5);
		boolean signV3 = (signScheme == 0 || signScheme == 2 || signScheme == 4 || signScheme == 6);
		bvLog("[BetterVia] 签名方案: V1=" + signV1 + ", V2=" + signV2 + ", V3=" + signV3);
		try {
			java.security.PrivateKey privateKey = readPrivateKey(pk8File);
			List<java.security.cert.X509Certificate> certs = readCertificates(pemFile);
			com.android.apksig.ApkSigner.SignerConfig signerConfig = new com.android.apksig.ApkSigner.SignerConfig.Builder(
					"CERT", privateKey, certs).build();
			List<com.android.apksig.ApkSigner.SignerConfig> signerConfigs = new ArrayList<>();
			signerConfigs.add(signerConfig);
			com.android.apksig.ApkSigner.Builder builder = new com.android.apksig.ApkSigner.Builder(signerConfigs)
					.setInputApk(inputApk).setOutputApk(outputApk).setV1SigningEnabled(signV1) 
					.setV2SigningEnabled(signV2).setV3SigningEnabled(signV3) 
					.setOtherSignersSignaturesPreserved(false); 
			builder.build().sign();
			bvLog("[BetterVia] APK 签名完成");
		} catch (Exception e) {
			bvLog("[BetterVia] 签名过程中出错: " + e.getMessage());
			throw e;
		}
	}
	private byte[] readFileToBytes(File file) throws IOException {
		long length = file.length();
		byte[] bytes = new byte[(int) length];
		FileInputStream fis = new FileInputStream(file);
		try {
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
		} finally {
			fis.close();
		}
		return bytes;
	}
	private java.security.PrivateKey readPrivateKey(File pk8File) throws Exception {
		byte[] keyBytes = readFileToBytes(pk8File);
		java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
		java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}
	private List<java.security.cert.X509Certificate> readCertificates(File pemFile) throws Exception {
		List<java.security.cert.X509Certificate> certs = new ArrayList<>();
		FileInputStream fis = new FileInputStream(pemFile);
		try {
			java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
			Collection<? extends java.security.cert.Certificate> collection = cf.generateCertificates(fis);
			for (java.security.cert.Certificate cert : collection) {
				if (cert instanceof java.security.cert.X509Certificate) {
					certs.add((java.security.cert.X509Certificate) cert);
				}
			}
		} finally {
			fis.close();
		}
		return certs;
	}
	private java.security.cert.X509Certificate readCertificate(File pemFile) throws Exception {
		byte[] certBytes = readFileToBytes(pemFile);
		java.security.cert.CertificateFactory certFactory = java.security.cert.CertificateFactory.getInstance("X.509");
		java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) certFactory
				.generateCertificate(new java.io.ByteArrayInputStream(certBytes));
		return cert;
	}
	private interface DownloadProgressCallback {
		void onProgress(long downloaded, long total);
	}
	private void downloadFile(String url, File dest, DownloadProgressCallback callback) throws Exception {
		bvLog("[BetterVia] 下载文件: " + url + " -> " + dest.getAbsolutePath());
		HttpURLConnection conn = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			URL urlObj = new URL(url);
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			conn.setRequestMethod("GET");
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new Exception("HTTP response code: " + responseCode);
			}
			long total = conn.getContentLengthLong();
			is = conn.getInputStream();
			fos = new FileOutputStream(dest);
			byte[] buffer = new byte[MONET_BUFFER_SIZE];
			int len;
			long downloaded = 0;
			while ((len = is.read(buffer)) != -1) {
				if (monetCancelled) {
					throw new Exception("Download cancelled");
				}
				fos.write(buffer, 0, len);
				downloaded += len;
				if (callback != null) {
					callback.onProgress(downloaded, total);
				}
			}
			bvLog("[BetterVia] 文件下载完成: " + dest.getAbsolutePath());
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (is != null) {
				is.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	private void extractZip(File apkFile, File destDir) throws Exception {
		bvLog("[BetterVia] 解压Zip: " + apkFile.getAbsolutePath() + " -> " + destDir.getAbsolutePath());
		java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(apkFile);
		java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			java.util.zip.ZipEntry entry = entries.nextElement();
			File file = new File(destDir, entry.getName());
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				InputStream is = zipFile.getInputStream(entry);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[MONET_BUFFER_SIZE];
				int len;
				while ((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				is.close();
				fos.close();
			}
		}
		zipFile.close();
		bvLog("[BetterVia] Zip解压完成");
	}
	private void collectFiles(File dir, List<String> files, File rootDir) {
		File[] list = dir.listFiles();
		if (list != null) {
			for (File file : list) {
				if (file.isDirectory()) {
					collectFiles(file, files, rootDir);
				} else {
					String relativePath = file.getAbsolutePath().substring(rootDir.getAbsolutePath().length() + 1);
					files.add(relativePath.replace("\\", "/"));
				}
			}
		}
	}
	private void showStorageManagerDialog(final Activity act) {
		if (act == null)
			return;
		final String packageName = act.getPackageName(); 
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act.isFinishing() || act.isDestroyed())
					return;
				LinearLayout container = new LinearLayout(act);
				container.setOrientation(LinearLayout.VERTICAL);
				int padding = dp(act, 20);
				container.setPadding(padding, padding, padding, padding);
				final File internalDir = new File("/data/user/0/" + packageName + "/files/BetterVia/");
				final File externalDir = new File(
						"/storage/emulated/0/Android/data/" + packageName + "/files/BetterVia/");
				long totalSize = getFolderSize(internalDir) + getFolderSize(externalDir);
				String sizeStr = formatSize(totalSize);
				final TextView cacheTitle = createTitle(act,
						LocalizedStringProvider.getInstance().get(act, "storage_cache_title")); 
				final TextView cacheSize = createSubtitle(act, sizeStr); 
				Button clearCacheBtn = createButton(act,
						LocalizedStringProvider.getInstance().get(act, "storage_clear")); 
				clearCacheBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						deleteFolder(internalDir);
						deleteFolder(externalDir);
						cacheSize.setText(formatSize(0));
					}
				});
				container.addView(cacheTitle);
				container.addView(cacheSize);
				container.addView(clearCacheBtn);
				final TextView dataTitle = createTitle(act,
						LocalizedStringProvider.getInstance().get(act, "storage_clear_data_title")); 
				final Button clearDataBtn = createButton(act,
						LocalizedStringProvider.getInstance().get(act, "storage_clear"));
				clearDataBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new AlertDialog.Builder(act)
								.setTitle(LocalizedStringProvider.getInstance().get(act, "storage_confirm_title"))
								.setMessage(LocalizedStringProvider.getInstance().get(act, "storage_confirm_message"))
								.setNegativeButton(LocalizedStringProvider.getInstance().get(act, "dialog_cancel"),
										null)
								.setPositiveButton(
										LocalizedStringProvider.getInstance().get(act, "storage_confirm_delete"),
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												performDeepClean(act, packageName, clearDataBtn);
											}
										})
								.show();
					}
				});
				container.addView(space(act, 16)); 
				container.addView(dataTitle);
				container.addView(clearDataBtn);
				AlertDialog dialog = new AlertDialog.Builder(act)
						.setTitle(LocalizedStringProvider.getInstance().get(act, "storage_title")).setView(container)
						.setPositiveButton(LocalizedStringProvider.getInstance().get(act, "dialog_close"), null)
						.create();
				dialog.show();
			}
		});
	}
	private static void performDeepClean(final Activity act, final String packageName, final Button triggerBtn) {
		triggerBtn.setEnabled(false);
		triggerBtn.setText(LocalizedStringProvider.getInstance().get(act, "storage_cleaning"));
		final ProgressBar progressBar = new ProgressBar(act);
		progressBar.setIndeterminate(true);
		final AlertDialog loadingDialog = new AlertDialog.Builder(act)
				.setTitle(LocalizedStringProvider.getInstance().get(act, "storage_cleaning_title")).setView(progressBar)
				.setCancelable(false).create();
		loadingDialog.show();
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean success = true;
				try {
					File internalDir = new File("/data/user/0/" + packageName + "/files/BetterVia/");
					File externalDir = new File(
							"/storage/emulated/0/Android/data/" + packageName + "/files/BetterVia/");
					File prefFile = new File("/data/user/0/" + packageName + "/shared_prefs/BetterVia.xml");
					safeDelete(internalDir);
					safeDelete(externalDir);
					if (prefFile.exists()) {
						prefFile.delete();
					}
				} catch (Throwable t) {
					success = false;
				}
				final boolean finalSuccess = success;
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						loadingDialog.dismiss();
						if (finalSuccess) {
							Toast.makeText(act, LocalizedStringProvider.getInstance().get(act, "storage_clean_success"),
									Toast.LENGTH_LONG).show();
							act.getWindow().getDecorView().postDelayed(new Runnable() {
								@Override
								public void run() {
									android.os.Process.killProcess(android.os.Process.myPid());
									System.exit(0);
								}
							}, 800);
						} else {
							triggerBtn.setEnabled(true);
							triggerBtn
									.setText(LocalizedStringProvider.getInstance().get(act, "storage_confirm_delete"));
							Toast.makeText(act, LocalizedStringProvider.getInstance().get(act, "storage_clean_failed"),
									Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		}).start();
	}
	private static long getFolderSize(File dir) {
		if (dir == null || !dir.exists())
			return 0;
		long size = 0;
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					size += getFolderSize(f);
				} else {
					size += f.length();
				}
			}
		}
		return size;
	}
	private static void deleteFolder(File dir) {
		if (dir == null || !dir.exists())
			return;
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				}
				f.delete();
			}
		}
	}
	private static String formatSize(long size) {
		if (size <= 0)
			return "0 B";
		final String[] units = new String[]{"B", "KB", "MB", "GB"};
		int digit = (int) (Math.log10(size) / Math.log10(1024));
		return String.format(Locale.getDefault(), "%.2f %s", size / Math.pow(1024, digit), units[digit]);
	}
	private static TextView createTitle(Context ctx, String text) {
		TextView tv = new TextView(ctx);
		tv.setText(text);
		tv.setTextSize(16);
		tv.setTypeface(Typeface.DEFAULT_BOLD);
		return tv;
	}
	private static TextView createSubtitle(Context ctx, String text) {
		TextView tv = new TextView(ctx);
		tv.setText(text);
		tv.setTextSize(13);
		tv.setAlpha(0.7f);
		return tv;
	}
	private static Button createButton(Context ctx, String text) {
		Button btn = new Button(ctx);
		btn.setText(text);
		return btn;
	}
	private static View space(Context ctx, int dp) {
		View v = new View(ctx);
		v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, dp)));
		return v;
	}
	private static void safeDelete(File dir) {
		if (dir == null || !dir.exists())
			return;
		File[] files = dir.listFiles();
		if (files == null)
			return;
		for (File f : files) {
			try {
				if (f.isDirectory()) {
					safeDelete(f);
				}
				f.delete();
			} catch (Throwable ignored) {
			}
		}
	}
}