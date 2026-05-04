import { http } from '@/lib/http/request'

export const getDishPage = (params: any) =>
  http({
    url: '/dish/page',
    method: 'get',
    params,
  })

export const deleteDish = (ids: string) =>
  http({
    url: '/dish',
    method: 'delete',
    params: { ids },
  })

export const editDish = (params: any) =>
  http({
    url: '/dish',
    method: 'put',
    data: { ...params },
  })

export const addDish = (params: any) =>
  http({
    url: '/dish',
    method: 'post',
    data: { ...params },
  })

export const queryDishById = (id: string | (string | null)[]) =>
  http({
    url: `/dish/${id}`,
    method: 'get',
  })

export const getCategoryList = (params: any) =>
  http({
    url: '/category/list',
    method: 'get',
    params,
  })

export const queryDishList = (params: any) =>
  http({
    url: '/dish/list',
    method: 'get',
    params,
  })

export const commonDownload = (params: any) =>
  http({
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
    },
    url: '/common/download',
    method: 'get',
    params,
  })

export const dishStatusByStatus = (params: any) =>
  http({
    url: `/dish/status/${params.status}`,
    method: 'post',
    params: { id: params.id },
  })

