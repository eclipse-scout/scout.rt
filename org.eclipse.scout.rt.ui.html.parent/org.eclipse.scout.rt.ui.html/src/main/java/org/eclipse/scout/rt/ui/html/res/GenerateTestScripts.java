/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res;

import java.util.Arrays;

/**
 * Generate js and css files used for testing. This java class is run using the maven
 * org.codehaus.mojo/exec-maven-plugin in the phase generate-test-resources
 *
 * @since 5.0.0
 */
public class GenerateTestScripts {
  public static void main(String[] args) {
    System.out.println("HELLO MAVEN " + Arrays.toString(args));
  }

}
