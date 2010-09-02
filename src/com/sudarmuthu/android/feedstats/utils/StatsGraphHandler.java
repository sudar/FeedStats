/**
 * Graph Handler
 */
package com.sudarmuthu.android.feedstats.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.webkit.WebView;

/**
 * Handles data between JavaWorld and JavaScript World 
 * 
 * @author "Sudar Muthu (http://sudarmuthu.com)"
 *
 */
public class StatsGraphHandler {
	private WebView mAppView;
	private Map<String, String> mStats;
	
	public StatsGraphHandler(WebView appView, Map<String, String> stats) {
		mAppView = appView;
		mStats = stats;
	}
	
	/**
	 * Set the title of the graph
	 * 
	 * @return
	 */
	public String getGraphTitle() {
		//TODO: Move it to string.xml
		return "Your Feed stats";
	}

	/**
	 * Load the default graph
	 */
	@SuppressWarnings("unchecked")
	public void loadGraph() {
		JSONArray data = new JSONArray();
		
		Iterator it = mStats.entrySet().iterator();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		while (it.hasNext()) {
			JSONArray entry = new JSONArray();
			Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();
			
			try {
				entry.put(formatter.parse(pairs.getKey() + " 00:00:00").getTime());
			} catch (ParseException e) {
				Log.d(this.getClass().getSimpleName(), "Some problem in parsing dates");
				e.printStackTrace();
			}
			
			entry.put(Integer.parseInt(pairs.getValue()));
			data.put(entry);
		}
		
		loadGraph(data);
	}
	
	/**
	 * Load Graph data
	 */
	private void loadGraph(JSONArray data) {
		JSONArray arr = new JSONArray();

		JSONObject result = new JSONObject();
			 try {
				result.put("data", data);//will ultimately look like: {"data": p[x1,y1],[x2,y2],[x3,y3],[]....]},
				result.put("lines", getLineOptionsJSON()); // { "lines": { "show" : true }},
				result.put("points", getPointOptionsJSON()); // { "points": { "show" : true }}
			} catch (JSONException e) {
				Log.d(this.getClass().getSimpleName(), "Got an exception while trying to parse JSON");
				e.printStackTrace();
			} 
			arr.put(result);
			
		// return arr.toString(); //This _WILL_ return the data in a good looking JSON string, but if you pass it straight into the Flot Plot method, it will not work!
		mAppView.loadUrl("javascript:GotGraph(" + arr.toString() + ")"); // this callback works!
	}

	/**
	 * Get Points action
	 * @return
	 */
	private JSONArray getPointOptionsJSON() {
		JSONArray pointOption = new JSONArray();
		pointOption.put("show");
		pointOption.put(true);
		
		return pointOption;		
	}

	/**
	 * Get Lines option
	 * 
	 * @return
	 */
	private JSONArray getLineOptionsJSON() {
		JSONArray lineOption = new JSONArray();
		lineOption.put("show");
		lineOption.put(true);
		
		return lineOption;
	}
}