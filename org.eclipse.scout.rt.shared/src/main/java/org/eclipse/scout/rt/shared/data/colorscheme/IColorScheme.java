/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
