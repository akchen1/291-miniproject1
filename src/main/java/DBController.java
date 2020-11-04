import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DBController {
    private final String driverName = "org.sqlite.JDBC";
    Connection conn;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private String dbUrl = "jdbc:sqlite:%s";

    public DBController(String dbName) {
        dbUrl = String.format(dbUrl, dbName);
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
     *
     * @param uid
     */
    public String getPwd(String uid) {
        String pwdQueryString =
                "select pwd from users where uid like ?";
        String pwd = null;

        try (PreparedStatement pwdStatement = conn.prepareStatement(pwdQueryString)) {
            pwdStatement.setString(1, uid);
            ResultSet res = pwdStatement.executeQuery();
            if (res.next()) {
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
     *
     * @param uid
     * @return
     */
    public String getUid(String uid) {
        String uidQueryString =
                "select uid from users where uid like ?";
        String resId = null;

        try (PreparedStatement pwdStatement = conn.prepareStatement(uidQueryString)) {
            pwdStatement.setString(1, uid);
            ResultSet res = pwdStatement.executeQuery();
            if (res.next()) {
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

        try (PreparedStatement pwdStatement = conn.prepareStatement(checkPrivilegedQueryString)) {
            pwdStatement.setString(1, uid);
            ResultSet res = pwdStatement.executeQuery();
            if (res.next()) {
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

            Boolean result1 = insertPostsStatement.execute();
            Boolean result2 = insertQuestionsStatement.execute();
            return result1 && result2;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("Insert into posts failed", throwables);
        }
    }

    public boolean postAnswer(String qPid, String apid, String title, String body, String poster) {
        String insertPostsQuery = "INSERT INTO posts values(?, ?, ?, ?, ?)";
        String insertAnswerQuery = "INSERT INTO answers values(?, ?)";
        Date date = new Date(Calendar.getInstance().getTime().getTime());
        try (PreparedStatement insertPostStatement = conn.prepareStatement(insertPostsQuery);
             PreparedStatement insertAnswerStatement = conn.prepareStatement(insertAnswerQuery)) {
            insertPostStatement.setString(1, apid);
            insertPostStatement.setString(2, dateFormatter.format(date));
            insertPostStatement.setString(3, title);
            insertPostStatement.setString(4, body);
            insertPostStatement.setString(5, poster);
            insertAnswerStatement.setString(1, apid);
            insertAnswerStatement.setString(2, qPid);

            Boolean result1 = insertPostStatement.execute();
            Boolean result2 = insertAnswerStatement.execute();
            return result1 && result2;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("Insert into posts failed for answers", throwables);
        }
    }


    public ArrayList<SearchResult> search(String[] keywords) {
        String searchQuery = "SELECT posts.pid " +
                "FROM posts LEFT OUTER JOIN tags on (posts.pid=tags.pid) " +
                "WHERE ";
        String template = "posts.title LIKE ? or posts.body LIKE ? or tags.tag LIKE ? ";
        for (int i = 0; i < keywords.length; i++) {
            searchQuery += template;
            if (i == 0) {
                template = "or " + template;
            }
        }
        searchQuery += "GROUP BY posts.pid";

        String countQuery = "SELECT p.pid, p.pdate, p.title, p.body, p.poster, count(DISTINCT answers.pid) as numAns, count(DISTINCT votes.vno) as numVotes " +
                "FROM posts p join (" + searchQuery + ") p1 on (p.pid=p1.pid) LEFT OUTER JOIN votes on (p1.pid=votes.pid) " +
                "LEFT OUTER JOIN answers on (p1.pid=answers.qid) GROUP BY p.pid, p.pdate, p.title, p.body, p.poster;";

        try (PreparedStatement searchStatement = conn.prepareStatement(countQuery)) {
            int j = 1;
            searchStatement.setString(j, keywords[0]);
            for (int i = 0; i < keywords.length; i++) {
                searchStatement.setString(j, "%" + keywords[i] + "%");
                j++;
                searchStatement.setString(j, "%" + keywords[i] + "%");
                j++;
                searchStatement.setString(j, "%" + keywords[i] + "%");
                j++;
            }


            ResultSet result = searchStatement.executeQuery();
            ArrayList<SearchResult> searchResults = new ArrayList<>();
            Map<String, ArrayList<String>> tags = new HashMap<>();
            while (result.next()) {
                String pid = result.getString("pid");
                tags.put(pid, new ArrayList<String>());
                Date date;
                try {
                    date = new Date(dateFormatter.parse(result.getString("pdate")).getTime());
                } catch (ParseException e) {
                    date = result.getDate("pdate");
                }
                String title = result.getString("title");
                String body = result.getString("body");
                String poster = result.getString("poster");
                Integer numAns = result.getInt("numAns");
                Integer numVote = result.getInt("numVotes");
                searchResults.add(new SearchResult(pid, date, title, body, poster, numAns, numVote));
            }
            result.close();
            String searchTags = "SELECT * FROM tags;";  // might change later to in (pids)
            PreparedStatement statement = conn.prepareStatement(searchTags);
            ResultSet resultTags = statement.executeQuery();
            while (resultTags.next()) {
                String pid = resultTags.getString("pid");
                String tag = resultTags.getString("tag");
                if (tags.containsKey(pid)) {
                    tags.get(pid).add(tag);
                }
            }
            resultTags.close();
            searchResults.sort(new SortByKeywords(keywords, tags));

            return searchResults;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("Search failed", throwables);
        }
    }

    /**
     * @param pid
     * @return
     */
    public Post getPost(String pid) {
        String getPostQuery =
                "select * from posts where pid like ?";
        Post post = null;

        try (PreparedStatement badgeStatement = conn.prepareStatement(getPostQuery)) {
            badgeStatement.setString(1, pid);
            ResultSet res = badgeStatement.executeQuery();
            if (res.next()) {
                post = new Post(res.getString(1), res.getString(5));
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

        return post;
    }

    /**
     * Checks if the badge exists, returns null if no such badge
     * else return the badge name
     *
     * @param bname
     * @return
     */
    public String getBadge(String bname) {
        String getBadgeQuery =
                "select bname from badges where bname like ?";
        String badge = null;

        try (PreparedStatement badgeStatement = conn.prepareStatement(getBadgeQuery)) {
            badgeStatement.setString(1, bname);
            ResultSet res = badgeStatement.executeQuery();
            if (res.next()) {
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
        try (PreparedStatement badgeStatement = conn.prepareStatement(giveBadgeQuery)) {
            badgeStatement.setString(1, uid);
            badgeStatement.setString(2, dateFormatter.format(date));
            badgeStatement.setString(3, bname);
            return badgeStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }
    }

    public boolean giveVote(String pid, int vno, String voteGiver) {
        String giveVoteQuery =
                "insert into votes values(?, ?, ?, ?)";

        Date date = new Date(Calendar.getInstance().getTime().getTime());
        try (PreparedStatement badgeStatement = conn.prepareStatement(giveVoteQuery)) {
            badgeStatement.setString(1, pid);
            badgeStatement.setInt(2, vno);
            badgeStatement.setString(3, dateFormatter.format(date));
            badgeStatement.setString(4, voteGiver);
            return badgeStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("Giving vote FAILED", throwables);
        }
    }

    public boolean checkUniqueBadge(String bname, String uid) {
        String checkBadgeQuery =
                "select * from ubadges where uid like ? and bdate like ? and bname like ?";

        Date date = new Date(Calendar.getInstance().getTime().getTime());
        try (PreparedStatement badgeStatement = conn.prepareStatement(checkBadgeQuery)) {
            badgeStatement.setString(1, uid);
            badgeStatement.setString(2, dateFormatter.format(date));
            badgeStatement.setString(3, bname);
            ResultSet res = badgeStatement.executeQuery();
            if (res.next()) {    // exists entry
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

    public int getLargestVno(String pid) {
        String getVnoQuery =
                "select vno from votes where pid like ? order by vno desc";
        int maxVno = 0;

        try (PreparedStatement voteStatement = conn.prepareStatement(getVnoQuery)) {
            voteStatement.setString(1, pid);
            ResultSet res = voteStatement.executeQuery();
            if (res.next()) {
                maxVno = res.getInt(1);
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

        return maxVno;
    }

    /**
     * Return true if you have voted on this post with uid
     *
     * @param pid
     * @param uid
     * @return
     */
    public boolean checkVoted(String pid, String uid) {
        String getVoteMatchQuery =
                "select * from votes where pid like ? and uid like ?";

        try (PreparedStatement voteStatement = conn.prepareStatement(getVoteMatchQuery)) {
            voteStatement.setString(1, pid);
            voteStatement.setString(2, uid);
            ResultSet res = voteStatement.executeQuery();
            if (res.next()) {
                res.close();
                return true;
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

        return false;
    }

    /**
     * Check if tag exists on the pid
     *
     * @param tag
     * @param pid
     * @return
     */
    public void pushTag(String pid, String tag) {
        String pushTagQuery =
                "INSERT INTO tags values(?, ?)";

        try (PreparedStatement existTagStatement = conn.prepareStatement(pushTagQuery)) {
            existTagStatement.setString(1, pid);
            existTagStatement.setString(2, tag);
            existTagStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }
    }

    public boolean existsTag(String pid, String tag) {
        String getTagQuery =
                "select * from tags where pid like ? and tag like ?";

        try (PreparedStatement voteStatement = conn.prepareStatement(getTagQuery)) {
            voteStatement.setString(1, pid);
            voteStatement.setString(2, tag);
            ResultSet res = voteStatement.executeQuery();
            if (res.next()) {
                res.close();
                return true;
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

        return false;
    }

    public boolean checkIsAnswer(String pid) {
        String getAnswerQuery =
                "select * from answers where pid like ?";

        try (PreparedStatement checkAnswerStatement = conn.prepareStatement(getAnswerQuery)) {
            checkAnswerStatement.setString(1, pid);
            ResultSet res = checkAnswerStatement.executeQuery();
            if (res.next()) {
                res.close();
                return true;
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

        return false;
    }

    // given an answer's pid, get the question's accepted answer and pid
    // return array of question pid and question theaid
    public String[] getAcceptedAnswer(String pid) {
        String[] data = new String[2];
        String getAcceptedAnswerQuery =
                "select * from questions, answers where answers.pid like ? and answers.qid=questions.pid;";
        try (PreparedStatement acceptedAnswerStatement = conn.prepareStatement(getAcceptedAnswerQuery)) {
            acceptedAnswerStatement.setString(1, pid);
            ResultSet res = acceptedAnswerStatement.executeQuery();
            if (res.next()) {
                data[0] = res.getString(1);
                data[1] = res.getString(2);
                res.close();
                return data;
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }

        return data;
    }

    public void updateQuestion(String qid, String theaid) {
        String updateQuestionQuery =
                "update questions set theaid=? where pid like ?";
        try (PreparedStatement updateQuestionStatement = conn.prepareStatement(updateQuestionQuery)) {
            updateQuestionStatement.setString(1, theaid);
            updateQuestionStatement.setString(2, qid);
            updateQuestionStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }
    }

    public String[] getEditables(String pid) {
        String[] data = new String[2];
        String getEditablesQuery =
                "select title, body from posts where pid like ?";
        try (PreparedStatement getEditablesStatement = conn.prepareStatement(getEditablesQuery)) {
            getEditablesStatement.setString(1, pid);
            ResultSet res = getEditablesStatement.executeQuery();
            if (res.next()) {
                data[0] = res.getString(1);
                data[1] = res.getString(2);
                res.close();
                return data;
            }
            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("QUERY FAILED", throwables);
        }
        return data;
    }

    public void updateEditables(String pid, String[] editables) {
        String updateEditablesQuery =
                "update posts set title=?, body=? where pid like ?";
        try (PreparedStatement updateEditablesStatement = conn.prepareStatement(updateEditablesQuery)) {
            updateEditablesStatement.setString(1, editables[0]);
            updateEditablesStatement.setString(2, editables[1]);
            updateEditablesStatement.setString(3, pid);
            updateEditablesStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("Query FAILED", throwables);
        }
    }
}
