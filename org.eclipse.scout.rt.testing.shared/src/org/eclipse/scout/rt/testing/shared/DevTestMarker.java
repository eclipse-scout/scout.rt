/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.core.runtime.Platform;
import org.junit.Test;

/**
 * This marker annotation is used in development mode {@link Platform#inDevelopmentMode()} to annotate a single test
 * that is to be run alone.
 * <p>
 * This is useful when writing a new test case and the test case is to be debugged or test-run. In development mode only
 * the {@link Test} marked with DevTestMarker is run when using {@link ScoutJUnitPluginTestExecutor}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DevTestMarker {
}
