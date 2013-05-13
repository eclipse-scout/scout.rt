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
package org.eclipse.scout.rt.ui.rap.mobile.action;

import java.util.List;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.action.MenuSizeEstimator;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

/**
 * @since 3.9.0
 */
public class RwtScoutActionButton extends RwtScoutComposite<IAction> implements IRwtScoutActionButton {
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;
  private int m_menuOpeningDirection = SWT.DOWN;
  private boolean m_ellipsisRemovalEnabled;

  public RwtScoutActionButton() {
    m_ellipsisRemovalEnabled = true;
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);

    int style = createButtonStyle();
    Button uiButton = getUiEnvironment().getFormToolkit().createButton(container, "", style);
    uiButton.addSelectionListener(new P_RwtSelectionListener());

    initLayout(container, uiButton);

    setUiField(uiButton);
    setUiContainer(container);
  }

  protected void initLayout(Composite container, Button uiButton) {
    container.setLayout(new LogicalGridLayout(0, 0));

    LogicalGridData data = new LogicalGridData();
    data.useUiWidth = true;
    data.useUiHeight = false; // make button as height as logical grid row height
    uiButton.setLayoutData(data);
  }

  protected int createButtonStyle() {
    int style = SWT.CENTER;
    if (hasChildActions()) {
      style |= SWT.TOGGLE;
    }
    else {
      style |= SWT.PUSH;
    }

    return style;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IAction action = getScoutObject();
    setIconIdFromScout(action.getIconId());
    setTextFromScout(action.getText());
    setTooltipTextFromScout(action.getTooltipText());
    setEnabledFromScout(action.isEnabled());
  }

  @Override
  protected void detachScout() {
    super.detachScout();
  }

  @Override
  public Button getUiField() {
    return (Button) super.getUiField();
  }

  protected void setIconIdFromScout(String iconId) {
    if (iconId == null) {
      return;
    }

    Image icon = getUiEnvironment().getIcon(iconId);
    Button button = getUiField();
    button.setImage(icon);
  }

  protected void setTextFromScout(String text) {
    if (text == null) {
      text = "";
    }

    if (isEllipsisRemovalEnabled()) {
      text = removeEllipsis(text);
    }

    Button button = getUiField();
    button.setText(text);
  }

  /**
   * Removes the ellipsis at the end of the text to save space which can be essential on small screens.
   */
  protected String removeEllipsis(String text) {
    if (!StringUtility.hasText(text)) {
      return text;
    }

    if (text.endsWith("...")) {
      text = text.substring(0, text.length() - 3);
    }

    return text;
  }

  protected void setTooltipTextFromScout(String tooltipText) {
    getUiField().setToolTipText(tooltipText);
  }

  protected void setEnabledFromScout(boolean enabled) {
    getUiField().setEnabled(enabled);
  }

  public void setEllipsisRemovalEnabled(boolean ellipsisRemovalEnabled) {
    m_ellipsisRemovalEnabled = ellipsisRemovalEnabled;
  }

  public boolean isEllipsisRemovalEnabled() {
    return m_ellipsisRemovalEnabled;
  }

  protected void handleUiSelection() {
    if (hasChildActions()) {
      handleUiPopupMenu();
    }
    else {
      handleUiAction();
    }
  }

  protected void handleUiAction() {
    if (m_handleActionPending) {
      return;
    }

    m_handleActionPending = true;
    Runnable t = new Runnable() {
      @Override
      public void run() {
        try {
          getScoutObject().getUIFacade().fireActionFromUI();
        }
        finally {
          m_handleActionPending = false;
        }
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
  }

  protected void handleUiPopupMenu() {
    Menu menu = createMenu();
    if (menu == null) {
      return;
    }

    //Toggling the selection should open or close the menu.
    if (!getUiField().getSelection()) {
      return;
    }

    menu.addMenuListener(new MenuAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void menuHidden(MenuEvent e) {
        try {
          //Remove selection if menu hides  (toggle state must reflect the menu state (open or close))
          //Note: Keyboard events (ESC, Space) are not considered at the moment because it's optimized for mobile.
          //If this class should be available for web client in the future keyboard handling should be considered to properly support toggling.
          if (!getUiField().isFocusControl()) {
            getUiField().setSelection(false);
          }
        }
        finally {
          ((Menu) e.getSource()).removeMenuListener(this);
        }
      }
    });

    showMenu(menu);
  }

  private Menu createMenu() {
    if (getUiField().getMenu() != null) {
      getUiField().getMenu().dispose();
      getUiField().setMenu(null);
    }
    Menu contextMenu = new Menu(getUiField().getShell(), SWT.POP_UP);
    contextMenu.addMenuListener(new P_ContextMenuListener());
    getUiField().setMenu(contextMenu);

    return contextMenu;
  }

  private void showMenu(Menu menu) {
    Point menuPosition = null;

    if (getMenuOpeningDirection() == SWT.UP) {
      menuPosition = computeMenuPositionForTop(menu);
    }
    else {
      menuPosition = computeMenuPositionForBottom();
    }

    showMenu(menu, menuPosition);
  }

  private void showMenu(Menu menu, Point location) {
    menu.setLocation(location);
    menu.setVisible(true);
  }

  private Point computeMenuPositionForTop(Menu menu) {
    Rectangle buttonBounds = getUiField().getBounds();
    int menuLocationX = buttonBounds.x;
    int menuLocationY = buttonBounds.y - new MenuSizeEstimator(menu).estimateMenuHeight(getChildActions());
    return getUiField().getParent().toDisplay(menuLocationX, menuLocationY);
  }

  private Point computeMenuPositionForBottom() {
    Rectangle buttonBounds = getUiField().getBounds();
    int menuLocationX = buttonBounds.x;
    int menuLocationY = buttonBounds.y + buttonBounds.height;
    return getUiField().getParent().toDisplay(menuLocationX, menuLocationY);
  }

  public boolean hasChildActions() {
    return RwtMenuUtility.hasChildActions(getScoutObject());
  }

  private List<? extends IActionNode> getChildActions() {
    return RwtMenuUtility.getChildActions(getScoutObject());
  }

  @Override
  public int getMenuOpeningDirection() {
    return m_menuOpeningDirection;
  }

  @Override
  public void setMenuOpeningDirection(int menuOpeningDirection) {
    if (menuOpeningDirection != SWT.TOP) {
      menuOpeningDirection = SWT.DOWN;
    }

    m_menuOpeningDirection = menuOpeningDirection;
  }

  /**
   * in rwt thread
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);

    if (IAction.PROP_ICON_ID.equals(name)) {
      setIconIdFromScout((String) newValue);
    }
    else if (IAction.PROP_TEXT.equals(name)) {
      setTextFromScout((String) newValue);
    }
    else if (IAction.PROP_TOOLTIP_TEXT.equals(name)) {
      setTextFromScout((String) newValue);
    }
    else if (IAction.PROP_ENABLED.equals(name)) {
      setEnabledFromScout(((Boolean) newValue).booleanValue());
    }

  }

  private class P_RwtSelectionListener extends SelectionAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetSelected(SelectionEvent e) {
      handleUiSelection();
    }

  }

  private class P_ContextMenuListener extends MenuAdapter {
    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener() {
    }

    @Override
    public void menuShown(MenuEvent e) {
      List<? extends IActionNode> actions = getChildActions();
      if (actions == null) {
        return;
      }

      Menu menu = ((Menu) e.getSource());
      RwtMenuUtility.fillContextMenu(actions, getUiEnvironment(), menu);
    }

  }

}
