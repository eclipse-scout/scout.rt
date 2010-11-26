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

import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.UIManager;
import javax.swing.plaf.synth.SynthLookAndFeel;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.ILookAndFeelProvider;
import org.eclipse.scout.rt.ui.swing.internal.Activator;

/**
 *
 */
public class InitLookAndFeelInjector {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(InitLookAndFeelInjector.class);

  public InitLookAndFeelInjector() {
  }

  public void inject(Properties initProperties) {
    String scoutLaf = initProperties.getProperty("scout.laf");
    if (!isStringEmpty(scoutLaf)) {
      initScoutLAF(scoutLaf);
      return;
    }
    String synthLaf = initProperties.getProperty("javax.swing.plaf.synth.style");
    if (!isStringEmpty(synthLaf)) {
      initSynthLAF(synthLaf);
      return;
    }
    String defaultLaf = initProperties.getProperty("swing.defaultlaf");
    if (!isStringEmpty(defaultLaf)) {
      initDefaultLAF(defaultLaf);
    }
  }

  protected boolean isStringEmpty(String s) {
    return s == null || s.length() == 0;
  }

  protected void initSynthLAF(String synthStyleProperty) {
    SynthLookAndFeel synthLaf = new SynthLookAndFeel();
    try {
      URL url = null;
      String resourceName = null;
      Matcher m = Pattern.compile("(.*\\.)?([^.]+\\.xml)").matcher(synthStyleProperty);
      if (m.matches()) {
        resourceName = "/" + m.group(1).replace('.', '/') + m.group(2);
        url = Activator.getDefault().getBundle().getResource(resourceName);
      }
      if (url == null) {
        throw new IllegalArgumentException("config.ini: javax.swing.plaf.synth.style=" + synthLaf + ": resource " + resourceName + " could not be found");
      }
      synthLaf.load(url);
      UIManager.setLookAndFeel(synthLaf);
      LOG.info("Installed Synth L&F with " + synthStyleProperty);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("config.ini: javax.swing.plaf.synth.style=" + synthLaf, e);
    }
  }

  /**
   * @param scoutLaf
   */
  protected void initScoutLAF(String scoutLaf) {
    try {
      Class<?> lafClass = Activator.getDefault().getBundle().loadClass(scoutLaf);
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
