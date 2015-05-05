/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;


/**
 *
 */
public final class PlatformConfigProperties {

  private PlatformConfigProperties() {
  }

  /**
   * Property to specify if the application is running in development mode. Default is <code>false</code>.
   */
  public static class PlatformDevModeProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }

    @Override
    public String getKey() {
      return "scout.dev.mode";
    }
  }

  public static class ApplicationVersionProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return "0.0.0";
    }

    @Override
    public String getKey() {
      return "scout.application.version";
    }
  }

  public static class ApplicationNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return "unknown";
    }

    @Override
    public String getKey() {
      return "scout.application.name";
    }
  }

  /**
   * Specifies if jandex indexes should be rebuilt.
   */
  public static class JandexRebuildProperty extends AbstractBooleanConfigProperty {

    public static final String JANDEX_REBUILD_PROPERTY_NAME = "jandex.rebuild";

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }

    @Override
    public String getKey() {
      return JANDEX_REBUILD_PROPERTY_NAME;
    }
  }

  /**
   * The number of threads to keep in the pool, even if they are idle
   */
  public static class JobCorePoolSizeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return Integer.valueOf(10);
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.job.corePoolSize";
    }
  }

  /**
   * The maximal number of threads to be created once the core-pool-size is exceeded.
   */
  public static class JobMaximumPoolSizeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return Integer.MAX_VALUE;
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.job.maximumPoolSize";
    }
  }

  /**
   * The time limit (in seconds) for which threads, which are created upon exceeding the 'core-pool-size' limit, may
   * remain idle
   * before being terminated.
   */
  public static class JobKeepAliveTimeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return Integer.valueOf(60);
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.job.keepAliveTime";
    }
  }

  /**
   * Specifies whether threads of the core-pool should be terminated after being idle for longer than 'keepAliveTime'.
   */
  public static class JobAllowCoreThreadTimeoutProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.job.allowCoreThreadTimeOut";
    }
  }

  /**
   * The number of dispatcher threads to be used to dispatch delayed jobs, meaning jobs scheduled with a delay or
   * periodic jobs.
   */
  public static class JobDispatcherThreadCountProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return Integer.valueOf(1);
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.job.dispatcherThreadCount";
    }
  }
}
