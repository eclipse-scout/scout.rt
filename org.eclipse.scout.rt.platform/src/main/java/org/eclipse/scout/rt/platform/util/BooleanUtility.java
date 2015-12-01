/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

/**
 * Title: BSI Scout V3 Copyright:
 * 
 * @version 3.x
 */

public final class BooleanUtility {

  private BooleanUtility() {
  }

  public static boolean nvl(Boolean b) {
    return nvl(b, false);
  }

  public static boolean nvl(Boolean b, boolean defaultValue) {
    if (b == null) {
      return defaultValue;
    }
    return b.booleanValue();
  }
}
