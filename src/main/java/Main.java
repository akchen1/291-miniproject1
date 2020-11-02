import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.io.Console;

public class Main {
    private final DBController dbController;
    static Scanner scanner = new Scanner(System.in);

    private boolean pUser = false;
    private String currentUserUID;
    private Post selectedPost = new Post();

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
        System.out.print("Enter a body: ");
        String body = scanner.nextLine();

        String genPid = Utils.generateID(4);
        Post checkPost = dbController.getPost(genPid);
        while(checkPost != null) {
            genPid = Utils.generateID(4);
            checkPost = dbController.getPost(genPid);
        }

        Date date = Utils.getSQLDate();
        Boolean status = dbController.postQuestion(Utils.generateID(4), date, title, body, currentUserUID);
        // TODO what to do if fail
        System.out.println("Thanks for posting your question!");
    }

    private Iterator printSearch(Iterator iterator) {
        int counter = 0;
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
            counter++;
            if (counter == 5) {
                break;
            }
        }
        return iterator;
    }

    public void searchPost() {
        System.out.print("Search Keywords: ");
        String keywords = scanner.nextLine();
        String[] splitKeywords = keywords.split(",");
        if (splitKeywords.length == 0) {
            return;
        }

        ArrayList<SearchResult> searchResults = dbController.search(splitKeywords);
        Iterator iterator = searchResults.iterator();
        System.out.println("Displaying first 5 results.");
        System.out.println("q to quit, > for next page, s to select a post");
        iterator = printSearch(iterator);
        Boolean done = false;
        while (true) {
            if (!iterator.hasNext() && !done) {
                System.out.println("End of search results");
                done = true;
            }
            String input = scanner.nextLine();
            if (input.toLowerCase().compareTo("q") == 0) {
                return;
            } else if (input.toLowerCase().compareTo(">") == 0 && !done) {
                if (done) {
                    System.out.println("End of search results");
                } else {
                    iterator = printSearch(iterator);
                }
            } else if (input.toLowerCase().compareTo("s") == 0) {
                System.out.println("Enter the id of the post you want to select");
                String post = scanner.nextLine();
                if (searchResults.stream().anyMatch((searchResult -> { if(searchResult.pid.toLowerCase().compareTo(post) == 0) {
                    selectedPost.selectPost(searchResult.pid, searchResult.poster);
                    return true;
                } return false;}))) {
                    System.out.println(post + " Selected");
                    return;
                } else {
                    System.out.println("Selected post does not exist. Start search again.");
                }
            } else {
                System.out.println("Invalid input");
                System.out.println("q to quit, > for next page, s to select a post");
            }
        }
    }

    public void answerPost() {
        if(selectedPost.pid == null) {
            System.out.println("You never selected a valid post! Please search first");
            return;
        }

        System.out.print("Enter a title: ");
        String title = scanner.nextLine();
        System.out.print("Enter a body: ");
        String body = scanner.nextLine();

        // check if post exists
        String genPid = Utils.generateID(4);
        Post checkPost = dbController.getPost(genPid);
        while(checkPost != null) {
            genPid = Utils.generateID(4);
            checkPost = dbController.getPost(genPid);
        }

        Boolean status = dbController.postAnswer(selectedPost.pid, genPid, title, body, currentUserUID);
        System.out.println("Successful post thanks for answering the question!");
    }

    public void vote() {
        // vote increments from the earliest vote recv.
        if(dbController.checkVoted(selectedPost.pid, currentUserUID)) {
            System.out.println("Cannot give vote because you already have given your vote to " + selectedPost.pid);
            return;
        }

        int largestVno = dbController.getLargestVno(selectedPost.pid);
        dbController.giveVote(selectedPost.pid, largestVno+1, currentUserUID);

        System.out.println("Thanks for casting your vote!~");
    }

    public void help() {
        System.out.println();
        System.out.println(StringConstants.INTRO);
        if(pUser)
            System.out.println(StringConstants.P_INTRO);
    }
    public void markAccepted() {}

    // badge name case insensitive
    // *** REQUIRES SELECTED POST TO BE NOT NULL or EMPTY ***
    public void giveBadge() {
        if(selectedPost.pid == null) {
            System.out.println("You never selected a valid post! Please search first");
            return;
        }
        System.out.println("Give a user a badge by giving a proper badge name");
        String bname;
        while(true) {
            System.out.print("Badge name: ");
            String in = scanner.nextLine();
            bname = dbController.getBadge(in);
            if(bname == null) {
                System.out.println("Exists no such badge :(");
                continue;
            }
            else if(!dbController.checkUniqueBadge(bname, selectedPost.owner)) {
                System.out.println("That badge has been given to this user today already!");
                continue;
            }
            break;
        }

        dbController.giveBadge(bname, selectedPost.owner);
    }
    public void tag() {
        if(selectedPost.pid == null) {
            System.out.println("You never selected a valid post! Please search first!");
            return;
        }
        System.out.println("Give selected post a tag!");
        while(true) {
            System.out.print("Tag: ");
            String in = scanner.nextLine();

            if(dbController.existsTag(selectedPost.pid, in)) {
                System.out.println("Already exists this tag for this post, enter another");
                continue;
            } else {
                dbController.pushTag(selectedPost.pid, in);
            }
            System.out.println("Added the tag to " + selectedPost.pid);
            return;
        }
    }
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

	Console console = System.console();
        String pwd = new String(console.readPassword("PASSWORD: ")); 

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

        // TODO DELETE AFTER
//        selectedPost.selectPost("p001", "mid1");

        System.out.println();
        System.out.println(StringConstants.INTRO);
        if(pUser)
            System.out.println(StringConstants.P_INTRO);

        String in;
        while(true) {   // main functional loop
            System.out.print("cmd: ");
            in = scanner.nextLine(); // wait for input
            // TODO: ONLY PERMITTED HERE SHOULD BE ps because you need to
            // TODO: search or make a post
            if(parseInput(in, StringConstants.ALL_ACTIONS)) // only allow psm for this menu
                break;
        }
    }

    /**
     * return true if you want to exit the program false otherwise
     * @param in
     * @param permitted a list of permitted letters for this input
     * @return
     */
    public boolean parseInput(String in, String permitted) {
        // invalid input or cannot use the command
        if(in == null) {
            System.out.println(StringConstants.INVALID_INPUT);
            return false;
        } else if (in.compareTo("exit") == 0) {
            return true;
        } else if (in.compareTo("<") == 0) {
            return false;
        } else if (in.compareTo("help") == 0) { // always allowed
        } if (!permitted.contains(in)) {
            System.out.println(StringConstants.INVALID_INPUT);
            return false;
        } else if(!pUser && StringConstants.PRIVILEGED_CMDS.contains(in)) {
            System.out.println(StringConstants.INVALID_PRIVILEGE);
            return false;
        }

        // get method from map
        Method m = cmds.get(in);
        if(m == null) {
            System.out.println(StringConstants.INVALID_INPUT);
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
