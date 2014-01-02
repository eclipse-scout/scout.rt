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
package org.eclipse.scout.rt.spec.client.gen.extract;

/**
 * A property of a scout model object of type <code>T</code> that should be documented.
 * <p>
 * E.g. the label of a <code>IFormField</code> should appear in the documentation table with table header "Label" and
 * the label of the respective form field as cell text.
 * </p>
 */
public interface IDocTextExtractor<T> {

  /**
   * @return header description for a this property
   */
  String getHeader();

  /**
   * @param object
   * @return a documentation text for a scout model object
   */
  String getText(T object);

}
