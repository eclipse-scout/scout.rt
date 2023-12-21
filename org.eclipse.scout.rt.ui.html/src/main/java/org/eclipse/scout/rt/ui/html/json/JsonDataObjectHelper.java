/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IIdSignatureDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.json.JSONArray;
import org.json.JSONObject;

@Bean
public class JsonDataObjectHelper {

  /**
   * This helper solely converts {@link JSONObject}s to {@link IDataObject}s and vice versa that are received from or
   * sent to the browser. Therefore, the {@link IDataObjectMapper} always needs to be an
   * {@link IIdSignatureDataObjectMapper}.
   */
  protected final IIdSignatureDataObjectMapper m_defaultDataObjectMapper = BEANS.get(IIdSignatureDataObjectMapper.class);
  protected IIdSignatureDataObjectMapper m_dataObjectMapper = null;

  public IIdSignatureDataObjectMapper getDataObjectMapper() {
    return (m_dataObjectMapper == null ? m_defaultDataObjectMapper : m_dataObjectMapper);
  }

  public JsonDataObjectHelper withDataObjectMapper(IIdSignatureDataObjectMapper dataObjectMapper) {
    m_dataObjectMapper = dataObjectMapper;
    return this;
  }

  public JSONObject dataObjectToJson(IDoEntity dataObject) {
    if (dataObject == null) {
      return null;
    }
    String str = getDataObjectMapper().writeValue(dataObject);
    return new JSONObject(str);
  }

  public JSONArray dataObjectsToJson(List<? extends IDoEntity> dataObjects) {
    if (dataObjects == null) {
      return null;
    }
    String str = getDataObjectMapper().writeValue(dataObjects);
    return new JSONArray(str);
  }

  public <T extends IDoEntity> JSONArray dataObjectsToJson(List<T> dataObjects, Function<T, JSONObject> mapper) {
    return dataObjectsToJson(dataObjects, mapper, true);
  }

  public <T extends IDoEntity> JSONArray dataObjectsToJson(List<T> dataObjects, Function<T, JSONObject> mapper, boolean skipNulls) {
    if (dataObjects == null) {
      return null;
    }
    return new JSONArray(dataObjects.stream()
        .map(mapper)
        .filter(skipNulls ? Objects::nonNull : x -> true)
        .collect(Collectors.toList()));
  }

  public <T extends IDataObject> T jsonToDataObject(JSONObject jsonObject, Class<T> type) {
    if (jsonObject == null) {
      return null;
    }
    return getDataObjectMapper().readValue(jsonObject.toString(), type);
  }

  public <T extends IDataObject> List<T> jsonToDataObjects(JSONArray jsonArray, Class<T> type) {
    if (jsonArray == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    DoList<T> list = getDataObjectMapper().readValue(jsonArray.toString(), DoList.class);
    return list.get();
  }
}
