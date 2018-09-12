/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.util;

/**
 * Functional interface allowing lambda expressions that are throwing any {@link Throwable}.
 */
@FunctionalInterface
public interface ITestExecutable {

  void execute() throws Throwable; // NOSONAR squid:S1181
}
