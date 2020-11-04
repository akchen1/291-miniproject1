public class Post {
    public String pid;
    public String owner;
    public boolean selected;

    // Object to store post so it's easier to do selections on it
    public Post() {
    }

    public Post(String pid, String owner) {
        this.pid = pid;
        this.owner = owner;
    }

    public void selectPost(String pid, String owner) {
        this.pid = pid;
        this.owner = owner;
        selected = true;
    }

    public void deselectPost() {
        this.pid = null;
        this.owner = null;
        selected = false;
    }

}
