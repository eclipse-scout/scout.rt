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
package org.eclipse.scout.rt.spec.client;

import java.io.File;
import java.io.Writer;
import java.util.List;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.DefaultDocConfig;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.rt.spec.client.link.LinkTarget;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.ILinkTarget;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiLinkGen;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiLinkTargetManager;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiWriter;

/**
 *
 */
public class AbstractSpecGen {
  private final SpecFileConfig m_fileConfig;

  public AbstractSpecGen() {
    m_fileConfig = new SpecFileConfig();
  }

  protected SpecFileConfig getFileConfig() {
    return m_fileConfig;
  }

  protected IDocConfig getConfiguration() {
    return new DefaultDocConfig();
  }

  protected void write(IDocSection section, String id, String[] imagePaths, String simpleId) throws ProcessingException {
    File out = getFileConfig().getSpecDir();
    out.mkdirs();

    File wiki = SpecIOUtility.createNewFile(getFileConfig().getMediawikiDir(), id, ".mediawiki");
    Writer fileWriter = SpecIOUtility.createWriter(wiki);
    MediawikiWriter w = new MediawikiWriter(fileWriter, section, imagePaths);
    w.write();

    storeLinkTargets(section, wiki, simpleId);
  }

  /**
   * store link targets in property file
   */
  protected void storeLinkTargets(IDocSection section, File wiki, String simpleId) throws ProcessingException {
    List<ILinkTarget> links = new MediawikiLinkGen().getLinkAnchors(section, wiki.getName());
    //add simple name to allow manual links to simple name
    //TODO move to preprocessor
    links.add(new LinkTarget(simpleId, simpleId, wiki.getName()));
    MediawikiLinkTargetManager w = new MediawikiLinkTargetManager(getFileConfig().getLinksFile());
    w.writeLinks(links);
  }

  public String getId(ITypeWithClassId o) {
    return o.classId();
  }

}
