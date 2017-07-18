package dk.kb.dis.cumulusdatajobs.pdf;

import org.apache.fop.apps.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import org.apache.xalan.processor.TransformerFactoryImpl;
import javax.xml.transform.TransformerFactory;
import org.apache.avalon.framework.configuration.*;
import org.xml.sax.SAXException;
import java.io.*;
import java.net.*;
import gnu.getopt.*;

/**
 * This class accepts URIs of a stylesheet and an XML document as
 * parameters. Furthermore it requires a file name where it will print a
 * resulting PDF content;
 *
 * You typically start by instantiating the object
 *
 * ToPdfProcessor proc = new ToPdfProcessor();
 *
 * You then set the four parameters.
 *
 * proc.setStyleSheetURI(style URI);
 * proc.setContentSourceURI(input xml file URI);
 * proc.setDestinationFile(output file name with full path);
 * proc.setFopConfFile(fopConfig full name and path);
 *
 * Finally you save the pdf file
 *
 * proc.savePdf();
 *
 *
 */
public class ToPdfProcessor {

    private static org.apache.log4j.Logger logger = 
	org.apache.log4j.Logger.getLogger(ToPdfProcessor.class);


    // Step 1: Construct a FopFactory
    // (reuse if you plan to render multiple documents!)

    private FopFactory             fopFactory       = null;
    private TransformerFactoryImpl transfactoryImpl = new TransformerFactoryImpl();
    private TransformerFactory     xslFactory       = null;
    private String                 fileName         = "";
    private String                 sourceUri        = "";
    private String                 sheetUri         = "";
    private String                 fopConfFile      = "";

    CrudClient client = null;

    public  ToPdfProcessor () {

    }

    boolean savePdf() {

	// Let's be pessimistic for a change

	boolean success = false;

	// Step 2: Set up output stream.

	OutputStream out = getDestination();

	logger.debug("Done creating output stream\n");

	if(this.xslFactory == null) {
	    this.xslFactory = transfactoryImpl.newInstance();
	}

	try {

	    Configuration cfg = null;
	    String cfgFile = this.getFopConfFile();
            logger.debug("about to read " + cfgFile);
	    try {
		if(cfgFile.length() > 0) {
		    DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
		    File file = new File(new URI(cfgFile));
		    try {
			cfg = cfgBuilder.buildFromFile(file);
		    } catch(IOException ioProblem){
			logger.error("io problem");
			ioProblem.printStackTrace();
		    }
		}
	    } catch(ConfigurationException confProblem) {
		logger.error("Configuration problem");
	    } catch(SAXException malformedCfg) {
		logger.error("SAXException malformedCfg");
	    }

	    if(cfg == null) {
		logger.error("fop not properly configured ");
	    }

	    // Step 3: Construct fop with desired output format

	    if(fopFactory == null) {
		fopFactory = FopFactory.newInstance();
	    }

	    if(cfg != null) {
		fopFactory.setUserConfig(cfg);
	    }

	    logger.debug("made a fop factory\n");

	    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

	    // Step 4: Setup JAXP with style sheet
	    Source xslt = new StreamSource(this.getStyleSheetURI().toString());

	    logger.debug("made xslt stream source" + this.getStyleSheetURI().toString() + "\n");

	    Transformer transformer = xslFactory.newTransformer(xslt);

	    // Step 5: Setup input and output for XSLT transformation
	    // Setup input stream

	    Source src = this.getHttpClient().recieveData(this.getContentSourceURI().toString());

	    logger.debug("made a content source\n");

	    logger.debug("timestamp " + this.getHttpClient().getTimestamp() );

	    // Resulting SAX events (the generated FO) must be piped through to FOP
	    Result res = new SAXResult(fop.getDefaultHandler());

	    logger.debug("make a SAXResult\n");

	    // Step 6: Start XSLT transformation and FOP processing
	    transformer.transform(src, res);
	    
	    logger.debug("do the transform\n");

	    success = true;

	} catch(URISyntaxException malformedURI) {
	    logger.error(malformedURI.getMessage());
	} catch(FOPException fopProb) {
	    logger.error(fopProb.getMessage());
	} catch(TransformerConfigurationException xalanconfig) {
	    logger.debug(xalanconfig.getMessage());
	} catch(TransformerException xalanproblem) {
	    logger.debug(xalanproblem.getMessage());
	} catch(IOException ioProblem){
	    logger.error(ioProblem.getMessage());
	} finally {
	    if(out != null) {
		try {
		    out.close();
		} catch(java.io.IOException ioException) { 
		    logger.error(ioException.getMessage());
		}
	    }
	}

	if(out != null) {
	    try {
		out.close();
	    } catch(java.io.IOException ioException) { 
		logger.error(ioException.getMessage());
	    }
	}

	return success;

    }

    public void setHttpClient(CrudClient client) {
	this.client = client;
    }

    public CrudClient getHttpClient() {
	return this.client;
    }


    public void setFopConfFile(String fileName) {
	this.fopConfFile = fileName;
    }

    public String getFopConfFile() {
	return this.fopConfFile;
    }

    public void setStyleSheetURI(String sheetUri) {
	this.sheetUri = sheetUri;
    }
    
    public URI getStyleSheetURI() throws URISyntaxException {
	return new URI(sheetUri);
    }

    public void setContentSourceURI(String sourceUri) {
	this.sourceUri = sourceUri;
    }

    public URI getContentSourceURI()  throws URISyntaxException {
	return new URI(sourceUri);
    }

    public void setDestinationFile(String fileName) {
	this.fileName = fileName;
    }

    public String getDestinationFile() {
	return this.fileName;
    }

    private OutputStream getDestination() {
	OutputStream stream = null;
	// Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
	try {
	    stream = new BufferedOutputStream(new FileOutputStream(new File(this.getDestinationFile())));
	} catch(java.io.FileNotFoundException fileNotFound) {
	}
	return  stream;
    }

    public static void main(String[] args) throws Exception {

	LongOpt[] options = new LongOpt[5];
     
	StringBuffer sb = new StringBuffer();
	options[0] = new LongOpt("output",   LongOpt.REQUIRED_ARGUMENT,  sb, 'o');
	options[1] = new LongOpt("input",    LongOpt.REQUIRED_ARGUMENT,  sb, 'i');
	options[2] = new LongOpt("style",    LongOpt.REQUIRED_ARGUMENT,  sb, 's');
	options[3] = new LongOpt("config",   LongOpt.REQUIRED_ARGUMENT,  sb, 'c');

	String output    = "";
	String input     = "";
	String style     = "";
	String fopConfig = "";

	char   opt; 
	int    c;
	Getopt g   = new Getopt("term_extractor",args,"o:i:s:",options);
	while( (c = g.getopt()) != -1) {
	    switch(c) {
	    case 0:
		opt = (char)(new Integer(sb.toString())).intValue();
		String val = g.getOptarg();
		if(opt == 'i') {
		    input     = val;
		} else if (opt == 'o') {
		    output    = val;
		} else if (opt == 's') {
		    style     = val;
		} else if (opt == 'c') {
		    fopConfig = val;
		}
		break;
	    case 'i': 
		input  = g.getOptarg();
		break;
	    case 'o': 
		output = g.getOptarg();
		break;
	    case 's':
		style  = g.getOptarg();
		break;
	    case 'c':
		fopConfig  = g.getOptarg();
		break;
	    }
	}
	
	if(input.length()*output.length()*style.length() > 0) {
	    CrudClient httpClient = new CrudClient();
	    ToPdfProcessor proc = new ToPdfProcessor();
	    proc.setHttpClient(httpClient);
	    proc.setStyleSheetURI(style);
	    proc.setContentSourceURI(input);
	    proc.setDestinationFile(output);
	    proc.setFopConfFile(fopConfig);
	    System.err.print("style  = " + style     + "\n" +
			     "input  = " + input     + "\n" +
			     "output = " + output    + "\n" +
			     "config = " + fopConfig + "\n" );
	    
	    if(proc.savePdf()) {
	    } else {
		System.err.print("Something went wrong when saving pdf\n");
	    }
	} else {
	    System.err.print("usage: --input <data uri> --output <output file> --style <style sheet uri> --config <fop configuration file>\n");
	}

    }

}
