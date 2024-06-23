package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.model.SendStatus;
import io.github.bty834.springtxmessage.model.TxMessagePO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class TxMessageRepository {

    private final DataSource dataSource;

    private final String tableName;

    private JdbcTemplate jdbcTemplate;

    @Setter
    private Integer limit = 1000;

    private Integer insertBatchSize = 1000;

    private static final RowMapper<TxMessagePO> ROW_MAPPER = new TxMessageRowMapper();

    public TxMessageRepository(DataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public TxMessagePO queryById(Long id) {
        String sql = "select * from " + tableName + " where id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
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
        String sql = " insert into " + tableName + " (topic, sharding_key, send_status, content, next_retry_time) value (?, ?, ?, ?, ?)";

        for (TxMessagePO message : messages) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                // 指定主键
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
                preparedStatement.setString(1, message.getTopic());
                preparedStatement.setString(2, message.getShardingKey());
                preparedStatement.setInt(3, message.getSendStatus().getCode());
                preparedStatement.setString(4, message.getContent());
                preparedStatement.setTimestamp(5, Timestamp.valueOf(message.getNextRetryTime()));
                return preparedStatement;
            }, keyHolder);
            message.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        }
    }

    public void updateById(TxMessagePO msg) {
        String sql = "update " + tableName + " set send_status = ?, msg_id = ? where id = ? and deleted = 0";
        jdbcTemplate.update(sql, msg.getSendStatus().getCode(), msg.getMsgId(), msg.getId());
    }

    public void updateById2(TxMessagePO message) {
        jdbcTemplate.update("update " + tableName + " set send_status = ?, retry_times = ? , next_retry_time = ?"
                                + " where id = ? and deleted = 0",
            message.getSendStatus().getCode(), message.getRetryTimes(), message.getNextRetryTime(), message.getId());
    }

    private static class TxMessageRowMapper implements RowMapper<TxMessagePO> {
        @Override
        public TxMessagePO mapRow(ResultSet rs, int rowNum) throws SQLException {
            TxMessagePO txMessage = new TxMessagePO();
            txMessage.setContent(rs.getString("content"));
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



