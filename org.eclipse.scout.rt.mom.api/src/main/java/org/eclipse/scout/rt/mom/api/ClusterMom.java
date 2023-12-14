/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api;

import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractMapConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;

/**
 * Message oriented middleware (MOM) for sending and receiving messages in the cluster.
 * <p>
 * By default, {@link ClusterMom} uses {@link ObjectMarshaller} to transport objects across the network, because cluster
 * messages may contain arbitrary (serializable) Java objects.
 *
 * @see IMom
 * @since 6.1
 */
@CreateImmediately
public class ClusterMom extends AbstractMomTransport {

  @PostConstruct
  public void init() {
    // Trigger delegate initialization during platform startup for ClusterMom ("fail early")
    getDelegate();
  }

  @Override
  protected Class<? extends IMomImplementor> getConfiguredImplementor() {
    return CONFIG.getPropertyValue(ClusterMomImplementorProperty.class);
  }

  @Override
  protected Map<String, String> getConfiguredEnvironment() {
    return CONFIG.getPropertyValue(ClusterMomEnvironmentProperty.class);
  }

  @Override
  protected IMarshaller getConfiguredDefaultMarshaller() {
    return BEANS.get(ObjectMarshaller.class);
  }

  /**
   * Specifies the MOM implementor.
   * <p>
   * Example to work with a JMS based implementor:
   *
   * <pre>
   * scout.mom.cluster.implementor = org.eclipse.scout.rt.mom.jms.JmsMomImplementor
   * </pre>
   */
  public static class ClusterMomImplementorProperty extends AbstractClassConfigProperty<IMomImplementor> {

    @Override
    public String getKey() {
      return "scout.mom.cluster.implementor";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies the MOM implementor.\n"
          + "Example to work with a JMS based implementor:\n" +
          "%s=org.eclipse.scout.rt.mom.jms.JmsMomImplementor", getKey());
    }
  }

  /**
   * Contains the configuration to connect to the network or broker. This configuration is specific to the MOM
   * implementor as specified in {@link ClusterMomImplementorProperty}.
   * <p>
   * <strong>In the following there are some examples specific to ActiveMQ broker.</strong>
   * <p>
   * Example to connect to a peer based cluster, which is useful in development mode because there is no central broker:
   *
   * <pre>
   * scout.mom.cluster.environment[scout.mom.name]=Scout Cluster MOM
   * scout.mom.cluster.environment[scout.mom.connectionfactory.name]=ClusterMom
   * scout.mom.cluster.environment[java.naming.factory.initial]=org.apache.activemq.jndi.ActiveMQInitialContextFactory
   * <strong>scout.mom.cluster.environment[java.naming.provider.url]=failover:(peer://mom/cluster?persistent=false)</strong>
   * scout.mom.cluster.environment[connectionFactoryNames]=ClusterMom
   * </pre>
   *
   * Example to start an embedded broker which accepts remote connections from other hosts:
   *
   * <pre>
   * scout.mom.cluster.environment[scout.mom.name]=Scout Cluster MOM
   * scout.mom.cluster.environment[scout.mom.connectionfactory.name]=ClusterMom
   * scout.mom.cluster.environment[java.naming.factory.initial]=org.apache.activemq.jndi.ActiveMQInitialContextFactory
   * <strong>scout.mom.cluster.environment[java.naming.provider.url]=vm:(broker:(tcp://0.0.0.0:5050)?persistent=false</strong>
   * scout.mom.cluster.environment[connectionFactoryNames]=ClusterMom
   * </pre>
   *
   * Example to connect to a remote broker:
   *
   * <pre>
   * scout.mom.cluster.environment[scout.mom.name]=Scout Cluster MOM
   * scout.mom.cluster.environment[scout.mom.connectionfactory.name]=ClusterMom
   * scout.mom.cluster.environment[java.naming.factory.initial]=org.apache.activemq.jndi.ActiveMQInitialContextFactory
   * <strong>scout.mom.cluster.environment[java.naming.provider.url]=tcp://ip_of_broker:5050</strong>
   * scout.mom.cluster.environment[connectionFactoryNames]=ClusterMom
   * </pre>
   */
  public static class ClusterMomEnvironmentProperty extends AbstractMapConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.cluster.environment";
    }

    @Override
    public String description() {
      return "Contains the configuration to connect to the network or broker. This configuration is specific to the MOM implementor\n" +
          "Example to connect to a peer based cluster, which is useful in development mode because there is no central broker:\n" +
          "scout.mom.cluster.environment[scout.mom.name]=Scout Cluster MOM\n" +
          "scout.mom.cluster.environment[scout.mom.connectionfactory.name]=ClusterMom\n" +
          "scout.mom.cluster.environment[java.naming.factory.initial]=org.apache.activemq.jndi.ActiveMQInitialContextFactory\n" +
          "scout.mom.cluster.environment[java.naming.provider.url]=failover:(peer://mom/cluster?persistent=false)\n" +
          "scout.mom.cluster.environment[connectionFactoryNames]=ClusterMom";
    }
  }
}
