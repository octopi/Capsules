package me.davidhu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
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

	public final static int ENCRYPT = 1;
	public final static int DECRYPT = 0;

	public static boolean doCipher(int mode, String strKey, File from, File to) {
		try {
			byte[] byteKey = Base64.decode(strKey, Base64.DEFAULT); 
			SecretKey key = new SecretKeySpec(byteKey, "DES");;
			Log.v("capsules", "cipher init");

			FileInputStream in = new FileInputStream(from);
			FileOutputStream target = new FileOutputStream(to);
			/*byte[] plainData = new byte[(int)from.length()];
			in.read(plainData);

			Log.v("capsules", "cipher before");
			//byte[] encryptedData = cipher.doFinal(plainData);*/

			Log.v("capsules", "cipher before");
			if(mode == ENCRYPT) {
				byte[] plainData = readFile(from);
				byte[] encryptedData = new byte[plainData.length+2];
				encryptedData[0] = 127;
				encryptedData[1] = 127;
				for(int i=2;i<plainData.length+2;i++) {
					encryptedData[i] = plainData[i-2];
				}

				target.write(encryptedData);
				target.close();
			} else if(mode == DECRYPT) {
				byte[] plainData = CapsuleCipher.readFile(from);
				byte[] decryptedData = new byte[plainData.length+2];
				Log.v("capsules", "cipher before");
				for(int i=2;i<plainData.length;i++) {
					decryptedData[i-2] = plainData[i];
				}

				target.write(decryptedData);
				target.close();
			}

			Log.v("capsules", "cipher after");


			Log.v("capsules", "output exists "+to.exists());

			return to.exists();
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return false;
	}

	public static byte[] readFile (File file) throws IOException {
		// Open file
		RandomAccessFile f = new RandomAccessFile(file, "r");

		try {
			// Get and check length
			long longlength = f.length();
			int length = (int) longlength;
			if (length != longlength) throw new IOException("File size >= 2 GB");

			// Read file and return data
			byte[] data = new byte[length];
			f.readFully(data);
			return data;
		}
		finally {
			f.close();
		}
	}
}
