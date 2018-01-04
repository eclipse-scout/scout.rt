/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.data.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.BadRequestException;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class QueryHelper {

  private static final Logger LOG = LoggerFactory.getLogger(QueryHelper.class);

  /**
   * @param param
   *          Comma separated strings
   */
  public List<String> parseParam(String param) {
    return Arrays.asList(StringUtility.tokenize(param, ','));
  }

  /**
   * @param params
   *          Collection of parameters
   */
  public String formatParam(Collection<String> params) {
    return StringUtility.join(",", params);
  }

  /**
   * Splits the string into tokens delimited by ','. Ignores the ',' inside brackets.
   * <p>
   * Examples:
   * <li>attr,attr2 creates [attr,attr2]</li>
   * <li>attr(sub,sub2),attr2 creates [attr(sub,sub2),attr2]</li>
   */
  public List<String> split(String str) {
    List<String> splitted = new ArrayList<>();
    if (str == null || str.isEmpty()) {
      return splitted;
    }
    int bracketCounter = 0;
    StringBuilder token = new StringBuilder();
    for (Character character : str.toCharArray()) {
      if (character.equals('(')) {
        bracketCounter++;
      }
      else if (character.equals(')')) {
        bracketCounter--;
        if (bracketCounter < 0) {
          throw new IllegalStateException("Parsing failed, closing bracket before opening bracket detected.");
        }
      }
      else if (character.equals(',') && bracketCounter == 0) {
        splitted.add(token.toString());
        token = new StringBuilder();
        continue;
      }
      token.append(character);
    }
    splitted.add(token.toString());
    if (bracketCounter != 0) {
      throw new IllegalStateException("Parsing failed, number of opening brackets doesn't match the number of the closing brackets.");
    }
    return splitted;
  }

  /**
   * Creates and returns instances of a query with an include. The given include string is passed and parsed to the
   * instantiated ResourceInclude.
   *
   * @param queryClass
   * @param includeClass
   * @param include
   *          string as received from HTTP query param string
   */
  @SuppressWarnings({"unchecked", "squid:S1166"})
  public <Q extends IDataQueryWithInclude<I>, I extends ResourceInclude> Q buildQueryWithInclude(Class<Q> queryClass, Class<I> includeClass, String include) {
    Q query;
    try {
      query = queryClass.newInstance();
    }
    catch (InstantiationException | IllegalAccessException e) {
      LOG.debug("Failed to instantiate query class {}", queryClass);
      throw new BadRequestException("Failed to instantiate query class.");
    }

    try {
      query.setInclude((I) includeClass.newInstance().parse(include));
      return query;
    }
    catch (Exception e) {
      LOG.debug("Parsing failed with message {}", e.getMessage());
      throw new BadRequestException("Query parsing failed. Check your request parameters.");
    }
  }

}
