import { Button, Card, Form, Input, Select, Space, Tag, Typography, message } from 'antd'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { addDish, editDish, getCategoryList, queryDishById } from '@/api/dish'
import { ImageUpload } from '@/components/ImageUpload/ImageUpload'
import { usePageTitle } from '@/lib/ui/usePageTitle'

type FlavorPreset = { name: string; value: string[] }
type DishFlavor = { name: string; value: string[] }

type DishForm = {
  name: string
  categoryId: string
  price: string
  image: string
  description?: string
}

export function DishAddPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const id = searchParams.get('id') || ''
  const actionType = id ? 'edit' : 'add'
  const title = actionType === 'add' ? '添加菜品' : '修改菜品'
  usePageTitle(`IntelliDining - ${title}`)

  const [form] = Form.useForm<DishForm>()
  const [loading, setLoading] = useState(false)
  const [categories, setCategories] = useState<Array<{ id: string; name: string }>>([])
  const [flavorPresets] = useState<FlavorPreset[]>([
    { name: '甜味', value: ['无糖', '少糖', '半糖', '多糖', '全糖'] },
    { name: '温度', value: ['热饮', '常温', '去冰', '少冰', '多冰'] },
    { name: '忌口', value: ['不要葱', '不要蒜', '不要香菜', '不要辣'] },
    { name: '辣度', value: ['不辣', '微辣', '中辣', '重辣'] },
  ])
  const [dishFlavors, setDishFlavors] = useState<DishFlavor[]>([])
  const [status, setStatus] = useState<boolean>(true)

  const leftFlavorOptions = useMemo(() => {
    const selected = new Set(dishFlavors.map((f) => f.name).filter(Boolean))
    return flavorPresets.filter((p) => !selected.has(p.name))
  }, [dishFlavors, flavorPresets])

  useEffect(() => {
    ;(async () => {
      const res = await getCategoryList({ type: 1 })
      if (String(res.data?.code) === '1') {
        setCategories(res.data?.data || [])
        return
      }
      message.error(res.data?.msg || '获取分类失败')
    })()
  }, [])

  useEffect(() => {
    if (!id) return
    ;(async () => {
      setLoading(true)
      try {
        const res = await queryDishById(id)
        if (String(res.data?.code) === '1') {
          const data = res.data?.data || {}
          form.setFieldsValue({
            name: data.name,
            categoryId: String(data.categoryId),
            price: String(data.price),
            image: data.image,
            description: data.description,
          })
          setStatus(String(data.status) === '1')
          const flavors: DishFlavor[] =
            data.flavors?.map((obj: any) => ({
              name: obj.name,
              value: Array.isArray(obj.value) ? obj.value : JSON.parse(obj.value || '[]'),
            })) || []
          setDishFlavors(flavors)
          return
        }
        message.error(res.data?.msg || '查询失败')
      } finally {
        setLoading(false)
      }
    })()
  }, [id, form])

  const addFlavor = () => {
    setDishFlavors((prev) => [...prev, { name: '', value: [] }])
  }

  const removeFlavor = (name: string) => {
    setDishFlavors((prev) => prev.filter((f) => f.name !== name))
  }

  const selectFlavor = (index: number, flavorName: string) => {
    const preset = flavorPresets.find((p) => p.name === flavorName)
    if (!preset) return
    setDishFlavors((prev) => {
      const next = [...prev]
      next[index] = { name: preset.name, value: [...preset.value] }
      return next
    })
  }

  const removeFlavorLabel = (index: number, labelIndex: number) => {
    setDishFlavors((prev) => {
      const next = [...prev]
      const item = next[index]
      if (!item) return prev
      next[index] = { ...item, value: item.value.filter((_, i) => i !== labelIndex) }
      return next
    })
  }

  const submit = async (mode: 'back' | 'continue') => {
    const values = await form.validateFields()
    if (!values.image) {
      message.error('菜品图片不能为空')
      return
    }
    const payload: any = {
      ...values,
      categoryId: values.categoryId,
      flavors: dishFlavors
        .filter((f) => f.name)
        .map((obj) => ({
          ...obj,
          value: JSON.stringify(obj.value),
        })),
      status: actionType === 'add' ? 0 : status ? 1 : 0,
    }

    setLoading(true)
    try {
      if (actionType === 'add') {
        const res = await addDish(payload)
        if (String(res.data?.code) === '1') {
          message.success('菜品添加成功！')
          if (mode === 'back') {
            navigate('/dish')
          } else {
            form.resetFields()
            setDishFlavors([])
            setStatus(true)
          }
          return
        }
        message.error(res.data?.desc || res.data?.msg || '菜品添加失败')
        return
      }

      const res = await editDish({ ...payload, id })
      if (String(res.data?.code) === '1') {
        message.success('菜品修改成功！')
        navigate('/dish')
        return
      }
      message.error(res.data?.desc || res.data?.msg || '菜品修改失败')
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

      <Form<DishForm>
        form={form}
        layout="horizontal"
        labelCol={{ style: { width: 140 } }}
        wrapperCol={{ span: 12 }}
      >
        <Form.Item
          label="菜品名称"
          name="name"
          rules={[
            { required: true, message: '请输入菜品名称' },
            {
              validator: async (_: any, value?: string) => {
                if (!value) return
                const reg = /^([A-Za-z0-9\u4e00-\u9fa5]){2,20}$/
                if (!reg.test(value)) throw new Error('菜品名称输入不符，请输入2-20个字符')
              },
            },
          ]}
        >
          <Input placeholder="请填写菜品名称" maxLength={20} />
        </Form.Item>

        <Form.Item
          label="菜品分类"
          name="categoryId"
          rules={[{ required: true, message: '请选择菜品分类' }]}
        >
          <Select
            placeholder="请选择菜品分类"
            options={categories.map((c) => ({ value: String(c.id), label: c.name }))}
          />
        </Form.Item>

        <Form.Item label="菜品价格" name="price" rules={[{ required: true, message: '请设置菜品价格' }]}>
          <Input placeholder="请设置菜品价格" />
        </Form.Item>

        <Form.Item label="口味做法配置">
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {dishFlavors.length === 0 ? (
              <Button type="link" onClick={addFlavor} style={{ padding: 0 }}>
                + 添加口味
              </Button>
            ) : (
              <>
                {dishFlavors.map((item, index) => (
                  <div
                    key={`${item.name}-${index}`}
                    style={{
                      border: '1px solid rgba(5, 5, 5, 0.08)',
                      borderRadius: 6,
                      padding: 12,
                    }}
                  >
                    <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                      <div style={{ width: 200 }}>
                        <Select
                          placeholder="请选择口味"
                          value={item.name || undefined}
                          options={[
                            ...(item.name ? [{ value: item.name, label: item.name }] : []),
                            ...leftFlavorOptions.map((o) => ({ value: o.name, label: o.name })),
                          ]}
                          onChange={(v) => selectFlavor(index, v)}
                        />
                      </div>
                      <Button type="link" danger onClick={() => removeFlavor(item.name)}>
                        删除
                      </Button>
                    </div>

                    <div style={{ marginTop: 10, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                      {(item.value || []).map((v, ind) => (
                        <Tag
                          key={`${v}-${ind}`}
                          closable
                          onClose={(e) => {
                            e.preventDefault()
                            removeFlavorLabel(index, ind)
                          }}
                        >
                          {v}
                        </Tag>
                      ))}
                    </div>
                  </div>
                ))}

                {leftFlavorOptions.length > 0 && dishFlavors.length < flavorPresets.length ? (
                  <Button type="link" onClick={addFlavor} style={{ padding: 0 }}>
                    添加口味
                  </Button>
                ) : null}
              </>
            )}
          </div>
        </Form.Item>

        <Form.Item
          label="菜品图片"
          name="image"
          rules={[{ required: true, message: '菜品图片不能为空' }]}
        >
          <ImageUpload>
            图片大小不超过2M
            <br />
            仅能上传 PNG JPEG JPG类型图片
            <br />
            建议上传200*200或300*300尺寸的图片
          </ImageUpload>
        </Form.Item>

        <Form.Item label="菜品描述" name="description" wrapperCol={{ span: 16 }}>
          <Input.TextArea rows={3} maxLength={200} placeholder="菜品描述，最长200字" />
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
    </Card>
  )
}
