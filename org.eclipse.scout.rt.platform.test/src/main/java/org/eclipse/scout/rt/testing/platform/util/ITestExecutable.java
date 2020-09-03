/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform.util;

import org.junit.function.ThrowingRunnable;

/**
 * Functional interface allowing lambda expressions that are throwing any {@link Throwable}.
 * @deprecated Use {@link ThrowingRunnable} instead. Will be removed in Scout 12.
 */
@Deprecated
@FunctionalInterface
public interface ITestExecutable {

  void execute() throws Throwable; // NOSONAR squid:S1181
}
