/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertEqualsWithComparisonFailure;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.scout.rt.dataobject.DataObjectVisitors;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.testing.TestingResourceHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.uuid.IUuidProvider;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract test implementation for a single {@link IDoStructureMigrationHandler}.
 */
public abstract class AbstractDoStructureMigrationHandlerTest {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractDoStructureMigrationHandlerTest.class);

  private static IBean<?> s_uuidProvider;

  @BeforeClass
  public static void beforeClass() {
    // Use a constant uuid provider in order to receive the same tests results on each run
    s_uuidProvider = BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(IUuidProvider.class, new ConstantUuidProvider()));
  }

  @AfterClass
  public static void afterClass() {
    BEANS.get(BeanTestingHelper.class).unregisterBean(s_uuidProvider);
  }

  protected Class<?> getResourceBaseClass() {
    return this.getClass();
  }

  /**
   * @param filenamePrefix
   *          Filename prefix of file in resource folder
   * @param fromVersionClass
   *          From version is only required to determine to file to load that contains the previous version (source)
   * @param toVersionClass
   *          To version is required to determine the file to load that contains the expected data object (target)
   */
  public void testMigration(String filenamePrefix, Class<? extends ITypeVersion> fromVersionClass, Class<? extends ITypeVersion> toVersionClass) throws IOException {
    testMigration(filenamePrefix, BEANS.get(fromVersionClass).getVersion().unwrap(), toVersionClass);
  }

  public void testMigration(String filenamePrefix, String fromVersionText, Class<? extends ITypeVersion> toVersionClass) throws IOException {
    NamespaceVersion toVersion = BEANS.get(toVersionClass).getVersion();
    IPrettyPrintDataObjectMapper dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);

    IDoEntity actual; // 'actual' because loaded from fromVersion and then migrated -> represents actual data object
    try (InputStream in = getClass().getResourceAsStream(getFilename(filenamePrefix, fromVersionText))) {
      assertNotNull("File " + getFilename(filenamePrefix, fromVersionText) + " is missing", in);
      actual = (IDoEntity) dataObjectMapper.readValueRaw(in);
    }

    DoStructureMigrationContext ctx = BEANS.get(DoStructureMigrationContext.class);
    boolean changed = BEANS.get(DoStructureMigrator.class).migrateDataObject(ctx, actual, toVersion);

    assertTrue("Data object was not changed by migration", changed);

    IDoEntity expected = null; // from toVersion -> expected data object (according to .json file)
    String filename = getFilename(filenamePrefix, toVersion.unwrap());
    File referenceFile = new File(BEANS.get(TestingResourceHelper.class).getTestResourceDirectory(getResourceBaseClass()), filename);
    if (referenceFile.exists()) {
      try (FileInputStream fis = new FileInputStream(referenceFile)) {
        expected = (IDoEntity) dataObjectMapper.readValueRaw(fis);
      }
    }

    if (!ObjectUtility.equals(actual, expected)) {
      DataObjectVisitors.forEach(actual, IDoEntity.class, e -> assertEquals("Always use raw DoEntity class in migration - use readRaw, cloneRaw, etc.", DoEntity.class, e.getClass()));

      BEANS.get(TestingResourceHelper.class).writeTestResource(getResourceBaseClass(), filename, actual);

      // Do not use pretty print output for logging. Single line log entry is useful for failing jenkins tests.
      LOG.warn("Expected content for {}: {}", referenceFile.getName(), BEANS.get(IDataObjectMapper.class).writeValue(actual));
      assertEqualsWithComparisonFailure("Expected DO entity structure differs from actual content. Verify the changes in the result JSON file and commit the changed file.", expected, actual);
      fail("Expected DO entity structure differs from actual content. Verify the changes in the result JSON file and commit the changed file.");
    }
  }

  protected String getFilename(String filenamePrefix, String typeVersion) {
    return filenamePrefix + "-" + typeVersion + ".json";
  }
}
