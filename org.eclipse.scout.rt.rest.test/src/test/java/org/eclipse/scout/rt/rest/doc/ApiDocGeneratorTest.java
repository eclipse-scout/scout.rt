/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.doc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.lang.annotation.Annotation;

import org.eclipse.scout.rt.rest.doc.ApiDocGenerator.DescriptionDescriptor;
import org.junit.Test;

public class ApiDocGeneratorTest {

  @Test
  public void testGetDescription() {
    ApiDocGenerator generator = new ApiDocGenerator();

    // plain text
    assertDescription(generator.toDescriptionDescriptor(createDescription("plain text", false)), true, "plain text", "plain text");
    assertDescription(generator.toDescriptionDescriptor(createDescription("plain text\nsecond line", false)), true, "plain text second line", "plain text<br>second line");
    assertDescription(generator.toDescriptionDescriptor(createDescription("plain text\nsecond line", false)), false, "plain text\nsecond line", "plain text<br>second line");

    // HTML
    assertDescription(generator.toDescriptionDescriptor(createDescription("<p>plain text</p>", true)), true, "plain text", "<p>plain text</p>");
    assertDescription(generator.toDescriptionDescriptor(createDescription("<b>plain text</b><br><p>second line</p>", true)), true, "plain text second line", "<b>plain text</b><br><p>second line</p>");
    assertDescription(generator.toDescriptionDescriptor(createDescription("<b>plain text</b><br><p>second line</p>", true)), false, "plain text\nsecond line", "<b>plain text</b><br><p>second line</p>");
  }

  protected ApiDocDescription createDescription(String text, boolean htmlEnabled) {
    return new ApiDocDescription() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return ApiDocDescription.class;
      }

      @Override
      public String textKey() {
        return null;
      }

      @Override
      public String text() {
        return text;
      }

      @Override
      public boolean htmlEnabled() {
        return htmlEnabled;
      }
    };
  }

  protected void assertDescription(DescriptionDescriptor descriptor, boolean removeNewLines, String expectedPlainText, String expectedHtmlText) {
    assertFalse(descriptor.isEmpty());
    assertEquals(expectedPlainText, descriptor.toPlainText(removeNewLines));
    assertEquals(expectedHtmlText, descriptor.toHtml());
  }
}
