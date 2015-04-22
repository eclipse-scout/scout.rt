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

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;

/**
 *
 */
public abstract class AbstractBinaryConfigProperty extends AbstractConfigProperty<byte[]> {
  @Override
  protected byte[] parse(String value) {
    return Base64Utility.decode(value);
  }

  @Override
  protected IProcessingStatus getStatus(byte[] value) {
    String rawValue = getRawValue();
    if (StringUtility.hasText(rawValue) && (value == null || value.length < 1)) {
      // parse failed
      return new ProcessingStatus("Invalid value for property '" + getKey() + "'. Must be a valid base64 encoded byte array.", new Exception("origin"), 0, IProcessingStatus.ERROR);
    }
    return ProcessingStatus.OK_STATUS;
  }
}
