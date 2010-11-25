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
package org.eclipse.scout.rt.ui.swing.form.fields.tablefield;

import javax.swing.JComponent;

import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

/**
 * Table field with status not under the table but on the desktop
 */
public class LegacySwingScoutTableField extends SwingScoutTableField {

  /**
   * complete override
   */
  @Override
  protected ISwingTableStatus createSwingTableStatus(JComponent container) {
    if (getScoutObject().isTableStatusVisible()) {
      return new ISwingTableStatus() {
        @Override
        public void setStatusText(String s) {
          ISwingEnvironment env = getSwingEnvironment();
          if (env != null && env.getRootComposite() != null) {
            //bsi ticket 95826: eliminate newlines
            if (s != null) {
              s = s.replaceAll("[\\s]+", " ");
            }
            env.getRootComposite().setSwingStatus(new ProcessingStatus(s != null ? s : "", ProcessingStatus.INFO));
          }
        }
      };
    }
    return null;
  }

  @Override
  protected void detachScout() {
    if (getSwingTableStatus() != null) {
      getSwingTableStatus().setStatusText(null);
    }
    super.detachScout();
  }
}
