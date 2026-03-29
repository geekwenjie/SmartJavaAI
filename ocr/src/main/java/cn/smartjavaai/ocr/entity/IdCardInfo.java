package cn.smartjavaai.ocr.entity;

import lombok.Data;

/**
 * 身份证信息（聚合正反面）
 *
 * 当前主要使用正面信息，预留反面字段，方便后续扩展。
 *
 * @author dwj
 * @date 2025/5/22
 */
@Data
public class IdCardInfo {

    /**
     * 身份证正面信息
     */
    private IdCardFrontInfo front;

    /**
     * 身份证反面信息
     */
    private IdCardBackInfo back;
}

