/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.ContributionCommand;
import org.eclipse.scout.rt.platform.BEANS;

public class BrowserCallbacksDesktopExtension extends AbstractDesktopExtension {

  @Override
  protected ContributionCommand execInit() {
    getCoreDesktop().addAddOn(BEANS.get(BrowserCallbacks.class));
    return super.execInit();
  }
}
