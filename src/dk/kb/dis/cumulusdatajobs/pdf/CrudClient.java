package dk.kb.dis.cumulusdatajobs.pdf;

import java.util.Properties;


/**
 * <p>This class implements the HTTP stuff needed for the pdf generation
 * application.</p>
 * @author Sigfrid Lundberg (slu@kb.dk)
 * @version $Revision$
 */
public class CrudClient {
    Properties props = null;
    String     timeStamp = "";

    private static org.apache.log4j.Logger logger = 
	org.apache.log4j.Logger.getLogger(CrudClient.class);

    public CrudClient () {}

    public javax.xml.transform.stream.StreamSource recieveData(String newRequest) 
	throws java.io.IOException 
    {

	logger.info("About to retrieve " + newRequest);

	org.apache.commons.httpclient.HttpClient httpClient = 
	    new org.apache.commons.httpclient.HttpClient();

	httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

	//create a method object
	org.apache.commons.httpclient.methods.GetMethod get_method = 
	    new org.apache.commons.httpclient.methods.GetMethod(newRequest);

	get_method.setFollowRedirects(true);

	httpClient.executeMethod(get_method);

	/* The rest is to do whatever is needed to do after the transaction */

	this.setTimestamp(get_method.getResponseHeader("Last-Modified-Time-Stamp").getValue());

	int status = get_method.getStatusLine().getStatusCode();

	logger.info("response status:\t" + 
		    status + 
		    " (" + get_method.getStatusLine().toString()  + ")"); 

	if(logger.isDebugEnabled()) {
	    org.apache.commons.httpclient.Header[] clientResponseHeaders = 
		get_method.getResponseHeaders();
	    for(int i=0;i<clientResponseHeaders.length;i++) {
		logger.debug(clientResponseHeaders[i].getName() + ":\t" + clientResponseHeaders[i].getValue()); 
	    }
	}

	java.io.InputStream in      = get_method.getResponseBodyAsStream();

	
	return new javax.xml.transform.stream.StreamSource(in);


    }   

    public void setTimestamp(String timeStamp) {
	this.timeStamp = timeStamp;
	return;
    }

    public String getTimestamp() {
	return this.timeStamp;
    }

    /**
     * When we've made a PDF, and stored it, we'll need to update the database by sending targetUri to updateUri
     * together with a timestamp
     * @param updateUri  a URI to which we send data to update the database
     * @param targetUri the URI of the PDF we've just generated and which the database need to know.
     */
    public void submitData(String user, String updateUri, String targetUri) 
	throws java.io.IOException 
    {

	org.apache.commons.httpclient.HttpClient httpClient = 
	    new org.apache.commons.httpclient.HttpClient();

	httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
	httpClient.getParams().setParameter("http.protocol.single-cookie-header", true);
	httpClient.getParams().setCookiePolicy(
        org.apache.commons.httpclient.cookie.CookiePolicy.BROWSER_COMPATIBILITY );

	logger.info("Sending request to URI: " + updateUri); 

	//create a method object
	org.apache.commons.httpclient.methods.PostMethod post_method =
	    new org.apache.commons.httpclient.methods.PostMethod();

	post_method.setPath(updateUri);
	post_method.addParameter("pdfidentifier",targetUri);
	post_method.addParameter("lastmodified", this.getTimestamp());
	post_method.addParameter("user", "cop+pdf+creator+client");

	logger.info("creating object: " + post_method.getPath());

	post_method.setFollowRedirects(false);
	httpClient.executeMethod(post_method);

	// We read data from the request
	//in.close();

	post_method.releaseConnection();

	org.apache.commons.httpclient.Header[] responseHeaders = 
	    post_method.getResponseHeaders();

	int status = post_method.getStatusLine().getStatusCode();

	if(logger.isInfoEnabled()) {
	    logger.info("Transaction\n" + "updateUri:" + updateUri + "\ntargetUri:" +  targetUri + "\nresponse status:\t" + 
			status + 
			" (" + post_method.getStatusLine().toString()  + ")"); 
	}

	if(logger.isDebugEnabled()) {
	    org.apache.commons.httpclient.Header[] clientResponseHeaders = post_method.getResponseHeaders();
	    logger.debug("response:\n");
	    for(int i=0;i<clientResponseHeaders.length;i++) {
		logger.debug(clientResponseHeaders[i].getName() + ":\t" + clientResponseHeaders[i].getValue()); 
	    }
	}

	String responseBody = post_method.getResponseBodyAsString();

    }    
}
