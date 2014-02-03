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
package org.eclipse.scout.rt.spec.client.out;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for {@link IDocSection}
 */
public final class DocSectionUtility {

  /**
   * @param section
   *          {@link IDocSection}
   * @return the section and all its subsections as a flat list
   */
  public static List<IDocSection> getSectionsAsFlatList(IDocSection section) {
    List<IDocSection> l = new ArrayList<IDocSection>();
    addSectionsToListRec(l, section);
    return l;
  }

  private static void addSectionsToListRec(List<IDocSection> list, IDocSection s) {
    //TODO there should be no subsections that are null
    if (s != null) {
      list.add(s);
      IDocSection[] subSections = s.getSubSections();
      for (IDocSection sub : subSections) {
        addSectionsToListRec(list, sub);
      }
    }
  }
}
