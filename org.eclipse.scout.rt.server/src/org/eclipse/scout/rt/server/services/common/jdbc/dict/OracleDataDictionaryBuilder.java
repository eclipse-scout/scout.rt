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
package org.eclipse.scout.rt.server.services.common.jdbc.dict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class OracleDataDictionaryBuilder {
  public static final int SOURCE_TYPE_ORACLE_SERVER = 100;
  public static final int SOURCE_TYPE_ORACLE_CLIENT_LITE = 101;
  public static final int SOURCE_TYPE_ORACLE_PPC_LITE = 102;

  private static final String SELECT_ALL_TABLES = "select table_name from all_tables where owner=?";

  // MVI 26.3.2009 Special all_views statment for oracle lite. to_char needed in
  // some Korean systems
  private static final String SELECT_ALL_VIEWS_ORACLE = "select view_name,text_length,text from all_views where owner=?";
  private static final String SELECT_ALL_VIEWS_LITE = "select view_name,text_length,to_char(text) from all_views where owner=?";

  private static final String SELECT_ALL_SEQUENCES = "select SEQUENCE_NAME,MIN_VALUE,MAX_VALUE,INCREMENT_BY from all_sequences where sequence_owner=?";
  // for tables:
  // AMO 22.1.2007 Do not select items from Oracle10g Rel. 2 Recycle Bin
  private static final String SELECT_PRIMARY_KEY_COLUMNS = "select table_name,constraint_name,column_name from all_cons_columns where owner=? and table_name not like 'BIN$%' and constraint_name in (select constraint_name from all_constraints where owner=? and constraint_type='P') order by table_name,constraint_name,position";
  private static final String SELECT_ALL_INDEXES = "select table_name,index_name,uniqueness,index_type from all_indexes where owner=? order by table_name,index_name";
  private static final String SELECT_ALL_INDEXES_ORACLELITE = "select table_name,index_name,uniqueness,'NORMAL' from all_indexes where owner=? order by table_name,index_name";
  private static final String SELECT_ALL_INDEX_COLUMNS = "select index_name,column_name from all_ind_columns where index_owner=? order by index_name,column_position";
  private static final String SELECT_ALL_COLUMNS = "select table_name,COLUMN_NAME,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,DATA_SCALE,NULLABLE,COLUMN_ID,DATA_DEFAULT from all_tab_columns where owner=? and table_name in (select table_name from all_tables where owner=?) order by table_name,column_id";

  /**
   * @rn imo, 19.07.2006, granting to rep user is done automatically in
   *     DataDictValidator
   * @since Build 204
   */
  /*
   * //for tables and views private static final String
   * SELECT_ALL_TABLE_PRIVILEGES=
   * "select table_name,grantee,privilege,grantable from all_tab_privs where table_schema=? union select distinct table_name,grantee,privilege,grantable from all_col_privs where table_schema=? order by table_name"
   * ; private static final StringSELECT_ALL_TABLE_PRIVILEGES_ORACLELITE=
   * "select table_name,grantee,grant_type,grantable from table_privileges where owner=? union select distinct table_name,grantee,grant_type,grantable from column_privileges where owner=? order by table_name"
   * ;
   */

  public DataDictionary build(Connection conn, String schema, boolean lite) throws SQLException {
    schema = schema.toUpperCase();
    DataDictionary dd = new DataDictionary(schema);
    dd.setSourceType(lite ? SOURCE_TYPE_ORACLE_CLIENT_LITE : SOURCE_TYPE_ORACLE_SERVER);
    //
    PreparedStatement stm = null;
    try {
      // load tables
      stm = conn.prepareStatement(SELECT_ALL_TABLES);
      stm.setString(1, schema);
      ResultSet rs;
      rs = stm.executeQuery();
      while (rs.next()) {
        TableDesc td = new TableDesc(rs.getString(1), schema);
        dd.addTable(td);
      }
      stm.close();
      stm = null;
      // load views
      stm = conn.prepareStatement(lite ? SELECT_ALL_VIEWS_LITE : SELECT_ALL_VIEWS_ORACLE);
      stm.setString(1, schema);
      rs = stm.executeQuery();
      while (rs.next()) {
        ViewDesc vd = new ViewDesc(rs.getString(1), schema, rs.getString(3).trim());
        dd.addView(vd);
      }
      stm.close();
      stm = null;
      // load sequences
      stm = conn.prepareStatement(SELECT_ALL_SEQUENCES);
      stm.setString(1, schema);
      rs = stm.executeQuery();
      while (rs.next()) {
        SequenceDesc sd = new SequenceDesc(rs.getString(1), rs.getBigDecimal(2), rs.getBigDecimal(3), rs.getBigDecimal(4));
        dd.addSequence(sd);
      }
      stm.close();
      stm = null;
      // for tables:
      // check if there is a PK
      stm = conn.prepareStatement(SELECT_PRIMARY_KEY_COLUMNS);
      stm.setString(1, schema);
      stm.setString(2, schema);
      rs = stm.executeQuery();
      TableDesc curTableDesc = null;
      PrimaryKeyDesc curPk = null;
      while (rs.next()) {
        String tableName = rs.getString(1);
        String pkName = rs.getString(2);
        String colName = rs.getString(3);
        if (curPk == null || (!curPk.getName().equals(pkName))) {
          curTableDesc = dd.getTable(tableName);
          curPk = new PrimaryKeyDesc(pkName);
          curTableDesc.setPrimaryKey(curPk);
        }
        curPk.addColumnName(colName);
      }
      stm.close();
      stm = null;

      // load indexes except pk index
      HashMap<String, IndexDesc> allIndexes = new HashMap<String, IndexDesc>();
      if (lite) {
        stm = conn.prepareStatement(SELECT_ALL_INDEXES_ORACLELITE);
      }
      else {
        stm = conn.prepareStatement(SELECT_ALL_INDEXES);
      }
      stm.setString(1, schema);
      rs = stm.executeQuery();
      curTableDesc = null;
      curPk = null;
      while (rs.next()) {
        String tableName = rs.getString(1);
        String indexName = rs.getString(2);
        String unique = rs.getString(3);
        String indexType = ("" + rs.getString(4)).toUpperCase();
        if (indexType.startsWith("FUNCTION-BASED")) {
          // ignore functional indexes
        }
        else {
          if (curTableDesc == null || (!curTableDesc.getName().equals(tableName))) {
            curTableDesc = dd.getTable(tableName);
            curPk = curTableDesc.getPrimaryKey();
          }
          IndexDesc id = new IndexDesc(indexName, unique.equalsIgnoreCase("unique"));
          if (curPk != null && curPk.getName().equalsIgnoreCase(indexName)) {
            // ignore it, it's the primary key index
          }
          else {
            curTableDesc.addIndex(id);
          }
          allIndexes.put(indexName, id);
        }
      }
      stm.close();
      stm = null;
      // load index columns
      stm = conn.prepareStatement(SELECT_ALL_INDEX_COLUMNS);
      stm.setString(1, schema);
      rs = stm.executeQuery();
      IndexDesc curIndexDesc = null;
      while (rs.next()) {
        String indexName = rs.getString(1);
        String colName = rs.getString(2);
        if (curIndexDesc == null || (!curIndexDesc.getName().equals(indexName))) {
          curIndexDesc = allIndexes.get(indexName);
        }
        if (curIndexDesc != null) {
          curIndexDesc.addColumnName(colName);
        }
      }
      stm.close();
      stm = null;
      // load all columns
      stm = conn.prepareStatement(SELECT_ALL_COLUMNS);
      stm.setString(1, schema);
      stm.setString(2, schema);
      rs = stm.executeQuery();
      curTableDesc = null;
      while (rs.next()) {
        String tableName = rs.getString(1);
        if (curTableDesc == null || (!curTableDesc.getName().equals(tableName))) {
          curTableDesc = dd.getTable(tableName);
        }
        ColumnDesc cd = new ColumnDesc(
            rs.getString(2).toUpperCase(),
            rs.getString(3),
            rs.getLong(4),
            rs.getLong(5),
            rs.getLong(6),
            rs.getString(7).equalsIgnoreCase("y"),
            rs.getString(9)
            );
        curTableDesc.addColumn(cd);
      }
      stm.close();
      stm = null;
      /**
       * @rn imo, 19.07.2006, granting to rep user is done automatically in
       *     DataDictValidator
       * @since Build 204
       */
      /*
       * //load table privileges try{ //first try normal oracle query
       * stm=conn.prepareStatement(SELECT_ALL_TABLE_PRIVILEGES); }
       * catch(Exception e){ //try oracle lite query
       * stm=conn.prepareStatement(SELECT_ALL_TABLE_PRIVILEGES_ORACLELITE); }
       * stm.setString(1,schema); stm.setString(2,schema);
       * rs=stm.executeQuery(); curTableDesc=null; while(rs.next()){ String
       * tableName=rs.getString(1); TableDesc td=dd.getTable(tableName);
       * if(td!=null){ TableGrantDesc gd=new
       * TableGrantDesc(rs.getString(2),rs.getString
       * (3),!"no".equalsIgnoreCase(rs.getString(4))); td.addGrant(gd); } else{
       * //a view ViewDesc vd=dd.getView(tableName); if(vd!=null){
       * TableGrantDesc gd=new
       * TableGrantDesc(rs.getString(2),rs.getString(3),!"no"
       * .equalsIgnoreCase(rs.getString(4))); vd.addGrant(gd); } } }
       * stm.close(); stm=null;
       */
    }
    finally {
      if (stm != null) {
        try {
          stm.close();
        }
        catch (Exception fatal) {
        }
      }
    }
    return dd;
  }

}
