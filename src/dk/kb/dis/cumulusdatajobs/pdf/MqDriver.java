package dk.kb.dis.cumulusdatajobs.pdf;

import dk.kb.dis.ConfigReader;
import javax.jms.*;
import gnu.getopt.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MqDriver extends Thread {

    /* 
      MQ stuff left as documentation 
      ******************************
      private static String url     = "http://disdev-01.kb.dk:8161/admin/";

      URL of the JMS server
      private static String url     = "tcp://disdev-01.kb.dk:61616";

      Name of the queue we will receive messages from
      private static String subject = "kb.cop.pdf";
    */

    // Other Mq Stuff
    private static Connection      connection = null;
    private static MessageConsumer consumer   = null;

    // the message from Mq is just a metadata url
    private volatile String   metadataUrl   = (String)null;
    private volatile String   styleUri      = (String)null;
    private volatile String   fopConfig     = (String)null;
    private volatile String   copUpdate     = (String)null;
    private volatile String   copPdfStorage = (String)null;
    private volatile String   copPdfExtUri  = (String)null;
    private volatile String   copPdfUser    = (String)null;
    private volatile String   notifyBackend = (String)null;
    private volatile MqDriver driverThread  = (MqDriver)null;

    static ConfigReader dojoConf = null;

    private static org.apache.log4j.Logger logger = 
	org.apache.log4j.Logger.getLogger(MqDriver.class);

    public MqDriver(String url, String styleUri, String outdir, String config) {
	this.metadataUrl    = url;
	this.styleUri       = styleUri;
	this.copPdfStorage  = outdir;
	this.fopConfig      = config;
    }

    public MqDriver(String url, ConfigReader dConf) {
	this.metadataUrl   = url;
	this.dojoConf      = dConf;
	this.styleUri      = dConf.getProperty("coppdf.xsl");
	this.notifyBackend = dConf.getProperty("coppdf.notify");
	this.fopConfig     = dConf.getProperty("coppdf.fopConfig");
	this.copUpdate     = dConf.getProperty("coppdf.update");
	this.copPdfStorage = dConf.getProperty("coppdf.storage");
	this.copPdfExtUri  = dConf.getProperty("coppdf.extUri");
	this.copPdfUser    = dConf.getProperty("coppdf.user");
    }

    public void myStop() {
        MqDriver me = driverThread;
        driverThread = null;
        me.interrupt();
    }

    public void run() {
	logger.info("The URL is " + this.metadataUrl + "\n");
	driverThread = (MqDriver)this.currentThread();
	// We will get the data via a CrudClient ()

	CrudClient client   = new  CrudClient ();
	ToPdfProcessor proc = new ToPdfProcessor();
	proc.setHttpClient(client);

	/**************** Creation of URIs and file names *****************/
	// What follows is just boring, but must be exactly right or the
	// components won't glue together. :( Indata is in the form

	// this.copPdfStorage: /data1/e-mat/cop/

	// copPdfExtUri 
	// http://www.kb.dk/e-mat/cop/

	// the metadataUrl will has this form
	// http://www.kb.dk/cop/syndication/manus/vmanus/2011/dec/ha/object81213/en/

	/************************* common stuff   ***********************/
	String src_url     = this.metadataUrl.replaceAll("^.*syndication/","");

	// src_url should now be 
	// manus/vmanus/2011/dec/ha/object81213/en/

	src_url = src_url.replaceAll("^(.*?)(object)([0-9]+)(.*)$","$1$2$3");
	// which removes the language. Leading to
	// src_url = manus/vmanus/2011/dec/ha/object81213

	/********** Now, first make the PDF URLs  ***********/
	
	String subDirs      = src_url.replaceAll("^(.+/)(.+/)([0-9][0-9][0-9][0-9]).*$","$1$2");
	// i.e., we remove the year and evertything to the right of it
	// leading to subDirs = manus/vmanus/

	String url = src_url.replaceAll("/","-");
	// leading to  manus-vmanus-2011-dec-ha-object81213 which we use as file name for the pdf

	logger.debug( this.metadataUrl + " became " + url);

	String root         = this.copPdfStorage;
	// could be like  root = /data1/e-mat/cop/

	String local        = subDirs + "/" + url + ".pdf";

	// local would be = manus/vmanus/manus-vmanus-2011-dec-ha-object81213.pdf

	String output       = root + local;
	// hence 
	// /data1/e-mat/cop/manus/vmanus/manus-vmanus-2011-dec-ha-object81213
	
	String contentUri   = this.copPdfExtUri + local;

	// since copPdfExtUri is http://www.kb.dk/e-mat/cop/
	// contentUri would be 
	// http://www.kb.dk/e-mat/cop/manus/vmanus/manus-vmanus-2011-dec-ha-object81213.pdf

	// since update URL is http://cop-02.kb.dk:8080/cop/update/
	// Then, calculate where we should send contentUri to

	String updateUri    = this.copUpdate + src_url;

	// or more human readable
	// http://cop-02.kb.dk:8080/cop/update/manus/vmanus/2011/dec/ha/object81213

	// here we create the directory
	// root + / + subDirs = /data1/e-mat/cop/ +  manus/vmanus/ =
	// /data1/e-mat/cop/manus/vmanus/ 

	java.io.File dir = new java.io.File(root + "/" + subDirs);
	dir.mkdirs() ;
	logger.debug( this.metadataUrl + " complete path " + output );
	
	// You then set the four parameters.

	proc.setStyleSheetURI(this.styleUri);
	if(this.metadataUrl.indexOf("format=") > 0) {
	    proc.setContentSourceURI(this.metadataUrl);
	} else {
	    String mods = this.metadataUrl + "?format=mods";
	    proc.setContentSourceURI(mods);
	}
	proc.setDestinationFile(output);
	proc.setFopConfFile(this.fopConfig);

	// Finally you save the pdf file and notify COP backend that it is been completed.

	if(proc.savePdf()) {
	    logger.info("Successfully transformed " + this.metadataUrl + " into " + output );
	    if(this.notifyBackend.equals("yes")) {
		if(this.copPdfUser.length()>0 && updateUri.length()>0 && contentUri.length()>0) {
		    try {
			client.submitData(this.copPdfUser, updateUri, contentUri);
		    } catch(java.io.IOException ioproblem) {
			logger.fatal("Couldn't notify cop because of " + ioproblem.getMessage());
		    }
		} else {
		    logger.fatal("lacking data so I cannot notify COP, even though I've created the PDF.");
		}		
	    } else {
		logger.info("not notifying cop backend about pdf location");
	    }
	} else {
	    logger.warn("Failure to transform " + this.metadataUrl + " into " + output );
	}
	this.myStop();
	if(this.isAlive()) {
	    logger.info("The assassin hasn't been here yet");
	} else {
	    logger.info("MqDriver RIP");
	}
	return;
    }

    public static void main(String[] args) {
        // Getting JMS connection from the server

	LongOpt[] options = new LongOpt[5];
     
	StringBuffer sb = new StringBuffer();
	options[0] = new LongOpt("server",   LongOpt.REQUIRED_ARGUMENT,  sb, 's');
	options[1] = new LongOpt("queue",    LongOpt.REQUIRED_ARGUMENT,  sb, 'q');
	options[2] = new LongOpt("xsl",      LongOpt.REQUIRED_ARGUMENT,  sb, 'x');
	options[3] = new LongOpt("config",   LongOpt.REQUIRED_ARGUMENT,  sb, 'c');

	logger.info("starting cop-pdf services\n");	

	ConfigReader dConf = ConfigReader.getInstance();
	String server  = dConf.getProperty("coppdf.server").length()>0 ?dConf.getProperty("coppdf.server") : ""; 
	String queue   = dConf.getProperty("coppdf.queue").length()>0  ?dConf.getProperty("coppdf.queue")  : "";
	String threads = dConf.getProperty("coppdf.threads").length()>0?dConf.getProperty("coppdf.threads"): "25";
	String napLen  = dConf.getProperty("coppdf.naplen").length()>0 ?dConf.getProperty("coppdf.naplen"): "10";

	String xsl       = "";
	String fopConfig = "";
	String outDir    = "./"; 

	char   opt; 
	int    c;
	Getopt g   = new Getopt("term_extractor",args,"o:q:s:c:",options);
	while( (c = g.getopt()) != -1) {
	    switch(c) {
	    case 0:
		opt = (char)(new Integer(sb.toString())).intValue();
		String val = g.getOptarg();
		if(opt == 'q') {
		    queue     = val;
		} else if (opt == 's') {
		    server = val;
		} else if (opt == 'x') {
		    xsl = val;
		} else if (opt == 'c') {
		    fopConfig = val;
		}
		break;
	    case 'q': 
		queue  = g.getOptarg();
		break;
	    case 's': 
		server = g.getOptarg();
		break;
	    case 'x':
		xsl  = g.getOptarg();
		break;
	    case 'c':
		fopConfig  = g.getOptarg();
		break;
	    }
	}

	try {

	    ConnectionFactory connectionFactory
		= new ActiveMQConnectionFactory(server);

	    connection = connectionFactory.createConnection();
	    connection.start();

	    // Creating session for seding messages
	    Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);

	    // Getting the queue
	    Destination destination = session.createQueue(queue);

	    // MessageConsumer is used for receiving (consuming) messages
	    consumer = session.createConsumer(destination);

	} catch (JMSException jmsProblem) {
	    logger.error("Failed to initialize consumer\n");
	    logger.error(jmsProblem.getMessage());
	}

	try {
	    int count     = 0;
	    int mxThreads = (new java.lang.Integer(threads)).intValue();
	    int nap       = (new java.lang.Integer( napLen)).intValue();
	    while(true) {
		count = java.lang.Thread.activeCount();
		if(count > mxThreads) {
		    logger.warn("Number of threads " + count);
		    try {SECONDS.sleep(nap);}
		    catch (InterruptedException wokeUp ) {
			logger.warn("Something woke me up");
		    }
		} else {
		    logger.debug("Threads " + count);
		}
		// Here we receive the message.
		// By default this call is blocking, which means it will wait
		// for a message to arrive on the queue.
		Message message = consumer.receive();

		// There are many types of Message and TextMessage
		// is just one of them. Producer sent us a TextMessage
		// so we must cast to it to get access to its .getText()
		// method.
		if (message instanceof TextMessage) {
		    TextMessage textMessage = (TextMessage) message;

		    String metadata = textMessage.getText().trim();

		    // starting a new thread for running the actual transform

		    MqDriver driver = null;
		    if(dConf.getProperty("dojo.environment").length()>0) {
			driver = new MqDriver(metadata,dConf);
		    } else {
			driver = new MqDriver(metadata,xsl, outDir, fopConfig);
			//,copUpdate,copPdfStorage,copPdfExtUri)
		    }
		    driver.start();
		}
	    } // Ends while(true)
	} catch (JMSException jmsProblem) {
	    logger.error("Consumer failed to read from queue\n");
	    logger.error(jmsProblem.getMessage());
	}

	try {
	    connection.close();
	} catch (JMSException jmsProblem) {
	    logger.error("Failed to close from jms session\n");
	    logger.error(jmsProblem.getMessage());
	}
    }
}
