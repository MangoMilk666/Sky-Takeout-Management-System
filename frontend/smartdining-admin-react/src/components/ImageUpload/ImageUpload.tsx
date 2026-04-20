import type { ChangeEvent, ReactNode } from 'react'
import { useRef, useState } from 'react'
import { http } from '@/lib/http/request'
import { message } from '@/lib/ui/message'

type Props = {
  value?: string
  onChange?: (value: string) => void
  accept?: string
  maxSizeMb?: number
  children?: ReactNode
}

export function ImageUpload({
  value,
  onChange,
  accept = '.jpg,.jpeg,.png',
  maxSizeMb = 2,
  children,
}: Props) {
  const inputRef = useRef<HTMLInputElement | null>(null)
  const [uploading, setUploading] = useState(false)

  const upload = async (file: File) => {
    const ok = file.size / 1024 / 1024 < maxSizeMb
    if (!ok) {
      message.error(`上传文件大小不能超过${maxSizeMb}M!`)
      return
    }

    const form = new FormData()
    form.append('file', file)

    setUploading(true)
    try {
      const res = await http({
        url: '/common/upload',
        method: 'post',
        data: form,
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      const data: any = res.data
      const next = String(data?.data || data?.url || data?.data?.url || '')
      if (!next) {
        message.error(data?.msg || '图片上传失败')
        return
      }
      onChange?.(next)
      message.success('图片上传成功')
    } catch (e: any) {
      message.error(e?.message || '图片上传失败')
    } finally {
      setUploading(false)
      if (inputRef.current) inputRef.current.value = ''
    }
  }

  const onPick = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    await upload(file)
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
      <input ref={inputRef} type="file" accept={accept} style={{ display: 'none' }} onChange={onPick} />

      <div
        role="button"
        tabIndex={0}
        onClick={() => {
          if (uploading) return
          inputRef.current?.click()
        }}
        onKeyDown={(e) => {
          if (uploading) return
          if (e.key === 'Enter' || e.key === ' ') inputRef.current?.click()
        }}
        style={{
          width: 200,
          height: 160,
          borderRadius: 6,
          overflow: 'hidden',
          border: value ? '1px solid rgba(5, 5, 5, 0.15)' : '1px dashed rgba(5, 5, 5, 0.25)',
          cursor: uploading ? 'not-allowed' : 'pointer',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'rgba(0,0,0,0.45)',
          position: 'relative',
        }}
      >
        {value ? (
          <img src={value} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        ) : (
          <span>{uploading ? '上传中...' : '点击上传'}</span>
        )}
      </div>

      <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)', lineHeight: '18px' }}>{children}</div>
    </div>
  )
}
