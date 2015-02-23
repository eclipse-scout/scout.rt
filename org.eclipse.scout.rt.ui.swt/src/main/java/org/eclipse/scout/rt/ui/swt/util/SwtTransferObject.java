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

import org.eclipse.swt.dnd.Transfer;

public class SwtTransferObject {
  private Transfer m_transfer;
  private Object m_data;

  public SwtTransferObject(Transfer transfer, Object data) {
    m_transfer = transfer;
    m_data = data;
  }

  public Transfer getTransfer() {
    return m_transfer;
  }

  public void setTransfer(Transfer transfer) {
    m_transfer = transfer;
  }

  public Object getData() {
    return m_data;
  }

  public void setData(Object data) {
    m_data = data;
  }
}
