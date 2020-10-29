import java.sql.Date;

public class SearchResult {
    String pid;
    Date date;
    String title;
    String body;
    String poster;
    Integer numAns;
    Integer numVotes;

    public SearchResult(String pid, Date date, String title, String body, String poster, Integer numAns, Integer numVotes) {
        this.pid = pid;
        this.date = date;
        this.title = title;
        this.body = body;
        this.poster = poster;
        this.numAns = numAns;
        this.numVotes = numVotes;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s, %d, %d ", pid, date.toString(), title, body, poster, numAns, numVotes);
    }
}
