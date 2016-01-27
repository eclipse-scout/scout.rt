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
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunWithRunContext;
import org.eclipse.scout.rt.server.commons.authentication.ICredentialVerifier;
import org.eclipse.scout.rt.server.jaxws.provider.auth.handler.AuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.IAuthenticationMethod;

/**
 * Use this annotation to generate an <em>entry point port type</em> for an <em>endpoint interface</em>. Like the
 * implementing port type, an entry point implements all methods of the endpoint interface, and is to be published
 * instead of the implementing port type. However, the entry point simply intercepts webservice requests, but does not
 * answer them, which is still done by the implementing port type. Finally, the implementing port type is looked up as a
 * bean, and must implement the endpoint interface.
 * <p>
 * The entry point ensures the request to run in a {@link RunContext}, and optionally enforces authentication. Following
 * this, the request is propagated to the implementing port type.
 * <p>
 * Any sibling annotation is added to the entry point as well. That also applies for {@link WebService} annotation to
 * overwrite values as declared in the WSDL file. If you provide an explicit <em>handler chain binding file</em>,
 * handlers and authentication as declared with this annotation are ignored.
 * <p>
 * The generation of the <em>entry point port type</em> is done during compile-time. In short, APT is used to spider for
 * {@link WebServiceEntryPoint} annotations. Then, for each annotation found, an <em>entry point port type</em>
 * according to the annotation's specification is generated.
 * <p>
 * The binding to the concrete endpoint is done via {@link #endpointInterface()} attribute. If a WSDL declares multiple
 * services, create a separate <em>entry point definition</em> for each service to be published.
 * <p>
 * <strong>Example usage:</strong>
 *
 * <pre>
 * &#64;WebServiceEntryPoint(
 *     endpointInterface = PingWebServicePortType.class,
 *     entryPointName = "PingWebServiceEntryPoint",
 *     entryPointPackage = "org.eclipse.ws.ping",
 *     serviceName = "PingWebService",
 *     portName = "PingWebServicePort",
 *     handlerChain = {
 *         &#64;Handler(@Clazz(LogHandler.class)),
 *         &#64;Handler(value = @Clazz(IPAddressFilter.class) , initParams = {
 *             &#64;InitParam(key = "rangeFrom", value = "192.200.0.0"),
 *             &#64;InitParam(key = " rangeTo", value = "192.255.0.0")})
 *     },
 *     authentication = @Authentication(
 *         method = @Clazz(BasicAuthenticationMethod.class) ,
 *         verifier = @Clazz(ConfigFileCredentialVerifier.class) ) )
 * </pre>
 *
 * @since 5.1
 */
@Target(java.lang.annotation.ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface WebServiceEntryPoint {

  public static final String DERIVED = "derived";

  /**
   * Specifies the endpoint interface for which to generate an entry point during compile-time.
   * <p>
   * An endpoint interface defines the service's abstract webservice contract, and is also known as port type interface.
   * Also, the endpoint interface is annotated with {@link WebService} annotation.
   */
  Class<?> endpointInterface();

  /**
   * Specifies the class name of the entry point generated. If not set, the name is like the name of the endpoint
   * interface suffixed with <em>EntryPoint</em>.
   */
  String entryPointName() default WebServiceEntryPoint.DERIVED;

  /**
   * Specifies the package name of the entry point generated. If not set, the package name is the same as of the element
   * declaring this {@link WebServiceEntryPoint} annotation.
   */
  String entryPointPackage() default WebServiceEntryPoint.DERIVED;

  /**
   * Specifies the service name as declared in the WSDL file, and must be set if publishing the webservice via auto
   * discovery in an EE container. Both, {@link #serviceName()} and {@link #portName()} uniquely identify a webservice
   * endpoint to be published.
   *
   * <pre>
   * &lt;wsdl:service name="SERVICE_NAME">
   *  ...
   * &lt;/wsdl:service&gt
   * </pre>
   */
  String serviceName() default "";

  /**
   * Specifies the name of the port as declared in the WSDL file, and must be set if publishing the webservice via auto
   * discovery in an EE container. Both, {@link #serviceName()} and {@link #portName()} uniquely identify a webservice
   * endpoint to be published.
   *
   * <pre>
   * &lt;wsdl:service name="...">
   *  &lt;wsdl:port name="PORT_NAME" binding="..."/&gt;
   * &lt;/wsdl:service&gt
   * </pre>
   */
  String portName() default "";

  /**
   * Specifies the location of the WSDL document. If not set, the location is derived from {@link WebServiceClient}
   * annotation which is typically initialized with the 'wsdlLocation' as provided to 'wsimport'.
   */
  String wsdlLocation() default WebServiceEntryPoint.DERIVED;

  /**
   * Specifies the authentication mechanism to be installed, and in which {@link RunContext} to run authenticated
   * requests. By default, authentication is <em>disabled</em>. If <em>enabled</em>, an {@link AuthenticationHandler} is
   * generated at compile time (triggered by APT) and registered in the handler chain as very first handler.
   * <ul>
   * <li>{@link ICredentialVerifier} can be configured to be invoked in a {@link RunContext} by annotating it with
   * {@link RunWithRunContext} annotation.</li>
   * <li>{@link IAuthenticationMethod} and {@link ICredentialVerifier} must not be visible at compile-time, but can be
   * referenced with their qualified names instead.</li>
   * <li>If providing a <em>handler binding file</em> yourself, this annotation is ignored.</li>
   * </ul>
   */
  Authentication authentication() default @Authentication()
  ;

  /**
   * Specifies the handlers to be installed. The order of the handlers is as declared. A handler is looked up as a bean,
   * and must implement {@link javax.xml.ws.handler.Handler} interface.
   * <ul>
   * <li>A handler can be configured to run in a {@link RunContext} by annotating it with {@link RunWithRunContext}
   * annotation.</li>
   * <li>A handler can be instrumented with <em>init-params</em>.</li>
   * <li>A handler must not be visible at compile-time, but can be referenced with its qualified name instead.</li>
   * <li>At runtime, handlers are resolved as beans, meaning that a handler must be annotated with
   * {@link ApplicationScoped} annotation</li>
   * <li>If providing a <em>handler binding file</em> yourself, this annotation is ignored.</li>
   * </ul>
   */
  Handler[] handlerChain() default {};
}
