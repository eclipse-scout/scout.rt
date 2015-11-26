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
package org.eclipse.scout.rt.server.jdbc.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.server.jdbc.builder.FormDataStatementBuilder.EntityStrategy;
import org.junit.Test;

/**
 * test {@link EntityContributionUtility}
 */
public class EntityContributionUtilityTest {

  @Test
  public void testIsAnsiJoin() {
    assertFalse(EntityContributionUtility.isAnsiJoin(null));
    assertFalse(EntityContributionUtility.isAnsiJoin(""));
    assertFalse(EntityContributionUtility.isAnsiJoin(" "));
    assertFalse(EntityContributionUtility.isAnsiJoin("TABLE A"));
    assertFalse(EntityContributionUtility.isAnsiJoin("(SELECT 1 FROM TAB T INNER JOIN I ON I.NR=T.NR)"));
    assertFalse(EntityContributionUtility.isAnsiJoin("(JOIN T)"));
    assertFalse(EntityContributionUtility.isAnsiJoin("LEFT.OUTER.JOIN T"));
    assertFalse(EntityContributionUtility.isAnsiJoin("LEFT"));
    assertFalse(EntityContributionUtility.isAnsiJoin("LEFT OUTER"));
    assertFalse(EntityContributionUtility.isAnsiJoin("INNER"));

    assertTrue(EntityContributionUtility.isAnsiJoin("JOIN T"));
    assertTrue(EntityContributionUtility.isAnsiJoin("JOIN (T)"));
    assertTrue(EntityContributionUtility.isAnsiJoin("JOIN\nT"));
    assertTrue(EntityContributionUtility.isAnsiJoin("JOIN\n(T)"));
    assertTrue(EntityContributionUtility.isAnsiJoin("JOIN\tT"));
    assertTrue(EntityContributionUtility.isAnsiJoin("JOIN\t(T)"));
    assertTrue(EntityContributionUtility.isAnsiJoin("LEFT JOIN T"));
    assertTrue(EntityContributionUtility.isAnsiJoin("RIGHT JOIN T"));
    assertTrue(EntityContributionUtility.isAnsiJoin("INNER JOIN T"));
    assertTrue(EntityContributionUtility.isAnsiJoin("OUTER JOIN T"));
    assertTrue(EntityContributionUtility.isAnsiJoin("LEFT OUTER JOIN T"));
    assertTrue(EntityContributionUtility.isAnsiJoin("LEFT INNER JOIN T"));
    assertTrue(EntityContributionUtility.isAnsiJoin("RIGHT OUTER JOIN T"));
    assertTrue(EntityContributionUtility.isAnsiJoin("RIGHT INNER JOIN T"));
  }

  @Test
  public void testMerge() {
    EntityContribution e = new EntityContribution();
    e.getFromParts().add("ADDRESS A");
    e.getFromParts().add("INNER JOIN CONTACT C ON C.PERSON_ID=P.PERSON_ID");
    String s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, "select 1 from <fromParts>user u</fromParts> where 1=1", e, true).getSelectParts().get(0);
    assertEquals("select 1 from user u, ADDRESS A INNER JOIN CONTACT C ON C.PERSON_ID=P.PERSON_ID where 1=1", s);
  }

  @Test
  public void testMergeStaticallyConfiguredGroupByAndHavingParts() throws Exception {
    String baseSqlWithTags = "SELECT <selectParts>SUM(T.B)</selectParts> FROM <fromParts>SCOUT_TABLE T</fromParts> WHERE <whereParts>T.W = 14</whereParts>";
    //
    String s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags, new EntityContribution(), true).getSelectParts().get(0);
    assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts/> HAVING 1=1 <havingParts/></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 ", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts> HAVING 1=1 <havingParts/></groupBy>", new EntityContribution(), true)
        .getSelectParts().get(0);
    assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP HAVING 1=1 ", s);
    //
    s = EntityContributionUtility
        .mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts> HAVING <havingParts>SUM(T.TEST) > 0</havingParts></groupBy>", new EntityContribution(), true).getSelectParts()
        .get(0);
    assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP HAVING SUM(T.TEST) > 0", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>HAVING 1=1 <havingParts/></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 ", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>HAVING 1=1 <havingParts>AND SUM(T.OTHER) > 10</havingParts></groupBy>", new EntityContribution(), true).getSelectParts().get(0);
    assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 HAVING 1=1 AND SUM(T.OTHER) > 10", s);
    //
    EntityContribution e = new EntityContribution();
    e.addSelectExpression("T.OTHER_ATTRIBUTE", false);
    e.addSelectExpression("AVG(T.NUM)", true);
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery, baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts> HAVING <havingParts>SUM(T.TEST) > 0</havingParts></groupBy>", e, true)
        .getSelectParts().get(0);
    assertEquals("SELECT SUM(T.B), T.OTHER_ATTRIBUTE, AVG(T.NUM) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP, T.OTHER_ATTRIBUTE HAVING SUM(T.TEST) > 0", s);
    //
    s = EntityContributionUtility.mergeContributions(EntityStrategy.BuildQuery,
        baseSqlWithTags + " <groupBy>GROUP BY <groupByParts>T.GROUP</groupByParts> HAVING <havingParts>SUM(T.TEST) > 0</havingParts></groupBy> "
            + "UNION "
            + baseSqlWithTags + " <groupBy>GROUP BY <groupByParts/> HAVING 1=1 <havingParts/></groupBy>",
        new EntityContribution(), true).getSelectParts().get(0);
    assertEquals("SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 GROUP BY T.GROUP HAVING SUM(T.TEST) > 0 UNION SELECT SUM(T.B) FROM SCOUT_TABLE T WHERE T.W = 14 ", s);
  }

  @Test
  public void testCreateWhereConstraintNullAndEmpty() {
    assertNull(EntityContributionUtility.contributionToConstraintText(null));
    assertNull(EntityContributionUtility.contributionToConstraintText(new EntityContribution()));
  }

  @Test
  public void testCreateWhereConstraintWithoutFromParts() {
    EntityContribution contrib = new EntityContribution();
    contrib.getWhereParts().add("T.ATTRIBUTE = 1");
    assertEquals("T.ATTRIBUTE = 1", EntityContributionUtility.contributionToConstraintText(contrib));
    // add additional attribute
    contrib.getWhereParts().add("T.OTHER_ATTRIBUTE = 1");
    assertEquals("T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1", EntityContributionUtility.contributionToConstraintText(contrib));
    // select parts are ignored
    contrib.getSelectParts().add("T.ATTRIBUTE");
    assertEquals("T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1", EntityContributionUtility.contributionToConstraintText(contrib));
    // having and group by parts are ignored because there is no from part
    contrib.getGroupByParts().add("T.ATTRIBUTE");
    contrib.getHavingParts().add("SUM(T.OTHER_ATTRIBUTE)=10");
    assertEquals("T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1", EntityContributionUtility.contributionToConstraintText(contrib));
  }

  @Test
  public void testCreateWhereConstraintWithFromParts() {
    EntityContribution contrib = new EntityContribution();
    contrib.getFromParts().add("TABLE T");
    contrib.getWhereParts().add("T.ATTRIBUTE = 1");
    assertEquals(" EXISTS (SELECT 1 FROM TABLE T WHERE T.ATTRIBUTE = 1)", EntityContributionUtility.contributionToConstraintText(contrib));
    // add additional attribute
    contrib.getWhereParts().add("T.OTHER_ATTRIBUTE = 1");
    assertEquals(" EXISTS (SELECT 1 FROM TABLE T WHERE T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1)", EntityContributionUtility.contributionToConstraintText(contrib));
    // select parts are ignored
    contrib.getSelectParts().add("T.ATTRIBUTE");
    assertEquals(" EXISTS (SELECT 1 FROM TABLE T WHERE T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1)", EntityContributionUtility.contributionToConstraintText(contrib));
    // having and group by parts are used as well
    contrib.getGroupByParts().add("T.ATTRIBUTE");
    contrib.getHavingParts().add("SUM(T.OTHER_ATTRIBUTE)=10");
    assertEquals(" EXISTS (SELECT 1 FROM TABLE T WHERE T.ATTRIBUTE = 1 AND T.OTHER_ATTRIBUTE = 1 GROUP BY T.ATTRIBUTE HAVING SUM(T.OTHER_ATTRIBUTE)=10)", EntityContributionUtility.contributionToConstraintText(contrib));
  }

  @Test
  public void testCreateConstraintsContributionNullAndEmpty() {
    assertNull(EntityContributionUtility.createConstraintsContribution(null));
    assertNull(EntityContributionUtility.createConstraintsContribution(new EntityContribution()));
  }

  @Test
  public void testCreateConstraints() {
    EntityContribution contrib = new EntityContribution();
    contrib.getWhereParts().add("T.ATTRIBUTE = 1");
    EntityContribution expected = new EntityContribution();
    expected.getWhereParts().add("T.ATTRIBUTE = 1");
    assertEquals(expected, EntityContributionUtility.createConstraintsContribution(contrib));
    //
    contrib.getWhereParts().add("T.OTHER_ATTRIBUTE = 1");
    expected.getWhereParts().add("T.OTHER_ATTRIBUTE = 1");
    assertEquals(expected, EntityContributionUtility.createConstraintsContribution(contrib));
    // select parts are ignored
    contrib.getSelectParts().add("T.ATTRIBUTE");
    assertEquals(expected, EntityContributionUtility.createConstraintsContribution(contrib));
    //
    contrib.getGroupByParts().add("T.ATTRIBUTE");
    contrib.getHavingParts().add("SUM(T.OTHER_ATTRIBUTE)=10");
    expected.getGroupByParts().add("T.ATTRIBUTE");
    expected.getHavingParts().add("SUM(T.OTHER_ATTRIBUTE)=10");
    assertEquals(expected, EntityContributionUtility.createConstraintsContribution(contrib));
  }
}
