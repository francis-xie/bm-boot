// test
package com.emis.admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;


public class Frame1 extends JFrame {
  JPanel contentPane;
  JMenuBar MenuBar = new JMenuBar();
  JMenu MenuMain = new JMenu();
  JMenuItem MenuExit = new JMenuItem();
  JMenu MenuHelp = new JMenu();
  JMenuItem MenuHelpAbout = new JMenuItem();
  GridLayout gridLayout = new GridLayout();
  JMenuItem MenuConnect = new JMenuItem();
  JSplitPane spliter1 = new JSplitPane();
  JPanel rightPanel = new JPanel();
  JPanel NodePanel = new JPanel();
  JSplitPane spliter2 = new JSplitPane();
  JPanel applyPanel = new JPanel();
  JScrollPane PropPanel = new JScrollPane();
  GridLayout gridLayout1 = new GridLayout();
  GridLayout gridLayout2 = new GridLayout();

  /**Construct the frame*/
  public Frame1() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  /**Component initialization*/
  private void jbInit() throws Exception  {
    //setIconImage(Toolkit.getDefaultToolkit().createImage(Frame1.class.getResource("[Your Icon]")));
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(gridLayout);
    this.setSize(new Dimension(400, 300));
    this.setTitle("Frame Title");
    MenuMain.setActionCommand("Main");
    MenuMain.setText("Main");
    MenuExit.setText("Exit");
    MenuExit.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        MenuExit_actionPerformed(e);
      }
    });
    MenuHelp.setText("Help");
    MenuHelpAbout.setText("About");
    MenuHelpAbout.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        MenuHelpAbout_actionPerformed(e);
      }
    });
    MenuConnect.setText("Connect");
    MenuConnect.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MenuConnect_actionPerformed(e);
      }
    });
    spliter2.setOrientation(JSplitPane.VERTICAL_SPLIT);
    rightPanel.setLayout(gridLayout1);
    applyPanel.setLayout(gridLayout2);
    spliter1.setToolTipText("");
    spliter1.setLeftComponent(NodePanel);
    spliter1.setRightComponent(rightPanel);
    contentPane.setAlignmentX((float) 0.7);
    contentPane.setAlignmentY((float) 0.7);
    contentPane.setMinimumSize(new Dimension(43, 40));
    contentPane.setPreferredSize(new Dimension(24, 15));
    MenuMain.add(MenuConnect);
    MenuMain.add(MenuExit);
    MenuHelp.add(MenuHelpAbout);
    MenuBar.add(MenuMain);
    MenuBar.add(MenuHelp);
    contentPane.add(spliter1, null);
    spliter1.add(rightPanel, JSplitPane.RIGHT);
    spliter1.add(NodePanel, JSplitPane.LEFT);
    rightPanel.add(spliter2, null);
    spliter2.add(applyPanel, JSplitPane.TOP);
    spliter2.add(PropPanel, JSplitPane.BOTTOM);
    this.setJMenuBar(MenuBar);
  }

  public void adjustPosition ()
  {
    spliter1.setDividerLocation((int) contentPane.getBounds().getWidth() / 4);
    spliter2.setDividerLocation((int) rightPanel.getBounds().getHeight() * 5 / 6);
  }

  /**File | Exit action performed*/
  public void MenuExit_actionPerformed(ActionEvent e) {
    System.exit(0);
  }
  /**Help | About action performed*/
  public void MenuHelpAbout_actionPerformed(ActionEvent e) {
    Frame1_AboutBox dlg = new Frame1_AboutBox(this);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.show();
  }
  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      MenuExit_actionPerformed(null);
    }
  }

  void MenuConnect_actionPerformed(ActionEvent e) {
  // connect
  }
}