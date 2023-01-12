/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsImplementorProperty;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsMetroSpecifics;

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
      return "scout.jaxws.implementor";
    }

    @Override
    public Class<? extends JaxWsImplementorSpecifics> getDefaultValue() {
      return JaxWsMetroSpecifics.class;
    }
  }
}
