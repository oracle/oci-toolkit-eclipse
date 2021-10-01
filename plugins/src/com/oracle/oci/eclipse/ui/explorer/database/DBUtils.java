package com.oracle.oci.eclipse.ui.explorer.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ErrorHandler;

public class DBUtils {

	public static final String TOOL_PROPERTIES_FILE_NAME = "tool.properties";
	public final static String TOOL_PROPERTIES_KEY_ADMIN_PASSWORD_SECURE_KEY_NAME = "tool.properties.key.admin.password.secure.key.name";
	private final static Object toolPropsLock = new Object();
	
    public static void copyPasswordToClipboard(Display d, String password) {
        Clipboard clipboard = new Clipboard(d);
        TextTransfer.getInstance();
        TextTransfer transfer = TextTransfer.getInstance();
        Object[] data = new Object[] {password};
        clipboard.setContents(data, new Transfer[] {transfer});
    }

    public static boolean writeToToolProperties(File toolPropsDir, Properties props)
    {
    	synchronized(toolPropsLock) {
	        Properties allProps = new Properties();
	        File toolPropFile = new File(toolPropsDir, TOOL_PROPERTIES_FILE_NAME);
	        if (toolPropFile.exists())
	        {
		        try (FileInputStream fis = new FileInputStream(toolPropFile))
		        {
		        	allProps.load(fis);
		        } catch (IOException e) {
		        	ErrorHandler.logErrorStack("Error opening tool.properties", e);
		        	return false;
				}
	        }
	        
	        try (FileOutputStream fos = new FileOutputStream(toolPropFile))
	        {
	        	allProps.putAll(props); // update with any changes
	        	allProps.store(fos, "Extra info for tooling");
	        } catch (IOException e) {
	        	ErrorHandler.logErrorStack("Error storing tool.properties", e);
	        	return false;
			}
	        return true;
    	}
    }
    
    public static Properties readToolProperties(File toolPropsDir) throws IOException
    {
    	synchronized(toolPropsLock) {
    		Properties allProps = new Properties();
    		
    		File toolPropsFile = new File(toolPropsDir, TOOL_PROPERTIES_FILE_NAME);
    		if (toolPropsFile.exists()) {
				try (FileInputStream fis = new FileInputStream(toolPropsFile))
	    		{
	    			allProps.load(fis);
	    		} 
    		}
    		return allProps;
    	}
    }
}
