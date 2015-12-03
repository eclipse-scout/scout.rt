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
package org.eclipse.scout.rt.client.services.common.bookmark;

import java.util.EventObject;

public class BookmarkServiceEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private int m_type;

  public static final int TYPE_CHANGED = 10;

  public BookmarkServiceEvent(IBookmarkService service, int type) {
    super(service);
    m_type = type;
  }

  public IBookmarkService getBookmarkService() {
    return (IBookmarkService) getSource();
  }

  public int getType() {
    return m_type;
  }

}
