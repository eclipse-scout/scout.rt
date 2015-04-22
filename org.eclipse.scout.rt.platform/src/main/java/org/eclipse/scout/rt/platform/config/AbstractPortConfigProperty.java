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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;

/**
 *
 */
public abstract class AbstractPortConfigProperty extends AbstractPositiveIntegerConfigProperty {

  @Override
  protected IProcessingStatus getStatus(Integer value) {
    IProcessingStatus status = super.getStatus(value);
    if (!status.isOK()) {
      return status;
    }

    if (value == null) {
      return ProcessingStatus.OK_STATUS;
    }

    int val = value.intValue();
    if (val >= 1 && val <= 65535) {
      return ProcessingStatus.OK_STATUS;
    }

    return new ProcessingStatus("Invalid value for port config with key '" + getKey() + "': '" + value + "'. Valid values are integers in range [1, 65535].", new Exception("origin"), 0, IProcessingStatus.ERROR);
  }
}
