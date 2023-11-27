package uz.english.englishmaterials.easybot.annotation;

import org.telegram.telegrambots.meta.api.objects.Update;
import uz.english.englishmaterials.entity.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandleUserStepExecutor {
    private final Map<String, Method> userStepHandlers = new HashMap<>();
    private final Object botInstance;

    public HandleUserStepExecutor(Object botInstance) {
        this.botInstance = botInstance;
        registerUserStepHandlers();
    }

    public Boolean handleUserStep(User user, Update update) {
        Method handler = userStepHandlers.get(user.getStep());
        if (handler != null) {
            try {
                handler.invoke(botInstance, update, user);
                return true;
            } catch (IllegalAccessException | InvocationTargetException e) {
                return false;
            }
        }
        return false;
    }

    private void registerUserStepHandlers() {
        for (Method method : botInstance.getClass().getMethods()) {
            if (method.isAnnotationPresent(HandleUserStep.class) && method.getParameterCount() == 2) {
                HandleUserStep annotation = method.getAnnotation(HandleUserStep.class);
                userStepHandlers.put(annotation.value(), method);
            }
        }
    }
}
