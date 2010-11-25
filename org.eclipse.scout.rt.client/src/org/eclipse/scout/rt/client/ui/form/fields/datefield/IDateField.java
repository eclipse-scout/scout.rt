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
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface IDateField extends IValueField<Date> {
  long MILLIS_PER_DAY = 1000L * 3600L * 24L;

  String PROP_HAS_TIME = "hasTime";
  String PROP_DATE_ICON_ID = "dateIconId";

  IDateFieldUIFacade getUIFacade();

  void setFormat(String s);

  String getFormat();

  void setHasTime(boolean b);

  boolean isHasTime();

  void setDateIconId(String s);

  String getDateIconId();

  void setAutoTimeMillis(long l);

  void setAutoTimeMillis(int hour, int minute, int second);

  long getAutoTimeMillis();

  Double getTimeValue();

  void adjustDate(int days, int months, int years);
}
