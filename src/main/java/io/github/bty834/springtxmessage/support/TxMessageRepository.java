package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.model.SendStatus;
import io.github.bty834.springtxmessage.model.TxMessagePO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

public class TxMessageRepository {

    private final String tableName;

    private final JdbcTemplate jdbcTemplate;

    @Setter
    private Integer limit = 1000;

    @Setter
    private Integer insertBatchSize = 500;

    private static final RowMapper<TxMessagePO> ROW_MAPPER = new TxMessageRowMapper();

    public TxMessageRepository(DataSource dataSource, String tableName) {
        this.tableName = tableName;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public TxMessagePO queryByNumber(Long number) {
        String sql = "select * from " + tableName + " where number = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, number);
    }

    public TxMessagePO queryByMsgId(String msgId) {
        String sql = "select * from " + tableName + " where msg_id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, msgId);
    }

    public List<TxMessagePO> queryReadyToSendMessages(int maxRetryTimes, int delaySeconds) {
        LocalDateTime nextRetryTimeLte = LocalDateTime.now().minusSeconds(delaySeconds);
        String sql = "select * from " + tableName + " where send_status in (?,?) and next_retry_time <= ? and retry_times <= ? and deleted = 0 limit ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, SendStatus.INIT.getCode(), SendStatus.FAILED.getCode(), nextRetryTimeLte, maxRetryTimes, limit);
    }

    // save并回填id
    public void batchSave(List<TxMessagePO> messages) {
        if (messages.isEmpty()) {
            return;
        }
        String sql = " insert into " + tableName + " (number, topic, sharding_key, send_status, content, next_retry_time) value (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, messages, insertBatchSize, new ParameterizedPreparedStatementSetter<TxMessagePO>() {
            @Override
            public void setValues(PreparedStatement ps, TxMessagePO message) throws SQLException {
                ps.setLong(1, message.getNumber());
                ps.setString(2, message.getTopic());
                ps.setString(3, message.getShardingKey());
                ps.setInt(4, message.getSendStatus().getCode());
                ps.setString(5, message.getContent());
                ps.setTimestamp(6, Timestamp.valueOf(message.getNextRetryTime()));
            }
        });
    }

    public void updateByNumber(TxMessagePO msg) {
        String sql = "update " + tableName + " set send_status = ?, msg_id = ? where number = ? and deleted = 0";
        jdbcTemplate.update(sql, msg.getSendStatus().getCode(), msg.getMsgId(), msg.getNumber());
    }

    public void updateByNumber2(TxMessagePO message) {
        jdbcTemplate.update("update " + tableName + " set send_status = ?, retry_times = ? , next_retry_time = ?"
                                + " where number = ? and deleted = 0",
            message.getSendStatus().getCode(), message.getRetryTimes(), message.getNextRetryTime(), message.getNumber());
    }

    private static class TxMessageRowMapper implements RowMapper<TxMessagePO> {
        @Override
        public TxMessagePO mapRow(ResultSet rs, int rowNum) throws SQLException {
            TxMessagePO txMessage = new TxMessagePO();
            txMessage.setContent(rs.getString("content"));
            txMessage.setNumber(rs.getLong("number"));
            txMessage.setNextRetryTime(rs.getTimestamp("next_retry_time").toLocalDateTime());
            txMessage.setRetryTimes(rs.getInt("retry_times"));
            txMessage.setId(rs.getLong("id"));
            txMessage.setSendStatus(SendStatus.fromCode(rs.getInt("send_status")));
            txMessage.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
            txMessage.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            txMessage.setMsgId(rs.getString("msg_id"));
            txMessage.setTopic(rs.getString("topic"));
            txMessage.setShardingKey(rs.getString("sharding_key"));
            txMessage.setDeleted(rs.getBoolean("deleted"));
            return txMessage;
        }
    }

}



