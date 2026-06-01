package com.ecommerce.agent.rag;

import dev.langchain4j.data.document.Document;

import java.util.ArrayList;
import java.util.List;

final class KnowledgeDocuments {

    private KnowledgeDocuments() {
    }

    static List<Document> getAllDocuments() {
        List<Document> docs = new ArrayList<>();
        docs.add(companyInfo());
        docs.add(productSpecs());
        docs.add(marketAnalysis());
        docs.add(complianceAndCertification());
        docs.add(logisticsAndShipping());
        docs.add(tradeShowsAndExhibitions());
        docs.add(industryTerminology());
        docs.add(b2bPlatformOptimization());
        docs.add(emailInquiryTemplates());
        docs.add(localizationGuide());
        return docs;
    }

    private static Document companyInfo() {
        return Document.from("""
                ## 公司信息
                深圳市杰创展示有限公司 (Shenzhen JC Display Ltd) 是一家专业的纸展示架/POP陈列架制造商。
                成立17年，拥有自有工厂，ISO 9001认证。
                主营产品：瓦楞纸展示架、POP陈列架、PDQ展示盒、纸货架、Counter Display、Pallet Display。
                出口30+国家和地区：美国、英国、德国、法国、日本、韩国、澳大利亚、加拿大等。
                官网: www.displaystandpop.com
                邮箱: sales@displaystandpop.com
                核心优势: 自有工厂直销、免费3D设计、免费打样、平摊包装节省运费。
                行业定位: B2B出口制造，FOB深圳为主要贸易方式。
                """);
    }

    private static Document productSpecs() {
        return Document.from("""
                ## 产品规格与技术参数
                材质: 300G铜版纸/灰板纸 + B楞/E楞/F楞瓦楞纸板。
                印刷: 4C CMYK胶版印刷，支持高光/哑光覆膜。
                表面处理: 光面覆膜(Glossy Lamination)、哑面覆膜(Matte Lamination)、UV局部上光。
                结构类型: Floor Display Stand(落地展示架)、Counter Display(柜台展示架)、
                Pallet Display(托盘展示架)、PDQ Display(快速陈列盒)、Dump Bin(散装陈列箱)、
                Shelf Ready Packaging(即上架包装)。
                定制程度: 尺寸、形状、颜色、印刷完全可定制。
                MOQ起订量: 100件/款 (可协商)。
                生产周期: 10-12个工作日。
                样品: 3个工作日内免费打样。
                包装方式: Flat Pack平摊包装，10件/箱。
                价格区间: USD 8-25/pc (根据尺寸和复杂度不同)。
                适用场景: 超市、便利店、药妆店、零售店、品牌促销、贸易展会。
                """);
    }

    private static Document marketAnalysis() {
        return Document.from("""
                ## 目标市场分析
                美国市场(US): 全球最大POP展示架消费市场，超市连锁(Walmart/Kroger/Costco)需求旺盛。
                FMCG品牌商季度促销驱动稳定需求，环保趋势下纸质展示架逐步替代塑料/PVC。
                竞争国: 中国(60%)、土耳其(10%)、波兰(8%)。
                推荐定价: $8-25/pc。

                日本市场(JP): 便利店文化发达(7-11/FamilyMart/Lawson)，对展示架精细度和印刷质量要求极高。
                偏好小型化、精致化设计。日本进口商重视长期合作关系。
                推荐定价: $10-30/pc。

                英国市场(UK): 环保法规严格，FSC认证为基本准入要求。
                Tesco/Sainsbury's等零售商对供应商有ESG审计要求。
                脱欧后关税政策需关注。推荐定价: $8-22/pc。

                德国市场(DE): 欧洲最大经济体，零售业发达。Aldi/Lidl等折扣超市对成本敏感。
                环保标准最高，ISTA包装认证和ROHS油墨标准为必备。
                推荐定价: $8-20/pc。

                韩国市场(KR): 美妆护肤品类展示架需求大(Olive Young等)。
                偏好时尚设计感，对色彩和印刷效果要求高。
                推荐定价: $8-25/pc。

                澳大利亚市场(AU): 人口集中沿海城市，零售连锁发达(Coles/Woolworths)。
                对包装环保性要求高，FSC认证必备。
                推荐定价: $10-28/pc。
                """);
    }

    private static Document complianceAndCertification() {
        return Document.from("""
                ## 合规与认证要求
                FSC认证: 森林管理委员会认证，证明纸张原料来自可持续管理森林。
                欧美大型零售商(Tesco/Walmart)基本要求，是进入欧美市场的必备认证。

                ISTA认证: 国际安全运输协会包装标准。适用于运输包装的跌落、振动、压缩测试。
                通常要求ISTA 1A或ISTA 3A级别认证。Flat Pack包装可降低测试难度。

                ROHS环保标准: 限制有害物质指令，主要针对电子电器产品，但也影响印刷油墨。
                展示架印刷油墨需符合ROHS标准，不使用含铅、汞、镉等重金属的油墨。

                EN71标准: 欧洲玩具安全标准，当展示架用于玩具/儿童产品陈列时适用。
                要求油墨无毒、无尖锐边缘、无小零件脱落风险。

                SGS检测: 国际公认的第三方检测机构，CNAS认可。可出具FSC、ROHS、ISTA等检测报告。
                JC Display产品通过SGS检测，可提供检测报告给海外客户。

                ISO 9001: 质量管理体系认证。JC Display工厂持有此认证，
                证明公司建立了完善的质量管理体系，从原材料到出货全程可追溯。
                """);
    }

    private static Document logisticsAndShipping() {
        return Document.from("""
                ## 物流与运输
                FOB深圳: Free On Board，卖方负责将货物运至深圳港并完成出口报关。
                买方承担海运费用和保险。90%的JC Display订单采用此贸易方式。

                DDP: Delivered Duty Paid，卖方承担从工厂到客户指定地点的一切费用和风险。
                包括海运费、关税、目的港清关和末端派送。适合样品单和小批量订单。

                Flat Pack平摊包装: 将展示架拆解为平面组件包装，相比组装后运输节省60%运费。
                每箱装10件，外箱尺寸通常为120x80x20cm。到港后客户可自行组装或找当地装配商。

                海运时效:
                - 深圳→美国西海岸(LA/LB): 约18-22天
                - 深圳→美国东海岸(NY): 约28-35天
                - 深圳→欧洲(鹿特丹/汉堡): 约25-30天
                - 深圳→日本(东京/大阪): 约5-8天
                - 深圳→韩国(釜山): 约4-6天
                - 深圳→澳大利亚(悉尼/墨尔本): 约15-20天

                推荐提前3个月备货以应对Q4(10-12月)旺季。春节(1-2月)期间产能紧张，需提前规划。
                """);
    }

    private static Document tradeShowsAndExhibitions() {
        return Document.from("""
                ## 行业展会与展览
                EuroShop(欧洲零售展): 三年一届，德国杜塞尔多夫。全球最大零售业展会，
                覆盖店铺装修、陈列设备、视觉营销、POP展示全产业链。最近一届: 2026年。

                GlobalShop(全球零售展): 美国一年一届，全球知名零售设计及购物体验展。
                2025年在芝加哥举办。适合展示创新零售陈列解决方案。

                POPAI(采购点广告国际协会): 全球POP/POS行业的权威组织。
                举办年度峰会、设计竞赛，发布行业趋势报告和白皮书。

                广交会(Canton Fair): 中国进出口商品交易会，每年春秋两届，在广州举办。
                全球最大的综合性贸易展览，JC Display每届均参展。展位位于一期消费品展区。

                Global Sources(环球资源展): 香港一年两届，聚焦亚洲消费电子产品、
                礼品家居品和时尚产品出口。

                Ambiente(法兰克福消费品展): 德国法兰克福一年一届，全球最重要的消费品展之一。
                适合展示高端展示架和礼品包装解决方案。
                """);
    }

    private static Document industryTerminology() {
        return Document.from("""
                ## 展示架行业术语
                POP Display: Point of Purchase Display，采购点展示架。放置于收银台或购物区，
                用于促进冲动购买。常见形式有Counter Display、Floor Display、Sidekick等。

                PDQ Display: Pretty Damn Quick Display/Product Display Quickly，快速陈列展示盒。
                预装产品，零售商开箱即可放置陈列，省时省力。通常为小型桌面/货架展示。

                Corrugated Cardboard: 瓦楞纸板，由面纸、芯纸和里纸组成。B楞(2.5-3mm厚)、
                E楞(1.1-1.6mm薄)、F楞(0.6-0.9mm超薄)为展示架常用楞型。

                Offset Printing: 胶版印刷/平版印刷，工业生产中品质最高的印刷方式。4C指CMYK四色印刷。
                相对于数码印刷，胶印在批量生产时成本更低、颜色一致性更好。

                Dump Bin: 散装陈列箱，开放式展示容器，顾客可直接从中取商品。
                常见于超市通道两端，用于促销品和季节性商品陈列。

                Shelf Ready Packaging(SRP): 即上架包装，包装本身就是陈列工具。
                店员无需取出单个商品，直接将外包装放置在货架上即可开始销售。

                Kraft Paper: 牛皮纸/本色纸，棕色，环保复古风格。可作展示架面纸或内衬，
                配合单色印刷营造自然手工质感。

                Die-Cut: 模切工艺，根据设计图案用模具冲切纸板，形成特定形状。
                常用于展示架的品牌Logo镂空、异形轮廓等。

                Flat Pack: 平摊包装，将展示架拆解为平面组件，到客户处再组装。
                核心优势是大幅降低运输体积和成本(可节省60%运费)。
                """);
    }

    private static Document b2bPlatformOptimization() {
        return Document.from("""
                ## B2B平台优化
                Alibaba国际站产品标题公式:
                [Custom/Wholesale] + [Material] + [Product Type] + [Usage] + [Feature 1] + [Feature 2] + [Factory Direct]
                示例: Custom Wholesale Cardboard Floor Display Stand for Supermarket Retail POP Promotion Factory Direct

                标题长度: 150-200字符，前置核心关键词。
                五点描述(Bullet Points): 突出Material、Customization、MOQ、Lead Time、Certification。
                产品描述结构化: Overview → Specifications → Features → FAQ → Company Profile → Contact。

                Global Sources平台优化:
                强调工厂实力(Factory Audit Report、视频工厂参观)、MOQ灵活性、
                OEM/ODM能力、快速的样品交付。Global Sources买家更专业，偏好技术参数详细的供应商。

                独立站SEO:
                主关键词: cardboard display stand manufacturer, custom pop display, wholesale display rack
                长尾关键词: custom cardboard floor display stand, wholesale pdq display box,
                corrugated pop display supplier china
                内容营销: 每2周发布一篇行业趋势文章或案例研究，LinkedIn同步推广。

                Google Shopping:
                产品Feed需包含GTIN/EAN条码、清晰白底图、有竞争力的价格。
                标题格式: [Brand] + [Product Type] + [Size/Material] + [Color] + [Pack Size]
                """);
    }

    private static Document emailInquiryTemplates() {
        return Document.from("""
                ## 询盘回复邮件策略
                邮件结构:
                1. Subject行: Re: [客户原主题] | [Product Name] | JC Display Factory Quote
                2. 开头问候: 感谢询盘(Thank you for your inquiry)，简介公司实力(17年、自有工厂、ISO认证)
                3. 报价核心: MOQ、单价范围、生产周期、付款方式(T/T 30%定金+70%发货前)
                4. 服务亮点: 免费3D设计(2工作日内)、免费打样(3工作日内)、OEM/ODM
                5. 引导回复: 询问目标尺寸、预估数量、特殊印刷要求、目标市场
                6. 结尾: 公司全称、地址、官网、邮箱、WhatsApp、WeChat

                语气风格:
                - 美国/加拿大客户: 直接专业，强调数据(17年、30+国家、SGS认证)
                - 日本客户: 礼貌敬语，强调品质细节和长期合作意愿
                - 欧洲客户: 强调环保认证、ESG合规、可持续发展
                - 中东/南美客户: 灵活谈判，强调价格优势和批量折扣

                常见客户问题及回复要点:
                - "What's your MOQ?" → 100pcs/款，样品单可接受
                - "Can I get a sample?" → 免费样品，3工作日内发货，客户承担运费
                - "What's your lead time?" → 10-12工作日，急单可协商
                - "Do you do OEM?" → 完全支持，提供免费3D设计
                - "Can you ship to my country?" → 已出口30+国家，可提供当地客户参考
                """);
    }

    private static Document localizationGuide() {
        return Document.from("""
                ## 跨境电商本地化指南
                尺寸单位本地化:
                - 美国市场: inch/ft (1 inch = 25.4mm, 1 ft = 304.8mm)
                - 欧洲市场: cm/m (1 cm = 10mm, 1 m = 1000mm)
                - 日本市场: cm/m (同公制)
                - 展示架尺寸描述: 美国用 W"×D"×H"，欧洲用 W×D×H cm

                货币符号:
                - 美国: USD $ (美元)
                - 欧洲: EUR (欧元)
                - 英国: GBP (英镑)
                - 日本: JPY (日元)
                - 韩国: KRW (韩元)
                - 报价应同时标注FOB深圳和目的国当地货币换算

                语言风格差异:
                - 英语(美式): 直接、简洁、强调Benefits和ROI
                - 英语(英式): 正式、保守、强调品质和传统
                - 日语: 礼貌等级高、间接表达、重视关系建立
                - 德语: 直接、技术导向、重视数据和认证
                - 法语: 优雅、品牌导向、重视设计和美学
                - 韩语: 注重外观和时尚感、强调性价比

                文化禁忌:
                - 日本: 避免数字4(与"死"同音)和9(与"苦"同音)，包装数量避开4和9
                - 中东: 避免使用猪/狗相关图像，宗教符号需谨慎
                - 德国: 避免夸大宣传，德国买家看重事实和数据
                - 韩国: 颜色有文化含义，白色(纯洁)、红色(好运)、蓝色(活力)

                电商法规注意事项:
                - 美国: FCC/FTC广告法，明确标注广告性质，不得虚假宣传
                - 欧洲: GDPR数据保护，不得未经同意收集用户数据
                - 日本: 景品表示法，禁止夸大宣传和不当赠品
                - 澳大利亚: ACCC消费者保护法，严格规范产品描述和退款政策
                """);
    }
}
