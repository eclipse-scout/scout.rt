/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
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
