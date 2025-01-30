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
import org.eclipse.scout.rt.client.opentelemetry.OpenTelemetryExtensionInstrumenterFactory.OpenTelemetryExtensionRequest;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.BEANS;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public class OpenTelemetryTableExtension extends AbstractTableExtension<AbstractTable> {

  private Instrumenter<OpenTelemetryExtensionRequest<AbstractTable>, Void> m_instrumenter;
  private static final AttributeKey<String> ROWS_COUNT = AttributeKey.stringKey("scout.extension.rows.count");
  private static final AttributeKey<String> ROWS_INDEX = AttributeKey.stringKey("scout.extension.rows.index");
  private static final AttributeKey<String> MOUSE_BUTTON_NAME = AttributeKey.stringKey("scout.extension.mouse.button.name");

  public OpenTelemetryTableExtension(AbstractTable owner) {
    super(owner);
    m_instrumenter = BEANS.get(OpenTelemetryExtensionInstrumenterFactory.class).createInstrumenter(
        getClass(),
        (request) -> request.getOwner().getTitle());
  }

  @Override
  public void execRowsSelected(TableRowsSelectedChain chain, List<? extends ITableRow> rows) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execRowsSelected")
        .withAdditionalAttributesProvider(attributes -> attributes.put(ROWS_COUNT, String.valueOf(rows.size())))
        .wrapCall(() -> super.execRowsSelected(chain, rows), m_instrumenter);
  }

  @Override
  public void execRowClick(TableRowClickChain chain, ITableRow row, MouseButton mouseButton) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execRowClick")
        .withAdditionalAttributesProvider(attributes -> {
          attributes.put(MOUSE_BUTTON_NAME, mouseButton.name());
          attributes.put(ROWS_INDEX, String.valueOf(row.getRowIndex()));
        })
        .wrapCall(() -> super.execRowClick(chain, row, mouseButton), m_instrumenter);
  }
}
