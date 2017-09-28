/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
