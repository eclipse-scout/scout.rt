/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBinaryConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractSubjectConfigProperty;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

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
   * Time to Live for the authentication token in milliseconds. The default is 10 minutes.
   */
  public static class AuthTokenTimeToLiveProperty extends AbstractPositiveLongConfigProperty {

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

  /**
   * Absolute URL to the deployed http(s):// base of the web-application. The expected 'external' URL should include
   * proxies, redirects, etc. Example: <code>https://www.bsi-software.com/bsi-crm/</code>.
   */
  public static class ExternalBaseUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.external.base.url";
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

  /**
   * Property representing the service tunnel URL.
   * <p>
   * This property is based on convention over configuration, meaning that without an explicit configuration, the URL
   * points to '{@link BackendUrlProperty}/process'.
   */
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
   * Property to specify if remote proxy beans should be created for interfaces annotated with {@link TunnelToServer}.
   * Default is <code>true</code>.
   */
  public static class CreateTunnelToServerBeansProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.beans.createTunnelToServerBeans";
    }

    @Override
    public Boolean getDefaultValue() {
      // if no backend url is set proxy instances will not be created by default
      return Boolean.valueOf(StringUtility.hasText(BEANS.get(ServiceTunnelTargetUrlProperty.class).getValue()));
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
      return convertToSubject("notification-authenticator");
    }
  }

  /**
   * Time to Live for level permission check caching in milliseconds.
   * <p>
   * If calculating the permission level for a permission instance, it can be internally be cached. This caching is
   * typically useful in a client and should be relative small (few minutes). If no value is set, no caching at all is
   * used. As default, no time to live is set and therefore caching is disabled. Currently this property is only used in
   * {@link BasicHierarchyPermission}.
   */
  public static class PermissionLevelCheckCacheTimeToLiveProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.permission.level.check.cache.ttl";
    }
  }
}
