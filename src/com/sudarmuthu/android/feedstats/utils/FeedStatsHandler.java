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
    StringBuffer buff = null;
    boolean buffering = false;
    private Map<String, String> stats;
    
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
    
//    @Override
//    public void characters(char ch[], int start, int length) {
//        if(buffering) {
//            buff.append(ch, start, length);
//        }
//    } 
//    
//    @Override
//    public void endElement(String namespaceURI, String localName, String qName) 
//    throws SAXException {
//        if (localName.equals("entry")) {
//            buffering = false; 
//            String content = buff.toString();
//            
//            // Do something with the full text content that we've just parsed
//        }
//    }	
}