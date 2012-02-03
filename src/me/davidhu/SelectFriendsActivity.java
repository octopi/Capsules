package me.davidhu;

import java.util.ArrayList;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CheckinGroup;
import fi.foyt.foursquare.api.entities.CompactUser;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.CompleteUser;
import fi.foyt.foursquare.api.entities.HereNow;
import fi.foyt.foursquare.api.entities.UserGroup;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class SelectFriendsActivity extends Activity {

	private FoursquareApi fsq; 
	ArrayList<CompactUser> friendsHereList;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectfriends);

		LinearLayout friendsPane = (LinearLayout) findViewById(R.id.friendsPane);
		Button selectFriendsButton = (Button) findViewById(R.id.selectFriendsButton);

		friendsHereList = new ArrayList<CompactUser>();

		// set up fsq
		SharedPreferences settings = getSharedPreferences("OurCapsulesAuth", 0);
		String accessToken = settings.getString("accessToken", "");
		fsq = new FoursquareApi("F4CIIS2L1QKBNX4FUPXZ0XZHALSEX2YG43DYODQ4JDPHCH3U", "LSEGW52TJWSESUPZUAGGCA0WIHUU0RE55U1G0DLVQ51OP1IG", "http://young-spring-5136.herokuapp.com");
		Log.v("fsq", "using oauthtoken "+accessToken);
		fsq.setoAuthToken(accessToken);

		// get friends who are here now and add to view
		try {
			CompleteUser user = fsq.user("self").getResult();
			CompactVenue venue = user.getCheckins().getItems()[0].getVenue();
			Checkin[] herenow = fsq.venue(venue.getId()).getResult().getHereNow().getGroups()[0].getItems(); // needed for some reason... (prob CompleteVenue a subclass of CompactVenue). 0 returns friends list
			Log.v("4sq", "herenow at: "+venue.getName()+" ("+venue.getId()+") "+venue.getHereNow());
			for(int i=0;i<herenow.length;i++) {
				final CompactUser currUser = herenow[i].getUser();

				final CheckBox cb = new CheckBox(this);
				cb.setText(herenow[i].getUser().getFirstName());
				friendsPane.addView(cb);

				cb.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if(cb.isChecked())
							friendsHereList.add(currUser);
						else
							friendsHereList.remove(currUser);
					}
				});
			}
			
			// dummy data for testing... Sid will always show up
			final CompactUser dummy = fsq.user("4372968").getResult();
			CheckBox cb = new CheckBox(this);
			cb.setText(dummy.getFirstName());
			friendsPane.addView(cb);

			cb.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					friendsHereList.add(dummy);
				}
			});

		} catch (FoursquareApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		selectFriendsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent(v.getContext(), NewCapsuleActivity.class);
					// add current user to the list of people that need to check in to unlock
					friendsHereList.add(fsq.user("self").getResult());
					intent.putExtra("friendsList", friendsHereList);
					startActivity(intent);
				} catch (FoursquareApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
