import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class DBController {
    private final String driverName = "org.sqlite.JDBC";
    private final String dbUrl = "jdbc:sqlite:sql/database.db";
    Connection conn;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyy-MM-dd");

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
                "select pwd from users where uid like ?";
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

    /**
     * Returns null if no such uid exists
     * @param uid
     * @return
     */
    public String getUid(String uid) {
        String uidQueryString =
                "select uid from users where uid like ?";
        String resId = null;

        try(PreparedStatement pwdStatement = conn.prepareStatement(uidQueryString)) {
            pwdStatement.setString(1, uid);
            ResultSet res = pwdStatement.executeQuery();
            if(res.next()) {
                resId = res.getString(1);
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

        return resId;
    }

    public void insertUser(String[] details) {
        String insertUserQuery = "INSERT INTO users values(?, ?, ?, ?, ?);";
        Date date = new Date(Calendar.getInstance().getTime().getTime());
        try (PreparedStatement insertUserStatement = conn.prepareStatement(insertUserQuery)) {
            insertUserStatement.setString(1, details[0]);
            insertUserStatement.setString(2, details[1]);
            insertUserStatement.setString(3, details[2]);
            insertUserStatement.setString(4, details[3]);
            insertUserStatement.setString(5, dateFormatter.format(date));
            insertUserStatement.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("could not insert into users", throwables);
        }
    }

    public boolean isPrivileged(String uid) {
        String checkPrivilegedQueryString =
                "select uid from privileged where uid like ?";

        String retId;

        try(PreparedStatement pwdStatement = conn.prepareStatement(checkPrivilegedQueryString)) {
            pwdStatement.setString(1, uid);
            ResultSet res = pwdStatement.executeQuery();
            if(res.next()) {
                retId = res.getString(1);
            } else {
                return false;
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

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

    /**
     * Checks if the badge exists, returns null if no such badge
     * else return the badge name
     * @param bname
     * @return
     */
    public String getBadge(String bname) {
        String getBadgeQuery =
                "select bname from badges where bname like ?";
        String badge = null;

        try(PreparedStatement badgeStatement = conn.prepareStatement(getBadgeQuery)) {
            badgeStatement.setString(1, bname);
            ResultSet res = badgeStatement.executeQuery();
            if(res.next()) {
                badge = res.getString(1);
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

        return badge;
    }

    public boolean giveBadge(String bname, String uid) {
        String giveBadgeQuery =
                "insert into ubadges values(?, ?, ?)";

        Date date = new Date(Calendar.getInstance().getTime().getTime());
        try(PreparedStatement badgeStatement = conn.prepareStatement(giveBadgeQuery)) {
            badgeStatement.setString(1, uid);
            badgeStatement.setString(2, dateFormatter.format(date));
            badgeStatement.setString(3, bname);
            return badgeStatement.execute();
//            ResultSet res = badgeStatement.executeQuery();
//            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }
    }

    public boolean checkUniqueBadge(String bname, String uid) {
        String checkBadgeQuery =
                "select * from ubadges where uid like ? and bdate like ? and bname like ?";

        Date date = new Date(Calendar.getInstance().getTime().getTime());
        try(PreparedStatement badgeStatement = conn.prepareStatement(checkBadgeQuery)) {
            badgeStatement.setString(1, uid);
            badgeStatement.setString(2, dateFormatter.format(date));
            badgeStatement.setString(3, bname);
            ResultSet res = badgeStatement.executeQuery();
            if(res.next()) {    // exists entry
                System.out.println("this has smth");
                res.close();
                return false;
            } else {
                System.out.println("no more");
                res.close();
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }
    }

}
