/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.ClientConfigProperties.JobCompletionDelayOnSessionShutdown;
import org.eclipse.scout.rt.client.session.ClientSessionStopHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.res.loader.HtmlDocumentParser;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

/**
 * This class provides all properties configured in the config.properties file that affect the HTML UI module.
 */
public final class UiHtmlConfigProperties {

  private UiHtmlConfigProperties() {
  }

  public static class UiThemeProperty extends AbstractStringConfigProperty {

    public static final String DEFAULT_THEME = "default";

    @Override
    public String getKey() {
      return "scout.ui.theme";
    }

    @Override
    public String description() {
      return "The name of the UI theme which is activated when the application starts.";
    }

    @Override
    public String getDefaultValue() {
      return DEFAULT_THEME;
    }
  }

  public static class UiPrebuildProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.ui.prebuild";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("When this property returns true, file pre-building is performed when the UI application server starts up.\n" +
          "This means the application start takes more time, but in return the first user request takes less time.\n" +
          "'File' in this context means web-resources like HTML, CSS and JS.\n" +
          "These files are typically processed by Scout's '%s' and '%s'.\n" +
          "By default this property is enabled when the application is not running in development mode (property '%s' is false).",
          ScriptProcessor.class.getSimpleName(), HtmlDocumentParser.class.getName(), BEANS.get(PlatformDevModeProperty.class).getKey());
    }

    @Override
    public Boolean getDefaultValue() {
      return !Platform.get().inDevelopmentMode();
    }
  }

  public static class UiPrebuildFilesProperty extends AbstractConfigProperty<List<String>, String> {

    @Override
    public String getKey() {
      return "scout.ui.prebuild.files";
    }

    @Override
    protected List<String> parse(String value) {
      String[] tokens = StringUtility.tokenize(value, ',');
      // Prevent accidental modification by returning an unmodifiable list because property is cached and always returns the same instance
      return unmodifiableList(asList(tokens));
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Contains a comma separated list of files in '/WebContent/res' that should be pre-built when the (UI) application server starts up.\n" +
          "Since it takes a while to build files, especially JS and CSS (LESS) files, we want to do this when the server starts. Otherwise its always the first user who must wait a long time until all files are built.\n" +
          "Since CSS and JS files are always referenced by a HTML file, we simply specify the main HTML files in this property.\n"
          + "By default no files are prebuild. This property only has an effect if property '%s' is enabled.", BEANS.get(UiPrebuildProperty.class).getKey());
    }

    @Override
    public List<String> getDefaultValue() {
      return emptyList();
    }
  }

  public static class UiLocalesProperty extends AbstractConfigProperty<List<String>, String> {

    @Override
    public String getKey() {
      return "scout.ui.locales";
    }

    @Override
    protected List<String> parse(String value) {
      String[] tokens = StringUtility.tokenize(value, ',');
      // Prevent accidental modification by returning an unmodifiable list because property is cached and always returns the same instance
      return unmodifiableList(Arrays.asList(tokens));
    }

    @Override
    public String description() {
      return "Contains a comma separated list of supported locales (e.g. en,en-US,de-CH).\n" +
          "This is only relevant if locales.json and texts.json should be sent to the client, which is not the case for remote apps. So this property is only used for JS only apps.\n"
          + "By default no locales are supported.";
    }

    @Override
    public List<String> getDefaultValue() {
      return Collections.emptyList();
    }
  }

  public static class MaxUserIdleTimeProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.HOURS.toSeconds(4);
    }

    @Override
    public String description() {
      return "If a user is inactive (no user action) for the specified number of seconds, the session is stopped and the user is logged out.\n"
          + "By default this property is set to 4 hours.";
    }

    @Override
    public String getKey() {
      return "scout.ui.maxUserIdleTime";
    }
  }

  public static class BackgroundPollingIntervalProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.SECONDS.toSeconds(60);
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("The polling request (which waits for a background job to complete) stays open until a background job has completed or the specified number of seconds elapsed.\n"
          + "This property must have a value between 3 and the value of property '%s'.\n"
          + "By default this property is set to 1 minute.", BEANS.get(MaxUserIdleTimeProperty.class).getKey());
    }

    @Override
    public String getKey() {
      return "scout.ui.backgroundPollingMaxWaitTime";
    }
  }

  public static class UiModelJobsAwaitTimeoutProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.HOURS.toSeconds(1);
    }

    @Override
    public String description() {
      return "The maximal timeout in seconds to wait for model jobs to complete during a UI request. After that timeout the model jobs will be aborted so that the request may return to the client.\n"
          + "By default this property is set to 1 hour.";
    }

    @Override
    public String getKey() {
      return "scout.ui.modelJobTimeout";
    }
  }

  public static class SessionStoreHousekeepingDelayProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.ui.sessionstore.housekeepingDelay";
    }

    @Override
    public String description() {
      return "Number of seconds before the housekeeping job starts after a UI session has been unregistered from the store.\n"
          + "By default this property is set to 30 seconds.";
    }

    @Override
    public Integer getDefaultValue() {
      return 30;
    }
  }

  /**
   * @deprecated since 6.1 not used anymore, see {@link ClientSessionStopHelper}
   */
  @Deprecated
  public static class SessionStoreHousekeepingMaxWaitShutdownProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.ui.sessionstore.housekeepingMaxWaitForShutdown";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Maximum time in seconds to wait for a client session to be stopped by the housekeeping job.\n" +
          "The value should be smaller than the session timeout (typically defined in the web.xml) and greater than the value of property '%s'\n"
          + "By default this property is set to 1 minute.", BEANS.get(JobCompletionDelayOnSessionShutdown.class).getKey());
    }

    @Override
    public Integer getDefaultValue() {
      return 60; // 1 minute
    }
  }

  /**
   * @deprecated since 6.1 not used anymore, see {@link ClientSessionStopHelper}
   */
  @Deprecated
  public static class SessionStoreMaxWaitWriteLockProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.ui.sessionStore.valueUnboundMaxWaitForWriteLock";
    }

    @Override
    public String description() {
      return "Maximum time in seconds to wait for the write lock when the session store is unbound from the HTTP session.\n"
          + "This value should not be too large because waiting on the lock might suspend background processes of the application server.\n"
          + "By default this property is set to 5 seconds.";
    }

    @Override
    public Integer getDefaultValue() {
      return 5;
    }
  }

  /**
   * @deprecated since 6.1 not used anymore, see {@link ClientSessionStopHelper}
   */
  @Deprecated
  public static class SessionStoreMaxWaitAllShutdownProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.ui.sessionStore.maxWaitForAllShutdown";
    }

    @Override
    public String description() {
      return "Maximum time in second to wait for all client sessions to be stopped after the HTTP session has become invalid.\n" +
          "After this number of seconds a 'leak detection' test is performed. You are advised to change this value only if your sessions need an unusual long time to shutdown.\n"
          + "By default this property is set to 1 minute.";
    }

    @Override
    public Integer getDefaultValue() {
      return 60; // 1 minute
    }
  }

  public static class ScriptfileBuildProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.dev.scriptfile.rebuild";
    }

    @Override
    public String description() {
      return "Specifies if scriptfiles should be rebuild on modification or with every request. False for rebuild on modifications. The default value is true.";
    }

    @Override
    protected Boolean parse(String value) {
      if (!Platform.get().inDevelopmentMode()) {
        return true;
      }
      // only works in development mode
      return super.parse(value);
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }

  public static class ScriptfileBuilderDevCacheKey extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.dev.scriptfile.persist.key";
    }

    @Override
    public String description() {
      return "Specifies a key to store the keys (HttpCacheKey) scripts in development cache. "
          + "The keys of the last application start in dev mode will be stored in the user.home/.eclipse/org.eclipse.scout.dev/scriptfile_cache_{key}.obj. "
          + "For the next run in dev mode the keys stored under this key are preloaded.";
    }
  }
}
