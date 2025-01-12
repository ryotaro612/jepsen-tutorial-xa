CREATE TABLE account(
  user_id    varchar(128)  NOT NULL,
  balance    int           NOT NULL default 0,
  PRIMARY KEY (user_id)
);

