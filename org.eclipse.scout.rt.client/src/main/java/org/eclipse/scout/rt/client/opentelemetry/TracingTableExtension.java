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

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowClickChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowsSelectedChain;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;

import io.opentelemetry.api.trace.Tracer;

public class TracingTableExtension extends AbstractTableExtension<AbstractTable> {

  private Tracer m_tracer;

  public TracingTableExtension(AbstractTable owner) {
    super(owner);
    m_tracer = BEANS.get(ITracingHelper.class).createTracer(TracingTableExtension.class);
  }

  @Override
  public void execRowsSelected(TableRowsSelectedChain chain, List<? extends ITableRow> rows) {
    String name = getOwner().getClass().getSimpleName() + "#execRowsSelected";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.table.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.table.text", getOwner().getTitle());
      span.setAttribute("scout.client.tree.event.rows.count", rows.size());
      super.execRowsSelected(chain, rows);
    });
  }

  @Override
  public void execRowClick(TableRowClickChain chain, ITableRow row, MouseButton mouseButton) {
    String name = getOwner().getClass().getSimpleName() + "#execRowClick";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.table.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.table.text", getOwner().getTitle());
      span.setAttribute("scout.client.table.event.mouseButton", mouseButton.name());
      span.setAttribute("scout.client.table.event.row.index", row.getRowIndex());
      super.execRowClick(chain, row, mouseButton);
    });
  }
}
