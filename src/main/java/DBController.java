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

    public boolean postQuestion(String pid, Date date, String title, String body, String poster) {
        String insertPostsQuery = "INSERT INTO posts values(?, ?, ?, ?, ?);";
        String insertQuestionsQuery = "INSERT INTO questions values(?, ?);";
        try (PreparedStatement insertPostsStatement = conn.prepareStatement(insertPostsQuery);
            PreparedStatement insertQuestionsStatement = conn.prepareStatement(insertQuestionsQuery)) {
            insertPostsStatement.setString(1, pid);
            insertPostsStatement.setDate(2, date);
            insertPostsStatement.setString(3, title);
            insertPostsStatement.setString(4, body);
            insertPostsStatement.setString(5, poster);
            insertQuestionsStatement.setString(1, pid);
            insertQuestionsStatement.setString(2, null);

            // TODO maybe merge statements so both need to pass for it to work look into rollback
            Boolean result1 = insertPostsStatement.execute();
            Boolean result2 = insertQuestionsStatement.execute();
            return result1 && result2;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("Insert into posts failed", throwables);
        }
    }
}
