/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
public interface IChartValueGroupBean extends Serializable {

  String getType();

  void setType(String type);

  Object getGroupKey();

  void setGroupKey(Object groupKey);

  String getGroupName();

  void setGroupName(String groupName);

  List<String> getColorHexValue();

  void setColorHexValue(String... colorHexValue);

  void setColorHexValue(List<String> colorHexValue);

  void addColorHexValue(String colorHexValue);

  void clearColorHexValue();

  boolean isClickable();

  void setClickable(boolean clickable);
}
