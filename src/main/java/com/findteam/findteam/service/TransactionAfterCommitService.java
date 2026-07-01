package com.findteam.findteam.service;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TransactionAfterCommitService {

	private static final Logger log = LoggerFactory.getLogger(TransactionAfterCommitService.class);

	public void runAfterCommit(Runnable action) {
		Runnable asyncAction = () -> CompletableFuture.runAsync(() -> {
			try {
				action.run();
			} catch (Exception ex) {
				log.warn("After-commit action failed: exception={}, message={}", ex.getClass().getSimpleName(), ex.getMessage());
			}
		});

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			asyncAction.run();
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				asyncAction.run();
			}
		});
	}
}
