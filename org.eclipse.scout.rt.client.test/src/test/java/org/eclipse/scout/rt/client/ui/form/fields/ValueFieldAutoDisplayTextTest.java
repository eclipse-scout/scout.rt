/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractValueField#setAutoDisplayText(boolean)}.
 *
 * @deprecated Will be removed with the O-Release.
 */
@Deprecated
@RunWith(PlatformTestRunner.class)
@SuppressWarnings("deprecation")
public class ValueFieldAutoDisplayTextTest extends AbstractValueField<Integer> {

  private static Locale s_original_locale;

  @BeforeClass
  public static void setupBeforeClass() {
    s_original_locale = NlsLocale.get(false);
    NlsLocale.set(new Locale("de", "CH"));
  }

  @AfterClass
  public static void tearDownAfterClass() {
    NlsLocale.set(s_original_locale);
  }

  @Override
  protected Integer execValidateValue(Integer rawValue) {
    if (rawValue.equals(Integer.valueOf(13))) {
      throw new VetoException("Superstitious test class does not accept number 13.");
    }
    return super.execValidateValue(rawValue);
  }

  @Test
  public void testSetValidValue() {
    setAutoDisplayText(true);
    setValue(42);
    assertEquals("Value was not set as expected.", Integer.valueOf(42), getValue());
    assertEquals("DisplayText not as expected.", "42", getDisplayText());
    assertNull("No errorStatus expected for valid value.", getErrorStatus());
  }

  @Test
  public void testSetValidValueForNonAutoDisplayText() {
    setAutoDisplayText(false);
    setValue(42);
    assertEquals("Value was not set as expected.", Integer.valueOf(42), getValue());
    assertTrue("DisplayText should not be set.", StringUtility.isNullOrEmpty(getDisplayText()));
    assertNull("No errorStatus expected for valid value.", getErrorStatus());
  }

  @Test
  public void testSetInvalidValueDefault() {
    setAutoDisplayText(true);
    assertNull("Expected null as initial value.", getValue());
    setValue(13);
    assertNull("Expected still no value set, as 13 is invalid.", getValue());
    assertEquals("DisplayText should be, set when setting an invalid value.", "13", getDisplayText());
    assertNotNull("ErrorStatus expected for valid value.", getErrorStatus());
  }

  @Test
  public void testSetInvalidValueForNonAutoDisplayText() {
    setAutoDisplayText(false);
    assertNull("Expected null as initial value.", getValue());
    setValue(13);
    assertNull("Expected still no value set, as 13 is invalid.", getValue());
    assertTrue("DisplayText should not be set.", StringUtility.isNullOrEmpty(getDisplayText()));
    assertNotNull("ErrorStatus expected for valid value.", getErrorStatus());
  }

  @Test
  public void testSetInvalidValueFieldNotEmpty() {
    setAutoDisplayText(true);
    setValue(42);
    assertEquals("Expected 42 as initial value.", Integer.valueOf(42), getValue());
    setValue(13);
    assertEquals("Expected value to be unchanged, as 13 is invalid.", Integer.valueOf(42), getValue());
    assertEquals("DisplayText should be, set when setting an invalid value.", "13", getDisplayText());
    assertNotNull("ErrorStatus expected for valid value.", getErrorStatus());
  }
}
