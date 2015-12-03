/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;

public abstract class AbstractBinaryConfigProperty extends AbstractConfigProperty<byte[]> {

  @Override
  protected byte[] parse(String value) {
    byte[] bytes = Base64Utility.decode(value);
    if ((bytes == null || bytes.length < 1) && StringUtility.hasText(value)) {
      // parse failed
      throw new PlatformException("Invalid value for property '" + getKey() + "'. Must be a valid base64 encoded byte array.");
    }
    return bytes;
  }

}
