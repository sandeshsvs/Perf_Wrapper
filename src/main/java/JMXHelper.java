/**
 * Created by sandesh.karkera on 28/09/15.
 */


import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import java.sql.*;
import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class JMXHelper {

    private static HashMap<String, String> payloadBody = new HashMap<String, String>();

//    JDBC driver name and database URL
//    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://IP:3306/NFR_Results";

    //  Database credentials
    static final String USER = "SQL_User";
    static final String PASS = "Pass";

    public static void setPayloadBody() {
        JMXHelper.payloadBody.put("Template1", "Template1.txt");
        JMXHelper.payloadBody.put("Template2", "Template2.txt");

    }

    public static HTTPSamplerProxy addSampler(String samplerName, String domain, String path, Integer port, String body_template) throws IOException {
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setName(samplerName);


        if(samplerName.equalsIgnoreCase("Template_for_Get_Call"))
        {

            Arguments arg = new Arguments();
            arg.setName("HTTPsampler.Arguments");
            arg.setProperty(TestElement.GUI_CLASS, HTTPArgumentsPanel.class.getName());
            arg.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
            arg.setEnabled(true);

            arg.addArgument(JMXHelper.getHTTPArg("ids", "1,2"));
            arg.addArgument(JMXHelper.getHTTPArg("startTime", "2011-09-23T00:00:00%2B05:30"));
            arg.addArgument(JMXHelper.getHTTPArg("endTime", "2018-09-24T00:00:00%2B05:30"));
            arg.addArgument(JMXHelper.getHTTPArg("metrics","m1,m2,m3"));

            sampler.setArguments(arg);
            sampler.setMethod("GET");
            sampler.setPostBodyRaw(false);
        }
        else{
            String payload = getBody(body_template);
            System.out.println("payload : " + payload);
            //sampler.addArgument("Argument.value", payload, "=");
            sampler.addNonEncodedArgument("Argument.value", payload, "=");
            sampler.setMethod("POST");
            sampler.setPostBodyRaw(true);

        }

        sampler.setDomain(domain);
        sampler.setPort(port);
        sampler.setConnectTimeout("100");
        sampler.setResponseTimeout("200");
        sampler.setFollowRedirects(true);
        sampler.setAutoRedirects(false);
        sampler.setUseKeepAlive(true);
        sampler.setPath(path);
        sampler.setDoMultipartPost(false);
        sampler.setMonitor(false);

        sampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setEnabled(true);
        return (sampler);
    }

    private static HTTPArgument getHTTPArg(String name, String value){
        HTTPArgument arg = new HTTPArgument();
        arg.setAlwaysEncoded(false);
        arg.setName(name);
        arg.setValue(value);
        arg.setMetaData("=");
        arg.setUseEquals(true);
        arg.setProperty("Argument.name", name);
        return  arg;
    }

    public static LoopController addLoopController(String looperName) {
        LoopController loopController = new LoopController();
        loopController.setLoops(-1);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        loopController.setEnabled(true);
        loopController.setContinueForever(false);
        loopController.setName(looperName);
        return (loopController);
    }

    public static ThreadGroup addThreadGroup(String TGname, Integer threads, Integer rampUP, Long duration) {
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName(TGname);
        threadGroup.setNumThreads(threads);
        threadGroup.setRampUp(rampUP);
        threadGroup.setStartTime(System.currentTimeMillis());
        threadGroup.setEndTime(System.currentTimeMillis());
        threadGroup.setScheduler(true);
        threadGroup.setDuration(duration);
        threadGroup.setSamplerController(addLoopController("Looper"));
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        threadGroup.setEnabled(true);
        return (threadGroup);
    }

    public static HeaderManager addHeaderManager() {
        HeaderManager payloadHeader = new HeaderManager();
        payloadHeader.setName("Default Header");
        Header Content_Type = new Header("Content-Type", "application/json");
        payloadHeader.add(Content_Type);
        payloadHeader.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        payloadHeader.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        payloadHeader.setEnabled(true);
        return (payloadHeader);
    }

    public static CSVDataSet addCSVDataSet(String configName, String fileName, String variables) {
        CSVDataSet csvDataSet = new CSVDataSet();
        csvDataSet.setName("CSV Data Set Config");
        csvDataSet.setProperty("delimiter", ",");
        csvDataSet.setProperty("filename", fileName);
        csvDataSet.setProperty("quotedData", false);
        csvDataSet.setProperty("recycle", false);
        csvDataSet.setProperty("shareMode", "shareMode.all");
        csvDataSet.setProperty("stopThread", true);
        csvDataSet.setProperty("variableNames", variables);
        csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
        csvDataSet.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
        csvDataSet.setEnabled(true);
        return (csvDataSet);
    }

    public static TestPlan addTestPlan() {
        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
        testPlan.setEnabled(true);
        return (testPlan);
    }

    public static ResponseAssertion addResponseAssertion() {
        ResponseAssertion responseAssert = new ResponseAssertion();
        responseAssert.setName("Response Assertion");
        responseAssert.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
        responseAssert.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
        responseAssert.addTestString("200");
        responseAssert.setAssumeSuccess(false);
        responseAssert.setTestFieldResponseCode();
        responseAssert.setEnabled(true);
        return (responseAssert);
    }

    public static String getBody(String body) throws IOException {
        setPayloadBody();
        String bodyFile = payloadBody.get(body);
        InputStream iStream = JMXHelper.class.getResourceAsStream(bodyFile);
        BufferedReader r = new BufferedReader(new InputStreamReader(iStream));
        return org.apache.commons.io.IOUtils.toString(r);
    }

    public static Properties getProperties(String template) throws IOException
    {
        String inputFilePath = template;

        File file = new File(inputFilePath);
        FileInputStream fileInput = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fileInput);
        fileInput.close();
        return  properties;
    }

    public static void dbInsert(String[] row, String table_name, String startTime, String endTime)
    {
        Connection conn = null;
        Statement stmt = null;
        try{
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Inserting records into the table...");
            stmt = conn.createStatement();

            //SqlClient sqlClient = new SqlClient();
            char dq = '"';

            String sql = "INSERT INTO " + table_name + "(sampler_name,start_time,end_time,total_samples,avg_resp_time,min_resp_time,max_resp_time,90_percent_line,std_dev,error_percent,throughput,KB_per_sec,avg_bytes) VALUES (" + dq +row[0] + dq + "," + "'" + startTime + "'" +"," + "'" + endTime + "'" + "," + row[1] + "," + row[2] + ","+ row[3] + ","+ row[4] + ","+ row[5] + ","+ row[6] + ","+ row[7].substring(0, row[7].length()-1) + "," + row[8] + ","+ row[9] + ","  + row[10] +");";
            System.out.println(sql);
            stmt.executeUpdate(sql);

            System.out.println("Inserted records into the table...");

        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
    }


    public static void sendMail(String filename, String TestName)
    {

        // Recipient's email ID needs to be mentioned.
        String to = "email1@gmail.com";

        // Sender's email ID needs to be mentioned
        final String from = "email2@gmail.com";

        // Assuming you are sending email from localhost
        String host = "smtp.gmail.com";

        // Get system properties
        ////Properties properties = System.getProperties();

        Properties properties = new Properties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties ,new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("email2@gmail.com","App_pass");//change accordingly
            }
        });

//        Session session = Session.getInstance(properties);

        try{
            // Create a default MimeMessage object.

            javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(TestName + " perf run results!");
//
//            // Now set the actual message
//            message.setText("Test Test");

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Fill the message
            messageBodyPart.setText("Please find the results attached");

            // Create a multipart message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
//            String filename = ".csv";
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename.substring(filename.lastIndexOf('/')+1));
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart );


            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    public static void executeCMD(String command) throws Exception
    {
        Process jmeterRun = Runtime.getRuntime().exec(command);

        //Read output
        StringBuilder out = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(jmeterRun.getInputStream()));
        String line = null, previous = null;
        while ((line = br.readLine()) != null)
            if (!line.equals(previous)) {
                previous = line;
                out.append(line).append('\n');
                System.out.println(line);
            }

        //Check result
        if (jmeterRun.waitFor() == 0) {
            System.out.println("Success!");
//            System.exit(0);
        }

        //Abnormal termination: Log command parameters and output and throw ExecutionException
        System.err.println(command);
        System.err.println(out.toString());

    }

}
