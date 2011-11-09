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
package org.eclipse.scout.rt.shared.services.common.processing;

import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.service.IService;

public interface IServerProcessingCancelService extends IService {

  /**
   * @deprecated use {@link #cancel(long)} instead
   */
  @Deprecated
  void cancel();

  /**
   * cancel only specific backend job transaction of the same server session
   */
  @InputValidation(IValidationStrategy.NO_CHECK.class)
  void cancel(long requestSequence);
}
