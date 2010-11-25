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
package org.eclipse.scout.rt.client.ui.form;

public enum PrintDevice {
  /**
   * <pre>
   * Print to a printer device
   * Further parameters are normally passed by a parameter map of type Map&lt;String,Object&gt;
   * printerName=String (optional)
   * jobName=String (optional)
   * </pre>
   */
  Printer(1),
    /**
     * <pre>
     * Print to a printer device
     * Further parameters are normally passed by a parameter map of type Map&lt;String,Object&gt;
     * file=java.io.File
     * contentType=String (optional)
     * </pre>
     */
    File(2);

  private int value;

  PrintDevice(int value) {
    this.value = value;
  }
}
