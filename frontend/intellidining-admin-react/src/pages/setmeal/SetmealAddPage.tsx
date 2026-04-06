import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  Space,
  Table,
  Tabs,
  Typography,
  message,
} from 'antd'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getCategoryList, queryDishList } from '@/api/dish'
import { addSetmeal, editSetmeal, querySetmealById } from '@/api/setMeal'
import { ImageUpload } from '@/components/ImageUpload/ImageUpload'
import { usePageTitle } from '@/lib/ui/usePageTitle'

type SetmealForm = {
  name: string
  idType: string
  price: string
  image: string
  description?: string
}

type DishItem = {
  dishId: string
  dishName: string
  name: string
  price: number
  status?: number | string
  copies: number
}

export function SetmealAddPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const id = searchParams.get('id') || ''
  const actionType = id ? 'edit' : 'add'
  const title = actionType === 'add' ? '添加套餐' : '修改套餐'
  usePageTitle(`IntelliDining - ${title}`)

  const [form] = Form.useForm<SetmealForm>()
  const [loading, setLoading] = useState(false)
  const [categories, setCategories] = useState<Array<{ id: string; name: string }>>([])

  const [dishTable, setDishTable] = useState<DishItem[]>([])
  const [dishDialogOpen, setDishDialogOpen] = useState(false)
  const [dishTypes, setDishTypes] = useState<Array<{ id: string; name: string }>>([])
  const [activeDishTypeId, setActiveDishTypeId] = useState<string>('')
  const [dishSearchInput, setDishSearchInput] = useState('')
  const [dishSearchKey, setDishSearchKey] = useState('')
  const [dishList, setDishList] = useState<any[]>([])
  const [dishSelectedNames, setDishSelectedNames] = useState<string[]>([])
  const [dishSelectedAll, setDishSelectedAll] = useState<any[]>([])

  useEffect(() => {
    ;(async () => {
      const res = await getCategoryList({ type: 2, page: 1, pageSize: 1000 })
      if (String(res.data?.code) === '1') {
        setCategories(res.data?.data || [])
        return
      }
      message.error(res.data?.msg || '获取套餐分类失败')
    })()
  }, [])

  useEffect(() => {
    ;(async () => {
      const res = await getCategoryList({ type: 1 })
      if (String(res.data?.code) === '1') {
        const list = res.data?.data || []
        setDishTypes(list)
        setActiveDishTypeId(list?.[0]?.id ? String(list[0].id) : '')
      }
    })()
  }, [])

  const fetchDishList = async (opts?: { categoryId?: string; name?: string }) => {
    const res = await queryDishList({
      categoryId: opts?.categoryId,
      name: opts?.name,
    })
    if (String(res.data?.code) !== '1') {
      message.error(res.data?.msg || '获取菜品失败')
      return
    }
    const newArr = (res.data?.data || []).map((n: any) => ({
      ...n,
      dishId: String(n.id),
      dishName: n.name,
      copies: 1,
    }))
    setDishList(newArr)
  }

  useEffect(() => {
    if (!dishDialogOpen) return
    if (dishSearchKey.trim()) {
      fetchDishList({ name: dishSearchKey.trim() })
      return
    }
    if (activeDishTypeId) fetchDishList({ categoryId: activeDishTypeId })
  }, [dishDialogOpen, activeDishTypeId, dishSearchKey])

  useEffect(() => {
    if (!id) return
    ;(async () => {
      setLoading(true)
      try {
        const res = await querySetmealById(id)
        if (String(res.data?.code) === '1') {
          const data = res.data?.data || {}
          form.setFieldsValue({
            name: data.name,
            idType: String(data.categoryId),
            price: String(data.price),
            image: data.image,
            description: data.description,
          })
          const dishes = (data.setmealDishes || []).map((d: any) => ({
            dishId: String(d.dishId),
            dishName: d.dishName || d.name,
            name: d.name,
            price: Number(d.price),
            copies: Number(d.copies || 1),
          }))
          setDishTable(dishes.reverse())
          return
        }
        message.error(res.data?.msg || '查询失败')
      } finally {
        setLoading(false)
      }
    })()
  }, [id, form])

  const dishColumns = useMemo(
    () => [
      { title: '名称', dataIndex: 'dishName', width: 220 },
      {
        title: '原价',
        dataIndex: 'price',
        width: 160,
        render: (p: number) => (Number(p) || 0).toFixed(2),
      },
      {
        title: '份数',
        dataIndex: 'copies',
        width: 180,
        render: (_: any, row: DishItem, index: number) => (
          <InputNumber
            min={1}
            max={99}
            value={row.copies}
            onChange={(v) => {
              setDishTable((prev) => {
                const next = [...prev]
                next[index] = { ...next[index], copies: Number(v || 1) }
                return next
              })
            }}
          />
        ),
      },
      {
        title: '操作',
        width: 120,
        render: (_: any, _row: DishItem, index: number) => (
          <Button
            type="link"
            danger
            onClick={() => {
              setDishTable((prev) => prev.filter((_, i) => i !== index))
            }}
          >
            删除
          </Button>
        ),
      },
    ],
    [],
  )

  const submit = async (mode: 'back' | 'continue') => {
    const values = await form.validateFields()
    if (dishTable.length === 0) {
      message.error('套餐下菜品不能为空')
      return
    }
    if (!values.image) {
      message.error('套餐图片不能为空')
      return
    }

    const payload: any = {
      name: values.name,
      price: values.price,
      image: values.image,
      description: values.description,
      categoryId: values.idType,
      setmealDishes: dishTable.map((obj) => ({
        copies: obj.copies,
        dishId: obj.dishId,
        name: obj.name || obj.dishName,
        price: obj.price,
      })),
      status: actionType === 'add' ? 0 : 1,
    }

    setLoading(true)
    try {
      if (actionType === 'add') {
        const res = await addSetmeal(payload)
        if (String(res.data?.code) === '1') {
          message.success('套餐添加成功！')
          if (mode === 'back') {
            navigate('/setmeal')
          } else {
            form.resetFields()
            setDishTable([])
          }
          return
        }
        message.error(res.data?.msg || '套餐添加失败')
        return
      }

      const res = await editSetmeal({ ...payload, id })
      if (String(res.data?.code) === '1') {
        message.success('套餐修改成功！')
        navigate('/setmeal')
        return
      }
      message.error(res.data?.msg || '套餐修改失败')
    } catch (e: any) {
      if (e?.errorFields) return
      message.error(e?.message || '提交失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card>
      <Typography.Title level={4} style={{ marginTop: 0 }}>
        {title}
      </Typography.Title>

      <Form<SetmealForm>
        form={form}
        layout="horizontal"
        labelCol={{ style: { width: 140 } }}
        wrapperCol={{ span: 12 }}
      >
        <Form.Item
          label="套餐名称"
          name="name"
          rules={[{ required: true, message: '请填写套餐名称' }]}
        >
          <Input placeholder="请填写套餐名称" maxLength={14} />
        </Form.Item>

        <Form.Item
          label="套餐分类"
          name="idType"
          rules={[{ required: true, message: '请选择套餐分类' }]}
        >
          <Select
            placeholder="请选择套餐分类"
            options={categories.map((c) => ({ value: String(c.id), label: c.name }))}
          />
        </Form.Item>

        <Form.Item
          label="套餐价格"
          name="price"
          rules={[
            {
              required: true,
              validator: async (_: any, value?: string) => {
                const reg = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/
                if (!value || !reg.test(value) || Number(value) <= 0) {
                  throw new Error('套餐价格格式有误，请输入大于零且最多保留两位小数的金额')
                }
              },
            },
          ]}
        >
          <Input placeholder="请设置套餐价格" />
        </Form.Item>

        <Form.Item label="套餐菜品" required>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {dishTable.length === 0 ? (
              <Button type="link" onClick={() => setDishDialogOpen(true)} style={{ padding: 0 }}>
                + 添加菜品
              </Button>
            ) : (
              <>
                <Button type="link" onClick={() => setDishDialogOpen(true)} style={{ padding: 0 }}>
                  + 添加菜品
                </Button>
                <Table<DishItem>
                  rowKey={(r) => `${r.dishId}-${r.name}`}
                  dataSource={dishTable}
                  columns={dishColumns as any}
                  pagination={false}
                  size="small"
                />
              </>
            )}
          </div>
        </Form.Item>

        <Form.Item
          label="套餐图片"
          name="image"
          rules={[{ required: true, message: '套餐图片不能为空' }]}
        >
          <ImageUpload>
            图片大小不超过2M
            <br />
            仅能上传 PNG JPEG JPG类型图片
            <br />
            建议上传200*200或300*300尺寸的图片
          </ImageUpload>
        </Form.Item>

        <Form.Item label="套餐描述" name="description" wrapperCol={{ span: 16 }}>
          <Input.TextArea rows={3} maxLength={200} placeholder="套餐描述，最长200字" />
        </Form.Item>

        <Form.Item wrapperCol={{ offset: 4 }}>
          <Space>
            <Button onClick={() => navigate(-1)}>取消</Button>
            <Button type="primary" loading={loading} onClick={() => submit('back')}>
              保存
            </Button>
            {actionType === 'add' ? (
              <Button type="primary" loading={loading} onClick={() => submit('continue')}>
                保存并继续添加
              </Button>
            ) : null}
          </Space>
        </Form.Item>
      </Form>

      <Modal
        open={dishDialogOpen}
        title="添加菜品"
        width="60%"
        onCancel={() => {
          setDishDialogOpen(false)
          setDishSelectedAll(JSON.parse(JSON.stringify(dishTable)))
          setDishSelectedNames(dishTable.map((d) => d.name))
          setDishSearchInput('')
          setDishSearchKey('')
        }}
        onOk={() => {
          const list = (dishSelectedAll.length ? dishSelectedAll : dishTable).map((d: any) => ({
            dishId: String(d.dishId || d.id),
            dishName: d.dishName || d.name,
            name: d.name,
            price: Number(d.price),
            copies: Number(d.copies || 1),
            status: d.status,
          }))
          setDishTable(list.map((n) => ({ ...n, copies: n.copies || 1 })))
          setDishDialogOpen(false)
          setDishSearchInput('')
          setDishSearchKey('')
        }}
        okText="添 加"
        cancelText="取 消"
      >
        <div style={{ display: 'flex', gap: 12, marginBottom: 12 }}>
          <Input
            value={dishSearchInput}
            placeholder="请输入菜品名称进行搜索"
            allowClear
            onChange={(e) => setDishSearchInput(e.target.value)}
            onPressEnter={() => setDishSearchKey(dishSearchInput)}
          />
          <Button onClick={() => setDishSearchKey(dishSearchInput)}>搜索</Button>
        </div>

        {dishSearchKey.trim() === '' ? (
          <Tabs
            activeKey={activeDishTypeId}
            onChange={(k) => setActiveDishTypeId(k)}
            items={dishTypes.map((t) => ({ key: String(t.id), label: t.name }))}
          />
        ) : null}

        <div style={{ display: 'flex', gap: 16 }}>
          <div style={{ flex: 1 }}>
            <Table
              rowKey={(r: any) => r.name}
              dataSource={dishList}
              size="small"
              pagination={false}
              rowSelection={{
                selectedRowKeys: dishSelectedNames,
                onChange: (keys, selectedRows) => {
                  setDishSelectedNames(keys as string[])
                  const selected = selectedRows.map((r: any) => ({
                    ...r,
                    dishId: String(r.dishId || r.id),
                    dishName: r.dishName || r.name,
                    copies: r.copies || 1,
                  }))
                  const merged = [...dishSelectedAll, ...selected]
                  const seen = new Set<string>()
                  const dedup = merged.filter((it: any) => {
                    if (seen.has(it.name)) return false
                    seen.add(it.name)
                    return true
                  })
                  const finalList = dedup.filter((it: any) => (keys as string[]).includes(it.name))
                  setDishSelectedAll(finalList)
                },
              }}
              columns={[
                { title: '名称', dataIndex: 'dishName', render: (_: any, r: any) => r.dishName || r.name },
                {
                  title: '状态',
                  dataIndex: 'status',
                  width: 90,
                  render: (s: any) => (String(s) === '0' ? '停售' : '在售'),
                },
                {
                  title: '价格',
                  dataIndex: 'price',
                  width: 120,
                  render: (p: any) => (Number(p) || 0).toFixed(2),
                },
              ]}
            />
          </div>

          <div style={{ width: 280 }}>
            <div style={{ marginBottom: 8 }}>已选菜品({dishSelectedAll.length})</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {dishSelectedAll.map((it: any) => (
                <div
                  key={it.name}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    gap: 8,
                    padding: 8,
                    border: '1px solid rgba(5, 5, 5, 0.08)',
                    borderRadius: 6,
                  }}
                >
                  <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {it.dishName || it.name}
                  </div>
                  <Button
                    type="link"
                    danger
                    onClick={() => {
                      setDishSelectedAll((prev) => prev.filter((x: any) => x.name !== it.name))
                      setDishSelectedNames((prev) => prev.filter((x) => x !== it.name))
                    }}
                  >
                    删除
                  </Button>
                </div>
              ))}
            </div>
          </div>
        </div>
      </Modal>
    </Card>
  )
}
