/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.dev.jetty;

import org.eclipse.scout.rt.platform.config.AbstractPortConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

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
      return "Setting this property enables the jetty https connector. For example 'file:/dev/my-https.jks'";
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
      return "Https private key password. Supports obfuscated values prefixed with 'OBF:'.";
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
      return "Https certificate alias in keystore.";
    }
  }
}
