package Graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.*;



public class Menubar extends JMenuBar{

	private JMenu mFile;
	private Application source;
	
	public Menubar(Application source) {
		this.source = source;
		mFile = new JMenu("File");
		JMenuItem subOpen = new JMenuItem("Open File...");
		subOpen.addActionListener(new OpenMenuListener());
		JMenuItem subSave = new JMenuItem("Save As..");
        subSave.addActionListener(new SaveMenuListener());
        mFile.add(subOpen);
        mFile.add(subSave);
        this.add(mFile);
	}
	
	
	private class OpenMenuListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			File fcselected = searchFile("Choose .txt file to open.");
			StringBuilder contenu = new StringBuilder();
			try {
				Scanner sc = new Scanner(fcselected);
				while (sc.hasNextLine()){ contenu.append(sc.nextLine()).append("\n"); }
				sc.close();
			} catch (IOException exp){
				contenu = new StringBuilder("<Le fichier n'a pas pu �tre ouvert...>");
			}
			source.topTextarea.setText(contenu.toString());
		}
	}
	
	
	private class SaveMenuListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			File file = searchFile("Save file.");
			try {
				Files.write(Paths.get(file.getAbsolutePath()), source.topTextarea.getText().getBytes());
			} catch (IOException ignored){}
		}
	}
	
	
	// Cette fonction demande � l'utilisateur d'aller chercher un fichier
    private File searchFile(String dialog){
    	
    	JFileChooser fc;
    	fc = new JFileChooser();
    	String initialpath = ".";
    	if (source.openedfile_path != null) initialpath = source.openedfile_path;
		fc.setCurrentDirectory(new java.io.File(initialpath));
		fc.setDialogTitle(dialog);
		fc.showOpenDialog(new JButton());

		File selectedFile = fc.getSelectedFile();
		source.openedfile_path = selectedFile.getPath();

		return selectedFile;
    }
    
}
