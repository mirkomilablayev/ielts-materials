package uz.english.englishmaterials.easybot.configuration;

import org.telegram.telegrambots.meta.api.objects.Update;
import uz.english.englishmaterials.easybot.annotation.HandleMessageExecutor;
import uz.english.englishmaterials.easybot.annotation.HandleUndefinedExecutor;
import uz.english.englishmaterials.easybot.annotation.HandleUserStepExecutor;
import uz.english.englishmaterials.entity.User;

public class SmartBotConfiguration {
    private final HandleMessageExecutor handleMessageExecutor;
    private final HandleUserStepExecutor handleUserStepExecutor;
    private final HandleUndefinedExecutor handleUndefinedExecutor;
    // Add other handlers as needed

    public SmartBotConfiguration(Object botInstance) {
        handleMessageExecutor = new HandleMessageExecutor(botInstance);
        handleUserStepExecutor = new HandleUserStepExecutor(botInstance);
        handleUndefinedExecutor = new HandleUndefinedExecutor(botInstance);
    }

    public void handleUpdate(User user, Update update) {
        boolean handle = handleMessageExecutor.handle(update, user);
        if (!handle) {
            Boolean aBoolean = handleUserStepExecutor.handleUserStep(user, update);
            if (!aBoolean) {
                handleUndefinedExecutor.handle(update, user);
            }
        }
    }
}
