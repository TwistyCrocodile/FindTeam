package com.findteam.findteam.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {

    @Bean
    @ConditionalOnExpression("'${telegram.bot-token:}' != ''")
    public TelegramBot telegramBot(@Value("${telegram.bot-token}") String botToken) {
        return new TelegramBot(botToken);
    }
}
