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
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.spec.client.config.DefaultDocConfig;
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
import org.osgi.framework.Bundle;

/**
 *
 */
public abstract class AbstractFormSpecGen {
  public static final String NEWLINE = System.getProperty("line.separator");

  public void printForm() throws ProcessingException {
    IForm form = createAndStartForm();
    File screensDir = getImageDir();
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
    File out = getSpecDir();
    out.mkdirs();

    File preprocessed = getMediawikiRawDir();
    File wiki = SpecIOUtility.createNewFile(preprocessed, formId, "_raw.mediawiki");
    Writer mediaWikiWriter = SpecIOUtility.createWriter(wiki);
    File properties = SpecIOUtility.createNewFile(preprocessed, formId, ".properties");
    Writer linkIdWriter = SpecIOUtility.createWriter(properties);

    //
    FormPrinter printer = new FormPrinter(getImageDir());
    File[] printFiles = printer.getPrintFiles(form);
    String[] imagePaths = SpecIOUtility.addPrefix(SpecIOUtility.getRelativePaths(printFiles, getSpecDir()), "../");
    MediawikiWriter wikimediaFormWriter = new MediawikiWriter(linkIdWriter, mediaWikiWriter, formDoc, imagePaths);
    wikimediaFormWriter.write();

    File mediawiki = getMediawikiDir();
    File finalWiki = SpecIOUtility.createNewFile(mediawiki, formId, ".mediawiki");
    Properties prop = SpecIOUtility.loadProperties(properties);
    MediawikiLinkPostProcessor postproc = new MediawikiLinkPostProcessor(prop);
    postproc.replaceLinks(wiki, finalWiki);

    convertToHTML(out, formId, finalWiki);
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

  private File convertToHTML(File out, String id, File mediaWiki) throws ProcessingException {
    try {
      // copy css
      File htmlDir = getHtmlDir();
      htmlDir.mkdirs();
      File css = new File(htmlDir, "default.css");
      TemplateUtility.copyDefaultCss(css);

      File htmlFile = SpecIOUtility.createNewFile(htmlDir, id, ".html");
      HtmlConverter htmlConverter = new HtmlConverter(css);
      htmlConverter.convertWikiToHtml(mediaWiki, htmlFile);
      return htmlFile;
    }
    catch (IOException e) {
      throw new ProcessingException("Error writing mediawiki file.", e);
    }
  }

  /**
   * @return root directory for the generated output
   * @throws ProcessingException
   */
  protected File getSpecDir() throws ProcessingException {
    try {
      Bundle bundle = Platform.getBundle(getPluginName());
      URL bundleRoot = bundle.getEntry("/");
      URI uri = FileLocator.resolve(bundleRoot).toURI();
      File targetFile = new File(uri);
      return new File(targetFile + File.separator + "target" + File.separator + "spec");
    }
    catch (IOException e) {
      throw new ProcessingException("Folder not found", e);
    }
    catch (URISyntaxException e) {
      throw new ProcessingException("Folder not found", e);
    }
  }

  /**
   * Location of referenced images
   * 
   * @return image directory
   * @throws ProcessingException
   */
  protected File getImageDir() throws ProcessingException {
    return new File(getSpecDir() + "/images");
  }

  protected File getHtmlDir() throws ProcessingException {
    return new File(getSpecDir(), "html");
  }

  protected File getMediawikiDir() throws ProcessingException {
    return new File(getSpecDir(), "mediawiki");
  }

  protected File getMediawikiRawDir() throws ProcessingException {
    return new File(getSpecDir(), "mediawiki_raw");
  }

  protected abstract String getPluginName() throws ProcessingException;

  protected abstract IForm createAndStartForm() throws ProcessingException;
}
