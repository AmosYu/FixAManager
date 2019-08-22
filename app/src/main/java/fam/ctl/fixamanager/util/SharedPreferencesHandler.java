package fam.ctl.fixamanager.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


public class SharedPreferencesHandler {
	//static ��̬
	private static SharedPreferences prefs = null;
	private static SharedPreferences.Editor editor = null;
	/**
	 * �õ�ʵ��
	 * @param context  ��Ŀ��Դ
	 * @return
	 */
	public static SharedPreferences getInstance(Context context) {
		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}
		return prefs;
	}

	// д���� 
	public static void setDataToPref(Context context, String key, String value) {
		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}

		if (editor == null) {
			editor = prefs.edit();
		}
		//�������
		editor.putString(key, value);
		//�ύ
		editor.commit();
	}

	public static void setDataToPref(Context context, String key, int value) {
		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}

		if (editor == null) {
			editor = prefs.edit();
		}

		editor.putInt(key, value);
		editor.commit();
	}

	public static void setDataToPref(Context context, String key, boolean value) {
		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}

		if (editor == null) {
			editor = prefs.edit();
		}

		editor.putBoolean(key, value);
		editor.commit();
	}

	// ������
	/**
	 * ������
	 * @param context  ��Ŀ��Դ
	 * @param key      key
	 * @param defValue Ĭ��ֵ
	 * @return
	 */
	public static String getDataFromPref(Context context, String key,
			String defValue) {
		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}

		return prefs.getString(key, defValue);
	}

	public static int getDataFromPref(Context context, String key, int defValue) {
		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}
		int ceshi = prefs.getInt(key, defValue);
		Log.i("mytag",ceshi + "��������֪");
//		return prefs.getInt(key, defValue);
		return ceshi;
	}

	public static boolean getDataFromPref(Context context, String key,
			boolean defValue) {
		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}

		return prefs.getBoolean(key, defValue);
	}
	
	
	// ɾ������
	public static void deleteDataToPref(Context context, String key) {
		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}

		if (editor == null) {
			editor = prefs.edit();
		}
		
		editor.remove(key);
		editor.commit();
	}
}