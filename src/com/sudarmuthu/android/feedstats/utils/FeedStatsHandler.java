/**
 * Parse the response XML
 */
package com.sudarmuthu.android.feedstats.utils;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Parese the response XML from feedburner
 * 
 * @author "Sudar Muthu (sudarm@)"
 *
 */
public class FeedStatsHandler extends DefaultHandler {
    private Map<String, String> stats;
    private boolean isError = false;
    private String errorMsg;
    
    public FeedStatsHandler(Map<String, String> stats) {
    	this.setStats(stats);
    }
    
    @Override
    public void startDocument() throws SAXException {
        // Some sort of setting up work
    	Log.d(this.getClass().getSimpleName(), "XML Parsing started");
    } 
    
    @Override
    public void endDocument() throws SAXException {
        // Some sort of finishing up work
    	Log.d(this.getClass().getSimpleName(), "XML Parsing ended");    	
    } 
    
    @Override
    public void startElement(String namespaceURI, String localName, String qName, 
            Attributes atts) throws SAXException {
    	
        if (localName.equals("entry")) {
        	String date = atts.getValue("date");
        	String count = atts.getValue("circulation");
        	stats.put(date, count);
        	Log.d(this.getClass().getSimpleName(), date + ":" + count);
        } else if (localName.equals("rsp")) {
        	String result = atts.getValue("stat");
        	Log.d(this.getClass().getSimpleName(), "Response: " + result);
        	if (result.equalsIgnoreCase("fail")) {
        		isError = true;
        	}
        } else if (localName.equals("err")) {
        	isError = true;
        	errorMsg = atts.getValue("msg");
        }
    }

	/**
	 * @param stats the stats to set
	 */
	public void setStats(Map<String, String> stats) {
		this.stats = stats;
	}

	/**
	 * @return the stats
	 */
	public Map<String, String> getStats() {
		return stats;
	}

	/**
	 * @return
	 */
	public boolean isError() {
		return isError;
	}
	
	/**
	 * Get the error message
	 * @return
	 */
	public String getErrorMsg() {
		return errorMsg;
	}
}