package org.linphone.tutorials;

import org.linphone.LinphoneActivity;
import org.linphone.LinphoneLauncherActivity;
import org.linphone.LinphoneService;
import org.linphone.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class LiveHDTutorialActivity extends Activity implements OnClickListener{
	 int[] myImageId = { 
	         R.drawable.page1,
	         R.drawable.page2,
	         R.drawable.page3,
	     };
         
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.livehd_tutorial_main);
		LinearLayout tutorialContainer = (LinearLayout) findViewById(R.id.tutorialContainer);
        TutorialContainerFact tutorialCreator = new TutorialContainerFact(getWinWidth(), getWinHeight());
        tutorialContainer.addView(tutorialCreator.createTutorialContainerView(getApplicationContext(), myImageId, this));
	}
    private int getWinWidth(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
    private int getWinHeight(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.layout.livehd_tutorial_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		final Class<? extends Activity> classToStart;
		switch(v.getId()) {
			case R.id.tutorialOkBtn:
				classToStart = LinphoneActivity.class;
				LinphoneService.instance().setActivityToLaunchOnIncomingReceived(classToStart);
				startActivity(new Intent().setClass(LiveHDTutorialActivity.this, classToStart).setData(getIntent().getData()));
				finish();
				break;
		}
	}
}
