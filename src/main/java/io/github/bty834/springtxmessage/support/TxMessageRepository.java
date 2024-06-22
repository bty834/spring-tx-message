package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.model.TxMessage;
import io.github.bty834.springtxmessage.model.TxMessageSendResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class TxMessageRepository {


    private final DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    private void init() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public TxMessage queryById(Long id){

        return new TxMessage();
    }

    public TxMessage queryByMsgId(String msgId){
        return new TxMessage();
    }

    public List<TxMessage> queryReadyToSendMessages(int maxRetryTimes, int delaySeconds) {
        return new ArrayList<>();
    }

    // save并回填id
    public void batchSave(List<TxMessage> messages) {

    }

    public void updateToSuccess(TxMessage message, TxMessageSendResult result){


    }

    public void updateToFailed(TxMessage message) {

    }


}
