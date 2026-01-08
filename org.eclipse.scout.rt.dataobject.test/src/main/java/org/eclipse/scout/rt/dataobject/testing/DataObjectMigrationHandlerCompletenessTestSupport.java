/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.dataobject.migration.IDoStructureMigrationHandler;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.platform.util.AbstractCompletenessTestSupport;

/**
 * Checks for each {@link IDoStructureMigrationHandler} if there exists a corresponding
 * {@link AbstractDoStructureMigrationHandlerCompletenessTest}
 */
public class DataObjectMigrationHandlerCompletenessTestSupport extends AbstractCompletenessTestSupport {

  /**
   * Pattern to detect {@link IDoStructureMigrationHandler} files that require a {@link AbstractDoStructureMigrationHandlerCompletenessTest}
   */
  @Override
  protected Pattern createFilePattern() {
    return Pattern.compile("class (?!Abstract)\\w+\\s+(?:implements\\s+IDoStructureMigrationHandler\\s+|extends\\s+(?!\\w*(" + StringUtility.join("|", getExcludedParentClassSuffixes()) + "))\\w*MigrationHandler[\\s_]+)");
  }

  protected List<String> getExcludedParentClassSuffixes() {
    return CollectionUtility.arrayList("ValueMigrationHandler");
  }

  /**
   * Pattern to detect {@link AbstractDoStructureMigrationHandlerCompletenessTest} files
   */
  @Override
  protected Pattern createTestFilePattern() {
    return Pattern.compile("extends \\w*DoStructureMigrationHandlerCompletenessTest\\s+");
  }

  /**
   * Pattern to extract the package name prefix from {@link AbstractDoStructureMigrationHandlerCompletenessTest#getPackageNamePrefix()}
   */
  @Override
  protected Pattern createPackageNamePrefixPattern() {
    return Pattern.compile("String getPackageNamePrefix\\(\\)\\s+\\{\\s+return\\s+\"([^\"]+)\";\\s+}");
  }

  @Override
  protected boolean acceptFile(Path path, String content) {
    return !path.toString().contains(Path.of("src/test").toString()) && !path.getFileName().toString().endsWith("Test.java") && path.getFileName().toString().contains("MigrationHandler") && getFilePattern().matcher(content).find();
  }

  @Override
  protected boolean acceptTestFile(Path path, String content) {
    return path.getFileName().toString().endsWith("DoStructureMigrationHandlerCompletenessTest.java") && getTestFilePattern().matcher(content).find();
  }

  @Override
  protected String getErrorTitle() {
    return "No DoStructureMigrationHandlerCompletenessTest found for the following files";
  }
}
