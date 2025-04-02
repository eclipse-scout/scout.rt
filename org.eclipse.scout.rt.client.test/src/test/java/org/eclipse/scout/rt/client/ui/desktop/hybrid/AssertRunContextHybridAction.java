/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.util.Assertions;

public class AssertRunContextHybridAction extends AbstractHybridAction<AssertRunContextHybridActionDo> {

  @Override
  public void execute(AssertRunContextHybridActionDo data) {
    var runContext = Assertions.assertInstance(ClientRunContext.CURRENT.get(), ClientRunContext.class);
    Assertions.assertEquals(runContext.getDesktop(), data.getDesktop());
    Assertions.assertEquals(runContext.getOutline(), data.getOutline());
    Assertions.assertEquals(runContext.getForm(), data.getForm());
  }
}
