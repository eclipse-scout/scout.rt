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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

/**
 *
 */
public abstract class AbstractFilterStringConfigProperty extends AbstractStringConfigProperty implements IFilterConfigProperty<String> {

  private FilterConfig m_config;

  @Override
  protected String getRawValue() {
    return getConfig().getInitParameter(getKey());
  }

  @Override
  public IProcessingStatus getStatus() {
    if (getConfig() == null) {
      return ProcessingStatus.OK_STATUS;
    }
    return super.getStatus();
  }

  @Override
  public FilterConfig getConfig() {
    return m_config;
  }

  @Override
  public void setConfig(FilterConfig config) {
    m_config = Assertions.assertNotNull(config);
  }
}
