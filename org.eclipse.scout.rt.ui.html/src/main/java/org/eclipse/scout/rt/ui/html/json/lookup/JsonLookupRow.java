/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.lookup;

import java.util.function.Function;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONObject;

public class JsonLookupRow<T> implements IJsonObject {

  private final ILookupRow<T> m_lookupRow;
  private final boolean m_multipleColumns;
  private final Function<T, ?> m_keyMapper;

  public JsonLookupRow(ILookupRow<T> lookupRow) {
    this(lookupRow, false, null);
  }

  public JsonLookupRow(ILookupRow<T> lookupRow, boolean multipleColumns, Function<T, ?> keyMapper) {
    Assertions.assertNotNull(lookupRow);
    m_lookupRow = lookupRow;
    m_multipleColumns = multipleColumns;
    m_keyMapper = keyMapper;
  }

  @Override
  public Object toJson() {
    JSONObject json = new JSONObject();
    json.put("key", getKey(m_lookupRow.getKey()));
    json.put("text", m_lookupRow.getText());
    if (StringUtility.hasText(m_lookupRow.getIconId())) {
      json.put("iconId", BinaryResourceUrlUtility.createIconUrl(m_lookupRow.getIconId()));
    }
    if (StringUtility.hasText(m_lookupRow.getTooltipText())) {
      json.put("tooltipText", m_lookupRow.getTooltipText());
    }
    if (StringUtility.hasText(m_lookupRow.getBackgroundColor())) {
      json.put("backgroundColor", m_lookupRow.getBackgroundColor());
    }
    if (StringUtility.hasText(m_lookupRow.getForegroundColor())) {
      json.put("foregroundColor", m_lookupRow.getForegroundColor());
    }
    if (m_lookupRow.getFont() != null) {
      json.put("font", m_lookupRow.getFont().toPattern());
    }
    if (!m_lookupRow.isEnabled()) {
      json.put("enabled", m_lookupRow.isEnabled());
    }
    if (m_lookupRow.getParentKey() != null) {
      json.put("parentKey", getKey(m_lookupRow.getParentKey()));
    }
    if (!m_lookupRow.isActive()) {
      json.put("active", m_lookupRow.isActive());
    }
    if (m_multipleColumns && m_lookupRow.getAdditionalTableRowData() != null) {
      json.put("additionalTableRowData", tableRowDataToJson(m_lookupRow.getAdditionalTableRowData()));
    }
    if (StringUtility.hasText(m_lookupRow.getCssClass())) {
      json.put("cssClass", m_lookupRow.getCssClass());
    }
    return json;
  }

  protected Object tableRowDataToJson(AbstractTableRowData tableRowData) {
    if (tableRowData == null) {
      return null;
    }
    return MainJsonObjectFactory.get().createJsonObject(tableRowData).toJson();
  }

  protected Object getKey(T key) {
    if (key == null) {
      return null;
    }
    return m_keyMapper == null ? key : m_keyMapper.apply(key);
  }

  public static <T> Object toJson(ILookupRow<T> lookupRow) {
    return toJson(lookupRow, false, null);
  }

  public static <T> Object toJson(ILookupRow<T> lookupRow, boolean multipleColumns, Function<T, ?> keyMapper) {
    if (lookupRow == null) {
      return null;
    }
    return new JsonLookupRow<>(lookupRow, multipleColumns, keyMapper).toJson();
  }
}
