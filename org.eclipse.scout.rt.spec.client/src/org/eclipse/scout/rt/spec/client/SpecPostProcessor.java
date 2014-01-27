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
import java.util.HashMap;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiAnchorCollector;

/**
 * A post processor for replacing link tags to point to the generated files.
 */
public class SpecPostProcessor extends AbstractSpecGen implements ISpecProcessor {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(SpecPostProcessor.class);

  @Override
  public void process() throws ProcessingException {
    if (!getFileConfig().getMediawikiDir().exists()) {
      LOG.warn("MediawikiDir does not exists! (" + getFileConfig().getMediawikiDir().getPath() + ")");
      return;
    }
    for (File wiki : getFileConfig().getMediawikiDir().listFiles()) {
      replaceLinks(wiki);
      File html = convertToHTML(wiki);
      replaceWikiFileLinks(html);
    }
  }

  private void replaceLinks(File f) throws ProcessingException {
    MediawikiAnchorCollector c = new MediawikiAnchorCollector(f);
    c.replaceLinks(f, getFileConfig().getLinksFile());
  }

  private void replaceWikiFileLinks(File htmlFile) throws ProcessingException {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("/wiki/", "");
    map.put(".mediawiki", ".html");
    SpecIOUtility.replaceAll(htmlFile, map);
  }

}
