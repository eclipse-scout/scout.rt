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
package org.eclipse.scout.rt.ui.rap.mobile.window.desktop;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.rap.mobile.window.desktop.toolbar.RwtScoutMobileToolbarContainer;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutToolbar;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutDesktop;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileDesktop extends RwtScoutDesktop {
  private IRwtScoutToolbar<IDesktop> m_uiToolbar;
  private static final int TOOLBAR_HEIGHT = 43;

  public RwtScoutMobileDesktop() {
    setToolbarHeight(TOOLBAR_HEIGHT);
  }

  @Override
  protected Control createToolBar(Composite parent) {
    m_uiToolbar = new RwtScoutMobileToolbarContainer();
    m_uiToolbar.createUiField(parent, getScoutObject(), getUiEnvironment());
    return m_uiToolbar.getUiContainer();
  }

  @Override
  public IRwtScoutToolbar getUiToolbar() {
    return m_uiToolbar;
  }

}
