package com.github.mob41.gotowhere.transitdb.build.impl.hktransit;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.mob41.gotowhere.transitdb.build.TransitDatabaseBuilder;
import com.github.mob41.gotowhere.transitdb.db.AddressKey;
import com.github.mob41.gotowhere.transitdb.db.RouteNameKey;
import com.github.mob41.gotowhere.transitdb.db.StopNameKey;
import com.github.mob41.gotowhere.transitdb.db.TransitRoute;
import com.github.mob41.gotowhere.transitdb.db.TransitStop;
import com.github.mob41.gotowhere.transitdb.db.TransitType;

public class HkTransitDatabaseBuilder extends TransitDatabaseBuilder {
	
	private static final String COMPANY_CODE_XML = "http://static.data.gov.hk/td/routes-fares-xml/COMPANY_CODE.xml";
	
	private static final String ROUTE_BUS_XML = "http://static.data.gov.hk/td/routes-fares-xml/ROUTE_BUS.xml";
	
	private static final String RSTOP_BUS_XML = "http://static.data.gov.hk/td/routes-fares-xml/RSTOP_BUS.xml";
	
	private static final String STOP_BUS_XML = "http://static.data.gov.hk/td/routes-fares-xml/STOP_BUS.xml";

	public HkTransitDatabaseBuilder(TransitType transitType, String providerName) {
		super(transitType, providerName, "hktransit_" + providerName);
		setSharedDownloadedKey("hktransit");
	}
	
	public static List<HkTransitDatabaseBuilder> getHkTransitBuilders() throws Exception {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    
		URL url = new URL(COMPANY_CODE_XML);
		URLConnection conn = url.openConnection();
		InputStream in = conn.getInputStream();
	    
	    Document doc = builder.parse(in);
	    in.close();
	    in = null;
	    
	    NodeList list = doc.getElementsByTagName("COMPANY_CODE");
	    List<HkTransitDatabaseBuilder> builders = new ArrayList<HkTransitDatabaseBuilder>();
	    
	    Node node;
	    TransitType type;
	    String companyCode;
	    for (int i = 0; i < list.getLength(); i++) {
	    	node = list.item(i);
	    	if (node.getChildNodes().getLength() < 2) {
	    		continue;
	    	}
	    	companyCode = getValue(node, "COMPANY_CODE");
	    	if (companyCode.equals("TRAM") || companyCode.equals("PTRAM")) {
    			type = TransitType.TRAM;
	    	} else if (companyCode.equals("GMB")) {
    			type = TransitType.MINIBUS;
	    	} else if (companyCode.equals("FERRY")) {
    			type = TransitType.FERRY;
	    	} else {
    			type = TransitType.BUS;
	    	}
	    	builders.add(new HkTransitDatabaseBuilder(type, companyCode));
	    }
	    
	    return builders;
	}
	
	public static String getStringFromDocument(Document doc)
	{
	    try
	    {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
	    }
	} 

	@Override
	public boolean build() throws Exception {
		int reqCount = 0;
		URL url;
		URLConnection conn;
		FileOutputStream fileOut;
		PrintWriter fileWriter;
		FileInputStream fileIn;
		InputStream in;
		String fileKey;
	    
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    
	    //Bus routes
		reportMessage("Downloading bus routes...");
	    
		fileKey = "ROUTE_BUS.xml";
	    fileIn = readDownloaded(fileKey);
	    if (fileIn == null) {
			reqCount++;
			url = new URL(ROUTE_BUS_XML);
		    conn = url.openConnection();
	    	in = conn.getInputStream();
	    } else {
	    	in = fileIn;
	    }
	    
	    Document doc = builder.parse(in);
	    in.close();
	    in = null;
	    
	    if (fileIn == null) {
		    fileOut = writeDownloaded(fileKey);
			fileWriter = new PrintWriter(new OutputStreamWriter(fileOut, StandardCharsets.UTF_8));
			fileWriter.println(getStringFromDocument(doc));
			fileWriter.flush();
			fileWriter.close();
			fileOut.close();
	    }
	    
	    List<Node> routeNodes = new ArrayList<Node>();
	    NodeList list = doc.getElementsByTagName("ROUTE");
	    Node listNode;
	    for (int i = 0; i < list.getLength(); i++) {
			reportMessage("Processing routes... " + (i + 1) + "/" + list.getLength());
	    	listNode = list.item(i);
	    	if (getValue(listNode, "COMPANY_CODE").equals(providerName)) {
		    	routeNodes.add(list.item(i));
	    	}
			reportProgress((int) ((i + 1) / (float) list.getLength() * 100.0 / 3));
	    }
	    
	    //Route stops
		reportMessage("Downloading route stops...");
	    
	    fileKey = "RSTOP_BUS.xml";
	    fileIn = readDownloaded(fileKey);
	    if (fileIn == null) {
			reqCount++;
			url = new URL(RSTOP_BUS_XML);
		    conn = url.openConnection();
	    	in = conn.getInputStream();
	    } else {
	    	in = fileIn;
	    }
	    
	    doc = builder.parse(in);
	    in.close();
	    in = null;
	    
	    if (fileIn == null) {
		    fileOut = writeDownloaded(fileKey);
			fileWriter = new PrintWriter(new OutputStreamWriter(fileOut, StandardCharsets.UTF_8));
			fileWriter.println(getStringFromDocument(doc));
			fileWriter.flush();
			fileWriter.close();
			fileOut.close();
	    }
	    
	    String routeId;
	    String companyCode;
	    int i;
    	int j;
    	List<Node> boundList;
    	String[] path;
    	RouteNameKey routeNameKey;
	    list = doc.getElementsByTagName("RSTOP");
	    List<List<Node>> bounds;
	    String stopId;
	    List<String> stopIds = new ArrayList<String>();
	    List<Node> stopNodes = new ArrayList<Node>();
	    int x;
	    Node routeNode;
	    for (x = 0; x < routeNodes.size(); x++) {
	    	routeNode = routeNodes.get(x);
			reportMessage("Processing route stops... " + (x + 1) + "/" + routeNodes.size());
	    	bounds = new ArrayList<List<Node>>();
			bounds.add(new ArrayList<Node>());
	    	routeId = getValue(routeNode, "ROUTE_ID");
	    	companyCode = getValue(routeNode, "COMPANY_CODE");
	    	for (i = 0; i < list.getLength(); i++) {
	    		listNode = list.item(i);
	    		if (getValue(listNode, "ROUTE_ID").equals(routeId)) {
	    			if (getValue(listNode, "ROUTE_SEQ").equals("2")) {
	    				if (bounds.size() < 2) {
	    					bounds.add(new ArrayList<Node>());
	    				}
	    				bounds.get(1).add(listNode);
	    			} else {
	    				bounds.get(0).add(listNode);
	    			}
	    			
	    			stopId = getValue(listNode, "STOP_ID");
	    			if (!stopIds.contains(stopId)) {
	    				stopIds.add(stopId);
		    			stopNodes.add(listNode);
	    			}
	    		}
	    	}
	    	
	    	for (List<Node> bound : bounds) {
	    		Collections.sort(bound, (a, b) -> {
	    			return Integer.parseInt(getValue(a, "STOP_SEQ")) - Integer.parseInt(getValue(b, "STOP_SEQ"));
	    		});
	    	}
	    	
	    	String[][] paths = new String[bounds.size()][];
	    	for (i = 0; i < paths.length; i++) {
	    		boundList = bounds.get(i);
	    		path = new String[boundList.size()];
	    		for (j = 0; j < path.length; j++) {
	    			path[j] = getValue(boundList.get(j), "STOP_ID");
	    		}
	    		paths[i] = path;
	    	}
	    	
	    	routeNameKey = new RouteNameKey(getValue(routeNode, "ROUTE_NAMEE"));
	    	routeNameKey.setLocale("en", getValue(routeNode, "ROUTE_NAMEE"));
	    	routeNameKey.setLocale("zh", getValue(routeNode, "ROUTE_NAMEC"));
	    	routeNameKey.setLocale("zh-hk", getValue(routeNode, "ROUTE_NAMEC"));
	    	routeNameKey.setLocale("zh-cn", getValue(routeNode, "ROUTE_NAMES"));
	    	addRoute(new TransitRoute(transitType, providerName, routeId, routeNameKey, paths, companyCode.split("\\+")));
			reportProgress((int) ((x + 1) / (float) routeNodes.size() * 100.0 / 3 + 100 / 3.0));
	    }
	    
	    //Stop Lat Lng
		reportMessage("Downloading stop locations...");
	    
	    fileKey = "STOP_BUS.xml";
	    fileIn = readDownloaded(fileKey);
	    if (fileIn == null) {
			reqCount++;
			url = new URL(STOP_BUS_XML);
		    conn = url.openConnection();
	    	in = conn.getInputStream();
	    } else {
	    	in = fileIn;
	    }

	    doc = builder.parse(in);
	    in.close();
	    in = null;
	    
	    if (fileIn == null) {
		    fileOut = writeDownloaded(fileKey);
			fileWriter = new PrintWriter(new OutputStreamWriter(fileOut, StandardCharsets.UTF_8));
			fileWriter.println(getStringFromDocument(doc));
			fileWriter.flush();
			fileWriter.close();
			fileOut.close();
	    }
	    
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CRSFactory csFactory = new CRSFactory();
        
        CoordinateReferenceSystem HK80 = csFactory.createFromParameters("EPSG:2326", "+proj=tmerc +lat_0=22.31213333333334 +lon_0=114.1785555555556 +k=1 +x_0=836694.05 +y_0=819069.8 +ellps=intl +towgs84=-162.619,-276.959,-161.764,0.067753,-2.24365,-1.15883,-1.09425 +units=m +no_defs");
        CoordinateReferenceSystem WGS84 = csFactory.createFromParameters("WGS84", "+proj=longlat +datum=WGS84 +no_defs");
        CoordinateTransform trans = ctFactory.createTransform(HK80, WGS84);
	    
	    list = doc.getElementsByTagName("STOP");
	    ProjCoordinate neCoord;
        ProjCoordinate geoCoord;
        StopNameKey stopNameKey;
        AddressKey addrKey;
        Node stopNode;
	    for (x = 0; x < stopNodes.size(); x++) {
	    	stopNode = stopNodes.get(x);
			reportMessage("Processing stop locations... " + (x + 1) + "/" + stopNodes.size());
	    	stopId = getValue(stopNode, "STOP_ID");
	    	for (i = 0; i < list.getLength(); i++) {
	    		listNode = list.item(i);
	    		if (getValue(listNode, "STOP_ID").equals(stopId)) {
	    			neCoord = new ProjCoordinate();
	    		    geoCoord = new ProjCoordinate();
	    		    
	    		    neCoord.x = Double.parseDouble(getValue(listNode, "X"));
	    		    neCoord.y = Double.parseDouble(getValue(listNode, "Y"));
	    		    trans.transform(neCoord, geoCoord);
	    		    
	    		    stopNameKey = new StopNameKey(getValue(stopNode, "STOP_NAMEE"));
	    		    stopNameKey.setLocale("en", getValue(stopNode, "STOP_NAMEE"));
	    		    stopNameKey.setLocale("zh", getValue(stopNode, "STOP_NAMEC"));
	    		    stopNameKey.setLocale("zh-hk", getValue(stopNode, "STOP_NAMEC"));
	    		    stopNameKey.setLocale("zh-cn", getValue(stopNode, "STOP_NAMES"));

	    		    addrKey = new AddressKey(getValue(stopNode, "STOP_NAMEE"));
	    		    addrKey.setLocale("en", getValue(stopNode, "STOP_NAMEE"));
	    		    addrKey.setLocale("zh", getValue(stopNode, "STOP_NAMEC"));
	    		    addrKey.setLocale("zh-hk", getValue(stopNode, "STOP_NAMEC"));
	    		    addrKey.setLocale("zh-cn", getValue(stopNode, "STOP_NAMES"));
	    		    
	    		    addStop(new TransitStop(transitType, providerName, stopId, stopNameKey, addrKey, geoCoord.y, geoCoord.x));
	    		}
	    	}
			reportProgress((int) ((x + 1) / (float) stopNodes.size() * 100.0 / 3 + 200 / 3.0));
	    }
		reportMessage("Done!");
		return true;
	}
	
	private static String getValue(Node node, String tagName) {
	    NodeList list = node.getChildNodes();
	    Node x;
	    for (int i = 0; i < list.getLength(); i++) {
	    	x = list.item(i);
	    	if (x.getNodeName().equals(tagName)) {
	    		return x.getTextContent();
	    	}
	    }
	    return null;
	}

	@Override
	public String generateVersion(long currentTimeMs, TransitRoute[] routes, TransitStop[] stops) {
		return Long.toString(currentTimeMs);
	}

}
