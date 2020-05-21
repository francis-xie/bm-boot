package com.emis.admin;

import javax.swing.*;
import java.awt.*;

public class adminGUI {
  boolean packFrame = false;

  /**Construct the application*/
  public adminGUI() {
    Frame1 frame = new Frame1();
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    int _h = screenSize.height / 4;
    int _w = screenSize.width  / 4;
    frame.setBounds((screenSize.height - _h*3)/2,(screenSize.width-_w*3)/2,_w * 3 , _h * 3);
    frame.setVisible(true);
    frame.adjustPosition();
  }
  /**Main method*/
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    new adminGUI();
  }
}