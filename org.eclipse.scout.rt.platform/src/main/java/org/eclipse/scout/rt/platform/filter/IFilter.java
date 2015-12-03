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
package org.eclipse.scout.rt.platform.filter;

/**
 * Filter to accept elements for processing.
 *
 * @since 5.1
 */
public interface IFilter<ELEMENT> {

  /**
   * @return <code>true</code> to accept the given element, <code>false</code> otherwise.
   */
  boolean accept(ELEMENT element);
}
