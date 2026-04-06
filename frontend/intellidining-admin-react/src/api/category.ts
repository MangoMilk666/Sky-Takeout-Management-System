import { http } from '@/lib/http/request'

export const getCategoryPage = (params: any) =>
  http({
    url: '/category/page',
    method: 'get',
    params,
  })

export const deleteCategory = (id: string) =>
  http({
    url: '/category',
    method: 'delete',
    params: { id },
  })

export const editCategory = (params: any) =>
  http({
    url: '/category',
    method: 'put',
    data: { ...params },
  })

export const addCategory = (params: any) =>
  http({
    url: '/category',
    method: 'post',
    data: { ...params },
  })

export const toggleCategoryStatus = (params: any) =>
  http({
    url: `/category/status/${params.status}`,
    method: 'post',
    params: { id: params.id },
  })

