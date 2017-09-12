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
