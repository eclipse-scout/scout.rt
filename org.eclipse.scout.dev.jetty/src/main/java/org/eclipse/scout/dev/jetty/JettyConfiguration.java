/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.dev.jetty;

import org.eclipse.scout.rt.platform.config.AbstractPortConfigProperty;

public final class JettyConfiguration {

  private JettyConfiguration() {
  }

  public static class ScoutJettyPortProperty extends AbstractPortConfigProperty {

    @Override
    public String getKey() {
      return "scout.jetty.port";
    }

    @Override
    public Integer getDefaultValue() {
      return 8080;
    }

    @Override
    public String description() {
      return "The port under which the jetty will be running.";
    }
  }

}
