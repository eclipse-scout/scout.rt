/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

/**
 * Interface required for interaction between JsonOutline and JsonOutlineTable.
 */
@FunctionalInterface
public interface IJsonOutlineAdapter {

  /**
   * Returns the JSON node ID for the given table row.
   */
  String getNodeId(ITableRow tableRow);
}
