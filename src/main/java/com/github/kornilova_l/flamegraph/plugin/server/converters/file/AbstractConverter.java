package com.github.kornilova_l.flamegraph.plugin.server.converters.file;

import java.util.HashMap;

public abstract class AbstractConverter {
    public abstract CFlamegraph getCFlamegraph();

    public Integer getId(HashMap<String, Integer> map, String name) {
        Integer id = map.get(name);
        if (id == null) {
            Integer newId = map.size();
            map.put(name, newId);
            return newId;
        }
        return id;
    }

    public String[] toArray(HashMap<String, Integer> names) {
        return names.keySet().toArray(new String[names.size()]);
    }
}
