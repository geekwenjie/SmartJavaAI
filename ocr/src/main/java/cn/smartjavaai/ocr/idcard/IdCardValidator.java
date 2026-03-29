package cn.smartjavaai.ocr.idcard;

import cn.smartjavaai.ocr.entity.IdCardBackInfo;
import cn.smartjavaai.ocr.entity.IdCardFrontInfo;
import cn.smartjavaai.ocr.entity.IdCardInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 身份证字段校验工具。
 *
 * 提供：
 * - 正面字段完整性与格式校验
 * - 反面字段简单格式校验（预留）
 * - 身份证号码校验位校验
 */
@Slf4j
public class IdCardValidator {

    public boolean validateFront(IdCardFrontInfo info) {
        if (info == null) {
            return false;
        }
        boolean nameOk = info.getName() != null
                && info.getName().length() >= 2
                && info.getName().length() <= 4;
        boolean genderOk = "男".equals(info.getGender()) || "女".equals(info.getGender());
        boolean ethnicOk = info.getEthnicity() != null && IdCardOcrUtils.ETHNIC_SET.contains(info.getEthnicity());
        boolean idOk = validateIdNumber(info.getIdNumber());

        log.debug("身份证正面字段校验详情：nameOk={}, genderOk={}, ethnicOk={}, idOk={}",
                nameOk, genderOk, ethnicOk, idOk);
        return nameOk && genderOk && ethnicOk && idOk;
    }

    public boolean validateBack(IdCardBackInfo info) {
        // 目前只做存在性校验，后续可以根据业务需要增加日期格式、范围等更严格校验
        if (info == null) {
            return false;
        }
        boolean authorityOk = info.getIssuingAuthority() != null && !info.getIssuingAuthority().isEmpty();
        boolean validFromOk = info.getValidFrom() != null && !info.getValidFrom().isEmpty();
        boolean validToOk = info.getValidTo() != null && !info.getValidTo().isEmpty();
        log.info("身份证反面字段校验详情：authorityOk={}, validFromOk={}, validToOk={}",
                authorityOk, validFromOk, validToOk);
        return authorityOk && validFromOk && validToOk;
    }

    public boolean validate(IdCardInfo info) {
        if (info == null) {
            return false;
        }
        boolean frontOk = info.getFront() == null || validateFront(info.getFront());
        boolean backOk = info.getBack() == null || validateBack(info.getBack());
        return frontOk && backOk;
    }

    /**
     * 简单的身份证号码格式 + 校验位校验
     */
    public boolean validateIdNumber(String id) {
        if (id == null) {
            return false;
        }
        String upper = id.toUpperCase();
        if (!upper.matches("^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9X]$")) {
            return false;
        }
        // 校验位
        char[] chars = upper.toCharArray();
        int[] weight = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] validateCode = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (chars[i] - '0') * weight[i];
        }
        int mod = sum % 11;
        return validateCode[mod] == chars[17];
    }
}
