/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.print;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class PrintEvent extends EventObject implements IModelEvent {

  private static final long serialVersionUID = 1L;

  public static final int TYPE_PRINT_START = 100;
  public static final int TYPE_PRINT_DONE = 200;

  private final int m_type;

  public PrintEvent(Object source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  public int getType() {
    return m_type;
  }

}
