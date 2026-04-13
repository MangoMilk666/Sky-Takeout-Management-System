import { http } from '@/lib/http/request'

export type CouponRow = {
  id: number
  name: string
  discountType: number
  discount: number
  totalCount: number
  remainedCount: number
  beginTime: string
  endTime: string
  createTime?: string
  updateTime?: string
}

export const publishCoupon = (data: {
  name: string
  discountType: number
  discount: number
  totalCount: number
  beginTime: string
  endTime: string
}) =>
  http({
    url: '/coupon/publish',
    method: 'post',
    data,
  })

export const listCoupons = () =>
  http({
    url: '/coupon/list',
    method: 'get',
  })

