package me.davidhu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import fi.foyt.foursquare.api.entities.CompactUser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DevFestActivity extends Activity {

	private static final String TAG = "DevFest";
	
	private ArrayList<File> capsuleFiles;
	private String fsqid;
	private String fsqName;
	
	private SharedPreferences mPrefs;
	
	private ArrayList<CompactUser> friendsList;
	
	TextView capsuleList;
	Button createCapsule;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		capsuleFiles = new ArrayList<File>();
		
		/* FOURSAUARE INITIALIZATION */
		
        mPrefs = getSharedPreferences("OurCapsulesAuth", 0);
        fsqid = mPrefs.getString("fsqid", null);
        fsqName = mPrefs.getString("fsqName", null);
        friendsList = (ArrayList<CompactUser>) getIntent().getSerializableExtra("friendsList");
        String instrString = "You are about to make a capsule with: ";
        for(int i=0;i<friendsList.size()-1;i++) {
        	instrString+= friendsList.get(i).getFirstName()+", ";
        }
        if(friendsList.size() > 0) 
        	instrString += friendsList.get(friendsList.size()-1).getFirstName();
        ((TextView) findViewById(R.id.instructions)).setText(instrString);
        
		
		/* ADD PICS */
		Button addPic = (Button) findViewById(R.id.addPicButton);
		addPic.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
			}
		});
		
		/* CREATE NEW CAPSULE */
		createCapsule = (Button) findViewById(R.id.createCapsuleButton);
		createCapsule.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				File sdDir = Environment.getExternalStorageDirectory();
				
				// making sure these directories exist (first run)
				String[] makeTheseDirs = {sdDir.toString()+"/Android/data/me.davidhu", sdDir.toString()+"/Android/data/me.davidhu/files"};
				File destDir;
				for(int i=0;i<makeTheseDirs.length;i++) {
					destDir = new File(makeTheseDirs[i]);
					if(!destDir.exists())
						destDir.mkdir();
				}

				destDir = new File(makeTheseDirs[makeTheseDirs.length-1]);

				if(destDir.exists()) { 		   					
					// write encrypted files & delete originals
					SecretKey key;
					try {
						key = KeyGenerator.getInstance("DES").generateKey();
						String strKey = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
						Log.v(TAG, "key: "+strKey);
						
						String capsuleFilesJson = "{ "; // create a JSON string for capsule files... used later
						for(int i=0;i<capsuleFiles.size();i++) {
							File origFile = capsuleFiles.get(i);
							File newFile = new File(destDir+"/"+origFile.getName());
							doCipher(Cipher.ENCRYPT_MODE, strKey, origFile, newFile);
							//origFile.delete();
							capsuleFilesJson += "\""+i+"\": \""+newFile.getAbsolutePath()+"\"";
							if(i < capsuleFiles.size()-1) {
								capsuleFilesJson += ", ";
							}
						}
						capsuleFilesJson += "}";
						
						// create new capsule object in database via HTTP post request to web service
						HttpClient httpclient = new DefaultHttpClient();
					    HttpPost httppost = new HttpPost("http://young-spring-5136.herokuapp.com/new_capsule");

					    try {
					        // Add your data
					        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					        nameValuePairs.add(new BasicNameValuePair("strKey", strKey));
					        
					        String friendsObject = "{";
					        CompactUser curr;
					        for(int i=0;i<friendsList.size()-1;i++) {
					        	curr = friendsList.get(i);
					        	friendsObject += "{\""+i+"\": {\"fsqid\": "+curr.getId()+", \"fsqName\": \""+curr.getFirstName()+"\"}, ";
					        }
					        nameValuePairs.add(new BasicNameValuePair("members", "{\"0\": {\"fsqid\": "+fsqid+", \"fsqName\": \""+fsqName+"\"}, \"1\": { \"fsqid\": 123, \"fsqName\": \"Test person "+Math.floor(Math.random()*20)+"\" } }"));
					        nameValuePairs.add(new BasicNameValuePair("fileList", capsuleFilesJson));
					        nameValuePairs.add(new BasicNameValuePair("locked", "true"));
					        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					        // Execute HTTP Post Request
					        HttpResponse response = httpclient.execute(httppost);
					        Header[] responseHeaders = response.getAllHeaders();
					        for(int i=0;i<responseHeaders.length;i++) {
					        	Log.v(TAG, responseHeaders[i].getName()+": "+responseHeaders[i].getValue());
					        }
					        
					        capsuleList.setText(capsuleList.getText()+"\n Capsule created!");	
					        
					    } catch (ClientProtocolException e) {
					        // TODO Auto-generated catch block
					    } catch (IOException e) {
					        // TODO Auto-generated catch block
					    }
						
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		});
		
		/* FOURSQUARE LOGOUT */
		((Button) findViewById(R.id.fbLogout)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO
			}
		});
		
		
		// decryption test
		((Button) findViewById(R.id.decryptButton)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				File encrypted = new File("/mnt/sdcard/Android/data/me.davidhu/files/outencrypted.jpg");
				doCipher(Cipher.DECRYPT_MODE, "sMut4EDWNFg=", encrypted, new File("/mnt/sdcard/Android/data/me.davidhu/files/outdecrypted.jpg"));
				encrypted.delete();
				Log.v(TAG, "encrypted exists? "+encrypted.exists());
			}
		});
		
		capsuleList = (TextView) findViewById(R.id.capsuleFiles);
		

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			if(requestCode == 1) {
				Uri selectedImageUri = data.getData();
				String oiString = selectedImageUri.getPath();
				String mediaString = getPath(selectedImageUri); // path via media gallery
				File origFile = new File(mediaString);
				
				capsuleFiles.add(origFile);
				capsuleList.setText(capsuleList.getText()+"\n"+origFile.getName());				
			}
		}
	}

	/**
	 * Actually does the encryption or decryption. Creates copies and doesn't move original file
	 * @param mode
	 * @param strKey
	 * @param from
	 * @param to
	 * @return
	 */
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
			Log.v(TAG, "starting cipher...");
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(mode, key);
			
			FileInputStream in = new FileInputStream(from);
			byte[] plainData = new byte[(int)from.length()];
			in.read(plainData);
			
			byte[] encryptedData = cipher.doFinal(plainData);
			FileOutputStream target = new FileOutputStream(to);
			target.write(encryptedData);
			target.close();
			
			Log.v(TAG, "output exists "+to.exists());
			
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
	
	public void goBack() {
		Intent intent = new Intent(this, HomescreenActivity.class);
		startActivity(intent);
	}
	
	public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }	
}