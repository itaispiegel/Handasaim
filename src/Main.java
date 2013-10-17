import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Main {
	
	protected static Shell shell;
	
	private Display display = new Display();
	
	//tabFolder
	private CTabFolder tabFolder;
	
	//GridData
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1); //Set data from shell resizing
	
    //Trees
	private Tree scheduleOutput;
	private Tree lessons;
    
	//JSoup variables
	private Document doc;
	private Elements e;
	
	//Desktop
	private static Desktop d = Desktop.getDesktop();
	
	//ArrayLists
	private ArrayList<MyTreeItem> subjectRoomData = new ArrayList<MyTreeItem>(); //Get the data in subjectRooms
	
	//The Icon
	private final Image image = new Image(display, Main.class.getResourceAsStream("/org/eclipse/wb/swt/Logo.bmp")); //The icon of the program
	
	//Links
	private static Link newsLink; //News
	private static Link booksLink;
	private static Link contact;
	
	// Tray
	private TrayItem item;
    
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Main window = new Main();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	/**
	 * Create contents of the window.
	 */
	@SuppressWarnings("null")
	protected void createContents() {
		shellInitialize();
		tabFolder = new CTabFolder(shell, SWT.NONE | SWT.RIGHT_TO_LEFT);
		tabFolder.setBounds(0, 0, 467, 291);
		
        tabFolder.setLayout(new GridLayout(1, true));
        data.heightHint = 308;
        tabFolder.setLayoutData(data);
		
		final CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("חדשות");
		
		newsLink = new Link(tabFolder, SWT.NONE | SWT.RIGHT_TO_LEFT | SWT.CURSOR_ARROW | SWT.V_SCROLL);//Just initialize some properties
		
		tabItem.setControl(newsLink);//Show link on screen
		
		//Initialize the schedule and subjectRooms windows
		ScheduleInitialize(tabFolder);
		subjectRoomsInitialize(tabFolder);
		booksInitialize(tabFolder);
		contactInitialize(tabFolder);
		
		//Initialize the tray icon
		final Tray tray = display.getSystemTray();
		
		if (tray == null && tray.isDisposed()) {
		    System.out.println ("The system tray is not available");
		} else {
		    item = new TrayItem (tray, SWT.NONE);
		    item.setToolTipText("תיכון הנדסאים");
		    
		    //Tray icon menu
		    final Menu menu = new Menu (shell, SWT.POP_UP);

		    //Set the open button
		    MenuItem mi = new MenuItem (menu, SWT.PUSH);
		    mi.setText ("פתח");
		    mi.addListener (SWT.Selection, new Listener () {
		        public void handleEvent (Event event) {
		        	shell.setMinimized(false);
		        	shell.forceActive();
		        }
		    });

		    new MenuItem(menu, SWT.SEPARATOR); //Separate between open and other buttons

			// Set the refresh button
			MenuItem mi1 = new MenuItem(menu, SWT.PUSH);
			mi1.setText("רענון");
			mi1.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					tabFolder.setSelection(tabItem);
					programFunctions(tabItem);
				}
			});
		    
		    //Set the open school website button
		    MenuItem mi2 = new MenuItem (menu, SWT.PUSH);
		    mi2.setText ("אתר בית הספר");
		    mi2.addListener (SWT.Selection, new Listener () {
		        public void handleEvent (Event event) {
		        	try {
						d.browse(new URI("http://handasaim.co.il/"));
					} catch (IOException | URISyntaxException e) {
						e.printStackTrace();
					}
		        }
		    });
		    
		    //Set the close app button
		    MenuItem mi3 = new MenuItem (menu, SWT.PUSH);
		    mi3.setText ("סגור");
		    mi3.addListener (SWT.Selection, new Listener () {
		        public void handleEvent (Event event) {
		        	item.dispose();
		        	shell.dispose();
		            System.exit(0);
		        }
		    });

		    item.addListener (SWT.DefaultSelection, new Listener () {
		        public void handleEvent (Event event) {
		        	menu.setVisible(true);
		        }
		    });


		    item.addListener (SWT.Selection, new Listener () {
		        public void handleEvent (Event event) {
		            menu.setVisible(true);
		        }
		    });

		    item.addListener (SWT.MenuDetect, new Listener () {
		        public void handleEvent (Event event) {
		            menu.setVisible (true);
		        }
		    });
		    item.setImage(image);
		
			programFunctions(tabItem);
		}

		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				item.dispose();
			}
		});
	}

	private void programFunctions(CTabItem tabItem) {
		final String lessonsLink = getNews(tabFolder, tabItem);//Get the news and schedule url
		
		getSchedule(lessonsLink);
		subjectRoom();
	}
	
	/*	Initializing the form of the schedule part*/
	private void booksInitialize(CTabFolder tabFolder) {
		CTabItem tab4 = new CTabItem(tabFolder, SWT.NONE);
		tab4.setText("ספרי לימוד");
		
		booksLink = new Link(tabFolder, SWT.NONE | SWT.RIGHT_TO_LEFT | SWT.CURSOR_ARROW);//Just initialize some properties

		booksLink.setLayoutData(new GridData(GridData.FILL_BOTH));
		tab4.setControl(booksLink);
		
		try {
			doc = Jsoup.connect("http://handasaim.co.il/?CategoryID=342").get();
			Elements e = doc.select("td.PageTitle");
			
			Elements element = e.first().parent().parent().child(2).select("h3");
			
			final ArrayList<MyLink> list = new ArrayList<MyLink>();//List of each individual news
			String output = "";
			
			for (int i = 0; i < element.size(); i++) {
				MyLink tmp = new MyLink();//New tmp news link
				tmp.setText(element.get(i).text());//Set the text
				tmp.setURL(element.get(i).child(0).attr("abs:href"));//Set the link
				
				list.add(tmp);//Add tmp to the list
				
				output += i + 1 + ". "; //Add numbering to the news and make it clickable only if it holds a link
				output += (tmp.getURL() != "") ? "<a>" + tmp.getText() + "</a>" : tmp.getText();
				output += "\n\n";
			}
			
			booksLink.setText(output);
			
			booksLink.addListener(SWT.Selection, new Listener() {//Set the link to be clickable
				@Override
				public void handleEvent(Event event) {
					for (int i = 0; i < list.size(); i++) {
						if (event.text.equals(list.get(i).getText())) {//Find the url of the link
							try {
								URL tmp = new URL(list.get(i).getURL());
								
								d.browse(tmp.toURI()); //Open it in default web browser
								break; //Exit the loop
							} catch (IOException | URISyntaxException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
/*	Initializing the form of the schedule part*/
	private void ScheduleInitialize(CTabFolder tabFolder) {
		CTabItem tab2 = new CTabItem(tabFolder, SWT.NONE);
		tab2.setText("מערכת");
		
		scheduleOutput = new Tree(tabFolder, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.RIGHT_TO_LEFT | SWT.WRAP);

		scheduleOutput.setLinesVisible(true);
		scheduleOutput.setLayoutData(new GridData(GridData.FILL_BOTH));
		tab2.setControl(scheduleOutput);
		scheduleOutput.setHeaderVisible(true);	
		
		scheduleOutput.addMouseListener(new MouseListener()
		{
			public void mouseDown(MouseEvent event)
			{
			}
			public void mouseUp(MouseEvent event)
			{
				try {
					scheduleOutput.getItem(new Point(event.x, event.y)).getText();
				}catch(Exception ex) {
					scheduleOutput.deselectAll();
				}
			}
			public void mouseDoubleClick(MouseEvent event)
			{
			}
		});
	}
	
	private void subjectRoomsInitialize(CTabFolder tabFolder) {
		CTabItem tab3 = new CTabItem(tabFolder, SWT.NONE);
		tab3.setText("חדרי מקצוע");
		
		lessons = new Tree(tabFolder, SWT.BORDER | SWT.FULL_SELECTION | SWT.RIGHT_TO_LEFT | SWT.MULTI | SWT.WRAP);
		
		lessons.setLinesVisible(true);
		lessons.setLayoutData(new GridData(GridData.FILL_BOTH));
		tab3.setControl(lessons);
		
		lessons.setHeaderVisible(true);	
		
		lessons.addMouseListener(new MouseListener()
		{
			MyTreeItem clicked = new MyTreeItem();
			
			ArrayList<TreeItem> tmpList = new ArrayList<TreeItem>();
			
			public void mouseDown(MouseEvent event)
			{
			}
			public void mouseUp(MouseEvent event)
			{
				try {
					lessons.getItem(new Point(event.x, event.y)).getText();
				}catch(Exception ex) {
					lessons.deselectAll();
				}
			}
			public void mouseDoubleClick(MouseEvent event)
			{
				for (int i = 0; i < subjectRoomData.size(); i++) {
					tmpList.add(subjectRoomData.get(i).item);
				}
				
				try {
					clicked.setItem(lessons.getItem(new Point(event.x, event.y)));
					clicked.item.setExpanded(clicked.item.getExpanded());
					
					int clickedIndex = tmpList.indexOf(clicked.item);
					
					URL tmp = new URL(subjectRoomData.get(clickedIndex).getLink());
					
					d.browse(tmp.toURI());
				}catch(Exception ex) {
					lessons.deselectAll();
				}
			}
		});
	}
	
	private void contactInitialize(CTabFolder tabFolder) {
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("צור קשר");
		
		contact = new Link(tabFolder, SWT.NONE | SWT.RIGHT_TO_LEFT | SWT.CURSOR_ARROW | SWT.V_SCROLL);//Just initialize some properties
		contact.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabItem.setControl(contact);
		
		contact.setText("פרטי בית הספר: \n \n" +
		"תיכון הנדסאים – הרצליה \n" +
		"רח' ז'בוטינסקי 3 הרצליה \n" +
		"אתר: <a>http://www.handasaim.co.il</a> \n" + 
		"אימייל: <a>handas1@walla.com</a> \n" +
		"טלפונים: 09-9553728, 09-9553891 \n" +
		"פקס:  09-9553795 \n \n \n" +
		
		"פרטי המפתח: \n \n" +
		"איתי שפיגל י'1 \n" +
		"אימייל: <a>itai.spiegel@gmail.com</a> \n" +
		"טלפון: 054-3104581");
		
		contact.addListener(SWT.Selection, new Listener() {//Set the link to be clickable
			@Override
			public void handleEvent(Event event) {
				try {
					URI uri = (event.text.contains("@")) ? URI.create("mailto:" + event.text) : new URL(event.text).toURI();
					
					d.browse(uri);
				} catch (URISyntaxException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/* Initialzing the shell properties */
	private void shellInitialize() {
		shell = new Shell(display, SWT.CLOSE | SWT.MIN | SWT.RESIZE | SWT.TITLE | SWT.RIGHT_TO_LEFT);
		shell.setImage(image);
		
        shell.setLayout(new GridLayout(1, true));
        shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		shell.setSize(759, 523);
		shell.setText("הנדסאים");
		shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
	}
	
	public static String getNews(CTabFolder tabFolder, CTabItem tabItem) {
		String lessons = "";
		
		try {
			//Connect to the school web page and fing the news part and get the schedule url
			Document doc = Jsoup.connect("http://handasaim.co.il/").get();
			Elements e = doc.getElementsByClass("tickerItemContainer");
			
            lessons = doc.select("a:contains(מערכת שעות)").attr("abs:href").toString();
			
			String output = "";//Output string
			
			final ArrayList<MyLink> list = new ArrayList<MyLink>();//List of each individual news

			for (int i = 0; i < e.size(); i++) {
				MyLink tmp = new MyLink();//New tmp news link
				tmp.setText(e.get(i).text());//Set the text
				tmp.setURL(e.get(i).child(0).attr("abs:href"));//Set the link
				
				list.add(tmp);//Add tmp to the list
				
				output += i + 1 + ". "; //Add numbering to the news and make it clickable only if it holds a link
				output += (tmp.getURL() != "") ? "<a>" + tmp.getText() + "</a>" : tmp.getText();
				output += "\n\n";
			}
	        
			newsLink.setText(output);
            //newsLink.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			
			newsLink.addListener(SWT.Selection, new Listener() {//Set the link to be clickable
				@Override
				public void handleEvent(Event event) {
					for (int i = 0; i < list.size(); i++) {
						if (event.text.equals(list.get(i).getText())) {//Find the url of the link
							try {
								URL tmp = new URL(list.get(i).getURL());
								
								d.browse(tmp.toURI()); //Open it in default web browser
								break; //Exit the loop
							} catch (IOException | URISyntaxException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
		} catch(IOException e1){
			newsLink.setText("שגיאת התחברות! נסה להתחבר מחדש.");
		}
		
		return lessons;
	}
	
	public void getSchedule(String lessonsURL){
        scheduleOutput.clearAll(true);
        scheduleOutput.removeAll();
        Workbook workbook;
        
        if (lessonsURL.trim().equals("")) {
			TreeItem item = new TreeItem(scheduleOutput, SWT.NONE);
			item.setText("אין מערכת");
        }
        else {
			try {
				workbook = Workbook.getWorkbook(new URL(lessonsURL).openStream());
				Sheet sheet = workbook.getSheet(0);

				for (int i = 1; i < 13; i++) {
					String tmp = sheet.getCell(i, 0).getContents();

					tmp = tmp.replace(" ", " ");

					TreeItem item = new TreeItem(scheduleOutput, SWT.NONE);
					item.setText(tmp);

					for (int j = 1; j < sheet.getColumn(i).length; j++) {
						TreeItem subItem = new TreeItem(item, SWT.NONE);
						subItem.setText(j - 1 + ".  "
								+ sheet.getCell(i, j).getContents());
					}
					scheduleOutput.pack();
				}

			} catch (BiffException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void subjectRoom() {
		lessons.clearAll(true);
		lessons.removeAll();
		if (newsLink.getText() != "שגיאת התחברות! נסה להתחבר מחדש.")
		try {
			MyTreeItem headline = new MyTreeItem(lessons, SWT.NONE);
			headline.setText("חדרי מקצוע");
			headline.setLink("http://handasaim.co.il/?CategoryID=217");
			
			subjectRoomData.add(headline);
			
			
			doc = Jsoup.connect("http://handasaim.co.il/?CategoryID=217").get();
			e = doc.getElementsByClass("box1Color").select("a[href]");
			for (int i = 0; i < e.size(); i++) {
				
				String tmp = e.get(i).text();
				
				if (!e.get(i).className().contains("LightVersion")) {
					MyTreeItem item = new MyTreeItem(lessons, SWT.NONE);
					item.setText(tmp.toString());
					item.setLink(e.get(i).attr("abs:href"));
					
					subjectRoomData.add(item);
				}

				else {
					MyTreeItem subItem = new MyTreeItem(lessons.getItem(lessons.getItemCount() - 1), SWT.NONE);
					subItem.setText(tmp.toString());
					subItem.setLink(e.get(i).attr("abs:href"));
					
					subjectRoomData.add(subItem);
				}
			}
			lessons.pack();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}