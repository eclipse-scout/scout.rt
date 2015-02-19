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
import java.util.Set;

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
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;

/**
 * a swing runnable that can be enqueued into the awt event queue when run it
 * creates swing menus out of scout menus and shows a popup menu
 */
public class SwingPopupWorker implements Runnable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingPopupWorker.class);

  private final EventListenerList m_listeners;

  private final ISwingEnvironment m_environment;
  private final Component m_target;
  private final Point m_location;
  private final IContextMenu m_contextMenu;
  private final List<? extends IMenu> m_scoutMenus;
  private final Set<? extends IMenuType> m_menuTypes;

  private JTextComponent m_systemMenuOwner;
  private boolean m_isLightWeightPopup;

  public SwingPopupWorker(ISwingEnvironment environment, Component target, Point location, IContextMenu contextMenu) {
    this(environment, target, location, contextMenu, null);
  }

  public SwingPopupWorker(ISwingEnvironment environment, Component target, Point location, IContextMenu contextMenu, Set<? extends IMenuType> menuTypes) {
    this(environment, target, location, contextMenu, contextMenu.getChildActions(), menuTypes);
  }

  public SwingPopupWorker(ISwingEnvironment environment, Component target, Point location, List<IMenu> scoutMenus) {
    this(environment, target, location, scoutMenus, null);
  }

  public SwingPopupWorker(ISwingEnvironment environment, Component target, Point location, List<IMenu> scoutMenus, Set<? extends IMenuType> menuTypes) {
    this(environment, target, location, null, scoutMenus, menuTypes);
  }

  private SwingPopupWorker(ISwingEnvironment environment, Component target, Point location, IContextMenu contextMenu, List<IMenu> scoutMenus, Set<? extends IMenuType> menuTypes) {
    m_environment = environment;
    m_target = target;
    m_location = location;
    m_contextMenu = contextMenu;
    m_scoutMenus = scoutMenus;
    m_menuTypes = menuTypes;

    m_listeners = new EventListenerList();
  }

  public ISwingEnvironment getEnvironment() {
    return m_environment;
  }

  public Component getTarget() {
    return m_target;
  }

  public Point getLocation() {
    return m_location;
  }

  public IContextMenu getContextMenu() {
    return m_contextMenu;
  }

  public List<? extends IMenu> getScoutMenus() {
    return m_scoutMenus;
  }

  public Set<? extends IMenuType> getMenuTypes() {
    return m_menuTypes;
  }

  public void setSystemMenuOwner(JTextComponent systemMenuOwner) {
    m_systemMenuOwner = systemMenuOwner;
  }

  public JTextComponent getSystemMenuOwner() {
    return m_systemMenuOwner;
  }

  public boolean isLightWeightPopup() {
    return m_isLightWeightPopup;
  }

  public void setLightWeightPopup(boolean isLightWeightPopup) {
    m_isLightWeightPopup = isLightWeightPopup;
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
        final IActionFilter aboutToShowFilter;
        if (getMenuTypes() == null) {
          aboutToShowFilter = ActionUtility.TRUE_FILTER;
        }
        else {
          aboutToShowFilter = ActionUtility.createMenuFilterMenuTypes(getMenuTypes(), false);
        }
        if (getContextMenu() != null) {
          getContextMenu().callAboutToShow(aboutToShowFilter);
        }
        else {
          for (IMenu m : m_scoutMenus) {
            m.acceptVisitor(new IActionVisitor() {
              @Override
              public int visit(IAction action) {
                if (action instanceof IMenu && aboutToShowFilter.accept(action)) {
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
    JobEx prepareJob = m_environment.invokeScoutLater(t, 0);
    try {
      prepareJob.join(1200);
    }
    catch (InterruptedException e) {
      LOG.error("error during prepare menus.", e);
    }

    final IActionFilter displayFilter;
    if (getMenuTypes() == null) {
      displayFilter = ActionUtility.createVisibleFilter();
    }
    else {
      displayFilter = ActionUtility.createMenuFilterMenuTypes(getMenuTypes(), true);
    }

    List<? extends IMenu> normalizedMenus = ActionUtility.normalizedActions(getScoutMenus(), displayFilter);
    if (!CollectionUtility.hasElements(normalizedMenus) && getSystemMenuOwner() == null) {
      return;
    }
    //
    JPopupMenu pop = new JPopupMenu();
    for (PopupMenuListener l : m_listeners.getListeners(PopupMenuListener.class)) {
      pop.addPopupMenuListener(l);
    }
    pop.setLightWeightPopupEnabled(isLightWeightPopup());
    for (JMenuItem item : getSystemMenus()) {
      pop.add(item);
    }
    if (pop.getComponentCount() > 0 && CollectionUtility.hasElements(normalizedMenus)) {
      pop.addSeparator();
    }
    // recursively add actions
    getEnvironment().appendActions(pop, normalizedMenus, displayFilter);
    try {
      if (pop.getComponentCount() > 0) {
        Component target = getTarget();
        Point whereOnTarget = getLocation();
        // adjust, if outside screen
        if (!target.isVisible()) {
          Component visibleAncestor = target;
          while (visibleAncestor != null && (!visibleAncestor.isVisible())) {
            visibleAncestor = visibleAncestor.getParent();
          }
          whereOnTarget = SwingUtilities.convertPoint(target, whereOnTarget, visibleAncestor);
          target = visibleAncestor;
        }
        Point compLocationOnScreen = target.getLocationOnScreen();
        Point p = new Point(whereOnTarget);
        p.translate(compLocationOnScreen.x, compLocationOnScreen.y);
        Rectangle r = new Rectangle(p, pop.getPreferredSize());

        // <bsh 2010-11-22>
        // Always make sure, that the menu appears on the same screen than the component it belongs to.
        Rectangle ownerBounds = new Rectangle(
            target.getLocationOnScreen().x,
            target.getLocationOnScreen().y,
            target.getWidth(),
            target.getHeight()
            );
        if (target.getParent() instanceof JViewport && target.getParent().getParent() instanceof JScrollPane) {
          Container scrollpane = target.getParent().getParent();
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
        pop.show(target, p.x, p.y);
      }
    }
    catch (Exception e) {
      LOG.error(null, e);
    }
  }

  private List<JMenuItem> getSystemMenus() {
    List<JMenuItem> items = new ArrayList<JMenuItem>();
    if (getSystemMenuOwner() != null) {
      if (getSystemMenuOwner().isEditable()) {
        JMenuItem cutItem = new JMenuItem(SwingUtility.getNlsText("Cut"));
        cutItem.setEnabled(StringUtility.hasText(getSystemMenuOwner().getSelectedText()));
        cutItem.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            getSystemMenuOwner().cut();
          }
        });
        items.add(cutItem);
      }

      JMenuItem copyItem = new JMenuItem(SwingUtility.getNlsText("Copy"));
      if (getSystemMenuOwner().isEnabled() && getSystemMenuOwner().isEditable()) {
        copyItem.setEnabled(StringUtility.hasText(getSystemMenuOwner().getSelectedText()));
      }
      copyItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
          if (getSystemMenuOwner().isEnabled() && getSystemMenuOwner().isEditable()) {
            getSystemMenuOwner().copy();
          }
          else {
            //Ticket 86'427: Kopieren - Einfügen
            boolean hasSelection = StringUtility.hasText(getSystemMenuOwner().getSelectedText());
            if (hasSelection) {
              getSystemMenuOwner().copy();
            }
            else {
              getSystemMenuOwner().selectAll();
              getSystemMenuOwner().copy();
              getSystemMenuOwner().select(0, 0);
            }
          }
        }
      });
      items.add(copyItem);

      if (getSystemMenuOwner().isEditable()) {
        JMenuItem pasteItem = new JMenuItem(SwingUtility.getNlsText("Paste"));
        pasteItem.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            getSystemMenuOwner().paste();
          }
        });
        items.add(pasteItem);
      }
    }
    return items;
  }

  public void enqueue() {
    m_environment.invokeSwingLater(this);
  }
}
