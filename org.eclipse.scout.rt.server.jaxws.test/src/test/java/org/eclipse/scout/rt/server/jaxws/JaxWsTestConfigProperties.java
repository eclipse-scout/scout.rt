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
package org.eclipse.scout.rt.server.jaxws;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsImplementorProperty;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsRISpecifics;

public final class JaxWsTestConfigProperties {

  private JaxWsTestConfigProperties() {
  }

  /**
   * By default, JAX-WS Metro is used as JAX-WS implementor. However, JUnit tests should be implementor independent,
   * which is why the implementor is set to JAX-WS RI bundled with JRE.
   */
  @Replace
  public static class JaxWsTestImplementorProperty extends JaxWsImplementorProperty {

    @Override
    public String getKey() {
      return "jaxws.implementor";
    }

    @Override
    protected String getDefaultValue() {
      return JaxWsRISpecifics.class.getName();
    }
  }
}
