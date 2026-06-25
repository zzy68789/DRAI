package com.zzy.drai.llm;

import java.util.List;

public final class PromptTemplates {
    private PromptTemplates() {
    }

    public static String planner(String query, String critique) {
        return """
                你是调研规划助手。请针对用户问题生成 3-5 个检索子问题。
                只输出合法 JSON array，例如 ["背景", "技术路线", "风险"]。
                不要输出解释文字。

                用户问题：%s
                上轮审查意见：%s
                """.formatted(query, critique == null ? "" : critique);
    }

    public static String writer(String query, List<String> searchResults, String critique) {
        return """
                你是专业技术报告撰写人。请基于资料生成结构化 Markdown 调研报告。
                要求：
                - 不要编造资料外事实。
                - 尽量保留 LOCAL evidence / WEB evidence 中的来源线索。
                - 如果证据不足，请明确说明限制。

                用户问题：%s
                资料：
                %s
                上轮审查意见：%s
                """.formatted(query, String.join("\n\n", searchResults), critique == null ? "" : critique);
    }

    public static String reviewer(String query, String report) {
        return """
                请审查报告是否充分回答用户问题，并且是否有证据支撑。
                只输出合法 JSON，不要输出 Markdown 或解释文字。
                JSON 格式只能是：
                {"status":"PASS","feedback":""}
                或：
                {"status":"FAIL","feedback":"一条具体改进建议"}

                用户问题：%s
                报告：
                %s
                """.formatted(query, report);
    }

    public static String reviewer(String query, String report, List<String> evidence) {
        return reviewer(query, report)
                + "\n检索证据：\n"
                + String.join("\n\n", evidence);
    }

    public static String refine(String oldReport, String instruction) {
        return """
                你是报告编辑。请根据用户修改指令改写旧报告，保持 Markdown 结构，直接输出完整新版本报告。

                旧报告：
                %s
                修改指令：
                %s
                """.formatted(oldReport, instruction);
    }
}
