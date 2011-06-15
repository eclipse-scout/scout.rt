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
package org.eclipse.scout.commons;

import java.lang.reflect.Field;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public final class CloneUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CloneUtility.class);

  private CloneUtility() {
  }

  /**
   * When cloning inner types, the synthetic members this$0, this$1, ... are not cloned to the copy instance.
   * This helper adapts the membership to the new copy instance
   * 
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public static void adaptSyntheticMembershipFields(Object oldOuterObject, Object clonedOuterObject, Object clonedObject) throws Exception {
    int i = 0;
    for (Field f : clonedObject.getClass().getDeclaredFields()) {
      if (f.getName().startsWith("this$")) {
        f.setAccessible(true);
        if (f.get(clonedObject) == oldOuterObject) {
          f.set(clonedObject, clonedOuterObject);
        }
      }
    }
  }
}
