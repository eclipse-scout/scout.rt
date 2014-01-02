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
package org.eclipse.scout.rt.spec.client.out.html;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder.Stylesheet;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Converts a wikimedia file to html
 * 
 * @author jgu
 */
public class HtmlConverter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HtmlConverter.class);

  private static final String ENCODING = "utf-8";
  private final Stylesheet m_css;

  public HtmlConverter(File css) {
    m_css = new Stylesheet(css);
  }

  public void convertWikiToHtml(File in, File out) throws ProcessingException {
    try {

      BufferedWriter htmlWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out.getPath()), "UTF-8"));

      HtmlDocumentBuilder builder = new HtmlDocumentBuilder(htmlWriter);
      builder.addCssStylesheet(m_css);
      builder.setUseInlineStyles(false);
      builder.setEncoding(ENCODING);

      MarkupParser parser = new MarkupParser(new MediaWikiLanguage());
      parser.setBuilder(builder);
      Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in.getPath()), ENCODING));
      parser.parse(reader);

      htmlWriter.flush();
      htmlWriter.close();
    }
    catch (FileNotFoundException e) {
      throw new ProcessingException("Could not convert document to html", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Could not convert document to html", e);
    }
  }

}
