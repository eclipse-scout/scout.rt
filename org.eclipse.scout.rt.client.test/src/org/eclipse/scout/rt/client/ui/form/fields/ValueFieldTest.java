/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractValueField}
 */
@RunWith(ScoutClientTestRunner.class)
public class ValueFieldTest {
  private static final String PARSE_ERROR_MESSAGE = "Parse Error Message";
  private static final String UNPARSABLE_VALUE = "unparsable";

  @Test
  public void testNoInitialError() {
    IValueField<String> v = new ParseErrorField();
    assertTrue(v.isContentValid());
  }

  @Test
  public void testParseError() {
    IValueField<String> v = new ParseErrorField();
    v.parseValue(UNPARSABLE_VALUE);
    assertEquals(PARSE_ERROR_MESSAGE, v.getErrorStatus().getMessage());
    assertEquals(IStatus.ERROR, v.getErrorStatus().getSeverity());
    assertFalse(v.isContentValid());
  }

  @Test
  public void testResetParse() throws Exception {
    IValueField<String> v = new ParseErrorField();
    v.parseValue(UNPARSABLE_VALUE);
    v.parseValue("valid");
    assertTrue(v.isContentValid());
  }

  static class ParseErrorField extends AbstractValueField<String> {
    @Override
    protected String execParseValue(String text) throws ProcessingException {
      if (UNPARSABLE_VALUE.equals(text)) {
        throw new ProcessingException(PARSE_ERROR_MESSAGE);
      }
      return text;
    }
  }

}
