/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
