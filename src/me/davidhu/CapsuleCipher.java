package me.davidhu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

public class CapsuleCipher {
	public static boolean doCipher(int mode, String strKey, File from, File to) {
		try {
			byte[] byteKey = Base64.decode(strKey, Base64.DEFAULT); 
			SecretKey key = new SecretKeySpec(byteKey, "DES");;
			Log.v("capsules", "cipher init");
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(mode, key);
			
			FileInputStream in = new FileInputStream(from);
			byte[] plainData = new byte[(int)from.length()];
			in.read(plainData);
			
			Log.v("capsules", "cipher before");
			byte[] encryptedData = cipher.doFinal(plainData);
			Log.v("capsules", "cipher after");
			FileOutputStream target = new FileOutputStream(to);
			target.write(encryptedData);
			target.close();
			
			Log.v("capsules", "output exists "+to.exists());
			
			return to.exists();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
}
