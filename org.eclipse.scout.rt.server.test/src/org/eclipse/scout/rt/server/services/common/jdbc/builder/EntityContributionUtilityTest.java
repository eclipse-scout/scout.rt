/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.jdbc.builder;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.services.common.jdbc.builder.FormDataStatementBuilder.EntityStrategy;
import org.junit.Assert;
import org.junit.Test;

/**
 * test {@link EntityContributionUtility}
 */
public class EntityContributionUtilityTest {

  @Test
  public void testIsAnsiJoin() {
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin(null));
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin(""));
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin(" "));
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin("TABLE A"));
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin("(SELECT 1 FROM TAB T INNER JOIN I ON I.NR=T.NR)"));
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin("(JOIN T)"));
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin("LEFT.OUTER.JOIN T"));
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin("LEFT"));
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin("LEFT OUTER"));
    Assert.assertFalse(EntityContributionUtility.isAnsiJoin("INNER"));

    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("JOIN T"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("JOIN (T)"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("JOIN\nT"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("JOIN\n(T)"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("JOIN\tT"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("JOIN\t(T)"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("LEFT JOIN T"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("RIGHT JOIN T"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("INNER JOIN T"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("OUTER JOIN T"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("LEFT OUTER JOIN T"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("LEFT INNER JOIN T"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("RIGHT OUTER JOIN T"));
    Assert.assertTrue(EntityContributionUtility.isAnsiJoin("RIGHT INNER JOIN T"));
  }

  @Test
  public void testMerge() throws ProcessingException {
    EntityContribution e = new EntityContribution();
    e.getFromParts().add("ADDRESS A");
    e.getFromParts().add("INNER JOIN CONTACT C ON C.PERSON_ID=P.PERSON_ID");
    String s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, "select 1 from <fromParts>user u</fromParts> where 1=1", e, true).getSelectParts().get(0);
    Assert.assertEquals("select 1 from user u, ADDRESS A INNER JOIN CONTACT C ON C.PERSON_ID=P.PERSON_ID where 1=1", s);
  }

  @Test
  public void testMergeStaticallyConfiguredGroupByAndHavingParts() throws Exception {
    String baseSqlWithTags = "SELECT <selectParts>SUM(T.B)</selectParts> FROM <fromParts>SCOUT_TABLE T</fromParts> WHERE <whereParts>T.W = 14</whereParts>";
    //
    String s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags, new EntityContribution(), true).getSelectParts().get(0);
    Assert.assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts/> HAVING 1=1 <havingParts/></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    Assert.assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 ", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    Assert.assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts> HAVING 1=1 <havingParts/></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    Assert.assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP HAVING 1=1 ", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts> HAVING <havingParts>SUM(T.TEST) > 0</havingParts></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    Assert.assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP HAVING SUM(T.TEST) > 0", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>HAVING 1=1 <havingParts/></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    Assert.assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 ", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>HAVING 1=1 <havingParts>AND SUM(T.OTHER) > 10</havingParts></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    Assert.assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 HAVING 1=1 AND SUM(T.OTHER) > 10", s);
    //
    EntityContribution e = new EntityContribution();
    e.addSelectExpression("T.OTHER_ATTRIBUTE", false);
    e.addSelectExpression("AVG(T.NUM)", true);
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts> HAVING <havingParts>SUM(T.TEST) > 0</havingParts></groupBy>", e, true).getSelectParts().get(0);
    Assert.assertEquals("SELECT SUM(T.B), T.OTHER_ATTRIBUTE, AVG(T.NUM) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP, T.OTHER_ATTRIBUTE HAVING SUM(T.TEST) > 0", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery,
        baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts> HAVING <havingParts>SUM(T.TEST) > 0</havingParts></groupBy> " +
            "UNION " +
            baseSqlWithTags + " <groupBy>GROUP BY <groupByParts/> HAVING 1=1 <havingParts/></groupBy>",
        new EntityContribution(), true).getSelectParts().get(0);
    Assert.assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP HAVING SUM(T.TEST) > 0 UNION SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 ", s);
  }

  @Test
  public void testCreateWhereConstraintNullAndEmpty() {
    Assert.assertNull(EntityContributionUtility.contributionToConstraintText(null));
    Assert.assertNull(EntityContributionUtility.contributionToConstraintText(new EntityContribution()));
  }

  @Test
  public void testCreateWhereConstraintWithoutFromParts() {
    EntityContribution contrib = new EntityContribution();
    contrib.getWhereParts().add("T.ATTRIBUTE = 1");
    Assert.assertEquals("T.ATTRIBUTE = 1", EntityContributionUtility.contributionToConstraintText(contrib));
    // add additional attribute
    contrib.getWhereParts().add("T.OTHER_ATTRIBUTE = 1");
    Assert.assertEquals("T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1", EntityContributionUtility.contributionToConstraintText(contrib));
    // select parts are ignored
    contrib.getSelectParts().add("T.ATTRIBUTE");
    Assert.assertEquals("T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1", EntityContributionUtility.contributionToConstraintText(contrib));
    // having and group by parts are ignored because there is no from part
    contrib.getGroupByParts().add("T.ATTRIBUTE");
    contrib.getHavingParts().add("SUM(T.OTHER_ATTRIBUTE)=10");
    Assert.assertEquals("T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1", EntityContributionUtility.contributionToConstraintText(contrib));
  }

  @Test
  public void testCreateWhereConstraintWithFromParts() {
    EntityContribution contrib = new EntityContribution();
    contrib.getFromParts().add("TABLE T");
    contrib.getWhereParts().add("T.ATTRIBUTE = 1");
    Assert.assertEquals(" EXISTS (SELECT 1 FROM TABLE T WHERE T.ATTRIBUTE = 1)", EntityContributionUtility.contributionToConstraintText(contrib));
    // add additional attribute
    contrib.getWhereParts().add("T.OTHER_ATTRIBUTE = 1");
    Assert.assertEquals(" EXISTS (SELECT 1 FROM TABLE T WHERE T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1)", EntityContributionUtility.contributionToConstraintText(contrib));
    // select parts are ignored
    contrib.getSelectParts().add("T.ATTRIBUTE");
    Assert.assertEquals(" EXISTS (SELECT 1 FROM TABLE T WHERE T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1)", EntityContributionUtility.contributionToConstraintText(contrib));
    // having and group by parts are used as well
    contrib.getGroupByParts().add("T.ATTRIBUTE");
    contrib.getHavingParts().add("SUM(T.OTHER_ATTRIBUTE)=10");
    Assert.assertEquals(" EXISTS (SELECT 1 FROM TABLE T WHERE T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1 GROUP BY T.ATTRIBUTE HAVING SUM(T.OTHER_ATTRIBUTE)=10)", EntityContributionUtility.contributionToConstraintText(contrib));
  }

  @Test
  public void testCreateConstraintsContributionNullAndEmpty() {
    Assert.assertNull(EntityContributionUtility.createConstraintsContribution(null));
    Assert.assertNull(EntityContributionUtility.createConstraintsContribution(new EntityContribution()));
  }

  @Test
  public void testCreateConstraints() {
    EntityContribution contrib = new EntityContribution();
    contrib.getWhereParts().add("T.ATTRIBUTE = 1");
    EntityContribution expected = new EntityContribution();
    expected.getWhereParts().add("T.ATTRIBUTE = 1");
    Assert.assertEquals(expected, EntityContributionUtility.createConstraintsContribution(contrib));
    //
    contrib.getWhereParts().add("T.OTHER_ATTRIBUTE = 1");
    expected.getWhereParts().add("T.OTHER_ATTRIBUTE = 1");
    Assert.assertEquals(expected, EntityContributionUtility.createConstraintsContribution(contrib));
    // select parts are ignored
    contrib.getSelectParts().add("T.ATTRIBUTE");
    Assert.assertEquals(expected, EntityContributionUtility.createConstraintsContribution(contrib));
    //
    contrib.getGroupByParts().add("T.ATTRIBUTE");
    contrib.getHavingParts().add("SUM(T.OTHER_ATTRIBUTE)=10");
    expected.getGroupByParts().add("T.ATTRIBUTE");
    expected.getHavingParts().add("SUM(T.OTHER_ATTRIBUTE)=10");
    Assert.assertEquals(expected, EntityContributionUtility.createConstraintsContribution(contrib));
  }
}
