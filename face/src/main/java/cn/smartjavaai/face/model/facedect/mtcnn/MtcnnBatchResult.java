package cn.smartjavaai.face.model.facedect.mtcnn;

import ai.djl.ndarray.NDArray;
import lombok.Data;

import java.util.List;

/**
 * @author dwj
 */
@Data
public class MtcnnBatchResult {

    public List<NDArray> boxes;
    public List<NDArray> probs;
    public List<NDArray> points;

    public MtcnnBatchResult(List<NDArray> boxes, List<NDArray> probs, List<NDArray> points) {
        this.boxes = boxes;
        this.probs = probs;
        this.points = points;
    }

}
