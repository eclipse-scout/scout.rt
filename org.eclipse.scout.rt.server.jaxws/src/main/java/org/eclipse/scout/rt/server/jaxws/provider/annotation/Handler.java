/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContextProducer;
import org.eclipse.scout.rt.platform.context.RunWithRunContext;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsHandlerSubjectProperty;

/**
 * Describes a JAX-WS handler to intercept webservice requests.
 * <p>
 * To run the handler on behalf of a {@link RunContext}, annotate the handler class with {@link RunWithRunContext}.
 * Thereto, a {@link RunContext} is produced as described by {@link RunContextProducer}, and is initialized with the
 * authenticated user, or if not authenticated, with the {@link Subject} as configured in
 * {@link JaxWsHandlerSubjectProperty}.
 *
 * @since 5.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Handler {

  /**
   * The handler to be installed which will be looked up as a bean.
   */
  Clazz value();

  /**
   * Optional <i>init-parameters</i> to instrument the handler.
   * <p>
   * In order to work, the handler must declare a field of the type <code>Map&lt;String, String&gt;</code> annotated
   * with @Resource. At construction time, the parameters are injected into that field.
   * <p>
   * Example:
   *
   * <pre>
   * &#064;ApplicationScoped
   * public class ExampleHandler implements SOAPHandler&lt;SOAPMessageContext&gt; {
   *
   *   &#064;Resource
   *   private Map&lt;String, String&gt; m_initParams;
   *   ...
   * }
   * </pre>
   */
  InitParam[] initParams() default {};

  /**
   * Indicates the type of the handler. This attribute is only of relevance and mandatory, if the <i>handler class</i>
   * is not visible at compile-time, e.g. because being located in another module that is only visible at run-time. By
   * default, the type is {@link HandlerType#SOAP}.
   */
  HandlerType handlerType() default HandlerType.SOAP;

  enum HandlerType {
    /**
     * SOAP protocol-specific handler type.
     */
    SOAP,
    /**
     * Protocol-agnostic handler type.
     */
    LOGICAL
  }
}
