/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.TableOrganizer;

/**
 * Provides an instance of {@link TableOrganizer}.
 *
 * @since 5.2
 */
public class TableOrganizerProvider implements ITableOrganizerProvider {

  @Override
  public ITableOrganizer createTableOrganizer(ITable table) {
    return new TableOrganizer(table);
  }
}
