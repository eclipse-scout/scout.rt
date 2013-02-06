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
package org.eclipse.scout.rt.ui.rap.action;

import java.util.List;

import org.eclipse.rwt.internal.theme.AbstractThemeAdapter;
import org.eclipse.rwt.internal.theme.WidgetMatcher;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.form.fields.button.SeparatorDummyWidget;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

/**
 * Since it's not possible to get the size of a menu, it's necessary to estimate it. There is no guarantee at all that
 * the estimation is correct.
 * <p>
 * The estimation is based on the actions to be displayed and some css properties like padding and border.
 */
@SuppressWarnings("restriction")
public class MenuSizeEstimator {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(MenuSizeEstimator.class);

  private Rectangle m_menuPadding;
  private Rectangle m_menuItemPadding;
  private int m_menuBorderWidth;
  private Rectangle m_menuItemSeparatorPadding;

  public MenuSizeEstimator(Menu menu) {
    SeparatorDummyWidget separatorDummyWidget = null;
    try {
      MenuThemeAdapter menuThemeAdapter = new MenuThemeAdapter();
      m_menuPadding = menuThemeAdapter.getPadding("Menu", menu);
      m_menuBorderWidth = menuThemeAdapter.getBorderWidth("Menu", menu);
      m_menuItemPadding = menuThemeAdapter.getPadding("MenuItem", menu);

      separatorDummyWidget = new SeparatorDummyWidget(menu);
      m_menuItemSeparatorPadding = menuThemeAdapter.getPadding("MenuItem", separatorDummyWidget);
    }
    catch (Throwable t) {
      LOG.error("Css properties for menu size estimation could not be loaded", t);

      m_menuPadding = new Rectangle(0, 0, 0, 0);
      m_menuItemPadding = new Rectangle(0, 0, 0, 0);
      m_menuItemSeparatorPadding = new Rectangle(0, 0, 0, 0);
    }
    finally {
      if (separatorDummyWidget != null) {
        separatorDummyWidget.dispose();
      }
    }
  }

  public int estimateMenuHeight(List<? extends IActionNode> actions) {
    actions = RwtMenuUtility.cleanup(actions);
    if (actions == null || actions.size() == 0) {
      return 0;
    }

    int height = 0;
    int itemNum = 0;
    for (IActionNode<?> actionNode : actions) {
      if (actionNode.isSeparator()) {
        if (itemNum != 0 && itemNum != actions.size() - 1 && !actions.get(itemNum - 1).isSeparator()) {
          height += m_menuItemSeparatorPadding.height + 7; // separator padding and height
        }
      }
      else {
        height += m_menuItemPadding.height + 15; // menu item padding and height
      }
      itemNum++;
    }
    if (height > 0) {
      height += m_menuPadding.height + m_menuBorderWidth * 2 + 2; // menu padding and border width
    }

    return height;
  }

  private class MenuThemeAdapter extends AbstractThemeAdapter {

    public Rectangle getPadding(String cssElement, Widget widget) {
      return getCssBoxDimensions(cssElement, "padding", widget);
    }

    public int getBorderWidth(String cssElement, Widget widget) {
      return getCssBorderWidth(cssElement, "border", widget);
    }

    @Override
    protected void configureMatcher(WidgetMatcher matcher) {
      matcher.addStyle("SEPARATOR", SeparatorDummyWidget.STYLE_SEPARATOR);
    }
  }
}
