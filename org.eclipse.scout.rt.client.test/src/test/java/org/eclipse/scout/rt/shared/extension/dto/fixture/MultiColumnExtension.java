/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigPageWithTable.Table;

@Data(MultiColumnExtensionData.class)
@Extends(OrigPageWithTable.Table.class)
public class MultiColumnExtension extends AbstractTableExtension<OrigPageWithTable.Table> {

  /**
   * @param owner
   */
  public MultiColumnExtension(Table owner) {
    super(owner);
  }

  public ThirdLongColumn getThirdLongColumn() {
    return getOwner().getColumnSet().getColumnByClass(ThirdLongColumn.class);
  }

  public FourthBigDecimalColumn getFourthBigDecimalColumn() {
    return getOwner().getColumnSet().getColumnByClass(FourthBigDecimalColumn.class);
  }

  @Order(3000)
  public class ThirdLongColumn extends AbstractLongColumn {

  }

  @Order(4000)
  public class FourthBigDecimalColumn extends AbstractBigDecimalColumn {

  }
}
