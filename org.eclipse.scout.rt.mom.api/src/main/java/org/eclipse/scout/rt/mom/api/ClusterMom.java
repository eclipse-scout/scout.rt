package org.eclipse.scout.rt.mom.api;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractMapConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message oriented middleware (MOM) for sending and receiving messages in the cluster.
 * <p>
 * By default, {@link ClusterMom} uses {@link ObjectMarshaller} to transport objects across the network.
 * <p>
 * See {@link IMom} for more information.
 *
 * @see IMom
 * @since 6.1
 */
public class ClusterMom extends AbstractMomDelegate implements IMomTransport {

  private static final Logger LOG = LoggerFactory.getLogger(ClusterMom.class);

  @Override
  protected IMom initDelegate() throws Exception {
    if (ClusterMomImplementorProperty.isNullImplementor()) {
      LOG.info("+++ Using '{}' for transport '{}'. No messages are published and received. To enable this transport, configure a MOM implementor with property '{}'.",
          NullMomImplementor.class.getSimpleName(),
          ClusterMom.class.getSimpleName(),
          BEANS.get(ClusterMomImplementorProperty.class).getKey());
      return BEANS.get(NullMomImplementor.class);
    }

    final IMomImplementor implementor = BEANS.get(CONFIG.getPropertyValue(ClusterMomImplementorProperty.class));
    implementor.init(lookupEnvironment());
    implementor.setDefaultMarshaller(BEANS.get(ObjectMarshaller.class));
    return implementor;
  }

  /**
   * Returns the configuration to connect to an environment.
   */
  protected Map<Object, Object> lookupEnvironment() {
    final ClusterMomEnvironmentProperty envProperty = BEANS.get(ClusterMomEnvironmentProperty.class);
    final Map<String, String> env = assertNotNull(envProperty.getValue(), "Missing configuration for {}: MOM environment not specified [config={}]", ClusterMom.class.getSimpleName(), envProperty.getKey());
    return new HashMap<Object, Object>(env);
  }

  /**
   * Specifies the MOM implementor.
   * <p>
   * Example to work with a JMS based implementor.
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

    /**
     * Returns <code>true</code> if no {@link IMomImplementor} is configured for {@link ClusterMom}.
     */
    public static boolean isNullImplementor() {
      final Class<? extends IMomImplementor> implementorClass = CONFIG.getPropertyValue(ClusterMomImplementorProperty.class);
      return implementorClass == null || implementorClass == NullMomImplementor.class;
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
   * scout.mom.cluster.environment=(scout.mom.name)->Scout Cluster MOM\
   *                               (scout.mom.connectionfactory.name)->ClusterMom\
   *                               (java.naming.factory.initial)->org.apache.activemq.jndi.ActiveMQInitialContextFactory\
   *                               <strong>(java.naming.provider.url)->failover:(peer://mom/cluster?persistent=false)\</strong>
   *                               (connectionFactoryNames)->ClusterMom
   * </pre>
   *
   * Example to start an embedded broker which accepts remote connections from other hosts:
   *
   * <pre>
   * scout.mom.cluster.environment=(scout.mom.name)->Scout Cluster MOM\
   *                               (scout.mom.connectionfactory.name)->ClusterMom\
   *                               (java.naming.factory.initial)->org.apache.activemq.jndi.ActiveMQInitialContextFactory\
   *                               <strong>(java.naming.provider.url)->vm:(broker:(tcp://0.0.0.0:5050)?persistent=false\</strong>
   *                               (connectionFactoryNames)->ClusterMom
   * </pre>
   *
   * Example to connect to a remote broker:
   *
   * <pre>
   * scout.mom.cluster.environment=(scout.mom.name)->Scout Cluster MOM\
   *                               (scout.mom.connectionfactory.name)->ClusterMom\
   *                               (java.naming.factory.initial)->org.apache.activemq.jndi.ActiveMQInitialContextFactory\
   *                               <strong>(java.naming.provider.url)->tcp://ip_of_broker:5050\</strong>
   *                               (connectionFactoryNames)->ClusterMom
   * </pre>
   */
  public static class ClusterMomEnvironmentProperty extends AbstractMapConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.cluster.environment";
    }
  }
}
