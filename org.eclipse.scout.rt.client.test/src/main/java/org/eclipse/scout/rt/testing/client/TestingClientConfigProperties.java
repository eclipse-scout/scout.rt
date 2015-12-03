/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
      return Long.valueOf(TimeUnit.DAYS.toMillis(1));
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.testing.client.ClientSessionProviderWithCache#expiration";
    }
  }
}
