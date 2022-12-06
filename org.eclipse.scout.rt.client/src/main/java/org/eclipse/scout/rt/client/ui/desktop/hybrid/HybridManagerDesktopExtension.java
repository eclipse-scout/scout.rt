/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.ContributionCommand;
import org.eclipse.scout.rt.platform.BEANS;

public class HybridManagerDesktopExtension extends AbstractDesktopExtension {

  @Override
  protected ContributionCommand execInit() {
    getCoreDesktop().addAddOn(BEANS.get(HybridManager.class));
    return super.execInit();
  }

  @Override
  protected ContributionCommand execGuiDetached() {
    getCoreDesktop().getAddOn(HybridManager.class).clear();
    return super.execGuiDetached();
  }
}
