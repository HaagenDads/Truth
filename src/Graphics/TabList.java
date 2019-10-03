package Graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;

import Core.Theorem;

//import Core.Theorem;

public class TabList extends JComponent {
	
	ArrayList<Tab> tabs;
	final static int maxwidth = 250;
	final static int minwidth = 140;
	final static String shortenmark = "...";
	
	final static int CrossBottomrightX = 8;
	final static int CrossBottomrightY = 6;
	final static int CrossSize = 8;
	
	boolean mouseDragging;
	int activetab;
	int draggingId;
	boolean draggingDirection;
	int hoveredtab;
	
	private BotTextArea bottextarea;
	
	public TabList() {
		tabs = new ArrayList<Tab>();
		mouseDragging = false;
		activetab = -1;
		draggingId = -1;
		draggingDirection = false;
		hoveredtab = -1;
	}
	
	public void attachTextArea(BotTextArea bottextarea) {
		this.bottextarea = bottextarea;
	}
	
	public void addnew(Theorem thm) {
		for (Tab t: tabs) {
			if (t.thm.equals(thm)) {
				select(t.id);
				return;
			}
		}
		int tabssize = tabs.size();
		tabs.add(new Tab(thm, tabssize));
		select(tabssize);
		repaint();
	}
	
	private boolean isEmpty() {
		return tabs.size() == 0;
	}
	
	private void select(int id) {
		
		if (isEmpty()) {
			bottextarea.initializeText();
		} else {
			for (Tab t: tabs) {
				t.active = t.id == id;
			}
			activetab = id;
			clearMouseHover();
			try {
				Theorem activethm = tabs.get(activetab).thm;
				activethm.removeEmbeding();
				if (activethm.hasFilePath()) {
					activethm.compileDemonstration();
				} 
				bottextarea.drawTheorem(activethm);
	
			} catch (BadLocationException ignored) {}
		}
		
	}
	
	private void removeTab(int id) {
		
		for (Tab t: tabs) {
			if (t.id > id) t.id--;
		}
		
		tabs.remove(id);
		if (tabs.size() == 1) activetab = 0;
		else if (id < activetab) activetab--;
		else if (id == activetab && id == tabs.size()) activetab--;
		select(activetab);
	}
	
	public void mouseClicked(MouseEvent e) {
		if (isEmpty()) return;
		int id = getMouseTabId(e);
		if (id == -1) return;
		
		int x = e.getX() - 11;
		Tab t = tabs.get(id);
		
		if (t.crosshitbox.contains(new Point(x, e.getY()+1))) removeTab(t.id);
		else select(t.id);
		repaint();
	}
	
	public void mouseMoved(MouseEvent e) {
		if (isEmpty()) return;
		int id = getMouseTabId(e);
		
		if (id == -1 && hoveredtab != -1) {
			tabs.get(hoveredtab).hovered = false;
			hoveredtab = -1;
			repaint();
		} else if (id != -1 && id != hoveredtab) {
			tabs.get(id).hovered = true;
			if (hoveredtab != id) {
				clearMouseHover();
				hoveredtab = id;
			}
			repaint();
		}
	}
	
	public void mouseExited(MouseEvent e) {
		if (isEmpty()) return;
		clearMouseHover();
		repaint();
	}
	
	private void clearMouseHover() {
		if (hoveredtab != -1 && hoveredtab < tabs.size()) tabs.get(hoveredtab).hovered = false;
		hoveredtab = -1;
	}
	
	private int getMouseTabId(MouseEvent e) {
		int x = e.getX() - 11;
		for (Tab t: tabs) {
			if (t.posleft < x && x < t.posleft + t.width + 24 ) {
				return t.id;
			}
		}
		return -1;
	}
	
	public void mouseDragged(MouseEvent e) {
		if (isEmpty()) return;
		if (!mouseDragging) {
			mouseClicked(e);
			mouseDragging = true;
		}
		
		int movingid = -1;
		if (e.getY() > 0 && e.getY() < 30) {
			int x = e.getX() - 11;
			for (Tab t: tabs) {
				int leftcenter = t.posleft + 3*t.width/4;
				int rightcenter = t.posleft + t.width/4;
				if (t.id < activetab && leftcenter > x) {
					movingid = t.id;
					draggingDirection = true;
					break;
				}
				else if (t.id > activetab && rightcenter < x) {
					movingid = t.id;
					draggingDirection = false;
				}
			}
			
			if (movingid >= 0) {
				draggingId = movingid;
				repaint();
			}
		}
		
		if (movingid != draggingId) {
			draggingId = -1;
			repaint();
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if (isEmpty()) return;
		mouseDragging = false;
		if (draggingId != -1) {
			Tab selected = null;
			ArrayList<Tab> newtab = new ArrayList<Tab>();
			for (Tab t: tabs) {
				if (t.id == activetab) {
					selected = t;
					t.id = draggingId;
				}
				else newtab.add(t);
			}
			newtab.add(draggingId, selected);
			int idn = 0;
			for (Tab t: newtab) t.id = idn++;
			tabs = newtab;
			activetab = draggingId;
			draggingId = -1;
			repaint();
		}
	}
	
	
	public Theorem getActiveTheorem() {
		return tabs.get(activetab).thm;
	}
	
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont( new Font("Consolas", Font.PLAIN, 12));
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
        	    						  RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        FontMetrics fm = g.getFontMetrics();
        
        int runningLeftpos = 0;
        int width;
        int postfixSize = fm.stringWidth(shortenmark); // When we need to shorten names
        
        for (Tab t: tabs) {
     
        	String tname = t.name;
        	width = fm.stringWidth(tname);
        	
        	if (width > maxwidth) {
        		tname = tname.substring(0, tname.length()-3);
        		width = fm.stringWidth(tname);
        	
	        	while (width > maxwidth - postfixSize) {
	        		tname = tname.substring(0, tname.length()-1);
	        		width = fm.stringWidth(tname);
	        	}
	        	tname = tname.concat(shortenmark);
	        	width += postfixSize;
        	}
        	
        	width = Math.max(width, minwidth);
        	t.paintComponent(g, runningLeftpos, width, tname);
        	
        	if (draggingId == t.id) drawDraggingBars(g, runningLeftpos, width);
        	
        	runningLeftpos += width + 26;
        }
    }
	
	
	private void drawDraggingBars(Graphics g, int leftpos, int width) {
		g.drawRect(leftpos + (draggingDirection ? 0 : width+20), 4, 1, 24);
		g.drawRect(leftpos + 2 + (draggingDirection ? 0 : width+20), 4, 1, 24);
	}
	
	
	private class Tab {
		
		String name;
		boolean active, hovered;
		int id;
		int posleft;
		int width;
		public Theorem thm;
		
		Rectangle cross, crosshitbox;
		
		
		private Tab(Theorem thm, int id) {
			this.name = thm.name;
			this.id = id;
			this.thm = thm;
		}
		
		private void computeCross() {
        	int crossleft = posleft + width + 24 - CrossBottomrightX - CrossSize;
        	int crosstop = 26 - CrossBottomrightY - CrossSize;
        	cross = new Rectangle(crossleft, crosstop, CrossSize, CrossSize);
        	crosshitbox = new Rectangle(crossleft-5, crosstop-5, CrossSize+10, CrossSize+10);
		}
		
		public void paintComponent(Graphics g, int posleft, int width, String displaytext) {
			g.setColor(active ? Color.WHITE : (hovered ? new Color(220, 220, 220) : Color.LIGHT_GRAY));
	        g.fillRect(posleft, 4, width+24, active ? 26 : 22);
	        
	        g.setColor(Color.BLACK);
	        g.drawString(displaytext, posleft + 2,  20);
	        this.posleft = posleft;
	        this.width = width;
	        
	        computeCross();
	        
	        if (active || hovered) {
	        	Graphics2D g2 = (Graphics2D) g;
	        	RenderingHints hints = new RenderingHints(
	                    RenderingHints.KEY_ANTIALIASING,
	                    RenderingHints.VALUE_ANTIALIAS_ON);
	            g2.setRenderingHints(hints);
	        	g2.setColor(hovered ? new Color(30, 30, 55) : new Color(20, 20, 45));
	        	g2.setStroke(new BasicStroke(1));
	        	
	        	g2.drawLine(cross.x, cross.y, cross.x + cross.width, cross.y + cross.height);
	        	g2.drawLine(cross.x, cross.y + cross.height, cross.x + cross.width, cross.y);
	        }
	    }

	}
}
