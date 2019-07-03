package Graphics;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

public class LeftButton extends JButton {

		private int bgstate;
		final Border ispressed = BorderFactory.createMatteBorder(6, 20, 6, 20, new Color(240, 240, 240));
		final Border ishovered = BorderFactory.createMatteBorder(6, 20, 6, 20, Colors.colorTextbackground);
		final Border isalone = BorderFactory.createMatteBorder(6, 20, 6, 20, Colors.colorExterior);
		protected LeftButton() {
            this(null);
        }

		protected LeftButton(String text) {
            super(text);
            super.setContentAreaFilled(false);
            setBorder(BorderFactory.createMatteBorder(
                    6, 20, 6, 20, Colors.colorExterior));
            bgstate = 0;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isPressed()) {
                g.setColor(new Color(240, 240, 240));
                if (bgstate != 2) { bgstate = 2; setBorder(ispressed); }
                
            } else if (getModel().isRollover()) {
                g.setColor(Colors.colorTextbackground);
                if (bgstate != 1) { bgstate = 1; setBorder(ishovered); }
            } else {
            	g.setColor(Colors.colorExterior);
                if (bgstate != 0) { bgstate = 0; setBorder(isalone); }
            }
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }

        @Override
        public void setContentAreaFilled(boolean b) {
        }
        
    }