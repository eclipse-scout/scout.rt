#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.setup.table;

import ${package}.db.setup.IDatabaseObject;

public interface IGenerateTable extends IDatabaseObject {

  String getSchemaName();

  String createSQLInternal();

}
