SELECT P.PERSON_NR,
       P.TYPE_UID,
       P.PERSON_NO,
       P.ICON,
       P.DISPLAY_NAME || DECODE (A.CITY, NULL, '', ' (' || A.CITY || ')'),
       P.PERSON_NO,
       NP.SALUTATION_UID,
       NVL (NP.LAST_NAME, P.DISPLAY_NAME),
       NP.FIRST_NAME,
       S.NAME || ' ' || PA.HOUSE_NO,
       A.ZIP_CODE,
       A.CITY,
       CO.ISO_CODE,
       NP.EVT_BIRTH,
       BP.DISPLAY_NAME,
       P.PORTFOLIO_NR,
       P2.PERSON_NO,
       NVL (NP2.LAST_NAME, P2.DISPLAY_NAME),
       NP2.FIRST_NAME,
       S2.NAME || ' ' || PA2.HOUSE_NO,
       A2.ZIP_CODE,
       A2.CITY,
       CO2.ISO_CODE,
       BP2.DISPLAY_NAME,
       P.TYPE_UID,
       P.ENTERPRISE_TYPE_UID
  FROM ORS_PERSON P,
       ORS_PORTFOLIO PO,
       ORS_NATURAL_PERSON NP,
       ORS_PERSON_ADDRESS PA,
       ORS_STREET S,
       ORS_ADDRESS A,
       ORS_COUNTRY CO,
       ORS_PERSON BP,
       ORS_RELATION R,
       ORS_PERSON P2,
       ORS_PORTFOLIO PO2,
       ORS_NATURAL_PERSON NP2,
       ORS_PERSON_ADDRESS PA2,
       ORS_STREET S2,
       ORS_ADDRESS A2,
       ORS_COUNTRY CO2,
       ORS_PERSON BP2
 WHERE     (P.PERSON_NR IN (6948538))
       AND NP.PERSON_NR(+) = P.PERSON_NR
       AND PA.TYPE_UID(+) = 2469
       AND PA.PERSON_NR(+) = P.PERSON_NR
       AND S.STREET_NR(+) = PA.STREET_NR
       AND A.ADDRESS_NR(+) = PA.ADDRESS_NR
       AND CO.COUNTRY_UID(+) = A.COUNTRY_UID
       AND BP.PERSON_NR(+) = PO.CONSULTANT_USER_NR
       AND P.ACTIVE = 1
       AND P.PORTFOLIO_NR = PO.PORTFOLIO_NR
       AND P.STATUS_UID = 146250
       AND R.FST_ROLE_OWNER_NR = P2.PERSON_NR
       AND (R.FST_ROLE_OWNER_NR IN (2238292, 7396244, 633874466))
       AND R.FST_ROLE_UID = 893739
       AND R.RELATION_UID = 893740
       AND R.SEC_ROLE_OWNER_NR = P.PERSON_NR
       AND R.STATUS_UID = 146250
       AND NP2.PERSON_NR(+) = P2.PERSON_NR
       AND PA2.TYPE_UID(+) = 2469
       AND PA2.PERSON_NR(+) = P2.PERSON_NR
       AND S2.STREET_NR(+) = PA2.STREET_NR
       AND A2.ADDRESS_NR(+) = PA2.ADDRESS_NR
       AND CO2.COUNTRY_UID(+) = A2.COUNTRY_UID
       AND BP2.PERSON_NR(+) = PO2.CONSULTANT_USER_NR
       AND P2.ACTIVE = 1
       AND P2.PORTFOLIO_NR = PO2.PORTFOLIO_NR
       AND P2.STATUS_UID = 146250
       AND 1 = DECODE (100,
                       100, 1,
                       0, 0,
                       ORS_UTIL.PERSON_PRIV (100,
                                             P.PERSON_NR,
                                             700383856,
                                             75221405,
                                             NEW NUMBERARRAY (),
                                             NEW NUMBERARRAY (),
                                             NEW NUMBERARRAY (79915020,
                                                              79915147,
                                                              578865242,
                                                              79915110,
                                                              79915133,
                                                              79915145)))
