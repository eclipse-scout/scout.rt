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
package org.eclipse.scout.rt.ui.rap.mobile.action;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.mobile.MobileScoutFormToolkit;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.util.ScoutFormToolkit;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutActionBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public abstract class AbstractRwtScoutActionBar<T extends IPropertyObserver> extends RwtScoutComposite<T> implements IRwtScoutActionBar<T> {
  private static final String VARIANT_ACTION_BAR_CONTAINER = "actionBarContainer";

  private ActionButtonBar m_leftButtonBar;
  private Composite m_centerTitleBar;
  private ActionButtonBar m_rightButtonBar;

  private boolean m_alwaysVisible = false;
  private Integer m_heightHint;
  private int m_menuOpeningDirection = SWT.DOWN;
  private CLabel m_titleField;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    container.setData(WidgetUtil.CUSTOM_VARIANT, getActionBarContainerVariant());
    setUiContainer(container);

    addLeftButtonBar(container);
    addCenterTitleBar(container);
    addRightButtonBar(container);

    initLayout(container);
    computeContainerVisibility();
  }

  private void computeContainerVisibility() {
    if (m_leftButtonBar == null || m_centerTitleBar == null || m_rightButtonBar == null) {
      getUiContainer().setVisible(false);
      return;
    }

    boolean makeInvisible = !isAlwaysVisible() && !m_leftButtonBar.hasButtons() && !m_rightButtonBar.hasButtons() && getTitle() == null;
    getUiContainer().setVisible(!makeInvisible);
  }

  protected void initLayout(Composite container) {
    GridLayout containerLayout = RwtLayoutUtility.createGridLayoutNoSpacing(3, false);
    container.setLayout(containerLayout);

    {
      GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
      m_leftButtonBar.setLayoutData(gridData);
    }

    {
      GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
      m_centerTitleBar.setLayoutData(gridData);
      excludeCenterTitleBarIfNoTitleSet();
    }

    {
      GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
      m_rightButtonBar.setLayoutData(gridData);
    }
  }

  protected String getActionBarContainerVariant() {
    return VARIANT_ACTION_BAR_CONTAINER;
  }

  protected void addLeftButtonBar(Composite parent) {
    List<IMenu> menuList = new LinkedList<IMenu>();
    collectMenusForLeftButtonBar(menuList);

    m_leftButtonBar = createButtonBar(parent, m_leftButtonBar, menuList, SWT.LEFT | getMenuOpeningDirection());

    adaptLeftButtonBar(m_leftButtonBar);
  }

  protected void adaptLeftButtonBar(ActionButtonBar buttonBar) {
    // Left bar should always display a regular button.
    // Otherwise it could happen that there is only a "..." button on the left side and at the same time some regular buttons on the right side.
    buttonBar.setMinNumberOfAlwaysVisibleButtons(1);
  }

  protected void addCenterTitleBar(Composite parent) {
    m_centerTitleBar = createCenterTitleBar(parent, m_centerTitleBar);

    adaptCenterTitleBar(m_centerTitleBar);
  }

  protected void adaptCenterTitleBar(Composite buttonBar) {
  }

  protected void addRightButtonBar(Composite parent) {
    List<IMenu> menuList = new LinkedList<IMenu>();
    collectMenusForRightButtonBar(menuList);

    m_rightButtonBar = createButtonBar(parent, m_rightButtonBar, menuList, SWT.RIGHT | getMenuOpeningDirection());

    adaptRightButtonBar(m_rightButtonBar);
  }

  protected void adaptRightButtonBar(ActionButtonBar buttonBar) {
  }

  private Composite createCenterTitleBar(Composite parent, Composite existingButtonBar) {
    if (existingButtonBar != null) {
      existingButtonBar.dispose();
      existingButtonBar = null;
    }

    return createTitleBar(parent);
  }

  protected Composite createTitleBar(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    container.setData(WidgetUtil.CUSTOM_VARIANT, getActionBarContainerVariant());
    container.setLayout(new FillLayout());

    m_titleField = getUiEnvironment().getFormToolkit().createCLabel(container, null, SWT.CENTER);
    //FIXME CGU ClabelEx does not automatically expand if screen gets bigger
    m_titleField.setData(WidgetUtil.CUSTOM_VARIANT, getActionBarContainerVariant());

    return container;
  }

  private ActionButtonBar createButtonBar(Composite parent, ActionButtonBar existingButtonBar, List<IMenu> menuList, int style) {
    if (existingButtonBar != null) {
      //Only create a new bar if the actions have changed (to reduce flickering)
      if (existingButtonBar.isEqualMenuList(menuList)) {
        return existingButtonBar;
      }

      existingButtonBar.dispose();
      existingButtonBar = null;
    }

    return createActionButtonBar(parent, menuList, style);
  }

  protected ActionButtonBar createActionButtonBar(Composite parent, List<IMenu> menus, int style) {
    ScoutFormToolkit formToolkit = getUiEnvironment().getFormToolkit();
    //TODO check for MobileScoutFormToolkit can be removed as soon as ActionButtonBar is moved to core plugin
    if (formToolkit instanceof MobileScoutFormToolkit) {
      IMenu[] menuArray = menus.toArray(new IMenu[menus.size()]);
      return ((MobileScoutFormToolkit) formToolkit).createActionButtonBar(parent, menuArray, style);
    }

    return null;
  }

  public void rebuildRightButtonBarFromScout() {
    if (isUiDisposed()) {
      return;
    }
    Runnable job = new Runnable() {
      @Override
      public void run() {
        if (isUiDisposed()) {
          return;
        }

        addRightButtonBar(getUiContainer());
        initLayout(getUiContainer());
        computeContainerVisibility();

        getUiContainer().getParent().layout(true, true);
      }
    };
    getUiEnvironment().invokeUiLater(job);
  }

  protected void setTitle(String title) {
    if (title == null) {
      return;
    }

    if (getTitleField() != null) {
      getTitleField().setText(title);
    }

    excludeCenterTitleBarIfNoTitleSet();
  }

  private void excludeCenterTitleBarIfNoTitleSet() {
    boolean hasTitle = getTitle() != null;

    ((GridData) m_centerTitleBar.getLayoutData()).exclude = !hasTitle;
  }

  public String getTitle() {
    if (getTitleField() == null) {
      return null;
    }

    return getTitleField().getText();
  }

  public boolean isAlwaysVisible() {
    return m_alwaysVisible;
  }

  public void setAlwaysVisible(boolean alwaysVisible) {
    m_alwaysVisible = alwaysVisible;
  }

  public Integer getHeightHint() {
    return m_heightHint;
  }

  public void setHeightHint(Integer heightHint) {
    m_heightHint = heightHint;
  }

  public int getMenuOpeningDirection() {
    return m_menuOpeningDirection;
  }

  public void setMenuOpeningDirection(int menuOpeningDirection) {
    m_menuOpeningDirection = menuOpeningDirection;
  }

  public CLabel getTitleField() {
    return m_titleField;
  }

  protected abstract void collectMenusForLeftButtonBar(List<IMenu> menuList);

  protected abstract void collectMenusForRightButtonBar(List<IMenu> menuList);
}
