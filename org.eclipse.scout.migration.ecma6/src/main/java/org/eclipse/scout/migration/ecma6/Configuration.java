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

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ApplicationScoped
public class Configuration {

  public static Configuration get() {
    return BEANS.get(Configuration.class);
  }

  /**
   * @return the source directory to be migrated. must exist. Usually something like '.../[com.bsiag.bsicrm.]ui.html'
   */
  public Path getSourceModuleDirectory() {
    return null;
  }

  /**
   * @return the directory where the result of the migration is written to. Must not exist, the directory will be
   *         created if it or one of its parents does not exist.
   */
  public Path getTargetModuleDirectory() {
    return null;
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
    return null;
  }

  /**
   * @return The folder where all library api's used for this migration are located. In this folder might be several
   *         *.json * files from previous migrations.
   */
  public Path getLibraryApiDirectory() {
    return null;
  }

  /**
   * In case the persist library file is set the API of the migrated module is written in JSON format to this file. If
   * the persistLibraryFile is set the persistLibraryName must also be set.
   *
   * @return a file to persist the api.
   */
  public Path getPersistLibraryFile() {
    return null;
  }

  /**
   * The library name under which the migrated API is stored in the JSON format. The library API can be used as input
   * for a dependent module migration. If the persistLibraryFile is set the persistLibraryName must also be set.
   *
   * @return the library name (npm name) of the library which is optionally written.
   */
  public String getPersistLibraryName() {
    return "@eclipse-scout/eclipse-scout-core";
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
    if (getPersistLibraryFile() != null && StringUtility.isNullOrEmpty(getPersistLibraryName())) {
      throw new VetoException(configurationErrorMessage("In case the persistLibraryFile is set the persistLibraryName must also be set."));
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
