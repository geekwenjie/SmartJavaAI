package cn.smartjavaai.common.entity;

/**
 * 语言枚举
 * @author dwj
 */
public enum Language {


    EN("en", "English", "英语"),
    ZH("zh", "Chinese", "中文"),
    DE("de", "German", "德语"),
    ES("es", "Spanish", "西班牙语"),
    RU("ru", "Russian", "俄语"),
    KO("ko", "Korean", "韩语"),
    FR("fr", "French", "法语"),
    JA("ja", "Japanese", "日语"),
    PT("pt", "Portuguese", "葡萄牙语"),
    TR("tr", "Turkish", "土耳其语"),
    PL("pl", "Polish", "波兰语"),
    CA("ca", "Catalan", "加泰罗尼亚语"),
    NL("nl", "Dutch", "荷兰语"),
    AR("ar", "Arabic", "阿拉伯语"),
    SV("sv", "Swedish", "瑞典语"),
    IT("it", "Italian", "意大利语"),
    ID("id", "Indonesian", "印尼语"),
    HI("hi", "Hindi", "印地语"),
    FI("fi", "Finnish", "芬兰语"),
    VI("vi", "Vietnamese", "越南语"),
    HE("he", "Hebrew", "希伯来语"),
    UK("uk", "Ukrainian", "乌克兰语"),
    EL("el", "Greek", "希腊语"),
    MS("ms", "Malay", "马来语"),
    CS("cs", "Czech", "捷克语"),
    RO("ro", "Romanian", "罗马尼亚语"),
    DA("da", "Danish", "丹麦语"),
    HU("hu", "Hungarian", "匈牙利语"),
    TA("ta", "Tamil", "泰米尔语"),
    NO("no", "Norwegian", "挪威语"),
    TH("th", "Thai", "泰语"),
    UR("ur", "Urdu", "乌尔都语"),
    HR("hr", "Croatian", "克罗地亚语"),
    BG("bg", "Bulgarian", "保加利亚语"),
    LT("lt", "Lithuanian", "立陶宛语"),
    LA("la", "Latin", "拉丁语"),
    MI("mi", "Maori", "毛利语"),
    ML("ml", "Malayalam", "马拉雅拉姆语"),
    CY("cy", "Welsh", "威尔士语"),
    SK("sk", "Slovak", "斯洛伐克语"),
    TE("te", "Telugu", "泰卢固语"),
    FA("fa", "Persian", "波斯语"),
    LV("lv", "Latvian", "拉脱维亚语"),
    BN("bn", "Bengali", "孟加拉语"),
    SR("sr", "Serbian", "塞尔维亚语"),
    AZ("az", "Azerbaijani", "阿塞拜疆语"),
    SL("sl", "Slovenian", "斯洛文尼亚语"),
    KN("kn", "Kannada", "卡纳达语"),
    ET("et", "Estonian", "爱沙尼亚语"),
    MK("mk", "Macedonian", "马其顿语"),
    BR("br", "Breton", "布列塔尼语"),
    EU("eu", "Basque", "巴斯克语"),
    IS("is", "Icelandic", "冰岛语"),
    HY("hy", "Armenian", "亚美尼亚语"),
    NE("ne", "Nepali", "尼泊尔语"),
    MN("mn", "Mongolian", "蒙古语"),
    BS("bs", "Bosnian", "波斯尼亚语"),
    KK("kk", "Kazakh", "哈萨克语"),
    SQ("sq", "Albanian", "阿尔巴尼亚语"),
    SW("sw", "Swahili", "斯瓦希里语"),
    GL("gl", "Galician", "加利西亚语"),
    MR("mr", "Marathi", "马拉地语"),
    PA("pa", "Punjabi", "旁遮普语"),
    SI("si", "Sinhala", "僧伽罗语"),
    KM("km", "Khmer", "高棉语"),
    SN("sn", "Shona", "修纳语"),
    YO("yo", "Yoruba", "约鲁巴语"),
    SO("so", "Somali", "索马里语"),
    AF("af", "Afrikaans", "南非荷兰语"),
    OC("oc", "Occitan", "奥克语"),
    KA("ka", "Georgian", "格鲁吉亚语"),
    BE("be", "Belarusian", "白俄罗斯语"),
    TG("tg", "Tajik", "塔吉克语"),
    SD("sd", "Sindhi", "信德语"),
    GU("gu", "Gujarati", "古吉拉特语"),
    AM("am", "Amharic", "阿姆哈拉语"),
    YI("yi", "Yiddish", "意第绪语"),
    LO("lo", "Lao", "老挝语"),
    UZ("uz", "Uzbek", "乌兹别克语"),
    FO("fo", "Faroese", "法罗语"),
    HT("ht", "Haitian Creole", "海地克里奥尔语"),
    PS("ps", "Pashto", "普什图语"),
    TK("tk", "Turkmen", "土库曼语"),
    NN("nn", "Nynorsk", "新挪威语"),
    MT("mt", "Maltese", "马耳他语"),
    SA("sa", "Sanskrit", "梵语"),
    LB("lb", "Luxembourgish", "卢森堡语"),
    MY("my", "Myanmar", "缅甸语"),
    BO("bo", "Tibetan", "藏语"),
    TL("tl", "Tagalog", "他加禄语"),
    MG("mg", "Malagasy", "马尔加什语"),
    AS("as", "Assamese", "阿萨姆语"),
    TT("tt", "Tatar", "鞑靼语"),
    HAW("haw", "Hawaiian", "夏威夷语"),
    LN("ln", "Lingala", "林加拉语"),
    HA("ha", "Hausa", "豪萨语"),
    BA("ba", "Bashkir", "巴什基尔语"),
    JW("jw", "Javanese", "爪哇语"),
    SU("su", "Sundanese", "巽他语"),
    YUE("yue", "Cantonese", "粤语");

    private final String code;        // Whisper语言代码
    private final String englishName; // 英文名称
    private final String chineseName; // 中文名称

    Language(String code, String englishName, String chineseName) {
        this.code = code;
        this.englishName = englishName;
        this.chineseName = chineseName;
    }

    public String getCode() {
        return code;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getChineseName() {
        return chineseName;
    }

    @Override
    public String toString() {
        return code;
    }

}
