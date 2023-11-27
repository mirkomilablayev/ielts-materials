package uz.english.englishmaterials.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.english.englishmaterials.entity.Applicant;
import uz.english.englishmaterials.entity.ApplicantRepository;
import uz.english.englishmaterials.entity.User;
import uz.english.englishmaterials.entity.UserRepository;
import uz.english.englishmaterials.util.Steps;

import java.util.Optional;

@Service
@AllArgsConstructor
public class LogicService {
    private final UserRepository userRepository;
    private final ApplicantRepository applicantRepository;

    public User createUser(Update update) {
        String realId = getRealId(update);
        org.telegram.telegrambots.meta.api.objects.User tgUser = getUser(update);
        String chatId = getChatId(update);

        Optional<User> userOptional = userRepository.findByChatId(realId);
        if (!userOptional.isPresent()) {
            User user = new User();
            user.setIsAdmin(Boolean.FALSE);
            user.setChatId(chatId);
            user.setRealId(realId);
            user.setFullName(tgUser.getFirstName() + (tgUser.getLastName() == null ? "" : " " + tgUser.getLastName()));
            user.setPage(Steps.REGISTERED);
            return userRepository.save(user);
        }
        return userOptional.get();
    }
    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void saveApplicant(Applicant applicant){
        applicantRepository.save(applicant);
    }

    public void closeNotCompletedApplicant(User user){
        for (Applicant applicant : applicantRepository.findAllByApplicantIdAndActive(user.getId(), true)) {
            if (!applicant.getCompleted()){
                applicant.setActive(false);
                applicantRepository.save(applicant);
            }
        }
    }

    public Applicant getActiveApplicant(User user){
        return applicantRepository.findByApplicantIdAndActiveAndCompleted(user.getId(), true, false).orElse(new Applicant());
    }


    public String getRealId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId().toString();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getFrom().getId().toString();
        } else if (update.hasChatMember()) {
            return update.getChatMember().getFrom().getId().toString();
        } else if (update.hasChosenInlineQuery()) {
            return update.getChosenInlineQuery().getFrom().getId().toString();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getFrom().getId().toString();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getFrom().getId().toString();
        } else if (update.hasMyChatMember()) {
            return update.getMyChatMember().getFrom().getId().toString();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom().getId().toString();
        } else if (update.hasPollAnswer()) {
            return update.getPollAnswer().getUser().getId().toString();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom().getId().toString();
        } else if (update.hasShippingQuery()) {
            return update.getShippingQuery().getFrom().getId().toString();
        }
        return "";
    }

    private org.telegram.telegrambots.meta.api.objects.User getUser(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getFrom();
        } else if (update.hasChatMember()) {
            return update.getChatMember().getFrom();
        } else if (update.hasChosenInlineQuery()) {
            return update.getChosenInlineQuery().getFrom();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getFrom();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getFrom();
        } else if (update.hasMyChatMember()) {
            return update.getMyChatMember().getFrom();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom();
        } else if (update.hasPollAnswer()) {
            return update.getPollAnswer().getUser();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom();
        } else if (update.hasShippingQuery()) {
            return update.getShippingQuery().getFrom();
        }
        return new org.telegram.telegrambots.meta.api.objects.User();
    }


    public String getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getChatId().toString();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getChatId().toString();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getChatId().toString();
        } else {
            return update.getMessage().getChatId().toString();
        }
    }

    public String getText(@NonNull Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().getText() != null) {
                return update.getMessage().getText();
            }
            return "error_text";
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getText();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getText();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getText();
        }
        return "";
    }


}