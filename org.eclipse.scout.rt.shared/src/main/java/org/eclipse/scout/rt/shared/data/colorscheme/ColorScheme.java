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

import java.util.stream.Stream;

/**
 * @since 5.2
 */
public enum ColorScheme implements IColorScheme {
  DEFAULT(SchemeIds.SCHEME_ID_DEFAULT, false),
  DEFAULT_INVERTED(SchemeIds.SCHEME_ID_DEFAULT, true),
  ALTERNATIVE(SchemeIds.SCHEME_ID_ALTERNATIVE, false),
  ALTERNATIVE_INVERTED(SchemeIds.SCHEME_ID_ALTERNATIVE, true),
  RAINBOW(SchemeIds.SCHEME_ID_RAINBOW, false);

  private final String m_schemeId;
  private final boolean m_inverted;

  ColorScheme(String schemeId, boolean inverted) {
    m_schemeId = schemeId;
    m_inverted = inverted;
  }

  @Override
  public String getIdentifier() {
    return m_schemeId + (m_inverted ? "-inverted" : "");
  }

  public static ColorScheme parse(String identifier) {
    return Stream.of(values())
        .filter(colorScheme -> colorScheme.getIdentifier().equals(identifier))
        .findFirst()
        .orElse(null);
  }

  /**
   * Utility method returning the inverted color scheme for this scheme.
   */
  public ColorScheme invert() {
    switch (this) {
      case DEFAULT:
        return DEFAULT_INVERTED;
      case DEFAULT_INVERTED:
        return DEFAULT;
      case ALTERNATIVE:
        return ALTERNATIVE_INVERTED;
      case ALTERNATIVE_INVERTED:
        return ALTERNATIVE;
      default:
        return this; // unknown scheme cannot be inverted
    }
  }

  /**
   * Utility method returning the "opposite" color scheme for this scheme.
   */
  public ColorScheme toggle() {
    switch (this) {
      case DEFAULT:
        return ALTERNATIVE;
      case DEFAULT_INVERTED:
        return ALTERNATIVE_INVERTED;
      case ALTERNATIVE:
        return RAINBOW;
      case ALTERNATIVE_INVERTED:
        return RAINBOW;
      case RAINBOW:
        return DEFAULT;
      default:
        return this; // unknown scheme cannot be toggled
    }
  }

  // These constants need to correspond to the IDs defined in Tile.js
  public interface SchemeIds {
    String SCHEME_ID_DEFAULT = "default";
    String SCHEME_ID_ALTERNATIVE = "alternative";
    String SCHEME_ID_RAINBOW = "rainbow";
  }
}
