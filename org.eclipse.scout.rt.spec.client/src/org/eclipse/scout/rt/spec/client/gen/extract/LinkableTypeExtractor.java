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

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * Extractor for the entity's documented type (name with link to the doc section where the type is explained).
 * <p>
 * The entity's class hierarchy will be searched bottom up for the first documented type. A documented type is a a class
 * with a {@link ClassId} annotation for which a doc-text with key <code>[classid]_name</code> is available.
 * 
 * @param <T>
 */
public class LinkableTypeExtractor<T> extends AbstractNamedTextExtractor<T> {
  public static final String LINKS_TAG_NAME = "links";

  public LinkableTypeExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.type"));
  }

  @Override
  public String getText(T o) {
    Class type = o.getClass();
    return getText(type);
  }

  public String getText(Class type) {
    Class hierarchyType = type;
    StringBuilder specType = new StringBuilder();
    while (hierarchyType != null) {
      String name = TEXTS.getWithFallback(ConfigurationUtility.getAnnotatedClassIdWithFallback(hierarchyType) + "_name", null);
      if (name != null) {
        specType.append(MediawikiUtility.createLink("c_" + ConfigurationUtility.getAnnotatedClassIdWithFallback(hierarchyType), name));
        break;
      }
      hierarchyType = hierarchyType.getSuperclass();
    }
    if (specType.length() == 0) {
      specType.append(type.getSimpleName());
    }
    return specType.toString();
  }
}
