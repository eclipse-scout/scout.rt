/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.app;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.http.HttpCookie.SameSite;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPortConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * This class contains properties for the embedded Jetty server used as Scout application runtime. The string
 * {@code scout.app.} is used as prefix for all properties instead of {@code scout.application.} in order to distinguish
 * the configuration for the embedded Jetty server from the general application name and version property
 * (scout.application.name/.version).
 */
public final class ApplicationProperties {

  private ApplicationProperties() {
  }

  public static class ScoutApplicationConsoleInputHandlerEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.consoleInputHandlerEnabled";
    }

    @Override
    public Boolean getDefaultValue() {
      return Platform.get().inDevelopmentMode();
    }

    @Override
    public String description() {
      return "Specifies whether the application uses a console input handler or not. The default value is true for development, false otherwise.";
    }
  }

  public static class ScoutApplicationPortProperty extends AbstractPortConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.port";
    }

    @Override
    public Integer getDefaultValue() {
      return 8080;
    }

    @Override
    public String description() {
      return "The port under which the application will be running. Default value is " + getDefaultValue() + ".";
    }
  }

  public static class ScoutApplicationWebappDirectoryProperty extends AbstractConfigProperty<File, String> {

    public static final String DEFAULT_VALUE = "/src/main/webapp/";

    @Override
    public String getKey() {
      return "scout.app.webappDirectory";
    }

    @Override
    protected File parse(String value) {
      if (StringUtility.hasText(value)) {
        return new File(value);
      }
      return null;
    }

    @Override
    public File getDefaultValue() {
      return new File(Paths.get(".").toAbsolutePath().normalize().toFile(), DEFAULT_VALUE);
    }

    @Override
    public String description() {
      return "The folder to find the webapp resources. The default value is '" + DEFAULT_VALUE + "'";
    }
  }

  public static class ScoutApplicationContextPathProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.contextPath";
    }

    @Override
    protected String parse(String value) {
      if (StringUtility.hasText(value)) {
        if (!value.startsWith("/")) {
          value = "/" + value;
        }
        return value;
      }
      return null;
    }

    @Override
    public String getDefaultValue() {
      return "/";
    }

    @Override
    public String description() {
      return "The context path under which the application can be reached. The default value is / (i.e. root path).";
    }
  }

  public static class ScoutApplicationHttpSessionEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.httpSessionEnabled";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }

    @Override
    public String description() {
      return "Specifies whether the application uses HTTP session or not. The default value is true.";
    }
  }

  public static class ScoutApplicationSessionTimeoutProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.httpSessionTimeout";
    }

    @Override
    public Integer getDefaultValue() {
      // Do not use a short timeout in DEV mode to allow longer debugging sessions
      return (int) TimeUnit.MINUTES.toSeconds(Platform.get().inDevelopmentMode() ? 60 : 5);
    }

    @Override
    public String description() {
      return "The session timeout in seconds to use if HTTP sessions are enabled. The default value is 300 seconds (5 minutes) for non-development mode, 3600 seconds (60 minutes) for development mode.";
    }
  }

  public static class ScoutApplicationSessionCookieConfigHttpOnlyProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.sessionCookieConfigHttpOnly";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }

    @Override
    public String description() {
      return "Specifies whether the HTTP session cookie is HTTP only or not. The default value is true.";
    }
  }

  public static class ScoutApplicationSessionCookieConfigSecureProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.sessionCookieConfigSecure";
    }

    @Override
    public Boolean getDefaultValue() {
      return !Platform.get().inDevelopmentMode(); // except for dev mode, use secure = true
    }

    @Override
    public String description() {
      return "Specifies whether any session tracking cookies created by the web application will be marked as secure. "
          + "If true, the session tracking cookie will be marked as secure even if the request initiated the corresponding session using plain HTTP instead of HTTPS (e.g. Scout application behind reverse proxy terminating SSL). "
          + "If false, the session tracking cookie will only be marked as secure if the request initiated the corresponding session is secure (using HTTPS). "
          + "The default value is true for non-development mode.";
    }
  }

  public static class ScoutApplicationSessionCookieConfigSameSiteProperty extends AbstractConfigProperty<SameSite, String> {

    @Override
    public String getKey() {
      return "scout.app.sessionCookieConfigSameSite";
    }

    @Override
    public String description() {
      return "Specifies the SameSite attribute of the HTTP session cookie. Valid values are: "
          + "'" + SameSite.NONE.getAttributeValue() + "', "
          + "'" + SameSite.STRICT.getAttributeValue() + "' or "
          + "'" + SameSite.LAX.getAttributeValue() + "'. "
          + "Default value is '" + getDefaultValue().getAttributeValue() + "'.";
    }

    @Override
    public SameSite getDefaultValue() {
      return SameSite.LAX;
    }

    @Override
    protected SameSite parse(String value) {
      return Arrays.stream(SameSite.values())
          .filter(e -> value.equals(e.getAttributeValue()))
          .findFirst()
          .orElseThrow();
    }
  }

  public static class ScoutApplicationHttpRequestMaxHeaderSizeProperty extends AbstractIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.http.request.maxHeaderSize";
    }

    @Override
    public String description() {
      return "Specifies the maximum allowed size in bytes for a HTTP request header. The default value is 8 KB (see " + HttpConfiguration.class.getName() + ").";
    }
  }

  public static class ScoutApplicationJvmShutdownHookEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.jvmShutdownHookEnabled";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }

    @Override
    public String description() {
      return "Specifies if the JVM shutdown hook is registered. The default value is true.";
    }
  }

  /**
   * @since 24.1
   */
  public static class ScoutApplicationUseTlsProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.useTls";
    }

    @Override
    public String description() {
      return "Specifies if the Scout application should use TLS. If true, the server must either be started in development mode (then a new self-singed certificate is created automatically), "
          + "or a Java KeyStore must be configured using property '" + BEANS.get(ScoutApplicationKeyStorePathProperty.class).getKey() + "'. "
          + "By default this property is true, if a Java KeyStore has been specified.";
    }

    @Override
    public Boolean getDefaultValue() {
      return CONFIG.getPropertyValue(ScoutApplicationKeyStorePathProperty.class) != null;
    }
  }

  /**
   * @since 10.0
   */
  public static class ScoutApplicationKeyStorePathProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.keyStorePath";
    }

    @Override
    public String description() {
      return "Setting this property enables the HTTPS connector. The value of this property must point to the local key store.\n"
          + "Example: 'classpath:/dev/my-https.jks' or 'file:///C:/Users/usr/Desktop/my-store.jks' or 'C:/Users/usr/Desktop/my-store.jks'.";
    }
  }

  /**
   * @since 10.0
   */
  public static class ScoutApplicationAutoCreateSelfSignedCertificateProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.autoCreateSelfSignedCertificate";
    }

    @Override
    public String description() {
      return "Specifies the X-500 name to use in the self-signed certificate when starting Scout application in development mode with TLS enabled.\n"
          + "Example: 'CN=my-host.my-domain.com,C=US,ST=CA,L=Sunnyvale,O=My Company Inc.'.\n"
          + "This property is only used in development mode and only if the property '" + BEANS.get(ScoutApplicationUseTlsProperty.class).getKey() + "' is true "
          + "and no existing Java keystore is specified (property '" + BEANS.get(ScoutApplicationKeyStorePathProperty.class).getKey() + "').";
    }

    @Override
    public String getDefaultValue() {
      return "CN=localhost";
    }
  }

  public static class ScoutApplicationKeyStorePasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.keyStorePassword";
    }

    @Override
    public String description() {
      return "HTTPS keystore password. Supports obfuscated values prefixed with 'OBF:'.";
    }
  }

  public static class ScoutApplicationPrivateKeyPasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.privateKeyPassword";
    }

    @Override
    public String description() {
      return "The password (if any) for the specific key within the key store. Supports obfuscated values prefixed with 'OBF:'.";
    }
  }

  public static class ScoutApplicationCertificateAliasProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.app.certificateAlias";
    }

    @Override
    public String description() {
      return "HTTPS certificate alias of the key in the keystore to use.";
    }
  }
}
