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

public class TypeVersionClassNamingTestSupport extends AbstractDataObjectTestSupport {

  @Override
  protected Pattern createFilePattern() {
    return Pattern.compile("class \\w+ extends AbstractTypeVersion\\b");
  }

  @Override
  protected Pattern createTestFilePattern() {
    return Pattern.compile("\\bextends AbstractTypeVersionClassNamingTest\\b");
  }

  @Override
  protected Pattern createPackageNamePrefixPattern() {
    return Pattern.compile("String getPackageNamePrefix\\(\\)\\s*\\{\\s*return \\s*\"(.+?)\";\\s*}");
  }

  @Override
  protected boolean acceptFile(Path path, String content) {
    return !path.toString().contains(Path.of("src/test").toString()) && !path.getFileName().toString().endsWith("Test.java") && getFilePattern().matcher(content).find();
  }

  @Override
  protected boolean acceptTestFile(Path path, String content) {
    return path.getFileName().toString().endsWith("TypeVersionClassNamingTest.java") && getTestFilePattern().matcher(content).find();
  }

  @Override
  protected String getErrorTitle() {
    return "No TypeVersionClassNamingTest found for the following files:";
  }
}
