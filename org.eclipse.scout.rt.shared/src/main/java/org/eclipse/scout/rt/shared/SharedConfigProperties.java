/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared;

import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.AbstractBinaryConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractSubjectConfigProperty;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.text.dev.TextKeyTextProviderService;

public final class SharedConfigProperties {

  private SharedConfigProperties() {
  }

  public static class AuthTokenPrivateKeyProperty extends AbstractBinaryConfigProperty {

    @Override
    public String getKey() {
      return "scout.auth.privateKey";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies the Base64 encoded private key for signing requests from the UI server to the backend server. By validating the signature the server can ensure the request is trustworthy.\n" +
          "Furthermore, the CookieAccessController uses this private key to sign the cookie.\n" +
          "New public-private-key-pairs can be created by invoking the class '%s' on the command line.", SecurityUtility.class.getName());
    }
  }

  public static class AuthTokenPublicKeyProperty extends AbstractBinaryConfigProperty {

    @Override
    public String getKey() {
      return "scout.auth.publicKey";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies the Base64 encoded public key used to validate signed requests on the backend server. The public key must match the private key stored in the property '%s' on the UI server.\n" +
          "New public-private-key-pairs can be created by invoking the class '%s' on the command line.", BEANS.get(AuthTokenPrivateKeyProperty.class).getKey(), SecurityUtility.class.getName());
    }
  }

  public static class LoadWebResourcesFromFilesystemConfigProperty extends AbstractBooleanConfigProperty {
    @Override
    public Boolean getDefaultValue() {
      return Platform.get().inDevelopmentMode();
    }

    @Override
    public String getKey() {
      return "scout.loadWebResourcesFromFilesystem";
    }

    @Override
    public String description() {
      return "Specifies if the application should look for web resources (like .js, .html or .css) on the local filesystem. " +
          "If true, the resources will be searched in modules that follow the Scout naming conventions (e.g. name.ui.app.dev, name.ui.app, name.ui) on the local filesystem first and (if not found) on the classpath second. " +
          "If false, the resources are searched on the Java classpath only. " +
          "By default this property is true in dev mode and false otherwise.";
    }
  }

  public static class AuthTokenTimeToLiveProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.MINUTES.toMillis(10);
    }

    @Override
    public String description() {
      return "Number of milliseconds a signature on a request from the UI server to the backend server is valid (TTL for the authentication token). If a request is not received within this time, it is rejected.\n"
          + "By default this property is set to 10 minutes.";
    }

    @Override
    public String getKey() {
      return "scout.auth.tokenTtl";
    }
  }


  public static class ExternalBaseUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.externalBaseUrl";
    }

    @Override
    public String description() {
      return String.format("Absolute URL to the deployed http(s):// base of the web-application. The URL should include proxies, redirects, etc.\n" +
          "Example: %s=https://www.my-company.com/my-scout-application/.\n" +
          "This URL is used to replace '<scout:base />' tags.", getKey());
    }

    @Override
    protected String parse(String value) {
      if (StringUtility.hasText(value)) {
        if (!value.endsWith("/")) {
          value += "/";
        }
        return value;
      }
      return null;
    }
  }

  public static class NotificationSubjectProperty extends AbstractSubjectConfigProperty {

    public static final String NOTIFICATION_AUTHENTICATOR_SUBJECT_NAME = "notification-authenticator";

    @Override
    public String getKey() {
      return "scout.client.notificationSubject";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Technical subject under which received client notifications are executed.\n"
          + "By default '%s' is used.", NOTIFICATION_AUTHENTICATOR_SUBJECT_NAME);
    }

    @Override
    public Subject getDefaultValue() {
      return convertToSubject(NOTIFICATION_AUTHENTICATOR_SUBJECT_NAME);
    }
  }

  public static class DevTextProvidersShowKeysProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.texts.showKeys";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format(
          "If this property is set to true, the '%s' will be registered with high priority, and each call to %s.get() will return the given text key instead of the translation.\n"
              + "This is useful for debug/testing purposes or exporting forms to JSON.\n"
              + "By default this property is false.",
          TextKeyTextProviderService.class.getSimpleName(), TEXTS.class.getSimpleName());
    }

    @Override
    public Boolean getDefaultValue() {
      return false;
    }
  }
}
