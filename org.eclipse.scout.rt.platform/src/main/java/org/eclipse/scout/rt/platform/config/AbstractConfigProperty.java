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

import org.eclipse.scout.rt.platform.exception.PlatformException;

public abstract class AbstractConfigProperty<DATA_TYPE> implements IConfigProperty<DATA_TYPE> {

  private DATA_TYPE m_value;
  private boolean m_valueInitialized;
  private PlatformException m_error;

  protected DATA_TYPE getDefaultValue() {
    return null;
  }

  @Override
  public final DATA_TYPE getValue() {
    if (!m_valueInitialized) {
      try {
        m_value = createValue();
      }
      catch (PlatformException t) {
        m_error = t;
      }
      catch (Exception e) {
        m_error = new PlatformException(e.getMessage(), e);
      }
      finally {
        m_valueInitialized = true;
      }
    }
    if (m_error != null) {
      throw m_error;
    }
    return m_value;
  }

  protected DATA_TYPE createValue() {
    String rawValue = ConfigUtility.getProperty(getKey());
    if (rawValue == null && !ConfigUtility.hasProperty(getKey())) {
      return getDefaultValue();
    }
    return parse(rawValue);
  }

  /**
   * @throws PlatformException
   */
  protected abstract DATA_TYPE parse(String value);

  @Override
  public PlatformException getError() {
    return m_error;
  }

}
