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
package org.eclipse.scout.rt.ui.swt.form.fields.stringfield;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.swt.SWT;
import org.junit.Test;

/**
 * Test for {@link SwtScoutStringField}
 */
public class SwtScoutStringFieldTest {

  /**
   * Test for {@link SwtScoutStringField#getSwtStyle(IStringField)} inputMasked
   */
  @Test
  public void testSwtStylePassword() {
    IStringField mockFieldWithMask = mock(IStringField.class);
    when(mockFieldWithMask.isInputMasked()).thenReturn(true);
    SwtScoutStringField swtField = new SwtScoutStringField();
    assertContainsSwtStyle(SWT.PASSWORD, swtField.getSwtStyle(mockFieldWithMask));
  }

  /**
   * Test for {@link SwtScoutStringField#getSwtStyle(IStringField)} no inputMasked
   */
  @Test
  public void testNoSwtStylePassword() {
    IStringField mockFieldWithMask = mock(IStringField.class);
    when(mockFieldWithMask.isInputMasked()).thenReturn(false);
    SwtScoutStringField swtField = new SwtScoutStringField();
    assertNotContainsSwtStyle(SWT.PASSWORD, swtField.getSwtStyle(mockFieldWithMask));
  }

  private void assertContainsSwtStyle(int expectedStyle, int actualStyle) {
    assertTrue("Incorrect SWT Style. Style " + expectedStyle + "expected. Actual Style is " + actualStyle,
        (actualStyle & expectedStyle) == expectedStyle);
  }

  private void assertNotContainsSwtStyle(int expectedStyle, int actualStyle) {
    assertTrue("Incorrect SWT Style. Style " + expectedStyle + "should not be present. Actual Style is " + actualStyle,
        (actualStyle & expectedStyle) == 0);
  }

}
