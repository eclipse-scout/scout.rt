/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.search;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.scout.rt.client.services.common.search.DataModelDefaultSearchFilterServiceTest.MyComposerField.CarEntity;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerDisplayTextBuilder;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.data.model.DataModelAttributeOp;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link DefaultSearchFilterService}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DataModelDefaultSearchFilterServiceTest {

  private static final String LABEL = "Label";

  private ISearchFilterService m_searchFilterService;
  private SearchFilter m_searchFilter;

  @Before
  public void setUp() {
    m_searchFilterService = BEANS.get(ISearchFilterService.class);
    m_searchFilter = new SearchFilter();
  }

  @Test
  public void testComposerField() {
    MyComposerField composer = new MyComposerField();
    composer.init();
    runBasicAsserts(composer);

    CarEntity carEntity = composer.new CarEntity();
    ITreeNode carNode = composer.addEntityNode(
        composer.getTree().getRootNode(),
        carEntity,
        true,
        Collections.emptyList(),
        new ArrayList<>());

    composer.addAttributeNode(carNode,
        carEntity.new ColorAttribute(),
        DataModelConstants.AGGREGATION_NONE,
        DataModelAttributeOp.create(DataModelConstants.OPERATOR_EQ),
        CollectionUtility.arrayList("blue key"),
        CollectionUtility.arrayList("blue value"));
    m_searchFilterService.applySearchDelegate(composer, m_searchFilter, false);
    StringBuilder result = new StringBuilder();
    new ComposerDisplayTextBuilder().build(composer.getTree().getRootNode(), result, "");
    Assert.assertEquals(result.toString().trim(), m_searchFilter.getDisplayTextsPlain());
  }

  public void runBasicAsserts(IFormField f) {
    m_searchFilterService.applySearchDelegate(f, m_searchFilter, false);
    Assert.assertEquals("", m_searchFilter.getDisplayTextsPlain());
    f.setLabel(LABEL);
    m_searchFilterService.applySearchDelegate(f, m_searchFilter, false);
    Assert.assertEquals("", m_searchFilter.getDisplayTextsPlain());
  }

  public class MyComposerField extends AbstractComposerField {
    @Order(80)
    public class CarEntity extends AbstractDataModelEntity {
      private static final long serialVersionUID = 1L;

      @Override
      public String getConfiguredText() {
        return "Auto";
      }

      @Order(10)
      public class ColorAttribute extends AbstractDataModelAttribute {
        private static final long serialVersionUID = 1L;

        @Override
        public String getConfiguredText() {
          return "Color";
        }

        @Override
        public int getConfiguredType() {
          return IDataModelAttribute.TYPE_STRING;
        }
      }
    }
  }
}
