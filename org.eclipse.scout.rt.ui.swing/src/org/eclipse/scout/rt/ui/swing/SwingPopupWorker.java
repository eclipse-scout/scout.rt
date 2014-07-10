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
package org.eclipse.scout.rt.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;

/**
 * a swing runnable that can be enqueued into the awt event queue when run it
 * creates swing menus out of scout menus and shows a popup menu
 */
public class SwingPopupWorker implements Runnable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingPopupWorker.class);

  private final ISwingEnvironment m_env;
  private final Point m_point;

  private IContextMenu m_contextMenu;
  private final List<? extends IMenu> m_scoutMenus;
  private boolean m_isLightWeightPopup;
  private final EventListenerList m_listeners;

  private Component m_target;
  private final JTextComponent m_systemMenuOwner;

  private final IActionFilter m_actionFilter;

  public SwingPopupWorker(ISwingEnvironment env, Component target, Point point, IContextMenu contextMenu) {
    this(env, target, point, contextMenu.getChildActions());
    m_contextMenu = contextMenu;
  }

  public SwingPopupWorker(ISwingEnvironment env, Component target, Point point, List<IMenu> scoutMenus) {
    this(env, target, null, point, scoutMenus, ActionUtility.TRUE_FILTER);
  }

  public SwingPopupWorker(ISwingEnvironment env, Component target, Point point, IContextMenu contextMenu, IActionFilter actionFilter) {
    this(env, target, null, point, contextMenu.getChildActions(), actionFilter);
    m_contextMenu = contextMenu;
  }

  public SwingPopupWorker(ISwingEnvironment env, Component target, JTextComponent systemMenuOwner, Point point, IContextMenu contextMenu, IActionFilter actionFilter) {
    this(env, target, systemMenuOwner, point, contextMenu.getChildActions(), actionFilter);
    m_contextMenu = contextMenu;
  }

  protected SwingPopupWorker(ISwingEnvironment env, Component target, JTextComponent systemMenuOwner, Point point, List<? extends IMenu> scoutMenus, IActionFilter actionFilter) {
    this(env, target, systemMenuOwner, point, scoutMenus, actionFilter, true);
  }

  public SwingPopupWorker(ISwingEnvironment env, Component target, JTextComponent systemMenuOwner, Point point, IContextMenu contextMenu, boolean isLightWeightPopup) {
    this(env, target, systemMenuOwner, point, contextMenu.getChildActions(), isLightWeightPopup);
    m_contextMenu = contextMenu;
  }

  public SwingPopupWorker(ISwingEnvironment env, Component target, JTextComponent systemMenuOwner, Point point, List<? extends IMenu> scoutMenus, boolean isLightWeightPopup) {
    this(env, target, systemMenuOwner, point, scoutMenus, ActionUtility.TRUE_FILTER, isLightWeightPopup);
  }

  public SwingPopupWorker(ISwingEnvironment env, Component target, JTextComponent systemMenuOwner, Point point, IContextMenu contextMenu, IActionFilter actionFilter, boolean isLightWeightPopup) {
    this(env, target, systemMenuOwner, point, contextMenu.getChildActions(), actionFilter, isLightWeightPopup);
    m_contextMenu = contextMenu;
  }

  protected SwingPopupWorker(ISwingEnvironment env, Component target, JTextComponent systemMenuOwner, Point point, List<? extends IMenu> scoutMenus, IActionFilter actionFilter, boolean isLightWeightPopup) {
    m_env = env;
    m_target = target;
    m_systemMenuOwner = systemMenuOwner;
    m_point = point;
    m_scoutMenus = scoutMenus;
    m_actionFilter = actionFilter;
    m_isLightWeightPopup = isLightWeightPopup;
    m_listeners = new EventListenerList();
  }

  public void addListener(PopupMenuListener l) {
    m_listeners.add(PopupMenuListener.class, l);
  }

  public void removeListener(PopupMenuListener l) {
    m_listeners.add(PopupMenuListener.class, l);
  }

  @Override
  public void run() {
    // about to show
    Runnable t = new Runnable() {
      @SuppressWarnings("deprecation")
      @Override
      public void run() {
        if (m_contextMenu != null) {
          m_contextMenu.callAboutToShow(m_actionFilter);
        }
        else {
          for (IMenu m : m_scoutMenus) {
            m.acceptVisitor(new IActionVisitor() {
              @Override
              public int visit(IAction action) {
                if (action instanceof IMenu) {
                  ((IMenu) action).aboutToShow();
                  ((IMenu) action).prepareAction();
                }
                return CONTINUE;
              }
            });
          }
        }
      }
    };
    JobEx prepareJob = m_env.invokeScoutLater(t, 0);
    try {
      prepareJob.join(1200);
    }
    catch (InterruptedException e) {
      LOG.error("error during prepare menus.", e);
    }
    IActionFilter displayFilter = ActionUtility.createCombinedFilter(ActionUtility.createVisibleFilter(), m_actionFilter);

    List<? extends IMenu> normalizedMenus = ActionUtility.normalizedActions(m_scoutMenus, displayFilter);
    if (!CollectionUtility.hasElements(normalizedMenus) && m_systemMenuOwner == null) {
      return;
    }
    //
    JPopupMenu pop = new JPopupMenu();
    for (PopupMenuListener l : m_listeners.getListeners(PopupMenuListener.class)) {
      pop.addPopupMenuListener(l);
    }
    pop.setLightWeightPopupEnabled(m_isLightWeightPopup);
    for (JMenuItem item : getSystemMenus()) {
      pop.add(item);
    }
    if (pop.getComponentCount() > 0 && CollectionUtility.hasElements(normalizedMenus)) {
      pop.addSeparator();
    }
    // recursively add actions
    m_env.appendActions(pop, normalizedMenus, displayFilter);
    try {
      if (pop.getComponentCount() > 0) {
        Point whereOnTarget = m_point;
        // adjust, if outside screen
        if (!m_target.isVisible()) {
          Component visibleAncestor = m_target;
          while (visibleAncestor != null && (!visibleAncestor.isVisible())) {
            visibleAncestor = visibleAncestor.getParent();
          }
          whereOnTarget = SwingUtilities.convertPoint(m_target, whereOnTarget, visibleAncestor);
          m_target = visibleAncestor;
        }
        Point compLocationOnScreen = m_target.getLocationOnScreen();
        Point p = new Point(whereOnTarget);
        p.translate(compLocationOnScreen.x, compLocationOnScreen.y);
        Rectangle r = new Rectangle(p, pop.getPreferredSize());

        // <bsh 2010-11-22>
        // Always make sure, that the menu appears on the same screen than the component it belongs to.
        Rectangle ownerBounds = new Rectangle(
            m_target.getLocationOnScreen().x,
            m_target.getLocationOnScreen().y,
            m_target.getWidth(),
            m_target.getHeight()
            );
        if (m_target.getParent() instanceof JViewport && m_target.getParent().getParent() instanceof JScrollPane) {
          Container scrollpane = m_target.getParent().getParent();
          ownerBounds = new Rectangle(
              scrollpane.getLocationOnScreen().x,
              scrollpane.getLocationOnScreen().y,
              scrollpane.getWidth(),
              scrollpane.getHeight()
              );
        }
        Rectangle ownerScreen = SwingUtility.getFullScreenBoundsFor(ownerBounds, true);
        r = SwingUtility.validateRectangleOnScreen(r, ownerScreen, true, true);
        // Check if the menu hides the component. If so, kindly try to move the menu away.
        // Do _not_ do this when the component is larger than the menu (e.g. for context menus).
        if (r.y < ownerBounds.y && r.height > ownerBounds.height) {
          Rectangle tmp;
          Rectangle result;
          // Try to move the menu _below_ the component
          tmp = new Rectangle(r);
          tmp.y = ownerBounds.y + ownerBounds.height + 2;
          result = SwingUtility.validateRectangleOnScreen(tmp, ownerScreen, true, true);
          if (result.equals(tmp)) {
            r = tmp;
          }
          else {
            // Try to move the menu _above_ the component
            tmp = new Rectangle(r);
            tmp.y = ownerBounds.y - pop.getPreferredSize().height - 2;
            result = SwingUtility.validateRectangleOnScreen(tmp, ownerScreen, true, true);
            if (result.equals(tmp)) {
              r = tmp;
            }
            else {
              // Give up
            }
          }
        }
        // </bsh>

        p = r.getLocation();
        p.translate(-compLocationOnScreen.x, -compLocationOnScreen.y);
        pop.show(m_target, p.x, p.y);
      }
    }
    catch (Exception e) {
      LOG.error(null, e);
    }
  }

  private List<JMenuItem> getSystemMenus() {
    List<JMenuItem> items = new ArrayList<JMenuItem>();
    if (m_systemMenuOwner != null) {
      if (m_systemMenuOwner.isEditable()) {
        JMenuItem cutItem = new JMenuItem(SwingUtility.getNlsText("Cut"));
        cutItem.setEnabled(StringUtility.hasText(m_systemMenuOwner.getSelectedText()));
        cutItem.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            m_systemMenuOwner.cut();
          }
        });
        items.add(cutItem);
      }

      JMenuItem copyItem = new JMenuItem(SwingUtility.getNlsText("Copy"));
      if (m_systemMenuOwner.isEnabled() && m_systemMenuOwner.isEditable()) {
        copyItem.setEnabled(StringUtility.hasText(m_systemMenuOwner.getSelectedText()));
      }
      copyItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
          if (m_systemMenuOwner.isEnabled() && m_systemMenuOwner.isEditable()) {
            m_systemMenuOwner.copy();
          }
          else {
            //Ticket 86'427: Kopieren - Einfügen
            boolean hasSelection = StringUtility.hasText(m_systemMenuOwner.getSelectedText());
            if (hasSelection) {
              m_systemMenuOwner.copy();
            }
            else {
              m_systemMenuOwner.selectAll();
              m_systemMenuOwner.copy();
              m_systemMenuOwner.select(0, 0);
            }
          }
        }
      });
      items.add(copyItem);

      if (m_systemMenuOwner.isEditable()) {
        JMenuItem pasteItem = new JMenuItem(SwingUtility.getNlsText("Paste"));
        pasteItem.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            m_systemMenuOwner.paste();
          }
        });
        items.add(pasteItem);
      }
    }
    return items;
  }

  public void enqueue() {
    m_env.invokeSwingLater(this);
  }
}
