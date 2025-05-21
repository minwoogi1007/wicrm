    /**
     * 첨부파일이 있는 가장 최근 문의 ID 조회
     * @return 최근 문의 ID
     */
package com.wio.crm.mapper;

import com.wio.crm.model.ConsultingInquiry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ConsultingMapper {
    
    // 상담 문의 조회
    List<ConsultingInquiry> findConsultingInquiries(Map<String, Object> params);
    
    // 페이지네이션이 있는 상담 문의 조회
    List<ConsultingInquiry> findConsultingInquiriesWithPaging(Map<String, Object> params);
    
    // 상담 문의 개수 조회
    long countConsultingInquiries(Map<String, Object> params);
    
    // 상담 문의 상세 조회
    ConsultingInquiry getConsultingInquiryById(@Param("inquiryId") Long inquiryId);
    
    // 상담 문의 상태 업데이트
    int updateInquiryStatus(Map<String, Object> params);
    
    // 내부 메모 업데이트
    int updateMemo(Map<String, Object> params);
    
    // 처리 내용 업데이트
    int updateProcessContent(Map<String, Object> params);
    
    // 상담 문의 등록
    int insertConsultingInquiry(ConsultingInquiry inquiry);
    
    // 첨부 파일 등록
    int insertAttachment(Map<String, Object> params);
    
    // 첨부 파일 조회
    List<Map<String, Object>> getAttachmentsByInquiryId(@Param("inquiryId") Long inquiryId);
    
    // 코멘트 등록
    int insertComment(Map<String, Object> params);
    
    // 코멘트 조회
    List<Map<String, Object>> getCommentsByInquiryId(@Param("inquiryId") Long inquiryId);
    
    // 코멘트 날짜 조회
    String getCommentDateById(@Param("commentId") Long commentId);
    
    // 모든 문의 유형 조회
    List<Map<String, Object>> getAllInquiryTypes();
    
    // 활성화된 응답 템플릿 조회
    List<Map<String, Object>> getActiveReplyTemplates();
    
    // 카테고리별로 그룹화된 응답 템플릿 조회
    List<Map<String, Object>> getGroupedReplyTemplatesByCategory();
    
    // 테이블 목록 조회
    List<Map<String, Object>> getAllTableNames();
    
    // 첨부파일이 있는 최근 문의 ID 조회
    Long findLatestInquiryIdWithAttachments();
}
