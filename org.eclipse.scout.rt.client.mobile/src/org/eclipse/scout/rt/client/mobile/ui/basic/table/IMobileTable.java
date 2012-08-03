/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;

/**
 * @since 3.9.0
 */
public interface IMobileTable extends ITable {

  String PROP_DRILL_DOWN_STYLE_MAP = "drillDownStyleMap";
  String PROP_AUTO_CREATE_TABLE_ROW_FORM = "autoCreateTableRowForm";

  DrillDownStyleMap getDrillDownStyleMap();

  void setDrillDownStyleMap(DrillDownStyleMap drillDownStyleMap);

  boolean isAutoCreateTableRowForm();

  void setAutoCreateTableRowForm(boolean autoCreateRowForm);
}
