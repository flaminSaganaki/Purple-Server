package com.purple.app;

import com.purple.helper.GeneralHelper;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.text.DecimalFormat;

import javax.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

@WebServlet("/chat/*")
public class Chat extends HttpServlet {
	
	//static String S3PREFIX="https://s3.amazonaws.com/videotag/";
	static final String JDBC_DRIVER="com.mysql.jdbc.Driver";
	static final String DB_URL="jdbc:mysql://purplerds.cntsqx9tx2uv.us-east-1.rds.amazonaws.com:3306/purple?zeroDateTimeBehavior=convertToNull";
	static final String DB_USER="werunever";
	static final String DB_PASSWD="werunever!!";

	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException
	{
        response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();
		try {
			//String sessionToken = request.getHeader("sessionToken");
			String user_id = request.getParameter("user_id");
			String orientation = request.getParameter("preference");
			if (orientation == "0") {
				json.put("groups", checkUser("1"));
			} else {
				json.put("groups", checkUser("0"));
			}
			
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (javax.servlet.ServletException e2){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (org.json.JSONException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		out.print(json.toString());
		out.close();
	}
	
	static JSONArray checkUser(String orientation) throws IOException, ServletException {
		JSONArray groupArray = new JSONArray();
		
		Connection con = null;
        Statement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;
        try {
            Class.forName(JDBC_DRIVER);
            con =DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWD);
            stmt = con.createStatement();
            
            if (orientation != null ){
				String sql = "SELECT * FROM `group` WHERE num_users = 1 AND orientation = ?";
                stmt2 = con.prepareStatement(sql);
				if (orientation == "0") {
					stmt2.setString(1, "1");
				} else {
					stmt2.setString(1, "0");
				}
                
            }
            
            rs=stmt2.executeQuery();
            if (rs.next()){
                String this_convo_id = rs.getString(1);
                JSONObject json = new JSONObject();
                json.put("id", this_convo_id);
				json.put("num_users", rs.getString(2));
				json.put("orientation", rs.getString(3));
				json.put("url", rs.getString(4));
				json.put("created", rs.getTimestamp(5));
	
                groupArray.put(json);
            } else {
				
			}
        } catch (SQLException e) {
        } catch (ClassNotFoundException e) {
        }
        catch (Exception e) {
        }finally {
            try {
                GeneralHelper.closeRsStmtConn(rs, stmt, con);   
            } catch (Exception e) {
            }
        }
        return groupArray;
	}
}