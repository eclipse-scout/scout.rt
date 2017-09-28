/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
  UNKNOWN;

  @Override
  public String getIdentifier() {
    return name();
  }

  public static UiEngineType createByIdentifier(String identifier) {
    return valueOf(identifier);
  }

}
