/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html;

import static java.util.Collections.unmodifiableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.util.StringUtility;

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
}
