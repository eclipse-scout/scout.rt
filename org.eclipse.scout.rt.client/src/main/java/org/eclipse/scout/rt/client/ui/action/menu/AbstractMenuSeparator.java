/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu;

import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("37764b18-0496-47f2-bcaf-36bf0fb26ecd")
public abstract class AbstractMenuSeparator extends AbstractMenu {

  @Override
  protected final boolean getConfiguredSeparator() {
    return true;
  }

  @Override
  protected final String getConfiguredKeyStroke() {
    return null;
  }

  @Override
  protected final void execAction() {
    // void
  }

  @Override
  protected final void execSelectionChanged(boolean selection) {
    // void
  }

}
