SELECT u.user_nr     user_nr,
                 pp.project_nr entity_nr,
                 0             VALUE,
                 0             rnk
          FROM   ors_user u,
                 ors_project_person pp
          WHERE  u.user_nr = pp.person_nr
                 AND pp.type_uid = 2443
          MINUS
          SELECT i.user_nr   user_nr,
                 i.entity_nr entity_nr,
                 0           VALUE,
                 0           rnk
          FROM   ors_dwh_index i
          WHERE  i.index_uid = :indexUid
                 AND i.time_range_uid = :timeRangeUid
         UNION ALL
         SELECT i.user_nr,
                i.entity_nr,
                i.VALUE,
                ROWNUM rnk
         FROM   (SELECT i.user_nr   user_nr,
                        i.entity_nr entity_nr,
                        i.VALUE     VALUE
                 FROM   ors_dwh_index i
                 WHERE  i.index_uid = :indexUid
                        AND i.time_range_uid = :timeRangeUid
                 ORDER  BY 3 ASC) i
