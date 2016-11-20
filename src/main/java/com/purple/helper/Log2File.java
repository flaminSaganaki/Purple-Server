package com.purple.helper;

import java.io.*;

public class Log2File  {
	
	public static void log(String logText){
        try{
			LineNumberReader  lnr = new LineNumberReader(new FileReader(new File("/var/lib/tomcat8/webapps/versa/log/newLog.txt")));
			lnr.skip(Long.MAX_VALUE);
			if(lnr.getLineNumber()>5000){
				PrintWriter writer = new PrintWriter(new File("/var/lib/tomcat8/webapps/versa/log/newLog.txt"));
				writer.print("");
				writer.close();
			}
        }catch(Exception e){
            writeLinesToFile("/var/lib/tomcat8/webapps/versa/log/newLog.txt", new String[] {"Exception writing Log2File.log"}, true);
        }
		writeLinesToFile("/var/lib/tomcat8/webapps/versa/log/newLog.txt", new String[] {logText}, true);
	}
    
    public static void logEncodingError(String logText){
        try{
            LineNumberReader  lnr = new LineNumberReader(new FileReader(new File("/var/lib/tomcat8/webapps/versa/log/encodingErrorLog.txt")));
            lnr.skip(Long.MAX_VALUE);
            if(lnr.getLineNumber()>5000){
                PrintWriter writer = new PrintWriter(new File("/var/lib/tomcat8/webapps/versa/log/encodingErrorLog.txt"));
                writer.print("");
                writer.close();
            }
        }catch(Exception e){
            writeLinesToFile("/var/lib/tomcat8/webapps/versa/log/newLog.txt", new String[] {"Exception writing ENCODING LOG"}, true);
        }
        writeLinesToFile("/var/lib/tomcat8/webapps/versa/log/encodingErrorLog.txt", new String[] {logText}, true);
    }
	
	public static void logVTQ(String logText){
        try{
			LineNumberReader  lnr = new LineNumberReader(new FileReader(new File("/var/lib/tomcat8/webapps/versa/log/vtq.txt")));
			lnr.skip(Long.MAX_VALUE);
			if(lnr.getLineNumber()>5000){
				PrintWriter writer = new PrintWriter(new File("/var/lib/tomcat8/webapps/versa/log/vtq.txt"));
				writer.print("");
				writer.close();
			}
        }catch(Exception e){
            writeLinesToFile("/var/lib/tomcat8/webapps/versa/log/vtq.txt", new String[] {"Exception writing Log2File.logVTQ"}, true);
        }
		writeLinesToFile("/var/lib/tomcat8/webapps/versa/log/vtq.txt", new String[] {logText}, true);
	}
	
	public static void clearLog() {
		try{
			PrintWriter writer = new PrintWriter(new File("/var/lib/tomcat8/webapps/versa/log/newLog.txt"));
			writer.print("");
			writer.close();
        }catch(Exception e){
            writeLinesToFile("/var/lib/tomcat8/webapps/versa/log/newLog.txt", new String[] {"Exception writing Log2File.log"}, true);
        }
	}
	
	 public static void writeLinesToFile(String filename, String[] linesToWrite, boolean appendToFile) {
		PrintWriter pw = null;
		try {
			if (appendToFile) {
				//If the file already exists, start writing at the end of it.
				pw = new PrintWriter(new FileWriter(filename, true));
			} else {
				pw = new PrintWriter(new FileWriter(filename));
			}
			for (int i = 0; i < linesToWrite.length; i++) {
				pw.println(linesToWrite[i]);
			}
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (pw != null)
				pw.close();
		}        
	}
	
}