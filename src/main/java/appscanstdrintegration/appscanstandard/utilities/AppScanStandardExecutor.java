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
import java.util.List;

import org.jfree.util.Log;

import appscanstdrdintegration.appscanstandard.AppScanStandardInvocation;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;

public class AppScanStandardExecutor {
	
	private AppScanStandardExecutor() {
	}

	public static void execute(FilePath ws,Launcher launcher, String exe,
			TaskListener listener, List<String> cliScriptContent,
			List<String> cliScriptContentReport) throws InterruptedException {

		try {

			// Build the command line commands with necessary parameters
			JenkinsExecutor runner = new JenkinsExecutor(ws, launcher, listener);
			AppScanStandardInvocation invocation = new AppScanStandardInvocation(exe);
			invocation.addScriptFile(cliScriptContent);

			// Execute command line
			if (!invocation.execute(runner)) {
				throw new AbortException("AppScan Standard execution failed");
			}

			if (!cliScriptContentReport.isEmpty()) {
				AppScanStandardInvocation invocation2 = new AppScanStandardInvocation(
						exe);
				invocation2.addScriptFile(cliScriptContentReport);

				if (!invocation2.execute(runner)) {
					throw new AbortException(
							"AppScan Standard execution failed");
				}

			}
		} catch (IOException e) {
			Log.error("Interrupted exception during AppScanStandard execution." + e);
		}
	}
}
