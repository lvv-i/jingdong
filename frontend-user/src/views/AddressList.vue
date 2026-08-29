<template>
  <!-- B-P12 地址管理：U-003 列表 + U-004 新增 + U-005 编辑 + U-006 删除 + U-007 设默认；超 20 个 2006 提示 -->
  <div class="address-page">
    <div class="page-header">
      <h2>收货地址</h2>
      <el-button type="danger" :disabled="addresses.length >= 20" @click="openDialog()">
        新增地址
      </el-button>
    </div>

    <div v-loading="loading" class="address-body">
      <EmptyState
        v-if="!loading && addresses.length === 0"
        title="还没有收货地址"
        description="添加地址后下单更快捷"
      >
        <el-button type="danger" @click="openDialog()">新增地址</el-button>
      </EmptyState>

      <div v-else class="address-grid">
        <div v-for="addr in addresses" :key="addr.id" class="address-card">
          <div class="ac-head">
            <span class="ac-receiver">{{ addr.receiver }}</span>
            <span class="ac-phone">{{ addr.phone }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small" type="danger" effect="plain">默认</el-tag>
          </div>
          <div class="ac-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</div>
          <div class="ac-actions">
            <el-button v-if="addr.isDefault !== 1" link type="primary" :loading="addr.setting" @click="handleSetDefault(addr)">
              设为默认
            </el-button>
            <el-button link type="primary" @click="openDialog(addr)">编辑</el-button>
            <el-button link type="danger" :loading="addr.deleting" @click="handleDelete(addr)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗（U-004/U-005） -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑地址' : '新增地址'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="90px">
        <el-form-item label="收货人" required>
          <el-input v-model="form.receiver" maxlength="50" placeholder="收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="省份" required>
          <el-input v-model="form.province" placeholder="如：广东省" />
        </el-form-item>
        <el-form-item label="城市" required>
          <el-input v-model="form.city" placeholder="如：广州市" />
        </el-form-item>
        <el-form-item label="区/县">
          <el-input v-model="form.district" placeholder="选填，如：天河区" />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAddresses, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'
import EmptyState from '@/components/EmptyState.vue'

const addresses = ref([])
const loading = ref(true)

const dialogVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const form = reactive({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

/** U-003 地址列表 */
async function loadAddresses() {
  loading.value = true
  try {
    const addrData = await getAddresses({ silent: true })
    // 双兼容：旧 jar 裸数组 / 新后端 PageResult{list,total}（A 已按 T5 契约修复）
    addresses.value = Array.isArray(addrData) ? addrData : (addrData && addrData.list) || []
  } catch {
    addresses.value = []
  } finally {
    loading.value = false
  }
}

/** 打开新增/编辑弹窗 */
function openDialog(addr) {
  editingId.value = addr ? addr.id : null
  Object.assign(form, {
    receiver: addr?.receiver || '',
    phone: addr?.phone || '',
    province: addr?.province || '',
    city: addr?.city || '',
    district: addr?.district || '',
    detail: addr?.detail || '',
    isDefault: addr?.isDefault ?? 0
  })
  dialogVisible.value = true
}

/** U-004/U-005 保存（2006 超上限提示；前端校验必填与手机号） */
async function handleSave() {
  if (!form.receiver.trim()) return ElMessage.warning('请填写收货人')
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return ElMessage.warning('联系电话格式不正确（1 开头 11 位数字）')
  if (!form.province.trim()) return ElMessage.warning('请填写省份')
  if (!form.city.trim()) return ElMessage.warning('请填写城市')
  if (!form.detail.trim()) return ElMessage.warning('请填写详细地址')
  submitting.value = true
  try {
    const payload = {
      receiver: form.receiver.trim(),
      phone: form.phone,
      province: form.province.trim(),
      city: form.city.trim(),
      district: form.district.trim(),
      detail: form.detail.trim(),
      isDefault: form.isDefault
    }
    if (editingId.value) {
      await updateAddress(editingId.value, payload, { silent: true })
      ElMessage.success('地址已更新')
    } else {
      await addAddress(payload, { silent: true })
      ElMessage.success('地址已添加')
    }
    dialogVisible.value = false
    loadAddresses()
  } catch (err) {
    if (err.code === 2006) {
      ElMessage.warning('收货地址已达上限（20 个），请先删除不需要的地址')
    }
    // 其他错误全局拦截器已提示
  } finally {
    submitting.value = false
  }
}

/** U-006 删除（二次确认） */
async function handleDelete(addr) {
  try {
    await ElMessageBox.confirm(`确定删除「${addr.receiver}」的地址吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  addr.deleting = true
  try {
    await deleteAddress(addr.id, { silent: true })
    ElMessage.success('已删除')
    loadAddresses()
  } finally {
    addr.deleting = false
  }
}

/** U-007 设为默认 */
async function handleSetDefault(addr) {
  addr.setting = true
  try {
    await setDefaultAddress(addr.id, { silent: true })
    ElMessage.success('已设为默认地址')
    loadAddresses()
  } finally {
    addr.setting = false
  }
}

onMounted(loadAddresses)
</script>

<style scoped>
.address-page {
  max-width: 900px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 14px;
}
.page-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
  border-left: 4px solid #e60012;
  padding-left: 10px;
}
.address-body {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  min-height: 140px;
}
.address-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.address-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px 14px;
}
.ac-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.ac-receiver {
  font-weight: 600;
  color: #333;
}
.ac-phone {
  color: #666;
  font-size: 13px;
}
.ac-detail {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  min-height: 40px;
}
.ac-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f5f5f5;
}
</style>
