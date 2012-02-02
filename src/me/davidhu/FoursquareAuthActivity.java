package me.davidhu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class FoursquareAuthActivity extends Activity {
	/** Called when the activity is first created. */

	WebView foursquareWebView; 
	LinearLayout thisView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foursquare);

		foursquareWebView = (WebView) findViewById(R.id.foursquareWebView);
		foursquareWebView.getSettings().setJavaScriptEnabled(true);
		foursquareWebView.setWebViewClient(new FsqWebViewClient());
		foursquareWebView.loadUrl("https://foursquare.com/oauth2/authenticate?client_id=F4CIIS2L1QKBNX4FUPXZ0XZHALSEX2YG43DYODQ4JDPHCH3U&response_type=token&display=touch&redirect_uri=http://young-spring-5136.herokuapp.com/login");

	}

	private class FsqWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.v(null, "loaded URL: "+url);
			int atIndex = url.indexOf("access_token");
			
			// if we're at the page with the access token, grab it and move to the next activity
			if(atIndex > 0) {
				String accessToken = url.substring(url.indexOf("=")+1);
				Log.v("", "accesstoken: "+accessToken);
				SharedPreferences settings = getSharedPreferences("OurCapsulesAuth", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("accessToken", accessToken);
				editor.commit();
				
				Intent intent = new Intent(view.getContext(), MainMenuActivity.class);
				startActivity(intent);
			}
		}
	}
}