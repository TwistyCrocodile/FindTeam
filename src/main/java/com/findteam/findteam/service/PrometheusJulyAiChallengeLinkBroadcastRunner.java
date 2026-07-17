package com.findteam.findteam.service;

import com.findteam.findteam.service.PrometheusJulyAiChallengeLinkBroadcastService.BroadcastResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
		name = "findteam.broadcast.prometheus-july-ai-link.enabled",
		havingValue = "true")
public class PrometheusJulyAiChallengeLinkBroadcastRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(PrometheusJulyAiChallengeLinkBroadcastRunner.class);

	private final PrometheusJulyAiChallengeLinkBroadcastService broadcastService;
	private final ConfigurableApplicationContext applicationContext;

	public PrometheusJulyAiChallengeLinkBroadcastRunner(
			PrometheusJulyAiChallengeLinkBroadcastService broadcastService,
			ConfigurableApplicationContext applicationContext) {
		this.broadcastService = broadcastService;
		this.applicationContext = applicationContext;
	}

	@Override
	public void run(ApplicationArguments args) {
		int exitCode = 0;
		try {
			BroadcastResult result = broadcastService.sendOnce();
			if (result.skipped()) {
				log.info("Prometheus July AI Challenge link broadcast one-off command finished without sending: already executed.");
			} else {
				log.info(
						"Prometheus July AI Challenge link broadcast one-off command finished: totalRecipients={}, successfulSends={}, failedSends={}",
						result.totalRecipients(),
						result.successfulSends(),
						result.failedSends());
			}
		} catch (Exception ex) {
			exitCode = 1;
			log.error("Prometheus July AI Challenge link broadcast one-off command failed before completion.", ex);
		} finally {
			int finalExitCode = exitCode;
			int springExitCode = SpringApplication.exit(applicationContext, () -> finalExitCode);
			System.exit(springExitCode);
		}
	}
}
