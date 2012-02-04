package me.davidhu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import fi.foyt.foursquare.api.FoursquareApi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class HomescreenActivity extends Activity {
	/** Called when the activity is first created. */
	
	WebView foursquareWebView; 
	RelativeLayout thisView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homescreen);
		thisView = (RelativeLayout) findViewById(R.id.mainLayout);
		
		// check to see if already logged in before and auto-advance
		SharedPreferences settings = getSharedPreferences("OurCapsulesAuth", 0);
		if(settings.getString("accessToken", null) != null) {
			Intent intent = new Intent(this, MainMenuActivity.class);
			startActivity(intent);
		}
		
		((Button) findViewById(R.id.passThru)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), FoursquareAuthActivity.class);
				startActivity(intent);
			}
		});
	}

}