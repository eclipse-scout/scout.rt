/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.junit.Test;

public class JsonAdapterPropertyConfigBuilderTest {

  @Test
  public void testDefault() {
    JsonAdapterPropertyConfig config = new JsonAdapterPropertyConfig();
    assertTrue(config.isDisposeOnChange());
    assertFalse(config.isGlobal());
    assertNull(config.getFilter());
  }

  @Test
  public void testDisposeOnChange() {
    JsonAdapterPropertyConfig config = new JsonAdapterPropertyConfigBuilder().disposeOnChange(false).build();
    assertFalse(config.isDisposeOnChange());
    assertFalse(config.isGlobal());
    assertNull(config.getFilter());
  }

  @Test
  public void testGlobal() {
    JsonAdapterPropertyConfig config = new JsonAdapterPropertyConfigBuilder().global().build();
    assertFalse(config.isDisposeOnChange());
    assertTrue(config.isGlobal());
    assertNull(config.getFilter());
  }

  @Test
  public void testFilter() {
    DisplayableActionFilter<IAction> actionFilter = new DisplayableActionFilter<>();
    JsonAdapterPropertyConfig config = new JsonAdapterPropertyConfigBuilder().filter(actionFilter).build();
    assertTrue(config.isDisposeOnChange());
    assertFalse(config.isGlobal());
    assertSame(actionFilter, config.getFilter());
  }
}
