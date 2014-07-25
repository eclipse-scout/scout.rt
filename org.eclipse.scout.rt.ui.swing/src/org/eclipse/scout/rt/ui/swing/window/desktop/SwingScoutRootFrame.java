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
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

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

import org.eclipse.core.runtime.Platform;
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
import org.eclipse.scout.rt.ui.swing.ext.busy.SwingBusyIndicator;
import org.eclipse.scout.rt.ui.swing.ext.internal.LogicalGridLayoutSpyAction;
import org.eclipse.scout.rt.ui.swing.focus.SwingScoutFocusTraversalPolicy;
import org.eclipse.scout.rt.ui.swing.window.desktop.layout.MultiSplitDesktopManager;
import org.eclipse.scout.rt.ui.swing.window.desktop.menubar.SwingScoutMenuBar;
import org.eclipse.scout.rt.ui.swing.window.desktop.status.SwingScoutStatusBar;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.SwingScoutHeaderPanel;
import org.eclipse.scout.rt.ui.swing.window.desktop.tray.ISwingScoutTray;

/**
 * Checks UIManager.get("MenuBar.policy") == "menubar" (alternative: "toolbar")
 * <p>
 * Checks UIManager.get("StatusBar.visible") == "true"
 */
public class SwingScoutRootFrame extends SwingScoutComposite<IDesktop> implements ISwingScoutRootFrame {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutRootFrame.class);

  private Frame m_swingFrame;
  private JRootPane m_swingRootPane;
  private P_ScoutDesktopListener m_scoutDesktopListener;
  private ISwingScoutDesktop m_swingScoutDesktop;
  private SwingScoutMenuBar m_menuBarComposite;
  private SwingScoutHeaderPanel m_swingScoutHeaderPanel;
  private SwingScoutStatusBar m_statusBarComposite;
  // cache
  private String m_title;
  private Set<IKeyStroke> m_installedScoutKs;

  public SwingScoutRootFrame(Frame frame) {
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
    m_swingRootPane.putClientProperty(SwingBusyIndicator.BUSY_SUPPORTED_CLIENT_PROPERTY, true);
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
    if (isShowMenuBar()) {
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
    }
    // content
    Container contentPane = m_swingRootPane.getContentPane();
    contentPane.removeAll();
    contentPane.setLayout(new BorderLayoutEx(0, 0));
    // header panel (navigation, view buttons, tool buttons)
    m_swingScoutHeaderPanel = createSwingScoutHeaderPanel();
    m_swingScoutHeaderPanel.createField(getScoutObject(), getSwingEnvironment());
    if (!m_swingScoutHeaderPanel.isEmpty()) {
      JComponent swingHeaderPanel = m_swingScoutHeaderPanel.getSwingField();
      contentPane.add(swingHeaderPanel, BorderLayoutEx.NORTH);
    }
    // desktop pane
    JPanel spacerPanel = new JPanelEx();
    if (!SwingUtility.isSynth()) {
      spacerPanel.setBorder(new EmptyBorder(0, 1, 3, 1));
    }
    spacerPanel.setName("RootFrame.Spacer");
    spacerPanel.setLayout(new SingleLayout());
    if (UIManager.get("desktop") != null) {
      spacerPanel.setBackground(UIManager.getColor("desktop"));
    }
    m_swingScoutDesktop = getSwingEnvironment().createDesktop(m_swingFrame, getScoutObject());
    JComponent desktopPane = m_swingScoutDesktop.getSwingDesktopPane();
    desktopPane.setCursor(null);
    spacerPanel.add(desktopPane);
    contentPane.add(spacerPanel, BorderLayoutEx.CENTER);

    // activity pane
    if (isShowStatusBar()) {
      m_statusBarComposite = new SwingScoutStatusBar(SwingScoutStatusBar.VISIBLE_ALWAYS);
      m_statusBarComposite.createField(getScoutObject(), getSwingEnvironment());
      contentPane.add(m_statusBarComposite.getSwingStatusBar(), BorderLayout.SOUTH);
    }

    m_swingFrame.pack();
    if (m_swingFrame instanceof JFrame) {
      // register ctrl-TAB and ctrl-shift-TAB actions according to ui
      JComponent comp = (JComponent) ((JFrame) m_swingFrame).getContentPane();
      comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ctrl TAB"), "selectFirstFrame");
      comp.getActionMap().put("selectFirstFrame", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          if (m_swingScoutDesktop != null) {
            JInternalFrame[] a = m_swingScoutDesktop.getSwingDesktopPane().getAllFrames();
            if (a != null && a.length > 0) {
              a[0].getContentPane().transferFocus();
            }
          }
        }
      });
      // restore bounds and maximization
      ClientUIPreferences prefs = ClientUIPreferences.getInstance(getSwingEnvironment().getScoutSession());
      Rectangle r = SwingUtility.createRectangle(prefs.getApplicationWindowBounds());
      if (r != null) {
        r = SwingUtility.validateRectangleOnScreen(r, false, false);
      }
      boolean maximized = prefs.getApplicationWindowMaximized();
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
      // register layout spy
      if (Platform.inDevelopmentMode()) {
        ((JComponent) contentPane).getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("shift alt F2"), "layoutSpy");
        ((JComponent) contentPane).getActionMap().put("layoutSpy", new LogicalGridLayoutSpyAction());
      }
    }
  }

  private boolean isShowMenuBar() {
    String s = UIManager.getString("MenuBar.policy");
    return s == null || "menubar".equals(s);
  }

  private boolean isShowStatusBar() {
    Boolean b = (Boolean) UIManager.get("StatusBar.visible");
    return b == null || b.booleanValue();
  }

  @Override
  public Frame getSwingFrame() {
    return m_swingFrame;
  }

  @Override
  public ISwingScoutDesktop getDesktopComposite() {
    return m_swingScoutDesktop;
  }

  public SwingScoutHeaderPanel getSwingScoutHeaderPanel() {
    return m_swingScoutHeaderPanel;
  }

  /**
   * start GUI process by presenting a desktop frame use this method to show the
   * dialog, not getSwingFrame().setVisible()
   */
  @Override
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
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_scoutDesktopListener == null) {
      m_scoutDesktopListener = new P_ScoutDesktopListener();
      getScoutObject().addDesktopListener(m_scoutDesktopListener);
    }
    IDesktop f = getScoutObject();
    setTitleFromScout(f.getTitle());
    setKeyStrokesFromScout();
  }

  /*
   * properties
   */
  protected void setTitleFromScout(String s) {
    m_title = (s == null ? "" : s);
    m_swingFrame.setTitle(m_title);
  }

  protected void setKeyStrokesFromScout() {
    JComponent component = (JComponent) m_swingRootPane.getContentPane();
    if (component != null) {
      // remove old key strokes
      if (m_installedScoutKs != null) {
        for (IKeyStroke scoutKs : m_installedScoutKs) {
          KeyStroke swingKs = SwingUtility.createKeystroke(scoutKs);
          //
          InputMap imap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
          imap.remove(swingKs);
          ActionMap amap = component.getActionMap();
          amap.remove(scoutKs.getActionId());
        }
      }
      m_installedScoutKs = null;
      // add new key strokes
      Set<IKeyStroke> scoutKeyStrokes = getScoutObject().getKeyStrokes();
      for (IKeyStroke scoutKs : scoutKeyStrokes) {
        int swingWhen = JComponent.WHEN_IN_FOCUSED_WINDOW;
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

  protected void setStatusFromScout() {
    if (getScoutObject() != null) {
      IProcessingStatus newStatus = getScoutObject().getStatus();
      //when a tray item is available, use it, otherwise set status on views/dialogs
      TrayIcon trayItem = null;
      ISwingScoutTray trayComposite = getSwingEnvironment().getTrayComposite();
      if (trayComposite != null) {
        trayItem = trayComposite.getSwingTrayIcon();
      }
      if (trayItem != null) {
        String title = newStatus != null ? newStatus.getTitle() : null;
        String text = newStatus != null ? newStatus.getMessage() : null;
        if (newStatus != null && text != null) {
          // icon
          TrayIcon.MessageType iconId;
          switch (newStatus.getSeverity()) {
            case IProcessingStatus.ERROR:
            case IProcessingStatus.FATAL: {
              iconId = MessageType.ERROR;
              break;
            }
            case IProcessingStatus.WARNING: {
              iconId = MessageType.WARNING;
              break;
            }
            case IProcessingStatus.INFO: {
              iconId = MessageType.INFO;
              break;
            }
            default: {
              iconId = MessageType.NONE;
              break;
            }
          }
          trayItem.displayMessage(title, text, iconId);
        }
        else {
          //nop
        }
      }
      else {
        //show on status bar
        setSwingStatus(newStatus);
      }
    }
  }

  /*
   * event handlers
   */
  protected void handleSwingWindowOpened(WindowEvent e) {
    // fit initial size of views
    if (getDesktopComposite() != null) {
      DesktopManager dm = getDesktopComposite().getSwingDesktopPane().getDesktopManager();
      if (dm instanceof MultiSplitDesktopManager) {
        ((MultiSplitDesktopManager) dm).fitFrames(getDesktopComposite().getSwingDesktopPane().getAllFrames());
      }
    }
  }

  protected void handleSwingWindowActivated(WindowEvent e) {
  }

  protected void handleSwingWindowClosing(WindowEvent e) {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireDesktopClosingFromUI(false);
      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  protected void handleScoutDesktopClosedInSwing(DesktopEvent e) {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (focusOwner != null && focusOwner instanceof JComponent && ((JComponent) focusOwner).getInputVerifier() != null) {
      boolean ok = ((JComponent) focusOwner).getInputVerifier().verify((JComponent) focusOwner);
      if (!ok) {
        return;
      }
    }
    final boolean maximized = ((m_swingFrame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0);
    Rectangle r = m_swingFrame.getBounds();
    // <bsh 2010-10-21>
    // Even if window is maximized, store the non-maximized size. JFramEx can provide us
    // with this size. For all other frames, we have to first set the extended state to
    // NORMAL and then read the window bounds (this procedure is visible to the user).
    if (maximized) {
      if (m_swingFrame instanceof JFrameEx) {
        r = ((JFrameEx) m_swingFrame).getNonMaximizedBounds();
        if (r == null) {
          r = m_swingFrame.getBounds(); // fall-back
        }
      }
      else {
        m_swingFrame.setExtendedState(Frame.NORMAL);
        r = m_swingFrame.getBounds();
      }
    }
    // </bsh>
    ClientUIPreferences.getInstance(getSwingEnvironment().getScoutSession()).setApplicationWindowPreferences(r != null ? new BoundsSpec(r.x, r.y, r.width, r.height) : null, maximized);

    // desktop detach
    disconnectFromScout();
    m_swingFrame.dispose();
  }

  protected void handleTraverseFocusFromScout(boolean forward) {
    Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (comp != null) {
      if (forward) {
        comp.transferFocus();
      }
      else {
        comp.transferFocusBackward();
      }
    }
  }

  protected void handleScoutPrintInSwing(DesktopEvent e) {
    final WidgetPrinter cp = new WidgetPrinter(getSwingFrame());
    try {
      cp.print(e.getPrintDevice(), e.getPrintParameters());
    }
    catch (Throwable ex) {
      LOG.error(null, ex);
    }
    finally {
      Runnable r = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireDesktopPrintedFromUI(cp.getOutputFile());
        }
      };
      getSwingEnvironment().invokeScoutLater(r, 0);
    }
  }

  /*
   * extended property observer
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (IDesktop.PROP_TITLE.equals(name)) {
      setTitleFromScout((String) newValue);
    }
    else if (IDesktop.PROP_KEY_STROKES.equals(name)) {
      setKeyStrokesFromScout();
    }
    else if (IDesktop.PROP_STATUS.equals(name)) {
      setStatusFromScout();
    }
  }

  /**
   * To be overwritten to install a custom header panel
   * 
   * @return
   */
  protected SwingScoutHeaderPanel createSwingScoutHeaderPanel() {
    return new SwingScoutHeaderPanel();
  }

  /*
   * other observers
   */
  private class P_ScoutDesktopListener implements DesktopListener {
    @Override
    public void desktopChanged(final DesktopEvent e) {
      switch (e.getType()) {
        case DesktopEvent.TYPE_PRINT: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleScoutPrintInSwing(e);
            }
          };
          getSwingEnvironment().invokeSwingLater(t);
          break;
        }
        case DesktopEvent.TYPE_DESKTOP_CLOSED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleScoutDesktopClosedInSwing(e);
            }
          };
          getSwingEnvironment().invokeSwingAndWait(t, 60000);
          break;
        }
        case DesktopEvent.TYPE_TRAVERSE_FOCUS_NEXT: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleTraverseFocusFromScout(true);
            }
          };
          getSwingEnvironment().invokeSwingLater(t);
          break;
        }
        case DesktopEvent.TYPE_TRAVERSE_FOCUS_PREVIOUS: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleTraverseFocusFromScout(false);
            }
          };
          getSwingEnvironment().invokeSwingLater(t);
          break;
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
