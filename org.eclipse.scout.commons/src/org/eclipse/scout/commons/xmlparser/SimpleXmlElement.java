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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.osgi.ContextFinderBasedObjectInputStream;

/**
 * Simple xml parser/writer. Very efficient and performant when handling xml
 * content without care of namespaces.
 */
public class SimpleXmlElement {
  private static final HashMap<String, String> ENTITIES;
  private static final HashMap<String, String> INVERSE_ENTITIES;

  private TreeMap<String, String> m_attributeMap;
  private ArrayList<String> m_attributeNames;
  private SimpleXmlElement m_parent;
  private ArrayList<SimpleXmlElement> m_children;
  private String m_name;
  private String m_contents;
  private int m_lineNr;
  private char m_charReadTooMuch;
  private Reader m_reader;
  private int m_parserLineNr;
  // flags
  private boolean m_ignoreCase;
  private boolean m_ignoreWhitespace;

  public SimpleXmlElement() {
    this((String) null);
  }

  public SimpleXmlElement(String name) {
    this(name, false, false);
  }

  public SimpleXmlElement(String name, boolean ignoreWhiteSpace, boolean ignoreCase) {
    m_name = name;
    m_ignoreCase = ignoreCase;
    m_ignoreWhitespace = ignoreWhiteSpace;
    m_contents = "";
    m_attributeMap = new TreeMap<String, String>();
    m_attributeNames = new ArrayList<String>();
    m_children = new ArrayList<SimpleXmlElement>();
    m_lineNr = 0;
  }

  public SimpleXmlElement(SimpleXmlElement x) {
    m_name = x.m_name;
    m_ignoreCase = x.m_ignoreCase;
    m_ignoreWhitespace = x.m_ignoreWhitespace;
    m_contents = x.m_contents;
    m_attributeMap = new TreeMap<String, String>(x.m_attributeMap);
    m_attributeNames = new ArrayList<String>(x.m_attributeNames);
    m_children = new ArrayList<SimpleXmlElement>();
    m_lineNr = 0;
  }

  public void setIgnoreWhitespace(boolean b) {
    m_ignoreWhitespace = b;
  }

  public boolean isIgnoreWhitespace() {
    return m_ignoreWhitespace;
  }

  public void setIgnoreCase(boolean b) {
    m_ignoreCase = b;
  }

  public boolean isIgnoreCase() {
    return m_ignoreCase;
  }

  public void addChild(SimpleXmlElement child) {
    if (child.m_parent != null) {
      child.m_parent.removeChild(child);
    }
    m_children.add(child);
    child.m_parent = this;
  }

  public void addChild(SimpleXmlElement child, int pos) {
    m_children.add(pos, child);
    child.m_parent = this;
  }

  public void replaceChild(SimpleXmlElement oldChild, SimpleXmlElement newChild) {
    if (newChild == null) {
      throw new IllegalArgumentException("newChild must not be null");
    }
    int index = m_children.indexOf(oldChild);
    if (index < 0) {
      throw new IllegalArgumentException("oldChild " + oldChild + " does not exist");
    }
    m_children.set(index, newChild);
    newChild.m_parent = this;
    oldChild.m_parent = null;
  }

  public void setAttribute(String name, Object value) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    setAttributeInternal(name, value != null ? value.toString() : null);
  }

  public void setIntAttribute(String name, int value) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    setAttributeInternal(name, Integer.toString(value));
  }

  public void setDoubleAttribute(String name, double value) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    setAttributeInternal(name, Double.toString(value));
  }

  public int countChildren() {
    return m_children.size();
  }

  public void clearAttributes() {
    m_attributeMap.clear();
    m_attributeNames.clear();
  }

  public void moveTo(int newIndex) {
    if (m_parent != null) {
      ArrayList<SimpleXmlElement> a = m_parent.m_children;
      int i = a.indexOf(this);
      if (i >= 0 && i != newIndex) {
        newIndex = Math.min(Math.max(0, newIndex), a.size() - 1);
        a.remove(i);
        a.add(newIndex, this);
      }
    }
  }

  public int getIndex() {
    if (m_parent != null) {
      ArrayList<SimpleXmlElement> a = m_parent.m_children;
      return a.indexOf(this);
    }
    return -1;
  }

  public ArrayList<String> getChildNames() {
    ArrayList<String> a = new ArrayList<String>();
    for (SimpleXmlElement e : getChildren()) {
      a.add(e.getName());
    }
    return a;
  }

  public ArrayList<SimpleXmlElement> getChildren() {
    return new ArrayList<SimpleXmlElement>(m_children);
  }

  public ArrayList<SimpleXmlElement> getChildren(String childrenName) {
    ArrayList<SimpleXmlElement> a = new ArrayList<SimpleXmlElement>();
    for (SimpleXmlElement c : m_children) {
      if (c.getName().equalsIgnoreCase(childrenName)) {
        a.add(c);
      }
    }
    return a;
  }

  /**
   * Returns an ArrayList with all children whose name matches the given regular
   * expression.
   */
  public ArrayList<SimpleXmlElement> getChildrenRegEx(String childNameAsRegEx) {
    ArrayList<SimpleXmlElement> selectedChildren = new ArrayList<SimpleXmlElement>();
    for (SimpleXmlElement child : m_children) {
      if (child.getName().matches(childNameAsRegEx)) {
        selectedChildren.add(child);
      }
    }
    return selectedChildren;
  }

  public SimpleXmlElement getChild(String name) {
    for (SimpleXmlElement c : m_children) {
      if (c.getName().equalsIgnoreCase(name)) {
        return c;
      }
    }
    return null;
  }

  /**
   * Returns the first child that matches the given regular expression or null
   * if there's no match.
   */
  public SimpleXmlElement getChildRegEx(String childNameAsRegEx) {
    Iterator<SimpleXmlElement> iterator = m_children.iterator();

    while (iterator.hasNext()) {
      SimpleXmlElement child = iterator.next();

      if (child.getName().matches(childNameAsRegEx)) {
        return child;
      }
    }

    return null;
  }

  public String getContent() {
    return m_contents;
  }

  public int getLineNr() {
    return m_lineNr;
  }

  public Object getAttribute(String name) {
    return this.getAttribute(name, null);
  }

  public boolean hasAttribute(String name) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    return m_attributeMap.containsKey(name);
  }

  public Map<String, String> getAttributes() {
    return new HashMap<String, String>(m_attributeMap);
  }

  /**
   * Returns the first attribute that matches the given regular expression or
   * null if there's no match.
   */
  public Object getAttributeRegEx(String attributeNameAsRegEx) {
    Iterator<String> iterator = m_attributeMap.keySet().iterator();

    while (iterator.hasNext()) {
      String attributeName = iterator.next();

      if (attributeName.matches(attributeNameAsRegEx)) {
        return this.getAttribute(attributeName);
      }
    }

    return null;
  }

  public Object getAttribute(String name, Object defaultValue) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    Object value = getAttributeInternal(name);
    if (value == null || ("" + value).length() == 0) {
      value = defaultValue;
    }
    return value;
  }

  public Object getAttribute(String name, HashMap<Object, Object> valueSet, String defaultKey, boolean allowLiterals) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    Object key = getAttributeInternal(name);
    Object result;
    if (key == null) {
      key = defaultKey;
    }
    result = valueSet.get(key);
    if (result == null) {
      if (allowLiterals) {
        result = key;
      }
      else {
        throw this.invalidValue(name, (String) key);
      }
    }
    return result;
  }

  public String getStringAttribute(String name) {
    return this.getStringAttribute(name, null);
  }

  public String getStringAttribute(String name, String defaultValue) {
    return (String) this.getAttribute(name, defaultValue);
  }

  public String getStringAttribute(String name, HashMap<Object, Object> valueSet, String defaultKey, boolean allowLiterals) {
    return (String) this.getAttribute(name, valueSet, defaultKey, allowLiterals);
  }

  public int getIntAttribute(String name) {
    return this.getIntAttribute(name, 0);
  }

  public int getIntAttribute(String name, int defaultValue) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    String value = getAttributeInternal(name);
    if (value == null) {
      return defaultValue;
    }
    else {
      try {
        return Integer.parseInt(value);
      }
      catch (NumberFormatException e) {
        throw this.invalidValue(name, value);
      }
    }
  }

  public Long getLongAttribute(String name) {
    String value = getAttributeInternal(name);
    if (value == null) {
      return null;
    }
    else {
      return new Long(Long.parseLong(value));
    }
  }

  public Object getObjectAttribute(String name, Object defaultValue) throws IOException, ClassNotFoundException {
    return getObjectAttribute(name, defaultValue, null);
  }

  /**
   * @return a serialized object attribute from base64 serialized data using {@link ContextFinderBasedObjectInputStream}
   *         that tries
   *         to find the class using default osgi class loading and in a
   *         second stage using the caller classes class loaders.
   */
  public Object getObjectAttribute(String name, Object defaultValue, ClassLoader primaryLoader) throws IOException, ClassNotFoundException {
    String base64 = getStringAttribute(name, "");
    if (base64.length() <= 0) {
      return defaultValue;
    }
    byte[] raw = Base64Utility.decode(base64);
    ContextFinderBasedObjectInputStream oi = new ContextFinderBasedObjectInputStream(new ByteArrayInputStream(raw), primaryLoader);
    Object o = oi.readObject();
    if (o == null) {
      o = defaultValue;
    }
    oi.close();
    return o;
  }

  /**
   * write a serialized object attribute
   */
  public void setObjectAttribute(String name, Object o) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bout);
    out.writeObject(o);
    out.close();
    String base64 = Base64Utility.encode(bout.toByteArray()).trim();
    this.setAttribute(name, base64);
  }

  public int getIntAttribute(String name, HashMap<Object, Integer> valueSet, String defaultKey, boolean allowLiteralNumbers) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    Object key = getAttributeInternal(name);
    Integer result;
    if (key == null) {
      key = defaultKey;
    }
    try {
      result = valueSet.get(key);
    }
    catch (ClassCastException e) {
      throw this.invalidValueSet(name);
    }
    if (result == null) {
      if (!allowLiteralNumbers) {
        throw this.invalidValue(name, (String) key);
      }
      try {
        result = Integer.valueOf((String) key);
      }
      catch (NumberFormatException e) {
        throw this.invalidValue(name, (String) key);
      }
    }
    return result.intValue();
  }

  public double getDoubleAttribute(String name) {
    return this.getDoubleAttribute(name, 0.0);
  }

  public double getDoubleAttribute(String name, double defaultValue) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    String value = getAttributeInternal(name);
    if (value == null) {
      return defaultValue;
    }
    else {
      try {
        return Double.valueOf(value).doubleValue();
      }
      catch (NumberFormatException e) {
        throw this.invalidValue(name, value);
      }
    }
  }

  public double getDoubleAttribute(String name, Map<Object, Double> valueSet, String defaultKey, boolean allowLiteralNumbers) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    Object key = getAttributeInternal(name);
    Double result;
    if (key == null) {
      key = defaultKey;
    }
    try {
      result = valueSet.get(key);
    }
    catch (ClassCastException e) {
      throw this.invalidValueSet(name);
    }
    if (result == null) {
      if (!allowLiteralNumbers) {
        throw this.invalidValue(name, (String) key);
      }
      try {
        result = Double.valueOf((String) key);
      }
      catch (NumberFormatException e) {
        throw this.invalidValue(name, (String) key);
      }
    }
    return result.doubleValue();
  }

  public boolean getBooleanAttribute(String name, String trueValue, String falseValue, boolean defaultValue) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    Object value = getAttributeInternal(name);
    if (value == null) {
      return defaultValue;
    }
    else if (value.equals(trueValue)) {
      return true;
    }
    else if (value.equals(falseValue)) {
      return false;
    }
    else {
      throw this.invalidValue(name, (String) value);
    }
  }

  public String getName() {
    return m_name;
  }

  public String getTagName() {
    return this.getName();
  }

  public void parseStream(InputStream in) throws IOException {
    parseStream(in, 1);
  }

  public void parseStream(InputStream input, int startingLineNr) throws IOException {
    parseStream(input, startingLineNr, true);
  }

  public void parseStream(InputStream input, int startingLineNr, boolean autoClose) throws IOException {
    // check input encoding if present
    removeChildren();
    BufferedInputStream bin;
    if (input instanceof BufferedInputStream) {
      bin = (BufferedInputStream) input;
    }
    else {
      bin = new BufferedInputStream(input);
    }
    bin.mark(1024);
    BufferedReader encodedReader = null;
    int ch = bin.read();
    while (ch >= 0 && (ch == '\n' || ch == '\t' || ch == ' ' || ch == '\r')) {
      ch = bin.read();
    }
    if (ch == '<') {
      ch = bin.read();
      if (ch == '?') {
        StringBuffer buf = new StringBuffer("<?");
        ch = bin.read();
        while (ch >= 0 && ch != '?') {
          buf.append((char) ch);
          ch = bin.read();
        }
        if (ch == '?') {
          buf.append((char) ch);
        }
        ch = bin.read();
        if (ch == '>') {
          buf.append((char) ch);
        }
        String xmlLine = buf.toString();
        int k = xmlLine.indexOf("encoding");
        if (k >= 0) {
          k = xmlLine.indexOf('=', k);
          if (k >= 0) {
            k = xmlLine.indexOf('"', k);
            if (k >= 0) {
              int m = xmlLine.indexOf('"', k + 1);
              if (m >= 0) {
                String encoding = xmlLine.substring(k + 1, m);
                // replace input reader
                bin.reset();
                encodedReader = new BufferedReader(new InputStreamReader(bin, encoding));
              }
            }
          }
        }
      }
    }
    // do not close bin
    if (encodedReader == null) {
      bin.reset();
      encodedReader = new BufferedReader(new InputStreamReader(bin));
    }
    try {
      parseImpl(encodedReader, 1);
    }
    finally {
      if (autoClose) {
        encodedReader.close();
      }
    }
  }

  protected void parseImpl(Reader r, int startingLineNr) throws IOException {
    m_name = null;
    m_contents = "";
    m_attributeMap = new TreeMap<String, String>();
    m_attributeNames = new ArrayList<String>();
    m_children = new ArrayList<SimpleXmlElement>();
    m_charReadTooMuch = '\0';
    m_reader = r;
    m_parserLineNr = startingLineNr;

    for (;;) {
      char ch = this.scanWhitespace();

      if (ch != '<') {
        throw this.expectedInput("<");
      }

      ch = this.readChar();

      if ((ch == '!') || (ch == '?')) {
        this.skipSpecialTag(0);
      }
      else {
        this.unreadChar(ch);
        this.scanElement(this);
        return;
      }
    }
  }

  public void parseReader(Reader r) throws IOException {
    parseImpl(r, 1);
  }

  public void parseString(String string) {
    try {
      this.parseImpl(new StringReader(string), 1);
    }
    catch (IOException e) {
      /* nop */
    }
  }

  public void parseString(String string, int offset) {
    this.parseString(string.substring(offset));
  }

  public void parseString(String string, int offset, int end) {
    this.parseString(string.substring(offset, end));
  }

  public void parseString(String string, int offset, int end, int startingLineNr) {
    string = string.substring(offset, end);
    try {
      this.parseImpl(new StringReader(string), startingLineNr);
    }
    catch (IOException e) {
      // Java exception handling suxx
    }
  }

  public void parseCharArray(char[] input, int offset, int end) {
    this.parseCharArray(input, offset, end, 1);
  }

  public void parseCharArray(char[] input, int offset, int end, int startingLineNr) {
    try {
      Reader reader = new CharArrayReader(input, offset, end);
      parseImpl(reader, startingLineNr);
    }
    catch (IOException e) {
      // This exception will never happen.
    }
  }

  public SimpleXmlElement getParent() {
    return m_parent;
  }

  public boolean isAncestorOf(SimpleXmlElement child) {
    SimpleXmlElement e = child;
    while (e != null) {
      if (e == this) {
        return true;
      }
      e = e.getParent();
    }
    return false;
  }

  public SimpleXmlElement getRoot() {
    if (m_parent == null) {
      return this;
    }
    else {
      return m_parent.getRoot();
    }
  }

  public void removeChild(SimpleXmlElement child) {
    m_children.remove(child);
    child.m_parent = null;
  }

  public void removeChildren() {
    m_children.clear();
  }

  public void removeChildren(String tagName) {
    for (Iterator<SimpleXmlElement> it = m_children.iterator(); it.hasNext();) {
      SimpleXmlElement e = it.next();
      if (e.getName().equalsIgnoreCase(tagName)) {
        it.remove();
      }
    }
  }

  public void removeAttribute(String name) {
    if (m_ignoreCase) {
      name = name.toUpperCase();
    }
    removeAttributeInternal(name);
  }

  protected SimpleXmlElement createAnotherElement() {
    return new SimpleXmlElement(null, m_ignoreWhitespace, m_ignoreCase);
  }

  public void setContent(String content) {
    m_contents = content;
  }

  public void setTagName(String name) {
    this.setName(name);
  }

  public void setName(String name) {
    m_name = name;
  }

  // important: make toString different if tags different
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("<");
    buf.append(getName());
    for (String key : m_attributeNames) {
      String val = "" + getAttributeInternal(key);
      buf.append(" ");
      buf.append(key + "=\"" + val + "\"");
    }
    buf.append("]");
    return buf.toString();
  }

  public void writeDocument(Writer w, String dtdName, String encoding) throws IOException {
    writeDocumentImpl(w, dtdName, encoding);
  }

  public void writeDocument(OutputStream out, String dtdName, String encoding) throws IOException {
    if (encoding != null) {
      writeDocumentImpl(new BufferedWriter(new OutputStreamWriter(out, encoding)), dtdName, encoding);
    }
    else {
      writeDocumentImpl(new BufferedWriter(new OutputStreamWriter(out)), dtdName, encoding);
    }
  }

  protected void writeDocumentImpl(Writer w, String dtdName, String encoding) throws IOException {
    try {
      if (encoding != null) {
        w.write("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n");
      }
      else {
        w.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
      }
      if (dtdName != null) {
        w.write("<!DOCTYPE app SYSTEM \"" + dtdName + "\">\n");
      }
      write(w, "");
    }
    finally {
      w.close();
    }
  }

  protected void write(Writer writer, String prefix) throws IOException {
    if (m_name == null) {
      this.writeEncoded(writer, m_contents);
      return;
    }
    writer.write(prefix + '<');
    writer.write(m_name);
    if (!m_attributeMap.isEmpty()) {
      for (String key : m_attributeNames) {
        String value = getAttributeInternal(key);
        if (value != null) {
          writer.write(' ');
          writer.write(key);
          writer.write('=');
          writer.write('"');
          this.writeEncoded(writer, value);
          writer.write('"');
        }
      }
    }
    if (!m_children.isEmpty()) {
      writer.write('>');
      writer.write("\n");
      Iterator<?> en = this.getChildren().iterator();
      while (en.hasNext()) {
        SimpleXmlElement child = (SimpleXmlElement) en.next();
        child.write(writer, prefix + "  ");
      }
      writer.write(prefix + '<');
      writer.write('/');
      writer.write(m_name);
      writer.write('>');
      writer.write("\n");
    }
    else if ((m_contents != null) && (m_contents.length() > 0)) {
      writer.write('>');
      this.writeEncoded(writer, m_contents);
      writer.write('<');
      writer.write('/');
      writer.write(m_name);
      writer.write('>');
      writer.write("\n");
    }
    else {// this.children.isEmpty()
      writer.write('/');
      writer.write('>');
      writer.write("\n");
    }
  }

  protected void writeEncoded(Writer writer, String str) throws IOException {
    if (str == null) {
      return;
    }
    for (int i = 0; i < str.length(); i += 1) {
      String ch = "" + str.charAt(i);
      String escaped = INVERSE_ENTITIES.get(ch);
      if (escaped != null) {
        writer.write(escaped);
      }
      else {
        writer.write(ch);
      }
    }
  }

  protected void scanIdentifier(StringBuffer result) throws IOException {
    for (;;) {
      char ch = this.readChar();
      if (((ch < 'A') || (ch > 'Z')) && ((ch < 'a') || (ch > 'z')) && ((ch < '0') || (ch > '9')) && (ch != '_') && (ch != '.') && (ch != ':') && (ch != '-') && (ch <= '\u007E')) {
        this.unreadChar(ch);
        return;
      }
      result.append(ch);
    }
  }

  protected char scanWhitespace() throws IOException {
    for (;;) {
      char ch = this.readChar();
      switch (ch) {
        case ' ':
        case '\t':
        case '\n':
        case '\r':
          break;
        default:
          return ch;
      }
    }
  }

  protected char scanWhitespace(StringBuffer result) throws IOException {
    for (;;) {
      char ch = this.readChar();
      switch (ch) {
        case ' ':
        case '\t':
        case '\n':
          result.append(ch);
        case '\r':
          break;
        default:
          return ch;
      }
    }
  }

  protected void scanString(StringBuffer string) throws IOException {
    char delimiter = this.readChar();
    if ((delimiter != '\'') && (delimiter != '"')) {
      throw this.expectedInput("' or \"");
    }
    for (;;) {
      char ch = this.readChar();
      if (ch == delimiter) {
        return;
      }
      else if (ch == '&') {
        this.resolveEntity(string);
      }
      else {
        string.append(ch);
      }
    }
  }

  protected void scanPCData(StringBuffer data) throws IOException {
    for (;;) {
      char ch = this.readChar();
      if (ch == '<') {
        ch = this.readChar();
        if (ch == '!') {
          this.checkCDATA(data);
        }
        else {
          this.unreadChar(ch);
          return;
        }
      }
      else if (ch == '&') {
        this.resolveEntity(data);
      }
      else {
        data.append(ch);
      }
    }
  }

  protected boolean checkCDATA(StringBuffer buf) throws IOException {
    char ch = this.readChar();
    if (ch != '[') {
      this.unreadChar(ch);
      this.skipSpecialTag(0);
      return false;
    }
    else if (!this.checkLiteral("CDATA[")) {
      this.skipSpecialTag(1);
      // one [ has already been read
      return false;
    }
    else {
      int delimiterCharsSkipped = 0;
      while (delimiterCharsSkipped < 3) {
        ch = this.readChar();
        switch (ch) {
          case ']':
            if (delimiterCharsSkipped < 2) {
              delimiterCharsSkipped += 1;
            }
            else {// 2,3,...
              // delimiterCharsSkipped=delimiterCharsSkipped+1-1 add current [
              // and remove first [
              buf.append(']');// add first [ to buffer
            }
            break;
          case '>':
            if (delimiterCharsSkipped < 2) {
              for (int i = 0; i < delimiterCharsSkipped; i++) {
                buf.append(']');
              }
              delimiterCharsSkipped = 0;
              buf.append('>');
            }
            else {
              delimiterCharsSkipped = 3;
            }
            break;
          default:
            for (int i = 0; i < delimiterCharsSkipped; i += 1) {
              buf.append(']');
            }
            buf.append(ch);
            delimiterCharsSkipped = 0;
        }
      }
      return true;
    }
  }

  protected void skipComment() throws IOException {
    int dashesToRead = 2;
    while (dashesToRead > 0) {
      char ch = this.readChar();
      if (ch == '-') {
        dashesToRead -= 1;
      }
      else {
        dashesToRead = 2;
      }
    }
    if (this.readChar() != '>') {
      throw this.expectedInput(">");
    }
  }

  protected void skipSpecialTag(int bracketLevel) throws IOException {
    int tagLevel = 1;
    // <
    char stringDelimiter = '\0';
    if (bracketLevel == 0) {
      char ch = this.readChar();
      if (ch == '[') {
        bracketLevel += 1;
      }
      else if (ch == '-') {
        ch = this.readChar();
        if (ch == '[') {
          bracketLevel += 1;
        }
        else if (ch == ']') {
          bracketLevel -= 1;
        }
        else if (ch == '-') {
          this.skipComment();
          return;
        }
      }
    }
    while (tagLevel > 0) {
      char ch = this.readChar();
      if (stringDelimiter == '\0') {
        if ((ch == '"') || (ch == '\'')) {
          stringDelimiter = ch;
        }
        else if (bracketLevel <= 0) {
          if (ch == '<') {
            tagLevel += 1;
          }
          else if (ch == '>') {
            tagLevel -= 1;
          }
        }
        if (ch == '[') {
          bracketLevel += 1;
        }
        else if (ch == ']') {
          bracketLevel -= 1;
        }
      }
      else {
        if (ch == stringDelimiter) {
          stringDelimiter = '\0';
        }
      }
    }
  }

  protected boolean checkLiteral(String literal) throws IOException {
    int length = literal.length();
    for (int i = 0; i < length; i += 1) {
      if (this.readChar() != literal.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  protected char readChar() throws IOException {
    if (m_charReadTooMuch != '\0') {
      char ch = m_charReadTooMuch;
      m_charReadTooMuch = '\0';
      return ch;
    }
    else {
      int i = m_reader.read();
      if (i < 0) {
        throw this.unexpectedEndOfData();
      }
      else if (i == 10) {
        m_parserLineNr += 1;
        return '\n';
      }
      else {
        return (char) i;
      }
    }
  }

  protected void scanElement(SimpleXmlElement elt) throws IOException {
    StringBuffer buf = new StringBuffer();
    this.scanIdentifier(buf);
    String eltName = buf.toString();
    elt.setName(eltName);
    char ch = this.scanWhitespace();
    while ((ch != '>') && (ch != '/')) {
      buf.setLength(0);
      this.unreadChar(ch);
      this.scanIdentifier(buf);
      String key = buf.toString();
      ch = this.scanWhitespace();
      if (ch != '=') {
        throw this.expectedInput("=");
      }
      this.unreadChar(this.scanWhitespace());
      buf.setLength(0);
      this.scanString(buf);
      elt.setAttribute(key, buf);
      ch = this.scanWhitespace();
    }
    if (ch == '/') {
      ch = this.readChar();
      if (ch != '>') {
        throw this.expectedInput(">");
      }
      return;
    }
    buf.setLength(0);
    ch = this.scanWhitespace(buf);
    if (ch != '<') {
      this.unreadChar(ch);
      this.scanPCData(buf);
    }
    else {
      for (;;) {
        ch = this.readChar();
        if (ch == '!') {
          if (this.checkCDATA(buf)) {
            this.scanPCData(buf);
            break;
          }
          else {
            ch = this.scanWhitespace(buf);
            if (ch != '<') {
              this.unreadChar(ch);
              this.scanPCData(buf);
              break;
            }
          }
        }
        else {
          if ((ch != '/') || m_ignoreWhitespace) {
            buf.setLength(0);
          }
          if (ch == '/') {
            this.unreadChar(ch);
          }
          break;
        }
      }
    }
    if (buf.length() == 0) {
      while (ch != '/') {
        if (ch == '!') {
          ch = this.readChar();
          if (ch != '-') {
            throw this.expectedInput("Comment or Element");
          }
          ch = this.readChar();
          if (ch != '-') {
            throw this.expectedInput("Comment or Element");
          }
          this.skipComment();
        }
        else {
          this.unreadChar(ch);
          SimpleXmlElement child = this.createAnotherElement();
          this.scanElement(child);
          elt.addChild(child);
        }
        ch = this.scanWhitespace();
        if (ch != '<') {
          throw this.expectedInput("<");
        }
        ch = this.readChar();
      }
      this.unreadChar(ch);
    }
    else {
      if (m_ignoreWhitespace) {
        elt.setContent(buf.toString().trim());
      }
      else {
        elt.setContent(buf.toString());
      }
    }
    ch = this.readChar();
    if (ch != '/') {
      throw this.expectedInput("/");
    }
    this.unreadChar(this.scanWhitespace());
    if (!this.checkLiteral(eltName)) {
      throw this.expectedInput(eltName);
    }
    if (this.scanWhitespace() != '>') {
      throw this.expectedInput(">");
    }
  }

  protected void resolveEntity(StringBuffer buf) throws IOException {
    char ch = '\0';
    StringBuffer keyBuf = new StringBuffer();
    for (;;) {
      ch = this.readChar();
      if (ch == ';') {
        break;
      }
      keyBuf.append(ch);
    }
    String key = keyBuf.toString();
    if (key.charAt(0) == '#') {
      try {
        if (key.charAt(1) == 'x') {
          ch = (char) Integer.parseInt(key.substring(2), 16);
        }
        else {
          ch = (char) Integer.parseInt(key.substring(1), 10);
        }
      }
      catch (NumberFormatException e) {
        throw this.unknownEntity(key);
      }
      buf.append(ch);
    }
    else {
      String value = ENTITIES.get(key);
      if (value == null) {
        System.out.println(getClass().getSimpleName() + ": unknownEntity " + key);
        value = "?";
      }
      buf.append(value);
    }
  }

  protected void unreadChar(char ch) {
    m_charReadTooMuch = ch;
  }

  protected IllegalArgumentException invalidValueSet(String name) {
    String msg = "Invalid value set (entity name = \"" + name + "\")";
    return new IllegalArgumentException(this.getName() + ": " + msg);
  }

  protected IllegalArgumentException invalidValue(String name, String value) {
    String msg = "Attribute \"" + name + "\" does not contain a valid " + "value (\"" + value + "\")";
    return new IllegalArgumentException(this.getName() + ": " + msg);
  }

  protected IOException unexpectedEndOfData() {
    String msg = "Unexpected end of data reached";
    return new IOException(this.getName() + ": " + msg);
  }

  protected IOException syntaxError(String context) {
    String msg = "Syntax error while parsing " + context;
    return new IOException(this.getName() + ": " + msg);
  }

  protected IOException expectedInput(String charSet) {
    String msg = "Expected: " + charSet;
    return new IOException(this.getName() + ": " + msg);
  }

  protected IOException unknownEntity(String name) {
    String msg = "Unknown or invalid entity: &" + name + ";";
    return new IOException(this.getName() + ": " + msg);
  }

  private void setAttributeInternal(String name, String val) {
    if (!m_attributeNames.contains(name)) {
      m_attributeNames.add(name);
    }
    m_attributeMap.put(name, val);
  }

  private String getAttributeInternal(String name) {
    return m_attributeMap.get(name);
  }

  private void removeAttributeInternal(String name) {
    m_attributeNames.remove(name);
    m_attributeMap.remove(name);
  }

  static {
    ENTITIES = new HashMap<String, String>();
    ENTITIES.put("amp", "&");
    ENTITIES.put("quot", "\"");
    ENTITIES.put("apos", "'");
    ENTITIES.put("lt", "<");
    ENTITIES.put("gt", ">");
    ENTITIES.put("auml", "ä");
    ENTITIES.put("ouml", "ö");
    ENTITIES.put("uuml", "ü");
    ENTITIES.put("Auml", "Ä");
    ENTITIES.put("Ouml", "Ö");
    ENTITIES.put("Uuml", "Ü");
    // reverse entities are used for exporting xml
    // normally ä,ö,ü are NOT excaped since encoding formats like ISO.. are used
    INVERSE_ENTITIES = new HashMap<String, String>();
    INVERSE_ENTITIES.put("&", "&amp;");
    INVERSE_ENTITIES.put("\"", "&quot;");
    INVERSE_ENTITIES.put("'", "&apos;");
    INVERSE_ENTITIES.put("<", "&lt;");
    INVERSE_ENTITIES.put(">", "&gt;");
  }
}
