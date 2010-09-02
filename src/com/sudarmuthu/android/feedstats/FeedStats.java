package com.sudarmuthu.android.feedstats;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sudarmuthu.android.feedstats.utils.FeedStatsHandler;
import com.sudarmuthu.android.feedstats.utils.StatsGraphHandler;

/**
 * The main activity class
 * 
 * @author "Sudar Muthu"
 *
 */
public class FeedStats extends Activity {
    private EditText tFeedUrl;
	private Context  mContext;
	private Map<String, String> mStats = new HashMap<String, String>();
	private StatsGraphHandler mGraphHandler;
	
	private static final String FEEDBURNER_API_URL = "https://feedburner.google.com/awareness/1.0/GetFeedData?uri=";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tFeedUrl = (EditText) findViewById(R.id.feedUrl);
        mContext = this;
        
        tFeedUrl.setText("http://feeds.feedburner.com/SudarBlogs"); //for debugging
        
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
				
				//Add date query
				
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, -1); //we should start with previous day
				String endDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE);
				c.add(Calendar.DATE, -30);
				String startDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE);
				
				// get data from feedburner
				try {
					URL url = new URL(FEEDBURNER_API_URL + feedUrl + "&dates=" + startDate + "," + endDate);
					
					/* Get a SAXParser from the SAXPArserFactory. */
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser sp = spf.newSAXParser();

					/* Get the XMLReader of the SAXParser we created. */
					XMLReader xr = sp.getXMLReader();
					/* Create a new ContentHandler and apply it to the XML-Reader*/
					FeedStatsHandler feedStatsHandler = new FeedStatsHandler(mStats);
					xr.setContentHandler(feedStatsHandler);

					/* Parse the xml-data from our URL. */
					xr.parse(new InputSource(url.openStream()));
					/* Parsing has finished. */ 
					
					mStats = feedStatsHandler.getStats();
					Log.d(this.getClass().getSimpleName(), mStats.size() + "");
					
				} catch (MalformedURLException e) {
					handleError(e);
				} catch (IOException e) {
					handleError(e);
				} catch (ParserConfigurationException e) {
					handleError(e);
				} catch (SAXException e) {
					handleError(e);
				}
				
				
				//Show the webview
		        WebView wv = (WebView) findViewById(R.id.wv1);
		        
		        mGraphHandler = new StatsGraphHandler(wv, mStats);
		        
		        wv.getSettings().setJavaScriptEnabled(true);
		        wv.addJavascriptInterface(mGraphHandler, "testhandler");
		        wv.loadUrl("file:///android_asset/flot/stats_graph.html");
		        
			}
		});
    }

    /**
     * When the activity is resumed
     */
	@Override
	protected void onResume() {
		super.onResume();
//		mGraphHandler.loadGraph();
	}

    
	/**
	 * @param e
	 */
	private void handleError(Exception e) {
		e.printStackTrace();
		Toast.makeText(mContext, mContext.getResources().getString(R.string.error) + "-" + e.getMessage(), Toast.LENGTH_LONG);
	}
}