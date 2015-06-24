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
package org.eclipse.scout.rt.ui.html.json.form.fields;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
  public void testGlobal() throws Exception {
    JsonAdapterPropertyConfig config = new JsonAdapterPropertyConfigBuilder().global().build();
    assertFalse(config.isDisposeOnChange());
    assertTrue(config.isGlobal());
    assertNull(config.getFilter());
  }

  @Test
  public void testFilter() throws Exception {
    DisplayableActionFilter<IAction> actionFilter = new DisplayableActionFilter<IAction>();
    JsonAdapterPropertyConfig config = new JsonAdapterPropertyConfigBuilder().filter(actionFilter).build();
    assertTrue(config.isDisposeOnChange());
    assertFalse(config.isGlobal());
    assertSame(actionFilter, config.getFilter());
  }

}
