package common;

import javax.swing.*;
import java.awt.*;

public class RGBViewer extends JFrame {

    public RGBViewer() {
        super("RGB Emulator");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(Color.black);
        this.setVisible(true);
    }

    public void setColour(LedState s){
        this.getContentPane().setBackground(new Color(s.getRed(), s.getGreen(), s.getBlue()));
    }

}
