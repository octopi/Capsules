package me.davidhu;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import praeda.muzikmekan.RestClient;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.CompactUser;
import fi.foyt.foursquare.api.entities.CompleteUser;
import fi.foyt.foursquare.api.entities.UserGroup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainMenuActivity extends Activity {

	private FoursquareApi fsq;
	private String fsqid;
	private String fsqName;

	private JSONObject userCapsules;

	private LinearLayout capsuleList;
	ArrayList<String> listItems = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmenu);

		/* FOURSQUARE INITIALIZATION */
		// get access token
		SharedPreferences settings = getSharedPreferences("OurCapsulesAuth", 0);
		String accessToken = settings.getString("accessToken", "");
		try {
			fsq = new FoursquareApi("F4CIIS2L1QKBNX4FUPXZ0XZHALSEX2YG43DYODQ4JDPHCH3U", "LSEGW52TJWSESUPZUAGGCA0WIHUU0RE55U1G0DLVQ51OP1IG", "http://young-spring-5136.herokuapp.com");
			fsq.setoAuthToken(accessToken);
			Result<CompleteUser> user = fsq.user("self");
			
			// write fsq data to sharedprefs if doesn't already exist
			if(settings.getString("fsqid", null) == null) {
				fsqid = user.getResult().getId();
				fsqName = user.getResult().getFirstName()+" "+user.getResult().getLastName();
				
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("fsqid", fsqid);
				editor.putString("fsqName", fsqName);
				editor.commit();
			} else {
				fsqid = settings.getString("fsqid", null);
				fsqName = settings.getString("fsqName", null);
			}
			
			String fsqFriends = "";
			UserGroup[] friendsList = user.getResult().getFriends().getGroups();
			for(int i=0;i<friendsList.length;i++) {
				Log.v("fsq", friendsList[i].getName()+" has "+friendsList[i].getItems().length+" friends");
			}
			

			// populate list of capsules
			new GetJSONTask().execute("http://young-spring-5136.herokuapp.com/get_capsules/"+fsqid); // populate userCapsules
			capsuleList = (LinearLayout) findViewById(R.id.capsuleList);

			/* CREATE NEW CAPSULE */
			((Button) findViewById(R.id.newCapsule)).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), SelectFriendsActivity.class);
					startActivity(intent);
				}
			});
		} catch (FoursquareApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public void goBack() {
		Intent intent = new Intent(this, HomescreenActivity.class);
		startActivity(intent);
	}

	/**
	 * For populating capsule list
	 * @author d
	 *
	 */
	private class GetJSONTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			RestClient client = new RestClient();
			JSONObject jsonResult = client.connect(params[0]);

			return jsonResult;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			try {
				Log.v(null, result.getJSONObject("0").getString("strKey"));
				userCapsules = result;

				JSONArray names = userCapsules.names();
				for(int i=0;i<names.length();i++) {
					final String thisStrKey = userCapsules.getJSONObject(names.getString(i)).getString("strKey");
					final String[] theseFiles;

					final JSONObject fileList = userCapsules.getJSONObject(names.getString(i)).getJSONObject("fileList");

					JSONObject members;
					members = userCapsules.getJSONObject(names.getString(i)).getJSONObject("members");
					JSONArray membersList = members.names();
					String theMembers = "";
					for(int j=0;j<membersList.length();j++) {
						String currMember = members.getJSONObject(membersList.getString(j)).getString("fsqName");
						Log.v("", "in capsule "+i+", member "+currMember);
						theMembers += currMember+" ";
					}


					// add current capsule to list
					Button but = new Button(getBaseContext());
					but.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
					but.setText("Capsule with "+theMembers);
					but.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(v.getContext(), UnlockCapsuleActivity.class);
							intent.putExtra("strKey", thisStrKey);
							intent.putExtra("fileList", fileList.toString());
							startActivity(intent);
						}
					});
					capsuleList.addView(but);


				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
