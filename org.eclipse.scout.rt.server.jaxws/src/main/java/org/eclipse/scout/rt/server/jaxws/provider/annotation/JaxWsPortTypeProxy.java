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
package org.eclipse.scout.rt.server.jaxws.provider.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.jws.WebService;
import javax.xml.ws.WebServiceClient;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.commons.authentication.ICredentialVerifier;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.jaxws.provider.auth.handler.AuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.IAuthenticationMethod;

/**
 * Annotate an interface with this annotation, if you like to generate a PortTypProxy for a webservice provider. A
 * PortTypeProxy implements all methods of a webservice and makes webservice requests to run on behalf of a
 * {@link ServerRunContext}, before being propagated to the Bean implementing the port type interface. Also,
 * installation of authentication and JAX-WS handlers is facilitated.
 * <p>
 * Any annotation added to the interface is added to the proxy as well. That also applies for {@link WebService}
 * annotation to overwrite values derived from WSDL. If you provide an explicit handler-chain binding file, handlers and
 * authentication declared on this annotation are ignored.
 * <p>
 * The binding to the concrete endpoint is done via {@link #endpointInterface()} attribute. If a WSDL declares multiple
 * services, create a separate decorator for each service to be published.
 * <p>
 * <strong>Example usage:</strong>
 *
 * <pre>
 * &#64;JaxWsPortTypeProxy(
 *     endpointInterface = TestWebServicePortType.class,
 *     serviceName = "TestWebService",
 *     portName = "TestWebServicePort",
 *     handlerChain = {
 *         &#64;Handler(@Clazz(Handler1.class)),
 *         &#64;Handler(@Clazz(qualifiedName = "f.q.n.Handler2")),
 *         &#64;Handler(value = @Clazz(Handler3.class) ,
 *             initParams = {@InitParam(key = "key1", value = "value1"),
 *                 &#64;InitParam(key = " key2", value = "value2")}),
 *     },
 *     authentication = @Authentication(
 *         method = @Clazz(BasicAuthenticationMethod.class) ,
 *         verifier = @Clazz(ConfigFileCredentialVerifier.class))
 * public interface TestWebServicePortTypeProxyDescriptor {
 * }
 * </pre>
 *
 * @since 5.1
 */
@Target(java.lang.annotation.ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JaxWsPortTypeProxy {

  public static final String DERIVED = "derived";

  /**
   * The endpoint interface which a proxy implementation should be generated for to facilitate registration of handlers,
   * installation of authentication, and to ensure web service requests to run in a proper {@link RunContext}.
   * <p>
   * An endpoint interface defines the service's abstract Web Service contract, and is also known as port type interface
   * and annotated with {@link WebService} annotation.
   * <p>
   * The proxy generated delegates to the implementation via bean manager, meaning that the implementation must be
   * annotated with {@link Bean} or {@link ApplicationScoped} annotation.
   */
  Class<?> endpointInterface();

  /**
   * The class name of the PortTypeProxy to be generated. If not set, the name is derived from the PortType interface
   * suffixed with 'Proxy'.
   */
  String portTypeProxyName() default JaxWsPortTypeProxy.DERIVED;

  /**
   * The service name as specified in the WSDL file, and must be set if publishing the webservice in a EE container.
   * Both, {@link #serviceName()} and {@link #portName()} uniquely identify a webservice endpoint to be published.
   *
   * <pre>
   * &lt;wsdl:service name="SERVICE_NAME">
   *  ...
   * &lt;/wsdl:service&gt
   * </pre>
   */
  String serviceName() default "";

  /**
   * The name of the port as specified in the WSDL file, and must be set if publishing the webservice in a EE container.
   * Both, {@link #serviceName()} and {@link #portName()} uniquely identify a webservice endpoint to be published.
   *
   * <pre>
   * &lt;wsdl:service name="...">
   *  &lt;wsdl:port name="PORT_NAME" binding="..."/&gt;
   * &lt;/wsdl:service&gt
   * </pre>
   */
  String portName() default "";

  /**
   * The location of the WSDL document. If not set, the location is derived from {@link WebServiceClient} annotation
   * which is typically initialized with the location provided to 'wsimport'.
   */
  String wsdlLocation() default JaxWsPortTypeProxy.DERIVED;

  /**
   * The authentication mechanism to be installed on the webservice endpoint, and to specify in which {@link RunContext}
   * to run authenticated webservice requests. By default, authentication is disabled. If <i>enabled</i>, an
   * {@link AuthenticationHandler} is generated at compile time (APT) and registered in the handler chain as very first
   * handler.
   * <ul>
   * <li>The {@link ICredentialVerifier} can be configured to be invoked in a {@link RunContext} by annotating it with
   * <code>@RunWithRunContext</code> annotation.</li>
   * <li>The {@link IAuthenticationMethod} and {@link ICredentialVerifier} must not be visible at compile-time, but can
   * be referenced with their qualified names instead.</li>
   * <li>If providing a handler binding file yourself, this annotation is ignored.</li>
   * </ul>
   */
  Authentication authentication() default @Authentication()
  ;

  /**
   * To specify JAX-WS handlers to be installed on the webservice endpoint in the order as being declared. Thereto, for
   * each handler, a proxy handler is created at compile-time and registered in 'handler-chain.xml'.
   * <ul>
   * <li>A handler can be configured to run in a {@link RunContext} by annotating it with
   * <code>@RunWithRunContext</code> annotation.</li>
   * <li>A handler can be instrumented with <i>init-params</i>.</li>
   * <li>A handler must not be visible at compile-time, but can be referenced with its qualified name instead.</li>
   * <li>At runtime, handlers are resolved by the {@link IPlatform}, meaning that a handler must be annotated with
   * <code>@ApplicationScoped</code></li>
   * <li>If providing a handler binding file yourself, this annotation is ignored.</li>
   * </ul>
   */
  Handler[] handlerChain() default {};
}
