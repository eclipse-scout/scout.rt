/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Configuration {
  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

  public static Configuration get() {
    return BEANS.get(Configuration.class);
  }

  private String m_sourceModuleDirectory;
  private String m_targetModuleDirectory;
  private String m_namespace;
  private String m_persistLibraryName;
  private String m_persistLibraryFileName;

  /**
   * Paths and names based on Taskliste-IntelliJ-Ecma.xlsx
   */
  protected Configuration() {
    String sourceBase = getConfiguredSourceBase();
    String targetBase = getConfiguredTargetBase();
    String moduleName = getConfiguredModule();
    if (moduleName == null) {
      return;
    }

    switch (moduleName) {
      case "org.eclipse.scout.rt.ui.html":
        setSourceModuleDirectory(sourceBase + "/org.eclipse.scout.rt/" + moduleName);
        setTargetModuleDirectory(targetBase + "/org.eclipse.scout.rt/" + moduleName);
        setNamespace("scout");
        setPersistLibraryName("@eclipse-scout/core");
        setPersistLibraryFileName("01-api_eclipse-scout_core.json");
        break;
      case "org.eclipse.scout.rt.svg.ui.html":
        setSourceModuleDirectory(sourceBase + "/org.eclipse.scout.rt/" + moduleName);
        setTargetModuleDirectory(targetBase + "/org.eclipse.scout.rt/" + moduleName);
        setNamespace("scout");
        setPersistLibraryName("@eclipse-scout/svg");
        setPersistLibraryFileName("02-api_eclipse-scout_svg.json");
        break;
      case "org.eclipse.scout.jswidgets.ui.html":
        setSourceModuleDirectory(sourceBase + "/org.eclipse.scout.docs/code/widgets/" + moduleName);
        setTargetModuleDirectory(targetBase + "/org.eclipse.scout.docs/code/widgets/" + moduleName);
        setNamespace("jswidgets");
        setPersistLibraryName("@eclipse-scout/demo-jswidgets");
        setPersistLibraryFileName("10-api_demo-jswidgets.json");
        break;

      case "org.eclipse.scout.widgets.heatmap.ui.html":
        setSourceModuleDirectory(sourceBase + "/org.eclipse.scout.docs/code/widgets/" + moduleName);
        setTargetModuleDirectory(targetBase + "/org.eclipse.scout.docs/code/widgets/" + moduleName);
        setNamespace("scout");
        setPersistLibraryName("@eclipse-scout/demo-widgets-heatmap");
        setPersistLibraryFileName("40-api_demo-widgets-heatmap.json");
        break;
      case "org.eclipse.scout.widgets.ui.html":
        setSourceModuleDirectory(sourceBase + "/org.eclipse.scout.docs/code/widgets/" + moduleName);
        setTargetModuleDirectory(targetBase + "/org.eclipse.scout.docs/code/widgets/" + moduleName);
        setNamespace("widgets");
        setPersistLibraryName("@eclipse-scout/demo-widgets");
        setPersistLibraryFileName("41-api_demo-widgets.json");
        break;

      case "com.bsiag.scout.rt.ui.html":
        setSourceModuleDirectory(sourceBase + "/bsi.scout.rt/" + moduleName);
        setTargetModuleDirectory(targetBase + "/bsi.scout.rt/" + moduleName);
        setNamespace("bsiscout");
        setPersistLibraryName("@eclipse-scout/bsi");
        setPersistLibraryFileName("50-api_bsi_scout_core.json");
        break;
      case "com.bsiag.studio.ui.html":
        setSourceModuleDirectory(sourceBase + "/bsistudio/" + moduleName);
        setTargetModuleDirectory(targetBase + "/bsistudio/" + moduleName);
        setNamespace("studio");
        setPersistLibraryName("@eclipse-scout/studio");
        setPersistLibraryFileName("99-api_bsi_studio.json");
        break;
      default:
        throw new ProcessingException("unknown module " + moduleName);
    }
    LOG.info("SourceModuleDirectory: " + getSourceModuleDirectory());
    LOG.info("TargetModuleDirectory: " + getTargetModuleDirectory());
    LOG.info("Namespace: " + getNamespace());
    LOG.info("PersistLibraryName: " + getPersistLibraryName());
    LOG.info("PersistLibraryFileName: " + getPersistLibraryFileName());
    LOG.info("LibraryApiDirectory: " + getLibraryApiDirectory());
  }

  protected String getConfiguredSourceBase() {
    return null;
  }

  protected String getConfiguredTargetBase() {
    return null;
  }

  protected String getConfiguredModule() {
    return null;
  }

  /**
   * @return the source directory to be migrated. must exist. Usually something like '.../[com.bsiag.bsicrm.]ui.html'
   */
  public Path getSourceModuleDirectory() {
    return Paths.get(m_sourceModuleDirectory);
  }

  public void setSourceModuleDirectory(String sourceModuleDirectory) {
    m_sourceModuleDirectory = sourceModuleDirectory;
  }

  /**
   * @return the directory where the result of the migration is written to. Must not exist, the directory will be
   *         created if it or one of its parents does not exist.
   */
  public Path getTargetModuleDirectory() {
    return Paths.get(m_targetModuleDirectory);
  }

  public void setTargetModuleDirectory(String targetModuleDirectory) {
    m_targetModuleDirectory = targetModuleDirectory;
  }

  /**
   * @return whether or not the target folder should be wiped out before write the result of the migration.
   */
  public boolean cleanTargetBeforeWriteFiles() {
    return true;
  }

  /**
   * @return (e.g. scout | bsicrm | amag) look at the js files in the module to migrate for the correct namespace.
   */
  public String getNamespace() {
    return m_namespace;
  }

  public void setNamespace(String namespace) {
    m_namespace = namespace;
  }

  /**
   * The library name under which the migrated API is stored in the JSON format. The library API can be used as input
   * for a dependent module migration. If the persistLibraryFile is set the persistLibraryName must also be set.
   *
   * @return the library name (npm name) of the library which is optionally written.
   */
  public String getPersistLibraryName() {
    return m_persistLibraryName;
  }

  public void setPersistLibraryName(String persistLibraryName) {
    m_persistLibraryName = persistLibraryName;
  }

  /**
   * In case the persist library file is set the API of the migrated module is written in JSON format to this file. If
   * the persistLibraryFile is set the persistLibraryName must also be set.
   *
   * @return a file to persist the api.
   */
  public String getPersistLibraryFileName() {
    return m_persistLibraryFileName;
  }

  public void setPersistLibraryFileName(String persistLibraryFileName) {
    m_persistLibraryFileName = persistLibraryFileName;
  }

  public Path getPersistLibraryFile() {
    return Paths.get(getLibraryApiDirectory().toString(), getPersistLibraryFileName());
  }

  /**
   * @return The folder where all library api's used for this migration are located. In this folder might be several
   *         *.json * files from previous migrations.
   */
  public Path getLibraryApiDirectory() {
    return Paths.get(getConfiguredSourceBase() + "/ecma6-mig-apis");
  }

  /**
   * @return the prefix will be used for every To Do comment written by the migrator.
   */
  public String getTodoPrefix() {
    return "TODO MIG: ";
  }

  @PostConstruct
  public void validate() {
    if (getSourceModuleDirectory() == null || !Files.exists(getSourceModuleDirectory()) || !Files.isDirectory(getSourceModuleDirectory())) {
      throw new VetoException(configurationErrorMessage("'sourceModuleDirectory' with value: '" + getSourceModuleDirectory() + "' is not set, does not exist or is not a directory."));
    }
    if (StringUtility.isNullOrEmpty(getNamespace())) {
      throw new VetoException(configurationErrorMessage("'namespace' with value: '" + getNamespace() + "' is not set."));
    }
    if (getPersistLibraryFileName() != null && StringUtility.isNullOrEmpty(getPersistLibraryName())) {
      throw new VetoException(configurationErrorMessage("In case the persistLibraryFileName is set the persistLibraryName must also be set."));
    }
    if (getLibraryApiDirectory() != null) {
      if (!Files.exists(getLibraryApiDirectory()) || !Files.isDirectory(getLibraryApiDirectory())) {
        throw new VetoException(configurationErrorMessage("In case a libraryApiDirectory is set '" + getLibraryApiDirectory() + "' it must exist and be a directory"));
      }
    }
  }

  protected String configurationErrorMessage(String message) {
    return "Configuration is not valid: " + message + " Replace bean " + getClass().getName() + " and provide valid configurations.";
  }
}
