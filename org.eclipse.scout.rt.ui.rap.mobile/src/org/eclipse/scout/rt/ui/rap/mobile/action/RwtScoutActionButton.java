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

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonListener;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.rap.mobile.form.fields.button.RwtScoutMobileButton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @since 3.8.0
 */
public class RwtScoutActionButton extends RwtScoutComposite<IAction> implements IRwtScoutActionButton {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutActionButton.class);
  public static final int BUTTON_HEIGHT = RwtScoutMobileButton.BUTTON_HEIGHT;

  private ButtonListener m_scoutButtonListener;
  private OptimisticLock m_selectionLock;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;
  private boolean m_selectionAlreadyRemoved;
  private int m_menuOpeningDirection = SWT.DOWN;
  private Menu m_contextMenu;
  private boolean m_ellipsisRemovalEnabled;

  public RwtScoutActionButton() {
    m_selectionLock = new OptimisticLock();
    m_ellipsisRemovalEnabled = true;
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);

    int style = createButtonStyle();
    Button uiButton = getUiEnvironment().getFormToolkit().createButton(container, "", style);

    uiButton.addSelectionListener(new P_RwtSelectionListener());
    if ((style & SWT.TOGGLE) != 0) {
      uiButton.addMouseListener(new MouseAdapter() {
        private static final long serialVersionUID = 1L;

        @Override
        public void mouseDown(MouseEvent e) {
          m_selectionAlreadyRemoved = false;
        }
      });
    }

    m_contextMenu = new Menu(uiButton.getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener());
    uiButton.setMenu(m_contextMenu);

    initLayout(container, uiButton);

    setUiField(uiButton);
    setUiContainer(container);
  }

  protected void initLayout(Composite container, Button uiButton) {
    container.setLayout(new LogicalGridLayout(0, 0));

    LogicalGridData data = new LogicalGridData();
    data.useUiHeight = true;
    data.useUiWidth = true;
    data.heightHint = BUTTON_HEIGHT;
    data.fillHorizontal = true;
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
    Menu menu = getUiField().getMenu();
    if (menu == null) {
      return;
    }

    if (m_selectionAlreadyRemoved) {
      getUiField().setSelection(false);
      m_selectionAlreadyRemoved = false;
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
          //Remove selection if menu hides (toggle state must reflect the menu state (open or close))
          getUiField().setSelection(false);
          m_selectionAlreadyRemoved = true;
        }
        finally {
          ((Menu) e.getSource()).removeMenuListener(this);
        }
      }
    });

    showMenu(menu);
  }

  private void showMenu(Menu menu) {
    Point menuPosition = null;

    if (getMenuOpeningDirection() == SWT.UP) {
      menuPosition = computeMenuPositionForTop();
    }
    else {
      menuPosition = computeMenuPositionForBottom();
    }

    menu.setLocation(menuPosition);
    menu.setVisible(true);
  }

  private Point computeMenuPositionForTop() {
    Rectangle buttonBounds = getUiField().getBounds();
    int menuLocationX = buttonBounds.x;
    int menuLocationY = buttonBounds.y - estimateMenuHeight();
    return getUiField().getParent().toDisplay(menuLocationX, menuLocationY);
  }

  private Point computeMenuPositionForBottom() {
    Rectangle buttonBounds = getUiField().getBounds();
    int menuLocationX = buttonBounds.x;
    int menuLocationY = buttonBounds.y + buttonBounds.height;
    return getUiField().getParent().toDisplay(menuLocationX, menuLocationY);
  }

  /**
   * Estimates the menu height based on the actions to be displayed. If the font or padding properties changes
   * (scout.css) it breaks.
   */
  private int estimateMenuHeight() {
    List<? extends IActionNode> actions = getChildActions();
    if (actions == null || actions.size() == 0) {
      return 0;
    }

    int height = 0;
    int itemNum = 0;
    for (IActionNode<?> actionNode : actions) {
      if (actionNode.isSeparator()) {
        if (itemNum != 0 && itemNum != actions.size() - 1 && !actions.get(itemNum - 1).isSeparator()) {
          height += 4 + 7; // separator padding and height
        }
      }
      else {
        height += 14 + 15; // menu item padding and height
      }
      itemNum++;
    }
    if (height > 0) {
      height += 8 + 4; // menu padding and border width
    }

    return height;
  }

  public boolean hasChildActions() {
    if (!(getScoutObject() instanceof IActionNode<?>)) {
      return false;
    }

    IActionNode<? extends IActionNode> actionNode = (IActionNode<?>) getScoutObject();
    return actionNode.hasChildActions();
  }

  private List<? extends IActionNode> getChildActions() {
    if (!(getScoutObject() instanceof IActionNode<?>)) {
      return null;
    }

    IActionNode<? extends IActionNode> actionNode = (IActionNode<?>) getScoutObject();
    if (!actionNode.hasChildActions()) {
      return null;
    }

    return actionNode.getChildActions();
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

  private class P_ContextMenuListener extends MenuAdapterEx {
    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener() {
      super(getUiField(), getUiField());
    }

    @Override
    protected Menu getContextMenu() {
      return m_contextMenu;
    }

    @Override
    protected void setContextMenu(Menu contextMenu) {
      m_contextMenu = contextMenu;
    }

    @Override
    public void menuShown(MenuEvent e) {
      if (m_contextMenu != null) {
        for (MenuItem item : m_contextMenu.getItems()) {
          disposeMenuItem(item);
        }
      }

      List<? extends IActionNode> actions = getChildActions();
      if (actions == null) {
        return;
      }

      RwtMenuUtility.fillContextMenu(actions, getUiEnvironment(), m_contextMenu);
    }

  }

}
