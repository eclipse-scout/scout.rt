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
