package Graphics;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.util.Vector;

public class fonthelper {

    private JComponent ui = null;
    private String text = "≡ ¬(∃ x ¬(x > 0))    (2)";

    fonthelper() {
        initUI();
    }

    public void initUI() {
        if (ui!=null) return;

        ui = new JPanel(new BorderLayout(4,4));
        ui.setBorder(new EmptyBorder(4,4,4,4));

        String[] fontFamilies = GraphicsEnvironment.
                getLocalGraphicsEnvironment().
                getAvailableFontFamilyNames();
        Vector<String> croatFreindlyFonts = new Vector<String>();
        for (String name : fontFamilies) {
            Font font = new Font(name, Font.PLAIN, 20);
            if (font.canDisplayUpTo(text)<0) {
                croatFreindlyFonts.add(name);
            }
        }
        @SuppressWarnings({ "rawtypes", "unchecked" })
		final JList list = new JList(croatFreindlyFonts);
        list.setVisibleRowCount(20);
        list.getSelectionModel().setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        ui.add(new JScrollPane(list), BorderLayout.LINE_START);

        final JTextArea output = new JTextArea(text, 2, 12);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        ui.add(new JScrollPane(output));

        ListSelectionListener showFontListener = new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Font f = new Font(
                        list.getSelectedValue().toString(), Font.PLAIN, 50);
                output.setFont(f);
            }
        };
        list.addListSelectionListener(showFontListener);
        list.setSelectedIndex(0);
    }

    public JComponent getUI() {
        return ui;
    }

    public static void main(String[] args) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception useDefault) {
                }
                fonthelper o = new fonthelper();

                JFrame f = new JFrame("Croation Text in GUI");
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.setLocationByPlatform(true);

                f.setContentPane(o.getUI());
                f.pack();
                f.setMinimumSize(f.getSize());

                f.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);
    }
}