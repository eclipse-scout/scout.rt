package org.eclipse.scout.rt.mom.api;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractMapConfigProperty;

/**
 * Message oriented middleware (MOM) for sending and receiving messages in the cluster. See {@link IMom} for more
 * information.
 * <p>
 * Example <i>config.properties</i> of {@link ClusterMom} based on JMS with ActiveMQ as implementor.
 *
 * <pre>
 * scout.mom.cluster.implementor=org.eclipse.scout.rt.mom.jms.JmsMom
 * scout.mom.cluster.environment=(scout.mom.name)->Scout Cluster MOM\
 *                               (scout.mom.connectionfactory.name)->ClusterMom\
 *                               (java.naming.factory.initial)->org.apache.activemq.jndi.ActiveMQInitialContextFactory\
 *                               (java.naming.provider.url)->failover:(peer://mom/cluster?persistent=false)\
 *                               (connectionFactoryNames)->ClusterMom
 * </pre>
 *
 * @see IMom
 * @since 6.1
 */
@ApplicationScoped
public class ClusterMom extends MomDelegate {

  @Override
  protected IMom initDelegate() throws Exception {
    final ClusterMomImplementorProperty implementorProperty = BEANS.get(ClusterMomImplementorProperty.class);
    final Class<? extends IMomImplementor> implementorClazz = assertNotNull(implementorProperty.getValue(), "Missing configuration for {}: MOM implementor not specified [config={}]", ClusterMom.class.getSimpleName(), implementorProperty.getKey());
    final IMomImplementor momImplementor = BEANS.get(implementorClazz);
    momImplementor.init(lookupEnvironment());
    return momImplementor;
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
   * scout.mom.cluster.implementor = org.eclipse.scout.rt.mom.api.jms.JmsMom
   * </pre>
   */
  public static class ClusterMomImplementorProperty extends AbstractClassConfigProperty<IMomImplementor> {

    @Override
    public String getKey() {
      return "scout.mom.cluster.implementor";
    }
  }

  /**
   * Configures the MOM connector and is specific to {@link ClusterMomImplementorProperty}.
   * <p>
   * Example to connect to ActiveMQ broker via <code>JmsMom</code>.
   *
   * <pre>
   * scout.mom.cluster.environment=(scout.mom.name)->Scout Cluster MOM\
   *                               (scout.mom.connectionfactory.name)->ClusterMom\
   *                               (java.naming.factory.initial)->org.apache.activemq.jndi.ActiveMQInitialContextFactory\
   *                               (java.naming.provider.url)->failover:(peer://mom/cluster?persistent=false)\
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
