/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form.fields.composer;

import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;

/**
 * Data representation for a composer either/or value instance in a {@link AbstractComposerData}
 */
public class ComposerEitherOrNodeData extends TreeNodeData {
  private static final long serialVersionUID = 1L;

  private boolean m_beginOfEitherOr;
  private boolean m_negated = false;

  public boolean isBeginOfEitherOr() {
    return m_beginOfEitherOr;
  }

  public void setBeginOfEitherOr(boolean beginOfEitherOr) {
    m_beginOfEitherOr = beginOfEitherOr;
  }

  public boolean isNegative() {
    return m_negated;
  }

  public void setNegative(boolean b) {
    m_negated = b;
  }
}
