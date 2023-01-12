/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui;

/**
 * @since 6.0
 */
public enum UiEngineType implements IUiEngineType {
  ANDROID,
  CHROME,
  SAFARI,
  FIREFOX,
  IE,
  OPERA,
  KONQUEROR,
  EDGE,
  UNKNOWN;

  @Override
  public String getIdentifier() {
    return name();
  }

  public static UiEngineType createByIdentifier(String identifier) {
    return valueOf(identifier);
  }

}
