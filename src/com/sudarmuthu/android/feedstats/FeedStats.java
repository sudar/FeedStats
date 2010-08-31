package com.sudarmuthu.android.feedstats;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The main activity class
 * 
 * @author "Sudar Muthu"
 *
 */
public class FeedStats extends Activity {
    private EditText tFeedUrl;
	private Context  mContext;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tFeedUrl = (EditText) findViewById(R.id.feedUrl);
        mContext = this;
        
        Button getGraph = (Button) findViewById(R.id.getGraph);
        getGraph.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//get the feed url and validate
				
				String feedUrl = tFeedUrl.getText().toString();
				if (feedUrl == null || feedUrl.equals("http://") || feedUrl.equals("")) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.feed_url_empty), Toast.LENGTH_SHORT).show();
					return;
				}
				
				// get data from feedburner
				
				
				//Show the webview
			}
		});
    }
}