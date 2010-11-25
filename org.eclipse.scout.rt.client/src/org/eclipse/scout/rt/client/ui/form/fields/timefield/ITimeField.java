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
package org.eclipse.scout.rt.client.ui.form.fields.timefield;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * Represent a time between 00:00:00.000 and 23:59:59.999 as a double value
 * where the double value range is between 0.0 (inclusive) and 1.0 (exclusive)
 */
public interface ITimeField extends IValueField<Double> {
  long MILLIS_PER_DAY = 1000L * 3600L * 24L;

  String PROP_TIME_ICON_ID = "timeIconId";

  void setFormat(String s);

  String getFormat();

  void setTimeIconId(String s);

  String getTimeIconId();

  Double getTimeValue();

  ITimeFieldUIFacade getUIFacade();
}
