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

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.tools.ToolProperty;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;

public class AppScanStandardInstallation extends ToolInstallation {

	private static final long serialVersionUID = 1L;

	@DataBoundConstructor
	public AppScanStandardInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
		super(name, home, properties);
	}

	public AppScanStandardInstallation forEnvironment(EnvVars environment) {
		return new AppScanStandardInstallation(getName(), environment.expand(getHome()),
				getProperties().toList());
	}

	public AppScanStandardInstallation forNode(Node node, TaskListener log) throws InterruptedException {
		try {
			return new AppScanStandardInstallation(getName(), translateFor(node, log),
					getProperties().toList());
		} catch (IOException e) {
			Logger logger = Logger.getAnonymousLogger();
			logger.log(Level.SEVERE, "AppScan Standard not found on selected node.", e);
			log.getLogger().println("Verify that AppScan Standard is installed in the selected node.");
		}
		return null;
	}

	public static String getExecutable(String name, AppScanStandardCommand command, Node node,
			TaskListener listener, EnvVars env) throws InterruptedException {

		Jenkins j = Jenkins.getInstance();
		
		/**
		 * To avoid nested conditions/cycles.
		 */
		if (j == null || StringUtils.isEmpty(name)) {
			return command.getName();
		}
		
		for (AppScanStandardInstallation tool : j.getDescriptorByType(DescriptorImpl.class).getInstallations()) {

			if (tool.getName().equals(name)) {

				if (node != null)
					tool = tool.forNode(node, listener);
				if (env != null)
					tool = tool.forEnvironment(env);

				String home = Util.fixEmpty(tool.getHome());
				/*
				 * To avoid Cyclomatic complexity.
				 */
				return validateCommand(home, node, command);

			}
		}

		return command.getName();
	}

	public static String validateCommand(String home, Node node, AppScanStandardCommand command) {
		if (home != null) {
			if (node != null) {
				FilePath homePath = node.createPath(home);
				if (homePath != null) {
					return homePath.child(command.getName()).getRemote();
				}
			}
			return home + "/" + command.getName();
		}
		return command.getName();
	}

	public static AppScanStandardInstallation[] allInstallations() {
		AppScanStandardInstallation[] installations = {};

		Jenkins jenkins = Jenkins.getInstance();
		if (jenkins != null) {
			installations = jenkins.getDescriptorByType(
					AppScanStandardInstallation.DescriptorImpl.class)
					.getInstallations();
		}

		return installations;
	}

	public static AppScanStandardInstallation getInstallation(String appScanStandardInstallation)
			throws IOException {
		AppScanStandardInstallation[] installations = allInstallations();
		if (appScanStandardInstallation == null) {
			if (installations.length == 0) {
				throw new IOException("AppScan Standard not found");
			}
			return installations[0];
		} else {
			for (AppScanStandardInstallation installation : installations) {
				if (appScanStandardInstallation.equals(installation.getName())) {
					return installation;
				}
			}
		}
		throw new IOException("AppScan Standard not found");
	}

	@Extension
	public static class DescriptorImpl extends ToolDescriptor<AppScanStandardInstallation> {

		public DescriptorImpl() {
			load();
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			super.configure(req, json);
			save();
			return true;
		}

		@Override
		public String getDisplayName() {
			return "AppScan Standard";
		}
	}
}
