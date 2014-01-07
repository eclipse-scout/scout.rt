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
import java.util.List;
import java.util.Properties;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.StringUtility.ITagProcessor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.link.DocLink;

/**
 * Post processor to replace texts within {@value SpecReplaceUtility#REPLACE_TAG_NAME}
 */
public class MediawikiLinkPostProcessor implements ITagProcessor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MediawikiLinkPostProcessor.class);
  public static final String NEWLINE = System.getProperty("line.separator");

  private final Properties m_properties;

  /**
   * @param properties
   *          Properties for replacing references.
   */
  public MediawikiLinkPostProcessor(Properties properties) {
    m_properties = properties;
  }

  /**
   * Replaces links within {@value SpecReplaceUtility#REPLACE_TAG_NAME} with the actual link.
   * 
   * @param in
   *          input file
   * @param out
   *          output file
   * @throws ProcessingException
   */
  public void replaceLinks(File in, File out) throws ProcessingException {
    FileReader reader = null;
    BufferedReader br = null;
    FileWriter writer = null;
    try {
      reader = new FileReader(in);
      br = new BufferedReader(reader);

      writer = new FileWriter(out);
      String line;
      while ((line = br.readLine()) != null) {
        String replacedLine = replaceLinkText(line);
        writer.write(replacedLine);
        writer.write(NEWLINE);
      }
    }
    catch (FileNotFoundException e) {
      throw new ProcessingException("Error replacing links", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Error replacing links", e);
    }
    finally {
      if (br != null) {
        try {
          br.close();
        }
        catch (IOException e) {
          // nop
        }
      }
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

  /**
   * @param input
   *          XML containing id and name, e.g.
   *          {@code
   * <id>org.eclipse.scout.rt.spec.example.ui.swing.form.TestPersonForm$MainBox<id/><name>MainBox<name/>
   * }
   * @return
   *         //
   */
  public String replaceLinkText(String input) {
    return StringUtility.replaceTags(input, DocLink.REPLACE_TAG_NAME, this);
  }

  @Override
  public String processTag(String tagName, String tagContent) {
    try {
      List<DocLink> links = DocLink.parse(tagContent);
      String link = getFirstMatchAsLink(links);
      if (link != null) {
        return link;
      }
      return getFallbackString(links);
    }
    catch (ProcessingException e) {
      LOG.error("Replacing Tags failed: " + tagContent, e);
    }
    return null;
  }

  private String getFallbackString(List<DocLink> links) {
    if (links.size() > 0) {
      return links.get(0).getDisplayName();
    }
    return null;
  }

  private String getFirstMatchAsLink(List<DocLink> links) {
    for (DocLink link : links) {
      String targetResult = (String) m_properties.get(link.getTargetId());
      if (targetResult != null) {
        return createRelativeWikiLink(targetResult, link.getDisplayName());
      }
    }
    return null;
  }

  private String createRelativeWikiLink(String target, String name) {
    return "[[#" + target + "|" + name + "]]";
  }
}
