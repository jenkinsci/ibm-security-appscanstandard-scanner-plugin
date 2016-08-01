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


package appscanstdrdintegration.appscanstandard;

public class AppScanStandardConstants {
	/**
	 * Commands provided by the AppScan Command Line Interface (CLI)
	 * Used for the entirety of the interaction by building a  command using an ArrayList of Strings
	 */
	public static final String APPSCANCMD_START_URL = "/starting_url";
	public static final String APPSCANCMD_VERBOSE = "/verbose";
	public static final String APPSCANCMD_EXEC = "/exec";
	public static final String APPSCANCMD_BASE_SCAN = "/base_scan";
	public static final String APPSCANCMD_DEST_SCAN = "/dest_scan";
	public static final String APPSCANCMD_SCAN_TEMPLATE = "/scan_template";
	public static final String APPSCANCMD_REPORT = "/report";
	public static final String APPSCANCMD_REP_FILE = "/report_file";
	public static final String APPSCANCMD_REP_TYPE = "/report_type";

	public static final String APPSCANCMD_REP_TYPE_HTML = "Html";
	public static final String APPSCANCMD_REP_TYPE_PDF = "Pdf";

	public static final String APPSCANCMD_LOGIN_FILE = "/login_file";

	public static final String APPSCANCMD_POLICY_FILE = "/policy_file";
	
	public static final String APPSCANCMD_MERGE_URLS = "/merge_manual_explore_requests";
	
	public static final String APPSCANCMD_MANUAL_EXPLORE = "/manual_explore_file";

	/**
	 * RemoteShell - this approach has been replaced with the node approach
	 */
	public static final String WINRS_EXEC_CMD = "WinRS";
	public static final String WINRS_REMOTE_ADDR = "/r:";
	public static final String WINRS_REMOTE_USER = "/u:";
	public static final String WINRS_REMOTE_PW = "/p:";
	public static final String CMD_AND = "&"; 


	public static final String JAVA_CMD = "java";
	public static final String JAR_CMD = "-jar";

	public static final String DEL_CMD = "del";
	public static final String AND_CMD = "&";
	public static final String QUIET_CMD = "/Q";

	public static final String APPSCANCMD_EXPLORE_ONLY = "/eo";

	public static final String APPSCANCMD_NAME_EXE = "AppScanCMD.exe";
	
	private AppScanStandardConstants(){}
}
