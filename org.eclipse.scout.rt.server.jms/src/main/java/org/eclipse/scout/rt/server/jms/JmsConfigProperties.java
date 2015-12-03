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
package org.eclipse.scout.rt.server.jms;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;

public final class JmsConfigProperties {
  private JmsConfigProperties() {
  }

  /**
   * timeout in milliseconds
   */
  public static class JmsRequestTimeoutProperty extends AbstractPositiveLongConfigProperty {

    @Override
    protected Long getDefaultValue() {
      return TimeUnit.SECONDS.toMillis(1);
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.jms.AbstractSimpleJmsService#requestTimeout";
    }
  }
}
