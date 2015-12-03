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

import org.eclipse.scout.rt.platform.util.StringUtility;

public abstract class AbstractStringConfigProperty extends AbstractConfigProperty<String> {

  @Override
  protected String parse(String value) {
    if (StringUtility.hasText(value)) {
      return value;
    }
    return null;
  }
}
