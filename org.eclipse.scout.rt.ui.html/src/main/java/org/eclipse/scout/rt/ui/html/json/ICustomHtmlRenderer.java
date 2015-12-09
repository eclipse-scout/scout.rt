/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

/**
 * Pseudo html is html code written with html tags that is converted in to real html using whitelist rules and malicious
 * code detection checks.
 * <p>
 * It is enclosed in &lt;html&gt; &lt;/html&gt;
 * <p>
 * Basically the following tags are allowed: b, i, p, br, a, span, table, tr, td
 */
public interface ICustomHtmlRenderer {

  /**
   * @return if text is enclosed in &lt;html&gt; &lt;/html&gt;
   */
  boolean isHtml(String text);

  /**
   * TODO [5.2] jgu: from kle: make complete using BBCode with KefirBB maven
   *
   * <pre>
   * <xmp>
   * <dependency>
   *   <groupId>org.kefirsf</groupId>
   *   <artifactId>kefirbb</artifactId>
   *   <version>1.0</version>
   * </dependency>
   * </xmp>
   * </pre>
   *
   * @param customTextOrHtml
   * @param allowHtmlTags
   *          true if html is allowed, false if html is not allowed
   * @return
   *         <p>
   *         true: safe text with all html tags escaped
   *         <p>
   *         false: safe real html with only the tags replaced that are white-listed
   */
  String convert(String customTextOrHtml, boolean allowHtmlTags);

}
