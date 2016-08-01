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

import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;

import appscanstdrintegration.appscanstandard.utilities.JenkinsExecutor;

public class AppScanStandardInvocation {
	private String exe;
	private List<String> scriptFile;
	private final Map<String, String> environment = new HashMap<>();

	public AppScanStandardInvocation(String exe) throws IOException, InterruptedException {
		this.exe = exe;

	}

	public boolean execute(JenkinsExecutor runner) {
		try {
			return runner.execute(buildCommandLine(), environment);
		} catch (IOException | InterruptedException e) {
			Log.error("Exception building command line." + e);
		}
		return false;
	}

	protected ArgumentListBuilder appendExecutable(ArgumentListBuilder args) {
		args.add(exe);
		return args;
	}

	public AppScanStandardInvocation addScriptFile(List<String> cliScriptContentReport) {
		this.scriptFile = cliScriptContentReport;
		return this;
	}

	protected ArgumentListBuilder appendScript(ArgumentListBuilder args) {
		for (String str : scriptFile) {
			args.add(str);
		}
		return args;
	}

	protected ArgumentListBuilder buildCommandLine() throws InterruptedException, IOException {
		ArgumentListBuilder args = new ArgumentListBuilder();
		appendExecutable(args);
		appendScript(args);

		return args;
	}

}
