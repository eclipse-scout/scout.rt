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

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.osgi.framework.Bundle;

/**
 * A post processor for generating linear output files (mediawiki and html)
 * <p>
 * The generated output files will have the same base filename as the configuration file with differnt ending.
 */
public class LinearOutputPostProcessor extends AbstractSpecGen implements ISpecProcessor {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(LinearOutputPostProcessor.class);
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
    List<Bundle> sourceBundles = getFileConfig().getSourceBundles();
    Collections.reverse(sourceBundles);
    for (Bundle bundle : sourceBundles) {
      List<String> fileList = SpecIOUtility.listFiles(bundle, getFileConfig().getRelativeSourceDirPath(), getConfigFileFilter(configFile));
      if (fileList.size() > 0) {
        m_configFile = new File(getFileConfig().getSpecDir(), configFile);
        SpecIOUtility.copyFile(bundle, getFileConfig().getRelativeSourceDirPath() + File.separator + configFile, m_configFile);
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
    String outputFileName = m_configFile.getName().replace(".config", "");
    List<String> sourceFiles = IOUtility.readLines(m_configFile);
    if (!StringUtility.isNullOrEmpty(outputFileName)) {
      PrintWriter writer = null;
      m_outputFile = new File(getFileConfig().getMediawikiDir(), outputFileName + ".mediawiki");
      m_outputFile.delete();
      try {
        m_outputFile.createNewFile();
        writer = new PrintWriter(m_outputFile);
        for (String sourceFile : sourceFiles) {
          String mediawikiFile = sourceFile + ".mediawiki";
          LOG.info("appending file. " + mediawikiFile);
          IOUtility.appendFile(writer, new File(getFileConfig().getMediawikiDir(), mediawikiFile));
          writer.flush();
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

  // TODO ASA what about links?
//  private void replaceLinks(File f) throws ProcessingException {
//    MediawikiAnchorCollector c = new MediawikiAnchorCollector(f);
//    c.replaceLinks(f, getFileConfig().getLinksFile());
//  }
//
//  private void replaceWikiFileLinks(File htmlFile) throws ProcessingException {
//    HashMap<String, String> map = new HashMap<String, String>();
//    map.put("/wiki/", "");
//    map.put(".mediawiki", ".html");
//    SpecIOUtility.replaceAll(htmlFile, map);
//  }

}
