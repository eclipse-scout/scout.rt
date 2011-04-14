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
package org.eclipse.scout.rt.ui.swing.simulator;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public final class SwingScoutSimulator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutSimulator.class);

  private static SwingScoutSimulator instance = new SwingScoutSimulator();

  public static SwingScoutSimulator getInstance() {
    return instance;
  }

  private P_AWTEventListener m_awtListener;
  private Window m_activeWindow;
  private long m_recordingStart;
  private ArrayList<Runnable> m_recordingScript;
  private ArrayList<Runnable> m_recordedScript;

  private SwingScoutSimulator() {

  }

  public void attach() {
    if (m_awtListener == null) {
      m_awtListener = new P_AWTEventListener();
      Toolkit.getDefaultToolkit().addAWTEventListener(m_awtListener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
    }
  }

  public void detach() {
    if (m_awtListener != null) {
      Toolkit.getDefaultToolkit().removeAWTEventListener(m_awtListener);
      m_awtListener = null;
    }
  }

  public void record() {
    System.out.println("RECORD");
    m_recordingStart = System.currentTimeMillis();
    m_recordingScript = new ArrayList<Runnable>();
  }

  public void stop() {
    m_recordedScript = m_recordingScript;
    System.out.println("STOP");
    m_recordingScript = null;
  }

  public void play() {
    play(1);
  }

  public void play(int count) {
    final ArrayList fList = m_recordedScript;
    final int fCount = count;
    if (fList != null) {
      new Thread() {
        @Override
        public void run() {
          playWorker(fList, fCount);
        }
      }.start();
    }
  }

  private void playWorker(ArrayList script, int count) {
    Exception ex = null;
    for (int index = 0; index < count && ex == null; index++) {
      System.out.println("PLAY " + (index + 1) + " OF " + count);
      long startTime = System.currentTimeMillis();
      for (Iterator it = m_recordedScript.iterator(); it.hasNext() && ex == null;) {
        Object o = it.next();
        try {
          if (o instanceof P_MouseTask) {
            P_MouseTask task = (P_MouseTask) o;
            long dt = (startTime + task.getTimeOffset()) - System.currentTimeMillis() - 10;
            if (dt > 0) try {
              Thread.sleep(dt);
            }
            catch (InterruptedException ie) {
            }
            SwingUtilities.invokeAndWait(task);
          }
          else if (o instanceof P_KeyStrokeTask) {
            P_KeyStrokeTask task = (P_KeyStrokeTask) o;
            long dt = (startTime + task.getTimeOffset()) - System.currentTimeMillis() - 10;
            if (dt > 0) try {
              Thread.sleep(dt);
            }
            catch (InterruptedException ie) {
            }
            SwingUtilities.invokeAndWait(task);
          }
        }
        catch (Exception e) {
          ex = e;
        }
      }
    }
    if (ex != null) {
      ex.printStackTrace();
    }
  }

  private Window getActiveWindow() {
    Window w = m_activeWindow;
    if (w != null) {
      Window[] wa = w.getOwnedWindows();
      if (wa != null) {
        for (int i = 0; i < wa.length; i++) {
          if (wa[i].isShowing()) {
            w = wa[i];
            break;
          }
        }
      }
    }
    return w;
  }

  private Component getDeepestComponentAt(Component parent, int x, int y) {
    if (!parent.contains(x, y)) {
      return null;
    }
    if (parent instanceof Container) {
      Component[] components = ((Container) parent).getComponents();
      for (int i = 0; i < components.length; i++) {
        Component comp = components[i];
        if (comp != null && comp.isVisible()) {
          Point loc = comp.getLocation();
          if (comp instanceof Container) {
            comp = getDeepestComponentAt(comp, x - loc.x, y - loc.y);
          }
          else {
            comp = comp.getComponentAt(x - loc.x, y - loc.y);
          }
          if (comp != null && comp.isVisible()) {
            return comp;
          }
        }
      }
    }
    return parent;
  }

  /**
   * private classes
   */
  private class P_AWTEventListener implements AWTEventListener {
    @Override
    public void eventDispatched(AWTEvent e) {
      if (e instanceof MouseEvent) {
        if (m_recordingScript != null) {
          MouseEvent me = (MouseEvent) e;
          switch (e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_RELEASED:
            case MouseEvent.MOUSE_CLICKED: {
              if (me.getComponent().isShowing()) {
                m_recordingScript.add(new P_MouseTask(me, me.getComponent().getLocationOnScreen(), System.currentTimeMillis() - m_recordingStart));
              }
              break;
            }
          }
        }
      }
      else if (e instanceof KeyEvent) {
        if (m_recordingScript != null) {
          KeyEvent ke = (KeyEvent) e;
          switch (e.getID()) {
            case KeyEvent.KEY_PRESSED:
            case KeyEvent.KEY_RELEASED:
            case KeyEvent.KEY_TYPED: {
              if (ke.getComponent().isShowing()) {
                if (ke.getKeyCode() == KeyEvent.VK_S && (ke.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                  // the stop shortcut
                }
                else {
                  m_recordingScript.add(new P_KeyStrokeTask(ke, ke.getComponent().getLocationOnScreen(), System.currentTimeMillis() - m_recordingStart));
                }
              }
              break;
            }
          }
        }
      }
      else if (e instanceof WindowEvent) {
        WindowEvent we = (WindowEvent) e;
        switch (e.getID()) {
          case WindowEvent.WINDOW_ACTIVATED: {
            Component c = we.getComponent();
            while (c != null && c.isLightweight()) {
              c = c.getParent();
            }
            if (c != null) {
              m_activeWindow = (Window) we.getComponent();
            }
            break;
          }
        }
      }
    }
  }// end private class

  private class P_MouseTask implements Runnable {
    private MouseEvent m_event;
    private Point m_screenLocation;
    private long m_timeOffset;

    public P_MouseTask(MouseEvent e, Point screenLocation, long timeOffset) {
      m_event = e;
      m_screenLocation = screenLocation;
      m_timeOffset = timeOffset;
    }

    public long getTimeOffset() {
      return m_timeOffset;
    }

    @Override
    public void run() {
      Window w = getActiveWindow();
      if (w != null) {
        Component c = getDeepestComponentAt(w, m_screenLocation.x - w.getX(), m_screenLocation.y - w.getY());
        if (c != null) {
          MouseEvent syntEvent = new MouseEvent(
              c,
              m_event.getID(),
              System.currentTimeMillis(),
              m_event.getModifiers(),
              m_event.getX(),
              m_event.getY(),
              m_event.getClickCount(),
              m_event.isPopupTrigger(),
              m_event.getButton()
              );
          syntEvent.getComponent().dispatchEvent(syntEvent);
        }
      }
    }
  }// end private class

  private class P_KeyStrokeTask implements Runnable {
    private KeyEvent m_event;
    private Point m_screenLocation;
    private long m_timeOffset;

    public P_KeyStrokeTask(KeyEvent e, Point screenLocation, long timeOffset) {
      m_event = e;
      m_screenLocation = screenLocation;
      m_timeOffset = timeOffset;
    }

    public long getTimeOffset() {
      return m_timeOffset;
    }

    @Override
    public void run() {
      Window w = getActiveWindow();
      if (w != null) {
        Component c = getDeepestComponentAt(w, m_screenLocation.x - w.getX(), m_screenLocation.y - w.getY());
        if (c != null) {
          KeyEvent syntEvent = new KeyEvent(
              c,
              m_event.getID(),
              System.currentTimeMillis(),
              m_event.getModifiers(),
              m_event.getKeyCode(),
              m_event.getKeyChar(),
              m_event.getKeyLocation()
              );
          syntEvent.getComponent().dispatchEvent(syntEvent);
        }
      }
    }
  }// end private class
}
