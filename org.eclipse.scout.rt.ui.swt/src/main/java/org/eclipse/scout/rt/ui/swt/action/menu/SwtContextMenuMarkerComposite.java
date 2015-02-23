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
package org.eclipse.scout.rt.ui.swt.action.menu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.SwtIcons;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

/**
 *
 */
public class SwtContextMenuMarkerComposite extends Canvas implements ISwtContextMenuMarker {

  private Label m_markerLabel;
  private Point m_markerLabelSize;
//  private Image m_dropDownIcon;
  private List<SelectionListener> m_selectionListeners;
  private ISwtEnvironment m_environment;

  private int m_markerLabelTopMargin = 1;
  private int m_markerLabelLeftMargin = 1;
  private int m_markerLabelRightMargin = 1;
  private int m_markerLabelBottomMargin = 1;

  public SwtContextMenuMarkerComposite(Composite parent, ISwtEnvironment environment) {
    this(parent, environment, SWT.BORDER);
  }

  /**
   *
   */
  public SwtContextMenuMarkerComposite(Composite parent, ISwtEnvironment environment, int style) {
    super(parent, style);
    m_environment = environment;
    m_selectionListeners = new ArrayList<SelectionListener>();
    setLayout(new MarkerCompositeLayout());
    m_markerLabel = environment.getFormToolkit().createLabel(this, "");
    Image dropDownIcon = Activator.getIcon(SwtIcons.DropDownFieldArrowDownDisabled);
    m_markerLabelSize = new Point(dropDownIcon.getBounds().width, dropDownIcon.getBounds().height);
    m_markerLabel.setImage(dropDownIcon);
    m_markerLabel.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseUp(MouseEvent e) {
        Rectangle markerLabelBounds = m_markerLabel.getBounds();
        if (isMarkerVisible() && e.x >= 0 && e.x <= markerLabelBounds.width && e.y >= 0 && e.y <= markerLabelBounds.height) {
          handleDropDownSelection(e);
        }
      }

    });
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  protected Label getMarkerLabel() {
    return m_markerLabel;
  }

  @Override
  public void setMarkerVisible(boolean visible) {
    if (m_markerLabel == null || m_markerLabel.isDisposed()) {
      return;
    }
    m_markerLabel.setVisible(visible);
    layout(true);
  }

  @Override
  public boolean isMarkerVisible() {
    return m_markerLabel.getVisible();
  }

  public int getMarkerLabelTopMargin() {
    return m_markerLabelTopMargin;
  }

  public void setMarkerLabelTopMargin(int markerLabelTopMargin) {
    m_markerLabelTopMargin = markerLabelTopMargin;
  }

  public int getMarkerLabelLeftMargin() {
    return m_markerLabelLeftMargin;
  }

  public void setMarkerLabelLeftMargin(int markerLabelLeftMargin) {
    m_markerLabelLeftMargin = markerLabelLeftMargin;
  }

  public int getMarkerLabelRightMargin() {
    return m_markerLabelRightMargin;
  }

  public void setMarkerLabelRightMargin(int markerLabelRightMargin) {
    m_markerLabelRightMargin = markerLabelRightMargin;
  }

  public int getMarkerLabelBottomMargin() {
    return m_markerLabelBottomMargin;
  }

  public void setMarkerLabelBottomMargin(int markerLabelBottomMargin) {
    m_markerLabelBottomMargin = markerLabelBottomMargin;
  }

  @Override
  public void addSelectionListener(SelectionListener listener) {
    synchronized (m_selectionListeners) {
      m_selectionListeners.add(listener);
    }
  }

  @Override
  public boolean removeSelectionListener(SelectionListener listener) {
    synchronized (m_selectionListeners) {
      return m_selectionListeners.remove(listener);
    }
  }

  private void handleDropDownSelection(MouseEvent e) {
    List<SelectionListener> listeners = null;
    synchronized (m_selectionListeners) {
      listeners = CollectionUtility.arrayList(m_selectionListeners);
    }
    Event event = new Event();
    event.button = e.button;
    event.count = e.count;
    event.data = e.data;
    event.doit = true;
    event.display = e.display;
    event.stateMask = e.stateMask;
    event.time = e.time;
    event.widget = e.widget;
    event.x = e.x;
    event.y = e.y;
    SelectionEvent selectionEvent = new SelectionEvent(event);
    for (SelectionListener l : listeners) {
      l.widgetSelected(selectionEvent);
      if (!selectionEvent.doit) {
        break;
      }
    }
  }

  private class MarkerCompositeLayout extends Layout {

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
      Control[] children = composite.getChildren();
      if (children.length != 2) {
        throw new IllegalArgumentException("Marker composite only allows exacly one child control!");
      }
      int width = 0;
      int height = 0;
      if (isMarkerVisible()) {
        // markerlabel
        height = getMarkerLabelTopMargin() + getMarkerLabelBottomMargin() + m_markerLabelSize.y;
        width = getMarkerLabelLeftMargin() + getMarkerLabelRightMargin() + m_markerLabelSize.x;
      }
      for (Control c : children) {
        if (c != m_markerLabel) {
          if (wHint == SWT.DEFAULT) {
            Point computedSize = c.computeSize(wHint, hHint);
            width += computedSize.x;
            height = Math.max(height, computedSize.y);
          }
          else if (hHint == SWT.DEFAULT) {
            Point computedSize = c.computeSize(Math.max(-1, wHint - width), hHint);
            width += computedSize.x;
            height = Math.max(height, computedSize.y);
          }
          break;
        }
      }
      if (wHint != SWT.DEFAULT) {
        width = wHint;
      }
      if (hHint != SWT.DEFAULT) {
        height = hHint;
      }
      return new Point(width, height);
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
      Control[] children = composite.getChildren();
      if (children.length != 2) {
        throw new IllegalArgumentException("Marker composite only allows exacly one child control!");
      }
      Rectangle clientArea = composite.getClientArea();
      Rectangle controlBounds = new Rectangle(clientArea.x, clientArea.y, clientArea.width, clientArea.height);
      if (isMarkerVisible()) {
        controlBounds.width = controlBounds.width - m_markerLabelSize.x - getMarkerLabelLeftMargin() - getMarkerLabelRightMargin();
        Rectangle markerIconBounds = new Rectangle(clientArea.width - m_markerLabelSize.x - getMarkerLabelRightMargin(), getMarkerLabelTopMargin(), m_markerLabelSize.x, m_markerLabelSize.y);
        m_markerLabel.setBounds(markerIconBounds);
      }
      for (Control c : children) {
        if (c != m_markerLabel) {
          c.setBounds(controlBounds);
          break;
        }
      }
    }
  }
}
