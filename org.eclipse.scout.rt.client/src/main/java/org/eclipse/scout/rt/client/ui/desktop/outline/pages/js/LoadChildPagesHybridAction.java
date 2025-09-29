/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages.js;

import org.eclipse.scout.rt.client.ui.desktop.hybrid.AbstractHybridAction;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionType;

@HybridActionType(LoadChildPagesHybridAction.TYPE)
public class LoadChildPagesHybridAction extends AbstractHybridAction<LoadChildPagesHybridActionDo> {

  protected static final String TYPE = "scout.LoadChildPages";

  @Override
  public void execute(LoadChildPagesHybridActionDo data) {
    try {
      var jsPage = getContextElement("page").getElement(IJsPage.class);
      jsPage.loadChildPages(data.getPageParams(), data.isReplace());
    }
    finally { // always signal the end of the action to the UI, even in the case of an error on the server
      fireHybridActionEndEvent();
    }
  }
}
