/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.io.Serializable;

/**
 * The column descriptor class is used to define texts, widths and order of columns. It is typically used for smart
 * fields with a proposal chooser of type table.
 */
public class ColumnDescriptor implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String m_propertyName;
  private final int m_width;
  private final String m_text;

  public ColumnDescriptor(String propertyName, String text, int width) {
    m_propertyName = propertyName;
    m_text = text;
    m_width = width;
  }

  public String getPropertyName() {
    return m_propertyName;
  }

  public int getWidth() {
    return m_width;
  }

  public String getText() {
    return m_text;
  }

}
