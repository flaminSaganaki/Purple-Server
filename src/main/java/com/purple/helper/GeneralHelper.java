package com.purple.helper;

import java.io.*;
import javax.enterprise.inject.New;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.String;
import java.util.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import org.apache.commons.io.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.fileupload.ProgressListener;
import java.util.concurrent.TimeUnit;
//import org.apache.commons.lang.*;
//import vidlib-1.0-SNAPSHOT.jar.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import com.google.gdata.client.*;
import com.google.gdata.client.youtube.*;
import com.google.gdata.data.*;
import com.google.gdata.data.geo.impl.*;
import com.google.gdata.data.media.*;
import com.google.gdata.data.media.mediarss.*;
import com.google.gdata.data.youtube.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import org.apache.log4j.*;
import javapns.*;
import javapns.notification.*;
import javapns.Push;
import javapns.communication.exceptions.KeystoreException;
import javapns.communication.exceptions.CommunicationException;

import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Paths;


public class GeneralHelper{
   
    static String S3PREFIX="https://s3.amazonaws.com/videotag/";
    static final String JDBC_DRIVER="com.mysql.jdbc.Driver";
    static final String DB_URL="jdbc:mysql://versa.c8lmtgguroll.us-east-1.rds.amazonaws.com:3306/versa?zeroDateTimeBehavior=convertToNull";
    static final String DB_USER="louis_lapat";
    static final String DB_PASSWD="myDicks!1";
    
    public static void closeRsStmtConn(ResultSet rs, Statement stmt, Connection con){
        try { if (rs != null) rs.close(); } catch (Exception e) {};
        try { if (stmt != null) stmt.close(); } catch (Exception e) {};
        try { if (con != null) con.close(); } catch (Exception e) {};
    }
    
    public static String returnRandom6Digit(){
        char[] alphNum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        
        Random rnd = new Random();
        
        StringBuilder sb = new StringBuilder((100 + rnd.nextInt(900)) + "-");
        for (int i = 0; i < 3; i++)
            sb.append(alphNum[rnd.nextInt(alphNum.length)]);
        
        String id = sb.toString();
        
        //System.out.println(id);
        return id;
    }
    
    public static double fileExistsAndIsGreaterThanZero(String fileLocation){
        File f = new File(fileLocation);
        if(f.exists()){
            double bytes = f.length();
            double kilobytes = (bytes / 1024);
            double megabytes = (kilobytes / 1024);
            return bytes;
        }else {
            return -1;
        }
    }
    public static boolean fileExists(String fileLocation){
        File f = new File(fileLocation);
        if(f.exists()){
            return true;
        }else {
            return false;
        }
    }


    
    public static void setFileToFullWriteReadExecute(String pathToFile) throws IOException {
        //using PosixFilePermission to set file permissions 777
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        //add owners permission
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        //add group permissions
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        //add others permissions
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        
        Files.setPosixFilePermissions(Paths.get(pathToFile), perms);
    }
    
    public static void deleteAllFilesInFolder(File folder){
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                deleteMe(listOfFiles[i]);
                //System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                //System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }
    
    public static List<String> getEveryoneAlreadyInvited(String convo_id)throws IOException, ServletException{
        Log2File.log("getEveryoneAlreadyInvited - convo_id = " + convo_id);
        List<String> everyoneInvitedAlready=new ArrayList<String>();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement stmt2 = null;
        
        try {
            Class.forName(JDBC_DRIVER);
            con =DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWD);
            stmt = con.createStatement();
            String sql = "SELECT DISTINCT user_id FROM convo_members WHERE convo_id = ? ";
            stmt2 = con.prepareStatement(sql);
            stmt2.setString(1, convo_id);
            rs=stmt2.executeQuery();
            while (rs.next()){
                everyoneInvitedAlready.add(rs.getString(1));
            }
        } catch (SQLException e) {
            Log2File.log("sql exception in getEveryoneAlreadyInvited"+e);
        } catch (ClassNotFoundException e) {
            Log2File.log("Class not found E in getEveryoneAlreadyInvited"+e);
        }
        catch (Exception e) {
            Log2File.log("E in getEveryoneAlreadyInvited e="+e);
        }finally {
            try {
                if(rs != null) {
                    rs.close();
                    rs = null;
                }
                if(stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if(con != null) {
                    con.close();
                    con = null;
                }
            } catch (SQLException e) {
                Log2File.log("sql exception getEveryoneAlreadyInvited "+e);
            }
        }
        return everyoneInvitedAlready;
    }
    
    public static void deleteMe(File f){
        if (f.exists()){
            f.delete();
        }
    }
    public static void writeLinesToFile(String filename,
                                        String[] linesToWrite,
                                        boolean appendToFile) {
        
        PrintWriter pw = null;
        try {
            if (appendToFile) {
                //If the file already exists, start writing at the end of it.
                pw = new PrintWriter(new FileWriter(filename, true));
            }
            else {
                pw = new PrintWriter(new FileWriter(filename));
                //this is equal to:
                //pw = new PrintWriter(new FileWriter(filename, false));
            }
            for (int i = 0; i < linesToWrite.length; i++) {
                pw.println(linesToWrite[i]);
            }
            pw.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            //Close the PrintWriter
            if (pw != null)
                pw.close();
        }
        
        
    }
    
public static void changePermissionLocallay(String localDirectoryChmodString){
		
		//Log2File.log("copying movie file to local machine string="+scpStr);
		//exec(localDirectoryChmodString);
		try {
			Process p = Runtime.getRuntime().exec(localDirectoryChmodString);
			int status = p.waitFor();
			Log2File.log("changing permission finished with this status code= "+status);
			BufferedReader err=new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			try {
				while ((line = err.readLine()) != null) {
					Log2File.log("err="+line);
					//Log2File.log("copying finished movie err="+line);
					
				}
			} catch (Exception e) {
				Log2File.log("exception in changePermissionLocallay = "+e);
			}
			
		} catch (Exception e) {
			Log2File.log("exception in changePermissionLocallay = "+e);
			
		}
	}

}