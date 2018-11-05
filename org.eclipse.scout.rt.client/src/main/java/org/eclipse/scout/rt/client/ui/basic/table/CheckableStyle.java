/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

public enum CheckableStyle {

  /**
   * When row is checked a boolean column with a checkbox is inserted into the table.
   */
  CHECKBOX,

  /**
   * When a row is checked the table-row is marked as checked. By default a background color is set on the table-row
   * when the row is checked.
   */
  TABLE_ROW,

  /**
   * Like the CHECKBOX Style but a click anywhere on the row triggers the check.
   */
  CHECKBOX_TABLE_ROW
}
