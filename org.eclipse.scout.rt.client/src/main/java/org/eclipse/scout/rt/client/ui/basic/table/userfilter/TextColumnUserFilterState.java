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
package org.eclipse.scout.rt.client.ui.basic.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public class TextColumnUserFilterState extends ColumnUserFilterState {
  private static final long serialVersionUID = 1L;

  private String m_freeText;

  public TextColumnUserFilterState(IColumn<?> column) {
    super(column);
  }

  public String getFreeText() {
    return m_freeText;
  }

  public void setFreeText(String freeText) {
    m_freeText = freeText;
  }

}
