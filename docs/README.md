# WICRM 문서 인덱스

이 디렉터리는 **WICRM 프로젝트 관련 문서를 Markdown으로 일관되게 관리**하기 위한 기준 위치입니다.

## 문서 목록 (현재 저장소 기준)

- `project_plan.md`: 프로젝트 마스터 문서(구조/기능/변경이력 포함)
- `project_personas.md`: 프로젝트 의사결정 페르소나(상세)
- `ai_report_service.md`: AI 상담 분석 리포트(유료 서비스) 설계/구현/운영 가이드
- `troubleshooting_image_attachment.md`: 상담/교환반품 첨부 이미지 이슈 트러블슈팅
- `ai_report_table_script.sql`: AI 리포트 서비스 DB 테이블 생성 스크립트
- `banner_table_script.sql`: 배너 기능 DB 테이블 생성 스크립트

## 루트(최상위) 문서의 역할

- `../README.md`: 프로젝트 진입점(빠른 개요 + 이 인덱스 링크)
- `../PERSONAS.md`: 페르소나 **빠른 참조(요약)** — 상세는 `docs/project_personas.md`
- `../WICRM_URGENT_FIX_PLAN.md`: 특정 시점 긴급 작업 보고서(운영/보안 관련) — 히스토리 용도
- `../typehandlers.md`: 특정 이슈(타입 매핑) 분석 노트 — 추후 `docs/`로 이동 권장

## 문서 작성/관리 규칙 (최소 기준)

- **문서 위치**: 신규 문서는 기본적으로 `docs/` 아래에 생성합니다.
- **문서 링크**: 저장소 기준 상대경로로 작성합니다. (예: `./project_plan.md`, `../README.md`)
- **변경 이력**: 큰 변경은 `docs/project_plan.md`의 변경이력 섹션에 요약을 남깁니다.
- **비밀정보 금지**: API Key, DB 비밀번호, 키스토어 비밀번호 등 **실제 값**은 문서/코드/예시에 절대 포함하지 않습니다.
  - 예시는 `${ENV_VAR}` 형태로만 표기합니다. (예: `openai.api.key=${OPENAI_API_KEY:}`)

