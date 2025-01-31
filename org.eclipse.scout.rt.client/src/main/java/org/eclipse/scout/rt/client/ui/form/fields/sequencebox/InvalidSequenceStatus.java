/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.status.Status;

/**
 * Marker subclass so we know inside
 * {@link AbstractSequenceBox#execCheckFromTo(org.eclipse.scout.rt.client.ui.form.fields.IValueField[], int)} that a
 * previous failure was due to invalid sequence values and can safely be cleared once the sequence is valid again.
 */
@Order(50)
public class InvalidSequenceStatus extends Status {
  private static final long serialVersionUID = 1L;

  public InvalidSequenceStatus(String message) {
    super(message, ERROR);
  }
}
