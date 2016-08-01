/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Tiago Lopes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package appscanstdrintegration.appscanstandard.utilities;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.jfree.util.Log;

import hudson.FilePath;

public class AppScanStandardManualExploreFile {

	private final String includeURLS;
	private FilePath tempFile;
	private PrintStream logger;
	private StringBuilder sb;

	public AppScanStandardManualExploreFile(String includeURLS, PrintStream logger) {
		this.includeURLS = includeURLS;
		this.logger = logger;
	}

	public void createManualExploreFile(FilePath ws) {

		/**
		 * Including URLs to be scanned using a manual explore file (.exd),
		 * this file consists of GET requests that appscan will send and once it obtains the response,
		 * the URL will be included for scanning along with those found by the spiders.
		 *
		 */
		
		sb = new StringBuilder();
		
		String[] urlsToInclude = includeURLS.split("\n");
		

		/*
		 * EXD file header, preparing to create the requests.
		 */
		sb.append("<?xml version='1.0' encoding='UTF-8'?>" + System.lineSeparator()
				+ "<!-- Automatically created by AppScan -->" + System.lineSeparator()
				+ "<!-- Do NOT Edit! -->" + System.lineSeparator() + "<requests>"
				+ System.lineSeparator());

		/*
		 * Creating one GET request for each URL to be included.
		 */
		for (int i = 0; i < urlsToInclude.length; i++) {
			if (!urlsToInclude[i].isEmpty()) {
				/**
				 * Creates a GET request for this URI and appends it to the stringbuilder.
				 */
				createGETRequests(urlsToInclude[i]);
			}
		}

		sb.append("</requests>");

		/*
		 * Create a temporary EXD file with the requests.
		 */
		try {
			tempFile = ws.createTextTempFile("manual_explore_file", ".exd", sb.toString());
		} catch (IOException | InterruptedException e) {
			Log.error("Exception creating temporary text file for manual explore."+e);
		}

	}
	
	public void createGETRequests(String sURL){

		URI inc = null;
		try {
			inc = new URI(sURL);

			/*
			 * Changing the default path from empty to /,
			 * else appscan will fail to load the .exd file.
			 */
			String path = inc.getPath();
			if ("".equals(path))
				path = "/";

			/*
			 * Changing the default port from -1  to 80,
			 * else appscan will fail to load the .exd file.
			 */
			int port = inc.getPort();
			if (port < 0)
				port = 80;

			String query = "";

			if (inc.getQuery() != null)
				query = "?" + inc.getQuery();

			/*
			 * Regular HTTP GET request.
			 */
			sb.append("<request scheme=\"" + inc.getScheme()
					+ "\" host=\"" + inc.getHost()
					+ "\" path=\"" + path + query
					+ "\" port=\"" + port
					+ "\" method=\"GET\">"
					+ System.lineSeparator()
					+ "<raw>GET " + path + "?" + query
					+ " HTTP/1.1"
					+ System.lineSeparator()
					+ "Accept: */*"
					+ System.lineSeparator()
					+ "Accept-Language: en-US,he;q=0.5"
					+ System.lineSeparator()
					+ "User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; MS-RTC LM 8)"
					+ System.lineSeparator()
					+ "Connection: Keep-Alive"
					+ System.lineSeparator() + "Host: "
					+ inc.getHost() + ":" + port
					+ System.lineSeparator()
					+ "Content-Length: 0"
					+ System.lineSeparator()
					+ System.lineSeparator() + "</raw>"
					+ System.lineSeparator()
					+ "</request>"
					+ System.lineSeparator());
		} catch (URISyntaxException e) {
			Log.error("Exception converting string to URI."+e);
			logger.println("Invalid URL: [ "+sURL+ " ] skipping it.");
		}
	}

	public String getManualExploreFileLocation() {
		return tempFile.getRemote();
	}

	/*
	 * Delete the temporary EXD file after used.
	 */
	public void deleteManualExploreFile(){
		try {
			tempFile.delete();
		} catch (IOException | InterruptedException e) {
			Log.error("Exception deleting  temporary text file for manual explore."+e);
		}
	}

}
