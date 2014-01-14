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
package org.eclipse.scout.rt.spec.client.gen.extract.form;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for {@link IDocTextExtractor} for {@link org.eclipse.scout.rt.client.ui.form.IForm IForm}
 */
//TODO jgu
@Ignore
public class FormExtractorTest {

  /**
   * Test for {@link FormTitleExtractor#getText}
   */
  @Test
  public void testColumnHeaderTextExtractor() {
    FormTitleExtractor ex = new FormTitleExtractor();
    final String TEST_TEXT = "TEST";
    AbstractForm form = mock(AbstractForm.class);
    when(form.getTitle()).thenReturn(TEST_TEXT);
    String text = ex.getText(form);
    assertEquals(TEST_TEXT, text);
  }

}
