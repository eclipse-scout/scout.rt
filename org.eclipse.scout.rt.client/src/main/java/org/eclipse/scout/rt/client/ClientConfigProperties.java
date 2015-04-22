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
package org.eclipse.scout.rt.client;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.rt.client.services.common.prefs.FileSystemUserPreferencesStorageService;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

/**
 *
 */
public final class ClientConfigProperties {
  private ClientConfigProperties() {
  }

  /**
   * Specifies how long the client keeps fetched data before it is discarded.<br>
   * Must be one of the following values: small, medium or large. Default is large.
   */
  public static class MemoryPolicyProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    protected IProcessingStatus getStatus(String value) {
      if (!StringUtility.hasText(value)) {
        return ProcessingStatus.OK_STATUS;
      }

      if (CompareUtility.isOneOf(value, "small", "medium", "large")) {
        return ProcessingStatus.OK_STATUS;
      }

      return new ProcessingStatus("Invalid value for property '" + getKey() + "': '" + value + "'. Valid values are small, medium or large", new Exception("origin"), 0, IProcessingStatus.ERROR);
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.memory";
    }
  }

  /**
   * Specifies the user area on the local file system where to store user preferences.<br>
   * If nothing is specified the user home of the operating system is used.
   *
   * @see FileSystemUserPreferencesStorageService
   */
  public static class UserAreaProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "user.area";
    }
  }
}
