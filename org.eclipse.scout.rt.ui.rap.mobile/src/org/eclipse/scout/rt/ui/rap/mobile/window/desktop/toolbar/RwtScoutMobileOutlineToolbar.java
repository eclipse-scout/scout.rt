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
package org.eclipse.scout.rt.ui.rap.mobile.window.desktop.toolbar;

import org.eclipse.scout.rt.ui.rap.mobile.action.ActionButtonBar;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.window.desktop.toolbar.RwtScoutToolbar;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileOutlineToolbar extends RwtScoutToolbar {

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);

    Control viewButtonbar = createViewButtonBar(container);
    Control busyIndicator = createBusyIndicator(container);
    Control toolButtonBar = createToolButtonBar(container);

    initLayout(container, viewButtonbar, busyIndicator, toolButtonBar);

    setUiContainer(container);
  }

  @Override
  protected void initLayout(Composite container, Control viewButtonbar, Control busyIndicator, Control toolButtonBar) {
    container.setLayout(RwtLayoutUtility.createGridLayoutNoSpacing(3, false));

    if (viewButtonbar != null) {
      viewButtonbar.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL));
    }

    if (busyIndicator != null) {
      busyIndicator.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
    }

    if (toolButtonBar != null) {
      toolButtonBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER));
    }
  }

  @Override
  public void handleRightViewPositionChanged(int rightViewX) {
    // nothing to do on mobile because there is no view on the right side.
  }

  @Override
  protected Control createViewButtonBar(Composite parent) {
    return new ActionButtonBar(parent, getUiEnvironment(), getScoutObject().getMenus());
  }

}
