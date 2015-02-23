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
package org.eclipse.scout.rt.ui.swt.util;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

public final class JVMLocalObjectTransfer extends ByteArrayTransfer {
  private static final JVMLocalObjectTransfer INSTANCE = new JVMLocalObjectTransfer();
  private static final String CF_TEXT = "CF_TEXT"; //$NON-NLS-1$
  private static final int CF_TEXTID = 1;

  private transient Object m_localObject;

  private JVMLocalObjectTransfer() {
  }

  /**
   * Returns the singleton instance of the JavaElementTransfer class.
   * 
   * @return the singleton instance of the JavaElementTransfer class
   */
  public static JVMLocalObjectTransfer getInstance() {
    return INSTANCE;
  }

  @Override
  public void javaToNative(Object object, TransferData transferData) {
    m_localObject = object;
    super.javaToNative(new byte[]{'J'}, transferData);
  }

  @Override
  public Object nativeToJava(TransferData transferData) {
    super.nativeToJava(transferData);
    return m_localObject;
  }

  @Override
  protected int[] getTypeIds() {
    return new int[]{CF_TEXTID};
  }

  @Override
  protected String[] getTypeNames() {
    return new String[]{CF_TEXT};
  }
}
