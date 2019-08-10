/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package com.bsiag.scout.rt.shared.data.basic.chart;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @since 5.2
 */
public interface IChartBean extends Serializable {

  List<List<IChartAxisBean>> getAxes();

  List<IChartValueGroupBean> getChartValueGroups();

  void removeCustomProperty(String name);

  void addCustomProperty(String name, Object prop);

  Object getCustomProperty(String name);

  Map<String, Object> getCustomProperties();
}
