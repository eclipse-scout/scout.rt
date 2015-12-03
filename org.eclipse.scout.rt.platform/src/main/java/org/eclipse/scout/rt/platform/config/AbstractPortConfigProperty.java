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

public abstract class AbstractPortConfigProperty extends AbstractPositiveIntegerConfigProperty {

  @Override
  protected Integer parse(String value) {
    Integer i = super.parse(value);
    if (i == null) {
      return i;
    }
    int port = i.intValue();
    if (port >= 1 && port <= 65535) {
      return i;
    }
    throw new PlatformException("Invalid value for port config with key '" + getKey() + "': '" + value + "'. Valid values are integers in range [1, 65535].");
  }
}
