/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.dataobject.migration;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.fixture.FirstSimpleContributionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.SecondSimpleContributionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.SimpleFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrator.DataObjectMigratorResult;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_2;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests {@link AbstractRemoveDoEntityContributionValueMigrationHandler}
 */
public class AbstractRemoveDoEntityContributionValueMigrationHandlerTest {

  protected static IDataObjectMapper s_dataObjectMapper;

  @BeforeClass
  public static void beforeClass() {
    s_dataObjectMapper = BEANS.get(IDataObjectMapper.class);
  }

  /**
   * Tests that data object is not changed if there is no contribution.
   */
  @Test
  public void testMigrationOfDoEntityWithoutContribution() {
    SimpleFixtureDo fixture = createSimpleFixtureDo();

    String fixtureJson = s_dataObjectMapper.writeValue(fixture);
    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class).putGlobal(BEANS.get(DoValueMigrationIdsContextData.class).withAppliedValueMigrationIds(getAppliedValueMigrations()));
    DataObjectMigratorResult<SimpleFixtureDo> result = BEANS.get(DataObjectMigrator.class).migrateDataObject(ctx, fixtureJson, SimpleFixtureDo.class);
    assertFalse(result.isChanged());
    assertEquals(fixture, result.getDataObject());
  }

  /**
   * Tests that data object is not changed if there is no matching contribution.
   */
  @Test
  public void testMigrationOfDoEntityWithDifferentContribution() {
    SimpleFixtureDo fixture = createSimpleFixtureDo();
    fixture.contribution(SecondSimpleContributionFixtureDo.class)
        .withSecondValue("mySecondValue");

    String fixtureJson = s_dataObjectMapper.writeValue(fixture);
    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class).putGlobal(BEANS.get(DoValueMigrationIdsContextData.class).withAppliedValueMigrationIds(getAppliedValueMigrations()));
    DataObjectMigratorResult<SimpleFixtureDo> result = BEANS.get(DataObjectMigrator.class).migrateDataObject(ctx, fixtureJson, SimpleFixtureDo.class);
    assertFalse(result.isChanged());
    assertEquals(fixture, result.getDataObject());
  }

  /**
   * Tests that the DO is not changed because the contribution to be removed still exists in the code.
   */
  @Test
  public void testMigrationOfDoWithMatchingAndStillExistingContribution() {
    SimpleFixtureDo dataObject = createDataObjectWithExistingContribution();

    String json = s_dataObjectMapper.writeValue(dataObject);
    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class).putGlobal(BEANS.get(DoValueMigrationIdsContextData.class).withAppliedValueMigrationIds(getAppliedValueMigrations()));
    DataObjectMigratorResult<SimpleFixtureDo> result = BEANS.get(DataObjectMigrator.class).migrateDataObject(ctx, json, SimpleFixtureDo.class);
    assertFalse(result.isChanged());
    assertEquals(dataObject, result.getDataObject());
  }

  /**
   * Tests that the contribution is removed from the data object.
   */
  @Test
  public void testMigrationOfDoWithMatchingAndNonExistingContribution() {
    testMigration(createDataObjectWithUnknownContribution(), createSimpleFixtureDo());
  }

  /**
   * Combination of multiple contributions.
   */
  @Test
  public void testMigrationOfDoWithMultipleContributions() {
    // Existing contribution 1 - should not be removed because the contribution still exists in the code
    IDoEntity expectedExistingContribution1 = BEANS.get(DoEntity.class);
    expectedExistingContribution1.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.FirstSimpleContributionFixture");
    expectedExistingContribution1.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_1.VERSION.unwrap());
    expectedExistingContribution1.put("firstValue", "myFirstValue");

    // Existing contribution 2 - no migration handler
    IDoEntity expectedExistingContribution2 = BEANS.get(DoEntity.class);
    expectedExistingContribution2.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.SecondSimpleContributionFixture");
    expectedExistingContribution2.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_1.VERSION.unwrap());
    expectedExistingContribution2.put("secondValue", "mySecondValue");

    // Unknown contribution 1 - should not be removed because of different type version
    IDoEntity expectedUnknownContribution1 = BEANS.get(DoEntity.class);
    expectedUnknownContribution1.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.MyTestContribution");
    expectedUnknownContribution1.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_2.VERSION.unwrap());
    expectedUnknownContribution1.put("testValue", "myTestValue");

    // Unknown contribution 2 - no migration handler
    IDoEntity expectedUnknownContribution2 = BEANS.get(DoEntity.class);
    expectedUnknownContribution2.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.MyOtherTestContribution");
    expectedUnknownContribution2.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_1.VERSION.unwrap());
    expectedUnknownContribution2.put("otherTestValue", "myOtherTestValue");

    // build the DO manually, because we want to add unknown/invalid contributions
    IDoEntity expectedDataObject = BEANS.get(DoEntity.class);
    expectedDataObject.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.SimpleFixture");
    expectedDataObject.put("name1", "myName1");
    expectedDataObject.putList("_contributions", CollectionUtility.arrayList(expectedExistingContribution1, expectedExistingContribution2, expectedUnknownContribution1, expectedUnknownContribution2));

    // serialize and deserialize the DO, so that we get a SimpleFixtureDo instead of a DoEntity
    IDoEntity expected = s_dataObjectMapper.readValue(s_dataObjectMapper.writeValue(expectedDataObject), IDoEntity.class);

    testMigration(createDataObjectWithMultipleContributions(), expected);
  }

  protected void testMigration(IDoEntity value, IDoEntity expected) {
    String json = s_dataObjectMapper.writeValue(value);
    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class).putGlobal(BEANS.get(DoValueMigrationIdsContextData.class).withAppliedValueMigrationIds(getAppliedValueMigrations()));
    DataObjectMigratorResult<IDoEntity> result = BEANS.get(DataObjectMigrator.class).migrateDataObject(ctx, json, IDoEntity.class);
    assertTrue("Data object migration didn't change the data object", result.isChanged());
    assertEquals(expected, result.getDataObject());
  }

  /**
   * @return the set of all available value migration IDs, except the migrations to be tested. This is to make sure that no other value
   * migrations than the expected ones will be applied.
   */
  protected Set<DoValueMigrationId> getAppliedValueMigrations() {
    return BEANS.get(DataObjectMigrationInventory.class).getValueMigrationHandlers().stream()
        .map(IDoValueMigrationHandler::id)
        .filter(id -> !MyTestContributionRemoveDoEntityContributionValueMigrationHandler.ID.equals(id))
        .filter(id -> !FirstSimpleContributionFixtureRemoveDoEntityContributionValueMigrationHandler.ID.equals(id))
        .collect(Collectors.toSet());
  }

  protected SimpleFixtureDo createSimpleFixtureDo() {
    return BEANS.get(SimpleFixtureDo.class)
        .withName1("myName1");
  }

  protected SimpleFixtureDo createDataObjectWithExistingContribution() {
    SimpleFixtureDo fixture = createSimpleFixtureDo();
    fixture.contribution(FirstSimpleContributionFixtureDo.class)
        .withFirstValue("myFirstValue");
    return fixture;
  }

  protected IDoEntity createDataObjectWithUnknownContribution() {
    // Unknown contribution
    IDoEntity contribution = BEANS.get(DoEntity.class);
    contribution.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.MyTestContribution");
    contribution.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_1.VERSION.unwrap());
    contribution.put("testValue", "myTestValue");

    // build the DO manually, because we want to add unknown/invalid contributions
    IDoEntity dataObject = BEANS.get(DoEntity.class);
    dataObject.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.SimpleFixture");
    dataObject.put("name1", "myName1");
    dataObject.putList("_contributions", Collections.singletonList(contribution));
    return dataObject;
  }

  protected IDoEntity createDataObjectWithMultipleContributions() {
    // Existing contribution
    IDoEntity simpleContribution1 = BEANS.get(DoEntity.class);
    simpleContribution1.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.FirstSimpleContributionFixture");
    simpleContribution1.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_1.VERSION.unwrap());
    simpleContribution1.put("firstValue", "myFirstValue");

    // Existing contribution
    IDoEntity simpleContribution2 = BEANS.get(DoEntity.class);
    simpleContribution2.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.SecondSimpleContributionFixture");
    simpleContribution2.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_1.VERSION.unwrap());
    simpleContribution2.put("secondValue", "mySecondValue");

    // Unknown contribution 1
    IDoEntity nonExistingContribution1 = BEANS.get(DoEntity.class);
    nonExistingContribution1.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.MyTestContribution");
    nonExistingContribution1.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_1.VERSION.unwrap());
    nonExistingContribution1.put("testValue", "myTestValue");

    IDoEntity nonExistingContribution2 = BEANS.get(DoEntity.class);
    nonExistingContribution2.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.MyTestContribution");
    nonExistingContribution2.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_2.VERSION.unwrap());
    nonExistingContribution2.put("testValue", "myTestValue");

    // Unknown contribution 3
    IDoEntity nonExistingContribution3 = BEANS.get(DoEntity.class);
    nonExistingContribution3.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.MyOtherTestContribution");
    nonExistingContribution3.put(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, AlfaFixture_1.VERSION.unwrap());
    nonExistingContribution3.put("otherTestValue", "myOtherTestValue");

    // build the DO manually, because we want to add unknown/invalid contributions
    SimpleFixtureDo dataObject = BEANS.get(SimpleFixtureDo.class);
    dataObject.put(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, "scout.SimpleFixture");
    dataObject.put("name1", "myName1");
    dataObject.putList("_contributions", CollectionUtility.arrayList(simpleContribution1, simpleContribution2, nonExistingContribution1, nonExistingContribution2, nonExistingContribution3));

    BEANS.get(IDataObjectMapper.class).readValue(BEANS.get(IDataObjectMapper.class).writeValue(dataObject), IDoEntity.class);

    return dataObject;
  }

  /**
   * Migration handler that removes an unknown contribution
   */
  public static class MyTestContributionRemoveDoEntityContributionValueMigrationHandler extends AbstractRemoveDoEntityContributionValueMigrationHandler<SimpleFixtureDo> {

    public static final DoValueMigrationId ID = DoValueMigrationId.of("44d6e9bb-210d-47b3-bf75-8fc28a314361");

    @Override
    public DoValueMigrationId id() {
      return ID;
    }

    @Override
    public Class<? extends ITypeVersion> typeVersionClass() {
      return AlfaFixture_1.class;
    }

    @Override
    protected String getContributionTypeName() {
      return "scout.MyTestContribution";
    }

    @Override
    protected Class<? extends ITypeVersion> getContributionTypeVersionClass() {
      return AlfaFixture_1.class;
    }
  }

  /**
   * Migration handler that tries to remove the contribution {@link FirstSimpleContributionFixtureDo} that still exists in the code -> migration will not do anything
   */
  public static class FirstSimpleContributionFixtureRemoveDoEntityContributionValueMigrationHandler extends AbstractRemoveDoEntityContributionValueMigrationHandler<SimpleFixtureDo> {

    public static final DoValueMigrationId ID = DoValueMigrationId.of("1f386486-203c-4a6e-ba3d-9186b5766c71");

    @Override
    public DoValueMigrationId id() {
      return ID;
    }

    @Override
    public Class<? extends ITypeVersion> typeVersionClass() {
      return AlfaFixture_1.class;
    }

    @Override
    protected String getContributionTypeName() {
      return "scout.FirstSimpleContributionFixture";
    }

    @Override
    protected Class<? extends ITypeVersion> getContributionTypeVersionClass() {
      return AlfaFixture_1.class;
    }
  }
}
