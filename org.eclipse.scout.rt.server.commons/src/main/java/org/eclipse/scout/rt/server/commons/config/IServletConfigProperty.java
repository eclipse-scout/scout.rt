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
package org.eclipse.scout.rt.server.commons.config;

import javax.servlet.ServletConfig;

import org.eclipse.scout.rt.platform.config.IConfigPropertyWithStatus;

/**
 * A config property with servlet configuration as data source
 *
 * @see ServletConfig
 */
public interface IServletConfigProperty<DATA_TYPE> extends IConfigPropertyWithStatus<DATA_TYPE> {
  /**
   * Gets the {@link ServletConfig} associated with this property.
   *
   * @return The {@link ServletConfig} of this property
   */
  ServletConfig getConfig();

  /**
   * Sets the {@link ServletConfig} of this property.
   *
   * @param config
   *          The new {@link ServletConfig}.
   */
  void setConfig(ServletConfig config);
}
