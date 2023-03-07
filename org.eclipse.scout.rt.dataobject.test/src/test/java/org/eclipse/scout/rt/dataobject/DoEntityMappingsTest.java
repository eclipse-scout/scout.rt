/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.rt.dataobject.fixture.EntityMapperContributionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.EntityMapperFixture;
import org.eclipse.scout.rt.dataobject.fixture.EntityMapperFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.EntityMapperSubPeerFixture;
import org.eclipse.scout.rt.dataobject.fixture.OtherEntityMapperFixtureDo;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.dataobject.mapping.IDoEntityMapping;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class DoEntityMappingsTest {

  /**
   * Test for {@link DoEntityMappings#with(IDoEntityMapping)}
   */
  @Test
  public void testWithIDoEntityMapping() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.with(new IDoEntityMapping<>() {

      @Override
      public void toDo(EntityMapperFixture source, EntityMapperFixtureDo dataObject) {
        dataObject.withId(source.getId());
      }

      @Override
      public void fromDo(EntityMapperFixtureDo dataObject, EntityMapperFixture target) {
        if (dataObject.id().exists()) {
          target.setId(dataObject.getId());
        }
      }
    });

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class).withId("test");
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setId("test2");

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  /**
   * Test for {@link DoEntityMappings#with(BiConsumer, BiConsumer)}
   */
  @Test
  public void testWithConsumers() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.with((source, doValue) -> doValue.withId(source.getId()), (doValue, target) -> target.setId(doValue.getId()));

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class).withId("test");
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setId("test2");

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  /**
   * Test for {@link DoEntityMappings#with(Function, Function)}
   */
  @Test
  public void testWithGetterOnly() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.with(EntityMapperFixtureDo::id, EntityMapperFixture::getId);

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class);
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setId("test");

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  /**
   * Test for {@link DoEntityMappings#with(Function, Function, BiConsumer)}
   */
  @Test
  public void testWithGetterAndSetter() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.with(EntityMapperFixtureDo::id, EntityMapperFixture::getId, EntityMapperFixture::setId);

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class).withId("test");
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setId("test2");

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  /**
   * Test for {@link DoEntityMappings#withHolder(Function, Function)}
   */
  @Test
  public void testWithHolder() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.withHolder(EntityMapperFixtureDo::id, EntityMapperFixture::getIdHolder);

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class).withId("test");
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setId("test2");

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  @Test
  public void testWithDoCollection() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.withDoCollection(EntityMapperFixtureDo::stringCollection, EntityMapperFixture::getStringCollection, EntityMapperFixture::setStringCollection);
    mappings.withDoCollection(EntityMapperFixtureDo::entityCollection, EntityMapperFixture::getEntityCollection, EntityMapperFixture::setEntityCollection);
    mappings.withDoCollection(EntityMapperFixtureDo::entityList, EntityMapperFixture::getEntityList, EntityMapperFixture::setEntityList);
    mappings.withDoCollection(EntityMapperFixtureDo::entitySet, EntityMapperFixture::getEntitySet, EntityMapperFixture::setEntitySet);

    Collection<String> stringCollection = List.of("string.one", "string.two", "string.three");
    Collection<OtherEntityMapperFixtureDo> collection =
        List.of(BEANS.get(OtherEntityMapperFixtureDo.class).withId("col.one"), BEANS.get(OtherEntityMapperFixtureDo.class).withId("col.two"), BEANS.get(OtherEntityMapperFixtureDo.class).withId("col.three"));
    List<OtherEntityMapperFixtureDo> list =
        List.of(BEANS.get(OtherEntityMapperFixtureDo.class).withId("list.one"), BEANS.get(OtherEntityMapperFixtureDo.class).withId("list.two"), BEANS.get(OtherEntityMapperFixtureDo.class).withId("list.three"));
    Set<OtherEntityMapperFixtureDo> set = Set.of(BEANS.get(OtherEntityMapperFixtureDo.class).withId("set.one"), BEANS.get(OtherEntityMapperFixtureDo.class).withId("set.two"), BEANS.get(OtherEntityMapperFixtureDo.class).withId("set.three"));

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class)
        .withStringCollection(stringCollection)
        .withEntityCollection(collection)
        .withEntityList(list)
        .withEntitySet(set);

    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setStringCollection(DoCollection.of(stringCollection));
    source.setEntityCollection(DoCollection.of(collection));
    source.setEntityList(DoList.of(list));
    source.setEntitySet(DoSet.of(set));

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  /**
   * Test for {@link DoEntityMappings#withChildMapping(Function, Class, Function, Consumer)}
   */
  @Test
  public void testWithChildMappingInitConsumer() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.withChildMapping(EntityMapperFixtureDo::otherEntity, OtherEntityMapperFixtureDo.class, Function.identity(), this::initChildMappings);

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class)
        .withOtherEntity(BEANS.get(OtherEntityMapperFixtureDo.class).withId("test"));
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setOtherId("test2");

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  protected void initChildMappings(DoEntityMappings<OtherEntityMapperFixtureDo, EntityMapperFixture> mappings) {
    mappings.with(OtherEntityMapperFixtureDo::id, EntityMapperFixture::getOtherId, EntityMapperFixture::setOtherId);
  }

  /**
   * Test for {@link DoEntityMappings#withChildMapping(Function, Class, Function, BiConsumer, BiConsumer)}
   */
  @Test
  public void testWithChildMappingFromAndToDoConsumers() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.withChildMapping(EntityMapperFixtureDo::otherEntity, OtherEntityMapperFixtureDo.class, Function.identity(),
        (source, targetDo) -> targetDo.withId(source.getOtherId()),
        (target, sourceDo) -> target.setOtherId(sourceDo.getId()));

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class)
        .withOtherEntity(BEANS.get(OtherEntityMapperFixtureDo.class).withId("test"));
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setOtherId("test2");

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  /**
   * Test for {@link DoEntityMappings#withContribution(Class, Consumer)}
   */
  @Test
  public void testWithContributionDoEntityMappings() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.withContribution(EntityMapperContributionFixtureDo.class, this::initContributionMappings);

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class);
    sourceDo.putContribution(BEANS.get(EntityMapperContributionFixtureDo.class).withContributedValue(2L));
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setContributedValue(3L);

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  /**
   * Test for {@link DoEntityMappings#withContribution(Class, Predicate, Consumer)}
   */
  @Test
  public void testWithContributionPredicate() {
    Holder<Boolean> addContributionToggle = new Holder<>(false);

    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.withContribution(EntityMapperContributionFixtureDo.class, (contributionDo) -> addContributionToggle.getValue(), this::initContributionMappings);

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class);
    sourceDo.putContribution(BEANS.get(EntityMapperContributionFixtureDo.class).withContributedValue(2L));
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.setContributedValue(3L);

    EntityMapperFixtureDo target = BEANS.get(EntityMapperFixtureDo.class);
    mappings.toDo(source, target);
    assertFalse(target.hasContributions());

    addContributionToggle.setValue(true);

    target = BEANS.get(EntityMapperFixtureDo.class);
    mappings.toDo(source, target);
    assertTrue(target.hasContributions());

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  protected void initContributionMappings(DoEntityMappings<EntityMapperContributionFixtureDo, EntityMapperFixture> contributionMappings) {
    contributionMappings.with(EntityMapperContributionFixtureDo::contributedValue, EntityMapperFixture::getContributedValue, EntityMapperFixture::setContributedValue);
  }

  /**
   * Test for {@link DoEntityMappings#withContribution(Class, Class, Consumer)}
   */
  @Test
  public void testWithContributionSubPeer() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.withContribution(EntityMapperContributionFixtureDo.class, EntityMapperSubPeerFixture.class, this::initSubPeerContributionMappings);

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class);
    sourceDo.putContribution(BEANS.get(EntityMapperContributionFixtureDo.class).withContributedSubPeerValue(2L));
    EntityMapperSubPeerFixture source = BEANS.get(EntityMapperSubPeerFixture.class);
    source.setSubPeerValue(3L);

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  /**
   * Test for {@link DoEntityMappings#withContribution(Class, Class, Predicate, Consumer)}
   */
  @Test
  public void testWithContributionSubPeerPredicate() {
    Holder<Boolean> addContributionToggle = new Holder<>(false);

    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.withContribution(EntityMapperContributionFixtureDo.class, EntityMapperSubPeerFixture.class, (contributionDo) -> addContributionToggle.getValue(), this::initSubPeerContributionMappings);

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class);
    sourceDo.putContribution(BEANS.get(EntityMapperContributionFixtureDo.class).withContributedSubPeerValue(2L));
    EntityMapperSubPeerFixture source = BEANS.get(EntityMapperSubPeerFixture.class);
    source.setSubPeerValue(3L);

    EntityMapperFixtureDo target = BEANS.get(EntityMapperFixtureDo.class);
    mappings.toDo(source, target);
    assertFalse(target.hasContributions());

    addContributionToggle.setValue(true);

    target = BEANS.get(EntityMapperFixtureDo.class);
    mappings.toDo(source, target);
    assertTrue(target.hasContributions());

    applyAndAssertMappings(mappings, sourceDo, source);
  }

  protected void initSubPeerContributionMappings(DoEntityMappings<EntityMapperContributionFixtureDo, EntityMapperSubPeerFixture> contributionMappings) {
    contributionMappings.with(EntityMapperContributionFixtureDo::contributedSubPeerValue, EntityMapperSubPeerFixture::getSubPeerValue, EntityMapperSubPeerFixture::setSubPeerValue);
  }

  /**
   * Test for {@link DoEntityMappings#toPrimitiveBoolean(BiConsumer)}
   */
  @Test
  public void testToPrimitiveBoolean() {
    BooleanHolder booleanHolder = new BooleanHolder(false);
    BiConsumer<Object, Boolean> consumer = DoEntityMappings.toPrimitiveBoolean((test, value) -> {
      assertNotNull(value); // expecting false as result from converting a null Boolean to boolean
      booleanHolder.setValue(true);
    });

    consumer.accept(this, null);
    assertTrue(booleanHolder.getValue());
  }

  @Test
  public void testNonExistingNodeKeepsEntityValueUnchanged() {
    DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings = new DoEntityMappings<>();
    mappings.with(EntityMapperFixtureDo::id, EntityMapperFixture::getId, EntityMapperFixture::setId);
    mappings.withHolder(EntityMapperFixtureDo::id, EntityMapperFixture::getIdHolder);

    EntityMapperFixtureDo sourceDo = BEANS.get(EntityMapperFixtureDo.class);
    EntityMapperFixture target = BEANS.get(EntityMapperFixture.class);
    target.setId("test");

    mappings.fromDo(sourceDo, target);
    assertEquals(target.getId(), "test");
  }

  protected void applyAndAssertMappings(DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings, EntityMapperFixtureDo sourceDo, EntityMapperFixture source) {
    EntityMapperFixture target = source instanceof EntityMapperSubPeerFixture ? BEANS.get(EntityMapperSubPeerFixture.class) : BEANS.get(EntityMapperFixture.class);
    mappings.fromDo(sourceDo, target);
    assertMappings(sourceDo, target);

    EntityMapperFixtureDo targetDo = BEANS.get(EntityMapperFixtureDo.class);
    mappings.toDo(source, targetDo);
    assertMappings(targetDo, source);
  }

  protected void assertMappings(EntityMapperFixtureDo doValue, EntityMapperFixture value) {
    assertEquals(doValue.getId(), value.getId());
    if (doValue.getOtherEntity() == null) {
      assertNull(value.getOtherId());
    }
    else {
      assertEquals(doValue.getOtherEntity().getId(), value.getOtherId());
    }

    if (doValue.hasContribution(EntityMapperContributionFixtureDo.class)) {
      assertEquals(doValue.getContribution(EntityMapperContributionFixtureDo.class).getContributedValue(), value.getContributedValue());
    }
    else {
      assertNull(value.getContributedValue());
    }

    if (value instanceof EntityMapperSubPeerFixture) {
      assertTrue(doValue.hasContribution(EntityMapperContributionFixtureDo.class));
      assertEquals(doValue.getContribution(EntityMapperContributionFixtureDo.class).getContributedSubPeerValue(), ((EntityMapperSubPeerFixture) value).getSubPeerValue());
    }

    if (doValue.entityCollection().exists()) {
      assertEquals(doValue.getEntityCollection(), value.getEntityCollection() == null ? null : value.getEntityCollection().get());
    }
    else {
      assertNull(value.getEntityCollection());
    }

    if (doValue.entityList().exists()) {
      assertEquals(doValue.getEntityList(), value.getEntityList() == null ? null : value.getEntityList().get());
    }
    else {
      assertNull(value.getEntityList());
    }

    if (doValue.entitySet().exists()) {
      assertEquals(doValue.getEntitySet(), value.getEntitySet() == null ? null : value.getEntitySet().get());
    }
    else {
      assertNull(value.getEntitySet());
    }
  }
}
