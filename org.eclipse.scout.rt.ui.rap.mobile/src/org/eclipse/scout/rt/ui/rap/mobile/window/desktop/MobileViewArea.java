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

import org.eclipse.scout.rt.client.mobile.transformation.MobileDeviceTransformer;
import org.eclipse.scout.rt.client.mobile.transformation.TabletDeviceTransformer;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutViewStack;
import org.eclipse.scout.rt.ui.rap.window.desktop.viewarea.ViewArea;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Sash;

/**
 * @since 3.9.0
 */
public class MobileViewArea extends ViewArea {

  private static final long serialVersionUID = 1L;

  public MobileViewArea(Composite parent) {
    super(parent);
  }

  @Override
  protected RwtScoutViewStack createRwtScoutViewStack(Composite parent) {
    return new RwtScoutMobileViewStack(parent, getUiEnvironment(), this);
  }

  @Override
  protected Sash createSash(Composite parent, int style) {
    SimpleSash simpleSash = new SimpleSash(parent, style);
    return simpleSash;
  }

  @Override
  protected int getSashWidth() {
    return 1;
  }

  /**
   * On tablet devices there are at maximum two view stacks, on mobile only one. So it is not necessary to create the
   * other ones which saves unnecessary composites and therefore loading time.
   * 
   * @see {@link MobileDeviceTransformer}, {@link TabletDeviceTransformer}
   */
  @Override
  protected boolean acceptViewId(String viewId) {
    return IForm.VIEW_ID_CENTER.equals(viewId) || IForm.VIEW_ID_E.equals(viewId);
  }

}
