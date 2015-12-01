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
package org.eclipse.scout.rt.platform.visitor;

/**
 * Visitor for visiting elements.
 *
 * @since 5.1
 */
public interface IVisitor<ELEMENT> {

  /**
   * Is called upon visiting an element.
   *
   * @return <code>true</code>=continue visiting, <code>false</code>=end visiting.
   */
  boolean visit(ELEMENT element);
}
