package com.findteam.findteam.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class TelegramBotService {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    private final ObjectProvider<TelegramBot> telegramBotProvider;
    private final TelegramStartCommandHandler startCommandHandler;
    private TelegramBot telegramBot;

    public TelegramBotService(
            ObjectProvider<TelegramBot> telegramBotProvider,
            TelegramStartCommandHandler startCommandHandler
    ) {
        this.telegramBotProvider = telegramBotProvider;
        this.startCommandHandler = startCommandHandler;
    }

    @PostConstruct
    public void start() {
        telegramBot = telegramBotProvider.getIfAvailable();
        if (telegramBot == null) {
            log.info("Telegram bot is disabled because TELEGRAM_BOT_TOKEN is not configured.");
            return;
        }

        telegramBot.setUpdatesListener(updates -> {
            updates.forEach(update -> startCommandHandler.handle(update, telegramBot));
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        log.info("Telegram bot long polling started.");
    }

    @PreDestroy
    public void stop() {
        if (telegramBot != null) {
            telegramBot.removeGetUpdatesListener();
            log.info("Telegram bot long polling stopped.");
        }
    }
}
