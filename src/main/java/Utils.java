import org.apache.commons.lang3.RandomStringUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class Utils {
    public static String generateID(int count) {
        return RandomStringUtils.random(count, true, true);
    }

    public static Date getSQLDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        java.util.Date cDate = new java.util.Date();
        dateFormat.format(cDate);
        return new Date(cDate.getTime());
    }

}
