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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.SpecIOUtility.IStringProcessor;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.osgi.framework.Bundle;

/**
 * A post processor for generating linear output files (mediawiki and html)
 * <p>
 * The generated output files will have the same base filename as the configuration file with differnt ending.
 */
public class LinearOutputPostProcessor implements ISpecProcessor {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(LinearOutputPostProcessor.class);
  public static final String ANCHOR_PREFIX = "lo_";
  private File m_configFile;
  private File m_outputFile;

  /**
   * @param configFile
   *          Filename of configuration file which will be searched in {@link SpecFileConfig#getRelativeSourceDirPath()}
   *          in all {@link SpecFileConfig#getSourceBundles()} according their priority.
   *          <p>
   *          The configuration file is expected to have the file ending <code>.config</code>
   * @throws ProcessingException
   */
  public LinearOutputPostProcessor(String configFile) throws ProcessingException {
    List<Bundle> sourceBundles = SpecIOUtility.getSpecFileConfigInstance().getSourceBundles();
    Collections.reverse(sourceBundles);
    for (Bundle bundle : sourceBundles) {
      List<String> fileList = SpecIOUtility.listFiles(bundle, SpecIOUtility.getSpecFileConfigInstance().getRelativeSourceDirPath(), getConfigFileFilter(configFile));
      if (fileList.size() > 0) {
        m_configFile = new File(SpecIOUtility.getSpecFileConfigInstance().getSpecDir(), configFile);
        SpecIOUtility.copyFile(bundle, SpecIOUtility.getSpecFileConfigInstance().getRelativeSourceDirPath() + File.separator + configFile, m_configFile);
        break;
      }
    }
    if (m_configFile == null || !m_configFile.exists()) {
      LOG.error("Configfile " + configFile + " could not be copied!");
    }
  }

  private FilenameFilter getConfigFileFilter(final String configFile) {
    return new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.equals(configFile);
      }
    };
  }

  @Override
  public void process() throws ProcessingException {
    if (m_configFile == null || !m_configFile.exists()) {
      return;
    }
    concatenateFiles();
    prefixAnchorsAndLinks();
  }

  // TODO ASA unittest (also test that image links are not prefixed and multiple links on same line)
  protected void prefixAnchorsAndLinks() throws ProcessingException {
    SpecIOUtility.process(m_outputFile, new P_AnchorProcessor("(\\{\\{a:)([^}]+}})"));
    SpecIOUtility.process(m_outputFile, new P_AnchorProcessor("(\\[\\[)([A-Za-z][A-Za-z0-9_\\.-]+\\|)"));
  }

  protected static class P_AnchorProcessor implements IStringProcessor {

    private String m_twoGroupRegex;

    /**
     * @param string
     */
    public P_AnchorProcessor(String twoGroupRegex) {
      m_twoGroupRegex = twoGroupRegex;
    }

    @Override
    public String processLine(String input) {
      Pattern pattern = Pattern.compile(m_twoGroupRegex);
      Matcher matcher = pattern.matcher(input);
      StringBuilder sb = new StringBuilder();
      int index = 0;
      while (matcher.find()) {
        sb.append(input.substring(index, matcher.start(0)));
        sb.append(matcher.group(1)).append(ANCHOR_PREFIX).append(matcher.group(2));
        index = matcher.end();
      }
      sb.append(input.substring(index));
      return sb.toString();
    }

  }

  private void concatenateFiles() throws ProcessingException {
    String outputFileName = m_configFile.getName().replace(".config", "");
    List<String> sourceFiles = IOUtility.readLines(m_configFile);
    if (!StringUtility.isNullOrEmpty(outputFileName)) {
      PrintWriter writer = null;
      m_outputFile = new File(SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir(), outputFileName + ".mediawiki");
      m_outputFile.delete();
      try {
        m_outputFile.createNewFile();
        writer = new PrintWriter(m_outputFile);
        for (String sourceFile : sourceFiles) {
          String mediawikiFile = sourceFile + ".mediawiki";
          LOG.info("appending file. " + mediawikiFile);
          File file = new File(SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir(), mediawikiFile);
          if (file.exists()) {
            IOUtility.appendFile(writer, file);
            writer.flush();
          }
          else {
            LOG.warn("File does not exist: " + file.getPath() + " Skipping...");
          }
        }
      }
      catch (IOException e) {
        throw new ProcessingException("Error creating output file.", e);
      }
      finally {
        if (writer != null) {
          writer.close();
        }
      }
    }
  }

}
