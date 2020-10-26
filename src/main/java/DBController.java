import java.sql.*;


public class DBController {
    private final String driverName = "org.sqlite.JDBC";
    private final String dbUrl = "jdbc:sqlite:sql/database.db";
    Connection conn;

    public DBController() {
        try {
            Class.forName(driverName);
            conn = DriverManager.getConnection(dbUrl);
        } catch (Exception e) {
            // this is a little lazy but its okay
            e.printStackTrace();
            throw new RuntimeException("FAILED TO CONNECT TO DB AT RUNTIME");
        }
    }

    /**
     * Get pwd given the uid
     * return null if not found
     * @param uid
     */
    public String getPwd(String uid) {
        String pwdQueryString =
                "select pwd from users where uid=?";
        String pwd = null;

        try(PreparedStatement pwdStatement = conn.prepareStatement(pwdQueryString)) {
            pwdStatement.setString(1, uid);
            ResultSet res = pwdStatement.executeQuery();
            if(res.next()) {
                pwd = res.getString(1);
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

        return pwd;
    }

    // TODO
    public boolean isPrivileged(String uid) {
        return true;
    }


}
