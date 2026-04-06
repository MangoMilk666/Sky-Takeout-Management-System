import { Button, Result } from 'antd'
import { Link } from 'react-router-dom'
import { usePageTitle } from '@/lib/ui/usePageTitle'

export function NotFoundPage() {
  usePageTitle('IntelliDining - 404')
  return (
    <Result
      status="404"
      title="404"
      subTitle="页面不存在"
      extra={
        <Button type="primary">
          <Link to="/">返回首页</Link>
        </Button>
      }
    />
  )
}

