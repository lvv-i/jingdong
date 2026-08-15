# skill: admin-page-dev（后台页面开发步骤）

> 成员 C 专属 ｜ 用于 C1/C3/C4：快速按统一模板产出商家/管理员后台页面
> 基线：`.qoder/skills/frontend-component-guide.md`（组件选择）、`.qoder/rules/state-machine.md`（状态只读）

## 适用场景

开发 `admin-web/src/views/` 下任意后台页面时，按本模板执行。

## 页面开发统一步骤

1. **读契约三件套**（顺序固定）：
   - `docs/phase1/member-c/deliverables/C-01-后台页面清单.md` → 找到页面编号（C-Mxx / C-Axx）与关键内容
   - `docs/phase1/member-a/deliverables/05-接口清单v1.0.md` → 找到该页用到的 T5 编号行，抄录 URL/参数/返回/错误码/备注
   - `docs/phase1/member-a/deliverables/01-状态机枚举表.md` → 找到该页涉及状态对象的合法流转与禁止流转
2. **写接口封装**：在 `src/api/merchant.js` 或 `src/api/admin.js` 中新增函数，函数名 = 接口编号（如 `M011_shipOrder`、`A002_approveMerchant`），注释标注 T5 编号与 URL。
3. **实现页面骨架**：
   - 列表页：`el-table` + 状态筛选 `el-tabs`/`el-select` + 搜索框 + `el-pagination`（对齐分页约定 page/pageSize）
   - 表单页：`el-dialog`/独立路由表单 + `el-form` 校验（必填字段与 T5 请求参数一致）
   - 审核/裁决类：`el-dialog` 内展示单据详情 + `el-input type=textarea` 填原因/回复（驳回/拒绝/下架类必填）
4. **状态按钮显隐**：用 `src/utils/status.js` 中的状态映射表 + `v-if` 按状态渲染操作按钮；只允许 T1 合法流转的按钮出现。
5. **操作反馈闭环**：操作成功后 `ElMessage.success` + 重新拉取列表 + 关闭弹窗；失败按 request 封装的错误码映射提示。
6. **空状态**：列表无数据用 `el-table` 的 empty 插槽（插画 + 引导文案），不留空白。

## 页面类型模板索引

| 页面类型 | 参考模板要点 | 示例页面 |
|---|---|---|
| 统计仪表盘 | 卡片行（`el-row`/`el-col`）+ 数字 + 今日维度 | C-M01（M-015）、C-A07 统计部分（A-019） |
| 分页列表 + 状态操作 | tabs 状态筛选 + 行内按钮按状态显隐 | C-M02（M-004）、C-M04（M-009）、C-A01（A-001/A-010） |
| 表单 + 附件 | 图片上传（`el-upload`，对应后端文件接口）+ 富文本/textarea 详情 | C-M02 新建/编辑（M-003/M-005） |
| 审核/裁决弹窗 | 详情摘要 + 意见必填校验 + 双按钮（通过/驳回） | C-A01（A-002/A-003）、C-A06（A-015） |
| 树形管理 | `el-tree` + 节点操作菜单 | C-A02 类目管理（A-004~A-007） |
| 日志查询 | 多条件筛选 + 时间范围 + 只读表格 | C-A07 日志（A-018） |

## 完成自查

- [ ] 每个接口调用可回溯到 T5 编号（代码注释有标注）
- [ ] 所有状态操作按钮与 T1 合法流转一致，无禁止流转入口
- [ ] 必填校验与 T5 请求参数一致（尤其 auditReason/reply/adminResult/shippingNo）
- [ ] 分页、空状态、成功/失败反馈齐备
