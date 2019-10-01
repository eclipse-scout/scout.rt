/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.config;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.inventory.internal.JandexInventoryBuilder.RebuildStrategy;

public final class PlatformConfigProperties {

  private PlatformConfigProperties() {
  }

  public static class PlatformDevModeProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.devMode";
    }

    @Override
    public String description() {
      return "Property to specify if the application is running in development mode. Default is false.";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }
  }

  public static class ApplicationVersionProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.application.version";
    }

    @Override
    public String description() {
      return "The application version as displayed to the user. Used e.g. in the info form and the diagnostic views. The default value is '0.0.0'.";
    }

    @Override
    public String getDefaultValue() {
      return "0.0.0";
    }
  }

  public static class ApplicationNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.application.name";
    }

    @Override
    public String description() {
      return "The display name of the application. Used e.g. in the info form and the diagnostic views. The default value is 'unknown'.";
    }

    @Override
    public String getDefaultValue() {
      return "unknown";
    }
  }

  public static class JandexRebuildProperty extends AbstractConfigProperty<RebuildStrategy, String> {

    @Override
    public String getKey() {
      return "scout.jandex.rebuild";
    }

    @Override
    public String description() {
      return "Specifies if Jandex indexes should be rebuilt. Is only necessary to enable during development when the class files change often. The default value is false.";
    }

    @Override
    public RebuildStrategy getDefaultValue() {
      return parse(Boolean.FALSE.toString());
    }

    @Override
    protected RebuildStrategy parse(String value) {
      if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
        return RebuildStrategy.ALWAYS;
      }
      if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
        // do not use the CONFIG class here because the platform is not ready yet
        return new PlatformDevModeProperty().getValue() ? RebuildStrategy.IF_MODIFIED : RebuildStrategy.IF_MISSING;
      }
      // throws IllegalArgumentException when missing
      return RebuildStrategy.valueOf(value);
    }
  }

  public static class JobManagerCorePoolSizeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.jobmanager.corePoolSize";
    }

    @Override
    public String description() {
      return "The number of threads to keep in the pool, even if they are idle. The default value is 25.";
    }

    @Override
    public Integer getDefaultValue() {
      return 25;
    }
  }

  public static class JobManagerPrestartCoreThreadsProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.jobmanager.prestartCoreThreads";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies whether all threads of the core-pool should be started upon job manager startup, so that they are idle waiting for work.\n"
          + "By default this is disabled in development mode (property '%s' is true) and enabled otherwise.", BEANS.get(PlatformDevModeProperty.class).getKey());
    }

    @Override
    public Boolean getDefaultValue() {
      return !Platform.get().inDevelopmentMode();
    }
  }

  public static class JobManagerMaximumPoolSizeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.jobmanager.maximumPoolSize";
    }

    @Override
    public String description() {
      return String.format("The maximal number of threads to be created once the value of '%s' is exceeded. The default value is unlimited (which means limited by the resources of the machine).",
          BEANS.get(JobManagerCorePoolSizeProperty.class).getKey());
    }

    @Override
    public Integer getDefaultValue() {
      return Integer.MAX_VALUE;
    }
  }

  public static class JobManagerKeepAliveTimeProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.jobmanager.keepAliveTime";
    }

    @Override
    public String description() {
      return String.format("The time limit (in seconds) for which threads, which are created upon exceeding the '%s' limit, may remain idle before being terminated. The default value is 1 minute.",
          BEANS.get(JobManagerCorePoolSizeProperty.class).getKey());
    }

    @Override
    public Long getDefaultValue() {
      return 60L;
    }
  }

  public static class JobManagerAllowCoreThreadTimeoutProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.jobmanager.allowCoreThreadTimeOut";
    }

    @Override
    public String description() {
      return String.format("Specifies whether threads of the core-pool should be terminated after being idle for longer than the value of property '%s'. The defautl value is false.",
          BEANS.get(JobManagerKeepAliveTimeProperty.class).getKey());
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }
  }
}
