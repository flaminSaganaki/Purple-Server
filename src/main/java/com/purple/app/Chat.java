package com.purple.app;

import com.purple.helper.GeneralHelper;
import com.purple.helper.Log2File;

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
			//String user_id = request.getParameter("user_id");
			String orientation = request.getParameter("preference");
			String group_id = request.getParameter("group_id");

			if (orientation!=null) {
				json = checkGroup(orientation);
			}else if (group_id!=null){
				json = doesGroupIdExist(group_id);
			}

		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (javax.servlet.ServletException e2){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		out.print(json.toString());
		out.close();
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		try {
			String orientation = request.getParameter("preference");
			String url = request.getParameter("url");
			if (orientation!=null && url!=null) {
				json.put("group_id", startGroup(orientation, url));
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				json.put("Error", "One or more of the required query parameters is missing.");
			}

		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			Log2File.log("IOException exception in doPost "+e);
		} catch (javax.servlet.ServletException e){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			Log2File.log("ServletException exception in doPost "+e);
		} catch (org.json.JSONException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			Log2File.log("JSONException exception in doPost "+e);
		}
		out.print(json.toString());
		out.close();
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		try {
			String group_id = request.getParameter("group_id");
			if (group_id!= null) {
				if (deleteGroup(group_id) < 1) {
					json.put("deleted", "false");
				} else {
					json.put("deleted", "true");
				}
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				json.put("Error", "One or more of the required query parameters is missing.");
			}

		} catch (org.json.JSONException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			Log2File.log("E in ouputFeedScreenIfChange2 - Getting All Conversations="+ e);
		}
		out.print(json.toString());
		out.close();
	}

	static JSONObject doesGroupIdExist(String group_id) throws IOException, ServletException {
		JSONObject json = new JSONObject();
		Connection con = null;
		Statement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet rs = null;
		try {
			Class.forName(JDBC_DRIVER);
			con =DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWD);
			stmt = con.createStatement();
			if (group_id != null ){
				String sql = "SELECT id FROM `group` WHERE id= ?";
				stmt2 = con.prepareStatement(sql);
				stmt2.setString(1, group_id);
				rs=stmt2.executeQuery();
				int num_users=0;
				if (rs.next()){
					json.put("exists", "true");
				}else{
					json.put("exists", "false");
				}
			}
		} catch (SQLException e) {
			Log2File.log("sql exception in doesGroupIdExist "+e);
		} catch (ClassNotFoundException e) {
			Log2File.log("Class not found E in doesGroupIdExist"+e);
		}
		catch (Exception e) {
			Log2File.log("E in doesGroupIdExist e="+e);
		}finally {
			try {
				GeneralHelper.closeRsStmtConn(rs, stmt, con);
			} catch (Exception e) {
				Log2File.log("sql exception doesGroupIdExist "+e);
			}
		}
		return json;
	}


	static JSONObject checkGroup(String orientation) throws IOException, ServletException {
		//JSONArray groupArray = new JSONArray();
		JSONObject json = new JSONObject();
		Log2File.log("checkGroup with orientation - "+ orientation);
		Connection con = null;
		Statement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet rs = null;
		try {
			Class.forName(JDBC_DRIVER);
			con =DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWD);
			stmt = con.createStatement();

			if (orientation != null ){
				String sql = "SELECT * FROM `group` WHERE num_users = 1 AND orientation = ? ORDER BY created ASC";
				stmt2 = con.prepareStatement(sql);
				if (orientation.equals("0")) {
					stmt2.setString(1, "1");
				} else {
					stmt2.setString(1, "0");
				}
			}

			rs=stmt2.executeQuery();
			if (rs.next()){
				Log2File.log("found an entry");
				String group_id = rs.getString(1);

				json.put("id", group_id);
				json.put("num_users", rs.getString(2));
				json.put("orientation", rs.getString(3));
				json.put("url", rs.getString(4));
				json.put("created", rs.getTimestamp(5));

				String sql2 = "UPDATE `group` SET num_users = 2 WHERE id = ?";
				stmt2 = con.prepareStatement(sql2);
				stmt2.setString(1, group_id);
				stmt2.executeUpdate();
			} else {
				Log2File.log("no entry found");
				//startGroup(orientation)
			}
		} catch (SQLException e) {
			Log2File.log("sql exception in checkGroup "+e);
		} catch (ClassNotFoundException e) {
			Log2File.log("Class not found E in checkGroup"+e);
		}
		catch (Exception e) {
			Log2File.log("E in checkGroup() e="+e);
		}finally {
			try {
				GeneralHelper.closeRsStmtConn(rs, stmt, con);
			} catch (Exception e) {
				Log2File.log("sql exception checkGroup() "+e);
			}
		}
		return json;
	}

	static long startGroup(String orientation, String url) throws IOException, ServletException{
		Log2File.log("startGroup() orientation - " + orientation + " url - " +url);
		long group_id = 0;
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			Class.forName(JDBC_DRIVER);
			con =DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWD);
			//stmt = con.createStatement();
			String updateQry="INSERT INTO `group` (orientation, url) VALUES (?, ?)";
			stmt = con.prepareStatement(updateQry, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, orientation);
			stmt.setString(2, url);
			int affectedRows = stmt.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Creating user failed, no rows affected.");
			}
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					group_id = generatedKeys.getLong(1);
				} else {
					throw new SQLException("Creating group failed, no ID obtained.");
				}
			}
		} catch (SQLException e) {
			Log2File.log("SQLExceptionE in startGroup "+e);
		} catch (ClassNotFoundException e) {
			Log2File.log("ClassNotFoundExceptionE in startGroup "+e);
		}
		catch (Exception e) {
			Log2File.log("Exception E in startGroup e="+e);
		}
		finally {
			GeneralHelper.closeRsStmtConn(null, stmt, con);
		}
		return group_id;
	}

	static int deleteGroup(String group_id) throws ServletException, IOException {
		Log2File.log("deleteGroup() group_id - " + group_id);
		Connection con = null;
		PreparedStatement stmt = null;
		int numOfDeleted = 0;
		try {
			Class.forName(JDBC_DRIVER);
			con =DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWD);
			String updateQry="DELETE FROM `group` WHERE id = ?";
			stmt = con.prepareStatement(updateQry);
			stmt.setString(1, group_id);
			numOfDeleted=stmt.executeUpdate();
		} catch (SQLException e) {
			Log2File.log("E in deleteRid "+e);
		} catch (ClassNotFoundException e) {
			Log2File.log("E in deleteRid"+e);
		}catch (Exception e) {
			Log2File.log("E in deleteRid="+e);
		}finally {
			GeneralHelper.closeRsStmtConn(null, stmt, con);
			try {
				if (stmt != null) { stmt.close(); }
			}catch (Exception e) {
				Log2File.log("E in deleteRid e="+e);
			}
			try {
				if (con != null) { con.close(); }
			}
			catch (Exception e) {
				Log2File.log("E in deleteRid e="+e);
			}
		}
		return numOfDeleted;
	}
}
