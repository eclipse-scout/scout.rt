/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Before;
import org.junit.Test;

public class DestinationConfigPropertyParserTest {

  private DestinationConfigPropertyParser m_parser;

  @Before
  public void before() {
    newParser();
  }

  protected void newParser() {
    m_parser = new DestinationConfigPropertyParser();
  }

  @Test(expected = AssertionException.class)
  public void testFailIfNotParsed() {
    m_parser.getDestinationName();
  }

  @Test(expected = AssertionException.class)
  public void testFailIfParsedTwice() {
    m_parser.parse("jndi:///simpleQueue");
    m_parser.parse("jndi:///anotherSimpleQueue");
  }

  @Test(expected = AssertionException.class)
  public void testFailUnknownResolveMethod() {
    m_parser.parse("nothing:///simpleQueue");
  }

  @Test(expected = AssertionException.class)
  public void testFailInvalidString() {
    m_parser.parse("this:is:in:invalid:string");
  }

  @Test(expected = AssertionException.class)
  public void testFailEmptyName() {
    m_parser.parse("jndi:///?param=1");
  }

  @Test(expected = AssertionException.class)
  public void testFailEmptyNameOpaque() {
    m_parser.parse("jndi:");
  }

  @Test
  public void testParseEmpty() {
    m_parser.parse("");
  }

  @Test
  public void testParseNull() {
    m_parser.parse(null);
  }

  @Test
  public void testJndiLookupQueueThreeSlashes() {
    m_parser.parse("jndi:///simpleQueue");
    assertEquals("simpleQueue", m_parser.getDestinationName());
    assertEquals(ResolveMethod.JNDI, m_parser.getResolveMethod());
    assertEquals(0, m_parser.getParameters().size());
  }

  @Test
  public void testJndiLookupQueueTwoSlashes() {
    m_parser.parse("jndi://simpleQueue");
    assertEquals("simpleQueue", m_parser.getDestinationName());
    assertEquals(ResolveMethod.JNDI, m_parser.getResolveMethod());
    assertEquals(0, m_parser.getParameters().size());
  }

  @Test
  public void testJndiLookupQueueOpaque() {
    m_parser.parse("jndi:simpleQueue");
    assertEquals("simpleQueue", m_parser.getDestinationName());
    assertEquals(ResolveMethod.JNDI, m_parser.getResolveMethod());
    assertEquals(0, m_parser.getParameters().size());
  }

  @Test
  public void testJndiLookupTopic() {
    m_parser.parse("jndi:///my/special/Topic");
    assertEquals("my/special/Topic", m_parser.getDestinationName());
    assertEquals(ResolveMethod.JNDI, m_parser.getResolveMethod());
    assertEquals(0, m_parser.getParameters().size());
  }

  @Test
  public void testDefineQueue() {
    m_parser.parse("define:///my%3AQueue?param1=&param2=%25%3F");
    assertEquals("my:Queue", m_parser.getDestinationName());
    assertEquals(ResolveMethod.DEFINE, m_parser.getResolveMethod());
    assertEquals(2, m_parser.getParameters().size());
    assertEquals("", m_parser.getParameters().get("param1"));
    assertEquals("%?", m_parser.getParameters().get("param2"));
  }

  @Test
  public void testDefineQueueOpaque() {
    m_parser.parse("define:my%3AQueue?param1=&param2=%25%3F");
    assertEquals("my:Queue?param1=&param2=%?", m_parser.getDestinationName());
    assertEquals(ResolveMethod.DEFINE, m_parser.getResolveMethod());
    assertEquals(0, m_parser.getParameters().size());
  }

  @Test
  public void testDefineSpecialCharactersWhitespace() {
    m_parser.parse("define:///%20a%20name%20%20%20with%20many%20%20spaces?ampersand=%26&manylines=a%0Ab%0Ac");
    assertEquals(" a name   with many  spaces", m_parser.getDestinationName());
    assertEquals(ResolveMethod.DEFINE, m_parser.getResolveMethod());
    assertEquals(2, m_parser.getParameters().size());
    assertEquals("&", m_parser.getParameters().get("ampersand"));
    assertEquals("a\nb\nc", m_parser.getParameters().get("manylines"));
  }

  @Test
  public void testDefineSpecialCharactersLeadingSlash() {
    m_parser.parse("define:///%2Fhello/bla");
    assertEquals("/hello/bla", m_parser.getDestinationName());
    assertEquals(ResolveMethod.DEFINE, m_parser.getResolveMethod());
    assertEquals(0, m_parser.getParameters().size());
  }

  @Test(expected = AssertionException.class)
  public void testMissingResolveMethod() {
    m_parser.parse("hello");
  }

  @Test(expected = AssertionException.class)
  public void testMissingResolveMethodWithSlashesAndColon() {
    m_parser.parse(":///hello");
  }

  @Test(expected = AssertionException.class)
  public void testMissingResolveMethodWithSlashes() {
    m_parser.parse("///hello");
  }
}
