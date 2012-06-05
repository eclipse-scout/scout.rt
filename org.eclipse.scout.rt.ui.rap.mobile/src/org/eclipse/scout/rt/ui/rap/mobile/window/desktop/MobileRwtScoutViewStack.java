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

import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutViewStack;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public class MobileRwtScoutViewStack extends RwtScoutViewStack {

  private static final long serialVersionUID = 1L;

  public MobileRwtScoutViewStack(Composite parent, IRwtEnvironment uiEnvironment) {
    super(parent, uiEnvironment);
  }

  @Override
  protected boolean alwaysHideTabBar() {
    return true;
  }

}
