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
package org.eclipse.scout.rt.ui.rap.mobile.form;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.window.desktop.toolbar.RwtScoutToolButtonBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileOutlineFormHeader extends AbstractRwtScoutFormHeader {
  private RwtScoutToolButtonBar m_uiToolButtonBar;

  @Override
  protected void collectMenusForLeftButtonBar(List<IMenu> menuList) {
    IMenu[] desktopMenus = getDesktopMenus();
    if (desktopMenus != null) {
      menuList.addAll(Arrays.asList(desktopMenus));
    }
  }

  @Override
  protected void setTitle(String title) {
    // No title because there is not enough space
  }

  @Override
  protected Composite createRightContainer(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    GridLayout gridLayout = RwtLayoutUtility.createGridLayoutNoSpacing(1, false);
    container.setLayout(gridLayout);

    m_uiToolButtonBar = new RwtScoutMobileToolButtonBar();
    m_uiToolButtonBar.createUiField(container, getDesktop(), getUiEnvironment());

    GridData gridData = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
    m_uiToolButtonBar.getUiContainer().setLayoutData(gridData);

    return container;
  }

  protected IDesktop getDesktop() {
    return getUiEnvironment().getClientSession().getDesktop();
  }

  private IMenu[] getDesktopMenus() {
    return getDesktop().getMenus();
  }

}
