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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.table;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;

/**
 * @since 3.9.0
 */
public interface IMobileTableField<T extends ITable> extends ITableField<T> {
  String PROP_ACTION_BAR_VISIBLE = "actionBarVisible";

  boolean isActionBarVisible();

  void setActionBarVisible(boolean visible);
}
