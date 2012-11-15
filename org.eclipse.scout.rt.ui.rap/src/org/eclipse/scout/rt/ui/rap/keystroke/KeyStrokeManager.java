/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.keystroke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rwt.RWT;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * <h3>KeyStrokeManager</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class KeyStrokeManager implements IKeyStrokeManager {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(KeyStrokeManager.class);
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  private Listener m_keyListener;
  private KeyAdapter m_keyAdapter;
  private List<IRwtKeyStroke> m_globalKeyStrokes;
  private List<String> m_globalCancelKeyList;
  private Map<Widget, List<String>> m_widgetActiveKeys;
  private Map<Widget, List<String>> m_widgetCancelKeys;

  private P_KeyEventHandler m_keyEventHandler;

  private boolean m_globalKeyStrokesActivated = false;
  private Object m_globalKeyStrokeListLock;
  private final IRwtEnvironment m_environment;

  public KeyStrokeManager(IRwtEnvironment environment) {
    m_environment = environment;
    m_globalKeyStrokeListLock = new Object();
    m_globalKeyStrokes = new ArrayList<IRwtKeyStroke>();
    m_globalCancelKeyList = new ArrayList<String>();
    m_widgetActiveKeys = new HashMap<Widget, List<String>>();
    m_widgetCancelKeys = new HashMap<Widget, List<String>>();
    m_keyEventHandler = new P_KeyEventHandler();
    m_keyAdapter = new KeyAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.keyCode > 0 && e.display != null) {
          m_keyEventHandler.addEvent(e);
          e.display.asyncExec(m_keyEventHandler);
        }
      }
    };
    m_keyListener = new Listener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void handleEvent(final Event e) {
        if (e.keyCode > 0 && e.display != null) {
          m_keyEventHandler.addEvent(e);
          e.display.asyncExec(m_keyEventHandler);
        }
      }
    };
    environment.getDisplay().addFilter(SWT.KeyDown, m_keyListener);
  }

  @Override
  public void addGlobalKeyStroke(IRwtKeyStroke stroke, boolean exclusive) {
    synchronized (m_globalKeyStrokeListLock) {
      m_globalKeyStrokes.add(stroke);
      if (exclusive) {
        if (stroke.isRegisterActiveKey()) {
          m_globalCancelKeyList.add(resolveKeyString(stroke));
        }
      }
      updateGlobalKeys();
    }
  }

  @Override
  public boolean removeGlobalKeyStroke(IRwtKeyStroke stroke) {
    synchronized (m_globalKeyStrokeListLock) {
      boolean retVal = m_globalKeyStrokes.remove(stroke);
      m_globalCancelKeyList.remove(resolveKeyString(stroke));
      updateGlobalKeys();
      return retVal;
    }
  }

  protected List<IRwtKeyStroke> getGlobalKeyStrokes() {
    synchronized (m_globalKeyStrokeListLock) {
      return m_globalKeyStrokes;
    }
  }

  @Override
  public void addKeyStroke(Control control, IRwtKeyStroke stroke, boolean exclusive) {
    @SuppressWarnings("unchecked")
    List<IRwtKeyStroke> keyStrokes = (List<IRwtKeyStroke>) control.getData(DATA_KEY_STROKES);
    if (keyStrokes == null) {
      keyStrokes = new ArrayList<IRwtKeyStroke>();
    }
    keyStrokes.add(stroke);
    control.setData(DATA_KEY_STROKES, keyStrokes);

    List<String> widgetActiveKeys = m_widgetActiveKeys.get(control);
    if (widgetActiveKeys == null) {
      widgetActiveKeys = new ArrayList<String>();
    }
    if (stroke.isRegisterActiveKey()) {
      widgetActiveKeys.add(resolveKeyString(stroke));
    }
    m_widgetActiveKeys.put(control, widgetActiveKeys);

    if (exclusive) {
      List<String> widgetCancelKeys = m_widgetCancelKeys.get(control);
      if (widgetCancelKeys == null) {
        widgetCancelKeys = new ArrayList<String>();
      }
      if (stroke.isRegisterActiveKey()) {
        widgetCancelKeys.add(resolveKeyString(stroke));
      }
      m_widgetCancelKeys.put(control, widgetCancelKeys);
    }
    updateControlKeys(control);
  }

  @Override
  public boolean removeKeyStroke(Control control, IRwtKeyStroke stroke) {
    boolean retVal = false;
    if (control == null
        || control.isDisposed()) {
      m_widgetActiveKeys.remove(control);
      m_widgetCancelKeys.remove(control);
      return retVal;
    }

    @SuppressWarnings("unchecked")
    List<IRwtKeyStroke> keyStrokes = (List<IRwtKeyStroke>) control.getData(DATA_KEY_STROKES);
    if (keyStrokes != null) {
      retVal = keyStrokes.remove(stroke);
      control.setData(DATA_KEY_STROKES, keyStrokes);
    }

    List<String> widgetActiveKeys = m_widgetActiveKeys.get(control);
    if (widgetActiveKeys != null) {
      widgetActiveKeys.remove(resolveKeyString(stroke));
      m_widgetActiveKeys.put(control, widgetActiveKeys);
    }

    List<String> widgetCancelKeys = m_widgetCancelKeys.get(control);
    if (widgetCancelKeys != null) {
      widgetCancelKeys.remove(resolveKeyString(stroke));
      m_widgetCancelKeys.put(control, widgetCancelKeys);
    }
    updateControlKeys(control);

    return retVal;
  }

  @Override
  public boolean removeKeyStrokes(Control control) {
    boolean retVal = false;
    if (control != null && !control.isDisposed()) {
      for (IRwtKeyStroke stroke : new ArrayList<IRwtKeyStroke>(getKeyStrokes(control))) {
        retVal &= removeKeyStroke(control, stroke);
      }
      control.setData(DATA_KEY_STROKES, null);
    }
    m_widgetActiveKeys.remove(control);
    m_widgetCancelKeys.remove(control);
    return retVal;
  }

  @SuppressWarnings("unchecked")
  protected List<IRwtKeyStroke> getKeyStrokes(Widget widget) {
    List<IRwtKeyStroke> keyStrokes = null;
    if (widget != null) {
      Object data = widget.getData(DATA_KEY_STROKES);
      if (data instanceof List && ((List<IRwtKeyStroke>) data).size() > 0) {
        keyStrokes = CollectionUtility.copyList((List<IRwtKeyStroke>) data);
      }
    }
    if (keyStrokes == null) {
      keyStrokes = Collections.emptyList();
    }
    return keyStrokes;
  }

  private void updateGlobalKeys() {
    //active keys
    Set<String> activeKeys = new HashSet<String>(m_globalKeyStrokes.size());
    for (IRwtKeyStroke stroke : m_globalKeyStrokes) {
      if (stroke.isRegisterActiveKey()) {
        String activeKey = resolveKeyString(stroke);
        activeKeys.add(activeKey);
      }
    }
    if (activeKeys.size() == 0) {
      m_environment.getDisplay().setData(RWT.ACTIVE_KEYS, EMPTY_STRING_ARRAY);
    }
    else {
      String[] activeKeyArray = activeKeys.toArray(new String[activeKeys.size()]);
      m_environment.getDisplay().setData(RWT.ACTIVE_KEYS, activeKeyArray);
    }
    //cancel keys
    if (m_globalCancelKeyList == null || m_globalCancelKeyList.size() == 0) {
      m_environment.getDisplay().setData(RWT.CANCEL_KEYS, EMPTY_STRING_ARRAY);
    }
    else {
      String[] cancelKeyArray = m_globalCancelKeyList.toArray(new String[m_globalCancelKeyList.size()]);
      m_environment.getDisplay().setData(RWT.CANCEL_KEYS, cancelKeyArray);
    }
  }

  private void updateControlKeys(Control control) {
    control.removeKeyListener(m_keyAdapter);
    //active keys
    boolean requireListener = false;
    List<String> activeKeys = m_widgetActiveKeys.get(control);
    if (activeKeys == null || activeKeys.size() == 0) {
      control.setData(RWT.ACTIVE_KEYS, EMPTY_STRING_ARRAY);
    }
    else {
      String[] activeKeyArray = activeKeys.toArray(new String[activeKeys.size()]);
      control.setData(RWT.ACTIVE_KEYS, activeKeyArray);
      requireListener = true;
    }
    //cancel keys
    List<String> widgetCancelKeys = m_widgetCancelKeys.get(control);
    if (widgetCancelKeys == null || widgetCancelKeys.size() == 0) {
      control.setData(RWT.CANCEL_KEYS, EMPTY_STRING_ARRAY);
    }
    else {
      String[] cancelKeyArray = widgetCancelKeys.toArray(new String[widgetCancelKeys.size()]);
      control.setData(RWT.CANCEL_KEYS, cancelKeyArray);
      requireListener = true;
    }
    //
    if (requireListener) {
      control.addKeyListener(m_keyAdapter);
    }
  }

  private String resolveKeyString(IRwtKeyStroke stroke) {
    //resolve modifier
    String modifier = "";
    if (stroke.getStateMask() > 0) {
      List<String> modifiers = new ArrayList<String>();
      if ((stroke.getStateMask() & SWT.SHIFT) == SWT.SHIFT) {
        modifiers.add("SHIFT");
      }
      if ((stroke.getStateMask() & SWT.ALT) == SWT.ALT) {
        modifiers.add("ALT");
      }
      if ((stroke.getStateMask() & SWT.CTRL) == SWT.CTRL
          || (stroke.getStateMask() & SWT.COMMAND) == SWT.COMMAND) {
        modifiers.add("CTRL");
      }
      modifier = StringUtility.join("+", CollectionUtility.toArray(modifiers, String.class));
    }

    //resolve key
    String key = RwtUtility.getKeyTextUpper(stroke.getKeyCode());

    //concatenate modifier & key
    String keyString = StringUtility.join("+", modifier, key);
    return keyString;
  }

  public boolean isGlobalKeyStrokesActivated() {
    return m_globalKeyStrokesActivated;
  }

  public void setGlobalKeyStrokesActivated(boolean globalKeyStrokesActivated) {
    m_globalKeyStrokesActivated = globalKeyStrokesActivated;
  }

  private void handleKeyEvent(Event event) {
    if (event.doit && event.display != null) {
      handleKeyEventHierarchical(event, event.display.getFocusControl());
    }
    if (event.doit) {
      // handle global key strokes
      if (isGlobalKeyStrokesActivated()) {
        for (IRwtKeyStroke keyStroke : getGlobalKeyStrokes()) {
          if (keyStroke.getKeyCode() == event.keyCode && keyStroke.getStateMask() == event.stateMask) {
            keyStroke.handleUiAction(event);
            if (!event.doit) {
              break;
            }
          }
        }
      }
    }
  }

  private boolean isWidgetModalDialog(Widget widget) {
    if (widget instanceof Shell) {
      Object data = widget.getData();
      if (data instanceof Dialog) {
        Dialog dialog = (Dialog) data;
        if ((dialog.getShell().getStyle() & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL) {
          return true;
        }
      }
    }
    return false;
  }

  private void handleKeyEventHierarchical(Event event, Widget widget) {
    if (widget == null || widget.isDisposed()) {
      return;
    }
    // key stroke handling
    for (IRwtKeyStroke keyStroke : getKeyStrokes(widget)) {
      if (RwtUtility.getKeyTextUpper(event.keyCode).equals(RwtUtility.getKeyTextUpper(keyStroke.getKeyCode()))
          && keyStroke.getStateMask() == event.stateMask) {
        keyStroke.handleUiAction(event);
        if (!event.doit) {
          return;
        }
      }
    }
    if (isWidgetModalDialog(widget)) {
      event.doit = false;
      return;
    }
    if (widget instanceof Control) {
      handleKeyEventHierarchical(event, ((Control) widget).getParent());
    }
  }

  private class P_KeyEventHandler implements Runnable {
    private Map<CompositeObject, List<Event>> m_eventListMap = null;

    public void addEvent(KeyEvent e) {
      // do not touch the original event
      Event eventCopy = new Event();
      eventCopy.character = e.character;
      eventCopy.data = e.data;
      eventCopy.display = e.display;
      eventCopy.doit = e.doit;
      eventCopy.keyCode = e.keyCode;
      eventCopy.stateMask = e.stateMask;
      eventCopy.widget = e.widget;

      addEventInternal(eventCopy);
    }

    public void addEvent(Event e) {
      // do not touch the original event
      Event eventCopy = new Event();
      eventCopy.button = e.button;
      eventCopy.character = e.character;
      eventCopy.count = e.count;
      eventCopy.data = e.data;
      eventCopy.detail = e.detail;
      eventCopy.display = e.display;
      eventCopy.doit = e.doit;
      eventCopy.end = e.end;
      eventCopy.gc = e.gc;
      eventCopy.height = e.height;
      eventCopy.index = e.index;
      eventCopy.item = e.item;
      eventCopy.keyCode = e.keyCode;
      eventCopy.start = e.start;
      eventCopy.stateMask = e.stateMask;
      eventCopy.text = e.text;
      eventCopy.time = e.time;
      eventCopy.type = e.type;
      eventCopy.widget = e.widget;
      eventCopy.width = e.width;
      eventCopy.x = e.x;
      eventCopy.y = e.y;

      addEventInternal(eventCopy);
    }

    private void addEventInternal(Event e) {
      CompositeObject key = new CompositeObject(e.keyCode, e.stateMask);
      List<Event> eventList = CollectionUtility.getObject(m_eventListMap, key);
      eventList = CollectionUtility.appendList(eventList, e);
      m_eventListMap = CollectionUtility.putObject(m_eventListMap, key, eventList);
    }

    @Override
    public void run() {
      CompositeObject[] keys = CollectionUtility.getKeyArray(m_eventListMap, CompositeObject.class);
      for (CompositeObject key : keys) {
        List<Event> eventList = CollectionUtility.getObject(m_eventListMap, key);

        boolean doit = true;
        Iterator<Event> iterator = eventList.iterator();
        while (iterator.hasNext()) {
          Event event = (Event) iterator.next();
          iterator.remove();
          event.doit = doit;
          KeyStrokeManager.this.handleKeyEvent(event);
          doit = event.doit;
        }
        m_eventListMap = CollectionUtility.putObject(m_eventListMap, key, eventList);
      }
    }
  }
}
