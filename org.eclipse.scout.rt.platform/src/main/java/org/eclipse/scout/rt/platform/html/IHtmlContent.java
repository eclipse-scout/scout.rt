/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html;

import java.io.Serializable;

/**
 * Marker interface for any HTML content that may contain bind variables.
 */
public interface IHtmlContent extends CharSequence, Serializable {

  String toHtml();

  String toPlainText();

  /**
   * See {@link #withNewLineToBr(boolean)}.
   */
  boolean isNewLineToBr();

  /**
   * @param newLineToBr
   *     {@code true} if new lines should be replaced by &lt;br&gt; tags, {@code false} otherwise (default is
   *     {@code true}).
   */
  IHtmlContent withNewLineToBr(boolean newLineToBr);
}
