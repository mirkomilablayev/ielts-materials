package uz.english.englishmaterials.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.english.englishmaterials.configuration.BotConfiguration;
import uz.english.englishmaterials.easybot.annotation.HandleMessage;
import uz.english.englishmaterials.easybot.annotation.HandleUndefined;
import uz.english.englishmaterials.easybot.annotation.HandleUserStep;
import uz.english.englishmaterials.easybot.configuration.SmartBotConfiguration;
import uz.english.englishmaterials.entity.Applicant;
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
        SmartBotConfiguration smartBotConfiguration = new SmartBotConfiguration(this);
        smartBotConfiguration.handleUpdate(currentUser, update);
    }


    @HandleMessage("/start")
    public void start(Update update, User user) {
        SendMessage sendMessage = sendMessage(user.getChatId());

        if (user.getIsAdmin()) {
            sendMessage.setText("ADMIN MAIN MENU");
            sendMessage.setReplyMarkup(buttonService.adminMainMenuButton());
            user.setStep(Steps.REGISTERED);
        } else {
            sendMessage.setText("Assalomu alaykum, Tanlovingizni qiling va gren kartada ishtirok eting");
            sendMessage.setReplyMarkup(buttonService.userMainMenuButton());
            user.setStep(Steps.REGISTERED);
            logicService.closeNotCompletedApplicant(user);
        }
        logicService.updateUser(user);
        sendMessageExecutor(sendMessage);
    }

    @HandleMessage(ButtonConst.SINGLE)
    public void singleButton(Update update, User user) throws TelegramApiException {
        SendMessage sendMessage = sendMessage(user.getChatId());
        sendMessage.setText("Siz yolg'iz o'ynashni tanladingiz!\n" +
                "Ism familiyangizni kiriting | Masalan: Palonchayev Pistoncha");
        sendMessage.setReplyMarkup(buttonService.rejectButton());

        logicService.closeNotCompletedApplicant(user);
        Applicant applicant = new Applicant();
        applicant.setApplicantId(user.getId());
        applicant.setSingle(true);
        applicant.setActive(true);
        applicant.setCompleted(false);
        logicService.saveApplicant(applicant);

        user.setStep(Steps.FULL_NAME_ASK);
        logicService.updateUser(user);
        sendMessageExecutor(sendMessage);
    }

    @HandleMessage(ButtonConst.FAMILY)
    public void familyButton(Update update, User user) throws TelegramApiException {
        SendMessage sendMessage = sendMessage(user.getChatId());
        sendMessage.setText("Siz Oilaviy o'ynashni tanladingiz!\n" +
                "Ism familiyangizni kiriting | Masalan: Palonchayev Pistoncha");
        sendMessage.setReplyMarkup(buttonService.rejectButton());

        logicService.closeNotCompletedApplicant(user);
        Applicant applicant = new Applicant();
        applicant.setApplicantId(user.getId());
        applicant.setActive(true);
        applicant.setCompleted(false);
        applicant.setSingle(false);
        logicService.saveApplicant(applicant);

        user.setStep(Steps.FULL_NAME_ASK);
        logicService.updateUser(user);
        sendMessageExecutor(sendMessage);
    }


    @HandleUserStep(Steps.FULL_NAME_ASK)
    public void registeredPage(Update update, User user) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = logicService.getText(update);
        Applicant activeApplicant = logicService.getActiveApplicant(user);
        if (activeApplicant.getId() == null) {
            rejectButton(update, user);
            sendMessage.setText("Ariza topilmadi!");
            sendMessageExecutor(sendMessage);
            return;
        }
        activeApplicant.setFullName(text);
        logicService.saveApplicant(activeApplicant);
        user.setStep(Steps.GENDER_ASK);

        sendMessage.setText("Jinsingizni Tanlang!");
        sendMessage.setReplyMarkup(buttonService.genderListButton());
        sendMessageExecutor(sendMessage);
    }

    @HandleMessage("ERKAK")
    public void genderMale(Update update, User user) {
        saveGender(update, user, "ERKAK");
    }

    @HandleMessage("AYOL")
    public void genderFemaleMale(Update update, User user) {
        saveGender(update, user, "AYOL");
    }

    public void saveGender(Update update, User user, String gender) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setText("Tug'ilgan Kuningizni kiriting, SANA.OY.YIL - 05.02.2003");
        sendMessage.setReplyMarkup(buttonService.rejectButton());
        user.setStep(Steps.ASK_BIRTHDAY);
        logicService.updateUser(user);
        Applicant applicant = logicService.getActiveApplicant(user);
        if (applicant.getId() == null) {
            rejectButton(update, user);
            sendMessage.setText("Ariza topilmadi!");
            sendMessageExecutor(sendMessage);
            return;
        }
        applicant.setGender(gender);
        logicService.saveApplicant(applicant);
        sendMessageExecutor(sendMessage);
    }

    @HandleUserStep(Steps.ASK_BIRTHDAY)
    public void saveBirthdate(Update update, User user) {
        String text = logicService.getText(update);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setText("Tug'ilgan Joyingiz?");
        sendMessage.setReplyMarkup(buttonService.rejectButton());
        user.setStep(Steps.ASK_BIRTHPLACE);
        logicService.updateUser(user);
        Applicant applicant = logicService.getActiveApplicant(user);
        if (applicant.getId() == null) {
            rejectButton(update, user);
            sendMessage.setText("Ariza topilmadi!");
            sendMessageExecutor(sendMessage);
            return;
        }
        applicant.setBirthDay(text);
        logicService.saveApplicant(applicant);
        sendMessageExecutor(sendMessage);
    }

    @HandleUserStep(Steps.ASK_BIRTHPLACE)
    public void saveBirthPlace(Update update, User user) {
        String text = logicService.getText(update);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setText("Tug'ilgan Davlatingiz?");
        sendMessage.setReplyMarkup(buttonService.rejectButton());
        user.setStep(Steps.ASK_BIRTH_COUNTRY);
        logicService.updateUser(user);
        Applicant applicant = logicService.getActiveApplicant(user);
        if (applicant.getId() == null) {
            rejectButton(update, user);
            sendMessage.setText("Ariza topilmadi!");
            sendMessageExecutor(sendMessage);
            return;
        }
        applicant.setBirthPlace(text);
        logicService.saveApplicant(applicant);
        sendMessageExecutor(sendMessage);
    }

    @HandleUserStep(Steps.ASK_BIRTH_COUNTRY)
    public void saveBirthCountry(Update update, User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        Applicant applicant = logicService.getActiveApplicant(user);
        if (applicant.getId() == null) {
            rejectButton(update, user);
            sendMessage.setText("Ariza topilmadi!");
            sendMessageExecutor(sendMessage);
            return;
        }
        String text = logicService.getText(update);
        applicant.setBirthCountry(text);
        logicService.saveApplicant(applicant);

        user.setStep(Steps.ASK_PHOTO);
        logicService.updateUser(user);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(user.getChatId());
        sendPhoto.setCaption("Yuqoridagidek shaklda rasmingizni yuboring");
        sendPhoto.setPhoto(new InputFile(getFileById(EXAMPLE_PHOTO_ID)));
        sendPhoto.setReplyMarkup(buttonService.rejectButton());
        sendPhotoExecutor(sendPhoto);
    }


    @HandleUserStep(Steps.ASK_PHOTO)
    public void saveUserPhoto(Update update, User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        Applicant applicant = logicService.getActiveApplicant(user);
        if (applicant.getId() == null) {
            rejectButton(update, user);
            sendMessage.setText("Ariza topilmadi!");
            sendMessageExecutor(sendMessage);
            return;
        }

        String fileId = getFileId(update);
        if (fileId.length() == 0) {
            sendMessage.setText("Iltimos rasm yuboring!");
            sendMessageExecutor(sendMessage);
            return;
        }
        applicant.setPhotoId(fileId);
        logicService.saveApplicant(applicant);

        user.setStep(Steps.ASK_EDUCATION_LEVEL);
        logicService.updateUser(user);

        sendMessage.setText("Talim dajarangizni kiriting\nO'rta\nOliy\nMagistr ...");
        sendMessage.setReplyMarkup(buttonService.rejectButton());
        sendMessageExecutor(sendMessage);
    }

    @HandleUserStep(Steps.ASK_EDUCATION_LEVEL)
    public void saveEducationLevel(Update update, User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        Applicant applicant = logicService.getActiveApplicant(user);
        if (applicant.getId() == null) {
            rejectButton(update, user);
            sendMessage.setText("Ariza topilmadi!");
            sendMessageExecutor(sendMessage);
            return;
        }
        String text = logicService.getText(update);
        applicant.setEducationStatus(text);
        logicService.saveApplicant(applicant);


        if (applicant.getSingle()) {
            user.setStep(Steps.REGISTERED);
            logicService.updateUser(user);
            SendPhoto sendPhoto = getSendPhoto(user, applicant);
            sendPhoto.setReplyMarkup(buttonService.choiceButton());
            sendPhotoExecutor(sendPhoto);
        } else {
            user.setStep(Steps.ASK_LIFE_STATUS);
            logicService.updateUser(user);
            sendMessage.setText("Oilaviy Xolatingizni yuboring");
            sendMessage.setReplyMarkup(inlineButtonService.lifeStatusInlineKeyBoar(Arrays.asList(LifeStatusConst.UNMARRIED, LifeStatusConst.MARRIED, LifeStatusConst.DIVORCED)));
            sendMessageExecutor(sendMessage);
        }
    }

    private SendPhoto getSendPhoto(User user, Applicant applicant) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(user.getChatId());
        sendPhoto.setPhoto(new InputFile(getFileById(applicant.getPhotoId())));
        sendPhoto.setCaption(
                "\nAriza Turi <b>" + (applicant.getSingle() ? "YOLG'IZ" : "OILAVIY") + "</b>" +
                        "\nIsm-Familiya <b>" + applicant.getFullName() + "</b>" +
                        "\nJinsi <b>" + applicant.getGender() + "</b>" +
                        "\nTug'ilgan sana <b>" + applicant.getBirthDay() + "</b>" +
                        "\nTug'ilgan joy <b>" + applicant.getBirthPlace() + "</b>" +
                        "\nTug'ilgan davlat <b>" + applicant.getBirthCountry() + "</b>" +
                        "\nTa'lim darajangiz <b>" + applicant.getEducationStatus() + "</b>"
        );
        sendPhoto.setParseMode("HTML");
        return sendPhoto;
    }


    @HandleUserStep(Steps.ASK_LIFE_STATUS)
    public void saveLifeStatus(Update update, User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        Applicant applicant = logicService.getActiveApplicant(user);
        if (applicant.getId() == null) {
            rejectButton(update, user);
            sendMessage.setText("Ariza topilmadi!");
            sendMessageExecutor(sendMessage);
            return;
        }
        String text;
        if (update.hasCallbackQuery()){
            text = String.valueOf(update.getCallbackQuery().getMessage().getDate());
        }else {
            sendMessage.setText("Iltimos Oilaviy Xolatingizni belgilang");
            sendMessage.setReplyMarkup(inlineButtonService.lifeStatusInlineKeyBoar(Arrays.asList(LifeStatusConst.UNMARRIED, LifeStatusConst.MARRIED, LifeStatusConst.DIVORCED)));
            sendMessageExecutor(sendMessage);
            return;
        }
        applicant.setLifeStatus(text);
        logicService.saveApplicant(applicant);
        user.setStep(Steps.ASK_CHILD_COUNT);
        logicService.updateUser(user);
        sendMessage.setText("Farzandlaringizni sonini kiriting, 1 yoki 2 manashu shaklda");
        sendMessage.setReplyMarkup(buttonService.rejectButton());
        sendMessageExecutor(sendMessage);
    }




    @HandleMessage(ButtonConst.SEND)
    public void sendButton(Update update, User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());

        Applicant applicant = logicService.getActiveApplicant(user);
        if (applicant.getId() == null) {
            sendMessage.setText("Ariza topilmadi qaytadan urinib ko'ring");
            sendMessage.setReplyMarkup(buttonService.userMainMenuButton());
            user.setStep(Steps.REGISTERED);
            logicService.closeNotCompletedApplicant(user);
            logicService.updateUser(user);
            sendMessageExecutor(sendMessage);
            return;
        }

        applicant.setActive(false);
        applicant.setCompleted(true);
        logicService.saveApplicant(applicant);
        sendMessage.setText("Sizning arizangiz yuborildi va sizga yaqin vaqt ichida javob keladi");
        sendMessage.setReplyMarkup(buttonService.userMainMenuButton());
        user.setStep(Steps.REGISTERED);
        logicService.closeNotCompletedApplicant(user);
        logicService.updateUser(user);
        sendMessageExecutor(sendMessage);
    }

    @HandleMessage(ButtonConst.REJECT_BUTTON)
    public void rejectButton(Update update, User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setText("Assalomu alaykum, Tanlovingizni qiling va gren kartada ishtirok eting");
        sendMessage.setReplyMarkup(buttonService.userMainMenuButton());
        user.setStep(Steps.REGISTERED);
        logicService.closeNotCompletedApplicant(user);
        logicService.updateUser(user);

        sendMessageExecutor(sendMessage);
    }

    @HandleUndefined
    public void undefined(Update update, User user) {
        SendMessage sendMessage = sendMessage(user.getChatId());
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
