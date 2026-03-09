/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
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
import java.util.stream.Collectors;

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
  private static final Logger LOG = LoggerFactory.getLogger(DisposeWidgetsHybridAction.class);

  protected static final String TYPE = "scout.DisposeWidgets";

  @Override
  public void execute(DisposeWidgetsHybridActionDo data) {
    // get all widgets that need to be disposed
    Map<String, IWidget> widgets = data.getIds().stream()
        .map(id -> new IdWidgetTuple(id, hybridManager().getWidgetById(id)))
        .filter(idWidgetTuple -> idWidgetTuple.widget() != null)
        .collect(Collectors.toMap(IdWidgetTuple::id, IdWidgetTuple::widget));

    // remove widgets from hybrid manager before disposing the widgets to reduce the update count of the hybrid managers widgets property
    hybridManager().removeWidgetsById(widgets.keySet());

    // dispose widgets
    widgets.forEach((id, widget) -> {
      widget.dispose();
      LOG.debug("Disposed hybrid widget with id {}", id);
    });

    fireHybridActionEndEvent();
  }

  protected record IdWidgetTuple(String id, IWidget widget) {
  }
}
