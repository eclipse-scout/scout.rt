/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.client;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;

public final class TestingClientConfigProperties {
  private TestingClientConfigProperties() {
  }

  /**
   * Client session expiration in milliseconds. Default is one day.
   */
  public static class ClientSessionCacheExpirationProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.DAYS.toMillis(1);
    }

    @Override
    public String description() {
      return "Testing client session expiration in milliseconds. The default value is 1 day.";
    }

    @Override
    public String getKey() {
      return "scout.client.testingSessionTtl";
    }
  }
}
