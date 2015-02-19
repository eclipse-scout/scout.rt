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
package org.eclipse.scout.rt.ui.swing;

/**
 * <p>
 * Implementations of ILookAndFeelProvider install a Swing look and feel. You may specify an ILookAndFeelProvider on
 * startup. Example:<br/>
 * <code>-Dscout.laf=fully.qualified.class.name.of.the.ILookAndFeelProvider</code>
 * </p>
 * <p>
 * The installLookAndFeel method allows to execute additional setup code used for the look and feel, which is primarily
 * used for non JRE look and feels.
 * </p>
 */
public interface ILookAndFeelProvider {

  /**
   * Installs the Swing look and feel.
   */
  void installLookAndFeel();

}
