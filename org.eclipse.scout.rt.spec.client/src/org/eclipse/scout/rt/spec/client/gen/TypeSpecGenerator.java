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

import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.internal.SectionWithTable;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

public class TypeSpecGenerator {
  private final IDocConfig m_config;
  private String m_anchorId;
  private String m_title;

  public TypeSpecGenerator(IDocConfig config, String anchorId, String title) {
    m_config = config;
    m_anchorId = anchorId;
    m_title = title;
  }

  public IDocSection getDocSection(Class[] types) {
    String anchor = MediawikiUtility.createAnchor(m_anchorId);
    String titleWithAnchor = MediawikiUtility.transformToWiki(anchor + m_title);
    IDocSection typeSection = DocGenUtility.createDocSection(types, m_config.getTypesConfig());
    return new SectionWithTable(titleWithAnchor, typeSection);
  }
}
