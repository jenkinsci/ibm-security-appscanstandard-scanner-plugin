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
import java.util.Map;

import org.jfree.util.Log;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

public class JenkinsExecutor {
	private final Launcher launcher;
	private final TaskListener taskListener;
	private final FilePath workspace;

	public JenkinsExecutor(AbstractBuild<?, ?> build, Launcher launcher, BuildListener taskListener) {
		this.launcher = launcher;
		this.taskListener = taskListener;
		this.workspace = build.getWorkspace();
	}

	public JenkinsExecutor(FilePath workspace, Launcher launcher, TaskListener taskListener) {
		this.launcher = launcher;
		this.taskListener = taskListener;
		this.workspace = workspace;
	}

	public boolean execute(ArgumentListBuilder args, Map<String, String> environment)
			throws InterruptedException {
		try {
			return launcher.launch().pwd(workspace).envs(environment).cmds(args).stdout(taskListener)
					.join() == 0;
		} catch (IOException e) {
			Log.error("Exception executing Jenkins."+e);
		}
		return false;
	}
}
