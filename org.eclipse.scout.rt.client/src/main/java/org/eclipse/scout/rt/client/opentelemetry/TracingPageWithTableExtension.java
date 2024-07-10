/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.opentelemetry;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTablePopulateTableChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;

import io.opentelemetry.api.trace.Tracer;

public class TracingPageWithTableExtension<T extends ITable> extends AbstractPageWithTableExtension<T, AbstractPageWithTable<T>> {

  private Tracer m_tracer;

  public TracingPageWithTableExtension(AbstractPageWithTable<T> owner) {
    super(owner);
    m_tracer = BEANS.get(ITracingHelper.class).createTracer(TracingPageWithTableExtension.class);
  }

  @Override
  public void execPopulateTable(PageWithTablePopulateTableChain<? extends ITable> chain) {
    String name = getOwner().getClass().getSimpleName() + "#execPopulateTable";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.pageWithTable.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.pageWithTable.id", getOwner().getNodeId());
      span.setAttribute("scout.client.pageWithTable.text", getOwner().getCell().getText());
      super.execPopulateTable(chain);
    });
  }
}
