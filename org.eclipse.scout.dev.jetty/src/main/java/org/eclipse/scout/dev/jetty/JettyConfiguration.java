/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.dev.jetty;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPortConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;

public final class JettyConfiguration {

  private JettyConfiguration() {
  }

  public static class ScoutJettyPortProperty extends AbstractPortConfigProperty {

    @Override
    public String getKey() {
      return "scout.jetty.port";
    }

    @Override
    public Integer getDefaultValue() {
      return 8080;
    }

    @Override
    public String description() {
      return "The port under which the jetty will be running.";
    }
  }

  /**
   * @since 10.0
   */
  public static class ScoutJettyKeyStorePathProperty extends AbstractStringConfigProperty {
    @Override
    public String getKey() {
      return "scout.jetty.keyStorePath";
    }

    @Override
    public String description() {
      return "Setting this property enables the jetty https connector using this keystore. "
          + "For example 'classpath:/dev/my-https.jks' or 'file:///C:/Users/usr/Desktop/my-store.jks' or 'C:/Users/usr/Desktop/my-store.jks'.";
    }
  }

  /**
   * @since 24.1
   */
  public static class ScoutJettyUseTlsProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.jetty.useTls";
    }

    @Override
    public String description() {
      return "Specifies if the Jetty server should use TLS. If true, the server must either be started in development mode (then a new self-singed certificate is created automatically),"
          + "or a Java KeyStore must be configured using property '" + BEANS.get(ScoutJettyKeyStorePathProperty.class).getKey() + "'."
          + "By default this property is true, if a Java KeyStore has been specified.";
    }

    @Override
    public Boolean getDefaultValue() {
      return StringUtility.hasText(CONFIG.getPropertyValue(ScoutJettyKeyStorePathProperty.class));
    }
  }

  /**
   * @since 10.0
   */
  public static class ScoutJettyAutoCreateSelfSignedCertificateProperty extends AbstractStringConfigProperty {
    @Override
    public String getKey() {
      return "scout.jetty.autoCreateSelfSignedCertificate";
    }

    @Override
    public String description() {
      return "Specifies the X-500 name to use in the self-signed certificate when starting Jetty in development mode with TLS enabled."
          + "For example 'CN=my-host.my-domain.com,C=US,ST=CA,L=Sunnyvale,O=My Company Inc.'.\n"
          + "This property is only used in development mode and only if the property '" + BEANS.get(ScoutJettyUseTlsProperty.class).getKey() + "' is true "
          + "and no existing Java keystore is specified (property '" + BEANS.get(ScoutJettyKeyStorePathProperty.class).getKey() + "').";
    }
  }

  /**
   * @since 10.0
   */
  public static class ScoutJettyKeyStorePasswordProperty extends AbstractStringConfigProperty {
    @Override
    public String getKey() {
      return "scout.jetty.keyStorePassword";
    }

    @Override
    public String description() {
      return "Https keystore password. Supports obfuscated values prefixed with 'OBF:'.";
    }
  }

  /**
   * @since 10.0
   */
  public static class ScoutJettyPrivateKeyPasswordProperty extends AbstractStringConfigProperty {
    @Override
    public String getKey() {
      return "scout.jetty.privateKeyPassword";
    }

    @Override
    public String description() {
      return "The password (if any) for the specific key within the key store. Supports obfuscated values prefixed with 'OBF:'.";
    }
  }

  /**
   * @since 10.0
   */
  public static class ScoutJettyCertificateAliasProperty extends AbstractStringConfigProperty {
    @Override
    public String getKey() {
      return "scout.jetty.certificateAlias";
    }

    @Override
    public String description() {
      return "Https certificate alias of the key in the keystore to use.";
    }
  }
}
