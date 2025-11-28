package cn.smartjavaai.face.model.facedect.mtcnn;

import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDList;
import cn.smartjavaai.face.model.facedect.MtcnnFaceDetModel;

/**
 * @author dwj
 * @date 2025/11/24
 */
public class MtcnnPredictors implements AutoCloseable{

    public Predictor<NDList, NDList> pNetPredictor;
    public Predictor<NDList, NDList> rNetPredictor;
    public Predictor<NDList, NDList> oNetPredictor;

    // 标记是否由外部借用，用于控制 close 行为
    private MtcnnFaceDetModel model;

    public MtcnnPredictors(Predictor<NDList, NDList> p, Predictor<NDList, NDList> r, Predictor<NDList, NDList> o, MtcnnFaceDetModel m) {
        this.pNetPredictor = p;
        this.rNetPredictor = r;
        this.oNetPredictor = o;
        this.model = m;
    }

    @Override
    public void close() throws Exception {
        model.returnPredictor(pNetPredictor, rNetPredictor, oNetPredictor);
    }
}
