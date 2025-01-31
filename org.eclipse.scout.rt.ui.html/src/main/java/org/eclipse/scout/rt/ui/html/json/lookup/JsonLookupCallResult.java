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

import java.util.Collection;
import java.util.function.Function;

import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam.QueryBy;
import org.eclipse.scout.rt.platform.exception.IThrowableWithContextInfo;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonLookupCallResult<T> implements IJsonObject {

  private final ILookupCallResult<T> m_result;
  private final boolean m_multipleColumns;
  private final Function<T, ?> m_keyMapper;

  public JsonLookupCallResult(ILookupCallResult<T> result) {
    this(result, false, null);
  }

  public JsonLookupCallResult(ILookupCallResult<T> result, boolean multipleColumns, Function<T, ?> keyMapper) {
    Assertions.assertNotNull(result);
    m_result = result;
    m_multipleColumns = multipleColumns;
    m_keyMapper = keyMapper;
  }

  @Override
  public Object toJson() {
    JSONObject json = new JSONObject();
    json.put("lookupRows", lookupRowsToJson(m_result.getLookupRows()));
    IQueryParam queryParam = m_result.getQueryParam();
    json.put("queryBy", queryParam.getQueryBy());
    if (queryParam.is(QueryBy.TEXT)) {
      json.put("text", queryParam.getText());
    }
    else if (queryParam.is(QueryBy.KEY)) {
      json.put("key", getKey(queryParam.getKey()));
    }
    else if (queryParam.is(QueryBy.REC)) {
      json.put("rec", getKey(queryParam.getKey()));
    }
    if (m_result.getException() != null) {
      json.put("exception", exceptionToJson(m_result.getException()));
    }
    return json;
  }

  protected JSONArray lookupRowsToJson(Collection<ILookupRow<T>> lookupRows) {
    if (lookupRows == null) {
      return null;
    }
    JSONArray json = new JSONArray();
    for (ILookupRow<T> lookupRow : lookupRows) {
      json.put(lookupRowToJson(lookupRow, m_multipleColumns));
    }
    return json;
  }

  protected Object lookupRowToJson(ILookupRow<T> lookupRow, boolean multipleColumns) {
    return JsonLookupRow.toJson(lookupRow, multipleColumns, m_keyMapper);
  }

  @SuppressWarnings("unchecked")
  protected Object getKey(Object key) {
    if (key == null) {
      return null;
    }
    return m_keyMapper == null ? key : m_keyMapper.apply((T) key);
  }

  protected Object exceptionToJson(Throwable exception) {
    if (exception instanceof PlatformException) {
      return ((IThrowableWithContextInfo) exception).getDisplayMessage();
    }
    else {
      return exception.getMessage();
    }
  }

  public static <T> Object toJson(ILookupCallResult<T> result) {
    return toJson(result, false, null);
  }

  public static <T> Object toJson(ILookupCallResult<T> result, boolean multipleColumns, Function<T, ?> keyMapper) {
    if (result == null) {
      return null;
    }
    return new JsonLookupCallResult<>(result, multipleColumns, keyMapper).toJson();
  }
}
