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
package org.eclipse.scout.rt.ui.swt.test;

import org.eclipse.scout.rt.ui.swt.AbstractSwtEnvironment;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStrokeFilter;
import org.eclipse.swt.widgets.Widget;

/**
 * Utility class providing common functions that can be used in "org.eclipse.scout.rt.ui.swt.test"
 * 
 * @since Scout 4.1.0
 */
public final class SwtTestingUtility {

  /**
   * Create a dummy {@link AbstractSwtEnvironment}
   * 
   * @return swtEnvironment instance
   */
  public static AbstractSwtEnvironment createTestSwtEnvironment() {
    return new AbstractSwtEnvironment(null, null, null) {
      @Override
      public void addKeyStrokeFilter(Widget c, ISwtKeyStrokeFilter filter) {
        //ignore key strokes in test
      }
    };
  }

  private SwtTestingUtility() {
  }
}
