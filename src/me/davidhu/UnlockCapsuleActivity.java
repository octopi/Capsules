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
				CapsuleCipher.doCipher(CapsuleCipher.DECRYPT, strKey, currFile, new File("/mnt/sdcard/Android/data/me.davidhu/files/outdecrypted"+i+".jpg"));
				//encrypted.delete();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
