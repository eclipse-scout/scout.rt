/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.selenium.junit;

/**
 * A category used to mark tests that are unstable on the CI build system but are successful when run locally. When the
 * CI build executes tests, this category is excluded. So you should only apply this category when there's no way to fix
 * the test for CI build - usually there's some sort of timing problem, when a test fails on CI build.
 * <p>
 * <strong>IMPORTANT</strong>: when you must use this category, always document <i>why</i> you had to use it. This will
 * help to analyze or solve the issue in a later release.
 */
public class UnstableCiTest implements UiTestCategory {

}
