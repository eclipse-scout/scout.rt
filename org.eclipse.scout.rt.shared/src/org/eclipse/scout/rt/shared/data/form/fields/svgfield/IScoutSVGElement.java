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
package org.eclipse.scout.rt.shared.data.form.fields.svgfield;

import java.io.Serializable;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * The interface for all scout svg items
 */
public interface IScoutSVGElement extends Serializable {

  String getId();

  /**
   * @return true if the element is interactive (clickable and context menu provider in ui)
   */
  boolean isInteractive();

  void read(SimpleXmlElement node) throws ProcessingException;

  void write(SimpleXmlElement node) throws ProcessingException;

}
