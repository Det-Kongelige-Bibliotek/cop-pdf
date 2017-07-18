package dk.kb.dis;

import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: jac
 * Date: 11-05-14
 * Time: 22:36
 * This class reads the propertyfile on when initialized.
 *
 *
 */
public class ConfigReader {

    private static org.apache.log4j.Logger logger = 
	org.apache.log4j.Logger.getLogger(ConfigReader.class);

    Map<String, String> envMap = System.getenv();

    // private static final String CONFIG_NAME = "kb.cop-pdf.config";
    private static ConfigReader ourInstance = new ConfigReader();

    // p holds the properties of this project
    Properties prop = new Properties();

    public static ConfigReader getInstance() {
        return ourInstance;
    }

    private ConfigReader() {
        String confFile = envValue("CONFIG_NAME");
	String message  = "";
	logger.debug("CONFIG_NAME " + confFile);
        try {
	    message = "reading " + confFile + "\n";
	    prop.load(new FileInputStream(new File(confFile)));
        } catch (FileNotFoundException e) {
	    message = "Cannot find " + confFile + "\n" + e.getMessage();
	    System.err.print(message);
	    logger.fatal(message);
	    System.exit(1);
        } catch (IOException e) {
	    message = "Cannot open or read " + confFile + "\n" + e.getMessage();
	    System.err.print(message);
	    logger.fatal(message);
	    System.exit(1);
        }
	logger.debug("environment is " +  prop.getProperty("dojo.environment"));
    }

    public String getProperty(String key){
      return prop.getProperty(key).trim() ;
    }

   
    public String envValue(String arg) {
	return envMap.get(arg);
    }



}
