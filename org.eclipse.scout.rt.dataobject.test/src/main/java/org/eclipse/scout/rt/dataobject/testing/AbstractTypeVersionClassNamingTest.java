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

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.scout.rt.dataobject.AbstractTypeVersion;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public abstract class AbstractTypeVersionClassNamingTest {

  @Test
  public final void testTypeVersions() {
    List<String> malformedTypeVersionClassNames = collectMalformedTypeVersionClassNames();
    if (!malformedTypeVersionClassNames.isEmpty()) {
      Assert.fail("Malformed type version class names found.\nType version class names must follow the following pattern: "
          + "<Namespace>_<version1>[_<version2>[_<versionN>]][__<comment>] where <Namespace> must be called after the respective scout namespace with all "
          + "letters in lower case except for the first letter, which must be capitalized.\n- " + String.join("\n- ", malformedTypeVersionClassNames));
    }
  }

  protected abstract String getPackageNamePrefix();

  protected Set<Class<ITypeVersion>> getExclusionList() {
    return CollectionUtility.emptyHashSet();
  }

  protected List<String> collectMalformedTypeVersionClassNames() {
    String packagePrefix = getPackageNamePrefix();

    Set<Class<ITypeVersion>> excludedTypeVersions = getExclusionList();

    return BEANS.all(ITypeVersion.class).stream()
        .map(Object::getClass)
        .filter(typeVersionClass -> !excludedTypeVersions.contains(typeVersionClass))
        .filter(typeVersionClass -> typeVersionClass.getPackageName().startsWith(packagePrefix))
        .map(Class::getSimpleName)
        .filter(this::isMalformedTypeVersionClassName)
        .toList();
  }

  protected boolean isMalformedTypeVersionClassName(String className) {
    Matcher matcher = AbstractTypeVersion.CLASS_NAME_PATTERN.matcher(className);
    if (!matcher.matches()) {
      return false;
    }

    String actualNamespaceIdString = matcher.group(1);
    String expectedNamespaceIdString = StringUtility.uppercaseFirst(actualNamespaceIdString.toLowerCase());

    return !actualNamespaceIdString.equals(expectedNamespaceIdString);
  }
}
