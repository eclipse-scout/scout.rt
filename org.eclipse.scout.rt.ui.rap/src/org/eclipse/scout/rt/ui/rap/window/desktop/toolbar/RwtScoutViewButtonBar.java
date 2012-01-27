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
package org.eclipse.scout.rt.ui.rap.window.desktop.toolbar;

import java.util.HashMap;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * <h3>ViewButtonBar</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 */
public class RwtScoutViewButtonBar extends RwtScoutComposite<IDesktop> {

  private static final String VARIANT_TOOLBAR_CONTAINER = "toolbarContainer";
  private static final String VARIANT_TOOLBAR_MENU_BUTTON = "menuButton";
  private static final String VARIANT_VIEW_BUTTON_ACTIVE = "viewButton-active";
  private static final String VARIANT_VIEW_BUTTON = "viewButton";

  private HashMap<IViewButton, IRwtScoutToolButton> m_viewTabItems;
  private Control m_buttonBar;
  private Button m_menuButton;

  public RwtScoutViewButtonBar() {
    m_viewTabItems = new HashMap<IViewButton, IRwtScoutToolButton>();
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    if (getScoutObject().getMenus().length > 0) {
      Control menu = createMenu(container);
      RowData data = new RowData(40, 25);
      menu.setLayoutData(data);
    }
    if (getScoutObject().getViewButtons().length > 0) {
      m_buttonBar = createButtons(container);
    }
    //layout
    RowLayout containerLayout = new RowLayout(SWT.HORIZONTAL);
    containerLayout.wrap = false;
    containerLayout.marginTop = 0;
    containerLayout.marginBottom = 0;
    containerLayout.marginLeft = 3;
    containerLayout.marginRight = 0;
    container.setLayout(containerLayout);

    setUiContainer(container);
  }

  protected Control createMenu(Composite parent) {
    m_menuButton = getUiEnvironment().getFormToolkit().createButton(parent, "", SWT.PUSH);
    m_menuButton.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        Rectangle buttonBounds = m_menuButton.getBounds();
        Point buttonLocation = m_menuButton.toDisplay(buttonBounds.x, buttonBounds.y);
        Menu menu = m_menuButton.getMenu();
        menu.setLocation(new Point(buttonLocation.x, buttonLocation.y + buttonBounds.height));
        menu.setVisible(true);
      }
    });
    m_menuButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TOOLBAR_MENU_BUTTON);
    Menu contextMenu = new Menu(m_menuButton.getShell(), SWT.POP_UP);
    RwtMenuUtility.fillContextMenu(getScoutObject().getMenus(), getUiEnvironment(), contextMenu);
    m_menuButton.setMenu(contextMenu);

    return m_menuButton;
  }

  protected Control createButtons(Composite parent) {
    Composite buttonBar = getUiEnvironment().getFormToolkit().createComposite(parent);
    buttonBar.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TOOLBAR_CONTAINER);
    for (IViewButton scoutButton : getScoutObject().getViewButtons()) {
      if (scoutButton.isVisible()) {
        RwtScoutToolButton uiButton = new RwtScoutToolButton(true, false, VARIANT_VIEW_BUTTON, VARIANT_VIEW_BUTTON_ACTIVE);
        uiButton.createUiField(buttonBar, scoutButton, getUiEnvironment());
        m_viewTabItems.put(scoutButton, uiButton);
      }
    }
    RowLayout layout = new RowLayout(SWT.HORIZONTAL);
    layout.marginBottom = 0;
    layout.marginTop = 0;
    layout.wrap = false;

    buttonBar.setLayout(layout);
    return buttonBar;
  }

}
