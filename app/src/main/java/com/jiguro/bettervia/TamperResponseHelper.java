// TamperResponseHelper.java
package com.jiguro.bettervia;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import org.json.*;

import java.lang.Process;

public class TamperResponseHelper {

	public interface TamperResponseCallback {
		void onComplete(boolean success);
	}

	public static void handleTamper(final Activity activity, final TamperResponseCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 第一步：检查是否有root权限
				if (hasRootAccess(activity)) {
					// 第二步：检查是否已获得root权限
					if (PermissionHelper.hasAnyPermission(activity)) {
						// 已获得root权限，直接执行第六步
						createConfigFilesWithRoot(activity);
						callback.onComplete(true);
						return;
					} else {
						// 未获得root权限，执行第三步：申请root权限
						requestRootPermission(activity, callback);
						return;
					}
				}

				// 没有root权限，执行第四步
				handleWithoutRoot(activity, callback);
			}
		}).start();
	}

	private static boolean hasRootAccess(Context context) {
		SharedPreferences sp = context.getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE);
		String suCommand = sp.getString("su_command", "").trim();
		String command = suCommand.isEmpty() ? "su" : suCommand;

		try {
			Process process = Runtime.getRuntime().exec(command + " -c id");
			int exitCode = process.waitFor();
			return exitCode == 0;
		} catch (Exception e) {
			return false;
		}
	}

	private static void requestRootPermission(final Activity activity, final TamperResponseCallback callback) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				PermissionHelper.requestRootPermission(activity, new PermissionHelper.PermissionCallback() {
					@Override
					public void onPermissionResult(boolean granted, String type) {
						if (granted) {
							// root权限申请成功，执行第六步
							createConfigFilesWithRoot(activity);
							callback.onComplete(true);
						} else {
							// root权限申请失败，执行第四步
							handleWithoutRoot(activity, callback);
						}
					}
				});
			}
		});
	}

	private static void handleWithoutRoot(final Activity activity, final TamperResponseCallback callback) {
		// 第四步：检查是否有存储权限
		if (hasStoragePermission(activity)) {
			// 有存储权限，执行第六步
			createConfigFilesWithoutRoot(activity);
			callback.onComplete(true);
		} else {
			// 没有存储权限，执行第五步：申请存储权限
			requestStoragePermission(activity, callback);
		}
	}

	private static boolean hasStoragePermission(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return activity.checkSelfPermission(
					android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		}
		return true;
	}

	private static void requestStoragePermission(final Activity activity, final TamperResponseCallback callback) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			activity.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);

			// 我们需要在onRequestPermissionsResult中处理结果
			// 这里我们假设Activity已经设置了适当的处理逻辑
		} else {
			// 在旧版本Android上，默认有存储权限
			createConfigFilesWithoutRoot(activity);
			callback.onComplete(true);
		}
	}

	// 处理存储权限请求结果
	public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions,
        int[] grantResults, TamperResponseCallback callback) {
    if (requestCode == 1001) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 权限申请成功，执行第六步
            createConfigFilesWithoutRoot(activity);
            callback.onComplete(true);
        } else {
            // 权限申请失败，退出应用
            Toast.makeText(activity, activity.getString(R.string.security_fix_storage_permission_denied), Toast.LENGTH_SHORT).show();
            callback.onComplete(false);
        }
    }
}

	/* ================== 此处省略部分代码... ================== */
	    
}

