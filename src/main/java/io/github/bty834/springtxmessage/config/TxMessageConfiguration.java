package io.github.bty834.springtxmessage.config;

import io.github.bty834.springtxmessage.support.DefaultTxMessageCompensateSender;
import io.github.bty834.springtxmessage.support.DefaultTxMessageSender;
import io.github.bty834.springtxmessage.TxMessageCompensateSender;
import io.github.bty834.springtxmessage.support.TxMessageRepository;
import io.github.bty834.springtxmessage.TxMessageSendAdapter;
import io.github.bty834.springtxmessage.TxMessageSender;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;


@Configuration
@ConditionalOnBean({TxMessageSendAdapter.class, TxMessageRepository.class})
public class TxMessageConfiguration {

    @Bean
    @ConditionalOnProperty(value = TxMessageSender.ENABLED_KEY, havingValue = "true")
    public TxMessageSender txMessageSender(TxMessageSendAdapter adapter, TxMessageRepository txMessageRepository, PropertyResolver propertyResolver) {
        return new DefaultTxMessageSender(adapter, txMessageRepository, propertyResolver);
    }

    @Bean
    @ConditionalOnProperty(value = TxMessageCompensateSender.COMPENSATE_ENABLED_KEY, havingValue = "true")
    public TxMessageCompensateSender txMessageCompensateSender(TxMessageSendAdapter txMessageSendAdapter, TxMessageRepository txMessageRepository, PropertyResolver propertyResolver) {
        return new DefaultTxMessageCompensateSender(txMessageSendAdapter, txMessageRepository, propertyResolver);
    }
}
