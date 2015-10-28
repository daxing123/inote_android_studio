package org.dayup.decription;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.Build;
import android.provider.Settings.Secure;
import android.text.format.DateUtils;

public class Utils {
	public static String getDigest() throws NoSuchAlgorithmException {
		String str = Build.DEVICE + Build.FINGERPRINT + Build.ID + Build.VERSION.SDK + (System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS)
			+ getString();
		
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(str.getBytes());
		byte[] digest = m.digest();
		
		return Base64Coder.encodeLines(digest);
	}
	
	public static String getDigest2(String random) throws NoSuchAlgorithmException {
		String str = Build.DEVICE + Build.FINGERPRINT + Build.ID + Build.VERSION.SDK + (System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS)
			+ getString() + Secure.ANDROID_ID + random;
		
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(str.getBytes());
		byte[] digest = m.digest();
		
		return Base64Coder.encodeLines(digest);
	}
	
	
	public static String getString() {
		String ret = "";
		for (int i = 0; i < 3; i++) {
			int t = i | (i << 16) | (i >> 4);
			for (int j = 0; j < 5; j++) {
				t = t | (t << 16) | (t >> 4);
			}
			if (t < 0 || (t & 0xff00) == 0) {
				ret += (t << 2);
			} else {
				ret += (t >> 1);
			}
		}
		return ret;
	}
}
