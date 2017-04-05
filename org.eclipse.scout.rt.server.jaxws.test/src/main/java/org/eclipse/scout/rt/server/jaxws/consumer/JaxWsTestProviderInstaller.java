package org.eclipse.scout.rt.server.jaxws.consumer;

import java.io.IOException;
import java.net.ServerSocket;

import javax.xml.ws.Endpoint;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates test JAX-WS providers and publishes them at local endpoints. A random free local port is used to support
 * concurrent executions of unit tests on the same machine.
 * <p>
 * This is an application scoped bean. Replace to register custom providers.
 * <p>
 * Usage: Call {@link #install()} in &#64;BeforeClass, {@link #uninstall()} in &#64;AfterClass.
 */
@ApplicationScoped
public class JaxWsTestProviderInstaller {

  private static final Logger LOG = LoggerFactory.getLogger(JaxWsTestProviderInstaller.class);

  protected Endpoint m_consumerEndpoint;
  protected Endpoint m_pingEndpoint;

  public void install() {
    // Find a random free local port
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
      socket.close();
    }
    catch (IOException e) {
      throw new ProcessingException("Error while obtaining free random local port", e);
    }

    // Build endpointUrls
    String consumerTestAddress = "http://localhost:" + port + "/WS/JaxWsConsumerTestService";
    String pingTestAddress = "http://localhost:" + port + "/WS/JaxWsPingTestService";

    // Publish endpoints
    m_consumerEndpoint = Endpoint.publish(consumerTestAddress, new JaxWsConsumerTestServiceProvider());
    LOG.info("Published {} on endpoint {}", JaxWsConsumerTestServiceProvider.class.getSimpleName(), consumerTestAddress);
    m_pingEndpoint = Endpoint.publish(pingTestAddress, new JaxWsPingTestServiceProvider());
    LOG.info("Published {} on endpoint {}", JaxWsPingTestServiceProvider.class.getSimpleName(), pingTestAddress);

    // Configure clients to use the corresponding endpointUrls (overrides values from WSDL)
    BEANS.get(JaxWsConsumerTestClient.class).setEndpointUrl(consumerTestAddress);
    BEANS.get(JaxWsPingTestClient.class).setEndpointUrl(pingTestAddress);
  }

  public void uninstall() {
    m_consumerEndpoint.stop();
    m_pingEndpoint.stop();
  }
}
