import { http } from '@/lib/http/request'

export const login = (data: any) =>
  http({
    url: '/employee/login',
    method: 'post',
    data,
  })

export const userLogout = (params: any) =>
  http({
    url: '/employee/logout',
    method: 'post',
    params,
  })

export const getEmployeeList = (params: any) =>
  http({
    url: '/employee/page',
    method: 'get',
    params,
  })

export const enableOrDisableEmployee = (params: any) =>
  http({
    url: `/employee/status/${params.status}`,
    method: 'post',
    params: { id: params.id },
  })

export const addEmployee = (params: any) =>
  http({
    url: '/employee',
    method: 'post',
    data: { ...params },
  })

export const editEmployee = (params: any) =>
  http({
    url: '/employee',
    method: 'put',
    data: { ...params },
  })

export const queryEmployeeById = (id: string | (string | null)[]) =>
  http({
    url: `/employee/${id}`,
    method: 'get',
  })

