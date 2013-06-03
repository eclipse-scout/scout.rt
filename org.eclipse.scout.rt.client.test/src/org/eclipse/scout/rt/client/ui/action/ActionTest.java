/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.annotations.Replace;
import org.junit.Test;

/**
 * JUnit tests for {@link AbstractAction}
 * 
 * @since 3.8.2
 */
public class ActionTest {

  @Test
  public void testGetFieldId() {
    assertEquals("BaseAction", new BaseAction().getActionId());
    assertEquals("BaseAction", new ExtendedTestAction().getActionId());
    assertEquals("ExtendedTestActionWithoutReplace", new ExtendedTestActionWithoutReplace().getActionId());
    //
    assertEquals("Custom", new TestActionWithCustomActionId().getActionId());
    assertEquals("Custom", new ExtendedTestActionWithCustomActionId().getActionId());
  }

  public static class BaseAction extends AbstractAction {
  }

  @Replace
  public static class ExtendedTestAction extends BaseAction {
  }

  public static class ExtendedTestActionWithoutReplace extends BaseAction {
  }

  public static class TestActionWithCustomActionId extends AbstractAction {
    @Override
    public String getActionId() {
      return "Custom";
    }
  }

  @Replace
  public static class ExtendedTestActionWithCustomActionId extends TestActionWithCustomActionId {
  }
}
