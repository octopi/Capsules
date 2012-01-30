package me.davidhu;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomescreenActivity extends Activity {
	/** Called when the activity is first created. */
	
	Facebook facebook;
	String FILENAME = "AndroidSSO_data";
    private SharedPreferences mPrefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homescreen);
		
		facebook = new Facebook("372475036103410");
		
		/*
         * Get existing access_token if any
         */
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
            advance();
        }
		
		((Button) findViewById(R.id.passThru)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				facebook.authorize(HomescreenActivity.this, new DialogListener() {
		            @Override
		            public void onComplete(Bundle values) {
		            	SharedPreferences.Editor editor = mPrefs.edit();
	                    editor.putString("access_token", facebook.getAccessToken());
	                    editor.putLong("access_expires", facebook.getAccessExpires());
	                    editor.commit();
		            	advance();
		            }

		            @Override
		            public void onCancel() {}

					@Override
					public void onFacebookError(FacebookError e) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onError(DialogError e) {
						// TODO Auto-generated method stub
						
					}
		        });
			}
		});
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
	
	private void advance() {
		Intent intent = new Intent(this, DevFestActivity.class);
		startActivity(intent);
	}
}
