/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client;

import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

public final class ClientConfigProperties {

  private ClientConfigProperties() {
  }

  public static class MemoryPolicyProperty extends AbstractStringConfigProperty {

    @Override
    protected String parse(String value) {
      if (ObjectUtility.isOneOf(value, "small", "medium", "large")) {
        return value;
      }
      throw new PlatformException("Invalid value for property '" + getKey() + "': '" + value + "'. Valid values are small, medium or large");
    }

    @Override
    public String getKey() {
      return "scout.client.memoryPolicy";
    }

    @Override
    public String description() {
      return "Specifies how long the client keeps fetched data before it is discarded. One of 'small', 'medium' or 'large'. The default value is 'large'.";
    }

    @Override
    public String getDefaultValue() {
      return "large";
    }
  }

  public static class UserAreaProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.client.userArea";
    }

    @Override
    public String description() {
      return "User data area (e.g. in the user home) to store user preferences. If nothing is specified the user home of the operating system is used. By default no user home is set.";
    }
  }

  public static class JobCompletionDelayOnSessionShutdown extends AbstractPositiveLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.client.jobCompletionDelayOnSessionShutdown";
    }

    @Override
    public String description() {
      return "Specifies the maximal time (in seconds) to wait until running jobs are cancelled on session shutdown.\n"
          + "The default value is 10 seconds.";
    }

    @Override
    public Long getDefaultValue() {
      return 10L;
    }
  }
}
