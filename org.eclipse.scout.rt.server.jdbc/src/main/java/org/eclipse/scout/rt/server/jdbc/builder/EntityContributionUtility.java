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
package org.eclipse.scout.rt.server.jdbc.builder;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.jdbc.builder.FormDataStatementBuilder.EntityStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for building statements with {@link EntityContribution}
 */
public final class EntityContributionUtility {
  private static final Logger LOG = LoggerFactory.getLogger(EntityContributionUtility.class);

  private EntityContributionUtility() {
  }

  public static EntityContribution constraintTextToContribution(String wherePart) {
    EntityContribution contrib = new EntityContribution();
    if (wherePart != null) {
      contrib.getWhereParts().add(wherePart);
    }
    return contrib;
  }

  /**
   * Creates a where constraints based on the {@link EntityContribution}. This means that from parts etc. are wrapped
   * inside an EXISTS (SELECT 1 FROM ... WHERE ... ) clause.
   *
   * @return a where constraint or null if the {@link EntityContribution} is empty.
   *         <p>
   *         The constraint does not start with "AND" and can be added with {@link #addWhere(String, NVPair...)} by
   *         prepending "AND"
   */
  public static String contributionToConstraintText(EntityContribution contrib) {
    // if there are no where parts, do nothing
    EntityContribution whereContribution = createConstraintsContribution(contrib);
    if (whereContribution == null) {
      return null;
    }
    if (whereContribution.getWhereParts().isEmpty()) {
      return null;
    }
    return CollectionUtility.format(whereContribution.getWhereParts(), " AND ");
  }

  /**
   * Creates constraints based on the {@link EntityContribution}. This means that from parts etc. are wrapped inside an
   * EXISTS (SELECT 1 FROM ... WHERE ... GROUP BY ... HAVING ...) clause.
   *
   * @return Returns an {@link EntityContribution} with constraints or null if the given {@link EntityContribution} is
   *         empty.
   */
  public static EntityContribution createConstraintsContribution(EntityContribution contrib) {
    // if there are no where or having parts, do nothing
    if (contrib == null) {
      return null;
    }
    if (contrib.getWhereParts().isEmpty() && contrib.getHavingParts().isEmpty()) {
      return null;
    }
    if (contrib.getFromParts().isEmpty()) {
      // no from parts, just use where, group by and having parts
      EntityContribution result = new EntityContribution();
      result.getWhereParts().addAll(contrib.getWhereParts());
      result.getGroupByParts().addAll(contrib.getGroupByParts());
      result.getHavingParts().addAll(contrib.getHavingParts());
      return result;
    }
    // there are from parts
    // create an EXISTS (SELECT 1 FROM ... WHERE ... GROUP BY ... HAVING ...)
    StringBuilder sb = new StringBuilder();
    sb.append(" EXISTS (SELECT 1 FROM ");
    sb.append(CollectionUtility.format(contrib.getFromParts(), ", "));
    if (!contrib.getWhereParts().isEmpty()) {
      sb.append(" WHERE ");
      sb.append(CollectionUtility.format(contrib.getWhereParts(), " AND "));
    }
    if (!contrib.getGroupByParts().isEmpty()) {
      sb.append(" GROUP BY ");
      sb.append(CollectionUtility.format(contrib.getGroupByParts(), ", "));
    }
    if (!contrib.getHavingParts().isEmpty()) {
      sb.append(" HAVING ");
      sb.append(CollectionUtility.format(contrib.getHavingParts(), " AND "));
    }
    sb.append(")");
    return EntityContribution.create(sb.toString());
  }

  /**
   * Evaluates the collecting tags in the entity statement and fills in the values of the {@link EntityContribution}. If
   * the contributing tags are missing, the complete part is treated as 'select' on {@link EntityStrategy#BuildQuery}
   * and as 'where' on {@link EntityStrategy#BuildConstraints}
   * <p>
   * Before the call is processed, all {@link IFormDataStatementBuilderInjection}s are invoked.
   *
   * @param entityStrategy
   * @param entityPartWithTags
   *          may contain the collecting tags selectParts, fromParts, whereParts, groupBy, groupByParts, havingParts
   *          <br/>
   *          as well as the contributing selectPart, fromPart, wherePart, groupByPart, havingPart for the outer calling
   *          part.
   * @param childContributions
   *          is the set of tags collected by all children
   * @param consumeChildContributions
   *          true: consume the child tags inside the entity statement. The returned entity contributions will not
   *          contain any of these tags
   *          <p>
   *          false: don't consume the child tags inside the entity statement. The returned entity contribution contains
   *          its onw plus all of these child tags (proxy)
   */
  @SuppressWarnings({"bsiRulesDefinition:htmlInString", "squid:S138"})
  public static EntityContribution mergeContributions(EntityStrategy entityStrategy, final String entityPartWithTags, EntityContribution childContributions, boolean consumeChildContributions) {
    String entityPart = entityPartWithTags;
    EntityContribution parentContrib = new EntityContribution();
    //PROCESS collectiong tags: selectParts, fromParts, whereParts, groupBy, groupByParts, havingParts
    if (!consumeChildContributions) {
      //just proxy through to parent
      parentContrib.add(childContributions);
    }
    else {
      // extend the select section
      if (!childContributions.getSelectParts().isEmpty()) {
        StringBuilder selectBuf = new StringBuilder();
        for (String selectPart : childContributions.getSelectParts()) {
          if (selectBuf.length() > 0) {
            selectBuf.append(", ");
          }
          selectBuf.append(autoBracketSelectPart(selectPart));
        }
        final String s = selectBuf.toString();
        if (StringUtility.getTag(entityPart, "selectParts") != null) {
          entityPart = StringUtility.replaceTags(entityPart, "selectParts", (tagName, tagContent) -> {
            if (!tagContent.isEmpty()) {
              return tagContent + ", " + s;
            }
            return s;
          });
        }
        else {
          throw new IllegalArgumentException("missing <selectParts/> tag");
        }
      }
      entityPart = StringUtility.removeTagBounds(entityPart, "selectParts");
      // extend the from section
      Set<String> fromParts = new TreeSet<>(childContributions.getFromParts());
      if (!fromParts.isEmpty()) {
        StringBuilder buf = new StringBuilder();
        for (String fromPart : fromParts) {
          if (!isAnsiJoin(fromPart)) {
            buf.append(",");
          }
          buf.append(" ");
          buf.append(fromPart);
        }
        final String s = buf.toString();
        if (StringUtility.getTag(entityPart, "fromParts") != null) {
          entityPart = StringUtility.replaceTags(entityPart, "fromParts", (tagName, tagContent) -> tagContent + s);
        }
        else {
          throw new IllegalArgumentException("missing <fromParts/> tag");
        }
      }
      entityPart = StringUtility.removeTagBounds(entityPart, "fromParts");
      // extend the where section
      if (!childContributions.getWhereParts().isEmpty()) {

        final String s = CollectionUtility.format(childContributions.getWhereParts(), " AND ");
        if (StringUtility.getTag(entityPart, "whereParts") != null) {
          entityPart = StringUtility.replaceTags(entityPart, "whereParts", (tagName, tagContent) -> {
            return tagContent + " AND " + s;//legacy: always prefix an additional AND
          });
        }
        else {
          entityPart = entityPart + " AND " + s;
        }
      }
      entityPart = StringUtility.removeTagBounds(entityPart, "whereParts");
      // extend the group by / having section
      if (StringUtility.getTag(entityPart, "groupBy") != null) {
        int selectGroupByDelta = childContributions.getSelectParts().size() - childContributions.getGroupByParts().size();
        if ((selectGroupByDelta > 0 && !childContributions.getGroupByParts().isEmpty()) || !childContributions.getHavingParts().isEmpty()) {
          entityPart = StringUtility.removeTagBounds(entityPart, "groupBy");
          if (!childContributions.getGroupByParts().isEmpty()) {
            //check group by parts
            for (String s : childContributions.getGroupByParts()) {
              checkGroupByPart(s);
            }
            final String s = CollectionUtility.format(childContributions.getGroupByParts(), ", ");
            if (StringUtility.getTag(entityPart, "groupByParts") != null) {
              entityPart = StringUtility.replaceTags(entityPart, "groupByParts", (tagName, tagContent) -> {
                if (!tagContent.isEmpty()) {
                  return tagContent + ", " + s;
                }
                return s;
              });
            }
            else {
              throw new IllegalArgumentException("missing <groupByParts/> tag");
            }
          }
          else {
            //no group by parts, avoid empty GROUP BY clause
            entityPart = StringUtility.replaceTags(entityPart, "groupByParts", (tagName, tagContent) -> {
              if (!tagContent.isEmpty()) {
                return tagContent;
              }
              return tagContent + " 1 ";
            });
          }
          entityPart = StringUtility.removeTagBounds(entityPart, "groupByParts");
          //
          if (!childContributions.getHavingParts().isEmpty()) {
            final String s = CollectionUtility.format(childContributions.getHavingParts(), " AND ");
            if (StringUtility.getTag(entityPart, "havingParts") != null) {
              entityPart = StringUtility.replaceTags(entityPart, "havingParts", (tagName, tagContent) -> {
                return tagContent + " AND " + s;//legacy: always prefix an additional AND
              });
            }
            else {
              throw new IllegalArgumentException("missing <havingParts/> tag");
            }
          }
          else {
            entityPart = StringUtility.removeTagBounds(entityPart, "havingParts");
          }
        }
        else {
          entityPart = StringUtility.replaceTags(entityPart, "groupBy", (tagName, tagContent) -> {
            if (!StringUtility.hasText(StringUtility.getTag(tagContent, "groupByParts"))
                && !StringUtility.hasText(StringUtility.getTag(tagContent, "havingParts"))) {
              return "";
            }

            // preserve statically defined group-by and having parts
            tagContent = StringUtility.replaceTags(tagContent, "groupByParts", (innerTagName, innerTagContent) -> {
              if (!innerTagContent.isEmpty()) {
                return innerTagContent;
              }
              return innerTagContent + " 1 ";
            });
            tagContent = StringUtility.replaceTags(tagContent, "havingParts", (innerTagName, innerTagContent) -> innerTagContent);
            return tagContent;
          });
        }
      }
    }
    //PROCESS contributing tags: selectPart, fromPart, wherePart, groupByPart, havingPart
    String selectPart = StringUtility.getTag(entityPart, "selectPart");
    if (selectPart != null) {
      parentContrib.getSelectParts().add(selectPart);
      entityPart = StringUtility.removeTag(entityPart, "selectPart").trim();
    }
    //
    String fromPart = StringUtility.getTag(entityPart, "fromPart");
    if (fromPart != null) {
      parentContrib.getFromParts().add(fromPart);
      entityPart = StringUtility.removeTag(entityPart, "fromPart").trim();
    }
    //
    String wherePart = StringUtility.getTag(entityPart, "wherePart");
    if (wherePart != null) {
      parentContrib.getWhereParts().add(wherePart);
      entityPart = StringUtility.removeTag(entityPart, "wherePart").trim();
    }
    //
    String groupByPart = StringUtility.getTag(entityPart, "groupByPart");
    if (groupByPart != null) {
      parentContrib.getGroupByParts().add(groupByPart);
      entityPart = StringUtility.removeTag(entityPart, "groupByPart").trim();
    }
    //
    String havingPart = StringUtility.getTag(entityPart, "havingPart");
    if (havingPart != null) {
      parentContrib.getHavingParts().add(havingPart);
      entityPart = StringUtility.removeTag(entityPart, "havingPart").trim();
    }
    if (parentContrib.isEmpty()) {
      switch (entityStrategy) {
        case BuildConstraints: {
          parentContrib.getWhereParts().add(entityPart);
          break;
        }
        case BuildQuery: {
          parentContrib.getSelectParts().add(entityPart);
          parentContrib.getGroupByParts().add("1");
          break;
        }
      }
    }
    else {
      //check for remaining dirt
      if (!entityPart.isEmpty()) {
        LOG.warn("entityPart {} contains content that is not wrapped in a tag: {}", entityPartWithTags, entityPart);
      }
    }
    return parentContrib;
  }

  private static final Pattern ANSI_JOIN_PATTERN = Pattern.compile("\\s*(LEFT\\s+|RIGHT\\s+)?(OUTER\\s+|INNER\\s+)?JOIN\\s+.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  public static boolean isAnsiJoin(String fromPart) {
    if (fromPart == null) {
      return false;
    }
    return ANSI_JOIN_PATTERN.matcher(fromPart).matches();
  }

  private static final Pattern CHECK_GROUP_BY_CONTAINS_SELECT_PATTERN = Pattern.compile("[^a-z0-9\"'.%$_]SELECT[^a-z0-9\"'.%$_]", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  public static final int STATUS_CODE_INVALID_GROUP_BY_PART = 0x70000001;

  /**
   * Check if a group by part is valid, i.e. ist not a SELECT clause.
   *
   * @throws ProcessingException
   *           with {@link IStatus#getCode()} = X
   * @since 3.8
   */
  public static void checkGroupByPart(String groupByPart) {
    if (groupByPart == null) {
      return;
    }
    if (CHECK_GROUP_BY_CONTAINS_SELECT_PATTERN.matcher(groupByPart).find()) {
      throw new ProcessingException("Invalid group by clause").withCode(STATUS_CODE_INVALID_GROUP_BY_PART);
    }
  }

  private static String autoBracketSelectPart(String s) {
    if (s != null && !s.startsWith("(") && s.toLowerCase().contains("select")) {
      return "(" + s + ")";
    }
    return s;
  }

}
