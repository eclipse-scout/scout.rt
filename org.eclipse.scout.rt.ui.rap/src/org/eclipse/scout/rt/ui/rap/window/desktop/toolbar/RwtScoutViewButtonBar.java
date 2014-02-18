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

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.services.common.patchedclass.IPatchedClassService;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>ViewButtonBar</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 */
public class RwtScoutViewButtonBar extends RwtScoutComposite<IDesktop> implements IRwtScoutViewButtonBar<IDesktop> {

  private static final String VARIANT_TOOLBAR_CONTAINER = "toolbarContainer";
  private static final String VARIANT_TOOLBAR_MENU_BUTTON = "menuButton";
  private static final String VARIANT_VIEW_BUTTON_ACTIVE = "viewButton-active";
  private static final String VARIANT_VIEW_BUTTON = "viewButton";

  private HashMap<IViewButton, IRwtScoutToolButton> m_viewTabItems;
  private Control m_buttonBar;

  public RwtScoutViewButtonBar() {
    m_viewTabItems = new HashMap<IViewButton, IRwtScoutToolButton>();
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    if (getScoutObject().getMenus().size() > 0) {
      Control menu = createMainMenu(container);
      if (menu != null) {
        RowData data = new RowData(40, 25);
        menu.setLayoutData(data);
      }
    }
    if (getScoutObject().getViewButtons().size() > 0) {
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

  protected Control createMainMenu(Composite parent) {
    RwtScoutMainMenuButton uiMainMenuButton = new RwtScoutMainMenuButton();
    uiMainMenuButton.createUiField(parent, getScoutObject(), getUiEnvironment());
    return uiMainMenuButton.getUiContainer();
  }

  protected Control createButtons(Composite parent) {
    Composite buttonBar = getUiEnvironment().getFormToolkit().createComposite(parent);
    buttonBar.setData(RWT.CUSTOM_VARIANT, VARIANT_TOOLBAR_CONTAINER);
    for (IViewButton scoutButton : getScoutObject().getViewButtons()) {
      if (scoutButton.isVisible()) {
        IRwtScoutToolButtonForPatch uiButton = SERVICES.getService(IPatchedClassService.class).createRwtScoutToolButton(true, false, VARIANT_VIEW_BUTTON, VARIANT_VIEW_BUTTON_ACTIVE);
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
