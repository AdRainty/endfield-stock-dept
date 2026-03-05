package com.adrainty.stock.util;

import java.util.*;

/**
 * 终末地世界观数据
 * 基于《明日方舟：终末地》游戏设定
 *
 * @author adrainty
 * @since 2026-03-05
 */
public class EndfieldWorldView {

    /**
     * 交易所信息
     */
    public static final Map<Long, ExchangeInfo> EXCHANGES = new HashMap<>() {{
        put(1L, new ExchangeInfo(
            "四号谷底交易所",
            "VALLEY",
            "位于塔卫二星球四号谷底的能源与材料交易中心，周边环绕着大量工业设施和矿业基地",
            List.of("能源调度券", "材料调度券", "数据调度券", "技术调度券")
        ));
        put(2L, new ExchangeInfo(
            "武陵交易所",
            "WULING",
            "位于武陵地区的技术与数据交易中心，是终末地工业的技术研发核心区",
            List.of("能源调度券", "材料调度券", "数据调度券", "技术调度券")
        ));
    }};

    /**
     * 世界观关键词
     */
    public static final List<String> WORLD_VIEW_KEYWORDS = Arrays.asList(
        // 地理相关
        "塔卫二", "塔卫一", "四号谷底", "武陵", "科尔逊", "沙地", "绿洲",
        "工业基地", "采矿场", "冶炼厂", "能源站", "数据中枢",

        // 组织相关
        "终末地工业", "回收站", "协议同化", "监督", "管理员",
        "工程科", "技术部", "调度中心", "维序部队",

        // 技术相关
        "源石", "源石技艺", "工业装置", "自动机械", "智能机器人",
        "数据链", "量子计算", "曲率引擎", "空间折叠",

        // 资源相关
        "源石矿", "稀有金属", "工业原料", "能源电池", "数据模块",
        "技术蓝图", "核心芯片", "催化剂",

        // 事件相关
        "天灾", "沙暴", "辐射尘", "能源危机", "技术突破",
        "协议签订", "工业扩张", "资源争夺", "殖民开拓"
    );

    /**
     * 新闻主题模板
     */
    public static final List<String> NEWS_TEMPLATES = Arrays.asList(
        // 能源类
        "【能源动态】{location}能源站今日{action}，{impact}",
        "【资源开发】{location}发现新型{resource}矿藏，储量{amount}",
        "【工业新闻】{company}宣布{technology}技术取得突破性进展",
        "【市场分析】{resource}价格{trend}，分析师预计{prediction}",
        "【政策发布】终末地工业发布{policy}，影响{scope}",
        "【技术研发】{company}研发团队成功{achievement}",
        "【天灾预警】{location}即将遭遇{disaster}，相关部门已启动应急预案",
        "【合作协议】{company1}与{company2}签署{cooperation}协议",
        "【产能报告】{location}本月产能{change}，环比{percent}",
        "【环境评估】{location}环境影响评估{result}，{measure}"
    );

    /**
     * 利好利空词库
     */
    public static final Map<String, List<String>> SENTIMENT_WORDS = new HashMap<>() {{
        put("positive", Arrays.asList(
            "突破", "增长", "成功", "利好", "上涨", "扩张", "合作",
            "丰收", "发现", "升级", "优化", "效率提升", "成本降低",
            "订单激增", "产能翻倍", "技术领先", "市场火热", "供不应求"
        ));
        put("negative", Arrays.asList(
            "危机", "下降", "失败", "利空", "下跌", "收缩", "冲突",
            "枯竭", "泄漏", "故障", "延误", "成本上升", "效率低下",
            "订单取消", "产能过剩", "技术落后", "市场低迷", "供过于求",
            "事故", "停产", "亏损", "裁员", "制裁", "限制"
        ));
    }};

    /**
     * 交易所信息类
     */
    public static class ExchangeInfo {
        private final String name;
        private final String code;
        private final String description;
        private final List<String> instruments;

        public ExchangeInfo(String name, String code, String description, List<String> instruments) {
            this.name = name;
            this.code = code;
            this.description = description;
            this.instruments = instruments;
        }

        public String getName() { return name; }
        public String getCode() { return code; }
        public String getDescription() { return description; }
        public List<String> getInstruments() { return instruments; }
    }

    /**
     * 获取随机位置
     */
    public static String getRandomLocation() {
        List<String> locations = Arrays.asList(
            "四号谷底", "武陵", "科尔逊工业区", "中央绿洲",
            "北境矿区", "南部冶炼厂", "东部数据港", "西部能源站",
            "荒漠边缘", "地下实验室", "轨道电梯", "浮空平台"
        );
        return locations.get(new Random().nextInt(locations.size()));
    }

    /**
     * 获取随机公司/组织
     */
    public static String getRandomCompany() {
        List<String> companies = Arrays.asList(
            "终末地工业", "回收站", "科联集团", "源石科技",
            "数据先锋", "能源无限", "材料大师", "技术前沿",
            "量子动力", "星际矿业", "深空探索", "维度科技"
        );
        return companies.get(new Random().nextInt(companies.size()));
    }

    /**
     * 获取随机资源
     */
    public static String getRandomResource() {
        List<String> resources = Arrays.asList(
            "源石", "稀有金属", "工业原料", "能源电池",
            "数据模块", "技术蓝图", "核心芯片", "催化剂"
        );
        return resources.get(new Random().nextInt(resources.size()));
    }
}
