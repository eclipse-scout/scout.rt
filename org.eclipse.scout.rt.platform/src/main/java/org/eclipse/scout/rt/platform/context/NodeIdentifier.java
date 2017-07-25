package org.eclipse.scout.rt.platform.context;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the current node's identification.
 *
 * @see 6.1
 */
@ApplicationScoped
public class NodeIdentifier {

  private static final Logger LOG = LoggerFactory.getLogger(NodeIdentifier.class);
  private String m_nodeId;

  @PostConstruct
  public void postConstruct() {
    m_nodeId = compute();
  }

  /**
   * Returns the identifier of the current node.
   */
  public String get() {
    return m_nodeId;
  }

  /**
   * Computes the current node identifier.
   */
  protected String compute() {
    // Check system property defined node id
    String nodeId = CONFIG.getPropertyValue(NodeIdProperty.class);
    if (StringUtility.hasText(nodeId)) {
      return nodeId;
    }

    // Check for WebLogic name
    nodeId = System.getProperty("weblogic.Name");
    if (StringUtility.hasText(nodeId)) {
      return nodeId;
    }

    // Check for JBoss node name
    nodeId = System.getProperty("jboss.node.name");
    if (StringUtility.hasText(nodeId)) {
      return nodeId;
    }

    // Use host name
    String hostname = null;
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    }
    catch (final UnknownHostException e) { // NOSONAR
      LOG.debug("Failed to resolve hostname", e);
    }

    if (StringUtility.isNullOrEmpty(hostname) || "localhost".equalsIgnoreCase(hostname)) {
      return UUID.randomUUID().toString(); // use random number
    }

    // In development mode there might be running multiple instances on different ports.
    // Therefore, we use the Jetty port as well.
    if (Platform.get().inDevelopmentMode()) {
      return StringUtility.join(":", hostname, ConfigUtility.getProperty("scout.jetty.port"));
    }

    return StringUtility.join(":", hostname, 8080);
  }

  /**
   * Represents the config-property to configure a node identifier.
   */
  public static class NodeIdProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.node.id";
    }
  }
}
