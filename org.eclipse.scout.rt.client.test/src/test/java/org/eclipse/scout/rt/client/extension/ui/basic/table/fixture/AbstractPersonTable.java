/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.basic.table.fixture;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;

public abstract class AbstractPersonTable extends AbstractTable {

  public NameColumn getNameColumn() {
    return getColumnSet().getColumnByClass(NameColumn.class);
  }

  public AgeColumn getAgeColumn() {
    return getColumnSet().getColumnByClass(AgeColumn.class);
  }

  @Order(10)
  public class NameColumn extends AbstractStringColumn {
  }

  @Order(20)
  public class AgeColumn extends AbstractLongColumn {
  }
}
