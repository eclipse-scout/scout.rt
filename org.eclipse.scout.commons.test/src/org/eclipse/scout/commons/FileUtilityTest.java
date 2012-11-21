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
package org.eclipse.scout.commons;

import java.io.File;
import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 3.9.0
 */
public class FileUtilityTest extends Assert {

  @Test
  public void testIsZipFile() throws ProcessingException {
    Assert.assertTrue("zip.zip is not a zip file", FileUtility.isZipFile(getFile("zip.zip")));
    Assert.assertFalse("nozip.zip is a zip file", FileUtility.isZipFile(getFile("nozip.zip")));
  }

  private File getFile(String fileName) throws ProcessingException {
    URL resource = FileUtilityTest.class.getResource(fileName);
    return new File(resource.getPath());
  }
}
