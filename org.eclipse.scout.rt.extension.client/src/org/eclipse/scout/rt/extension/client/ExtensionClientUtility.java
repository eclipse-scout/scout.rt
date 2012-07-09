/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client;

import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.commons.CompareUtility;

/**
 * @since 3.9.0
 */
public final class ExtensionClientUtility {

  private ExtensionClientUtility() {
  }

  /**
   * Removes all objects from the given list that are exact instance of the java types provided. An object <em>o</em> is
   * an exact instance of a class <em>C</em>, iff <code>o.getClass() == C</code>. i.e. instances of sub classes of
   * <em>C</em> are not exact instances of <em>C</em>.
   * 
   * @param objectList
   *          the list of classes to remove types from. The list is modified in general.
   * @param types
   *          vararg with exact types to be removed.
   */
  public static void removeByType(List<?> objectList, Class<?>... types) {
    if (objectList == null || types == null || types.length == 0) {
      return;
    }

    for (Iterator<?> it = objectList.iterator(); it.hasNext();) {
      Object next = it.next();
      if (next != null && CompareUtility.isOneOf(next.getClass(), (Object[]) types)) {
        it.remove();
      }
    }
  }
}
