/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonDateColumn<T extends IDateColumn> extends JsonColumn<T> {

  public JsonDateColumn(T model) {
    super(model);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    // FIXME CGU: update IDateColumnInterface
    // getDateFormat uses NlsLocale. IMHO getDateFormat should not perform any logic because it just a getter-> refactor. same on AbstractDateField
    // Alternative would be to use a clientJob or set localethreadlocal in ui thread as well, as done in rap
    Locale oldLocale = NlsLocale.get(false);
    try {
      NlsLocale.set(getUiSession().getClientSession().getLocale());
      Method method = AbstractDateColumn.class.getDeclaredMethod("getDateFormat");
      method.setAccessible(true);
      SimpleDateFormat dateFormat = (SimpleDateFormat) method.invoke(getColumn());
      JsonObjectUtility.putProperty(json, "format", dateFormat.toPattern()); //Don't use toLocalizedPattern, it translates the chars ('d' to 't' for german).
    }
    catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new JsonException("", e);
    }
    finally {
      NlsLocale.set(oldLocale);
    }
    return json;
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
