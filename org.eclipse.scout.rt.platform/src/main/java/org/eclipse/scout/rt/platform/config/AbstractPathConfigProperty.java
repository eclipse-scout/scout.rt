/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.scout.rt.platform.util.StringUtility;

public abstract class AbstractPathConfigProperty extends AbstractConfigProperty<Path, String> {

  @Override
  protected Path parse(String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }
    return Paths.get(value);
  }

}
