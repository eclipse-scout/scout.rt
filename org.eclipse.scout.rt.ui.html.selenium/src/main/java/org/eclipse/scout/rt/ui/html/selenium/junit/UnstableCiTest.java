/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.selenium.junit;

/**
 * A category used to mark tests that are unstable on the CI build system but are successful when run locally. When the
 * CI build executes tests, this category is excluded. So you should only apply this category when there's no way to fix
 * the test for CI build - usually there's some sort of timing problem, when a test fails on CI build.
 */
public class UnstableCiTest implements UiTestCategory {

}
