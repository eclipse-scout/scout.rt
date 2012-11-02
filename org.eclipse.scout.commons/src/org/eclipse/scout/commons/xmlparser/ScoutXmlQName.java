/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.xmlparser;

/**
 * Title : Scout XML QName Description: QName (qualified name) support for
 * ScoutXml elements and attributes Copyright : Copyright (c) 2006 BSI AG, ETH
 * Zürich, Stefan Vogt Company : BSI AG www.bsiag.com
 * 
 * @version 1.0
 */
public class ScoutXmlQName implements Comparable {
  private String m_namespaceURI;
  private String m_localName;
  private String m_prefix;

  /**
   * @since 1.0
   */
  protected ScoutXmlQName(String localPart) {
    this(null, null, localPart);
  }

  /**
   * @since 1.0
   */
  protected ScoutXmlQName(String namespace, String localPart) {
    this(namespace, null, localPart);
  }

  /**
   * @since 1.0
   */
  protected ScoutXmlQName(String namespace, String prefix, String localPart) {
    m_namespaceURI = namespace;
    m_prefix = prefix;
    m_localName = localPart;
  }

  /**
   * @since 1.0
   */
  protected static String extractNamespace(String nameExpanded) {
    if (isExpanded(nameExpanded)) {
      return nameExpanded.replaceAll("\\{", "").replaceAll("}.*", "");
    }
    else {
      return "";
    }
  }

  /**
   * @since 1.0
   */
  protected static String extractPrefix(String namePrefixed) {
    if (isPrefixed(namePrefixed)) {
      return namePrefixed.replaceAll(":.*", "");
    }
    else {
      return "";
    }
  }

  /**
   * @since 1.0
   */
  protected static String extractLocalName(String name) {
    if (isPrefixed(name)) {
      return name.replaceAll(".*:", "");
    }
    else if (isExpanded(name)) {
      return name.replaceAll("\\{.*\\}", "");
    }
    else {
      return name;
    }
  }

  /**
   * @since 1.0
   */
  protected static boolean isExpanded(String name) {
    return name.matches("\\{.*\\}.*");
  }

  /**
   * @since 1.0
   */
  protected static boolean isPrefixed(String name) {
    return name.matches(".*:.*");
  }

  /**
   * @since 1.0
   */
  public static ScoutXmlQName valueOf(String name) {
    if ((name != null) && (!name.equals(""))) {
      if (isPrefixed(name)) {
        return new ScoutXmlQName(null, extractPrefix(name), extractLocalName(name));
      }
      else if (isExpanded(name)) {
        return new ScoutXmlQName(extractNamespace(name), null, extractLocalName(name));

      }
      else {
        return new ScoutXmlQName(null, null, name);
      }
    }
    else {
      return null;
    }
  }

  /**
   * @since 1.0
   */
  @Override
  public int compareTo(Object object) {
    if (object != null) {
      return this.toString().compareTo(object.toString());
    }
    else {
      return 0;
    }
  }

  /**
   * @since 1.0
   */
  public int compareToIgnoreCase(Object object) {
    if (object != null) {
      return this.toString().compareToIgnoreCase(object.toString());
    }
    else {
      return 0;
    }
  }

  /**
   * @since 1.0
   */
  @Override
  public boolean equals(Object object) {
    return ((object != null) && (object instanceof ScoutXmlQName) && (((ScoutXmlQName) object).toString().equals(this.toString())));
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  /**
   * @since 1.0
   */
  public boolean equalsIgnoreCase(Object object) {
    return ((object != null) && (object instanceof ScoutXmlQName) && (((ScoutXmlQName) object).toString().equalsIgnoreCase(this.toString())));
  }

  /**
   * @since 1.0
   */
  public String getPrefix() {
    return m_prefix;
  }

  /**
   * @since 1.0
   */
  public String getNamespace() {
    return m_namespaceURI;
  }

  /**
   * @since 1.0
   */
  public boolean hasNamespace() {
    return (m_namespaceURI != null) && (!m_namespaceURI.equals(""));
  }

  /**
   * @since 1.0
   */
  public boolean hasPrefix() {
    return (m_prefix != null) && (!m_prefix.equals(""));
  }

  /**
   * @since 1.0
   */
  public String getExpandedForm() {
    return ((this.hasNamespace() ? "{" + this.getNamespace() + "}" : "") + this.getLocalName());
  }

  /**
   * @since 1.0
   */
  public String getPrefixedForm() {
    return ((this.hasPrefix() ? this.getPrefix() + ":" : "") + this.getLocalName());
  }

  /**
   * @since 1.0
   */
  public String getLocalName() {
    return m_localName;
  }

  /**
   * Returns the expanded form, e.g. {namespace}localpart, of this QName.
   * 
   * @since 1.0
   */
  @Override
  public String toString() {
    return this.getExpandedForm();
  }
}
