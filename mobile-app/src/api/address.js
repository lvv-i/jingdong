/**
 * 收货地址接口模块（T5 U-003；U-001~U-002 增删改归 X5 我的/地址链路）
 */
import { get } from "../utils/request";

// U-003 地址列表：返回 {list, total}（item: {id, receiver, phone, province, city, district, detail, isDefault 0/1}）
export const getAddresses = (silent) => get("/api/addresses", null, silent);
