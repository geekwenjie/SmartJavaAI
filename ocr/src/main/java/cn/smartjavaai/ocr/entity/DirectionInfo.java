package cn.smartjavaai.ocr.entity;

/**
 * 方向检测结果
 * @author Calvin
 * @mail 179209347@qq.com
 * @website www.aias.top
 */
public class DirectionInfo {

    /**
     * 方向 0 90 180 270
     */
    private String name;

    /**
     * 置信度
     */
    private Double prob;

    public DirectionInfo(String name, Double prob) {
        this.name = name;
        this.prob = prob;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getProb() {
        return prob;
    }

    public void setProb(Double prob) {
        this.prob = prob;
    }
}
