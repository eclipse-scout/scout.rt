/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.events;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.ConfigUtility;

/**
 * This class is Thread safe
 */
public final class ListenerListRegistry {

  private static IListenerListProfiler globalInstance;

  static {
    if (ConfigUtility.getPropertyBoolean(EnableListenerListProfiling.KEY, false)) {
      globalInstance = new DefaultListenerListProfiler();
    }
    else {
      globalInstance = new DisabledListenerListProfiler();
    }
  }

  ListenerListRegistry() {
    //singleton
  }

  public static IListenerListProfiler globalInstance() {
    return globalInstance;
  }

  /**
   * This method is intended for unit testing only
   */
  static void setGlobalInstance(IListenerListProfiler newInstance) {
    globalInstance = newInstance;
  }

  public static class EnableListenerListProfiling extends AbstractBooleanConfigProperty {
    protected static final String KEY = "org.eclipse.scout.rt.platform.management.EnableListenerListProfiling";

    @Override
    public String getKey() {
      return KEY;
    }

    @Override
    public String description() {
      return "Defines if all IListenerListWithManagement classes are monitored and exposed via JMX beans. "
          + "Enable this feature to detect memory issues or possible leaks, this may result in a global "
          + "performance decrease therefore this feature is disabled by default.";
    }
  }
}
