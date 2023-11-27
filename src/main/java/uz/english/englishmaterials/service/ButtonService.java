package uz.english.englishmaterials.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.english.englishmaterials.util.ButtonConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ButtonService {

    public ReplyKeyboardMarkup shareContactButton() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestContact(true);
        keyboardButton.setText(ButtonConst.SHARE_CONTACT);
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboardRow(new KeyboardRow(new ArrayList<>(Collections.singletonList(keyboardButton)))).build();
    }

    public ReplyKeyboardMarkup shareLocationBot() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestLocation(true);
        keyboardButton.setText(ButtonConst.SHARE_LOCATION);
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboardRow(new KeyboardRow(new ArrayList<>(Collections.singletonList(keyboardButton)))).build();
    }

    public ReplyKeyboardRemove createEmptyButton() {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        return keyboardRemove;
    }


    public ReplyKeyboardMarkup createCustomButtons(int countPerLine, List<String> buttonNames) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        int i = 0;
        int counter = 0;
        int qoldiq = buttonNames.size() % countPerLine;
        int size = buttonNames.size();
        for (String name : buttonNames) {
            keyboardRow.add(name);
            i++;
            if (i == countPerLine || (size - counter == qoldiq && i == qoldiq)) {
                keyboardRowList.add(keyboardRow);
                keyboardRow = new KeyboardRow();
                counter += i;
                i = 0;
            }
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }


    private static ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        return replyKeyboardMarkup;
    }


    public ReplyKeyboard userMainMenuButton() {
        return createCustomButtons(2, Arrays.asList(ButtonConst.FAMILY, ButtonConst.SINGLE));
    }

    public ReplyKeyboard adminMainMenuButton() {
        return createCustomButtons(2, Arrays.asList(ButtonConst.APPLICATION, ButtonConst.STATISTICS, ButtonConst.SEND_CONFIRM_NUMBER));
    }


    public ReplyKeyboard genderListButton() {
        return createCustomButtons(2, Arrays.asList("ERKAK", "AYOL", ButtonConst.REJECT_BUTTON));
    }
    public ReplyKeyboard choiceButton() {
        return createCustomButtons(2, Arrays.asList(ButtonConst.SEND, ButtonConst.REJECT_BUTTON));
    }

    public ReplyKeyboard rejectButton() {
        return createCustomButtons(1, Collections.singletonList(ButtonConst.REJECT_BUTTON));
    }

}
