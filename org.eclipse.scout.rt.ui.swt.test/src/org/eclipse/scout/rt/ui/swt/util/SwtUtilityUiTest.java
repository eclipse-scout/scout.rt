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
package org.eclipse.scout.rt.ui.swt.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * JUnit tests for {@link SwtUtility}.
 * Tests in this class require an UI.
 * 
 * @since 3.10.0-M6
 */
public class SwtUtilityUiTest {
  /**
   * Test for {@link SwtUtility#runSwtInputVerifier()}. The verifier should return <code>True</code> if no control has
   * the focus, this can be the case if a tray menu was selected
   */
  @Test
  public void testRunSwtInputVerifier() {
    assertTrue(SwtUtility.runSwtInputVerifier());
    assertTrue(SwtUtility.runSwtInputVerifier(null));
  }
}
