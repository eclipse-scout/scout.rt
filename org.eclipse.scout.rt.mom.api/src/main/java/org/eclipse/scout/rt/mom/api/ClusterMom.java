package org.eclipse.scout.rt.mom.api;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractMapConfigProperty;

/**
 * Message oriented middleware (MOM) for sending and receiving messages in the cluster.
 * <p>
 * See {@link IMom} for more information.
 *
 * @see IMom
 * @since 6.1
 */
public class ClusterMom extends MomDelegate implements IMomTransport {

  @Override
  protected IMom initDelegate() throws Exception {
    final Class<? extends IMomImplementor> implementorClass = BEANS.get(ClusterMomImplementorProperty.class).getValue();
    if (implementorClass == null) {
      fail("Missing configuration for {}: MOM implementor not specified [config={}]", ClusterMom.class.getSimpleName(), BEANS.get(ClusterMomImplementorProperty.class).getKey());
    }

    final IMomImplementor implementor = BEANS.get(implementorClass);
    implementor.init(lookupEnvironment());
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
   * scout.mom.cluster.implementor = org.eclipse.scout.rt.mom.jms.JmsMom
   * </pre>
   */
  public static class ClusterMomImplementorProperty extends AbstractClassConfigProperty<IMomImplementor> {

    @Override
    public String getKey() {
      return "scout.mom.cluster.implementor";
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
