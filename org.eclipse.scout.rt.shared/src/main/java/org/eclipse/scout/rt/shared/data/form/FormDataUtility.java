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
package org.eclipse.scout.rt.shared.data.form;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.reflect.FastBeanInfo;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

/**
 * @since 3.8.0
 */
public final class FormDataUtility {

  private static final Pattern FIELD_SUFFIX_PATTERN = Pattern.compile("Field$");
  private static final Pattern BUTTON_SUFFIX_PATTERN = Pattern.compile("Button$");
  private static final Pattern DATA_SUFFIX_PATTERN = Pattern.compile("Data$");

  private FormDataUtility() {
  }

  /**
   * Computes the field data ID for a given form field ID.
   * <p/>
   * <b>Note:</b> This method behaves exactly the same as the generate FormData operation in Scout SDK.
   *
   * @return Returns the corresponding field data ID for the given form field ID. The result is <code>null</code> if the
   *         field ID is <code>null</code> or if it contains white spaces only.
   */
  public static String getFieldDataId(String formFieldId) {
    String s = StringUtility.trim(formFieldId);
    if (StringUtility.isNullOrEmpty(s)) {
      return null;
    }
    if (s.endsWith("Field")) {
      return FIELD_SUFFIX_PATTERN.matcher(s).replaceAll("");
    }
    if (s.endsWith("Button")) {
      return BUTTON_SUFFIX_PATTERN.matcher(s).replaceAll("");
    }
    return s;
  }

  public static String getFieldDataId(AbstractFormFieldData fieldData) {
    String s = fieldData.getFieldId();
    if (s != null && s.endsWith("Data")) {
      s = DATA_SUFFIX_PATTERN.matcher(s).replaceAll("");
    }
    return getFieldDataId(s);
  }

  /**
   * @param formData
   * @param includeEmptyData
   *          true to include null and empty properties and fields, false to ignore them
   * @return
   */
  public static String toString(AbstractFormData formData, boolean includeEmptyData) {
    DataNode root = new DataNode();
    appendFormData(root, formData);
    DataNode n = root.firstChild();
    if (n != null && !includeEmptyData) {
      n.removeEmptyData();
    }
    return String.valueOf(n);
  }

  /**
   * @param field
   * @param includeEmptyData
   *          true to include null and empty properties and fields, false to ignore them
   * @return
   */
  public static String toString(AbstractFormFieldData field, boolean includeEmptyData) {
    DataNode root = new DataNode();
    appendFieldData(root, field);
    DataNode n = root.firstChild();
    if (n != null && !includeEmptyData) {
      n.removeEmptyData();
    }
    return String.valueOf(n);
  }

  /**
   * @param prop
   * @param includeEmptyData
   *          true to include null and empty properties and fields, false to ignore them
   * @return
   */
  public static String toString(AbstractPropertyData<?> prop, boolean includeEmptyData) {
    DataNode root = new DataNode();
    appendPropertyData(root, prop);
    DataNode n = root.firstChild();
    if (n != null && !includeEmptyData) {
      n.removeEmptyData();
    }
    return String.valueOf(n);
  }

  private static void appendFormData(DataNode parent, AbstractFormData formData) {
    if (formData == null) {
      return;
    }
    DataNode n = parent.addChild();
    n.prefix = formData.getClass().getSimpleName() + "[";
    n.suffix = "]";
    appendPropertyDatas(n, formData.getAllProperties());
    appendFieldDatas(n, formData.getFields());
  }

  private static void appendFieldDatas(DataNode parent, AbstractFormFieldData[] fields) {
    if (fields == null || fields.length == 0) {
      return;
    }
    Arrays.sort(fields, FORM_FIELD_DATA_COMPARATOR);
    for (AbstractFormFieldData field : fields) {
      appendFieldData(parent, field);
    }
  }

  private static void appendFieldData(DataNode parent, AbstractFormFieldData field) {
    if (field == null) {
      return;
    }
    DataNode n = parent.addChild();
    if (field instanceof AbstractValueFieldData<?>) {
      AbstractValueFieldData<?> valueField = (AbstractValueFieldData<?>) field;
      n.prefix = field.getClass().getSimpleName() + ": " + toLogText(valueField.getValue());
      if (!valueField.isValueSet()) {
        n.prefix += " (valueSet=false)";
      }
      n.hasContent = (valueField.getValue() != null);
    }
    else if (field instanceof AbstractTableFieldBeanData) {
      AbstractTableFieldBeanData tableData = (AbstractTableFieldBeanData) field;
      FastBeanInfo fastBeanInfo = BeanUtility.getFastBeanInfo(tableData.getRowType(), AbstractTableFieldBeanData.class);
      int rows = tableData.getRowCount();
      int cols = fastBeanInfo.getPropertyDescriptors().length;
      n.prefix = field.getClass().getSimpleName() + ": " + rows + " rows, " + cols + " cols [";
      if (!tableData.isValueSet()) {
        n.prefix += " (valueSet=false)";
      }
      n.suffix = "]";
    }
    else {
      n.prefix = field.getClass().getSimpleName();
      n.hasContent = field.isValueSet();
    }

    // field properties
    appendPropertyDatas(n, field.getAllProperties());

    // inner fields
    appendFieldDatas(n, field.getFields());
  }

  private static void appendPropertyDatas(DataNode parent, AbstractPropertyData<?>[] properties) {
    if (properties == null || properties.length == 0) {
      return;
    }
    Arrays.sort(properties, PROPERTY_DATA_COMPARATOR);
    for (AbstractPropertyData<?> prop : properties) {
      appendPropertyData(parent, prop);
    }
  }

  private static void appendPropertyData(DataNode parent, AbstractPropertyData<?> prop) {
    if (prop == null) {
      return;
    }
    DataNode n = parent.addChild();
    n.prefix = prop.getClass().getSimpleName() + ": " + toLogText(prop.getValue());
    if (!prop.isValueSet()) {
      n.prefix += " (valueSet=false)";
    }
    n.hasContent = (prop.getValue() != null);
  }

  private static String toLogText(Object o) {
    if (o == null) {
      return "null";
    }
    if (o.getClass().isArray()) {
      int n = Array.getLength(o);
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i < n; i++) {
        if (i > 0) {
          sb.append(",");
        }
        sb.append(toLogText(Array.get(o, i)));
      }
      sb.append("]");
      return sb.toString();
    }
    else {
      return o.toString();
    }
  }

  private static void indent(StringBuilder sb, int level) {
    for (int i = 0; i < level; i++) {
      sb.append("  ");
    }
  }

  private static final Comparator<AbstractPropertyData<?>> PROPERTY_DATA_COMPARATOR = new Comparator<AbstractPropertyData<?>>() {
    @Override
    public int compare(AbstractPropertyData<?> a, AbstractPropertyData<?> b) {
      String aid = null;
      String bid = null;
      if (a != null) {
        aid = a.getPropertyId();
      }
      if (b != null) {
        bid = b.getPropertyId();
      }
      return ObjectUtility.compareTo(aid, bid);
    }
  };
  private static final Comparator<AbstractFormFieldData> FORM_FIELD_DATA_COMPARATOR = new Comparator<AbstractFormFieldData>() {
    @Override
    public int compare(AbstractFormFieldData a, AbstractFormFieldData b) {
      String aid = null;
      String bid = null;
      if (a != null) {
        aid = a.getFieldId();
      }
      if (b != null) {
        bid = b.getFieldId();
      }
      return ObjectUtility.compareTo(aid, bid);
    }
  };

  private static final class DataNode {
    public String prefix;//NOSONAR
    public String suffix;//NOSONAR
    public boolean hasContent;//NOSONAR
    private final List<DataNode> m_children = new ArrayList<>();

    public DataNode addChild() {
      DataNode n = new DataNode();
      m_children.add(n);
      return n;
    }

    public DataNode firstChild() {
      return m_children.isEmpty() ? null : m_children.get(0);
    }

    public void removeEmptyData() {
      for (Iterator<DataNode> it = m_children.iterator(); it.hasNext();) {
        DataNode c = it.next();
        c.removeEmptyData();
        if (!c.hasContent && c.m_children.isEmpty()) {
          it.remove();
        }
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      buildRec(sb, 0);
      return sb.toString();
    }

    private void buildRec(StringBuilder sb, int level) {
      if (prefix != null) {
        sb.append(prefix);
      }
      if (!m_children.isEmpty()) {
        for (DataNode c : m_children) {
          sb.append("\n");
          indent(sb, level + 1);
          c.buildRec(sb, level + 1);
        }
        if (suffix != null) {
          sb.append("\n");
          indent(sb, level);
        }
      }
      if (suffix != null) {
        sb.append(suffix);
      }
    }
  }

}
