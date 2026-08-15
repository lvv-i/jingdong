<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="login-title">
          <h2>京东风格电商平台</h2>
          <p>商家后台 · 管理员后台</p>
        </div>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tips">
        <p>演示账号（seed.sql）：商家 merchant001 / 管理员 admin001，密码 123456</p>
        <p>登录后按角色进入对应后台：MERCHANT → 商家后台，ADMIN → 管理员后台</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
// C2 任务书：登录页。调 P-002，成功按 role 分流（T4：商家登录签发 shopId）
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { P002_login } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    // P-002：{token, userInfo:{id,username,role,shopId}}
    const data = await P002_login({ username: form.username, password: form.password })
    if (!data.token) {
      ElMessage.error('登录失败：未返回凭证')
      return
    }
    userStore.setLogin({ token: data.token, userInfo: data.userInfo })
    // 非后台角色不允许进入（USER 登录提示无权限，对应 1003 语义）
    if (data.userInfo.role === 'MERCHANT') {
      ElMessage.success('登录成功，欢迎商家')
      router.replace('/merchant/dashboard')
    } else if (data.userInfo.role === 'ADMIN') {
      ElMessage.success('登录成功，欢迎管理员')
      router.replace('/admin/audit')
    } else {
      userStore.logout()
      ElMessage.warning('该账号无后台权限，请使用商家或管理员账号')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #c81623 0%, #a0121b 100%);
}
.login-card {
  width: 400px;
}
.login-title {
  text-align: center;
}
.login-title h2 {
  margin: 0;
  color: #c81623;
}
.login-title p {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}
.login-tips {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
.login-tips p {
  margin: 2px 0;
}
</style>
