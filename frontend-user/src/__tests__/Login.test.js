// Login.vue 单元测试
// 1) 表单校验规则配置（用户名为空/密码为空提示、手机号格式、验证码 6 位、两次密码一致校验器）
//    说明：element-plus 表单 validate() 在 jsdom 下状态卡 validating（引擎自身正常），
//          故按「规则配置级」断言 + 登录成功/失败真实行为路径组合验证。
// 2) 点击登录调用登录接口（silent）；登录成功写 localStorage；2003 表单级错误提示
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia } from 'pinia'
import ElementPlus, { ElMessage } from 'element-plus'

const h = vi.hoisted(() => {
  const m = {
    login: vi.fn(),
    smsLogin: vi.fn(),
    sendSmsCode: vi.fn(),
    register: vi.fn(),
    push: vi.fn()
  }
  return m
})

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: {} }),
  useRouter: () => ({ push: h.push })
}))

vi.mock('@/api/auth', () => ({
  login: h.login,
  smsLogin: h.smsLogin,
  sendSmsCode: h.sendSmsCode,
  register: h.register
}))

import Login from '@/views/Login.vue'

function mountLogin() {
  return mount(Login, {
    global: { plugins: [ElementPlus, createPinia()] }
  })
}

function rulesOf(wrapper, index) {
  const form = wrapper.findAllComponents({ name: 'ElForm' })[index]
  return form.vm.rules ?? {}
}

function runValidator(validator, value) {
  return new Promise((resolve) => validator({}, value, (err) => resolve(err ? err.message : null)))
}

describe('Login.vue 表单校验规则', () => {
  let wrapper

  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    wrapper = mountLogin()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  it('密码登录规则：用户名为空提示"请输入用户名"、密码为空提示"请输入密码"', () => {
    const rules = rulesOf(wrapper, 0)
    expect(rules.username).toBeTruthy()
    expect(rules.username).toEqual(
      expect.arrayContaining([expect.objectContaining({ required: true, message: '请输入用户名' })])
    )
    expect(rules.password).toEqual(
      expect.arrayContaining([expect.objectContaining({ required: true, message: '请输入密码' })])
    )
  })

  it('短信登录规则：手机号必填+格式校验、验证码必填+6 位', () => {
    const rules = rulesOf(wrapper, 1)
    expect(rules.phone[0].required).toBe(true)
    expect(rules.phone[1].pattern).toBeInstanceOf(RegExp)
    expect(String(rules.phone[1].message)).toContain('手机号格式不正确')
    expect(rules.smsCode[1].len).toBe(6)
    expect(rules.smsCode[0].required).toBe(true)
  })

  it('注册规则：两次密码不一致时校验器报错、一致时通过', async () => {
    const rules = rulesOf(wrapper, 2)
    const validator = rules.confirmPassword.find((r) => typeof r.validator === 'function').validator
    // 在组件上下文中设置 password，再以不一致的确认密码触发校验器
    const regPane = wrapper.findAll('.el-tab-pane')[2]
    await regPane.find('input[placeholder="密码（6-20 位）"]').setValue('123456')
    const mismatch = await runValidator(validator, '654321')
    expect(mismatch).toBe('两次输入的密码不一致')
    const ok = await runValidator(validator, '123456')
    expect(ok).toBeNull()
  })

  it('用户名与密码均填写：点击登录调用登录接口（silent 表单级提示）', async () => {
    h.login.mockResolvedValue({ token: 'tk-1', userInfo: { id: 1, username: 'user001', role: 'USER' } })
    await wrapper.find('input[placeholder="用户名（3-20 位）"]').setValue('user001')
    await wrapper.find('input[placeholder="密码（6-20 位）"]').setValue('123456')
    await wrapper.find('.login-btn').trigger('click')
    await flushPromises()
    expect(h.login).toHaveBeenCalledTimes(1)
    expect(h.login).toHaveBeenCalledWith(
      { username: 'user001', password: '123456' },
      { silent: true }
    )
  })

  it('登录成功：写入 localStorage token/userInfo 并跳转首页', async () => {
    const spySet = vi.spyOn(Storage.prototype, 'setItem')
    h.login.mockResolvedValue({ token: 'tk-succ', userInfo: { id: 1, username: 'user001', role: 'USER' } })
    await wrapper.find('input[placeholder="用户名（3-20 位）"]').setValue('user001')
    await wrapper.find('input[placeholder="密码（6-20 位）"]').setValue('123456')
    await wrapper.find('.login-btn').trigger('click')
    await flushPromises()
    expect(spySet).toHaveBeenCalledWith('token', 'tk-succ')
    expect(spySet).toHaveBeenCalledWith('userInfo', JSON.stringify({ id: 1, username: 'user001', role: 'USER' }))
    expect(h.push).toHaveBeenCalledWith('/')
    spySet.mockRestore()
  })

  it('登录失败（2003）：表单级提示"用户名或密码错误"，不跳转', async () => {
    const spyError = vi.spyOn(ElMessage, 'error').mockImplementation(() => {})
    const err = new Error('用户名或密码错误')
    err.code = 2003
    h.login.mockRejectedValue(err)
    await wrapper.find('input[placeholder="用户名（3-20 位）"]').setValue('user001')
    await wrapper.find('input[placeholder="密码（6-20 位）"]').setValue('wrong')
    await wrapper.find('.login-btn').trigger('click')
    await flushPromises()
    expect(spyError).toHaveBeenCalledWith('用户名或密码错误')
    expect(h.push).not.toHaveBeenCalled()
    spyError.mockRestore()
  })

  it('短信登录成功：调用 smsLogin 并写入 token', async () => {
    h.smsLogin.mockResolvedValue({ token: 'tk-sms', userInfo: { username: 'user001', role: 'USER' } })
    await wrapper.find('input[placeholder="手机号"]').setValue('13800000001')
    await wrapper.find('input[placeholder="验证码"]').setValue('123456')
    const smsLoginBtn = wrapper.findAll('.login-btn')[1]
    await smsLoginBtn.trigger('click')
    await flushPromises()
    expect(h.smsLogin).toHaveBeenCalledWith(
      { phone: '13800000001', smsCode: '123456' },
      { silent: true }
    )
    expect(localStorage.getItem('token')).toBe('tk-sms')
  })
})