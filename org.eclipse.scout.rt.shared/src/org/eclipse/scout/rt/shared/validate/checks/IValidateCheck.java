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

import org.eclipse.scout.rt.shared.validate.ValidateCheckSet;

/**
 * A specific check that is performed by a validator
 */
public interface IValidateCheck {

  /**
   * @return the check id. When multiple checks with the same id exist in a {@link ValidateCheckSet} then the latest
   *         added (and accepted) is applied, the others are ignored.
   */
  String getCheckId();

  /**
   * @return true if the check can handle the object, resp. is appropriate for the object
   */
  boolean accept(Object obj);

  /**
   * @throws Exception
   *           if the validation fails
   */
  void check(Object obj) throws Exception;
}
