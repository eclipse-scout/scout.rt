/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.builder;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.jdbc.builder.FormDataStatementBuilder.AttributeStrategy;
import org.eclipse.scout.rt.server.jdbc.builder.FormDataStatementBuilder.EntityStrategy;
import org.eclipse.scout.rt.server.jdbc.oracle.OracleSqlStyle;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.DataModelUtility;
import org.eclipse.scout.rt.shared.data.model.EntityPath;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.8.1
 */
@RunWith(PlatformTestRunner.class)
public class FormDataStatementBuilderWithComposerTest {

  private FormDataStatementBuilder m_builder;
  private TestDataModel m_dataModel;

  @Before
  public void before() {
    m_builder = new FormDataStatementBuilder(new OracleSqlStyle());
    // set data model
    m_dataModel = new TestDataModel();
    m_dataModel.init();
    m_builder.setDataModel(m_dataModel);
    // add data model mappings
    m_builder.setDataModelEntityDefinition(TestDataModel.Entity.SubEntity.class, "EXISTS ( SELECT 1 "
        + "FROM TABLE @Table@ "
        + "WHERE @Table@.PRIMARY_KEY=@parent.Table@.PRIMARY_KEY "
        + "<whereParts/> "
        + "<groupBy> "
        + "  GROUP BY @parent.Table@.PRIMARY_KEY "
        + "  HAVING 1=1 "
        + "  <havingParts/> "
        + "</groupBy> "
        + ")",
        "SELECT <selectParts/> "
            + "FROM <fromParts>TABLE @Table@</fromParts> "
            + "WHERE <whereParts>@Table@.PRIMARY_KEY=@parent.Table@.PRIMARY_KEY</whereParts>");
    m_builder.setDataModelAttributeDefinition(TestDataModel.Entity.SubEntity.SubAttribute.class, "@Table@.SUB_ATTRIBUTE");

  }

  @Test
  public void testBuildComposerEntityNodeStrategyBuildConstraintsAggregationNone() {
    ComposerAttributeNodeData subAttributeNode = prepareComposer(DataModelConstants.AGGREGATION_NONE);
    ComposerEntityNodeData subEntityNode = (ComposerEntityNodeData) subAttributeNode.getParentNode();
    m_builder.getAliasMapper().setNodeAlias(subEntityNode.getParentNode(), "Table", "T");

    EntityContribution entityContribution = m_builder.buildComposerEntityNodeContribution(subEntityNode, EntityStrategy.BuildConstraints);
    Assert.assertNotNull(entityContribution);
    Assert.assertTrue(entityContribution.getSelectParts().isEmpty());
    Assert.assertTrue(entityContribution.getFromParts().isEmpty());
    Assert.assertTrue(entityContribution.getGroupByParts().isEmpty());
    Assert.assertTrue(entityContribution.getHavingParts().isEmpty());
    Assert.assertFalse(entityContribution.getWhereParts().isEmpty());

    assertEquals(1, entityContribution.getWhereParts().size());
    Assert.assertEquals("EXISTS ( SELECT 1 FROM TABLE a0001 WHERE a0001.PRIMARY_KEY=T.PRIMARY_KEY AND a0001.SUB_ATTRIBUTE=:__a2 )",
        StringUtility.cleanup(entityContribution.getWhereParts().get(0)));
  }

  @Test
  public void testBuildComposerEntityNodeStrategyBuildConstraintsAggregationSum() {
    ComposerAttributeNodeData subAttributeNode = prepareComposer(DataModelConstants.AGGREGATION_SUM);
    ComposerEntityNodeData subEntityNode = (ComposerEntityNodeData) subAttributeNode.getParentNode();
    m_builder.getAliasMapper().setNodeAlias(subEntityNode.getParentNode(), "Table", "T");

    EntityContribution entityContribution = m_builder.buildComposerEntityNodeContribution(subEntityNode, EntityStrategy.BuildConstraints);
    Assert.assertNotNull(entityContribution);
    Assert.assertTrue(entityContribution.getSelectParts().isEmpty());
    Assert.assertTrue(entityContribution.getFromParts().isEmpty());
    Assert.assertTrue(entityContribution.getGroupByParts().isEmpty());
    Assert.assertTrue(entityContribution.getHavingParts().isEmpty());
    Assert.assertFalse(entityContribution.getWhereParts().isEmpty());

    assertEquals(1, entityContribution.getWhereParts().size());
    Assert.assertEquals("EXISTS ( SELECT 1 FROM TABLE a0001 WHERE a0001.PRIMARY_KEY=T.PRIMARY_KEY GROUP BY T.PRIMARY_KEY HAVING 1=1 AND SUM(a0001.SUB_ATTRIBUTE)=:__a2 )",
        StringUtility.cleanup(entityContribution.getWhereParts().get(0)));
  }

  @Test
  public void testBuildComposerAttributeNodeStrategyBuildConstraintOfAttributeAggregationNone() {
    ComposerAttributeNodeData subAttributeNode = prepareComposer(DataModelConstants.AGGREGATION_NONE);
    m_builder.getAliasMapper().setNodeAlias(subAttributeNode.getParentNode(), "Table", "T");

    EntityContribution attributeContribution = m_builder.buildComposerAttributeNode(subAttributeNode, AttributeStrategy.BuildConstraintOfAttribute);
    Assert.assertNotNull(attributeContribution);
    Assert.assertTrue(attributeContribution.getSelectParts().isEmpty());
    Assert.assertTrue(attributeContribution.getFromParts().isEmpty());
    Assert.assertTrue(attributeContribution.getGroupByParts().isEmpty());
    Assert.assertTrue(attributeContribution.getHavingParts().isEmpty());
    Assert.assertFalse(attributeContribution.getWhereParts().isEmpty());

    assertEquals(1, attributeContribution.getWhereParts().size());
    assertEquals("T.SUB_ATTRIBUTE=:__a1", attributeContribution.getWhereParts().get(0));
  }

  @Test
  public void testBuildComposerAttributeNodeStrategyBuildConstraintOfAttributeAggregationSum() {
    ComposerAttributeNodeData subAttributeNode = prepareComposer(DataModelConstants.AGGREGATION_SUM);
    m_builder.getAliasMapper().setNodeAlias(subAttributeNode.getParentNode(), "Table", "T");

    EntityContribution attributeContribution = m_builder.buildComposerAttributeNode(subAttributeNode, AttributeStrategy.BuildConstraintOfAttribute);
    Assert.assertNotNull(attributeContribution);
    Assert.assertTrue(attributeContribution.getSelectParts().isEmpty());
    Assert.assertTrue(attributeContribution.getFromParts().isEmpty());
    Assert.assertTrue(attributeContribution.getGroupByParts().isEmpty());
    Assert.assertFalse(attributeContribution.getHavingParts().isEmpty());
    Assert.assertTrue(attributeContribution.getWhereParts().isEmpty());

    assertEquals(1, attributeContribution.getHavingParts().size());
    assertEquals("SUM(T.SUB_ATTRIBUTE)=:__a1", attributeContribution.getHavingParts().get(0));
  }

  @Test
  public void testBuildComposerAttributeNodeStrategyBuildConstraintOfAttributeWithContextAggregationNone() {
    ComposerAttributeNodeData subAttributeNode = prepareComposer(DataModelConstants.AGGREGATION_NONE);
    m_builder.getAliasMapper().setNodeAlias(subAttributeNode.getParentNode(), "Table", "T");

    EntityContribution attributeContribution = m_builder.buildComposerAttributeNode(subAttributeNode, AttributeStrategy.BuildConstraintOfAttributeWithContext);
    Assert.assertNotNull(attributeContribution);
    Assert.assertTrue(attributeContribution.getSelectParts().isEmpty());
    Assert.assertTrue(attributeContribution.getFromParts().isEmpty());
    Assert.assertTrue(attributeContribution.getGroupByParts().isEmpty());
    Assert.assertTrue(attributeContribution.getHavingParts().isEmpty());
    Assert.assertFalse(attributeContribution.getWhereParts().isEmpty());

    assertEquals(1, attributeContribution.getWhereParts().size());
    assertEquals("T.SUB_ATTRIBUTE=:__a1", attributeContribution.getWhereParts().get(0));
  }

  @Test
  public void testBuildComposerAttributeNodeStrategyBuildConstraintOfAttributeWithContextAggregationSum() {
    ComposerAttributeNodeData subAttributeNode = prepareComposer(DataModelConstants.AGGREGATION_SUM);
    m_builder.getAliasMapper().setNodeAlias(subAttributeNode.getParentNode(), "Table", "T");

    EntityContribution attributeContribution = m_builder.buildComposerAttributeNode(subAttributeNode, AttributeStrategy.BuildConstraintOfAttributeWithContext);
    Assert.assertNotNull(attributeContribution);
    Assert.assertTrue(attributeContribution.getSelectParts().isEmpty());
    Assert.assertTrue(attributeContribution.getFromParts().isEmpty());
    Assert.assertTrue(attributeContribution.getGroupByParts().isEmpty());
    Assert.assertTrue(attributeContribution.getHavingParts().isEmpty());
    Assert.assertFalse(attributeContribution.getWhereParts().isEmpty());

    assertEquals(1, attributeContribution.getWhereParts().size());
    assertEquals("SUM(T.SUB_ATTRIBUTE)=:__a1", attributeContribution.getWhereParts().get(0));
  }

  private ComposerAttributeNodeData prepareComposer(int aggregationType) {
    IDataModelEntity entity = m_dataModel.getEntity(TestDataModel.Entity.class);
    IDataModelEntity subEntity = entity.getEntity(TestDataModel.Entity.SubEntity.class);
    IDataModelAttribute subAttribute = subEntity.getAttribute(TestDataModel.Entity.SubEntity.SubAttribute.class);

    ComposerAttributeNodeData subAttributeNode = new ComposerAttributeNodeData();
    subAttributeNode.setAggregationType(aggregationType);
    subAttributeNode.setOperator(DataModelConstants.OPERATOR_EQ);
    String attributeExternalId = DataModelUtility.attributePathToExternalId(m_dataModel, new EntityPath().addToEnd(entity).addToEnd(subEntity).addToEnd(subAttribute));
    subAttributeNode.setAttributeExternalId(attributeExternalId);
    subAttributeNode.setValues(CollectionUtility.arrayList(10L));

    ComposerEntityNodeData subEntityNode = new ComposerEntityNodeData();
    subEntityNode.setEntityExternalId(DataModelUtility.entityPathToExternalId(m_dataModel, new EntityPath().addToEnd(entity).addToEnd(subEntity)));
    subEntityNode.setChildNodes(Arrays.<TreeNodeData> asList(subAttributeNode));

    ComposerEntityNodeData entityNode = new ComposerEntityNodeData();
    entityNode.setEntityExternalId(DataModelUtility.entityPathToExternalId(m_dataModel, new EntityPath().addToEnd(entity)));
    entityNode.setChildNodes(Arrays.<TreeNodeData> asList(subEntityNode));
    return subAttributeNode;
  }

  private static class TestDataModel extends AbstractDataModel {
    private static final long serialVersionUID = 1L;

    @Order(10)
    public class Entity extends AbstractDataModelEntity {
      private static final long serialVersionUID = 1L;

      @Order(10)
      public class SubEntity extends AbstractDataModelEntity {
        private static final long serialVersionUID = 1L;

        @Order(10)
        public class SubAttribute extends AbstractDataModelAttribute {
          private static final long serialVersionUID = 1L;

          @Override
          protected int getConfiguredType() {
            return TYPE_LONG;
          }
        }
      }

      @Order(10)
      public class Attribute extends AbstractDataModelAttribute {
        private static final long serialVersionUID = 1L;

        @Override
        protected int getConfiguredType() {
          return TYPE_LONG;
        }
      }
    }
  }
}
