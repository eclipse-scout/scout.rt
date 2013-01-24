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

import org.eclipse.scout.rt.ui.rap.window.desktop.toolbar.RwtScoutToolButtonBar;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileToolButtonBar extends RwtScoutToolButtonBar {

  @Override
  public boolean isShowingCollapseButtonEnabled() {
    return false;
  }

  @Override
  public boolean isShowingLabelEnabled() {
    return false;
  }
}
