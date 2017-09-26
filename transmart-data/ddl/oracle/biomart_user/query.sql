--
-- Type: TABLE; Owner: BIOMART_USER; Name: QUERY
--
CREATE TABLE "BIOMART_USER"."QUERY"
(
    "ID" NUMBER NOT NULL ENABLE,
    "USERNAME" VARCHAR2(50 BYTE) NOT NULL,
    "PATIENTS_QUERY" CLOB,
    "OBSERVATIONS_QUERY" CLOB,
    "API_VERSION" VARCHAR2(25 BYTE),
    "BOOKMARKED" CHAR(1 BYTE),
    "DELETED" CHAR(1 BYTE),
    "CREATE_DATE" TIMESTAMP (6),
    "UPDATE_DATE" TIMESTAMP (6),
    PRIMARY KEY ("ID")
);

--
-- Type: INDEX; Owner: BIOMART_USER; Name: QUERY_USER
--
CREATE INDEX "BIOMART_USER"."QUERY_USER" ON "BIOMART_USER"."QUERY" ("USERNAME")
TABLESPACE "TRANSMART" ;

--
-- Type: SEQUENCE; Owner: BIOMART_USER; Name: QUERY_ID
--
CREATE SEQUENCE "BIOMART_USER"."QUERY_ID";

--
-- Type: TRIGGER; Owner: BIOMART_USER; Name: TRG_QUERY_ID
--
  CREATE OR REPLACE TRIGGER "BIOMART_USER"."TRG_QUERY_ID"
	 before insert on "QUERY"
	 for each row begin
	 if inserting then
	 if :NEW."ID" is null then
	 select QUERY_ID.nextval into :NEW."ID" from dual;
	 end if;
	 end if;
	 end;

/
ALTER TRIGGER "BIOMART_USER"."TRG_QUERY_ID" ENABLE;
