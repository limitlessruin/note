import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Helloworld from '../Helloworld.vue'

describe('Helloworld.vue', () => {
  it('renders properly', () => {
    const wrapper = mount(Helloworld)
    expect(wrapper.text()).toContain('Hello')
  })

  it('has correct component structure', () => {
    const wrapper = mount(Helloworld)
    expect(wrapper.exists()).toBe(true)
  })
})