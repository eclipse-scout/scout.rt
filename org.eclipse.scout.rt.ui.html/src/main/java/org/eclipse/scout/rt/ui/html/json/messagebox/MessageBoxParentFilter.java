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
package org.eclipse.scout.rt.ui.html.json.messagebox;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.client.ui.desktop.outline.IMessageBoxParent;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;

/**
 * Filter to accept message-boxes attached to a specific {@link IMessageBoxParent}.
 */
public class MessageBoxParentFilter implements IFilter<IMessageBox> {

  private final IMessageBoxParent m_messageBoxParent;

  public MessageBoxParentFilter(final IMessageBoxParent messageBoxParent) {
    m_messageBoxParent = messageBoxParent;
  }

  @Override
  public boolean accept(final IMessageBox messageBox) {
    return m_messageBoxParent == messageBox.messageBoxParent();
  }
}
