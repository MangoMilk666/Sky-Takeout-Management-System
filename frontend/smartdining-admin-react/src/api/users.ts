import { http } from '@/lib/http/request'

export const editPassword = (data: { oldPassword: string; newPassword: string }) =>
  http({
    url: '/employee/editPassword',
    method: 'put',
    data,
  })

