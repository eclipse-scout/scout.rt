/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.basic.table.fixture;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.AbstractStringColumnExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.AbstractPersonTable.NameColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;

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
