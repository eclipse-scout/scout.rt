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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.SpecIOUtility.IStringProcessor;

// TODO ASA javadoc unittest
public class HtmlFilePostProcessor implements ISpecProcessor {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(HtmlFilePostProcessor.class);
  private String[] m_filenames;

  /**
   * @param string
   */
  public HtmlFilePostProcessor(String... filenames) {
    m_filenames = filenames;
  }

  @Override
  public void process() throws ProcessingException {
    for (final String filename : m_filenames) {
      File file = new File(SpecIOUtility.getSpecFileConfigInstance().getHtmlDir(), filename);
      if (file.exists()) {
        P_FirstProcessor firstProcessor = new P_FirstProcessor(filename);
        SpecIOUtility.process(file, firstProcessor);
        SpecIOUtility.replaceAll(file, firstProcessor.m_anchorIds);
      }
      else {
        LOG.warn("File " + file.getPath() + " does not exists!");
      }
    }
  }

  /**
   * <li>replaces <?xml version='1.0' encoding='utf-8' ?> by "" <li>strips filename from local links <li>builds a map
   * with anchorIds
   */
  protected final class P_FirstProcessor implements IStringProcessor {
    private final String m_filename;
    protected HashMap<String, String> m_anchorIds = new HashMap<String, String>();
    private long m_counter = 0;

    /**
     * @param filename
     */
    private P_FirstProcessor(String filename) {
      m_filename = filename;
    }

    @Override
    public String processLine(String input) {
      String line = input.replace("<?xml version='1.0' encoding='utf-8' ?>", "");
      line = line.replace(m_filename + "#", "#");
      line = line.replaceAll("\\<span\\s+id=", "<a name=");

      Pattern pattern = Pattern.compile("\\<a name=\"([A-Za-z][A-Za-z0-9_\\.-]+)\"");
      Matcher matcher = pattern.matcher(line);
      while (matcher.find()) {
        m_anchorIds.put("\"" + matcher.group(1) + "\"", "\"loa_" + m_counter + "\"");
        m_anchorIds.put("\"#" + matcher.group(1) + "\"", "\"#loa_" + m_counter + "\"");
        ++m_counter;
      }
      return line;
    }
  }

}
