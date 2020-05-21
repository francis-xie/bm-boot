package com.emis.admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

public class Frame1_AboutBox extends JDialog implements ActionListener {

  JPanel panel1 = new JPanel();
  JPanel panel2 = new JPanel();
  JPanel insetsPanel1 = new JPanel();
  JPanel insetsPanel3 = new JPanel();
  JButton button1 = new JButton();
  JLabel Lversion = new JLabel();
  JLabel Lright = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  GridLayout gridLayout1 = new GridLayout();
  String product = "";
  String version = "EMIS Admin Tool 1.0";
  String copyright = "EMIS Company Copyright (c) 2000";
  String comments = "";
  public Frame1_AboutBox(Frame parent) {
    super(parent);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    pack();
  }
  /**Component initialization*/
  private void jbInit() throws Exception  {
    //imageLabel.setIcon(new ImageIcon(Frame1_AboutBox.class.getResource("[Your Image]")));
    this.setTitle("About");
    setResizable(false);
    panel1.setLayout(borderLayout1);
    panel2.setLayout(borderLayout2);
    gridLayout1.setRows(4);
    gridLayout1.setColumns(1);
    Lversion.setText(version);
    Lright.setText(copyright);
    insetsPanel3.setLayout(gridLayout1);
    insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
    button1.setText("Ok");
    button1.addActionListener(this);
    this.getContentPane().add(panel1, null);
    insetsPanel3.add(Lversion, null);
    insetsPanel3.add(Lright, null);
    panel1.add(insetsPanel1, BorderLayout.SOUTH);
    insetsPanel1.add(button1, null);
    panel2.add(insetsPanel3, BorderLayout.CENTER);
    panel1.add(panel2, BorderLayout.NORTH);
  }
  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      cancel();
    }
    super.processWindowEvent(e);
  }
  /**Close the dialog*/
  void cancel() {
    dispose();
  }
  /**Close the dialog on a button event*/
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == button1) {
      cancel();
    }
  }
}