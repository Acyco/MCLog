package cn.acyco.mclog.core;

/**
 * @author Acyco
 * @create 2022-01-15 04:58
 * @url https://acyco.cn
 */
public enum BlockActionType {
    BREAK(0),
    PLACE(1),
    CLICK(2)
    ;
    private int value;

    BlockActionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

