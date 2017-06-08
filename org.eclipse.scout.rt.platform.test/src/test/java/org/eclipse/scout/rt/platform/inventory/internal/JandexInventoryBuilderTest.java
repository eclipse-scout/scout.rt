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
package org.eclipse.scout.rt.platform.inventory.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.eclipse.scout.rt.platform.inventory.internal.JandexInventoryBuilder.RebuildStrategy;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.junit.Test;

public class JandexInventoryBuilderTest {

  @Test
  public void testScanJar() throws URISyntaxException, MalformedURLException {
    URL jarUrl = getClass().getResource("test repository/test.jar_");
    URL scoutXml = new URL("jar:" + jarUrl.toExternalForm() + "!/" + JandexInventoryBuilder.SCOUT_XML_PATH);
    JandexInventoryBuilder builder = new JandexInventoryBuilder(RebuildStrategy.ALWAYS);
    Index index = builder.scanModule(builder.findIndexUri(scoutXml));
    assertNotNull(index);
  }

  @Test
  public void testScanFolder() {
    URL scoutXml = getClass().getResource("/" + JandexInventoryBuilder.SCOUT_XML_PATH);
    JandexInventoryBuilder builder = new JandexInventoryBuilder(RebuildStrategy.ALWAYS);
    Index index = builder.scanModule(builder.findIndexUri(scoutXml));
    assertNotNull(index);
    ClassInfo c = index.getClassByName(DotName.createSimple(FixtureJandexInventoryObject.class.getName()));
    assertNotNull(c);
    assertEquals(FixtureJandexInventoryObject.class.getSimpleName(), c.simpleName());
  }

  @Test
  public void testScanModuleWithSpaceInPath() throws IOException, URISyntaxException {
    JandexInventoryBuilder builder = new JandexInventoryBuilder(RebuildStrategy.ALWAYS);
    Index index1 = builder.scanModule(builder.findIndexUri(getClass().getResource("test repository/META-INF/scout.xml")));
    Index index2 = builder.scanModule(builder.findIndexUri(new URL("jar:" + getClass().getResource("test repository/test.jar_").toExternalForm() + "!/META-INF/scout.xml")));
    assertNotNull(index1);
    assertNotNull(index2);
  }

  @Test
  public void testScanFolderWithRebuildStrategyAlways() {
    URL scoutXml = getClass().getResource("/" + JandexInventoryBuilder.SCOUT_XML_PATH);
    URI indexUri = new JandexInventoryBuilder(RebuildStrategy.ALWAYS).findIndexUri(scoutXml);
    File indexFile = new File(indexUri);
    FixtureJandexInventoryBuilder builder;
    Index index;

    //force rebuild
    indexFile.delete();
    assertFalse(indexFile.exists());
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.ALWAYS);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(1, builder.saveIndexCount);
    assertNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //force rebuild (repeat now that file exists)
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.ALWAYS);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(1, builder.saveIndexCount);
    assertNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());
  }

  @Test
  public void testScanFolderWithRebuildStrategyIfMissing() {
    URL scoutXml = getClass().getResource("/" + JandexInventoryBuilder.SCOUT_XML_PATH);
    URI indexUri = new JandexInventoryBuilder(RebuildStrategy.ALWAYS).findIndexUri(scoutXml);
    File indexFile = new File(indexUri);
    FixtureJandexInventoryBuilder builder;
    Index index;

    //file does not exist
    indexFile.delete();
    assertFalse(indexFile.exists());
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MISSING);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(1, builder.saveIndexCount);
    assertNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //file exists
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MISSING);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(0, builder.saveIndexCount);
    assertNotNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());
  }

  @Test
  public void testScanFolderWithRebuildStrategyIfModified() throws URISyntaxException {
    URL scoutXml = getClass().getResource("/" + JandexInventoryBuilder.SCOUT_XML_PATH);
    URI indexUri = new JandexInventoryBuilder(RebuildStrategy.ALWAYS).findIndexUri(scoutXml);
    File indexFile = new File(indexUri);
    Class<?> aClass = this.getClass();
    File aClassFile = new File(
        new File(aClass.getProtectionDomain().getCodeSource().getLocation().toURI()),
        aClass.getPackage().getName().replace(".", "/") + "/" + aClass.getSimpleName() + ".class");
    FixtureJandexInventoryBuilder builder;
    Index index;

    //index file does not exist
    indexFile.delete();
    assertFalse(indexFile.exists());
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MODIFIED);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(1, builder.saveIndexCount);
    assertNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //index file exists and is recent
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MODIFIED);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(0, builder.saveIndexCount);
    assertNotNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //index file exists and is newer than content (-2 minutes)
    aClassFile.setLastModified(indexFile.lastModified() - 120000L);
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MODIFIED);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(0, builder.saveIndexCount);
    assertNotNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //index file exists and is older that content (2 minutes)
    aClassFile.setLastModified(indexFile.lastModified() + 120000L);
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MODIFIED);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(1, builder.saveIndexCount);
    assertNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //index file exists and a class was deleted
    // we simulate the deleted class by excluding it in the directory scan
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MODIFIED)
        .withClassFixtureJandexInventoryObjectExcluded();
    index = builder.scanModule(indexUri);
    assertEquals(2, builder.readIndexCount);
    assertEquals(1, builder.saveIndexCount);
    assertNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());
  }

  private static class FixtureJandexInventoryBuilder extends JandexInventoryBuilder {
    int readIndexCount;
    Index readIndexReturn;
    int saveIndexCount;

    private boolean m_fixtureClassExcluded;

    public FixtureJandexInventoryBuilder(RebuildStrategy rebuildStrategy) {
      super(rebuildStrategy);
    }

    public FixtureJandexInventoryBuilder withClassFixtureJandexInventoryObjectExcluded() {
      m_fixtureClassExcluded = true;
      return this;
    }

    @Override
    protected Index readIndex(URI indexUri) {
      readIndexCount++;
      readIndexReturn = super.readIndex(indexUri);
      return readIndexReturn;
    }

    @Override
    protected void saveIndex(File file, Index index) {
      saveIndexCount++;
      super.saveIndex(file, index);
    }

    @Override
    protected boolean acceptPathForIndex(Path path) {
      if (m_fixtureClassExcluded && path.endsWith("JandexInventoryBuilderTest$FixtureJandexInventoryObject.class")) {
        return false;
      }
      return true;
    }
  }

  public static class FixtureJandexInventoryObject {
  }
}
