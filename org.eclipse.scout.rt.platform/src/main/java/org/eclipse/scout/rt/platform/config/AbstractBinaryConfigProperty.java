/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.config;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;

public abstract class AbstractBinaryConfigProperty extends AbstractConfigProperty<byte[], String> {

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
