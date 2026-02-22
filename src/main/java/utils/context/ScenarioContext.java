package utils.context;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScenarioContext {
    private final Map<String, Object> data;

    public ScenarioContext() {
        data = new ConcurrentHashMap<>();
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }
    
    public Set<String> getAllKeys(){
    	return data.keySet();
    }
}
