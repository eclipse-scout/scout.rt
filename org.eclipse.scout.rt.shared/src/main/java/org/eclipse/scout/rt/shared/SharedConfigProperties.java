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

import javax.security.auth.Subject;

import org.eclipse.scout.commons.ConfigUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBinaryConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractSubjectConfigProperty;
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
    public String getKey() {
      return "scout.auth.privatekey";
    }
  }

  /**
   * public key for {@link AuthTokenPrivateKeyProperty}
   */
  public static class AuthTokenPublicKeyProperty extends AbstractBinaryConfigProperty {

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
    protected Long getDefaultValue() {
      return Long.valueOf(TimeUnit.MINUTES.toMillis(10));
    }

    @Override
    public String getKey() {
      return "scout.auth.token.ttl";
    }
  }

  public static class BackendUrlProperty extends AbstractStringConfigProperty {

    @Override
    protected String getDefaultValue() {
      return ConfigUtility.getProperty("server.url");//legacy
    }

    @Override
    public String getKey() {
      return "scout.server.url";
    }

    @Override
    protected String parse(String value) {
      if (value == null) {
        return null;
      }
      int i = value.lastIndexOf("/process");
      if (i >= 0) {
        value = value.substring(0, i);
      }
      return super.parse(value);
    }

  }

  public static class ServiceTunnelTargetUrlProperty extends AbstractStringConfigProperty {

    @Override
    protected String getDefaultValue() {
      String backendUrl = BEANS.get(BackendUrlProperty.class).getValue();
      if (StringUtility.hasText(backendUrl)) {
        return backendUrl + "/process";
      }
      return null;
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.servicetunnel.targetUrl";
    }
  }

  public static class CompressServiceTunnelRequestProperty extends AbstractBooleanConfigProperty {

    @Override
    protected Boolean getDefaultValue() {
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
    protected Tier getDefaultValue() {
      return parse(ConfigUtility.getProperty("scout.osgi.tier")); // legacy
    }

    @Override
    public String getKey() {
      return "scout.tier";
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
   * Property to specify if remote proxy beans should be created for interfaces annotated with {@link TunnelToServer}.
   * Default is <code>true</code>.
   */
  public static class CreateTunnelToServerBeansProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.beans.createTunnelToServerBeans";
    }

    @Override
    protected Boolean createValue() {
      // if no backend url is set proxy instances will not be created
      if (StringUtility.isNullOrEmpty(BEANS.get(ServiceTunnelTargetUrlProperty.class).getValue())) {
        return Boolean.FALSE;
      }
      return super.createValue();
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }

  /**
   * Technical {@link Subject} used to authenticate notification requests.
   */
  public static class NotificationSubjectProperty extends AbstractSubjectConfigProperty {

    @Override
    public String getKey() {
      return "notification.user.authenticator";
    }

    @Override
    protected Subject getDefaultValue() {
//TODO jgu
//      return convertToSubject("notification-authenticator");
      return convertToSubject("system");
    }
  }
}
