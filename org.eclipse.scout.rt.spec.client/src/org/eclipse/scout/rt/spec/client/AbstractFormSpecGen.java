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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.spec.client.config.DefaultDocConfig;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.rt.spec.client.gen.FormSpecGenerator;
import org.eclipse.scout.rt.spec.client.gen.SpecImageFilter;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.docbook.DocBookConverter;
import org.eclipse.scout.rt.spec.client.out.html.HtmlConverter;
import org.eclipse.scout.rt.spec.client.out.html.TemplateUtility;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiLinkPostProcessor;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiWriter;
import org.eclipse.scout.rt.spec.client.screenshot.FormPrinter;
import org.eclipse.scout.rt.spec.client.screenshot.PrintFormListener;

/**
 *
 */
public abstract class AbstractFormSpecGen {
  private final SpecFileConfig m_fileConfig;

  public AbstractFormSpecGen(String pluginName) {
    m_fileConfig = new SpecFileConfig(pluginName);
  }

  public void printForm() throws ProcessingException {
    IForm form = createAndStartForm();
    File screensDir = m_fileConfig.getImageDir();
    FormPrinter h = new FormPrinter(screensDir);

    form.addFormListener(new PrintFormListener(h));
    form.waitFor();
  }

  public void printAllFields() throws ProcessingException {
    // prepare form
    IForm form = createAndStartForm();

    // template
    DefaultDocConfig template = new DefaultDocConfig();

    // get the data
    String formId = form.getClass().getName();
    FormSpecGenerator g = new FormSpecGenerator(template);
    IDocSection formDoc = g.getDocSection(form);

    // write
    File out = m_fileConfig.getSpecDir();
    out.mkdirs();

    File preprocessed = m_fileConfig.getMediawikiRawDir();
    File wiki = SpecIOUtility.createNewFile(preprocessed, formId, "_raw.mediawiki");
    Writer mediaWikiWriter = SpecIOUtility.createWriter(wiki);
    File properties = SpecIOUtility.createNewFile(preprocessed, formId, ".properties");
    Writer linkIdWriter = SpecIOUtility.createWriter(properties);

    //
    FormPrinter printer = new FormPrinter(m_fileConfig.getImageDir());
    File[] printFiles = printer.getPrintFiles(form);
    String[] imagePaths = SpecIOUtility.addPrefix(SpecIOUtility.getRelativePaths(printFiles, m_fileConfig.getSpecDir()), "../");
    MediawikiWriter wikimediaFormWriter = new MediawikiWriter(linkIdWriter, mediaWikiWriter, formDoc, imagePaths);
    wikimediaFormWriter.write();

    File mediawiki = m_fileConfig.getMediawikiDir();
    File finalWiki = SpecIOUtility.createNewFile(mediawiki, formId, ".mediawiki");
    Properties prop = SpecIOUtility.loadProperties(properties);
    MediawikiLinkPostProcessor postproc = new MediawikiLinkPostProcessor(prop);
    postproc.replaceLinks(wiki, finalWiki);

    convertToHTML(formId, finalWiki);
  }

  private String[] getImages(String id, File dir) {
    String[] images = dir.list(new SpecImageFilter(id));
    return images;
  }

  protected File convertToDocBook(File out, String id, File mediaWiki) {
    File docBook = new File(out, id + ".xml");
    DocBookConverter c = new DocBookConverter();
    c.convertWikiToDocBook(mediaWiki, docBook);
    return docBook;
  }

  private File convertToHTML(String id, File mediaWiki) throws ProcessingException {
    File htmlDir = m_fileConfig.getHtmlDir();
    htmlDir.mkdirs();
    File htmlFile = SpecIOUtility.createNewFile(htmlDir, id, ".html");

    // copy css
    File css = new File(htmlDir, "default.css");
    TemplateUtility.copyDefaultCss(css);

    HtmlConverter htmlConverter = new HtmlConverter(css);
    htmlConverter.convertWikiToHtml(mediaWiki, htmlFile);
    return htmlFile;
  }

  protected abstract IForm createAndStartForm() throws ProcessingException;

  protected SpecFileConfig getFileConfig() {
    return m_fileConfig;
  }

}
