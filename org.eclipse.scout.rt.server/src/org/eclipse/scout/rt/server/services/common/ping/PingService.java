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
package org.eclipse.scout.rt.server.services.common.ping;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.form.InputValidation;
import org.eclipse.scout.rt.shared.data.form.ValidationStrategy;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.util.ValidationUtility;
import org.eclipse.scout.service.AbstractService;

@Priority(-1)
public class PingService extends AbstractService implements IPingService {

  @Override
  @InputValidation(ValidationStrategy.NO_CHECK)
  public String ping(String s) {
    try {
      ValidationUtility.checkMaxLength("ping", s, 2000);
    }
    catch (ProcessingException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
    return s;
  }

}
