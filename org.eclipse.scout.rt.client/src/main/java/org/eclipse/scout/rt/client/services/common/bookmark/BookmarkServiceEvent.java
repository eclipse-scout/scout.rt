/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.bookmark;

import java.util.EventObject;

public class BookmarkServiceEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private final int m_type;

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
