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
package org.eclipse.scout.rt.spec.client.out.docbook;

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
import org.eclipse.mylyn.wikitext.core.parser.builder.DocBookDocumentBuilder;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class DocBookConverter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DocBookConverter.class);
  private static final String ENCODING = "utf-8";

  public void convertWikiToDocBook(File in, File out) {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out.getPath()), ENCODING));
      DocBookDocumentBuilder builder = new DocBookDocumentBuilder(writer);

      MarkupParser parser = new MarkupParser(new MediaWikiLanguage());
      parser.setBuilder(builder);
      Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in.getPath()), ENCODING));
      parser.parse(reader);
      writer.flush();
      writer.close();
    }
    catch (FileNotFoundException e) {
      LOG.error("Error converting to docbook", e);
    }
    catch (IOException e) {
      LOG.error("Error converting to docbook", e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          // nop
        }
      }
    }
  }

}
