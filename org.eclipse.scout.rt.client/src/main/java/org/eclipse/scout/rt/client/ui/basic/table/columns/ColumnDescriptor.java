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

  private String m_propertyName;
  private String m_text;
  private int m_width;
  private boolean m_fixedWidth;
  private int m_horizontalAlignment = -1;

  public ColumnDescriptor(String propertyName) {
    m_propertyName = propertyName;
  }

  public ColumnDescriptor(String propertyName, String text, int width) {
    m_propertyName = propertyName;
    m_text = text;
    m_width = width;
  }

  public String getPropertyName() {
    return m_propertyName;
  }

  public String getText() {
    return m_text;
  }

  public ColumnDescriptor withText(String text) {
    m_text = text;
    return this;
  }

  public int getWidth() {
    return m_width;
  }

  public ColumnDescriptor withWidth(int width) {
    m_width = width;
    return this;
  }

  public boolean getFixedWidth() {
    return m_fixedWidth;
  }

  public ColumnDescriptor withFixedWidth(boolean fixedWidth) {
    m_fixedWidth = fixedWidth;
    return this;
  }

  public int getHorizontalAlignment() {
    return m_horizontalAlignment;
  }

  public ColumnDescriptor withHorizontalAlignment(int horizontalAlignment) {
    m_horizontalAlignment = horizontalAlignment;
    return this;
  }

}
