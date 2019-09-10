package org.eclipse.scout.migration.ecma6.task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.task.json.IConstPlaceholderMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;

@Order(30000)
public class T30000_JsonToJsModule extends AbstractTask {

  private static final Pattern KEY_PAT = Pattern.compile("\"(\\w+)\":");
  private static final Pattern TRAILING_WHITESPACE_CHARS_PAT = Pattern.compile("\\s$");
  private static final String JSON_EXTENSION = "json";
  private static final String JS_EXTENSION = "js";
  private static final String ESCAPED_REPLACEMENT1 = "@@@_@@@escaped1@@@_@@@";
  private static final String ESCAPED_REPLACEMENT2 = "@@@_@@@escaped2@@@_@@@";
  public static final String JSON_MODEL_NAME_SUFFIX = "Model";

  private static final Pattern PLACEHOLDER_PAT = Pattern.compile("(\\w+):\\s*'\\$\\{(\\w+):([^}]+)}'");

  private Predicate<PathInfo> m_fileFilter = PathFilters.withExtension(JSON_EXTENSION);

  private List<IConstPlaceholderMapper> m_placeholderMappers;

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    if (!m_fileFilter.test(pathInfo)) {
      return false;
    }

    WorkingCopy candidate = context.ensureWorkingCopy(pathInfo.getPath());
    return candidate.getSource().contains("\"objectType\":");
  }

  @PostConstruct
  private void init() {
    m_placeholderMappers = BEANS.all(IConstPlaceholderMapper.class);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    String originalSource = workingCopy.getSource();

    // migrate scout json model file to js file
    String step1 = KEY_PAT.matcher(originalSource).replaceAll("$1:");
    String step2 = step1.replace("\\\"", ESCAPED_REPLACEMENT1).replace("'", ESCAPED_REPLACEMENT2);
    String step3 = step2.replace('"', '\'');
    String step4 = step3.replace(ESCAPED_REPLACEMENT1, "\"").replace(ESCAPED_REPLACEMENT2, "\\'");
    String step5 = TRAILING_WHITESPACE_CHARS_PAT.matcher(step4).replaceAll("");
    String step6 = "export default function() {\n" +
        "  return " + step5 + ";\n}\n";

    String step7 = T25000_ModelsGetModelToImport.replace(PLACEHOLDER_PAT, step6, (m, r) -> migratePlaceholders(m, r, pathInfo.getPath(), context));

    // change file name from Xyz.json to XyzModel.js
    Path jsonRelPath = Configuration.get().getSourceModuleDirectory().relativize(pathInfo.getPath());
    String jsonFileName = jsonRelPath.getFileName().toString();
    String jsFileName = jsonFileName.substring(0, jsonFileName.length() - JSON_EXTENSION.length() - 1) + JSON_MODEL_NAME_SUFFIX + '.' + JS_EXTENSION;
    Path newRelPath = jsonRelPath.getParent().resolve(jsFileName);
    Path newFileNameInSourceFolder = pathInfo.getPath().getParent().resolve(jsFileName);
    Assertions.assertFalse(Files.exists(newFileNameInSourceFolder),
        "The migration of file '{}' would be stored in '{}' but this file already exists in the source folder!", pathInfo.getPath(), newRelPath);

    workingCopy.setSource(step7);
    workingCopy.setRelativeTargetPath(newRelPath);

    BEANS.get(T5020_ResolveClassEnumReferencesAndCreateImports.class).process(pathInfo, context);
    BEANS.get(T5040_ResolveUtilityReferencesAndCreateImports.class).process(pathInfo, context);
    BEANS.get(T29000_JsCreateImports.class).process(pathInfo, context);
  }

  protected void migratePlaceholders(Matcher matcher, StringBuilder result, Path file, Context context) {
    String key = matcher.group(1);
    String type = matcher.group(2);
    String value = matcher.group(3);
    result.append(key).append(": ");
    if("textKey".equals(type)) {
      // no migration for the moment. Keep as it was
      result.append("'${textKey:").append(value).append("}'");
    }
    else if ("const".equals(type)) {
      result.append(migratePlaceholderConst(value, key, file, context));
    }
    else if ("iconId".equals(type)) {
      result.append(migratePlaceholderIconId(value, file));
    }
    else {
      Assertions.fail("unknown json placeholder: '{}' in file '{}'.", type, file);
    }
  }

  protected String migratePlaceholderIconId(String iconId, Path file) {
    Assertions.assertTrue(StringUtility.hasText(iconId), "Empty iconId placeholder in json model '{}'.", file);

    int lastDotPos = iconId.lastIndexOf('.');
    if (lastDotPos > 0) {
      // qualified
      return iconId.substring(0, lastDotPos) + ".icons" + iconId.substring(lastDotPos);
    }

    return "scout.icons." + iconId;
  }

  protected String migratePlaceholderConst(String constValue, String key, Path file, Context context) {
    Assertions.assertTrue(StringUtility.hasText(key), "Empty key in json model '{}'.", key, file);
    Assertions.assertTrue(StringUtility.hasText(constValue), "Empty const placeholder for attribute '{}' in json model '{}'.", key, file);
    for (IConstPlaceholderMapper mapper : m_placeholderMappers) {
      String migrated = mapper.migrate(key, constValue, file, context);
      if (migrated != null) {
        return migrated;
      }
    }
    return "scout.objects.resolveConst('" + constValue + "')"; // default migration
  }
}
