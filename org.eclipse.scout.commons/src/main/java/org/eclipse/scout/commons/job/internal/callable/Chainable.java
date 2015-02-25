/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.job.internal.callable;

import java.util.concurrent.Callable;

/**
 * Interface to mark a processing object as 'chainable' in the language of the design pattern 'chain-of-responsibility'.
 * <p/>
 * <i>Currently, this interface is only used to test the concatenation of element processors in JUnit.</i>
 *
 * @param <RESULT>
 *          the result type of the callable's computation.
 * @since 5.1
 */
public interface Chainable<RESULT> {

  /**
   * @return next element in the chain.
   */
  Callable<RESULT> getNext();
}
