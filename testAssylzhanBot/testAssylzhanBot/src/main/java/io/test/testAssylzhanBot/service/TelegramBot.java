package io.test.testAssylzhanBot.service;
import io.test.testAssylzhanBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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

    //method to do action when update received from user
    @Override
    public void onUpdateReceived(Update update) {
        //a container for nearby locations

        ArrayList<String> places;
        //upon receive of geolocation tries to find nearby locations
        if (update.hasMessage() && update.getMessage().hasLocation()) {
            Location location = update.getMessage().getLocation();
            long chatId = update.getMessage().getChatId();
            try {
                places = getNearbyPlaces(config.getApiKey(), location.getLatitude(), location.getLongitude(), 50);
                sendPlaces(chatId, places);
            } catch (IOException e) {
                sendMessage(chatId, "Error while fetching nearest addresses.");
            }
        }
        //welcomes user by their name
        else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                String ans = "Welcome to this bot, " + update.getMessage().getChat().getFirstName() + "!!!" + "\n" + "Please send your geoposition!";
                sendMessageStart(chatId, ans);
            } else {
                sendMessage(chatId, "Sorry, command does not exist");
            }
        }
        // else tries to give the full text of location if button is pressed
        else if(update.hasCallbackQuery()){
            long mesId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            EditMessageText mes = new EditMessageText();
            mes.setMessageId((int)mesId);
            mes.setChatId(String.valueOf(chatId));
            int i = Integer.parseInt(update.getCallbackQuery().getData());
            mes.setText(getButtonText(update, i));
            try {
                execute(mes);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //method to create buttons with found nearby locations
    private void sendPlaces(long chatId, ArrayList<String> toSend) {
        SendMessage mes = new SendMessage();
        mes.setChatId(String.valueOf(chatId));
        //check for empty array
        if(toSend.isEmpty()){
            mes.setText("Please, choose another location! No nearby addresses were found.");
        }
        //else create a list of buttons with nearby locations
        else {
            mes.setText("Please, choose one of these addresses!");
            InlineKeyboardMarkup buttonInLine = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            for(int i = 1; i <= toSend.size() && i <= 3; i++){
                List<InlineKeyboardButton> row = new ArrayList<>();
                getButtonRow(row, toSend.get(i-1), i);
                rows.add(row);
            }

            buttonInLine.setKeyboard(rows);
            mes.setReplyMarkup(buttonInLine);
        }
        try {
            execute(mes);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //extracts address as text from chosen button by user
    private String getButtonText(Update update, int i){
        return update.getCallbackQuery().getMessage().getReplyMarkup().getKeyboard().get(--i).get(0).getText();
    }

    //method to send message
    private void sendMessage(long chatId, String toSend) {
        SendMessage mes = new SendMessage(String.valueOf(chatId), toSend);
        try {
            execute(mes);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //reply keyboard markup button to request user's location
    private void sendMessageStart(long chatId, String toSend) {
        KeyboardButton keyboardButton = new KeyboardButton("Send Location");
        keyboardButton.setRequestLocation(true);

        KeyboardRow r = new KeyboardRow();
        r.add(keyboardButton);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(r);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboardRows, true, false, false, null);

        SendMessage mes = new SendMessage(String.valueOf(chatId), toSend);
        mes.setReplyMarkup(keyboardMarkup);

        try {
            execute(mes);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //method to search for nearby locations via google maps
    private static ArrayList<String> getNearbyPlaces(String apiKey, double latitude, double longitude, int radius) throws IOException {
        final String NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + latitude + "," + longitude +
                "&radius=" + radius +
                "&key=" + apiKey +
                "&sensor=true" +
                "&language=ru";

        URL url = new URL(NEARBY_SEARCH_URL);
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
                String placeName = place.get("name").getAsString();
                String vic = place.get("vicinity").getAsString();
                String nearbyPlacesBuilder = vic + " (" + placeName + ")" ;
                ans.add(nearbyPlacesBuilder);
            }

        }
        return ans;
    }

    //method to create a button
    private void getButtonRow(List<InlineKeyboardButton> r, String s, int i){
        var button = new InlineKeyboardButton();
        if(!s.isEmpty()){
            button.setText(s);
        }else{
            button.setText("No address was found.");
        }
        button.setCallbackData(String.valueOf(i));
        r.add(button);
    }
}


