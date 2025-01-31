/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
