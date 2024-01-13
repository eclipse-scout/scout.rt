/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

import jakarta.servlet.MultipartConfigElement;

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
      return "Contains a comma separated list of supported locales (e.g. en,en-US,de-CH). To support all locales, use the keyword 'all' instead.\n" +
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

  public static class MaxUploadFileCountProperty extends AbstractLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return 100L;
    }

    @Override
    public String description() {
      return "For security reasons, file upload is limited to a maximum number of file that can be processed at once.\n"
          + "By default this property is set to 100. A value of -1 means no limit";
    }

    @Override
    public String getKey() {
      return "scout.ui.maxUploadFileCount";
    }
  }

  /**
   * {@link MultipartConfigElement} for {@link UiServlet}.
   */
  public static class UiServletMultipartConfigProperty extends AbstractConfigProperty<MultipartConfigElement, Map<String, String>> {

    private static final String LOCATION = "location";
    private static final String MAX_FILE_SIZE = "maxFileSize";
    private static final String MAX_REQUEST_SIZE = "maxRequestSize";
    private static final String FILE_SIZE_THRESHOLD = "fileSizeThreshold";

    @Override
    public String getKey() {
      return "scout.uiServletMultipartConfig";
    }

    @Override
    public String description() {
      return String.format("Multipart configuration for inbound servlet.\n"
              + "Map property with the keys as follows:\n"
              + "- %s: the directory location where files will be stored temporarily (default: temp directory)\n"
              + "- %s: the maximum size allowed in MB for uploaded files (default: %d MB) \n"
              + "- %s: the maximum size allowed in MB for multipart/form-data requests (default: %d MB) \n"
              + "- %s: the size threshold in MB after which files will written to disk (default: %d MB) \n",
          LOCATION,
          MAX_FILE_SIZE, getDefaultMaxFileSizeMB(),
          MAX_REQUEST_SIZE, getDefaultMaxRequestSizeMB(),
          FILE_SIZE_THRESHOLD, getDefaultFileSizeThresholdMB());
    }

    @Override
    public Map<String, String> readFromSource(String namespace) {
      return ConfigUtility.getPropertyMap(getKey(), null, namespace);
    }

    @Override
    public MultipartConfigElement getDefaultValue() {
      return parse(Collections.emptyMap()); // defaults are on a per key base
    }

    protected String getDefaultLocation() {
      return System.getProperty("java.io.tmpdir");
    }

    protected long getDefaultMaxFileSizeMB() {
      return 50; // 50 MB
    }

    protected long getDefaultMaxRequestSizeMB() {
      return 100; // 100 MB
    }

    protected int getDefaultFileSizeThresholdMB() {
      return 20; // 20 MB
    }

    @Override
    protected MultipartConfigElement parse(Map<String, String> value) {
      Set<String> invalidMapKeys = new HashSet<>(value.keySet());
      Arrays.asList(LOCATION, MAX_FILE_SIZE, MAX_REQUEST_SIZE, FILE_SIZE_THRESHOLD).forEach(invalidMapKeys::remove);
      if (!invalidMapKeys.isEmpty()) {
        throw new PlatformException("Invalid values for map property {}: {}", getKey(), invalidMapKeys);
      }

      String location = ObjectUtility.nvl(StringUtility.nullIfEmpty(value.get(LOCATION)), getDefaultLocation());
      long maxFileSize = ObjectUtility.nvl(TypeCastUtility.castValue(value.get(MAX_FILE_SIZE), Long.class), getDefaultMaxFileSizeMB()) * 1024 * 1024;
      long maxRequestSize = ObjectUtility.nvl(TypeCastUtility.castValue(value.get(MAX_REQUEST_SIZE), Long.class), getDefaultMaxRequestSizeMB()) * 1024 * 1024;
      int fileSizeThreshold = ObjectUtility.nvl(TypeCastUtility.castValue(value.get(FILE_SIZE_THRESHOLD), Integer.class), getDefaultFileSizeThresholdMB()) * 1024 * 1024;
      return new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
    }
  }
}
