package com.findteam.findteam.service;

import com.findteam.findteam.model.PreferredLanguage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.springframework.stereotype.Service;

@Service
public class TelegramStartCommandHandler {

    private static final String START_COMMAND = "/start";
    private static final String LANGUAGE_RU = "LANGUAGE_RU";
    private static final String LANGUAGE_EN = "LANGUAGE_EN";
    private static final String LANGUAGE_SELECTION_MESSAGE = "Choose language / Выберите язык";
    private static final String LANGUAGE_SELECTED_RU_MESSAGE = "Language selected: Russian / Язык выбран: русский";
    private static final String LANGUAGE_SELECTED_EN_MESSAGE = "Language selected: English / Язык выбран: английский";
    private static final String START_MESSAGE_RU = """
            🚀 Добро пожаловать в FindTeam!

            FindTeam — это Telegram Mini App для поиска команды в IT-проекты, стартапы, хакатоны, олимпиады, MVP и pet-проекты.

            Здесь вы можете:

            👨‍💻 Найти разработчиков, дизайнеров, маркетологов и PM'ов для своего проекта

            🔍 Найти команду, если хотите присоединиться к интересному проекту, стартапу, хакатону, олимпиаде, MVP или pet-проекту

            🤝 Откликаться на проекты и связываться с участниками напрямую через Telegram

            📢 Публиковать собственные объявления о поиске команды или поиске проекта

            ━━━━━━━━━━━━━━

            📢 Новости проекта

            Следить за развитием FindTeam, новыми обновлениями и историей создания проекта можно в нашем Telegram-канале:

            https://t.me/FindTeamIT
            """;
    private static final String START_MESSAGE_EN = """
            🚀 Welcome to FindTeam!

            FindTeam is a Telegram Mini App for finding teammates for IT projects, startups, hackathons, olympiads, MVPs, and pet projects.

            Here you can:

            👨‍💻 Find developers, designers, marketers, and PMs for your project

            🔍 Find a team if you want to join an interesting project, startup, hackathon, olympiad, MVP, or pet project

            🤝 Apply to projects and contact participants directly through Telegram

            📢 Publish your own posts when you are looking for a team or a project

            ━━━━━━━━━━━━━━

            📢 Project news

            Follow FindTeam development, new updates, and the story behind the project in our Telegram channel:

            https://t.me/FindTeamIT
            """;

    private final UserService userService;

    public TelegramStartCommandHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Update update, TelegramBot bot) {
        if (update.callbackQuery() != null) {
            handleCallbackQuery(update.callbackQuery(), bot);
            return;
        }

        Message message = update.message();
        if (message == null || message.text() == null || message.chat() == null) {
            return;
        }

        if (!isStartCommand(message.text())) {
            return;
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton("Русский").callbackData(LANGUAGE_RU),
                new InlineKeyboardButton("English").callbackData(LANGUAGE_EN)
        );

        bot.execute(new SendMessage(message.chat().id(), LANGUAGE_SELECTION_MESSAGE).replyMarkup(keyboard));
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        if (callbackQuery.id() != null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
        }

        Message message = callbackQuery.message();
        if (message == null || message.chat() == null) {
            return;
        }

        if (!LANGUAGE_SELECTION_MESSAGE.equals(message.text())) {
            return;
        }

        LanguageSelection selection = switch (callbackQuery.data()) {
            case LANGUAGE_RU -> new LanguageSelection(
                    PreferredLanguage.RU,
                    LANGUAGE_SELECTED_RU_MESSAGE,
                    START_MESSAGE_RU
            );
            case LANGUAGE_EN -> new LanguageSelection(
                    PreferredLanguage.EN,
                    LANGUAGE_SELECTED_EN_MESSAGE,
                    START_MESSAGE_EN
            );
            default -> null;
        };

        if (selection == null) {
            return;
        }

        if (callbackQuery.from() == null || callbackQuery.from().id() == null) {
            return;
        }

        BaseResponse editResponse = bot.execute(new EditMessageText(
                message.chat().id(),
                message.messageId(),
                selection.selectedMessage()
        ));

        if (editResponse.isOk()) {
            userService.savePreferredLanguage(callbackQuery.from().id(), selection.preferredLanguage());
            bot.execute(new SendMessage(message.chat().id(), selection.welcomeMessage()));
        }
    }

    private boolean isStartCommand(String text) {
        String command = text.trim().split("\\s+", 2)[0];
        String commandName = command.split("@", 2)[0];
        return START_COMMAND.equals(commandName);
    }

    private record LanguageSelection(
            PreferredLanguage preferredLanguage,
            String selectedMessage,
            String welcomeMessage
    ) {
    }
}
