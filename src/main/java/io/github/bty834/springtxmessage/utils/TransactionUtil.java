package io.github.bty834.springtxmessage.utils;

import java.util.function.Consumer;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionUtil {

    public static void executeAfterCommit(Runnable runnable , Consumer<Exception> exceptionConsumer) {
        // 有事务，注册Synchronization，事务提交后执行
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronization transactionSynchronization = new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        exceptionConsumer.accept(e);
                    }
                }
            };
            // 注册Synchronization
            TransactionSynchronizationManager.registerSynchronization(transactionSynchronization);
            return;
        }
        // 无事务直接执行
        runnable.run();
    }
}
