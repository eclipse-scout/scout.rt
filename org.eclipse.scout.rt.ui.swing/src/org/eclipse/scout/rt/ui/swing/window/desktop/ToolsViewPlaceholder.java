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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * Provides a single access point to the tools view, which is in fact not really a view but
 * a collection of JInternalFrames placed in the right most column of the MultiSplitLayout
 */
public class ToolsViewPlaceholder {
  private final Collection<ComponentListener> m_listeners = new ArrayList<ComponentListener>();
  private final ArrayList<JInternalFrame> m_frames = new ArrayList<JInternalFrame>();
  private final P_FrameListener m_frameListener = new P_FrameListener();
  private final JDesktopPane m_desktopPane;
  private final ColumnSplitStrategyWithToolsView m_columnSplitStrategy;
  private int m_minWidth;
  private boolean m_expanded;

  public ToolsViewPlaceholder(JDesktopPane desktopPane, ColumnSplitStrategyWithToolsView columnSplitStrategy) {
    m_desktopPane = desktopPane;
    m_columnSplitStrategy = columnSplitStrategy;
  }

  private void fireComponentResized(ComponentEvent e) {
    for (ComponentListener l : m_listeners) {
      l.componentResized(e);
    }
  }

  public void addComponentListener(ComponentListener l) {
    m_listeners.add(l);
  }

  public void removeComponentListener(ComponentListener l) {
    m_listeners.remove(l);
  }

  public int getWidth() {
    if (m_frames.size() > 0) {
      return m_frames.get(m_frames.size() - 1).getWidth();
    }
    else {
      return 0;
    }
  }

  public void addFrame(JInternalFrame frame) {
    m_frames.add(frame);
    frame.addComponentListener(m_frameListener);
    frame.addInternalFrameListener(m_frameListener);
  }

  public void removeFrame(JInternalFrame frame) {
    if (m_frames.contains(frame)) {
      m_frames.remove(frame);
      frame.removeComponentListener(m_frameListener);
      frame.removeInternalFrameListener(m_frameListener);
    }
  }

  public void setMinimumWidth(int minWidth) {
    m_minWidth = minWidth;
    m_columnSplitStrategy.setToolsViewMinWidth(m_minWidth);
    m_desktopPane.revalidate();
    m_desktopPane.repaint();
  }

  public void expandView() {
    //set split position to minimal location
    if (m_minWidth > 0) {
      m_columnSplitStrategy.setToolsViewWidth(m_minWidth);
    }
    m_expanded = true;
    m_desktopPane.revalidate();
    m_desktopPane.repaint();
  }

  public void collapseView() {
    m_expanded = false;
    m_desktopPane.revalidate();
    m_desktopPane.repaint();
  }

  private class P_FrameListener extends InternalFrameAdapter implements ComponentListener {
    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
      if (e.getInternalFrame().isVisible()) {
        fireComponentResized(new ComponentEvent(e.getInternalFrame(), ComponentEvent.COMPONENT_RESIZED));
      }
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
      removeFrame(e.getInternalFrame());
    }

    @Override
    public void componentResized(ComponentEvent e) {
      if (e.getComponent().isVisible()) {
        fireComponentResized(e);
      }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
      if (e.getComponent().isVisible()) {
        fireComponentResized(e);
      }
    }

    @Override
    public void componentShown(ComponentEvent e) {
      if (e.getComponent().isVisible()) {
        fireComponentResized(e);
      }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

  }
}
