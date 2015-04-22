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

import java.util.regex.Pattern;

import javax.servlet.FilterConfig;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;

/**
 *
 */
public abstract class AbstractFilterIntegerConfigProperty extends AbstractConfigProperty<Integer> implements IFilterConfigProperty<Integer> {

  private static final Pattern INT_PAT = Pattern.compile("^\\-?\\d{1,9}$");

  private FilterConfig m_config;

  @Override
  protected Integer parse(String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }

    return Integer.parseInt(value);
  }

  @Override
  protected IProcessingStatus getStatusRaw(String rawValue) {
    // property is not mandatory
    if (!StringUtility.hasText(rawValue)) {
      return ProcessingStatus.OK_STATUS;
    }

    // if specified: it must be a valid integer
    if (INT_PAT.matcher(rawValue).matches()) {
      return ProcessingStatus.OK_STATUS;
    }
    return new ProcessingStatus("Invalid integer value '" + rawValue + "' for property '" + getKey() + "'.", new Exception("origin"), 0, IProcessingStatus.ERROR);
  }

  @Override
  public IProcessingStatus getStatus() {
    if (getConfig() == null) {
      return ProcessingStatus.OK_STATUS;
    }
    return super.getStatus();
  }

  @Override
  protected String getRawValue() {
    return m_config.getInitParameter(getKey());
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
