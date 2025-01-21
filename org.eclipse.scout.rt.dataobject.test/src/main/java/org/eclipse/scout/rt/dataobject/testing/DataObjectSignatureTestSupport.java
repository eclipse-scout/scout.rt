/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.testing.signature.AbstractDataObjectSignatureTest;

/**
 * Checks for each {@link IDoEntity} with a {@link TypeVersion} if there exists a corresponding
 * {@link AbstractDataObjectSignatureTest}
 */
public class DataObjectSignatureTestSupport extends AbstractDataObjectTestSupport {

  /**
   * Pattern to detect data object files that require a {@link AbstractDataObjectSignatureTest}
   */
  @Override
  protected Pattern createFilePattern() {
    return Pattern.compile("@TypeVersion\\(");
  }

  /**
   * Pattern to detect {@link AbstractDataObjectSignatureTest} files
   */
  @Override
  protected Pattern createTestFilePattern() {
    return Pattern.compile("extends \\w*DataObjectSignatureTest\\s+");
  }

  /**
   * Pattern to extract the package name prefix from {@link AbstractDataObjectSignatureTest#getPackageNamePrefix()}
   */
  @Override
  protected Pattern createPackageNamePrefixPattern() {
    return Pattern.compile("String getPackageNamePrefix\\(\\)\\s+\\{\\s+return\\s+\"([^\"]+)\";\\s+}");
  }

  @Override
  protected boolean acceptFile(Path path, String content) {
    return !path.toString().contains(Path.of("src/test").toString()) && m_filePattern.matcher(content).find();
  }

  @Override
  protected boolean acceptTestFile(Path path, String content) {
    return path.getFileName().toString().endsWith("DataObjectSignatureTest.java") && m_testFilePattern.matcher(content).find();
  }

  @Override
  protected String getErrorTitle() {
    return "No DataObjectSignatureTest found for the following files";
  }
}
