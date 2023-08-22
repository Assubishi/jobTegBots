package io.project.fliersUserBot.service;

import io.project.fliersUserBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TelegramBot(BotConfig config){
        this.config = config;
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if(message.equals("/start")){
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());

            } else {

                    startCommandReceived(chatId, "Sorry, such command does not exist");
            }
        }
    }


    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + ". Welcome to this fliers bot. \nPlease choose your language.";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long id, String ans)  {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(id));
        message.setText(ans);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        var kaz_button = new InlineKeyboardButton();
        String textButton = "\uD83C\uDDF0\uD83C\uDDFF" + " Қазақша" ;
        kaz_button.setText(textButton);
        kaz_button.setCallbackData("Kaz_button");

        var rus_button = new InlineKeyboardButton();
        textButton = "🇷🇺" + " Русский";
        rus_button.setText(textButton);
        rus_button.setCallbackData("Rus_button");
        row1.add(kaz_button);
        row2.add(rus_button);
        rowsInline.add(row1);
        rowsInline.add(row2);
        markup.setKeyboard(rowsInline);
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }
}
