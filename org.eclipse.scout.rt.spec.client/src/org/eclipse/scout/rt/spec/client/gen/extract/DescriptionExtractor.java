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
package org.eclipse.scout.rt.spec.client.gen.extract;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * A {@link IDocTextExtractor} for the documentation text defined by classid
 */
public class DescriptionExtractor<T extends ITypeWithClassId> extends AbstractNamedTextExtractor<T> implements IDocTextExtractor<T> {

  public DescriptionExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.doc"));
  }

  /**
   * The documentation text of a scout model entity without enclosing html tags.
   */
  @Override
  public String getText(T o) {
    String doc = getDocAssociatedWithClassId(o);
    if (doc != null) {
      doc = doc.replaceAll("</html>", "");
      doc = doc.replaceAll("<html>", "");
    }
    return doc;
  }

  /**
   * @param o
   * @return
   */
  private String getDocAssociatedWithClassId(T o) {
    ArrayList<String> classIdParts = new ArrayList<String>(Arrays.asList(o.classId().split(ITypeWithClassId.ID_CONCAT_SYMBOL)));
    String text = null;
    while (!classIdParts.isEmpty() && text == null) {
      String classId = CollectionUtility.format(classIdParts, ITypeWithClassId.ID_CONCAT_SYMBOL);
      text = TEXTS.getWithFallback(classId, null);
      classIdParts.remove(classIdParts.size() - 1);
    }

    return text != null ? text : "";
  }

}
