import java.lang.reflect.Method;
import java.sql.Date;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    private final DBController dbController;
    static Scanner scanner = new Scanner(System.in);

    private boolean pUser = false;
    private String currentUserUID;

    final HashMap<String, Method> cmds;

    public Main() throws NoSuchMethodException {
        dbController = new DBController();
        cmds = new HashMap<String, Method>(){{
            put("p", Main.class.getMethod("postQuestion"));
            put("s", Main.class.getMethod("searchPost"));
            put("a", Main.class.getMethod("answerPost"));
            put("v", Main.class.getMethod("vote"));
            put("h", Main.class.getMethod("help"));
            put("m", Main.class.getMethod("markAccepted"));
            put("b", Main.class.getMethod("giveBadge"));
            put("t", Main.class.getMethod("tag"));
            put("e", Main.class.getMethod("editPost"));
        }};
    }

    // TODO ALL THESE FUNCTIONS NEED TO DO THE LOGIC INDICATED
    // TODO : REMEMBER TO CHECK STRING LENGTH SO YOU CAN MAKE VALID QUERY
    // TODO : OR A VALID ENTRY
    public void postQuestion() {
        System.out.print("Enter a title: ");
        String title = scanner.nextLine();
        System.out.print("Enter a body");
        String body = scanner.nextLine();

        Date date = Utils.getSQLDate();
        Boolean status = dbController.postQuestion(Utils.generateID(4), date, title, body, currentUserUID);
        // TODO what to do if fail
    }

    public void searchPost() {
        System.out.print("Search Keywords: ");
        String keywords = scanner.nextLine();
    }
    public void answerPost() {}
    public void vote() {}
    public void help() {
        System.out.println();
        System.out.println(StringConstants.INTRO);
        if(pUser)
            System.out.println(StringConstants.P_INTRO);
    }
    public void markAccepted() {}
    public void giveBadge() {}
    public void tag() {}
    public void editPost() {}

    /**
     * Return true for successful login, false for failure
     * Can also return uid if that's more convenient for future
     * Also needs to check if the user is privilege
     * @return
     */
    public boolean attemptLogin() {
        System.out.println("Please login by entering your username, you will be prompted for your password after");
        System.out.print("UID: ");

        String uid = scanner.nextLine();
        currentUserUID = uid;
        // echo back username
//        System.out.println(username);
        System.out.print("PASSWORD: ");
        String pwd = scanner.nextLine();
        // echo back pwd
//        System.out.println(pwd);

        String dbPwd = dbController.getPwd(uid);
        if(dbPwd == null || dbPwd.compareTo(pwd) != 0) {
            System.out.println(StringConstants.INVALID_CREDS);
            System.out.println();
            return false;
        }

        pUser = dbController.isPrivileged(uid);

        return true;
    }

    public void show() {
        while(!attemptLogin());

        System.out.println();
        System.out.println(StringConstants.INTRO);
        if(pUser)
            System.out.println(StringConstants.P_INTRO);

        String in;
        while(true) {   // main functional loop
            System.out.print("cmd: ");
            in = scanner.nextLine(); // wait for input
            if(parseInput(in))
                break;
        }
    }

    /**
     * return true if you want to exit the program false otherwise
     * @param in
     * @return
     */
    public boolean parseInput(String in) {
        if(in.compareTo("exit") == 0)
            return true;

        // get method from map
        Method m = cmds.get(in);
        if(m == null) {
            System.out.println(StringConstants.INVALID_INPUT);
            return false;
        }
        // do a check if the user can use the command
        if(!pUser && StringConstants.PRIVILEGED_CMDS.contains(in)) {
            System.out.println(StringConstants.INVALID_PRIVILEGE);
            return false;
        }

        Object res; // can get response but why lol

        try {   // invoke your method
            res = m.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failure on invoking function");
            throw new RuntimeException("Failure on invoking method in cmds", e);
        }

        return false;
    }

    public static void main(String[] args) {
        System.out.println(StringConstants.LOGO);
        Main mainView = null;
        try {
            mainView = new Main();
        } catch (NoSuchMethodException e) {
            System.out.println("One of the methods is invalid in the HashMap");
            e.printStackTrace();
        }
        assert mainView != null;
        mainView.show();
        System.out.println();
        System.out.println(StringConstants.EXIT_MESSAGE);
    }

}
