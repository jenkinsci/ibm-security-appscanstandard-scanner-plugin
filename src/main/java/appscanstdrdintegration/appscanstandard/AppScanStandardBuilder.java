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

import appscanstdrintegration.appscanstandard.utilities.AppScanStandardExecutor;
import appscanstdrintegration.appscanstandard.utilities.AppScanStandardManualExploreFile;
import appscanstdrintegration.appscanstandard.utilities.AppScanStandardScanTemplateBuilder;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * 
 * @author Tiago Lopes
 * @since 2.0
 *
 */

public class AppScanStandardBuilder extends Builder implements SimpleBuildStep {

	/**
	 * Information provided from config.jelly (job configuration)
	 */

	/**
	 * URL provided in the job configuration  to start the scan from.
	 */
	private String startingURL;

	/**
	 * If true the scan will use some form of authentication.
	 */
	private boolean authScan;

	/**
	 * If true, the authentication method is a recorded login sequence.
	 * if false, it will use form based authentication.
	 */
	private boolean authScanRadio;

	/**
	 * User name to be used for form based authentication.
	 */
	private String authScanUser;

	/**
	 * Password for the user to be used for form based authentication.
	 */
	private String authScanPw;

	/**
	 * If true, a report will be generated after scanning.
	 */
	private boolean generateReport;

	/**
	 * The name to save the report with.
	 */
	private String reportName;

	/**
	 * If true, an HTML report is generated after scanning.
	 */
	private boolean htmlReport;

	/**
	 * If true, a PDF report is generated after scanning.
	 */
	private boolean pdfReport;

	/**
	 * For additional commands for AppScan provided in the job configuration.
	 */
	private String additionalCommands;

	/**
	 * The test policy file to be used during scanning.
	 */
	private String policyFile;

	/**
	 * Holds the path to the recorded login sequence to be used for authenticated scanning.
	 */
	private String pathRecordedLoginSequence;

	/**
	 * Holds the path to AppScan installation.
	 */
	private String installation;

	/**
	 * URLs to be included for scanning (manually)
	 */
	private String includeURLS;

	/**
	 * Provides access to the Jenkins Logger.
	 */
	private PrintStream logger;

	/**
	 * Used to build the commands to be executed via AppScan Standard's CLI.
	 */
	private ArrayList<String> cmd;
	private ArrayList<String> cmdreport;

	/**
	 * Used to store the commands set in the global configuration.
	 */
	String splitGlobalCommands;

	/**
	 * If true, a report will be generated after scanning.
	 */
	private boolean verbose;

	/**
	 * Performs on-the-fly validation of the form field 'startingURL'.
	 *
	 * @param startingURL
	 *	Starting URL for AppScan to spider on.
	 *@param installation
	 *	AppScan Standard installation directory path.
	 */
	@DataBoundConstructor
	public AppScanStandardBuilder(String startingURL, String installation) {

		/**
		 * Those are the only mandatory parameters, the rest are set using @DataBoundSetter.
		 */
		this.startingURL = startingURL;
		this.installation = installation;

	}

	/**
	 * This method is called when the build is executed and is responsible for triggering AppScanCMD.
	 */
	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		/**
		 * This is where you 'build' the project.
		 * This also shows how you can consult the global configuration of the builder.
		 */

		/*
		//It's unclear the objective of this code, causing errors when launching the plugin via pipeline

		Computer computer = Computer.currentComputer();
		if (computer == null) {
			throw new AbortException(
					"The AppScan Standard build step requires to be launched on a node");
		}
		*/

		if (StringUtils.isBlank(this.getStartingURL())) {
			throw new AbortException(
					"The AppScan Standard requires a Starting URL to run a scan.");
		}

		if (StringUtils.isBlank(this.getInstallation())) {
			throw new AbortException(
					"AppScan Standard installation is not set, please configure it in the Configure Global Tools section.");
		}

		/*
		//Gets the node on which AppScan is going to run.
		//It's unclear the objective of this code, causing errors when launching the plugin via pipeline

		Node node = computer.getNode();
		*/
		Node node = null; //quick fix for handling pipelines without refactoring

		//Gets Jenkins Environment Variables.
		EnvVars envVars = build.getEnvironment(listener);

		//Gets Jenkins logger.
		logger = listener.getLogger();

		//Creates a String ArrayList to build the command to be run on AppScanCMD (CLI).
		cmd = new ArrayList<>();
		cmdreport = new ArrayList<>();

		AppScanStandardScanTemplateBuilder scanTemplateBuilder = null;
		AppScanStandardManualExploreFile manualExploreFile = null;

		/**
		 * Creating basic AppScan Standard Command:
		 * " AppScanCMD /exec /su http://url.com /v "
		 */

		String baseScanName = "AppScanCMD_";

		cmd.add(AppScanStandardConstants.APPSCANCMD_EXEC);
		cmd.add(AppScanStandardConstants.APPSCANCMD_START_URL);
		cmd.add(getStartingURL());
		if (verbose) {
			cmd.add(AppScanStandardConstants.APPSCANCMD_VERBOSE);
		}

		/**
		 * If both report types are going to be generated,
		 * a base scan must be saved to use for generating the second report.
		 */
		if (isHtmlReport() && isPdfReport()) {
			baseScanName += new SimpleDateFormat("DD_MM_YY_HH_mm_ss")
					.format(Calendar.getInstance().getTime());
			baseScanName += ".scan";
			cmd.add(AppScanStandardConstants.APPSCANCMD_DEST_SCAN);
			cmd.add(workspace.getRemote() + "\\" + baseScanName);
		}

		/**
		 * Setup to include the scan template with the  authentication credentials.
		 */
		if (authScan) {
			/*
			 * If true, authentication scan is based on a recorded login file,
			 * the path to the file must be provided.
			 *
			 * If false, form based authentication will be used,
			 * user name and password must be provided.
			 */
			if (authScanRadio && StringUtils.isNotBlank(pathRecordedLoginSequence)) {
				/**
				 * " [AppScanCMD ...]	 /login_file pathToFile "
				 */
				cmd.add(AppScanStandardConstants.APPSCANCMD_LOGIN_FILE);
				cmd.add(pathRecordedLoginSequence);
			} else if (!authScanRadio && StringUtils.isNotBlank(authScanUser)) {
				//Allowing users without password which may be possible during development phase

				/**
				 * A temporary scan template file is created containing the user name and password,
				 * AppScan will use this data to feed the login form.
				 * This approach is not bulletproof by itself and it's only implemented as a plus for those
				 * who can't create a login sequence.
				 * [ The password is currently being stored as clear-text. ]
				 */
				scanTemplateBuilder = new AppScanStandardScanTemplateBuilder(
						authScanUser, authScanPw);

				scanTemplateBuilder.createScanTemplate(workspace);

				/**
				 * " [AppScanCMD ...]	 /scan_template pathToTemplate "
				 */
				cmd.add(AppScanStandardConstants.APPSCANCMD_SCAN_TEMPLATE);

				cmd.add(scanTemplateBuilder.getScanTemplateFileLocation());
			}
		}

		/**
		 * Using a specific test policy file, if not provided the default test policy is used.
		 */
		if (policyFile != null && !policyFile.isEmpty()) {
			/**
			 * " [AppScanCMD ...]	 /policy_file pathToFile "
			 */
			cmd.add(AppScanStandardConstants.APPSCANCMD_POLICY_FILE);
			cmd.add(policyFile);
		}

		/**
		 * Generating a report, HTML or PDF or both.
		 */
		if (isGenerateReport() && StringUtils.isNotBlank(getReportName())) {
			/**
			 * " [AppScanCMD ...]	 /report_file pathToSaveReport /report_type Html "
			 */
			cmd.add(AppScanStandardConstants.APPSCANCMD_REP_FILE);

			cmd.add(workspace.getRemote() + "\\" + getReportName());

			cmd.add(AppScanStandardConstants.APPSCANCMD_REP_TYPE);

			if (isHtmlReport()) {
				cmd.add(AppScanStandardConstants.APPSCANCMD_REP_TYPE_HTML);

				/**
				 * Generates both type of reports with the same name
				 */
				if (isPdfReport()) {
					cmdreport.add(AppScanStandardConstants.APPSCANCMD_REPORT);
					cmdreport.add(AppScanStandardConstants.APPSCANCMD_BASE_SCAN);
					cmdreport.add(workspace.getRemote() + "\\" + baseScanName);

					cmdreport.add(AppScanStandardConstants.APPSCANCMD_REP_FILE);
					cmdreport.add(workspace.getRemote() + "\\"
							+ getReportName());
					cmdreport.add(AppScanStandardConstants.APPSCANCMD_REP_TYPE);
					cmdreport.add(AppScanStandardConstants.APPSCANCMD_REP_TYPE_PDF);
				}

			} else if (isPdfReport()) {
				cmd.add(AppScanStandardConstants.APPSCANCMD_REP_TYPE_PDF);
			} else {
				/**
				 * Default to HTML report if no option was selected.
				 */
				setHtmlReport(true);
				cmd.add(AppScanStandardConstants.APPSCANCMD_REP_TYPE_HTML);
			}
		}

		/**
		  *
		  * Includes URLs for scanning
		  *
		  * By accessing the URL and checking "Scan URL" on the plugin job's GUI
		  * it will automatically active scan all accessed URLs
		  *
		  * Splits the URLs inserted in the text area by new line and accesses each individually
		  */

		if ((includeURLS != null) && (!"".equals(includeURLS.trim()))) {
			manualExploreFile = new AppScanStandardManualExploreFile(includeURLS, logger);

			manualExploreFile.createManualExploreFile(workspace);

			cmd.add(AppScanStandardConstants.APPSCANCMD_MERGE_URLS);
			cmd.add(AppScanStandardConstants.APPSCANCMD_MANUAL_EXPLORE);
			cmd.add(manualExploreFile.getManualExploreFileLocation());

		}

		/**
		 * Additional commands can be inserted during job configuration such as flags.
		 * Must split to avoid quotes inside the query.
		 */

		String[] splitCommands = additionalCommands.split(" ");
		for (String str : splitCommands) {
			cmd.add(str);
		}

		/**
		 * This descriptor is used to GET the data from Jenkin's Global Configuration.
		 */
		splitGlobalCommands = getDescriptor().getAdditionalCommandsGlobal();

		if (!StringUtils.isBlank(splitGlobalCommands)) {
			String[] splitGlobalCommands2 = splitGlobalCommands.split(" ");
			for (String str : splitGlobalCommands2) {
				cmd.add(str);
			}
		}

		/**
		 * Executes AppScan using the CLI, the built command is provided to set the scan configuration.
		 */

		String exe = AppScanStandardInstallation.getExecutable(installation,
				AppScanStandardCommand.APPSCANSTANDARDCLI, node, listener, envVars);

		if ("AppScanCMD.exe".equals(exe)) {
			logger.println("\nWarning: The path to the AppScan Standard installation may not be set, please configure it in the Configure Global Tools section or setup PATH.\n");
		}

		logger.println("AppScan Standard is going to scan the target:  " + getStartingURL());

		AppScanStandardExecutor.execute(workspace, launcher, exe, listener, cmd, cmdreport);

		/**
		 * Deletes the temporary scan template file created for form based authentication.
		 * !authScanRadio means that "form based auth" was used.
		 */
		if (scanTemplateBuilder != null)

		{
			scanTemplateBuilder.deleteScanTemplateFile();
		}

		if (manualExploreFile != null) {
			manualExploreFile.deleteManualExploreFile();
		}

		logger.println("Report available at: " + workspace.getRemote() + "\\" + getReportName());

	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		/**
		 * To persist global configuration information, simply store it in a field and call save().
		 *
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */

		/*
		 * Information provided from global.jelly (global configuration)
		 */

		/**
		 * Used to execute additional commands such as flags on every job running AppScan.
		 */
		private String additionalCommandsGlobal;

		/**
		 * In order to load the persisted global configuration,
		 * you have to call load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}

		/**
		 * Gets AppScan installation(s) from Global Tool Configuration.
		 * @return The ListBoxModel object.
		 */
		public ListBoxModel doFillInstallationItems() {
			ListBoxModel model = new ListBoxModel();
			for (AppScanStandardInstallation tool : AppScanStandardInstallation
					.allInstallations()) {
				model.add(Util.fixEmptyAndTrim(tool.getName()));
			}
			return model;
		}

		/**
		 * Performs on-the-fly validation of the form field 'startingURL'.
		 *
		 * @param value
		 *                    This parameter receives the value
		 *                    that the user has typed.
		 * @return Indicates the outcome of the validation. This
		 *         is sent to the browser.
		 *         
		 *         Note that returning
		 *         {@link FormValidation#error(String)} does not
		 *         prevent the form from being saved. It just
		 *         means that a message will be displayed to the
		 *         user.
		 *         @throws ServletException
		 *         Defines a general exception a servlet can throw when it encounters difficulty. 
		 */
		public FormValidation doCheckStartingURL(@QueryParameter String value) throws ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set the starting URL");
			if (value.length() < 4)
				return FormValidation.warning("Isn't the URL too short?");

			return FormValidation.ok();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			/**
			 * Indicates that this builder can be used with all kinds of project types.
			 */
			return true;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			/**
			 * To persist global configuration information,set that to properties and call save().
			 */

			/**
			 * Gets the additional commands from Global Configuration.
			 */
			additionalCommandsGlobal = formData.getString("additionalCommandsGlobal");

			save();
			return super.configure(req, formData);
		}

		/**
		 * Text to display on the "Add build step" drop down list
		 */
		@Override
		public String getDisplayName() {
			return "Run AppScan Standard";
		}

		/**
		 * Getter for additional commands from Global Configuration
		 * @return The commands, if any.
		 */
		public String getAdditionalCommandsGlobal() {
			return additionalCommandsGlobal;
		}
	}

	/**
	 * To be used in config.jelly to bind fields with values,
	 * Getter for Job Configuration to persist values onto fields.
	 * 
	 * @return The data provided in the filed.
	 */
	public String getStartingURL() {
		return startingURL;
	}

	public String getInstallation() {
		return installation;
	}

	public boolean isAuthScan() {
		return authScan;
	}

	public String getAuthScanUser() {
		return authScanUser;
	}

	public String getAuthScanPw() {
		return authScanPw;
	}

	public boolean isHtmlReport() {
		return htmlReport;
	}

	public boolean isPdfReport() {
		return pdfReport;
	}

	public boolean isGenerateReport() {
		return generateReport;
	}

	public String getReportName() {
		return reportName;
	}

	public String getPathRecordedLoginSequence() {
		return pathRecordedLoginSequence;
	}

	public String getPolicyFile() {
		return policyFile;
	}

	public String getAdditionalCommands() {
		return additionalCommands;
	}

	public boolean isAuthScanRadio() {
		return authScanRadio;
	}

	public String getIncludeURLS() {
		return includeURLS;
	}

	public boolean isVerbose() {
		return verbose;
	}

	@DataBoundSetter
	public void setAuthScan(boolean authScan) {
		this.authScan = authScan;
	}

	@DataBoundSetter
	public void setAuthScanRadio(boolean authScanRadio) {
		this.authScanRadio = authScanRadio;
	}

	@DataBoundSetter
	public void setAuthScanUser(String authScanUser) {
		this.authScanUser = authScanUser;
	}

	@DataBoundSetter
	public void setAuthScanPw(String authScanPw) {
		this.authScanPw = authScanPw;
	}

	@DataBoundSetter
	private void setHtmlReport(boolean htmlReport) {
		this.htmlReport = htmlReport;
	}

	@DataBoundSetter
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	@DataBoundSetter
	public void setGenerateReport(boolean generateReport) {
		this.generateReport = generateReport;
	}

	@DataBoundSetter
	public void setPdfReport(boolean pdfReport) {
		this.pdfReport = pdfReport;
	}

	@DataBoundSetter
	public void setPathRecordedLoginSequence(String pathRecordedLoginSequence) {
		this.pathRecordedLoginSequence = pathRecordedLoginSequence;
	}

	@DataBoundSetter
	public void setPolicyFile(String policyFile) {
		this.policyFile = policyFile;
	}

	@DataBoundSetter
	public void setAdditionalCommands(String additionalCommands) {
		this.additionalCommands = additionalCommands;
	}

	@DataBoundSetter
	public void setIncludeURLS(String includeURLS) {
		this.includeURLS = includeURLS;
	}

	@DataBoundSetter
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}