/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.validate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Test;

public class ValidationUtilityTest {

  @Test
  public void testTreat0AsNull() {
    Integer intVal1 = new Integer(8);
    Integer intval2 = new Integer(0);
    assertEquals(intVal1, ValidationUtility.treat0AsNull(intVal1));
    assertEquals(null, ValidationUtility.treat0AsNull(intval2));
  }

  @Test
  public void testCheckMaxLength() {
    String value1 = "Hans Musterli";
    Integer length1 = value1.length() - 1;

    try {
      ValidationUtility.checkMaxLength(value1, length1);
      fail("Expected Exception");
    }
    catch (ProcessingException e) {
      // nop
    }

    String value2 = "My name is Martin Meyer";
    Integer expectedLength2 = value2.length();
    try {
      ValidationUtility.checkMaxLength(value2, expectedLength2);
    }
    catch (ProcessingException e) {
      fail("testCheckMaxLength failed");
    }

  }
}
