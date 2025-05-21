-- 배너 관리를 위한 테이블 생성 스크립트 (Oracle 11g)

-- 시퀀스 생성
CREATE SEQUENCE banner_seq 
    START WITH 1 
    INCREMENT BY 1 
    NOCACHE 
    NOCYCLE;

-- 배너 테이블 생성
CREATE TABLE banner (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    image_url VARCHAR2(255) NOT NULL,
    link_url VARCHAR2(255) NOT NULL,
    position VARCHAR2(50) NOT NULL,
    active NUMBER(1) DEFAULT 1,
    display_order NUMBER(10) DEFAULT 0,
    start_date DATE,
    end_date DATE
);

-- 트리거 생성 (ID 자동 증가)
CREATE OR REPLACE TRIGGER banner_bi_trg
BEFORE INSERT ON banner
FOR EACH ROW
BEGIN
    SELECT banner_seq.NEXTVAL INTO :NEW.id FROM DUAL;
END;
/

-- 샘플 데이터 삽입
INSERT INTO banner (name, image_url, link_url, position, active, display_order, start_date, end_date)
VALUES ('쿠팡파트너스 사이드배너 1', '/assets/media/banners/coupang_banner1.jpg', 'https://link.coupang.com/a/csrzQ7', 'sidebar', 1, 1, NULL, NULL);

INSERT INTO banner (name, image_url, link_url, position, active, display_order, start_date, end_date)
VALUES ('쿠팡파트너스 사이드배너 2', '/assets/media/banners/coupang_banner2.jpg', 'https://link.coupang.com/a/csrzQ7', 'sidebar', 1, 2, NULL, NULL);

COMMIT;