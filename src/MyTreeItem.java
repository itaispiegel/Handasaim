import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public class MyTreeItem{
	
	String link;
	TreeItem item;

	public MyTreeItem() {
	}
	
	public MyTreeItem(Tree arg0, int arg1) {
		item = new TreeItem(arg0, arg1);
	}

	public MyTreeItem(TreeItem arg0, int arg1) {
		item = new TreeItem(arg0, arg1);
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setItem(TreeItem item) {
		this.item = item;
	}
	
	public void setText(String text) {
		this.item.setText(text);
	}
}
