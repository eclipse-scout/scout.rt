/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.validate.checks;

import org.eclipse.scout.rt.shared.validate.ValidationUtility;
import org.eclipse.scout.rt.shared.validate.annotations.Mandatory;

/**
 * Check implementation of {@link Mandatory} annotation for method parameter or field.
 */
public class MandatoryCheck implements IValidateCheck {
  public static final String ID = "mandatory";

  private final boolean m_value;

  public MandatoryCheck(boolean value) {
    m_value = value;
  }

  @Override
  public String getCheckId() {
    return ID;
  }

  @Override
  public boolean accept(Object obj) {
    return true;
  }

  @Override
  public void check(Object obj) throws Exception {
    if (!m_value) {
      return;
    }
    if (obj != null && obj.getClass().isArray()) {
      ValidationUtility.checkMandatoryArray(obj);
    }
    else {
      ValidationUtility.checkMandatoryValue(obj);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + m_value;
  }
}
