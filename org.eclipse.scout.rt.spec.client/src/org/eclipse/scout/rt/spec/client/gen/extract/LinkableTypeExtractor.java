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
import org.eclipse.scout.rt.spec.client.utility.SpecUtility;

/**
 * Extractor for the entity's documented type (name with link to the doc section where the type is explained).
 * <p>
 * The entity's class hierarchy will be searched bottom up for the first documented type.
 * 
 * @param <T>
 */
public class LinkableTypeExtractor<T> extends AbstractNamedTextExtractor<T> {
  public static final String LINKS_TAG_NAME = "links";
  private Class m_supertype;
  private boolean m_assumeAllSubtypesDocumented;

  /**
   * A type is assumed to be documented when the type is annotated with a {@link ClassId} annotation for which a
   * doc-text with key <code>[classid]_name</code> is available.
   */
  public LinkableTypeExtractor() {
    this(Object.class, false);
  }

  /**
   * @param supertype
   *          only subclasses of <code>supertype</code> are considered as documented
   * @param assumeAllSubtypesDocumented
   *          If set to <code>true</code> all subclasses of <code>supertype</code> are assumed to be documented, even if
   *          no doc-text is available.
   */
  public LinkableTypeExtractor(Class supertype, boolean assumeAllSubtypesDocumented) {
    super(TEXTS.get("org.eclipse.scout.rt.spec.type"));
    m_supertype = supertype;
    m_assumeAllSubtypesDocumented = assumeAllSubtypesDocumented;
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
      if (SpecUtility.isDocType(hierarchyType, m_supertype, m_assumeAllSubtypesDocumented)) {
        String name = TEXTS.getWithFallback(ConfigurationUtility.getAnnotatedClassIdWithFallback(hierarchyType) + "_name", hierarchyType.getSimpleName());
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
