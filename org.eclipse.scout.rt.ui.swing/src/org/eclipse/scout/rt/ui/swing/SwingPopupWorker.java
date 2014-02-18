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
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * a swing runnable that can be enqueued into the awt event queue when run it
 * creates swing menus out of scout menus and shows a popup menu
 */
public class SwingPopupWorker implements Runnable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingPopupWorker.class);

  private ISwingEnvironment m_env;
  private Component m_target;
  private Point m_point;
  private List<? extends IMenu> m_scoutMenus;
  private boolean m_isLightWeightPopup;

  public SwingPopupWorker(ISwingEnvironment env, Component target, Point point, List<? extends IMenu> scoutMenus) {
    this(env, target, point, scoutMenus, true);
  }

  public SwingPopupWorker(ISwingEnvironment env, Component target, Point point, List<? extends IMenu> scoutMenus, boolean isLightWeightPopup) {
    m_env = env;
    m_target = target;
    m_point = point;
    m_scoutMenus = scoutMenus;
    m_isLightWeightPopup = isLightWeightPopup;
  }

  @Override
  public void run() {
    if (!CollectionUtility.hasElements(m_scoutMenus)) {
      return;
    }
    //
    JPopupMenu pop = new JPopupMenu();
    pop.setLightWeightPopupEnabled(m_isLightWeightPopup);
    // recursively add actions
    m_env.appendActions(pop, m_scoutMenus);
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

  public void enqueue() {
    m_env.invokeSwingLater(this);
  }
}
