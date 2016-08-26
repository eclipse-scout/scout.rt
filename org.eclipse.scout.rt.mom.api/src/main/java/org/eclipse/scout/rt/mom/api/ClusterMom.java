package org.eclipse.scout.rt.mom.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractMapConfigProperty;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Message oriented middleware (MOM) for sending and receiving messages in the cluster. See {@link IMom} for more
 * information.
 * <p>
 * Example <i>config.properties</i> of {@link ClusterMom} based on JMS with ActiveMQ as implementor.
 *
 * <pre>
 * scout.mom.cluster.implementor=org.eclipse.scout.rt.mom.jms.JmsMom
 * scout.mom.cluster.environment=(java.naming.factory.initial)->org.apache.activemq.jndi.ActiveMQInitialContextFactory\
 *                               (java.naming.provider.url)->failover:(peer://mom/cluster?persistent=false)\
 *                               (connectionFactoryNames)->ClusterMom\
 *                               (scout.naming.mom.factory.connection)->ClusterMom
 * </pre>
 *
 * @see IMom
 * @since 6.1
 */
@ApplicationScoped
public class ClusterMom extends AbstractMomDelegate implements IMom {

  @Override
  protected IMom initDelegate() throws Exception {
    // Obtain the MOM implementor.
    final ClusterMomImplementorProperty implementorClazz = BEANS.get(ClusterMomImplementorProperty.class);
    final IMom mom = BEANS.get(Assertions.assertNotNull(implementorClazz.getValue(), "MOM implementor for {} not specified [config={}]", ClusterMom.class.getSimpleName(), implementorClazz.getKey()));

    // Configure the MOM.
    ((IMomInitializer) mom).init(initEnvironment());
    return mom;
  }

  /**
   * Returns the configuration to connect to an environment.
   */
  protected Map<Object, Object> initEnvironment() {
    final ClusterMomEnvironmentProperty config = BEANS.get(ClusterMomEnvironmentProperty.class);
    return new HashMap<Object, Object>(Assertions.assertNotNull(config.getValue(), "Missing configuration for {} [config={}]", ClusterMom.class.getSimpleName(), config.getKey()));
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
  public static class ClusterMomImplementorProperty extends AbstractClassConfigProperty<IMom> {

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
   * scout.mom.cluster.implementor = org.eclipse.scout.rt.mom.api.jms.JmsMom
   * scout.mom.cluster.environment=(java.naming.factory.initial)->org.apache.activemq.jndi.ActiveMQInitialContextFactory\
   *                              (java.naming.provider.url)->failover:(peer://mom/cluster?persistent=false)\
   *                              (connectionFactoryNames)->ClusterMom\
   *                              (x-scout.naming.mom.factory.connection)->ClusterMom
   * </pre>
   */
  public static class ClusterMomEnvironmentProperty extends AbstractMapConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.cluster.environment";
    }
  }
}
