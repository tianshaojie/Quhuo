package cn.skyui.module.ugc.data.model.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tiansj on 15/4/4.
 */
public enum TerminalType implements Serializable {
    BROWSER(1),
    ANDROID(2),
    IOS(3);

    private static Map<Integer, TerminalType> map = new HashMap<Integer, TerminalType>();
    static {
        TerminalType[] values = TerminalType.values();
        for (TerminalType tt : values) {
            map.put(tt.getType(), tt);
        }
    }

    public static TerminalType getBytype(int terminalType) {
        return map.get(terminalType);
    }

    TerminalType(int type) {
        this.type = type;
    }

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
