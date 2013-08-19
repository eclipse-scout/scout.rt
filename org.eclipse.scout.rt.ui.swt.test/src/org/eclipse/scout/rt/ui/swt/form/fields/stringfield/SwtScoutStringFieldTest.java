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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
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
    IStringField mockFieldWithMask = EasyMock.createNiceMock(IStringField.class);
    expect(mockFieldWithMask.isInputMasked()).andReturn(true);
    replay(mockFieldWithMask);
    SwtScoutStringField swtField = new SwtScoutStringField();
    assertContainsSwtStyle(SWT.PASSWORD, swtField.getSwtStyle(mockFieldWithMask));
  }

  /**
   * Test for {@link SwtScoutStringField#getSwtStyle(IStringField)} no inputMasked
   */
  @Test
  public void testNoSwtStylePassword() {
    IStringField mockFieldWithMask = EasyMock.createNiceMock(IStringField.class);
    expect(mockFieldWithMask.isInputMasked()).andReturn(false);
    replay(mockFieldWithMask);
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
