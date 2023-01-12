/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.testcategory;

import org.junit.experimental.categories.Category;

/**
 * A marker interface for tests which require a lot of time to be executed. These tests can be excluded during
 * continuous builds but are run on a daily basis. Try to avoid this category by rewriting tests.
 * <p>
 * Used together with {@link Category} annotation in unit-tests.
 */
public interface SlowTest extends ITestCategory {
}
