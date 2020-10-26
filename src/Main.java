import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    private DBController dbController = new DBController();
    static Scanner scanner = new Scanner(System.in);

    private boolean pUser = false;

    final HashMap<String, Method> cmds;

    public Main() throws NoSuchMethodException {
        cmds = new HashMap<>(){{
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

    public void postQuestion() {
        System.out.println("This is question example");
    }
    public void searchPost() {}
    public void answerPost() {}
    public void vote() {}
    public void help() {}
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
        System.out.print("USERNAME: ");

        String username = scanner.nextLine();
        // echo back username
//        System.out.println(username);
        System.out.print("PASSWORD: ");
        String pwd = scanner.nextLine();
        // echo back pwd
//        System.out.println(pwd);

        // TODO CHECK DB FOR USERNAME
        dbController.getUser();

        pUser = true;
        return true;
    }

    public void show() {
        while(!attemptLogin());

        System.out.println(Constants.INTRO);
        if(pUser)
            System.out.println(Constants.P_INTRO);

        String in;
        while(true) {   // main functional loop
            System.out.print("cmd: ");
            in = scanner.nextLine(); // wait for input
            if(parseInput(in))
                break;
        }
    }

    public boolean parseInput(String in) {
        if(in.compareTo("exit") == 0)
            return true;

        // get method from map
        Method m = cmds.get(in);
        if(m == null) {
            System.out.println(Constants.INVALID_INPUT);
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
        System.out.println(Constants.LOGO);
        Main mainView = null;
        try {
            mainView = new Main();
        } catch (NoSuchMethodException e) {
            System.out.println("One of the methods is invalid in the HashMap");
            e.printStackTrace();
        }
        assert mainView != null;
        mainView.show();
        System.out.println(Constants.EXIT_MESSAGE);
    }

}
