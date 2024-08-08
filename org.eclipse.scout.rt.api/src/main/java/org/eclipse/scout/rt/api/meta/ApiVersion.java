/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.meta;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;

@Order(IBean.DEFAULT_BEAN_ORDER + 100)
public class ApiVersion implements IApiVersion {

  private static final String VERSION = "1.0.0";

  @Override
  public String getVersion() {
    return VERSION;
  }
}
