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
import android.os.AsyncTask;
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
    private EditText mFeedUrl;
    private Button mGetButton;
    
	private Context  mContext;
	private StatsGraphHandler mGraphHandler;
	
	private static final String FEEDBURNER_API_URL = "https://feedburner.google.com/awareness/1.0/GetFeedData?uri=";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mFeedUrl = (EditText) findViewById(R.id.feedUrl);
        mGetButton = (Button) findViewById(R.id.getGraph);
        mContext = this;
        
        mFeedUrl.setText("http://feeds.feedburner.com/SudarBlogs"); //for debugging
        
        Button getGraph = (Button) findViewById(R.id.getGraph);
        getGraph.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//get the feed url and validate
				
				String feedUrl = mFeedUrl.getText().toString();
				if (feedUrl == null || feedUrl.equals("http://") || feedUrl.equals("")) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.feed_url_empty), Toast.LENGTH_SHORT).show();
					return;
				}
				
		        new GetStatsTask().execute(feedUrl);
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
	 * Task to fetch and parse feeds
	 * 
	 * @author "Sudar Muthu (sudarm@)"
	 *
	 */
	private class GetStatsTask extends AsyncTask<String, Void, Map<String, String>> {
		private String errorMsg;
		
		/**
		 * Before the task is started
		 */
		@Override
		protected void onPreExecute() {
			mGetButton.setEnabled(false);
		}

		/**
		 * Start the background process
		 */
		protected Map<String, String> doInBackground(String... feedUrl) {
			Map<String, String> stats = new HashMap<String, String>();
			FeedStatsHandler feedStatsHandler = new FeedStatsHandler(stats);
			
			//Add date query
			
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -1); //we should start with previous day
			String endDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE);
			c.add(Calendar.DATE, -30);
			String startDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE);
			
			// get data from feedburner
			try {
				URL url = new URL(FEEDBURNER_API_URL + feedUrl[0] + "&dates=" + startDate + "," + endDate);
				
				/* Get a SAXParser from the SAXPArserFactory. */
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();

				/* Get the XMLReader of the SAXParser we created. */
				XMLReader xr = sp.getXMLReader();
				/* Create a new ContentHandler and apply it to the XML-Reader*/
				xr.setContentHandler(feedStatsHandler);

				/* Parse the xml-data from our URL. */
				xr.parse(new InputSource(url.openStream()));
				/* Parsing has finished. */ 
				
				if (!feedStatsHandler.isError()) {
					stats = feedStatsHandler.getStats();
				} else {
					errorMsg = feedStatsHandler.getErrorMsg();
					stats = null;
				}
				
			} catch (MalformedURLException e) {
				handleError(e);
			} catch (IOException e) {
				handleError(e);
			} catch (ParserConfigurationException e) {
				handleError(e);
			} catch (SAXException e) {
				handleError(e);
			} catch (Exception e) {
				handleError(e);
			}
			
			return stats;
		}

		/**
		 * When the background process is complete
		 */
		protected void onPostExecute(Map<String, String> stats) {
			if (stats != null && stats.size() > 0) {

				// Show the webview
				WebView wv = (WebView) findViewById(R.id.wv1);

				mGraphHandler = new StatsGraphHandler(wv, stats);

				wv.getSettings().setJavaScriptEnabled(true);
				wv.addJavascriptInterface(mGraphHandler, "testhandler");
				wv.loadUrl("file:///android_asset/flot/stats_graph.html");
			} else {
				//show error message
				Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG).show();
			}
			mGetButton.setEnabled(true);			
		}
		
		/**
		 * Print Error message
		 * 
		 * @param e
		 */
		private void handleError(Exception e) {
			Log.d(this.getClass().getSimpleName(), "Caught some exceiton");
			e.printStackTrace();
			Toast.makeText(mContext, mContext.getResources().getString(R.string.error) + "-" + e.getMessage(), Toast.LENGTH_LONG);
			errorMsg = e.getMessage();
		}
	}
}