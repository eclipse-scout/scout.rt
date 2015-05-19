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
package org.eclipse.scout.rt.ui.swing.inject;

import org.eclipse.scout.commons.ConfigUtility;
import org.eclipse.scout.rt.ui.swing.ILookAndFeelProvider;

/**
 *
 */
public class InitLookAndFeelInjector {

  public InitLookAndFeelInjector() {
  }

  public void inject() {
    String scoutLaf = ConfigUtility.getProperty("scout.laf");
    if (!isStringEmpty(scoutLaf)) {
      initScoutLAF(scoutLaf);
      return;
    }
    String defaultLaf = ConfigUtility.getProperty("swing.defaultlaf");
    if (!isStringEmpty(defaultLaf)) {
      initDefaultLAF(defaultLaf);
    }
  }

  protected boolean isStringEmpty(String s) {
    return s == null || s.length() == 0;
  }

  /**
   * @param scoutLaf
   */
  protected void initScoutLAF(String scoutLaf) {
    try {
      Class<?> lafClass = getClass().getClassLoader().loadClass(scoutLaf);
      Object o = lafClass.newInstance();
      if (!(o instanceof ILookAndFeelProvider)) {
        throw new IllegalArgumentException("class provided for scout.laf=" + scoutLaf + " is not an instance of ILookAndFeelProvider");
      }
      ILookAndFeelProvider lafProvider = (ILookAndFeelProvider) o;
      lafProvider.installLookAndFeel();
    }
    catch (Exception e) {
      throw new IllegalArgumentException("failed to instantiate class provided for scout.laf=" + scoutLaf, e);
    }
  }

  protected void initDefaultLAF(String defaultLaf) {
    System.setProperty("swing.defaultlaf", defaultLaf);
  }

}
