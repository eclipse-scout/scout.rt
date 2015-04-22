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

import javax.servlet.FilterConfig;

import org.eclipse.scout.rt.platform.config.IConfigPropertyWithStatus;

/**
 * A config property with servlet filter configuration as data source
 * 
 * @see FilterConfig
 */
public interface IFilterConfigProperty<DATA_TYPE> extends IConfigPropertyWithStatus<DATA_TYPE> {

  /**
   * Gets the {@link FilterConfig} associated with this property.
   * 
   * @return The {@link FilterConfig} of this property
   */
  FilterConfig getConfig();

  /**
   * Sets the {@link FilterConfig} of this property.
   * 
   * @param config
   *          The new {@link FilterConfig}.
   */
  void setConfig(FilterConfig config);
}
