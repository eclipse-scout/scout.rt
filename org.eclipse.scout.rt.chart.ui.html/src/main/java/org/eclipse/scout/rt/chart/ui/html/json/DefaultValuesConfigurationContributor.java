/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.ui.html.json;

import java.net.URL;

import org.eclipse.scout.rt.chart.ui.html.ResourceBase;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.ui.html.json.IDefaultValuesConfigurationContributor;

@Order(5450)
public class DefaultValuesConfigurationContributor implements IDefaultValuesConfigurationContributor {

  @Override
  public URL contributeDefaultValuesConfigurationUrl() {
    return ResourceBase.class.getResource("json/defaultValues.json");
  }
}
