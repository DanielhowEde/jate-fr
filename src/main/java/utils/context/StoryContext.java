package utils.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StoryContext {
    private static final ThreadLocal<Map<String, Object>> storyData = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> storyMode = ThreadLocal.withInitial(() -> false);

    public static void put(String key, Object value) {
        storyData.get().put(key, value);
    }

    public static Object get(String key) {
        return storyData.get().get(key);
    }

    public static void clear() {
        storyData.get().clear();
        storyMode.set(false);
    }

    public static void enableStoryMode() {
        storyMode.set(true);
    }

    public static boolean isStoryMode() {
        return storyMode.get();
    }
    
    public static Set<String> getAllKeys() {
        return storyData.get().keySet();
    }
}