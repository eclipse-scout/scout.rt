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
package org.eclipse.scout.rt.shared;

import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBinaryConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractSubjectConfigProperty;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
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
          "Furthermore the CookieAccessController uses this private key to sign the cookie.\n" +
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

  public static class BackendUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return ConfigUtility.getProperty("server.url");//legacy
    }

    @Override
    public String getKey() {
      return "scout.backendUrl";
    }

    @Override
    public String description() {
      return "The URL of the scout backend server (without any servlets). E.g.: http://localhost:8080\n"
          + "By default this property is null.";
    }

    @Override
    protected String parse(String value) {
      if (value == null) {
        return null;
      }
      int i = value.lastIndexOf(ServiceTunnelTargetUrlProperty.PROCESS_SERVLET_MAPPING);
      if (i >= 0) {
        value = value.substring(0, i);
      }
      return super.parse(value);
    }
  }

  public static class ExternalBaseUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.externalBaseUrl";
    }

    @Override
    public String description() {
      return "Absolute URL to the deployed http(s):// base of the web-application. The URL should include proxies, redirects, etc.\n" +
          "Example: https://www.my-company.com/my-scout-application/.\n" +
          "This URL is used to replace '<scout:base />' tags.";
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

  public static class ServiceTunnelTargetUrlProperty extends AbstractStringConfigProperty {

    public static final String PROCESS_SERVLET_MAPPING = "/process";

    @Override
    public String getDefaultValue() {
      String backendUrl = BEANS.get(BackendUrlProperty.class).getValue();
      if (StringUtility.hasText(backendUrl)) {
        return backendUrl + PROCESS_SERVLET_MAPPING;
      }
      return null;
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies the URL to the ServiceTunnelServlet on the backend server.\n"
          + "By default this property points to the value of property '%s' with '%s' appended.", BEANS.get(BackendUrlProperty.class).getKey(), PROCESS_SERVLET_MAPPING);
    }

    @Override
    public String getKey() {
      return "scout.servicetunnel.targetUrl";
    }
  }

  public static class CompressServiceTunnelRequestProperty extends AbstractBooleanConfigProperty {

    @Override
    @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
    public Boolean getDefaultValue() {
      // no default value. means the response decides
      return null;
    }

    @Override
    public String description() {
      return "Specifies if the service tunnel should compress the data. If null, the response decides which is default to true.";
    }

    @Override
    public String getKey() {
      return "scout.servicetunnel.compress";
    }
  }

  public static class CreateTunnelToServerBeansProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.createTunnelToServerBeans";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies if the Scout platform should create proxy beans for interfaces annotated with '%s'. Calls to beans of such types are then tunneled to the Scout backend.\n"
          + "By default this property is enabled if the property '%s' is set.", TunnelToServer.class.getSimpleName(), BEANS.get(ServiceTunnelTargetUrlProperty.class).getKey());
    }

    @Override
    public Boolean getDefaultValue() {
      // if no backend url is set proxy instances will not be created by default
      return StringUtility.hasText(BEANS.get(ServiceTunnelTargetUrlProperty.class).getValue());
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

  public static class PermissionLevelCheckCacheTimeToLiveProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.permissionLevelCacheTtl";
    }

    @Override
    public String description() {
      return "Time to live for level permission check caching in milliseconds.\n" +
          "If calculating the permission level for a permission instance, it can be internally cached. This caching is typically useful in a client and should be relative small (few minutes). If no value is set, caching is disabled.\n" +
          "As default, no time to live is set and therefore caching is disabled.";
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
