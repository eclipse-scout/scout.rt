/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

/**
 * Describes a JAX-WS handler to intercept webservice requests.
 *
 * @since 5.1
 */
@Target(java.lang.annotation.ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Handler {

  /**
   * The handler to be installed.
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

  public static enum HandlerType {
    /**
     * SOAP protocol-specific handler type.
     */
    SOAP,
    /**
     * Protocol-agnostic handler type.
     */
    LOGICAL;
  }
}
