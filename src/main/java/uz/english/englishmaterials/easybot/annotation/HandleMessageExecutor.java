package uz.english.englishmaterials.easybot.annotation;

import org.telegram.telegrambots.meta.api.objects.Update;
import uz.english.englishmaterials.entity.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandleMessageExecutor {
    private final Map<String, Method> messageHandlers = new HashMap<>();
    private final Object target;

    public HandleMessageExecutor(Object target) {
        this.target = target;
        registerMessageHandlers();
    }

    public boolean handle(Update update, User user) {
        String messageText = null;
        if (update.hasMessage()) {
            messageText = update.getMessage().getText();
        }
        Method handler = messageHandlers.get(messageText);
        if (handler != null) {
            try {
                handler.invoke(target, update, user);
                return true;
            } catch (IllegalAccessException | InvocationTargetException e) {
                return false;
            }
        }
        return false;
    }

    private void registerMessageHandlers() {
        for (Method method : target.getClass().getMethods()) {
            if (method.isAnnotationPresent(HandleMessage.class) && method.getParameterCount() == 2) {
                HandleMessage annotation = method.getAnnotation(HandleMessage.class);
                messageHandlers.put(annotation.value(), method);
            }
        }
    }
}
