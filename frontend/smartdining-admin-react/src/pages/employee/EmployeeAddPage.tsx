import { Button, Card, Form, Input, Radio, Space, message } from 'antd'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { addEmployee, editEmployee, queryEmployeeById } from '@/api/employee'
import { usePageTitle } from '@/lib/ui/usePageTitle'

type EmployeeForm = {
  username: string
  name: string
  phone: string
  sex: '男' | '女'
  idNumber: string
}

export function EmployeeAddPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const id = searchParams.get('id') || ''
  const actionType = id ? 'edit' : 'add'
  const title = actionType === 'add' ? '添加员工' : '修改员工信息'
  usePageTitle(`smart-dining智能点餐系统 - ${title}`)

  const [form] = Form.useForm<EmployeeForm>()
  const [loading, setLoading] = useState(false)

  const rules = useMemo(
    () => ({
      username: [
        { required: true, message: '请输入账号' },
        {
          validator: async (_: any, value?: string) => {
            if (!value) return
            const reg = /^([a-z]|[0-9]){3,20}$/
            if (!reg.test(value)) throw new Error('账号输入不符，请输入3-20个字符')
          },
        },
      ],
      name: [{ required: true, message: '请输入员工姓名' }],
      phone: [
        { required: true, message: '请输入手机号' },
        {
          validator: async (_: any, value?: string) => {
            if (!value) return
            const reg = /^1(3|4|5|6|7|8)\d{9}$/
            if (!reg.test(value)) throw new Error('请输入正确的手机号!')
          },
        },
      ],
      idNumber: [
        { required: true, message: '请输入身份证号码' },
        {
          validator: async (_: any, value?: string) => {
            if (!value) return
            const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
            if (!reg.test(value)) throw new Error('身份证号码不正确')
          },
        },
      ],
    }),
    [],
  )

  useEffect(() => {
    if (!id) {
      form.setFieldsValue({ sex: '男' } as any)
      return
    }
    ;(async () => {
      setLoading(true)
      try {
        const res = await queryEmployeeById(id)
        if (String(res.data?.code) === '1') {
          const data = res.data?.data || {}
          form.setFieldsValue({
            username: data.username,
            name: data.name,
            phone: data.phone,
            idNumber: data.idNumber,
            sex: String(data.sex) === '0' ? '女' : '男',
          })
        } else {
          message.error(res.data?.msg || '查询失败')
        }
      } finally {
        setLoading(false)
      }
    })()
  }, [id, form])

  const submit = async (mode: 'back' | 'continue') => {
    const values = await form.validateFields()
    setLoading(true)
    try {
      const payload: any = {
        ...values,
        sex: values.sex === '女' ? '0' : '1',
      }
      if (actionType === 'add') {
        const res = await addEmployee(payload)
        if (String(res.data?.code) === '1') {
          message.success('员工添加成功！')
          if (mode === 'back') {
            navigate('/employee')
          } else {
            form.resetFields()
            form.setFieldsValue({ sex: '男' } as any)
          }
          return
        }
        message.error(res.data?.msg || '员工添加失败')
        return
      }

      const res = await editEmployee({ ...payload, id })
      if (String(res.data?.code) === '1') {
        message.success('员工信息修改成功！')
        navigate('/employee')
        return
      }
      message.error(res.data?.msg || '员工信息修改失败')
    } catch (e: any) {
      if (e?.errorFields) return
      message.error(e?.message || '提交失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card>
      <Form<EmployeeForm>
        form={form}
        layout="horizontal"
        labelCol={{ style: { width: 140 } }}
        wrapperCol={{ span: 10 }}
        initialValues={{ sex: '男' }}
      >
        <Form.Item label="账号" name="username" rules={rules.username}>
          <Input placeholder="请输入账号" maxLength={20} disabled={actionType === 'edit'} />
        </Form.Item>
        <Form.Item label="员工姓名" name="name" rules={rules.name}>
          <Input placeholder="请输入员工姓名" maxLength={12} />
        </Form.Item>
        <Form.Item label="手机号" name="phone" rules={rules.phone}>
          <Input placeholder="请输入手机号" maxLength={11} />
        </Form.Item>
        <Form.Item label="性别" name="sex">
          <Radio.Group>
            <Radio value="男">男</Radio>
            <Radio value="女">女</Radio>
          </Radio.Group>
        </Form.Item>
        <Form.Item label="身份证号" name="idNumber" rules={rules.idNumber}>
          <Input placeholder="请输入身份证号" maxLength={20} />
        </Form.Item>

        <Form.Item wrapperCol={{ offset: 4 }}>
          <Space>
            <Button onClick={() => navigate('/employee')}>取消</Button>
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
