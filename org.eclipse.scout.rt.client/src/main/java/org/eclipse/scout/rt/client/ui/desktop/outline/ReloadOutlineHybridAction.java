/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.desktop.hybrid.AbstractHybridAction;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionType;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.js.LoadChildPagesHybridActionDo;

@HybridActionType(ReloadOutlineHybridAction.TYPE)
public class ReloadOutlineHybridAction extends AbstractHybridAction<LoadChildPagesHybridActionDo> {

  protected static final String TYPE = "scout.ReloadOutline";

  @Override
  public void execute(LoadChildPagesHybridActionDo data) {
    try {
      var outline = getContextElement("outline").getWidget(IOutline.class);
      var rootPage = outline.getRootPage();
      if (rootPage != null) {
        rootPage.reloadPage();
      }
    }
    finally { // always signal the end of the action to the UI, even in the case of an error on the server
      fireHybridActionEndEvent();
    }
  }
}
