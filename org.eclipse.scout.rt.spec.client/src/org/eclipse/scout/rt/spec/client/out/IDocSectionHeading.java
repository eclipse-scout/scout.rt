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
 * A section heading containing an id that can be referenced and a title to be displayed.
 */
public interface IDocSectionHeading {

  /**
   * @return a unique id
   */
  String getId();

  /**
   * @return a name of the heading
   */
  String getName();

  /**
   * @return <code>true</code>, if heading contains a valid id and name
   */
  boolean isValid();

}
