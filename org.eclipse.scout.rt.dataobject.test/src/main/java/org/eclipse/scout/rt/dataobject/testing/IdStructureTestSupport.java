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
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Checks for each {@link IId} if there exists a corresponding {@link AbstractIdStructureTest}
 */
public class IdStructureTestSupport extends AbstractDataObjectTestSupport {

  /**
   * Pattern to detect {@link IId} files that require a {@link AbstractIdStructureTest}
   */
  @Override
  protected Pattern createFilePattern() {
    return Pattern.compile("class (?!Abstract)\\w+ (?:implements|extends) \\w+(" + StringUtility.join("|", getIdClassSuffixes()) + ") ");
  }

  protected List<String> getIdClassSuffixes() {
    return CollectionUtility.arrayList("RootId", "CompositeId", "StringId", "LongId", "UuId");
  }

  /**
   * Pattern to detect {@link AbstractIdStructureTest} files
   */
  @Override
  protected Pattern createTestFilePattern() {
    return Pattern.compile("extends \\w*IdStructureTest\\s+");
  }

  /**
   * Pattern to extract the package name prefixes from {@link AbstractIdStructureTest#streamIdClasses(String)}
   */
  @Override
  protected Pattern createPackageNamePrefixPattern() {
    return Pattern.compile("streamIdClasses\\(\"([^\"]+)\"\\)");
  }

  @Override
  protected boolean acceptFile(Path path, String content) {
    return !path.toString().contains(Path.of("src/test").toString()) && m_filePattern.matcher(content).find();
  }

  @Override
  protected boolean acceptTestFile(Path path, String content) {
    return path.getFileName().toString().endsWith("IdStructureTest.java") && m_testFilePattern.matcher(content).find();
  }

  @Override
  protected String getErrorTitle() {
    return "No IdStructureTest found for the following files:";
  }
}
