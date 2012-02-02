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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

public class UnlockCapsuleActivity extends Activity {
	
	private String strKey;
	private JSONObject fileList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unlock_screen);
		
		strKey = getIntent().getExtras().getString("strKey");
		
		try {
			fileList = (JSONObject) new JSONTokener(getIntent().getExtras().getString("fileList")).nextValue();
			Log.v("", fileList.getString("0"));
			JSONArray fileListNames = fileList.names();
			for(int i=0;i<fileListNames.length();i++) {
				File currFile = new File(fileList.getString(fileListNames.getString(i)));
				doCipher(Cipher.DECRYPT_MODE, strKey, currFile, new File("/mnt/sdcard/Android/data/me.davidhu/files/outdecrypted"+i+".jpg"));
				//encrypted.delete();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean doCipher(int mode, String strKey, File from, File to) {
		try {
			byte[] byteKey = Base64.decode(strKey, Base64.DEFAULT); 
			SecretKey key = new SecretKeySpec(byteKey, "DES");;
			/*if(mode == Cipher.ENCRYPT_MODE) {
				key = KeyGenerator.getInstance("DES").generateKey();
				Log.v(TAG, "key: "+Base64.encodeToString(key.getEncoded(), Base64.DEFAULT));
			} else if(mode == Cipher.DECRYPT_MODE) {
				byte[] byteKey = Base64.decode(strKey, Base64.DEFAULT); 
				key = new SecretKeySpec(byteKey, "DES");
			} else {
				return false;
			}*/
			Log.v("", "starting cipher...");
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(mode, key);
			
			FileInputStream in = new FileInputStream(from);
			byte[] plainData = new byte[(int)from.length()];
			in.read(plainData);
			
			byte[] encryptedData = cipher.doFinal(plainData);
			FileOutputStream target = new FileOutputStream(to);
			target.write(encryptedData);
			target.close();
			
			Log.v("", "output exists "+to.exists());
			
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
