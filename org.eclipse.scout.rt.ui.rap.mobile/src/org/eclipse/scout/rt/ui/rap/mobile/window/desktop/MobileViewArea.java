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

import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutViewStack;
import org.eclipse.scout.rt.ui.rap.window.desktop.viewarea.ViewArea;
import org.eclipse.swt.widgets.Composite;

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

}
