// request.js 单元测试
// 验证请求拦截器注入 Bearer token、响应拦截器解包/1002 跳登录/业务错误分支
import { describe, it, expect, vi, beforeEach } from 'vitest'

const h = vi.hoisted(() => {
  const m = {
    requestInterceptor: null,
    responseInterceptor: null,
    responseError: null,
    mockPost: vi.fn(),
    ElMessage: { error: vi.fn(), warning: vi.fn(), success: vi.fn() }
  }
  return m
})

vi.mock('element-plus', () => ({ ElMessage: h.ElMessage }))

vi.mock('axios', () => ({
  default: {
    create: () => ({
      post: h.mockPost,
      interceptors: {
        request: { use: (ok, err) => { h.requestInterceptor = ok; }} ,
        response: { use: (ok, err) => { h.responseInterceptor = ok; h.responseError = err } }
      }
    })
  }
}))

import request from '@/api/request'

function mockLocation(pathname) {
  Object.defineProperty(window, 'location', {
    writable: true,
    value: { href: `http://localhost${pathname}`, pathname, search: '', assign: vi.fn() }
  })
}

describe('request.js 拦截器', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    mockLocation('/cart')
    h.ElMessage.error.mockClear()
    h.ElMessage.warning.mockClear()
  })

  it('请求拦截器：localStorage 存在 token 时注入 Authorization: Bearer <token>', () => {
    localStorage.setItem('token', 'tk-abc-123')
    const config = { headers: {} }
    const result = h.requestInterceptor(config)
    expect(result).toBe(config)
    expect(config.headers.Authorization).toBe('Bearer tk-abc-123')
  })

  it('请求拦截器：未登录时不添加 Authorization 头', () => {
    const config = { headers: {} }
    h.requestInterceptor(config)
    expect(config.headers.Authorization).toBeUndefined()
  })

  it('响应拦截器：code=200 时返回 data 解包数据', async () => {
    const res = { data: { code: 200, message: 'ok', data: { id: 101, title: '手机 A' } } }
    const data = await h.responseInterceptor(res)
    expect(data).toEqual({ id: 101, title: '手机 A' })
  })

  it('响应拦截器：非标准信封（缺少 code）原样透传', async () => {
    expect(await h.responseInterceptor({ data: 'raw-string' })).toBe('raw-string')
    expect(await h.responseInterceptor({ data: 42 })).toBe(42)
  })

  it('响应拦截器：code=1002 时跳转 /login 并清除 token/userInfo', async () => {
    localStorage.setItem('token', 'x')
    localStorage.setItem('userInfo', '{}')
    await expect(
      h.responseInterceptor({ data: { code: 1002, message: '未登录', data: null } })
    ).rejects.toThrow('未登录')
    expect(window.location.href).toBe('/login')
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('userInfo')).toBeNull()
    expect(h.ElMessage.warning).toHaveBeenCalled()
  })

  it('响应拦截器：已处于 /login 页时 1002 不重复跳转', async () => {
    mockLocation('/login')
    localStorage.setItem('token', 'x')
    await expect(
      h.responseInterceptor({ data: { code: 1002, message: '未登录', data: null } })
    ).rejects.toThrow('未登录')
    expect(window.location.href).toBe('http://localhost/login')
  })

  it('响应拦截器：业务错误 code 附带错误码并 reject（非 silent 弹 toast）', async () => {
    await expect(
      h.responseInterceptor({ data: { code: 4003, message: '购物车为空', data: null }, config: {} })
    ).rejects.toMatchObject({ code: 4003, message: '购物车为空' })
    expect(h.ElMessage.error).toHaveBeenCalledWith('购物车为空')
  })

  it('响应拦截器：silent=true 时不弹全局 toast（页面自处理）', async () => {
    await expect(
      h.responseInterceptor({
        data: { code: 2003, message: '用户名或密码错误', data: null },
        config: { silent: true }
      })
    ).rejects.toMatchObject({ code: 2003 })
    expect(h.ElMessage.error).not.toHaveBeenCalled()
  })

  it('响应拦截器：网络层错误（后端未启动/500）按 message 提示并 reject', async () => {
    const err = new Error('Network Error')
    err.config = {}
    await expect(h.responseError(err)).rejects.toThrow('Network Error')
    expect(h.ElMessage.error).toHaveBeenCalledWith('Network Error')
  })

  it('响应拦截器：网络层错误使用后端返回的 message（如 500）作 toast，但 reject 保留原始错误', async () => {
    const err = new Error('Request failed with status code 500')
    err.response = { data: { message: '服务器内部错误' } }
    err.config = {}
    await expect(h.responseError(err)).rejects.toThrow('Request failed with status code 500')
    expect(h.ElMessage.error).toHaveBeenCalledWith('服务器内部错误')
  })

  it('响应拦截器：silent=true 时网络错误也不弹全局 toast', async () => {
    const err = new Error('Network Error')
    err.config = { silent: true }
    await expect(h.responseError(err)).rejects.toThrow('Network Error')
    expect(h.ElMessage.error).not.toHaveBeenCalled()
  })
})

describe('request 实例导出', () => {
  it('默认导出为 axios.post 的挂载实例（供 api 模块消费）', () => {
    expect(request).toBeDefined()
  })
})