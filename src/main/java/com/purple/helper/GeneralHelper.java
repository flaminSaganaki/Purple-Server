package com.purple.helper;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Connection;



public class GeneralHelper{

    public static void closeRsStmtConn(ResultSet rs, Statement stmt, Connection con){
        try { if (rs != null) rs.close(); } catch (Exception e) {};
        try { if (stmt != null) stmt.close(); } catch (Exception e) {};
        try { if (con != null) con.close(); } catch (Exception e) {};
    }


}
