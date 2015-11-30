/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.resource;

import org.junit.Test;

public class BinaryResourceTest {

  // Default constructors should never fail
  @Test
  public void testConstructor() {
    String filename = "file.dat";
    MimeType mimeType = MimeType.TEXT_PLAIN;
    String contentType = "application/binary";
    long modified = System.currentTimeMillis();
    byte[] emptyContent = new byte[0];
    byte[] content = new byte[]{'a', 'b', 'c'};

    new BinaryResource((String) null, null);
    new BinaryResource((String) null, content);
    new BinaryResource((String) null, emptyContent);
    new BinaryResource(filename, null);
    new BinaryResource(filename, content);
    new BinaryResource(filename, emptyContent);

    new BinaryResource((MimeType) null, null);
    new BinaryResource((MimeType) null, content);
    new BinaryResource(mimeType, null);
    new BinaryResource(mimeType, content);

    new BinaryResource(null, null, null);
    new BinaryResource(null, null, content);
    new BinaryResource(null, null, emptyContent);
    new BinaryResource(null, contentType, null);
    new BinaryResource(null, contentType, content);
    new BinaryResource(null, contentType, emptyContent);
    new BinaryResource(filename, contentType, null);
    new BinaryResource(filename, contentType, content);
    new BinaryResource(filename, contentType, emptyContent);

    new BinaryResource(null, null, null, modified);
    new BinaryResource(null, null, content, modified);
    new BinaryResource(null, null, emptyContent, modified);
    new BinaryResource(null, contentType, null, modified);
    new BinaryResource(null, contentType, content, modified);
    new BinaryResource(null, contentType, emptyContent, modified);
    new BinaryResource(filename, contentType, null, modified);
    new BinaryResource(filename, contentType, content, modified);
    new BinaryResource(filename, contentType, emptyContent, modified);
  }

  @Test(expected = NullPointerException.class)
  public void testFileConstructor() {
    new BinaryResource(null);
  }
}
