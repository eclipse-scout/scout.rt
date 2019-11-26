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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPathConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.ConfigUtility;

public class MigrationConfigProperties {

  public static class IncludeFilesProperty extends AbstractConfigProperty<List<Path>, List<String>> {

    @Override
    public String getKey() {
      return "scout.ES6Migration.includeFiles";
    }

    @Override
    public String description() {
      return "If this property is set the migrator will only migrate the given comma separated file list (absolute paths).";
    }

    @Override
    public List<String> readFromSource(String namespace) {
      return ConfigUtility.getPropertyList(getKey(), null, namespace);
    }

    @Override
    protected List<Path> parse(List<String> value) {
      if (value == null) {
        return null;
      }
      return value.stream()
          .filter(Objects::nonNull)
          .map(Paths::get)
          .collect(Collectors.toList());
    }
  }

  public static class ModuleNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.ES6Migration.moduleName";
    }

    @Override
    public String description() {
      return "The module name will be migrated.";
    }
  }

  public static class SourceBaseProperty extends AbstractPathConfigProperty {

    @Override
    public String getKey() {
      return "scout.ES6Migration.sourceBase";
    }

    @Override
    public String description() {
      return "The path to the source base directory.";
    }
  }

  public static class TargetBaseProperty extends AbstractPathConfigProperty {

    @Override
    public String getKey() {
      return "scout.ES6Migration.targetBase";
    }

    @Override
    public String description() {
      return "The path to the target base directory.";
    }
  }

  public static class ApiBaseProperty extends AbstractPathConfigProperty {

    @Override
    public String getKey() {
      return "scout.ES6Migration.apiBase";
    }

    @Override
    public String description() {
      return "The path to the base where all API's are.";
    }
  }

  public static class ParseOnlyIncludeFilesProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.ES6Migration.parseOnlyIncludeFiles";
    }

    @Override
    public String description() {
      return "Only files of the includeFiles property are parsed. Rest of the API is taken from the persisted API.";
    }
  }
}
