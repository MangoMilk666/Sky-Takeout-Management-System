import { http } from '@/lib/http/request'

export const getShopStatus = () =>
  http({
    url: '/shop/status',
    method: 'get',
  })

export const setShopStatus = (status: 0 | 1) =>
  http({
    url: `/shop/${status}`,
    method: 'put',
  })

