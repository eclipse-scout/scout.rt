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
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import org.eclipse.scout.rt.client.ui.form.fields.ScoutFieldStatus;
import org.eclipse.scout.rt.platform.Order;

/**
 * Marker subclass so we know inside
 * {@link AbstractSequenceBox#execCheckFromTo(org.eclipse.scout.rt.client.ui.form.fields.IValueField[], int)} that a
 * previous failure was due to invalid sequence values and can safely be cleared once the sequence is valid again.
 */
@Order(50)
public class InvalidSequenceStatus extends ScoutFieldStatus {
  private static final long serialVersionUID = 1L;

  public InvalidSequenceStatus(String message) {
    super(message, ERROR);
  }

}
