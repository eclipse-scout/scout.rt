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
import java.util.Properties;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.DefaultDocConfig;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.docbook.DocBookConverter;
import org.eclipse.scout.rt.spec.client.out.html.HtmlConverter;
import org.eclipse.scout.rt.spec.client.out.html.TemplateUtility;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiAnchorCollector;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiLinkPostProcessor;
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

  protected File convertToDocBook(File out, String id, File mediaWiki) {
    File docBook = new File(out, id + ".xml");
    DocBookConverter c = new DocBookConverter();
    c.convertWikiToDocBook(mediaWiki, docBook);
    return docBook;
  }

  protected IDocConfig getConfiguration() {
    return new DefaultDocConfig();
  }

  protected void write(IDocSection section, String id, String[] imagePaths) throws ProcessingException {
    File out = getFileConfig().getSpecDir();
    out.mkdirs();

    File preprocessed = getFileConfig().getMediawikiRawDir();
    File wiki = SpecIOUtility.createNewFile(preprocessed, id, "_raw.mediawiki");
    Writer mediaWikiWriter = SpecIOUtility.createWriter(wiki);
    File properties = SpecIOUtility.createNewFile(preprocessed, id, ".properties");
    Writer linkIdWriter = SpecIOUtility.createWriter(properties);

    //
    MediawikiWriter wikimediaFormWriter = new MediawikiWriter(linkIdWriter, mediaWikiWriter, section, imagePaths);
    wikimediaFormWriter.write();

    File mediawiki = getFileConfig().getMediawikiDir();
    File finalWiki = SpecIOUtility.createNewFile(mediawiki, id, ".mediawiki");
    Properties prop = SpecIOUtility.loadProperties(properties);
    MediawikiLinkPostProcessor postproc = new MediawikiLinkPostProcessor(prop);
    postproc.replaceLinks(wiki, finalWiki);
    new MediawikiAnchorCollector(finalWiki).storeAnchors(getFileConfig().getLinksFile());

    convertToHTML(id, finalWiki);
  }

  public String getId(ITypeWithClassId o) {
    return o.classId();
  }

  protected File convertToHTML(File mediaWiki) throws ProcessingException {
    String htmlName = mediaWiki.getName().replace(".mediawiki", "");
    return convertToHTML(htmlName, mediaWiki);
  }

  protected File convertToHTML(String id, File mediaWiki) throws ProcessingException {
    File htmlDir = getFileConfig().getHtmlDir();
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
