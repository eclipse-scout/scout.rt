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
package org.eclipse.scout.rt.ui.swt.window.desktop.view;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.test.SwtTestingUtility;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Junit tests for {@link AbstractScoutView}
 * 
 * @since Scout 4.1.0
 */
public class ScoutViewTest {

  /**
   * Ensure that the scout event {@link IForm#PROP_SAVE_NEEDED} is handled by the scout view.
   * current implementation fires a {@link IWorkbenchPartConstants#PROP_DIRTY} property changed event.
   * 
   * @See Bug 437869.
   * @throws Exception
   */
  @Test
  public void testSaveNeededPropertyHandled() throws Exception {
    final ISwtEnvironment env = SwtTestingUtility.createTestSwtEnvironment();
    AbstractScoutView viewUnderTest = new AbstractScoutView() {

      @Override
      protected ISwtEnvironment getSwtEnvironment() {
        return env;
      }
    };

    IPropertyListener mock = Mockito.mock(IPropertyListener.class);
    viewUnderTest.addPropertyListener(mock);

    viewUnderTest.handleScoutPropertyChange(IForm.PROP_SAVE_NEEDED, true);

    Mockito.verify(mock).propertyChanged(viewUnderTest, IWorkbenchPartConstants.PROP_DIRTY);
  }
}
