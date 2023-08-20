package io.projectAdmin.telegramAdminBot.service;

import io.projectAdmin.telegramAdminBot.Config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


@Component
public class AdminTelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public AdminTelegramBot(BotConfig c){
        this.config = c;
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

            } else  {
                checCommandReceived(chatId, message);
            }
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name;
        sendMessage(chatId, answer);
    }

    private void checCommandReceived(long chatId, String name) {
        try{
            acceptChec(chatId, Integer.parseInt(name));
        } catch (NumberFormatException e){
            sendMessage(chatId, "There is something wrong in provided data");
        }

    }

    private void sendMessage(long id, String ans)  {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(id));
        message.setText(ans);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private void acceptChec(long chatId, long chec){
        String jdbcUrl = "jdbc:postgresql://localhost:5432/fliers"; // Update with your database details
        String username = "postgres"; // Update with your database username
        String password = "5169"; // Update with your database password


        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            String insertQuery = "INSERT INTO promo_codes (bill, salon_name, promcode) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            // Set values
            preparedStatement.setDouble(1, 100); // Example bill amount
            preparedStatement.setString(2, "Salon1"); // Example salon name
            preparedStatement.setString(3, "sdgfsdfgsdg"); // Convert chec to string

            // Execute the query
            int rowsAffected = preparedStatement.executeUpdate();
            String answer = rowsAffected + " row(s) inserted.";
            sendMessage(chatId, answer);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            sendMessage(chatId, "An error occurred while inserting data.");
            e.printStackTrace();
        }
    }

}
