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
package org.eclipse.scout.rt.testing.platform.runner.parameterized;

/**
 * This convenience class implements the interface {@link IScoutTestParameter}. It holds the name of the test parameter
 * and implements the getter.
 */
public abstract class AbstractScoutTestParameter implements IScoutTestParameter {

  /** Name of the test parameter. */
  private final String m_name;

  /**
   * @param name
   *          of the test parameter
   */
  public AbstractScoutTestParameter(String name) {
    m_name = name;
  }

  @Override
  public String getName() {
    return m_name;
  }
}
