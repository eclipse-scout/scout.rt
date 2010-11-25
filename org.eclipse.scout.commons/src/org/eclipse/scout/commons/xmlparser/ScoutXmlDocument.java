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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.xml.sax.Attributes;

/**
 * Title : Scout XML Document Description: Copyright : Copyright (c) 2006-2008
 * BSI AG, ETH Zürich, Stefan Vogt Company : BSI AG www.bsiag.com
 * 
 * @version 1.4
 */
@SuppressWarnings("unchecked")
public class ScoutXmlDocument {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutXmlDocument.class);

  private static final boolean DEFAULT_ACCEPT_REGEX_QUERIES = false;
  private static final boolean DEFAULT_IGNORE_INDENT_WHITESPACE = false;
  private static final boolean DEFAULT_IGNORE_QUERY_CASE = false;
  private static final String DEFAULT_INDENT = "  ";
  private static final boolean DEFAULT_PRETTY_PRINT = true;
  private static final boolean DEFAULT_STRICTLY_CHECKING = true;
  private static final String DEFAULT_XML_ENCODING = "UTF-8"; // "ISO-8859-1"
  private static final String DEFAULT_XML_VERSION = "1.0";
  private static final int INITIAL_ATTRIBUTE_LIST_SIZE = 5;
  private static final int INITIAL_CONTENT_LIST_SIZE = 10;
  private static final int INITIAL_NAMESPACE_MAP_SIZE = 5;

  public static final Hashtable<String, String> XML_ENTITIES;

  static {
    XML_ENTITIES = new Hashtable<String, String>();

    XML_ENTITIES.put("&", "&amp;");
    XML_ENTITIES.put("\"", "&quot;");
    XML_ENTITIES.put("'", "&apos;");
    XML_ENTITIES.put("<", "&lt;");
    XML_ENTITIES.put(">", "&gt;");
  }

  private P_Registry m_nameRegistry;
  private P_Registry m_textRegistry;
  private P_Registry m_attvRegistry;

  private boolean m_acceptRegExQueries;
  private boolean m_ignoreQueryCase;
  private boolean m_ignoreIndentWhitespace;
  private String m_indent;
  private String m_externalDtdPublicId;
  private String m_externalDtdLocation;
  private boolean m_prettyPrint;
  private ScoutXmlElement m_root;
  private boolean m_strictlyChecking;
  private String m_xmlEncoding;
  private String m_xmlVersion;

  /**
   * @since 1.0
   */
  public ScoutXmlDocument() {
    m_nameRegistry = new P_Registry();
    m_textRegistry = new P_Registry();
    m_attvRegistry = new P_Registry();

    this.setAcceptRegExQueries(DEFAULT_ACCEPT_REGEX_QUERIES);
    this.setIgnoreIndentWhitespace(DEFAULT_IGNORE_INDENT_WHITESPACE);
    this.setIgnoreQueryCase(DEFAULT_IGNORE_QUERY_CASE);
    this.setIndent(DEFAULT_INDENT);
    this.setPrettyPrint(DEFAULT_PRETTY_PRINT);
    this.setStrictlyChecking(DEFAULT_STRICTLY_CHECKING);
    this.setXmlEncoding(DEFAULT_XML_ENCODING);
    this.setXmlVersion(DEFAULT_XML_VERSION);

    this.setExternalDTD(null, null);
  }

  /**
   * @since 1.0
   */
  public ScoutXmlDocument(File file) {
    this();

    try {
      new ScoutXmlParser(this).parse(file);
    }
    catch (Exception exception) {
      LOG.error("Could not parse the given file.", exception);
    }
  }

  /**
   * @since 1.0
   */
  public ScoutXmlDocument(InputStream stream) {
    this();

    try {
      new ScoutXmlParser(this).parse(stream);
    }
    catch (Exception exception) {
      LOG.error("Could not parse the given stream.", exception);
    }
  }

  /**
   * @since 1.0
   */
  public ScoutXmlDocument(InputStream stream, String systemID) {
    this();

    try {
      new ScoutXmlParser(this).parse(stream, systemID);
    }
    catch (Exception exception) {
      LOG.error("Could not parse the given stream.", exception);
    }
  }

  /**
   * @since 1.0
   */
  public ScoutXmlDocument(ScoutXmlElement root) {
    this();
    this.setRoot(root);
  }

  /**
   * @since 1.0
   */
  public ScoutXmlDocument(String source) {
    this();

    try {
      new ScoutXmlParser(this).parse(source);
    }
    catch (Exception exception) {
      LOG.error("Could not parse the given source.", exception);
    }
  }

  /**
   * @since 1.0
   */
  public ScoutXmlDocument(String source, String systemID) {
    this();

    try {
      new ScoutXmlParser(this).parse(source, systemID);
    }
    catch (Exception exception) {
      LOG.error("Could not parse the given file.", exception);
    }
  }

  /**
   * @since 1.0
   */
  public ScoutXmlDocument(URL url) {
    this();

    try {
      new ScoutXmlParser(this).parse(url);
    }
    catch (Exception exception) {
      LOG.error("Could not parse the given file.", exception);
    }
  }

  /**
   * Returns true if and only if the String representations of the two
   * ScoutXmlDocument instances are equal. Indents and new-line characters are
   * ignored.
   * 
   * @since 1.3
   */
  public boolean equalsSemantically(Object object) {
    if (object == null) return false;
    try {
      boolean oldSetting = this.isPrettyPrint();
      this.setPrettyPrint(false);
      String string = this.getRoot().toString();
      this.setPrettyPrint(oldSetting);
      if (object instanceof ScoutXmlDocument) {
        oldSetting = ((ScoutXmlDocument) object).isPrettyPrint();
        ((ScoutXmlDocument) object).setPrettyPrint(false);
        boolean result = string.equals(((ScoutXmlDocument) object).getRoot().toString());
        ((ScoutXmlDocument) object).setPrettyPrint(oldSetting);

        return result;
      }
      else if (object instanceof String) return string.equals(object);
      else return false;
    }
    catch (Exception exception) {
      return false;
    }
  }

  /**
   * @since 1.1
   */
  public ScoutXmlElement getChild(String name) {
    String n = null;

    if (name.startsWith(n = this.getRoot().getNameExpanded())) {
      if (name.equals(n)) return this.getRoot();
      else return this.getRoot().getChild(name.replaceFirst(n, ""));
    }
    else if (name.startsWith(n = this.getRoot().getNamePrefixed())) {
      if (name.equals(n)) return this.getRoot();
      else return this.getRoot().getChild(name.replaceFirst(n, ""));
    }
    else return null;
  }

  /**
   * @since 1.0
   */
  public String getExternalDtdLocation() {
    return m_externalDtdLocation;
  }

  /**
   * @since 1.0
   */
  public String getExternalDtdPublicId() {
    return m_externalDtdLocation;
  }

  /**
   * @since 1.0
   */
  public String getIndent() {
    return m_indent;
  }

  /**
   * @since 1.0
   */
  public ScoutXmlElement getRoot() {
    return m_root;
  }

  /**
   * @since 1.0
   */
  public String getXmlEncoding() {
    return m_xmlEncoding;
  }

  /**
   * @since 1.0
   */
  public String getXmlVersion() {
    return m_xmlVersion;
  }

  /**
   * @since 1.0
   */
  public boolean hasExternalDTD() {
    return (m_externalDtdLocation != null);
  }

  /**
   * @since 1.0
   */
  public boolean hasRoot() {
    return (m_root != null);
  }

  /**
   * @since 1.0
   */
  public boolean isAcceptRegExQueries() {
    return m_acceptRegExQueries;
  }

  /**
   * @since 1.0
   */
  public boolean isEmpty() {
    return ((m_root == null) && (m_externalDtdLocation == null) && (m_externalDtdPublicId == null));
  }

  /**
   * @since 1.0
   */
  public boolean isIgnoreIndentWhitespace() {
    return m_ignoreIndentWhitespace;
  }

  /**
   * @since 1.0
   */
  public boolean isIgnoreQueryCase() {
    return m_ignoreQueryCase;
  }

  /**
   * @since 1.0
   */
  public boolean isPrettyPrint() {
    return m_prettyPrint;
  }

  /**
   * @since 1.0
   */
  public boolean isStrictlyChecking() {
    return m_strictlyChecking;
  }

  /**
   * @since 1.0
   */
  public void setAcceptRegExQueries(boolean acceptRegEx) {
    m_acceptRegExQueries = acceptRegEx;
  }

  /**
   * @since 1.0
   */
  public void setExternalDTD(String publicID, String dtdLocation) {
    m_externalDtdPublicId = publicID;
    m_externalDtdLocation = dtdLocation;
  }

  /**
   * @since 1.0
   */
  public void setIgnoreIndentWhitespace(boolean indentWhitespace) {
    m_ignoreIndentWhitespace = indentWhitespace;
  }

  /**
   * @since 1.0
   */
  public void setIgnoreQueryCase(boolean ignoreCase) {
    m_ignoreQueryCase = ignoreCase;
  }

  /**
   * @since 1.0
   */
  public void setIndent(String indent) {
    m_indent = indent;
  }

  /**
   * @since 1.0
   */
  public void setPrettyPrint(boolean prettyPrint) {
    m_prettyPrint = prettyPrint;

    if (prettyPrint) this.setIndent(DEFAULT_INDENT);
    else this.setIndent("");
  }

  /**
   * @since 1.0
   */
  public void setRoot(ScoutXmlElement root) {
    if (root.getDocument().equals(ScoutXmlDocument.this)) {
      m_root = root;
    }
    else {
      m_root = null;

      try {
        new ScoutXmlParser(this).parse(root.toString());
      }
      catch (Exception exception) {
        // Must not happpen
      }
    }
  }

  /**
   * @since 1.0
   */
  public ScoutXmlElement setRoot(String name) {
    return (m_root = new ScoutXmlElement(name));
  }

  /**
   * @since 1.0
   */
  public void setStrictlyChecking(boolean strictlyChecking) {
    m_strictlyChecking = strictlyChecking;
  }

  /**
   * @since 1.0
   */
  public void setXmlEncoding(String encoding) {
    m_xmlEncoding = encoding;
  }

  /**
   * @since 1.0
   */
  public void setXmlVersion(String xmlVersion) {
    m_xmlVersion = xmlVersion;
  }

  /**
   * Converts the XML datastructure into an ArrayList[] Works of course only
   * with specific XML documents, for example one which was created with new
   * ScoutXmlDocument(arrayList).
   * 
   * @since 1.2
   */
  public ArrayList[] toData() {
    ArrayList[] data = null;
    ScoutXmlElement rowElement = null;
    ScoutXmlElement colElement = null;

    try {
      int noRows = this.getChild("ArrayList").countChildren();
      data = new ArrayList[noRows];

      for (int rowNr = 0; rowNr < noRows; rowNr++) {
        rowElement = this.getChild("ArrayList").getChild(rowNr);
        data[rowNr] = new ArrayList(rowElement.countChildren());

        for (int colNr = 0; colNr < rowElement.countChildren(); colNr++) {
          colElement = this.getChild("ArrayList").getChild(rowNr).getChild(colNr);
          data[rowNr].add(Class.forName(colElement.getAttribute("type")).getConstructor(new Class[]{String.class}).newInstance(new Object[]{colElement.getText()}));
        }
      }
    }
    catch (Exception exception) {
      LOG.error("Could not create ArrayList[].", exception);
    }

    return data;
  }

  /**
   * @since 1.0
   */
  @Override
  public String toString() {
    try {
      StringWriter writer = new StringWriter();
      this.write(writer);
      writer.close();

      return writer.toString();
    }
    catch (Exception exception) {
      return null;
    }
  }

  /**
   * @since 1.0
   */
  public File write(File file) throws IOException {
    this.write(new FileOutputStream(file));
    return file;
  }

  /**
   * @since 1.0
   */
  public void write(OutputStream stream) throws IOException {
    this.write(new BufferedWriter(new OutputStreamWriter(stream, m_xmlEncoding)));
  }

  /**
   * @since 1.0
   */
  private void write(Writer writer) throws IOException {
    BufferedWriter bufferedWriter = null;

    if (writer instanceof BufferedWriter) bufferedWriter = (BufferedWriter) writer;
    else bufferedWriter = new BufferedWriter(writer);

    bufferedWriter.write("<?xml version=\"" + m_xmlVersion + "\" encoding=\"" + m_xmlEncoding + "\"?>");

    if (ScoutXmlDocument.this.isPrettyPrint()) bufferedWriter.newLine();

    if (m_root != null) {
      if (m_externalDtdLocation != null) {
        bufferedWriter.write("<!DOCTYPE");
        bufferedWriter.write(' ');
        bufferedWriter.write(m_root.getNamePrefixed());
        bufferedWriter.write(' ');

        if (m_externalDtdPublicId == null) bufferedWriter.write("SYSTEM \"" + m_externalDtdLocation);
        else bufferedWriter.write("PUBLIC \"" + m_externalDtdPublicId + "\" \"" + m_externalDtdLocation);
        bufferedWriter.write("\">");

        if (ScoutXmlDocument.this.isPrettyPrint()) bufferedWriter.newLine();
      }

      m_root.write(bufferedWriter);
    }

    bufferedWriter.flush();
  }

  /**
   * Title : Scout XML Node Description: Private class for Scout XML Document
   * Copyright : Copyright (c) 2006 BSI AG, ETH Zürich, Stefan Vogt Company :
   * BSI AG www.bsiag.com
   * 
   * @version 1.0
   * @since 1.0
   */
  private abstract class P_AbstractNode {
    protected int m_nameID; // The name ID of this node. Is resolved in the

    // enclosing document.

    /**
     * Returns this node's QName.
     * 
     * @return This node's QName.
     * @since 1.0
     */
    public abstract ScoutXmlQName getName();

    /**
     * Returns this node's QName in the expanded form. If the namespace doesn't
     * exist only the localName is returned.
     * 
     * @return This node's QName in the expanded form. For example
     *         "{http://namespace}localName".
     * @since 1.0
     */
    public String getNameExpanded() {
      return this.getName().getExpandedForm();
    }

    /**
     * Returns this node's QName in the prefixed form. If the prefix doesn't
     * exist only the localName is returned.
     * 
     * @return This node's QName in the prefixed form. For example
     *         "ns0:localName".
     * @since 1.0
     */
    public String getNamePrefixed() {
      return m_nameRegistry.getValueAsString(m_nameID);
    }

    /**
     * @since 1.0
     */
    public abstract String getNamespace(String prefix);

    /**
     * Sets this node's name. Accepted forms are 'localname',
     * '{namespace}localname' and 'prefix:localname'. In the case of a prefixed
     * name, the corresponding namespace is only checked if the document is set
     * to strictly checking.
     * 
     * @since 1.0
     */
    public void setName(String name) {
      if (ScoutXmlDocument.this.isStrictlyChecking()) if (ScoutXmlQName.isPrefixed(name)) if (getNamespace(ScoutXmlQName.extractPrefix(name)) == null) throw new ScoutXmlException("Unbound prefix: " + ScoutXmlQName.extractPrefix(name));

      m_nameID = m_nameRegistry.getID(name, true);
    }

    /**
     * @since 1.0
     */
    public abstract void write(Writer writer) throws IOException;
  }

  /**
   * Title : Node counter Description: Private class for Scout XML Document
   * Copyright : Copyright (c) 2006 BSI AG, ETH Zürich, Stefan Vogt Company :
   * BSI AG www.bsiag.com
   * 
   * @version 1.0
   * @since 1.0
   */
  private class P_NodeCounter extends P_AbstractNodeVisitor {
    private int m_counter = 0;

    public P_NodeCounter(String name) {
      super(name);
    }

    @Override
    public Object getResult() {
      return new Integer(m_counter);
    }

    @Override
    public boolean isNotDoneYet() {
      return true;
    }

    @Override
    protected void nameMatched(P_AbstractNode element) {
      m_counter++;
    }
  }

  /**
   * Title : Node existence checker Description: Private class for Scout XML
   * Document Copyright : Copyright (c) 2006 BSI AG, ETH Zürich, Stefan Vogt
   * Company : BSI AG www.bsiag.com
   * 
   * @version 1.0
   * @since 1.0
   */
  private class P_NodeExistenceChecker extends P_AbstractNodeVisitor {
    private boolean m_nodeFound = false;

    public P_NodeExistenceChecker(String name) {
      super(name);
    }

    @Override
    public Object getResult() {
      return new Boolean(m_nodeFound);
    }

    @Override
    public boolean isNotDoneYet() {
      return (!m_nodeFound);
    }

    @Override
    protected void nameMatched(P_AbstractNode node) {
      m_nodeFound = true;
    }
  }

  /**
   * Title : Multi node selector Description: Private class for Scout XML
   * Document Copyright : Copyright (c) 2006 BSI AG, ETH Zürich, Stefan Vogt
   * Company : BSI AG www.bsiag.com
   * 
   * @version 1.0
   * @since 1.0
   */
  private class P_NodeSelectorMulti extends P_AbstractNodeVisitor {
    private List m_selectedNodes = new ArrayList();
    private String[] m_requiredAttributeNames = null;
    private String[] m_requiredAttributeValues = null;

    public P_NodeSelectorMulti(String name) {
      super(name);
    }

    public P_NodeSelectorMulti(String name, String[] requiredAttributeNames, String[] requiredAttributeValues) {
      super(name);

      m_requiredAttributeNames = requiredAttributeNames;
      m_requiredAttributeValues = requiredAttributeValues;
    }

    @Override
    public Object getResult() {
      return m_selectedNodes;
    }

    @Override
    public boolean isNotDoneYet() {
      return true;
    }

    @Override
    protected void nameMatched(P_AbstractNode node) {
      if (m_requiredAttributeNames != null) {
        for (int i = 0; i < m_requiredAttributeNames.length; i++) {
          try {
            if (!((ScoutXmlElement) node).hasAttribute(m_requiredAttributeNames[i], m_requiredAttributeValues[i])) return;
          }
          catch (NullPointerException exception) {
            return;
          }
        }
      }

      m_selectedNodes.add(node);
    }
  }

  /**
   * Title : Single node selector Description: Private class for Scout XML
   * Document Copyright : Copyright (c) 2006 BSI AG, ETH Zürich, Stefan Vogt
   * Company : BSI AG www.bsiag.com
   * 
   * @version 1.0
   * @since 1.0
   */
  private class P_NodeSelectorSingle extends P_AbstractNodeVisitor {
    private P_AbstractNode m_selectedNode = null;
    private String[] m_requiredAttributeNames = null;
    private String[] m_requiredAttributeValues = null;

    public P_NodeSelectorSingle(String name) {
      super(name);
    }

    public P_NodeSelectorSingle(String name, String[] requiredAttributeNames, String[] requiredAttributeValues) {
      super(name);

      m_requiredAttributeNames = requiredAttributeNames;
      m_requiredAttributeValues = requiredAttributeValues;
    }

    @Override
    public Object getResult() {
      return m_selectedNode;
    }

    @Override
    public boolean isNotDoneYet() {
      return (m_selectedNode == null);
    }

    @Override
    protected void nameMatched(P_AbstractNode node) {
      if (m_requiredAttributeNames != null) {
        for (int i = 0; i < m_requiredAttributeNames.length; i++) {
          try {
            if (!((ScoutXmlElement) node).hasAttribute(m_requiredAttributeNames[i], m_requiredAttributeValues[i])) return;
          }
          catch (NullPointerException exception) {
            return;
          }
        }
      }

      m_selectedNode = node;
    }
  }

  /**
   * Title : Node Visitor Description: Private class for Scout XML Document
   * Copyright : Copyright (c) 2006 BSI AG, ETH Zürich, Stefan Vogt Company :
   * BSI AG www.bsiag.com
   * 
   * @version 1.0
   * @since 1.0
   */
  private abstract class P_AbstractNodeVisitor {
    private int m_mode = 0;
    private String m_stringToCompare;

    protected P_AbstractNodeVisitor(String name) {
      if (name.matches("(\\*)|(\\{\\*\\}\\*)|(\\*:\\*)")) // * || {*}* || *:*
      {
        m_mode = 0;
        m_stringToCompare = null;
      }
      else if (name.matches("(\\{\\*\\}.*)|(\\*:.*)")) // {*}localpart ||
      // *:localpart
      {
        m_mode = 1;
        m_stringToCompare = ScoutXmlQName.extractLocalName(name);
      }
      else if (name.matches("\\{.*\\}\\*")) // {namespace}*
      {
        m_mode = 2;
        m_stringToCompare = ScoutXmlQName.extractNamespace(name);
      }
      else if (name.matches(".*:\\*")) // prefix:*
      {
        m_mode = 3;
        m_stringToCompare = ScoutXmlQName.extractPrefix(name);
      }
      else if (name.matches("\\{.*\\}.*")) // {namespace}localpart || {}localpart
      {
        m_mode = 4;
        m_stringToCompare = name.replaceAll("\\{\\}", "");
      }
      else // prefix:localpart || localpart
      {
        m_mode = 5;
        m_stringToCompare = name;
      }
    }

    protected void visit(P_AbstractNode node) {
      String string = "";

      switch (m_mode) {
        case 0:
          nameMatched(node);
          return;
        case 1:
          string = node.getName().getLocalName();
          break;
        case 2:
          string = node.getName().getNamespace();
          break;
        case 3:
          string = node.getName().getPrefix();
          break;
        case 4:
          string = node.getNameExpanded();
          break;
        case 5:
          string = node.getNamePrefixed();
          break;
      }

      if (ScoutXmlDocument.this.isAcceptRegExQueries()) {
        String stringToCompare;

        string = string.replace('{', ' ').replace('}', ' ');
        stringToCompare = m_stringToCompare.replace('{', ' ').replace('}', ' ');

        if (m_ignoreQueryCase) {
          string = string.toUpperCase();
          stringToCompare = stringToCompare.toUpperCase();
        }

        if (string.matches(stringToCompare)) nameMatched(node);
      }
      else {
        if (m_ignoreQueryCase && string.equalsIgnoreCase(m_stringToCompare)) nameMatched(node);
        else if (string.equals(m_stringToCompare)) nameMatched(node);
      }
    }

    protected abstract void nameMatched(P_AbstractNode element);

    protected abstract boolean isNotDoneYet();

    protected abstract Object getResult();
  }

  /**
   * Title : Scout XML Registry Description: Stores names and values in a
   * central place to preserve resources. Copyright : Copyright (c) 2006 BSI AG,
   * ETH Zürich, Stefan Vogt Company : BSI AG www.bsiag.com
   * 
   * @version 1.0
   * @since 1.0
   */
  private class P_Registry {
    private static final int INITIAL_SIZE = 50;

    private List m_values; // Maps IDs to values
    private Map m_valuesReverse; // Maps values to IDs

    /**
     * @since 1.0
     */
    public P_Registry() {
      m_values = new ArrayList(INITIAL_SIZE);
      m_valuesReverse = new Hashtable(INITIAL_SIZE);
    }

    /**
     * @since 1.0
     */
    public int getID(Object value, boolean addValueIfNotExists) {
      if (value != null) {
        if (m_valuesReverse.containsKey(value)) {
          return ((Integer) m_valuesReverse.get(value)).intValue();
        }
        else if (addValueIfNotExists) {
          m_values.add(value);
          m_valuesReverse.put(value, new Integer(m_values.size() - 1));

          return (m_values.size() - 1);
        }
      }

      return -1;
    }

    /**
     * @since 1.0
     */
    public Object getValue(int valueID) {
      return m_values.get(valueID);
    }

    /**
     * @since 1.0
     */
    public Object getValue(Integer valueID) {
      return this.getValue(valueID.intValue());
    }

    /**
     * @since 1.0
     */
    public String getValueAsString(int valueID) {
      return (String) this.getValue(valueID);
    }

    /**
     * @since 1.0
     */
    public String getValueAsString(Integer valueID) {
      return (String) this.getValue(valueID);
    }
  }

  /**
   * Title : Scout XML Element Description: Copyright : Copyright (c) 2006 BSI
   * AG, ETH Zürich, Stefan Vogt Company : BSI AG www.bsiag.com
   * 
   * @version 1.0
   */
  public class ScoutXmlElement extends P_AbstractNode {
    private List m_attributes; // This element's attributes
    private Object m_content; // This element's content (text, a child, a list
    // of children or a mixed list)
    private Map m_namespaces; // This element's namespace declarations
    private ScoutXmlElement m_parent; // This element's predecessor

    /**
     * @since 1.0
     */
    protected ScoutXmlElement() {
      m_attributes = null;
      m_content = null;
      m_namespaces = null;
      m_parent = null;
    }

    /**
     * @since 1.0
     */
    protected ScoutXmlElement(String name) {
      this();
      super.setName(name);
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement addChild() {
      ScoutXmlElement child = new ScoutXmlElement();

      child.setParent(this);
      this.addChild(child);

      return child;
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement addChild(String name) {
      ScoutXmlElement child = this.addChild();

      child.setName(name);

      return child;
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement addChild(ScoutXmlDocument document) {
      return this.addChild(document, -1);
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement addChild(ScoutXmlDocument document, int position) {
      return this.addChild(document.getRoot(), position);
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement addChild(ScoutXmlElement child) {
      return this.addChild(child, -1);
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement addChild(ScoutXmlElement child, int position) {
      if (child.getDocument().equals(ScoutXmlDocument.this)) {
        if (child != null) {
          child.setParent(this);

          if (m_content == null) {
            m_content = child;
          }
          else if (m_content instanceof List) {
            if (position == -1) ((List) m_content).add(child);
            else ((List) m_content).add(position, child);
          }
          else {
            List newContent = new ArrayList(INITIAL_CONTENT_LIST_SIZE);
            newContent.add(m_content);
            newContent.add(child);
            m_content = newContent;
          }
        }
      }
      else {
        try {
          new ScoutXmlParser(ScoutXmlDocument.this, ScoutXmlElement.this).parse(child.toString());
        }
        catch (Exception e) {
          // Must not happpen
        }
      }

      return child;
    }

    /**
     * @since 1.0
     */
    public void addChildren(List children) {
      for (int i = 0; i < children.size(); i++)
        this.addChild((ScoutXmlElement) children.get(i));
    }

    /**
     * @since 1.0
     */
    public void addContent(Object content) {
      if (content instanceof ScoutXmlElement) {
        this.addChild((ScoutXmlElement) content);
      }
      else {
        this.addText(content.toString());
      }
    }

    /**
     * @since 1.0
     */
    public void addText(String text) {
      if ((text != null) && (!text.equals(""))) {
        Integer textID = new Integer(m_textRegistry.getID(text, true));

        if (m_content == null) {
          m_content = textID;
        }
        else if (m_content instanceof List) {
          ((List) m_content).add(textID);
        }
        else {
          List newContent = new ArrayList(INITIAL_ATTRIBUTE_LIST_SIZE);
          newContent.add(m_content);
          newContent.add(textID);
          m_content = newContent;
        }
      }
    }

    /**
     * Returns this element's number of attributes.
     * 
     * @since 1.0
     */
    public int countAttributes() {
      if (m_attributes != null) return m_attributes.size();
      else return 0;
    }

    /**
     * Returns this element's number of attributes with the given name.
     * 
     * @since 1.0
     */
    public int countAttributes(String name) {
      return ((Integer) this.visitAttributes(new P_NodeCounter(name))).intValue();
    }

    /**
     * Returns this element's number of child elements.
     * 
     * @since 1.0
     */
    public int countChildren() {
      if (m_content instanceof List) {
        Iterator iterator = ((List) m_content).iterator();
        int noChildren = 0;

        while (iterator.hasNext())
          if (iterator.next() instanceof ScoutXmlElement) noChildren++;

        return noChildren;
      }
      else if (m_content instanceof ScoutXmlElement) {
        return 1;
      }
      else {
        return 0;
      }
    }

    /**
     * Returns this element's number of child elements with the given name.
     * 
     * @since 1.0
     */
    public int countChildren(String name) {
      return ((Integer) this.visitChildren(new P_NodeCounter(name))).intValue();
    }

    /**
     * Returns this element's number of descendant elements.
     * 
     * @since 1.0
     */
    public int countDescendants() {
      return this.countDescendants("*");
    }

    /**
     * Returns this element's number of descendant elements with the given name.
     * 
     * @since 1.0
     */
    public int countDescendants(String name) {
      return ((Integer) this.visitDescendants(new P_NodeCounter(name))).intValue();
    }

    /**
     * Returns this element's number of sibling elements.
     * 
     * @since 1.1
     */
    public int countSiblings() {
      return this.countSiblings("*");
    }

    /**
     * Returns this element's number of sibling elements with the given name.
     * 
     * @since 1.1
     */
    public int countSiblings(String name) {
      if (this.hasParent()) return (((Integer) this.getParent().visitChildren(new P_NodeCounter(name))).intValue() - 1);
      else return 0;
    }

    /**
     * Returns the number of namespaces which are declared on this element.
     * 
     * @since 1.0
     */
    public int countNamespaces() {
      if (this.hasNamespace()) return m_namespaces.size();
      else return 0;
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @return The attribute's value.
     * @throws ScoutXmlException
     *           If the attribute does not exist or if its value is invalid.
     * @since 1.0
     */
    public String getAttribute(String name) {
      try {
        return this.getAttributeNode(name).getValueAsString();
      }
      catch (NullPointerException exception) {
        throw new ScoutXmlException("Attribute '" + name + "' does not exist for '" + this.getPath() + "'");
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @param defaultValue
     *          The default value for this attribute.
     * @return The attribute's value or the default value if the attribute does
     *         not exist.
     * @since 1.0
     */
    public String getAttribute(String name, String defaultValue) {
      try {
        return this.getAttributeNode(name).getValueAsString();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @return The attribute's value.
     * @throws ScoutXmlException
     *           If the attribute does not exist or if its value is invalid.
     * @since 1.0
     */
    public boolean getAttributeAsBoolean(String name) {
      try {
        return this.getAttributeNode(name).getValueAsBoolean();
      }
      catch (NullPointerException exception) {
        throw new ScoutXmlException("Attribute '" + name + "' does not exist for '" + this.getPath() + "'");
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @param defaultValue
     *          The default value for this attribute.
     * @return The attribute's value or the default value if the attribute does
     *         not exist.
     * @since 1.0
     */
    public boolean getAttributeAsBoolean(String name, boolean defaultValue) {
      try {
        return this.getAttributeNode(name).getValueAsBoolean();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @param format
     *          The format pattern for this date.
     * @return The attribute's value.
     * @throws ScoutXmlException
     *           If the attribute does not exist or if its value is invalid.
     * @since 1.1
     */
    public Date getAttributeAsDate(String name, String format) {
      try {
        return this.getAttributeNode(name).getValueAsDate(format);
      }
      catch (NullPointerException exception) {
        throw new ScoutXmlException("Attribute '" + name + "' does not exist for '" + this.getPath() + "'");
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @param format
     *          The format pattern for this date.
     * @param defaultValue
     *          The default value for this attribute.
     * @return The attribute's value or the default value if the attribute does
     *         not exist.
     * @since 1.1
     */
    public Date getAttributeAsDate(String name, String format, Date defaultValue) {
      try {
        return this.getAttributeNode(name).getValueAsDate(format);
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @return The attribute's value.
     * @throws ScoutXmlException
     *           If the attribute does not exist or if its value is invalid.
     * @since 1.0
     */
    public double getAttributeAsDouble(String name) {
      try {
        return this.getAttributeNode(name).getValueAsDouble();
      }
      catch (NullPointerException exception) {
        throw new ScoutXmlException("Attribute '" + name + "' does not exist for '" + this.getPath() + "'");
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @param defaultValue
     *          The default value for this attribute.
     * @return The attribute's value or the default value if the attribute does
     *         not exist.
     * @since 1.0
     */
    public double getAttributeAsDouble(String name, double defaultValue) {
      try {
        return this.getAttributeNode(name).getValueAsDouble();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @return The attribute's value.
     * @throws ScoutXmlException
     *           If the attribute does not exist or if its value is invalid.
     * @since 1.0
     */
    public int getAttributeAsInt(String name) {
      try {
        return this.getAttributeNode(name).getValueAsInt();
      }
      catch (NullPointerException exception) {
        throw new ScoutXmlException("Attribute '" + name + "' does not exist for '" + this.getPath() + "'");
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @param defaultValue
     *          The default value for this attribute.
     * @return The attribute's value or the default value if the attribute does
     *         not exist.
     * @since 1.0
     */
    public int getAttributeAsInt(String name, int defaultValue) {
      try {
        return this.getAttributeNode(name).getValueAsInt();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @return The attribute's value.
     * @throws ScoutXmlException
     *           If the attribute does not exist or if its value is invalid.
     * @since 1.0
     */
    public long getAttributeAsLong(String name) {
      try {
        return this.getAttributeNode(name).getValueAsLong();
      }
      catch (NullPointerException exception) {
        throw new ScoutXmlException("Attribute '" + name + "' does not exist for '" + this.getPath() + "'");
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @param defaultValue
     *          The default value for this attribute.
     * @return The attribute's value as a string or the default value if the
     *         attribute does not exist.
     * @since 1.0
     */
    public long getAttributeAsLong(String name, long defaultValue) {
      try {
        return this.getAttributeNode(name).getValueAsLong();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @return The attribute's value.
     * @throws ScoutXmlException
     *           If the attribute does not exist or if its value is invalid.
     * @since 1.0
     */
    public short getAttributeAsShort(String name) {
      return this.getAttributeNode(name).getValueAsShort();
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @param defaultValue
     *          The default value for this attribute.
     * @return The attribute's value or the default value if the attribute does
     *         not exist.
     * @since 1.0
     */
    public short getAttributeAsShort(String name, short defaultValue) {
      try {
        return this.getAttributeNode(name).getValueAsShort();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves an attribute node by name.
     * 
     * @param name
     *          The name of the attribute to retrieve.
     * @return The attribute node or null if it does not exist.
     * @since 1.0
     */
    private P_Attribute getAttributeNode(String name) {
      return (P_Attribute) this.visitAttributes(new P_NodeSelectorSingle(name));
    }

    /**
     * @since 1.0
     */
    private Collection getAttributeNodes(String name) {
      return (Collection) this.visitAttributes(new P_NodeSelectorMulti(name));
    }

    /**
     * @since 1.0
     */
    public int getIndex() {
      if (this.hasParent()) return this.getParent().getChildren().indexOf(this);
      else return 0;
    }

    /**
     * @since 1.0
     */
    @Override
    public String getNamespace(String prefix) {
      if (prefix != null) if (this.hasNamespace(prefix)) return (String) m_namespaces.get(prefix);
      else if (this.hasParent()) return this.getParent().getNamespace(prefix);
      else return null;
      else return null;
    }

    /**
     * @since 1.0
     */
    public Map getNamespaces() {
      return m_namespaces;
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement getChild(int index) {
      try {
        if ((index == 0) && (m_content instanceof ScoutXmlElement)) return (ScoutXmlElement) m_content;
        else return (ScoutXmlElement) this.getChildren().get(index);
      }
      catch (Exception exception) {
        throw new ScoutXmlException("Child with index " + index + " does not exist for '" + this.getPath() + "'");
      }
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement getChild(String name) {
      return (ScoutXmlElement) this.visitChildren(new P_NodeSelectorSingle(name));
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement getChild(String name, String requiredAttributeName, String requiredAttributeValue) {
      return this.getChild(name, new String[]{requiredAttributeName}, new String[]{requiredAttributeValue});
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement getChild(String name, String[] requiredAttributeNames, String[] requiredAttributeValues) {
      return (ScoutXmlElement) this.visitChildren(new P_NodeSelectorSingle(name, requiredAttributeNames, requiredAttributeValues));
    }

    /**
     * Returns this element's children. If there are no children, an empty list
     * is returned.
     * 
     * @since 1.0
     */
    public List getChildren() {
      ArrayList children = new ArrayList();

      if (m_content instanceof List) {
        if (this.hasText()) {
          for (int i = 0; i < ((List) m_content).size(); i++) {
            Object o = ((List) m_content).get(i);

            if (o instanceof ScoutXmlElement) children.add(o);
          }
        }
        else {
          return (List) m_content;
        }
      }
      else if (m_content instanceof ScoutXmlElement) {
        children.add(m_content);
      }

      return children;
    }

    /**
     * @since 1.0
     */
    public List getChildren(String name) {
      return (List) this.visitChildren(new P_NodeSelectorMulti(name));
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement getDescendant(String name) {
      return (ScoutXmlElement) this.visitDescendants(new P_NodeSelectorSingle(name));
    }

    /**
     * @since 1.0
     */
    public List getDescendants(String name) {
      return (List) this.visitDescendants(new P_NodeSelectorMulti(name));
    }

    /**
     * @since 1.0
     */
    public ScoutXmlDocument getDocument() {
      return ScoutXmlDocument.this;
    }

    /**
     * @since 1.0
     */
    public boolean hasAttribute(String name) {
      try {
        return ((Boolean) this.visitAttributes(new P_NodeExistenceChecker(name))).booleanValue();
      }
      catch (Exception exception) {
        return false;
      }
    }

    /**
     * @since 1.0
     */
    public boolean hasAttribute(String name, String value) {
      return (this.hasAttribute(name) && this.getAttribute(name).equals(value));
    }

    /**
     * @since 1.0
     */
    public boolean hasAttributeExact(String name) {
      if (this.hasAttributes()) {
        Iterator iterator = m_attributes.iterator();

        for (int i = 0; iterator.hasNext(); i++) {
          if (((P_Attribute) iterator.next()).getNamePrefixed().equals(name)) return true;
        }
      }

      return false;
    }

    /**
     * @since 1.0
     */
    public boolean hasAttributes() {
      return ((m_attributes != null) && (!m_attributes.isEmpty()));
    }

    /**
     * @since 1.0
     */
    public boolean hasChild(String name) {
      return ((Boolean) this.visitChildren(new P_NodeExistenceChecker(name))).booleanValue();
    }

    /**
     * @since 1.0
     */
    public boolean hasChildren() {
      if (m_content instanceof ScoutXmlElement) {
        return true;
      }
      else if (m_content instanceof List) {
        Iterator iterator = ((List) m_content).iterator();

        while (iterator.hasNext())
          if (iterator.next() instanceof ScoutXmlElement) return true;
      }

      return false;
    }

    /**
     * @since 1.0
     */
    public boolean hasContent() {
      return (m_content != null);
    }

    /**
     * @since 1.0
     */
    public boolean hasNamespace() {
      if (m_namespaces != null) return (!m_namespaces.isEmpty());
      else return false;
    }

    /**
     * @since 1.0
     */
    public boolean hasNamespace(String prefix) {
      if (this.hasNamespace() && (prefix != null)) return m_namespaces.containsKey(prefix);
      else return false;
    }

    /**
     * @since 1.0
     */
    public boolean hasParent() {
      return (this.getParent() != null);
    }

    /**
     * @since 1.0
     */
    public boolean hasName() {
      return (m_nameID != -1);
    }

    /**
     * @since 1.0
     */
    public boolean hasText() {
      if (m_content instanceof Integer) {
        return true;
      }
      else if (m_content instanceof String) {
        return (!((String) m_content).equals(""));
      }
      else if (m_content instanceof List) {
        Iterator iterator = ((List) m_content).iterator();

        while (iterator.hasNext()) {
          Object contentPart = iterator.next();
          if (contentPart instanceof String || contentPart instanceof Integer) return true;
        }
      }

      return false;
    }

    /**
     * @since 1.0
     */
    public boolean isRoot() {
      return (!this.hasParent());
    }

    /**
     * Optimizes the memory consumption of this element's data structures
     * Intended to be called by an instance of ScoutXmlParser.
     * 
     * @since 1.0
     */
    protected void optimize() {

      // 1. Optimize content data structure

      if (m_content instanceof List) {
        if (((List) m_content).size() == 0) m_content = null;
        else ((ArrayList) m_content).trimToSize();
      }

      // 2. Optimize attribute data structure

      if (m_attributes != null) {
        if (m_attributes.isEmpty()) m_attributes = null;
        else ((ArrayList) m_attributes).trimToSize();
      }

      // 3. Optimize namespace data structure

      if (m_namespaces != null) {
        if (m_namespaces.isEmpty()) m_namespaces = null;
      }
    }

    /**
     * Retrieves this element's text. If the element contains mixed content the
     * different text parts are concatenated. If the element contains no text
     * the empty string is returned.
     * 
     * @return This element's text.
     * @since 1.0
     */
    public String getText() {
      return getText("");
    }

    /**
     * Retrieves this element's text. If the element contains mixed content the
     * different text parts are concatenated. If the element contains no text
     * the given defaultValue is returned.
     * 
     * @param defaultValue
     *          The default value.
     * @return This element's text.
     * @since 1.4
     */
    public String getText(String defaultValue) {
      String text = null;

      if (m_content instanceof Integer) {
        text = m_textRegistry.getValueAsString((Integer) m_content);
      }
      else if (m_content instanceof String) {
        text = (String) m_content;
      }
      else if (m_content instanceof List) {
        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < ((List) m_content).size(); i++) {
          Object contentPart = ((List) m_content).get(i);

          if (contentPart instanceof String) stringBuffer.append((String) contentPart);
          else if (contentPart instanceof Integer) stringBuffer.append(m_textRegistry.getValueAsString((Integer) contentPart));
        }

        text = stringBuffer.toString();
      }

      if (text != null) if (ScoutXmlDocument.this.isIgnoreIndentWhitespace()) return text.replaceAll("\n *", "\n").replaceAll("\n\t*", "\n");
      else return text;
      else return defaultValue;
    }

    /**
     * Retrieves this element's text.
     * 
     * @return The element's text.
     * @throws ScoutXmlException
     *           If the value is invalid.
     * @since 1.0
     */
    public boolean getTextAsBoolean() {
      try {
        return Boolean.valueOf(this.getText()).booleanValue();
      }
      catch (Exception exception) {
        throw new ScoutXmlException("The text '" + this.getText() + "' is not a valid 'boolean' value (" + ScoutXmlElement.this.getPath() + ")");
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @param defaultValue
     *          The default value.
     * @return The element's text or the default value if the text value is
     *         invalid.
     * @since 1.0
     */
    public boolean getTextAsBoolean(boolean defaultValue) {
      try {
        return Boolean.valueOf(this.getText()).booleanValue();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @format The date's format pattern.
     * @return The element's text.
     * @throws ScoutXmlException
     *           If the value is invalid.
     * @since 1.0
     */
    public Date getTextAsDate(String format) {
      try {
        return new SimpleDateFormat(format).parse(this.getText());
      }
      catch (Exception exception) {
        throw new ScoutXmlException("The text '" + this.getText() + "' is not a valid 'Date' value (" + ScoutXmlElement.this.getPath() + ")");
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @format The date's format pattern.
     * @param defaultValue
     *          The default value.
     * @return The element's text or the default value if the text value is
     *         invalid.
     * @since 1.0
     */
    public Date getTextAsDate(String format, Date defaultValue) {
      try {
        return new SimpleDateFormat(format).parse(this.getText());
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @return The element's text.
     * @throws ScoutXmlException
     *           If the value is invalid.
     * @since 1.0
     */
    public double getTextAsDouble() {
      try {
        return Double.valueOf(this.getText()).doubleValue();
      }
      catch (Exception exception) {
        throw new ScoutXmlException("The text '" + this.getText() + "' is not a valid 'double' value (" + ScoutXmlElement.this.getPath() + ")");
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @param defaultValue
     *          The default value.
     * @return The element's text or the default value if the text value is
     *         invalid.
     * @since 1.0
     */
    public double getTextAsDouble(double defaultValue) {
      try {
        return Double.valueOf(this.getText()).doubleValue();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @return The element's text.
     * @throws ScoutXmlException
     *           If the value is invalid.
     * @since 1.0
     */
    public int getTextAsInt() {
      try {
        return Integer.valueOf(this.getText()).intValue();
      }
      catch (Exception exception) {
        throw new ScoutXmlException("The text '" + this.getText() + "' is not a valid 'int' value (" + ScoutXmlElement.this.getPath() + ")");
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @param defaultValue
     *          The default value.
     * @return The element's text or the default value if the text value is
     *         invalid.
     * @since 1.0
     */
    public int getTextAsInt(int defaultValue) {
      try {
        return Integer.valueOf(this.getText()).intValue();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @return The element's text.
     * @throws ScoutXmlException
     *           If the value is invalid.
     * @since 1.0
     */
    public long getTextAsLong() {
      try {
        return Long.valueOf(this.getText()).longValue();
      }
      catch (Exception exception) {
        throw new ScoutXmlException("The text '" + this.getText() + "' is not a valid 'long' value (" + ScoutXmlElement.this.getPath() + ")");
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @param defaultValue
     *          The default value.
     * @return The element's text or the default value if the text value is
     *         invalid.
     * @since 1.0
     */
    public long getTextAsLong(long defaultValue) {
      try {
        return Long.valueOf(this.getText()).longValue();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @return The element's text.
     * @throws ScoutXmlException
     *           If the value is invalid.
     * @since 1.0
     */
    public short getTextAsShort() {
      try {
        return Short.valueOf(this.getText()).shortValue();
      }
      catch (Exception exception) {
        throw new ScoutXmlException("The text '" + this.getText() + "' is not a valid 'short' value (" + ScoutXmlElement.this.getPath() + ")");
      }
    }

    /**
     * Retrieves this element's text.
     * 
     * @param defaultValue
     *          The default value.
     * @return The element's text or the default value if the text value is
     *         invalid.
     * @since 1.0
     */
    public short getTextAsShort(short defaultValue) {
      try {
        return Short.valueOf(this.getText()).shortValue();
      }
      catch (Exception exception) {
        return defaultValue;
      }
    }

    public String getLocalName() {
      String name = m_nameRegistry.getValueAsString(m_nameID);
      String localName = ScoutXmlQName.extractLocalName(name);
      return localName;
    }

    public String getNamePrefix() {
      String name = m_nameRegistry.getValueAsString(m_nameID);
      String prefix = ScoutXmlQName.extractPrefix(name);
      return prefix;
    }

    public String getNameNamespace() {
      String name = m_nameRegistry.getValueAsString(m_nameID);
      String prefix = ScoutXmlQName.extractPrefix(name);
      return getNamespace(prefix);
    }

    /**
     * @since 1.0
     */
    @Override
    public ScoutXmlQName getName() {
      String name = m_nameRegistry.getValueAsString(m_nameID);
      String prefix = ScoutXmlQName.extractPrefix(name);
      String localName = ScoutXmlQName.extractLocalName(name);
      return new ScoutXmlQName(this.getNamespace(prefix), prefix, localName);
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement getParent() {
      return m_parent;
    }

    /**
     * @since 1.1
     */
    public String getPath() {
      StringBuffer buffer = new StringBuffer();
      ScoutXmlElement element = this;

      while (element.hasParent()) {
        if (element.countSiblings(element.getNameExpanded()) > 0) {
          buffer.insert(0, ']');
          buffer.insert(0, element.getIndex());
          buffer.insert(0, '[');
        }

        buffer.insert(0, element.getNameExpanded());
        buffer.insert(0, '/');

        element = element.getParent();
      }

      buffer.insert(0, element.getNameExpanded());

      return buffer.toString();
    }

    /**
     * @since 1.0
     */
    public ScoutXmlElement getRoot() {
      if (this.hasParent()) return this.getParent().getRoot();
      else return this;
    }

    /**
     * Removes this element's attribute which matches the given name. Nothing is
     * done if the attribute can't be found.
     * 
     * @since 1.0
     */
    public void removeAttribute(String name) {
      try {
        m_attributes.remove(this.getAttributeNode(name));
      }
      catch (Exception exception) {
        // Do nothing...
      }
    }

    /**
     * Removes all of this element's attributes.
     * 
     * @since 1.0
     */
    public void removeAttributes() {
      m_attributes = null;
    }

    /**
     * Removes all of this element's attributes which match the given name.
     * Nothing is done if no matching attribute can be found.
     * 
     * @since 1.0
     */
    public void removeAttributes(String name) {
      try {
        m_attributes.removeAll(this.getAttributeNodes(name));
      }
      catch (Exception exception) {
        // Do nothing...
      }
    }

    /**
     * Removes the the child with the given index. Nothing is done if the child
     * can't be found.
     * 
     * @since 1.0
     */
    public void removeChild(int index) {
      try {
        this.removeChild(this.getChild(index));
      }
      catch (Exception exception) {
        // Do nothing...
      }
    }

    /**
     * Removes the given child from this element. Nothing is done if the child
     * element doesn't belong to this element.
     * 
     * @param child
     *          The element to be removed.
     * @since 1.0
     */
    public void removeChild(ScoutXmlElement child) {
      try {
        if (m_content.equals(child)) m_content = null;
        else ((List) m_content).remove(child);
      }
      catch (Exception exception) {
        // Do nothing...
      }
    }

    /**
     * Removes the given child from this element. Nothing is done if the child
     * element can't be found.
     * 
     * @param child
     *          The element to be removed.
     * @since 1.0
     */
    public void removeChild(String name) {
      try {
        this.removeChild(this.getChild(name));
      }
      catch (Exception exception) {
        // Do nothing...
      }
    }

    /**
     * Removes all children of this element.
     * 
     * @since 1.0
     */
    public void removeChildren() {
      m_content = null;
    }

    /**
     * Removes the given children.
     * 
     * @param children
     * @since 1.0
     */
    public void removeChildren(Collection children) {
      try {
        ((List) m_content).removeAll(children);
      }
      catch (Exception exception) {
        // Do nothing...
      }
    }

    /**
     * Removes all children which match the given name.
     * 
     * @param name
     * @since 1.0
     */
    public void removeChildren(String name) {
      try {
        this.removeChildren(this.getChildren(name));
      }
      catch (Exception exception) {
        // Do nothing...
      }
    }

    /**
     * Removes all content of this element. This includes text and children.
     * 
     * @since 1.0
     */
    public void removeContent() {
      m_content = null;
    }

    /**
     * @since 1.0
     */
    public void removeNamespace(String prefix) {
      if (this.hasNamespace(prefix)) m_namespaces.remove(prefix);
      else LOG.warn("Namespace with prefix '" + prefix + "' not removed, it doesn't exist for element '" + this.getNamePrefixed() + "'.");
    }

    /**
     * @since 1.0
     */
    public void removeNamespaces() {
      m_namespaces = null;
    }

    /**
     * Removes this element's text.
     * 
     * @since 1.0
     */
    public void removeText() {
      if (m_content instanceof Integer) {
        m_content = null;
      }
      else if (m_content instanceof String) {
        m_content = null;
      }
      else if (m_content instanceof List) {
        for (int i = 0; i < ((List) m_content).size(); i++) {
          Object contentPart = ((List) m_content).get(i);

          if (contentPart instanceof String) ((List) m_content).remove(contentPart);
          else if (contentPart instanceof Integer) ((List) m_content).remove(contentPart);
        }
      }
    }

    /**
     * Adds a new attribute or modifies an existing one.
     * 
     * @param name
     *          The name of the attribute to create or modify.
     * @param value
     *          The value to be set.
     * @since 1.0
     */
    public void setAttribute(String name, boolean value) {
      this.setAttribute(name, String.valueOf(value));
    }

    /**
     * Adds a new attribute or modifies an existing one.
     * 
     * @param name
     *          The name of the attribute to create or modify.
     * @param value
     *          The value to be set.
     * @since 1.1
     */
    public void setAttribute(String name, Date value, String dateFormat) {
      this.setAttribute(name, new SimpleDateFormat(dateFormat).format(value));
    }

    /**
     * Adds a new attribute or modifies an existing one.
     * 
     * @param name
     *          The name of the attribute to create or modify.
     * @param value
     *          The value to be set.
     * @since 1.0
     */
    public void setAttribute(String name, double value) {
      this.setAttribute(name, String.valueOf(value));
    }

    /**
     * Adds a new attribute or modifies an existing one.
     * 
     * @param name
     *          The name of the attribute to create or modify.
     * @param value
     *          The value to be set.
     * @since 1.0
     */
    public void setAttribute(String name, int value) {
      this.setAttribute(name, String.valueOf(value));
    }

    /**
     * Adds a new attribute or modifies an existing one.
     * 
     * @param name
     *          The name of the attribute to create or modify.
     * @param value
     *          The value to be set.
     * @since 1.0
     */
    public void setAttribute(String name, long value) {
      this.setAttribute(name, String.valueOf(value));
    }

    /**
     * Adds a new attribute or modifies an existing one.
     * 
     * @param name
     *          The name of the attribute to create or modify.
     * @param value
     *          The value to be set.
     * @since 1.0
     */
    public void setAttribute(String name, short value) {
      this.setAttribute(name, String.valueOf(value));
    }

    /**
     * Adds a new attribute or modifies an existing one.
     * 
     * @param name
     *          The name of the attribute to create or modify.
     * @param value
     *          The value to be set.
     * @since 1.0
     */
    public void setAttribute(String name, String value) {
      if (value != null) {
        if (m_attributes == null) m_attributes = new ArrayList(INITIAL_ATTRIBUTE_LIST_SIZE);

        P_Attribute newAttribute = new P_Attribute(name, value);

        if (ScoutXmlDocument.this.isStrictlyChecking() && this.hasAttribute(newAttribute.getNameExpanded())) this.getAttributeNode(newAttribute.getNameExpanded()).setValue(value);

        m_attributes.add(newAttribute);
      }
    }

    /**
     * @param attributes
     * @since 1.0
     */
    protected void setAttributes(Attributes attributes) {
      if (attributes.getLength() > 0) {
        if (m_attributes == null) m_attributes = new ArrayList(attributes.getLength());

        for (int i = 0; i < attributes.getLength(); i++)
          if ((attributes.getValue(i) != null) && (!attributes.getValue(i).equals(""))) m_attributes.add(new P_Attribute(attributes.getQName(i), attributes.getValue(i)));
      }
    }

    /**
     * @since 1.0
     */
    public void setNamespace(String prefix, String namespaceURI) {
      if (m_namespaces == null) m_namespaces = new Hashtable(INITIAL_NAMESPACE_MAP_SIZE);

      m_namespaces.put((prefix != null ? prefix : ""), namespaceURI);
    }

    /**
     * @since 1.0
     */
    protected void setNamespaces(Map namespaces) {
      if (m_namespaces == null) m_namespaces = new Hashtable(namespaces.size());

      m_namespaces.putAll(namespaces);
    }

    /**
     * @since 1.0
     */
    public void setParent(ScoutXmlElement parent) {
      m_parent = parent;
    }

    /**
     * @since 1.0
     */
    public String export() {
      try {
        StringWriter stringWriter = new StringWriter();
        this.write(stringWriter);
        return stringWriter.toString();
      }
      catch (IOException exception) {
        return null;
      }
    }

    /**
     * @since 1.0
     */
    @Override
    public String toString() {
      return export();
    }

    /**
     * @since 1.0
     */
    protected Object visitAttributes(P_AbstractNodeVisitor visitor) {
      if (this.hasAttributes()) {
        Iterator iterator = m_attributes.iterator();

        while (iterator.hasNext() && visitor.isNotDoneYet())
          visitor.visit((P_Attribute) iterator.next());

        return visitor.getResult();
      }
      else {
        return null;
      }
    }

    /**
     * @since 1.0
     */
    protected Object visitChildren(P_AbstractNodeVisitor visitor) {
      Iterator iterator = this.getChildren().iterator();

      while (iterator.hasNext() && visitor.isNotDoneYet())
        visitor.visit((ScoutXmlElement) iterator.next());

      return visitor.getResult();
    }

    /**
     * @since 1.0
     */
    protected Object visitDescendants(P_AbstractNodeVisitor visitor) {
      Iterator iterator = this.getChildren().iterator();
      ScoutXmlElement child = null;

      while (iterator.hasNext() && visitor.isNotDoneYet()) {
        child = (ScoutXmlElement) iterator.next();

        visitor.visit(child);
        child.visitDescendants(visitor);
      }

      return visitor.getResult();
    }

    /**
     * @since 1.0
     */
    @Override
    public void write(Writer writer) throws IOException {
      BufferedWriter bufferedWriter = null;

      if (writer instanceof BufferedWriter) bufferedWriter = (BufferedWriter) writer;
      else bufferedWriter = new BufferedWriter(writer);

      this.write(bufferedWriter, "");
      bufferedWriter.flush();
    }

    /**
     * @since 1.0
     */
    private void write(BufferedWriter bufferedWriter, String currentIndent) throws IOException {
      if (this.hasName()) {
        bufferedWriter.write(currentIndent);
        bufferedWriter.write('<');
        bufferedWriter.write(this.getNamePrefixed());

        if (this.hasAttributes()) {
          Iterator iterator = m_attributes.iterator();

          while (iterator.hasNext()) {
            bufferedWriter.write(' ');
            ((P_Attribute) iterator.next()).write(bufferedWriter);
          }
        }

        if (this.hasNamespace()) {
          Iterator iterator = m_namespaces.keySet().iterator();
          String prefix = null;

          while (iterator.hasNext()) {
            prefix = (String) iterator.next();

            bufferedWriter.write(" xmlns");
            if (!prefix.equals("")) {
              bufferedWriter.write(':');
              bufferedWriter.write(prefix);
            }
            bufferedWriter.write('=');
            bufferedWriter.write('"');
            this.writeEncoded(bufferedWriter, this.getNamespace(prefix));
            bufferedWriter.write('"');
          }
        }

        if (this.hasContent()) {
          bufferedWriter.write('>');

          if (m_content instanceof List) {
            if (ScoutXmlDocument.this.isPrettyPrint()) bufferedWriter.newLine();

            for (int i = 0; i < ((List) m_content).size(); i++) {
              Object o = ((List) m_content).get(i);

              if (o instanceof ScoutXmlElement) ((ScoutXmlElement) o).write(bufferedWriter, currentIndent + ScoutXmlDocument.this.getIndent());
              else if (o instanceof Integer) {
                bufferedWriter.write(currentIndent + ScoutXmlDocument.this.getIndent());
                this.writeEncoded(bufferedWriter, m_textRegistry.getValueAsString(((Integer) o).intValue()));

                if (ScoutXmlDocument.this.isPrettyPrint()) bufferedWriter.newLine();
              }
            }

            bufferedWriter.write(currentIndent);
          }
          else if (m_content instanceof ScoutXmlElement) {
            if (ScoutXmlDocument.this.isPrettyPrint()) bufferedWriter.newLine();

            ((ScoutXmlElement) m_content).write(bufferedWriter, currentIndent + ScoutXmlDocument.this.getIndent());
            bufferedWriter.write(currentIndent);
          }
          else if (m_content instanceof Integer) this.writeEncoded(bufferedWriter, m_textRegistry.getValueAsString((Integer) m_content));

          bufferedWriter.write('<');
          bufferedWriter.write('/');
          bufferedWriter.write(this.getNamePrefixed());
          bufferedWriter.write('>');
        }
        else {
          bufferedWriter.write('/');
          bufferedWriter.write('>');
        }

        if (ScoutXmlDocument.this.isPrettyPrint()) bufferedWriter.newLine();
      }
      else {
        throw new ScoutXmlException("Can't write this element because it has no name.");
      }
    }

    /**
     * @since 1.0
     */
    protected void writeEncoded(BufferedWriter bufferedWriter, String string) throws IOException {
      if (string != null) {
        for (int i = 0; i < string.length(); i++) {
          String ch = "" + string.charAt(i);
          String escaped = ScoutXmlDocument.XML_ENTITIES.get(ch);
          if (escaped != null) bufferedWriter.write(escaped);
          else bufferedWriter.write(ch);
        }
      }
    }

    /**
     * Title : Scout XML Attribute Description: Copyright : Copyright (c) 2006
     * BSI AG, ETH Zürich, Stefan Vogt Company : BSI AG www.bsiag.com
     * 
     * @version 1.0
     */
    private class P_Attribute extends P_AbstractNode {
      private int m_valueID;

      /**
       * @since 1.0
       */
      public P_Attribute(String name, String value) {
        super.setName(name);
        this.setValue(value);
      }

      /**
       * @since 1.0
       */
      @Override
      public boolean equals(Object object) {
        try {
          P_Attribute attribute = (P_Attribute) object;
          return this.getName().equals(attribute.getName()) && this.getValueAsString().equals(attribute.getValueAsString());
        }
        catch (Exception exception) {
          return false;
        }
      }

      @Override
      public int hashCode() {
        return this.getName().hashCode() ^ this.getValueAsString().hashCode();
      }

      /**
       * @since 1.0
       */
      public ScoutXmlDocument getDocument() {
        return ScoutXmlDocument.this;
      }

      /**
       * @since 1.0
       */
      @Override
      public ScoutXmlQName getName() {
        String name = m_nameRegistry.getValueAsString(m_nameID);

        String prefix = ScoutXmlQName.extractPrefix(name);
        String localName = ScoutXmlQName.extractLocalName(name);

        return new ScoutXmlQName(this.getNamespace(prefix), prefix, localName);
      }

      /**
       * @since 1.0
       */
      @Override
      public String getNamespace(String prefix) {
        return ScoutXmlElement.this.getNamespace(prefix);
      }

      /**
       * @since 1.0
       */
      public Object getValue() {
        return m_nameRegistry.getValue(m_valueID);
      }

      /**
       * @since 1.0
       */
      public boolean getValueAsBoolean() {
        return Boolean.valueOf(this.getValueAsString()).booleanValue();
      }

      /**
       * @since 1.0
       */
      public Date getValueAsDate(String format) {
        try {
          return new SimpleDateFormat(format).parse(this.getValueAsString());
        }
        catch (Exception exception) {
          throw new ScoutXmlException("Attribute value '" + this.getValueAsString() + "' is not a valid 'Date' value (" + ScoutXmlElement.this.getPath() + "/@" + this.getNameExpanded() + ")");
        }
      }

      /**
       * @since 1.0
       */
      public double getValueAsDouble() {
        try {
          return Double.valueOf(this.getValueAsString()).doubleValue();
        }
        catch (NumberFormatException exception) {
          throw new ScoutXmlException("Attribute value '" + this.getValueAsString() + "' is not a valid 'double' value (" + ScoutXmlElement.this.getPath() + "/@" + this.getNameExpanded() + ")");
        }
      }

      /**
       * @since 1.0
       */
      public int getValueAsInt() {
        try {
          return Integer.valueOf(this.getValueAsString()).intValue();
        }
        catch (NumberFormatException exception) {
          throw new ScoutXmlException("Attribute value '" + this.getValueAsString() + "' is not a valid 'int' value (" + ScoutXmlElement.this.getPath() + "/@" + this.getNameExpanded() + ")");
        }
      }

      /**
       * @since 1.0
       */
      public long getValueAsLong() {
        try {
          return Long.valueOf(this.getValueAsString()).longValue();
        }
        catch (NumberFormatException exception) {
          throw new ScoutXmlException("Attribute value '" + this.getValueAsString() + "' is not a valid 'long' value (" + ScoutXmlElement.this.getPath() + "/@" + this.getNameExpanded() + ")");
        }
      }

      /**
       * @since 1.0
       */
      public short getValueAsShort() {
        try {
          return Short.valueOf(this.getValueAsString()).shortValue();
        }
        catch (NumberFormatException exception) {
          throw new ScoutXmlException("Attribute value '" + this.getValueAsString() + "' is not a valid 'short' value (" + ScoutXmlElement.this.getPath() + "/@" + this.getNameExpanded() + ")");
        }
      }

      /**
       * @since 1.0
       */
      public String getValueAsString() {
        return m_attvRegistry.getValueAsString(m_valueID);
      }

      /**
       * @since 1.0
       */
      public void setValue(String value) {
        m_valueID = m_attvRegistry.getID(value, true);
      }

      /**
       * @since 1.0
       */
      @Override
      public String toString() {
        try {
          StringWriter stringWriter = new StringWriter();
          this.write(stringWriter);
          return stringWriter.toString();
        }
        catch (IOException exception) {
          return null;
        }
      }

      /**
       * @since 1.0
       */
      @Override
      public void write(Writer writer) throws IOException {
        BufferedWriter bufferedWriter = null;

        if (writer instanceof BufferedWriter) bufferedWriter = (BufferedWriter) writer;
        else bufferedWriter = new BufferedWriter(writer);

        bufferedWriter.write(m_nameRegistry.getValueAsString(m_nameID));
        bufferedWriter.write('=');
        bufferedWriter.write('"');
        ScoutXmlElement.this.writeEncoded(bufferedWriter, this.getValueAsString());
        bufferedWriter.write('"');

        bufferedWriter.flush();
      }
    }
  }
}
