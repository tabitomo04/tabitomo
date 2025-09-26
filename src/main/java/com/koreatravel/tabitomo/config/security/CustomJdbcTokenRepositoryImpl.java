package com.koreatravel.tabitomo.config.security;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;

public class CustomJdbcTokenRepositoryImpl extends JdbcDaoSupport implements PersistentTokenRepository {
    // 기본 쿼리 오버라이드
    public static final String CREATE_TABLE_SQL = "create table persistent_logins (id bigint auto_increment primary key, member_id binary(16) not null, series varchar(64) not null, token varchar(64) not null, last_used timestamp not null, created_at timestamp default current_timestamp, updated_at timestamp default current_timestamp on update current_timestamp, unique (series), foreign key (member_id) references member(id) on delete cascade, index idx_member_id (member_id))";
    
    public static final String DEF_TOKEN_BY_SERIES_SQL = "select member_id, series, token, last_used from persistent_logins where series = ?";
    
    public static final String DEF_INSERT_TOKEN_SQL = "insert into persistent_logins (member_id, series, token, last_used) values (?, ?, ?, ?)";
    
    public static final String DEF_UPDATE_TOKEN_SQL = "update persistent_logins set token = ?, last_used = ? where series = ?";
    
    public static final String DEF_REMOVE_USER_TOKENS_SQL = "delete from persistent_logins where member_id = ?";

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        // member_id를 UUID로 변환 (username이 이메일이므로 member 테이블에서 조회 필요)
        String memberId = getJdbcTemplate().queryForObject(
            "SELECT id FROM member WHERE email = ?", 
            String.class, 
            token.getUsername()
        );
        
        getJdbcTemplate().update(
            DEF_INSERT_TOKEN_SQL,
            memberId,
            token.getSeries(),
            token.getTokenValue(),
            token.getDate()
        );
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        getJdbcTemplate().update(
            DEF_UPDATE_TOKEN_SQL,
            tokenValue,
            lastUsed,
            series
        );
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        try {
            return getJdbcTemplate().queryForObject(
                DEF_TOKEN_BY_SERIES_SQL,
                (rs, rowNum) -> new PersistentRememberMeToken(
                    getUsernameByMemberId(rs.getBytes("member_id")),
                    rs.getString("series"),
                    rs.getString("token"),
                    rs.getTimestamp("last_used")
                ),
                seriesId
            );
        } catch (IncorrectResultSizeDataAccessException e) {
            logger.debug("Querying token for series '" + seriesId + "' returned no results.", e);
            return null;
        } catch (DataAccessException e) {
            logger.error("Failed to load token for series: " + seriesId, e);
            return null;
        }
    }

    @Override
    public void removeUserTokens(String username) {
        String memberId = getJdbcTemplate().queryForObject(
            "SELECT id FROM member WHERE email = ?", 
            String.class, 
            username
        );
        
        getJdbcTemplate().update(DEF_REMOVE_USER_TOKENS_SQL, memberId);
    }

    private String getUsernameByMemberId(byte[] memberId) {
        return getJdbcTemplate().queryForObject(
            "SELECT email FROM member WHERE id = ?",
            String.class,
            memberId
        );
    }
}
