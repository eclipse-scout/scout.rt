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
import org.eclipse.scout.rt.spec.client.out.html.HtmlConverter;
import org.eclipse.scout.rt.spec.client.out.html.TemplateUtility;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiAnchorCollector;

/**
 * A post processor for mediawiki files which performs the following tasks:
 * <p>
 * <li>Replace link tags to point to the generated files.
 * <li>Convert all mediawiki files to html files
 */
public class MediawikiPostProcessor implements ISpecProcessor {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(MediawikiPostProcessor.class);

  @Override
  public void process() throws ProcessingException {
    if (!SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().exists()) {
      LOG.warn("MediawikiDir does not exists! (" + SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().getPath() + ")");
      return;
    }
    for (File wiki : SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().listFiles()) {
      replaceLinks(wiki);
      File html = convertToHTML(wiki);
      replaceWikiFileLinks(html);
    }
  }

  private void replaceLinks(File f) throws ProcessingException {
    MediawikiAnchorCollector c = new MediawikiAnchorCollector(f);
    c.replaceLinks(f, SpecIOUtility.getSpecFileConfigInstance().getLinksFile());
  }

  private void replaceWikiFileLinks(File htmlFile) throws ProcessingException {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("/wiki/", "");
    map.put(".mediawiki", ".html");
    SpecIOUtility.replaceAll(htmlFile, map);
  }

  protected File convertToHTML(File mediaWiki) throws ProcessingException {
    String htmlName = mediaWiki.getName().replace(".mediawiki", "");
    return convertToHTML(htmlName, mediaWiki);
  }

  protected File convertToHTML(String id, File mediaWiki) throws ProcessingException {
    File htmlDir = SpecIOUtility.getSpecFileConfigInstance().getHtmlDir();
    htmlDir.mkdirs();
    File htmlFile = SpecIOUtility.createNewFile(htmlDir, id, ".html");

    // copy css
    File css = new File(htmlDir, "default.css");
    TemplateUtility.copyDefaultCss(css);

    HtmlConverter htmlConverter = new HtmlConverter(css);
    htmlConverter.convertWikiToHtml(mediaWiki, htmlFile);
    return htmlFile;
  }

}
