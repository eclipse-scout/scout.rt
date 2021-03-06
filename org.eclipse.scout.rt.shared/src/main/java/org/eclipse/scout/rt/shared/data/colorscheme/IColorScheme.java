/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.data.colorscheme;

import java.util.Optional;

/**
 * @since 8.0
 */
public interface IColorScheme {

  /**
   * @return the static identifier (known to the UI) for this scheme.
   */
  String getIdentifier();

  static IColorScheme parse(String identifier) {
    return Optional.ofNullable((IColorScheme) ColorScheme.parse(identifier)).orElse(() -> identifier);
  }
}
