import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { enableOrDisableEmployee, getEmployeeList } from '@/api/employee'
import { usePageTitle } from '@/lib/ui/usePageTitle'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { ElButton } from '@/components/legacy-vue/ElButton'
import { ElInput } from '@/components/legacy-vue/ElInput'
import { ElPagination } from '@/components/legacy-vue/ElPagination'

type EmployeeRow = {
  id: string
  name: string
  username: string
  phone: string
  status: number | string
  updateTime: string
}

export function EmployeePage() {
  usePageTitle('IntelliDining - 员工管理')
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [isSearch, setIsSearch] = useState(false)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [rows, setRows] = useState<EmployeeRow[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  const fetchList = async (opts?: {
    resetPage?: boolean
    markSearch?: boolean
    keywordOverride?: string
  }) => {
    if (opts?.resetPage) setPage(1)
    if (opts?.markSearch !== undefined) setIsSearch(opts.markSearch)
    setLoading(true)
    try {
      const keywordToUse = opts?.keywordOverride ?? keyword
      const res = await getEmployeeList({
        page: opts?.resetPage ? 1 : page,
        pageSize,
        name: keywordToUse ? keywordToUse : undefined,
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

  return (
    <div className="dashboard-container">
      <div className="container">
        <div className="tableBar">
          <label style={{ marginRight: 5 }}>员工姓名：</label>
          <ElInput
            value={keyword}
            placeholder="请输入员工姓名"
            style={{ width: '15%' }}
            clearable
            onClear={() => {
              setKeyword('')
              fetchList({ resetPage: true, keywordOverride: '' })
            }}
            onChange={(e) => {
              const v = e.target.value
              setKeyword(v)
              if (v === '') fetchList({ resetPage: true, keywordOverride: '' })
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter') fetchList({ resetPage: true, markSearch: true })
            }}
          />
          <ElButton className="normal-btn continue" onClick={() => fetchList({ resetPage: true, markSearch: true })}>
            查询
          </ElButton>
          <ElButton
            elType="primary"
            style={{ float: 'right' }}
            onClick={() => navigate('/employee/add')}
          >
            + 添加员工
          </ElButton>
        </div>

        <div className="el-table tableBox el-table--fit el-table--striped el-table--enable-row-hover">
          <div className="el-table__header-wrapper">
            <table className="el-table__header">
              <thead>
                <tr>
                  <th className="el-table__cell">
                    <div className="cell">员工姓名</div>
                  </th>
                  <th className="el-table__cell">
                    <div className="cell">账号</div>
                  </th>
                  <th className="el-table__cell">
                    <div className="cell">手机号</div>
                  </th>
                  <th className="el-table__cell">
                    <div className="cell">账号状态</div>
                  </th>
                  <th className="el-table__cell">
                    <div className="cell">最后操作时间</div>
                  </th>
                  <th className="el-table__cell is-center" style={{ width: 160 }}>
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
                    const isAdmin = row.username === 'admin'
                    const isDisabled = String(row.status) === '0'
                    const nextStatus = String(row.status) === '1' ? 0 : 1
                    return (
                      <tr key={row.id} className="el-table__row">
                        <td className="el-table__cell">
                          <div className="cell">{row.name}</div>
                        </td>
                        <td className="el-table__cell">
                          <div className="cell">{row.username}</div>
                        </td>
                        <td className="el-table__cell">
                          <div className="cell">{row.phone}</div>
                        </td>
                        <td className="el-table__cell">
                          <div className="cell">
                            <div className={`tableColumn-status ${isDisabled ? 'stop-use' : ''}`}>
                              {isDisabled ? '禁用' : '启用'}
                            </div>
                          </div>
                        </td>
                        <td className="el-table__cell">
                          <div className="cell">{row.updateTime}</div>
                        </td>
                        <td className="el-table__cell is-center">
                          <div className="cell">
                            <ElButton
                              elType="text"
                              size="small"
                              className={`blueBug ${isAdmin ? 'disabled-text' : ''}`}
                              disabled={isAdmin}
                              onClick={() => navigate(`/employee/add?id=${encodeURIComponent(row.id)}`)}
                            >
                              修改
                            </ElButton>
                            <ElButton
                              elType="text"
                              size="small"
                              className={[
                                'non',
                                isAdmin ? 'disabled-text' : '',
                                isDisabled ? 'blueBug' : 'delBut',
                              ]
                                .filter(Boolean)
                                .join(' ')}
                              disabled={isAdmin}
                              onClick={async () => {
                                const ok = window.confirm('确认调整该账号的状态?')
                                if (!ok) return
                                await enableOrDisableEmployee({ id: row.id, status: nextStatus })
                                window.alert('账号状态更改成功！')
                                await fetchList()
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
                          {loading ? '加载中...' : isSearch ? '未搜索到相关员工' : '暂无数据'}
                        </span>
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        <ElPagination
          className="pageList"
          page={page}
          pageSize={pageSize}
          total={total}
          onPageChange={(p) => setPage(p)}
          onPageSizeChange={(ps) => setPageSize(ps)}
        />
      </div>
    </div>
  )
}
