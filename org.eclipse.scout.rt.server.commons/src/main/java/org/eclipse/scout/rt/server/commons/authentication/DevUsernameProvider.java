/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Provides a username for the {@link DevelopmentAccessController}.
 */
@ApplicationScoped
public class DevUsernameProvider {

  /**
   * The {@link DevUsernameConfigProperty} is always checked first in order to allow a temporary override of the
   * username during development. If the property is not set, the "user.name" system property is returned.
   *
   * @return Returns the username to be used by the {@link DevelopmentAccessController}.
   */
  public String getUsername() {
    String usernameFromProperty = CONFIG.getPropertyValue(DevUsernameConfigProperty.class);
    if (StringUtility.hasText(usernameFromProperty)) {
      return usernameFromProperty;
    }
    return getUsernameInternal();
  }

  protected String getUsernameInternal() {
    return System.getProperty("user.name");
  }

  public static class DevUsernameConfigProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "dev.username";
    }

    @Override
    public String description() {
      return "Allows to override the username returned by the DevUsernameProvider.";
    }
  }
}
