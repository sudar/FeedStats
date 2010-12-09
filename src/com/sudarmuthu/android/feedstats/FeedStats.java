/*  Copyright 2010  Sudar Muthu  (email : sudar@sudarmuthu.com)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License, version 2, as
    published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
	static final int PROGRESS_DIALOG = 0;
	static final int ABOUT_DIALOG = 1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mFeedUrl = (EditText) findViewById(R.id.feedUrl);
        mGetButton = (Button) findViewById(R.id.getGraph);
        mContext = this;
        
//        mFeedUrl.setText("http://feeds.feedburner.com/SudarBlogs"); //for debugging
        
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
				
				//start the dialog
				showDialog(PROGRESS_DIALOG);
		        new GetStatsTask().execute(feedUrl);
			}
		});
    }

    /** Before the dialog is created
     * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
        case PROGRESS_DIALOG:
        	ProgressDialog dialog = ProgressDialog.show(mContext, "", getResources().getString(R.string.loading_msg), true);        	
            return dialog;
        case ABOUT_DIALOG:
        	AlertDialog.Builder builder;
        	Dialog dialog2;

        	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        	View layout = inflater.inflate(R.layout.about, (ViewGroup) findViewById(R.id.layout_root));

			builder = new AlertDialog.Builder(mContext);
			builder.setView(layout);
			builder.setMessage("")
		       .setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
			
			dialog2 = builder.create();
			
			View projectUrl = layout.findViewById(R.id.project_url);
			projectUrl.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//When the project url is clicked
					Uri uri = Uri.parse(getString(R.string.about_project_url));
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}
			});
			
			return dialog2;
        default:
            return null;
        }
	}

	/**
	 * Create options menu
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * When the menu item is selected
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			displayAboutBox();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
     * When the activity is resumed
     */
	@Override
	protected void onResume() {
		super.onResume();
		//TODO: Need to handle screen orientation changes properly.
	}

	/**
	 * Display About box
	 */
    protected void displayAboutBox() {
    	showDialog(ABOUT_DIALOG);
//        startActivity(new Intent(this, About.class));
    }
    
	/**
	 * Task to fetch and parse feeds
	 * 
	 * @author "Sudar Muthu"
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
			dismissDialog(PROGRESS_DIALOG);			
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