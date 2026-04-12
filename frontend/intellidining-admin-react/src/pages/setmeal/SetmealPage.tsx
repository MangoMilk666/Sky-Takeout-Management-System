import { Button, Card, Input, Modal, Select, Space, Table, Tag, message } from 'antd'
import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getCategoryList } from '@/api/dish'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { deleteSetmeal, getSetmealPage, setmealStatusByStatus } from '@/api/setMeal'
import { usePageTitle } from '@/lib/ui/usePageTitle'

type SetmealRow = {
  id: string
  name: string
  image: string
  categoryName: string
  categoryId: string | number
  price: number
  status: number | string
  updateTime: string
}

export function SetmealPage() {
  usePageTitle('smart-dining智能点餐系统 - 套餐管理')
  const navigate = useNavigate()

  const [keyword, setKeyword] = useState('')
  const [categoryId, setCategoryId] = useState<string | undefined>(undefined)
  const [saleStatus, setSaleStatus] = useState<number | undefined>(undefined)
  const [isSearch, setIsSearch] = useState(false)

  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [rows, setRows] = useState<SetmealRow[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [selectedIds, setSelectedIds] = useState<string[]>([])
  const [categoryOptions, setCategoryOptions] = useState<Array<{ value: string; label: string }>>([])

  const fetchList = async (opts?: {
    resetPage?: boolean
    markSearch?: boolean
    keywordOverride?: string
    categoryIdOverride?: string | undefined
    statusOverride?: number | undefined
  }) => {
    if (opts?.resetPage) setPage(1)
    if (opts?.markSearch !== undefined) setIsSearch(opts.markSearch)
    setLoading(true)
    try {
      const res = await getSetmealPage({
        page: opts?.resetPage ? 1 : page,
        pageSize,
        name: (opts?.keywordOverride ?? keyword) || undefined,
        categoryId: (opts?.categoryIdOverride ?? categoryId) || undefined,
        status: opts?.statusOverride ?? saleStatus,
      })
      if (String(res.data?.code) === '1') {
        setRows(res.data?.data?.records || [])
        setTotal(Number(res.data?.data?.total || 0))
      } else {
        message.error(res.data?.msg || '查询失败')
      }
    } catch (e: any) {
      if (isRequestCanceled(e)) return
      message.error(`请求出错了：${e?.message || '未知错误'}`)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchList()
  }, [page, pageSize])

  useEffect(() => {
    ;(async () => {
      const res = await getCategoryList({ type: 2 })
      if (String(res.data?.code) === '1') {
        setCategoryOptions(
          (res.data?.data || []).map((item: any) => ({ value: String(item.id), label: item.name })),
        )
      }
    })()
  }, [])

  const columns = useMemo(
    () => [
      { title: '套餐名称', dataIndex: 'name' },
      {
        title: '图片',
        dataIndex: 'image',
        render: (src: string) => (
          <img
            src={src}
            alt=""
            style={{ width: 80, height: 40, objectFit: 'cover', borderRadius: 4 }}
          />
        ),
      },
      { title: '套餐分类', dataIndex: 'categoryName' },
      {
        title: '套餐价',
        dataIndex: 'price',
        render: (p: number) => <span>￥{(Number(p) || 0).toFixed(2)}</span>,
      },
      {
        title: '售卖状态',
        dataIndex: 'status',
        render: (s: SetmealRow['status']) => {
          const stopped = String(s) === '0'
          return <Tag color={stopped ? 'default' : 'green'}>{stopped ? '停售' : '启售'}</Tag>
        },
      },
      { title: '最后操作时间', dataIndex: 'updateTime' },
      {
        title: '操作',
        key: 'actions',
        width: 240,
        render: (_: any, row: SetmealRow) => {
          const nextStatus = String(row.status) === '0' ? '1' : '0'
          return (
            <Space>
              <Button
                type="link"
                onClick={() => navigate(`/setmeal/add?id=${encodeURIComponent(row.id)}`)}
              >
                修改
              </Button>
              <Button
                type="link"
                danger
                onClick={() => {
                  Modal.confirm({
                    title: '确定删除',
                    content: '确认删除该套餐, 是否继续?',
                    okText: '删除',
                    cancelText: '取消',
                    onOk: async () => {
                      const res = await deleteSetmeal(row.id)
                      if (String(res.data?.code) === '1') {
                        message.success('删除成功！')
                        await fetchList()
                        return
                      }
                      message.error(res.data?.msg || '删除失败')
                    },
                  })
                }}
              >
                删除
              </Button>
              <Button
                type="link"
                onClick={() => {
                  Modal.confirm({
                    title: '提示',
                    content: '确认更改该套餐状态?',
                    okText: '确定',
                    cancelText: '取消',
                    onOk: async () => {
                      const res = await setmealStatusByStatus({ ids: row.id, status: nextStatus })
                      if (String(res.data?.code) === '1') {
                        message.success('套餐状态已经更改成功！')
                        await fetchList()
                        return
                      }
                      message.error(res.data?.msg || '状态更改失败')
                    },
                  })
                }}
              >
                {String(row.status) === '0' ? '启售' : '停售'}
              </Button>
            </Space>
          )
        },
      },
    ],
    [navigate, categoryId, saleStatus, keyword, page, pageSize],
  )

  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, marginBottom: 16 }}>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
          <Space>
            <span>套餐名称：</span>
            <Input
              value={keyword}
              placeholder="请填写套餐名称"
              style={{ width: 200 }}
              allowClear
              onChange={(e) => {
                const v = e.target.value
                setKeyword(v)
                if (v === '') fetchList({ resetPage: true, keywordOverride: '' })
              }}
              onPressEnter={() => fetchList({ resetPage: true, markSearch: true })}
            />
          </Space>

          <Space>
            <span>套餐分类：</span>
            <Select
              allowClear
              placeholder="请选择"
              style={{ width: 180 }}
              value={categoryId}
              options={categoryOptions}
              onChange={(v) => {
                setCategoryId(v)
                fetchList({ resetPage: true, categoryIdOverride: v })
              }}
            />
          </Space>

          <Space>
            <span>售卖状态：</span>
            <Select
              allowClear
              placeholder="请选择"
              style={{ width: 140 }}
              value={saleStatus}
              options={[
                { value: 0, label: '停售' },
                { value: 1, label: '启售' },
              ]}
              onChange={(v) => {
                setSaleStatus(v)
                fetchList({ resetPage: true, statusOverride: v })
              }}
            />
          </Space>

          <Button onClick={() => fetchList({ resetPage: true, markSearch: true })}>查询</Button>
        </div>

        <Space>
          <Button
            danger
            disabled={selectedIds.length === 0}
            onClick={() => {
              if (selectedIds.length === 0) return
              Modal.confirm({
                title: '确定删除',
                content: '确认删除所选套餐, 是否继续?',
                okText: '删除',
                cancelText: '取消',
                onOk: async () => {
                  const res = await deleteSetmeal(selectedIds.join(','))
                  if (String(res.data?.code) === '1') {
                    message.success('删除成功！')
                    setSelectedIds([])
                    await fetchList()
                    return
                  }
                  message.error(res.data?.msg || '删除失败')
                },
              })
            }}
          >
            批量删除
          </Button>
          <Button type="primary">
            <Link to="/setmeal/add">+ 新建套餐</Link>
          </Button>
        </Space>
      </div>

      <Table<SetmealRow>
        rowKey="id"
        loading={loading}
        dataSource={rows}
        columns={columns as any}
        rowSelection={{
          selectedRowKeys: selectedIds,
          onChange: (keys) => setSelectedIds(keys as string[]),
        }}
        pagination={
          total > 10
            ? {
                current: page,
                pageSize,
                total,
                showSizeChanger: true,
                onChange: (p, ps) => {
                  setPage(p)
                  setPageSize(ps)
                },
              }
            : false
        }
        locale={{
          emptyText: isSearch ? '未搜索到相关套餐' : '暂无数据',
        }}
      />
    </Card>
  )
}
