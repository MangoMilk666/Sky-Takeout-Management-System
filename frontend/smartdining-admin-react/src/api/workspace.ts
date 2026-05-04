import { http } from '@/lib/http/request'

export const getOrderOverview = () =>
  http({
    url: '/workspace/overviewOrders',
    method: 'get',
  })

export const getOverviewDishes = () =>
  http({
    url: '/workspace/overviewDishes',
    method: 'get',
  })

export const getOverviewSetmeals = () =>
  http({
    url: '/workspace/overviewSetmeals',
    method: 'get',
  })

export const getBusinessData = () =>
  http({
    url: '/workspace/businessData',
    method: 'get',
  })

export const getTurnoverStatistics = (params: any) =>
  http({
    url: '/report/turnoverStatistics',
    method: 'get',
    params,
  })

export const getUserStatistics = (params: any) =>
  http({
    url: '/report/userStatistics',
    method: 'get',
    params,
  })

export const getOrderStatistics = (params: any) =>
  http({
    url: '/report/ordersStatistics',
    method: 'get',
    params,
  })

export const getTop10 = (params: any) =>
  http({
    url: '/report/top10',
    method: 'get',
    params,
  })

export const getDataOverView = (params: any) =>
  http({
    url: '/report/dataOverView',
    method: 'get',
    params,
  })

