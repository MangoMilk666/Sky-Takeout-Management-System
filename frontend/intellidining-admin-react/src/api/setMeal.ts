import { http } from '@/lib/http/request'

export const getSetmealPage = (params: any) =>
  http({
    url: '/setmeal/page',
    method: 'get',
    params,
  })

export const deleteSetmeal = (ids: string) =>
  http({
    url: '/setmeal',
    method: 'delete',
    params: { ids },
  })

export const editSetmeal = (params: any) =>
  http({
    url: '/setmeal',
    method: 'put',
    data: { ...params },
  })

export const addSetmeal = (params: any) =>
  http({
    url: '/setmeal',
    method: 'post',
    data: { ...params },
  })

export const querySetmealById = (id: string | (string | null)[]) =>
  http({
    url: `/setmeal/${id}`,
    method: 'get',
  })

export const setmealStatusByStatus = (params: any) =>
  http({
    url: `/setmeal/status/${params.status}`,
    method: 'post',
    params: { id: params.ids },
  })

