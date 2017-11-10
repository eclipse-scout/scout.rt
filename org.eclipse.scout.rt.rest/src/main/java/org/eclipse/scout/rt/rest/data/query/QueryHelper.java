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
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ApplicationScoped
public class QueryHelper {

  /**
   * @param param
   *          comma separated strings
   */
  public List<String> parseParam(String param) {
    return Arrays.asList(StringUtility.tokenize(param, ','));
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
    boolean inBrackets = false;
    StringBuilder token = new StringBuilder();
    for (Character character : str.toCharArray()) {
      if (character.equals('(')) {
        inBrackets = true;
      }
      else if (character.equals(')')) {
        inBrackets = false;
      }
      else if (character.equals(',') && !inBrackets) {
        splitted.add(token.toString());
        token = new StringBuilder();
        continue;
      }
      token.append(character);
    }
    splitted.add(token.toString());
    return splitted;
  }

}
