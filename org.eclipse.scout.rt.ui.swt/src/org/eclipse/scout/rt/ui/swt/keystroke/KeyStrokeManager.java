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
package org.eclipse.scout.rt.ui.swt.keystroke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>KeyStrokeManager</h3> ...
 * 
 * @since 1.0.0 30.04.2008
 */
public class KeyStrokeManager implements IKeyStrokeManager {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(KeyStrokeManager.class);

  private Listener m_keyListener;
  private ArrayList<ISwtKeyStroke> m_globalKeyStrokes;

  private boolean m_globalKeyStrokesActivated;
  private Object m_globalKeyStrokeListLock;
  private final ISwtEnvironment m_environment;

  public KeyStrokeManager(ISwtEnvironment environment) {
    m_environment = environment;
    m_globalKeyStrokeListLock = new Object();
    m_globalKeyStrokes = new ArrayList<ISwtKeyStroke>();
    m_keyListener = new Listener() {
      @Override
      public void handleEvent(Event event) {
        handleKeyEvent(event);
      }
    };
    PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyUp, m_keyListener);
  }

  protected ISwtKeyStroke[] getKeyStrokes(Widget widget) {
    Object data = widget.getData(DATA_KEY_STROKES);
    ISwtKeyStroke[] keyStrokes = null;
    if (data instanceof ISwtKeyStroke[]) {
      keyStrokes = (ISwtKeyStroke[]) data;
    }
    else {
      keyStrokes = new ISwtKeyStroke[0];
    }
    return keyStrokes;
  }

  @Override
  public void addGlobalKeyStroke(ISwtKeyStroke stroke) {
    synchronized (m_globalKeyStrokeListLock) {
      m_globalKeyStrokes.add(stroke);
    }
  }

  @Override
  public boolean removeGlobalKeyStroke(ISwtKeyStroke stroke) {
    synchronized (m_globalKeyStrokeListLock) {
      return m_globalKeyStrokes.remove(stroke);
    }
  }

  protected ISwtKeyStroke[] getGlobalKeyStrokes() {
    synchronized (m_globalKeyStrokeListLock) {
      return m_globalKeyStrokes.toArray(new ISwtKeyStroke[m_globalKeyStrokes.size()]);
    }
  }

  @Override
  public void addKeyStroke(Widget widget, ISwtKeyStroke stroke) {
    ISwtKeyStroke[] keyStrokes = (ISwtKeyStroke[]) widget.getData(DATA_KEY_STROKES);
    if (keyStrokes == null) {
      keyStrokes = new ISwtKeyStroke[0];
    }
    List<ISwtKeyStroke> list = new ArrayList<ISwtKeyStroke>(Arrays.asList(keyStrokes));
    list.add(stroke);
    widget.setData(DATA_KEY_STROKES, list.toArray(new ISwtKeyStroke[list.size()]));
  }

  @Override
  public boolean removeKeyStroke(Widget widget, ISwtKeyStroke stroke) {
    boolean retVal = false;
    ISwtKeyStroke[] keyStrokes = (ISwtKeyStroke[]) widget.getData(DATA_KEY_STROKES);
    if (keyStrokes != null) {
      ArrayList<ISwtKeyStroke> list = new ArrayList<ISwtKeyStroke>(Arrays.asList(keyStrokes));
      retVal = list.remove(stroke);
      widget.setData(DATA_KEY_STROKES, list.toArray(new ISwtKeyStroke[list.size()]));
    }
    return retVal;
  }

  protected ISwtKeyStrokeFilter[] getKeyStrokeFilters(Widget widget) {
    ISwtKeyStrokeFilter[] keyStrokeFilters = (ISwtKeyStrokeFilter[]) widget.getData(DATA_KEY_STROKE_FILTERS);
    if (keyStrokeFilters == null) {
      keyStrokeFilters = new ISwtKeyStrokeFilter[0];
    }
    return keyStrokeFilters;
  }

  @Override
  public void addKeyStrokeFilter(Widget widget, ISwtKeyStrokeFilter stroke) {
    ISwtKeyStrokeFilter[] keyStrokeFilters = (ISwtKeyStrokeFilter[]) widget.getData(DATA_KEY_STROKE_FILTERS);
    if (keyStrokeFilters == null) {
      keyStrokeFilters = new ISwtKeyStrokeFilter[0];
    }
    ArrayList<ISwtKeyStrokeFilter> list = new ArrayList<ISwtKeyStrokeFilter>(Arrays.asList(keyStrokeFilters));
    list.add(stroke);
    widget.setData(DATA_KEY_STROKE_FILTERS, list.toArray(new ISwtKeyStrokeFilter[list.size()]));
  }

  @Override
  public boolean removeKeyStrokeFilter(Widget widget, ISwtKeyStrokeFilter stroke) {
    boolean retVal = false;
    ISwtKeyStrokeFilter[] keyStrokeFilters = (ISwtKeyStrokeFilter[]) widget.getData(DATA_KEY_STROKE_FILTERS);
    if (keyStrokeFilters != null) {
      List<ISwtKeyStrokeFilter> list = Arrays.asList(keyStrokeFilters);
      retVal = list.remove(stroke);
      widget.setData(DATA_KEY_STROKE_FILTERS, list.toArray(new ISwtKeyStrokeFilter[list.size()]));
    }
    return retVal;
  }

  private void handleKeyEvent(Event event) {
    // do not touch the original event
    Event eventCopy = new Event();
    eventCopy.button = event.button;
    eventCopy.character = event.character;
    eventCopy.count = event.count;
    eventCopy.data = event.data;
    eventCopy.detail = event.detail;
    eventCopy.display = event.display;
    eventCopy.doit = event.doit;
    eventCopy.end = event.end;
    eventCopy.gc = event.gc;
    eventCopy.height = event.height;
    eventCopy.index = event.index;
    eventCopy.item = event.item;
    eventCopy.keyCode = event.keyCode;
    eventCopy.start = event.start;
    eventCopy.stateMask = event.stateMask;
    eventCopy.text = event.text;
    eventCopy.time = event.time;
    eventCopy.type = event.type;
    eventCopy.widget = event.widget;
    eventCopy.width = event.width;
    eventCopy.x = event.x;
    eventCopy.y = event.y;
    handleKeyEventHierarchical(eventCopy, event.widget);
    if (eventCopy.doit) {
      // handle global key strokes
      if (isGlobalKeyStrokesActivated()) {
        for (ISwtKeyStroke keyStroke : getGlobalKeyStrokes()) {
          if (keyStroke.getKeyCode() == eventCopy.keyCode && keyStroke.getStateMask() == eventCopy.stateMask) {
            keyStroke.handleSwtAction(eventCopy);
            if (!eventCopy.doit) {
              break;
            }
          }
        }
      }
    }
    event.doit = eventCopy.doit;
  }

  private void handleKeyEventHierarchical(Event event, Widget widget) {
    if (widget == null) {
      return;
    }
    // filter handling
    for (ISwtKeyStrokeFilter filter : getKeyStrokeFilters(widget)) {
      if (!filter.accept(event, m_environment)) {
        return;
      }
    }
    // key stroke handling
    for (ISwtKeyStroke keyStroke : getKeyStrokes(widget)) {
      if (keyStroke.getKeyCode() == event.keyCode && keyStroke.getStateMask() == event.stateMask) {
        keyStroke.handleSwtAction(event);
        if (!event.doit) {
          return;
        }
      }
    }
    if (widget instanceof Control) {
      handleKeyEventHierarchical(event, ((Control) widget).getParent());
    }
  }

  public boolean isGlobalKeyStrokesActivated() {
    return m_globalKeyStrokesActivated;
  }

  public void setGlobalKeyStrokesActivated(boolean globalKeyStrokesActivated) {
    m_globalKeyStrokesActivated = globalKeyStrokesActivated;
  }
}
