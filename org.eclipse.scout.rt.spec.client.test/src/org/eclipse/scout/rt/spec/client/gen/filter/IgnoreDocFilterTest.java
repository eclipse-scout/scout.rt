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
package org.eclipse.scout.rt.spec.client.gen.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.annotations.Doc;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.spec.client.gen.filter.IgnoreDocFilter;
import org.junit.Test;

/**
 * Test for {@link IgnoreDocFilter}
 */
public class IgnoreDocFilterTest {

  /**
   * Tests, if a {@link IFormField} with <code>@Doc(ignore=true)</code>is not accepted by the {@link IgnoreDocFilter}.
   */
  @Test
  public void testIgnoreDocFilter() {
    IgnoreDocFilter<IFormField> filter = new IgnoreDocFilter<IFormField>();
    DocIgnoreTestFormField testFormField = new DocIgnoreTestFormField();
    boolean accept = filter.accept(testFormField);
    assertFalse(accept);
  }

  /**
   * Tests, if a {@link IFormField} with <code>@Doc(ignore=false)</code> is accepted by the {@link IgnoreDocFilter}.
   */
  @Test
  public void testNoIgnoreDocFilter() {
    IgnoreDocFilter<IFormField> filter = new IgnoreDocFilter<IFormField>();
    DocNoIgnoreTestFormField testFormField = new DocNoIgnoreTestFormField();
    boolean accept = filter.accept(testFormField);
    assertTrue(accept);
  }

  /**
   * Tests, if a {@link IFormField} without <code>@Doc</code> annotation is accepted by the {@link IgnoreDocFilter}.
   */
  @Test
  public void testIgnoreDocFilterNoAnnotation() {
    IgnoreDocFilter<IFormField> filter = new IgnoreDocFilter<IFormField>();
    IFormField testFormField = new AbstractFormField() {
    };
    boolean accept = filter.accept(testFormField);
    assertTrue(accept);
  }

  @Doc(ignore = true)
  class DocIgnoreTestFormField extends AbstractFormField {
  }

  @Doc(ignore = false)
  class DocNoIgnoreTestFormField extends AbstractFormField {
  }

}
