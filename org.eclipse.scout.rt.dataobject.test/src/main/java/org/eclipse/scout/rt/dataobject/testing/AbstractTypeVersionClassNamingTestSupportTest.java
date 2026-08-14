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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public abstract class AbstractTypeVersionClassNamingTestSupportTest {

  @Test
  public void testFilePattern() {
    Pattern filePattern = BEANS.get(TypeVersionClassNamingTestSupport.class).getFilePattern();

    assertThat(filePattern.matcher("class MyVersion extends AbstractTypeVersion {}").find(), is(true));
    assertThat(filePattern.matcher("public class MyVersion extends AbstractTypeVersion {}").find(), is(true));
    assertThat(filePattern.matcher("final class MyVersion extends AbstractTypeVersion {}").find(), is(true));
    assertThat(filePattern.matcher("public final class MyVersion extends AbstractTypeVersion {}").find(), is(true));
    assertThat(filePattern.matcher("class MyVersion extends AbstractTypeVersionTest {}").find(), is(false));
  }
}
