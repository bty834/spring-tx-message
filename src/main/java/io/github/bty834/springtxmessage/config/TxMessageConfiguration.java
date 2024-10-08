package io.github.bty834.springtxmessage.config;

import io.github.bty834.springtxmessage.support.DefaultTxMessageCompensateSender;
import io.github.bty834.springtxmessage.support.DefaultTxMessageSender;
import io.github.bty834.springtxmessage.TxMessageCompensateSender;
import io.github.bty834.springtxmessage.support.TxMessageRepository;
import io.github.bty834.springtxmessage.TxMessageSendAdapter;
import io.github.bty834.springtxmessage.TxMessageSender;
import io.github.bty834.springtxmessage.utils.SnowFlake;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;


@Configuration
public class TxMessageConfiguration {

    @Bean
    @ConditionalOnBean({TxMessageSendAdapter.class, TxMessageRepository.class, SnowFlake.class})
    public TxMessageSender txMessageSender(TxMessageSendAdapter adapter, TxMessageRepository txMessageRepository, PropertyResolver propertyResolver, SnowFlake snowFlake) {
        return new DefaultTxMessageSender(adapter, txMessageRepository, propertyResolver, snowFlake);
    }

    @Bean
    @ConditionalOnBean({TxMessageSendAdapter.class, TxMessageRepository.class})
    public TxMessageCompensateSender txMessageCompensateSender(TxMessageSendAdapter txMessageSendAdapter, TxMessageRepository txMessageRepository, PropertyResolver propertyResolver) {
        return new DefaultTxMessageCompensateSender(txMessageSendAdapter, txMessageRepository, propertyResolver);
    }
}
