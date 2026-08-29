/**
 * 用户/鉴权接口模块（T5 公共组 P-001/P-002/P-007/P-008）
 * 每个函数注释标注 T5 编号、状态流转与主要错误码
 */
import { get, post, put } from "../utils/request";

// P-001 注册：错误码 1001/2001/2002；注册角色默认 USER
export const register = (data) => post("/api/users/register", data);

// P-002 账号密码登录：错误码 1001/2003/2004；商家登录返回 shopId
export const login = (data) => post("/api/users/login", data);

// P-007 发送短信验证码：错误码 1001/1007；demo 固定码 123456；60 秒限频
export const sendSmsCode = (phone) => post("/api/users/sms-code", { phone });

// P-008 验证码登录：错误码 1001/2003（验证码错误或过期）/2004（账号禁用）；未注册手机号自动注册（D 增补）；T5 列 2005 为笔误
export const smsLogin = (data) => post("/api/users/login/sms", data);

// U-001 个人资料（X5 使用，先行封装）：错误码 1002
export const getProfile = () => get("/api/users/profile");

// U-002 修改资料：错误码 1001/2002；改密字段 oldPassword/newPassword（原密码错误抛 2003）
export const updateProfile = (data) => put("/api/users/profile", data);
