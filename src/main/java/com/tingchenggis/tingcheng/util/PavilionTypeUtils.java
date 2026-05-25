package com.tingchenggis.tingcheng.util;

/**
 * 亭子类型中英文映射工具
 */
public final class PavilionTypeUtils {

    private PavilionTypeUtils() {}

    public static String normalize(String type) {
        if (type == null || type.isBlank()) {
            return "HISTORICAL";
        }
        return switch (type.trim()) {
            case "历史文化亭", "HISTORICAL", "historical" -> "HISTORICAL";
            case "现代景观亭", "MODERN", "modern" -> "MODERN";
            case "文化主题亭", "CULTURAL", "cultural" -> "CULTURAL";
            default -> type.toUpperCase();
        };
    }

    public static String toLabel(String type) {
        if (type == null) {
            return "未知类型";
        }
        return switch (normalize(type)) {
            case "HISTORICAL" -> "历史文化亭";
            case "MODERN" -> "现代景观亭";
            case "CULTURAL" -> "文化主题亭";
            default -> type;
        };
    }

    /**
     * 从调查数据推断亭子类型
     */
    public static String inferType(String name, String structure, String locationDesc) {
        String combined = ((name != null ? name : "") + (locationDesc != null ? locationDesc : "")).toLowerCase();
        if (combined.contains("琅琊") || combined.contains("醉翁") || combined.contains("欧阳")
            || combined.contains("丰乐") || combined.contains("古") || combined.contains("遗址"))
            return "HISTORICAL";
        if (structure != null && (structure.contains("钢") || structure.contains("玻璃") || structure.contains("塑料")))
            return "MODERN";
        return "CULTURAL";
    }

    public static boolean matchesFilter(String pavilionType, String filter) {
        if (filter == null || filter.isBlank() || "ALL".equalsIgnoreCase(filter)) {
            return true;
        }
        return normalize(pavilionType).equals(normalize(filter));
    }
}
