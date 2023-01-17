/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
    final String stringContent = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. öüä."; // do not remove umlaut
    final byte[] content = stringContent.getBytes(charset);
    final String contentType = MimeType.TXT.getType();
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
    Assert.assertArrayEquals(content, res.getContent());
    Assert.assertEquals(contentType, res.getContentType());
    Assert.assertEquals(lastModified, res.getLastModified());
    Assert.assertEquals(content.length, res.getContentLength());

    final byte[] newContent = "New content".getBytes(charset);
    res = BinaryResources.create(res).withContent(newContent).build();
    Assert.assertEquals(filename, res.getFilename());
    Assert.assertEquals("UTF-8", res.getCharset());
    Assert.assertArrayEquals(newContent, res.getContent());
    Assert.assertEquals(contentType, res.getContentType());
    Assert.assertEquals(lastModified, res.getLastModified());
    Assert.assertEquals(newContent.length, res.getContentLength());

    // with string content: null
    res = BinaryResources.create().withContent(content).withContent((String) null).build();
    Assert.assertNull(res.getCharset());
    Assert.assertNull(res.getContent());

    // with string content: default encoding
    res = BinaryResources.create().withContent(stringContent).build();
    Assert.assertEquals("UTF-8", res.getCharset());
    Assert.assertArrayEquals(stringContent.getBytes(StandardCharsets.UTF_8), res.getContent());

    // with string content: iso 8859-1 encoding
    res = BinaryResources.create().withContent(stringContent, StandardCharsets.ISO_8859_1).build();
    Assert.assertEquals("ISO-8859-1", res.getCharset());
    Assert.assertArrayEquals(stringContent.getBytes(StandardCharsets.ISO_8859_1), res.getContent());
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
