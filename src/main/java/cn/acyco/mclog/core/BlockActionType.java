package cn.acyco.mclog.core;

/**
 * @author Acyco
 * @create 2022-01-15 04:58
 * @url https://acyco.cn
 */
public enum BlockActionType {

    BREAK(0), //移除
    PLACE(1),// 放置
    CLICK(2), // 交互或点击
    EXPLODE(3), // 爆炸
    IGNITE(4) // 点燃
    ;

    private int value;

    BlockActionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

