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
package com.bsiag.scout.rt.client.ui.form.fields.chartfield;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.beans.IPropertyObserver;

/**
 *
 */
public interface IChart extends IPropertyObserver, ITypeWithClassId {

  String PROP_AUTO_COLOR = "autoColor";
  String PROP_CHART_TYPE = "chartType";
  String PROP_CHART_DATA = "chartData";
  String PROP_ENABLED = "enabled";
  String PROP_VISIBLE = "visible";
  String PROP_CONTAINER = "chartContainer";

  void setChartType(int chartType);

  int getChartType();

  void setAutoColor(boolean isAutoColor);

  boolean isAutoColor();

  IChartUIFacade getUIFacade();

  void addChartListener(ChartListener listener);

  void removeChartListener(ChartListener listener);

  void setChartData(IChartBean data);

  IChartBean getChartData();

  void setEnabled(boolean enabled);

  boolean isEnabled();

  void setVisible(boolean visible);

  boolean isVisible();

  void setContainerInternal(ITypeWithClassId container);

  ITypeWithClassId getContainer();

}
