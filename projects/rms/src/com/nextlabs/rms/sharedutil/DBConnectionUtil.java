package com.nextlabs.rms.sharedutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionUtil {

  public static boolean checkConnection(String connectString, String user, String pwd)throws Exception
  {
    ConnectionInfo connInfo = new ConnectionInfo(connectString, user, pwd);
    return connInfo.testConnection();
  }
  
  private static class ConnectionInfo
  {
    private String url;
    private String userName;
    private String passWord;
    private String dbType;
    private String driverClass;
    private static final String DB_ORACLE = "oracle";
    private static final String DB_SQL_SERVER = "sqlserver";
    private static final String DB_MY_SQL = "mysql";
    private static final String DB_POSTGRE_SERVER = "postgresql";
    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String SQL_SERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String MY_SQL_DRIVER = "com.mysql.jdbc.Driver";
    private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    
    public ConnectionInfo(String connectStr, String uName, String pw)
    {
      this.userName = uName;
      this.passWord = pw;
      setDBType(connectStr);
    }
    
    private void setDBType(String connectStr)
    {
      if (connectStr == null) {
        throw new IllegalArgumentException("Connection String cannot be null");
      }
      String lowerConnectStr = connectStr.toLowerCase();
      if (lowerConnectStr.startsWith(DB_ORACLE))
      {
        this.dbType = DB_ORACLE;
        this.driverClass = ORACLE_DRIVER;
      }
      else if (lowerConnectStr.startsWith(DB_SQL_SERVER))
      {
        this.dbType = DB_SQL_SERVER;
        this.driverClass = SQL_SERVER_DRIVER;
      }
      else if(lowerConnectStr.startsWith(DB_MY_SQL)){
    	  this.dbType = DB_MY_SQL;
          this.driverClass = MY_SQL_DRIVER;
      }
      else if (lowerConnectStr.startsWith(DB_POSTGRE_SERVER))
      {
        this.dbType = DB_POSTGRE_SERVER;
        this.driverClass = POSTGRESQL_DRIVER;
      }
      else
      {
        throw new IllegalArgumentException("Unsupported prefix in connect string: " + connectStr);
      }
      this.url = ("jdbc:" + connectStr);
    }
    
    public boolean testConnection()
      throws Exception
    {
      Connection connection = null;
      boolean connected = false;
      try
      {
        Class.forName(this.driverClass);
        connection = DriverManager.getConnection(this.url, this.userName, this.passWord);
        
        connected = true;
      }
      catch (SQLException ex)
      {
        throw new Exception(ex.getMessage());
      }
      finally
      {
        if (connection != null)
        {
          try
          {
            connection.close();
          }
          catch (SQLException se) {}
          connection = null;
        }
      }
      return connected;
    }
  }
  
  public static void main(String[] args) throws Exception
  {
    if ((args == null) || (args.length < 3))
    {
      System.out.println("Usage:");
      System.out.println("Windows:");
      System.out.println("     java -cp <Tomcat_dir>/webapps/RMS/WEB-INF/lib/mysql-connector-java-5.1.38.jar;<Tomcat_dir>/webapps/RMS/WEB-INF/lib/ojdbc7.jar;<Tomcat_dir>/webapps/RMS/WEB-INF/lib/RMSUtil.jar;<Tomcat_dir>/webapps/RMS/WEB-INF/lib/sqljdbc41.jar com.nextlabs.rms.sharedutil.DBConnectionUtil [database_connection_string] [database_username] [database_password]");
      System.out.println("Linux:");
      System.out.println("     java -cp <Tomcat_dir>/webapps/RMS/WEB-INF/lib/mysql-connector-java-5.1.38.jar:<Tomcat_dir>/webapps/RMS/WEB-INF/lib/ojdbc7.jar:<Tomcat_dir>/webapps/RMS/WEB-INF/lib/RMSUtil.jar:<Tomcat_dir>/webapps/RMS/WEB-INF/lib/sqljdbc41.jar com.nextlabs.rms.sharedutil.DBConnectionUtil [database_connection_string] [database_username] [database_password]");
    }
    else
    {
      String url = args[0];
      String user = args[1];
      String pwd = args[2];
      boolean connectionOk = checkConnection(url, user, pwd);
      System.out.println("" + connectionOk);
    }
  }
}
