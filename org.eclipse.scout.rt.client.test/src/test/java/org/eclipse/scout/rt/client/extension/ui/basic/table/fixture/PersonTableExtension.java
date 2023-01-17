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

import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.AbstractStringColumnExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.AbstractPersonTable.NameColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;

public class PersonTableExtension extends AbstractTableExtension<AbstractPersonTable> {

  public PersonTableExtension(AbstractPersonTable owner) {
    super(owner);
  }

  public StreetColumn getStreetColumn() {
    return getOwner().getColumnSet().getColumnByClass(StreetColumn.class);
  }

  public CityColumn getCityColumn() {
    return getOwner().getColumnSet().getColumnByClass(CityColumn.class);
  }

  @Order(100)
  public class StreetColumn extends AbstractStringColumn {
  }

  @Order(200)
  public class CityColumn extends AbstractStringColumn {
  }

  public class NameColumnExtension extends AbstractStringColumnExtension<NameColumn> {
    public NameColumnExtension(NameColumn owner) {
      super(owner);
    }
  }
}
