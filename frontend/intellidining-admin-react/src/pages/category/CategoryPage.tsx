import { useEffect, useMemo, useState } from 'react'
import { addCategory, deleteCategory, editCategory, getCategoryPage, toggleCategoryStatus } from '@/api/category'
import { ElButton } from '@/components/legacy-vue/ElButton'
import { ElDialog } from '@/components/legacy-vue/ElDialog'
import { ElInput } from '@/components/legacy-vue/ElInput'
import { ElPagination } from '@/components/legacy-vue/ElPagination'
import { ElSelect } from '@/components/legacy-vue/ElSelect'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { usePageTitle } from '@/lib/ui/usePageTitle'

type CategoryRow = {
  id: string
  name: string
  type: number | string
  sort: number | string
  status: number | string
  updateTime: string
}

export function CategoryPage() {
  usePageTitle('smart-dining智能点餐系统 - 分类管理')
  const [name, setName] = useState('')
  const [categoryType, setCategoryType] = useState<number | undefined>(undefined)
  const [isSearch, setIsSearch] = useState(false)

  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [rows, setRows] = useState<CategoryRow[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  const [dialogVisible, setDialogVisible] = useState(false)
  const [action, setAction] = useState<'add' | 'edit'>('add')
  const [dialogType, setDialogType] = useState<1 | 2>(1)
  const [editingId, setEditingId] = useState('')
  const [formName, setFormName] = useState('')
  const [formSort, setFormSort] = useState('')

  const fetchList = async (opts?: {
    resetPage?: boolean
    markSearch?: boolean
    nameOverride?: string
    typeOverride?: number | undefined
  }) => {
    if (opts?.resetPage) setPage(1)
    if (opts?.markSearch !== undefined) setIsSearch(opts.markSearch)
    setLoading(true)
    try {
      const nameToUse = opts?.nameOverride ?? name
      const typeToUse = opts?.typeOverride ?? categoryType
      const res = await getCategoryPage({
        page: opts?.resetPage ? 1 : page,
        pageSize,
        name: nameToUse ? nameToUse : undefined,
        type: typeToUse,
      })
      if (String(res.data?.code) === '1') {
        setRows(res.data?.data?.records || [])
        setTotal(res.data?.data?.total || 0)
      } else {
        window.alert(res.data?.msg || '查询失败')
      }
    } catch (e: any) {
      if (isRequestCanceled(e)) return
      window.alert(`请求出错了：${e?.message || '未知错误'}`)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchList()
  }, [page, pageSize])

  const dialogTitle = useMemo(() => {
    if (action === 'edit') return dialogType === 1 ? '修改菜品分类' : '修改套餐分类'
    return dialogType === 1 ? '添加菜品分类' : '添加套餐分类'
  }, [action, dialogType])

  const validateForm = () => {
    const reg = new RegExp('^[A-Za-z\\u4e00-\\u9fa5]+$')
    if (!formName.trim()) return `${dialogTitle}不能为空`
    if (formName.trim().length < 2 || formName.trim().length > 20) return '分类名称输入不符，请输入2-20个字符'
    if (!reg.test(formName.trim())) return '分类名称包含特殊字符'
    if (formSort.trim() === '') return '排序不能为空'
    const regNum = /^\d+$/
    if (!regNum.test(formSort)) return '排序只能输入数字类型'
    if (Number(formSort) > 99) return '排序只能输入0-99数字'
    return ''
  }

  const openAdd = (type: 1 | 2) => {
    setAction('add')
    setDialogType(type)
    setEditingId('')
    setFormName('')
    setFormSort('')
    setDialogVisible(true)
  }

  const openEdit = (row: CategoryRow) => {
    setAction('edit')
    setDialogType(String(row.type) === '2' ? 2 : 1)
    setEditingId(row.id)
    setFormName(row.name)
    setFormSort(String(row.sort ?? ''))
    setDialogVisible(true)
  }

  const submit = async (mode?: 'go') => {
    const err = validateForm()
    if (err) {
      window.alert(err)
      return
    }

    const payload: any = { name: formName.trim(), sort: formSort, type: dialogType }
    if (action === 'add') {
      const res = await addCategory(payload)
      if (String(res.data?.code) === '1') {
        window.alert('分类添加成功！')
        await fetchList()
        if (mode === 'go') {
          setFormName('')
          setFormSort('')
        } else {
          setDialogVisible(false)
        }
        return
      }
      window.alert(res.data?.msg || '分类添加失败')
      return
    }

    const res = await editCategory({ ...payload, id: editingId })
    if (String(res.data?.code) === '1') {
      window.alert('分类修改成功！')
      await fetchList()
      setDialogVisible(false)
      return
    }
    window.alert(res.data?.msg || '分类修改失败')
  }

  return (
    <div className="dashboard-container">
      <div className="container">
        <div className="tableBar" style={{ display: 'inline-block', width: '100%' }}>
          <label style={{ marginRight: 10 }}>分类名称：</label>
          <ElInput
            value={name}
            placeholder="请填写分类名称"
            style={{ width: '15%' }}
            clearable
            onClear={() => {
              setName('')
              fetchList({ resetPage: true, nameOverride: '' })
            }}
            onChange={(e) => {
              const v = e.target.value
              setName(v)
              if (v === '') fetchList({ resetPage: true, nameOverride: '' })
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter') fetchList({ resetPage: true, markSearch: true })
            }}
          />

          <label style={{ marginRight: 5, marginLeft: 20 }}>分类类型：</label>
          <ElSelect<number>
            value={categoryType}
            options={[
              { value: 1, label: '菜品分类' },
              { value: 2, label: '套餐分类' },
            ]}
            clearable
            style={{ width: '15%' }}
            onChange={(v) => {
              setCategoryType(v)
              fetchList({ resetPage: true, typeOverride: v })
            }}
            onClear={() => {
              setCategoryType(undefined)
              fetchList({ resetPage: true, typeOverride: undefined })
            }}
          />

          <div style={{ float: 'right' }}>
            <ElButton elType="primary" className="continue" onClick={() => openAdd(1)}>
              + 新增菜品分类
            </ElButton>
            <ElButton elType="primary" style={{ marginLeft: 20 }} onClick={() => openAdd(2)}>
              + 新增套餐分类
            </ElButton>
          </div>

          <ElButton className="normal-btn continue" onClick={() => fetchList({ resetPage: true, markSearch: true })}>
            查询
          </ElButton>
        </div>

        <div className="el-table tableBox el-table--fit el-table--striped el-table--enable-row-hover">
          <div className="el-table__header-wrapper">
            <table className="el-table__header">
              <thead>
                <tr>
                  <th className="el-table__cell">
                    <div className="cell">分类名称</div>
                  </th>
                  <th className="el-table__cell">
                    <div className="cell">分类类型</div>
                  </th>
                  <th className="el-table__cell">
                    <div className="cell">排序</div>
                  </th>
                  <th className="el-table__cell">
                    <div className="cell">状态</div>
                  </th>
                  <th className="el-table__cell">
                    <div className="cell">操作时间</div>
                  </th>
                  <th className="el-table__cell is-center" style={{ width: 200 }}>
                    <div className="cell">操作</div>
                  </th>
                </tr>
              </thead>
            </table>
          </div>

          <div className="el-table__body-wrapper">
            <table className="el-table__body">
              <tbody>
                {rows.length ? (
                  rows.map((row) => {
                    const disabled = String(row.status) === '0'
                    const nextStatus = String(row.status) === '1' ? 0 : 1
                    return (
                      <tr key={row.id} className="el-table__row">
                        <td className="el-table__cell">
                          <div className="cell">{row.name}</div>
                        </td>
                        <td className="el-table__cell">
                          <div className="cell">{String(row.type) === '1' ? '菜品分类' : '套餐分类'}</div>
                        </td>
                        <td className="el-table__cell">
                          <div className="cell">{row.sort}</div>
                        </td>
                        <td className="el-table__cell">
                          <div className="cell">
                            <div className={`tableColumn-status ${disabled ? 'stop-use' : ''}`}>
                              {disabled ? '禁用' : '启用'}
                            </div>
                          </div>
                        </td>
                        <td className="el-table__cell">
                          <div className="cell">{row.updateTime}</div>
                        </td>
                        <td className="el-table__cell is-center">
                          <div className="cell">
                            <ElButton elType="text" size="small" className="blueBug" onClick={() => openEdit(row)}>
                              修改
                            </ElButton>
                            <ElButton
                              elType="text"
                              size="small"
                              className="delBut"
                              onClick={async () => {
                                const ok = window.confirm('确认删除该分类?')
                                if (!ok) return
                                const res = await deleteCategory(row.id)
                                if (String(res.data?.code) === '1') {
                                  window.alert('删除成功！')
                                  await fetchList()
                                  return
                                }
                                window.alert(res.data?.msg || '删除失败')
                              }}
                            >
                              删除
                            </ElButton>
                            <ElButton
                              elType="text"
                              size="small"
                              className={['non', disabled ? 'blueBug' : 'delBut'].join(' ')}
                              onClick={async () => {
                                const ok = window.confirm('确认调整该分类的状态?')
                                if (!ok) return
                                const res = await toggleCategoryStatus({ id: row.id, status: nextStatus })
                                if (String(res.data?.code) === '1' || String(res.status) === '200') {
                                  window.alert('状态更改成功！')
                                  await fetchList()
                                  return
                                }
                                window.alert(res.data?.msg || '状态更改失败')
                              }}
                            >
                              {String(row.status) === '1' ? '禁用' : '启用'}
                            </ElButton>
                          </div>
                        </td>
                      </tr>
                    )
                  })
                ) : (
                  <tr>
                    <td className="el-table__cell" colSpan={6}>
                      <div className="el-table__empty-block">
                        <span className="el-table__empty-text">
                          {loading ? '加载中...' : isSearch ? '未搜索到相关分类' : '暂无数据'}
                        </span>
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {total > 10 ? (
          <ElPagination
            className="pageList"
            page={page}
            pageSize={pageSize}
            total={total}
            onPageChange={(p) => setPage(p)}
            onPageSizeChange={(ps) => setPageSize(ps)}
          />
        ) : null}
      </div>

      <ElDialog
        open={dialogVisible}
        title={dialogTitle}
        width="30%"
        onClose={() => setDialogVisible(false)}
        footer={
          <span className="dialog-footer">
            <ElButton size="medium" onClick={() => setDialogVisible(false)}>
              取 消
            </ElButton>
            <ElButton elType="primary" size="medium" className={action === 'add' ? 'continue' : ''} onClick={() => submit()}>
              确 定
            </ElButton>
            {action !== 'edit' ? (
              <ElButton elType="primary" size="medium" onClick={() => submit('go')}>
                保存并继续添加
              </ElButton>
            ) : null}
          </span>
        }
      >
        <div className="el-form demo-form-inline">
          <div className="el-form-item">
            <label className="el-form-item__label" style={{ width: 100 }}>
              分类名称：
            </label>
            <div className="el-form-item__content" style={{ marginLeft: 100 }}>
              <ElInput value={formName} placeholder="请输入分类名称" maxLength={20} onChange={(e) => setFormName(e.target.value)} />
            </div>
          </div>
          <div className="el-form-item">
            <label className="el-form-item__label" style={{ width: 100 }}>
              排序：
            </label>
            <div className="el-form-item__content" style={{ marginLeft: 100 }}>
              <ElInput value={formSort} placeholder="请输入排序" onChange={(e) => setFormSort(e.target.value)} />
            </div>
          </div>
        </div>
      </ElDialog>
    </div>
  )
}
