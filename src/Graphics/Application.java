package Graphics;

import Core.Compiler;
import Core.Compiler.CompilerException;
import Core.GenException;
import Core.Theorem;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.*;

public class Application extends JFrame {
	
	public JTextArea topTextarea;
	public BotTextArea botTextarea;
	private JPanel tablistpanel;
	private JScrollPane topPanel, botPanel;
	public Compiler compiler;

	public TabList tablist;
	private static final Font consolasFont = new Font("DialogInput", Font.PLAIN, 12);
	private static final Font consolasFontButton = new Font("Consolas", Font.BOLD, 12);
	public String openedfile_path;
	
	public Application() {
		compiler = new Compiler(this);
		getContentPane().setLayout(new BorderLayout());
		
		instantiateToparea();
		instantiateBotarea();
		instantiateButtons();
		instantiateMenubar();
		openedfile_path = null;
	}
	
	
	/*
	 * Instantiating graphical elements 
	 * 
	 */
	
	private void instantiateMenubar() {
		Menubar menubar = new Menubar(this);
		menubar.setBackground(Colors.colorExterior);
		menubar.setBorder(BorderFactory.createMatteBorder(
                5, 10, 5, 10, Colors.colorExterior));
		menubar.setFont(consolasFont);
		this.setJMenuBar(menubar);
		
	}
	
	private void instantiateToparea() {
		topTextarea = new JTextArea(){ /*
            public boolean isManagingFocus(){ return false; }
	    */};
		topTextarea.setLineWrap(true);
		topPanel = new JScrollPane(topTextarea);
		topPanel.setPreferredSize(new Dimension(200, 250));
		topPanel.setBorder(BorderFactory.createMatteBorder(
                5, 10, 5, 10, Colors.colorCentral));

		getContentPane().add(topPanel, BorderLayout.NORTH);
		topTextarea.setFont(consolasFont);
		topTextarea.setBackground(Colors.colorTextbackground);
		topTextarea.setBorder(BorderFactory.createMatteBorder(
                5, 4, 1, 1, Colors.colorTextbackground));
	}
	
	private void instantiateTablist() {
		tablistpanel = new JPanel();
		
		tablist.attachTextArea(botTextarea);
		
		tablistpanel.setLayout(new BorderLayout());
		tablistpanel.setBackground(Colors.colorCentral);
		tablistpanel.setPreferredSize(new Dimension(200, 30));
		tablistpanel.setBorder(BorderFactory.createMatteBorder(
                1, 11, 1, 1, Colors.colorCentral));
		tablistpanel.add((JComponent) tablist);
		tablistpanel.addMouseListener(new tabclicker());
		tablistpanel.addMouseMotionListener(new tabclicker());
	}
	
	private void instantiateBottext() {
		botTextarea = new BotTextArea(tablist);
	    botTextarea.setEditable(false);
	    botTextarea.setBorder(BorderFactory.createMatteBorder(
                0, 4, 1, 1, Colors.colorTextbackground));
		botTextarea.addMouseListener(new bottextareaMouseListener());
		botTextarea.setBackground(Colors.colorTextbackground);
		botPanel = new JScrollPane(botTextarea);
	    botPanel.setPreferredSize(new Dimension(200, 250));
	    botPanel.setBorder(BorderFactory.createMatteBorder(
                0, 10, 5, 10, Colors.colorCentral));
	}
	
	private void instantiateBotarea() {
		tablist = new TabList();
		instantiateBottext();
		instantiateTablist();
	    
		JPanel dualbottom = new JPanel();
		dualbottom.setLayout(new BorderLayout());
		dualbottom.add(tablistpanel, BorderLayout.NORTH);
		dualbottom.add(botPanel, BorderLayout.CENTER);
		
		getContentPane().add(dualbottom, BorderLayout.CENTER);
		botTextarea.setFont(consolasFont);
	}
	
	
	private void instantiateButtons() {
		JButton button = new LeftButton("Compute");
		JPanel westpanel = new JPanel();
		
		button.addActionListener(new computeButton());
		westpanel.add(button);
		getContentPane().add(westpanel, BorderLayout.WEST);
		
		button.setFont(consolasFontButton);
		button.setBackground(Colors.colorExterior);
		westpanel.setBackground(Colors.colorWestPanel);
		
		
	}
	
	
	/* 
	 * Misc functions
	 * 
	 */
	
	

	public void loadTheorem(String filename) {
		openedfile_path = "theorems/" + filename;
		File fcselected = new File(openedfile_path);
		String contenu = "";
		try {
			Scanner sc = new Scanner(fcselected); 
		    while (sc.hasNextLine()){ contenu += sc.nextLine() + "\n"; }
		    sc.close();
		} catch (IOException exp){}
		topTextarea.setText(contenu);	
		botTextarea.setText("");
	}
		
	
	private class tabclicker implements MouseListener, MouseMotionListener {

		public void mouseClicked(MouseEvent e) {
			tablist.mouseClicked(e);
			
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {
			tablist.mouseExited(e);
		}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {
			tablist.mouseReleased(e);
		}
		
		public void mouseDragged(MouseEvent e) {
			tablist.mouseDragged(e);
		}
		
		public void mouseMoved(MouseEvent e) {
			tablist.mouseMoved(e);
		}
		
	}
	
	private class computeButton implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			try {
				compiler.refreshUnit();
				Theorem thm = compiler.readUnitFromEditor();
				tablist.addnew(thm);
				
			} catch (GenException e) { e.explain(); }
		}	
	}
	
	private class bottextareaMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			botTextarea.jTextPanelMouseClicked(e);	
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
	}
	
	
}
