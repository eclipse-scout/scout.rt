#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.jooq.SQLDialect;

public final class PersistenceProperties {

  private PersistenceProperties() {
  }

  public static class DialectProperty extends AbstractConfigProperty<SQLDialect, String> {

    @Override
    public SQLDialect getDefaultValue() {
      return SQLDialect.DERBY;
    }

    @Override
    public String getKey() {
      return "persistence.sql.dialect";
    }

    @Override
    protected SQLDialect parse(String value) {
      String dialect = ObjectUtility.nvl(value, "");
      try {
        return SQLDialect.valueOf(dialect);
      }
      catch (Exception e) {
        throw new PlatformException("Invalid SQL dialect '" + dialect + "' for property '" + getKey()
            + "'. Valid names are " + getValidValues());
      }
    }

    private String getValidValues() {
      return "'" + StringUtility.join("','", Arrays.asList(SQLDialect.values())) + "'";
    }

    @Override
    public String description() {
      return "The type of database that is used.";
    }
  }

  public static class JdbcMappingNameProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "persistence.sql.jdbc.mapping.name";
    }

    @Override
    public String description() {
      return "The JDBC mapping name of the database.";
    }
  }

  public static class UsernameProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return "db_username";
    }

    @Override
    public String getKey() {
      return "persistence.sql.username";
    }

    @Override
    public String description() {
      return "Specifies the username used to connect to the database.";
    }
  }

  public static class PasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "persistence.sql.password";
    }

    @Override
    public String description() {
      return "Specifies the password of the user used to connect to the database.";
    }
  }

  public static class SchemaProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return "SCOUTAPP";
    }

    @Override
    public String getKey() {
      return "persistence.sql.schema";
    }

    @Override
    public String description() {
      return "Specifies the name of the schema in the database.";
    }
  }

  public static class DatabaseAddSamplesProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return true;
    }

    @Override
    public String getKey() {
      return "persistence.sql.autopopulate";
    }

    @Override
    public String description() {
      return "Specifies if some sample data should be added after setup of the database.";
    }
  }

  public static class DriverProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return "org.apache.derby.jdbc.EmbeddedDriver";
    }

    @Override
    public String getKey() {
      return "persistence.sql.driver";
    }

    @Override
    public String description() {
      return "JDBC driver to use.";
    }
  }
}
