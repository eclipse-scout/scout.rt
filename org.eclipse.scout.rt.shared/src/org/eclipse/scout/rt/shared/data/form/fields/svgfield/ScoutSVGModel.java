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
package org.eclipse.scout.rt.shared.data.form.fields.svgfield;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * The syntax of scout svg contains a basic set of elements to create scalable vector graphics. The graphics is
 * pixel-based an can be auto-scaled by using {@link #setAutoFit(boolean)}.
 * <p>
 * All supported elements are listed in the following example xml snippet. All elements are clickable in the ui.
 * Elements with interactive="true" have a link like behaviour in the gui (hand cursor).
 * 
 * <pre>
 * <xmp>
 * <scout-svg>
 *  <img id="logo" x="0" y="0" width="0" height="0" interactive="false|true"/>
 *  <text id="" x="0" y="0" align="center" font-size="8" font-weight="bold" color="argb" scale-up="true|false" interactive="false|true">Hello World</text>
 *  <point id="" x="0" y="0" color="argb" interactive="false|true"/>
 *  <rectangle id="" x="0" y="0" width="0" height="0" color="argb" border-color="argb" interactive="false|true"/>
 *  <circle id="" x="0" y="0" width="0" height="0" color="argb" border-color="argb" interactive="false|true"/>
 *  <polygon id="" color="argb" border-color="argb" interactive="false|true">
 *   <point x="0" y="0"/>
 *   <point x="0" y="0"/>
 *   <point x="0" y="0"/>
 *   <point x="0" y="0"/>
 *  </polygon>
 * </scout-svg>
 * </xmp>
 * </pre>
 * 
 * Note on the img element: The <i>id</i> attribute references any image resource in one of the bundles resources/icons
 * directory. Or if you need to use runtime images you can use the <i>resource</i> element to inline binary resources.
 * For example the following combination creates a local resource and then uses it as an image:
 * 
 * <pre>
 * <xmp>
 *  <resource id="flower.png" content-type="binary/base64">.......</resource>
 *  <img id="flower.png" x="0" y="0" width="0" height="0"/>
 * </xmp>
 * </pre>
 * 
 * To create such base64 content you may use {@link Base64Utility}
 */
public class ScoutSVGModel implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final HashMap<String, Class<?>> PARSE_MAP;
  static {
    PARSE_MAP = new HashMap<String, Class<?>>();
    PARSE_MAP.put("circle", CircleElement.class);
    PARSE_MAP.put("img", ImageElement.class);
    PARSE_MAP.put("polygon", PolygonElement.class);
    PARSE_MAP.put("point", PointElement.class);
    PARSE_MAP.put("rectangle", RectangleElement.class);
    PARSE_MAP.put("text", TextElement.class);
    PARSE_MAP.put("resource", ResourceElement.class);
  }

  /**
   * Convenience static parse see {@link #parseDocument(InputStream)}
   * 
   * @throws ProcessingException
   */
  public static ScoutSVGModel parse(InputStream in) throws ProcessingException {
    ScoutSVGModel model = new ScoutSVGModel();
    model.parseDocument(in);
    return model;
  }

  private List<IScoutSVGElement> m_graphicsElements;
  private Map<String/*id*/, ResourceElement> m_resources;

  public ScoutSVGModel() {
    m_graphicsElements = new ArrayList<IScoutSVGElement>();
    m_resources = new HashMap<String, ResourceElement>();
  }

  /**
   * @return the life list of all elements. Changes to this list are directly changed in the owner model.
   */
  public List<IScoutSVGElement> getGraphicsElements() {
    return m_graphicsElements;
  }

  /**
   * Set the life list with all elements.
   */
  public void setGraphicsElements(List<IScoutSVGElement> elements) {
    if (elements == null) {
      elements = new ArrayList<IScoutSVGElement>();
    }
    m_graphicsElements = elements;
  }

  /**
   * Add an element
   */
  public void addGraphicsElement(IScoutSVGElement element) {
    m_graphicsElements.add(element);
  }

  public IScoutSVGElement findGraphicalElement(String id) {
    if (id == null) {
      return null;
    }
    for (IScoutSVGElement elem : getGraphicsElements()) {
      if (id.equals(elem.getId())) {
        return elem;
      }
    }
    return null;
  }

  public Map<String, ResourceElement> getResources() {
    return m_resources;
  }

  public void setResources(Map<String, ResourceElement> resources) {
    if (resources == null) {
      resources = new HashMap<String, ResourceElement>();
    }
    m_resources = resources;
  }

  /**
   * Add a resource
   */
  public void addResource(ResourceElement r) {
    m_resources.put(r.getId(), r);
  }

  /**
   * parse and read scout svg xml using {@link SimpleXmlElement}
   * 
   * @throws ProcessingException
   */
  public void parseDocument(InputStream in) throws ProcessingException {
    //clear all
    getResources().clear();
    getGraphicsElements().clear();
    SimpleXmlElement doc = new SimpleXmlElement();
    try {
      doc.parseStream(in);
    }
    catch (IOException e) {
      throw new ProcessingException("parsing scout-svg", e);
    }
    readInternal(doc);
  }

  /**
   * export scout svg xml to simple xml using {@link SimpleXmlElement}
   * 
   * @throws ProcessingException
   */
  public void writeDocument(OutputStream out) throws ProcessingException {
    SimpleXmlElement doc = new SimpleXmlElement();
    writeInternal(doc);
    try {
      doc.writeDocument(out, null, "UTF-8");
    }
    catch (IOException e) {
      throw new ProcessingException("parsing scout-svg", e);
    }
  }

  private void readInternal(SimpleXmlElement doc) throws ProcessingException {
    for (SimpleXmlElement child : doc.getChildren()) {
      Class<?> clazz = PARSE_MAP.get(child.getName().toLowerCase());
      if (clazz == null) {
        throw new ProcessingException("unknown element '" + child.getName() + "'");
      }
      IScoutSVGElement e;
      try {
        e = (IScoutSVGElement) clazz.newInstance();
      }
      catch (Exception ex) {
        throw new ProcessingException("cannot create element '" + child.getName() + "' of type " + clazz.getName(), ex);
      }
      e.read(child);
      if (e instanceof ResourceElement) {
        addResource((ResourceElement) e);
      }
      else {
        addGraphicsElement(e);
      }
    }
  }

  private void writeInternal(SimpleXmlElement doc) throws ProcessingException {
    doc.setName("scout-svg");
    for (IScoutSVGElement e : getResources().values()) {
      SimpleXmlElement child = new SimpleXmlElement();
      e.write(child);
      doc.addChild(child);
    }
    for (IScoutSVGElement e : getGraphicsElements()) {
      SimpleXmlElement child = new SimpleXmlElement();
      e.write(child);
      doc.addChild(child);
    }
  }

}
