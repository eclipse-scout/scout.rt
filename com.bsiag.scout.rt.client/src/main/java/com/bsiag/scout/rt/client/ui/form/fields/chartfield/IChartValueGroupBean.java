/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.form.fields.chartfield;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 */
public interface IChartValueGroupBean {
  String getGroupName();

  void setGroupName(String groupName);

  List<BigDecimal> getValues();

  void setValues(List<BigDecimal> values);

  List<String> getColorHexValue();

  void setColorHexValue(List<String> colorHexValue);
}
