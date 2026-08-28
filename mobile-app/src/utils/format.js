/**
 * 展示格式化工具（三端共享，不依赖端 API）
 */

/** 金额格式化：1234.5 → "1234.50"（BigDecimal 后端返回字符串或数字均兼容） */
export function formatPrice(value) {
	if (value === null || value === undefined || value === "") return "0.00";
	return Number(value).toFixed(2);
}

/** 千分位金额：123456.78 → "123,456.78" */
export function formatPriceWithComma(value) {
	const [int, dec] = formatPrice(value).split(".");
	return int.replace(/\B(?=(\d{3})+(?!\d))/g, ",") + "." + dec;
}

/** 日期时间格式化：2026-08-15T10:30:00 → 2026-08-15 10:30（兼容后端 LocalDateTime 字符串） */
export function formatDateTime(value) {
	if (!value) return "-";
	const str = String(value);
	const m = str.match(/^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})/);
	return m ? `${m[1]} ${m[2]}` : str;
}

/** 销量展示：12000 → "1.2万+" */
export function formatSales(count) {
	const n = Number(count) || 0;
	if (n >= 10000) return (n / 10000).toFixed(1) + "万+";
	return String(n);
}
