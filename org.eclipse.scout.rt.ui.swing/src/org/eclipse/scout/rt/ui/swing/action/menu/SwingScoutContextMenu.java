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
package org.eclipse.scout.rt.ui.swing.action.menu;

import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.menu.IContextMenu;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;

/**
 *
 */
public class SwingScoutContextMenu extends MouseAdapter implements FocusListener, ISwingScoutContextMenu {

  protected final BasicPropertySupport m_propertySupport;
  private final ISwingEnvironment m_environment;
  private final JComponent m_target;
  private Set<JTextComponent> m_copyPasteMenuOwner;
  private IContextMenu m_scoutContextMenu;
  private final IActionFilter m_menuFilter;

  public SwingScoutContextMenu(JComponent target, IContextMenu scoutContextMenu, IActionFilter menuFilter, ISwingEnvironment environment) {
    this(target, scoutContextMenu, menuFilter, environment, true);
  }

  public SwingScoutContextMenu(JComponent target, IContextMenu scoutContextMenu, IActionFilter menuFilter, ISwingEnvironment environment, boolean callInitializer) {
    m_target = target;
    m_scoutContextMenu = scoutContextMenu;
    m_menuFilter = menuFilter;
    m_environment = environment;
    m_propertySupport = new BasicPropertySupport(this);
    m_copyPasteMenuOwner = new HashSet<JTextComponent>();
  }

  public ISwingEnvironment getEnvironment() {
    return m_environment;
  }

  public JComponent getTarget() {
    return m_target;
  }

  public IContextMenu getScoutContextMenu() {
    return m_scoutContextMenu;
  }

  public IActionFilter getMenuFilter() {
    return m_menuFilter;
  }

  @Override
  public void mousePressed(MouseEvent e) {

    if (e.isPopupTrigger()) {
      if (isLocationOnText(e.getPoint())) {
        showSwingPopup(getTarget(), e.getX(), e.getY(), true);
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) {
      if (isLocationOnText(e.getPoint())) {
        showSwingPopup(getTarget(), e.getX(), e.getY(), true);
      }
    }
  }

  private boolean isLocationOnText(Point p) {
    if (getTarget() instanceof JTextComponent) {
      JTextComponent textComp = (JTextComponent) getTarget();
      Insets insets = textComp.getBorder().getBorderInsets(textComp);// textComp.getMargin();
      if (insets == null) {
        return true;
      }
      else {
        return p.x >= insets.left && p.y >= insets.top && p.x <= textComp.getWidth() - insets.right && p.y <= textComp.getHeight() - insets.bottom;
      }
    }
    return true;
  }

  @Override
  public void focusGained(FocusEvent e) {
    getTarget().setComponentPopupMenu(null);
  }

  @Override
  public void focusLost(FocusEvent e) {
    getTarget().setComponentPopupMenu(null);
  }

  public void showSwingPopup(int x, int y, boolean showSystemMenus) {
    showSwingPopup(getTarget(), x, y, showSystemMenus);
  }

  public void showSwingPopup(final JComponent comp, int x, int y, boolean showSystemMenus) {
    JTextComponent systemMenuOwner = null;
    if (m_copyPasteMenuOwner.contains(comp) && showSystemMenus) {
      systemMenuOwner = (JTextComponent) comp;
    }

    comp.requestFocus();
    new SwingPopupWorker(getEnvironment(), getTarget(), systemMenuOwner, new Point(x, y), getScoutContextMenu(),
        getMenuFilter()).run();
  }

  public void addCopyPasteMenuOwner(JTextComponent target) {
    m_copyPasteMenuOwner.add(target);
  }

  public boolean removeCopyPasteMenuOwner(JTextComponent target) {
    return m_copyPasteMenuOwner.remove(target);
  }

  public static SwingScoutContextMenu installContextMenuWithSystemMenus(JTextComponent target, IContextMenu scoutContextMenu, ISwingEnvironment environment) {
    SwingScoutContextMenu contextMenu = installContextMenu(target, scoutContextMenu, environment);
    contextMenu.addCopyPasteMenuOwner(target);
    return contextMenu;
  }

  public static SwingScoutContextMenu installContextMenu(final JComponent target, IContextMenu scoutContextMenu, ISwingEnvironment environment) {
    return installContextMenu(target, scoutContextMenu, ActionUtility.createMenuFilterVisibleAvailable(), environment);

  }

  public static SwingScoutContextMenu installContextMenu(final JComponent target, IContextMenu scoutContextMenu, IActionFilter menuFilter, ISwingEnvironment environment) {
    final SwingScoutContextMenu contextMenu = new SwingScoutContextMenu(target, scoutContextMenu, menuFilter, environment);
    target.addMouseListener(contextMenu);
    target.addFocusListener(contextMenu);
    target.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (KeyEvent.VK_CONTEXT_MENU == e.getKeyCode()) {
          contextMenu.showSwingPopup(target, target.getLocation().x, target.getLocation().y, target.isEnabled()/* && target.isEditable()*/&& target.isShowing());
        }
      }
    });

    return contextMenu;

  }

}
