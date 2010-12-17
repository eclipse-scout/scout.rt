/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.window.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DesktopManager;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.data.basic.BoundsSpec;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutAction;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.ComponentSpyAction;
import org.eclipse.scout.rt.ui.swing.ext.JFrameEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JRootPaneEx;
import org.eclipse.scout.rt.ui.swing.focus.SwingScoutFocusTraversalPolicy;
import org.eclipse.scout.rt.ui.swing.window.SwingWindowManager;
import org.eclipse.scout.rt.ui.swing.window.desktop.layout.MultiSplitDesktopManager;
import org.eclipse.scout.rt.ui.swing.window.desktop.menubar.SwingScoutMenuBar;
import org.eclipse.scout.rt.ui.swing.window.desktop.status.SwingScoutStatusBar;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.LegacySwingScoutToolBar;

public class LegacySwingScoutRootFrame extends SwingScoutComposite<IDesktop> implements ISwingScoutRootFrame {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LegacySwingScoutRootFrame.class);

  private Frame m_swingFrame;
  private JRootPane m_swingRootPane;
  private P_ScoutDesktopListener m_scoutDesktopListener;
  private P_SwingScoutRootListener m_swingScoutRootListener;
  private ISwingScoutDesktop m_desktopComposite;
  private SwingScoutMenuBar m_menuBarComposite;
  private LegacySwingScoutToolBar m_toolBarComposite;
  private SwingScoutStatusBar m_statusBarComposite;
  // cache
  private String m_title;
  private IKeyStroke[] m_installedScoutKs;

  public LegacySwingScoutRootFrame(ISwingEnvironment env, Frame frame, IDesktop scoutDesktop) {
    super();
    m_swingFrame = frame;
    if (m_swingFrame instanceof RootPaneContainer) {
      m_swingRootPane = ((RootPaneContainer) m_swingFrame).getRootPane();
    }
    else {
      m_swingRootPane = new JRootPaneEx();
      m_swingFrame.removeAll();
      m_swingFrame.add(m_swingRootPane);
    }
    createField(scoutDesktop, env);
  }

  @Override
  protected void initializeSwing() {
    if (m_swingFrame instanceof JFrame) {
      m_swingFrame.setState(JFrame.NORMAL);
      m_swingFrame.setResizable(true);
      ((JFrame) m_swingFrame).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    // focus handling
    SwingUtility.installFocusCycleRoot(m_swingRootPane, new SwingScoutFocusTraversalPolicy());
    //
    m_swingFrame.addWindowListener(new P_SwingWindowListener());
    // menubar
    m_menuBarComposite = new SwingScoutMenuBar();
    m_menuBarComposite.createField(getScoutObject(), getSwingEnvironment());
    if (!m_menuBarComposite.isEmpty()) {
      JMenuBar menuBar = m_menuBarComposite.getSwingMenuBar();
      if (m_swingFrame instanceof JFrame) {
        ((JFrame) m_swingFrame).setJMenuBar(menuBar);
      }
      else {
        m_swingRootPane.setJMenuBar(menuBar);
      }
    }
    // content
    Container contentPane = m_swingRootPane.getContentPane();
    contentPane.removeAll();
    contentPane.setLayout(new BorderLayoutEx(0, 0));
    // toolbar
    m_toolBarComposite = new LegacySwingScoutToolBar();
    m_toolBarComposite.createField(getScoutObject(), getSwingEnvironment());
    if (!m_toolBarComposite.isEmpty()) {
      JComponent toolBar = m_toolBarComposite.getSwingToolBar();
      contentPane.add(toolBar, BorderLayoutEx.NORTH);
    }
    // desktop pane
    JPanel spacerPanel = new JPanelEx();
    spacerPanel.setBorder(new EmptyBorder(0, 1, 3, 1));
    spacerPanel.setName("RootFrame.Spacer");
    spacerPanel.setLayout(new SingleLayout());
    if (UIManager.get("desktop") != null) {
      spacerPanel.setBackground(UIManager.getColor("desktop"));
    }
    m_desktopComposite = getSwingEnvironment().createDesktop(m_swingFrame, getScoutObject());
    JComponent desktopPane = m_desktopComposite.getSwingDesktopPane();
    desktopPane.setCursor(null);
    spacerPanel.add(desktopPane);
    contentPane.add(spacerPanel, BorderLayoutEx.CENTER);
    // activity pane
    m_statusBarComposite = new SwingScoutStatusBar(SwingScoutStatusBar.VISIBLE_ALWAYS);
    m_statusBarComposite.createField(getScoutObject(), getSwingEnvironment());
    contentPane.add(m_statusBarComposite.getSwingStatusBar(), BorderLayout.SOUTH);
    //
    m_swingFrame.pack();
    if (m_swingFrame instanceof JFrame) {
      // register ctrl-TAB and ctrl-shift-TAB actions according to ui
      JComponent comp = (JComponent) ((JFrame) m_swingFrame).getContentPane();
      comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ctrl TAB"), "selectFirstFrame");
      comp.getActionMap().put("selectFirstFrame", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          if (m_desktopComposite != null) {
            JInternalFrame[] a = m_desktopComposite.getSwingDesktopPane().getAllFrames();
            if (a != null && a.length > 0) {
              a[0].getContentPane().transferFocus();
            }
          }
        }
      });
      // restore bounds and maximization
      Rectangle r = SwingUtility.createRectangle(ClientUIPreferences.getInstance().getApplicationWindowBounds());
      if (r != null) {
        r = SwingUtility.validateRectangleOnScreen(r, false, false);
      }
      boolean maximized = ClientUIPreferences.getInstance().getApplicationWindowMaximized();
      //
      if (r == null) {
        r = SwingUtility.getFullScreenBoundsFor(new Rectangle(0, 0, 1, 1), false);
        r.grow(-120, -80);
      }
      m_swingFrame.setBounds(r);
      if (maximized) {
        m_swingFrame.setExtendedState(m_swingFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
      }
    }
    // register component spy
    if (contentPane instanceof JComponent) {
      ((JComponent) contentPane).getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("shift alt F1"), "componentSpy");
      ((JComponent) contentPane).getActionMap().put("componentSpy", new ComponentSpyAction());
    }
  }

  public Frame getSwingFrame() {
    return m_swingFrame;
  }

  public ISwingScoutDesktop getDesktopComposite() {
    return m_desktopComposite;
  }

  /**
   * start GUI process by presenting a desktop frame use this method to show the
   * dialog, not getSwingFrame().setVisible()
   */
  public void showSwingFrame() {
    m_swingFrame.setVisible(true);
  }

  @Override
  public void setSwingStatus(IProcessingStatus newStatus) {
    if (m_statusBarComposite != null) {
      m_statusBarComposite.setSwingStatus(newStatus);
    }
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutDesktopListener != null) {
      getScoutObject().removeDesktopListener(m_scoutDesktopListener);
      m_scoutDesktopListener = null;
    }
    if (m_swingScoutRootListener != null) {
      getSwingEnvironment().removePropertyChangeListener(m_swingScoutRootListener);
      m_swingScoutRootListener = null;
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_scoutDesktopListener == null) {
      m_scoutDesktopListener = new P_ScoutDesktopListener();
      getScoutObject().addDesktopListener(m_scoutDesktopListener);
    }
    if (m_swingScoutRootListener == null) {
      m_swingScoutRootListener = new P_SwingScoutRootListener();
      getSwingEnvironment().addPropertyChangeListener(m_swingScoutRootListener);
    }
    IDesktop f = getScoutObject();
    setTitleFromScout(f.getTitle());
    setKeyStrokesFromScout();
  }

  /*
   * properties
   */
  protected void setTitleFromScout(String s) {
    if (s == null) s = "";
    m_title = s;
    m_swingFrame.setTitle(m_title);
  }

  protected void setKeyStrokesFromScout() {
    JComponent component = (JComponent) m_swingRootPane.getContentPane();
    if (component != null) {
      // remove old key strokes
      if (m_installedScoutKs != null) {
        for (int i = 0; i < m_installedScoutKs.length; i++) {
          IKeyStroke scoutKs = m_installedScoutKs[i];
          KeyStroke swingKs = SwingUtility.createKeystroke(scoutKs);
          //
          InputMap imap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
          imap.remove(swingKs);
          ActionMap amap = component.getActionMap();
          amap.remove(scoutKs.getActionId());
        }
      }
      m_installedScoutKs = null;
      // add new key strokes
      IKeyStroke[] scoutKeyStrokes = getScoutObject().getKeyStrokes();
      for (IKeyStroke scoutKs : scoutKeyStrokes) {
        int swingWhen = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
        KeyStroke swingKs = SwingUtility.createKeystroke(scoutKs);
        SwingScoutAction<IAction> action = new SwingScoutAction<IAction>();
        action.createField(scoutKs, getSwingEnvironment());
        //
        InputMap imap = component.getInputMap(swingWhen);
        imap.put(swingKs, scoutKs.getActionId());
        ActionMap amap = component.getActionMap();
        amap.put(scoutKs.getActionId(), action.getSwingAction());
      }
      m_installedScoutKs = scoutKeyStrokes;
    }
  }

  /*
   * event handlers
   */
  protected void handleSwingWindowOpened(WindowEvent e) {
    SwingWindowManager.getInstance().setActiveWindow(m_swingFrame);
    // fit initial size of views
    if (getDesktopComposite() != null) {
      DesktopManager dm = getDesktopComposite().getSwingDesktopPane().getDesktopManager();
      if (dm instanceof MultiSplitDesktopManager) {
        ((MultiSplitDesktopManager) dm).fitFrames(getDesktopComposite().getSwingDesktopPane().getAllFrames());
      }
    }
  }

  protected void handleSwingWindowActivated(WindowEvent e) {
    SwingWindowManager.getInstance().setActiveWindow(m_swingFrame);
  }

  protected void handleSwingWindowClosing(WindowEvent e) {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (focusOwner != null && focusOwner instanceof JComponent && ((JComponent) focusOwner).getInputVerifier() != null) {
      boolean ok = ((JComponent) focusOwner).getInputVerifier().verify((JComponent) focusOwner);
      if (!ok) return;
    }
    final boolean maximized = ((m_swingFrame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0);
    final Rectangle r = maximized ? null : m_swingFrame.getBounds();
    ClientUIPreferences.getInstance().setApplicationWindowPreferences(r != null ? new BoundsSpec(r.x, r.y, r.width, r.height) : null, maximized);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireGuiDetached();
        getScoutObject().getUIFacade().fireDesktopClosingFromUI();

      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  protected void handleScoutDesktopClosedInSwing(DesktopEvent e) {
    // desktop detach
    disconnectFromScout();
    m_swingFrame.dispose();
  }

  protected void handleScoutPrintInSwing(DesktopEvent e) {
    WidgetPrinter cp = new WidgetPrinter(getSwingFrame());
    try {
      cp.print(e.getPrintDevice(), e.getPrintParameters());
    }
    catch (Throwable ex) {
      LOG.error(null, ex);
    }
  }

  /*
   * extended property observer
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IDesktop.PROP_TITLE)) {
      setTitleFromScout((String) newValue);
    }
    else if (name.equals(IDesktop.PROP_KEY_STROKES)) {
      setKeyStrokesFromScout();
    }
  }

  /*
   * other observers
   */
  private class P_ScoutDesktopListener implements DesktopListener {
    public void desktopChanged(final DesktopEvent e) {
      switch (e.getType()) {
        case DesktopEvent.TYPE_PRINT:
        case DesktopEvent.TYPE_DESKTOP_CLOSED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              switch (e.getType()) {
                case DesktopEvent.TYPE_PRINT: {
                  handleScoutPrintInSwing(e);
                  break;
                }
                case DesktopEvent.TYPE_DESKTOP_CLOSED: {
                  handleScoutDesktopClosedInSwing(e);
                  break;
                }
              }
            }
          };
          getSwingEnvironment().invokeSwingLater(t);
          break;
        }
      }
    }
  }// end private class

  private class P_SwingScoutRootListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(ISwingEnvironment.PROP_BUSY)) {
        boolean busy = ((Boolean) e.getNewValue()).booleanValue();
        if (m_swingFrame instanceof JFrameEx) {
          ((JFrameEx) m_swingFrame).setWaitCursor(busy);
        }
      }
    }
  }// end private class

  private class P_SwingWindowListener extends WindowAdapter {
    @Override
    public void windowOpened(WindowEvent e) {
      handleSwingWindowOpened(e);
    }

    @Override
    public void windowActivated(WindowEvent e) {
      handleSwingWindowActivated(e);
    }

    @Override
    public void windowClosing(WindowEvent e) {
      handleSwingWindowClosing(e);
    }
  }// end private class

}
