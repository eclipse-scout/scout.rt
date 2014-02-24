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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.SpecIOUtility;
import org.eclipse.scout.rt.spec.client.link.DocLink;

/**
 *
 */
// TODO ASA cleanup/rename MediawikiAnchorCollector; consolidate with MediawikiLinkTargetManager
public class MediawikiAnchorCollector {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MediawikiAnchorCollector.class);
  private final File m_input;

  private static final String LINK_START = "[[";
  private static final String LINK_TARGET_END = "|";
  private static final String LINK_END = "]]";

  public MediawikiAnchorCollector(File input) {
    m_input = input;
  }

  public void replaceLinks(File in, File linksFile) throws ProcessingException {
    FileReader reader = null;
    FileWriter writer = null;
    BufferedReader br = null;
    Properties p = new Properties();
    File temp = new File(in.getParent(), in.getName() + "_temp");

    try {
      reader = new FileReader(in);
      writer = new FileWriter(temp);
      br = new BufferedReader(reader);
      p.load(new FileReader(linksFile));

      String line;
      // TODO ASA refactor: use SpecIOUtility.process(...)
      while ((line = br.readLine()) != null) {
        String repl = replaceLink(line, p);
        writer.write(repl);
        writer.write(System.getProperty("line.separator"));
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        if (br != null) {
          br.close();
        }
      }
      catch (IOException e) {
        //nop
      }

      try {
        if (writer != null) {
          writer.close();
        }
      }
      catch (IOException e) {
        //nop
      }
    }
    SpecIOUtility.copy(temp, in);
    temp.delete();
  }

  protected String replaceLink(String s, Properties p) {

    int startPos = 0;
    boolean finished = false;

    while (!finished) {
      int targetStartPos = s.indexOf(LINK_START, startPos);
      int targetEndPos = s.indexOf(LINK_TARGET_END, targetStartPos);
      int linkNameStartPos = targetEndPos + 1;
      int linkNameEndPos = s.indexOf(LINK_END, targetEndPos);
      if (isLinkFound(startPos, targetStartPos, targetEndPos, linkNameEndPos)) {
        String targetId = s.substring(targetStartPos + LINK_START.length(), targetEndPos);
        String displayName = s.substring(linkNameStartPos, linkNameEndPos);
        DocLink link = new DocLink(targetId, displayName);
        String textBeforeLink = s.substring(0, targetStartPos);
        String textAfterLink = s.substring(linkNameEndPos + LINK_END.length());
        String newLink = replace(link, p);
        s = textBeforeLink + newLink + textAfterLink;
        startPos = targetStartPos + LINK_START.length() + newLink.length();
      }
      else {
        finished = true;
      }
    }
    return s;
  }

  private boolean isLinkFound(int startPos, int targetStartPos, int targetEndPos, int linkNameEndPos) {
    return targetStartPos > 0 && targetEndPos > 0 && linkNameEndPos > 0 && targetStartPos > startPos;
  }

  private String replace(DocLink link, Properties p) {
    String tag = link.getTargetId();
    if (p.containsKey(tag)) {
      DocLink newLink = new DocLink((String) p.getProperty(tag), link.getDisplayName());
      return toWikiString(newLink);
    }
    if (!tag.startsWith("#") && !tag.startsWith("Image")) {
      LOG.warn(tag + " not found in file " + m_input.getName() + ", replacing with plain text: " + link.getDisplayName());
      return link.getDisplayName();
    }
    return toWikiString(link);
  }

  private String toWikiString(DocLink link) {
    return LINK_START + link.getTargetId() + LINK_TARGET_END + link.getDisplayName() + LINK_END;
  }

}
