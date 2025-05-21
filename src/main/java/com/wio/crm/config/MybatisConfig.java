package com.wio.crm.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
@MapperScan(basePackages = "com.wio.crm.mapper")
public class MybatisConfig {

    /**
     * Oracle NUMBER 타입을 String으로 자동 변환하기 위한 타입 핸들러
     * 이 핸들러는 모든 숫자 데이터를 String으로 변환합니다.
     */
    public static class NumberToStringTypeHandler extends BaseTypeHandler<String> {
        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
            ps.setString(i, parameter);
        }

        @Override
        public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
            Object value = rs.getObject(columnName);
            return value != null ? String.valueOf(value) : null;
        }

        @Override
        public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            Object value = rs.getObject(columnIndex);
            return value != null ? String.valueOf(value) : null;
        }

        @Override
        public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            Object value = cs.getObject(columnIndex);
            return value != null ? String.valueOf(value) : null;
        }
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/**/*.xml"));
        
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        // 카멜 케이스 자동 변환 설정
        configuration.setMapUnderscoreToCamelCase(true);
        
        // null 값 처리 설정
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setCallSettersOnNulls(true);
        configuration.setReturnInstanceForEmptyRow(true);
        
        // 모든 숫자 타입에 대해 StringTypeHandler 등록
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        typeHandlerRegistry.register(String.class, JdbcType.INTEGER, new NumberToStringTypeHandler());
        typeHandlerRegistry.register(String.class, JdbcType.NUMERIC, new NumberToStringTypeHandler());
        typeHandlerRegistry.register(String.class, JdbcType.DECIMAL, new NumberToStringTypeHandler());
        typeHandlerRegistry.register(String.class, JdbcType.BIGINT, new NumberToStringTypeHandler());
        typeHandlerRegistry.register(String.class, JdbcType.DOUBLE, new NumberToStringTypeHandler());
        typeHandlerRegistry.register(String.class, JdbcType.FLOAT, new NumberToStringTypeHandler());
        
        // 특정 타입의 결과 매핑 시 오류 발생 방지를 위한 설정
        configuration.setAutoMappingBehavior(org.apache.ibatis.session.AutoMappingBehavior.FULL);
        configuration.setAutoMappingUnknownColumnBehavior(org.apache.ibatis.session.AutoMappingUnknownColumnBehavior.WARNING);
        
        // 디버깅을 위한 로그 설정
        configuration.setLogImpl(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
        
        sessionFactory.setConfiguration(configuration);
        return sessionFactory.getObject();
    }
} 