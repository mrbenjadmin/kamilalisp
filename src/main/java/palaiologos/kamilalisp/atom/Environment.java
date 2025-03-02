package palaiologos.kamilalisp.atom;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import palaiologos.kamilalisp.runtime.FunctionRegistry;

import java.math.MathContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Environment {
    public static BiMap<String, String> aliases = HashBiMap.create();
    private final HashMap<String, Atom> data;
    private final Environment parent;

    private Environment() {
        data = new HashMap<>();
        parent = null;
    }

    public Environment(Environment parent) {
        data = new HashMap<>();
        this.parent = parent;
    }

    public static Environment defaultEnvironment(boolean sandboxed) {
        Environment env = new Environment();
        FunctionRegistry.registerDefault(env, sandboxed);
        return env;
    }

    public void set(String key, Atom value) {
        data.put(key, value);
    }

    public void setPrimitive(String key, Atom value) {
        if (data.containsKey(key)) {
            throw new RuntimeException("Redefinition of built-in function " + key + ".");
        }
        data.put(key, value);
    }

    public void overwritePrimitive(String key, Atom value) {
        if (!data.containsKey(key))
            throw new RuntimeException("Cannot overwrite non-existent primitive " + key + ".");
        data.put(key, value);
    }

    public void setPrimitive(String key, String altkey, Atom value) {
        if (data.containsKey(key)) {
            throw new RuntimeException("Redefinition of built-in function " + key + ".");
        }
        if (data.containsKey(altkey)) {
            throw new RuntimeException("Redefinition of built-in function " + altkey + ".");
        }
        data.put(key, value);
        data.put(altkey, value);
        aliases.putIfAbsent(key, altkey);
    }

    public Atom get(String key) {
        if (data.containsKey(key)) {
            return data.get(key);
        } else if (parent != null) {
            return parent.get(key);
        } else {
            return Atom.NULL;
        }
    }

    private void recursiveKeys(Environment env, Set<String> keys) {
        keys.addAll(env.data.keySet());
        if (env.parent != null)
            recursiveKeys(env.parent, keys);
    }

    public Set<String> recursiveKeys() {
        Set<String> keys = new HashSet<>();
        recursiveKeys(this, keys);
        return keys;
    }

    public Set<String> keys() {
        return data.keySet();
    }

    public boolean has(String key) {
        return data.containsKey(key) || (parent != null && parent.has(key));
    }

    public boolean hasLocal(String key) {
        return data.containsKey(key);
    }

    public boolean isToplevel() {
        return parent == null || parent.parent == null;
    }

    public MathContext getMathContext() {
        int precision = get("fr").getReal().intValue();
        return new MathContext(precision);
    }

    public void remove(String key) {
        if (has(key))
            data.remove(key);
        else if (parent != null)
            parent.remove(key);
    }
}
