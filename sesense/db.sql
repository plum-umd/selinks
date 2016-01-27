DROP TABLE sesense_sensors;

CREATE TABLE sesense_sensors
(
  sensorid smallint PRIMARY KEY,
  networkid smallint NOT NULL,
  "name" varchar(128) NOT NULL DEFAULT 'unnamed',
  geo point,

  "function" varchar(256) NOT NULL DEFAULT '',

  ponder_ref label
);
ALTER TABLE sesense_sensors OWNER TO selinks;

INSERT INTO sesense_sensors VALUES (
  1, 1,
  'Video 1',
  '(38.990397, 76.936678)',
  'Video monitoring of a suspecious location.',
  'P2Obj("root/sensor/usa/video1")');
INSERT INTO sesense_sensors VALUES (
  2, 1,
  'Video 2',
  '(38.990397, 76.936678)',
  'Video monitoring of a suspecious location.',
  'P2Obj("root/sensor/usa/video2")');
INSERT INTO sesense_sensors VALUES (
  3, 1,
  'Smell 1',
  '(38.990397, 76.936678)',
  'Mission critical scent detection.',
  'P2Obj("root/sensor/coalition/scent1")');

INSERT INTO sesense_sensors VALUES (
  4, 1,
  'Audio 1',
  '(38.990397, 76.936678)',
  'Tea-time conversation monitoring.',
  'P2Obj("root/sensor/coalition/uk/audio1")');

INSERT INTO sesense_sensors VALUES (
  5, 1,
  'Audio 2',
  '(38.990397, 76.936678)',
  'Basra night-time whisper detection.',
  'P2Obj("root/sensor/coalition/iraq/audio2")');

DROP TABLE sesense_networks;
CREATE TABLE sesense_networks (
  networkid smallint PRIMARY KEY,
  "name" varchar(256) NOT NULL DEFAULT 'unnamed',
  geo point
);
ALTER TABLE sesense_networks OWNER TO selinks;

INSERT INTO sesense_networks VALUES (1, 'Shady Character Monitoring Network', '(38.990397, 76.936678)');
INSERT INTO sesense_networks VALUES (2, 'Global Waldo Search Network', '(38.990397, 76.936678)');
INSERT INTO sesense_networks VALUES (3, 'Very Large Scent Array', '(38.990397, 76.936678)');

DROP TABLE sesense_users;

-- from sewiki db
CREATE TABLE sesense_users (
    userid integer DEFAULT 0 NOT NULL,
    name character varying(30),
    "password" character varying(30),
    ponder_ref label
);
ALTER TABLE sesense_users OWNER TO selinks;

INSERT INTO sesense_users VALUES
  (100, 'Piotr Mardziel', 'pw', 'P2Obj("root/user/coalition/piotrm")');
INSERT INTO sesense_users VALUES
  (101, 'Jeff Foster',    'pw', 'P2Obj("root/user/usa/jfoster")');
INSERT INTO sesense_users VALUES
  (102, 'Michael Hicks',  'pw', 'P2Obj("root/user/coalition/uk/mwh")');
INSERT INTO sesense_users VALUES
  (103, 'Jalal Talabani', 'pw', 'P2Obj("root/user/coalition/iraq/jalal")');
