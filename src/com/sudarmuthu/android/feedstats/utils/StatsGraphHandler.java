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

/**
 * Graph Handler
 */
package com.sudarmuthu.android.feedstats.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
	public void loadGraph() {
		JSONArray data = new JSONArray();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		Object[] key = mStats.keySet().toArray();
		Arrays.sort(key); // keys should be sorted, otherwise the graph will not work properly.
		
		for (int i =0; i < key.length; i++) {
			JSONArray entry = new JSONArray();
			
			try {
				entry.put(formatter.parse(key[i] + " 00:00:00").getTime());
			} catch (ParseException e) {
				Log.d(this.getClass().getSimpleName(), "Some problem in parsing dates");
				e.printStackTrace();
			}
			
			entry.put(Integer.parseInt(mStats.get(key[i])));
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
			Log.d(this.getClass().getSimpleName(), arr.toString());
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