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
package org.eclipse.scout.rt.ui.swing.action;

import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.action.menu.text.CopyMenu;
import org.eclipse.scout.rt.ui.swing.action.menu.text.CutMenu;
import org.eclipse.scout.rt.ui.swing.action.menu.text.PasteMenu;

/**
 *
 */
public class SwingScoutMenuSupport extends MouseAdapter implements FocusListener, KeyListener {
  private final JComponent m_comp;
  private final IContextMenu m_contextMenu;
  private final ISwingEnvironment m_env;
  private final IFormField m_scoutObject;
  private final EventListenerList m_listeners;

  protected SwingScoutMenuSupport(JComponent comp, IContextMenu contextMenu, IFormField scoutObject, ISwingEnvironment env) {
    m_comp = comp;
    m_contextMenu = contextMenu;
    m_env = env;
    m_scoutObject = scoutObject;
    m_listeners = new EventListenerList();
  }

  public void addListener(PopupMenuListener l) {
    m_listeners.add(PopupMenuListener.class, l);
  }

  public void removeListener(PopupMenuListener l) {
    m_listeners.add(PopupMenuListener.class, l);
  }

  public static SwingScoutMenuSupport install(JTextComponent comp, IFormField scoutObject, final IContextMenu contextMenu, ISwingEnvironment env, boolean includeSystemMenus) {
    List<IMenu> menus = new ArrayList<IMenu>();
    if (includeSystemMenus) {
      menus.add(new CutMenu(comp));
      menus.add(new CopyMenu(comp));
      menus.add(new PasteMenu(comp));
    }
    return install(comp, scoutObject, contextMenu, env, menus);
  }

  public static SwingScoutMenuSupport install(JComponent comp, IFormField scoutObject, final IContextMenu contextMenu, ISwingEnvironment env) {
    return install(comp, scoutObject, contextMenu, env, new ArrayList<IMenu>());
  }

  private static SwingScoutMenuSupport install(JComponent comp, IFormField scoutObject, final IContextMenu contextMenu, ISwingEnvironment env, List<IMenu> menus) {
    final AtomicReference<List<IMenu>> ref = new AtomicReference<List<IMenu>>(null);
    Runnable t = new Runnable() {
      @Override
      public void run() {
        ref.set(contextMenu.getChildActions());
      }
    };

    JobEx job = env.invokeScoutLater(t, 1000);
    try {
      job.join(1000);
    }
    catch (InterruptedException ex) {
    }

    List<IMenu> list = ref.get();
    if (list != null) {
      List<? extends IMenu> allMenus = MenuUtility.consolidateMenus(list);
      if (allMenus.size() > 0) {
        SwingScoutMenuSupport support = new SwingScoutMenuSupport(comp, contextMenu, scoutObject, env);
        comp.addMouseListener(support);
        comp.addFocusListener(support);
        comp.addKeyListener(support);
        return support;
      }
    }
    return null;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) {
      if (isLocationOnText(e.getPoint())) {
        onSwingPopup(e.getPoint());
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) {
      if (isLocationOnText(e.getPoint())) {
        onSwingPopup(e.getPoint());
      }
    }
  }

  @Override
  public void focusGained(FocusEvent e) {
    m_comp.setComponentPopupMenu(null);
  }

  @Override
  public void focusLost(FocusEvent e) {
    m_comp.setComponentPopupMenu(null);
  }

  private boolean isLocationOnText(Point p) {
    if (m_comp instanceof JTextComponent) {
      JTextComponent jtc = (JTextComponent) m_comp;
      Insets insets = jtc.getMargin();
      if (insets == null) {
        return true;
      }
      else {
        return p.x >= insets.left && p.y >= insets.top && p.x <= jtc.getWidth() - insets.right && p.y <= jtc.getHeight() - insets.bottom;
      }
    }
    return true;
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void keyPressed(KeyEvent e) {
  }

  @Override
  public void keyReleased(KeyEvent e) {
    if (KeyEvent.VK_CONTEXT_MENU == e.getKeyCode()) {
      Point location = m_comp.getLocation();
      onSwingPopup(location);
    }
  }

  private void onSwingPopup(Point point) {
    final long timeout = 1200;
    final AtomicReference<List<? extends IMenu>> scoutMenusRef = new AtomicReference<List<? extends IMenu>>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        scoutMenusRef.set(ActionUtility.visibleNormalizedActions(m_contextMenu.getChildActions()));
      }
    };
    JobEx job = m_env.invokeScoutLater(t, timeout);
    try {
      job.join(timeout);
    }
    catch (InterruptedException ex) {
      //nop
    }

    List<? extends IMenu> list = scoutMenusRef.get();
    if (list != null) {
      SwingPopupWorker worker = new SwingPopupWorker(m_env, m_comp, point, list);
      for (PopupMenuListener l : m_listeners.getListeners(PopupMenuListener.class)) {
        worker.addListener(l);
      }
      worker.run();
    }
  }
}
