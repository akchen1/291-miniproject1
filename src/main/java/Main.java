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

    // badge name case insensitive
    public void giveBadge() {
    }
    public void tag() {}
    public void editPost() {}

    /**
     * Return true for successful login, false for failure
     * Can also return uid if that's more convenient for future
     * Also needs to check if the user is privilege
     * @return
     */
    // TODO HIDE PASSWORD IN LOGIN
    public boolean attemptLogin() {
        System.out.println("Please login by entering your username, you will be prompted for your password after");
        System.out.print("UID: ");

        String uid = scanner.nextLine();
        currentUserUID = uid;

        System.out.print("PASSWORD: ");
        String pwd = scanner.nextLine();

        String dbPwd = dbController.getPwd(uid);
        if(dbPwd == null || dbPwd.compareTo(pwd) != 0) {
            System.out.println(StringConstants.INVALID_CREDS);
            System.out.println();
            return false;
        }

        pUser = dbController.isPrivileged(uid);

        return true;
    }

    /**
     * Registers a user
     */
    // TODO HIDE PASSWORD IN REGISTER
    public void registerUser() {
        System.out.println("Please provide a uid. Uids must be 4 chars long and are case-insensitive");
        String in;
        // order of uid, name, city, pwd
        String[] details = new String[4];

        while(true) {
            System.out.print("UID: ");
            in = scanner.nextLine();
            in = in.toLowerCase();
            if(in.length() > 0 && in.length() < 5 && dbController.getUid(in) == null) {
                break;
            }
            System.out.println("Unfortunately that uid is invalid :( please try another");
        }
        currentUserUID = in;
        details[0] = in;
        System.out.println("User name selected! Please enter your details");
        System.out.print("name: ");
        scanner.nextLine();
        details[1] = in;
        System.out.print("city: ");
        scanner.nextLine();
        details[2] = in;
        System.out.print("pwd: ");
        scanner.nextLine();
        details[3] = in;

        dbController.insertUser(details);
        System.out.println("Thanks for signing up, enjoy your stay.");
    }

    public void loginMenu() {
        String in;
        System.out.println(StringConstants.LOGIN_MENU);

        while(true) {
            System.out.print("cmd: ");
            in = scanner.nextLine();
            if(in.compareTo("l") == 0) {
                while(!attemptLogin());
                return;
            } else if(in.compareTo("s") == 0) {
                registerUser();
                return;
            }
        }
    }


    public void show() {
        loginMenu();

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
