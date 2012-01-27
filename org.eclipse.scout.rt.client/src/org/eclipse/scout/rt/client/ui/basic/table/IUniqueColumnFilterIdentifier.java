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
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * An identifier used in {@link ClientUIPreferences} to persist column filter information for the implementing column.
 */
public interface IUniqueColumnFilterIdentifier {

  /**
   * @return a unique identifier, all columns with same {@link IColumn#getColumnId()}, same Table and same identifier
   *         will share the {@link ClientUIPreferences} for a column filter
   */
  String getIdentifier();

}
