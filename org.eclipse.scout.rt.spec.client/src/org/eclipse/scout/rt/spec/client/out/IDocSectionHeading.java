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
package org.eclipse.scout.rt.spec.client.out;

/**
 * A section heading containing a title to be displayed.
 */
// TODO ASA refactor: is IDocSectionHeading still needed
public interface IDocSectionHeading {

  /**
   * @return a name of the heading
   */
  String getName();

}
