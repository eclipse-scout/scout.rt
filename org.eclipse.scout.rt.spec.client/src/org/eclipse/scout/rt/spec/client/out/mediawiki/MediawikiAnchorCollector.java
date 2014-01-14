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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.SpecIOUtility;
import org.eclipse.scout.rt.spec.client.link.DocLink;

/**
 *
 */
public class MediawikiAnchorCollector {
  private final File m_input;

  private static final String LINK_START = "[[";
  private static final String LINK_TARGET_END = "|";
  private static final String LINK_END = "]]";

  public MediawikiAnchorCollector(File input) {
    m_input = input;
  }

  /**
   * @param in
   *          a file containing anchors
   * @param out
   *          the links Property file
   * @throws ProcessingException
   */
  public void storeAnchors(File out) throws ProcessingException {
    Properties props = getAnchorProperties();
    FileWriter writer = null;
    try {
      writer = new FileWriter(out, true);
      props.store(writer, null);
    }
    catch (IOException e) {
      try {
        if (writer != null) {
          writer.close();
        }
      }
      catch (IOException e1) {
        //nop
      }
    }
  }

  /**
   * Read all anchors {{XY}} in a file and creates properties of the form
   * XY=fileName#XY for them.
   **/
  public Properties getAnchorProperties() throws ProcessingException {
    List<String> anchors = collectAnchors(m_input);
    Properties props = new Properties();
    for (String a : anchors) {
      props.setProperty(a, m_input.getName() + "#" + a);
    }
    return props;
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
      while ((line = br.readLine()) != null) {
        String repl = replaceText(line, p);
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

  public String replaceText(String s, Properties p) {

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
      System.out.println(tag + " not found in file " + m_input.getName() + ", replacing with plain text: " + link.getDisplayName());
      return link.getDisplayName();
    }
    return toWikiString(link);
  }

  private String toWikiString(DocLink link) {
    return LINK_START + link.getTargetId() + LINK_TARGET_END + link.getDisplayName() + LINK_END;
  }

  /**
   * Collects all anchors: texts in double braces: {{ }} in a file
   **/
  public List<String> collectAnchors(File in) throws ProcessingException {
    FileReader reader = null;
    BufferedReader br = null;
    try {
      reader = new FileReader(in);
      br = new BufferedReader(reader);
      ArrayList<String> list = new ArrayList<String>();

      String line;
      while ((line = br.readLine()) != null) {
        String tag = readAnchorTag(line);
        if (!tag.isEmpty()) {
          list.add(tag);
        }
      }
      return list;
    }
    catch (FileNotFoundException e) {
      throw new ProcessingException("Error replacing links", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Error replacing links", e);
    }
    finally {
      try {
        if (br != null) {
          br.close();
        }
      }
      catch (IOException e) {
        // NOP
      }
    }
  }

  /**
   * @param inputLine
   * @return text enclosed in double braces: {{ }}
   */
  public String readAnchorTag(String inputLine) {
    String anchorPattern = "\\{\\{([^\\{\\}]+?)\\}\\}";
    Pattern p = Pattern.compile(anchorPattern);
    Matcher m = p.matcher(inputLine);
    if (m.find()) {
      return m.group().replaceAll("\\{", "").replaceAll("\\}", "").trim();
    }
    return "";
  }
}
