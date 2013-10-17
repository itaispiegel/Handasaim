/**
 * Created with IntelliJ IDEA.
 * User: Itai
 * Date: 9/3/13
 * Time: 7:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyLink {
    private String text;
    private String URL;

    public MyLink() {
    }

    public MyLink(String text) {
        this.text = text;
    }

    public MyLink(String text, String URL) {
        this.text = text;
        this.URL = URL;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    @Override
    public String toString() {
        return text + "\n " + URL;
    }
}
