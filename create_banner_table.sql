CREATE SEQUENCE banner_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE TABLE banner (id NUMBER PRIMARY KEY, name VARCHAR2(100) NOT NULL, image_url VARCHAR2(255) NOT NULL, link_url VARCHAR2(255) NOT NULL, position VARCHAR2(50) NOT NULL, active NUMBER(1) DEFAULT 1, display_order NUMBER(10) DEFAULT 0, start_date DATE, end_date DATE);
CREATE OR REPLACE TRIGGER banner_bi_trg BEFORE INSERT ON banner FOR EACH ROW BEGIN SELECT banner_seq.NEXTVAL INTO :NEW.id FROM DUAL; END;
