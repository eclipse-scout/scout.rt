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

import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.validate.ValidationUtility;
import org.eclipse.scout.rt.shared.validate.annotations.LookupValue;

/**
 * Check implementation of {@link LookupValue} annotation for method parameter or field.
 */
public class LookupValueCheck implements IValidateCheck {
  public static final String ID = "lookupValue";

  private LookupCall m_lookupCall;

  public LookupValueCheck(LookupCall lookupCall) {
    m_lookupCall = lookupCall;
  }

  @Override
  public String getCheckId() {
    return ID;
  }

  @Override
  public boolean accept(Object obj) {
    return obj != null;
  }

  @Override
  public void check(Object obj) throws Exception {
    if (obj.getClass().isArray()) {
      ValidationUtility.checkLookupCallArray(obj, m_lookupCall);
    }
    else {
      ValidationUtility.checkLookupCallValue(obj, m_lookupCall);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + m_lookupCall.getClass().getSimpleName();
  }

}
