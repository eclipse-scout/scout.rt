package org.eclipse.scout.rt.shared.http.proxy;

import java.net.Proxy;
import java.net.URI;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.config.AbstractStringListConfigProperty;

/**
 * <p>
 * Configure a proxy for the {@link ConfigurableProxySelector}. If an {@link URI} matches the {@link Pattern} defined by
 * this property the {@link Proxy} defined after the equal sign will be used. Only valid values for this property are:
 * <i>REGEXP</i>=<i>PROXY_HOST</i>:<i>PROXY_PORT</i>.
 * </p>
 * <p>
 * This property is a list property, example valid configurations:
 * </p>
 *
 * <pre>
 * scout.http.proxy[0]=.*\\.?example.com(:\\d+)?$=127.0.0.1:8888
 * scout.http.proxy[1]=.*\\.?example.org(:\\d+)?$=proxy.company.com
 * </pre>
 */
public class ProxyConfigurationProperty extends AbstractStringListConfigProperty {

  @Override
  public String getKey() {
    return "scout.http.proxy";
  }

}
