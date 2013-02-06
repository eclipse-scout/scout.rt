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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class CalendarItemTransferable implements Transferable {

  private Object m_item;

  public CalendarItemTransferable(Object item) {
    m_item = item;
  }

  static final DataFlavor[] SUPPORTED_FLAVORS = {null};

  static {
    try {
      SUPPORTED_FLAVORS[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
      return m_item;
    }
    else {
      return null;
    }
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return SUPPORTED_FLAVORS;
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType);
  }
}
