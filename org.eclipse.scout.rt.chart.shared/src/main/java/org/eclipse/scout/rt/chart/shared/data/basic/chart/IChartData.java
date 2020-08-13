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
import java.util.List;

/**
 * @since 5.2
 */
public interface IChartData extends Serializable {

  List<List<IChartAxisBean>> getAxes();

  List<IChartValueGroupBean> getChartValueGroups();
}
