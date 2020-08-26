/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.chart.shared.data.basic.chart;

import java.io.Serializable;

/**
 * @since 5.2
 */
public interface IChartValueGroupBean extends Serializable {

  String getType();

  void setType(String type);

  Object getGroupKey();

  void setGroupKey(Object groupKey);

  String getGroupName();

  void setGroupName(String groupName);

  String getColorHexValue();

  void setColorHexValue(String colorHexValue);

  String getCssClass();

  void setCssClass(String cssClass);

  boolean isClickable();

  void setClickable(boolean clickable);
}
