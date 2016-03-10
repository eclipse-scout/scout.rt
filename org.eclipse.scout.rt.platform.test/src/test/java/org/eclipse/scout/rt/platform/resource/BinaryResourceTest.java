/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.resource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.junit.Assert;
import org.junit.Test;

public class BinaryResourceTest {

  // Default constructors should never fail
  @Test
  public void testConstructor() {
    String filename = "file.dat";
    byte[] emptyContent = new byte[0];
    byte[] content = new byte[]{'a', 'b', 'c'};

    new BinaryResource((String) null, null);
    new BinaryResource((String) null, content);
    new BinaryResource((String) null, emptyContent);
    new BinaryResource(filename, null);
    new BinaryResource(filename, content);
    new BinaryResource(filename, emptyContent);
  }

  @Test
  public void testBuilder() {
    String filename = "file.dat";
    String contentType = "application/binary";
    long modified = System.currentTimeMillis();
    byte[] emptyContent = new byte[0];
    byte[] content = new byte[]{'a', 'b', 'c'};
    BinaryResources.create().withLastModified(modified).build();
    BinaryResources.create().withLastModified(modified).withContent(content).build();
    BinaryResources.create().withLastModified(modified).withContent(emptyContent).build();
    BinaryResources.create().withLastModified(modified).withContentType(contentType).build();
    BinaryResources.create().withLastModified(modified).withContent(content).withContentType(contentType).build();
    BinaryResources.create().withLastModified(modified).withContent(emptyContent).withContentType(contentType).build();

    BinaryResources.create().withLastModified(modified).withContentType(contentType).withFilename(filename).build();
    BinaryResources.create().withLastModified(modified).withContent(content).withContentType(contentType).withFilename(filename).build();
    BinaryResources.create().withLastModified(modified).withContent(emptyContent).withContentType(contentType).withFilename(filename).build();
  }

  @Test
  public void testLastModified() {
    Assert.assertEquals(-1, BinaryResources.create().withFilename("document.txt").build().getLastModified());
    Assert.assertEquals(-1, BinaryResources.create().withFilename("document.txt").withLastModified(-1).build().getLastModified());

    long now = BEANS.get(IDateProvider.class).currentMillis().getTime();
    Assert.assertTrue(BinaryResources.create().withFilename("document.txt").withLastModifiedNow().build().getLastModified() >= now);
  }

  @Test
  public void testFullBuilder() {
    final String filename = "document.txt";
    final Charset charset = StandardCharsets.UTF_8;
    final byte[] content = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua".getBytes(charset);
    final String contentType = MimeType.TEXT_PLAIN.getType();
    final long lastModified = BEANS.get(IDateProvider.class).currentMillis().getTime();

    BinaryResource res = BinaryResources.create()
        .withFilename(filename)
        .withContent(content)
        .withContentType(contentType)
        .withCharset(charset)
        .withLastModified(lastModified)
        .build();

    Assert.assertEquals(filename, res.getFilename());
    Assert.assertEquals("UTF-8", res.getCharset());
    Assert.assertEquals(content, res.getContent());
    Assert.assertEquals(contentType, res.getContentType());
    Assert.assertEquals(lastModified, res.getLastModified());
    Assert.assertEquals(content.length, res.getContentLength());

    final byte[] newContent = "New content".getBytes(charset);
    BinaryResource newRes = BinaryResources.create(res).withContent(newContent).build();
    Assert.assertEquals(filename, newRes.getFilename());
    Assert.assertEquals("UTF-8", newRes.getCharset());
    Assert.assertEquals(newContent, newRes.getContent());
    Assert.assertEquals(contentType, newRes.getContentType());
    Assert.assertEquals(lastModified, newRes.getLastModified());
    Assert.assertEquals(newContent.length, newRes.getContentLength());
  }

  @Test
  public void testCreateAlias() {
    byte[] content = new byte[]{'a', 'b', 'c'};
    BinaryResource resource = new BinaryResource("index.txt", content);
    Assert.assertEquals("index.txt", resource.getFilename());
    Assert.assertArrayEquals(content, resource.getContent());

    BinaryResource aliasedResource = resource.createAlias("help.log");
    Assert.assertEquals("help.log", aliasedResource.getFilename());
    Assert.assertArrayEquals(content, aliasedResource.getContent());

    BinaryResource aliasedResource2 = resource.createAliasWithSameExtension("help");
    Assert.assertEquals("help.txt", aliasedResource2.getFilename());
    Assert.assertArrayEquals(content, aliasedResource2.getContent());

    BinaryResource aliasedResource3 = new BinaryResource("index", content).createAliasWithSameExtension("help");
    Assert.assertEquals("help", aliasedResource3.getFilename());
    Assert.assertArrayEquals(content, aliasedResource3.getContent());
  }
}
