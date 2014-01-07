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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 *
 */
public class MediawikiAnchorCollector {
  Properties m_properties;

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
      return m.group().replaceAll("\\{", "").replaceAll("\\}", "");
    }
    return "";
  }
}
