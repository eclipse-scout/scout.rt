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
package org.eclipse.scout.rt.platform.config;

import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.commons.status.MultiStatus;
import org.eclipse.scout.rt.platform.exception.PlatformException;

public abstract class AbstractConfigProperty<DATA_TYPE> implements IConfigPropertyWithStatus<DATA_TYPE> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractConfigProperty.class);

  private boolean m_valueInitialized;
  private DATA_TYPE m_value;

  @Override
  public DATA_TYPE getValue() {
    if (!m_valueInitialized) {
      m_value = createValue();
      m_valueInitialized = true;
    }
    return m_value;
  }

  protected DATA_TYPE createValue() {
    String prop = getRawValue();
    if (!StringUtility.hasText(prop)) {
      return getDefaultValue();
    }

    IProcessingStatus status = getStatusRaw(prop);
    if (status.isOK()) {
      DATA_TYPE ret = parse(prop);
      if (ret != null && getStatus(ret).isOK()) {
        return ret;
      }
    }

    return getDefaultValue();
  }

  protected String getRawValue() {
    return ConfigUtility.getProperty(getKey());
  }

  @Override
  public IProcessingStatus getStatus() {
    String prop = getRawValue();
    IProcessingStatus status = getStatusRaw(prop);
    if (!status.isOK()) {
      return status;
    }
    DATA_TYPE ret = parse(prop);
    return getStatus(ret);
  }

  protected abstract DATA_TYPE parse(String value);

  protected IProcessingStatus getStatus(DATA_TYPE value) {
    return ProcessingStatus.OK_STATUS;
  }

  protected IProcessingStatus getStatusRaw(String rawValue) {
    return ProcessingStatus.OK_STATUS;
  }

  public static void checkStatus(IConfigPropertyWithStatus config) {
    checkStatus(CollectionUtility.arrayList(config));
  }

  public static void checkStatus(List<IConfigPropertyWithStatus> configs) {
    if (!CollectionUtility.hasElements(configs)) {
      return;
    }

    MultiStatus fatals = new MultiStatus();
    for (IConfigPropertyWithStatus property : configs) {
      IProcessingStatus s = property.getStatus();
      if (s != ProcessingStatus.OK_STATUS) {
        switch (s.getSeverity()) {
          case IProcessingStatus.FATAL:
          case IProcessingStatus.ERROR:
            fatals.add(s);
            break;
          default:
            LOG.log(s);
            break;
        }
      }
    }

    if (!fatals.isOK()) {
      StringBuilder msg = new StringBuilder();
      for (IStatus ps : fatals.getChildren()) {
        msg.append(ps.toString()).append("\n");
      }
      throw new PlatformException(msg.toString());
    }
  }
}
