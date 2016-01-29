/**
 * Created by sandesh.karkera on 28/09/15.
 */


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.opencsv.CSVReader;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.engine.*;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class NFR_Wrapper {

    public static void main(String[] argv) throws Exception {

        Properties properties = JMXHelper.getProperties(argv[0]);

        File jmeterHome = new File(properties.getProperty("Jmeter_Home"));
        String slash = System.getProperty("file.separator");

        //Check for jmeter
        if (!jmeterHome.exists()) {
            System.err.println("jmeter.home property is not set or pointing to incorrect location");
            System.exit(1);
        }

        //Load jmeter.properties
        File jmeterProperties = new File(jmeterHome.getPath() + slash + "bin" + slash + "jmeter.properties");

        //JMeter Engine
        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        //JMeter initialization (properties, log levels, locale, etc)
        JMeterUtils.setJMeterHome(jmeterHome.getPath());
        JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
        JMeterUtils.initLocale();

        //Hash tree initialization
        HashTree testPlanTree = new HashTree();

        //CSV Data Set Config
        CSVDataSet csvData = new CSVDataSet();

        if (properties.containsKey("Config_Name")) {
            csvData = JMXHelper.addCSVDataSet(properties.getProperty("Config_Name"), properties.getProperty("FilePath"),properties.getProperty("Variables"));
        }


        //Header Manager
        HeaderManager payloadHeader = JMXHelper.addHeaderManager();

        // HTTP Sampler
        List<HTTPSamplerProxy> samples = new ArrayList<HTTPSamplerProxy>();

        if (properties.getProperty("BodyTemplate").equals("Template1")) {
            samples.add(JMXHelper.addSampler(properties.getProperty("SamplerName"), properties.getProperty("Domain"), properties.getProperty("Path"), Integer.parseInt((properties.getProperty("Port"))), "Template1"));
        } else if (properties.getProperty("BodyTemplate").equals("Template2")) {
            samples.add(JMXHelper.addSampler(properties.getProperty("SamplerName"), properties.getProperty("Domain"), properties.getProperty("Path"), Integer.parseInt(properties.getProperty("Port")), "Template2"));
        }

        // Loop Controller
        //LoopController loopController = JMXHelper.addLoopController("TEST");

        // Thread Group
        ThreadGroup threadGroup = JMXHelper.addThreadGroup(properties.getProperty("TGName"), Integer.parseInt(properties.getProperty("Threads")), Integer.parseInt(properties.getProperty("RampUP")), Long.parseLong(properties.getProperty("DurationSec")));

        //ResponseAssertion
        ResponseAssertion responseAssertion = JMXHelper.addResponseAssertion();

        //Timer? - Constant Throughput Timer

        // Test Plan
        TestPlan testPlan = JMXHelper.addTestPlan();

        // Construct Test Plan from previously initialized elements
        testPlanTree.add(testPlan);

        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);

        //Add CSV config to the test plan
        if (properties.containsKey("Config_Name")) {
            threadGroupHashTree.add(csvData);
        }

        //Add header to the test plan
        threadGroupHashTree.add(payloadHeader);

        //Add payloads to the test plan
        for (HTTPSamplerProxy sampler : samples) {
            threadGroupHashTree.add(sampler);
        }

        //Add assertion to the test plan
        threadGroupHashTree.add(responseAssertion);

        // save generated test plan to JMeter's .jmx file format
        Date curr_time = new Date();
        String timestamp = new SimpleDateFormat("ddMMyyyy_hhmmss").format(curr_time);
        String startTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(curr_time);

        SaveService.saveTree(testPlanTree, new FileOutputStream(jmeterHome + slash + timestamp + ".jmx"));

        //add Summarizer output to get test progress in stdout like:
        // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        // Store execution results into a .jtl file
        String logFile = jmeterHome + slash + timestamp + ".jtl";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(jmeterHome + slash + timestamp + logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

//        SaveService.saveTree(testPlanTree, new FileOutputStream(jmeterHome + slash + timestamp + ".jmx"));

        // Run Test Plan

        jmeter.configure(testPlanTree);
//                  jmeter.run();

        //Creating execution command

        String[] hosts = new String[]{"host_ip1", "host_ip2", "host_ip3"}; //Remote hosts - you can also take this through properties file.
        String remoteHosts = new String();

        try {
            int instances = Integer.parseInt(properties.getProperty("Instances"));
            for (int i = 0; i < instances; i++) {
                remoteHosts = remoteHosts + hosts[i] + ",";
            }
            remoteHosts = remoteHosts.substring(0, remoteHosts.length() - 1);
        }
        catch (Exception e)
        {
            System.out.println("Invalid value entered for number of instances. The value must fall between 1 and 3");
            System.out.println("Exiting Program!");
            System.exit(1);
        }

        String command = jmeterHome + "/bin/jmeter " + "-n -t " + jmeterHome + slash + timestamp + ".jmx " + "-l" + jmeterHome + slash + timestamp + ".jtl " + "-j " + jmeterHome + slash + timestamp + ".log "+ "-R " + remoteHosts;
        System.out.println(command);
//        String command = jmeterHome + "/bin/jmeter " + "-n -t " + jmeterHome + slash + timestamp + ".jmx ";

//        JMXHelper.executeCMD(command);

        // save aggregate report in CSV
        String jarPath = jmeterHome + slash + "lib/ext/CMDRunner.jar";
        String generateCSV = "java -jar "+ jarPath + " --tool Reporter --generate-csv " + jmeterHome + slash + timestamp + ".csv --input-jtl " + jmeterHome + slash + timestamp + ".jtl --plugin-type SynthesisReport";

        System.out.println("Generating CSV");
//        JMXHelper.executeCMD(generateCSV);
        String end_Time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

        //READ FROM CSV
        Thread.sleep(5000);
        CSVReader reader = new CSVReader(new FileReader(jmeterHome + slash + timestamp + ".csv"), ',' , '\"' ,2);

        String[] values = reader.readNext();
        while(!values[0].equals("TOTAL"))
        {
            values=reader.readNext();
        }

        for(String value : values)
        {
            System.out.print(value + "      ");
        }

        //// Check for value[errorrate] and value[responsetime] etc.
        //// Trigger mail if value does not satisfy a given criteria
        //// assumption 2 = response time and 4 = error rate


        // Insert values to DB
        JMXHelper.dbInsert(values, properties.getProperty("BodyTemplate"), startTime, end_Time);

        // Send mail consisting of results
        JMXHelper.sendMail(jmeterHome + slash + timestamp + ".csv", properties.getProperty("BodyTemplate"));

        System.out.println("Test completed. See " + jmeterHome + slash + timestamp + ".jtl file for results");
        System.out.println("See " + jmeterHome + slash + timestamp + ".log file for logs");
        System.out.println("JMeter .jmx script is available at " + jmeterHome + slash + timestamp + ".jmx");
        System.out.println("Executing : java -jar "+ jarPath + " --tool Reporter --generate-csv " + jmeterHome + slash + timestamp + ".csv --input-jtl " + jmeterHome + slash + timestamp + ".jtl --plugin-type AggregateReport");
        System.out.println("Aggregate Report is available at " + jmeterHome + slash + timestamp + ".csv");

        System.exit(0);
    }



}
