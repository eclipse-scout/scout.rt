package org.eclipse.scout.migration.ecma6.task;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.AppNameContextProperty;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(200)
public class T200_IndexHtmlScriptTags extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T200_IndexHtmlScriptTags.class);

  private static Pattern END_BODY_REGEX = Pattern.compile("(\\s*)\\<\\/body\\>");

  private Predicate<PathInfo> m_indexJsFilter = PathFilters.oneOf(Paths.get("src/main/resources/WebContent/index.html"));

  private Set<String> m_scriptSourcesToRemove = CollectionUtility.hashSet(
      "res/jquery-all-macro.js",
      "res/libs-all-macro.js",
      "res/index.js");

  private String m_newLineAndIndet;
  private List<String> m_scriptsMoveToBody = new ArrayList<>();

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_indexJsFilter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    m_newLineAndIndet = workingCopy.getLineDelimiter() + "    ";
    removeScriptElements(workingCopy);
    addScriptElements(workingCopy, context);
  }

  private void removeScriptElements(WorkingCopy workingCopy) {
    String source = workingCopy.getSource();
    Document doc = Jsoup.parse(source);
    Elements scoutScriptElements = doc.getElementsByTag("scout:script");
    for (Element element : scoutScriptElements) {
      // try to find appname
      String attrSource = element.attr("src");
      source = removeElement(element, source, workingCopy);
    }

    Elements scriptElements = doc.getElementsByTag("script");
    for (Element element : scriptElements) {
      String attrSource = element.attr("src");
      if (attrSource != null && attrSource.toLowerCase().endsWith(".js")) {
        m_scriptsMoveToBody.add(element.outerHtml());
        source = removeElement(element, source, workingCopy);
      }
    }
    workingCopy.setSource(source);
  }

  private String removeElement(Element element, String source, WorkingCopy workingCopy) {
    Pattern p = Pattern.compile("(\\s*)" + Pattern.quote(element.outerHtml()));
    Matcher removeTagMatcher = p.matcher(source);
    if (removeTagMatcher.find()) {
      m_newLineAndIndet = removeTagMatcher.group(1);
      source = removeTagMatcher.replaceAll("");
    }
    else {
      source = MigrationUtility.prependTodo(source, "remove script tag: '" + element.outerHtml() + "'", workingCopy.getLineDelimiter());
      LOG.warn("Could not remove script tag '" + element.outerHtml() + "' in file '" + workingCopy.getPath() + "'.");
    }
    return source;
  }

  private void addScriptElements(WorkingCopy workingCopy, Context context) {
    String source = workingCopy.getSource();
    Matcher matcher = END_BODY_REGEX.matcher(source);
    if (matcher.find()) {
      String bodyIndent = matcher.group(1);
      StringBuilder scriptBuilder = new StringBuilder();
      scriptBuilder
          .append(m_newLineAndIndet)
          .append("<scout:script src=\"jquery.js\" />")
          .append(m_newLineAndIndet)
          .append("<scout:script src=\"eclipse-scout.js\" />")
          .append(m_newLineAndIndet)
          .append("<scout:script src=\"").append(context.getProperty(AppNameContextProperty.class)).append(".js\" />");
      for (String scriptElement : m_scriptsMoveToBody) {
        scriptBuilder.append(m_newLineAndIndet)
            .append(scriptElement);
      }

      scriptBuilder.append(bodyIndent)
          .append("</body>");
      source = matcher.replaceAll(scriptBuilder.toString());
      workingCopy.setSource(source);
    }
    else {
      MigrationUtility.prependTodo(workingCopy, "add script imports: <scout:script src=\"jquery.js\" /> " +
          "<scout:script src=\"eclipse-scout.js\" /> " +
          "<scout:script src=\"jswidgets.js\" />");
    }
  }
}
