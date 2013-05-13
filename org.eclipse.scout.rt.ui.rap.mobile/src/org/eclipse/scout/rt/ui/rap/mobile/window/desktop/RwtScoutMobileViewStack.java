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

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.window.IFormBoundsProvider;
import org.eclipse.scout.rt.ui.rap.window.desktop.IViewArea;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutDesktopForm;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutViewStack;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileViewStack extends RwtScoutViewStack {
  private static final long serialVersionUID = 1L;
  private IViewArea m_viewArea;

  public RwtScoutMobileViewStack(Composite parent, IRwtEnvironment uiEnvironment, IViewArea viewArea) {
    super(parent, uiEnvironment);

    m_viewArea = viewArea;
  }

  @Override
  protected boolean isTabBarCreationEnabled() {
    return false;
  }

  @Override
  protected RwtScoutDesktopForm createRwtScoutDesktopForm() {
    return new RwtScoutMobileDesktopForm();
  }

  @Override
  protected IFormBoundsProvider createFormBoundsProvider(IForm scoutForm, IRwtEnvironment uiEnvironment) {
    return new FormBasedDesktopFormBoundsProvider(scoutForm);
  }

  @Override
  protected void setPartVisibleImpl(IForm form) {
    super.setPartVisibleImpl(form);

    //Make sure the preferred size is updated if the visible part changes.
    if (form != null && form.isCacheBounds()) {
      initPreferredSize(getFormBoundsProviders().get(form));
      m_viewArea.updateSashPositionForViewStack(this);
      m_viewArea.layout();
    }
  }

}
