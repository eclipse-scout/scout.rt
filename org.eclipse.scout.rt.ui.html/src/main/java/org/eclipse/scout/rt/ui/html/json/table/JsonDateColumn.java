/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonDateColumnUserFilter;
import org.json.JSONObject;

public class JsonDateColumn<T extends IDateColumn> extends JsonColumn<T> {

  public JsonDateColumn(T model) {
    super(model);
  }

  @Override
  public String getObjectType() {
    return "DateColumn";
  }

  @Override
  protected ColumnUserFilterState createFilterStateFromJson(JSONObject json) {
    return new JsonDateColumnUserFilter(null).createFilterStateFromJson(getColumn(), json);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("hasDate", getColumn().isHasDate());
    json.put("hasTime", getColumn().isHasTime());
    json.put(IDateColumn.PROP_GROUP_FORMAT, getColumn().getGroupFormat());
    // TODO [7.0] CGU: update IDateColumnInterface
    // getDateFormat uses NlsLocale. IMHO getDateFormat should not perform any logic because it just a getter-> refactor. same on AbstractDateField
    // Alternative would be to use a clientJob or set localethreadlocal in ui thread as well, as done in rap
    Locale oldLocale = NlsLocale.getOrElse(null);
    try {
      NlsLocale.set(getUiSession().getClientSession().getLocale());
      Method method = AbstractDateColumn.class.getDeclaredMethod("getDateFormat");
      method.setAccessible(true);
      SimpleDateFormat dateFormat = (SimpleDateFormat) method.invoke(getColumn());
      json.put("format", dateFormat.toPattern()); //Don't use toLocalizedPattern, it translates the chars ('d' to 't' for german).
    }
    catch (ReflectiveOperationException e) {
      throw new UiException("Failed to create JSON from 'date column'", BEANS.get(DefaultExceptionTranslator.class).unwrap(e));
    }
    finally {
      NlsLocale.set(oldLocale);
    }
    return json;
  }

  @Override
  public boolean isValueRequired() {
    return true;
  }

  @Override
  public Object cellValueToJson(Object value) {
    Date date = (Date) value;
    if (date != null) {
      return new JsonDate(date).asJsonString(false, getColumn().isHasDate(), getColumn().isHasTime());
    }
    return null;
  }

}
