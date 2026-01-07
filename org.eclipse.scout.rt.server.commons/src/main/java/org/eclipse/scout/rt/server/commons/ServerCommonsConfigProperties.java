/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons;

import static java.util.Arrays.asList;
import static java.util.Collections.*;

import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringListConfigProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.healthcheck.RemoteHealthChecker;

public final class ServerCommonsConfigProperties {

  private ServerCommonsConfigProperties() {
  }

  public static class UrlHintsEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return Platform.get().inDevelopmentMode();
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Enable or disable changing UrlHints using URL parameters in the browser address line.\n"
              + "By default has the same value as the config property '%s' meaning it is by default only enabled in development mode.",
          BEANS.get(PlatformDevModeProperty.class).getKey());
    }

    @Override
    public String getKey() {
      return "scout.urlHints.enabled";
    }
  }

  public static class RemoteHealthCheckUrlsProperty extends AbstractConfigProperty<List<String>, String> {

    @Override
    public String getKey() {
      return "scout.healthCheckRemoteUrls";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Comma separated list of URLs the '%s' should access.\n"
          + "By default no URLs are set.", RemoteHealthChecker.class.getSimpleName());
    }

    @Override
    protected List<String> parse(String value) {
      String[] tokens = StringUtility.tokenize(value, ',');
      // Prevent accidental modification by returning an unmodifiable list because property is cached and always returns the same instance
      return unmodifiableList(asList(tokens));
    }

    @Override
    public List<String> getDefaultValue() {
      return emptyList();
    }
  }

  public static class TrustedCertificatesProperty extends AbstractStringListConfigProperty {

    @Override
    public String getKey() {
      return "scout.trustedCertificates";
    }

    @Override
    public String description() {
      return "URIs to DER (Base64) encoded certificate files that should be trusted. The URI may refer to a local file or a resource on the classpath (use classpath: prefix). The default value is an empty list.";
    }
  }
}
