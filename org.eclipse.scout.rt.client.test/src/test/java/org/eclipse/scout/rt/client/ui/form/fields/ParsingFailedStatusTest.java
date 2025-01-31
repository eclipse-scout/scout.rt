/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.junit.Test;

public class ParsingFailedStatusTest {

  @Test
  public void testConstructorCopiesCode() {
    ProcessingException veto = new VetoException("Foo").withCode(123);
    ParsingFailedStatus status = new ParsingFailedStatus(veto, "Bar");
    assertEquals("Foo", status.getMessage());
    assertEquals(123, status.getCode());
  }

  @Test
  public void testConstructorWithoutCode() {
    ProcessingException veto = new VetoException("Foo");
    ParsingFailedStatus status = new ParsingFailedStatus(veto, "Bar");
    assertEquals("Foo", status.getMessage());
    assertEquals(0, status.getCode()); // default value for in members
  }
}
