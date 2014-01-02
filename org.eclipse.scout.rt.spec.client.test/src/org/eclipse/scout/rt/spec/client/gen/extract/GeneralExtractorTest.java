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
package org.eclipse.scout.rt.spec.client.gen.extract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.spec.client.gen.extract.action.SingleSelectionExtractor;
import org.junit.Test;

/**
 *
 */
public class GeneralExtractorTest {

  /**
   * Tests that {@link SingleSelectionExtractor#getText(AbstractAction)} return the true text
   * {@link AbstractBooleanTextExtractor.DOC_ID_TRUE}, if single selection is enabled.
   */
  @Test
  public void testSimpleTypeExtractor() {
    SimpleTypeTextExtractor<AbstractAction> ex = new SimpleTypeTextExtractor<AbstractAction>();
    AbstractAction testAction = mock(AbstractAction.class);
    String text = ex.getText(testAction);
    assertEquals(testAction.getClass().getSimpleName(), text);
  }

  @Test
  public void testLinkTypeExtractor() {
    AbstractAction testAction = mock(AbstractAction.class);
    LinkableTypeExtractor<AbstractAction> ex = new LinkableTypeExtractor<AbstractAction>();
    String text = ex.getText(testAction);
    assertTrue(text.contains(testAction.getClass().getName()));
  }
}
