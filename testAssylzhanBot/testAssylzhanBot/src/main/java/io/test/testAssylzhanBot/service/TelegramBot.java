package io.test.testAssylzhanBot.service;
import io.test.testAssylzhanBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    final String apiKey= "AIzaSyCAmKukCkraQx42eiVu06NIQeyRuHtHYjI";
    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        ArrayList<String> places = new ArrayList<>();
        if (update.hasMessage() && update.getMessage().hasLocation()) {
            Location location = update.getMessage().getLocation();
            long chatId = update.getMessage().getChatId();
            try {
                places = getNearbyPlaces(apiKey, location.getLatitude(), location.getLongitude(), 50);
                sendPlaces(chatId, places);
            } catch (IOException e) {
                sendMessage(chatId, "Error while fetching nearest addresses.");
            }

        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":
                    String ans = "Welcome to this bot, " + update.getMessage().getChat().getFirstName() + "!!!" + "\n" + "Please send your geoposition!";
                    sendMessage(chatId, ans);
                    break;
                default:
                    sendMessage(chatId, "Sorry, command does not exist");
            }
        } else if(update.hasCallbackQuery()){
            String callBD = update.getCallbackQuery().getData();
            long mesId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            System.out.println(places);

            switch (callBD) {
                case "Call_1":
                    EditMessageText message = new EditMessageText();
                    message.setChatId(String.valueOf(chatId));
                    message.setText("Call1");
                    message.setMessageId((int)mesId);
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Call_2":
                    EditMessageText message2 = new EditMessageText();
                    message2.setChatId(String.valueOf(chatId));
                    message2.setText("Call2");
                    message2.setMessageId((int)mesId);
                    try {
                        execute(message2);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Call_3":
                    EditMessageText message3 = new EditMessageText();
                    message3.setChatId(String.valueOf(chatId));
                    message3.setText("Call3");
                    message3.setMessageId((int)mesId);
                    try {
                        execute(message3);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        }
    }

    private void sendPlaces(long chatId, ArrayList<String> toSend) {
        SendMessage mes = new SendMessage();
        mes.setChatId(String.valueOf(chatId));
        mes.setText("Please, choose one of these addresses!");
        InlineKeyboardMarkup buttonInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row = getButtonRow(row, toSend.get(0), 1);
        row2 = getButtonRow(row2, toSend.get(1), 2);
        row3 = getButtonRow(row3, toSend.get(2), 3);
        rows.add(row);
        rows.add(row2);
        rows.add(row3);

        buttonInLine.setKeyboard(rows);
        mes.setReplyMarkup(buttonInLine);
        try {
            execute(mes);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void sendMessage(long chatId, String toSend) {
        SendMessage mes = new SendMessage();
        mes.setChatId(String.valueOf(chatId));
        mes.setText(toSend);
            try {
                execute(mes);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
    }


    private static ArrayList<String> getNearbyPlaces(String apiKey, double latitude, double longitude, int radius) throws IOException {
        String nearbySearchUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + latitude + "," + longitude +
                "&radius=" + radius +
                "&key=" + apiKey;

        URL url = new URL(nearbySearchUrl);
        Scanner scanner = new Scanner(url.openStream());
        String response = scanner.useDelimiter("\\A").next();
        scanner.close();

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();
        ArrayList<String> ans = new ArrayList<>();
        if (jsonObject.has("results") && jsonObject.get("results").isJsonArray()) {
            JsonArray results = jsonObject.get("results").getAsJsonArray();
            for (JsonElement result : results) {
                JsonObject place = result.getAsJsonObject();
                JsonObject location = place.getAsJsonObject("geometry").getAsJsonObject("location");
                double placeLat = location.get("lat").getAsDouble();
                double placeLng = location.get("lng").getAsDouble();
                String placeName = place.get("name").getAsString();
                String vic = place.get("vicinity").getAsString();
                StringBuilder nearbyPlacesBuilder = new StringBuilder();
                nearbyPlacesBuilder.append("Place: ").append(placeName).append(", Vicinity: ").append(vic).append("\n");
                nearbyPlacesBuilder.append("Latitude: ").append(placeLat).append(", Longitude: ").append(placeLng).append("\n\n");
                ans.add(nearbyPlacesBuilder.toString());
            }

        }
        System.out.println(ans);
        return ans;
    }
    private List<InlineKeyboardButton>getButtonRow(List<InlineKeyboardButton> r, String s, int i){
        var button = new InlineKeyboardButton();
        button.setText(s);
        button.setCallbackData("Call_" + i);
        r.add(button);
        return r;
    }
}


