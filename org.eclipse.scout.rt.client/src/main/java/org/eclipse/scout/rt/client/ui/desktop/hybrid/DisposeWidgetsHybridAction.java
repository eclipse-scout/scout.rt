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

import java.util.Collection;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to dispose widgets added to the hybrid manager from remote.
 *
 * @see HybridManager#addWidgets(Map)
 * @see HybridManager#removeWidgets(Collection)
 */
@HybridActionType(DisposeWidgetsHybridAction.TYPE)
public class DisposeWidgetsHybridAction extends AbstractHybridAction<DisposeWidgetsHybridActionDo> {
  protected static final String TYPE = "DisposeWidgets";
  private static final Logger LOG = LoggerFactory.getLogger(DisposeWidgetsHybridAction.class);

  @Override
  public void execute(DisposeWidgetsHybridActionDo data) {
    for (String id : data.getIds()) {
      IWidget widget = hybridManager().getWidgetById(id);
      if (widget != null) {
        widget.dispose();
        LOG.debug("Disposed hybrid widget with id {}", id);
      }
    }
  }
}
