import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class SortByKeywords implements Comparator<SearchResult> {
    String[] keywords;
    Map<String, ArrayList<String>> tags;
    public SortByKeywords(String[] keywords, Map<String, ArrayList<String>> tags) {
        this.keywords = keywords;
        this.tags = tags;
    }

    @Override
    public int compare(SearchResult o1, SearchResult o2) {
        int o1Counter = 0;
        int o2Counter = 0;
        for (int i = 0; i < keywords.length; i++) {
            String keyword = keywords[i].toLowerCase();
            System.out.println(keyword);
            if (o1.body.toLowerCase().contains(keyword) || o1.title.toLowerCase().contains(keyword) ||
                    (tags.containsKey(o1.pid) && tags.get(o1.pid).stream().anyMatch(keyword::equalsIgnoreCase))) {
                o1Counter++;
            }
            if (o2.body.toLowerCase().contains(keyword) || o2.title.toLowerCase().contains(keyword) ||
                    (tags.containsKey(o2.pid) && tags.get(o2.pid).stream().anyMatch(keyword::equalsIgnoreCase))) {
                o2Counter++;
            }
        }

        if (o1Counter > o2Counter) {
            return -1;
        } else if (o1Counter < o2Counter) {
            return 1;
        }
        return 0;
    }
}
