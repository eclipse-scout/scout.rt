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
package org.eclipse.scout.rt.shared;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.rt.platform.config.AbstractBinaryConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.shared.TierState.Tier;

/**
 *
 */
public final class SharedConfigProperties {

  private SharedConfigProperties() {
  }

  /**
   * Private key for digital signature of service tunnel requests
   */
  public static class AuthTokenPrivateKeyProperty extends AbstractBinaryConfigProperty {

    @Override
    public byte[] getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "scout.auth.privatekey";
    }
  }

  /**
   * public key for {@link org.eclipse.scout.rt.shared.IConfigIniConstants#PRIVATE_KEY}
   */
  public static class AuthTokenPublicKeyProperty extends AbstractBinaryConfigProperty {

    @Override
    public byte[] getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "scout.auth.publickey";
    }
  }

  /**
   * Time to Life for the authentication token in milliseconds. The default is 10 minutes.
   */
  public static class AuthTokenTimeToLifeProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return Long.valueOf(TimeUnit.MINUTES.toMillis(10));
    }

    @Override
    public String getKey() {
      return "scout.auth.token.ttl";
    }
  }

  public static class ServiceTunnelTargetUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return ConfigIniUtility.getProperty("server.url") /* legacy */;
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.servicetunnel.targetUrl";
    }
  }

  public static class CompressServiceTunnelRequestProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      // no default value. means the response decides
      return null;
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.serviceTunnel.compress";
    }
  }

  /**
   * can be one of the following values: frontend, backend, undefined
   */
  public static class TierProperty extends AbstractConfigProperty<Tier> {

    @Override
    public Tier getDefaultValue() {
      return parse(ConfigIniUtility.getProperty("scout.osgi.tier")); // legacy
    }

    @Override
    public String getKey() {
      return "scout.tier";
    }

    @Override
    protected IProcessingStatus getStatusRaw(String rawValue) {
      if (!StringUtility.hasText(rawValue)) {
        return ProcessingStatus.OK_STATUS;
      }

      Tier[] values = Tier.class.getEnumConstants();
      Object[] valueNames = new String[values.length];
      for (int i = 0; i < values.length; i++) {
        valueNames[i] = values[i].name();
      }

      if (CompareUtility.isOneOf(rawValue, valueNames)) {
        return ProcessingStatus.OK_STATUS;
      }

      return super.getStatusRaw(rawValue);
    }

    @Override
    protected Tier parse(String value) {
      if (!StringUtility.hasText(value)) {
        return null;
      }

      return Enum.valueOf(Tier.class, value);
    }
  }

  /**
   * specifies if debug output should be enabled for http requests. Default is <code>false</code>
   */
  public static class HttpClientDebugProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.client.http.debug";
    }
  }
}
