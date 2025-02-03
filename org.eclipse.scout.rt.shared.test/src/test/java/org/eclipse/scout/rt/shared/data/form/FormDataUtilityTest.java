/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit tests for {@link FormDataUtility}
 *
 * @since 3.8.0
 */
public class FormDataUtilityTest {

  @Test
  public void testGetFieldDataId() {
    assertNull(FormDataUtility.getFieldDataId((String) null));
    assertNull(FormDataUtility.getFieldDataId(""));
    assertNull(FormDataUtility.getFieldDataId("   "));
    assertEquals("Test", FormDataUtility.getFieldDataId("Test"));
    assertEquals("Test", FormDataUtility.getFieldDataId("  Test  "));
    assertEquals("Name", FormDataUtility.getFieldDataId("NameField"));
    assertEquals("Name", FormDataUtility.getFieldDataId("NameField  "));
    assertEquals("Name", FormDataUtility.getFieldDataId("  NameField  "));
    assertEquals("1234", FormDataUtility.getFieldDataId("1234"));
    assertEquals("1234", FormDataUtility.getFieldDataId("1234Field"));
    assertEquals("TopBox", FormDataUtility.getFieldDataId("TopBox"));
    assertEquals("Ok", FormDataUtility.getFieldDataId("OkButton"));
  }
}
