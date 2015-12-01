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

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Config property for {@link Long} values.
 */
public abstract class AbstractLongConfigProperty extends AbstractConfigProperty<Long> {

  @Override
  protected Long parse(String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }
    return Long.parseLong(value);
  }

}
