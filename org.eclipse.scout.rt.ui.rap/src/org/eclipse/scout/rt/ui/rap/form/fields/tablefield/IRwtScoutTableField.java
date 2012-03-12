/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.tablefield;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;

public interface IRwtScoutTableField extends IRwtScoutFormField<ITableField<? extends ITable>> {

  /**
   * Custom variant for a table's container field when placed inside a TableField.
   */
  static final String VARIANT_TABLE_CONTAINER = "tableField";

  /**
   * Custom variant like {@link #VARIANT_TABLE_CONTAINER}, but for disabled state.
   * (Workaround, because RAP does not seem to apply the ":disabled" state correctly.)
   */
  static final String VARIANT_TABLE_CONTAINER_DISABLED = "tableFieldDisabled";

}
