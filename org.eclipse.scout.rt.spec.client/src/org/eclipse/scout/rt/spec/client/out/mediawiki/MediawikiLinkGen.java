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
package org.eclipse.scout.rt.spec.client.out.mediawiki;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.eclipse.scout.rt.spec.client.link.LinkTarget;
import org.eclipse.scout.rt.spec.client.out.DocSectionUtility;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.IDocSectionHeading;
import org.eclipse.scout.rt.spec.client.out.ILinkTarget;

/**
 * Creates links properties with id and name mediawiki sections.
 */
public class MediawikiLinkGen {
  private final MediaWikiLanguage m_lang = new MediaWikiLanguage();

  /**
   * @param f
   * @return the links for a {@link IDocSection}
   */
  public List<ILinkTarget> getLinkAnchors(IDocSection section, String filename) {
    List<ILinkTarget> links = new ArrayList<ILinkTarget>();
    for (IDocSection s : DocSectionUtility.getSectionsAsFlatList(section)) {
      if (s.isDisplayed()) {
        IDocSectionHeading heading = s.getHeading();
        if (heading.isValid()) {
          String id = heading.getId();
          links.add(new LinkTarget(id, getAnchor(heading), filename));
        }
      }
    }
    return links;
  }

  private String getAnchor(IDocSectionHeading heading) {
    return m_lang.getIdGenerationStrategy().generateId(heading.getName());
  }

}
