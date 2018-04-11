SELECT export.meters.place_code as placecode,
       (b.volume_m3 - a.volume_m3)/(30*24) as consumption,
       place_address
  FROM
    (
      SELECT *
      FROM export."CounterData_Minute"
      WHERE
        created_datetime_rounded = '2017-11-01 00:00:00' :: TIMESTAMP
    ) a
  JOIN
    (
      SELECT *
      FROM export."CounterData_Minute"
      WHERE
        created_datetime_rounded = '2017-11-30 00:00:00' :: TIMESTAMP
    ) b
  ON a.device_identification = b.device_identification
  JOIN export.meters
  ON
    export.meters.device_identification = a.device_identification
    AND
    export.meters.device_type = 14
  JOIN export.sdp
  ON
    export.sdp.place_code = export.meters.place_code
;