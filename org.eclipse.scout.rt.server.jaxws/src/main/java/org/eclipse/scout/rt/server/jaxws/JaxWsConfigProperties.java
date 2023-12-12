/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws;

import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractSubjectConfigProperty;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsMetroSpecifics;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.BasicAuthenticationMethod;
import org.eclipse.scout.rt.server.jaxws.provider.handler.HandlerDelegate;

/**
 * 'config.properties' used in JAX-WS RT.
 */
public final class JaxWsConfigProperties {

  private JaxWsConfigProperties() {
  }

  public static class JaxWsAuthenticatorSubjectProperty extends AbstractSubjectConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.provider.user.authenticator";
    }

    @Override
    public String description() {
      return "Technical Subject used to authenticate webservice requests. The default value is 'jaxws-authenticator'.";
    }

    @Override
    public Subject getDefaultValue() {
      return convertToSubject("jaxws-authenticator");
    }
  }

  public static class JaxWsHandlerSubjectProperty extends AbstractSubjectConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.provider.user.handler";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Technical subject used to invoke JAX-WS handlers if the request is not authenticated yet; used by '%s'.\n"
          + "The default value is 'jaxws-handler'.", HandlerDelegate.class.getName());
    }

    @Override
    public Subject getDefaultValue() {
      return convertToSubject("jaxws-handler");
    }
  }

  public static class JaxWsLogHandlerDebugProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.loghandlerDebug";
    }

    @Override
    public String description() {
      return "Indicates whether to log SOAP messages in debug or info level. The default value is false.";
    }

    @Override
    public Boolean getDefaultValue() {
      return false;
    }
  }

  public static class JaxWsImplementorProperty extends AbstractClassConfigProperty<JaxWsImplementorSpecifics> {

    @Override
    public String getKey() {
      return "scout.jaxws.implementor";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Fully qualified class name of the JAX-WS implementor to use. The class must extend '%s'.\n"
          + "By default, JAX-WS Metro (not bundled with JRE) is used. For that to work, add the Maven dependency to JAX-WS Metro to your server application's pom.xml: com.sun.xml.ws:jaxws-rt:2.2.10.",
          JaxWsImplementorSpecifics.class.getName());
    }

    @Override
    public Class<? extends JaxWsImplementorSpecifics> getDefaultValue() {
      return JaxWsMetroSpecifics.class;
    }
  }

  public static class JaxWsBasicAuthRealmProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.provider.authentication.basicRealm";
    }

    @Override
    public String description() {
      return String.format("Security Realm used for Basic Authentication; used by '%s'. The default value is 'JAX-WS'.", BasicAuthenticationMethod.class.getName());
    }

    @Override
    public String getDefaultValue() {
      return "JAX-WS";
    }
  }

  public static class JaxWsPortPoolEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.consumer.portPoolEnabled";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("To indicate whether to pool webservice clients.\n"
          + "Creating new service and Port instances is expensive due to WSDL and schema validation. Using the pool helps to reduce these costs. The default value is true.\n"
          + "The pool size is unlimited but its elements are removed after a certain time (configurable)\n"
          + "If this value is true, the value of property '%s' has no effect.", BEANS.get(JaxWsPortCacheEnabledProperty.class).getKey());
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }

  public static class JaxWsPortCacheEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.consumer.portCache.enabled";
    }

    @Override
    public String description() {
      return "Indicates whether to use a preemptive port cache for webservice clients.\n"
          + "Depending on the implementor used, cached ports may increase performance, because port creation is an expensive operation due to WSDL and schema validation.\n"
          + "The cache is based on a 'corePoolSize', meaning that that number of ports is created on a preemptive basis. If more ports than that number is required, they are created on demand and also added to the cache until expired, which is useful at a high load.\n"
          + "The default value is true.";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }

  public static class JaxWsPortCacheCorePoolSizeProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.consumer.portCache.corePoolSize";
    }

    @Override
    public String description() {
      return "Number of ports to be preemptively cached to speed up webservice calls. The default value is 10.";
    }

    @Override
    public Integer getDefaultValue() {
      return 10;
    }
  }

  public static class JaxWsPortCacheTTLProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.consumer.portCache.ttl";
    }

    @Override
    public String description() {
      return String.format("Maximum time in seconds to retain ports in the cache if the value of '%s' is exceeded. That typically occurs at high load, or if '%s' is undersized. The default value is 15 minutes.",
          BEANS.get(JaxWsPortCacheCorePoolSizeProperty.class).getKey(), BEANS.get(JaxWsPortCacheCorePoolSizeProperty.class).getKey());
    }

    @Override
    public Long getDefaultValue() {
      return TimeUnit.MINUTES.toSeconds(15);
    }
  }

  public static class JaxWsConnectTimeoutProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.consumer.connectTimeout";
    }

    @Override
    public String description() {
      return "Connect timeout in milliseconds to abort a webservice request, if establishment of the connection takes longer than this timeout. A timeout of null means an infinite timeout. The default value is null.";
    }

    @Override
    public Integer getDefaultValue() {
      return null; // infinite timeout
    }
  }

  public static class JaxWsReadTimeoutProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.jaxws.consumer.readTimeout";
    }

    @Override
    public String description() {
      return "Read timeout in milliseconds to abort a webservice request, if it takes longer than this timeout for data to be available for read. A timeout of null means an infinite timeout. The default value is null.";
    }

    @Override
    public Integer getDefaultValue() {
      return null; // infinite timeout
    }
  }
}
