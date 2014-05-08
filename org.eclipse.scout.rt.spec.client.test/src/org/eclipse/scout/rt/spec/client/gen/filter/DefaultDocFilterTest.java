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

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.annotations.Doc;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.spec.client.filter.DefaultDocFilter;
import org.junit.Test;

/**
 * Test for {@link DefaultDocFilter}
 */
public class DefaultDocFilterTest {

  protected static final DefaultDocFilter<IFormField> FILTER = new DefaultDocFilter<IFormField>();

  @Test
  public void testDefaultDocFilter() {
    assertEquals("field with annotated with REJECT should return REJECT", Doc.Filtering.REJECT, FILTER.accept(new DocRejectTestFormField()));
    assertEquals("field with annotated with ACCEPT should return ACCEPT", Doc.Filtering.ACCEPT, FILTER.accept(new DocAcceptTestFormField()));
    assertEquals("field with annotated with TRANSPARENT should return TRANSPARENT", Doc.Filtering.TRANSPARENT, FILTER.accept(new DocTransparentTestFormField()));
    assertEquals("field with annotated with ACCEPT_REJECT_CHILDREN should return ACCEPT_REJECT_CHILDREN", Doc.Filtering.ACCEPT_REJECT_CHILDREN, FILTER.accept(new DocAcceptRejectChildrenTestFormField()));

    IFormField testFormField = new AbstractFormField() {
    };
    assertEquals("field with no annotation return ACCEPT", Doc.Filtering.ACCEPT, FILTER.accept(testFormField));
  }

  // TODO ASA test default behavior for groupboxes

  @Doc(filter = Doc.Filtering.REJECT)
  class DocRejectTestFormField extends AbstractFormField {
  }

  @Doc(filter = Doc.Filtering.ACCEPT)
  class DocAcceptTestFormField extends AbstractFormField {
  }

  @Doc(filter = Doc.Filtering.TRANSPARENT)
  class DocTransparentTestFormField extends AbstractFormField {
  }

  @Doc(filter = Doc.Filtering.ACCEPT_REJECT_CHILDREN)
  class DocAcceptRejectChildrenTestFormField extends AbstractFormField {
  }

}
