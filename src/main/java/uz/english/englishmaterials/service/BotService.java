package uz.english.englishmaterials.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.english.englishmaterials.configuration.BotConfiguration;
import uz.english.englishmaterials.easybot.annotation.HandleMessage;
import uz.english.englishmaterials.easybot.annotation.HandleUndefined;
import uz.english.englishmaterials.easybot.annotation.HandleUserStep;
import uz.english.englishmaterials.easybot.configuration.SmartBotConfiguration;
import uz.english.englishmaterials.entity.User;
import uz.english.englishmaterials.util.ButtonConst;
import uz.english.englishmaterials.util.LifeStatusConst;
import uz.english.englishmaterials.util.Steps;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Component
public class BotService extends TelegramLongPollingBot {


    private static final SendMessage sendMessage = new SendMessage();


    private final BotConfiguration botConfiguration;
    private final LogicService logicService;
    private final ButtonService buttonService;
    private final InlineButtonService inlineButtonService;

    private static final String EXAMPLE_PHOTO_ID = "AgACAgIAAxkBAAOnZT-JOYPWnYm22p99zbqUKjWz_zgAAh_OMRsNZAABSjKggbtv7P4LAQADAgADeQADMAQ";

    @Override
    public String getBotUsername() {
        return this.botConfiguration.getUsername();
    }

    @Override
    public String getBotToken() {
        return this.botConfiguration.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        User currentUser = logicService.createUser(update);
        String chatId = logicService.getChatId(update);
        sendMessage.setChatId(chatId);
        SmartBotConfiguration smartBotConfiguration = new SmartBotConfiguration(this);
        smartBotConfiguration.handleUpdate(currentUser, update);
    }


    @HandleMessage("/start")
    public void start(Update update, User user) {
        sendMessage.setText("Telefon Raqamingizni yuboring");
        sendMessage.setReplyMarkup(buttonService.shareContactButton());
        user.setStep(Steps.ASK_CONTACT);
        logicService.updateUser(user);
        sendMessageExecutor(sendMessage);
    }
    @HandleUserStep(Steps.ASK_CONTACT)
    public void registeredPage(Update update, User user) throws TelegramApiException {

    }

    @HandleMessage(ButtonConst.SINGLE)
    public void singleButton(Update update, User user) throws TelegramApiException {

    }




    @HandleUndefined
    public void undefined(Update update, User user) {
        sendMessage.setText("Xato buyruq kiritildi");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private SendMessage sendMessage(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        return sendMessage;
    }


    public void sendMessageExecutor(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Yuborilmadi");
        }
    }

    public void sendVideoExecutor(SendVideo sendVideo) {
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            System.out.println("Yuborilmadi");
        }
    }

    public void sendLocationExecutor(SendLocation sendLocation) {
        try {
            execute(sendLocation);
        } catch (TelegramApiException e) {
            System.out.println("Yuborilmadi");
        }
    }

    public void sendDocumentExecutor(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            System.out.println("Yuborilmadi");
        }
    }

    public void sendPhotoExecutor(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            System.out.println("Yuborilmadi");
        }
    }

    public String getFileId(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasPhoto()) {
                List<PhotoSize> photos = message.getPhoto();
                PhotoSize largestPhoto = photos.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
                return largestPhoto != null ? largestPhoto.getFileId() : "";
            }
            return "";
        }
        return "";
    }

    public java.io.File getFileById(String fileId) {
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = execute(getFile);
            String fileDownloadUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
            InputStream in = new URL(fileDownloadUrl).openStream();
            java.io.File downloadedFile = java.io.File.createTempFile("downloaded-file", ".jpg");
            OutputStream out = Files.newOutputStream(downloadedFile.toPath());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return downloadedFile;
        } catch (Exception e) {
            return null;
        }
    }


}
