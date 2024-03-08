/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.security;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;

@Replace
@Order(BeanTestingHelper.TESTING_BEAN_ORDER)
public class TestingPermissionsInvalidationNotificationListener extends PermissionsInvalidationNotificationListener {
  @Override
  protected void init() {
    // nop in testing
  }
}
