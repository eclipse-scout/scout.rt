/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;

public class BlubbContextElementConverter extends AbstractHybridActionContextElementConverter<JsonTree<?>, Long, ITableRow> {

  @Override
  public ITableRow jsonToElement(JsonTree<?> adapter, Long jsonElement) {
    throw new ProcessingException("Should never happen");
  }

  @Override
  public Long elementToJson(JsonTree<?> adapter, ITableRow element) {
    throw new ProcessingException("Should never happen");
  }
}
