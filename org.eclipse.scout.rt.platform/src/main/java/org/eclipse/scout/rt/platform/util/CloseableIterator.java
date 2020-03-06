/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.util;

import java.util.Iterator;

/**
 * Iterator that must be closed.
 */
public interface CloseableIterator<E> extends Iterator<E>, AutoCloseable {

  /**
   * Closes any resources allocated by this iterator. This method may be invoked multiple times.
   */
  @Override
  void close();
}
