/**
 * 商品浏览接口模块（T5 公开组 P-003~P-006 + U-009 加购）
 * 每个函数注释标注 T5 编号、参数与主要错误码
 */
import { get, post } from "../utils/request";

// P-003 类目树（仅 ENABLED；data 为 {list, total}；白名单接口）
export const getCategories = (silent) => get("/api/categories", null, silent);

/**
 * P-004 商品列表（仅 ON_SALE；白名单接口）
 * @param {Object} params { page, pageSize, categoryId, keyword, sort }
 *   sort：综合(默认不传)/sales/priceAsc/priceDesc（T5 B 增补）
 */
export const getProducts = (params, silent) => get("/api/products", params, silent);

// P-005 商品详情：错误码 3001 商品不存在 / 3002 商品已下架
export const getProductDetail = (id, silent) => get(`/api/products/${id}`, null, silent);

// P-006 商品评价列表（userName 脱敏；分页 {list, total}）
export const getProductReviews = (id, params, silent) =>
	get(`/api/products/${id}/reviews`, params, silent);

// U-009 加购：仅 ON_SALE 可加购（3002）；重复商品累加数量；返回 {id}
export const addCartItem = (data) => post("/api/cart/items", data);
