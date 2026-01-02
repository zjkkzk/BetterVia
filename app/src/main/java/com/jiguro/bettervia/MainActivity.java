package com.jiguro.bettervia;

import android.*;
import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import org.json.*;

import java.lang.Process;

public class MainActivity extends Activity {

	private static final String PKG_VIA = "mark.via";
	private static final String PKG_VIAGP = "mark.via.gp";

	public static final String SP_NAME = "module_sp";
	private static final String KEY_AGREED = "user_agreed";
	private static final String KEY_LANGUAGE = "agreement_language";
	private static final String TAG = "BetterVia";
	private static final String KEY_LANGUAGE_SELECTED = "language_selected";

	// 语言常量
	private static final int LANGUAGE_AUTO = -1;
	private static final int LANGUAGE_SIMPLIFIED_CHINESE = 0;
	private static final int LANGUAGE_TRADITIONAL_CHINESE = 1;
	private static final int LANGUAGE_ENGLISH = 2;

	private TextView moduleStatusText;
	private WebView flowWebView;
	private FrameLayout rootContainer;
	private boolean useWebViewBackground = true;
	private boolean isIconHidden = false;
	private static final String ALIAS_ACTIVITY_NAME = "com.jiguro.bettervia.LauncherAlias";

	// 更新相关常量
	private static final String KEY_AUTO_UPDATE = "auto_update";
	private static final String KEY_UPDATE_SOURCE = "update_source";
	private static final int UPDATE_SOURCE_GITHUB = 0;
	private static final int UPDATE_SOURCE_GITEE = 1;

	// 修正 GitHub URL - 使用 raw.githubusercontent.com
	private static final String GITHUB_UPDATE_URL = "https://raw.githubusercontent.com/JiGuroLGC/BetterVia/main/update.json";
	private static final String GITEE_UPDATE_URL = "https://gitee.com/JiGuro/BetterVia/raw/master/update.json";

	private static final int REQUEST_STORAGE_PERMISSION_FOR_FIX = 1002;

	// 预设的包名和版本号
	private static final String EXPECTED_PACKAGE_NAME = "com.jiguro.bettervia";
	private static final int EXPECTED_VERSION_CODE = 20260101;
	private static final String EXPECTED_VERSION_NAME = "1.3.0";

	// 界面文本组件引用
	private TextView appNameText;
	private TextView byAuthorText;
	private TextView blogLinkText;
	private TextView emailLinkText;
	private TextView versionText;
	private TextView copyrightText;

	@Override
	protected void attachBaseContext(Context newBase) {
		// 在Activity创建前应用语言设置
		super.attachBaseContext(updateBaseContextForLanguage(newBase));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    /* ================== 此处省略部分代码... ================== */

		// 首先检查用户是否已经选择了语言
		if (!isLanguageSelected()) {
			// 如果还没有选择语言，显示语言选择对话框
			showLanguageSelectionDialog();
			return;
		}

		// 然后检查用户协议
		if (!isUserAgreed()) {
			showAgreementDialog();
			return;
		}

		initializeMainUI();

		// 启动后异步检查更新
		if (isAutoUpdateEnabled()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					checkUpdate();
				}
			}).start();
		}
	}

    /* ================== 此处省略部分代码... ================== */

	private void showTamperedAppDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// 获取当前语言设置
				SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
				int language = sp.getInt(KEY_LANGUAGE, LANGUAGE_AUTO);

				// 根据语言设置选择相应的标题和消息
				String title, message;

				switch (language) {
					case LANGUAGE_SIMPLIFIED_CHINESE :
						title = "安全检测异常";
						message = "检测到应用修改痕迹或存在安全风险！\n为了您的系统安全，程序将会自动退出。\n请下载正版软件或清空存储重试。";
						break;
					case LANGUAGE_TRADITIONAL_CHINESE :
						title = "安全檢測異常";
						message = "檢測到應用修改痕跡或存在安全風險！\n為了您的系統安全，程式將會自動退出。\n請下載正版軟體或清空存儲重試。";
						break;
					case LANGUAGE_ENGLISH :
					case LANGUAGE_AUTO :
					default :
						// 如果是自动选择，根据系统语言决定
						if (language == LANGUAGE_AUTO) {
							Locale systemLocale;
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								systemLocale = getResources().getConfiguration().getLocales().get(0);
							} else {
								systemLocale = getResources().getConfiguration().locale;
							}

							String languageCode = systemLocale.getLanguage();
							if (languageCode.startsWith("zh")) {
								String country = systemLocale.getCountry();
								if ("TW".equals(country) || "HK".equals(country) || "MO".equals(country)) {
									title = "安全檢測異常";
									message = "檢測到應用修改痕跡或存在安全風險！\n為了您的系統安全，程式將會自動退出。\n請下載正版軟體或清空存儲重試。";
								} else {
									title = "安全检测异常";
									message = "检测到应用修改痕迹或存在安全风险！\n为了您的系统安全，程序将会自动退出。\n请下载正版软件或清空存储重试。";
								}
							} else {
								title = "Security Detection Exception";
								message = "Application modification detected or security risk exists!\nFor your system security, the program will exit automatically.\nPlease download the official version or clear storage and try again.";
							}
						} else {
							title = "Security Detection Exception";
							message = "Application modification detected or security risk exists!\nFor your system security, the program will exit automatically.\nPlease download the official version or clear storage and try again.";
						}
						break;
				}

				final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle(title)
						.setMessage(message).setCancelable(false)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						}).create();

				dialog.show();

				// 3秒后自动退出
				new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
					@Override
					public void run() {
						if (!isFinishing() && !isDestroyed()) {
							if (dialog.isShowing()) {
								dialog.dismiss();
							}
							finish();
						}
					}
				}, 3000);
			}
		});
	}

	/**
	 * 检查用户是否已经选择了语言
	 */
	private boolean isLanguageSelected() {
		SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
		return sp.getBoolean(KEY_LANGUAGE_SELECTED, false);
	}

	/**
	 * 显示语言选择对话框（包含自动选择选项）
	 */
	private void showLanguageSelectionDialog() {
		// 使用系统默认语言显示对话框标题
		String dialogTitle;
		try {
			// 尝试获取字符串资源，如果失败则使用英文
			dialogTitle = getString(R.string.select_language);
		} catch (Resources.NotFoundException e) {
			dialogTitle = "Select Language";
		}

		final String[] languages = {getString(R.string.auto_select), getString(R.string.simplified_chinese),
				getString(R.string.traditional_chinese), getString(R.string.english)};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(dialogTitle).setItems(languages, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 保存语言选择
				SharedPreferences.Editor editor = getSharedPreferences(SP_NAME, MODE_PRIVATE).edit();

				// 处理自动选择
				if (which == 0) {
					editor.putInt(KEY_LANGUAGE, LANGUAGE_AUTO);
				} else {
					editor.putInt(KEY_LANGUAGE, which - 1);
				}

				// 标记语言已选择
				editor.putBoolean(KEY_LANGUAGE_SELECTED, true);
				editor.apply();

				// 立即重启Activity应用新语言
				restartActivityForLanguage();
			}
		}).setCancelable(false).show();
	}

	/**
	 * 更新上下文以应用语言设置
	 */
	private Context updateBaseContextForLanguage(Context context) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME, MODE_PRIVATE);
		int language = sp.getInt(KEY_LANGUAGE, LANGUAGE_AUTO);

		Locale selectedLocale = getSelectedLocale(context, language);

		Configuration config = context.getResources().getConfiguration();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			config.setLocale(selectedLocale);
			return context.createConfigurationContext(config);
		} else {
			config.locale = selectedLocale;
			context.getResources().updateConfiguration(config, dm);
			return context;
		}
	}

	/**
	 * 根据设置获取对应的Locale
	 */
	private Locale getSelectedLocale(Context context, int language) {
		switch (language) {
			case LANGUAGE_SIMPLIFIED_CHINESE :
				return Locale.SIMPLIFIED_CHINESE;
			case LANGUAGE_TRADITIONAL_CHINESE :
				return Locale.TRADITIONAL_CHINESE;
			case LANGUAGE_ENGLISH :
				return Locale.ENGLISH;
			case LANGUAGE_AUTO :
			default :
				return getSystemLanguageLocale(context);
		}
	}

	/**
	 * 获取系统语言对应的Locale
	 */
	private Locale getSystemLanguageLocale(Context context) {
		Locale systemLocale;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			systemLocale = context.getResources().getConfiguration().getLocales().get(0);
		} else {
			systemLocale = context.getResources().getConfiguration().locale;
		}

		// 根据系统语言选择合适的Locale
		String language = systemLocale.getLanguage();
		String country = systemLocale.getCountry();

		if (language.startsWith("zh")) {
			if ("TW".equals(country) || "HK".equals(country) || "MO".equals(country)) {
				return Locale.TRADITIONAL_CHINESE;
			} else {
				return Locale.SIMPLIFIED_CHINESE;
			}
		} else {
			return Locale.ENGLISH;
		}
	}

	/**
	 * 为语言设置重启Activity
	 */
	private void restartActivityForLanguage() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	/**
	 * 显示用户协议对话框（支持多语言）
	 */
	private void showAgreementDialog() {
		// 创建全屏Dialog
		final Dialog agreementDialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
		agreementDialog.setContentView(R.layout.dialog_agreement);
		agreementDialog.setCancelable(false);

		// 获取视图组件
		final TextView titleText = agreementDialog.findViewById(R.id.title_text);
		final TextView contentText = agreementDialog.findViewById(R.id.agreement_content);
		final ScrollView scrollView = agreementDialog.findViewById(R.id.agreement_scrollview);
		Button agreeButton = agreementDialog.findViewById(R.id.agree_button);
		Button disagreeButton = agreementDialog.findViewById(R.id.disagree_button);

		// 根据当前设置的语言获取协议内容
		String agreementTitle = getString(R.string.agreement_title);
		String agreementContent = getString(R.string.agreement_content);
		String agreeButtonText = getString(R.string.agree_button);
		String disagreeButtonText = getString(R.string.disagree_button);

		// 设置多语言内容
		titleText.setText(agreementTitle);
		contentText.setText(agreementContent);
		agreeButton.setText(agreeButtonText);
		disagreeButton.setText(disagreeButtonText);

		// 显示对话框
		agreementDialog.show();

		// 在视图渲染完成后确保标题可见
		titleText.post(new Runnable() {
			@Override
			public void run() {
				ensureTitleVisibility(titleText, scrollView, agreementDialog);
			}
		});

		// 同意按钮点击事件
		agreeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = getSharedPreferences(SP_NAME, MODE_PRIVATE).edit();
				editor.putBoolean(KEY_AGREED, true);
				editor.apply();
				agreementDialog.dismiss();

				// 重启Activity以确保所有资源正确加载
				restartActivity();
			}
		});

		// 拒绝按钮点击事件
		disagreeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String toastMessage = getString(R.string.toast_agreement_required);
				Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();

				agreementDialog.dismiss();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				}, 2000);
			}
		});

		// 添加进入动画
		animateDialogEntrance(agreementDialog);
	}

	/**
	 * 完全重启Activity以确保语言设置生效
	 */
	private void restartActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	/**
	 * 确保标题完全可见，防止被ScrollView挤压
	 */
	private void ensureTitleVisibility(TextView titleText, final ScrollView scrollView, Dialog dialog) {
		// 检查标题是否被挤压或遮挡
		int[] titleLocation = new int[2];
		int[] scrollLocation = new int[2];

		titleText.getLocationOnScreen(titleLocation);
		scrollView.getLocationOnScreen(scrollLocation);

		int titleBottom = titleLocation[1] + titleText.getHeight();
		int scrollTop = scrollLocation[1];

		// 如果ScrollView覆盖了标题，调整布局
		if (scrollTop < titleBottom) {
			adjustLayoutToProtectTitle(titleText, scrollView, dialog);
		}

		// 确保标题在屏幕顶部可见
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.scrollTo(0, 0);
			}
		});

		// 添加标题保护动画
		animateTitleProtection(titleText);
	}

	/**
	 * 调整布局以保护标题不被挤压
	 */
	private void adjustLayoutToProtectTitle(TextView titleText, ScrollView scrollView, Dialog dialog) {
		// 获取标题容器
		View titleContainer = dialog.findViewById(R.id.title_container);

		// 方案1: 增加标题容器高度
		ViewGroup.LayoutParams titleParams = titleContainer.getLayoutParams();
		titleParams.height = (int) (titleParams.height * 1.2f);
		titleContainer.setLayoutParams(titleParams);

		// 方案2: 为ScrollView添加上边距
		ViewGroup.MarginLayoutParams scrollParams = (ViewGroup.MarginLayoutParams) scrollView.getLayoutParams();
		scrollParams.topMargin = (int) (scrollParams.topMargin * 1.5f);
		scrollView.setLayoutParams(scrollParams);

		// 请求重新布局
		titleContainer.requestLayout();
		scrollView.requestLayout();
	}

	/**
	 * 标题保护动画
	 */
	private void animateTitleProtection(TextView titleText) {
		titleText.setAlpha(0f);

		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator fadeIn = ObjectAnimator.ofFloat(titleText, "alpha", 0f, 1f);
		ObjectAnimator scaleUp = ObjectAnimator.ofFloat(titleText, "scaleX", 0.95f, 1f);
		ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(titleText, "scaleY", 0.95f, 1f);

		animatorSet.playTogether(fadeIn, scaleUp, scaleUpY);
		animatorSet.setDuration(400);
		animatorSet.setInterpolator(new OvershootInterpolator());
		animatorSet.start();
	}

	/**
	 * 对话框进入动画
	 */
	private void animateDialogEntrance(Dialog dialog) {
		View dialogView = dialog.getWindow().getDecorView();
		dialogView.setAlpha(0f);
		dialogView.setScaleX(0.9f);
		dialogView.setScaleY(0.9f);

		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator fadeIn = ObjectAnimator.ofFloat(dialogView, "alpha", 0f, 1f);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(dialogView, "scaleX", 0.9f, 1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(dialogView, "scaleY", 0.9f, 1f);

		animatorSet.playTogether(fadeIn, scaleX, scaleY);
		animatorSet.setDuration(300);
		animatorSet.setInterpolator(new DecelerateInterpolator());
		animatorSet.start();
	}

	/**
	 * 初始化主界面UI
	 */
	private void initializeMainUI() {
		setContentView(R.layout.main);

		// 沉浸式深色图标
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().setStatusBarColor(Color.TRANSPARENT);
			int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			}
			getWindow().getDecorView().setSystemUiVisibility(flags);
		}

		// 隐藏系统ActionBar
		if (getActionBar() != null) {
			getActionBar().hide();
		}

		checkIconState();

		rootContainer = (FrameLayout) findViewById(R.id.root_container);
		flowWebView = (WebView) findViewById(R.id.flow_webview);

		checkAndSetupBackground();
		initViews();
		startEntranceAnimations();
	}

	private void checkAndSetupBackground() {
		// 检查设备是否支持WebView背景
		if (isWebViewSupported() && useWebViewBackground) {
			setupWebViewBackground();
		} else {
			// 回退到纯色背景
			useWebViewBackground = false;
			setupFallbackBackground();
			Log.w(TAG, "WebView背景不支持，使用回退方案");
		}
	}

	/**
	 * 检测图标是否可见（关键方法）
	 */
	private boolean isLauncherIconVisible() {
		try {
			ComponentName component = new ComponentName(this, ALIAS_ACTIVITY_NAME);
			PackageManager manager = getPackageManager();
			Intent intent = new Intent().setComponent(component);

			List<ResolveInfo> list;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				list = manager.queryIntentActivities(intent,
						PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY));
			} else {
				list = manager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			}

			return list != null && !list.isEmpty();
		} catch (Exception e) {
			Log.e(TAG, "检测图标状态异常: " + e.getMessage());
			return true;
		}
	}

	/**
	 * 设置图标可见性
	 */
	public void setLauncherIconVisible(boolean visible) {
		if (isLauncherIconVisible() == visible) {
			Log.d(TAG, "状态相同，无需操作");
			return;
		}

		ComponentName component = new ComponentName(this, ALIAS_ACTIVITY_NAME);
		PackageManager manager = getPackageManager();

		int newState = visible
				? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

		try {
			manager.setComponentEnabledSetting(component, newState, PackageManager.DONT_KILL_APP);
			isIconHidden = !visible;

			boolean success = (isLauncherIconVisible() == visible);
			Log.d(TAG, "操作结果: " + (success ? "成功" : "失败"));

			refreshLauncher();
			updateMenuState();

			String message = visible ? getString(R.string.toast_icon_shown) : getString(R.string.toast_icon_hidden);
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

		} catch (SecurityException e) {
			Log.e(TAG, "权限不足: " + e.getMessage());
			Toast.makeText(this, R.string.toast_permission_denied, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.e(TAG, "设置失败: " + e.getMessage());
			Toast.makeText(this, R.string.toast_operation_failed, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 更新菜单状态
	 */
	private void updateMenuState() {
		ImageButton menuButton = (ImageButton) findViewById(R.id.menu_button);
		if (menuButton != null) {
			Log.d(TAG, "菜单状态已更新，当前图标状态: " + (isIconHidden ? "已隐藏" : "显示"));
		}
	}

	private void checkIconState() {
		ComponentName aliasComponent = new ComponentName(this, ALIAS_ACTIVITY_NAME);
		PackageManager manager = getPackageManager();
		int currentState = manager.getComponentEnabledSetting(aliasComponent);

		isIconHidden = (currentState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
		Log.d(TAG, "当前图标状态: " + (isIconHidden ? "已隐藏" : "显示"));
	}

	private void refreshLauncher() {
		try {
			String[] actions = {"com.android.launcher3.action.REFRESH_LAUNCHER", "miui.intent.action.REFRESH_LAUNCHER",
					"com.huawei.android.launcher.action.REFRESH_LAUNCHER",
					"com.sec.android.app.launcher.action.REFRESH_LAUNCHER",
					"com.oppo.launcher.action.REFRESH_LAUNCHER"};

			for (String action : actions) {
				try {
					Intent intent = new Intent(action);
					sendBroadcast(intent);
					Log.d(TAG, "发送广播: " + action);
				} catch (Exception e) {
					Log.w(TAG, "广播发送失败: " + action);
				}
			}

			Intent stdIntent = new Intent(Intent.ACTION_MAIN);
			stdIntent.addCategory(Intent.CATEGORY_HOME);
			sendBroadcast(stdIntent);

		} catch (Exception e) {
			Log.e(TAG, "刷新桌面异常: " + e.getMessage());
		}
	}

	private boolean isWebViewSupported() {
		try {
			WebView testView = new WebView(this);
			testView.destroy();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "WebView不支持: " + e.getMessage());
			return false;
		}
	}

	private void setupWebViewBackground() {
		try {
			flowWebView.setBackgroundColor(Color.TRANSPARENT);
			flowWebView.setVerticalScrollBarEnabled(false);
			flowWebView.setHorizontalScrollBarEnabled(false);

			flowWebView.getSettings().setLoadWithOverviewMode(true);
			flowWebView.getSettings().setUseWideViewPort(true);
			flowWebView.getSettings().setJavaScriptEnabled(true);
			flowWebView.getSettings().setDomStorageEnabled(true);

			flowWebView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

			flowWebView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					return true;
				}
			});

			flowWebView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});

			flowWebView.loadUrl("file:///android_asset/flow_background.html");
			flowWebView.setVisibility(View.VISIBLE);

			Log.d(TAG, "WebView背景设置成功");

		} catch (Exception e) {
			Log.e(TAG, "WebView背景设置失败: " + e.getMessage());
			useWebViewBackground = false;
			setupFallbackBackground();
		}
	}

	private void setupFallbackBackground() {
		rootContainer.setBackgroundColor(Color.WHITE);
		flowWebView.setVisibility(View.GONE);
	}

	/**
	 * 初始化视图并设置多语言文本
	 */
	private void initViews() {
		// 获取所有文本组件引用
		moduleStatusText = (TextView) findViewById(R.id.module_status);
		appNameText = (TextView) findViewById(R.id.app_name);
		byAuthorText = (TextView) findViewById(R.id.by_author);
		blogLinkText = (TextView) findViewById(R.id.blog_link);
		emailLinkText = (TextView) findViewById(R.id.email_link);
		versionText = (TextView) findViewById(R.id.version_text);
		copyrightText = (TextView) findViewById(R.id.copyright_text);

		// 更新所有文本为当前语言
		updateAllTexts();

		// 博客链接
		if (blogLinkText != null) {
			blogLinkText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openBlog();
				}
			});
		}

		// 邮箱链接
		if (emailLinkText != null) {
			emailLinkText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sendEmail();
				}
			});
		}

		// 菜单按钮
		ImageButton menuButton = (ImageButton) findViewById(R.id.menu_button);
		if (menuButton != null) {
			menuButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showPopupMenu(v);
				}
			});
		}

		// 图标点击事件
		setupIconClickEvents();

		// 初始设置所有内容为透明和偏移
		setInitialViewStates();
	}

	/**
	 * 更新所有界面文本为当前语言
	 */
	private void updateAllTexts() {
		// 更新模块状态
		updateModuleStatus();

		// 更新应用名称
		if (appNameText != null) {
			appNameText.setText(getString(R.string.app_name));
		}

		// 更新作者信息
		if (byAuthorText != null) {
			byAuthorText.setText(getString(R.string.by_author));
		}

		// 更新博客链接
		if (blogLinkText != null) {
			blogLinkText.setText(getString(R.string.blog_link));
		}

		// 更新邮箱链接
		if (emailLinkText != null) {
			emailLinkText.setText(getString(R.string.contact_email));
		}

		// 更新版本信息
		if (versionText != null) {
			versionText.setText(getString(R.string.version_info));
		}

		// 更新版权信息
		if (copyrightText != null) {
			copyrightText.setText(getString(R.string.copyright_info));
		}

		// 更新菜单按钮的内容描述
		ImageButton menuButton = (ImageButton) findViewById(R.id.menu_button);
		if (menuButton != null) {
			menuButton.setContentDescription(getString(R.string.menu));
		}
	}

	/**
	 * 更新模块状态显示
	 */
	private void updateModuleStatus() {
		if (moduleStatusText != null) {
			// 设置模块状态文本
			moduleStatusText.setText(getString(R.string.module_status));

			if (isActivated()) {
				moduleStatusText.setText(getString(R.string.activated));
				moduleStatusText.setTextColor(0xFF4CAF50);
				animateActivatedStatus();
			} else {
				moduleStatusText.setText(getString(R.string.not_activated));
				moduleStatusText.setTextColor(0xFFF44336);
			}
		}
	}

	private void setupIconClickEvents() {
		// GitHub图标
		ImageView githubIcon = (ImageView) findViewById(R.id.github_icon);
		if (githubIcon != null) {
			githubIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openGitHub();
				}
			});
		}

		// Gitee图标
		ImageView giteeIcon = (ImageView) findViewById(R.id.gitee_icon);
		if (giteeIcon != null) {
			giteeIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openGitee();
				}
			});
		}

		// Xposed仓库图标
		ImageView xposedIcon = (ImageView) findViewById(R.id.xposed_icon);
		if (xposedIcon != null) {
			xposedIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openXposedRepo();
				}
			});
		}
	}

	private void setInitialViewStates() {
		// ActionBar区域
		View customActionBar = findViewById(R.id.custom_actionbar);
		if (customActionBar != null) {
			customActionBar.setAlpha(0f);
			customActionBar.setTranslationY(-30f);
		}

		// 第一段：标题和作者
		if (appNameText != null) {
			appNameText.setAlpha(0f);
			appNameText.setTranslationY(20f);
		}

		if (byAuthorText != null) {
			byAuthorText.setAlpha(0f);
			byAuthorText.setTranslationY(20f);
		}

		// 第二段：联系方式区域
		View divider = findViewById(R.id.contact_divider);
		if (divider != null) {
			divider.setAlpha(0f);
		}

		if (blogLinkText != null) {
			blogLinkText.setAlpha(0f);
			blogLinkText.setTranslationX(-30f);
		}

		if (emailLinkText != null) {
			emailLinkText.setAlpha(0f);
			emailLinkText.setTranslationX(30f);
		}

		// 第三段：图标区域
		ImageView githubIcon = (ImageView) findViewById(R.id.github_icon);
		if (githubIcon != null) {
			githubIcon.setAlpha(0f);
			githubIcon.setTranslationY(30f);
			githubIcon.setScaleX(0.8f);
			githubIcon.setScaleY(0.8f);
		}

		ImageView giteeIcon = (ImageView) findViewById(R.id.gitee_icon);
		if (giteeIcon != null) {
			giteeIcon.setAlpha(0f);
			giteeIcon.setTranslationY(30f);
			giteeIcon.setScaleX(0.8f);
			giteeIcon.setScaleY(0.8f);
		}

		ImageView xposedIcon = (ImageView) findViewById(R.id.xposed_icon);
		if (xposedIcon != null) {
			xposedIcon.setAlpha(0f);
			xposedIcon.setTranslationY(30f);
			xposedIcon.setScaleX(0.8f);
			xposedIcon.setScaleY(0.8f);
		}

		// 版本信息
		if (versionText != null) {
			versionText.setAlpha(0f);
			versionText.setTranslationY(20f);
		}

		if (copyrightText != null) {
			copyrightText.setAlpha(0f);
			copyrightText.setTranslationY(20f);
		}
	}

	private void startEntranceAnimations() {
		AnimatorSet animatorSet = new AnimatorSet();

		// 1. ActionBar淡入下滑动画
		View customActionBar = findViewById(R.id.custom_actionbar);
		Animator actionBarAnim = createActionBarAnimation(customActionBar);

		// 2. 主要内容动画
		AnimatorSet mainContentAnimSet = new AnimatorSet();

		Animator titleAuthorAnim = createTitleAuthorAnimation();
		Animator contactAnim = createContactAnimation();
		Animator iconsAnim = createIconsAnimation();
		Animator versionAnim = createVersionAnimation();

		contactAnim.setStartDelay(300);
		iconsAnim.setStartDelay(600);
		versionAnim.setStartDelay(900);

		mainContentAnimSet.playTogether(titleAuthorAnim, contactAnim, iconsAnim, versionAnim);
		animatorSet.playSequentially(actionBarAnim, mainContentAnimSet);

		animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animatorSet.setStartDelay(200);
		animatorSet.start();
	}

	private Animator createActionBarAnimation(View view) {
		if (view == null)
			return new AnimatorSet();

		AnimatorSet set = new AnimatorSet();
		ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
		ObjectAnimator slideDown = ObjectAnimator.ofFloat(view, "translationY", -30f, 0f);

		set.playTogether(fadeIn, slideDown);
		set.setDuration(400);
		set.setInterpolator(new DecelerateInterpolator());

		return set;
	}

	private Animator createTitleAuthorAnimation() {
		AnimatorSet set = new AnimatorSet();

		Animator titleAnim = createFadeUpAnimation(appNameText, 20f, 600);
		Animator authorAnim = createFadeUpAnimation(byAuthorText, 20f, 600);
		authorAnim.setStartDelay(100);

		set.playTogether(titleAnim, authorAnim);
		return set;
	}

	private Animator createContactAnimation() {
		AnimatorSet set = new AnimatorSet();

		View divider = findViewById(R.id.contact_divider);
		Animator dividerAnim = createFadeInAnimation(divider, 400);

		Animator blogAnim = createSlideInAnimation(blogLinkText, -30f, 0f, 500);
		blogAnim.setStartDelay(200);

		Animator emailAnim = createSlideInAnimation(emailLinkText, 30f, 0f, 500);
		emailAnim.setStartDelay(200);

		set.playTogether(dividerAnim, blogAnim, emailAnim);
		return set;
	}

	private Animator createIconsAnimation() {
		AnimatorSet set = new AnimatorSet();

		ImageView githubIcon = (ImageView) findViewById(R.id.github_icon);
		ImageView giteeIcon = (ImageView) findViewById(R.id.gitee_icon);
		ImageView xposedIcon = (ImageView) findViewById(R.id.xposed_icon);

		Animator githubAnim = createIconAnimation(githubIcon, 0);
		Animator giteeAnim = createIconAnimation(giteeIcon, 150);
		Animator xposedAnim = createIconAnimation(xposedIcon, 300);

		set.playTogether(githubAnim, giteeAnim, xposedAnim);
		return set;
	}

	private Animator createVersionAnimation() {
		AnimatorSet set = new AnimatorSet();

		Animator versionAnim = createFadeUpAnimation(versionText, 20f, 500);
		Animator copyrightAnim = createFadeUpAnimation(copyrightText, 20f, 500);
		copyrightAnim.setStartDelay(100);

		set.playTogether(versionAnim, copyrightAnim);
		return set;
	}

	private Animator createFadeUpAnimation(View view, float startY, long duration) {
		if (view == null)
			return new AnimatorSet();

		AnimatorSet set = new AnimatorSet();
		ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
		ObjectAnimator slideUp = ObjectAnimator.ofFloat(view, "translationY", startY, 0f);

		set.playTogether(fadeIn, slideUp);
		set.setDuration(duration);
		set.setInterpolator(new DecelerateInterpolator());

		return set;
	}

	private Animator createFadeInAnimation(View view, long duration) {
		if (view == null)
			return new AnimatorSet();

		ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
		fadeIn.setDuration(duration);
		fadeIn.setInterpolator(new DecelerateInterpolator());

		return fadeIn;
	}

	private Animator createSlideInAnimation(View view, float startX, float endX, long duration) {
		if (view == null)
			return new AnimatorSet();

		AnimatorSet set = new AnimatorSet();
		ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
		ObjectAnimator slideIn = ObjectAnimator.ofFloat(view, "translationX", startX, endX);

		set.playTogether(fadeIn, slideIn);
		set.setDuration(duration);
		set.setInterpolator(new DecelerateInterpolator());

		return set;
	}

	private Animator createIconAnimation(View view, long delay) {
		if (view == null)
			return new AnimatorSet();

		AnimatorSet set = new AnimatorSet();
		ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
		ObjectAnimator slideUp = ObjectAnimator.ofFloat(view, "translationY", 30f, 0f);
		ObjectAnimator scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
		ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);

		set.playTogether(fadeIn, slideUp, scaleUp, scaleUpY);
		set.setDuration(500);
		set.setStartDelay(delay);
		set.setInterpolator(new OvershootInterpolator(0.8f));

		return set;
	}

	private void animateActivatedStatus() {
		ObjectAnimator pulse = ObjectAnimator.ofFloat(moduleStatusText, "alpha", 1f, 0.7f, 1f);
		pulse.setDuration(1000);
		pulse.setRepeatCount(ObjectAnimator.INFINITE);
		pulse.setRepeatMode(ObjectAnimator.REVERSE);
		pulse.setStartDelay(1000);
		pulse.start();
	}

	private boolean isActivated() {
		return com.jiguro.bettervia.ModuleStatus.activated;
	}

	/**
	 * 显示弹出菜单（支持多语言）
	 */
	private void showPopupMenu(View view) {
		PopupMenu popupMenu = new PopupMenu(this, view);
		popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());

		// 动态设置菜单项标题（使用多语言）
		MenuItem updateSettingsItem = popupMenu.getMenu().findItem(R.id.menu_update_settings);
		if (updateSettingsItem != null) {
			updateSettingsItem.setTitle(getString(R.string.update_settings));
		}

		MenuItem toggleIconItem = popupMenu.getMenu().findItem(R.id.menu_toggle_icon);
		if (toggleIconItem != null) {
			toggleIconItem.setTitle(isIconHidden ? getString(R.string.show_icon) : getString(R.string.hide_icon));
		}

		MenuItem withdrawAgreementItem = popupMenu.getMenu().findItem(R.id.menu_withdraw_agreement);
		if (withdrawAgreementItem != null) {
			withdrawAgreementItem.setTitle(getString(R.string.withdraw_agreement));
		}

		MenuItem securityfixItem = popupMenu.getMenu().findItem(R.id.menu_security_fix);
		if (securityfixItem != null) {
			securityfixItem.setTitle(getString(R.string.security_fix));
		}

		MenuItem exitItem = popupMenu.getMenu().findItem(R.id.menu_exit);
		if (exitItem != null) {
			exitItem.setTitle(getString(R.string.exit));
		}

		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (item.getItemId() == R.id.menu_update_settings) {
					showUpdateSettingsDialog();
					return true;
				} else if (item.getItemId() == R.id.menu_toggle_icon) {
					setLauncherIconVisible(isIconHidden);
					return true;
				} else if (item.getItemId() == R.id.menu_withdraw_agreement) {
					showWithdrawAgreementDialog();
					return true;
				} else if (item.getItemId() == R.id.menu_security_fix) {
					showSecurityFixDialog();
					return true;
				} else if (item.getItemId() == R.id.menu_exit) {
					animateExit();
					return true;
				}
				return false;
			}
		});
		popupMenu.show();
	}

	/**
	 * 显示撤回声明同意的确认对话框
	 */
	private void showWithdrawAgreementDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.withdraw_agreement_title))
				.setMessage(getString(R.string.withdraw_agreement_message))
				.setPositiveButton(getString(R.string.withdraw_confirm), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						withdrawAgreement();
					}
				}).setNegativeButton(getString(R.string.withdraw_cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setCancelable(true).show();
	}

	/**
	 * 撤回用户协议同意
	 */
	private void withdrawAgreement() {
		try {
			// 重置用户同意状态
			SharedPreferences.Editor editor = getSharedPreferences(SP_NAME, MODE_PRIVATE).edit();
			editor.putBoolean(KEY_AGREED, false);
			editor.apply();

			// 显示成功消息
			Toast.makeText(this, getString(R.string.withdraw_success), Toast.LENGTH_LONG).show();

			// 延迟退出应用
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					finish();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						finishAffinity();
					}
				}
			}, 1500);

		} catch (Exception e) {
			Log.e(TAG, "撤回声明同意失败: " + e.getMessage());
			Toast.makeText(this, R.string.toast_operation_failed, Toast.LENGTH_SHORT).show();
		}
	}

	private void animateExit() {
		AnimatorSet exitAnimator = new AnimatorSet();
		View rootView = findViewById(android.R.id.content);
		ObjectAnimator fadeOut = ObjectAnimator.ofFloat(rootView, "alpha", 1f, 0f);
		ObjectAnimator scaleDown = ObjectAnimator.ofFloat(rootView, "scaleX", 1f, 0.95f);
		ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(rootView, "scaleY", 1f, 0.95f);

		exitAnimator.playTogether(fadeOut, scaleDown, scaleDownY);
		exitAnimator.setDuration(250);
		exitAnimator.setInterpolator(new AccelerateInterpolator());

		exitAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				finish();
			}
			@Override
			public void onAnimationCancel(Animator animation) {
				finish();
			}
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});
		exitAnimator.start();
	}

	private void openGitHub() {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JiGuroLGC/BetterVia"));
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(this, R.string.toast_cannot_open_github, Toast.LENGTH_SHORT).show();
		}
	}

	private void openGitee() {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitee.com/JiGuro/BetterVia"));
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(this, R.string.toast_cannot_open_gitee, Toast.LENGTH_SHORT).show();
		}
	}

	private void openXposedRepo() {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://modules.lsposed.org/module/com.jiguro.bettervia"));
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(this, R.string.toast_cannot_open_xposed, Toast.LENGTH_SHORT).show();
		}
	}

	private void openBlog() {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.blog_url)));
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(this, R.string.toast_cannot_open_link, Toast.LENGTH_SHORT).show();
		}
	}

	private void sendEmail() {
		try {
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("mailto:" + getString(R.string.email_address)));
			intent.putExtra(Intent.EXTRA_SUBJECT, "BetterVia Module Feedback");
			startActivity(Intent.createChooser(intent, getString(R.string.contact_email)));
		} catch (Exception e) {
			Toast.makeText(this, R.string.toast_cannot_send_email, Toast.LENGTH_SHORT).show();
		}
	}

	private boolean isUserAgreed() {
		SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
		return sp.getBoolean(KEY_AGREED, false);
	}

	/**
	 * 保存更新设置
	 */
	private void saveUpdateSettings(boolean autoUpdate, int updateSource) {
		SharedPreferences.Editor editor = getSharedPreferences(SP_NAME, MODE_PRIVATE).edit();
		editor.putBoolean(KEY_AUTO_UPDATE, autoUpdate);
		editor.putInt(KEY_UPDATE_SOURCE, updateSource);
		editor.apply();
	}

	/**
	* 检查是否启用自动更新
	*/
	private boolean isAutoUpdateEnabled() {
		SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
		return sp.getBoolean(KEY_AUTO_UPDATE, true);
	}

	/**
	 * 获取当前更新源
	 */
	private int getCurrentUpdateSource() {
		SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
		// 默认使用 Gitee
		return sp.getInt(KEY_UPDATE_SOURCE, UPDATE_SOURCE_GITEE);
	}

	/**
	 * 检查更新
	 */
	private void checkUpdate() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int updateSource = getCurrentUpdateSource();
					String updateUrl = (updateSource == UPDATE_SOURCE_GITHUB) ? GITHUB_UPDATE_URL : GITEE_UPDATE_URL;

					Log.d(TAG, "检查更新，URL: " + updateUrl);

					URL url = new URL(updateUrl);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(8000);
					conn.setReadTimeout(8000);
					conn.setRequestMethod("GET");

					if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
						Log.e(TAG, "HTTP错误代码: " + conn.getResponseCode());
						showUpdateCheckFailed();
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
					Log.d(TAG, "获取到的JSON: " + jsonResponse);

					final UpdateInfo info = new UpdateInfo();
					JSONObject json = new JSONObject(jsonResponse);
					info.versionName = json.getString("versionName");
					info.apkUrl = json.getString("apkUrl");

					// 多语言更新日志解析
					try {
						JSONObject updateLogJson = json.getJSONObject("updateLog");
						String currentLangCode = getCurrentLanguageCode();
						if (updateLogJson.has(currentLangCode)) {
							info.updateLog = updateLogJson.getString(currentLangCode);
						} else {
							// 回退到英文
							info.updateLog = updateLogJson.getString("en");
						}
					} catch (JSONException e) {
						// 如果不是多语言格式，尝试直接获取字符串
						info.updateLog = json.getString("updateLog");
					}

					String localVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
					Log.d(TAG, "本地版本: " + localVersion + ", 远程版本: " + info.versionName);

					if (!info.versionName.equals(localVersion)) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showUpdateDialog(info);
							}
						});
					} else {
						// 静默处理，不显示任何提示
						Log.d(TAG, "已是最新版本，无需更新");
					}
				} catch (Exception e) {
					Log.e(TAG, "更新检查异常: " + e.getMessage(), e);
					showUpdateCheckFailed();
				}
			}
		}).start();
	}

	/**
	 * 获取当前语言代码
	 */
	private String getCurrentLanguageCode() {
		SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
		int language = sp.getInt(KEY_LANGUAGE, LANGUAGE_AUTO);

		switch (language) {
			case LANGUAGE_SIMPLIFIED_CHINESE :
				return "zh-CN";
			case LANGUAGE_TRADITIONAL_CHINESE :
				return "zh-TW";
			case LANGUAGE_ENGLISH :
			case LANGUAGE_AUTO :
			default :
				// 如果是自动选择，根据系统语言决定
				if (language == LANGUAGE_AUTO) {
					Locale systemLocale;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						systemLocale = getResources().getConfiguration().getLocales().get(0);
					} else {
						systemLocale = getResources().getConfiguration().locale;
					}

					String languageCode = systemLocale.getLanguage();
					String country = systemLocale.getCountry();

					if (languageCode.startsWith("zh")) {
						if ("TW".equals(country) || "HK".equals(country) || "MO".equals(country)) {
							return "zh-TW";
						} else {
							return "zh-CN";
						}
					}
				}
				return "en";
		}
	}

	/**
	 * 显示更新设置对话框
	 */
	private void showUpdateSettingsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// 创建对话框视图
		View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_settings, null);
		builder.setView(dialogView);

		final Switch autoUpdateSwitch = dialogView.findViewById(R.id.auto_update_switch);
		final RadioGroup updateSourceGroup = dialogView.findViewById(R.id.update_source_group);
		final RadioButton githubRadio = dialogView.findViewById(R.id.github_radio);
		final RadioButton giteeRadio = dialogView.findViewById(R.id.gitee_radio);

		// 设置当前状态
		SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
		autoUpdateSwitch.setChecked(sp.getBoolean(KEY_AUTO_UPDATE, true));

		int updateSource = getCurrentUpdateSource();

		// 使用 RadioGroup 的 check 方法来设置初始选中状态
		if (updateSource == UPDATE_SOURCE_GITHUB) {
			updateSourceGroup.check(R.id.github_radio);
		} else {
			updateSourceGroup.check(R.id.gitee_radio);
		}

		// 为整个选项区域添加点击事件
		View githubOption = dialogView.findViewById(R.id.github_option_container);
		View giteeOption = dialogView.findViewById(R.id.gitee_option_container);

		if (githubOption != null) {
			githubOption.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					updateSourceGroup.check(R.id.github_radio);
				}
			});
		}

		if (giteeOption != null) {
			giteeOption.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					updateSourceGroup.check(R.id.gitee_radio);
				}
			});
		}

		builder.setTitle(getString(R.string.update_settings))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int selectedId = updateSourceGroup.getCheckedRadioButtonId();
						int selectedSource;

						if (selectedId == R.id.github_radio) {
							selectedSource = UPDATE_SOURCE_GITHUB;
						} else if (selectedId == R.id.gitee_radio) {
							selectedSource = UPDATE_SOURCE_GITEE;
						} else {
							// 如果没有选择，保持当前设置
							selectedSource = getCurrentUpdateSource();
						}

						// 保存设置
						saveUpdateSettings(autoUpdateSwitch.isChecked(), selectedSource);

						// 立即按照用户配置执行一次更新检测
						performImmediateUpdateCheck();
					}
				}).setNegativeButton(android.R.string.cancel, null).show();
	}

	/**
	 * 立即执行更新检查
	 */
	private void performImmediateUpdateCheck() {
		// 检查自动更新是否开启
		if (!isAutoUpdateEnabled()) {
			return;
		}

		// 显示检查中的提示
		Toast.makeText(this, getString(R.string.checking_updates), Toast.LENGTH_SHORT).show();

		// 延迟一小段时间后开始检查，让用户看到提示
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				checkUpdate();
			}
		}, 500);
	}

	/**
	 * 显示更新对话框
	 */
	private void showUpdateDialog(final UpdateInfo info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(String.format(getString(R.string.new_version_found), info.versionName))
				.setMessage(info.updateLog).setCancelable(false)
				.setPositiveButton(getString(R.string.download_now), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.apkUrl));
							startActivity(intent);
						} catch (Exception e) {
							Toast.makeText(MainActivity.this, getString(R.string.cannot_open_download_link),
									Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton(getString(R.string.later), null).show();
	}

	/**
	 * 显示更新检查失败提示
	 */
	private void showUpdateCheckFailed() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this, getString(R.string.update_check_failed), Toast.LENGTH_SHORT).show();
			}
		});
	}

	// 更新信息类
	private static class UpdateInfo {
		String versionName;
		String updateLog;
		String apkUrl;
	}

	/**
	* 配置安全修复
	*/

	private void showSecurityFixDialog() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.security_fix_dialog_title))
				.setMessage(getString(R.string.security_fix_dialog_message))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startSecurityFixProcess();
					}
				}).setNegativeButton(android.R.string.cancel, null).show();
	}

	/**
	 * 使用命令行方式检测Root权限
	 */
	private boolean hasRootPermission() {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("echo 'Checking root access'\n");
			os.writeBytes("exit\n");
			os.flush();

			int exitValue = process.waitFor();
			return exitValue == 0;
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (Exception e) {
				// 忽略异常
			}
		}
	}

	private void startSecurityFixProcess() {
		// 优先检测Root权限
		if (hasRootPermission()) {
			// 有Root权限，直接执行完整修复
			performSecurityFixWithRoot();
		} else {
			// 没有Root权限，检查存储权限
			if (checkStoragePermission()) {
				// 有存储权限，执行普通修复
				performSecurityFixWithoutRoot();
			} else {
				// 没有存储权限，申请存储权限
				requestStoragePermission();
			}
		}
	}

	private boolean checkStoragePermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		}
		return true; // Android 6.0以下默认有存储权限
	}

	private void requestStoragePermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					REQUEST_STORAGE_PERMISSION_FOR_FIX);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == REQUEST_STORAGE_PERMISSION_FOR_FIX) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				performSecurityFixWithoutRoot();
			} else {
				if (hasRootPermission()) {
					performSecurityFixWithRoot();
				} else {
					Toast.makeText(this, getString(R.string.security_fix_need_permission), Toast.LENGTH_LONG).show();
				}
			}
			return;
		}

		// 处理 TamperResponseHelper 的权限请求
		TamperResponseHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults,
				new TamperResponseHelper.TamperResponseCallback() {
					@Override
					public void onComplete(boolean success) {
						if (success) {
							showTamperedAppDialog();
						} else {
							Toast.makeText(MainActivity.this,
									getString(R.string.security_fix_storage_permission_denied), Toast.LENGTH_SHORT)
									.show();
							finish();
						}
					}
				});
	}

    /* ================== 此处省略部分代码... ================== */

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (flowWebView != null) {
			flowWebView.destroy();
		}
	}
}

