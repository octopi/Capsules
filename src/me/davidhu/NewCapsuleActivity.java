package me.davidhu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.CompactUser;
import fi.foyt.foursquare.api.entities.CompleteUser;
import fi.foyt.foursquare.api.entities.UserGroup;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewCapsuleActivity extends Activity {

	private static final String TAG = "DevFest";

	FoursquareApi fsq;
	String fsqid, fsqName;
	private ArrayList<File> capsuleFiles;
	private SharedPreferences mPrefs;
	private ArrayList<CompactUser> friendsList;
	
	private ArrayList<File> imageThumbs;
	private ImageAdapter mAdapter;

	Button createCapsule;
	GridView photoGrid; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newcapsule);
		
		/* UI INIT */
		imageThumbs = new ArrayList<File>();
		photoGrid = (GridView) findViewById(R.id.photoGrid);
		mAdapter = new ImageAdapter(this, imageThumbs);
		photoGrid.setAdapter(mAdapter);

		/* FOURSQUARE INITIALIZATION */
		// get access token
		mPrefs = getSharedPreferences("OurCapsulesAuth", 0);
		fsqid = mPrefs.getString("fsqid", null);
		fsqName = mPrefs.getString("fsqName", null);
		friendsList = (ArrayList<CompactUser>) getIntent().getSerializableExtra("friendsList");
		String instrString = "You are about to make a capsule with: ";
		for(int i=0;i<friendsList.size()-2;i++) { // offset 2 because user is always last and don't want to add him to string
			instrString+= friendsList.get(i).getFirstName()+", ";
		}
		if(friendsList.size() > 0) 
			instrString += friendsList.get(friendsList.size()-2).getFirstName();
		((TextView) findViewById(R.id.instructions)).setText(instrString);


		/* ADDING PICS */
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
		capsuleFiles = new ArrayList<File>();

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
							CapsuleCipher.doCipher(CapsuleCipher.ENCRYPT, strKey, origFile, newFile);
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
								
							String friendsObject = "{";
							CompactUser curr;
							for(int i=0;i<friendsList.size()-1;i++) {
								curr = friendsList.get(i);
								friendsObject += "\""+i+"\": {\"fsqid\": "+curr.getId()+", \"fsqName\": \""+curr.getFirstName()+"\"}, ";
							}
							curr = friendsList.get(friendsList.size()-1);
							friendsObject += "\""+(friendsList.size()-1)+"\": {\"fsqid\": "+curr.getId()+", \"fsqName\": \""+curr.getFirstName()+"\"}";
							friendsObject += "}";
							nameValuePairs.add(new BasicNameValuePair("members", friendsObject));
							nameValuePairs.add(new BasicNameValuePair("strKey", strKey));		
							nameValuePairs.add(new BasicNameValuePair("creator", fsqid));
							nameValuePairs.add(new BasicNameValuePair("fileList", capsuleFilesJson));
							nameValuePairs.add(new BasicNameValuePair("locked", "true"));
							httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

							// Execute HTTP Post Request
							HttpResponse response = httpclient.execute(httppost);
							Header[] responseHeaders = response.getAllHeaders();
							for(int i=0;i<responseHeaders.length;i++) {
								Log.v(TAG, responseHeaders[i].getName()+": "+responseHeaders[i].getValue());
							}
							

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


	}
	
	@Override
	/**
	 * Runs after we get a result from the image selector
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			if(requestCode == 1) {
				Uri selectedImageUri = data.getData();
				String oiString = selectedImageUri.getPath();
				String mediaString = getPath(selectedImageUri); // path via media gallery
				File origFile = new File(mediaString);
				
				capsuleFiles.add(origFile);	
				imageThumbs.add(origFile);
				mAdapter.notifyDataSetChanged();
				
			}
		}
	}
	
	/**
	 * Gets full path of a system URI
	 * @param uri
	 * @return
	 */
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
	
	/* GRID STUFF BELOW HERE */
	public class ImageAdapter extends BaseAdapter {
	    private Context mContext;
	    
	    private ArrayList<File> mThumbIds;

	    public ImageAdapter(Context c, ArrayList<File> thumbs) {
	        mContext = c;
	        mThumbIds = thumbs;
	    }

	    public int getCount() {
	        return mThumbIds.size();
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            imageView = new ImageView(mContext);
	            imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(10, 10, 10, 10);
	        } else {
	            imageView = (ImageView) convertView;
	        }

	        imageView.setImageBitmap(decodeFile(mThumbIds.get(position)));

	        return imageView;
	    }
	    
	  //decodes image and scales it to reduce memory consumption
	    private Bitmap decodeFile(File f){
	        try {
	            //Decode image size
	            BitmapFactory.Options o = new BitmapFactory.Options();
	            o.inJustDecodeBounds = true;
	            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	            //The new size we want to scale to
	            final int REQUIRED_SIZE=70;

	            //Find the correct scale value. It should be the power of 2.
	            int scale=1;
	            while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	                scale*=2;

	            //Decode with inSampleSize
	            BitmapFactory.Options o2 = new BitmapFactory.Options();
	            o2.inSampleSize=scale;
	            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	        } catch (FileNotFoundException e) {}
	        return null;
	    }

	}
	
}
