package fast.common.htmlReport;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.Reportable;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class Generates Html Report without maven from jar file.
 */

public class GenerateHtmlReport extends AbstractMojo {

    /**
     * Name of the project.
     *
     * @parameter property="project.name"
     * @required
     */
    private String projectName; //="Test"

    /**
     * Build number.
     *
     * @parameter property="build.number" default-value="1"
     */
    private String buildNumber; //="1"

    /**
     * Location of the file.
     *
     * @parameter default-value="${project.build.directory}/cucumber-reports"
     * @required
     */

    private File outputDirectory;
    /**
     * Location of the file.
     *
     * @parameter default-value="${project.build.directory}/cucumber.json"
     * @required
     */

    private File cucumberOutput;
    /**
     * Skip check for failed build result.
     *
     * @parameter default-value="true"
     * @required
     */
    private Boolean checkBuildResult=true;

    /**
     * Build reports from parallel tests.
     *
     * @parameter property="true" default-value="false"
     * @required
     */
    private Boolean parallelTesting;// = true;

    /**
     * Additional attributes to classify current test run.
     *
     * @parameter
     */
    private Map<String, String> classifications;

    @Override
    public void execute() throws MojoExecutionException {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        List<String> list = new ArrayList<>();
        for (File jsonFile : cucumberFiles(cucumberOutput)) {
            try {
                list.add(jsonFile.getCanonicalPath());
            } catch (IOException e) {
            	getLog().error("Failed to get the canonical pathname of file: " + jsonFile.getName());
            }
        }

        if (list.isEmpty()) {
            try {
                getLog().warn(cucumberOutput.getCanonicalPath() + " does not exist.");
            } catch (IOException e) {
            	getLog().error(e.getMessage());
            }
            return;
        }

        try {
            Configuration configuration = new Configuration(outputDirectory, projectName);
            configuration.setBuildNumber(buildNumber);
            configuration.setParallelTesting(parallelTesting);

            if (!MapUtils.isEmpty(classifications))
//          if (classifications!=null && !classifications.isEmpty()
            {
                for (Map.Entry<String, String> entry : classifications.entrySet()) {
                    configuration.addClassifications(StringUtils.capitalise(entry.getKey()), entry.getValue());
                }
            }


            ReportBuilder reportBuilder = new ReportBuilder(list, configuration);
            getLog().info("About to generate Cucumber report.");
            Reportable report = reportBuilder.generateReports();

            if (checkBuildResult&& report == null) {
                throw new MojoExecutionException("BUILD FAILED - Check Report For Details");
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Error Found:", e);
        }
    }

    static Collection<File> cucumberFiles(File file) throws MojoExecutionException {
        if (!file.exists()) {
            return Collections.emptyList();
        }
        if (file.isFile()) {
            return Arrays.asList(file);
        }
        return FileUtils.listFiles(file, new String[]{"json"}, true);
    }

    public static void main(String[]args) throws MojoExecutionException, IOException {

        GenerateHtmlReport report = new GenerateHtmlReport();

        report.cucumberOutput=new File(System.getProperty("cucumberOutput"));
        report.outputDirectory=new File(System.getProperty("outputDirectory"));
        report.parallelTesting= BooleanUtils.toBoolean(System.getProperty("parallelTesting"));
        report.projectName=new String(System.getProperty("projectName"));

        System.out.println("outputDirectory " + report.outputDirectory.getCanonicalPath());
        System.out.println("cucumberOutput " + report.cucumberOutput.getCanonicalPath());
        System.out.println("projectName " + report.projectName);

        report.execute();
     }
}
