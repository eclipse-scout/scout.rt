/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.annotations.ClassId;
import org.junit.Test;

/**
 * JUnit tests for {@link AbstractPage}
 *
 * @since 3.10.0
 */
public class AbstractPageTest {
  private static final String TEST_PAGE_CLASS_ID = "TEST_CLASS_ID";

  @Test
  public void testClassIdAnnotatedPage() {
    AbstractPageWithClassId testPage = new AbstractPageWithClassId();
    assertEquals("classid should correspond to annotated id", TEST_PAGE_CLASS_ID, testPage.classId());
  }

  @Test
  public void testComputedClassId() {
    AbstractPageWithoutClassId testPageNoAnnotation = new AbstractPageWithoutClassId();
    String className = testPageNoAnnotation.getClass().getName();
    assertEquals("classid should correspond to annotated id", className, testPageNoAnnotation.classId());
  }

  @ClassId(TEST_PAGE_CLASS_ID)
  class AbstractPageWithClassId extends AbstractPage {
  }

  class AbstractPageWithoutClassId extends AbstractPage {
  }

}
