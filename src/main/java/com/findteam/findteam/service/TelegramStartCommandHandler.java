package com.findteam.findteam.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;

@Service
public class TelegramStartCommandHandler {

    private static final String START_COMMAND = "/start";
    private static final String START_MESSAGE = """
            🚀 Добро пожаловать в FindTeam!

            FindTeam — это Telegram Mini App для поиска команды в IT-проекты и стартапы.

            Здесь вы можете:

            👨‍💻 Найти разработчиков, дизайнеров, маркетологов и PM'ов для своего проекта

            🔍 Найти команду, если хотите присоединиться к интересному стартапу или pet-проекту

            🤝 Откликаться на проекты и связываться с участниками напрямую через Telegram

            📢 Публиковать собственные объявления о поиске команды или поиске проекта

            ━━━━━━━━━━━━━━

            ⚠️ Важно

            Сейчас FindTeam находится на стадии MVP (ранней версии продукта).

            Первый запуск приложения может занять 1–2 минуты из-за особенностей бесплатного хостинга. Пожалуйста, не закрывайте приложение сразу, если загрузка занимает больше времени, чем обычно.

            После запуска приложение работает значительно быстрее.
            """;

    public void handle(Update update, TelegramBot bot) {
        Message message = update.message();
        if (message == null || message.text() == null || message.chat() == null) {
            return;
        }

        if (!isStartCommand(message.text())) {
            return;
        }

        bot.execute(new SendMessage(message.chat().id(), START_MESSAGE));
    }

    private boolean isStartCommand(String text) {
        String command = text.trim().split("\\s+", 2)[0];
        String commandName = command.split("@", 2)[0];
        return START_COMMAND.equals(commandName);
    }
}
