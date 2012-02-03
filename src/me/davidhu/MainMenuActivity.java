package me.davidhu;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import praeda.muzikmekan.RestClient;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CompactUser;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.CompleteUser;
import fi.foyt.foursquare.api.entities.HereNow;
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
	private CompleteUser user;
	private String fsqid;
	private String fsqName;
	private ArrayList<CompactUser> friendsHerenow;
	private CompactVenue userCurrVenue;

	private JSONObject userCapsules;

	private LinearLayout capsuleList;

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
			user = fsq.user("self").getResult();
			userCurrVenue = user.getCheckins().getItems()[0].getVenue();

			// write fsq data to sharedprefs if doesn't already exist
			if(settings.getString("fsqid", null) == null) {
				fsqid = user.getId();
				fsqName = user.getFirstName()+" "+user.getLastName();

				SharedPreferences.Editor editor = settings.edit();
				editor.putString("fsqid", fsqid);
				editor.putString("fsqName", fsqName);
				editor.commit();
			} else {
				fsqid = settings.getString("fsqid", null);
				fsqName = settings.getString("fsqName", null);
			}

			// get fsq friends at the user's current venue
			friendsHerenow = new ArrayList<CompactUser>();
			Checkin[] herenowCkins = fsq.venue(user.getCheckins().getItems()[0].getVenue().getId()).getResult().getHereNow().getGroups()[0].getItems();
			for(int i=0;i<herenowCkins.length;i++) {
				friendsHerenow.add(herenowCkins[i].getUser());
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
			if(result != null) {
				try {
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
						boolean allHere = true;
						for(int j=0;j<membersList.length();j++) {
							String currMember = members.getJSONObject(membersList.getString(j)).getString("fsqName");
							Log.v("c", "comparing "+fsq.user(members.getJSONObject(membersList.getString(j)).getString("fsqid")).getResult().getCheckins().getItems()[0].getVenue().getName()+" with "+userCurrVenue.getName());
							if(!fsq.user(members.getJSONObject(membersList.getString(j)).getString("fsqid")).getResult().getCheckins().getItems()[0].getVenue().getId().equals(userCurrVenue.getId())) {
								allHere = false;
							}
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
						// disable capsule if not everyone is here
						// TODO: maintain two lists: capsules you can unlock and ones you can't
						if(!allHere) {
							but.setClickable(false);
							but.setEnabled(false);
						}
						capsuleList.addView(but);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FoursquareApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
}
