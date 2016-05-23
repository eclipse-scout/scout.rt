/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons;

import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
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
}
