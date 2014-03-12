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
package org.eclipse.scout.rt.spec.client.gen;

import java.util.Set;

import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityListConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.internal.Section;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

public class TypeSpecGenerator {
  private final IDocEntityListConfig<Class> m_config;
  private String m_anchorId;
  private String m_title;

  public TypeSpecGenerator(IDocEntityListConfig<Class> config, String anchorId, String title) {
    m_config = config;
    m_anchorId = anchorId;
    m_title = title;
  }

  public IDocSection getDocSection(Set<Class> types) {
    String anchor = MediawikiUtility.createAnchor(m_anchorId);
    String titleWithAnchor = MediawikiUtility.transformToWiki(anchor + m_title);
    if (types.isEmpty()) {
      return new Section(titleWithAnchor);
    }
    IDocSection typeSection = DocGenUtility.createDocSection(types.toArray(new Class[types.size()]), m_config, false);
    return new Section(titleWithAnchor, typeSection);
  }
}
