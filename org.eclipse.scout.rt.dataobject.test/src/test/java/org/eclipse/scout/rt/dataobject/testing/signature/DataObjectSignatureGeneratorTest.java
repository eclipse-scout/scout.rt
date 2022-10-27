/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.testing.signature;

import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * Test cases for {@link DataObjectSignatureGenerator} and {@link DataObjectSignatureComparator}.
 */
public class DataObjectSignatureGeneratorTest {

  @Test
  public void testCreateSignature() {
    DataObjectSignatureDo signature = BEANS.get(DataObjectSignatureGenerator.class).createSignature(CollectionUtility.hashSet(getPackageNamePrefix()), c -> true, (c, s) -> true);
    DataObjectSignatureDo expected = BEANS.get(DataObjectSignatureDo.class)
        .withEntities(
            BEANS.get(EntityDataObjectSignatureDo.class)
                .withTypeName("dataObjectFixture.SignatureFixture")
                .withTypeVersion("dataObjectFixture-1.0.0")
                .withAttributes(
                    BEANS.get(AttributeDataObjectSignatureDo.class)
                        .withName("enumAttribute")
                        .withValueType("ENUM[scout.FixtureEnum]")
                        .withList(false),
                    BEANS.get(AttributeDataObjectSignatureDo.class)
                        .withName("idAttribute")
                        .withValueType("ID[scout.FixtureStringId]")
                        .withList(true),
                    BEANS.get(AttributeDataObjectSignatureDo.class)
                        .withName("abstractIdAttribute")
                        .withValueType("IDI[org.eclipse.scout.rt.dataobject.id.AbstractStringId]")
                        .withList(true),
                    BEANS.get(AttributeDataObjectSignatureDo.class)
                        .withName("doAttribute")
                        .withValueType("DO[dataObjectFixture.SignatureSubFixture]")
                        .withList(false),
                    BEANS.get(AttributeDataObjectSignatureDo.class)
                        .withName("doInterfaceAttribute")
                        .withValueType("DOI[org.eclipse.scout.rt.dataobject.testing.signature.fixture.ISignatureFixtureAttributeDo]")
                        .withList(false)),
            BEANS.get(EntityDataObjectSignatureDo.class)
                .withTypeName("dataObjectFixture.SignatureSubFixture")
                .withTypeVersion("dataObjectFixture-1.0.0.034")
                .withAttributes(
                    BEANS.get(AttributeDataObjectSignatureDo.class)
                        .withName("text")
                        .withValueType("CLASS[java.lang.String]")
                        .withList(false)))
        .withEnums(
            BEANS.get(EnumApiSignatureDo.class)
                .withEnumName("scout.FixtureEnum")
                .withValues("one", "two", "three"));

    DataObjectSignatureComparator comp = BEANS.get(DataObjectSignatureComparator.class);
    comp.compare(expected, signature);
    assertTrue("Did not expect differences, got " + comp.getDifferences(), comp.getDifferences().isEmpty());
  }

  /**
   * Only data object with matching package name prefixes are added to signature.
   */
  protected String getPackageNamePrefix() {
    return "org.eclipse.scout.rt.dataobject.testing.signature.fixture";
  }
}
