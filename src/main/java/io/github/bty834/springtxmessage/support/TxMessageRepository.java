package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.model.SendStatus;
import io.github.bty834.springtxmessage.model.TxMessage;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.sql.DataSource;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class TxMessageRepository {

    private final DataSource dataSource;

    private final String tableName;

    private JdbcTemplate jdbcTemplate;

    @Setter
    private Integer limit = 1000;

    private Integer insertBatchSize = 1000;

    private static final RowMapper<TxMessage> ROW_MAPPER = new TxMessageRowMapper();

    public TxMessageRepository(DataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public TxMessage queryById(Long id) {
        String sql = "select * from " + tableName + " where id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    public TxMessage queryByMsgId(String msgId) {
        String sql = "select * from " + tableName + " where msg_id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, msgId);
    }

    public List<TxMessage> queryReadyToSendMessages(int maxRetryTimes, int delaySeconds) {
        LocalDate nextRetryTimeLte = LocalDate.now().minus(Duration.of(delaySeconds, ChronoUnit.SECONDS));
        String sql = "select * from " + tableName + " where send_status in (?,?) and next_retry_time <= and deleted = 0 ? limit ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, SendStatus.INIT, SendStatus.FAILED, nextRetryTimeLte, limit);
    }

    // save并回填id
    public void batchSave(List<TxMessage> messages) {
        jdbcTemplate.batchUpdate(" insert into " + tableName + " (topic, key, send_status, content, nextRetryTime)" +
                                     " values (?, ?, ?, ?, ?)",
            messages,
            insertBatchSize,
            (PreparedStatement ps, TxMessage msg) -> {
                ps.setString(1, msg.getTopic());
                ps.setString(2, msg.getKey());
                ps.setInt(3, msg.getSendStatus().getCode());
                ps.setString(4, msg.getContent());
                ps.setDate(5, Date.valueOf(msg.getNextRetryTime()));
            });
    }

    public void updateToSuccess(TxMessage message) {
        jdbcTemplate.update("update " + tableName + " set send_status = ?, msg_id = ? where topic =?  and status = ? and is_deleted = 0", SendStatus.SUCCESS.getCode(),message.getMsgId(), message.getTopic(), message.getSendStatus());
    }

    public void updateToFailed(TxMessage message) {
        jdbcTemplate.update("update " + tableName + " set send_status = ?, retry_times = ? , next_retry_time = ?"
                                + " where topic =?  and send_status = ? and is_deleted = 0",
            SendStatus.FAILED.getCode(),message.getRetryTimes(), message.getNextRetryTime(), message.getTopic(), message.getSendStatus());
    }

    private static class TxMessageRowMapper implements RowMapper<TxMessage> {
        @Override
        public TxMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            TxMessage txMessage = new TxMessage();
            txMessage.setContent(rs.getString("content"));
            txMessage.setNextRetryTime(rs.getDate("next_retry_time").toLocalDate());
            txMessage.setRetryTimes(rs.getInt("retry_times"));
            txMessage.setId(rs.getLong("id"));
            txMessage.setSendStatus(SendStatus.fromCode(rs.getInt("send_status")));
            txMessage.setUpdateTime(rs.getDate("update_time").toLocalDate());
            txMessage.setCreateTime(rs.getDate("create_time").toLocalDate());
            txMessage.setMsgId(rs.getString("msg_id"));
            txMessage.setTopic(rs.getString("topic"));
            txMessage.setKey(rs.getString("key"));
            txMessage.setDeleted(rs.getBoolean("deleted"));
            return txMessage;
        }
    }

}



