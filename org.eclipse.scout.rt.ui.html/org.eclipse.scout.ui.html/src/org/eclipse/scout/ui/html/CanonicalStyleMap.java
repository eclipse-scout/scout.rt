package org.eclipse.scout.ui.html;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.CSSDeclaration;
import com.phloc.css.decl.CSSExpression;
import com.phloc.css.decl.CSSSelector;
import com.phloc.css.decl.CSSStyleRule;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.writer.CSSWriter;
import com.phloc.css.writer.CSSWriterSettings;

public class CanonicalStyleMap {
  private Map<CSSSelector, Map<String, CSSExpression>> m_selectors = new LinkedHashMap<>();

  public void put(CSSStyleRule styleRule) {
    List<CSSDeclaration> properties = styleRule.getAllDeclarations();
    List<CSSSelector> selectors = styleRule.getAllSelectors();
    for (CSSSelector selector : selectors) {
      putInternal(selector, properties);
    }
  }

  private void putInternal(CSSSelector selector, List<CSSDeclaration> declarations) {
    Map<String, CSSExpression> propertyMap = m_selectors.get(selector);
    if (propertyMap == null) {
      propertyMap = new LinkedHashMap<>();
      m_selectors.put(selector, propertyMap);
    }

    for (CSSDeclaration declaration : declarations) {
      String property = declaration.getProperty();
      CSSExpression value = declaration.getExpression();
      propertyMap.put(property, value);
    }
  }

  public CascadingStyleSheet generateStyleSheet() throws IOException {
    CascadingStyleSheet styleSheet = new CascadingStyleSheet();
    for (Entry<CSSSelector, Map<String, CSSExpression>> entry : m_selectors.entrySet()) {
      CSSSelector selector = entry.getKey();

      CSSStyleRule styleRule = new CSSStyleRule();
      styleRule.addSelector(selector);
      styleSheet.addRule(styleRule);

      Map<String, CSSExpression> propertyMap = entry.getValue();
      for (Entry<String, CSSExpression> property : propertyMap.entrySet()) {
        CSSDeclaration declaration = new CSSDeclaration(property.getKey(), property.getValue(), false);
        styleRule.addDeclaration(declaration);
      }
    }
    return styleSheet;
  }

  public String generateStyleSheetText() throws IOException {
    CascadingStyleSheet styleSheet = generateStyleSheet();

    CSSWriterSettings settings = new CSSWriterSettings(ECSSVersion.CSS30, false);
    CSSWriter writer = new CSSWriter(settings);
    return writer.getCSSAsString(styleSheet);
  }

}
