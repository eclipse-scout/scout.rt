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
package org.eclipse.scout.rt.server.commons;

import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractMapConfigProperty;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;

public final class ServerCommonsConfigProperties {

  private ServerCommonsConfigProperties() {
  }

  /**
   * Enable or disable changing {@link UrlHints} using URL parameters in the browser address line
   */
  public static class UrlHintsEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    protected Boolean getDefaultValue() {
      return Platform.get().inDevelopmentMode();
    }

    @Override
    public String getKey() {
      return "scout.urlHints.enabled";
    }
  }

  /**
   * Enable or disable Content Security Policy (CSP) headers.
   */
  public static class CspEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    protected Boolean getDefaultValue() {
      return true;
    }

    @Override
    public String getKey() {
      return "scout.csp.enabled";
    }

    /**
     * Configures individual Content Security Policy (CSP) directives.
     *
     * @see org.eclipse.scout.rt.server.commons.servlet.ContentSecurityPolicy
     */
    public static class CspDirectiveProperty extends AbstractMapConfigProperty {

      @Override
      public String getKey() {
        return "scout.csp.directive";
      }

    }
  }
}
