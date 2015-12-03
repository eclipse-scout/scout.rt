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
package org.eclipse.scout.rt.server.jdbc.internal.exec;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * Tests for {@link StatementProcessor#findNextBind(String, int)}
 * 
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=371963
 */
public class StatementProcessorFindNextBindTest {

  @Test
  public void testFind() throws Exception {
    callFindNextBind(-1, null, 0);

    callFindNextBind(0, "?", 0);
    callFindNextBind(3, "xx ?", 0);
    callFindNextBind(3, "xx ? xxx", 0);
    callFindNextBind(-1, "?", 1);
    callFindNextBind(3, "xx ?", 1);
    callFindNextBind(3, "xx ? xxx", 1);
    callFindNextBind(-1, "xx ?", 4);
    callFindNextBind(-1, "xx ? xxx", 4);

    callFindNextBind(0, "??", 0);
    callFindNextBind(3, "xx ??", 0);
    callFindNextBind(3, "xx ??", 3);
    callFindNextBind(4, "xx ??", 4);
    callFindNextBind(3, "xx ? ?", 3);
    callFindNextBind(5, "xx ? ?", 4);
    callFindNextBind(7, "xx ? xx?x", 4);

    callFindNextBind(-1, "'?'", 0);
    callFindNextBind(4, "'?' ? x", 0);
    callFindNextBind(7, "xx '?' ? x", 0);
    callFindNextBind(3, "xx ? xx '?' '?' ? x", 0);
    callFindNextBind(16, "xx ? xx '?' '?' ? x", 4);
    callFindNextBind(-1, "xx ? xx '?' '?' ? x", 17);
    callFindNextBind(-1, "xx '?' xx", 0);
    callFindNextBind(-1, "xx '????' xx", 0);
    callFindNextBind(12, "xx '????' x ? x", 0);
    callFindNextBind(8, "xx '' x ? x", 0);
    callFindNextBind(-1, "xx '' xx", 0);
    callFindNextBind(-1, "xx ' '' ? '' ' xx", 0);
    callFindNextBind(-1, "xx ''' ? ' ' ' xx", 0);
    callFindNextBind(6, "xx '' ? '' xx", 0);
    callFindNextBind(6, " '''' ? '' xx", 0);
  }

  private void callFindNextBind(int expected, String s, int start) throws Exception {
    Method m = StatementProcessor.class.getDeclaredMethod("findNextBind", String.class, int.class);
    m.setAccessible(true);
    int actual = (Integer) m.invoke(null, s, start);
    assertEquals(expected, actual);
  }

}
