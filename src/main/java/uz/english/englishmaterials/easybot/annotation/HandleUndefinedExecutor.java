package uz.english.englishmaterials.easybot.annotation;

import org.telegram.telegrambots.meta.api.objects.Update;
import uz.english.englishmaterials.entity.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandleUndefinedExecutor {
    private final Map<String, Method> messageHandlers = new HashMap<>();
    private final Object target;

    public HandleUndefinedExecutor(Object target) {
        this.target = target;
        registerMessageHandlers();
    }

    public void handle(Update update, User user) {
        Method handler = messageHandlers.get("key");
        if (handler != null) {
            try {
                handler.invoke(target, update, user);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerMessageHandlers() {
        for (Method method : target.getClass().getMethods()) {
            if (method.isAnnotationPresent(HandleUndefined.class) && method.getParameterCount() == 2) {
                messageHandlers.put("key", method);
            }
        }
    }
}


