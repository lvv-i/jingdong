<template>
  <!-- B-P04 登录/注册页（W1）
       账号密码登录（P-002）+ 短信验证码登录（P-007/P-008）+ 注册（P-001）
       依据：B-01 页面清单 + T5 接口清单 + T3 错误码分段表 -->
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <h1>京选商城</h1>
        <p>用户端 · 登录 / 注册</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 账号密码登录（P-002） -->
        <el-tab-pane label="密码登录" name="pwd">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="0" @submit.prevent>
            <el-form-item prop="username">
              <el-input v-model="pwdForm.username" placeholder="用户名（3-20 位）" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="pwdForm.password"
                type="password"
                show-password
                placeholder="密码（6-20 位）"
                :prefix-icon="Lock"
                size="large"
                @keyup.enter="handlePwdLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="login-btn" :loading="pwdLoading" @click="handlePwdLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 短信验证码登录（P-007/P-008，demo 固定码 123456） -->
        <el-tab-pane label="短信登录" name="sms">
          <el-form ref="smsFormRef" :model="smsForm" :rules="smsRules" label-width="0" @submit.prevent>
            <el-form-item prop="phone">
              <el-input v-model="smsForm.phone" placeholder="手机号" :prefix-icon="Iphone" size="large" maxlength="11" />
            </el-form-item>
            <el-form-item prop="smsCode">
              <div class="sms-row">
                <el-input v-model="smsForm.smsCode" placeholder="验证码" :prefix-icon="Key" size="large" maxlength="6" @keyup.enter="handleSmsLogin" />
                <el-button size="large" class="sms-btn" :disabled="countdown > 0" @click="handleSendCode">
                  {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="login-btn" :loading="smsLoading" @click="handleSmsLogin">
                登 录
              </el-button>
            </el-form-item>
            <div class="sms-hint">演示环境验证码固定为 123456（D-02 方案）</div>
          </el-form>
        </el-tab-pane>

        <!-- 注册（P-001，注册角色默认 USER） -->
        <el-tab-pane label="注册" name="register">
          <el-form ref="regFormRef" :model="regForm" :rules="regRules" label-width="0" @submit.prevent>
            <el-form-item prop="username">
              <el-input v-model="regForm.username" placeholder="用户名（3-20 位）" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="regForm.password" type="password" show-password placeholder="密码（6-20 位）" :prefix-icon="Lock" size="large" />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="regForm.confirmPassword" type="password" show-password placeholder="确认密码" :prefix-icon="Lock" size="large" />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input v-model="regForm.phone" placeholder="手机号" :prefix-icon="Iphone" size="large" maxlength="11" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="login-btn" :loading="regLoading" @click="handleRegister">
                注 册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Key } from '@element-plus/icons-vue'
import { login, smsLogin, sendSmsCode, register } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('pwd')

// ---------- 账号密码登录（P-002） ----------
const pwdFormRef = ref()
const pwdLoading = ref(false)
const pwdForm = reactive({ username: '', password: '' })
const pwdRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// ---------- 短信登录（P-007/P-008） ----------
const smsFormRef = ref()
const smsLoading = ref(false)
const smsForm = reactive({ phone: '', smsCode: '' })
const phonePattern = /^1[3-9]\d{9}$/
const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位数字', trigger: 'blur' }
  ]
}
const countdown = ref(0)
let timer = null

/** P-007 发送短信验证码（60 秒限频，后端 1007 兜底） */
async function handleSendCode() {
  try {
    await smsFormRef.value.validateField('phone')
  } catch {
    return
  }
  try {
    await sendSmsCode(smsForm.phone)
    ElMessage.success('验证码已发送（演示环境固定 123456）')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    // 1007 操作过于频繁等由全局 toast 展示，此处不重复提示
  }
}

// ---------- 注册（P-001） ----------
const regFormRef = ref()
const regLoading = ref(false)
const regForm = reactive({ username: '', password: '', confirmPassword: '', phone: '' })
const regRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== regForm.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

/** 登录成功统一处理：写入 store → 回跳 redirect 或首页 */
function onLoginSuccess(data) {
  userStore.setLogin({ token: data.token, userInfo: data.userInfo })
  ElMessage.success(`欢迎回来，${data.userInfo?.username || ''}`)
  const redirect = route.query.redirect
  router.push(typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/')
}

/** P-002 账号密码登录（silent：2003/2004 表单级特殊文案） */
async function handlePwdLogin() {
  try {
    await pwdFormRef.value.validate()
  } catch {
    return
  }
  pwdLoading.value = true
  try {
    const data = await login({ username: pwdForm.username, password: pwdForm.password }, { silent: true })
    onLoginSuccess(data)
  } catch (e) {
    // T3：2003 用户名或密码错误 / 2004 账号已被禁用（表单级提示，不依赖全局 toast）
    if (e.code === 2003) ElMessage.error('用户名或密码错误')
    else if (e.code === 2004) ElMessage.error('账号已被禁用')
  } finally {
    pwdLoading.value = false
  }
}

/** P-008 短信验证码登录（silent：2003 表单级特殊文案；T5 文档所列 2005 为笔误，后端不产生） */
async function handleSmsLogin() {
  try {
    await smsFormRef.value.validate()
  } catch {
    return
  }
  smsLoading.value = true
  try {
    const data = await smsLogin({ phone: smsForm.phone, smsCode: smsForm.smsCode }, { silent: true })
    onLoginSuccess(data)
  } catch (e) {
    // 2003 验证码错误或过期（SmsCodeService 使用 LOGIN_FAILED=2003）
    if (e.code === 2003) ElMessage.error('手机号或验证码错误')
  } finally {
    smsLoading.value = false
  }
}

/** P-001 注册（silent：2001/2002 表单级特殊文案） */
async function handleRegister() {
  try {
    await regFormRef.value.validate()
  } catch {
    return
  }
  regLoading.value = true
  try {
    await register(
      { username: regForm.username, password: regForm.password, phone: regForm.phone },
      { silent: true }
    )
    ElMessage.success('注册成功，请登录')
    // 注册成功后切换到密码登录并回填用户名
    pwdForm.username = regForm.username
    pwdForm.password = ''
    activeTab.value = 'pwd'
  } catch (e) {
    if (e.code === 2001) ElMessage.error('用户名已存在')
    else if (e.code === 2002) ElMessage.error('手机号已注册')
  } finally {
    regLoading.value = false
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff4d4f 0%, #e60012 60%, #b3000c 100%);
}
.login-card {
  width: 420px;
  padding: 32px 36px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
}
.login-brand {
  text-align: center;
  margin-bottom: 16px;
}
.login-brand h1 {
  margin: 0;
  font-size: 28px;
  color: #e60012;
}
.login-brand p {
  margin: 6px 0 0;
  color: #909399;
  font-size: 13px;
}
.login-btn {
  width: 100%;
}
.sms-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.sms-btn {
  flex-shrink: 0;
  width: 120px;
}
.sms-hint {
  font-size: 12px;
  color: #909399;
  text-align: center;
  margin-top: -4px;
  margin-bottom: 8px;
}
</style>
