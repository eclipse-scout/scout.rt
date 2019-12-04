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
package org.eclipse.scout.migration.ecma6.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.scout.migration.ecma6.configuration.MigrationConfigProperties.ApiBaseProperty;
import org.eclipse.scout.migration.ecma6.configuration.MigrationConfigProperties.IncludeFilesProperty;
import org.eclipse.scout.migration.ecma6.configuration.MigrationConfigProperties.ModuleNameProperty;
import org.eclipse.scout.migration.ecma6.configuration.MigrationConfigProperties.ParseOnlyIncludeFilesProperty;
import org.eclipse.scout.migration.ecma6.configuration.MigrationConfigProperties.SourceBaseProperty;
import org.eclipse.scout.migration.ecma6.configuration.MigrationConfigProperties.TargetBaseProperty;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Configuration {
  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

  public static final String MODULE__ORG_ECLIPSE_SCOUT_RT_UI_HTML = "org.eclipse.scout.rt.ui.html";
  public static final String MODULE__ORG_ECLIPSE_SCOUT_RT_SVG_UI_HTML = "org.eclipse.scout.rt.svg.ui.html";
  public static final String MODULE__ORG_ECLIPSE_SCOUT_JSWIDGETS_UI_HTML = "org.eclipse.scout.jswidgets.ui.html";
  public static final String MODULE__ORG_ECLIPSE_SCOUT_WIDGETS_HEATMAP_UI_HTML = "org.eclipse.scout.widgets.heatmap.ui.html";
  public static final String MODULE__ORG_ECLIPSE_SCOUT_WIDGETS_UI_HTML = "org.eclipse.scout.widgets.ui.html";
  public static final String MODULE__ORG_ECLIPSE_SCOUT_CONTACTS_UI_HTML = "org.eclipse.scout.contacts.ui.html";
  public static final String MODULE__COM_BSIAG_SCOUT_RT_UI_HTML = "com.bsiag.scout.rt.ui.html";
  public static final String MODULE__COM_BSIAG_SCOUT_RT_PDFVIEWER_UI_HTML = "com.bsiag.scout.rt.pdfviewer.ui.html";
  public static final String MODULE__COM_BSIAG_SCOUT_RT_OFFICEADDIN_UI_HTML = "com.bsiag.scout.rt.officeaddin.ui.html";
  public static final String MODULE__COM_BSIAG_SCOUT_RT_HTMLEDITOR_UI_HTML = "com.bsiag.scout.rt.htmleditor.ui.html";
  public static final String MODULE__COM_BSIAG_WIDGETS_UI_HTML = "com.bsiag.widgets.ui.html";
  public static final String MODULE__COM_BSIAG_STUDIO_UI_HTML = "com.bsiag.studio.ui.html";
  public static final String MODULE__COM_BSIAG_STUDIO_UI_HTML_TEST = "com.bsiag.studio.ui.html.test";
  public static final String MODULE__COM_BSIAG_CRM_UI_HTML_GRAPH = "com.bsiag.crm.ui.html.graph";
  public static final String MODULE__COM_BSIAG_CRM_UI_HTML_CORE = "com.bsiag.crm.ui.html.core";
  public static final String MODULE__COM_BSIAG_CRM_STUDIO_UI_HTML = "com.bsiag.crm.studio.ui.html";
  public static final String MODULE__COM_BSIAG_STUDIO_STEP_BASE = "com.bsiag.studio.step.base";
  public static final String MODULE__COM_BSIAG_STUDIO_STEP_CRM = "com.bsiag.studio.step.crm";
  public static final String MODULE__COM_BSIAG_STUDIO_STEP_WEATHER = "com.bsiag.studio.step.weather";
  public static final String MODULE__COM_BSIAG_STUDIO_STEP_MEDIA = "com.bsiag.studio.step.media";
  public static final String MODULE__COM_BSIAG_STUDIO_STEP_EXAMPLE = "com.bsiag.studio.step.example";
  public static final String MODULE__COM_BSIAG_STUDIO_STEP_PROTOTYPING = "com.bsiag.studio.step.prototyping";
  public static final String MODULE__COM_BSIAG_STUDIO_STEP_ML = "com.bsiag.studio.step.ml";
  public static final String MODULE__COM_BSIAG_ML_CORTEX = "com.bsiag.ml.cortex";
  public static final String MODULE__COM_BSIAG_PORTAL_UI = "com.bsiag.portal.ui";
  public static final String MODULE__COM_BSIAG_BSISTUDIO_LAB_UI_HTML = "com.bsiag.bsistudio.lab.ui.html";
  public static final String MODULE__COM_BSIAG_BRIEFCASE_UI_HTML = "com.bsiag.briefcase.ui.html";
  public static final String MODULE__COM_BSIAG_BSIBRIEFCASE_UI_HTML = "com.bsiag.bsibriefcase.ui.html";

  public static Configuration get() {
    return BEANS.get(Configuration.class);
  }

  private Path m_sourceModuleDirectory;
  private Path m_targetModuleDirectory;
  private String m_namespace;
  private String m_jsFolderName;
  private String m_persistLibraryName;
  private String m_persistLibraryFileName;
  private boolean m_removeJsFolder = true;
  private boolean m_useIndexJs = true;
  private String m_stepConfigTypeName;
  private List<Path> m_includeFiles;
  private boolean m_parseOnlyIncludeFiles;

  private Path m_apiBase;

  /**
   * Paths and names based on Taskliste-IntelliJ-Ecma.xlsx
   */
  protected Configuration() {
    setApiBase(getConfiguredApiBase());
    setIncludeFiles(getConfiguredIncludeFiles());
    setParseOnlyIncludeFiles(getConfiguredParseOnlyIncludedFiles());
    Path sourceBase = getConfiguredSourceBase();
    Path targetBase = getConfiguredTargetBase();
    String moduleName = getConfiguredModule();

    if (moduleName == null) {
      return;
    }

    switch (moduleName) {
      case MODULE__ORG_ECLIPSE_SCOUT_RT_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("org.eclipse.scout.rt", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("org.eclipse.scout.rt", moduleName)));
        setNamespace("scout");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@eclipse-scout/core");
        setPersistLibraryFileName("01-api_eclipse-scout_core.json");
        break;
      case MODULE__ORG_ECLIPSE_SCOUT_RT_SVG_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("org.eclipse.scout.rt", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("org.eclipse.scout.rt", moduleName)));
        setNamespace("scout");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@eclipse-scout/svg");
        setPersistLibraryFileName("02-api_eclipse-scout_svg.json");
        break;
      case MODULE__ORG_ECLIPSE_SCOUT_JSWIDGETS_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("org.eclipse.scout.docs/code/widgets/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("org.eclipse.scout.docs/code/widgets/", moduleName)));
        setNamespace("jswidgets");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@eclipse-scout/demo-jswidgets");
        setPersistLibraryFileName("10-api_demo-jswidgets.json");
        break;

      case MODULE__ORG_ECLIPSE_SCOUT_WIDGETS_HEATMAP_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("org.eclipse.scout.docs/code/widgets/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("org.eclipse.scout.docs/code/widgets/", moduleName)));
        setNamespace("scout");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@eclipse-scout/demo-widgets-heatmap");
        setPersistLibraryFileName("40-api_demo-widgets-heatmap.json");
        break;
      case MODULE__ORG_ECLIPSE_SCOUT_WIDGETS_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("org.eclipse.scout.docs/code/widgets/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("org.eclipse.scout.docs/code/widgets/", moduleName)));
        setNamespace("widgets");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@eclipse-scout/demo-widgets");
        setPersistLibraryFileName("41-api_demo-widgets.json");
        break;
      case MODULE__ORG_ECLIPSE_SCOUT_CONTACTS_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("org.eclipse.scout.docs/code/contacts/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("org.eclipse.scout.docs/code/contacts/", moduleName)));
        setNamespace("contacts");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@eclipse-scout/demo-contacts");
        setPersistLibraryFileName("43-api_demo-contacts.json");
        break;

      case MODULE__COM_BSIAG_SCOUT_RT_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setNamespace("scout");
        setJsFolderName("bsiscout");
        setPersistLibraryName("@bsi-scout/core");
        setPersistLibraryFileName("50-api_bsi_scout_core.json");
        break;
      case MODULE__COM_BSIAG_SCOUT_RT_PDFVIEWER_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setNamespace("scout");
        setJsFolderName("bsiscout");
        setPersistLibraryName("@bsi-scout/pdfviewer");
        setPersistLibraryFileName("52-api_bsi_scout_pdfviewer.json");
        break;
      case MODULE__COM_BSIAG_SCOUT_RT_OFFICEADDIN_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setNamespace("scout");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-scout/officeaddin");
        setPersistLibraryFileName("54-api_bsi_scout_officeaddin.json");
        break;
      case MODULE__COM_BSIAG_SCOUT_RT_HTMLEDITOR_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setNamespace("scout");
        setJsFolderName("bsiscout");
        setPersistLibraryName("@bsi-scout/htmleditor");
        setPersistLibraryFileName("56-api_bsi_scout_htmleditor.json");
        break;
      case MODULE__COM_BSIAG_WIDGETS_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsi.scout.rt/", moduleName)));
        setNamespace("scout");
        setJsFolderName("bsiwidgets");
        setPersistLibraryName("@bsi-scout/demo-widgets");
        setPersistLibraryFileName("80-api_bsi_demo_widgets.json");
        break;
      case MODULE__COM_BSIAG_STUDIO_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studio");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-studio/core");
        setPersistLibraryFileName("112-api_bsi_studio_core.json");
        break;
      case MODULE__COM_BSIAG_STUDIO_UI_HTML_TEST: {
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studio");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-studio/testing");
        setPersistLibraryFileName("113-api_bsi_studio_core_test.json");
        break;
      }
      case MODULE__COM_BSIAG_CRM_UI_HTML_GRAPH:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsicrm/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsicrm/", moduleName)));
        setNamespace("scout");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-crm/graph");
        setPersistLibraryFileName("100-api_bsicrm_graph.json");
        break;
      case MODULE__COM_BSIAG_CRM_UI_HTML_CORE:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsicrm/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsicrm/", moduleName)));
        setNamespace("crm");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-crm/core");
        setPersistLibraryFileName("110-api_bsicrm_core.json");
        break;
      case MODULE__COM_BSIAG_CRM_STUDIO_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studiocrm");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-crm/studio");
        setPersistLibraryFileName("120-api_bsicrm_studio.json");
        break;

      case MODULE__COM_BSIAG_STUDIO_STEP_BASE:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studio");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-studio/step");
        setPersistLibraryFileName("500-api_bsi_studio_steps_base.json");
        setRemoveJsFolder(false); // folder is required for steps because loaded from classloader
        setUseIndexJs(false); // there is no index.js for steps
        setStepConfigTypeName("steps");
        break;
      case MODULE__COM_BSIAG_STUDIO_STEP_CRM:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studio");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-studio/step-crm");
        setPersistLibraryFileName("510-api_bsi_studio_steps_crm.json");
        setRemoveJsFolder(false); // folder is required for steps because loaded from classloader
        setUseIndexJs(false); // there is no index.js for steps
        setStepConfigTypeName("steps");
        break;
      case MODULE__COM_BSIAG_STUDIO_STEP_WEATHER:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studio");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-studio/step-weather");
        setPersistLibraryFileName("520-api_bsi_studio_steps_weather.json");
        setRemoveJsFolder(false); // folder is required for steps because loaded from classloader
        setUseIndexJs(false); // there is no index.js for steps
        setStepConfigTypeName("steps");
        break;
      case MODULE__COM_BSIAG_STUDIO_STEP_MEDIA:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studio");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-studio/step-media");
        setPersistLibraryFileName("530-api_bsi_studio_steps_media.json");
        setRemoveJsFolder(false); // folder is required for steps because loaded from classloader
        setUseIndexJs(false); // there is no index.js for steps
        setStepConfigTypeName("steps");
        break;
      case MODULE__COM_BSIAG_STUDIO_STEP_EXAMPLE:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studio");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-studio/step-example");
        setPersistLibraryFileName("540-api_bsi_studio_steps_example.json");
        setRemoveJsFolder(false); // folder is required for steps because loaded from classloader
        setUseIndexJs(false); // there is no index.js for steps
        setStepConfigTypeName("steps");
        break;
      case MODULE__COM_BSIAG_STUDIO_STEP_PROTOTYPING:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studio");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-studio/step-prototyping");
        setPersistLibraryFileName("550-api_bsi_studio_steps_prototyping.json");
        setRemoveJsFolder(false); // folder is required for steps because loaded from classloader
        setUseIndexJs(false); // there is no index.js for steps
        setStepConfigTypeName("steps");
        break;
      case MODULE__COM_BSIAG_STUDIO_STEP_ML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("studio");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-studio/step-ml");
        setPersistLibraryFileName("560-api_bsi_studio_steps_ml.json");
        setRemoveJsFolder(false); // folder is required for steps because loaded from classloader
        setUseIndexJs(false); // there is no index.js for steps
        setStepConfigTypeName("steps");
        break;

      case MODULE__COM_BSIAG_ML_CORTEX:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsiml/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsiml/", moduleName)));
        setNamespace("studio");
        setJsFolderName("ml");
        setPersistLibraryName("@bsi-ml/cortex");
        setPersistLibraryFileName("600-api_bsi_ml_cortex.json");
        setRemoveJsFolder(false); // folder is required for steps because loaded from classloader
        setUseIndexJs(false); // there is no index.js for steps
        setStepConfigTypeName("cortexes");
        break;

      case MODULE__COM_BSIAG_PORTAL_UI:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsiportal/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsiportal/", moduleName)));
        setNamespace("portal");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-portal/core");
        setPersistLibraryFileName("700-api_bsi_portal_core.json");
        break;

      case MODULE__COM_BSIAG_BSISTUDIO_LAB_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsistudio/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsistudio/", moduleName)));
        setNamespace("crm");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-crm/bsiag-studio-lab");
        setPersistLibraryFileName("800-api_bsi_studio_lab.json");
        break;

      case MODULE__COM_BSIAG_BRIEFCASE_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsibriefcase/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsibriefcase/", moduleName)));
        setNamespace("briefcase");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-briefcase/core");
        setPersistLibraryFileName("900-api_bsi_briefcase_core.json");
        break;
      case MODULE__COM_BSIAG_BSIBRIEFCASE_UI_HTML:
        setSourceModuleDirectory(sourceBase.resolve(Paths.get("bsibriefcase/", moduleName)));
        setTargetModuleDirectory(targetBase.resolve(Paths.get("bsibriefcase/", moduleName)));
        setNamespace("bsibriefcase");
        setJsFolderName(getNamespace());
        setPersistLibraryName("@bsi-briefcase/bsiag");
        setPersistLibraryFileName("920-api_bsi_briefcase_bsiag.json");
        break;
      default:
        throw new ProcessingException("unknown module " + moduleName);
    }
    LOG.info("SourceModuleDirectory: " + getSourceModuleDirectory());
    LOG.info("TargetModuleDirectory: " + getTargetModuleDirectory());
    LOG.info("Namespace: " + getNamespace());
    LOG.info("PersistLibraryName: " + getPersistLibraryName());
    LOG.info("PersistLibraryFileName: " + getPersistLibraryFileName());
    LOG.info("LibraryApiDirectory: " + getApiBase());
  }

  protected Path getConfiguredSourceBase() {
    return CONFIG.getPropertyValue(SourceBaseProperty.class);
  }

  protected Path getConfiguredTargetBase() {
    return CONFIG.getPropertyValue(TargetBaseProperty.class);
  }

  protected String getConfiguredModule() {
    return CONFIG.getPropertyValue(ModuleNameProperty.class);
  }

  protected Path getConfiguredApiBase() {
    Path apiBase = CONFIG.getPropertyValue(ApiBaseProperty.class);
    if (apiBase == null) {
      // default
      apiBase = Paths.get(getConfiguredSourceBase() + "/ecma6-mig-apis");
    }
    return apiBase;
  }

  protected boolean getConfiguredParseOnlyIncludedFiles() {
    return CONFIG.getPropertyValue(ParseOnlyIncludeFilesProperty.class);
  }

  protected List<Path> getConfiguredIncludeFiles() {
    return CONFIG.getPropertyValue(IncludeFilesProperty.class);
  }
  /**
   * @return the source directory to be migrated. must exist. Usually something like '.../[com.bsiag.bsicrm.]ui.html'
   */
  public Path getSourceModuleDirectory() {
    return m_sourceModuleDirectory;
  }

  public void setSourceModuleDirectory(Path sourceModuleDirectory) {
    m_sourceModuleDirectory = sourceModuleDirectory;
  }

  /**
   * @return the directory where the result of the migration is written to. Must not exist, the directory will be
   *         created if it or one of its parents does not exist.
   */
  public Path getTargetModuleDirectory() {
    return m_targetModuleDirectory;
  }

  public void setTargetModuleDirectory(Path targetModuleDirectory) {
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

  public String getJsFolderName() {
    return m_jsFolderName;
  }

  public void setJsFolderName(String jsFolderName) {
    m_jsFolderName = jsFolderName;
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
    return getApiBase().resolve(Paths.get(getPersistLibraryFileName()));
  }


  /**
   * @return The folder where all library api's used for this migration are located. In this folder might be several
   *         *.json * files from previous migrations.
   */
  public Path getApiBase() {
    return m_apiBase;
  }

  public void setApiBase(Path apiBase) {
    m_apiBase = apiBase;
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
    if (getApiBase() != null) {
      if (!Files.exists(getApiBase()) || !Files.isDirectory(getApiBase())) {
        throw new VetoException(configurationErrorMessage("In case a libraryApiDirectory is set '" + getApiBase() + "' it must exist and be a directory"));
      }
    }
  }

  protected String configurationErrorMessage(String message) {
    return "Configuration is not valid: " + message + " Replace bean " + getClass().getName() + " and provide valid configurations.";
  }

  public boolean isRemoveJsFolder() {
    return m_removeJsFolder;
  }

  public void setRemoveJsFolder(boolean removeJsFolder) {
    m_removeJsFolder = removeJsFolder;
  }

  public boolean isUseIndexJs() {
    return m_useIndexJs;
  }

  public void setUseIndexJs(boolean useIndexJs) {
    m_useIndexJs = useIndexJs;
  }

  public String getStepConfigTypeName() {
    return m_stepConfigTypeName;
  }

  public void setStepConfigTypeName(String stepConfigTypeName) {
    m_stepConfigTypeName = stepConfigTypeName;
  }

  public List<Path> getIncludeFiles() {
    return m_includeFiles;
  }

  public void setIncludeFiles(List<Path> includeFiles) {
    m_includeFiles = includeFiles;
  }

  public boolean isParseOnlyIncludeFiles() {
    return m_parseOnlyIncludeFiles;
  }

  public void setParseOnlyIncludeFiles(boolean parseOnlyIncludeFiles) {
    m_parseOnlyIncludeFiles = parseOnlyIncludeFiles;
  }
}
