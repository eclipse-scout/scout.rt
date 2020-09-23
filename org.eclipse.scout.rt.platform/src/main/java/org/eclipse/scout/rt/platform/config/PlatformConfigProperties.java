/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.serialization.DefaultSerializerBlacklist;
import org.eclipse.scout.rt.platform.serialization.DefaultSerializerWhitelist;

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

  public static class JandexRebuildProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.jandex.rebuild";
    }

    @Override
    public String description() {
      return "Specifies if Jandex indexes should be rebuilt. Is only necessary to enable during development when the class files change often. The default value is false.";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
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

  /**
   * see {@link DefaultSerializerBlacklist}
   *
   * @since 11.0
   */
  public static class DefaultSerializerBlacklistAppendProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.serial.blacklistAppend";
    }

    @Override
    public String description() {
      return "Specifies a blacklist regex for fully qualified class names that are blocked in object serialization."
          + "\nThese rules are appended to the existing built-in blacklist (recommended)."
          + "\nComma separated list of regular expressions (regex).";
    }
  }

  /**
   * see {@link DefaultSerializerBlacklist}
   *
   * @since 11.0
   */
  public static class DefaultSerializerBlacklistReplaceProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.serial.blacklistReplace";
    }

    @Override
    public String description() {
      return "Specifies a blacklist regex for fully qualified class names that are blocked in object serialization."
          + "\nThese rules replace the existing built-in blacklist (not recommended)."
          + "\nComma separated list of regular expressions (regex).";
    }
  }

  /**
   * see {@link DefaultSerializerWhitelist}
   *
   * @since 11.0
   */
  public static class DefaultSerializerWhitelistProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.serial.whitelist";
    }

    @Override
    public String getDefaultValue() {
      return ".*";
    }

    @Override
    public String description() {
      return "Specifies the secure whitelist of fully qualified class names that are allowed to be used in object serialization."
          + "\nComma separated list of regular expressions (regex)."
          + "\nThe default regex .* is used for back compatibility only, this is unsafe."
          + "\nMake sure to define the property 'scout.serial.whitelist' in the config.properties";
    }
  }
}
