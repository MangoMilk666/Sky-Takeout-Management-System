import { http } from '@/lib/http/request'

export const getOrderDetailPage = (params: any) =>
  http({
    url: '/order/conditionSearch',
    method: 'get',
    params,
  })

export const queryOrderDetailById = (params: any) =>
  http({
    url: `/order/details/${params.orderId}`,
    method: 'get',
  })

export const deliveryOrder = (params: any) =>
  http({
    url: `/order/delivery/${params.id}`,
    method: 'put',
  })

export const completeOrder = (params: any) =>
  http({
    url: `/order/complete/${params.id}`,
    method: 'put',
  })

export const orderCancel = (params: any) =>
  http({
    url: '/order/cancel',
    method: 'put',
    data: { ...params },
  })

export const orderAccept = (params: any) =>
  http({
    url: '/order/confirm',
    method: 'put',
    data: { ...params },
  })

export const orderReject = (params: any) =>
  http({
    url: '/order/rejection',
    method: 'put',
    data: { ...params },
  })

export const getOrderStatistics = () =>
  http({
    url: '/order/statistics',
    method: 'get',
  })

