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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.SpecIOUtility.IStringProcessor;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.rt.spec.client.gen.DocGenUtility;
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
  private String m_outputFileName;

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
    else {
      m_outputFileName = m_configFile.getName().replace(".config", ".mediawiki");
    }
  }

  protected FilenameFilter getConfigFileFilter(final String configFile) {
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
    File outputFile = concatenateFiles();
    prefixAnchorsAndLinks(outputFile);
  }

  protected void prefixAnchorsAndLinks(File outputFile) throws ProcessingException {
    SpecIOUtility.process(outputFile, new P_AnchorProcessor("(\\{\\{a:)([^}]+}})"));
    SpecIOUtility.process(outputFile, new P_AnchorProcessor("(\\[\\[)([A-Za-z][A-Za-z0-9_\\.-]+\\|)"));
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

  protected File concatenateFiles() throws ProcessingException {
    File outputFile = null;
    List<String> configEntries = IOUtility.readLines(m_configFile, "UTF-8");
    List<String> referencedFileNames = new ArrayList<String>();
    PrintWriter writer = null;
    outputFile = new File(SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir(), m_outputFileName);
    outputFile.delete();
    try {
      outputFile.createNewFile();
      writer = new PrintWriter(outputFile);
      for (String configEntry : configEntries) {
        if (!isComment(configEntry)) {
          File file = findFileForConfigEntry(configEntry);
          if (file != null) {
            referencedFileNames.add(file.getName());
            IOUtility.appendFile(writer, file);
            writer.flush();
          }
          else {
            LOG.warn("No file found for config entry: " + configEntry + ". Skipping...");
          }
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
    logMissingEntries(referencedFileNames);
    return outputFile;
  }

  /**
   * log all mediawiki files that are not referenced in config
   * 
   * @param referencedFilenames
   * @throws ProcessingException
   */
  protected void logMissingEntries(List<String> referencedFilenames) throws ProcessingException {
    final String suffix = ".mediawiki";
    String[] fileList = SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir().list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(suffix);
      }
    });
    if (fileList != null) {
      for (String file : fileList) {
        if (!referencedFilenames.contains(file) && !m_outputFileName.equals(file)) {
          LOG.warn("There is no entry for the existing file " + file + " in " + m_configFile.getName() + ".");
        }
      }
    }
  }

  protected boolean isComment(String configEntry) {
    return configEntry.startsWith("#");
  }

  protected File findFileForConfigEntry(String configEntry) throws ProcessingException {
    // priority 1: try if entry is a file base name
    File file = new File(SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir(), configEntry + ".mediawiki");
    if (file.exists()) {
      return file;
    }
    // priority 2: try doc entity class name
    Set<Class> allClasses = DocGenUtility.getAllDocEntityClasses();
    for (Class c : allClasses) {
      if (configEntry.equals(c.getSimpleName()) || configEntry.equals(c.getName())) {
        File fileForClass = findFileForClass(c);
        if (fileForClass == null) {
          Set<Class> subclasses = getSubClassesOrderdByDistance(c, allClasses);
          for (Class subclass : subclasses) {
            fileForClass = findFileForClass(subclass);
            if (fileForClass != null) {
              break;
            }
          }
        }
        return fileForClass;
      }
    }
    return null;
  }

  protected Set<Class> getSubClassesOrderdByDistance(final Class<?> c, Set<Class> allClasses) {
    TreeSet<Class> subclasses = new TreeSet<Class>(new Comparator<Class>() {
      @Override
      public int compare(Class o1, Class o2) {
        Integer distanceO1 = getDistance(o1, c);
        Integer distanceO2 = getDistance(o2, c);
        return distanceO1.compareTo(distanceO2);
      }
    });
    for (Class candidate : allClasses) {
      if (c.isAssignableFrom(candidate) && c != candidate) {
        subclasses.add(candidate);
      }
    }
    return subclasses;
  }

  /**
   * evaluates the distance in the class hierarchy between a sub class and a super class
   * 
   * @param subClass
   * @param superClass
   * @return
   * @throws IllegalArgumentException
   *           if <code>subClass</code> is not a real sub class of <code>superClass</code> (In particular this is the
   *           case if one of them is an interface type or they are equal.)
   */
  protected static int getDistance(Class subClass, Class<?> superClass) {
    if (!superClass.isAssignableFrom(subClass) || subClass == superClass || subClass.isInterface() || superClass.isInterface()) {
      throw new IllegalArgumentException();
    }
    int distance = 0;
    Class c = subClass;
    while (c != superClass) {
      c = c.getSuperclass();
      ++distance;
    }
    return distance;
  }

  protected File findFileForClass(Class c) throws ProcessingException {
    File file = new File(SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir(), SpecUtility.getSpecFileBaseName(c) + ".mediawiki");
    if (file.exists()) {
      return file;
    }
    else {
      return null;
    }
  }
}
