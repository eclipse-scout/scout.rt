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

import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutDesktop;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileDesktop extends RwtScoutDesktop {

  private static final String VARIANT_VIEWS_AREA = "mobileViewsArea";

  /**
   * On mobile devices every form has a form header, the outline form too, so no global toolbar is necessary because the
   * forms
   * take care of that.
   */
  @Override
  protected Control createToolBar(Composite parent) {
    return null;
  }

  @Override
  protected String getViewsAreaVariant() {
    return VARIANT_VIEWS_AREA;
  }

}
