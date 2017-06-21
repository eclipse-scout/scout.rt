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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.inventory.internal.JandexInventoryBuilder.RebuildStrategy;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.SleepUtil;
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
    assertEquals(0, builder.readIndexCount);
    assertEquals(1, builder.writeIndexCount);
    assertNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //force rebuild (repeat now that file exists)
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.ALWAYS);
    index = builder.scanModule(indexUri);
    assertEquals(0, builder.readIndexCount);
    assertEquals(1, builder.writeIndexCount);
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
    assertEquals(0, builder.readIndexCount);
    assertEquals(1, builder.writeIndexCount);
    assertNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //file exists
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MISSING);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(0, builder.writeIndexCount);
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
    assertEquals(0, builder.readIndexCount);
    assertEquals(1, builder.writeIndexCount);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //index file exists and is recent
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MODIFIED);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(0, builder.writeIndexCount);
    assertNotNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //index file exists and is newer than content (-2 minutes)
    aClassFile.setLastModified(indexFile.lastModified() - 120000L);
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MODIFIED);
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(0, builder.writeIndexCount);
    assertNotNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //index file exists and is older that content (2 minutes)
    aClassFile.setLastModified(indexFile.lastModified() + 120000L);
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MODIFIED);
    index = builder.scanModule(indexUri);
    assertEquals(0, builder.readIndexCount);
    assertEquals(1, builder.writeIndexCount);
    assertNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());

    //index file exists and a class was deleted
    // we simulate the deleted class by excluding it in the directory scan
    builder = new FixtureJandexInventoryBuilder(RebuildStrategy.IF_MODIFIED)
        .withClassFixtureJandexInventoryObjectExcluded();
    index = builder.scanModule(indexUri);
    assertEquals(1, builder.readIndexCount);
    assertEquals(1, builder.writeIndexCount);
    assertNotNull(builder.readIndexReturn);
    assertNotNull(index);
    assertTrue(indexFile.exists());
  }

  @Test
  public void testFileLockLastModified() throws URISyntaxException, IOException {
    URL scoutXml = getClass().getResource("/" + JandexInventoryBuilder.SCOUT_XML_PATH);
    File targetFolder = new File(scoutXml.toURI()).getParentFile().getParentFile();
    File f = new File(targetFolder, "testLock");
    if (f.exists()) {
      assertTrue(f.delete());
    }
    //create new lock file
    long t0, t1, t2, t3, t4;
    try (LockedFile r = new LockedFile(f)) {
      t0 = r.lastModified();
      SleepUtil.sleepSafe(2, TimeUnit.SECONDS);
      nioWrite(r, "Foo");
      t1 = r.lastModified();
      SleepUtil.sleepSafe(2, TimeUnit.SECONDS);
      t2 = r.lastModified();
      r.setLastModified(1234567890000L);
      t3 = r.lastModified();
    }
    t4 = f.lastModified();
    assertNotEquals(t0, t1);
    assertEquals(t1, t2);
    assertNotEquals(t2, t3);
    assertEquals(1234567890000L, t3);
    assertEquals(1234567890000L, t4);

    try (LockedFile r = new LockedFile(f)) {
      t0 = r.lastModified();
    }
    t1 = f.lastModified();
    assertEquals(1234567890000L, t0);
    assertEquals(1234567890000L, t1);
  }

  @Test
  public void testFileLockWithRandomAccessFile() throws URISyntaxException, IOException {
    URL scoutXml = getClass().getResource("/" + JandexInventoryBuilder.SCOUT_XML_PATH);
    File targetFolder = new File(scoutXml.toURI()).getParentFile().getParentFile();
    File f = new File(targetFolder, "testFile");
    if (f.exists()) {
      assertTrue(f.delete());
    }
    //create new lock file
    try (LockedFile r = new LockedFile(f)) {
      nioWrite(r, "Foo");
    }
    //read existing lock file
    try (LockedFile r = new LockedFile(f)) {
      String s = nioRead(r);
      assertEquals(3, s.length());
      assertEquals("Foo", s);
    }
    //modify existing lock file write-read
    try (LockedFile r = new LockedFile(f)) {
      //write
      nioWrite(r, "B");
      //read
      String s = nioRead(r);
      assertEquals(1, s.length());
      assertEquals("B", s);
    }
    //modify existing lock file read-write-read
    try (LockedFile r = new LockedFile(f)) {
      //read
      String s = nioRead(r);
      assertEquals(1, s.length());
      assertEquals("B", s);
      //write
      nioWrite(r, "bar");
      //read
      s = nioRead(r);
      assertEquals(3, s.length());
      assertEquals("bar", s);
    }
    assertTrue(f.delete());
  }

  @Test
  public void testFileLockConcurency() throws URISyntaxException, IOException, InterruptedException {
    URL scoutXml = getClass().getResource("/" + JandexInventoryBuilder.SCOUT_XML_PATH);
    File targetFolder = new File(scoutXml.toURI()).getParentFile().getParentFile();
    final File f = new File(targetFolder, "testFile");
    if (f.exists()) {
      assertTrue(f.delete());
    }
    final CountDownLatch isBeforeLock = new CountDownLatch(2);
    final CountDownLatch waitBeforeLock1 = new CountDownLatch(1);
    final CountDownLatch waitBeforeLock2 = new CountDownLatch(1);
    final CountDownLatch isInsideLock1 = new CountDownLatch(1);
    final CountDownLatch isInsideLock2 = new CountDownLatch(1);
    final CountDownLatch waitInsideLock1 = new CountDownLatch(1);
    final CountDownLatch waitInsideLock2 = new CountDownLatch(1);
    final CountDownLatch isAfterLock1 = new CountDownLatch(1);
    final CountDownLatch isAfterLock2 = new CountDownLatch(1);
    Callable<String> job1 = new Callable<String>() {
      @Override
      public String call() throws Exception {
        isBeforeLock.countDown();
        waitBeforeLock1.await();
        String s = null;
        try (LockedFile r = new LockedFile(f)) {
          isInsideLock1.countDown();
          waitInsideLock1.await();
          //read
          s = nioRead(r);
          //write
          nioWrite(r, "job1");
        }
        isAfterLock1.countDown();
        return s;
      }
    };
    Callable<String> job2 = new Callable<String>() {
      @Override
      public String call() throws Exception {
        isBeforeLock.countDown();
        waitBeforeLock2.await();
        String s = null;
        try (LockedFile r = new LockedFile(f)) {
          isInsideLock2.countDown();
          waitInsideLock2.await();
          //read
          s = nioRead(r);
          //write
          nioWrite(r, "job2");
        }
        isAfterLock2.countDown();
        return s;
      }
    };
    IFuture<String> f1 = Jobs.schedule(job1, Jobs.newInput());
    IFuture<String> f2 = Jobs.schedule(job2, Jobs.newInput());

    assertTrue(isBeforeLock.await(1, TimeUnit.MINUTES));
    waitBeforeLock1.countDown();
    assertTrue(isInsideLock1.await(1, TimeUnit.MINUTES));
    waitBeforeLock2.countDown();
    assertFalse(isInsideLock2.await(3, TimeUnit.SECONDS));

    waitInsideLock1.countDown();
    assertTrue(isAfterLock1.await(1, TimeUnit.MINUTES));

    assertTrue(isInsideLock2.await(1, TimeUnit.MINUTES));
    waitInsideLock2.countDown();
    assertTrue(isAfterLock2.await(1, TimeUnit.MINUTES));

    assertEquals("", f1.awaitDoneAndGet());
    assertEquals("job1", f2.awaitDoneAndGet());
    try (LockedFile r = new LockedFile(f)) {
      assertEquals("job2", nioRead(r));
    }

    assertTrue(f.delete());
  }

  private static void nioWrite(LockedFile r, String s) throws IOException {
    r.newOutputStream().write(s.getBytes());
  }

  private static String nioRead(LockedFile r) throws IOException {
    return new String(IOUtility.readBytes(r.newInputStream()));
  }

  private static class FixtureJandexInventoryBuilder extends JandexInventoryBuilder {
    int readIndexCount;
    Index readIndexReturn;
    int writeIndexCount;

    private boolean m_fixtureClassExcluded;

    public FixtureJandexInventoryBuilder(RebuildStrategy rebuildStrategy) {
      super(rebuildStrategy);
    }

    public FixtureJandexInventoryBuilder withClassFixtureJandexInventoryObjectExcluded() {
      m_fixtureClassExcluded = true;
      return this;
    }

    @Override
    protected Index readIndex(URI indexUri, InputStream in) {
      readIndexCount++;
      readIndexReturn = super.readIndex(indexUri, in);
      return readIndexReturn;
    }

    @Override
    protected void writeIndex(Index index, LockedFile f) {
      writeIndexCount++;
      super.writeIndex(index, f);
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
