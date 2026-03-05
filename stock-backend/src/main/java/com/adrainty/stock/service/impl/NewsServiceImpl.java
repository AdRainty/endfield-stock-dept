package com.adrainty.stock.service.impl;

import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.entity.News;
import com.adrainty.stock.mapper.InstrumentMapper;
import com.adrainty.stock.mapper.NewsMapper;
import com.adrainty.stock.service.NewsService;
import com.adrainty.stock.util.EndfieldWorldView;
import com.adrainty.stock.util.EndfieldWorldView.ExchangeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 新闻服务实现类
 *
 * @author adrainty
 * @since 2026-03-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsMapper newsMapper;
    private final InstrumentMapper instrumentMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // 新闻类型
    private static final int TYPE_MORNING = 1;
    private static final int TYPE_EVENING = 2;
    private static final int TYPE_REALTIME = 3;

    // 影响幅度
    private static final double MAX_IMPACT = 0.30;  // 最大 30%

    /**
     * 系统提示词 - 终末地世界观
     */
    private static final String WORLD_VIEW_SYSTEM = """
        你是《明日方舟：终末地》游戏中的新闻生成 AI。以下是世界观设定：

        【基本背景】
        - 故事发生在塔卫二星球，人类在此进行工业开发和殖民开拓
        - 主要组织：终末地工业（大型工业企业）、回收站（资源回收组织）
        - 核心资源：源石（能源矿物）、数据模块、技术蓝图
        - 两大交易所：四号谷底交易所（能源/材料为主）、武陵交易所（技术/数据为主）

        【地理设定】
        - 四号谷底：工业矿区，能源和材料生产基地
        - 武陵：技术研发区，数据和高新技术中心
        - 科尔逊：工业区
        - 环境特征：沙暴、辐射尘、天灾等自然灾害频发

        【交易品种】
        - 能源调度券：源石、电池等能源类资源
        - 材料调度券：金属、原料等基础材料
        - 数据调度券：数据模块、信息资源
        - 技术调度券：技术蓝图、专利、核心芯片

        【新闻风格】
        - 工业科幻风格，使用术语如"调度券"、"原能"、"协议同化"等
        - 结合现实财经新闻格式，但融入游戏世界观
        - 保持事件连续性，前后新闻逻辑一致

        请根据以上设定生成符合终末地世界观的新闻内容。
        """;

    @Override
    @Scheduled(cron = "0 30 8 * * ?")
    @Transactional
    public News generateMorningNews(Long exchangeId) {
        log.info("开始生成早报新闻，交易所：{}", exchangeId);
        return generateNews(TYPE_MORNING, exchangeId, "早报");
    }

    @Override
    @Scheduled(cron = "0 0 21 * * ?")
    @Transactional
    public News generateEveningNews(Long exchangeId) {
        log.info("开始生成晚报新闻，交易所：{}", exchangeId);
        return generateNews(TYPE_EVENING, exchangeId, "晚报");
    }

    /**
     * 通用新闻生成方法
     */
    private News generateNews(int newsType, Long exchangeId, String typeDesc) {
        try {
            // 获取最新 10 条新闻作为参考
            List<News> latestNews = newsMapper.findLatest(10);

            // 构建提示词
            String prompt = buildNewsPrompt(newsType, exchangeId, latestNews);

            // 调用 AI 生成新闻
            String generatedContent = callAI(prompt);

            // 解析 AI 返回结果
            News news = parseAIResponse(generatedContent, exchangeId, newsType, latestNews);
            if (news != null) {
                saveNews(news);
                log.info("{}新闻生成成功：{}", typeDesc, news.getTitle());
                return news;
            }

        } catch (Exception e) {
            log.error("生成{}新闻失败", typeDesc, e);
        }

        // AI 失败时使用备用方案
        News news = createFallbackNews(exchangeId, newsType);
        saveNews(news);
        return news;
    }

    /**
     * 构建新闻生成提示词
     */
    private String buildNewsPrompt(int newsType, Long exchangeId, List<News> latestNews) {
        String typeDesc = newsType == TYPE_MORNING ? "早报" : "晚报";
        ExchangeInfo exchangeInfo = EndfieldWorldView.EXCHANGES.get(exchangeId);

        // 格式化已有新闻
        StringBuilder newsHistory = new StringBuilder();
        if (!latestNews.isEmpty()) {
            newsHistory.append("\n【最新 10 条新闻参考】\n");
            for (int i = 0; i < latestNews.size(); i++) {
                News n = latestNews.get(i);
                newsHistory.append(String.format("%d. [%s] %s (影响：%.1f%%)\n",
                    i + 1, formatSentiment(n.getSentimentLevel()), n.getTitle(),
                    n.getImpactPercent() != null ? n.getImpactPercent() * 100 : 0));
            }
        }

        return String.format("""
            请在%s生成%s新闻。

            【交易所信息】
            - 名称：%s
            - 代码：%s
            - 描述：%s
            - 交易品种：%s

            【生成要求】
            1. 生成 3-5 条新闻，涵盖不同交易品种
            2. 保持与已有新闻的事件连续性
            3. 每条新闻需要包含：标题、内容、关联品种、利好利空等级、影响幅度

            %s

            请按以下 JSON 格式输出：
            {"news":[{"title":"标题","content":"内容","instrument":"品种","sentiment":"重大利好 | 利好 | 中性 | 利空 | 重大利空","impact":0.15}]}

            现在请生成%s新闻：
            """,
            newsType == TYPE_MORNING ? "早晨 8:30" : "晚间 21:00", typeDesc,
            exchangeInfo.getName(), exchangeInfo.getCode(),
            exchangeInfo.getDescription(), String.join("、", exchangeInfo.getInstruments()),
            newsHistory.toString(), typeDesc);
    }

    /**
     * 调用 AI 生成内容
     */
    private String callAI(String userPrompt) {
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(WORLD_VIEW_SYSTEM));
            messages.add(new UserMessage(userPrompt));

            Prompt prompt = new Prompt(messages);
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            log.warn("AI 调用失败，使用本地模板生成", e);
            return null;
        }
    }

    /**
     * 解析 AI 返回的新闻
     */
    private News parseAIResponse(String response, Long exchangeId, int newsType, List<News> latestNews) {
        if (response == null) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode newsNode = root.get("news");
            if (newsNode != null && newsNode.isArray() && newsNode.size() > 0) {
                JsonNode firstNews = newsNode.get(0);
                return createNewsFromJson(firstNews, exchangeId, newsType, latestNews);
            }
        } catch (Exception e) {
            log.warn("JSON 解析失败，使用备用方案", e);
        }

        return null;
    }

    /**
     * 创建备用新闻（AI 失败时使用）
     */
    private News createFallbackNews(Long exchangeId, int newsType) {
        News news = new News();
        Random random = new Random();
        ExchangeInfo exchangeInfo = EndfieldWorldView.EXCHANGES.get(exchangeId);

        // 使用模板生成新闻
        String template = EndfieldWorldView.NEWS_TEMPLATES.get(random.nextInt(EndfieldWorldView.NEWS_TEMPLATES.size()));
        String title = template
            .replace("{location}", EndfieldWorldView.getRandomLocation())
            .replace("{action}", random.nextBoolean() ? "产能提升" : "技术升级")
            .replace("{impact}", "预计带动相关品种上涨")
            .replace("{resource}", EndfieldWorldView.getRandomResource())
            .replace("{amount}", (random.nextInt(100) + 10) + "万吨")
            .replace("{company}", EndfieldWorldView.getRandomCompany())
            .replace("{technology}", "源石提取")
            .replace("{trend}", random.nextBoolean() ? "持续上涨" : "小幅回落")
            .replace("{prediction}", "下周将创新高")
            .replace("{policy}", "新一轮产能扩张计划")
            .replace("{scope}", "全区域")
            .replace("{achievement}", "新型能源转化效率突破 30%")
            .replace("{disaster}", "大型沙暴")
            .replace("{cooperation}", "战略资源供应")
            .replace("{change}", random.nextBoolean() ? "增长 15%" : "下降 5%")
            .replace("{percent}", random.nextInt(20) + 5 + "%")
            .replace("{result}", random.nextBoolean() ? "通过" : "未通过")
            .replace("{measure}", "将采取环保措施");

        news.setTitle(title);
        news.setContent("【终末地工业讯】" + title + "。详细报道请关注后续更新。");
        news.setExchangeId(exchangeId);
        news.setNewsType(newsType);

        // 随机生成利好利空
        int sentiment = random.nextInt(6) - 3; // -3 到 2
        news.setSentimentLevel(sentiment);
        news.setImpactPercent(calculateImpact(sentiment));
        news.setProcessed(false);
        news.setPublishTime(LocalDateTime.now());
        news.setSource("AI 生成（备用）");

        // 关联品种
        List<Instrument> instruments = instrumentMapper.findByExchangeId(exchangeId);
        if (!instruments.isEmpty()) {
            Instrument inst = instruments.get(random.nextInt(instruments.size()));
            news.setInstrumentCode(inst.getInstrumentCode());
        }

        return news;
    }

    /**
     * 从 JSON 创建新闻对象
     */
    private News createNewsFromJson(JsonNode newsNode, Long exchangeId, int newsType, List<News> latestNews) {
        News news = new News();

        news.setTitle(newsNode.has("title") ? newsNode.get("title").asText() : "突发新闻");
        news.setContent(newsNode.has("content") ? newsNode.get("content").asText() : "");
        news.setExchangeId(exchangeId);
        news.setNewsType(newsType);

        // 解析 sentiment
        String sentiment = newsNode.has("sentiment") ? newsNode.get("sentiment").asText() : "中性";
        news.setSentimentLevel(parseSentimentLevel(sentiment));

        // 解析 impact
        news.setImpactPercent(newsNode.has("impact") ? newsNode.get("impact").asDouble() : 0.0);

        // 关联品种
        String instrument = newsNode.has("instrument") ? newsNode.get("instrument").asText() : "";
        news.setInstrumentCode(findInstrumentCodeByName(instrument));

        news.setProcessed(false);
        news.setPublishTime(LocalDateTime.now());
        news.setSource("AI 生成");

        // 设置参考新闻 ID
        if (!latestNews.isEmpty()) {
            List<Long> refIds = latestNews.stream().limit(5).map(News::getId).toList();
            try {
                news.setReferenceIds(objectMapper.writeValueAsString(refIds));
            } catch (Exception e) {
                // ignore
            }
        }

        return news;
    }

    /**
     * 解析 sentiment 字符串为等级
     */
    private int parseSentimentLevel(String sentiment) {
        return switch (sentiment) {
            case "重大利好" -> 3;
            case "利好" -> 2;
            case "偏多" -> 1;
            case "偏空" -> -1;
            case "利空" -> -2;
            case "重大利空" -> -3;
            default -> 0;
        };
    }

    /**
     * 格式化 sentiment 等级
     */
    private String formatSentiment(int level) {
        return switch (level) {
            case 3 -> "重大利好";
            case 2 -> "利好";
            case 1 -> "偏多";
            case -1 -> "偏空";
            case -2 -> "利空";
            case -3 -> "重大利空";
            default -> "中性";
        };
    }

    /**
     * 计算影响幅度
     */
    private Double calculateImpact(int sentimentLevel) {
        if (sentimentLevel == 0) return 0.0;

        Random random = new Random();
        double baseImpact = Math.abs(sentimentLevel) * 0.10;
        double variance = random.nextDouble() * 0.10;
        double impact = Math.min(baseImpact + variance, MAX_IMPACT);

        return sentimentLevel > 0 ? impact : -impact;
    }

    /**
     * 根据品种名称查找代码
     */
    private String findInstrumentCodeByName(String name) {
        List<Instrument> instruments = instrumentMapper.findAll();
        for (Instrument inst : instruments) {
            if (inst.getName().contains(name) || name.contains(inst.getName())) {
                return inst.getInstrumentCode();
            }
        }
        return null;
    }

    @Override
    public Double analyzeSentiment(News news, String instrumentCode) {
        if (news.getImpactPercent() != null) {
            return news.getImpactPercent();
        }
        return calculateImpact(news.getSentimentLevel());
    }

    @Override
    @Scheduled(cron = "0 15 9 * * 1-5")
    @Transactional
    public void executeCallAuction() {
        log.info("开始执行集合竞价价格更新");

        LocalDate today = LocalDate.now();
        if (today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            log.info("周末，跳过集合竞价");
            return;
        }

        try {
            // 获取所有未处理的新闻（包括 20:00 以后生成的新闻）
            List<News> unprocessedNews = newsMapper.findUnprocessed();

            if (unprocessedNews.isEmpty()) {
                log.info("没有未处理的新闻，跳过集合竞价");
                return;
            }

            // 按品种分组计算影响
            Map<String, Double> instrumentImpact = new HashMap<>();

            for (News news : unprocessedNews) {
                String code = news.getInstrumentCode();
                if (code != null) {
                    double currentImpact = instrumentImpact.getOrDefault(code, 0.0);
                    double newsImpact = analyzeSentiment(news, code);
                    instrumentImpact.merge(code, newsImpact, Double::sum);
                }
            }

            // 应用价格变化
            for (Map.Entry<String, Double> entry : instrumentImpact.entrySet()) {
                String instrumentCode = entry.getKey();
                Double impact = entry.getValue();

                // 限制在 -30% 到 30%
                impact = Math.max(-MAX_IMPACT, Math.min(MAX_IMPACT, impact));

                // 更新品种价格（集合竞价）
                Instrument instrument = instrumentMapper.findByInstrumentCode(instrumentCode);
                if (instrument != null) {
                    double currentPrice = instrument.getCurrentPrice().doubleValue();
                    double newPrice = currentPrice * (1 + impact);
                    newPrice = Math.max(0.01, newPrice);

                    instrument.setCurrentPrice(java.math.BigDecimal.valueOf(newPrice));
                    instrumentMapper.updateById(instrument);

                    log.info("集合竞价更新价格：{} -> {}, 幅度：{:.2f}%", instrumentCode, newPrice, impact * 100);
                }
            }

            // 标记新闻为已处理
            for (News news : unprocessedNews) {
                news.setProcessed(true);
                newsMapper.updateById(news);
            }

            log.info("集合竞价完成，处理新闻 {} 条", unprocessedNews.size());

        } catch (Exception e) {
            log.error("集合竞价执行失败", e);
        }
    }

    @Override
    public List<News> getLatestNews(int limit) {
        return newsMapper.findLatest(limit);
    }

    @Override
    public List<News> getNewsByExchange(Long exchangeId) {
        return newsMapper.findByExchangeId(exchangeId);
    }

    @Override
    public List<News> getNewsByInstrument(String instrumentCode) {
        return newsMapper.findByInstrumentCode(instrumentCode);
    }

    @Override
    @Transactional
    public void saveNews(News news) {
        if (news.getPublishTime() == null) {
            news.setPublishTime(LocalDateTime.now());
        }
        newsMapper.insert(news);
    }
}
