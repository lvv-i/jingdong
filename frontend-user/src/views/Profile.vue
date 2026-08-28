<template>
  <!-- B-P11 个人中心：U-001 展示 + U-002 修改资料与改密（原密码校验） -->
  <div class="profile-page">
    <!-- 资料展示（U-001） -->
    <section class="panel">
      <div class="panel-title">个人资料</div>
      <div v-loading="loading" class="profile-info">
        <div class="info-row"><span class="k">用户名</span><span class="v">{{ profile.username || '-' }}</span></div>
        <div class="info-row"><span class="k">手机号</span><span class="v">{{ profile.phone || '-' }}</span></div>
        <div class="info-row">
          <span class="k">角色</span>
          <el-tag size="small" :type="roleType">{{ roleLabel }}</el-tag>
        </div>
      </div>
    </section>

    <!-- 修改手机号（U-002） -->
    <section class="panel">
      <div class="panel-title">修改资料</div>
      <el-form :model="editForm" label-width="90px" class="edit-form">
        <el-form-item label="手机号">
          <el-input
            v-model="editForm.phone"
            placeholder="11 位手机号（留空表示不修改）"
            maxlength="11"
            style="width: 300px"
          />
          <div class="form-tip">格式：1 开头 11 位数字；留空不修改</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSavePhone">保存资料</el-button>
        </el-form-item>
      </el-form>
    </section>

    <!-- 修改密码（U-002 改密需原密码校验） -->
    <section class="panel">
      <div class="panel-title">修改密码</div>
      <el-form :model="pwdForm" label-width="90px" class="edit-form">
        <el-form-item label="原密码">
          <el-input
            v-model="pwdForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            show-password
            placeholder="6-20 位新密码"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入新密码"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" :loading="changingPwd" @click="handleChangePwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const profile = ref({})
const loading = ref(true)
const saving = ref(false)
const changingPwd = ref(false)

const editForm = reactive({ phone: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const roleLabel = computed(() => {
  const map = { USER: '普通用户', MERCHANT: '商家', ADMIN: '管理员' }
  return map[profile.value.role] || profile.value.role || '-'
})
const roleType = computed(() => {
  const map = { USER: 'info', MERCHANT: 'warning', ADMIN: 'danger' }
  return map[profile.value.role] || 'info'
})

/** U-001 个人资料 */
async function loadProfile() {
  loading.value = true
  try {
    profile.value = (await getProfile({ silent: true })) || {}
    editForm.phone = profile.value.phone || ''
  } catch {
    profile.value = {}
  } finally {
    loading.value = false
  }
}

/** U-002 修改手机号（留空不修改；失败按 code 提示） */
async function handleSavePhone() {
  const phone = editForm.phone.trim()
  if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('手机号格式不正确（1 开头 11 位数字）')
    return
  }
  saving.value = true
  try {
    await updateProfile({ phone }, { silent: true })
    ElMessage.success('资料已更新')
    userStore.userInfo = { ...userStore.userInfo, phone }
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    loadProfile()
  } catch (err) {
    ElMessage.warning(err.message || '保存失败')
  } finally {
    saving.value = false
  }
}

/** U-002 修改密码（原密码校验；1001 原密码错误提示） */
async function handleChangePwd() {
  if (!pwdForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!pwdForm.newPassword || pwdForm.newPassword.length < 6 || pwdForm.newPassword.length > 20) {
    ElMessage.warning('新密码长度 6-20 位')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  changingPwd.value = true
  try {
    await updateProfile(
      { oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword },
      { silent: true }
    )
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (err) {
    ElMessage.warning(err.message || '修改失败')
  } finally {
    changingPwd.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page {
  max-width: 700px;
}
.panel {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 14px;
}
.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  border-left: 4px solid #e60012;
  padding-left: 10px;
  margin-bottom: 14px;
}
.profile-info {
  min-height: 80px;
}
.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}
.info-row .k {
  width: 90px;
  color: #999;
}
.info-row .v {
  color: #333;
}
.edit-form {
  max-width: 480px;
}
.form-tip {
  width: 100%;
  font-size: 12px;
  color: #999;
  line-height: 1.6;
}
</style>
