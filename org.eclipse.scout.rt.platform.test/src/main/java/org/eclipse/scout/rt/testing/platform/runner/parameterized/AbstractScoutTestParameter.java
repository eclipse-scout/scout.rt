/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.parameterized;

/**
 * This convenience class implements the interface {@link IScoutTestParameter}. It holds the name of the test parameter
 * and implements the getter.
 */
public abstract class AbstractScoutTestParameter implements IScoutTestParameter {

  /**
   * Name of the test parameter.
   */
  private final String m_name;

  /**
   * @param name
   *     of the test parameter
   */
  public AbstractScoutTestParameter(String name) {
    m_name = name;
  }

  @Override
  public String getName() {
    return m_name;
  }
}
