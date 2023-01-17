/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.selenium.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil;

/**
 * Marker for unit tests that should not be executed if {@link SeleniumUtil#isMacOS()} returns <code>true</code>.
 * <p>
 * Currently you should use this marker for tests on a Mac that make use of the COMMAND key (like CTRL key for PCs) It
 * seems that webdriver on Mac doesn't support 'native' key combinations like COMMAND+C/V for copy and paste. There's no
 * easy workaround (we could run a bit of JS code on the mac to do copy/paste but currently that's not worth the
 * effort).
 *
 * @see <a href="https://bugs.chromium.org/p/chromedriver/issues/detail?id=30">
 *      https://bugs.chromium.org/p/chromedriver/issues/detail?id=30</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IgnoreTestOnMacOS {
}
