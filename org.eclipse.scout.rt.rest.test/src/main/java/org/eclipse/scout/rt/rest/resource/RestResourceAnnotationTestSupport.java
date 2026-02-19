/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.resource;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.testing.platform.util.AbstractCompletenessTestSupport;

/**
 * Checks for each {@link IRestResource} if there exists a corresponding {@link AbstractRestResourceAnnotationTest}
 */
public class RestResourceAnnotationTestSupport extends AbstractCompletenessTestSupport {

  /**
   * Pattern to detect {@link IRestResource}(s) files that require a {@link AbstractRestResourceAnnotationTest}
   */
  @Override
  protected Pattern createFilePattern() {
    // quick check if file is applicable, string check to quickly exclude EXT resources here already to avoid empty tests
    return Pattern.compile("(?<!@RestApplicationScope\\(RestApplicationScopes\\.EXT\\)\\Rpublic )class (?!Abstract)\\w+ implements IRestResource[,\\s{]");
  }

  /**
   * Pattern to detect {@link AbstractRestResourceAnnotationTest} files
   */
  @Override
  protected Pattern createTestFilePattern() {
    return Pattern.compile("extends \\w*RestResourceAnnotationTest\\s+");
  }

  /**
   * Pattern to extract the package name prefix from {@link AbstractRestResourceAnnotationTest#getPackageNamePrefix()}
   */
  @Override
  protected Pattern createPackageNamePrefixPattern() {
    return Pattern.compile("String getPackageNamePrefix\\(\\)\\s+\\{\\s+return\\s+\"([^\"]+)\";\\s+}");
  }

  @Override
  protected boolean acceptFile(Path path, String content) {
    return !path.toString().contains(Path.of("src/test").toString()) && !path.getFileName().toString().endsWith("Test.java") && getFilePattern().matcher(content).find();
  }

  @Override
  protected boolean acceptTestFile(Path path, String content) {
    return path.getFileName().toString().endsWith("RestResourceAnnotationTest.java") && getTestFilePattern().matcher(content).find();
  }

  @Override
  public String getErrorTitle() {
    return "No RestResourceAnnotationTest found for the following files:";
  }
}
