// Cart.vue 单元测试
// 全选联动 / 数量步进与小计/合计重算 / 删除移除商品
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus, { ElCheckbox, ElInputNumber, ElMessage, ElMessageBox } from 'element-plus'

const h = vi.hoisted(() => {
  const m = {
    getCartItems: vi.fn(),
    updateCartItem: vi.fn(),
    deleteCartItem: vi.fn(),
    push: vi.fn()
  }
  return m
})

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: h.push })
}))

vi.mock('@/api/cart', () => ({
  getCartItems: h.getCartItems,
  updateCartItem: h.updateCartItem,
  deleteCartItem: h.deleteCartItem
}))

import Cart from '@/views/Cart.vue'

// 后端 U-008 返回 {list,total}（T5 契约）
const seedItems = [
  { id: 1, productId: 101, title: '手机 A', price: 2999, quantity: 1, selected: 1, stock: 10 },
  { id: 2, productId: 102, title: '手机 B', price: 1999, quantity: 2, selected: 0, stock: 5 }
]

function mountCart() {
  h.getCartItems.mockResolvedValue({ list: JSON.parse(JSON.stringify(seedItems)), total: 2 })
  const wrapper = mount(Cart, { global: { plugins: [ElementPlus] } })
  return wrapper
}

describe('Cart.vue', () => {
  let wrapper

  beforeEach(async () => {
    vi.clearAllMocks()
    h.updateCartItem.mockResolvedValue(null)
    h.deleteCartItem.mockResolvedValue(null)
    vi.spyOn(ElMessage, 'success').mockImplementation(() => {})
    vi.spyOn(ElMessageBox, 'confirm').mockResolvedValue('confirm')
    wrapper = mountCart()
    await flushPromises()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    wrapper.unmount()
  })

  it('加载购物车列表渲染商品行', () => {
    expect(h.getCartItems).toHaveBeenCalled()
    expect(wrapper.text()).toContain('手机 A')
    expect(wrapper.text()).toContain('手机 B')
  })

  it('全选：点击表头全选后所有子项 selected 同步为 1', async () => {
    const allCheckbox = wrapper.findComponent(ElCheckbox)
    await allCheckbox.vm.$emit('change', true)
    await flushPromises()
    // 仅未被勾选的商品行触发接口，其余已勾选不动
    expect(h.updateCartItem).toHaveBeenCalledWith(2, { selected: 1 })
    // 所有子项联动选中
    const items = wrapper.vm.items
    expect(items.every((i) => i.selected === 1)).toBe(true)
    expect(allCheckbox.props('modelValue')).toBe(true)
  })

  it('全选：再次点击取消全选，所有子项 selected 同步为 0', async () => {
    const allCheckbox = wrapper.findComponent(ElCheckbox)
    await allCheckbox.vm.$emit('change', true)
    await flushPromises()
    vi.clearAllMocks()
    h.updateCartItem.mockResolvedValue(null)
    await allCheckbox.vm.$emit('change', false)
    await flushPromises()
    expect(h.updateCartItem).toHaveBeenCalledWith(1, { selected: 0 })
    expect(h.updateCartItem).toHaveBeenCalledWith(2, { selected: 0 })
    expect(wrapper.vm.items.every((i) => i.selected === 0)).toBe(true)
  })

  it('数量步进：数量加改为 3 后商品数量与小计、合计重算', async () => {
    const firstNumber = wrapper.findAllComponents(ElInputNumber)[0]
    await firstNumber.vm.$emit('change', 3)
    await flushPromises()
    expect(h.updateCartItem).toHaveBeenCalledWith(1, { quantity: 3 })
    const item = wrapper.vm.items.find((i) => i.id === 1)
    expect(item.quantity).toBe(3)
    // 商品 A 小计 2999*3=8997.00；合计仅统计勾选项（商品 B 未勾选不参与）
    expect(wrapper.text()).toContain('8997.00')
    expect(wrapper.find('.footer-summary').text()).toContain('已选 3 件')
    expect(wrapper.find('.footer-summary').text()).toContain('8997.00')
  })

  it('数量步进：接口失败时回退并重新加载列表', async () => {
    h.updateCartItem.mockRejectedValueOnce(new Error('fail'))
    const firstNumber = wrapper.findAllComponents(ElInputNumber)[0]
    await firstNumber.vm.$emit('change', 5)
    await flushPromises()
    expect(h.getCartItems).toHaveBeenCalledTimes(2) // 初载 + 回退刷新
  })

  it('删除：二次确认后调用删除接口并从列表移除', async () => {
    await wrapper.findAll('button').find((b) => b.text().includes('删除')).trigger('click')
    await flushPromises()
    expect(ElMessageBox.confirm).toHaveBeenCalled()
    expect(h.deleteCartItem).toHaveBeenCalledWith(1)
    expect(wrapper.text()).not.toContain('手机 A')
    expect(wrapper.text()).toContain('手机 B')
    expect(ElMessage.success).toHaveBeenCalled()
  })

  it('删除：取消二次确认时不删除', async () => {
    ElMessageBox.confirm.mockRejectedValueOnce(new Error('cancel'))
    await wrapper.findAll('button').find((b) => b.text().includes('删除')).trigger('click')
    await flushPromises()
    expect(h.deleteCartItem).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('手机 A')
  })

  it('未选中商品时"去结算"按钮禁用', async () => {
    const allCheckbox = wrapper.findComponent(ElCheckbox)
    await allCheckbox.vm.$emit('change', true)
    await flushPromises()
    vi.clearAllMocks()
    h.updateCartItem.mockResolvedValue(null)
    await allCheckbox.vm.$emit('change', false)
    await flushPromises()
    const checkoutBtn = wrapper.findAll('button').find((b) => b.text().includes('去结算'))
    expect(checkoutBtn.attributes('disabled')).toBeDefined()
  })
})