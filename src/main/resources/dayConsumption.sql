SELECT
  (CASE WHEN (a.accvolume IS NULL)
    THEN
      b.volume - a.volume
   ELSE (b.volume - b.accvolume) - (a.volume - a.accvolume)
   END) as consumption,
  placecode
FROM
  (
    SELECT
      export."CounterData_Minute".device_identification AS id,
      volume_m3                                         AS volume,
      volume_acc_m3                                     AS accvolume,
      place_code                                        AS placecode
    FROM export."CounterData_Minute"
      JOIN export.meters
        ON export."CounterData_Minute".device_identification = export.meters.device_identification
    WHERE export."CounterData_Minute".device_identification IN
          (
            (
              SELECT device_identification
              FROM export.meters
              WHERE
                '2017-11-29 00:00' :: TIMESTAMP > lower(active) AND upper(active) IS NULL
            )
            UNION
            (
              SELECT device_identification
              FROM export.meters
              WHERE
                upper(active) IS NOT NULL AND '2017-11-29 00:00' :: TIMESTAMP <@ active
                AND
                '2017-11-30 00:00' :: TIMESTAMP <@ active
            )
          )
          AND created_datetime_rounded = '2017-11-29 00:00' :: TIMESTAMP
          AND device_type = 14
  ) a
  JOIN
  (
    SELECT
      export."CounterData_Minute".device_identification AS id,
      volume_m3                                         AS volume,
      volume_acc_m3                                     AS accvolume
    FROM export."CounterData_Minute"
      JOIN export.meters
        ON export."CounterData_Minute".device_identification = export.meters.device_identification
    WHERE export."CounterData_Minute".device_identification IN
          (
            (
              SELECT device_identification
              FROM export.meters
              WHERE
                '2017-11-29 00:00' :: TIMESTAMP > lower(active) AND upper(active) IS NULL
            )
            UNION
            (
              SELECT device_identification
              FROM export.meters
              WHERE
                upper(active) IS NOT NULL AND '2017-11-29 00:00' :: TIMESTAMP <@ active
                AND
                '2017-11-30 00:00' :: TIMESTAMP <@ active
            )
          )
          AND created_datetime_rounded = '2017-11-30 00:00' :: TIMESTAMP
          AND device_type = 14
  ) b
    ON a.id = b.id
-- order by consumption DESC
;